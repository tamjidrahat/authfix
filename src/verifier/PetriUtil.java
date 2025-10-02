package verifier;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.io.renderer.impl.DotLTSRenderer;
import uniol.apt.io.renderer.impl.DotPNRenderer;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

public class PetriUtil {

    static final public String TS_NODE_TEMPLATE = "%1$s[label=\"%2$s\"]; // node for marking %3$s%n";
    static final public String TS_INIT_TEMPLATE = "%1$s[label=\"%2$s\", shape=circle]; // node for marking %3$s%n";
    static final public String TS_EDGE_TEMPLATE = "%1$s -> %2$s[label=\"%3$s\"];%n";

    public static String getPetrinetDot(PetriNet pn) {
        try {
            return new DotPNRenderer().render(pn);
        } catch (RenderException e) {
            return e.toString();
        }
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

    public static String getTransSystemDot(TransitionSystem ts) {
        try {
            return new DotLTSRenderer().render(ts);
        } catch (RenderException e) {
            return e.toString();
        }
    }

    public static boolean isFinal(State s) {
        return (s.getPostsetEdges().size() == 0);
    }

    public static String getCoverabilityDot(CoverabilityGraph cgraph) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");
        sb.append("node [shape = point, color=white, fontcolor=white]; start;");
        sb.append("edge [fontsize=20]\n");
        sb.append("node [fontsize=20,shape=circle,color=black,fontcolor=black,"
                + "height=0.5,width=0.5,fixedsize=true];\n");

        Formatter format = new Formatter(sb);

        Map<CoverabilityGraphNode, String> nodeLabels = new HashMap<>();
        nodeLabels.put(cgraph.getInitialNode(), "s0");
        format.format(TS_INIT_TEMPLATE, "s0", "s0", cgraph.getInitialNode().getMarking().toString());
        int nextState = 1;
        for (CoverabilityGraphNode node : cgraph.getNodes()) {
            if (!cgraph.getInitialNode().equals(node)) {
                String name = "s" + (nextState++);
                nodeLabels.put(node, name);
                format.format(TS_NODE_TEMPLATE, name, name, node.getMarking().toString());
            }
        }

        format.format(TS_EDGE_TEMPLATE, "start", "s0", "");

        for (CoverabilityGraphEdge edge : cgraph.getEdges()) {
            String source = nodeLabels.get(edge.getSource());
            String target = nodeLabels.get(edge.getTarget());
            String label = edge.getTransition().getLabel();
            format.format(TS_EDGE_TEMPLATE, source, target, label);
        }

        format.close();
        sb.append("}\n");

        return sb.toString();
    }
}
