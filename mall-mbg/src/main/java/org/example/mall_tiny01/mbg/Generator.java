package org.example.mall_tiny01.mbg;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Generator {

    public static void main(String[] args) throws Exception {
        // 1. 创建警告信息列表，用于收集生成过程中产生的非致命错误信息
        List<String> warnings = new ArrayList<>();
        
        // 2. 设置是否覆盖已存在的文件。true表示如果文件存在则覆盖，false表示不覆盖
        boolean overwrite = true;

        // 3. 指定 MyBatis Generator 的配置文件路径
        // 关键参数：src/main/resources/generatorConfig.xml 是 MBG 的核心配置文件，定义了数据库连接、表映射、生成策略等
        File configFile = new File("src/main/resources/generatorConfig.xml");
        
        // 4. 创建配置解析器，传入警告列表以接收解析过程中的警告
        ConfigurationParser cp = new ConfigurationParser(warnings);
        
        // 5. 解析配置文件，生成 Configuration 对象
        // 关键参数：configFile 即上一步定义的 XML 配置文件
        Configuration config = cp.parseConfiguration(configFile);

        // 6. 创建默认的回调函数，处理文件生成时的逻辑（如覆盖策略）
        // 关键参数：overwrite 决定了当目标文件已存在时是否直接覆盖
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        
        // 7. 创建 MyBatisGenerator 实例
        // 关键参数：
        // - config: 解析后的配置对象，包含所有生成规则
        // - callback: 回调函数，处理文件写入行为
        // - warnings: 警告列表，用于收集生成过程中的问题
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        
        // 8. 执行代码生成操作
        // 关键参数：null 表示不使用进度回调接口，若需监控进度可传入 ProgressCallback 实现类
        myBatisGenerator.generate(null);

        // 9. 打印生成过程中产生的所有警告信息
        for (String warning : warnings) {
            System.out.println(warning);
        }
        
        // 10. 提示生成成功
        System.out.println("代码生成成功！");
    }
}
