package com.xuenai.medical.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xuenai.medical.auth.CurrentUser;
import com.xuenai.medical.auth.JwtService;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.model.entity.ChatMessage;
import com.xuenai.medical.model.vo.ChatMessageVO;
import com.xuenai.medical.service.ChatMessageService;
import com.xuenai.medical.service.ConsultationSessionService;
import com.xuenai.medical.service.ProfileResolveService;
import com.xuenai.medical.mapper.ConsultationSessionMapper;
import com.xuenai.medical.model.entity.ConsultationSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问诊聊天 WebSocket 服务端点
 */
@Slf4j
@Component
@ServerEndpoint("/ws")
public class ChatEndpoint {

    private static JwtService jwtService;
    private static ChatMessageService chatMessageService;
    private static ConsultationSessionService consultationSessionService;
    private static ProfileResolveService profileResolveService;
    private static ConsultationSessionMapper consultationSessionMapper;
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // 存储当前在线用户的 Session (Key: userId)
    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    @Autowired
    public void setDependencies(JwtService jwtService, 
                                ChatMessageService chatMessageService,
                                ConsultationSessionService consultationSessionService,
                                ProfileResolveService profileResolveService,
                                ConsultationSessionMapper consultationSessionMapper) {
        ChatEndpoint.jwtService = jwtService;
        ChatEndpoint.chatMessageService = chatMessageService;
        ChatEndpoint.consultationSessionService = consultationSessionService;
        ChatEndpoint.profileResolveService = profileResolveService;
        ChatEndpoint.consultationSessionMapper = consultationSessionMapper;
    }

    @OnOpen
    public void onOpen(Session session) {
        try {
            Long userId = extractUserIdFromSession(session);
            if (userId == null) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Auth failed"));
                return;
            }
            onlineUsers.put(userId, session);
            log.info("User connected: {}", userId);
        } catch (Exception e) {
            log.error("WebSocket onOpen error", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = extractUserIdFromSessionSafe(session);
        if (userId != null) {
            onlineUsers.remove(userId);
            log.info("User disconnected: {}", userId);
        }
    }

    @OnMessage
    public void onMessage(String messageStr, Session session) {
        Long senderId = extractUserIdFromSessionSafe(session);
        if (senderId == null) return;

        try {
            ChatMessage chatMessage = objectMapper.readValue(messageStr, ChatMessage.class);
            chatMessage.setSenderId(senderId);

            // 1. 保存到数据库
            ChatMessage savedMsg = chatMessageService.saveMessage(chatMessage);
            
            // 转换为 VO 以包含发送者信息(头像、姓名等)
            ChatMessageVO msgVO = chatMessageService.convertToVO(savedMsg);

            // 2. 更新已使用时间 (如果是文本消息等, 简单起见每次发消息默认扣减时间，或由前端定时触发)
            // 简单实现：我们在这里只保存消息，时长的更新可以通过单独的心跳或接口来处理

            // 3. 推送给接收方
            // 根据当前发送者的 User ID 判断接收方的 User ID，避免前端传来的 receiverId（档案 ID）在值上碰巧相等导致判断错误
            Long receiverUserId = null;
            ConsultationSession sessionData = consultationSessionMapper.selectById(chatMessage.getSessionId());
            if (sessionData != null) {
                Long doctorUserId = profileResolveService.getUserIdByDoctorProfileId(sessionData.getDoctorId());
                Long patientUserId = profileResolveService.getUserIdByPatientProfileId(sessionData.getPatientId());
                
                if (senderId.equals(doctorUserId)) {
                    receiverUserId = patientUserId;
                } else if (senderId.equals(patientUserId)) {
                    receiverUserId = doctorUserId;
                }
            }

            if (receiverUserId != null) {
                Session receiverSession = onlineUsers.get(receiverUserId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    receiverSession.getBasicRemote().sendText(objectMapper.writeValueAsString(ResultUtils.success(msgVO)));
                }
            }

            // 也推送给发送方（回显状态）
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(ResultUtils.success(msgVO)));

        } catch (BusinessException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            log.error("WebSocket onMessage error", e);
            sendError(session, "发送失败");
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket onError", error);
    }

    private Long extractUserIdFromSession(Session session) {
        String queryString = session.getQueryString();
        if (queryString != null && queryString.contains("token=")) {
            String token = queryString.substring(queryString.indexOf("token=") + 6);
            if (token.contains("&")) {
                token = token.substring(0, token.indexOf("&"));
            }
            CurrentUser currentUser = jwtService.parseToken(token);
            if (currentUser != null) {
                // 将 userId 存入 session 的 userProperties 以便后续使用
                session.getUserProperties().put("userId", currentUser.id());
                return currentUser.id();
            }
        }
        return null;
    }

    private Long extractUserIdFromSessionSafe(Session session) {
        Object userId = session.getUserProperties().get("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    private void sendError(Session session, String errorMsg) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(ResultUtils.error(500, errorMsg)));
            }
        } catch (IOException e) {
            log.error("Send error response failed", e);
        }
    }
}
