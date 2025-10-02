package faultlocalizer;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatGPTService {


    private static String apikey = "";
    public static void main(String[] args) {
        OpenAiService service = new OpenAiService(apikey);

        final List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a bug localizer who can locate statements in a program that are faulty."));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), "Following is a OpenID connect program. Find the statements that are faulty:"));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), "public void validateRequestObject(AuthorizationRequest req, RedirectResponse red){\n" +
                "    if (!req.getAud().isEmpty() && !req.getAud().contains(config.getIssuer())) {\n" +
                "        throw red.sendWebException(...);\n" +
                "    }\n"));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(),"Return the output in JSON format, consisting of a single JSON object with the \"bug_location\" array that contains JSON objects with three fields: \"line_number\" (indicating the buggy code) and \"code_content\" (showing the actual code statement), and \"reason\" (explaining why this location is identified as buggy).\n"));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        service.createChatCompletion(chatCompletionRequest).getChoices().forEach((c) -> {
            System.out.println(c.getMessage().getContent());
        });

        service.shutdownExecutor();
    }


}
