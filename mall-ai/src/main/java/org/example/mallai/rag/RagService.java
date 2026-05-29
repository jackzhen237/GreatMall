package org.example.mallai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 *                           RAG 检索服务（核心服务类）
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 *
 * 【什么是 RAG？】
 * RAG = Retrieval（检索）+ Augmented（增强）+ Generation（生成）
 * 简单来说，RAG 就是让 AI 先去知识库检索相关信息，然后基于这些信息生成回答。
 *
 * 举个例子：
 *   用户问："华为手机有哪些？"
 *   传统 AI：可能胡编乱造一些华为手机型号
 *   RAG AI：先去知识库检索华为手机的信息，然后基于真实数据回答
 *
 *
 * 【RagService 在 RAG 系统中的位置和作用】
 *
 *   ┌─────────────┐      ┌──────────────┐      ┌─────────┐      ┌────────┐
 *   │  用户问题   │ ──▶  │  RagService  │ ──▶  │  LLM    │ ──▶  │  回答  │
 *   └─────────────┘      └──────────────┘      └─────────┘      └────────┘
 *                               │
 *                               ▼
 *                        ┌──────────────┐
 *                        │  向量数据库   │  ← 存储了所有商品、品牌知识
 *                        └──────────────┘
 *
 *   RagService 的工作流程：
 *   1. 接收用户问题（如 "华为手机多少钱？"）
 *   2. 将问题转换为向量（Embedding）
 *   3. 在向量数据库中搜索相似的文档
 *   4. 返回与问题最相关的文档列表
 *
 *
 * 【Embedding（向量化）是什么？】
 * Embedding = 把文字转换成数学向量（数字数组）
 *
 * 例如：
 *   "华为Mate60" → [0.123, -0.456, 0.789, 0.234, ...]  ← 一串数字
 *   "华为手机"   → [0.125, -0.460, 0.790, 0.236, ...]  ← 相似的内容，向量也相似
 *   "苹果手机"   → [-0.234, 0.567, -0.123, 0.890, ...] ← 不同的内容，向量差异大
 *
 * 为什么需要向量？
 * 因为计算机无法直接理解文字，但可以理解数字。
 * 通过向量相似度计算，可以找到与用户问题最相关的内容。
 *
 *
 * 【向量相似度搜索原理】
 *
 *   问题向量 ─────────────────┐
 *                              │
 *                              ▼
 *                    ┌─────────────────┐
 *                    │  计算向量相似度  │  ← 比较问题向量和每个文档向量的"距离"
 *                    └─────────────────┘
 *                              │
 *              ┌───────────────┼───────────────┐
 *              ▼               ▼               ▼
 *         文档A: 0.95     文档B: 0.87     文档C: 0.23
 *        （很相关）      （较相关）      （不相关）
 *
 *   返回相关度最高的前 K 个文档（如 topK=3，返回文档A和文档B）
 *
 *
 * 【当前实现说明】
 * - 使用内存向量存储（InMemoryEmbeddingStore），数据存在内存中
 * - 优点：简单，适合开发测试
 * - 缺点：应用重启后数据丢失，不适合大规模数据
 * - 生产环境建议替换为 Elasticsearch 或其他向量数据库
 *
 * @author mall-ai
 * @version 1.0
 */
@Service
public class RagService {

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     *                          核心组件：Embedding 模型和向量存储
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * 【embeddingModel 的作用】
     * 这是一个嵌入模型，负责把文字转换成向量。
     * 在 RagConfig 配置类中创建，底层调用 Ollama 的 nomic-embed-text 模型。
     *
     * 【embeddingStore 的作用】
     * 这是向量数据库，负责存储文档及其对应的向量。
     * 当前使用内存存储（InMemoryEmbeddingStore），适合开发测试。
     *
     * 【为什么需要两个东西？】
     * - embeddingModel  → 负责"转换"（文字 → 向量）
     * - embeddingStore  → 负责"存储"（文档 + 向量）
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 内存向量存储
     *
     * 解释：
     * - 这个变量存储了所有已经向量化的文档
     * - 每个文档包含：原始文本内容 + 对应的向量表示
     * - 当前使用内存实现，重启后会丢失数据
     *
     * 生产环境替换方案：
     * - ElasticsearchEmbeddingStore（推荐，用于大规模数据）
     * - ChromaEmbeddingStore
     * - PineconeEmbeddingStore（云服务）
     */
    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    public RagService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     *                          添加文档到知识库
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * 【这个方法是做什么的？】
     * 把一个文档（如商品信息、品牌介绍）存入向量数据库，使其能够被检索。
     *
     * 【使用场景】
     * - 初始化知识库时，批量添加商品、品牌数据
     * - 新增商品时，将商品信息添加到知识库
     *
     * 【方法执行流程】
     *
     *   输入: Document 对象
     *   ┌─────────────────────────────────────┐
     *   │  Document 内容示例：                 │
     *   │  "商品名称：华为Mate60              │
     *   │   品牌：华为                        │
     *   │   价格：6999元"                     │
     *   └─────────────────────────────────────┘
     *                  │
     *                  ▼
     *   ┌─────────────────────────────────────┐
     *   │  Step 1: 转换为 TextSegment          │
     *   │  把 Document 转成 TextSegment 格式   │
     *   │  TextSegment = 文本内容 + 元数据     │
     *   └─────────────────────────────────────┘
     *                  │
     *                  ▼
     *   ┌─────────────────────────────────────┐
     *   │  Step 2: 调用 Embedding 模型         │
     *   │  "华为Mate60..." → [0.123, ...]     │
     *   │  把文本转换为向量（一串数字）        │
     *   └─────────────────────────────────────┘
     *                  │
     *                  ▼
     *   ┌─────────────────────────────────────┐
     *   │  Step 3: 存入向量数据库              │
     *   │  embeddingStore.add(segment, vector)│
     *   │  存储 文本+向量 的映射关系          │
     *   └─────────────────────────────────────┘
     *
     * @param document 要添加的文档（包含内容和元数据）
     */
    public void addDocument(Document document) {
        TextSegment segment = TextSegment.from(document.text(), document.metadata());
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }

    /**
     * 批量添加文档
     * 
     * @param documents 文档列表
     */
    public void addDocuments(List<Document> documents) {
        List<Embedding> embeddings = new ArrayList<>();
        List<TextSegment> segments = new ArrayList<>();
        
        for (Document doc : documents) {
            TextSegment segment = TextSegment.from(doc.text(), doc.metadata());
            segments.add(segment);
            embeddings.add(embeddingModel.embed(segment).content());
        }
        
        embeddingStore.addAll(embeddings, segments);
    }

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     *                          检索相似文档（核心方法）
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * 【这个方法是做什么的？】
     * 根据用户的问题，从向量数据库中检索出最相关的文档。
     * 这是 RAG 系统的核心方法，用户问题通过此方法找到相关知识。
     *
     * 【使用场景】
     * 当用户向 Agent 提问时，Agent 会调用此方法获取相关背景知识
     *
     * 【方法执行流程】
     *
     *   输入: 用户问题 "华为手机有哪些？"
     *   ┌─────────────────────────────────────┐
     *   │  query = "华为手机有哪些？"          │
     *   │  topK = 3  ← 返回前3个最相关的文档  │
     *   └─────────────────────────────────────┘
     *                  │
     *                  ▼
     *   ┌─────────────────────────────────────┐
     *   │  Step 1: 将问题向量化                │
     *   │  "华为手机有哪些？"                  │
     *   │        → [0.234, -0.567, ...]       │
     *   │  使用 embeddingModel 将问题转成向量  │
     *   └─────────────────────────────────────┘
     *                  │
     *                  ▼
     *   ┌─────────────────────────────────────┐
     *   │  Step 2: 在向量数据库中搜索           │
     *   │  embeddingStore.findRelevant()       │
     *   │  比较问题向量与所有文档向量的相似度   │
     *   └─────────────────────────────────────┘
     *                  │
     *                  ▼
     *   ┌─────────────────────────────────────┐
     *   │  Step 3: 返回最相似的文档            │
     *   │                                    │
     *   │  文档A (华为Mate60) - 相似度 0.95   │  ← 返回
     *   │  文档B (华为P50)    - 相似度 0.88   │  ← 返回
     *   │  文档C (小米手机)   - 相似度 0.23   │  ← 不返回（相似度太低）
     *   └─────────────────────────────────────┘
     *
     * 【参数说明】
     * @param query 用户的问题/查询（String类型）
     * @param topK 返回最相关的 K 个文档（如 topK=3 返回前3个）
     *
     * 【返回值】
     * @return List<String> - 相似文档的内容列表，每个元素是一个文档的文本内容
     *
     * 【关于相似度阈值 0.7】
     * 在 findRelevant 方法中传入了 0.7，表示只返回相似度 >= 0.7 的文档
     * 相似度范围是 0~1，1 表示完全相同，0 表示完全不相关
     */
    public List<String> retrieve(String query, int topK) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, topK, 0.7)
                .stream()
                .map(result -> result.embedded().text())
                .toList();
    }

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     *                          检索相似文档（带相关性分数）
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * 【这个方法是做什么的？】
     * 与 retrieve() 方法类似，但额外返回每个文档的相关性分数。
     * 用于需要知道文档与问题匹配程度的场景。
     *
     * 【retrieve() vs retrieveWithScore()】
     * - retrieve()              → 只返回文档内容
     * - retrieveWithScore()     → 返回文档内容 + 相关性分数
     *
     * 【使用场景】
     * - 当你想根据相关性分数过滤结果时
     * - 当你想展示每个结果的相关程度时
     * - 当你想对结果进行二次排序时
     *
     * 【示例】
     * 返回结果示例：
     * ┌─────────────────────────────────────────────────────┐
     * │  RetrievalResult(                                  │
     * │    text = "商品名称：华为Mate60\n价格：6999元",      │
     * │    score = 0.95  ← 相关性分数，0~1之间             │
     * │  )                                                 │
     * │  RetrievalResult(                                  │
     * │    text = "商品名称：华为P50\n价格：4999元",        │
     * │    score = 0.87                                    │
     * │  )                                                 │
     * └─────────────────────────────────────────────────────┘
     *
     * @param query 用户查询
     * @param topK 返回数量
     * @return 检索结果列表，每项包含文本和相关性分数
     */
    public List<RetrievalResult> retrieveWithScore(String query, int topK) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, topK, 0.7)
                .stream()
                .map(result -> new RetrievalResult(
                    result.embedded().text(),
                    result.score()))
                .toList();
    }

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     *                          构建上下文字符串（供 LLM 使用）
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * 【这个方法是做什么的？】
     * 把检索到的多个文档内容整合成一个字符串，这个字符串会被发送给 LLM（大型语言模型），
     * 让 LLM 基于这些上下文信息来生成回答。
     *
     * 【为什么需要这个方法？】
     * - retrieve() 返回的是 List<String>，每个元素是一个文档
     * - 但是 LLM 期望的是一整段文字作为上下文
     * - 所以需要把多个文档合并成一个格式化好的字符串
     *
     * 【方法执行流程】
     *
     *   输入: 用户问题 "华为手机有哪些？"
     *   ┌─────────────────────────────────────┐
     *   │  Step 1: 调用 retrieveWithScore()    │
     *   │  检索出最相关的文档                  │
     *   │                                    │
     *   │  结果：                            │
     *   │  - 文档A: 华为Mate60, 6999元        │
     *   │    相关度: 0.95                    │
     *   │  - 文档B: 华为P50, 4999元           │
     *   │    相关度: 0.87                     │
     *   └─────────────────────────────────────┘
     *                  │
     *                  ▼
     *   ┌─────────────────────────────────────┐
     *   │  Step 2: 格式化成交上下文字符串       │
     *   │                                    │
     *   │  输出格式：                         │
     *   │  "以下是相关的参考信息：             │
     *   │                                    │
     *   │  【信息 1】（相关度：0.95）          │
     *   │  商品名称：华为Mate60               │
     *   │  价格：6999元                      │
     *   │                                    │
     *   │  【信息 2】（相关度：0.87）         │
     *   │  商品名称：华为P50                  │
     *   │  价格：4999元"                     │
     *   └─────────────────────────────────────┘
     *
     * 【这个字符串最终去哪里？】
     *
     *   buildContext() 返回的字符串
     *           │
     *           ▼
     *   ┌─────────────────────────────────────┐
     *   │  组装成 Prompt 发送给 LLM            │
     *   │                                     │
     *   │  "用户问题是：华为手机有哪些？       │
     *   │   参考信息如下：                     │
     *   │   [buildContext返回的字符串]         │
     *   │   请根据以上信息回答用户问题"        │
     *   └─────────────────────────────────────┘
     *           │
     *           ▼
     *   ┌─────────────────────────────────────┐
     *   │  LLM 生成最终回答                    │
     *   │  "华为手机有以下几款：..."          │
     *   └─────────────────────────────────────┘
     *
     * @param userQuestion 用户问题
     * @param topK 检索文档数量
     * @return 格式化的上下文字符串，可直接用于组装 Prompt
     */
    public String buildContext(String userQuestion, int topK) {
        List<RetrievalResult> results = retrieveWithScore(userQuestion, topK);
        
        if (results.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("以下是相关的参考信息：\n\n");
        
        for (int i = 0; i < results.size(); i++) {
            context.append(String.format("【信息 %d】（相关度：%.2f）\n%s\n\n", 
                i + 1, results.get(i).score(), results.get(i).text()));
        }
        
        return context.toString();
    }

    /**
     * 清空知识库
     */
    public void clear() {
        embeddingStore.removeAll();
    }

    /**
     * 检索结果封装类
     */
    public record RetrievalResult(String text, double score) {}
}