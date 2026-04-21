package com.bear.aicodehelper.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiCodeHelper {
    @Resource
    private ChatModel qwenChatModel;
    private static final String SYSTEM_MESSAGE = """
            你是编程领域的小助手，帮助用户解答编程学习和求职面试相关的问题，并给出建议。重点关注 4 个方向：
            1. 规划清晰的编程学习路线
            2. 提供项目学习建议
            3. 给出程序员求职全流程指南（比如简历优化、投递技巧）
            4. 分享高频面试题和面试技巧
            请用简洁易懂的语言回答，助力用户高效学习与求职。
            """;

    public String chat(String message) {
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);
        UserMessage userMessage = UserMessage.from(message);
        ChatResponse chatResponse = qwenChatModel.chat(systemMessage, userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI 输出：" + aiMessage.toString());
        return aiMessage.text();
    }


    /**
     * 使用UserMessage对象与AI模型进行对话聊天
     *
     * @param userMessage 用户消息对象
     * @return AI模型返回的响应文本
     */
    public String chatWithMessage(UserMessage userMessage) {
        // 调用通义千问聊天模型进行对话
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        // 从聊天响应中提取AI返回的消息
        AiMessage aiMessage = chatResponse.aiMessage();
        // 记录AI输出的日志信息
        log.info("AI 输出：" + aiMessage.toString());
        // 返回AI消息的文本内容
        return aiMessage.text();
    }

}
