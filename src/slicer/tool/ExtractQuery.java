package slicer.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
//import com.github.javaparser.ast.
import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import flocalization.G;
//import dcafixer.AssignExpInfo;
//import com.github.javaparser.ast.body.VariableDeclarator;
import net.sf.jsqlparser.JSQLParserException;
import slicer.tool.ExtractQuery.AssignExpInfo;
import slicer.tool.ExtractQuery.ParaInfo;
import slicer.utilities.StrUtil;

//
public class ExtractQuery {
	public static boolean Print = false;
	static class ParaInfo {
		@Override
		public String toString() {
			return  ", " +"dt=" + dt + ", pn=" + pn + ", plno=" + plno ;
		}
		String dt ="";
		String pn = "";
		int plno = 0;
	}
	static class MethodInfo {
		@Override
		public String toString() {
			String str = "Method name: " + name;
			if(parameters.size()>0) {
				str += "\n\tparameters " + parameters + "\n";
			} else {
				str += "\n";
			}
			return str ;
		}
		String name="";
		ArrayList<ParaInfo> parameters = new ArrayList<>();
	}

	public static boolean in_assignList(String arg) {
		for (AssignExpInfo i : assignList) {
			if(i.varName.equals(arg)) {
				return true;
			}
		}
		return false;

	}
	public static boolean is_methodCall(String arg) {
		for (MethodInfo i : methodsList) {
			if(i.name.equals(arg)) {
				return true;
			}
		}
		return false;

	}
	public static boolean is_methodArg(String arg) {
		for (MethodInfo i : methodsList) {
			for(ParaInfo p :i.parameters ) {
				if(p.pn.equals(arg)) {
					return true;
				}
			}
		}
		return false;

	}
	public static boolean in_assignListCalls(String arg) {
		for (AssignExpInfo i : assignListCalls) {

		}
		return false;
	}

	//propertiesVarList
	public static boolean in_propertiesVarList(String arg) {
		for (AssignExpInfo i : propertiesVarList) {
			if(arg.equals(i.varName)) { // test --> if(arg.contains(i.varName)) {
				return true;
			}
		}
		return false;
	}
//	public static class Info {
//
//		@Override
//		public String toString() {
//			return "Info"
////					+ ", call start @ lno = " + this.lno
////					+ "\n   called Method = " + this.key
//					+ "\n   " +this.key +" @ " + this.method + " @ " + this.lno
//					+ "\n   call_lines = " + this.call_lines
//					+ "\n   stmtVar = " + this.stmtVar  //+ ", app_stmt=" + this.app_stmt // I think I should remove one of them
//					+ "\n   arg = " + this.arg
//					+ "\n   query_lines = " + this.query_lines +", query starts @ qlno = " + qlno
//					+ "\n   query = " + this.query
//					+ "\n   app_conn = "+ this.app_conn
//					+ "\n   app_rs = " + this.app_rs
//					+ "\n   app_ps = " + this.app_ps
//					+ "\n ----------------\n";
//		}
//		ArrayList<Integer> call_lines = new ArrayList<>();
//		String stmtVar ="";
//		String arg="", query="";
//		int lno = 0;
//		ArrayList<Integer> query_lines = new ArrayList<>();
//		int qlno = 0;
////		int qlnoe = 0;
//		String key;
//		//+++++++++++
////		String pstmt_query="";
//		String app_conn="";
//		String app_rs="";
////		String app_sql=""; --> query
//		String app_ps="";
////		String app_stmt="";
//		String method="";
//
//	}

	static class AssignExpInfo {

@Override
		public String toString() {
			return "AssignExpInfo [varName=" + varName + ", value=" + value + ", query_lines=" + value_lines + ", lnos="
					+ lnos + ", dataType=" + dataType + ", conn=" + conn + ", method=" + method + ",\n\tvalueType="+valueType+"]\n";
		}
		//		ArrayList<Integer> lines = new ArrayList<>();
//		String rhs, lhs_query;
		String varName="";
		String value="";
		String valueType="";
		ArrayList<Integer> value_lines = new ArrayList<>();
		int lnos = 0;
//		int lnoe = 0;
		String dataType="";
		String conn="";
		String method="";
		//+++++++++++
//		String pstmt_query="";
//		String app_conn="";
//		String app_rs="";
//		String app_sql="";
//		String app_ps="";
//		String app_stmt="";

	}


	public static List<Info> infoSet = new ArrayList<>();
	public static List<Info> infoSet2 = new ArrayList<>();
	public static List<AssignExpInfo> assignList = new ArrayList<>();
	public static List<AssignExpInfo> assignListCalls = new ArrayList<>();
	public static List<AssignExpInfo> propertiesVarList = new ArrayList<>();//++
	public static List<Info> infoSet_properties = new ArrayList<>();
	public static List<MethodInfo> methodsList = new ArrayList<>();
	public static String pstmt_query;
	public static String app_conn;//*
	public static String app_rs;//*
	public static String app_sql;//*
	public static String app_ps;//*
	public static String app_stmt;//
	public static List<String> srcCode= new ArrayList<>();

	public static void findAssignExpr(File file) {
//      System.out.println("fff "+file.getName());
//		Print = true;
		if (Print) {
			System.out.println("*** AssignExpr ");
		}
		try {
			new VoidVisitorAdapter<>() {
				@Override
				// VariableDeclarator
				public void visit(AssignExpr n, Object arg) {
					super.visit(n, arg);
					//if(Print)System.out.println("XXXXX "+ n.getChildNodes().toString());
					String rhs = n.getChildNodes().get(1).toString();
					if(rhs.contains("executeQuery") || rhs.contains("executeUpdate")
							||rhs.contains("execute(") ) {
						app_rs = n.getChildNodes().get(0).toString();
						if(Print) {
							System.out.println("RHS: " + rhs);
							System.out.println("app_rs: " + app_rs);
						}
					}

					if (rhs.contains("prepareStatement")) {
						app_ps = n.getChildNodes().get(0).toString();
						if(Print) {
							System.out.println("RHS: " + rhs);
							System.out.println("app_ps: " + app_ps);
						}
					}

					if(rhs.contains("createStatement") || rhs.contains("prepareStatement") )
					{
						app_conn = n.getChildNodes().get(1).getChildNodes().get(0).toString();

						if(Print) {
							System.out.println(n.getChildNodes().get(1).getChildNodes().toString());
							System.out.println("RHS: " + rhs);//>>>>
							System.out.println("app_conn: " + app_conn);}
					}


					if(rhs.contains("getConnection"))
					{
						app_conn = n.getChildNodes().get(1).getChildNodes().get(0).toString();

						if(Print) {
							System.out.println(n.getChildNodes().get(1).getChildNodes().toString());
							System.out.println("RHS: " + rhs);//>>>>
							System.out.println("app_conn: " + app_conn);}
					}
//						System.out.println("var name" + n.getChildNodes().toString());

				}
			}.visit(JavaParser.parse(file), null);
			// System.out.println(); // empty line
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	public static void findMethodsArgs(File file) {
		try {
			new VoidVisitorAdapter<>() {
				@Override
				public void visit(MethodDeclaration n, Object arg) {
					super.visit(n, arg);
					MethodInfo m = new MethodInfo();
//					System.out.println(n.getDeclarationAsString());
//					System.out.println("name: "+n.getNameAsString());
					m.name = n.getNameAsString();
					//System.out.println(n.getAllContainedComments().toString());
					for (int i =0; i<n.getParameters().size();i++) {
//						System.out.println("parameter type ("+i+"): "+ n.getParameter(i).getType().toString());
//						System.out.println("parameter name ("+i+"): "+ n.getParameter(i).getNameAsString());

						ParaInfo pi = new ParaInfo();
						pi.pn = n.getParameter(i).getNameAsString();
						pi.dt = n.getParameter(i).getType().toString();
						pi.plno = n.getParameter(i).getBegin().get().line;
						m.parameters.add(pi);
					}
					methodsList.add(m);
				}
			}.visit(JavaParser.parse(file), null);
			// System.out.println(); // empty line
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (Print) {
			System.out.print(methodsList.toString());
		}
	}


	public static boolean is_constant(String varType, String varValue) {
	if(varType.equals(G.constantValue) ) {
		return true;
	}
        return !varValue.equals("") && varValue.startsWith("\"") && varValue.endsWith("\"") && StrUtil.countOccurrencesOf(varValue, '"') == 2;
    }


	public static AssignExpInfo getVarClosestValue_or_para ( String varName , int stmtloc , String method) {
		AssignExpInfo closeset = new AssignExpInfo();
//		AssignExpInfo closeset = null;
		for (AssignExpInfo v : assignList) {
			//TODO: Try to use Method name, it didn't work because of VariableDeclarator
			if(closeset!= null) {
				if (v.lnos < stmtloc && v.lnos > closeset.lnos && v.varName.equals(varName)) {// && v.method.equals(method)) {
//				closeset = new AssignExpInfo();
					closeset = v;
				}
			}
		}
		if(closeset!= null && closeset.lnos>0) {
		//if(closeset.lnos>0) {
			if(closeset.valueType.equals("NameExpr")) {
				System.out.println("@NameExpr");
				AssignExpInfo closeset_tmp = new AssignExpInfo();
				closeset_tmp = getVarClosestValue_or_para(closeset.value, closeset.lnos, closeset.method);
				if(closeset_tmp!= null && closeset_tmp.lnos>0) {
					System.out.println("@NameExpr - closeset_tmp");
					return closeset_tmp;
				}

			}

			return closeset;
		}else {
			// TODO: Try to get the closest method arg
			//huna1000
			System.out.println("c param");
			ParaInfo param = var_isMethodParam(closeset.method, closeset.value);
			if (param != null) {
				System.out.println("@NameExpr - param value");
				// queryVar_isMethodParam
				// param.dt;
				closeset.value = "";
				closeset.valueType = G.methodArg;
				closeset.lnos = param.plno;
				closeset.dataType = param.dt;

//				info.conn_url_value = "";
//				info.conn_url_value_type = G.methodArg;
//				info.conn_url_ln = param.plno;
//				info.conn_url_lines = null;
			}
			return closeset;
		}
//		else
//			return null;
	}
	public static AssignExpInfo getVarClosestValue ( String varName , int stmtloc , String method) {
		AssignExpInfo closeset = new AssignExpInfo();
//		AssignExpInfo closeset = null;
		for (AssignExpInfo v : assignList) {
			//TODO: Try to use Method name, it didn't work because of VariableDeclarator
			if(closeset!= null) {
				if (v.lnos < stmtloc && v.lnos > closeset.lnos && v.varName.equals(varName)) {// && v.method.equals(method)) {
//				closeset = new AssignExpInfo();
					closeset = v;
				}
			}
		}
		if(closeset.lnos>0) {
			return closeset;
		} else {
			return null;
		}
	}

	public static AssignExpInfo getClosestPropertiesValue ( String varName , int stmtloc , String method) {
		AssignExpInfo closeset = new AssignExpInfo();
		for (AssignExpInfo v : propertiesVarList) {
			//TODO: Try to use Method name, it didn't work because of VariableDeclarator
			if (v.lnos < stmtloc && v.lnos > closeset.lnos && v.varName.equals(varName)) {// && v.method.equals(method)) {
				closeset = v;
			}
		}
		return closeset;
	}


	public static Info getClosestPropertiesInfo ( String varName , int stmtloc , String method) {
		Info closeset = new Info();
		for (Info v : infoSet_properties) {
			//TODO: Try to use Method name, it didn't work because of VariableDeclarator
			if (v.lno < stmtloc && v.lno > closeset.lno && v.conn_prop.equals(varName)) {// && v.method.equals(method)) {
				closeset = v;
			}
		}
		return closeset;
	}

	public static boolean isClosestPropertiesObject ( String varName , int stmtloc , Info info) {
		Info closeset = new Info();
		for (Info v : infoSet_properties) {
			//TODO: Try to use Method name, it didn't work because of VariableDeclarator
			if (v.lno < stmtloc && v.lno > closeset.lno && v.conn_prop.equals(varName)) {// && v.method.equals(method)) {
				closeset = v;
			}
		}
        return info.lno == closeset.lno && info.conn_prop.equals(closeset.conn_prop);
	}

	public static AssignExpInfo getStmtDetils (  int stmtloc) {
//		AssignExpInfo closeset = assignList.get(0);
		for (AssignExpInfo ae :assignListCalls ) {
			if(ae.lnos == stmtloc) {
				return ae;
			}

		}
		return null;

	}

	public static AssignExpInfo getPropDetils (  int stmtloc) {
//		AssignExpInfo closeset = assignList.get(0);
		for (AssignExpInfo ae :propertiesVarList ) {
			if(ae.lnos == stmtloc) {
				return ae;
			}

		}
		return null;

	}
	public static void findVarDeclarator_and_AssignExps(File file) {
//      System.out.println("fff "+file.getName());
//		Print =true;
		if (Print) {
			System.out.println("*** Find VariableDeclarator ");
		}
		try {
			new VoidVisitorAdapter<>() {
				@Override

				public void visit(VariableDeclarator n, Object arg) {
					super.visit(n, arg);
					Node parent  = n.getParentNode().get();
					AssignExpInfo VarDec = new AssignExpInfo();
					if (Print) {
						System.out.println("$$$ new --- "+ "@"+n.getBegin().get().line +  n.getChildNodes().toString() );
					}
					if(n.getChildNodes().size() == 2) {

						VarDec.dataType = n.getChildNodes().get(0).toString();
						VarDec.varName = n.getChildNodes().get(1).toString();
						assignList.add(VarDec);
					}

					if(n.getChildNodes().size() == 3) {

						VarDec.dataType = n.getChildNodes().get(0).toString();
						VarDec.varName = n.getChildNodes().get(1).toString();
						VarDec.value =  n.getChildNodes().get(2).toString();
						//getMetaModel().toString() & .getMetaModel().getTypeName() & .getMetaModel().getTypeNameGenerified all give the same results
						VarDec.valueType = n.getChildNodes().get(2).getMetaModel().getTypeName();
						VarDec.lnos = n.getChildNodes().get(2).getBegin().get().line;
						if (Print) {
							System.out.println("Here 01: " + VarDec);
						}

						for (int i = n.getChildNodes().get(2).getBegin().get().line; i <= n.getChildNodes().get(2).getEnd()
								.get().line; i++) {
							VarDec.value_lines.add(i);
						}
						//+++++++++++++++
						if(VarDec.value.contains("createStatement") &&
								!(parent.toString().contains("executeQuery") || parent.toString().contains("execute")|| parent.toString().contains("executeUpdate")) ) {
							VarDec.conn= n.getChildNodes().get(2).getChildNodes().get(0).toString();
						}
						//VarDec.conn= n.getChildNodes().get(2).getChildNodes().get(0).getChildNodes().get(0).toString();
						//+++++++++++++++
						if(VarDec.value.contains("prepareStatement") || VarDec.value.contains("getConnection") ) { //|| VarDec.value.contains("createStatement") )
							VarDec.conn= n.getChildNodes().get(2).getChildNodes().get(0).toString();
						}

						if(VarDec.value.contains("executeQuery") || VarDec.value.contains("executeUpdate")
								||VarDec.value.contains("execute(") ||VarDec.value.contains("createStatement")||VarDec.value.contains("prepareStatement") ) {
							assignListCalls.add(VarDec);
						}
						//+++++++++++++++S
						else if(VarDec.value.contains("Properties(") || VarDec.dataType.equals("Properties")) {
							//ppppp
							propertiesVarList.add(VarDec);
							Info info = new Info();
							info.key = "Properties";
							info.conn_prop = VarDec.varName;
							info.lno = VarDec.lnos;
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
								info.call_lines.add(i);
							}
							infoSet_properties.add(info);

						}
						//+++++++++++++++E
						else {
							assignList.add(VarDec);
						}

					}
//					System.out.println("getChildNodes().get(2): "+VarDec.value
//							+"\ngetInitializer().get()"+n.getInitializer().get().toString());

//					VarDec.method = n.findParent(MethodDeclaration.class).get().getName().toString();

//					for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
//						VarDec.query_lines.add(i);
//					VarDec.lnoe = n.getEnd().get().line;

					//----------------------------------


				}
			}.visit(JavaParser.parse(file), null);
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			new VoidVisitorAdapter<>() {
				@Override

				public void visit(AssignExpr n, Object arg) {
					super.visit(n, arg);
					if (Print) {
						System.out.println("$$$ 	 --- " + "@"+n.getBegin().get().line+ n.getChildNodes().toString() );
					}
					AssignExpInfo VarDec = new AssignExpInfo();
					VarDec.dataType = "";//n.getChildNodes().get(0).toString();
					VarDec.varName = n.getChildNodes().get(0).toString();
					VarDec.value =  n.getChildNodes().get(1).toString();
					VarDec.valueType = n.getChildNodes().get(1).getMetaModel().getTypeName();

					VarDec.lnos = n.getBegin().get().line;
					//VarDec.method = n.findParent(MethodDeclaration.class).get().getName().toString();
					if (Print) {
						System.out.println("Here 1: " + VarDec);
					}

					for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
						VarDec.value_lines.add(i);
					}
					if (Print) {
						System.out.println("Here 2");
					}

//					if(VarDec.value.contains("createStatement")  || VarDec.value.contains("prepareStatement") )
//					{
//						VarDec.conn= n.getChildNodes().get(1).getChildNodes().get(0).toString();
//						//app_conn
////						if(Print)
//						{
//							System.out.println(n.getChildNodes().get(1).getChildNodes().toString());
//							System.out.println("RHS: " + VarDec.value);
//							System.out.println("app conn: " + VarDec.conn);
//						}
//					}
					if(VarDec.value.contains("executeQuery") || VarDec.value.contains("executeUpdate")
							||VarDec.value.contains("execute(") ||VarDec.value.contains("createStatement")||VarDec.value.contains("prepareStatement") ||VarDec.value.contains("getConnection")) {
						assignListCalls.add(VarDec);
					}
					//+++++++++++++++S
					else if(VarDec.value.contains("Properties()")) {// || VarDec.dataType.equals("Properties")) {
						//ppppp
						propertiesVarList.add(VarDec);
						Info info = new Info();
						info.key = "Properties";
						info.conn_prop = VarDec.varName;
						info.lno = VarDec.lnos;
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							info.call_lines.add(i);
						}
						infoSet_properties.add(info);

					}
					//+++++++++++++++E


					else {
						assignList.add(VarDec);
					}
					if (Print) {
						System.out.println("Here 3");
					}




				}
			}.visit(JavaParser.parse(file), null);
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//============
//		infoSet.forEach(info -> {
//			//================== getConnection vars
//			AssignExpInfo conarg1 = new AssignExpInfo();
//			//if url is var, get the closest value
//			if (!info.conn_url_var.equals("") && info.conn_url_value.equals("")) {
//				conarg1 = getQueryStr(info.conn_url_var, info.lno, info.method);
//				// Extract information
//				info.conn_url_value = conarg1.value;
//				info.qlno = conarg1.lnos;
//				info.query_lines = conarg1.value_lines;
//			}
//
//			AssignExpInfo conarg2 = new AssignExpInfo();
//			//if username is var, get the closest value
//			if (!info.conn_user_var.equals("") && info.conn_user_value.equals("")) {
//				conarg2 = getQueryStr(info.conn_user_var, info.lno, info.method);
//				// Extract information
//				info.conn_user_value = conarg2.value;
//				info.qlno = conarg2.lnos;
//				info.query_lines = conarg2.value_lines;
//			}
//
//
//			AssignExpInfo conarg3 = new AssignExpInfo();
//			//if password is var, get the closest value
//			if (!info.conn_pass_var.equals("") && info.conn_pass_value.equals("")) {
//				conarg3 = getQueryStr(info.conn_pass_var, info.lno, info.method);
//				// Extract information
//				info.conn_pass_value = conarg3.value;
//				info.conn_pass_dt = conarg3.dataType;
//				info.qlno = conarg3.lnos;
//				info.query_lines = conarg3.value_lines;
//			}
//			//===================================
//
////			System.out.println("KEY @ " + info.key);
//			AssignExpInfo ae = new AssignExpInfo();
//			//if query is var, get the closest value
//			if (!info.arg.equals("") && info.query.equals("")) {
//				ae = getQueryStr(info.arg, info.lno, info.method);
//				// Extract information
//				info.query = ae.value;
//				info.qlno = ae.lnos;
//				info.query_lines = ae.value_lines;
////				info.qlnoe = ae.lnoe;
//			}
//			AssignExpInfo ae2 = getStmtDetils(info.lno);
//			if(ae2 != null) {
//				String rhs = ae2.value;
//				if (rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) {// && !rhs.contains("createStatement")) {
//					info.app_rs = ae2.varName;
//					info.app_rs_dt = ae2.dataType;
////					if (Print) {
////						System.out.println("RHS: " + rhs);// >>>>
////						System.out.println("app_rs: " + info.app_rs);
////					}
//				}
////				if ((rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) && !rhs.contains("createStatement")) {
////
////				}
////				System.out.println("PSSSSS1 @ " + ae2.value);
//				if (rhs.contains("prepareStatement")) {
//					info.app_ps = ae2.varName;// n.getChildNodes().get(1).toString();
//					info.app_conn = ae2.conn;
//
////					if (Print) {
////						System.out.println("RHS: " + rhs);
////						System.out.println("app_ps: " + app_ps);
////					}
//				}
//
//				if(rhs.contains("createStatement")
//						&& !(rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) ) {
//
//					info.app_conn = ae2.conn;
//					info.stmtVar = ae2.varName;
//				}
//
//				if(rhs.contains("createStatement") && (rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) ) {
////					info.app_conn = ae2.conn;
//					info.app_rs = ae2.varName;
//				}
//			}
//
//				if(info.query.equals("")) {
//					//Check if it is a method parameter
//					int paralno = queryVar_isMethodParam(info.method, info.arg);
//					if(paralno > 0) {
//						info.query = "parameter";
//						info.query_lines.add(paralno);
//					}
//				}
//
//		});

	}



	public static void findVarDeclarator(File file) {
//      System.out.println("fff "+file.getName());
//		Print =true;
		if (Print) {
			System.out.println("*** Find VariableDeclarator ");
		}
		try {
			new VoidVisitorAdapter<>() {
				@Override

				public void visit(VariableDeclarator n, Object arg) {
					super.visit(n, arg);
					if (Print) {
						System.out.println("$$$ --- "+ n.getChildNodes().toString());
					}
					String rhs = n.getChildNodes().get(2).toString();
					//System.out.println("rhs: " + rhs);
					if(rhs.contains("executeQuery") || rhs.contains("executeUpdate")
							||rhs.contains("execute(") ) {
						app_rs = n.getChildNodes().get(1).toString();
						if(Print) {
							System.out.println("RHS: " + rhs);//>>>>
							System.out.println("app_rs: " + app_rs);
						}
					}

					if (rhs.contains("prepareStatement")) {
						app_ps = n.getChildNodes().get(1).toString();
						if(Print) {
							System.out.println("RHS: " + rhs);
							System.out.println("app_ps: " + app_ps);
						}
					}
					if(rhs.contains("createStatement")  || rhs.contains("prepareStatement") )
					{
						app_conn = n.getChildNodes().get(2).getChildNodes().get(0).toString();

						if(Print) {
							System.out.println(n.getChildNodes().get(2).getChildNodes().toString());
							System.out.println("RHS: " + rhs);
							System.out.println("app_conn: " + app_conn);
						}
					}
//					if(n.isAssignExpr())
//						n.getAncestorOfType(AssignExpr)

				}
			}.visit(JavaParser.parse(file), null);
			// System.out.println(); // empty line
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//********************************************
	public static void FindExecute(File file) {
//      System.out.println("fff "+file.getName());
		if (Print) {
			System.out.println("*** FindExecute ");
		}
		try {
			new VoidVisitorAdapter<>() {
				@Override
				public void visit(MethodCallExpr n, Object arg) {
					super.visit(n, arg);
					Node parent = n.getParentNode().get();
					if (Print) {
						System.out.println(n.getNameAsString() + ", isEmpty? "+n.getArguments().isEmpty());
					}
//					if (Print)System.out.println("\t" + n.getChildNodes().toString());
//					if (n.getArguments().isNonEmpty() &&
					//========================================= Key is "getConnection"
					if (n.getArguments().isNonEmpty() && n.getNameAsString().equals("getConnection")) {
						Info info = new Info();
						java.util.Optional<MethodDeclaration> method;
						try {
							method = n.findParent(MethodDeclaration.class);
							info.method = method.get().getNameAsString();
						}catch(RuntimeException e) {
							System.out.println("JavaParser Couldn't Extract method name");
						}
						info.key = "getConnection";
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							info.call_lines.add(i);
							// Check where exactly getConnection call is located!
							int loc = i-1;
							if(srcCode.get(loc).contains(n.getNameAsString())) {
								info.lno = i;//++++ for experiment 2
								if(Print ) {
									System.out.println("SRCLINE: srcCode[" + loc +"] line "+info.lno+" @ "+srcCode.get(loc));
								}

							}
						}


						//1- Extract from getConnection (Check children size )
						if(n.getArguments().size()>0) {
//							IF size = 1 -> getConnection(String url)


							String   arg1 = n.getArguments().get(0).toString();
							String   arg1_type = n.getArguments().get(0).getMetaModel().toString();

							if(arg1_type.equals(G.constantValue) || arg1_type.equals("BinaryExpr") ){
								//arg1_type.equals("BinaryExpr"): To handle the case when url is not constant and not only a var
								//Store url value
								info.conn_url_value_type = arg1_type;
								info.conn_url_value = arg1;

								if(arg1_type.equals("BinaryExpr") && n.getArguments().size()==1) {
//									//TODO: try to loop over the vars in the URL if all are constant , then  PW & UN are HardCoded!
									// if child is "NameExpr", find its value and check if it's constant or not!

								}
							}else {
								//Store url var
								if(arg1.startsWith("this.")) {
									arg1 = arg1.replace("this.", "");
								}
								info.conn_url_var = arg1;
							}
//						if (n.getArguments().get(0).getMetaModel().toString().equals("NameExpr")) {
//							info.conn_url_var = n.getArguments().get(0).toString();
//
//						}

						if(n.getArguments().size()==2) {
//							IF size = 2 -> getConnection(String url, Properties info)
							info.conn_prop = n.getArguments().get(1).toString();
//							if (n.getArguments().get(1).getMetaModel().toString().equals("NameExpr")) {
//								info.conn_prop = n.getArguments().get(1).toString();
//
//							}

						}

						if(n.getArguments().size()==3) {
//							IF size = 3 -> getConnection(String url, String user, String password)
							String   arg2 = n.getArguments().get(1).toString();
							String   arg2_type = n.getArguments().get(1).getMetaModel().toString();

							if(arg2_type.equals(G.constantValue) ){
								//Store username value
								info.conn_user_value_type = arg2_type;
								info.conn_user_value = arg2;
							}else {
								//Store username var
								if(arg2.startsWith("this.")) {
									arg2 = arg2.replace("this.", "");
								}
								info.conn_user_var = arg2;
							}


//							if (n.getArguments().get(1).getMetaModel().toString().equals("NameExpr")) {
//								info.conn_user_var = n.getArguments().get(1).toString();
//
//							}else {
//								//check correction
//								info.conn_user_value = n.getArguments().get(1).toString();
//							}


							String   arg3 = n.getArguments().get(2).toString();
							String   arg3_type = n.getArguments().get(2).getMetaModel().toString();

							if(arg3_type.equals(G.constantValue) ){
								//Store pass value
								info.conn_pass_value_type = arg3_type;
								info.conn_pass_value = arg3;
							}else {
								//Store username var
								if(arg3.startsWith("this.")) {
									arg3 = arg3.replace("this.", "");
								}
								info.conn_pass_var = arg3;
							}


//							if (n.getArguments().get(2).getMetaModel().toString().equals("NameExpr")) {
//								info.conn_pass_var = n.getArguments().get(2).toString();
//
//							}else {
//								//check correction
//								info.conn_pass_value = n.getArguments().get(2).toString();
//							}


						}
						}

						//2- Get connection name, properties
						//3- Check different cases:
						//		Connection x = ...getConnection
						//		...getConnection.executeCall??!!

						if(info.lno == 0)
						 {
							info.lno = n.getBegin().get().line;//++++ for experiment 2
						}
						infoSet.add(info);
 					}
					//========================================= Key is one of the Execute Calls, 2 cases (arg , and no arg)
					// ----- Case 1: Execute calls with parameters! No PS
					if (n.getArguments().isNonEmpty() && (n.getNameAsString().equals("prepareStatement")
							|| n.getNameAsString().equals("executeUpdate") || n.getNameAsString().equals("executeQuery")
							|| n.getNameAsString().equals("execute"))) {
						Info info = new Info();
//						java.util.Optional<MethodDeclaration> op = n.findParent(MethodDeclaration.class);
//						System.out.println("OP ------> " + op.get().getName().toString());
						// info.method = n.findParent(MethodDeclaration.class).get().getName().toString();
//						info.method =
//						Optional<MethodDeclaration> caller = n.findParent(MethodDeclaration.class);
						//+++++++++++++++
//						for (Node i : n.getChildNodes()) {
//							System.out.println("CCCCE  childrens: "+ i.toString());
//						}
//						System.out.println("PPPPE  parent: "+ n.getParentNode().get().toString());
//

//						System.out.println("PPPPE  parent children: ");
//						int k =0;
						// To Handle the case when createStatement and the execution function are called togather
						//	i.e., con.createStatement().executeQuery(...)
//						Node parent = n.getParentNode().get();
						for (Node i : parent.getChildNodes()) {
//							System.out.println("child (" +k++ +") :" + i.toString());
							if(i.toString().contains(n.getNameAsString()) && i.toString().contains("createStatement")) {
								if(Print) {
									System.out.println("Conn name: " +i.getChildNodes().get(0).getChildNodes().get(0));
								}
								info.app_conn = i.getChildNodes().get(0).getChildNodes().get(0).toString();
//								System.out.println("$$$$$ "+info.app_conn);
							}
						}
						//+++++++++++++++
						java.util.Optional<MethodDeclaration> method;
						try {
							method = n.findParent(MethodDeclaration.class);
							info.method = method.get().getNameAsString();
						}catch(RuntimeException e) {
							System.out.println("JavaParser Couldn't Extract method name");
						}
						//+++++++++++++++
//						info.method = n.findParent(MethodDeclaration.class).get().getName().toString();

						if (Print) {
							System.out.println("** 1 " + n.getNameAsString());
						}

						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							info.call_lines.add(i);
							// Check where exactly execute call is located!
							int loc = i-1;
							if(srcCode.get(loc).contains(n.getNameAsString())) {
								info.lno = i;//++++ for experiment 2
								if(Print ) {
									System.out.println("SRCLINE: srcCode[" + loc +"] line "+info.lno+" @ "+srcCode.get(loc));
								}

							}
						}
						if(info.lno == 0)
						 {
							info.lno = n.getBegin().get().line;//++++ for experiment 2
						}
						if (n.getArguments().get(0).getMetaModel().toString().equals("NameExpr")) {
							info.arg = n.getArguments().get(0).toString();
							app_sql = info.arg;
						} else {
							info.query = n.getArguments().get(0).toString();// +"@FindExecute";
							info.query_type = n.getArguments().get(0).getMetaModel().toString();
							info.qlno = n.getArguments().get(0).getBegin().get().line;
							for (int i = n.getArguments().get(0).getBegin().get().line; i <= n.getArguments().get(0).getEnd().get().line; i++) {
								info.query_lines.add(i);
							}

//							info.qlnoe =n.getArguments().get(0).getEnd().get().line;
							info.arg = "";
							app_sql = info.arg;
						}
//						info.stmtVar = n.getChildNodes().get(0).toString();
//						app_stmt = info.stmtVar;
						info.key = n.getNameAsString();



						if( n.getNameAsString().contains("prepareStatement")){
							info.app_conn = n.getChildNodes().get(0).toString();
						}
						//============= 3.13
						if(G.ExIPList.contains(n.getNameAsString())) {
							info.stmtVar = n.getChildNodes().get(0).toString();
							//System.err.println( "iiiiii info.stmtVar: "+info.stmtVar +", conn: "+info.app_conn);
							if(info.stmtVar.contains(".createStatement(")) {
								info.app_conn = n.getChildNodes().get(0).getChildNodes().get(0).toString();
								//System.err.println( "cccccc conn: "+info.app_conn);
								}

						}//............. 3.13
//						info.method = n.findParent(MethodDeclaration.class).get().getName().toString();
						infoSet.add(info);
						if (Print) {
							System.out.println(info);
						}

					}
					// ---- Case 2: Execute calls with no parameters - PS!
					if (n.getArguments().isEmpty() &&
							( n.getNameAsString().equals("executeUpdate") || n.getNameAsString().equals("executeQuery")
							|| n.getNameAsString().equals("execute") )) {
						if (Print) {
							System.out.println("######### "+n.getNameAsString() + ", isEmpty? "+n.getArguments().isEmpty());
						}
						Info info = new Info();
						info.key = n.getNameAsString();
						info.lno = n.getBegin().get().line;
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							info.call_lines.add(i);
						}
						info.query ="ps";
						info.app_ps = n.getChildNodes().get(0).toString();
//						info.method = n.findParent(MethodDeclaration.class).get().getName().toString();
						infoSet.add(info);
					}

					//++++++++++++

//					for (Node i : parent.getChildNodes()) {
////						System.out.println("child (" +k++ +") :" + i.toString());
//						if(i.toString().contains(n.getNameAsString()) && i.toString().contains("createStatement")) {
////							System.out.println("Conn name: " +i.getChildNodes().get(0).getChildNodes().get(0));
//							info.app_conn = i.getChildNodes().get(0).getChildNodes().get(0).toString();
////							System.out.println("$$$$$ "+info.app_conn);
//						}
//					}
//
//					for (Node i : n.getChildNodes()) {
//						System.out.println("CCCC createStatement childrens: "+ i.toString());
//					}
//					System.out.println("PPPP createStatement parent: "+ n.getParentNode().get().toString());
					//++++++++++++
					// =================== createStatement & No Execute call
					if(n.getNameAsString().equals("createStatement") && !(parent.toString().contains("executeUpdate") ||
							parent.toString().contains("executeQuery") || parent.toString().contains("execute"))) {

						Info info = new Info();
						info.key = "createStatement";
						info.app_conn = n.getChildNodes().get(0).toString();
						info.lno = n.getBegin().get().line;
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							info.call_lines.add(i);
							if (Print) {
								System.out.println( "@ createStatement Branch: "+ info + " 0 : "+n.getChildNodes().get(0).toString() +" 1 : "+ n.getChildNodes().get(1).toString());
							}

						}

//						info.method = n.findParent(MethodDeclaration.class).get().getName().toString();
						infoSet.add(info);
//						if(Print) System.out.println("info.app_conn: " + info.app_conn);
						if (Print) {
							System.out.println("------- lno: " + info.lno + "\nquery: " + info.query + "\nstmt: " + info.stmtVar
									+ "\nstr: " + info.arg+"\napp_conn: " + info.app_conn);
						}
					}

					//========================================= Key is "put" one of Properties calls.
					// +++++++++++++++++ Get Properties values S - huna1
					if (n.getArguments().isNonEmpty() && n.getNameAsString().equals("put")){
						//if() {
//						Info info = new Info();
//						info.key = "Properties";//"Properties";
						String properties_var = n.getChildNodes().get(0).toString();
						int put_ln = n.getBegin().get().line;

						for (Info info: infoSet_properties) {
							if(isClosestPropertiesObject (  properties_var , put_ln , info)) {
								//update the values in info
//								System.out.println("n.getName() : " +n.getName().toString());
//								System.out.println("n.getParentNodeForChildren().getChildNodes().get(0) : "+n.getParentNodeForChildren().getChildNodes().get(0).toString());

//								for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
//									info.call_lines.add(i);
//								}
						//int properties_var_lno = n.getBegin().get().line;
//						if(in_propertiesVarList(properties_var)) {
//
							//TODO: print properties_var, search if properties_var belongs to  propertiesVarList
							if(n.getArguments().size() == 2) {
								//TODO: get un, pw, url from properties
								String arg1 = n.getArguments().get(0).toString().toLowerCase();
								String arg2 = n.getArguments().get(1).toString();
								String arg2_type = n.getArguments().get(1).getMetaModel().toString();
//								String arg2_type_name = n.getArguments().get(1).getMetaModel().getTypeName();
								System.out.println("PPPP Put args: ( "+arg1+" , "+arg2+") ,\n\targ2_type: "+arg2_type);//, arg2_type_name: \"+arg2_type_name
								//if (n.getArguments().get(0).getMetaModel().toString().equals("NameExpr")) {
								// TODO: get values by getting the closest ones
								if (arg1.contains("user") || arg1.contains("username") || arg1.contains("uname")) {
									//add lines to the slice
									for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
										info.call_lines.add(i);
									}
									info.conn_user_ln= n.getBegin().get().line;//n.getArguments().get(1).getBegin().get().line;
//									if(arg2_type.equals("NameExpr") || arg2_type.equals("FieldAccessExpr")) {// value is stored in a var
//										//Store username var
//										if(arg2.startsWith("this."))
//											arg2 = arg2.replace("this.", "");
//										info.conn_user_var = arg2;
//										info.conn_user_value = "";
//									}else {
//										info.conn_user_value = arg2;
//									}

									if(arg2_type.equals(G.constantValue) ){
										//Store username value
										info.conn_user_value_type = arg2_type;
										info.conn_user_value = arg2;
									}else {
										//Store username var
										if(arg2.startsWith("this.")) {
											arg2 = arg2.replace("this.", "");
										}
										info.conn_user_var = arg2;
									}
								}

								if (arg1.contains("password") || arg1.contains("pass") || arg1.contains("pw")) {
									//add lines to the slice
									for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
										info.call_lines.add(i);
									}

									info.conn_pass_ln= n.getBegin().get().line;//n.getArguments().get(1).getBegin().get().line;
//									if(arg2_type.equals("NameExpr")) {// value is stored in a var
//										//Store pass var
//										info.conn_pass_var = arg2;
//										info.conn_pass_value ="";
//										// get value by getting the closest value
//									}else {
//										//Store pass value
//										info.conn_pass_value = arg2;
//									}

									if(arg2_type.equals(G.constantValue)){
										//Store username value
										info.conn_pass_value_type = arg2_type;
										info.conn_pass_value = arg2;
									}else {
										//Store username var
										if(arg2.startsWith("this.")) {
											arg2 = arg2.replace("this.", "");
										}
										info.conn_pass_var = arg2;
//										info.conn_pass_value = "";
									}
								}

								if (arg1.contains("url")) {
									//add lines to the slice
									for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
										info.call_lines.add(i);
									}

									info.conn_url_ln= n.getBegin().get().line;//n.getArguments().get(1).getBegin().get().line;
//									if(arg2_type.equals("NameExpr")) {// value is stored in a var
//										//Store url var
//										info.conn_url_var = arg2;
//										info.conn_url_value = "";
//										// get value by getting the closest value
//									}else {
//										//Store url value
//										info.conn_url_value = arg2;
//									}

									if(arg2_type.equals(G.constantValue)){
										//Store username value
										info.conn_url_value_type = arg2_type;
										info.conn_url_value = arg2;
									}else {
										//Store username var
										if(arg2.startsWith("this.")) {
											arg2 = arg2.replace("this.", "");
										}
										info.conn_url_var = arg2;
//										info.conn_url_value = "";
									}
								}
							}
						//}

//						infoSet.add(info);
							break; //Since we found the corresponding properties, we don't need to go over the rest
							}//end if
						}//End for
					}
					// +++++++++++++++++ Get Properties values E



				}
			}.visit(JavaParser.parse(file), null);
			// System.out.println(); // empty line
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	public static void FindPStmt(File file, String key, int lno) {
//      System.out.println("fff "+file.getName());
		if (Print) {
			System.out.println("*** FindPStmt ");
		}
		try {
			new VoidVisitorAdapter<>() {
				@Override
				public void visit(MethodCallExpr n, Object arg) {
					super.visit(n, arg);
					// System.out.println(n.getNameAsString());
					boolean foundline = false;
					if (n.getArguments().isNonEmpty() && (n.getNameAsString().equals(key))) {
						if (Print) {
							System.out.println("** 2 " + n.getNameAsString());
						}
						Info info = new Info();
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							if (n.getBegin().get().line == lno) {
								if (Print) {
									System.out.println(" lno is found! ");
								}
								foundline = true;
							}
							info.call_lines.add(n.getBegin().get().line);
						}
						if (n.getArguments().get(0).getMetaModel().toString().equals("NameExpr")) {
							info.arg = n.getArguments().get(0).toString();
							app_sql = info.arg;
						} else {
							info.query = "XX4 "+n.getArguments().get(0).toString();
							info.qlno =n.getBegin().get().line;
							for(int i =n.getBegin().get().line; i<= n.getEnd().get().line; i++) {
								info.query_lines.add(i);
							}

//							info.qlnoe =n.getEnd().get().line;
							info.arg = "";
							app_sql = info.arg;
						}
						info.stmtVar = n.getChildNodes().get(0).toString();
						app_stmt = info.stmtVar;
//                      info.method = n.getMetaModel().toString();//.getNameAsString();
						if (Print) {
							System.out.println("lno: " + info.lno + "\nquery: " + info.query + "\nstmt: " + info.stmtVar
									+ "\nstr: " + info.arg);
						}
						if (foundline) {
							infoSet2.add(info);
						}
					}

				}
			}.visit(JavaParser.parse(file), null);
			// System.out.println(); // empty line
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static int findClosestOne(List<Integer> list, int i) {
		int closeset = -1;
		for (int t : list) {
			if (t < i && t > closeset) {
				closeset = t;
			}
		}
		return closeset;
	}
	public static void FindStmtStr(File file) {
		if (Print) {
			System.out.println("*** FindStmtStr ");
		}
		List<Integer> list = new ArrayList<>();
		//Print = true;
		infoSet.forEach(info -> {
			if (Print) {
				System.out.println("** FindStmtStr info: " + info.stmtVar +"\tlno "+info.lno+"\t str"+info.arg);
			}
			try {
				new VoidVisitorAdapter<>() {
					@Override

					public void visit(VariableDeclarator n, Object arg) {
						super.visit(n, arg);
						// if(n.getName().toString().equals(info.stmt))System.out.println(info.stmt);
						if (n.getName().toString().equals(info.stmtVar) && n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print) {
								System.out.println("** 3 " + n.getName().toString() + " " + n.getBegin().get().line);
							}
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
								info.call_lines.add(n.getBegin().get().line);
							}
							list.add(n.getBegin().get().line);
						} else if (n.getInitializer().isPresent() && n.getName().toString().equals(info.arg)
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print) {
								System.out.println("** 4 " + n.getName().toString() + " " + n.getBegin().get().line);
							}
//							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
//								info.lines.add(n.getBegin().get().line);
							list.add(n.getBegin().get().line);
							info.query = n.getInitializer().get().toString();//+"FindStmtStr P1";
							info.qlno =n.getBegin().get().line;
							for(int i =n.getBegin().get().line; i<= n.getEnd().get().line; i++) {
								info.query_lines.add(i);
							}

//							info.qlnoe =n.getEnd().get().line;
							if (Print)
							 {
								System.out.println("** 5 " + info);
//								System.out.println("** 5 " + info.query +"\nqlno@ "+info.qlno + "\nqlnoE @ "+info.qlnoe);
							}
						}
					}
				}.visit(JavaParser.parse(file), null);
			} catch (RuntimeException e) {
				new RuntimeException(e);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			try {
				new VoidVisitorAdapter<>() {
					@Override

					public void visit(AssignExpr n, Object arg) {
						super.visit(n, arg);
//						 System.out.println("@@@@@@ info.lines.get(0): "+info.lines.get(0)+"\n"+n);
						if ((n.getTarget().toString().equals(info.stmtVar) || n.getTarget().toString().equals(info.arg))
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							System.out.println("@@@@@@ info.lines.get(0): "+info.call_lines.get(0));
							System.out.println("*** " + n.getTarget() + " " + n.getBegin().get().line);
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
								info.call_lines.add(n.getBegin().get().line);
							}
							list.add(n.getBegin().get().line);
							if (n.getTarget().toString().equals(info.arg)
									&& n.getBegin().get().line == findClosestOne(list, info.call_lines.get(0))
									//&& ++ check that line number is close to the stmt
									) {
								info.query = n.getValue().toString();//+"FindStmtStr P2";
								info.query_type = n.getValue().getMetaModel().toString();
								info.qlno =n.getBegin().get().line;
								for(int i =n.getBegin().get().line; i<= n.getEnd().get().line; i++) {
									info.query_lines.add(i);
								}

//								info.qlnoe =n.getEnd().get().line;
								if (Print)
								 {
									System.out.println("** 6 " + info);
//									System.out.println("** 6 " + info.query +"\nqlno@ "+info.qlno + "\nqlnoE @ "+info.qlnoe+"\nlist:"+list.toString());
								}

							}
						}

					}
				}.visit(JavaParser.parse(file), null);
			} catch (RuntimeException e) {
				new RuntimeException(e);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// System.out.println("QQQQQQQQQ "+info.query);
		});
	}

	public static String exp1Extractor(String basePath, String fileName, int n) {
		if (Print) {
			System.out.println("*** exp1Extractor ");
		}
		if (Print) {
			System.out.println("Extract from file: " + basePath + "/" + fileName);
		}
//        File file = new File(basePath+"/"+fileName);
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory()) {
			return null;
		}

		///// ***** Parse file by selecting line numbers ******////
		clear_all();//+++
		FindExecute(file);
		FindStmtStr(file);
		findAssignExpr(file);
		findVarDeclarator(file);
//		if (infoSet.size() > 0) {
//			System.out.println(fileName + " : " + infoSet.size());
//		}
		// create a sub-folder for each class and write the extracted queries to files
//		for (Info info : infoSet) {
//
//			if (info.query == null)
//				continue;
//			try {
//
//				File f2 = new File(basePath + "/Query" + n + ".txt");
//
//				if (!f2.exists())
//					f2.getParentFile().mkdir();
//				FileWriter myWriter = new FileWriter(f2);
//				myWriter.write(info.query);
//				myWriter.close();
//				return f2.getAbsolutePath();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		return null;
	}

	public static String queryVar_isMethodParam (String method, String sqlVar) {
		String dt = null;
		if(Print) {
			System.out.println("In queryVar_isMethodParam :");
		}

		for (MethodInfo mi : methodsList) {
			if(Print) {
				System.out.println("method: "+mi.toString() + ", sql var:" + sqlVar);
			}
			if(mi.name.equals(method)) {
				for (ParaInfo pi : mi.parameters) {
					if(Print) {
						System.out.println(pi.toString());
					}
					if(pi.pn.equals(sqlVar)) {
						//if(Print)System.out.println("I'm HERE!!!");
						return pi.dt;
					}
				}
			}

		}
		return dt;
	}

	//TODO: change the function below to return object similar to AssignExpInfo
	public static ParaInfo var_isMethodParam (String method, String sqlVar) {
		ParaInfo pinfo = null;
		if(Print) {
			System.out.println("In Var_isMethodParam :");
		}

		for (MethodInfo mi : methodsList) {
			if(Print) {
				System.out.println("method: "+mi.name + ", sql var:" + sqlVar);
			}
			if(mi.name.equals(method)) {
				for (ParaInfo pi : mi.parameters) {
					if(Print) {
						System.out.println(pi.toString());
					}
					if(pi.pn.equals(sqlVar)) {
						//if(Print)System.out.println("I'm HERE!!!");
						return pi;
					}
				}
			}

		}
		return pinfo;
	}
	public static int queryVar_isMethodParam_BU (String method, String sqlVar) {
		if(Print) {
			System.out.println("In queryVar_isMethodParam :");
		}

		for (MethodInfo mi : methodsList) {
			if(Print) {
				System.out.println("method: "+mi.toString() + ", sql var:" + sqlVar);
			}
			if(mi.name.equals(method)) {
				for (ParaInfo pi : mi.parameters) {
					if(Print) {
						System.out.println(pi.toString());
					}
					if(pi.pn.equals(sqlVar)) {
						//if(Print)System.out.println("I'm HERE!!!");
						return pi.plno;
					}
				}
			}

		}
		return -1;
	}
	public static String Extract_call_at_line(String basePath, String fileName, int line) {

		if (Print) {
			System.out.println("*** Extractor ");
		}
		if (Print) {
			System.out.println("Extract from file: " + basePath + "/" + fileName);
		}
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory()) {
			return null;
		}

		///// ***** Parse file by selecting line numbers ******////

		clear_all();//+++

		findMethodsArgs(file);
		FindExecute(file);
		findVarDeclarator_and_AssignExps(file);
//		Print = true;

		String[] ffName = fileName.split("[.]");
		for (Info info : infoSet) {
//			if (info.lno == line) {
//				System.out.println(" ******************** ");
//				System.out.println(info.toString());
				try {
					File f2 = new File(basePath + "/" + ffName[0] + "/Query" + line + ".txt");
					if (!f2.exists()) {
						f2.getParentFile().mkdir();
					}
					FileWriter myWriter = new FileWriter(f2);
					if (Print) {
						System.out.println("info.query: " + info.query);
					}
					myWriter.write(info.query);
					myWriter.close();
					return f2.getAbsolutePath();
				} catch (IOException e) {
					System.out.println(" ###### ");
					e.printStackTrace();
				}
			//}
		}
		return null;
	}

	public static String Extractor_no_comments(String basePath, String fileName, int n) {
		String query = null;
		if (Print) {
			System.out.println("*** Extractor ");
		}
		if (Print) {
			System.out.println("Extract from file: " + basePath + "/" + fileName);
		}
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory()) {
			return null;
		}

		///// ***** Parse file by selecting line numbers ******////
//		infoSet.clear();

		clear_all();//+++

		FindExecute(file);
		FindStmtStr(file);
		//+++
		findAssignExpr(file);
		findVarDeclarator(file);
		//+++
		if (infoSet.size() > 0) {
			if (Print) {
				System.out.println("\n====================\n");
			}
			if (Print) {
				System.out.println(fileName + " : " + infoSet.size());
			}
			if (Print) {
				for (Info info : infoSet) {
					System.out.println("lno: " + info.lno + "\nquery: " + info.query + "\nstmt: " + info.stmtVar
							+ "\nstr: " + info.arg);
					if(info.query != null) {

//					for (String line: info.query.split("\n")) {
//						System.out.println("~~~~~ "+line);
//					}
					}
				}
			}
		}
		String[] ffName = fileName.split("[.]");
//		int i = 1;
		for (Info info : infoSet) {
			if (info.query == null) {
				continue;
			}
			try {
				String temp =info.query;
				for (String line: info.query.split("\n")) {
					if(line.contains("//")) {
						String [] parts = line.split("//", 2);
						if (Print) {
							System.out.println("~~~~~ p1"+parts[0]);
						}
						if (Print) {
							System.out.println("~~~~~ p2"+parts[1]);
						}
						temp = temp.replace("//"+parts[1], "");
					}else {
						if (Print) {
							System.out.println("~~~~~ "+line);
						}

					}
					info.query = temp;
				}
//                File f = new File(basePath+"/Temp/"+ffName[0]);
//            	if(!f.exists()) f.mkdir();
				// File f2 = new File(basePath+"/Temp/"+ffName[0]+"/String"+i+".txt");
				File f2 = new File(basePath + "/" + ffName[0] + "/Query" + n + ".txt");
//            	File f2 = new File(basePath+"/"+ffName[0]+"_Query.txt");
				if (!f2.exists()) {
					f2.getParentFile().mkdir();
				}
//                FileWriter myWriter = new FileWriter(basePath+"/Temp/"+ffName[0]+"/String"+i+".txt");
				FileWriter myWriter = new FileWriter(f2);
				if (Print) {
					System.out.println("info.query: " + info.query);
				}
				myWriter.write(info.query);
				query = info.query;
				myWriter.close();
				//return f2.getAbsolutePath();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			i++;
		}
		return query;
	}

	public static String ExtractorPStmt(String filePath, String key, int lno) {
		if (Print) {
			System.out.println("*** ExtractorPStmt ");
		}
		File file = new File(filePath);
		if (file.isDirectory()) {
			return null;
		}

		///// ***** Parse file by selecting line numbers ******////
		clear_all();//+++
//		System.out.println("+++++ lno: " + lno);
		FindPStmt(file, key, lno);
//        FindExecute(file);
		FindStmtStr2(file);

		//+++
		findAssignExpr(file);
		findVarDeclarator(file);
		//+++

//		if (infoSet2.size() > 0) {
//			System.out.println(className + " : " + infoSet2.size());
//		}
		for (Info info : infoSet2) {
			if (Print) {
				System.out.println("##### " + info.query);
			}
			pstmt_query = info.query;
		}
		return pstmt_query;

	}
	public static void clear_all() {
		infoSet.clear();
		infoSet2.clear();
		assignList.clear();
		assignListCalls.clear();
		propertiesVarList.clear();//++
		infoSet_properties.clear();//++
		methodsList.clear();
		pstmt_query="";
		app_conn="";
		app_rs="";
		app_sql="";
		app_ps="";
		app_stmt="";
		srcCode.clear();
	}
	public static List<String> ExtractorExp1(String filePath) throws IOException {
		if (Print) {
			System.out.println("*** ExtractorExp1 ");
		}

		File file = new File(filePath);
		if (file.isDirectory()) {
			return null;
		}

		///// ***** Parse file by selecting line numbers ******////
		clear_all();//+++
//		srcCode


//		FindPStmt(file, key, lno);
		FindExecute(file);
		FindStmtStr(file);

		//+++
		findAssignExpr(file);
		findVarDeclarator(file);
		//+++
//		if (infoSet.size() > 1) {
//			System.err.println(filePath);
//		}
		List<String> key_query = new ArrayList<>();

		for (Info info : infoSet) {

//			System.out.println("##### " + info.query);
//			query = info.query;
			key_query.add(info.key + ":" + info.query);
			if (Print) {
				System.out.println("##### " + key_query + "\n" + info.call_lines);
			}
		}

		return key_query;

	}

	public static List<String> ExtractorExp2(String filePath, int loc) {
		if (Print) {
			System.out.println("*** ExtractorExp1 ");
		}

		File file = new File(filePath);
		if (file.isDirectory()) {
			return null;
		}

		///// ***** Parse file by selecting line numbers ******////
		clear_all();//+++
//		FindPStmt(file, key, lno);
		FindExecute(file);
		FindStmtStr(file);

		//+++
		findAssignExpr(file);
		findVarDeclarator(file);
		//+++
//		if (infoSet.size() > 1) {
//			System.err.println(filePath);
//		}
		List<String> key_query = new ArrayList<>();

		for (Info info : infoSet) {

//			System.out.println("##### " + info.query);
//			query = info.query;
			key_query.add(info.key + ":" + info.query);
			if (Print) {
				System.out.println("##### " + key_query + "\n" + info.call_lines);
			}
		}

		return key_query;

	}


	public static void FindStmtStr2(File file) {
		if (Print) {
			System.out.println("*** FindStmtStr2 ");
		}
		List<Integer> list = new ArrayList<>();

		infoSet2.forEach(info -> {

			try {
				new VoidVisitorAdapter<>() {
					@Override

					public void visit(VariableDeclarator n, Object arg) {
						super.visit(n, arg);
						// if(n.getName().toString().equals(info.stmt))System.out.println(info.stmt);
						if (n.getName().toString().equals(info.stmtVar) && n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							// System.out.println(n.getName().toString() + " " + n.getBegin().get().line);
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
								info.call_lines.add(n.getBegin().get().line);
							}
							list.add(n.getBegin().get().line);
						} else if (n.getInitializer().isPresent() && n.getName().toString().equals(info.arg)
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print) {
								System.out.println(n.getName().toString() + " " + n.getBegin().get().line);
							}
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
								info.call_lines.add(n.getBegin().get().line);
							}
							list.add(n.getBegin().get().line);
							info.query =  n.getInitializer().get().toString();
						}
					}
				}.visit(JavaParser.parse(file), null);
			} catch (RuntimeException e) {
				new RuntimeException(e);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			try {
				new VoidVisitorAdapter<>() {
					@Override
					public void visit(AssignExpr n, Object arg) {
						super.visit(n, arg);
						// System.out.println(n);
						if ((n.getTarget().toString().equals(info.stmtVar) || n.getTarget().toString().equals(info.arg))
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print) {
								System.out.println(n.getTarget() + " " + n.getBegin().get().line);
							}
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
								info.call_lines.add(i);
							}
//								info.call_lines.add(n.getBegin().get().line);
							list.add(n.getBegin().get().line);
							if (n.getTarget().toString().equals(info.arg)) {
								info.query = n.getValue().toString();
//								System.out.println("***** " + n.getTarget() + " " + n.getBegin().get().line);
//								System.out.println("***** " + n.toString());
//                                info.codeline = n.toString() +" ;";
								info.lno = n.getBegin().get().line;
							}
						}
					}
				}.visit(JavaParser.parse(file), null);
			} catch (RuntimeException e) {
				new RuntimeException(e);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// System.out.println("QQQQQQQQQ "+info.query);
		});
	}
public static void loopOverInfoSet() {
	infoSet_properties.forEach(info -> {infoSet.add(info);});

	infoSet.forEach(info -> {
		//================== getConnection vars

		//if url is var, get the closest value
		if (info.key.equals("getConnection")) {// in getVarClosestValue, we use the same stmt lno (info.lno) as all values are passed in one call
//			System.out.println("@@@ getConnection");
//			System.out.println("HHH 1: " + info.conn_url_value_type +" - "+ info.conn_url_value);
			// ==================== Extract url information
			AssignExpInfo conarg1 = null;//new AssignExpInfo();
			if(!is_constant(info.conn_url_value_type, info.conn_url_value)) {
//				System.out.println("HHH 2"+info.conn_url_value_type+" - "+info.conn_url_value);
//				conarg1 = getVarClosestValue_or_para(info.conn_url_var, info.lno, info.method);
////			if (!info.conn_url_var.equals("") && info.conn_url_value.equals("")) {
				conarg1 = getVarClosestValue(info.conn_url_var, info.lno, info.method);
				if (conarg1 != null) {
//					System.out.println("HHH 2.1");
					info.conn_url_value = conarg1.value;
					info.conn_url_value_type = conarg1.valueType;
					info.conn_url_ln = conarg1.lnos;
					info.conn_url_lines = conarg1.value_lines;
				} else {// search in method paras
//						// huna3-url
//					System.out.println("HHH 2.2");
					ParaInfo param = var_isMethodParam(info.method, info.conn_url_var);
					if (param != null) {
						// queryVar_isMethodParam
						// param.dt;
						info.conn_url_value = "";
						info.conn_url_value_type = G.methodArg;
						info.conn_url_ln = param.plno;
						info.conn_url_lines = null;
					}
				}
			}

			// ==================== Extract user information
			AssignExpInfo conarg2 = null;//new AssignExpInfo();
			// if username is var, get the closest value
//		if (!info.conn_user_var.equals("") && info.conn_user_value.equals("")) {
			if(!is_constant(info.conn_user_value_type, info.conn_user_value)) {
//				System.out.println("HHH 3: " +info.conn_user_value_type +" - "+info.conn_user_value);
//				conarg2 = getVarClosestValue_or_para(info.conn_user_var, info.lno, info.method);
				conarg2 = getVarClosestValue(info.conn_user_var, info.lno, info.method);
//				// Extract information
				if (conarg2 != null) {
//					System.out.println("HHH 3.1");
					info.conn_user_value = conarg2.value;
					info.conn_user_value_type = conarg2.valueType;
					info.conn_user_ln = conarg2.lnos;
					info.conn_user_lines = conarg2.value_lines;
				} else {// search in method paras
						// huna3-user
//					System.out.println("HHH 3.2");
					ParaInfo param = var_isMethodParam(info.method, info.conn_user_var);
					if (param != null) {
						// queryVar_isMethodParam
						// param.dt;
						info.conn_user_value = "";
						info.conn_user_value_type = G.methodArg;
						info.conn_user_ln = param.plno;
						info.conn_user_lines = null;

					}
				}
			}
			// ==================== Extract pass information
			AssignExpInfo conarg3 = null;//new AssignExpInfo();
			// if password is var, get the closest value
//			if (!info.conn_pass_var.equals("") && info.conn_pass_value.equals("")) {
			if (!is_constant(info.conn_pass_value_type, info.conn_pass_value)) {
//				System.out.println("HHH 4: " + info.conn_pass_value_type + " - " + info.conn_pass_value);
//				conarg3 = getVarClosestValue_or_para(info.conn_pass_var, info.lno, info.method);
				conarg3 = getVarClosestValue(info.conn_pass_var, info.lno, info.method);
//				// Extract information
				if (conarg3 != null) {
//					System.out.println("HHH 4.1");
					info.conn_pass_value = conarg3.value;
					info.conn_pass_value_type = conarg3.valueType;
					info.conn_pass_dt = conarg3.dataType;
					info.conn_pass_ln = conarg3.lnos;
					info.conn_pass_lines = conarg3.value_lines;
				} else {// search in method paras
					// huna3-pass
//					System.out.println("HHH 4.2");
					ParaInfo param = var_isMethodParam(info.method, info.conn_pass_var);
					if (param != null) {
//						System.out.println("HHH 4.2.1");
						// queryVar_isMethodParam
						// param.dt;
						info.conn_pass_value = "";
						info.conn_pass_value_type = G.methodArg;
						info.conn_pass_ln = param.plno;
						info.conn_pass_lines = null;
						info.conn_pass_dt = param.dt;
					}
				}
			}

		}

		//++++++++ TODO: get properties values - huna2
		// in getVarClosestValue, we use different stmt lno (info.conn_url_ln, ...) as  values are passed in different "put" calls
		if (info.key.equals("Properties")) {
			AssignExpInfo conarg1 = null;//new AssignExpInfo();
//			if (!info.conn_url_var.equals("") && info.conn_url_value.equals("")) {
//			if(!info.conn_url_value_type.equals(G.constantValue)) {
			if(!is_constant(info.conn_url_value_type, info.conn_url_value)) {
				conarg1 = getVarClosestValue(info.conn_url_var, info.conn_url_ln, info.method);
				// Extract information
				if(conarg1!=null) {
				info.conn_url_value = conarg1.value;
				info.conn_url_value_type = conarg1.valueType;
				info.conn_url_ln = conarg1.lnos;
				info.conn_url_lines = conarg1.value_lines;}
			}


			AssignExpInfo conarg2 = null;//new AssignExpInfo();
			// if username is var, get the closest value
//			if (!info.conn_user_var.equals("") && info.conn_user_value.equals("")) {
			if(!is_constant(info.conn_user_value_type, info.conn_user_value)) {
//			if(!info.conn_user_value_type.equals(G.constantValue) ) {
				conarg2 = getVarClosestValue(info.conn_user_var, info.conn_user_ln, info.method);
				// Extract information
				if(conarg2!=null) {
				info.conn_user_value = conarg2.value;
				info.conn_user_value_type = conarg2.valueType;
				info.conn_user_ln = conarg2.lnos;
				info.conn_user_lines = conarg2.value_lines;}
			}

			AssignExpInfo conarg3 = null;//new AssignExpInfo();
			// if password is var, get the closest value
//			if (!info.conn_pass_var.equals("") && info.conn_pass_value.equals("")) {
//			if(!info.conn_pass_value_type.equals(G.constantValue)) {
			if(!is_constant(info.conn_pass_value_type, info.conn_pass_value)) {
				conarg3 = getVarClosestValue(info.conn_pass_var, info.conn_pass_ln, info.method);
				// Extract information
				if(conarg3!=null) {
				info.conn_pass_value = conarg3.value;
				info.conn_pass_value_type = conarg3.valueType;
				info.conn_pass_dt = conarg3.dataType;
				info.conn_pass_ln = conarg3.lnos;
				info.conn_pass_lines = conarg3.value_lines;}
			}

		}
		//===================================

//		System.out.println("KEY @ " + info.key);
		// ==== In case the query value is stored in a tring variable
//		if(G.ExIPList.contains(info.key) || info.key.equals("prepareStatement")) {}
		AssignExpInfo ae = new AssignExpInfo();
		//if query is var, get the closest value
		if (!info.arg.equals("") && info.query.equals("")) {
			ae = getVarClosestValue(info.arg, info.lno, info.method);
			// Extract information
			if(ae!=null) {
			info.query = ae.value;
			info.qlno = ae.lnos;
			info.query_lines = ae.value_lines;
			info.query_type = ae.valueType;}
//			info.qlnoe = ae.lnoe;
		}
		//=== extract rs value and dt
		AssignExpInfo ae2 = getStmtDetils(info.lno);
		if(ae2 != null) {
			String rhs = ae2.value;
			if (rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) {// && !rhs.contains("createStatement")) {
				info.app_rs = ae2.varName;
				info.app_rs_dt = ae2.dataType;
//				if (Print) {
//					System.out.println("RHS: " + rhs);// >>>>
//					System.out.println("app_rs: " + info.app_rs);
//				}
			}
//			if ((rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) && !rhs.contains("createStatement")) {
//
//			}
//			System.out.println("PSSSSS1 @ " + ae2.value);
			if (rhs.contains("prepareStatement")) {
				info.app_ps = ae2.varName;// n.getChildNodes().get(1).toString();
				info.app_conn = ae2.conn;

//				if (Print) {
//					System.out.println("RHS: " + rhs);
//					System.out.println("app_ps: " + app_ps);
//				}
			}

			if(rhs.contains("createStatement")
					&& !(rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) ) {

				info.app_conn = ae2.conn;
				info.stmtVar = ae2.varName;

			}
			if(rhs.contains("getConnection") && !(rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) ) {
				info.app_conn = ae2.varName;
			}

			if(rhs.contains("createStatement") && (rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) ) {
//				info.app_conn = ae2.conn;
				info.app_rs = ae2.varName;
			}
		}

			if(info.query.equals("")) {
				//Check if it is a method parameter
//				int paralno = queryVar_isMethodParam(info.method, info.arg);
				String paraDt = queryVar_isMethodParam(info.method, info.arg);
//				if(paralno > 0) {
//					info.query = "parameter";
//					info.query_lines.add(paralno);
//				}
				if(paraDt != null) {
					info.query = "parameter";
					info.query_type = paraDt;
				}
			}

	});
}

public static void findAssigExpDataTypes() {
	//assignList, assignListCalls
	for (AssignExpInfo i : assignList) {
	if(i.dataType.isEmpty() || i.dataType.equals("")) {
		for (AssignExpInfo j : assignList) {
			if(i.lnos != j.lnos) {
				if(i.varName.equals(j.varName) && !j.dataType.equals("")) {
					i.dataType = j.dataType;
					break;
				}
			}
		}
	}
	}

}
public static String Extractor(String basePath, String fileName, int n) throws IOException {

		if (Print) {
			System.out.println("*** Extractor ");
		}
		if (Print) {
			System.out.println("Extract from file: " + basePath + "/" + fileName);
		}
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory()) {
			return null;
		}

		///// ***** Parse file by selecting line numbers ******////
//		infoSet.clear();

		clear_all();//+++
		srcCode = StrUtil.read_lines_list(basePath + "/" + fileName);

		for(int i =0 ;i<srcCode.size();i++) {
			if(Print) {
				System.out.println(i +"@" +srcCode.get(i));
			}
		}
		findVarDeclarator_and_AssignExps(file);
		//++++
		findAssigExpDataTypes();
		//+++
		findMethodsArgs(file);
		FindExecute(file);
//		findVarDeclarator_and_AssignExps(file);
		loopOverInfoSet();
//		FindStmtStr(file);
////		FindStmtStr2(file);
//		//+++
//		findAssignExpr(file);
//		findVarDeclarator(file);
		//+++


//		Print = true;
//		String[] ffName = fileName.split("[.]");
//		for (Info info : infoSet) {
//			if (Print) {
//				System.out.println(" ******************** ");
//				System.out.println(info.toString());
//			}
//			if (info.query == null || info.query.equals(""))
//				continue;
//			try {
//				File f2 = new File(basePath + "/" + ffName[0] + "/Query" + n + ".txt");
//				if (!f2.exists())
//					f2.getParentFile().mkdir();
//				FileWriter myWriter = new FileWriter(f2);
//				if (Print)
//					System.out.println("info.query: " + info.query);
//				myWriter.write(info.query);
//				myWriter.close();
//			} catch (IOException e) {
//				System.out.println(" ###### ");
//				e.printStackTrace();
//			}
//		}
		return null;
	}

	public static void print_all(){
		//propertiesVarList
		System.out.println("***************** (propertiesVarList) ***************** ");
		for (AssignExpInfo i : propertiesVarList) {
			System.out.println(i.toString());
		}
		System.out.println("***************** (infoSet) ***************** ");
		for (Info i : infoSet) {
			System.out.println(i.toString());
		}
//		System.out.println("***************** (infoSet2) ***************** ");
//		for (Info i : infoSet2) {
//			System.out.println(i.toString());
//		}
		System.out.println("***************** (assignList) ***************** ");
		for (AssignExpInfo i : assignList) {
			System.out.println(i.toString());
		}
		System.out.println("***************** (assignListCalls) ***************** ");
		for (AssignExpInfo i : assignListCalls) {
			System.out.println(i.toString());
		}
		System.out.println("***************** (methodsList) ***************** ");
		for (MethodInfo i : methodsList) {
			System.out.println(i.toString());
		}

	}
	public static void main(String[] args) throws JSQLParserException, IOException {

//		String app_conn = "";
//		String app_rs = "";
//		String app_sql = "";
		//
//		File file = new File("/Users/dareen/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/q_s_v_vul.java");
//		File file = new File("/Users/dareen/Fixer/DCAFixer_Experimets/hibernate-orm-main/hibernate-testing/src/main/java/org/hibernate/testing/cleaner/OracleDatabaseCleaner.java");
//		String path = "/Users/dareen/Fixer/DCAFixer_Experimets/hibernate-orm-main/hibernate-testing/src/main/java/org/hibernate/testing/cleaner";
//		String fileName = "q_s_v_vul.java";
//		String fileName = "OracleDatabaseCleaner.java";
//		String q = Extractor_no_comments(path, fileName, 2);
//		QParser.parse_query(q, true,null);
//		System.out.println("Extractor:\n"+ Extractor("/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace", "bookshop.java", 1020));
		//System.out.println("Extractor:\n"+ Extractor("/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Clases", "Imagen.java", 1020));
//		printAll();
		//
//		System.out.println("Extractor:\n"+ Extractor("/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Clases","Articulo.java", 0));

//		System.out.println("Extractor:\n"+ Extractor("/Users/Dareen/NetBeansProjects/DCAFixer_FuelOrdersClient/src/fuelordersclient","PetrolDBConnection.java", 0));
//		print_all();
//		clear_all();
//		System.out.println("Extractor:\n"+ Extractor("/Users/Dareen/NetBeansProjects/DCAFixer_FuelOrdersClient/src/fuelordersclient","PetrolInsertForm.java", 0));
//		print_all();
//		clear_all();

//		System.out.println("Extractor:\n"+ Extractor("/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/test/Java_BBDD-master/src/","DataBase.java",0));
		//
		System.out.println("Extractor:\n"+ Extractor("/Users/Dareen/NetBeansProjects/smallBank/src/TSet2/","PaperExample_vul.java",0));
//		System.out.println("Extractor:\n"+ Extractor("/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/GUI-DBMS-master/TableEasy/src/tableeasy/","OracleDatabaseConnection.java",0));
		print_all();
		clear_all();
		//   executeUpdate @ Delete @ 211

//		public static List<AssignExpInfo> assignList = new ArrayList<>();
//		public static List<AssignExpInfo> assignListCalls = new ArrayList<>();
//		public static List<MethodInfo> methodsList = new ArrayList<>();

//		/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Data.java

//
//		System.out.println("Extractor_no_comments:\n"+Extractor_no_comments("/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace", "bookshop.java", 0));

//		System.out.println("exp1Extractor:\n"+ exp1Extractor("/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace", "bookshop.java", 0));
//		String q = Extractor_no_comments(path, fileName, 2);
//		Extractor(path, fileName, 2);

//		findVarDeclarator(file);
//		findAssignExpr(file);

	}

}


