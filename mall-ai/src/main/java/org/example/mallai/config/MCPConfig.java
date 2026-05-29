package org.example.mallai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 配置类
 * 
 * 功能说明：
 * - 配置 MCP 服务器连接信息
 * - 管理可使用的 MCP 工具列表
 * 
 * 使用方式：
 * - 在 application.properties 中配置 MCP 服务器地址
 * - Agent 会通过此配置连接 MCP 服务器并调用工具
 */
@Configuration
public class MCPConfig {

    @Value("${mcp.server.url:http://localhost:8000}")
    private String mcpServerUrl;

    @Value("${mcp.enabled:true}")
    private boolean mcpEnabled;

    public String getMcpServerUrl() {
        return mcpServerUrl;
    }

    public boolean isMcpEnabled() {
        return mcpEnabled;
    }
}
