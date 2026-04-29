package com.bear.aicodehelper.ai;

import com.bear.aicodehelper.ai.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码助手服务工厂类
 * 负责创建和配置AI服务实例
 */
@Configuration
public class AiCodeHelperServiceFactory {

    @Resource
    private ChatModel qwenChatModel;

    @ Resource
    private ContentRetriever contentRetriever;


    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Bean
    public AiCodeHelperService aiCodeHelperService() {
        // 构建AI代码助手服务实例，集成聊天模型、会话记忆和RAG内容检索功能
        AiCodeHelperService aiCodeHelperService = AiServices.builder(AiCodeHelperService.class)
                // 配置通义千问聊天模型用于对话生成
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                // 配置会话记忆提供者，按 memoryId 隔离不同用户的会话上下文
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                // 配置RAG内容检索器，从文档中检索相关信息增强回答
                .contentRetriever(contentRetriever)
                // 注册自定义工具：面试问题生成工具
                .tools(new InterviewQuestionTool())
                .toolProvider(mcpToolProvider) // MCP 工具调用
                .build();
        return aiCodeHelperService;
    }
}