package com.xclusive.barber.dto.chatbot;

import java.util.List;

public record ChatRequest(String userMessage, List<ChatMessage> conversationHistory) {}
