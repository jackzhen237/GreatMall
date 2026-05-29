package org.example.mallai.rag;

import dev.langchain4j.data.document.Document;
import org.example.mall_tiny01.dto.PageResult;
import org.example.mall_tiny01.feign.PmsFeignClient;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 *                              知识库初始化器
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 *
 * 【什么是知识库初始化？】
 * 在 RAG 系统中，我们需要先把"知识"存入向量数据库，这个过程叫"构建知识库"。
 * KnowledgeBaseInitializer 的作用就是：在应用启动时，自动把商城的商品、品牌等数据加载到向量数据库中。
 *
 *
 * 【为什么需要这个类？】
 *
 *   传统方式：
 *   用户问："华为手机有哪些？"
 *   AI 回答：不知道（因为AI没有商城的知识）
 *
 *   使用 KnowledgeBaseInitializer 后：
 *   应用启动时 ──▶ 从数据库读取品牌、商品数据 ──▶ 存入向量数据库
 *   用户问："华为手机有哪些？"
 *   AI 回答：华为Mate60、华为P50...（基于知识库检索到的真实数据回答）
 *
 *
 * 【这个类在 RAG 系统中的位置】
 *
 *   ┌─────────────────────────────────────────────────────────────────────────┐
 *   │                           应用启动时执行                                 │
 *   └─────────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *                    ┌───────────────────────────────────┐
 *                    │  KnowledgeBaseInitializer         │
 *                    │                                   │
 *                    │  1. 通过 Feign 调用 mall-pms       │
 *                    │     获取品牌、商品数据             │
 *                    │                                   │
 *                    │  2. 调用 MallDocument 转换为       │
 *                    │     Document 格式                 │
 *                    │                                   │
 *                    │  3. 调用 RagService.addDocument()  │
 *                    │     存入向量数据库                 │
 *                    └───────────────────────────────────┘
 *                                    │
 *                                    ▼
 *                    ┌───────────────────────────────────┐
 *                    │        向量数据库                  │
 *                    │  - 品牌文档 1: 华为品牌介绍...     │
 *                    │  - 品牌文档 2: 苹果品牌介绍...     │
 *                    │  - 商品文档 1: 华为Mate60...       │
 *                    │  - 商品文档 2: 苹果iPhone15...    │
 *                    └───────────────────────────────────┘
 *
 *
 * 【CommandLineRunner 是什么？】
 * - 这是一个 Spring Boot 接口
 * - 实现了这个接口的 Bean，会在应用启动完成后自动执行 run() 方法
 * - 类似于 @PostConstruct，但更适合执行启动时的初始化任务
 *
 *
 * 【数据来源】
 * - 通过 PmsFeignClient（OpenFeign 客户端）调用 mall-pms 微服务
 * - 获取品牌数据：listAllBrands()、getBrandDetail(id)
 * - 获取商品数据：类似的方式
 *
 *
 * 【使用流程】
 * 1. 应用启动
 * 2. Spring 检测到实现了 CommandLineRunner 接口
 * 3. 自动调用 run() 方法
 * 4. run() 方法中调用 initializeBrandKnowledge() 和 initializeProductKnowledge()
 * 5. 品牌和商品数据被加载到向量数据库
 * 6. 用户可以开始使用 RAG 检索功能
 *
 * @author mall-ai
 * @version 1.0
 */
@Component
public class KnowledgeBaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseInitializer.class);

    private final RagService ragService;
    private final PmsFeignClient pmsFeignClient;

    public KnowledgeBaseInitializer(RagService ragService, PmsFeignClient pmsFeignClient) {
        this.ragService = ragService;
        this.pmsFeignClient = pmsFeignClient;
    }

    @Override
    public void run(String... args) {
        System.out.println("========== 开始初始化商城知识库 ==========");
        
        try {
            // 1. 初始化品牌知识库
            initializeBrandKnowledge();
            
            // 2. 初始化商品知识库
            initializeProductKnowledge();
            
            System.out.println("========== 商城知识库初始化完成 ==========");
        } catch (Exception e) {
            System.err.println("知识库初始化失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     *                              初始化品牌知识库
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * 【这个方法是做什么的？】
     * 从 mall-pms 微服务获取所有品牌数据，转换为 Document 格式，然后存入向量数据库。
     *
     * 【方法执行流程】
     *
     *   ┌─────────────────────────────────────────────────────────────────┐
     *   │  Step 1: 通过 Feign 调用 mall-pms 获取品牌数据                  │
     *   │                                                               │
     *   │  pmsFeignClient.listAllBrands()                               │
     *   │         │                                                     │
     *   │         ▼                                                     │
     *   │  返回 Result<List<PmsBrand>>                                   │
     *   │  例如：[PmsBrand(华为), PmsBrand(苹果), PmsBrand(小米), ...]   │
     *   └─────────────────────────────────────────────────────────────────┘
     *                         │
     *                         ▼
     *   ┌─────────────────────────────────────────────────────────────────┐
     *   │  Step 2: 遍历每个品牌，转换为 Document 格式                     │
     *   │                                                               │
     *   │  for (brand : brands) {                                        │
     *   │      MallDocument.createBrandDocument(                         │
     *   │          brand.getId(),     // 品牌ID                          │
     *   │          brand.getName(),   // 品牌名称                        │
     *   │          brand.getBrandStory(), // 品牌故事                    │
     *   │          brand.getFirstLetter(), // 首字母                     │
     *   │          brand.getProductCount() // 产品数量                   │
     *   │      )                                                         │
     *   │  }                                                             │
     *   │                                                               │
     *   │  转换为 Document：                                              │
     *   │  ┌─────────────────────────────────────────────┐               │
     *   │  │  Document 内容示例：                        │               │
     *   │  │  "品牌名称：华为                            │               │
     *   │  │   首字母：H                                │               │
     *   │  │   产品数量：100                            │               │
     *   │  │   品牌故事：华为是全球领先的..."           │               │
     *   │  │                                            │               │
     *   │  │  Metadata:                                 │               │
     *   │  │  - brandId: 1                              │               │
     *   │  │  - type: brand                             │               │
     *   │  │  - brandName: 华为                         │               │
     *   │  └─────────────────────────────────────────────┘               │
     *   └─────────────────────────────────────────────────────────────────┘
     *                         │
     *                         ▼
     *   ┌─────────────────────────────────────────────────────────────────┐
     *   │  Step 3: 调用 RagService.addDocuments() 批量存入向量数据库        │
     *   │                                                               │
     *   │  ragService.addDocuments(brandDocs)                            │
     *   │         │                                                     │
     *   │         ▼                                                     │
     *   │  向量数据库中新增：                                            │
     *   │  - 文档1: 华为品牌介绍...（向量：[0.123, ...]）               │
     *   │  - 文档2: 苹果品牌介绍...（向量：[0.234, ...]）               │
     *   │  - 文档3: 小米品牌介绍...（向量：[0.345, ...]）               │
     *   └─────────────────────────────────────────────────────────────────┘
     *
     *
     * 【什么是 PmsBrand？】
     * - 这是 mall-pms 微服务中的品牌实体类
     * - 包含品牌的各种属性：id、name、logo、brandStory、firstLetter 等
     * - 通过 Feign 调用获取远程数据
     *
     *
     * 【什么是 MallDocument.createBrandDocument？】
     * - 这是一个工具方法（在 MallDocument 类中）
     * - 负责把 PmsBrand 对象转换成 Document 对象
     * - 因为 RAG 系统只认识 Document 格式的数据
     */
    private void initializeBrandKnowledge() {
        logger.info("正在加载品牌知识库...");

        // 调用 listAllBrands() 一次性获取所有品牌
        Result<List<PmsBrand>> result = pmsFeignClient.listAllBrands();

        if (result != null && result.getCode() != null && result.getCode() == 200 && result.getData() != null) {
            List<Document> brandDocs = result.getData().stream()
                .map(brand -> MallDocument.createBrandDocument(
                    brand.getId(),
                    brand.getName(),
                    brand.getBrandStory(),
                    brand.getFirstLetter(),
                    brand.getProductCount()
                ))
                .toList();

            ragService.addDocuments(brandDocs);
            logger.info("品牌知识库加载完成，共 {} 个品牌", brandDocs.size());
        } else {
            logger.warn("品牌知识库加载失败：{}", result != null ? "状态码=" + result.getCode() : "结果为空");
        }
    }

    /**
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     *                              初始化商品知识库
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * 【这个方法是做什么的？】
     * 从 mall-pms 微服务获取所有商品数据，转换为 Document 格式，然后存入向量数据库。
     *
     * 【方法执行流程】
     *
     *   ┌─────────────────────────────────────────────────────────────────┐
     *   │  Step 1: 通过 Feign 调用 mall-pms 获取商品数据                  │
     *   │                                                               │
     *   │  pmsFeignClient.listProducts(pageNum, pageSize)              │
     *   │         │                                                     │
     *   │         ▼                                                     │
     *   │  返回 Result<PageResult<PmsProduct>>                           │
     *   │  例如：PageResult{list=[PmsProduct(华为Mate60), ...], total=100}  │
     *   └─────────────────────────────────────────────────────────────────┘
     *                         │
     *                         ▼
     *   ┌─────────────────────────────────────────────────────────────────┐
     *   │  Step 2: 遍历每个商品，转换为 Document 格式                     │
     *   │                                                               │
     *   │  for (product : products) {                                   │
     *   │      MallDocument.createProductDocument(                       │
     *   │          product.getId(),      // 商品ID                       │
     *   │          product.getName(),    // 商品名称                     │
     *   │          product.getBrandName(), // 品牌名称                  │
     *   │          product.getProductCategoryName(), // 分类名称         │
     *   │          product.getDescription(), // 描述                     │
     *   │          product.getPrice().toString(), // 价格                 │
     *   │          product.getKeywords()  // 关键词                      │
     *   │      )                                                         │
     *   │  }                                                             │
     *   │                                                               │
     *   │  转换为 Document：                                              │
     *   │  ┌─────────────────────────────────────────────┐               │
     *   │  │  Document 内容示例：                        │               │
     *   │  │  "商品名称：华为Mate60                      │               │
     *   │  │   品牌：华为                               │               │
     *   │  │   分类：手机                               │               │
     *   │  │   价格：6999元                             │               │
     *   │  │   商品属性：5G支持、卫星通话                │               │
     *   │  │   商品描述：华为最新款旗舰手机..."          │               │
     *   │  │                                            │               │
     *   │  │  Metadata:                                 │               │
     *   │  │  - productId: 1                            │               │
     *   │  │  - type: product                           │               │
     *   │  │  - brandName: 华为                         │               │
     *   │  │  - categoryName: 手机                      │               │
     *   │  └─────────────────────────────────────────────┘               │
     *   └─────────────────────────────────────────────────────────────────┘
     *                         │
     *                         ▼
     *   ┌─────────────────────────────────────────────────────────────────┐
     *   │  Step 3: 调用 RagService.addDocuments() 批量存入向量数据库        │
     *   │                                                               │
     *   │  ragService.addDocuments(productDocs)                          │
     *   │         │                                                     │
     *   │         ▼                                                     │
     *   │  向量数据库中新增：                                            │
     *   │  - 文档1: 华为Mate60...（向量：[0.123, ...]）                │
     *   │  - 文档2: 苹果iPhone15...（向量：[0.234, ...]）              │
     *   │  - 文档3: 小米14...（向量：[0.345, ...]）                    │
     *   └─────────────────────────────────────────────────────────────────┘
     *
     *
     * 【什么是 PmsProduct？】
     * - 这是 mall-pms 微服务中的商品实体类
     * - 包含商品的各种属性：id、name、price、brandName、description 等
     * - 通过 Feign 调用获取远程数据
     *
     *
     * 【为什么使用分页查询？】
     * - 商品数据可能非常多（成千上万）
     * - 一次性加载可能导致内存溢出
     * - 分页查询可以分批加载，更加安全
     */
    private void initializeProductKnowledge() {
        logger.info("正在加载商品知识库...");

        int pageNum = 1;
        int pageSize = 100;
        int totalLoaded = 0;

        while (true) {
            // 分页获取商品数据
            Result<PageResult<PmsProduct>> result = pmsFeignClient.listProducts(pageNum, pageSize);

            if (result == null || result.getCode() != 200 || result.getData() == null) {
                logger.warn("商品知识库加载失败：{}", result != null ? "状态码=" + result.getCode() : "结果为空");
                break;
            }

            PageResult<PmsProduct> pageResult = result.getData();
            List<PmsProduct> products = pageResult.getList();

            if (products == null || products.isEmpty()) {
                logger.info("商品知识库加载完成，共 {} 个商品", totalLoaded);
                break;
            }

            // 转换为 Document
            List<Document> productDocs = products.stream()
                .map(product -> MallDocument.createProductDocument(
                    product.getId(),
                    product.getName(),
                    product.getBrandName(),
                    product.getProductCategoryName(),
                    product.getDescription(),
                    product.getPrice() != null ? product.getPrice().toString() : "",
                    product.getKeywords()
                ))
                .toList();

            // 添加到向量数据库
            ragService.addDocuments(productDocs);
            totalLoaded += productDocs.size();

            logger.info("已加载第 {} 页商品，累计 {} 个", pageNum, totalLoaded);

            // 检查是否还有下一页
            if (totalLoaded >= pageResult.getTotal()) {
                logger.info("商品知识库加载完成，共 {} 个商品", totalLoaded);
                break;
            }

            pageNum++;
        }
    }
}