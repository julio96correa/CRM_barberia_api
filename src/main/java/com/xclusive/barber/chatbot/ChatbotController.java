package com.xclusive.barber.chatbot;

import com.xclusive.barber.dto.ApiResponse;
import com.xclusive.barber.dto.appointment.AppointmentResponse;
import com.xclusive.barber.dto.chatbot.ChatConfirmRequest;
import com.xclusive.barber.dto.chatbot.ChatRequest;
import com.xclusive.barber.dto.chatbot.ChatResponse;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.entity.User;
import com.xclusive.barber.enums.ClientTier;
import com.xclusive.barber.enums.Role;
import com.xclusive.barber.exception.InvalidOperationException;
import com.xclusive.barber.repository.AppointmentRepository;
import com.xclusive.barber.repository.ClientProfileRepository;
import com.xclusive.barber.repository.UserRepository;
import com.xclusive.barber.service.AppointmentService;
import com.xclusive.barber.dto.appointment.AppointmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "Gemini AI chatbot for appointment scheduling")
public class ChatbotController {

    private final GeminiService geminiService;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Send a message to the chatbot")
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatResponse>> message(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("OK", geminiService.sendMessage(request)));
    }

    @Operation(summary = "Confirm an appointment suggested by the chatbot")
    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirm(@RequestBody ChatConfirmRequest request) {
        if (appointmentRepository.existsByBarberProfileIdAndAppointmentDateAndStartHour(
                request.barberId(), request.appointmentDate(), request.startHour())) {
            throw new InvalidOperationException("Slot no disponible, por favor elige otro horario");
        }

        ClientProfile client = clientProfileRepository.findByPhone(request.clientPhone())
                .orElseGet(() -> createGuestClient(request.clientName(), request.clientPhone()));

        AppointmentRequest apptRequest = new AppointmentRequest(
                client.getId(),
                request.barberId(),
                request.serviceId(),
                request.appointmentDate(),
                request.startHour()
        );

        AppointmentResponse response = appointmentService.createAppointment(apptRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Cita confirmada", response));
    }

    private ClientProfile createGuestClient(String name, String phone) {
        String syntheticEmail = phone + "@guest.xclusivebarber.com";
        User user = User.builder()
                .email(syntheticEmail)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.CLIENT)
                .active(true)
                .build();
        user = userRepository.save(user);

        return clientProfileRepository.save(ClientProfile.builder()
                .user(user)
                .phone(phone)
                .notes("Cliente registrado via chatbot. Nombre: " + name)
                .loyaltyPoints(0)
                .tier(ClientTier.NEW)
                .build());
    }
}
