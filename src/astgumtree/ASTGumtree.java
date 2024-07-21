/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package astgumtree;

//import static astgumtree.FixPattern.getCtType;
import static astgumtree.EBSet.EB_selector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import com.github.gumtreediff.actions.model.Action;
//import com.github.gumtreediff.actions.model.Move;
//import com.github.gumtreediff.actions.model.Update;
//import com.github.gumtreediff.actions.model.Delete;
//import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
//import com.github.gumtreediff.gen.Registry.Factory;
import com.github.gumtreediff.tree.ITree;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.impl.StaticMDCBinder;
//import org.mozilla.javascript.Token;
//import org.mozilla.javascript.ast.*;

import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.PrettyPrinter;
import spoon.support.modelobs.ChangeCollector;
import spoon.support.sniper.SniperJavaPrettyPrinter;

/**
 *
 * @author Dareen
 */
public class ASTGumtree {

    /**
     * @param element
     */
    public static String partialElementPrint(CtElement element) {
        DefaultJavaPrettyPrinter print = new DefaultJavaPrettyPrinter(element.getFactory().getEnvironment()) {
            @Override
            public DefaultJavaPrettyPrinter scan(CtElement e) {
                if (e != null && e.getMetadata("isMoved") == null) {
                    return super.scan(e);
                }
                return this;
            }
        };

        print.scan(element);
        return print.getResult();
    }

    public static void operationProcessor(Operation op) {
        CtElement node = op.getNode();
        Action action = op.getAction();

        if (action instanceof Insert) {
        }

        if (action instanceof Update) {
        }
        if (action instanceof Move) {
        }

        if (action instanceof Delete) {
        }
    }

    public static void operationTree(Operation op) {
        ITree x = op.getAction().getNode();

        CtElement element = op.getSrcNode();

//        CtElement element = node;
        if (element != null) {
        }
        //int pos;
        if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
//            pos = element.getPosition().getSourceEnd();
            System.out.println("getColumn = " + element.getPosition().getColumn());

            System.out.println("getEndColumn = " + element.getPosition().getEndColumn());

            System.out.println("getEndLine = " + element.getPosition().getEndLine());

            System.out.println("getLine = " + element.getPosition().getLine());

            System.out.println("getSourceEnd = " + element.getPosition().getSourceEnd());
            System.out.println("getSourceStart = " + element.getPosition().getSourceStart());

        }
    }
public static void fixSqlVul(List<Operation> actions){//, ArrayList<String> context){//recieve context

    System.out.println("## Actions ("+ actions.size()+")");
//        System.out.println("## Actions :"+ actions.toString());
//       System.out.println("## context:"+ context.toString());
        for (Operation o : actions) {
            System.out.println("--------------------\n");//+o);
//            o.getAction().
            List<String> parts = operationParts(o);
            System.out.println(parts);

        }

}

public static List<String> operationParts(Operation op) {
//        op.getSrcNode().get

        List<String> parts = new ArrayList<>();
        CtElement node = op.getNode();
        Action action = op.getAction();

        String newline = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();

        // action name
//        System.out.print("\nAction Name: ");
        stringBuilder.append(action.getClass().getSimpleName());
        parts.add(action.getClass().getSimpleName());
        /*++++*/
//        System.out.print(action.getClass().getSimpleName());
        CtElement element = node;

        if (element == null) {
            // some elements are only in the gumtree for having a clean diff but not in the Spoon metamodel
            /*++++*/
//            System.out.print(stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")");
            //return stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")";
            return null;
        }

        // node type
//        System.out.print("\nNode Type: ");
        String nodeType = element.getClass().getSimpleName();

        nodeType = nodeType.substring(2, nodeType.length() - 4);
        stringBuilder.append(" ").append(nodeType);
        parts.add(nodeType);//==============
        /*++++*/
//        System.out.print(nodeType);

        // action position
//        System.out.print("\nAction Position: ");
        CtElement parent = element;
        while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
            parent = parent.getParent();
        }
        String position = " at ";
        if (parent instanceof CtType) {
            position += ((CtType) parent).getQualifiedName();
        }
        if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
            position += ":" + element.getPosition().getLine();

            parts.add(""+element.getPosition().getLine());//==============
        } else {
//            System.out.println("#### NO POS!\n");
            parts.add("No pos");
        }
        if (action instanceof Move) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            position = " from " + element.getParent(CtClass.class).getQualifiedName();
            if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
                position += ":" + element.getPosition().getLine();
            }
            position += " to " + elementDest.getParent(CtClass.class).getQualifiedName();
            if (elementDest.getPosition() != null && !(elementDest.getPosition() instanceof NoSourcePosition)) {
                position += ":" + elementDest.getPosition().getLine();
            }
        }
        stringBuilder.append(position).append(newline);
        /*++++*/
//        System.out.print(position + newline);
        // code change
//        System.out.print("\nCode Change: ");
        String label = partialElementPrint(element);
        if (action instanceof Move) {
            label = element.toString();
        }
        if (action instanceof Update) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            label += " to " + elementDest.toString();
        }
        String[] split = label.split(newline);
        for (String s : split) {
            parts.add(s);
            stringBuilder.append("\t").append(s).append(newline);
            /*++++*/
//            System.out.print("\t" + s + newline);
        }
        /*++++*/
//        System.out.print(stringBuilder.toString());
        return parts;
    }


    public static void operationPartsPrint(Operation op) {
//        op.getSrcNode().get
        CtElement node = op.getNode();
        Action action = op.getAction();

        String newline = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();

        // action name
        System.out.print("\nAction Name: ");
        stringBuilder.append(action.getClass().getSimpleName());
        /*++++*/
        System.out.print(action.getClass().getSimpleName());
        CtElement element = node;

        if (element == null) {
            // some elements are only in the gumtree for having a clean diff but not in the Spoon metamodel
            /*++++*/
            System.out.print(stringBuilder + " fake_node(" + action.getNode().getMetadata("type") + ")");
            //return stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")";
            return;
        }

        // node type
        System.out.print("\nNode Type: ");
        String nodeType = element.getClass().getSimpleName();
        nodeType = nodeType.substring(2, nodeType.length() - 4);
        stringBuilder.append(" ").append(nodeType);
        /*++++*/
        System.out.print(nodeType);

        // action position
        System.out.print("\nAction Position: ");
        CtElement parent = element;
        while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
            parent = parent.getParent();
        }
        String position = " at ";
        if (parent instanceof CtType) {
            position += ((CtType) parent).getQualifiedName();
        }
        if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
            position += ":" + element.getPosition().getLine();
        } else {
            System.out.println("#### NO POS!\n");
        }
        if (action instanceof Move) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            position = " from " + element.getParent(CtClass.class).getQualifiedName();
            if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
                position += ":" + element.getPosition().getLine();
            }
            position += " to " + elementDest.getParent(CtClass.class).getQualifiedName();
            if (elementDest.getPosition() != null && !(elementDest.getPosition() instanceof NoSourcePosition)) {
                position += ":" + elementDest.getPosition().getLine();
            }
        }
        stringBuilder.append(position).append(newline);
        /*++++*/
        System.out.print(position + newline);
        // code change
        System.out.print("\nCode Change: ");
        String label = partialElementPrint(element);
        if (action instanceof Move) {
            label = element.toString();
        }
        if (action instanceof Update) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            label += " to " + elementDest.toString();
        }
        String[] split = label.split(newline);
        for (String s : split) {
            stringBuilder.append("\t").append(s).append(newline);
            /*++++*/
            System.out.print("\t" + s + newline);
        }
        /*++++*/
        System.out.print(stringBuilder);
    }

    public static void main(String[] args) throws Exception {
        // TODO code application logic here
//        gumtree.spoon.AstComparator
        AstComparator diff = new AstComparator();

//        ArrayList<String> context = new ArrayList<String>();
//        //0,69,72,74,0
//        context.add("0");
//        context.add("69");
//        context.add("72");
//        context.add("74");
//        context.add("0");




        VulMD vmd = new VulMD("q,s,[v]", "SQLIV");
        System.out.println("-------------------------");
        String vslPath = vmd.get_v_sl_path();
        String sslPath = vmd.get_s_sl_path();
        System.out.println("vslPath: ("+ vslPath + ")"+"\nsslPath: (" + sslPath +")");
        String vconPath = vmd.get_v_con_path();
        vmd.get_v_con_data();
        ArrayList<String> sl_lines = vmd.get_v_sl_code_lines();
        int i=1;
        for (String l : sl_lines){
            System.out.println(i +" # "+l);
            i++;
        }
        System.out.println(vconPath+"\n-------------------------");
        ArrayList<String> src_lines = vmd.get_v_src_code_lines();
//        int j=1;
//        for (String l : src_lines){
//            System.out.println(j +" # "+l);
//            j++;
//        }
        System.out.println(vconPath+"\n-------------------------");
        File f1 = new File(vslPath);
        File f2 = new File(sslPath);



//        File f1 = new File( "/Users/dareen/Fixer/Experiments/TrainingCases/example_2/slices/test1_vul.java");
//        File f2 = new File("/Users/dareen/Fixer/Experiments/TrainingCases/example_2/slices/test1.java");
//        File f1 = new File( "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/CV1_vul.java";
//        File f2 = new File("/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/CV1_01.java";

//        File f1 = new File("/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS.java");
//        File f2 = new File("/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS_withFin_and_stmt.java");

        //"/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS_NoFin.java
        Diff result = diff.compare(f1, f2); //diffs to transform  f1 to be like f2

//                Diff result = diff.compare(f2, f1);
        // ===================== Another way to find the diff:
//        CtType f1_sat = getCtType(f1);
//        CtType f2_sat = getCtType(f2);
//        Diff result2 = new AstComparator().compare(f1_sat, f2_sat);
//         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!\n"+f1_sat.getAllMethods());
//        String path = f1_sat.getPosition().getFile().getAbsolutePath();
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!\n"+path);
        // =====================

        List<Operation> actions = result.getRootOperations();
//        List<Operation> actions = result.getAllOperations();

        System.out.println("## Actions ("+ actions.size()+")");

        for (Operation o : actions) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");//+o);
            operationPartsPrint(o);
//            operationTree(o);
//           System.out.println("######### dst: "+o.getDstNode().toString());
//           System.out.println("$$$$$$$$$ src: "+o.getSrcNode().toString());
        }
       // Operation o9 = new Operation();

        fixSqlVul( actions);//, context);

        EB_selector(actions, vmd, "q_s_v", 1, 1);
        System.out.println("&&&&&&\n");

//        actions.add(null);
       // TreeUtils.
//        System.out.print("\n===============\n");
//        System.out.print(actions.get(1).toString());
//        System.out.print("@@@@@\n" );
//        operationPartsPrint(actions.get(1));
        //===========================
//        CtElement element = actions.get(1).getNode();
//        if (element == null) {
//            System.out.print("fake node");
//        }

//        System.out.print(actions.get(1));
        //AstComparator x = (AstComparator)
//        System.out.print(new AstComparator().compare(el1, el2));
//        AstComparator.main(new String[] { el1.getAbsolutePath(), el2.getAbsolutePath() });
//        System.out.print(new AstComparator().main(new String[]{el1.getAbsolutePath(), el2.getAbsolutePath()}));
        //.compare((CtElement) el1, (CtElement) el2);
//        gumtree.spoon.AstComparator;//<> <>;
        //final Diff result = new AstComparator().compare(f1, f2);
        CtElement ancestor = result.commonAncestor();
        Factory factory = ancestor.getFactory();
        Environment env = factory.getEnvironment();
        env.setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(env));

        //attach ChangeCollector, an Exception will be throwed
        ChangeCollector changeCollector = new ChangeCollector();
        changeCollector.attachTo(env);

        //for my test file, the common ancestor is an instance of CtClass
        CtClass type = ancestor.getParent(CtClass.class);
        if (type == null) {
            if (CtClass.class.isAssignableFrom(ancestor.getClass())) {
                type = (CtClass) ancestor;
            }
        }

        //TODO: the replace part here-----------------
//        CtClass type5 = result.commonAncestor().getParent(CtClass.class);
//        List<Operation> opertations = result.getAllOperations();
//        for (Operation op : opertations) {
//            op.getSrcNode().replace(op.getDstNode());
//
//        }

        //--------------------------------------------
        Launcher launcher = new Launcher(factory);
        PrettyPrinter printer = launcher.getEnvironment().createPrettyPrinter();

        List typeList = Collections.singletonList(type);

        //If type is null,  an Exception will be throwed
        printer.calculate(factory.CompilationUnit().getOrCreate(type), typeList);

        String output = printer.getResult();

        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println(output);
    }

}
