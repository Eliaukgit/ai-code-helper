package com.bear.aicodehelper.ai.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ChatModelListenerConfig {

    /**
     * 创建聊天模型监听器Bean，用于监控和记录聊天模型的请求、响应和错误信息
     *
     * @return ChatModelListener 聊天模型监听器实例
     */
    @Bean
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {
            /**
             * 监听聊天模型请求事件，记录请求详情
             */
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.info("onRequest(): {}", requestContext.chatRequest());
            }

            /**
             * 监听聊天模型响应事件，记录响应结果
             */
            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("onResponse(): {}", responseContext.chatResponse());
            }

            /**
             * 监听聊天模型错误事件，记录错误信息
             */
            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.info("onError(): {}", errorContext.error().getMessage());
            }
        };
    }
}
