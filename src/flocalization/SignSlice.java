package flocalization;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import queryparser.QParser;
import slicer.datatypes.Sig;
import slicer.utilities.StrUtil;

public class SignSlice {
	public static QParser qp;//classPrint
//	public static boolean classPrint = true;
//  Get the slice and search for the faults
//	public static Sig sign_slice(String path, List<String> keys, char dir, String query_path) throws IOException, JSQLParserException {
	public static Sig sign_slice_querypath(String path, String key, char dir, String query_path, boolean print, List<String>uiVars) throws IOException, JSQLParserException {
		if(print ) {
			System.out.println("query_path:" + query_path);
		}
		String query = read_lines(query_path);
		return sign_slice_querystring(path,key, dir, query.trim(),  print,uiVars);
	}

	public static String get_slices_folder_path (Sig sig, String msg) {
		String path =null;
		String subfolder = get_subfolder(sig.get_V());
		if(subfolder == null) {
			System.err.println(msg);//print the message (warning/no solution || no vul)
			return null;
		}

//		String s = sig.toString().replace(" ", "");
//		if(sig.contains(","))//otherwise, it's already clean.
		String s= StrUtil.sig_removecommas(sig.toString().replace(" ", ""));
//		path = Globals.SCPath+ subfolder+ "/"+ s +"/SSlices";
		if (sig.get_V() == 'Q' || sig.get_V() == 'q') {
			if (sig_equals_qv(s) == 1) {
				path = G.SCPath +"/"+ subfolder + "/q_s_v/SSlices";
			} else if (sig_equals_qv(s) == 2) {
				path = G.SCPath +"/"+ subfolder + "/q_iud_v/SSlices";
			} else if (sig_equals_qtc(sig) == 1) {// t. tc. c
				path = G.SCPath +"/"+ subfolder + "/q_s_t/SSlices";
			} else if (sig_equals_qtc(sig) == 2) {
				path = G.SCPath +"/"+ subfolder + "/q_iud_t/SSlices";
			}else if (sig.get_I().contains('a') ||sig.get_I().contains('A') ) {
				System.err.println(G.MSG_UsePS_all+" "+msg);
				return null;
			}else if (sig.get_I().contains('v') &&(sig.get_I().contains('c') ||sig.get_I().contains('t') ) ) {
				System.err.println(G.MSG_NoSol+" "+G.MSG_ct+" "+msg);
				return null;
			}
		} else // if()
		{// TODO : copy other cases code and test.
			// return null;
		}


		return path;
	}

	public static String get_slices_folder_path_exp4 (Sig sig, String msg) {
		//TODO: switch sslices path with vslices path
		String path =null;
		String subfolder = get_subfolder(sig.get_V());
		if(subfolder == null) {
			System.err.println(msg);//print the message (warning/no solution || no vul)
			return null;
		}

//		String s = sig.toString().replace(" ", "");
//		if(sig.contains(","))//otherwise, it's already clean.
		String s= StrUtil.sig_removecommas(sig.toString().replace(" ", ""));
//		path = Globals.SCPath+ subfolder+ "/"+ s +"/SSlices";
		if (sig.get_V() == 'Q' || sig.get_V() == 'q') {
			if (sig_equals_qv(s) == 1) {
				path = G.SCPath +"/"+ subfolder + "/q_s_v/SSlices";
			} else if (sig_equals_qv(s) == 2) {
				path = G.SCPath +"/"+ subfolder + "/q_iud_v/SSlices";
			} else if (sig_equals_qtc(sig) == 1) {// t. tc. c
				path = G.SCPath +"/"+ subfolder + "/q_s_t/SSlices";
			} else if (sig_equals_qtc(sig) == 2) {
				path = G.SCPath +"/"+ subfolder + "/q_iud_t/SSlices";
			}else if (sig.get_I().contains('a') ||sig.get_I().contains('A') ) {
				System.err.println(G.MSG_UsePS_all+" "+msg);
				return null;
			}else if (sig.get_I().contains('v') &&(sig.get_I().contains('c') ||sig.get_I().contains('t') ) ) {
				System.err.println(G.MSG_NoSol+" "+G.MSG_ct+" "+msg);
				return null;
			}
		} else // if()
		{// TODO : copy other cases code and test.
			// return null;
		}


		return path;
	}

	public static int sig_equals_qv(String sig) {
		String s = sig;

		if(s.equals(StrUtil.sig_removecommas(G.QSV)) ){
			return 1;
		}else if( s.equals(StrUtil.sig_removecommas(G.QIV))
				|| s.equals(StrUtil.sig_removecommas(G.QUV))
				||s.equals(StrUtil.sig_removecommas(G.QDV))) {
			return 2;
		}
		return 0;
	}

	public static int sig_equals_qtc(Sig s) {
		if ( !s.get_I().contains('v') && !s.get_I().contains('V')) {
			if (s.get_T() == 's') {
				return 1;
			} else if (s.get_T() == 'i' || s.get_T() == 'u' || s.get_T() == 'd') {
				return 2;
			}
		}

		return 0;
	}
	public static String get_subfolder(char V) {
		if(V == 'Q' || V =='q') {
			return "SQLIV";
		} else if(V == 'C' || V =='c') {
			return "CONN";
		} else if(V == 'E' || V =='e') {
			return "ENC";
		} else  if(V == 'P' || V =='p') {
			return "PW";
		} else {
			return null;
		}


	}


	public static boolean sig_equals_qcv(String sig) {
		String s = sig;

        return s.equals(StrUtil.sig_removecommas(G.QSV))
                || s.equals(StrUtil.sig_removecommas(G.QIV))
                || s.equals(StrUtil.sig_removecommas(G.QUV))
                || s.equals(StrUtil.sig_removecommas(G.QDV))
                || s.equals(StrUtil.sig_removecommas(G.Q_V));
    }
	public static Sig sign_slice_querystring(String path, String key, char dir, String query, boolean print, List<String>uiVars) throws IOException, JSQLParserException {
		qp = null;
		Sig s = new Sig(); // V,T,{I}

		String lines = read_lines(path);
//		classPrint = print;
//		System.out.println("LINES:\n"+lines);
		if(print) {
			System.out.println("query:" + query);
		}
		if(query!= null && query.length()>0) {
			qp = new QParser(query,print, uiVars);
			}
		{
		// Search for the keywords to sign the slice
			if(((key.equals("executeQuery") || key.equals("executeUpdate")) && (dir == 'b' || dir == 'B'))
					|| (key.equals("PreparedStatement") && (dir == 'f' || dir == 'F') )) {
				////System.out.println("BRANCH1");
				if(key.equals("executeQuery") && (qp.get_qs_ql().get_T()== 'i' || qp.get_qs_ql().get_T()== 'u' || qp.get_qs_ql().get_T()== 'd'))
				{
					//Flag as Syntax Error
					////System.out.println("BRANCH2");
					s = qp.get_qs_ql();
					s.set_V('S');//s.V = 'S';
					if(print)
					 {
						System.err.print(G.MSG_SyntaxError +G.SyntaxError_quey_wrong_fun);//TODO: include line number in the message
					}

				}

				if(key.equals("executeUpdate") && qp.get_qs_ql().get_T()== 's' )
				{
					////System.out.println("BRANCH3");
					//Flag as Syntax Error
					s = QParser.qs_ql;
					s.set_V('S');//s.V = 'S';
					if(print)
					 {
						System.err.print(G.MSG_SyntaxError+G.SyntaxError_quey_wrong_fun);//TODO: include line number in the message
					}
				}


				if(lines.contains("PreparedStatement") || lines.contains("prepareStatement")) {
					// Check if column/table name is the variable. To do so, parse and check the query
					////System.out.println("BRANCH4");
					if(qp.get_qs_ql().get_I().size()>0) {
						////System.out.println("BRANCH41");
						if(qp.get_qs_ql().get_I().contains('c') || qp.get_qs_ql().get_I().contains('t')) {
							////System.out.println("BRANCH4111");
							//Flag as Syntax Error
							s = qp.get_qs_ql();
							s.set_V('S');//s.V = 'S';
							if(print)
							 {
								System.err.print(G.MSG_SyntaxError+G.SyntaxError_tc_in_PS);//TODO: include line number in the message
							}
						}else {// PS was used correctly
							////System.out.println("BRANCH4112");
							if(print) {
								System.out.print("Temp Sig (user input is colomun value): " + qp.get_qs_ql().toString());
							}
							s.setvalues('_', '_', null);// No vulnerability
						}
					}else {// PS was used correctly
						////System.out.println("BRANCH42");
						if(print) {
							System.out.print("Temp Sig: " + qp.get_qs_ql().toString());
						}
						s.setvalues('_', '_', null);// No vulnerability
					}

					//System.out.println("###1 s: " + s.toString() + ", qs_ql: "+s.toString());
				}else {
					if (qp.get_allHasReplace() && qp.get_prefered_sol() == G.SEC && !qp.get_qs_ql().get_I().contains('v')
							&& (qp.get_qs_ql().get_I().contains('t') || qp.get_qs_ql().get_I().contains('c'))) {
						// WL correctly
						if (print) {
							System.err.print(qp.get_msg());
						}
//						s.setvalues('_', '_', null);// No vulnerability
						s = qp.get_qs_ql();
						s.set_V('_');
					} else {
						// //System.out.println("BRANCH5");
						// Q, ?, [c,t,v]||[c,v]||[t,v]-> No sol
						// Q,?,[v] || Q,?,[t] || Q,?,[c] || Q,?,[c,t]
						//Q,_,a
						s = qp.get_qs_ql();
						if (print) {
							System.err.print(qp.get_msg());
						}
						// System.out.println("###2 s: " + s.toString() + ", qs_ql: "+s.toString());
					}

//					else if (qp.get_qs_ql().get_I().contains('v')
//							&& (qp.get_qs_ql().get_I().contains('t') || qp.get_qs_ql().get_I().contains('c'))
//							&& qp.get_prefered_sol() == Globals.NOSOL) {
//						if (print) {
//							System.err.print(qp.get_msg());
//						}
//						s = qp.get_qs_ql();// Q, ?, [c,t,v]||[c,v]||[t,v]-> No sol
//					}
				}


			}else if(key.contains("getConnection") && (dir == 'b' || dir == 'B')) { //dir.toLowerCase().equals("b")


			}else if(key.contains("getConnection") && (dir == 'f' || dir == 'F')) {//dir.toLowerCase().equals("f")) {

			}
//			else if(key.contains("getConnection") && ( dir.toLowerCase().equals("bf") || dir.toLowerCase().equals("fb"))) {
//
//			}
			else if(key.contains("Properties") && (dir == 'f' || dir == 'F')) {//dir.toLowerCase().equals("f")) {

			}
		}

		return s;
	}




	public static Sig sign_slice_querystring_exp1_temp(String path, String key, char dir, String query, boolean print, List<String>uiVars) throws IOException, JSQLParserException {
		if(print) {
			System.out.println("*** in sign_slice_querystring_exp1_temp");
		}
		qp = null;
		Sig s = new Sig(); // V,T,{I}
		String lines = read_lines(path);
//		System.out.println("LINES:\n"+lines);
		if(print) {
			System.out.println("query:" + query);
		}
		if (query != null && query.length() > 0) {
			qp = new QParser(query, print, uiVars);
		}
		if (qp == null) {
			return null;
		}
		if (qp.parserFailed() && qp.get_qs_ql() == null || qp.get_qs_ql().get_V() == 'F') {
			s.setvalues('F', '_', null);
			return s;
		}
		{
		// Search for the keywords to sign the slice
			if((key.equals("executeQuery") || key.equals("executeUpdate")) && (dir == 'b' || dir == 'B')) {
				//System.out.println("BRANCH1");
				if(key.equals("executeQuery") && (qp.get_qs_ql().get_T()== 'i' || qp.get_qs_ql().get_T()== 'u' || qp.get_qs_ql().get_T()== 'd'))
				{
					//Flag as Syntax Error
					////System.out.println("BRANCH2");
					s = qp.get_qs_ql();
					s.set_V('S');//s.V = 'S';
					if(print)
					 {
						System.err.print(G.MSG_SyntaxError +G.SyntaxError_quey_wrong_fun);//TODO: include line number in the message
					}

				}

				if(key.equals("executeUpdate") && qp.get_qs_ql().get_T()== 's' )
				{
					////System.out.println("BRANCH3");
					//Flag as Syntax Error
					s = QParser.qs_ql;
					s.set_V('S');//s.V = 'S';
					if(print)
					 {
						System.err.print(G.MSG_SyntaxError+G.SyntaxError_quey_wrong_fun);//TODO: include line number in the message
					}
				}

//
//				if(lines.contains("PreparedStatement") || lines.contains("prepareStatement")) {
//					// Check if column/table name is the variable. To do so, parse and check the query
//					////System.out.println("BRANCH4");
//					if(qp.get_qs_ql().get_I().size()>0) {
//						////System.out.println("BRANCH41");
//						if(qp.get_qs_ql().get_I().contains('c') || qp.get_qs_ql().get_I().contains('t')) {
//							////System.out.println("BRANCH4111");
//							//Flag as Syntax Error
//							s = qp.get_qs_ql();
//							s.set_V('S');//s.V = 'S';
//							if(print)
//								System.err.print(G.MSG_SyntaxError+G.SyntaxError_tc_in_PS);//TODO: include line number in the message
//						}else {// PS was used correctly
//							////System.out.println("BRANCH4112");
//							if(print)
//								System.out.print("Temp Sig (user input is colomun value): " + qp.get_qs_ql().toString());
//							s.setvalues('_', '_', null);// No vulnerability
//						}
//					}else {// PS was used correctly
//						////System.out.println("BRANCH42");
//						if(print)
//							System.out.print("Temp Sig: " + qp.get_qs_ql().toString());
//						s.setvalues('_', '_', null);// No vulnerability
//					}
//
//					//System.out.println("###1 s: " + s.toString() + ", qs_ql: "+s.toString());
//				}else {
//					if (qp.get_allHasReplace() && qp.get_prefered_sol() == G.SEC && !qp.get_qs_ql().get_I().contains('v')
//							&& (qp.get_qs_ql().get_I().contains('t') || qp.get_qs_ql().get_I().contains('c'))) {
//						// WL correctly
//						if (print)
//							System.err.print(qp.get_msg());
//						s.setvalues('_', '_', null);// No vulnerability
//					} else {
//						// //System.out.println("BRANCH5");
//						// Q, ?, [c,t,v]||[c,v]||[t,v]-> No sol
//						// Q,?,[v] || Q,?,[t] || Q,?,[c] || Q,?,[c,t]
//						//Q,_,a
//						s = qp.get_qs_ql();
//						if (print) {
//							System.err.print(qp.get_msg());
//						}
//						// System.out.println("###2 s: " + s.toString() + ", qs_ql: "+s.toString());
//					}
//				}


			}else if(key.equals("prepareStatement") && (dir == 'f' || dir == 'F') ) {
				//System.out.println("BRANCH5");
				if(qp.get_qs_ql().get_I().size()>0) {
					//System.out.println("BRANCH41");
					if(qp.get_qs_ql().get_I().contains('c') || qp.get_qs_ql().get_I().contains('t')) {
						//System.out.println("BRANCH4111");
						//Flag as Syntax Error
						s = qp.get_qs_ql();
						s.set_V('S');//s.V = 'S';
						if(print)
						 {
							System.err.print(G.MSG_SyntaxError+G.SyntaxError_tc_in_PS);//TODO: include line number in the message
						}
					}else {// PS was used correctly
						//System.out.println("BRANCH4112");
						if(print) {
							System.out.print("Temp Sig (user input is colomun value): " + qp.get_qs_ql().toString());
						}
						s.setvalues('_', '_', null);// No vulnerability
					}
				}else {// PS was used correctly
					//System.out.println("BRANCH42");
					if(print) {
						System.out.print("Temp Sig: " + qp.get_qs_ql().toString());
					}
					s.setvalues('_', '_', null);// No vulnerability
				}
			}
			else if(key.contains("getConnection") && (dir == 'b' || dir == 'B')) { //dir.toLowerCase().equals("b")


			}else if(key.contains("getConnection") && (dir == 'f' || dir == 'F')) {//dir.toLowerCase().equals("f")) {

			}
//			else if(key.contains("getConnection") && ( dir.toLowerCase().equals("bf") || dir.toLowerCase().equals("fb"))) {
//
//			}
			else if(key.contains("Properties") && (dir == 'f' || dir == 'F')) {//dir.toLowerCase().equals("f")) {

			}
		}

		return s;
	}


	public static String read_lines(String path) throws IOException {
		String slice="";
		BufferedReader reader = null;
		File map_file = new File(path);
		if (map_file.exists()) {
			reader = new BufferedReader(new FileReader(map_file));
			String line = reader.readLine();
			while (line != null) {
				slice = slice + " " +line;
				line = reader.readLine();
			}
			reader.close();
		}

		return slice;

	}

//	public static String search_slice_key(String keys, String dir, String lines) {
//		String s="";
//		// SQLI
//		if(keys.contains("executeQuery") || keys.contains("executeUpdate")) {
//			if(lines.contains("prepareStatement")) {
//				s = s+"";
//			}
//		}else if(keys.contains("getConnection")) {
//
//
//		}
//		return s;
//	}
	// Parse the query and add last part to the query.

	public static void main(String[] args)  {
		String s = "p,d,[a,b,c]";
		Sig new_s = new Sig();
		Sig new_s2 = new Sig();
		new_s.convert_to_sig(s);
		new_s2 = new_s;
		System.out.println(new_s);
		System.out.println(new_s2);

	}

}
