package org.example.mallai.chat;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 *                              聊天消息类
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 *
 * 【这个类是做什么的？】
 * 用于表示一次对话中的单条消息，可以是用户发送的消息，也可以是 AI 的回复。
 *
 * 【消息类型】
 * - USER    ：用户发送的消息
 * - AI      ：AI 生成的回复
 * - SYSTEM  ：系统消息（如：上下文中包含的知识库内容）
 *
 * 【为什么要区分消息类型？】
 * 在对话历史中，我们需要知道：
 * - 哪条消息是用户说的，哪条是 AI 回复的
 * - 这样 AI 才能理解对话的上下文
 *
 * 【示例】
 *   ChatMessage userMsg = new ChatMessage(MessageType.USER, "推荐一款华为手机");
 *   ChatMessage aiMsg = new ChatMessage(MessageType.AI, "推荐华为Mate60...");
 *
 */
public class ChatMessage {

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        USER,   // 用户消息
        AI,     // AI 回复
        SYSTEM  // 系统消息
    }

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息时间
     */
    private LocalDateTime timestamp;

    /**
     * 构造函数
     *
     * @param type    消息类型
     * @param content 消息内容
     */
    public ChatMessage(MessageType type, String content) {
        this.type = type;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数（指定时间）
     *
     * @param type      消息类型
     * @param content   消息内容
     * @param timestamp 消息时间
     */
    public ChatMessage(MessageType type, String content, LocalDateTime timestamp) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
    }

    // ==================== Getter 和 Setter ====================

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取格式化的消息字符串
     * 用于调试和日志输出
     */
    @Override
    public String toString() {
        return String.format("[%s][%s] %s",
            timestamp != null ? timestamp.toString() : "null",
            type != null ? type.name() : "null",
            content != null ? content : "null");
    }
}