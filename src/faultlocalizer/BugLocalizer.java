package faultlocalizer;

import com.theokanning.openai.completion.chat.ChatMessage;
import verifier.PetriUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class BugLocalizer {
    private static String apikey = "";

    public static void main(String[] args) {
        Map<String, String> argMap = PetriUtil.parseArguments(args);

        String srcFile = argMap.get("-src");
        String specFile = argMap.get("-spec");
        String apikey = argMap.get("-token");

        ChatGPTAPI api = new ChatGPTAPI(apikey);

        String q1 = "You are a bug localizer who can locate statements in a program that are faulty.\n";
        String q2 = "Following is a requirement from OpenID Connect specification: \""+
                ChatGPTUtil.readFile(specFile, true) + "\"\n";
        String q3 = "Following is a OpenID connect program. Find the statements that may violate the OpenID requirements above:\n"+
                ChatGPTUtil.readFile(srcFile, true)+"\n";

        String q4 = "Return the output in JSON format, consisting of a single JSON object with the \"bug_location\" " +
                "array that contains JSON objects with three fields: \"line_number\" (indicating the buggy code) and \"code_content\" " +
                "(showing the actual code statement), and \"reason\" (explaining why this location is identified as buggy).\n";

        final List<ChatMessage> query = new ArrayList<>();
        query.add(ChatGPTUtil.getSystemChatMessage(q1));
        query.add(ChatGPTUtil.getUserChatMessage(q2));
        query.add(ChatGPTUtil.getUserChatMessage(q3));
        query.add(ChatGPTUtil.getUserChatMessage(q4));

        String response = api.executeQuery(query);

    }
}
