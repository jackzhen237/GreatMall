package org.example.mallai.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 *                              聊天会话类
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 *
 * 【这个类是做什么的？】
 * 用于表示一个完整的聊天会话，包含会话 ID、用户名、创建时间、所有消息列表等信息。
 *
 * 【什么是会话？】
 * - 会话（Session）是一个独立的对话上下文
 * - 每个会话有唯一的 ID，用户可以切换不同的会话
 * - 每个会话属于特定的用户，包含多条消息（用户问 + AI 回复）
 *
 * 【会话示例】
 *   Session ID: abc123
 *   Username: zhangsan
 *   创建时间: 2024-01-01 10:00:00
 *   消息列表:
 *     [用户] 推荐一款华为手机
 *     [AI] 推荐华为Mate60...
 *     [用户] 价格多少？
 *     [AI] 6999元...
 *
 * 【为什么要绑定用户？】
 * - 一个用户可以创建多个会话
 * - 查询时需要按用户过滤，只返回该用户的会话
 * - 不同用户的会话互相隔离，保证隐私
 *
 */
public class ChatSession {

    /**
     * 会话唯一标识符
     * 使用 UUID 生成，确保每个会话都有唯一的 ID
     */
    private String sessionId;

    /**
     * 用户名
     * 用于标识这个会话属于哪个用户
     * 实现用户与会话的绑定
     */
    private String username;

    /**
     * 会话标题/简介
     * 通常取自用户的第一条消息
     */
    private String title;

    /**
     * 会话创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后活跃时间
     * 最后一条消息的时间，用于排序和清理
     */
    private LocalDateTime lastActiveTime;

    /**
     * 消息列表
     * 包含这个会话中的所有对话消息
     */
    private List<ChatMessage> messages;

    /**
     * 构造函数 - 创建新会话
     *
     * @param username 用户名（必填，用于绑定会话归属）
     * @param title 会话标题（通常取自第一条用户消息的前20个字符）
     */
    public ChatSession(String username, String title) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username 不能为空");
        }
        this.sessionId = UUID.randomUUID().toString().replace("-", "");
        this.username = username;
        this.title = title;
        this.createTime = LocalDateTime.now();
        this.lastActiveTime = LocalDateTime.now();
        this.messages = new ArrayList<>();
    }

    /**
     * 构造函数 - 完整参数
     *
     * @param sessionId     会话ID
     * @param username    用户名
     * @param title        会话标题
     * @param createTime   创建时间
     * @param lastActiveTime最后活跃时间
     * @param messages     消息列表
     */
    public ChatSession(String sessionId, String username, String title, LocalDateTime createTime,
                       LocalDateTime lastActiveTime, List<ChatMessage> messages) {
        this.sessionId = sessionId;
        this.username = username;
        this.title = title;
        this.createTime = createTime;
        this.lastActiveTime = lastActiveTime;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    // ==================== 消息操作方法 ====================

    /**
     * 添加用户消息
     *
     * @param content 消息内容
     */
    public void addUserMessage(String content) {
        this.messages.add(new ChatMessage(ChatMessage.MessageType.USER, content));
        this.lastActiveTime = LocalDateTime.now();
    }

    /**
     * 添加 AI 消息
     *
     * @param content 消息内容
     */
    public void addAiMessage(String content) {
        this.messages.add(new ChatMessage(ChatMessage.MessageType.AI, content));
        this.lastActiveTime = LocalDateTime.now();
    }

    /**
     * 添加系统消息
     *
     * @param content 消息内容
     */
    public void addSystemMessage(String content) {
        this.messages.add(new ChatMessage(ChatMessage.MessageType.SYSTEM, content));
        this.lastActiveTime = LocalDateTime.now();
    }

    /**
     * 获取消息数量
     */
    public int getMessageCount() {
        return this.messages.size();
    }

    // ==================== Getter 和 Setter ====================

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
     * 获取会话的简短描述
     */
    public String getShortTitle() {
        if (title == null || title.isEmpty()) {
            return "新会话";
        }
        return title.length() > 20 ? title.substring(0, 20) + "..." : title;
    }

    @Override
    public String toString() {
        return String.format("ChatSession[id=%s, title=%s, messages=%d]",
            sessionId, getShortTitle(), messages.size());
    }
}