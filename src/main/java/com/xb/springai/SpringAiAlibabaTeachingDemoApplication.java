package com.xb.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Alibaba 教学演示项目 - 启动类
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>它是干什么的</b>：一个普通的 Spring Boot 应用。因为 pom.xml 引入了
 * <code>spring-ai-alibaba-starter-dashscope</code>，启动时会通过自动装配机制，
 * 自动创建与 <b>阿里云通义千问 DashScope</b> 通信所需的 <code>ChatClient</code>、
 * <code>ChatModel</code>、<code>EmbeddingModel</code> 等 Bean，开箱即用。</p>
 *
 * <p><b>项目结构</b>：在 {@code controller/} 包下，按 demo01 ~ demoNN 增量组织，
 * 每个 Demo 用最少的代码讲清一个 Spring AI Alibaba 的核心知识点。</p>
 */
@SpringBootApplication
public class SpringAiAlibabaTeachingDemoApplication {

    /**
     * 程序入口：启动内嵌 Tomcat，加载所有自动配置。
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringAiAlibabaTeachingDemoApplication.class, args);
    }
}