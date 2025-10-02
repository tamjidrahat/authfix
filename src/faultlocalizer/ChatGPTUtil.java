package faultlocalizer;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class ChatGPTUtil {
    public static ChatMessage getUserChatMessage(String str) {
        return new ChatMessage(ChatMessageRole.USER.value(), str);
    }

    public static ChatMessage getSystemChatMessage(String str) {
        return new ChatMessage(ChatMessageRole.SYSTEM.value(), str);
    }

    public static List<ChatMessage> buildQuery(List<String> commands) {
        List<ChatMessage> messages = new ArrayList<>();
        for(String command: commands) {
            messages.add(getUserChatMessage(command + "\n"));
        }
        return messages;
    }

    public static String readFile(String path, boolean removeComments) {
        String res = null;
        try {
            
            res = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            if(removeComments) {
                
                res = res.replaceAll("(?s)/\\*.*?\\*/", "");
                
                res = res.replaceAll("//.*", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                argMap.put(args[i], args[i + 1]);
            } else {
                System.out.println("Error: Argument " + args[i] + " has no value.");
                System.exit(1);
            }
        }
        return argMap;
    }

    public static JSONObject getJsonObject(String jsonString) {
        return new JSONObject(jsonString);
    }
}
