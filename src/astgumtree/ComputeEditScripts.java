/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package astgumtree;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
//import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
//import com.github.gumtreediff.jdt.JdtTreeGenerator;

import java.io.File;
import java.io.IOException;


/**
 *
 * @author Dareen
 */
public class ComputeEditScripts {
// Run.initGenerators(); // registers the available parsers
//String srcFile = "\"/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS.java\"";
//String dstFile = "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS_withFin_and_stmt.java";
//
//
//        
//Tree src = TreeGenerators.getInstance().getTree(srcFile).getRoot(); // retrieves and applies the default parser for the file 
//Tree dst = TreeGenerators.getInstance().getTree(dstFile).getRoot(); // retrieves and applies the default parser for the file 
//Matcher defaultMatcher = Matchers.getInstance().getMatcher(); // retrieves the default matcher
//MappingStore mappings = defaultMatcher.match(src, dst); // computes the mappings between the trees
//EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
//EditScript actions = editScriptGenerator.computeActions(mappings); // computes the edit script
}
