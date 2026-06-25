package de.ugdata.mousesharett.controller;

import de.ugdata.mousesharett.model.InputMessage;
import de.ugdata.mousesharett.service.WorkerInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import de.ugdata.mousesharett.model.HeartbeatMessage;
import de.ugdata.mousesharett.service.WorkerRegistry;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

@Controller
public class InputController {

    @Autowired
    private WorkerInputService workerInputService;

    @Autowired
    private WorkerRegistry workerRegistry;

    @MessageMapping("/input")
    public void receiveInput(InputMessage message) {
        workerInputService.handleInput(message);
    }

    @MessageMapping("/heartbeat")
    public void receiveHeartbeat(@Payload HeartbeatMessage heartbeat, SimpMessageHeaderAccessor headerAccessor) {
        // In a real app we'd get the IP from the session/header if needed
        String ip = "unknown"; 
        workerRegistry.register(heartbeat.getId(), heartbeat.getHostname(), ip);
    }
}
