package org.example.mallai.agent;

import org.springframework.stereotype.Component;

/**
 * 商场 AI 智能体接口
 *
 * 功能说明：
 * - 定义 AI Agent 的核心行为规范
 * - 提供与用户进行自然语言对话的能力
 *
 * 设计理念：
 * - 采用接口+实现类的设计模式，便于后续扩展和替换
 * - chat 方法作为与 Agent 交互的唯一入口，简化调用方式
 */
@Component
public interface MallAgent {

    /**
     * 处理用户消息并返回 AI 回复
     *
     * @param userMessage 用户输入的消息/问题
     * @return String AI 生成的回复内容
     *
     * 使用示例：
     * String response = mallAgent.chat("你好，帮我推荐一些商品");
     */
    String chat(String userMessage);
}
