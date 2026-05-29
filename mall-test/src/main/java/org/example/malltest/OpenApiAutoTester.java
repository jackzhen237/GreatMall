package org.example.malltest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OpenApiAutoTester {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build();

    private static String token = "";

    // 微服务端口配置 - 根据网关路由规则
    private static final Map<String, String> SERVICE_PORTS = new HashMap<>();
    static {
        SERVICE_PORTS.put("mall-pms", "8081");      // 商品服务
        SERVICE_PORTS.put("mall-oms", "8082");      // 订单服务
        SERVICE_PORTS.put("mall-ums", "8083");      // 用户服务
        SERVICE_PORTS.put("mall-sms", "8084");      // 营销服务
        SERVICE_PORTS.put("mall-cms", "8086");      // 内容服务
        SERVICE_PORTS.put("mall-admin", "8085");    // 后台管理
    }

    // 路径前缀到服务名的映射
    private static final Map<String, String> PATH_TO_SERVICE = new HashMap<>();
    static {
        // mall-pms (商品服务)
        PATH_TO_SERVICE.put("/product", "mall-pms");
        PATH_TO_SERVICE.put("/brand", "mall-pms");
        PATH_TO_SERVICE.put("/sku", "mall-pms");
        PATH_TO_SERVICE.put("/productCategory", "mall-pms");
        PATH_TO_SERVICE.put("/productAttribute", "mall-pms");
        
        // mall-oms (订单服务)
        PATH_TO_SERVICE.put("/order", "mall-oms");
        PATH_TO_SERVICE.put("/cart", "mall-oms");
        
        // mall-ums (用户服务) - /admin 路径匹配多个服务，需要特殊处理
        PATH_TO_SERVICE.put("/sso", "mall-ums");
        PATH_TO_SERVICE.put("/member", "mall-ums");
        
        // mall-sms (营销服务)
        PATH_TO_SERVICE.put("/coupon", "mall-sms");
        PATH_TO_SERVICE.put("/flash", "mall-sms");
        PATH_TO_SERVICE.put("/home", "mall-sms");
        
        // mall-cms (内容服务)
        PATH_TO_SERVICE.put("/subject", "mall-cms");
        PATH_TO_SERVICE.put("/prefrenceArea", "mall-cms");
    }

    public static class TestResult {
        public String method;
        public String path;
        public String summary;
        public String service;
        public int status;
        public boolean success;
        public String error;
        public long duration;

        @Override
        public String toString() {
            return String.format("[%s] %s (%s) - %s (%d) %s", 
                    method, path, service, summary, status, success ? "✓" : "✗ " + error);
        }
    }

    public static void main(String[] args) {
        try {
            String openApiFile;
            
            if (args.length > 0) {
                openApiFile = args[0];
            } else {
                openApiFile = "C:/Users/23733/Desktop/mall后台系统.openapi.json";
            }

            System.out.println("🚀 开始自动测试微服务接口...");
            System.out.println("📄 OpenAPI文件: " + openApiFile);
            System.out.println("==========================================");
            
            // 显示微服务配置
            System.out.println("\n📋 微服务端口配置:");
            for (Map.Entry<String, String> entry : SERVICE_PORTS.entrySet()) {
                System.out.println("  - " + entry.getKey() + ": http://localhost:" + entry.getValue());
            }

            // 1. 先登录获取token (登录接口在mall-ums服务)
            System.out.println("\n🔐 正在登录获取Token...");
            String umsUrl = "http://localhost:" + SERVICE_PORTS.get("mall-ums");
            token = login(umsUrl);
            if (!token.isEmpty()) {
                System.out.println("✅ 登录成功，Token已获取");
            } else {
                System.out.println("⚠️ 登录失败，将尝试不携带Token测试");
            }

            // 2. 测试所有接口
            List<TestResult> results = testOpenApiFile(openApiFile);

            // 3. 按服务分组统计
            Map<String, int[]> serviceStats = new HashMap<>();
            for (TestResult result : results) {
                serviceStats.computeIfAbsent(result.service, k -> new int[]{0, 0});
                serviceStats.get(result.service)[0]++; // 总数
                if (result.success) {
                    serviceStats.get(result.service)[1]++; // 通过数
                }
            }

            // 4. 输出测试报告
            System.out.println("\n==========================================");
            System.out.println("📊 测试报告:");
            System.out.println("==========================================");

            int total = results.size();
            int passed = 0;
            int failed = 0;

            for (TestResult result : results) {
                if (result.success) {
                    passed++;
                    System.out.println("✅ [" + result.method + "] " + result.path + " (" + result.service + ") - " + result.summary + " (" + result.status + ")");
                } else {
                    failed++;
                    System.out.println("❌ [" + result.method + "] " + result.path + " (" + result.service + ") - " + result.summary + " - " + result.error);
                }
            }

            // 按服务分组汇总
            System.out.println("\n📈 按服务分组统计:");
            for (Map.Entry<String, int[]> entry : serviceStats.entrySet()) {
                String service = entry.getKey();
                int[] stats = entry.getValue();
                int serviceTotal = stats[0];
                int servicePassed = stats[1];
                int serviceFailed = serviceTotal - servicePassed;
                System.out.println("  " + service + ": " + servicePassed + "/" + serviceTotal + " 通过");
            }

            System.out.println("\n==========================================");
            System.out.println("📈 测试总结:");
            System.out.println("  总接口数: " + total);
            System.out.println("  通过: " + passed);
            System.out.println("  失败: " + failed);
            System.out.println("==========================================");

        } catch (Exception e) {
            System.err.println("❌ 测试过程发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String login(String baseUrl) {
        try {
            String json = "{\"username\":\"test\",\"password\":\"123456\"}";
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url(baseUrl + "/admin/login")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            System.out.println("   登录响应状态: " + response.code());
            
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("   登录响应内容: " + responseBody);
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode data = root.get("data");
                if (data != null && data.has("token")) {
                    String tokenValue = data.get("token").asText();
                    System.out.println("   获取到Token: " + tokenValue.substring(0, Math.min(20, tokenValue.length())) + "...");
                    if (tokenValue.startsWith("Bearer ")) {
                        return tokenValue.substring(7);
                    }
                    return tokenValue;
                } else {
                    System.out.println("   警告: 响应中没有找到data.token字段");
                }
            } else {
                System.out.println("   登录失败: " + response.message());
            }
        } catch (Exception e) {
            System.err.println("⚠️ 登录请求异常: " + e.getMessage());
        }
        return "";
    }

    private static String getServiceByPath(String path) {
        // 特殊处理 /admin 路径 - 根据二级路径判断
        if (path.startsWith("/admin/order")) {
            return "mall-oms";
        } else if (path.startsWith("/admin")) {
            return "mall-ums";
        }
        
        // 按路径前缀匹配
        for (Map.Entry<String, String> entry : PATH_TO_SERVICE.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 默认返回mall-ums
        return "mall-ums";
    }

    private static String getServiceUrl(String serviceName) {
        String port = SERVICE_PORTS.get(serviceName);
        return "http://localhost:" + (port != null ? port : "8083");
    }

    public static List<TestResult> testOpenApiFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("❌ OpenAPI文件不存在: " + filePath);
            return new ArrayList<>();
        }

        JsonNode root = objectMapper.readTree(file);
        List<TestResult> results = new ArrayList<>();

        JsonNode paths = root.get("paths");
        if (paths == null) {
            System.err.println("❌ 未找到paths字段");
            return results;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = paths.fields();
        int count = 0;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String path = entry.getKey();
            JsonNode methods = entry.getValue();

            Iterator<String> methodNames = methods.fieldNames();
            while (methodNames.hasNext()) {
                String method = methodNames.next().toUpperCase();
                JsonNode operation = methods.get(method.toLowerCase());

                // 根据路径确定服务
                String serviceName = getServiceByPath(path);
                String serviceUrl = getServiceUrl(serviceName);

                // 处理路径参数，把 {id} 替换为 1
                String testPath = path.replaceAll("\\{[^}]+\\}", "1");

                TestResult result = testSingleOperation(method, testPath, operation, serviceUrl, serviceName);
                results.add(result);
                count++;

                // 每测试10个接口显示进度
                if (count % 10 == 0) {
                    System.out.println("🔄 已测试 " + count + " 个接口...");
                }

                try {
                    Thread.sleep(50); // 避免请求过快
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return results;
    }

    private static TestResult testSingleOperation(String method, String path, JsonNode operation, String baseUrl, String serviceName) {
        TestResult result = new TestResult();
        result.method = method;
        result.path = path;
        result.summary = operation.has("summary") ? operation.get("summary").asText() : "";
        result.service = serviceName;

        long startTime = System.currentTimeMillis();
        try {
            Request request = buildRequest(method, baseUrl + path, operation);
            Response response = client.newCall(request).execute();

            result.status = response.code();
            result.success = response.isSuccessful();

            if (!result.success) {
                result.error = "HTTP " + response.code() + " - " + response.message();
            }

            response.close();

        } catch (Exception e) {
            result.success = false;
            result.error = e.getMessage();
        }
        result.duration = System.currentTimeMillis() - startTime;

        return result;
    }

    private static Request buildRequest(String method, String url, JsonNode operation) {
        Request.Builder builder = new Request.Builder().url(url);

        // 添加Authorization header
        if (!token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        // 设置Content-Type
        builder.addHeader("Content-Type", "application/json");
        builder.addHeader("Accept", "application/json");

        JsonNode requestBody = operation.get("requestBody");
        String bodyContent = null;

        if (requestBody != null && requestBody.has("content")) {
            JsonNode content = requestBody.get("content");
            if (content.has("application/json")) {
                JsonNode schema = content.get("application/json").get("schema");
                bodyContent = generateRequestBody(schema);
            }
        }

        switch (method) {
            case "GET":
                builder = builder.get();
                break;
            case "POST":
                if (bodyContent != null) {
                    builder = builder.post(RequestBody.create(bodyContent, MediaType.parse("application/json")));
                } else {
                    builder = builder.post(RequestBody.create(new byte[0], MediaType.parse("application/json")));
                }
                break;
            case "PUT":
                if (bodyContent != null) {
                    builder = builder.put(RequestBody.create(bodyContent, MediaType.parse("application/json")));
                } else {
                    builder = builder.put(RequestBody.create(new byte[0], MediaType.parse("application/json")));
                }
                break;
            case "DELETE":
                builder = builder.delete();
                break;
            case "PATCH":
                if (bodyContent != null) {
                    builder = builder.patch(RequestBody.create(bodyContent, MediaType.parse("application/json")));
                } else {
                    builder = builder.patch(RequestBody.create(new byte[0], MediaType.parse("application/json")));
                }
                break;
            default:
                builder = builder.get();
        }

        return builder.build();
    }

    private static String generateRequestBody(JsonNode schema) {
        try {
            if (schema == null) {
                return "{}";
            }
            
            // 处理 $ref 引用
            if (schema.has("$ref")) {
                return "{}";
            }
            
            // 处理 allOf
            if (schema.has("allOf")) {
                JsonNode allOf = schema.get("allOf");
                StringBuilder json = new StringBuilder("{");
                boolean first = true;
                for (JsonNode item : allOf) {
                    String partial = generateRequestBody(item);
                    if (!partial.equals("{}")) {
                        if (!first) json.append(",");
                        first = false;
                        // 去掉外层大括号
                        if (partial.startsWith("{")) partial = partial.substring(1);
                        if (partial.endsWith("}")) partial = partial.substring(0, partial.length() - 1);
                        json.append(partial);
                    }
                }
                json.append("}");
                return json.toString();
            }
            
            JsonNode properties = schema.get("properties");
            if (properties == null || properties.size() == 0) {
                return "{}";
            }

            StringBuilder json = new StringBuilder("{");
            Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
            boolean first = true;

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode prop = entry.getValue();

                if (!first) {
                    json.append(",");
                }
                first = false;

                json.append("\"").append(key).append("\":");
                
                String type = prop.has("type") ? prop.get("type").asText() : "string";
                
                // 根据字段名生成更合理的值
                json.append(generateSmartValue(key, type, prop));
            }

            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String generateSmartValue(String key, String type, JsonNode prop) {
        // 根据字段名生成更合理的默认值
        if (key.toLowerCase().contains("id")) {
            return "1";
        }
        if (key.toLowerCase().contains("name")) {
            return "\"测试名称\"";
        }
        if (key.toLowerCase().contains("title")) {
            return "\"测试标题\"";
        }
        if (key.toLowerCase().contains("description")) {
            return "\"测试描述\"";
        }
        if (key.toLowerCase().contains("password")) {
            return "\"123456\"";
        }
        if (key.toLowerCase().contains("username")) {
            return "\"test\"";
        }
        if (key.toLowerCase().contains("phone")) {
            return "\"13800138000\"";
        }
        if (key.toLowerCase().contains("email")) {
            return "\"test@example.com\"";
        }
        if (key.toLowerCase().contains("status")) {
            return "1";
        }
        if (key.toLowerCase().contains("sort")) {
            return "0";
        }
        if (key.toLowerCase().contains("price") || key.toLowerCase().contains("amount")) {
            return "99.99";
        }
        if (key.toLowerCase().contains("count")) {
            return "10";
        }
        if (key.toLowerCase().contains("ids")) {
            return "[1, 2, 3]";
        }
        
        // 处理数组类型
        if ("array".equals(type)) {
            JsonNode items = prop.get("items");
            if (items != null && items.has("type")) {
                String itemType = items.get("type").asText();
                if ("integer".equals(itemType) || "number".equals(itemType)) {
                    return "[1, 2, 3]";
                }
                if ("string".equals(itemType)) {
                    return "[\"test1\", \"test2\"]";
                }
            }
            return "[1]";
        }
        
        // 处理对象类型
        if ("object".equals(type)) {
            return generateRequestBody(prop);
        }
        
        // 默认类型处理
        return generateDefaultValue(type);
    }

    private static String generateDefaultValue(String type) {
        switch (type) {
            case "integer":
            case "number":
                return "1";
            case "boolean":
                return "true";
            case "array":
                return "[1]";
            case "object":
                return "{}";
            default:
                return "\"test\"";
        }
    }
}
