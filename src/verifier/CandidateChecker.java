package verifier;

import uniol.apt.analysis.exception.UnboundedException;

import java.util.Map;

public class CandidateChecker {

    public static void main(String[] args) {
        // Parse command-line arguments
        Map<String, String> argMap = PetriUtil.parseArguments(args);

        String srcPath = argMap.get("-src");
        String className = argMap.get("-classname");
        String methodName = argMap.get("-method");
        String specPath = argMap.get("-spec");

        
        if (srcPath == null || className == null || methodName == null || specPath == null) {
            System.out.println("Error: Missing required arguments.");
            System.exit(1);
        }

        OpenIdCandidateVerifier verifier = new OpenIdCandidateVerifier(srcPath, className, methodName, specPath);

        try {
            if (verifier.verify()) {
                System.out.println("verified");
            } else {
                System.out.println("not verified");
            }
        } catch (UnboundedException e) {
            e.printStackTrace();
        }
    }



}
