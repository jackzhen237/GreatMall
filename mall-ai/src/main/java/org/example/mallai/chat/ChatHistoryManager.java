package org.example.mallai.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 *                              会话历史管理器
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 *
 * 【这个类是做什么的？】
 * 负责管理所有聊天会话的存储和加载，使用 JSON 文件持久化保存会话数据。
 *
 * 【存储位置】
 * - Windows: C:\Users\用户名\.mall-ai\chat-history\
 * - Linux/Mac: ~/.mall-ai/chat-history/
 *
 * 【存储结构】
 * - 所有会话保存在一个 sessions.json 文件中（包含会话索引）
 * - 每个会话包含完整的消息历史，通过 username 关联用户
 *
 * 【核心功能】
 * 1. 创建新会话（绑定用户）
 * 2. 根据会话 ID 获取会话（验证用户归属）
 * 3. 保存会话（自动）
 * 4. 列出用户的所有会话
 * 5. 删除会话（验证用户归属）
 *
 * 【用户隔离机制】
 * - 每个会话绑定一个 username
 * - 查询时按 username 过滤，确保用户只能看到自己的会话
 * - 删除/获取会话时验证 username，防止跨用户访问
 *
 * 【线程安全】
 * - 使用 ConcurrentHashMap 存储会话，支持多线程并发访问
 * - 文件读写使用 synchronized 关键字保证线程安全
 *
 */
@Component
public class ChatHistoryManager {

    private static final Logger logger = LoggerFactory.getLogger(ChatHistoryManager.class);

    /**
     * 存储目录名称
     */
    private static final String STORE_DIR_NAME = ".mall-ai" + File.separator + "chat-history";

    /**
     * 索引文件名（存储所有会话的元信息）
     */
    private static final String INDEX_FILE_NAME = "sessions.json";

    /**
     * 存储目录
     */
    private Path storeDir;

    /**
     * 索引文件
     */
    private File indexFile;

    /**
     * 内存中的会话缓存
     * Key: sessionId, Value: ChatSession
     */
    private final Map<String, ChatSession> sessionCache = new ConcurrentHashMap<>();

    /**
     * Jackson ObjectMapper
     * 用于 JSON 序列化和反序列化
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     */
    public ChatHistoryManager() {
        this.objectMapper = new ObjectMapper();
        // 注册 Java 8 时间模块
        this.objectMapper.registerModule(new JavaTimeModule());
        // 美化输出（格式化 JSON）
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 忽略 null 值
        this.objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
    }

    /**
     * 初始化存储目录和加载已有会话
     * 使用 @PostConstruct 确保在构造函数后执行
     */
    @PostConstruct
    public void init() {
        try {
            // 获取用户主目录
            String userHome = System.getProperty("user.home");
            storeDir = Paths.get(userHome, STORE_DIR_NAME);

            // 创建存储目录（如果不存在）
            if (!Files.exists(storeDir)) {
                Files.createDirectories(storeDir);
                logger.info("创建会话存储目录: {}", storeDir);
            }

            indexFile = storeDir.resolve(INDEX_FILE_NAME).toFile();

            // 加载已有的会话
            loadSessions();

            logger.info("会话历史管理器初始化完成，已加载 {} 个会话", sessionCache.size());

        } catch (IOException e) {
            logger.error("初始化会话存储目录失败", e);
            throw new RuntimeException("无法初始化会话存储", e);
        }
    }

    // ==================== 核心功能方法 ====================

    /**
     * 创建新会话（绑定用户）
     *
     * @param username      用户名（必填，用于绑定会话归属）
     * @param firstMessage 用户的第一条消息（用于生成会话标题）
     * @return 新创建的会话
     */
    public synchronized ChatSession createSession(String username, String firstMessage) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username 不能为空");
        }

        // 生成会话标题（取前20个字符）
        String title = firstMessage != null && firstMessage.length() > 0
            ? firstMessage.substring(0, Math.min(20, firstMessage.length()))
            : "新会话";

        ChatSession session = new ChatSession(username, title);
        sessionCache.put(session.getSessionId(), session);
        saveSession(session);

        logger.info("创建新会话: {}, 所属用户: {}", session.getSessionId(), username);
        return session;
    }

    /**
     * 获取会话（不验证用户归属）
     * 注意：建议使用 getSessionForUser 方法以验证用户归属
     *
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在返回 null
     */
    public ChatSession getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }

    /**
     * 获取会话（验证用户归属）
     * 确保只有会话所属用户才能获取会话
     *
     * @param sessionId 会话ID
     * @param username  用户名
     * @return 会话对象，如果不存在或不属于该用户返回 null
     */
    public ChatSession getSessionForUser(String sessionId, String username) {
        ChatSession session = sessionCache.get(sessionId);
        if (session == null) {
            return null;
        }
        // 验证用户归属
        if (!session.getUsername().equals(username)) {
            logger.warn("用户 {} 尝试访问不属于他的会话 {}", username, sessionId);
            return null;
        }
        return session;
    }

    /**
     * 保存会话
     * 每次对话结束后自动调用
     *
     * @param session 要保存的会话
     */
    public synchronized void saveSession(ChatSession session) {
        try {
            // 更新缓存
            sessionCache.put(session.getSessionId(), session);

            // 保存到文件
            File sessionFile = storeDir.resolve(session.getSessionId() + ".json").toFile();
            objectMapper.writeValue(sessionFile, session);

            // 更新索引文件
            updateIndexFile();

            logger.debug("会话已保存: {}", session.getSessionId());

        } catch (IOException e) {
            logger.error("保存会话失败: {}", session.getSessionId(), e);
        }
    }

    /**
     * 删除会话（验证用户归属）
     *
     * @param sessionId 会话ID
     * @param username 用户名（用于验证会话归属）
     * @return true 删除成功，false 会话不存在或不属于该用户
     */
    public synchronized boolean deleteSession(String sessionId, String username) {
        ChatSession session = sessionCache.get(sessionId);
        if (session == null) {
            return false;
        }

        // 验证用户归属
        if (!session.getUsername().equals(username)) {
            logger.warn("用户 {} 尝试删除不属于他的会话 {}", username, sessionId);
            return false;
        }

        sessionCache.remove(sessionId);

        try {
            // 删除会话文件
            File sessionFile = storeDir.resolve(sessionId + ".json").toFile();
            if (sessionFile.exists()) {
                Files.delete(sessionFile.toPath());
            }

            // 更新索引文件
            updateIndexFile();

            logger.info("删除会话: {}, 所属用户: {}", sessionId, username);
            return true;

        } catch (IOException e) {
            logger.error("删除会话失败: {}", sessionId, e);
            return false;
        }
    }

    /**
     * 获取所有会话列表（不区分用户）
     * 注意：建议使用 listSessionsForUser 方法
     *
     * @return 会话列表
     */
    public List<ChatSession> listSessions() {
        return sessionCache.values().stream()
            .sorted((s1, s2) -> s2.getLastActiveTime().compareTo(s1.getLastActiveTime()))
            .collect(Collectors.toList());
    }

    /**
     * 获取指定用户的所有会话
     * 按最后活跃时间倒序排列
     *
     * @param username 用户名
     * @return 该用户的会话列表
     */
    public List<ChatSession> listSessionsForUser(String username) {
        return sessionCache.values().stream()
            .filter(session -> session.getUsername().equals(username))
            .sorted((s1, s2) -> s2.getLastActiveTime().compareTo(s1.getLastActiveTime()))
            .collect(Collectors.toList());
    }

    /**
     * 获取会话数量
     */
    public int getSessionCount() {
        return sessionCache.size();
    }

    /**
     * 清空所有会话
     * 谨慎使用！
     */
    public synchronized void clearAllSessions() {
        // 删除所有会话文件
        sessionCache.keySet().forEach(sessionId -> {
            try {
                File sessionFile = storeDir.resolve(sessionId + ".json").toFile();
                if (sessionFile.exists()) {
                    Files.delete(sessionFile.toPath());
                }
            } catch (IOException e) {
                logger.error("删除会话文件失败: {}", sessionId, e);
            }
        });

        // 清空缓存
        sessionCache.clear();

        // 清空索引文件
        updateIndexFile();

        logger.warn("已清空所有会话");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从文件加载所有会话
     */
    private void loadSessions() {
        if (!indexFile.exists()) {
            logger.info("会话索引文件不存在，将创建新文件");
            return;
        }

        try {
            // 读取索引文件获取所有会话 ID
            List<String> sessionIds = objectMapper.readValue(indexFile, new TypeReference<List<String>>() {});

            // 加载每个会话
            int loadedCount = 0;
            for (String sessionId : sessionIds) {
                File sessionFile = storeDir.resolve(sessionId + ".json").toFile();
                if (sessionFile.exists()) {
                    try {
                        ChatSession session = objectMapper.readValue(sessionFile, ChatSession.class);
                        sessionCache.put(sessionId, session);
                        loadedCount++;
                    } catch (Exception e) {
                        logger.warn("加载会话失败: {}", sessionId, e);
                    }
                }
            }

            logger.info("从文件加载了 {} 个会话", loadedCount);

        } catch (IOException e) {
            logger.error("加载会话索引失败", e);
        }
    }

    /**
     * 更新索引文件
     * 索引文件保存所有会话 ID 列表
     */
    private synchronized void updateIndexFile() {
        try {
            List<String> sessionIds = new ArrayList<>(sessionCache.keySet());
            objectMapper.writeValue(indexFile, sessionIds);
        } catch (IOException e) {
            logger.error("更新会话索引文件失败", e);
        }
    }
}