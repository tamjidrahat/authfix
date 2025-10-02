package sketchGen.analyzer.analysis;

import java.util.*;
import java.util.Random;

public class Grammar {

    private Map<String, List<List<String>>> rules;
    private List<String> expansion;

    public Grammar() {
        this.rules = new HashMap<>();
        this.expansion = new ArrayList<>();
    }

    public void addRule(String rule, List<String> expansion) {
        if (this.rules.containsKey(rule)) {
            this.rules.get(rule).add(expansion);
        } else {
            this.rules.put(rule, new ArrayList<>(Arrays.asList(expansion)));
        }
    }

    public void expand(String start) {
        if (this.rules.containsKey(start)) {
            List<List<String>> possibleExpansions = this.rules.get(start);
            List<String> randomExpansion = possibleExpansions.get(new Random().nextInt(possibleExpansions.size()));
            for (String elem : randomExpansion) {
                expand(elem);
            }
        } else {
            this.expansion.add(start);
        }
    }

    public List<String> getExpansion(String axiom) {
        expand(axiom);
        return this.expansion;
    }

    public static void main(String[] args) {
        Grammar cfree = new Grammar();
        cfree.addRule("S", Arrays.asList("NP", "VP"));
        cfree.addRule("NP", Arrays.asList("the", "N"));
        cfree.addRule("N", Arrays.asList("cat"));
        cfree.addRule("N", Arrays.asList("dog"));
        cfree.addRule("N", Arrays.asList("weinermobile"));
        cfree.addRule("N", Arrays.asList("duchess"));
        cfree.addRule("VP", Arrays.asList("V", "the", "N"));
        cfree.addRule("V", Arrays.asList("sees"));
        cfree.addRule("V", Arrays.asList("chases"));
        cfree.addRule("V", Arrays.asList("lusts after"));
        cfree.addRule("V", Arrays.asList("blames"));

        List<String> expansion = cfree.getExpansion("S");
        System.out.println(String.join(" ", expansion));
    }
}