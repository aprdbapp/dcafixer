package slicer.datatypes;

public class CodeLine {
	// public String method = null;
	public int lno;
	public String stmt;

	public int getLno() {
		return lno;
	}

	public void setLno(int lno) {
		this.lno = lno;
	}

	public String getStmt() {
		return stmt;
	}

	public void setStmt(String stmt) {
		this.stmt = stmt;
	}

}
