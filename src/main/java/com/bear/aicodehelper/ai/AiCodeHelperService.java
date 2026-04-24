package com.bear.aicodehelper.ai;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import java.util.List;

//@AiService
public interface AiCodeHelperService {

    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String userMessage);

    // 学习报告
    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String userMessage);

    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String userMessage);

    /**
     * 学习报告数据记录
     *
     * @param name 用户名称
     * @param suggestionList 学习建议列表
     */
    record Report(String name, List<String> suggestionList) {
    }


}
