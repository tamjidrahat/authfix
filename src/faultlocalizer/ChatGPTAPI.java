package faultlocalizer;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.HashMap;
import java.util.List;

public class ChatGPTAPI {
    private String apikey;
    private OpenAiService service;
    private int maxTokens;
    private String model;

    public ChatGPTAPI(String token) {
        this.apikey = token;
        this.service = new OpenAiService(apikey);
        this.maxTokens = 500; // default value
        this.model = "gpt-3.5-turbo"; // default value
    }

    public String executeQuery(List<ChatMessage> messages) {
        StringBuffer response = new StringBuffer();
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(this.model)
                .messages(messages)
                .n(1)
                .maxTokens(this.maxTokens)
                .logitBias(new HashMap<>())
                .build();


        ChatCompletionResult result = service.createChatCompletion(chatCompletionRequest);

        if (result != null) {
            List<ChatCompletionChoice> choices = result.getChoices();
            if (choices != null) {
                choices.forEach((c) -> {
                    if (c != null && c.getMessage() != null && c.getMessage().getContent() != null) {
                         response.append(c.getMessage().getContent());
                    }
                });
            }
        }


        service.shutdownExecutor();
        return response.toString();
    }




}
