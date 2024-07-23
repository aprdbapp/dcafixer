package slicer.datatypes;

import com.ibm.wala.ipa.slicer.Statement;

public class ExecuteStmt {
	public char type; // 'p' = preparedStatement
	public ClassUsed classused = ClassUsed.NA;
	public String methodused;
	public int lno;
	public Statement stmt;
}
