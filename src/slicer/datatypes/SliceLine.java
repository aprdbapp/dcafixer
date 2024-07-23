package slicer.datatypes;

import com.ibm.wala.ipa.slicer.Statement;

public class SliceLine implements Comparable<SliceLine> {
	public int lno;
	public String method;
	public String stmt;
	public Statement walaStmt;

	public int getLineNo() {
		return lno;
	}

	@Override
	public int compareTo(SliceLine o) {
		return Integer.compare(lno, o.lno);
	}

	@Override
	public String toString() {
		return "method: " + method + " #" + lno + stmt + "\n" + walaStmt.toString() + "\n--------------\n";
	}

	public String walaStmttoString() {
		return "method: " + method + " #" + lno + walaStmt.toString() + "\n--------------\n";
	}
}
