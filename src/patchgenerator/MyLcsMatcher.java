/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patchgenerator;

/**
 *
 * @author Dareen
 */
import java.util.List;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
//import com.github.gumtreediff.utils.SequenceAlgorithms;
public class MyLcsMatcher {
//    @Override
    public static MappingStore Match(ITree src, ITree dst, MappingStore mappings) {
        List<ITree> srcSeq = TreeUtils.preOrder(src);
        List<ITree> dstSeq = TreeUtils.preOrder(dst);
        List<int[]> lcs = SequenceAlgorithms.longestCommonSubsequenceWithTypeAndLabel_itree(srcSeq, dstSeq);
        for (int[] x : lcs) {
            ITree t1 = srcSeq.get(x[0]);
            ITree t2 = dstSeq.get(x[1]);
            //mappings.addMapping(t1, t2);
            mappings.link(t1, t2);
        }
        return mappings;
    }
    public static List<int[]> Match_int_WithType(ITree src, ITree dst, MappingStore mappings) {
        List<ITree> srcSeq = TreeUtils.preOrder(src);
        List<ITree> dstSeq = TreeUtils.preOrder(dst);
         List<int[]> lcs = SequenceAlgorithms.longestCommonSubsequenceWithType_itree(srcSeq, dstSeq);

//        for (int[] x : lcs) {
//            ITree t1 = srcSeq.get(x[0]);
//            ITree t2 = dstSeq.get(x[1]);
//            //mappings.addMapping(t1, t2);
//            mappings.link(t1, t2);
//        }
        return lcs;
    }
    public static List<int[]> Match_int_WithTypeAndLabel(ITree src, ITree dst, MappingStore mappings) {
        List<ITree> srcSeq = TreeUtils.preOrder(src);
        List<ITree> dstSeq = TreeUtils.preOrder(dst);
        List<int[]> lcs = SequenceAlgorithms.longestCommonSubsequenceWithTypeAndLabel_itree(srcSeq, dstSeq);

//        for (int[] x : lcs) {
//            ITree t1 = srcSeq.get(x[0]);
//            ITree t2 = dstSeq.get(x[1]);
//            //mappings.addMapping(t1, t2);
//            mappings.link(t1, t2);
//        }
        return lcs;
    }
}
