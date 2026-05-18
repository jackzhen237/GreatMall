package org.example.mall_tiny01.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.analysis.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Elasticsearch 工具类 —— 替代 MySQL LIKE 模糊查询。
 *
 * 内置自定义分词器 "my_pinyin_analyzer"，三级处理：
 * 1. Character Filter（去除标点）：用正则 [\p{Punct}] 把所有标点符号替换为空
 * 2. Tokenizer（分词）：standard 分词器，按空格和标点边界切分词语
 * 3. Token Filter（拼音小写）：pinyin 过滤器把所有中文转为拼音并转为小写
 *
 * 搜索示例：
 * 存 "华为手机" → 分词后 → [huawei, shouji] → 用户搜 "shouji" 命中 "华为手机"
 */
@Component
public class ElasticsearchUtil {

    private final ElasticsearchClient client;

    /** 自定义分词器名称 */
    public static final String ANALYZER_NAME = "my_pinyin_analyzer";

    public ElasticsearchUtil(ElasticsearchClient client) {
        this.client = client;
    }

    // ======================== 索引管理（含自定义分词器）====================

    /**
     * 创建索引并绑定自定义分词器。
     *
     * 自定义分词器 my_pinyin_analyzer 的管道：
     * 1. my_punct_filter (char_filter) → 正则去除标点符号
     * 2. standard (tokenizer) → 标准分词
     * 3. my_pinyin (filter) → 拼音转小写
     *
     * @param indexName 索引名称
     */
    public void createIndexWithAnalyzer(String indexName) {
        try {
            ExistsRequest existsReq = ExistsRequest.of(e -> e.index(indexName));
            if (client.indices().exists(existsReq).value()) {
                return;
            }

            // 1. 字符过滤器：正则去除标点符号
            //    \p{Punct} 匹配所有标点：。，！？；："'（）【】《》…—～,.!?;:"'()
            CharFilter punctFilter = CharFilter.of(cf -> cf
                    .definition(cfd -> cfd
                            .patternReplace(pr -> pr
                                    .pattern("[\\p{Punct}]")
                                    .replacement(""))));

            // 2. 分词器：standard
            Tokenizer tokenizer = Tokenizer.of(t -> t
                    .definition(td -> td.standard(s -> s)));

            // 3. Token 过滤器：拼音转小写
            //    pinyin 是 ES 插件，不在标准规范中，用 _custom 定义
            //    keep_first_letter: false → "华为" 输出完整拼音 "huawei" 而非首字母 "hw"
            //    lowercase: true → 强制转为小写
            Map<String, Object> pinyinConfig = Map.ofEntries(
                    Map.entry("type", "pinyin"),
                    Map.entry("keep_first_letter", false),
                    Map.entry("keep_full_pinyin", true),
                    Map.entry("limit_first_letter_length", 64),
                    Map.entry("lowercase", true),
                    Map.entry("remove_duplicated_term", true));
            TokenFilter pinyinFilter = TokenFilter.of(tf -> tf
                    .definition(tfd -> tfd
                            ._custom("pinyin", pinyinConfig)));

            // 组装自定义分析器
            Analyzer analyzer = Analyzer.of(a -> a
                    .custom(ca -> ca
                            .charFilter("my_punct_filter")
                            .tokenizer("standard")
                            .filter("my_pinyin")));

            // 创建索引，注入分析器定义
            CreateIndexRequest request = CreateIndexRequest.of(c -> c
                    .index(indexName)
                    .settings(s -> s
                            .analysis(an -> an
                                    .charFilter("my_punct_filter", punctFilter)
                                    .tokenizer("standard", tokenizer)
                                    .filter("my_pinyin", pinyinFilter)
                                    .analyzer(ANALYZER_NAME, analyzer))));

            client.indices().create(request);
        } catch (IOException e) {
            throw new RuntimeException("创建带分词器的索引失败: " + indexName, e);
        }
    }

    /**
     * 删除索引。
     */
    public void deleteIndex(String indexName) {
        try {
            client.indices().delete(DeleteIndexRequest.of(d -> d.index(indexName)));
        } catch (IOException e) {
            throw new RuntimeException("删除索引失败: " + indexName, e);
        }
    }

    // ======================== 文档 CRUD ========================

    /**
     * 保存或更新文档。
     *
     * @param indexName 索引名
     * @param id 文档 ID（对应 MySQL 主键，相同 ID 再保存会覆盖）
     * @param document 任意 Java 对象，自动序列化为 JSON
     */
    public void save(String indexName, String id, Object document) {
        try {
            String json = JSON.toJSONString(document);
            IndexRequest<String> req = IndexRequest.of(i -> i
                    .index(indexName).id(id).document(json));
            client.index(req);
        } catch (IOException e) {
            throw new RuntimeException("ES save 失败 index=" + indexName + " id=" + id, e);
        }
    }

    /**
     * 批量保存。
     */
    public void saveBatch(String indexName, Map<String, ?> dataMap) {
        try {
            List<BulkOperation> ops = new ArrayList<>();
            for (Map.Entry<String, ?> entry : dataMap.entrySet()) {
                ops.add(BulkOperation.of(b -> b.index(IndexOperation.of(i -> i
                        .index(indexName)
                        .id(entry.getKey())
                        .document(JsonData.fromJson(JSON.toJSONString(entry.getValue())))))));
            }
            client.bulk(BulkRequest.of(b -> b.operations(ops)));
        } catch (IOException e) {
            throw new RuntimeException("ES saveBatch 失败 index=" + indexName, e);
        }
    }

    /**
     * 根据 ID 删除文档。
     */
    public void delete(String indexName, String id) {
        try {
            client.delete(DeleteRequest.of(d -> d.index(indexName).id(id)));
        } catch (IOException e) {
            throw new RuntimeException("ES delete 失败 index=" + indexName + " id=" + id, e);
        }
    }

    /**
     * 根据 ID 查询。
     */
    public <T> T getById(String indexName, String id, Class<T> clazz) {
        try {
            GetRequest req = GetRequest.of(g -> g.index(indexName).id(id));
            GetResponse<String> resp = client.get(req, String.class);
            if (!resp.found()) return null;
            return JSON.parseObject(resp.source(), clazz);
        } catch (IOException e) {
            throw new RuntimeException("ES get 失败 index=" + indexName + " id=" + id, e);
        }
    }

    // ======================== 搜索 ========================

    /**
     * 单字段全文搜索（使用自定义拼音分词器）。
     * 示例：用户输入 "shouji" 能搜到 "手机" 相关数据。
     */
    public <T> List<T> search(String indexName, String field, String keyword, Class<T> clazz) {
        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(q -> q.match(m -> m.field(field).query(keyword).analyzer(ANALYZER_NAME))));
            return parseResponse(client.search(req, String.class), clazz);
        } catch (IOException e) {
            throw new RuntimeException("ES search 失败 index=" + indexName, e);
        }
    }

    /**
     * 多字段搜索。
     */
    public <T> List<T> multiSearch(String indexName, List<String> fields, String keyword, Class<T> clazz) {
        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(q -> q.multiMatch(m -> m.fields(fields).query(keyword).analyzer(ANALYZER_NAME))));
            return parseResponse(client.search(req, String.class), clazz);
        } catch (IOException e) {
            throw new RuntimeException("ES multiSearch 失败 index=" + indexName, e);
        }
    }

    /**
     * 分页搜索。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     */
    public <T> List<T> searchPage(String indexName, String field, String keyword,
                                   int page, int size, Class<T> clazz) {
        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(indexName)
                    .from((page - 1) * size)
                    .size(size)
                    .query(q -> q.match(m -> m.field(field).query(keyword).analyzer(ANALYZER_NAME))));
            return parseResponse(client.search(req, String.class), clazz);
        } catch (IOException e) {
            throw new RuntimeException("ES searchPage 失败 index=" + indexName, e);
        }
    }

    /**
     * 搜索结果总数。
     */
    public long count(String indexName, String field, String keyword) {
        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(indexName)
                    .trackTotalHits(th -> th.enabled(true))
                    .size(0)
                    .query(q -> q.match(m -> m.field(field).query(keyword).analyzer(ANALYZER_NAME))));
            SearchResponse<String> resp = client.search(req, String.class);
            return resp.hits().total() != null ? resp.hits().total().value() : 0;
        } catch (IOException e) {
            throw new RuntimeException("ES count 失败 index=" + indexName, e);
        }
    }

    // ======================== 内部方法 ========================

    private <T> List<T> parseResponse(SearchResponse<String> response, Class<T> clazz) {
        return response.hits().hits().stream()
                .map(hit -> JSON.parseObject(hit.source(), clazz))
                .collect(Collectors.toList());
    }
}
