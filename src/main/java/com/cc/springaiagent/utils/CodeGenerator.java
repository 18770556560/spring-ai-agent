package com.cc.springaiagent.utils;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;

/**
 * 代码生成器工具类
 * <p>
 * 基于 MyBatis-Plus 的 FastAutoGenerator 实现，用于根据数据库表结构自动生成 Entity、Mapper、Service 等层级的代码。
 */
public class CodeGenerator {
    static String url = "jdbc:mysql://localhost:3306/spring_ai_agent?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&allowPublicKeyRetrieval=true";
    static String username = "root";
    static String password = "root";

    /**
     * 程序入口方法，执行代码生成逻辑
     *
     * @param args 命令行参数（当前未使用）
     */
    public static void main(String[] args) {
        // 创建快速自动生成器实例，配置数据源连接信息
        FastAutoGenerator.create(url, username, password)
                // 配置全局生成策略
                .globalConfig(builder -> builder
                        .author("cc")
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java")
                        .commentDate("yyyy-MM-dd")
                        // 开启 Swagger 注解支持 (可选) 注意：生成的是swagger2老版本注解，项目引入的knife4j是新版本
//                        .enableSwagger()
                )
                // 配置包名策略，指定各层级代码的包路径
                .packageConfig(builder -> builder
                        .parent("com.cc.springaiagent")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
//                        .xml("mapper.xml")
                        // 配置XML文件生成路径：将Mapper XML文件输出到项目根目录下的 src/main/resources/mapper 文件夹
                        .pathInfo(java.util.Collections.singletonMap(
                                        com.baomidou.mybatisplus.generator.config.OutputFile.xml,
                                        Paths.get(System.getProperty("user.dir")) + "/src/main/resources/mapper"))
                )
                // 配置策略，启用 Lombok 支持以简化实体类代码
                .strategyConfig(builder -> builder
//                        .addInclude("ai_chat_session")//指定表
                        .entityBuilder()
                        .enableLombok()
                        .build()
                )
                // 指定使用 Freemarker 模板引擎进行代码渲染
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行代码生成操作
                .execute();
    }
}
