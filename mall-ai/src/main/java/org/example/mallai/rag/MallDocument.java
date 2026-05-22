package org.example.mallai.rag;

import dev.langchain4j.document.Document;
import dev.langchain4j.document.Metadata;
import java.util.Map;

/**
 * 商城文档转换工具类
 *
 * 【什么是 Document？】
 * 在 RAG 系统中，Document 是承载文档内容的基本单元
 * 每个 Document 包含：
 * - content：文档的实际文本内容（如 "商品名称：华为Mate60，价格：6999元"）
 * - metadata：元数据（如 商品ID、品牌名、分类等，用于后续过滤和追溯）
 *
 * 【这个类的作用】
 * 将商城中的各类数据（商品信息、品牌信息、分类信息等）
 * 转换成 langchain4j 需要的 Document 格式
 * 转换后的 Document 就可以被向量化并存储到向量数据库中
 *
 * 【文档类型说明】
 * - product: 商品文档 - 包含商品名称、价格、属性、描述等
 * - brand: 品牌文档 - 包含品牌名称、品牌故事、国家、特色等
 * - category: 分类文档 - 包含分类名称、父分类、描述等
 * - help: 帮助文档 - 包含常见问题及答案
 *
 * 【使用示例】
 * // 创建一个商品文档
 * Document doc = MallDocument.createProductDocument(
 *     1L,                    // 商品ID
 *     "华为Mate60",          // 商品名称
 *     "华为",                // 品牌名称
 *     "手机",                // 分类名称
 *     "旗舰手机",            // 商品描述
 *     "6999元",              // 价格
 *     "5G支持、卫星通话"      // 商品属性
 * );
 */
public class MallDocument {

    /**
     * 创建商品文档
     *
     * 【商品文档包含哪些信息？】
     * - 商品名称（用户搜索的主要关键词）
     * - 品牌名称（用于品牌筛选和比较）
     * - 分类名称（用于分类筛选）
     * - 价格（用户决策的重要因素）
     * - 商品属性（如：是否支持5G、屏幕尺寸、电池容量等）
     * - 商品描述（详细的商品介绍）
     *
     * 【为什么要存储这些信息？】
     * 当用户提问如 "推荐一款性价比高的华为手机" 时：
     * 1. 可以检索到品牌为"华为"的商品文档
     * 2. 检索到分类为"手机"的商品文档
     * 3. LLM 根据这些检索结果生成推荐回答
     *
     * @param productId 商品ID - 用于唯一标识和追溯
     * @param productName 商品名称 - 用户搜索的主要关键词
     * @param brandName 品牌名称 - 用于品牌相关的问答和筛选
     * @param categoryName 分类名称 - 用于分类相关的问答和筛选
     * @param description 商品描述 - 提供详细的商品信息
     * @param price 价格 - 用户决策的重要因素
     * @param attributes 商品属性 - 如：支持5G、屏幕尺寸、电池容量等
     * @return Document 格式的商品文档，可直接存入向量数据库
     */
    public static Document createProductDocument(
            Long productId,
            String productName,
            String brandName,
            String categoryName,
            String description,
            String price,
            String attributes) {

        // 【content 内容构建】
        // 使用文本格式化模板，将商品信息组织成易读的文本格式
        // 这些文本后续会被 Embedding 模型转换为向量
        String content = String.format("""
            商品名称：%s
            品牌：%s
            分类：%s
            价格：%s
            商品属性：%s
            商品描述：%s
            """, productName, brandName, categoryName, price, attributes, description);

        // 【metadata 元数据构建】
        // 元数据不参与向量计算，但非常重要：
        // 1. 用于追溯：这个文档对应哪个商品
        // 2. 用于过滤：可以按品牌、分类筛选文档
        // 3. 用于关联：可以关联到原始数据库中的商品记录
        Metadata metadata = Metadata.from(Map.of(
            "productId", String.valueOf(productId),
            "type", "product",  // 文档类型标识，便于区分不同类型文档
            "brandName", brandName,
            "categoryName", categoryName
        ));

        // 【Document.from() 的作用】
        // 将 content（文本内容）和 metadata（元数据）组合成一个 Document 对象
        // 这个 Document 对象后续会被：
        // 1. EmbeddingModel 转换为向量
        // 2. 存储到 EmbeddingStore（向量数据库）
        return Document.from(content, metadata);
    }

    /**
     * 创建品牌文档
     *
     * 【品牌文档包含哪些信息？】
     * - 品牌名称（用户搜索的品牌关键词）
     * - 品牌故事（增加用户对品牌的了解）
     * - 品牌首字母（用于分类索引）
     * - 品牌信息（如：品牌产品数量、评论数等）
     *
     * 【品牌文档的用途】
     * 当用户提问如 "苹果和华为哪个好？" 时：
     * 1. 检索到"苹果"和"华为"的品牌文档
     * 2. 获取两个品牌的特色和故事
     * 3. LLM 根据这些信息进行客观比较和推荐
     *
     * @param brandId 品牌ID - 用于唯一标识和追溯
     * @param brandName 品牌名称 - 用户搜索的主要关键词
     * @param brandStory 品牌故事 - 品牌历史和发展介绍
     * @param firstLetter 首字母 - 用于分类索引
     * @param productCount 产品数量 - 品牌的商品数量
     * @return Document 格式的品牌文档
     */
    public static Document createBrandDocument(
            Long brandId,
            String brandName,
            String brandStory,
            String firstLetter,
            Integer productCount) {

        String content = String.format("""
            品牌名称：%s
            首字母：%s
            产品数量：%s
            品牌故事：%s
            """, brandName, firstLetter, productCount, brandStory);

        Metadata metadata = Metadata.from(Map.of(
            "brandId", String.valueOf(brandId),
            "type", "brand",
            "brandName", brandName
        ));

        return Document.from(content, metadata);
    }

    /**
     * 创建分类文档
     *
     * 【分类文档包含哪些信息？】
     * - 分类名称（用户搜索的分类关键词）
     * - 父分类（了解分类层级关系，如：手机 -> 电子产品）
     * - 分类描述（分类的具体内容）
     * - 分类特点（该分类的整体特点）
     *
     * 【分类文档的用途】
     * 当用户提问如 "有哪些手机推荐？" 时：
     * 1. 先定位到"手机"分类
     * 2. 获取该分类下的热门商品
     * 3. 返回推荐结果
     *
     * @param categoryId 分类ID
     * @param categoryName 分类名称
     * @param parentCategoryName 父分类名称
     * @param description 分类描述
     * @param features 分类特点
     * @return Document 格式的分类文档
     */
    public static Document createCategoryDocument(
            Long categoryId,
            String categoryName,
            String parentCategoryName,
            String description,
            String features) {

        String content = String.format("""
            分类名称：%s
            父分类：%s
            分类描述：%s
            分类特点：%s
            """, categoryName, parentCategoryName, description, features);

        Metadata metadata = Metadata.from(Map.of(
            "categoryId", String.valueOf(categoryId),
            "type", "category",
            "categoryName", categoryName
        ));

        return Document.from(content, metadata);
    }

    /**
     * 创建帮助文档
     *
     * 【帮助文档的用途】
     * 当用户提问如 "如何申请退货？" 时：
     * 1. 检索到相关的帮助文档
     * 2. 获取退货流程和注意事项
     * 3. LLM 将帮助信息整理成易懂的回答
     *
     * 【帮助文档的特点】
     * 与商品/品牌文档不同，帮助文档更注重：
     * - 问题表述的准确性（用户可能用不同方式提问同一个问题）
     * - 答案的实用性（步骤清晰、可操作）
     *
     * @param helpId 帮助ID
     * @param question 问题
     * @param answer 答案
     * @param category 分类
     * @return Document 格式的帮助文档
     */
    public static Document createHelpDocument(
            Long helpId,
            String question,
            String answer,
            String category) {

        String content = String.format("""
            问题：%s
            答案：%s
            分类：%s
            """, question, answer, category);

        Metadata metadata = Metadata.from(Map.of(
            "helpId", String.valueOf(helpId),
            "type", "help",
            "category", category
        ));

        return Document.from(content, metadata);
    }
}