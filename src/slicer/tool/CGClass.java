package slicer.tool;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class CGClass {
	public CallGraph getCg() {
		return cg;
	}
	public void setCg(CallGraph cg) {
		this.cg = cg;
	}
	public CallGraphBuilder<InstanceKey> getBuilder() {
		return builder;
	}
	public void setBuilder(CallGraphBuilder<InstanceKey> builder) {
		this.builder = builder;
	}
	CallGraph cg;
	CallGraphBuilder<InstanceKey> builder ;
}
