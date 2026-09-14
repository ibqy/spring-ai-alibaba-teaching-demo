package com.xb.springai.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

/**
 * RAG 知识库配置 —— 把"店铺资料"变成可检索的向量库
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>RAG 是什么</b>：Retrieval-Augmented Generation（检索增强生成）。
 * 先把文档按句子切块 + 向量化存进向量库；提问时先检索最相关的片段，
 * 再连同问题一起交给大模型，从而让 AI"用你自己的资料回答"。</p>
 *
 * <p><b>用到的组件</b>：{@link EmbeddingModel}（把文字转成向量，由 starter 自动装配）、
 * {@link SimpleVectorStore}（内存向量库，适合教学）。</p>
 *
 * <p><b>注意</b>：SimpleVectorStore 在单价内存中，重启会丢失，需重新写入，适合演示；生产需用 PGVector 等。</p>
 */
@Configuration
public class RagConfig {

    /**
     * 声明向量库 Bean，并启动时把知识库文件预载进去。
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 构建内存向量库，需要 EmbeddingModel 完成文本向量化
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 读取 classpath 下的店铺资料：TextReader 实现 DocumentReader，get() 返回 List<Document>
        TextReader textReader = new TextReader(new ClassPathResource("kb/shop-intro.txt"));
        List<Document> documents = textReader.get();

        // 先把整份文档导入向量库（TextReader 默认按整份内容切块）
        vectorStore.add(documents);

        return vectorStore;
    }
}