package slicer.tool;

import java.util.ArrayList;

public class Info {
	public ArrayList<Integer> getCall_lines() {
		return call_lines;
	}
	public void setCall_lines(ArrayList<Integer> call_lines) {
		this.call_lines = call_lines;
	}
	public String getStmtVar() {
		return stmtVar;
	}
	public void setStmtVar(String stmtVar) {
		this.stmtVar = stmtVar;
	}
	public String getArg() {
		return arg;
	}
	public void setArg(String arg) {
		this.arg = arg;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getLno() {
		return lno;
	}
	public void setLno(int lno) {
		this.lno = lno;
	}
	public ArrayList<Integer> getQuery_lines() {
		return query_lines;
	}
	public void setQuery_lines(ArrayList<Integer> query_lines) {
		this.query_lines = query_lines;
	}
	public int getQlno() {
		return qlno;
	}
	public void setQlno(int qlno) {
		this.qlno = qlno;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getApp_conn() {
		return app_conn;
	}
	public void setApp_conn(String app_conn) {
		this.app_conn = app_conn;
	}
	public String getApp_rs() {
		return app_rs;
	}
	public void setApp_rs(String app_rs) {
		this.app_rs = app_rs;
	}
	public String getApp_ps() {
		return app_ps;
	}
	public void setApp_ps(String app_ps) {
		this.app_ps = app_ps;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	@Override
	public String toString() {
		return "Info" 
//				+ ", call start @ lno = " + this.lno
//				+ "\n   called Method = " + this.key
				+ "\n   " +this.key +" @ " + this.method + " @ " + this.lno
				+ "\n   call_lines = " + this.call_lines
				+ "\n   stmtVar = " + this.stmtVar  //+ ", app_stmt=" + this.app_stmt // I think I should remove one of them
				+ "\n   arg = " + this.arg 
				+ "\n   query_lines = " + this.query_lines +", query starts @ qlno = " + qlno 
				+ "\n   query = " + this.query  
				+ "\n   app_conn = "+ this.app_conn 
				+ "\n   app_rs = " + this.app_rs 
				+ "\n   app_ps = " + this.app_ps
				+ "\n ----------------\n";
	}
	ArrayList<Integer> call_lines = new ArrayList<>();
	String stmtVar ="";
	String arg="", query="";
	int lno = 0;
	ArrayList<Integer> query_lines = new ArrayList<>();
	int qlno = 0;
//	int qlnoe = 0;
	String key;
	//+++++++++++
//	String pstmt_query="";
	String app_conn="";
	String app_rs="";
//	String app_sql=""; --> query
	String app_ps="";
//	String app_stmt="";
	String method="";

}
