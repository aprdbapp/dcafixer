/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patchgenerator;

import static astgumtree.FixPattern.getCtType;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import java.io.File;
import java.util.List;
import spoon.reflect.declaration.CtType;

/**
 *
 * @author Dareen
 */
public class LCSFinder {
    public static int comput_lcsLength(String path1, String path2) throws Exception{
     File f1 = new File(path1);
        File f2 = new File(path2);
         final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
        //SpoonResource resource = SpoonResourceHelper.createResource(f1);
        CtType f1_sat = getCtType(f1);
        CtType f2_sat = getCtType(f2);
        // fsta.getClass().
//        fsta.getFactory();
        MappingStore mappings = new MappingStore();
        
        ITree code1_AST = scanner.getTree(f1_sat);
         ITree code2_AST = scanner.getTree(f2_sat);
        TreeContext code_context = scanner.getTreeContext();
        
        MyLcsMatcher.Match(code1_AST, code2_AST, mappings);
        // WithType is better because it doesn't count all differences in vars names.
//        List<int[]> t = MyLcsMatcher.Match_int_WithType(code1_AST, code2_AST, mappings);
//        System.out.println("WithType "+t.size());
//        return t.size();
        List<int[]> tl= MyLcsMatcher.Match_int_WithTypeAndLabel(code1_AST, code2_AST, mappings);
//        System.out.println("WithTypeAndLabel "+tl.size());
        return tl.size();
    }
     public static void main(String[] args) throws Exception {
         String appSrc_path = "/Users/Dareen/NetBeansProjects/smallBank/src/TSet/";
        String path1 = "/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/";
        
        comput_lcsLength(path1+"vvv.java",path1+"xxx1.java");
         comput_lcsLength(path1+"vvv.java",path1+"xxx2.java");
          comput_lcsLength(path1+"vvv.java",path1+"xxx3.java");
     }
}
