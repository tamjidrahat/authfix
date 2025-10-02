package sketchGen.analyzer.analysis;

public class CandidateGenerator {

    public static void main(String[] args) {
        BugLocationHandler bugLocationHandler = new BugLocationHandler();

       Map<String, String> argMap = PetriUtil.parseArguments(args);
       String srcdir = argMap.get("-srcdir"); 
       String outdir = argMap.get("-outdir"); 
       String buglocs = argMap.get("-loc");

       bugLocationHandler.processBugLocations(buglocs, srcdir, outdir);

    }
}
