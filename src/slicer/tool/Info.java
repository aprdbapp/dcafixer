package slicer.tool;

import java.util.ArrayList;

import flocalization.G;

//import patchgenerator.PatternGen.Patch;

public class Info {
	public String getConn_pass_dt() {
		return conn_pass_dt;
	}
	public void setConn_pass_dt(String conn_pass_dt) {
		this.conn_pass_dt = conn_pass_dt;
	}
	public String getQuery_type() {
		return query_type;
	}
	public void setQuery_type(String query_type) {
		this.query_type = query_type;
	}
	public String getConn_url_value() {
		return conn_url_value;
	}
	public void setConn_url_value(String conn_url_value) {
		this.conn_url_value = conn_url_value;
	}
	public String getConn_url_value_type() {
		return conn_url_value_type;
	}
	public void setConn_url_value_type(String conn_url_value_type) {
		this.conn_url_value_type = conn_url_value_type;
	}
	public String getConn_prop() {
		return conn_prop;
	}
	public void setConn_prop(String conn_prop) {
		this.conn_prop = conn_prop;
	}
	public String getConn_user_value() {
		return conn_user_value;
	}
	public void setConn_user_value(String conn_user_value) {
		this.conn_user_value = conn_user_value;
	}
	public String getConn_user_value_type() {
		return conn_user_value_type;
	}
	public void setConn_user_value_type(String conn_user_value_type) {
		this.conn_user_value_type = conn_user_value_type;
	}
	public String getConn_pass_value() {
		return conn_pass_value;
	}
	public void setConn_pass_value(String conn_pass_value) {
		this.conn_pass_value = conn_pass_value;
	}
	public String getConn_pass_value_type() {
		return conn_pass_value_type;
	}
	public void setConn_pass_value_type(String conn_pass_value_type) {
		this.conn_pass_value_type = conn_pass_value_type;
	}
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

	public String getApp_rs_dt() {
		return app_rs_dt;
	}
	public void setApp_rs_dt(String dt) {
		this.app_rs_dt = dt;
	}



	@Override
	public String toString() {
		if (G.ConnIPList.contains(this.key)) {
			return toString_conn();
		} else if (G.ExIPList.contains(this.key) ||  this.key.equals("createStatement")) {
			return toString_exec();
		} else {
			return toString_all();
		}
	}
	public String toString_all() {
		return "Info"
//				+ ", call start @ lno = " + this.lno
//				+ "\n   called Method = " + this.key
				+ "\n   " +this.key +" @ " + this.method + " @ " + this.lno
				+ "\n   call_lines = " + this.call_lines
				+ "\n   stmtVar = " + this.stmtVar  //+ ", app_stmt=" + this.app_stmt // I think I should remove one of them
				+ "\n   arg = " + this.arg
				+ "\n   query_lines = " + this.query_lines +", query starts @ qlno = " + qlno
				+ "\n   query = " + this.query
				+ "\n   query_type = " + this.query_type
				+ "\n   app_conn = "+ this.app_conn
				+ "\n   app_rs = " + this.app_rs
				+ "\n   app_ps = " + this.app_ps
				+ "\n   app_rs_dt = " + this.app_rs_dt
				+ "\n   conn_prop= " + this.conn_prop
				+ "\n   conn_url_var= " + this.conn_url_var
				+ "\n   conn_url_value= " + this.conn_url_value
				+ "\n   conn_url_value_type= " + this.conn_url_value_type
				+ "\n   conn_url_ln= "+this.conn_url_ln
				+ "\n   conn_url_lines= "+this.conn_url_lines
				+ "\n   conn_user_var= " + this.conn_user_var
				+ "\n   conn_user_value= " + this.conn_user_value
				+ "\n   conn_user_value_type= " + this.conn_user_value_type
				+ "\n   conn_user_ln= "+this.conn_user_ln
				+ "\n   conn_user_lines= "+ this.conn_user_lines
				+ "\n   conn_pass_var= " + this.conn_pass_var
				+ "\n   conn_pass_value= " + this.conn_pass_value
				+ "\n   conn_pass_value_type= " + this.conn_pass_value_type
				+ "\n   conn_pass_dt= " + this.conn_pass_dt
				+ "\n   conn_pass_ln= "+ this.conn_pass_ln
				+ "\n   conn_pass_lines= "+ this.conn_pass_lines
				+ "\n ----------------\n";
	}


	public String toString_conn() {
		return "Info"
//				+ ", call start @ lno = " + this.lno
//				+ "\n   called Method = " + this.key
				+ "\n   " +this.key +" @ " + this.method + " @ " + this.lno
				+ "\n   call_lines = " + this.call_lines
//				+ "\n   stmtVar = " + this.stmtVar  //+ ", app_stmt=" + this.app_stmt // I think I should remove one of them
//				+ "\n   arg = " + this.arg
//				+ "\n   query_lines = " + this.query_lines +", query starts @ qlno = " + qlno
//				+ "\n   query = " + this.query
				+ "\n   app_conn = "+ this.app_conn
//				+ "\n   app_rs = " + this.app_rs
//				+ "\n   app_ps = " + this.app_ps
//				+ "\n   app_rs_dt = " + this.app_rs_dt
				+ "\n   conn_prop= " + this.conn_prop
				+ "\n   conn_url_var= " + this.conn_url_var
				+ "\n   conn_url_value= " + this.conn_url_value
				+ "\n   conn_url_value_type= " + this.conn_url_value_type
				+ "\n   conn_url_ln= "+this.conn_url_ln
				+ "\n   conn_url_lines= "+this.conn_url_lines
				+ "\n   conn_user_var= " + this.conn_user_var
				+ "\n   conn_user_value= " + this.conn_user_value
				+ "\n   conn_user_value_type= " + this.conn_user_value_type
				+ "\n   conn_user_ln= "+this.conn_user_ln
				+ "\n   conn_user_lines= "+ this.conn_user_lines
				+ "\n   conn_pass_var= " + this.conn_pass_var
				+ "\n   conn_pass_value= " + this.conn_pass_value
				+ "\n   conn_pass_value_type= " + this.conn_pass_value_type
				+ "\n   conn_pass_dt= " + this.conn_pass_dt
				+ "\n   conn_pass_ln= "+ this.conn_pass_ln
				+ "\n   conn_pass_lines= "+ this.conn_pass_lines
				+ "\n ----------------\n";
	}

	public String toString_exec() {
		return "Info"
//				+ ", call start @ lno = " + this.lno
//				+ "\n   called Method = " + this.key
				+ "\n   " +this.key +" @ " + this.method + " @ " + this.lno
				+ "\n   call_lines = " + this.call_lines
				+ "\n   stmtVar = " + this.stmtVar  //+ ", app_stmt=" + this.app_stmt // I think I should remove one of them
				+ "\n   arg = " + this.arg
				+ "\n   query_lines = " + this.query_lines +", query starts @ qlno = " + qlno
				+ "\n   query = " + this.query
				+ "\n   query_type = " + this.query_type
				+ "\n   app_conn = "+ this.app_conn
				+ "\n   app_rs = " + this.app_rs
				+ "\n   app_ps = " + this.app_ps
				+ "\n   app_rs_dt = " + this.app_rs_dt
//				+ "\n   conn_prop= " + this.conn_prop
//				+ "\n   conn_url_var= " + this.conn_url_var
//				+ "\n   conn_url_value= " + this.conn_url_value
//				+ "\n   conn_url_value_type= " + this.conn_url_value_type
//				+ "\n   conn_url_ln= "+this.conn_url_ln
//				+ "\n   conn_url_lines= "+this.conn_url_lines
//				+ "\n   conn_user_var= " + this.conn_user_var
//				+ "\n   conn_user_value= " + this.conn_user_value
//				+ "\n   conn_user_value_type= " + this.conn_user_value_type
//				+ "\n   conn_user_ln= "+this.conn_user_ln
//				+ "\n   conn_user_lines= "+ this.conn_user_lines
//				+ "\n   conn_pass_var= " + this.conn_pass_var
//				+ "\n   conn_pass_value= " + this.conn_pass_value
//				+ "\n   conn_pass_value_type= " + this.conn_pass_value_type
//				+ "\n   conn_pass_dt= " + this.conn_pass_dt
//				+ "\n   conn_pass_ln= "+ this.conn_pass_ln
//				+ "\n   conn_pass_lines= "+ this.conn_pass_lines
				+ "\n ----------------\n";
	}
//	@Override
	public int compareTo(Info o) {
		int i = this.getLno();
		Integer j = Integer.valueOf(o.getLno());
		return Integer.compare(i, j);
	}

	ArrayList<Integer> call_lines = new ArrayList<>();
	String stmtVar ="";// Used for stmt variable name or properties variable.
	String arg="", query="";
	int lno = 0;
	ArrayList<Integer> query_lines = new ArrayList<>();
	String query_type;
	int qlno = 0;
//	int qlnoe = 0;
	String key;
	//+++++++++++
//	String pstmt_query="";
	String app_conn="";
	public String getConn_url_var() {
		return conn_url_var;
	}
	public void setConn_url_var(String conn_url_var) {
		this.conn_url_var = conn_url_var;
	}
	public int getConn_url_ln() {
		return conn_url_ln;
	}
	public void setConn_url_ln(int conn_url_ln) {
		this.conn_url_ln = conn_url_ln;
	}
	public ArrayList<Integer> getConn_url_lines() {
		return conn_url_lines;
	}
	public void setConn_url_lines(ArrayList<Integer> conn_url_lines) {
		this.conn_url_lines = conn_url_lines;
	}
	public String getConn_user_var() {
		return conn_user_var;
	}
	public void setConn_user_var(String conn_user_var) {
		this.conn_user_var = conn_user_var;
	}
	public int getConn_user_ln() {
		return conn_user_ln;
	}
	public void setConn_user_ln(int conn_user_ln) {
		this.conn_user_ln = conn_user_ln;
	}
	public ArrayList<Integer> getConn_user_lines() {
		return conn_user_lines;
	}
	public void setConn_user_lines(ArrayList<Integer> conn_user_lines) {
		this.conn_user_lines = conn_user_lines;
	}
	public String getConn_pass_var() {
		return conn_pass_var;
	}
	public void setConn_pass_var(String conn_pass_var) {
		this.conn_pass_var = conn_pass_var;
	}
	public int getConn_pass_ln() {
		return conn_pass_ln;
	}
	public void setConn_pass_ln(int conn_pass_ln) {
		this.conn_pass_ln = conn_pass_ln;
	}
	public ArrayList<Integer> getConn_pass_lines() {
		return conn_pass_lines;
	}
	public void setConn_pass_lines(ArrayList<Integer> conn_pass_lines) {
		this.conn_pass_lines = conn_pass_lines;
	}

	String app_rs="";
//	String app_sql=""; --> query
	String app_ps="";
//	String app_stmt="";
	String method="";
	String app_rs_dt="";
	//===============
	String conn_url_var="";
	String conn_url_value="";
	String conn_url_value_type="";
	int conn_url_ln =0;
	ArrayList<Integer> conn_url_lines = new ArrayList<>();
	//------
	String conn_prop="";
	//------
	String conn_user_var="";
	String conn_user_value="";
	String conn_user_value_type="";
	int conn_user_ln =0;
	ArrayList<Integer> conn_user_lines = new ArrayList<>();
	//------
	String conn_pass_var="";
	String conn_pass_value="";
	String conn_pass_value_type="";
	String conn_pass_dt="";
	int conn_pass_ln =0;
	ArrayList<Integer> conn_pass_lines = new ArrayList<>();
	//------
	//	IF size = 1 -> getConnection(String url)
	//		IF size = 2 -> getConnection(String url, Properties info)
	//		IF size = 3 -> getConnection(String url, String user, String password)

}
