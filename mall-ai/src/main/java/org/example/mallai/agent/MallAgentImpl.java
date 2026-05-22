package org.example.mallai.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.Agent;
import dev.langchain4j.service.SystemMessage;
import org.example.mallai.chat.ChatHistoryManager;
import org.example.mallai.chat.ChatMessage;
import org.example.mallai.chat.ChatSession;
import org.example.mallai.config.OllamaConfig;
import org.example.mallai.rag.RagService;
import org.example.mallai.tools.MCPTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 *                              商场 AI 智能体实现类
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 *
 * 【这个类是做什么的？】
 * 这是 Agent 的核心实现类，负责：
 * 1. 接收用户消息
 * 2. 通过 RAG 检索相关知识
 * 3. 结合 Tools 执行实际操作
 * 4. 将结果返回给用户
 *
 *
 * 【Agent 的工作流程】
 *
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                           用户提问                                      │
 *   │                    "推荐一款性价比高的华为手机"                          │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                        1. RAG 知识检索                                  │
 *   │  调用 RagService.buildContext() 从向量数据库检索相关信息                  │
 *   │  → 找到华为品牌介绍、华为手机商品列表、价格等信息                         │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                        2. 组装 Prompt                                  │
 *   │  将用户问题 + RAG检索结果 + 历史对话 组合成完整的 Prompt                 │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                        3. 调用 LLM                                     │
 *   │  将组装好的 Prompt 发送给 Ollama 的 qwen2.5 模型                         │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                        4. 返回回答                                      │
 *   │  "根据您的需求，我推荐华为Mate60..."                                    │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                        5. 保存对话历史                                  │
 *   │  将用户消息和AI回答保存到 ChatHistoryManager                            │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *
 *
 * 【核心组件依赖】
 * - ChatLanguageModel   : 与 Ollama LLM 通信
 * - RagService         : RAG 知识检索
 * - MCPTools           : MCP 工具调用
 * - ChatHistoryManager : 对话历史管理
 *
 *
 * 【为什么需要 RAG？】
 * - 如果没有 RAG：AI 可能编造虚假信息
 * - 有了 RAG：AI 基于真实的商品、品牌数据生成回答
 *
 *
 * 【为什么需要对话历史？】
 * - 如果没有对话历史：每次对话都是独立的，无法理解上下文
 * - 有了对话历史：AI 可以记住之前的对话内容
 *
 *   示例：
 *   用户: "推荐一款华为手机"
 *   AI: "推荐华为Mate60..."
 *   用户: "多少钱？"          ← 如果没有历史，AI不知道在问什么
 *   AI: "6999元"             ← 有了历史，AI知道在问Mate60的价格
 *
 */
@Agent
@Component
public class MallAgentImpl implements MallAgent {

    private static final Logger logger = LoggerFactory.getLogger(MallAgentImpl.class);

    /**
     * 聊天语言模型
     * 用于与 Ollama LLM 通信
     */
    private final ChatLanguageModel chatLanguageModel;

    /**
     * RAG 检索服务
     * 用于从向量数据库检索相关知识
     */
    private final RagService ragService;

    /**
     * MCP 工具集
     * 提供各种工具能力（查商品、查订单等）
     */
    private final MCPTools mcpTools;

    /**
     * 对话历史管理器
     * 负责会话的持久化和加载
     */
    private final ChatHistoryManager chatHistoryManager;

    /**
     * 构造函数
     * 通过依赖注入获取所有组件
     */
    public MallAgentImpl(
            OllamaConfig ollamaConfig,
            RagService ragService,
            MCPTools mcpTools,
            ChatHistoryManager chatHistoryManager) {
        this.chatLanguageModel = ollamaConfig.ollamaChatModel();
        this.ragService = ragService;
        this.mcpTools = mcpTools;
        this.chatHistoryManager = chatHistoryManager;
    }

    /**
     * 处理用户消息（简单模式 - 无会话，已废弃，使用会话模式）
     *
     * 【说明】
     * 此方法仅为兼容接口保留，实际内部会创建新会话
     * 建议使用 chatWithSession() 方法明确指定会话
     *
     * @param userMessage 用户输入的消息
     * @return AI 生成的回复
     */
    @Override
    public String chat(String userMessage) {
        // 简单模式实际会创建临时会话，但这里无法获取 username
        // 暂时返回友好提示，建议使用会话模式
        throw new UnsupportedOperationException("请使用会话模式：chatWithSession(message, username, sessionId)");
    }

    /**
     * 处理用户消息（会话模式 - 带历史和用户绑定）
     *
     * @param userMessage 用户输入的消息
     * @param username   用户名（用于绑定会话归属）
     * @param sessionId  会话ID，null 表示新会话
     * @return AI 生成的回复
     */
    public String chatWithSession(String userMessage, String username, String sessionId) {
        logger.info("收到用户消息: {}, 用户名: {}, 会话ID: {}", userMessage, username, sessionId);

        // Step 1: 获取或创建会话（绑定用户）
        ChatSession session = getOrCreateSession(username, sessionId, userMessage);

        try {
            // Step 2: 调用 RAG 检索相关知识
            String ragContext = ragService.buildContext(userMessage, 3);
            logger.debug("RAG 检索结果: {}", ragContext);

            // Step 3: 构建完整的 Prompt
            String prompt = buildPrompt(userMessage, session, ragContext);

            // Step 4: 调用 LLM 生成回答
            String aiResponse = chatLanguageModel.generate(prompt);
            logger.info("AI 回复: {}", aiResponse);

            // Step 5: 保存对话历史
            session.addUserMessage(userMessage);
            session.addAiMessage(aiResponse);
            chatHistoryManager.saveSession(session);

            return aiResponse;

        } catch (Exception e) {
            logger.error("处理消息失败", e);
            return "抱歉，我遇到了一些问题：" + e.getMessage();
        }
    }

    /**
     * 获取或创建会话（带用户绑定）
     *
     * @param username      用户名
     * @param sessionId    会话ID
     * @param firstMessage 第一条消息（用于创建新会话时生成标题）
     * @return 会话对象
     */
    private ChatSession getOrCreateSession(String username, String sessionId, String firstMessage) {
        if (sessionId != null && !sessionId.isEmpty()) {
            // 尝试获取会话并验证用户归属
            ChatSession existingSession = chatHistoryManager.getSessionForUser(sessionId, username);
            if (existingSession != null) {
                return existingSession;
            }
            logger.warn("会话不存在或不属于该用户: {}，创建新会话", sessionId);
        }

        // 创建新会话（必须绑定 username）
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username 不能为空");
        }
        return chatHistoryManager.createSession(username, firstMessage);
    }

    /**
     * 构建完整的 Prompt
     *
     * 【Prompt 结构】
     * 1. System Message: 定义 AI 的角色和能力
     * 2. RAG Context: 从知识库检索到的相关信息
     * 3. Chat History: 对话历史
     * 4. User Message: 当前用户的问题
     *
     * @param userMessage 用户消息
     * @param session 会话对象
     * @param ragContext RAG 检索到的上下文
     * @return 完整的 Prompt
     */
    private String buildPrompt(String userMessage, ChatSession session, String ragContext) {
        StringBuilder prompt = new StringBuilder();

        // 1. System Message
        prompt.append("你是商城的 AI 智能助手，专门帮助用户解答商品、品牌等相关问题。\n\n");
        prompt.append("【你的能力】\n");
        prompt.append("1. 回答关于商品的问题（价格、功能、评价等）\n");
        prompt.append("2. 回答关于品牌的问题（品牌故事、特色等）\n");
        prompt.append("3. 根据用户需求推荐合适的商品\n");
        prompt.append("4. 提供购物建议和帮助\n\n");

        // 2. RAG Context（如果检索到相关信息）
        if (ragContext != null && !ragContext.isEmpty()) {
            prompt.append("【以下是你可以参考的知识库信息】\n");
            prompt.append(ragContext);
            prompt.append("\n");
        } else {
            prompt.append("【注意】知识库中未找到相关信息，请基于你的常识回答用户问题。\n\n");
        }

        // 3. Chat History（对话历史）
        if (session.getMessageCount() > 0) {
            prompt.append("【对话历史】\n");
            for (ChatMessage msg : session.getMessages()) {
                if (msg.getType() == ChatMessage.MessageType.USER) {
                    prompt.append("用户: ").append(msg.getContent()).append("\n");
                } else if (msg.getType() == ChatMessage.MessageType.AI) {
                    prompt.append("助手: ").append(msg.getContent()).append("\n");
                }
            }
            prompt.append("\n");
        }

        // 4. Current User Message
        prompt.append("【当前问题】\n");
        prompt.append("用户: ").append(userMessage).append("\n\n");
        prompt.append("助手: ");

        return prompt.toString();
    }

    // ==================== 会话管理方法 ====================

    /**
     * 创建新会话（绑定用户）
     *
     * @param username      用户名（必填，用于绑定会话归属）
     * @param firstMessage 第一条消息（用于生成会话标题）
     * @return 新会话的 ID
     */
    public String createSession(String username, String firstMessage) {
        ChatSession session = chatHistoryManager.createSession(username, firstMessage);
        return session.getSessionId();
    }

    /**
     * 获取会话（验证用户归属）
     *
     * @param sessionId 会话ID
     * @param username 用户名（用于验证会话归属）
     * @return 会话对象，不存在或不属于该用户返回 null
     */
    public ChatSession getSession(String sessionId, String username) {
        return chatHistoryManager.getSessionForUser(sessionId, username);
    }

    /**
     * 获取用户的所有会话列表
     *
     * @param username 用户名
     * @return 该用户的会话列表
     */
    public java.util.List<ChatSession> listSessions(String username) {
        return chatHistoryManager.listSessionsForUser(username);
    }

    /**
     * 删除会话（验证用户归属）
     *
     * @param sessionId 会话ID
     * @param username 用户名（用于验证会话归属）
     * @return true 删除成功，false 会话不存在或不属于该用户
     */
    public boolean deleteSession(String sessionId, String username) {
        return chatHistoryManager.deleteSession(sessionId, username);
    }

    /**
     * 获取用户的会话数量
     *
     * @param username 用户名
     */
    public int getSessionCount(String username) {
        return chatHistoryManager.listSessionsForUser(username).size();
    }
}