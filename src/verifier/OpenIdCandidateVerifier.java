package verifier;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.AptPNParser;
import uniol.apt.util.Pair;
import utils.JarUtil;
import utils.WalaUtil;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

public class OpenIdCandidateVerifier {
    PetriNet prognet;
    PetriNet specnet;

    public OpenIdCandidateVerifier(String srcFile, String className, String methodName, String specSrcFile)  {
        PetriNet snet = null;
        PetriNet pnet = null;
        
        String path = srcFile.substring(0, srcFile.lastIndexOf('/')+1);
        String filename = srcFile.substring(srcFile.lastIndexOf('/'), srcFile.lastIndexOf('.'));

        try {
            //build petri net for specification from .apt file
            snet = new AptPNParser().parseFile(specSrcFile);

            CallGraph cg = null;

            if(srcFile.endsWith(".jar")) {
                
                cg = WalaUtil.buildCallgraph(srcFile, className);
            } else if(srcFile.endsWith(".java")) {
                
                JarUtil.buildJarFromSrc(srcFile, className);
                
                cg = WalaUtil.buildCallgraph(path+filename+".jar", className);
            } else {
                System.err.println("File format not supported: " + srcFile);
                System.exit(1);
            }

            if (cg != null) {
                
                CGNode cgNode = WalaUtil.findCGNode(cg, methodName);
                PetriBuilder petriBuilder = new PetriBuilder();
                
                pnet = petriBuilder.buildPetri(cgNode);

            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.specnet = snet;
        this.prognet = pnet;
    }

    public OpenIdCandidateVerifier(String aptFileProg, String aptFileSpec) {
        try {
            this.prognet = new AptPNParser().parseFile(aptFileProg);
            this.specnet = new AptPNParser().parseFile(aptFileSpec);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OpenIdCandidateVerifier(PetriNet prognet, PetriNet specnet) {
        this.prognet = prognet;
        this.specnet = specnet;
    }


    public boolean verify() throws UnboundedException {
        CoverabilityGraph progReach = CoverabilityGraph.getReachabilityGraph(prognet);
        CoverabilityGraph specReach = CoverabilityGraph.getReachabilityGraph(specnet);

        TransitionSystem ts_prog = progReach.toReachabilityLTS();
        TransitionSystem ts_spec = specReach.toReachabilityLTS();

        
        Deque<Pair<State, State>> workersList = new LinkedList<>();

        Pair<State, State> initialState = new Pair<>(ts_prog.getInitialState(), ts_spec.getInitialState());
        workersList.addFirst(initialState);

        while (!workersList.isEmpty()) {
            Pair<State, State> curstate = workersList.pollFirst();
            
            State progState = curstate.getFirst();
            State specState = curstate.getSecond();

            if(PetriUtil.isFinal(progState) && !PetriUtil.isFinal(specState)) {
                return false;
            }

            Set<Arc> progStateOutgoingEdges = progState.getPostsetEdges();
            Set<Arc> specStateOutgoingEdges = specState.getPostsetEdges();

            for(Arc edgeProg: progStateOutgoingEdges) {
                boolean matchedSpec = false;
                for(Arc edgeSpec: specStateOutgoingEdges) {
                    String progEvent = edgeProg.getEvent().getLabel();
                    String specEvent = edgeSpec.getEvent().getLabel();

                    if(progEvent.equals(specEvent)) {
                    
                        workersList.addLast(new Pair<>(edgeProg.getTarget(), edgeSpec.getTarget()));
                        matchedSpec = true;
                        break;
                    }
                }
                if(!matchedSpec) {
                    workersList.addLast(new Pair<>(edgeProg.getTarget(), specState));
                }
            }
        }
        return true;
    }
}
