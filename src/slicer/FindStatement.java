package slicer;

import java.util.Iterator;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;

/**************************************/
public class FindStatement {
	public static Statement findCallTo(CGNode n, String seed) {
		IR ir = n.getIR();
//		System.out.println(ir.toString());
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof SSAInvokeInstruction call) {
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(seed)) {

					IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
					return new NormalStatement(n, indices.intIterator().next());
				}
			}
		}
		Assertions.UNREACHABLE("Failed to find call to " + seed + " in " + n);
		return null;
	}

	/**************************************/
	public static Statement findCallTo_ith(CGNode n, String seed, int ith) {
		int i = 1;// +++
		IR ir = n.getIR();
//		System.out.println(ir.toString());
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof SSAInvokeInstruction call) {
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(seed)) {
					/* +++ */if (i == ith) {
						IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
						Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
						return new NormalStatement(n, indices.intIterator().next());
						/* +++ */}
				}
			}

		}
		Assertions.UNREACHABLE("Failed to find call to " + seed + " in " + n);
		return null;
	}

	/**************************************/
	public static Statement find_return(CGNode n) {
		IR ir = n.getIR();
//		System.out.println(ir.toString());
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof SSAReturnInstruction) {
//				SSAReturnInstruction call = (SSAReturnInstruction) s;
//				System.out.println(new NormalStatement(n, ((SSAReturnInstruction) s).iindex));
				return new NormalStatement(n, s.iIndex());
			}
		}
		return null;
	}
}
