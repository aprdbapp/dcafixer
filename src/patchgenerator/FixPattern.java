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


}
