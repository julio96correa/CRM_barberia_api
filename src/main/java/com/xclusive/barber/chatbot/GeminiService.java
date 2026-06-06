package com.xclusive.barber.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xclusive.barber.dto.chatbot.AppointmentSuggestion;
import com.xclusive.barber.dto.chatbot.ChatMessage;
import com.xclusive.barber.dto.chatbot.ChatRequest;
import com.xclusive.barber.dto.chatbot.ChatResponse;
import com.xclusive.barber.entity.BarberProfile;
import com.xclusive.barber.entity.Service;
import com.xclusive.barber.repository.BarberProfileRepository;
import com.xclusive.barber.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String FALLBACK_MESSAGE =
            "Lo sentimos, en este momento no podemos procesar tu solicitud. Contáctanos al número de WhatsApp para agendar tu cita.";

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ServiceRepository serviceRepository;
    private final BarberProfileRepository barberProfileRepository;
    private final ObjectMapper objectMapper;

    public ChatResponse sendMessage(ChatRequest request) {
        try {
            String url = GEMINI_URL + "?key=" + apiKey;

            ObjectNode payload = objectMapper.createObjectNode();

            // System instruction
            ObjectNode systemInstruction = objectMapper.createObjectNode();
            ObjectNode systemPart = objectMapper.createObjectNode();
            systemPart.put("text", buildSystemPrompt());
            systemInstruction.set("parts", objectMapper.createArrayNode().add(systemPart));
            payload.set("systemInstruction", systemInstruction);

            // Contents: history + new user message
            ArrayNode contents = objectMapper.createArrayNode();
            if (request.conversationHistory() != null) {
                for (ChatMessage msg : request.conversationHistory()) {
                    ObjectNode contentNode = objectMapper.createObjectNode();
                    contentNode.put("role", msg.role());
                    ArrayNode parts = objectMapper.createArrayNode();
                    ObjectNode part = objectMapper.createObjectNode();
                    part.put("text", msg.content());
                    parts.add(part);
                    contentNode.set("parts", parts);
                    contents.add(contentNode);
                }
            }

            ObjectNode userContent = objectMapper.createObjectNode();
            userContent.put("role", "user");
            ArrayNode userParts = objectMapper.createArrayNode();
            ObjectNode userPart = objectMapper.createObjectNode();
            userPart.put("text", request.userMessage());
            userParts.add(userPart);
            userContent.set("parts", userParts);
            contents.add(userContent);

            payload.set("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);

            String text = response.getBody()
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            AppointmentSuggestion suggestion = parseAppointmentSuggestion(text);

            return ChatResponse.builder()
                    .aiResponse(text)
                    .appointmentSuggestion(suggestion)
                    .build();

        } catch (Exception e) {
            log.error("Gemini API error", e);
            return ChatResponse.builder()
                    .aiResponse(FALLBACK_MESSAGE)
                    .build();
        }
    }

    private String buildSystemPrompt() {
        List<Service> services = serviceRepository.findAllByActiveTrue();
        List<BarberProfile> barbers = barberProfileRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("Eres el asistente virtual de Xclusive Barber. Ayudas a los clientes a agendar citas.\n\n");
        sb.append("SERVICIOS DISPONIBLES:\n");
        services.forEach(s -> sb.append(String.format("- ID:%d %s (puntos: %d)\n", s.getId(), s.getName(), s.getPointsValue())));
        sb.append("\nBARBEROS DISPONIBLES:\n");
        barbers.forEach(b -> sb.append(String.format("- ID:%d %s (especialidad: %s)\n",
                b.getId(), b.getUser().getEmail(), b.getSpecialty())));
        sb.append("\nCuando el cliente quiera agendar una cita, recopila: barbero, servicio, fecha, hora y nombre+teléfono del cliente.");
        sb.append("\nCuando tengas todos los datos, responde con un bloque JSON así:\n");
        sb.append("```json\n{\"appointmentSuggestion\":{\"barberId\":1,\"barberName\":\"...\",\"date\":\"YYYY-MM-DD\",\"startHour\":10,\"serviceId\":1,\"serviceName\":\"...\",\"clientName\":\"...\",\"clientPhone\":\"...\"}}\n```");

        return sb.toString();
    }

    private AppointmentSuggestion parseAppointmentSuggestion(String text) {
        try {
            int jsonStart = text.indexOf("```json");
            if (jsonStart == -1) return null;
            int contentStart = text.indexOf('\n', jsonStart) + 1;
            int jsonEnd = text.indexOf("```", contentStart);
            if (jsonEnd == -1) return null;

            String json = text.substring(contentStart, jsonEnd).trim();
            JsonNode root = objectMapper.readTree(json);
            JsonNode s = root.path("appointmentSuggestion");
            if (s.isMissingNode()) return null;

            return AppointmentSuggestion.builder()
                    .barberId(s.path("barberId").asLong())
                    .barberName(s.path("barberName").asText())
                    .date(LocalDate.parse(s.path("date").asText()))
                    .startHour(s.path("startHour").asInt())
                    .serviceId(s.path("serviceId").asLong())
                    .serviceName(s.path("serviceName").asText())
                    .clientName(s.path("clientName").asText())
                    .clientPhone(s.path("clientPhone").asText())
                    .build();
        } catch (Exception e) {
            log.warn("Could not parse appointment suggestion from Gemini response");
            return null;
        }
    }
}
