package verifier;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.util.collections.Iterator2Iterable;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import utils.WalaUtil;

import java.util.*;

public class PetriBuilder {

    public PetriNet buildPetri(CGNode cgNode) {
        int transId = 0;
        PetriNet pnet = new PetriNet();
        IR ir = cgNode.getIR();
        SSACFG cfg = ir.getControlFlowGraph();

        Deque<BasicBlock> workers = new LinkedList<>();
        ArrayList<Integer> visited = new ArrayList<>();

        workers.addLast(cfg.getBasicBlock(1));

        //initial marking at first basic block
        Map<String, Integer> initMarking = new HashMap<>();
        initMarking.put("b1", 1);

        while (!workers.isEmpty()) {
            BasicBlock bb = workers.pollFirst();
            visited.add(bb.getNumber());

            if(!pnet.containsPlace("b"+bb.getNumber())) {
                pnet.createPlace("b"+bb.getNumber());

            }

            if (bb != null) {
                List<SSAInstruction> insts = bb.getAllInstructions();

                SSAInstruction curBBLastInst = bb.getLastInstruction();

                if(curBBLastInst instanceof SSAInvokeInstruction) {
                    SSAInvokeInstruction inst = (SSAInvokeInstruction) curBBLastInst;
                    transId += 1;

                    // p1 (source block) --> t (transition)
                    pnet.createTransition("t"+transId, inst.getDeclaredTarget().getName().toString());
                    pnet.createFlow("b"+bb.getNumber(), "t"+transId);

                    // t (transition) --> p2 (target block)
                    for (ISSABasicBlock succ: Iterator2Iterable.make(cfg.getSuccNodes(bb))) {
                        BasicBlock succBB = (BasicBlock) succ;
                        if(succBB.getAllInstructions().size() > 0) {
                            
                            if (!pnet.containsPlace("b" + succBB.getNumber())) {
                                pnet.createPlace("b" + succBB.getNumber());
                            }
                            
                            if(!visited.contains(succBB.getNumber())) {
                                workers.addLast(succBB);
                            }

                            pnet.createFlow("t"+transId, "b"+succBB.getNumber());
                        }
                    }
                }

                else if(curBBLastInst instanceof SSAConditionalBranchInstruction) {
                    SSAConditionalBranchInstruction inst = (SSAConditionalBranchInstruction) curBBLastInst;
                    String val1 = "v"+ inst.getUse(0);
                    String val2 = "v"+ inst.getUse(1);
                    String op = WalaUtil.getComparisonOp(inst.getOperator().toString());
                    int targetBlock = cfg.getBlockForInstruction(inst.getTarget()).getNumber();

                    transId += 1;

                    // p1 (source block) --> t (transition)
                    pnet.createTransition("t"+transId, "ite("+val1+ " "+op+ " "+val2+ ","+targetBlock+")");
                    pnet.createFlow("b"+bb.getNumber(), "t"+transId);

                    // t (transition) --> p2 (target block)
                    for (ISSABasicBlock succ: Iterator2Iterable.make(cfg.getSuccNodes(bb))) {
                        BasicBlock succBB = (BasicBlock) succ;
                        if(succBB.getAllInstructions().size() > 0) {
                            
                            if (!pnet.containsPlace("b" + succBB.getNumber())) {
                                pnet.createPlace("b" + succBB.getNumber());
                            }
                            
                            if(!visited.contains(succBB.getNumber())) {
                                workers.addLast(succBB);
                            }

                            pnet.createFlow("t"+transId, "b"+succBB.getNumber());
                        }
                    }
                }
            }
        }

        pnet.setInitialMarking(new Marking(pnet, initMarking));
        return pnet;
    }

}
