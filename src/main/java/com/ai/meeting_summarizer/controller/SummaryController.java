package com.ai.meeting_summarizer.controller;

import com.ai.meeting_summarizer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://internship-assignment-mangodesk-san-one.vercel.app/"
})


@RestController
public class SummaryController {

    @Value("${GROQ_API_KEY}")
    private String openaiApiKey;

    @Value("${groq.model}")
    private String groqModel;

    @Autowired
    private EmailService emailService;

    @PostMapping("/summarize")
    public ResponseEntity<String> summarize(@RequestBody Map<String, String> request) {
        String transcript = request.get("transcript");
        String prompt = request.get("prompt");

        String fullPrompt = prompt + "\n\nTranscript:\n" + transcript;

        RestTemplate restTemplate = new RestTemplate();
        String groqEndpoint = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey); // use Groq API key here
        headers.set("Content-Type", "application/json");

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", groqModel);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant."),
                Map.of("role", "user", "content", fullPrompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String response = restTemplate.postForObject(groqEndpoint, entity, String.class);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, Object> request) {
        Object toField = request.get("to");
        String subject = (String) request.get("subject");
        String body = (String) request.get("body");

        if (toField instanceof String to) {
            emailService.sendEmail(to, subject, body);
        } else if (toField instanceof List<?> toList) {
            String[] recipients = toList.toArray(new String[0]);
            emailService.sendEmail(recipients, subject, body);
        }

        return ResponseEntity.ok("Email sent successfully");
    }
}

