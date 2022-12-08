package dcafixer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import flocalization.SignSlice;
import net.sf.jsqlparser.JSQLParserException;
import patchgenerator.AppMD;
import patchgenerator.PatternGen;
import patchgenerator.RplacePHs;
import slicer.datatypes.Sig;
import slicer.tool.EBCreation;
import slicer.tool.ExtractQuery;
import slicer.utilities.StrUtil;

/*
 * 
 * */
public class Experiment {
	static int Qparser = 0;// sig is null
	static int SEC_PS = 0;// -,-,null
	static int SEC_WL = 0;// -,?,[v,?]
	static int QV = 0; // Q,?,[v]
	static int QCT = 0; // Q,?,[c&|t]
	static int QA = 0; // Q,?,[a]
	static int SyntaxE_PS = 0;// S,?,[c&|t]
	static int SyntaxE_EU = 0;// S,?,[v]
	static int queryCount = 0;
	static int javaCount = 0;
	static List<String> failedCasesFiles = new ArrayList<>();
	static List<String> QVFiles = new ArrayList<>();
	public static void test_one(String dirPath, String file, boolean print) throws IOException, JSQLParserException {
		String path = dirPath + "/" + file;
		List<String> key_query = new ArrayList<>();
		//=================== Extractor 
		key_query = ExtractQuery.ExtractorExp1(path);
		
		if (key_query.size() > 0) {
			if(print)System.out.println("BRANCH 1");
			for (String kq : key_query) {
				String[] parts = kq.split(":");
				if (parts.length == 2) {
					if(print)System.out.println("BRANCH 2");
					String seed = parts[0];
					String query = parts[1];
					// Get signature
					Sig s = new Sig();
					boolean is_ps = false;
					//=================== Slice & Sign 
					if (seed.equals("prepareStatement")) {
						if(print)System.out.println("BRANCH 3 - PS");
						char sliceDirection = 'F';
						s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
								StrUtil.replace_questionmarks(query), print, null);
						is_ps = true;
						//System.out.println(s.toString());
					} else if (seed.equals("executeQuery") || seed.equals("executeUpdate")) {
						if(print)System.out.println("BRANCH 4 - EQ EU");
						char sliceDirection = 'B';
						s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
								StrUtil.replace_questionmarks(query), print, null);
					}
					// System.out.println(content +" : "+s.toString());
					//=================== Classify based on Signature
					if (SignSlice.qp.get_qs_ql() == null) {if(print) {
						System.err.println("Parser wasn't able to parse the query." + SignSlice.qp.get_msg());}
					} else if (s.get_V() == '_') {// -,-,null
						if(print)System.out.println("BRANCH 5 " + s.toString());
						if (s.get_T() == '_') {
							System.out.println("** Secure (PS is used): "+s.toString());// -,-,null
						} else {
							System.out.println("** Secure (WL is used): "+s.toString());// -,?,[v,?]
						}
					} else if (s.get_V() == 'q' || s.get_V() == 'Q') {
						if (SignSlice.qp.parserFailed()) {///+++++9/30
							Qparser++;
						}
						if (s.get_I().contains('v') && !s.get_I().contains('c') && !s.get_I().contains('t')) {
//				QV++; //Q,?,[v]
							System.out.println("** Column value is input: "+s.toString()); // Q,?,[v]
						} else if (!s.get_I().contains('v') && (s.get_I().contains('c') || s.get_I().contains('t'))) {
//				QCT = 0; //Q,?,[c&|t]
							System.out.println("** Column/Table name is input: "+s.toString()); // Q,?,[c&|t]
						} else if (s.get_I().contains('a')) {

							System.out.println("** All query is user input: "+s.toString()); // Q,?,[a]
						}
					} else if (s.get_V() == 'S') {
						if (is_ps) {
							System.out.println(
									"** Prepared stmt is used incorrectly (Column/Table name is user input)"+s.toString());// S,?,[c&|t]
						} else {
							System.out.println("** Wrong fucntion used with the query"+s.toString());// S,?,[v]
						}
					} else if (s.get_V() == 'F') {
//			Qparser++;
//			failedCasesFiles.add(content);
						
						System.out.println("**** Failed to parse the query:"+s.toString()+"\n" + query);
					}

				}
			}
		}

	}
	public static void collect_stat(Sig s, boolean print, boolean is_ps, String content) {
		if (SignSlice.qp == null) {
			if(print) {
				System.err.println("Parser wasn't able to parse the query." + SignSlice.qp.get_msg());}
			Qparser++;
		} else if (s.get_V() == '_') {// -,-,null
			if(print)System.out.println("BRANCH 5 " + s.toString());
			if (s.get_T() == '_') {
				if(print)System.out.println("BRANCH 5.1 " + s.toString());
				SEC_PS++;
			} else {
				if(print)System.out.println("BRANCH 5.2 " + s.toString());
				SEC_WL++;
			}
		} else if (s.get_V() == 'q' || s.get_V() == 'Q') {
			if(print)System.out.println("BRANCH 6 " + s.toString());
			if (SignSlice.qp.parserFailed()) {///+++++9/30
				Qparser++;
			}
			if (s.get_I().contains('v') && !s.get_I().contains('c') && !s.get_I().contains('t')) {
				if(print)System.out.println("BRANCH 6.1 " + s.toString());
				QV++; // Q,?,[v]
				QVFiles.add(content);
			} else if (!s.get_I().contains('v')
					&& (s.get_I().contains('c') || s.get_I().contains('t'))) {
				if(print)System.out.println("BRANCH 6.2 " + s.toString());
				QCT++; // Q,?,[c&|t]
			} else if (s.get_I().contains('a')) {
				if(print)System.out.println("BRANCH 6.3 " + s.toString());
				QA++; // Q,?,[a]
			}
		} else if (s.get_V() == 'S') {
			if(print)System.out.println("BRANCH 7 " + s.toString());
			if (is_ps) {
				if(print)System.out.println("BRANCH 7.1 " + s.toString());
				SyntaxE_PS++;// S,?,[c&|t]
			} else {
				if(print)System.out.println("BRANCH 7.2 " + s.toString());
				SyntaxE_EU++;// S,?,[v]
			}
		} else if (s.get_V() == 'F') {
			if(print)System.out.println("BRANCH 8 " + s.toString());
			Qparser++;
			failedCasesFiles.add(content);
			//System.out.println(content + ":\t" + query);
		}
	}
	public static void print_stats(String test, String sys) {
		System.out.println("Results of " + test + " cases that" + sys + " ( #queries: " + queryCount + "# Java files: "
				+ javaCount);
		// Qparser = queryCount - (SEC_PS + SEC_WL + QV + QCT + QA + SyntaxE_PS +
		// SyntaxE_EU);

		System.out.println("# Secure Cases (PS is used): " + SEC_PS);// -,-,null
		System.out.println("# Secure Cases (whitelisting is used): " + SEC_WL);// -,?,[v,?]
		System.out.println("# Vulnerable Cases: ");
		System.out.println("\t* Column value is input: " + QV); // Q,?,[v]
		System.out.println("\t* Column/Table name is input):" + QCT); // Q,?,[c&|t]
		System.out.println("\t* All query is user input: " + QA); // Q,?,[a]
		System.out.println("# SQL Syntax Error Cases: ");
		System.out.println("\t*Prepared statement is used incorrectly (Column/Table name is user input): " + SyntaxE_PS);// S,?,[c&|t]
		System.out.println("\t*Wrong fucntion used with the query: " + SyntaxE_EU);// S,?,[v]
		System.out.println("# Failed to parse the query: " + Qparser);// sig is 'F',?
		System.out.println(failedCasesFiles.toString());
//        failedCasesFiles
		// Get signature
	}
	//
	public static void test_fault_localization(String dirPath, String test, String sys, boolean print)
			throws Exception {

//		int tc = 0; //testCounter
		
		//System.out.println("*************************************");
//		String path = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Manual/After/after_1.java";
//		String path = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Manual/Before/before_1.java";
		File directoryPath = new File(dirPath);
		String[] contents = directoryPath.list();
//		long start = System.currentTimeMillis();
		float total =0;
		int number = 0;
		for (String content : contents) {
			number++;
			long start = System.currentTimeMillis();
			System.out.println(content);
//		String content = "Dummy_before_3477.java";
//		String content = "Dummy_before_2519.java";
			if (content.endsWith(".java") && content.startsWith("Dummy") 
					&& !content.contains("Dummy_before_X")) {
				javaCount++;	
//			String path = dirPath + "/" + content;
			String path = dirPath  + content;
//			loop over the files and for each file do:
			//	================= 1.	To slice find the IP line =================
			int v_lno = -1;
			char v_sliceDir = 'B';
			char v_sstype = 's';
			
			String v_seed="executeQuery";
			v_lno =StrUtil.findSeedLine(path, "executeQuery", 1);
			if(v_lno == -1) {
				v_seed="executeUpdate";
				v_lno = StrUtil.findSeedLine(path, "executeUpdate", 1);
			}
			if(v_lno == -1) {
				v_seed = "execute(";
				v_lno = StrUtil.findSeedLine(path, "execute(", 1);
			}
			if(v_lno == -1) {
				// No IP
				if(print)System.out.println("No IP");
//				continue;
			}
//			================= 2.	slice check that query is complete, path: /Users/Dareen/Fixer/tmp/TSet/Slices/AppVSlice =================

			String slices_and_context_path ="/Users/Dareen/Fixer/tmp/TSet/Slices/AppVSlice";
//			String md_file = "/"+content.replace(".java", "")+"_md.txt";
//			String v_slice_file = "/"+content.replace(".java", "")+"_VSlice.java";
//			String v_context_file = "/"+content.replace(".java", "")+"_context.txt";
			String md_file = "/exp3_md.txt";
			String v_slice_file = "/exp3_VSlice.java";
			String v_context_file = "/exp3_context.txt";
			String appJar = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/synthetic.jar";
			String mdPath = slices_and_context_path + md_file;// "/md.txt";// +
			String v_appSrc = dirPath + content;
			String v_slicePath = slices_and_context_path +v_slice_file;// "/VSlice.java";// + v_slice_file;
			String v_contextPath = slices_and_context_path + v_context_file;//"/context.txt";
			String[] v_subpath = v_appSrc.split("/");
			String v_classPath = v_subpath[v_subpath.length - 2] + "/";// TSet/
			String v_className_only = (v_subpath[v_subpath.length - 1].split("\\."))[0];// v_subpath[v_subpath.length - 1].replace(".java", "");
//			String v_path_className = v_classPath + v_className_only;
			String v_path_className = "datasets/Apps_Before/"+ v_className_only;
;
			///Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/Apps_Before
			if(print )System.out.println("v_path_className: "+ v_path_className);
			
			EBCreation.find_slice_and_context(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, v_seed, v_sstype,
					v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v');
			//========= test 10 cases
//			if(tc == 10)
//				break;
//			tc++;
//			================= 3.	sign and get SSlicesPath =================

			String SSlicesPath="";
			List<String> set_strings= new ArrayList<>();
			List<String> key_query = new ArrayList<>();
			key_query = ExtractQuery.ExtractorExp1(path);
			queryCount = queryCount + key_query.size();
			String s1 ="", s2="";
			//System.out.println("XXXX "+ExtractQuery.app_conn+","+ExtractQuery.app_rs+","+ ExtractQuery.app_sql+","+ ExtractQuery.app_ps);
			if (key_query.size() > 0) {
				if(print)System.out.println("BRANCH 1"+key_query.toString());
				for (String kq : key_query) {
					String[] parts = kq.split(":");
					if (parts.length == 2) {
						if(print)System.out.println("BRANCH 2");
						String seed = parts[0];
						String query = parts[1];
						// Get signature
						Sig s = new Sig();
						boolean is_ps = false;
						if (seed.equals("prepareStatement")) {
							if(print)System.out.println("BRANCH 3 - PS");
							char sliceDirection = 'F';
							s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
									StrUtil.replace_questionmarks(query), print, null);
							is_ps = true;
//						System.out.println(s.toString());
						} else if (seed.equals("executeQuery") || seed.equals("executeUpdate")) {
							if(print)System.out.println("BRANCH 4 - EQ EU");
							char sliceDirection = 'B';
//							List<String> ui = new ArrayList<>();
//							ui.add("colval");
							s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
									StrUtil.replace_questionmarks(query), print, null);
						}
						s1 = s.toString();
						
						collect_stat(s,  print,  is_ps,  content);
						// System.out.println(content +" : "+s.toString());
						//-------
						 SSlicesPath =SignSlice.get_slices_folder_path(s,"");
						 set_strings = SignSlice.qp.get_set_strings();
						 s2= SignSlice.qp.toString();
						 if(print) {
							 System.out.println("SSlicesPath: "+SSlicesPath);
							 System.out.println("~~~~~"+SignSlice.qp.get_pstmt_query());
							 System.out.println("~~~~~"+SignSlice.qp.get_set_strings().toString());
						}
						 
					}
				}
			}
			
			if(SSlicesPath == null) {
				System.err.println("SSlicesPath: is null due to: ("+s1 +"|"+s2+"), "+SignSlice.qp.get_msg());
				continue;
			}
			//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//			================= 4.	replace phs and create new SSlices in SSlicesPath+"/tmp” =================
			
			RplacePHs.replacePlaceHolders(v_slicePath, SSlicesPath, ExtractQuery.app_conn, 
					ExtractQuery.app_rs, ExtractQuery.app_sql, false);
//			================= 5.	call find diff all  =================
//			================= 6.	apply patches  =================
			 AppMD app_md = new AppMD(); 
		      app_md.app_sql = ExtractQuery.app_sql;
		      app_md.app_stmt = ExtractQuery.app_stmt;
		      app_md.ps_query =SignSlice.qp.get_pstmt_query();
		      app_md.wl_query = SignSlice.qp.getWL_query();
		      String setStrings ="";
		      boolean firstSet = true;
		      for(String set : SignSlice.qp.get_set_strings()) {
		    	  if(firstSet) {
		    		  firstSet = false;
		    	  setStrings = set;
		    			  }else {
		    				  setStrings = setStrings+"\n" + set; 
		    			  }
		      }
		      app_md.setStrings = setStrings;
		      PatternGen.findPatternsAndApply_exp3(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, false);
			//================= 7.	count Number of file that doesn’t have syntax error =================
			
		}
			long end = System.currentTimeMillis();
			float sec = (end - start) / 1000F;
			total = total + sec;
			float average = total / number;
			System.out.println("********* Elapsed time for one app: "+sec);
			System.out.println("********* Elapsed time for "+number+" apps: "+sec);
			System.out.println("********* Average elapsed time for "+number+" apps: "+average);
	}//End of for (contents)
		
		
			print_stats(test, sys);
		

	}

	
	public static void test_fault_localization_one(String className, String dirPath, String test, String sys, boolean print)
			throws Exception {

//		int tc = 0; //testCounter
		
		//System.out.println("*************************************");
//		String path = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Manual/After/after_1.java";
//		String path = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Manual/Before/before_1.java";
		File directoryPath = new File(dirPath);
		String[] contents = directoryPath.list();
		long start = System.currentTimeMillis();
			
			
		String content = className;;
		
		System.out.println(content);

			if (content.endsWith(".java") && content.startsWith("Dummy")) {
				javaCount++;
//			String path = dirPath + "/" + content;
			String path = dirPath  + content;
//			loop over the files and for each file do:
//	================= 1.	To slice find the IP line =================
			int v_lno = -1;
			char v_sliceDir = 'B';
			char v_sstype = 's';
			
			String v_seed="executeQuery";
			v_lno =StrUtil.findSeedLine(path, "executeQuery", 1);
			if(v_lno == -1) {
				v_seed="executeUpdate";
				v_lno = StrUtil.findSeedLine(path, "executeUpdate", 1);
			}
			if(v_lno == -1) {
				v_seed = "execute(";
				v_lno = StrUtil.findSeedLine(path, "execute(", 1);
			}
			if(v_lno == -1) {
				// No IP
				if(print)System.out.println("No IP");
//				continue;
			}
//			================= 2.	slice check that query is complete, path: /Users/Dareen/Fixer/tmp/TSet/Slices/AppVSlice =================

			String slices_and_context_path ="/Users/Dareen/Fixer/tmp/TSet/Slices/AppVSlice";
//			String md_file = "/"+content.replace(".java", "")+"_md.txt";
//			String v_slice_file = "/"+content.replace(".java", "")+"_VSlice.java";
//			String v_context_file = "/"+content.replace(".java", "")+"_context.txt";
			String md_file = "/exp3_md.txt";
			String v_slice_file = "/exp3_VSlice.java";
			String v_context_file = "/exp3_context.txt";
			String appJar = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/synthetic.jar";
			String mdPath = slices_and_context_path + md_file;// "/md.txt";// +
			String v_appSrc = dirPath + content;
			String v_slicePath = slices_and_context_path +v_slice_file;// "/VSlice.java";// + v_slice_file;
			String v_contextPath = slices_and_context_path + v_context_file;//"/context.txt";
			String[] v_subpath = v_appSrc.split("/");
			String v_classPath = v_subpath[v_subpath.length - 2] + "/";// TSet/
			String v_className_only = (v_subpath[v_subpath.length - 1].split("\\."))[0];// v_subpath[v_subpath.length - 1].replace(".java", "");
//			String v_path_className = v_classPath + v_className_only;
			String v_path_className = "datasets/Apps_Before/"+ v_className_only;
;
			///Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/Apps_Before
			if(print )System.out.println("v_path_className: "+ v_path_className);
			
			EBCreation.find_slice_and_context(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, v_seed, v_sstype,
					v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v');
			//========= test 10 cases
//			if(tc == 10)
//				break;
//			tc++;
//			================= 3.	sign and get SSlicesPath =================

			String SSlicesPath="";
			List<String> set_strings= new ArrayList<>();
			List<String> key_query = new ArrayList<>();
			key_query = ExtractQuery.ExtractorExp1(path);
			queryCount = queryCount + key_query.size();
			String s1="", s2="";
			//System.out.println("XXXX "+ExtractQuery.app_conn+","+ExtractQuery.app_rs+","+ ExtractQuery.app_sql+","+ ExtractQuery.app_ps);
			if (key_query.size() > 0) {
				if(print)System.out.println("BRANCH 1"+key_query.toString());
				for (String kq : key_query) {
					String[] parts = kq.split(":");
					if (parts.length == 2) {
						if(print)System.out.println("BRANCH 2");
						String seed = parts[0];
						String query = parts[1];
						// Get signature
						Sig s = new Sig();
						boolean is_ps = false;
						if (seed.equals("prepareStatement")) {
							if(print)System.out.println("BRANCH 3 - PS");
							char sliceDirection = 'F';
							s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
									StrUtil.replace_questionmarks(query), print, null);
							is_ps = true;
//						System.out.println(s.toString());
						} else if (seed.equals("executeQuery") || seed.equals("executeUpdate")) {
							if(print)System.out.println("BRANCH 4 - EQ EU");
							char sliceDirection = 'B';
							List<String> ui = new ArrayList<>();
							ui.add("colval");
							s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
									StrUtil.replace_questionmarks(query), print, ui);
						}
						s1= s.toString();
						collect_stat(s,  print,  is_ps,  content);
						// System.out.println(content +" : "+s.toString());
						//-------
						 SSlicesPath =SignSlice.get_slices_folder_path(s,"");
						 set_strings = SignSlice.qp.get_set_strings();
						 if(print) {
							 System.out.println("SSlicesPath: "+SSlicesPath);
							 System.out.println("~~~~~"+SignSlice.qp.get_pstmt_query());
							 System.out.println("~~~~~"+SignSlice.qp.get_set_strings().toString());
						}
						 s2= SignSlice.qp.toString();
//						 if(SSlicesPath == null) {
//								System.err.println("SSlicesPath: is null due to: ("+s.toString()+"), "+SignSlice.qp.get_msg());
//								continue;
//							}
					}
				}
			}
			
			if(SSlicesPath == null) {
				System.err.println("SSlicesPath: is null due to: ("+s1 +"|"+s2+"), "+SignSlice.qp.get_msg());
				
			}else {
//			================= 4.	replace phs and create new SSlices in SSlicesPath+"/tmp” =================
			RplacePHs.replacePlaceHolders(v_slicePath, SSlicesPath, ExtractQuery.app_conn, 
					ExtractQuery.app_rs, ExtractQuery.app_sql, false);
//			================= 5.	call find diff all  =================
//			================= 6.	apply patches  =================
			 AppMD app_md = new AppMD(); 
		      app_md.app_sql = ExtractQuery.app_sql;
		      app_md.app_stmt = ExtractQuery.app_stmt;
		      app_md.ps_query =SignSlice.qp.get_pstmt_query();
		      app_md.wl_query = SignSlice.qp.getWL_query();
		      String setStrings ="";
		      boolean firstSet = true;
		      for(String set : SignSlice.qp.get_set_strings()) {
		    	  if(firstSet) {
		    		  firstSet = false;
		    	  setStrings = set;
		    			  }else {
		    				  setStrings = setStrings+"\n" + set; 
		    			  }
		      }
		      app_md.setStrings = setStrings;
		      PatternGen.findPatternsAndApply_exp3(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, false);
//			================= 7.	count Number of file that doesn’t have syntax error =================
			}
		}
		long end = System.currentTimeMillis();
		long elapsedTime = end - start;
		double timeSec =  (elapsedTime*1.0/1000)%60;
		System.out.println("********* Elapsed time for EXP3:"+timeSec);
		//print_stats(test, sys);
		

	}

	public static void dcafixer_performance_dataset3() throws Exception {

//		File file1 = new File("/Users/Dareen/Desktop/DCAFixer_Experimets/Exp3_Before_results1.txt");
//		File file2 = new File("/Users/Dareen/Desktop/DCAFixer_Experimets/Exp3_Before_results2.txt");
//		File file3 = new File("/Users/Dareen/Desktop/DCAFixer_Experimets/Exp3_Before_results3.txt");
////	    PrintStream tofile = new PrintStream(new File("/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Synthetic/dcafixer.sql"));
//		PrintStream tofile1 = new PrintStream(file1);
//		PrintStream tofile2 = new PrintStream(file2);
//		PrintStream tofile3 = new PrintStream(file3);
//		PrintStream console = System.out;

		// --- Before
		// --- SQLIFix After
		// --- DCAFixer After
		// Create Jar and try to slice.
		// Wasn't able to build a jar file.
		// Read the files
//		System.setOut(tofile1);
//		String dirPath = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Manual/Before";
//		test_fault_localization(dirPath, "\"before\"", "are not fixed yet", false);
////		System.setOut(console);
//		System.out.println("Done with exp 1: Before !");
//
////		System.setOut(tofile2);
//		String dirPath2 = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Manual/After";
//		test_fault_localization(dirPath2, "\"after\"", "are not fixed by SQLIFix", false);
////		System.setOut(console);
//		System.out.println("Done with exp 2: After SQLIFIX !");
//		
//		String dirPath3 ="/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Synthetic/Apps_Before";
		String dirPath3 ="/Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/Apps_Before/";
		test_fault_localization(dirPath3, "\"SYNTHETIC\"", "are not fixed by SQLIFix", false);
//		test_fault_localization_one("Dummy_before_3037.java",dirPath3, "\"SYNTHETIC\"", "are not fixed by SQLIFix", true);
//		String content = "Dummy_before_2519.java";//"Dummy_before_3477.java";
	}

	public static void dcafixer_performance_dataset4() {

	}

	public static void main(String[] args) throws Exception {
		// TODO Call all functions you created
		// EBCreation
		// Test on one case
		// Experiments
		Experiment.dcafixer_performance_dataset3();
//		
//		test_one("/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFIX-main/Data Set/Manual/After", "after_120.java", true);
	}

}
