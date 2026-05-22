package org.example.mallai.controller;

import org.example.mallai.agent.MallAgent;
import org.example.mallai.agent.MallAgentImpl;
import org.example.mallai.chat.ChatSession;
import org.example.mall_tiny01.component.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 *                              AI 智能体 REST API 控制器
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 *
 * 【这个类是做什么的？】
 * 提供 HTTP 接口供外部系统调用 AI Agent，支持会话管理功能。
 * 用户身份通过 UserContext 自动获取（基于 JWT Token），无需前端传递。
 *
 *
 * 【用户身份获取机制】
 * - 用户登录后，请求携带 JWT Token
 * - JwtInterceptor 解析 Token，将 username 保存到 UserContext
 * - Controller 从 UserContext 获取当前登录用户的 username
 * - 用户只能操作自己的会话，实现会话隔离
 *
 *
 * 【API 接口列表】
 *
 * ┌───────────────────────────────────────────────────────────────────────────────┐
 * │                            对话接口                                           │
 * ├──────────┬─────────────────────────────────────────┬────────────────────────┤
 * │  方法    │  路径                                    │  说明                 │
 * ├──────────┼─────────────────────────────────────────┼────────────────────────┤
 * │  GET     │  /api/agent/chat                         │  简单对话（无会话）  │
 * │  POST    │  /api/agent/chat                         │  简单对话（无会话）  │
 * │  POST    │  /api/agent/chat/{sessionId}            │  会话模式对话        │
 * └──────────┴─────────────────────────────────────────┴────────────────────────┘
 *
 * ┌───────────────────────────────────────────────────────────────────────────────┐
 * │                            会话管理接口                                       │
 * ├──────────┬─────────────────────────────────────────┬────────────────────────┤
 * │  方法    │  路径                                    │  说明                 │
 * ├──────────┼─────────────────────────────────────────┼────────────────────────┤
 * │  GET     │  /api/agent/sessions                    │  获取用户会话列表    │
 * │  GET     │  /api/agent/sessions/{id}              │  获取指定会话        │
 * │  DELETE  │  /api/agent/sessions/{id}              │  删除指定会话        │
 * │  POST    │  /api/agent/sessions                  │  创建新会话          │
 * └──────────┴─────────────────────────────────────────┴────────────────────────┘
 *
 *
 * 【使用示例】
 *
 * 1. 简单对话（不带会话）
 *    GET  /api/agent/chat?message=推荐一款手机
 *    POST /api/agent/chat  body: {"message": "推荐一款手机"}
 *
 * 2. 会话模式对话
 *    POST /api/agent/chat/session123  body: {"message": "还有别的推荐吗？"}
 *
 * 3. 获取当前用户的会话列表
 *    GET /api/agent/sessions
 *
 * 4. 获取指定会话
 *    GET /api/agent/sessions/session123
 *
 * 5. 创建新会话
 *    POST /api/agent/sessions  body: {"firstMessage": "我想买一部手机"}
 *
 * 6. 删除会话
 *    DELETE /api/agent/sessions/session123
 *
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    /**
     * AI 智能体实例
     */
    @Autowired
    private MallAgent mallAgent;

    /**
     * AI 智能体实现（用于访问会话管理方法）
     */
    @Autowired
    private MallAgentImpl mallAgentImpl;

    // ==================== 对话接口 ====================

    /**
     * GET 请求：简单对话（自动创建新会话）
     *
     * @param message 用户消息
     * @return AI 的回复
     *
     * 调用示例：
     * GET http://localhost:8080/api/agent/chat?message=推荐一款手机
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam String message) {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        String sessionId = null; // 创建新会话
        String response = mallAgentImpl.chatWithSession(message, username, sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("response", response);
        result.put("sessionId", sessionId);
        result.put("username", username);
        return result;
    }

    /**
     * POST 请求：简单对话（自动创建新会话）
     *
     * @param request 请求体，包含 message 字段
     * @return AI 的回复
     *
     * 调用示例：
     * curl -X POST http://localhost:8080/api/agent/chat \
     *      -H "Content-Type: application/json" \
     *      -d '{"message": "推荐一款手机"}'
     */
    @PostMapping("/chat")
    public Map<String, Object> chatPost(@RequestBody Map<String, String> request) {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        String message = request.get("message");
        String sessionId = null; // 创建新会话
        String response = mallAgentImpl.chatWithSession(message, username, sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("response", response);
        result.put("sessionId", sessionId);
        result.put("username", username);
        return result;
    }

    /**
     * POST 请求：会话模式对话（用户绑定）
     *
     * @param sessionId 会话ID
     * @param request   请求体，包含 message 字段
     * @return AI 的回复
     *
     * 调用示例：
     * curl -X POST http://localhost:8080/api/agent/chat/session123 \
     *      -H "Content-Type: application/json" \
     *      -d '{"message": "还有别的推荐吗？"}'
     */
    @PostMapping("/chat/{sessionId}")
    public Map<String, Object> chatWithSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {

        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        String message = request.get("message");
        String response = mallAgentImpl.chatWithSession(message, username, sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("response", response);
        result.put("sessionId", sessionId);
        result.put("username", username);
        return result;
    }

    // ==================== 会话管理接口 ====================

    /**
     * GET 请求：获取当前用户的会话列表
     *
     * @return 该用户的所有会话列表（按最后活跃时间倒序）
     *
     * 调用示例：
     * GET http://localhost:8080/api/agent/sessions
     */
    @GetMapping("/sessions")
    public Map<String, Object> listSessions() {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        List<ChatSession> sessions = mallAgentImpl.listSessions(username);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", sessions.size());
        result.put("sessions", sessions);
        result.put("username", username);
        return result;
    }

    /**
     * GET 请求：获取指定会话（验证用户归属）
     *
     * @param sessionId 会话ID
     * @return 会话详情
     *
     * 调用示例：
     * GET http://localhost:8080/api/agent/sessions/session123
     */
    @GetMapping("/sessions/{sessionId}")
    public Map<String, Object> getSession(@PathVariable String sessionId) {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        ChatSession session = mallAgentImpl.getSession(sessionId, username);

        Map<String, Object> result = new HashMap<>();
        if (session != null) {
            result.put("success", true);
            result.put("session", session);
        } else {
            result.put("success", false);
            result.put("message", "会话不存在或不属于该用户");
        }
        result.put("username", username);
        return result;
    }

    /**
     * POST 请求：创建新会话（绑定用户）
     *
     * @param request 请求体，包含 firstMessage 字段（可选）
     * @return 新会话信息
     *
     * 调用示例：
     * curl -X POST http://localhost:8080/api/agent/sessions \
     *      -H "Content-Type: application/json" \
     *      -d '{"firstMessage": "我想买一部手机"}'
     */
    @PostMapping("/sessions")
    public Map<String, Object> createSession(@RequestBody Map<String, String> request) {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        String firstMessage = request.get("firstMessage");

        if (firstMessage == null || firstMessage.isEmpty()) {
            firstMessage = "新会话";
        }

        String sessionId = mallAgentImpl.createSession(username, firstMessage);
        ChatSession session = mallAgentImpl.getSession(sessionId, username);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("sessionId", sessionId);
        result.put("session", session);
        result.put("username", username);
        return result;
    }

    /**
     * DELETE 请求：删除指定会话（验证用户归属）
     *
     * @param sessionId 会话ID
     * @return 删除结果
     *
     * 调用示例：
     * DELETE http://localhost:8080/api/agent/sessions/session123
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Map<String, Object> deleteSession(@PathVariable String sessionId) {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        boolean deleted = mallAgentImpl.deleteSession(sessionId, username);

        Map<String, Object> result = new HashMap<>();
        result.put("success", deleted);
        if (deleted) {
            result.put("message", "会话已删除");
        } else {
            result.put("message", "会话不存在或不属于该用户");
        }
        result.put("username", username);
        return result;
    }

    /**
     * GET 请求：获取当前用户的会话统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "用户未登录");
            return errorResult;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("sessionCount", mallAgentImpl.getSessionCount(username));
        result.put("message", "当前用户的会话数量");
        result.put("username", username);
        return result;
    }
}