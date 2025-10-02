package utils;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.client.impl.ZeroOneContainerCFABuilderFactory;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WalaUtil {

    public static CallGraph buildCallgraph(String jarSource, String entryClass) {

        try {
            File jarFile = new FileProvider().getFile(jarSource);

            AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarFile.getAbsolutePath(), null);
            IClassHierarchy cha = ClassHierarchyFactory.make(scope);
            Iterable<Entrypoint> entrypoints = getEntrypoints("L"+entryClass, cha);

            AnalysisOptions options = new AnalysisOptions();
            options.setEntrypoints(entrypoints);
            options.getSSAOptions().setDefaultValues(SymbolTable::getDefaultValue);
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);

            IAnalysisCacheView cache =
                    new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory(), options.getSSAOptions());

            // CallGraphBuilder builder = new ZeroCFABuilderFactory().make(options, cache, cha, scope, false);
            CallGraphBuilder<?> builder = new ZeroOneContainerCFABuilderFactory().make(options, cache, cha, scope);

            System.out.println("Building call graph for "+jarSource+" ...");
            CallGraph cg = builder.makeCallGraph(options, null);

            return cg;

        } catch (IOException | ClassHierarchyException | CallGraphBuilderCancelException e) {
            e.printStackTrace();
            return null;
        }

    }


    public static List getCGNodesWithoutPrimordial(CallGraph cg) {
        List<CGNode> nodes = new ArrayList<>();
        for (CGNode cgNode: cg) {
            // skip library classes
            if (isPrimordial(cgNode)) {
                continue;
            }
            nodes.add(cgNode);
        }
        return nodes;
    }



    public static void printVariableLocalNames(CGNode node) {
        SSAInstruction[] instructions = node.getIR().getInstructions();
        Map<Integer, Set<String>> valNames = HashMapFactory.make();

        for(int j=0; j<instructions.length; j++) {
            if (instructions[j] == null) {
                continue;
            }
            for (int v = 0; v < instructions[j].getNumberOfDefs(); v++) {
                int valNum = instructions[j].getDef(v);
                String[] srcNames = node.getIR().getLocalNames(j, valNum);
                if (srcNames != null && srcNames.length > 0) {
                    if (!valNames.containsKey(valNum)) {
                        valNames.put(valNum, HashSetFactory.<String>make());
                    }
                    for (String s : srcNames) {
                        valNames.get(valNum).add(s);
                    }
                }
            }
            for (int v = 0; v < instructions[j].getNumberOfUses(); v++) {
                int valNum = instructions[j].getUse(v);
                String[] srcNames = node.getIR().getLocalNames(j, valNum);
                if (srcNames != null && srcNames.length > 0) {
                    if (!valNames.containsKey(valNum)) {
                        valNames.put(valNum, HashSetFactory.<String>make());
                    }
                    for (String s : srcNames) {
                        valNames.get(valNum).add(s);
                    }
                }
            }

        }
        if (!valNames.isEmpty()) {
            for (Map.Entry<Integer, Set<String>> e : valNames.entrySet()) {
                System.out.println("v"+e.getKey()+" --> "+e.getValue().toString());
            }
        }
    }

    public static boolean isPrimordial(CGNode node) {
        TypeReference declaringClass = node.getMethod().getReference().getDeclaringClass();
        return declaringClass.getClassLoader().equals(ClassLoaderReference.Primordial);
    }
    // prints values of all variables, v0, v1, ..., so on for current function
    public static void printAllVariables(CGNode cgNode) {
        SymbolTable symbolTable = cgNode.getIR().getSymbolTable();
        System.out.println("Variable values: ");
        for(int i = 1; i <= symbolTable.getMaxValueNumber(); i++) {
            if(symbolTable.getValue(i) != null) {
                System.out.println("v" + i + ": " + symbolTable.getValue(i));
            }
        }
    }

    public static void printAllParameters(CGNode cgNode) {
        SymbolTable symbolTable = cgNode.getIR().getSymbolTable();
        System.out.println(symbolTable.getNumberOfParameters());
        if(symbolTable.getNumberOfParameters() > 0) {
            System.out.println("Function params: ");
            for (int i = 1; i <= symbolTable.getNumberOfParameters(); i++) {
                System.out.println("v" + i + ": " + symbolTable.getParameter(i-1));
            }
        }
    }

    public static String getComparisonOp(String walaOp) {
        if(walaOp.equals("eq")) {
            return "==";
        } else if(walaOp.equals("ne")) {
            return "!=";
        }else if(walaOp.equals("lt")) {
            return "<";
        }else if(walaOp.equals("gt")) {
            return ">";
        }else if(walaOp.equals("ge")) {
            return ">=";
        }else if(walaOp.equals("le")) {
            return "<=";
        } else {
            return "NOP";
        }
    }

    public static void cfg2Json(CGNode cgNode) {
        IR ir = cgNode.getIR();
        System.out.println(cgNode.getMethod().getSignature());
        System.out.println(ir);
        SSACFG cfg = ir.getControlFlowGraph();
        Queue<BasicBlock> workers = new LinkedList<>();
        workers.add(cfg.getBasicBlock(0));

        while (!workers.isEmpty()) {
            BasicBlock bb = workers.poll();
            if (bb != null) {
                List<SSAInstruction> insts = bb.getAllInstructions();
                System.out.println("BB"+bb.getNumber()+" ("+cfg.getSuccNodeNumbers(bb)+"):");

                for (SSAInstruction inst: insts) {
                    System.out.println(""+inst.iIndex()+"   "+inst);

                    if(inst instanceof SSAConditionalBranchInstruction) {
//                        System.out.println(RefactorInstruction
//                                .refactorCondBranch((SSAConditionalBranchInstruction) inst, ir));
                    }

                    if(inst instanceof SSABinaryOpInstruction) {
//                        System.out.println(RefactorInstruction
//                                .refactorBinaryOp((SSABinaryOpInstruction) inst));
                    }

                    if(inst instanceof SSAGotoInstruction) {
//                        System.out.println(RefactorInstruction
//                                .refactorGoto((SSAGotoInstruction) inst, ir));
                    }

                }
                for (ISSABasicBlock succ: Iterator2Iterable.make(cfg.getSuccNodes(bb))) {
                    BasicBlock succBB = (BasicBlock) succ;
                    workers.add(succBB);
                }

            }

        }
    }

    public static void printCGIR(CallGraph cg) {
        for (CGNode cgNode: cg) {
            //System.out.println(cgNode.getMethod().getDeclaringClass().getClassLoader().getName());
            if(!isPrimordial(cgNode)) {
                IR ir = cgNode.getIR();
                printAllVariables(cgNode);
                System.out.println(cgNode.getMethod().getSignature());
                System.out.println(ir);
                SSACFG cfg = ir.getControlFlowGraph();
                SymbolTable symbolTable = ir.getSymbolTable();

                //printAllVariables(cgNode);
                //printAllParameters(cgNode);
//                System.out.println(cgNode.getIR().getControlFlowGraph());

//                for(SSAInstruction inst: Iterator2Iterable.make(cgNode.getIR().iterateAllInstructions())) {
//                    System.out.println(inst);
//                }

//                for (ISSABasicBlock sbb : Iterator2Iterable.make(cgNode.getIR().getBlocks())) {
//                    SSACFG.BasicBlock blk = (SSACFG.BasicBlock) sbb;
//
//                    System.out.println("Printing block "+blk.getNumber()+": \n");
//                    for(SSAInstruction inst: blk.getAllInstructions()) {
//                        System.out.println(inst);
//                        System.out.println(inst.iIndex());
//                    }
//                }
            }
        }
    }

    public static CGNode findCGNode(CallGraph cg, String functionName) {
        for (CGNode cgNode: cg) {
            if(!isPrimordial(cgNode)) {
                //System.out.println(cgNode.getMethod().getName());
                if(cgNode.getMethod().getName().toString().equals(functionName)) {
                    return cgNode;
                }
            }
        }
        System.out.println("No method found with name: "+functionName);
        return null;
    }

    public static void printAllInstructions(List<SSAInstruction> instructions) {
        for(SSAInstruction inst: instructions) {
            System.out.println(inst.toString());
        }
    }

    public static List<SSAInstruction> getInstructions(CGNode cgNode) {
        List<SSAInstruction> insts = new ArrayList<>();
        for(SSAInstruction inst: cgNode.getIR().getInstructions()) {
            if(inst != null) {
                insts.add(inst);
            }
        }
        return insts;
    }

    public static void getClasses(IClassHierarchy cha) {
        System.out.println("Getting all class names....");

        for(IClass c:cha){
            if (c.getClassLoader().getName().toString().equals("Source")){
                String classname = c.getName().toString();
                System.out.println(classname+"\n");
            }
        }
    }

    public static void getMethods(IClassHierarchy cha)  {
        System.out.println("Getting Methods...");
        for(IClass c:cha){
            if (!c.getClassLoader().getName().toString().equals("Primordial")){
                Collection<? extends IMethod> methods = c.getAllMethods();
                for(IMethod m : methods){
                    System.out.println("method: "+m.getSignature());
                }
            }
        }
    }
    public static void getFields(IClassHierarchy cha)  {
        System.err.println("Getting Fields...");
        for(IClass c:cha){
            if (c.getClassLoader().getName().toString().equals("Source")){
                Collection<? extends IField> fields = c.getAllFields();
                for(IField f : fields){
                    System.out.println("field: "+f.getReference().getSignature());
                }
            }
        }
    }

    public static String getWalaMethodName(CGNode cgNode) {
        return cgNode.getMethod().getName().toString();
    }

    public static TypeReference getWalaReturn(CGNode cgNode) {
        return cgNode.getMethod().getReturnType();
    }

    private static Iterable<Entrypoint> getEntrypoints(String mainClass, IClassHierarchy cha) {
        return Util.makeMainEntrypoints(JavaSourceAnalysisScope.SOURCE, cha, new String[] {mainClass});
    }

}
