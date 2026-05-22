package org.example.mallai.tools;

import dev.langchain4j.agent.tool.Tool;
import org.example.mall_tiny01.dto.Result;
import org.example.mall_tiny01.feign.PmsFeignClient;
import org.example.mall_tiny01.feign.UmsFeignClient;
import org.example.mall_tiny01.mbg.model.PmsBrand;
import org.example.mall_tiny01.mbg.model.PmsProduct;
import org.example.mall_tiny01.dto.PmsPortalProductDetail;
import org.example.mallai.config.MCPConfig;
import org.springframework.stereotype.Component;

/**
 * MCP 工具集成类
 * 
 * 功能说明：
 * - 封装 MCP 协议的工具调用
 * - 提供 Agent 与 MCP 服务器交互的桥接
 * 
 * 使用方式：
 * - Agent 通过 @Tool 注解的方法调用 MCP 工具
 * - 工具执行结果会返回给 Agent 继续处理
 */
@Component
public class MCPTools {

    private final MCPConfig mcpConfig;
    private final PmsFeignClient pmsFeignClient;
    private final UmsFeignClient umsFeignClient;

    public MCPTools(MCPConfig mcpConfig, PmsFeignClient pmsFeignClient, UmsFeignClient umsFeignClient) {
        this.mcpConfig = mcpConfig;
        this.pmsFeignClient = pmsFeignClient;
        this.umsFeignClient = umsFeignClient;
    }

    /**
     * 调用 MCP 工具：读取文件
     * 
     * @param filePath 文件路径
     * @return 文件内容
     */
    @Tool("读取文件内容")
    public String readFile(String filePath) {
        // 这里实现读取文件的逻辑，可以通过 MCP 协议调用外部文件系统工具
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        return "从 MCP 文件系统读取文件：" + filePath;
    }

    /**
     * 调用 MCP 工具：写入文件
     * 
     * @param filePath 文件路径
     * @param content 文件内容
     * @return 操作结果
     */
    @Tool("写入文件内容")
    public String writeFile(String filePath, String content) {
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        return "通过 MCP 文件系统写入文件：" + filePath;
    }

    /**
     * 调用 MCP 工具：执行命令
     * 
     * @param command 要执行的命令
     * @return 命令执行结果
     */
    @Tool("执行系统命令")
    public String executeCommand(String command) {
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        return "通过 MCP 执行命令：" + command;
    }

    /**
     * 通过 Feign 远程调用：查询商品详情
     * 
     * 调用链路：
     * mall-ai → mall-api (PmsFeignClient) → mall-pms → CacheQueryService 多级缓存
     * 
     * @param productId 商品ID
     * @return 商品详情信息
     */
    @Tool("查询商品详情")
    public String queryProductDetail(Long productId) {
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        Result<PmsPortalProductDetail> result = pmsFeignClient.getProductDetail(productId);
        if (result != null && result.isSuccess()) {
            PmsPortalProductDetail product = result.getData();
            return String.format("商品ID:%d, 商品名称:%s, 价格:%s", 
                product.getProduct().getId(), 
                product.getProduct().getName(),
                product.getProduct().getPrice());
        }
        return "查询失败:" + (result != null ? result.getMessage() : "未知错误");
    }

    /**
     * 通过 Feign 远程调用：查询商品基本信息
     * 
     * @param productId 商品ID
     * @return 商品信息
     */
    @Tool("查询商品信息")
    public String queryProduct(Long productId) {
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        Result<PmsProduct> result = pmsFeignClient.getProduct(productId);
        if (result != null && result.isSuccess()) {
            PmsProduct product = result.getData();
            return String.format("商品ID:%d, 商品名称:%s", product.getId(), product.getName());
        }
        return "查询失败:" + (result != null ? result.getMessage() : "未知错误");
    }

    /**
     * 通过 Feign 远程调用：查询品牌详情
     * 
     * @param brandId 品牌ID
     * @return 品牌信息
     */
    @Tool("查询品牌详情")
    public String queryBrandDetail(Long brandId) {
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        Result<PmsBrand> result = pmsFeignClient.getBrandDetail(brandId);
        if (result != null && result.isSuccess()) {
            PmsBrand brand = result.getData();
            return String.format("品牌ID:%d, 品牌名称:%s", brand.getId(), brand.getName());
        }
        return "查询失败:" + (result != null ? result.getMessage() : "未知错误");
    }

    /**
     * 通过 Feign 远程调用：查询购物车商品信息
     * 
     * @param productId 商品ID
     * @return 购物车商品信息
     */
    @Tool("查询购物车商品")
    public String queryCartProduct(Long productId) {
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        return "购物车商品查询 - 商品ID:" + productId;
    }
    
    /**
     * 调用 MCP 工具：浏览网页
     * 
     * @param url 网页地址
     * @return 网页内容
     */
    @Tool("浏览网页")
    public String browseWeb(String url) {
        if (!mcpConfig.isMcpEnabled()) {
            return "MCP 功能未启用";
        }
        return "通过 MCP 浏览网页：" + url;
    }
}