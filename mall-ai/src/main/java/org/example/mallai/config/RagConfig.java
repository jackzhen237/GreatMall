package org.example.mallai.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 配置类
 *
 * 【什么是 RAG？】
 * RAG = Retrieval（检索）+ Augmented（增强）+ Generation（生成）
 * 简单说就是：让 AI 先去知识库检索相关信息，再基于这些信息生成回答
 *
 * 【Embedding 是什么？】
 * Embedding = 把文字转换成数学向量（数字数组）
 * 例如："华为手机" → [0.123, -0.456, 0.789, ...]
 * 相似的内容转换后的向量距离更近，这样就可以通过向量相似度来检索相关内容
 *
 * 【为什么需要 Embedding？】
 * 1. 用户提问："推荐一款性价比高的手机"
 * 2. 将提问转换为向量
 * 3. 在向量数据库中搜索与提问向量最相似的内容
 * 4. 找到相关商品/品牌信息
 * 5. 将这些内容作为上下文传给 LLM 生成回答
 *
 * 【使用模型】
 * - nomic-embed-text (推荐，需提前下载: ollama pull nomic-embed-text)
 * - 或其他支持 embedding 的 Ollama 模型
 */
@Configuration
public class RagConfig {

    /**
     * Ollama 服务的地址
     *
     * 【什么是 Ollama？】
     * Ollama 是一个本地大模型运行平台，可以在本地运行各种 LLM（如 qwen2.5、llama2 等）
     * 不需要调用云端 API，保护数据隐私
     *
     * 【配置说明】
     * 默认值：http://localhost:11434
     * 可在 application.properties 中通过 ollama.base-url 配置项覆盖
     */
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaUrl;

    /**
     * Embedding 模型名称
     *
     * 【为什么需要专门的 Embedding 模型？】
     * Embedding 模型专门训练用于将文本转换为向量
     * 不同的 Embedding 模型效果差异很大
     *
     * 【推荐模型】
     * - nomic-embed-text：效果好，支持中文，模型较小
     * - 其他模型：如 m2e-base、text2vec 等
     *
     * 【使用前准备】
     * 必须先下载模型：ollama pull nomic-embed-text
     */
    @Value("${ollama.embedding-model:nomic-embed-text}")
    private String embeddingModelName;

    /**
     * 创建 Ollama Embedding 模型实例
     *
     * 【这个方法做了什么？】
     * 1. 创建一个 EmbeddingModel 实例
     * 2. 配置 Ollama 服务地址和模型名称
     * 3. 将此实例注册为 Spring Bean，供其他地方注入使用
     *
     * 【@Bean 的作用】
     * Spring 会自动调用此方法，并将返回的 EmbeddingModel 对象注册到容器中
     * 之后在 RagService 等类中通过 @Autowired 注入使用
     *
     * @return EmbeddingModel 用于文本向量化的模型实例
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(embeddingModelName)
                .build();
    }
}