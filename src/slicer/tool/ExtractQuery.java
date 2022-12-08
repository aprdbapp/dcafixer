package slicer.tool;

import com.github.javaparser.JavaParser;
//import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.Node;
//import com.github.javaparser.ast.
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Optional;
import com.ibm.wala.shrike.shrikeCT.*;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

//import dcafixer.AssignExpInfo;
//import com.github.javaparser.ast.body.VariableDeclarator;
import net.sf.jsqlparser.JSQLParserException;
import queryparser.QParser;
import slicer.utilities.StrUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
			if(parameters.size()>0)
				str += "\n\tparameters " + parameters.toString() + "\n";
			else 
				str += "\n";
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
					+ lnos + ", dataType=" + dataType + ", conn=" + conn + ", method=" + method + "]\n";
		}
		//		ArrayList<Integer> lines = new ArrayList<>();
//		String rhs, lhs_query;
		String varName="";
		String value="";
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
		if (Print)
			System.out.println("*** AssignExpr ");
		try {
			new VoidVisitorAdapter<Object>() {
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
			new VoidVisitorAdapter<Object>() {
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
		
		if (Print) 
			System.out.print(methodsList.toString());
	}
	public static AssignExpInfo getQueryStr ( String varName , int stmtloc , String method) {
		AssignExpInfo closeset = new AssignExpInfo();
		for(int i=0; i< assignList.size();i++) {
			AssignExpInfo v = assignList.get(i);
			//TODO: Try to use Method name, it didn't work because of VariableDeclarator
			if (v.lnos < stmtloc && v.lnos > closeset.lnos && v.varName.equals(varName)) {// && v.method.equals(method)) {
				closeset = v;
			}
		}
		return closeset;	
	}
	
	public static AssignExpInfo getStmtDetils (  int stmtloc) {
//		AssignExpInfo closeset = assignList.get(0);
		for (AssignExpInfo ae :assignListCalls ) {
			if(ae.lnos == stmtloc)
				return ae;
			
		}
		return null;
			
	}
	
	public static void findVarDeclarator_and_AssignExps(File file) {
//      System.out.println("fff "+file.getName());
//		Print =true;
		if (Print)
			System.out.println("*** Find VariableDeclarator ");
		try {
			new VoidVisitorAdapter<Object>() {
				@Override

				public void visit(VariableDeclarator n, Object arg) {
					super.visit(n, arg);
					Node parent  = n.getParentNode().get();
					AssignExpInfo VarDec = new AssignExpInfo();
					if (Print) 
					System.out.println("$$$ new --- "+ "@"+n.getBegin().get().line +  n.getChildNodes().toString() );
					
					if(n.getChildNodes().size() == 3) {
						VarDec.dataType = n.getChildNodes().get(0).toString();
						VarDec.varName = n.getChildNodes().get(1).toString();
						VarDec.value =  n.getChildNodes().get(2).toString();
						VarDec.lnos = n.getChildNodes().get(2).getBegin().get().line;
						for (int i = n.getChildNodes().get(2).getBegin().get().line; i <= n.getChildNodes().get(2).getEnd()
								.get().line; i++) 
							VarDec.value_lines.add(i);
						//+++++++++++++++
						if(VarDec.value.contains("createStatement") && 
								!(parent.toString().contains("executeQuery") || parent.toString().contains("execute")|| parent.toString().contains("executeUpdate")) ) 
							VarDec.conn= n.getChildNodes().get(2).getChildNodes().get(0).toString();
						//VarDec.conn= n.getChildNodes().get(2).getChildNodes().get(0).getChildNodes().get(0).toString();
						//+++++++++++++++
						if(VarDec.value.contains("prepareStatement") ) //|| VarDec.value.contains("createStatement") ) 
							VarDec.conn= n.getChildNodes().get(2).getChildNodes().get(0).toString();
						
						if(VarDec.value.contains("executeQuery") || VarDec.value.contains("executeUpdate") 
								||VarDec.value.contains("execute(") ||VarDec.value.contains("createStatement")||VarDec.value.contains("prepareStatement") ) {
							assignListCalls.add(VarDec);
						}else {
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
			new VoidVisitorAdapter<Object>() {
				@Override

				public void visit(AssignExpr n, Object arg) {
					super.visit(n, arg);
					if (Print) 
						System.out.println("$$$ new AssignExpr visitor --- " + "@"+n.getBegin().get().line+ n.getChildNodes().toString() );
					AssignExpInfo VarDec = new AssignExpInfo();
					VarDec.dataType = "";//n.getChildNodes().get(0).toString();
					VarDec.varName = n.getChildNodes().get(0).toString();
					VarDec.value =  n.getChildNodes().get(1).toString();
					VarDec.lnos = n.getBegin().get().line;
					//VarDec.method = n.findParent(MethodDeclaration.class).get().getName().toString();
					if (Print) 
						System.out.println("Here 1");
					
					for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
						VarDec.value_lines.add(i);
					if (Print) 
						System.out.println("Here 2");
					
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
							||VarDec.value.contains("execute(") ||VarDec.value.contains("createStatement")||VarDec.value.contains("prepareStatement") ) {
						assignListCalls.add(VarDec);
					}else {
						assignList.add(VarDec);
					}
					if (Print) 
						System.out.println("Here 3");
					
					
					

				}
			}.visit(JavaParser.parse(file), null);
		} catch (RuntimeException e) {
			new RuntimeException(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		infoSet.forEach(info -> {
//			System.out.println("KEY @ " + info.key);
			AssignExpInfo ae = new AssignExpInfo();
			//if query is var, get the closest value 
			if (!info.arg.equals("") && info.query.equals("")) {
				ae = getQueryStr(info.arg, info.lno, info.method);
				// Extract information
				info.query = ae.value;
				info.qlno = ae.lnos;
				info.query_lines = ae.value_lines;
//				info.qlnoe = ae.lnoe;
			}
			AssignExpInfo ae2 = getStmtDetils(info.lno);
			if(ae2 != null) {
				String rhs = ae2.value;
				if (rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) {// && !rhs.contains("createStatement")) {
					info.app_rs = ae2.varName;
//					if (Print) {
//						System.out.println("RHS: " + rhs);// >>>>
//						System.out.println("app_rs: " + info.app_rs);
//					}
				}
//				if ((rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) && !rhs.contains("createStatement")) {
//					
//				}
//				System.out.println("PSSSSS1 @ " + ae2.value);
				if (rhs.contains("prepareStatement")) {
					info.app_ps = ae2.varName;// n.getChildNodes().get(1).toString();
					info.app_conn = ae2.conn;

//					if (Print) {
//						System.out.println("RHS: " + rhs);
//						System.out.println("app_ps: " + app_ps);
//					}
				}
				
				if(rhs.contains("createStatement") 
						&& !(rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) ) {

					info.app_conn = ae2.conn;
					info.stmtVar = ae2.varName;
				}
				
				if(rhs.contains("createStatement") && (rhs.contains("executeQuery") || rhs.contains("executeUpdate") || rhs.contains("execute(") ) ) {
//					info.app_conn = ae2.conn;
					info.app_rs = ae2.varName;
				}
			}
		
				if(info.query.equals("")) {
					//Check if it is a method parameter
					int paralno = queryVar_isMethodParam(info.method, info.arg);
					if(paralno > 0) {
						info.query = "parameter";
						info.query_lines.add(paralno);
					}
				}

		});
		// TODO: print infoSet

	}


	
	public static void findVarDeclarator(File file) {
//      System.out.println("fff "+file.getName());
//		Print =true;
		if (Print)
			System.out.println("*** Find VariableDeclarator ");
		try {
			new VoidVisitorAdapter<Object>() {
				@Override

				public void visit(VariableDeclarator n, Object arg) {
					super.visit(n, arg);
					if (Print) System.out.println("$$$ --- "+ n.getChildNodes().toString());
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
		if (Print)
			System.out.println("*** FindExecute ");
		try {
			new VoidVisitorAdapter<Object>() {
				@Override
				public void visit(MethodCallExpr n, Object arg) {
					super.visit(n, arg);
					Node parent = n.getParentNode().get();
					if (Print)System.out.println(n.getNameAsString() + ", isEmpty? "+n.getArguments().isEmpty());
//					if (Print)System.out.println("\t" + n.getChildNodes().toString());
//					if (n.getArguments().isNonEmpty() &&
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
								if(Print)System.out.println("Conn name: " +i.getChildNodes().get(0).getChildNodes().get(0));
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
						
						if (Print)
							System.out.println("** 1 " + n.getNameAsString());
						
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							info.call_lines.add(i);
							// Check where exactly execute call is located!
							int loc = i-1;
							if(srcCode.get(loc).contains(n.getNameAsString())) {
								info.lno = i;//++++ for experiment 2
								if(Print )System.out.println("SRCLINE: srcCode[" + loc +"] line "+info.lno+" @ "+srcCode.get(loc));
								
							}
						}
						if(info.lno == 0)
							info.lno = n.getBegin().get().line;//++++ for experiment 2
						if (n.getArguments().get(0).getMetaModel().toString().equals("NameExpr")) {
							info.arg = n.getArguments().get(0).toString();
							app_sql = info.arg;
						} else {
							info.query = n.getArguments().get(0).toString();// +"@FindExecute";
							info.qlno = n.getArguments().get(0).getBegin().get().line;
							for (int i = n.getArguments().get(0).getBegin().get().line; i <= n.getArguments().get(0).getEnd().get().line; i++)
								info.query_lines.add(i);
								
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
//						info.method = n.findParent(MethodDeclaration.class).get().getName().toString();
						infoSet.add(info);
						if (Print)
							System.out.println( info.toString());
					} 
						
					if (n.getArguments().isEmpty() && 
							( n.getNameAsString().equals("executeUpdate") || n.getNameAsString().equals("executeQuery")
							|| n.getNameAsString().equals("execute") )) {
						if (Print)System.out.println("######### "+n.getNameAsString() + ", isEmpty? "+n.getArguments().isEmpty());
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

					if(n.getNameAsString().equals("createStatement") && 
							!(parent.toString().contains("executeUpdate") || parent.toString().contains("executeQuery") || parent.toString().contains("execute"))) {
						
						Info info = new Info();
						info.key = "createStatement";
						info.app_conn = n.getChildNodes().get(0).toString();
						info.lno = n.getBegin().get().line;
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							info.call_lines.add(i);
							if (Print)
								System.out.println( "@ createStatement Branch: "+ info.toString()+ " 0 : "+n.getChildNodes().get(0).toString() +" 1 : "+ n.getChildNodes().get(1).toString());

						}
						
//						info.method = n.findParent(MethodDeclaration.class).get().getName().toString();
						infoSet.add(info);
//						if(Print) System.out.println("info.app_conn: " + info.app_conn);
						if (Print)
							System.out.println("------- lno: " + info.lno + "\nquery: " + info.query + "\nstmt: " + info.stmtVar
									+ "\nstr: " + info.arg+"\napp_conn: " + info.app_conn);
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

	
	public static void FindPStmt(File file, String key, int lno) {
//      System.out.println("fff "+file.getName());
		if (Print)
			System.out.println("*** FindPStmt ");
		try {
			new VoidVisitorAdapter<Object>() {
				@Override
				public void visit(MethodCallExpr n, Object arg) {
					super.visit(n, arg);
					// System.out.println(n.getNameAsString());
					boolean foundline = false;
					if (n.getArguments().isNonEmpty() && (n.getNameAsString().equals(key))) {
						if (Print)
							System.out.println("** 2 " + n.getNameAsString());
						Info info = new Info();
						for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++) {
							if (n.getBegin().get().line == lno) {
								if (Print)
									System.out.println(" lno is found! ");
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
							for(int i =n.getBegin().get().line; i<= n.getEnd().get().line; i++)
								info.query_lines.add(i);
								
//							info.qlnoe =n.getEnd().get().line;
							info.arg = "";
							app_sql = info.arg;
						}
						info.stmtVar = n.getChildNodes().get(0).toString();
						app_stmt = info.stmtVar;
//                      info.method = n.getMetaModel().toString();//.getNameAsString();
						if (Print)
							System.out.println("lno: " + info.lno + "\nquery: " + info.query + "\nstmt: " + info.stmtVar
									+ "\nstr: " + info.arg);
						if (foundline)
							infoSet2.add(info);
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
		if (Print)
			System.out.println("*** FindStmtStr ");
		List<Integer> list = new ArrayList<>();
		//Print = true;
		infoSet.forEach(info -> {
			if (Print) System.out.println("** FindStmtStr info: " + info.stmtVar +"\tlno "+info.lno+"\t str"+info.arg);
			try {
				new VoidVisitorAdapter<Object>() {
					@Override

					public void visit(VariableDeclarator n, Object arg) {
						super.visit(n, arg);
						// if(n.getName().toString().equals(info.stmt))System.out.println(info.stmt);
						if (n.getName().toString().equals(info.stmtVar) && n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print)
								System.out.println("** 3 " + n.getName().toString() + " " + n.getBegin().get().line);
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
								info.call_lines.add(n.getBegin().get().line);
							list.add(n.getBegin().get().line);
						} else if (n.getInitializer().isPresent() && n.getName().toString().equals(info.arg)
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print)
								System.out.println("** 4 " + n.getName().toString() + " " + n.getBegin().get().line);
//							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
//								info.lines.add(n.getBegin().get().line);
							list.add(n.getBegin().get().line);
							info.query = n.getInitializer().get().toString();//+"FindStmtStr P1";
							info.qlno =n.getBegin().get().line;
							for(int i =n.getBegin().get().line; i<= n.getEnd().get().line; i++)
								info.query_lines.add(i);
								
//							info.qlnoe =n.getEnd().get().line;
							if (Print)
								System.out.println("** 5 " + info.toString());
//								System.out.println("** 5 " + info.query +"\nqlno@ "+info.qlno + "\nqlnoE @ "+info.qlnoe);
						}
					}
				}.visit(JavaParser.parse(file), null);
			} catch (RuntimeException e) {
				new RuntimeException(e);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			try {
				new VoidVisitorAdapter<Object>() {
					@Override

					public void visit(AssignExpr n, Object arg) {
						super.visit(n, arg);
//						 System.out.println("@@@@@@ info.lines.get(0): "+info.lines.get(0)+"\n"+n);
						if ((n.getTarget().toString().equals(info.stmtVar) || n.getTarget().toString().equals(info.arg))
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							System.out.println("@@@@@@ info.lines.get(0): "+info.call_lines.get(0));
							System.out.println("*** " + n.getTarget() + " " + n.getBegin().get().line);
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
								info.call_lines.add(n.getBegin().get().line);
							list.add(n.getBegin().get().line);
							if (n.getTarget().toString().equals(info.arg) 
									&& n.getBegin().get().line == findClosestOne(list, info.call_lines.get(0))
									//&& ++ check that line number is close to the stmt 
									) {
								info.query = n.getValue().toString();//+"FindStmtStr P2";
								info.qlno =n.getBegin().get().line;
								for(int i =n.getBegin().get().line; i<= n.getEnd().get().line; i++)
									info.query_lines.add(i);
									
//								info.qlnoe =n.getEnd().get().line;
								if (Print)
									System.out.println("** 6 " + info.toString());
//									System.out.println("** 6 " + info.query +"\nqlno@ "+info.qlno + "\nqlnoE @ "+info.qlnoe+"\nlist:"+list.toString());
								
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
		if (Print)
			System.out.println("*** exp1Extractor ");
		if (Print)
			System.out.println("Extract from file: " + basePath + "/" + fileName);
//        File file = new File(basePath+"/"+fileName);
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory())
			return null;

		///// ***** Parse file by selecting line numbers ******////
		clear_all();//+++
		FindExecute(file);
		FindStmtStr(file);
		findAssignExpr(file);
		findVarDeclarator(file);
//		if (infoSet.size() > 0) {
//			System.out.println(fileName + " : " + infoSet.size());
//		}

		for (Info info : infoSet) {

			if (info.query == null)
				continue;
			try {

				File f2 = new File(basePath + "/Query" + n + ".txt");

				if (!f2.exists())
					f2.getParentFile().mkdir();
				FileWriter myWriter = new FileWriter(f2);
				myWriter.write(info.query);
				myWriter.close();
				return f2.getAbsolutePath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static int queryVar_isMethodParam (String method, String sqlVar) {
		if(Print)System.out.println("In queryVar_isMethodParam :");
		
		for (MethodInfo mi : methodsList) {
			if(Print) System.out.println("method: "+mi.toString() + ", sql var:" + sqlVar);
			if(mi.name.equals(method)) {
				for (ParaInfo pi : mi.parameters) {
					if(Print) System.out.println(pi.toString());
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
		
		if (Print)
			System.out.println("*** Extractor ");
		if (Print)
			System.out.println("Extract from file: " + basePath + "/" + fileName);
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory())
			return null;

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
					if (!f2.exists())
						f2.getParentFile().mkdir();
					FileWriter myWriter = new FileWriter(f2);
					if (Print)
						System.out.println("info.query: " + info.query);
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
		if (Print)
			System.out.println("*** Extractor ");
		if (Print)
			System.out.println("Extract from file: " + basePath + "/" + fileName);
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory())
			return null;

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
			if (Print)
				System.out.println("\n====================\n");
			if (Print)
				System.out.println(fileName + " : " + infoSet.size());
			if (Print)
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
		String[] ffName = fileName.split("[.]");
//		int i = 1;
		for (Info info : infoSet) {
			if (info.query == null)
				continue;
			try {
				String temp =info.query;
				for (String line: info.query.split("\n")) {
					if(line.contains("//")) {
						String [] parts = line.split("//", 2);
						if (Print) System.out.println("~~~~~ p1"+parts[0]);
						if (Print) System.out.println("~~~~~ p2"+parts[1]);
						temp = temp.replace("//"+parts[1], "");
					}else {
						if (Print) System.out.println("~~~~~ "+line);
						
					}
					info.query = temp;
				}
//                File f = new File(basePath+"/Temp/"+ffName[0]);
//            	if(!f.exists()) f.mkdir();
				// File f2 = new File(basePath+"/Temp/"+ffName[0]+"/String"+i+".txt");
				File f2 = new File(basePath + "/" + ffName[0] + "/Query" + n + ".txt");
//            	File f2 = new File(basePath+"/"+ffName[0]+"_Query.txt");
				if (!f2.exists())
					f2.getParentFile().mkdir();
//                FileWriter myWriter = new FileWriter(basePath+"/Temp/"+ffName[0]+"/String"+i+".txt");
				FileWriter myWriter = new FileWriter(f2);
				if (Print)
					System.out.println("info.query: " + info.query);
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
		if (Print)
			System.out.println("*** ExtractorPStmt ");
		File file = new File(filePath);
		if (file.isDirectory())
			return null;

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
			if (Print)
				System.out.println("##### " + info.query);
			pstmt_query = info.query;
		}
		return pstmt_query;

	}
	public static void clear_all() {
		infoSet.clear();
		infoSet2.clear();
		assignList.clear();
		assignListCalls.clear();
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
		if (Print)
			System.out.println("*** ExtractorExp1 ");

		File file = new File(filePath);
		if (file.isDirectory())
			return null;

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
			if (Print)
				System.out.println("##### " + key_query + "\n" + info.call_lines);
		}

		return key_query;

	}

	public static List<String> ExtractorExp2(String filePath, int loc) {
		if (Print)
			System.out.println("*** ExtractorExp1 ");

		File file = new File(filePath);
		if (file.isDirectory())
			return null;

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
			if (Print)
				System.out.println("##### " + key_query + "\n" + info.call_lines);
		}

		return key_query;

	}

	
	public static void FindStmtStr2(File file) {
		if (Print)
			System.out.println("*** FindStmtStr2 ");
		List<Integer> list = new ArrayList<>();

		infoSet2.forEach(info -> {

			try {
				new VoidVisitorAdapter<Object>() {
					@Override

					public void visit(VariableDeclarator n, Object arg) {
						super.visit(n, arg);
						// if(n.getName().toString().equals(info.stmt))System.out.println(info.stmt);
						if (n.getName().toString().equals(info.stmtVar) && n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							// System.out.println(n.getName().toString() + " " + n.getBegin().get().line);
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
								info.call_lines.add(n.getBegin().get().line);
							list.add(n.getBegin().get().line);
						} else if (n.getInitializer().isPresent() && n.getName().toString().equals(info.arg)
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print)
								System.out.println(n.getName().toString() + " " + n.getBegin().get().line);
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
								info.call_lines.add(n.getBegin().get().line);
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
				new VoidVisitorAdapter<Object>() {
					@Override
					public void visit(AssignExpr n, Object arg) {
						super.visit(n, arg);
						// System.out.println(n);
						if ((n.getTarget().toString().equals(info.stmtVar) || n.getTarget().toString().equals(info.arg))
								&& n.getBegin().get().line < info.call_lines.get(0)
								&& !list.contains(n.getBegin().get().line)) {
							if (Print)
								System.out.println(n.getTarget() + " " + n.getBegin().get().line);
							for (int i = n.getBegin().get().line; i <= n.getEnd().get().line; i++)
								info.call_lines.add(i);
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

public static String Extractor(String basePath, String fileName, int n) throws IOException {
		
		if (Print)
			System.out.println("*** Extractor ");
		if (Print)
			System.out.println("Extract from file: " + basePath + "/" + fileName);
		File file = new File(basePath + "/" + fileName);
		if (file.isDirectory())
			return null;

		///// ***** Parse file by selecting line numbers ******////
//		infoSet.clear();

		clear_all();//+++
		srcCode = StrUtil.read_lines_list(basePath + "/" + fileName);
		
		for(int i =0 ;i<srcCode.size();i++) {
			if(Print)System.out.println(i +"@" +srcCode.get(i));
		}
		findMethodsArgs(file);
		FindExecute(file);
		findVarDeclarator_and_AssignExps(file);
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

	public static void printAll(){
		System.out.println("***************** (infoSet "+infoSet.size()+") ***************** ");
		for (Info i : infoSet) {
			System.out.println(i.toString());
		}
		System.out.println("***************** (infoSet2) ***************** ");
		for (Info i : infoSet2) {
			System.out.println(i.toString());
		}
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
		// TODO: test if I can get all place holders values
		String app_conn = "";
		String app_rs = "";
		String app_sql = "";
		//
//		File file = new File("/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/q_s_v_vul.java");
		File file = new File("/Users/Dareen/Desktop/DCAFixer_Experimets/hibernate-orm-main/hibernate-testing/src/main/java/org/hibernate/testing/cleaner/OracleDatabaseCleaner.java");
		String path = "/Users/Dareen/Desktop/DCAFixer_Experimets/hibernate-orm-main/hibernate-testing/src/main/java/org/hibernate/testing/cleaner";
//		String fileName = "q_s_v_vul.java";
//		String fileName = "OracleDatabaseCleaner.java";
//		String q = Extractor_no_comments(path, fileName, 2);
//		QParser.parse_query(q, true,null);
//		System.out.println("Extractor:\n"+ Extractor("/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace", "bookshop.java", 1020));
		//System.out.println("Extractor:\n"+ Extractor("/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Clases", "Imagen.java", 1020));
//		printAll();
		System.out.println("Extractor:\n"+ Extractor("/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Clases","Articulo.java", 0));
		printAll();

		//   executeUpdate @ Delete @ 211

//		public static List<AssignExpInfo> assignList = new ArrayList<>();
//		public static List<AssignExpInfo> assignListCalls = new ArrayList<>();
//		public static List<MethodInfo> methodsList = new ArrayList<>();
		
//		/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Data.java

//		
//		System.out.println("Extractor_no_comments:\n"+Extractor_no_comments("/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace", "bookshop.java", 0));
		
//		System.out.println("exp1Extractor:\n"+ exp1Extractor("/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace", "bookshop.java", 0));
//		String q = Extractor_no_comments(path, fileName, 2);
//		Extractor(path, fileName, 2);
		
//		findVarDeclarator(file);
//		findAssignExpr(file);

	}

}


