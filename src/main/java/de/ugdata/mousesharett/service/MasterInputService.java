package de.ugdata.mousesharett.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import de.ugdata.mousesharett.model.InputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.springframework.beans.factory.annotation.Value;

@Service
public class MasterInputService implements NativeMouseInputListener, NativeKeyListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${mouseshare.role}")
    private String role;

    private boolean capturing = false;

    @PostConstruct
    public void init() {
        if (!"master".equals(role)) {
            return;
        }
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeMouseListener(this);
            GlobalScreen.addNativeMouseMotionListener(this);
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (!"master".equals(role)) return;
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        // Logic to decide if we should send to worker
        // For now, let's just send if we are in master role
        if ("master".equals(role)) {
            messagingTemplate.convertAndSend("/topic/inputs", InputMessage.move(e.getX(), e.getY()));
        }
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        nativeMouseMoved(e);
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        InputMessage msg = new InputMessage();
        msg.setType(InputMessage.Type.MOUSE_PRESS);
        msg.setButton(e.getButton());
        messagingTemplate.convertAndSend("/topic/inputs", msg);
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        InputMessage msg = new InputMessage();
        msg.setType(InputMessage.Type.MOUSE_RELEASE);
        msg.setButton(e.getButton());
        messagingTemplate.convertAndSend("/topic/inputs", msg);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        InputMessage msg = new InputMessage();
        msg.setType(InputMessage.Type.KEY_PRESS);
        msg.setKeyCode(e.getKeyCode());
        messagingTemplate.convertAndSend("/topic/inputs", msg);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        InputMessage msg = new InputMessage();
        msg.setType(InputMessage.Type.KEY_RELEASE);
        msg.setKeyCode(e.getKeyCode());
        messagingTemplate.convertAndSend("/topic/inputs", msg);
    }
}
