package com.bear.aicodehelper.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * RAG（检索增强生成）配置类
 * 负责配置和初始化内容检索器，用于从文档中检索相关信息以增强AI回答
 */
@Configuration
public class RagConfig {

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 创建并配置内容检索器Bean
     * 该检索器会从指定目录加载文档，进行分段处理后存储到向量数据库中，
     * 并提供基于相似度评分的内容检索功能
     *
     * @return ContentRetriever 配置好的内容检索器实例
     */
    @Bean
    public ContentRetriever contentRetriever() {
        // 从文件系统加载文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");

        // 创建文档分段器：按段落分割，每段最大1000字符，相邻段重叠200字符以保持上下文连贯性
        DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(1000, 200);

        // 构建向量存储摄入器，负责将文档分段、转换并存入向量数据库
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(paragraphSplitter)
                // 为每个文本片段添加文件名前缀以提升搜索相关性
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                        textSegment.metadata()
                ))
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // 执行文档摄入：分段、向量化并存储
        ingestor.ingest(documents);

        // 构建基于向量存储的内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                // 设置最大返回5个最相关的检索结果
                .maxResults(5)
                // 设置最低相似度阈值为0.75，过滤低相关性结果
                .minScore(0.75)
                .build();
        return contentRetriever;
    }
}
