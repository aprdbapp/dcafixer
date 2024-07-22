package slicer;

import java.util.Iterator;

import com.ibm.wala.core.util.strings.Atom;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.debug.Assertions;
//import com.ibm.wala.util.strings.Atom;

public class FindMethod {

	public static CGNode findMethod(CallGraph cg, String Name, String methodCLass) {
		if (Name.equals(null) && methodCLass.equals(null)) {
			return null;
		}
		Atom name = Atom.findOrCreateUnicodeAtom(Name);
		for (CGNode n : cg) {
			if (n.getMethod().getName().equals(name)) {
				if (n.getMethod().getDeclaringClass().getName().toString().equals(methodCLass)) {

					return n;
				}
			}
		}
		Assertions.UNREACHABLE("Failed to find method " + name);
		return null;
	}

	public static Statement find_1st_CallTo(CGNode n, String methodName) {
		IR ir = n.getIR();
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof com.ibm.wala.ssa.SSAAbstractInvokeInstruction call) {
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
					com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1,
							"expected 1 but got " + indices.size());
					return new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());
				}
			}
		}
		Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
		return null;
	}

	public static Statement find_ith_CallTo(CGNode n, String methodName, int ith) {
		int i = 1;// +++
		IR ir = n.getIR();
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof com.ibm.wala.ssa.SSAAbstractInvokeInstruction call) {
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
					if (i == ith) {// +++
						com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
						com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1,
								"expected 1 but got " + indices.size());
						return new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());
					}
				}
			}
		}
		Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
		return null;
	}

}
