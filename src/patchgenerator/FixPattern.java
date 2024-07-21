/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patchgenerator;

import static spoon.testing.utils.Check.assertNotNull;
//import static spoon.testing.utils.Check.assertCtElementEquals;
import static spoon.testing.utils.ModelUtils.createFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import com.github.gumtreediff.tree.ITree;
//import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;

import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.compiler.Environment;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.path.CtPath;
import spoon.reflect.visitor.PrettyPrinter;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;
import spoon.support.modelobs.ChangeCollector;
import spoon.support.sniper.SniperJavaPrettyPrinter;

/**
 *
 * @author Dareen
 */
public class FixPattern {

    public static CtType getCtType(File file) throws Exception {

        SpoonResource resource = SpoonResourceHelper.createResource(file);
        return getCtType(resource);
    }

    public static CtType getCtType(SpoonResource resource) {
        Factory factory = createFactory();
        factory.getModel().setBuildModelIsFinished(false);
        SpoonModelBuilder compiler = new JDTBasedSpoonCompiler(factory);
        compiler.getFactory().getEnvironment().setLevel("OFF");
        compiler.addInputSource(resource);
        compiler.build();

        if (factory.Type().getAll().size() == 0) {
            return null;
        }

        // let's first take the first type.
        CtType type = factory.Type().getAll().get(0);
        // Now, let's ask to the factory the type (which it will set up the
        // corresponding
        // package)

        return factory.Type().get(type.getQualifiedName());
    }

    public static TreeContext getASTContext(String path) throws FileNotFoundException {
        Launcher spoon = new Launcher();
        Factory factory = spoon.createFactory();

        spoon.createCompiler(factory,
                SpoonResourceHelper.resources(path))
                .build();

        CtType<?> astLeft = factory.Type().get("PS");
        SpoonGumTreeBuilder builder = new SpoonGumTreeBuilder();
        ITree generatedTree = builder.getTree(astLeft);

        TreeContext tcontext = new TreeContext();
        tcontext.setRoot(generatedTree);
        return tcontext;
    }

    public void test_bug_Possition() throws Exception {
        AstComparator comparator = new AstComparator();
        File fl = new File("src/test/resources/examples/roots/test8/left_QuickNotepad_1.13.java");
        File fr = new File("src/test/resources/examples/roots/test8/right_QuickNotepad_1.14.java");

        CtType<?> astLeft = comparator.getCtType(fl);

        assertNotNull(astLeft);

        CtType<?> astRight = comparator.getCtType(fr);
        assertNotNull(astRight);

        Diff diffResult = comparator.compare(astLeft, astRight);
        List<Operation> rootOperations = diffResult.getRootOperations();
        getPaths(rootOperations);
        List<Operation> allOperations = diffResult.getAllOperations();
        getPaths(allOperations);

        System.out.println("Shouid be 1 : " + rootOperations.size());

        SourcePosition position = rootOperations.get(0).getSrcNode().getPosition();
        System.out.println("Shouid be > 0 : " + position.getLine());
        System.out.println("Shouid be 113 : " + position.getLine());

        if (!(position instanceof NoSourcePosition)) {
            System.out.println("Not NoSourcePosition ");
        }

    }

    private void getPaths(List<Operation> rootops) {
        for (Operation<?> op : rootops) {

            CtElement left = op.getSrcNode();
            CtPath pleft = left.getPath();
            System.out.println("pleft: " + pleft.toString());
            assertNotNull(pleft);

            CtElement right = op.getSrcNode();
            CtPath pright = right.getPath();
            System.out.println("pleft: " + pleft);
            assertNotNull(pright);

        }
    }

    public static void main(String[] args) throws Exception {
//        File f1 = new File("/Users/dareen/Fixer/Experiments/TrainingCases/example_2/slices/CV1_vul_slice_0.java");
//        File f2 = new File("/Users/dareen/Fixer/Experiments/TrainingCases/example_2/slices/CV1_01_slice_0.java");
//        File f1 = new File("/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/PaperExample_vul.java");
        String f1_path = "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS.java";
        File f1 = new File(f1_path);
        File f2 = new File("/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS_withFin_and_stmt.java");

        final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
        //SpoonResource resource = SpoonResourceHelper.createResource(f1);
        CtType f_sat = getCtType(f1);
        //fsta.getClass().
//        fsta.getFactory();
        ITree code_AST = scanner.getTree(f_sat);
        TreeContext code_context = scanner.getTreeContext();


//        TreeContext code_context2  = getASTContext(f1_path);
//
//        System.out.println("code_context 1: "+code_context2);
//        //System.out.println("code_context 2: "+code_context2.toString());
//        TreeSerializer ts = TreeIoUtils.toJson(code_context2);
//        String out = ts.toString();
//          System.out.println("\n************************\n");
//        System.out.println("\n************************\n");
//        System.out.println(out);


//        code_AST.getChild(2).setParent(null);
//       code_AST.insertChild(code_AST, i);
        //System.out.print(code_AST.toTreeString());
//        System.out.println("ROOT?: "+code_AST.isRoot());
//        System.out.println("Children: "+code_AST.getChildren().toString());
        //Iterator<ITree> iterator = code_AST.postOrder().iterator();
//        System.out.println("ITree breadthFirstSearch:");
//        Iterator<ITree> iterator = code_AST.breadthFirst().iterator();
//        int i=0;
//        while(iterator.hasNext()){
//            System.out.println(i++ +") "+iterator.next().getLabel());
//        }
//        System.out.println("--------------------------");
//        System.out.println("=================================");
//        System.out.println("Original Tree:");
//        System.out.println(code_AST.toPrettyString(code_context));
//        System.out.println(code_AST.toTreeString());
//        System.out.println("=================================");
//        System.out.println("TreeUtils breadthFirstSearch:");
//         List<ITree> bfs_list = TreeUtils.breadthFirst(code_AST);
//         int x=0;
//         for(ITree t : bfs_list){
//             System.out.println(x++ +") "+ t.getLabel()+" --- " + t.getDepth());
//         }
//
//        System.out.println("=================================");
//        System.out.println("TreeUtils postOrder:");
//
//         List<ITree> po_list = TreeUtils.postOrder(code_AST);
//         int y=0;
//         for(ITree t : po_list){
//             System.out.println(y++ +") "+ t.getLabel() +" --- " + t.getDepth());
//         }
//
//
//          System.out.println("=================================");
//         System.out.println("TreeUtils preOrder:");
//
//         List<ITree> preo_list = TreeUtils.preOrder(code_AST);//(code_AST);
//         //preo_list.
//         int j=0;
//         for(ITree t : preo_list){
//             System.out.println(j++ +") "+ t.getLabel() +" ---ch# " + t.getChildren().size());
//         }


        System.out.println("\n************************\n");
//         TreeSerializer ts = TreeIoUtils.toJson(code_context);
//         Writer output = new FileWriter("/Users/Dareen/Documents/output100.txt");
//
//         ts.writeTo(output);
//         CtElement ancestor = result.commonAncestor();
        Factory factory = f_sat.getFactory();//(Factory) ancestor.getFactory();
//        fsta.getClass()
        Environment env = factory.getEnvironment();
        env.setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(env));

        //attach ChangeCollector, an Exception will be throwed
        ChangeCollector changeCollector = new ChangeCollector();
        changeCollector.attachTo(env);

        //for my test file, the common ancestor is an instance of CtClass
        CtClass type = f_sat.getParent(CtClass.class);
        if (type == null) {
            if (CtClass.class.isAssignableFrom(f_sat.getClass())) {
                type = (CtClass) f_sat;
            }
        }

        //TODO: the replace part here-----------------
        CtClass type5 = f_sat.getParent(CtClass.class);
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
        //List<String> x= MyTreeUtils.breadthFirst_markleaves(code_AST);

        //TODO: Check also"
         /*
         https://github.com/GumTreeDiff/gumtree/blob/d8c5d910c5c602d803ae115b501fbe259681f23f/core/src/test/java/com/github/gumtreediff/test/TestTreeUtils.java
         TestTreeUtils
         TestSequenceAlgorithms.java
         https://www.codota.com/code/java/classes/com.github.gumtreediff.tree.ITree

         */
    }
}
