package de.ugdata.mousesharett.service;

import de.ugdata.mousesharett.model.InputMessage;
import org.springframework.stereotype.Service;

import de.ugdata.mousesharett.model.HeartbeatMessage;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.awt.*;
import java.awt.event.InputEvent;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

@Service
public class WorkerInputService {

    private Robot robot;
    private StompSession stompSession;
    private final String workerId = UUID.randomUUID().toString();
    private String hostname;

    @Value("${mouseshare.role}")
    private String role;

    @Value("${mouseshare.master-host:localhost}")
    private String masterHost;

    @Value("${server.port:8080}")
    private int port;

    @PostConstruct
    public void init() {
        if (!"worker".equals(role)) return;
        try {
            this.robot = new Robot();
            this.hostname = InetAddress.getLocalHost().getHostName();
            connectToMaster();
        } catch (AWTException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void connectToMaster() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://" + masterHost + ":" + port + "/ws-mouse";
        System.out.println("Attempting to connect to Master at: " + url);
        stompClient.connect(url, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                stompSession = session;
                System.out.println("Successfully connected to Master at " + url);
                stompSession.subscribe("/topic/inputs", new StompFrameHandler() {
                    @Override
                    public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                        return InputMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        handleInput((InputMessage) payload);
                    }
                });
            }
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("STOMP Error: " + exception.getMessage());
                exception.printStackTrace();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("Transport Error connecting to Master: " + exception.getMessage());
                if (exception.getMessage().contains("Connection refused")) {
                    System.err.println("HINT: Ensure the Master is running and 'mouseshare.master-host' is set to the Master's IP address (currently: " + masterHost + ")");
                }
            }
        });
    }

    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {
        if ("worker".equals(role) && stompSession != null && stompSession.isConnected()) {
            stompSession.send("/app/heartbeat", new HeartbeatMessage(workerId, hostname));
        }
    }

    public void handleInput(InputMessage msg) {
        if (robot == null || !"worker".equals(role)) return;
        
        switch (msg.getType()) {
            case MOUSE_MOVE:
                robot.mouseMove(msg.getX(), msg.getY());
                break;
            case MOUSE_PRESS:
                robot.mousePress(getAwtButton(msg.getButton()));
                break;
            case MOUSE_RELEASE:
                robot.mouseRelease(getAwtButton(msg.getButton()));
                break;
            case KEY_PRESS:
                robot.keyPress(msg.getKeyCode());
                break;
            case KEY_RELEASE:
                robot.keyRelease(msg.getKeyCode());
                break;
        }
    }

    private int getAwtButton(int nativeButton) {
        switch (nativeButton) {
            case 1: return InputEvent.BUTTON1_DOWN_MASK;
            case 2: return InputEvent.BUTTON2_DOWN_MASK;
            case 3: return InputEvent.BUTTON3_DOWN_MASK;
            default: return InputEvent.BUTTON1_DOWN_MASK;
        }
    }
}
