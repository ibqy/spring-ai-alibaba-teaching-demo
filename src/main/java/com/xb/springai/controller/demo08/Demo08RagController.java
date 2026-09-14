package com.xb.springai.controller.demo08;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * demo08：RAG 检索增强生成 —— 让 AI"只能"用你的私有资料回答
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>场景</b>：大模型不知道"小北咖啡店"的营业时间。我们先把店铺资料存进向量库
 * （见 {@code RagConfig}），提问时先检索最相关的片段，再把片段塞进提示词交给模型。</p>
 *
 * <p><b>流程</b>：用户提问 → 向量库相似度检索 → 把命中的资料拼进 prompt → 模型基于资料回答。
 * 这样回答既准确、又<strong>不会编造</strong>资料里没有的信息。</p>
 *
 * <p><b>运行</b>：<code>GET /api/demo08/ask?question=你们几点营业？</code></p>
 */
@RestController
@RequestMapping("/api/demo08")
public class Demo08RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public Demo08RagController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "你们几点营业？") String question) {

        // ① 检索：在向量库中找与问题最相关的前 3 条资料
        List<Document> hits = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)   // 用问题本身去检索
                        .topK(3)           // 取前 3 条最相关
                        .build());

        // ② 拼接：把命中的资料拼成可读文本
        String context = hits.stream()
                .map(Document::getText)              // 每条 Document 的原文
                .collect(Collectors.joining("\n"));

        // ③ 生成：把资料作为上下文交给模型，并要求"只依据资料回答"
        return chatClient.prompt()
                .user(String.format(
                        "请只依据下面的店铺资料回答用户的问题，资料里没有的不要编造。\n\n"
                                + "【店铺资料】\n%s\n\n【用户问题】%s", context, question))
                .call()
                .content();
    }
}