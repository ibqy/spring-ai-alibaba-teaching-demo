package com.xb.springai.controller.demo11;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * demo11 · 深阶实战：知识型订餐客服（Agent）—— 把前面所有基础能力真正"组合"起来
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>这是全项目最完整的一个 demo</b>。前面 demo08 RAG、demo06 记忆、demo05 工具、
 * demo10 客服雏形、demo02 流式 分别演示了单一能力；本 demo 把四样东西
 * （RAG 检索 + 多轮记忆 + 函数工具 + 联网搜索/流式) 拧成一台<b>能真正接待订餐的客服</b>。</p>
 *
 * <h3>组合模型：一次提问走完的流水线</h3>
 * <ol>
 *   <li><b>RAG 检索</b>：用 VectorStore.similaritySearch(SearchRequest) 把问题转向量，
 *       在 kb/shop-intro.txt 向量库中召回最相关的店铺资料（营业时间、地址等静态信息）。</li>
 *   <li><b>拼上下文</b>：把命中的资料拼进 user prompt，让模型"只依据资料答静态问题，避免编造"。</li>
 *   <li><b>函数工具 defaultTools</b>：模型发现顾客问"价格/库存/优惠"这类<b>动态业务数据</b>时，
 *       自动调用 ShopServiceTool 的 @Tool 方法拿到最新数据再回答（Function Calling）。</li>
 *   <li><b>多轮记忆 MessageChatMemoryAdvisor</b>：通过 conversationId 给每个顾客独立的
 *       对话历史，让客服"记得"前面聊过什么（如已选套餐），实现真正的多轮对话。</li>
 *   <li><b>联网搜索（可选）DasaScopeChatOptions.withEnableSearch</b>：当问题超出店铺资料
 *       与工具能力（如"现在打的还有优惠吗""实时新闻"），按需开启 DashScope 联网检索。</li>
 * </ol>
 *
 * <h3>为什么这么组合？它相对单个 demo 的实战价值</h3>
 * <ul>
 *   <li><b>能力分治</b>：静态资料走 RAG、动态数据走工具，避免把易变的价格写死在向量库里，
 *       也不用让大模型"猜价格"；这正是生产客服系统最常见的分层。</li>
 *   <li><b>一个 Agent，多种任务</b>：顾客既能问"营业时间"(RAG)、问"有没有货"(工具)、
 *       也能"昨天推荐了午市单人餐，今天帮我改双人下午茶"(记忆 + 工具)，还能问实时政策(联网)。
 *       任何单一 demo 都做不到这种"全能+多轮"。</li>
 *   <li><b>入口统一</b>：都收敛到 /api/demo11 下，前端只需调一个 Agent 端点即可。</li>
 * </ul>
 *
 * <h3>本类不新建任何 Bean，只做"编排"</h3>
 * <p>{@link VectorStore}、{@link ChatMemory} 分别来自 {@code RagConfig} / {@code ChatConfig} 全局 Bean，
 * ChatClient 由 starter 自动装配的 {@code ChatClient.Builder} 构建 —— 这正是 Bean 复用的价值所在。</p>
 *
 * <p><b>运行示例</b>：</p>
 * <pre>
 * ① POST /api/demo11/ask
 *    {"conversationId":"vip-01","message":"你们几点营业？想带朋友来"}
 * ② POST /api/demo11/ask
 *    {"conversationId":"vip-01","message":"他也是在杭州吗？地址发我"}  ← 记得刚才聊的"带朋友"
 * ③ POST /api/demo11/ask
 *    {"conversationId":"vip-01","message":"双人下午茶多少钱？有货吗？今天有优惠吗？"}
 * ④ GET  /api/demo11/stream?conversationId=vip-01&message=周末家庭套餐现在是多少钱？库存还有多少？
 * </pre>
 */
@RestController
@RequestMapping("/api/demo11")
public class Demo11AdvancedAgentController {

    /**
     * RAG 检索召回条数：一次最多从向量库取几条最相关资料。
     */
    private static final int TOP_K = 3;

    /** 组合后的 Agent：一个人设 + 记忆顾问 + 工具 的 ChatClient，两个端点共用。 */
    private final ChatClient chatClient;

    /** 店铺资料的向量库（RagConfig 注入）。 */
    private final VectorStore vectorStore;

    public Demo11AdvancedAgentController(ChatClient.Builder chatClientBuilder,
                                         ChatMemory chatMemory,
                                         VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                // ① 人设：告诉模型它是什么、怎么用"资料"与"工具"
                .defaultSystem("你是'小北咖啡店'的智能客服'小北'。"
                        + "顾客关于营业时间、地址等店铺介绍，请只依据提供的【店铺资料】回答；"
                        + "关于套餐价格、库存、优惠等实时问题，请调用提供的业务工具查询后回答，不要编造数字。"
                        + "语气友好简洁，先解答顾客最关心的问题。")
                // ② 记忆：MessageChatMemoryAdvisor 挂到每次调用上，conversationId 决定用的是哪段历史
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                // ③ 工具：把订餐/套餐查询工具注册进模型（模型需要时可自动调用）
                .defaultTools(new ShopServiceTool())
                .build();
    }

    /**
     * POST /api/demo11/ask —— 组合版客服（RAG + 记忆 + 工具 + 可选联网）
     *
     * <p><b>完整链路</b>：</p>
     * <ol>
     *   <li>解析请求，凑 conversationId（不传则用 "default"）。</li>
     *   <li>先用问题到向量库做<b>相似度检索(RAG)</b>，得到 TOP_K 条店铺资料。</li>
     *   <li>把命中资料与原始问题拼成一个带上下文的 user prompt。</li>
     *   <li>通过 .advisors(...) 传入 conversationId，让记忆顾问使用对应会话历史。</li>
     *   <li>若请求开启联网，用 DashScopeChatOptions.withEnableSearch(true) 追加搜索能力。</li>
     *   <li>.call().content() 拿到最终答案。</li>
     * </ol>
     */
    @PostMapping("/ask")
    public String ask(@Valid @RequestBody Demo11AskRequest req) {
        String conversationId = (req.conversationId() == null || req.conversationId().isBlank())
                ? "default" : req.conversationId();

        // ① RAG 检索：把问题转成向量，在店铺资料向量库中召回最相关片段
        String context = retrieveContext(req.message());

        // ② 构造"资料 + 问题"的 user prompt（动态部分在 per-request 传入，人设保持在 defaultSystem）
        String userPrompt = String.format(
                "请结合下面的【店铺资料】回答顾客，资料没有的不要编造；价格/库存/优惠请用工具实时查询。\n\n"
                        + "【店铺资料】\n%s\n\n【顾客】%s", context, req.message());

        // ③ 组一次 prompt
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt();
        // ④ 记忆会话隔离（1.0.0 用该上下文 key，与 demo06/demo10 一致）
        spec = spec.advisors(a -> a.param("chat_memory_conversation_id", conversationId));
        spec = spec.user(userPrompt);

        // ⑤ 可选：按需开启 DashScope 联网搜索（处理实时政策/新闻类问题）
        if (Boolean.TRUE.equals(req.enableSearch())) {
            spec = spec.options(DashScopeChatOptions.builder().withEnableSearch(true).build());
        }

        // ⑥ 一次拿完整结果
        return spec.call().content();
    }

    /**
     * GET /api/demo11/stream —— 流式版客服（RAG + 工具），逐字节推送
     *
     * <p><b>与 /ask 的区别</b>：这里用 {@code .stream().content()} 返回 {@link Flux}&lt;String&gt;，
     * Spring 以 text/event-stream 方式把模型输出<b>逐块</b>推给前端，体验是"边算边出字"。
     * 记忆、RAG、工具逻辑完全一致 —— 说明同一套组合能力既可同步返回也可流式输出。</p>
     *
     * @param conversationId 会话 ID（可选，默认 "default"）
     * @param message        顾客消息
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> stream(@RequestParam(defaultValue = "default") String conversationId,
                               @RequestParam(defaultValue = "周末家庭套餐现在多少钱？有货吗？") String message) {
        // RAG：同样先检索店铺资料
        String context = retrieveContext(message);
        String userPrompt = String.format(
                "请结合下面的【店铺资料】回答顾客，资料没有的不要编造；价格/库存/优惠请用工具实时查询。\n\n"
                        + "【店铺资料】\n%s\n\n【顾客】%s", context, message);

        return chatClient.prompt()
                // 记忆隔离：同一个 conversationId 复用多轮上下文
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .user(userPrompt)
                .stream()      // 关键点：与 /ask 的 call() 不同，这里是流式
                .content();    // Flux<String>：模型文字天然带换行，逐块推送
    }

    /**
     * 私有辅助：执行向量库相似度检索，把命中的资料拼成一段可读文本。
     *
     * <p>复用 demo08 的用法：{@link VectorStore#similaritySearch(SearchRequest)} +
     * {@link Document#getText()}，只是抽象成方法让 /ask 与 /stream 共用同一份 RAG 逻辑。</p>
     *
     * @param question 顾客的问题（作为检索 query）
     * @return 命中的资料原文，用换行连接
     */
    private String retrieveContext(String question) {
        List<Document> hits = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(TOP_K)
                        .build());
        return hits.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
    }
}