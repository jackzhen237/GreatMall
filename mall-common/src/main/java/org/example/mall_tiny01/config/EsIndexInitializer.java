package org.example.mall_tiny01.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动创建所有 ES 索引（使用 my_pinyin_analyzer 分词器）。
 * 每个索引只创建一次，已存在则跳过。
 */
@Component
public class EsIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EsIndexInitializer.class);

    private final ElasticsearchUtil esUtil;

    public EsIndexInitializer(ElasticsearchUtil esUtil) {
        this.esUtil = esUtil;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化 ES 索引...");

        // ========== mall-pms 索引 ==========
        safeCreate("pms_product");
        safeCreate("pms_brand");
        safeCreate("pms_sku_stock");
        safeCreate("pms_category");
        safeCreate("pms_product_attribute");
        safeCreate("pms_product_attribute_category");

        // ========== mall-oms 索引（后续模块使用）==========
        safeCreate("oms_order");

        // ========== mall-sms 索引（后续模块使用）==========
        safeCreate("sms_coupon");

        // ========== mall-cms 索引（后续模块使用）==========
        safeCreate("cms_subject");

        // ========== mall-ums 索引（后续模块使用）==========
        safeCreate("ums_admin");

        log.info("ES 索引初始化完成");
    }

    private void safeCreate(String indexName) {
        try {
            esUtil.createIndexWithAnalyzer(indexName);
            log.info("ES 索引就绪: {}", indexName);
        } catch (Exception e) {
            log.error("ES 索引创建失败: {}，请检查 ES 是否已启动", indexName, e);
        }
    }
}
