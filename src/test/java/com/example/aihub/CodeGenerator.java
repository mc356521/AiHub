package com.example.aihub;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.example.aihub.entity.BaseEntity;

import java.util.Collections;
import java.util.Scanner;

/**
 * MyBatis-Plus 代码生成器 (兼容 3.5.5 版本)
 * <p>
 * 使用方法:
 * 1. 运行 main() 方法
 * 2. 根据提示输入数据库连接信息
 * 3. 输入需要生成代码的表名（多个表名用逗号分隔）
 * 4. 选择是否覆盖已存在的文件
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
public class CodeGenerator {

    // 配置常量
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/aihub?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "root";
    private static final String AUTHOR_NAME = "AIHub Code Generator";
    private static final String PACKAGE_PARENT = "com.example.aihub";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 获取数据库连接信息
        String url = DEFAULT_URL;
        String username =  DEFAULT_USERNAME;
        String password = DEFAULT_PASSWORD;

        // 获取表名
        System.out.print("请输入要生成的表名（多个表名用逗号分隔）: ");
        String tablesInput = scanner.nextLine().trim();
        if (tablesInput.isEmpty()) {
            System.out.println("错误: 必须指定至少一个表名！");
            return;
        }
        String[] tables = tablesInput.split(",");
        for (int i = 0; i < tables.length; i++) {
            tables[i] = tables[i].trim();
        }

        // 是否覆盖已存在文件
        System.out.print("是否覆盖已存在的文件？(y/N): ");
        boolean fileOverride = "y".equalsIgnoreCase(scanner.nextLine().trim());

        // 获取项目路径
        String projectPath = System.getProperty("user.dir");
        System.out.println("项目路径: " + projectPath);
        System.out.println("开始生成代码...");

        try {
            // 执行代码生成
            generateCode(url, username, password, tables, fileOverride, projectPath);
            System.out.println("✅ 代码生成完成！");
            System.out.println("生成的文件位置:");
            System.out.println("  - Entity: " + projectPath + "/src/main/java/" + PACKAGE_PARENT.replace(".", "/") + "/entity");
            System.out.println("  - Mapper: " + projectPath + "/src/main/java/" + PACKAGE_PARENT.replace(".", "/") + "/mapper");
            System.out.println("  - Service: " + projectPath + "/src/main/java/" + PACKAGE_PARENT.replace(".", "/") + "/service");
            System.out.println("  - Controller: " + projectPath + "/src/main/java/" + PACKAGE_PARENT.replace(".", "/") + "/controller");
            System.out.println("  - Mapper XML: " + projectPath + "/src/main/resources/mapper");
        } catch (Exception e) {
            System.err.println("❌ 代码生成失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /**
     * 执行代码生成 (兼容 MyBatis-Plus 3.5.5)
     */
    private static void generateCode(String url, String username, String password,
                                     String[] tables, boolean fileOverride, String projectPath) {

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author(AUTHOR_NAME) // 设置作者
                            .enableSpringdoc() // 开启 springdoc 模式 (OpenAPI 3)
                            .outputDir(projectPath + "/src/main/java") // 指定输出目录
                            .dateType(DateType.TIME_PACK) // 使用 java.time 包下的时间类型
                            .disableOpenDir(); // 禁止打开输出目录

                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(PACKAGE_PARENT) // 设置父包名
                            .entity("entity") // 实体类包名
                            .mapper("mapper") // Mapper 接口包名
                            .service("service") // Service 接口包名
                            .serviceImpl("service.impl") // Service 实现类包名
                            .controller("controller") // Controller 包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper")); // Mapper XML 文件路径
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(tables) // 设置需要生成的表名
                            .addTablePrefix("t_", "c_", "sys_") // 设置过滤表前缀

                            // Entity 策略配置
                            .entityBuilder()
                            .superClass(BaseEntity.class) // 设置父类
                            .addSuperEntityColumns("id", "create_time", "update_time", "deleted") // 父类公共字段
                            .enableLombok() // 开启 Lombok
                            .enableTableFieldAnnotation() // 开启字段注解
                            .versionColumnName("version") // 乐观锁字段名
                            .logicDeleteColumnName("deleted") // 逻辑删除字段名
                            .naming(NamingStrategy.underline_to_camel) // 数据库表映射到实体的命名策略
                            .columnNaming(NamingStrategy.underline_to_camel) // 数据库字段映射到实体的命名策略
                            .disableSerialVersionUID() // 禁用序列化UID
                            .formatFileName("%s") // 实体类文件名格式

                            // Mapper 策略配置
                            .mapperBuilder()
                            .enableMapperAnnotation() // 开启 @Mapper 注解 (3.5.5 版本支持)
                            .enableBaseResultMap() // 启用 BaseResultMap 生成
                            .enableBaseColumnList() // 启用 BaseColumnList 生成
                            .formatMapperFileName("%sMapper") // Mapper 接口文件名格式
                            .formatXmlFileName("%sMapper") // Mapper XML 文件名格式

                            // Service 策略配置
                            .serviceBuilder()
                            .formatServiceFileName("%sService") // Service 接口文件名格式
                            .formatServiceImplFileName("%sServiceImpl") // Service 实现类文件名格式

                            // Controller 策略配置
                            .controllerBuilder()
                            .enableRestStyle() // 开启 @RestController
                            .enableHyphenStyle() // 开启驼峰转连字符
                            .formatFileName("%sController"); // Controller 文件名格式
                })
                // 模板引擎配置
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行生成
                .execute();
    }

    /**
     * 获取用户输入，支持默认值
     */
    private static String getInput(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt + " [默认: " + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
}