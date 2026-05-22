package org.example.mallai.config;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ollama 模型配置类
 *
 * 功能说明：
 * - 配置本地部署的 Ollama 服务的连接信息
 * - 创建 ChatLanguageModel 实例用于与 LLM 交互
 *
 * 使用方式：
 * - 通过 @Value 注解从 application.properties 读取配置
 * - baseUrl: Ollama 服务的地址，默认为 http://localhost:11434
 * - modelName: 使用的模型名称，默认为 qwen2.5
 */
@Configuration
public class OllamaConfig {

    /**
     * Ollama 服务的基地址
     * 默认值：http://localhost:11434
     * 可在 application.properties 中通过 ollama.base-url 配置项覆盖
     */
    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    /**
     * 使用的模型名称
     * 默认值：qwen2.5
     * 可在 application.properties 中通过 ollama.model-name 配置项覆盖
     * 确保本地已通过 ollama pull qwen2.5 下载该模型
     */
    @Value("${ollama.model-name:qwen2.5}")
    private String modelName;

    /**
     * 创建 OllamaChatModel 实例
     *
     * @return OllamaChatModel - 用于与 Ollama LLM 进行对话的模型实例
     *
     * 配置说明：
     * - baseUrl: Ollama 服务的地址
     * - modelName: 要使用的模型名称
     */
    @Bean
    public OllamaChatModel ollamaChatModel() {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }
}
