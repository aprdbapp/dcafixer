package dcafixer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.shrike.shrikeCT.*;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import flocalization.SignSlice;
import net.sf.jsqlparser.JSQLParserException;
import patchgenerator.AppMD;
import patchgenerator.PatternGen;
import patchgenerator.RplacePHs;
import queryparser.QParser;
import slicer.datatypes.Sig;
import slicer.datatypes.SliceLine;
import slicer.tool.EBCreation;
import slicer.tool.ExtractQuery;
import slicer.tool.Info;
import slicer.tool.SlicerTool;
import slicer.utilities.StrUtil;

public class Experiment2_FL_Detection {
//	static boolean Print = false;
	//==============
	static int GFailedButSigned = 0;// sig is NOT null but parser failed
	static int GFailedQparser = 0;// sig is null
	static int GSEC_PS = 0;// -,-,null
	static int GSEC_WL = 0;// -,?,[v,?]
	static int GSEC_CONS = 0; // Q, ? , [-]
	static int GQV = 0; // Q,?,[v]
	static int GQCT = 0; // Q,?,[c&|t]
	static int GQA = 0; // Q,?,[a]
	static int GSyntaxE_PS = 0;// S,?,[c&|t]
	static int GSyntaxE_EU = 0;// S,?,[v]
	//==============
	static int FailedButSigned = 0;// sig is NOT null but parser failed
	static int FailedQparser = 0;// sig is null
	static int SEC_PS = 0;// -,-,null
	static int SEC_WL = 0;// -,?,[v,?]
	static int SEC_CONS = 0; // Q, ? , [-]
	static int QV = 0; // Q,?,[v]
	static int QCT = 0; // Q,?,[c&|t]
	static int QA = 0; // Q,?,[a]
	static int SyntaxE_PS = 0;// S,?,[c&|t]
	static int SyntaxE_EU = 0;// S,?,[v]
	
	static List<String> failedCasesFiles = new ArrayList<>();
	static List<String> QVFiles = new ArrayList<>();
	
	public static void clearCounters() {
		 FailedButSigned = 0;
		 FailedQparser = 0;// sig is null
		 SEC_PS = 0;// -,-,null
		 SEC_WL = 0;// -,?,[v,?]
		 SEC_CONS = 0;
		 QV = 0; // Q,?,[v]
		 QCT = 0; // Q,?,[c&|t]
		 QA = 0; // Q,?,[a]
		 SyntaxE_PS = 0;// S,?,[c&|t]
		 SyntaxE_EU = 0;// S,?,[v]

	}
	
	public static void clearGloCounters() {
		GFailedButSigned = 0;
		 GFailedQparser = 0;// sig is null
		 GSEC_PS = 0;// -,-,null
		 GSEC_WL = 0;// -,?,[v,?]
		 GQV = 0; // Q,?,[v]
		 GQCT = 0; // Q,?,[c&|t]
		 GQA = 0; // Q,?,[a]
		 GSyntaxE_PS = 0;// S,?,[c&|t]
		 GSyntaxE_EU = 0;// S,?,[v]

	}
	
	public static void addCountersto_GCounters() {
		GFailedQparser = GFailedQparser + FailedQparser;// sig is null
		GSEC_PS = GSEC_PS+ SEC_PS;// -,-,null
		GSEC_WL = GSEC_WL + SEC_WL;// -,?,[v,?]
		GSEC_CONS = GSEC_CONS + SEC_CONS;
		GQV = GQV + QV; // Q,?,[v]
		GQCT = GQCT+ QCT; // Q,?,[c&|t]
		GQA = GQA+ QA; // Q,?,[a]
		GSyntaxE_PS = GSyntaxE_PS + SyntaxE_PS;// S,?,[c&|t]
		GSyntaxE_EU = GSyntaxE_EU + SyntaxE_EU;// S,?,[v]

	}
	
	public static void print_file_results(String filePath) {
		System.out.print("Path: "+ filePath);
		int V=0, S=0; 
		System.out.println("\nFailedQparser  "+ FailedQparser +
				"\nFailedButSigned: "+ FailedButSigned+
				"\nSEC_PS  "+ SEC_PS+		
				"\nSEC_WL  "+ SEC_WL+
				"\nSEC_CONS  "+ SEC_CONS+
				"\nV - QV  "+ QV +	
				"\nV - QCT  "+ QCT+
				"\nV - QA  "+ QA+
				"\nSyntaxE_PS  "+ SyntaxE_PS +
				"\nSyntaxE_EU  "+ SyntaxE_EU +
				"\nSecure: " +(SEC_PS+SEC_WL + SEC_CONS)+
				"\nVulnerable: "+(QV+ QCT+ QA)+
				"\n***************************************************************\n");

	}
	public static void print_all_results(String projectName) {
		System.out.println("************ "+projectName+ " Stats ************" +
		"\nGFailedButSigned: "+ GFailedButSigned+
		"\nGFailedQparser  "+ GFailedQparser +		
		"\nGSEC_PS  "+ GSEC_PS+	
		"\nGSEC_WL  "+ GSEC_WL+
		"\nGSEC_CONS  "+ GSEC_CONS+
		"\nGQV  "+ GQV +		
		"\nGQCT  "+ GQCT+
		"\nGQA  "+ GQA+
		"\nGSyntaxE_PS  "+ GSyntaxE_PS +		
		"\nGSyntaxE_EU  "+ GSyntaxE_EU +
		"\nSecure: " +(GSEC_PS+GSEC_WL+GSEC_CONS)+
		"\nVulnerable: "+(GQV+ GQCT+ GQA)+
		"\n***************************************************************\n");
		clearGloCounters();

	}
	public static void collect_stat(Sig s, boolean print, boolean is_ps, String content) {
		if (s == null) {
			if(print) {
				System.err.println("Parser wasn't able to parse the query." + SignSlice.qp.get_msg());}
			FailedQparser++;
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
//			if (SignSlice.qp.parserFailed()) {///+++++9/30
			if (QParser.ParserFail ) {//|| SignSlice.qp.parserFailed()) {///+++++9/30
				FailedButSigned++;
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
			}else if (s.get_I().contains('-')) {
				if(print)System.out.println("BRANCH 9 " + s.toString());
				SEC_CONS++;
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
			FailedQparser++;
			failedCasesFiles.add(content);
			//System.out.println(content + ":\t" + query);
		}
	}

	public static boolean has_ps (List<SliceLine> sliceLines) {
		for (SliceLine sl : sliceLines) {
			if(sl.stmt.contains("prepareStatement")) {
				return true;
			}
		}
		return false;
	}
	
	
	
	public static void fault_localization(String appJar,String filePath,  String projName, boolean print ) throws Exception {
		// ========= Collect: path , classname, DN, DP, time to locate one vul/all vuls, time to fix one
		// 	1- Time to locate a vul, and fix
		long start_locate_one_vul = System.currentTimeMillis();
		// 	2- TP, TN, FP, FN
		// P = 
		clearCounters();
		// ============ DCAFIXER STEPS:
		// ================= 1.	Find the IP lines =================
		// -------- Use Extractor, then loop over IPs
		System.out.println(filePath);
		
		String file_name = StrUtil.get_filename(filePath);
		System.out.println("--- " + file_name +" ---");
		String file_folder = StrUtil.get_folder_path(filePath);// *
		ExtractQuery.Extractor(file_folder, file_name, 0);
//		System.out.println("*** assignList ***\n"+ExtractQuery.assignList.toString());
//		System.out.println("*** assignListCalls ***\n"+ExtractQuery.assignListCalls.toString());
//		System.out.println("*** infoSet ***\n"+ExtractQuery.infoSet.toString());
//		System.out.println("=====================================\n");
		// ================= 2.	Slice & Sign =================

		for(Info info : ExtractQuery.infoSet) {
			
		//	--------------- 2.1)	slice check that query is complete, path: /Users/Dareen/Fixer/tmp/TSet/Slices/AppVSlice =================
			int v_lno = info.getLno();
			String slices_and_context_path ="/Users/Dareen/Desktop/DCAFixer_Experimets/Slices/"+projName;
			String md_file = "/"+file_name.replace(".java", "")+"_md.txt";
			String v_slice_file = "/"+file_name.replace(".java", "")+v_lno+"_VSlice.java";
			String v_context_file = "/"+file_name.replace(".java", "")+"_context.txt";
//			String md_file = "/exp4_md.txt";
//			String v_slice_file = "/"+file_name+v_lno+"_VSlice.java";
//			String v_context_file = "/exp4_context.txt";
			String mdPath = slices_and_context_path + md_file;// "/md.txt";// +
			String v_appSrc = filePath;//file_folder + "/" + file_name;
			String v_slicePath = slices_and_context_path +v_slice_file;// "/VSlice.java";// + v_slice_file;
			String v_contextPath = slices_and_context_path + v_context_file;//"/context.txt";
			String[] v_subpath = v_appSrc.split("/");
			String v_classPath = v_subpath[v_subpath.length - 2] + "/";// TSet/
			String v_className_only = (v_subpath[v_subpath.length - 1].split("\\."))[0];// v_subpath[v_subpath.length - 1].replace(".java", "");
//			String v_path_className = "com/revature/" + v_className_only;
//			String v_path_className = "src/main/java/com/revature/"+ v_className_only;
//			String v_path_className = "DCAFixer_FuelOrdersClient/src/fuelordersclient/"+ v_className_only;
			
//			String v_path_className = "Online-Book-Store-System-master/src/workspace/"+ v_className_only;
//			String v_path_className = "workspace/"+ v_className_only;;
//			String v_path_className ="Ecomerce/src/ecomerce/";
			
//			String v_path_className ="banking_app/"+v_className_only;
//			String v_path_className =v_classPath +v_className_only;
			//-----------------------
			String v_path_className ="";
			boolean src_found = false;
			for(int i =0; i< v_subpath.length-1;i++) {
				if(src_found)
					v_path_className = v_path_className +v_subpath[i] + "/";
				if(v_subpath[i].equals("src"))
					src_found = true;
			}
			if(!src_found)
				System.err.println("NO \"src\" folder in the path");
			v_path_className = v_path_className + v_className_only;
			//-----------------------
			
	
			if(print )
				System.out.println("v_path_className: "+ v_path_className +", lno:" + v_lno+ " key: "+info.getKey() + "\n Slice path: "+ v_slice_file);
			
			boolean failToParse =false , allQueryisUI =false,codeIsSecure = false, query_is_constant = false; 
			String key = info.getKey();
			Sig s = new Sig();	

//			--------------- 2.2) Sign to mark SQL Vuls ---------
			List<SliceLine> sliceLines = new ArrayList<>();
			boolean is_ps= false;
			if(!(key.contains("executeUpdate") || key.contains("executeQuery") ||key.contains("execute")) ){
				continue;
			}else {
				
				System.out.println("HERE 1!");
				char v_sliceDir = 'B';
				char v_sstype = 's';
				if(info.getQuery().equals("ps")) {
					// Slice to find ps line# from the slice
					if (print)System.out.println("HERE 1!");//,   "+ info.toString());
					s.setvalues('_', '_', null); //is_ps = true;
					/*find_slice_and_context(String appJar, String appSrc, String app_folder_className, char sliceDirection, int lno, String seed,
			char sstype, String slicePath, String contextPath, String mdPath,DataDependenceOptions dOptions,
			ControlDependenceOptions cOptions, char appType)*/
					//===================================
//					if (print) System.out.println("***** HERE 1.1");
//					
//					sliceLines = EBCreation.find_slice_and_context_query_sent(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
//							v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', null);
//					
//					
//					if(has_ps(sliceLines)) {
//						s.setvalues('_', '_', null);// No vulnerability
//						is_ps = true;
//					}else {
//						//F,_,_ TODO: create a signature
//						// Fail to parse query
//						s.setvalues('F', '_', null);
//						failToParse = true;
//					}
//					// TODO: Search the infoSet for the corresponding ps by line#
					//===================================
				}else if(info.getQuery().equals("parameter")) {
					
					if (print) System.out.println("HERE 2!");//,   "+ info.toString());
					if(ExtractQuery.is_methodArg(info.getArg())){
						List<Character> Is = new ArrayList<>();
						Is.add('a');
						s.setvalues('Q', '_', Is);
					}
					//================
//					//Check if parameter is a var in a method or call to for example, scanner
////					in
//					// All query is an arg
//					// slice to check if the value in the other function is constant
//					// parse and check if there is a value for the query
//					sliceLines = EBCreation.find_slice_and_context_query_sent(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
//							v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery());
//					if(has_ps(sliceLines)) {
//						
//						s.setvalues('_', '_', null);// No vulnerability
//						codeIsSecure = true;
//					}else {
//						//F,_,_ TODO: create a signature
//						// Fail to parse query
//						List<String> uiVar = new ArrayList<>();
//						uiVar.add(info.getArg());
//						QParser.parse_query(null, print, uiVar);
//						List<Character> Is = new ArrayList<>();
//						Is.add('a');
//						s.setvalues('Q', '_', Is);
//						allQueryisUI =true;
//						System.out.println("s: " + s.toString() + ", QParser.qs_ql: "+ QParser.qs_ql.toString());
//					}
					//================
					
					
				}else if(info.getQuery().equals(null) || info.getQuery().equals("")  ) {
					if (print) System.out.println("HERE 3!,   ");//+ info.toString());
					// TODO: create a signature F,_, _ failed to parse it
					//- Try from the slice
					s.setvalues('F', '_', null);
					failToParse = true;
				}else {
//					if (print) 
						System.out.println("HERE 4!");//,   "+ info.toString());
					// There is a complete query
					// ---- Sent directly 
					// ---- Stored in an arg
//					if(!info.getArg().equals(null) && !info.getArg().equals("")) {
//						
//					}
//					sliceLines = EBCreation.find_slice_and_context_query_sent(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
//							v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery());
					if(has_ps(sliceLines) ) {
						s.setvalues('_', '_', null);// No vulnerability
						codeIsSecure = true;
					}else {
						
						QParser.parse_query(info.getQuery(), false,null);
						s = QParser.qs_ql;

//						if(s.equals(null)) {
//						String q= info.getQuery().replace("\\s+", "");
//						if(q.startsWith("\"") && q.endsWith("\"")  &&( q.contains("\"+") || q.contains("\" +"))) {
//							s.setvalues('_', '_', null);
//						}	}
					//======================
//						System.out.println("HERE 4.1!" + info.getQuery());
////					s = SignSlice.sign_slice_querystring_exp1_temp(v_slicePath,key, v_sliceDir, info.getQuery(), false, null );
//						sliceLines = EBCreation.find_slice_and_context_query_sent(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
//								v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', null);
//						
//					if(s.get_I().get(0).equals('-')) {
//						query_is_constant = true;
//						System.out.println(info.toString() +"\n"+s.toString() + "\n&&&&fffffffff&&&&\n");
//					}
////					String path, String key, char dir, String query, boolean print, List<String>uiVars
					//======================
					}	
				}
//				if(s != null) {	
				collect_stat(s,false, is_ps,filePath);// Secure and failed cases signatures are to collect stats
				if (print) 
					System.out.println(info.toString() +"\n"+s.toString() + "\n&&&&&&&&&&&&&&&&\n");
//				}	
			}// --- end of EQ, EU, and E
			
//			if(sliceLines.size()>0)
//				System.out.println(sliceLines.toString());
//				for(SliceLine l :sliceLines )System.out.println(l.toString());
			// TODO: --------- Connections Vuls ---------
			if(key.equals("getConnection")) {
				
			}
			
//			//TODO: collect stats here and add counters
////			collect_stat(s,false, is_ps,filePath);// Secure and failed cases signatures are to collect stats
////			================= 3.	get SSlicesPath =================
//			//===== After reading one vulnerability , we try to fix one at a time.
//			if(!(failToParse || allQueryisUI|| codeIsSecure || query_is_constant  ) && s != null ) {
//			List<String> set_strings = new ArrayList();
//			String SSlicesPath=SignSlice.get_slices_folder_path(s,"");
//			SSlicesPath = SignSlice.get_slices_folder_path_exp4(s,"");
//			 set_strings = QParser.set_strings;//SignSlice.qp.get_set_strings();
////			 String s2= QParser.qs_ql.toString();//SignSlice.qp.toString();
//			 if(print) {
//				 System.out.println("SSlicesPath: "+SSlicesPath);
////				 System.out.println("~~~~~"+SignSlice.qp.get_pstmt_query());
////				 System.out.println("~~~~~"+SignSlice.qp.get_set_strings().toString());
//			}
//			 if(SSlicesPath == null) {
//					System.err.println("SSlicesPath: is null due to: ("+s.toString());// +"|"+s2+"), "+s2);
//					//continue;
//			}
//			 //TODO: from the patch get the number of the new fixed "execute" call. 
//			 //		This will help to check if the new code is fixed
//			 
////				================= 4.	replace phs and create new SSlices in SSlicesPath+"/tmp” =================
//				
//				RplacePHs.replacePlaceHolders(v_slicePath, SSlicesPath, info.getApp_conn(), 
//						info.getApp_rs(), info.getArg(), false);
////				================= 5.	call find diff all  =================
////				================= 6.	apply patches  =================
//				 AppMD app_md = new AppMD(); 
//			      app_md.app_sql = info.getArg();
//			      app_md.app_stmt = info.getStmtVar();
//			      app_md.ps_query = QParser.PS_query;//SignSlice.qp.get_pstmt_query();
//			      app_md.wl_query = QParser.WL_query;//SignSlice.qp.getWL_query();
//			      String setStrings ="";
//			      boolean firstSet = true;
//			      for(String set : QParser.set_strings ) {//SignSlice.qp.get_set_strings()) {
//			    	  if(firstSet) {
//			    		  firstSet = false;
//			    	  setStrings = set;
//			    			  }else {
//			    				  setStrings = setStrings+"\n" + set; 
//			    			  }
//			      }
//			      app_md.setStrings = setStrings;
//			      if (QParser.prefered_sol == 1)
//			      PatternGen.findPatternsAndApply_exp4(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, info.getQuery_lines(), QParser.PS_query, false);
//			      else if (QParser.prefered_sol == 2)
//			    	  PatternGen.findPatternsAndApply_exp4(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, info.getQuery_lines(), QParser.WL_query, true);	  
//				//================= 7.	count Number of file that doesn’t have syntax error =================
//				
//			
//			}else
//				System.out.println("s in Null");
//			
//			 failToParse =false ; allQueryisUI =false;codeIsSecure = false; 
//			 codeIsSecure = false;
			 //************************************
			


				
		}// ---------- END of loop over infoSet
		print_file_results(filePath);
		addCountersto_GCounters();
		//TODO print Stats
		
		
		
	}
	
	public static void main(String[] args) throws Exception {
		String CsvDir = "/Users/Dareen/Desktop/DCAFixer_Experimets/Csv_files/";
		int N, P;
		 long start = System.currentTimeMillis();
//		String proj1Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src";
////		String proj1Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app.jar";
//		String proj1Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app-devkala48-masterjar.jar";
////				"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/temp/com/revature/banking_app.jar";
//		String proj1Csv = CsvDir + "banking-app.csv";
//		 P= 4; N = 11;		
//		Experimnet2_PIs.analyze_project(proj1Jar, proj1Src, proj1Csv, false, P, N);
//		System.out.println(Experimnet2_PIs.SCList.toString());
//		Experimnet2_PIs.printSCList_dir_className();
//		for (String f: Experimnet2_PIs.SCList ) {
//			//(proj1Csv.split("/")[(proj1Csv.split("/")).length - 1].split("\\."))[0]
//			fault_localization(proj1Jar, f, "banking-app", true);
//		}
		
		// XX ---------------------- "banking-app"  ----------------
//		String proj1Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src";
//		String proj1Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app.jar";
//		String proj1Csv = CsvDir + "banking-app.csv";
//		P= 4; N = 11; 
//		Experimnet2_PIs.analyze_project(proj1Jar, proj1Src, proj1Csv, false, P, N);
//		String proj1Name = "banking-app";
//		for (String f: Experimnet2_PIs.SCList ) {
//			//(proj1Csv.split("/")[(proj1Csv.split("/")).length - 1].split("\\."))[0]
//			fault_localization(proj1Jar, f, proj1Name, false);
//		}
//		print_all_results(proj1Name);
		
		// XX ---------------------- BookStore ----------------
//		String proj2Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace";
//		String proj2Jar= //"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/BookStore.jar";
//				"/Users/Dareen/Fixer/Experiments/JARS/Online-Book-Store-System-master.jar";
//		String proj2Csv = CsvDir + "BookStore.csv";
//		P= 46; N = 0;
//		String proj2Name = "BookStore";
//		Experimnet2_PIs.analyze_project(proj2Jar, proj2Src, proj2Csv, false, P, N);
//		for (String f: Experimnet2_PIs.SCList ) {
//			//(proj1Csv.split("/")[(proj1Csv.split("/")).length - 1].split("\\."))[0]
//			fault_localization(proj2Jar, f, proj2Name, false);
//		}
//		print_all_results(proj2Name);
		
		// XX UNREACHABLE ---------------------- crawledemo_property ---------------- it has a syntax error
//		/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/crawledemo-master/property/
//		String proj3Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/crawledemo-master/property/src/com/property";
//		String proj3Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/crawledemo_property.jar";
//		String proj3Csv = CsvDir + "crawledemo_property.csv";
//		P= 3; N = 0; 
//		String proj3Name = "crawledemo_property";
//		Experimnet2_PIs.analyze_project(proj3Jar, proj3Src, proj3Csv, false, P, N);
//		for (String f: Experimnet2_PIs.SCList ) {
//			//(proj1Csv.split("/")[(proj1Csv.split("/")).length - 1].split("\\."))[0]
//			fault_localization(proj3Jar, f, proj3Name, false);
//		}
//		print_all_results(proj3Name);
		
		// XX UNREACHABLE , but w/ AllApplicationEntrypoints it's Line ---------------------- Ecomerce ----------------
//		String proj4Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/Ecomerce--master/Ecomerce/src/ecomerce";
//		String proj4Jar= //"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/Ecomerce.jar";
//				"/Users/Dareen/Fixer/Experiments/JARS/Ecomerce.jar";
//		String proj4Csv = CsvDir + "Ecomerce.csv";
//		P= 79; N = 15;
//		String proj4Name = "Ecomerce";
//		Experimnet2_PIs.analyze_project(proj4Jar, proj4Src, proj4Csv, false, P, N);
//
//		for (String f: Experimnet2_PIs.SCList ) {
//			fault_localization(proj4Jar, f, proj4Name, false);
//		}
//		print_all_results(proj4Name);

		// XX Line then (find_targeted_method )finding method "run" , nested classes ---------------------- fiftyfiftystockscreener ----------------
//		String proj5Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/fiftyfiftystockscreener-master/src/com/eddiedunn";
//		String proj5Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/fiftyfiftystockscreener.jar";
//		String proj5Csv = CsvDir + "fiftyfiftystockscreener.csv";
//		P= 32; N = 4; 
//
//		String proj5Name = "fiftyfiftystockscreener";
//		Experimnet2_PIs.analyze_project(proj5Jar, proj5Src, proj5Csv, false, P, N);
//
//		for (String f: Experimnet2_PIs.SCList ) {
//			fault_localization(proj5Jar, f, proj5Name, false);
//		}
//		print_all_results(proj5Name);
		

		// UNREACHABLE , with All line! ---------------------- GUI-DBMS ----------------
//		String proj6Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/GUI-DBMS-master/TableEasy/src/tableeasy";
//		String proj6Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/GUI-DBMS.jar";
//		String proj6Csv = CsvDir + "GUI-DBMS.csv";
//		P= 33; N = 20; 
//		String proj6Name = "GUI-DBMS";
//		Experimnet2_PIs.analyze_project(proj6Jar, proj6Src, proj6Csv, false, P, N);
//
//		for (String f: Experimnet2_PIs.SCList ) {
//			fault_localization(proj6Jar, f, proj6Name, false);
//		}
//		print_all_results(proj6Name);


		// UNREACHABLE or endless loop ----------------------  InventarioWeb  ----------------
//		String proj7Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/InventarioWeb-master/src/java";
//		String proj7Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/InventarioWeb.jar";
//		String proj7Csv = CsvDir + "InventarioWeb.csv";
//		P= 1; N = 31; 
//		
//		String proj7Name = "InventarioWeb";
//
//		Experimnet2_PIs.analyze_project(proj7Jar, proj7Src, proj7Csv, false, P, N);
//		for (String f: Experimnet2_PIs.SCList ) {
//			fault_localization(proj7Jar, f, proj7Name, true);
//		}
//		print_all_results(proj7Name);
		
		// Done ---------------------- JavaGit-master ----------------
//		String appJar = "/Users/Dareen/Fixer/Experiments/JARS/JavaGit-master.jar";
//		String appSrc = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/JavaGit-master/src/gs/veepeek/assessment/Assesment.java";
//		String classPath = "gs/veepeek/assessment/Assesment";
//
//		String proj8Src = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/JavaGit-master/src";
//		String proj8Jar = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/JavaGit-master.jar";
//		String proj8Csv = CsvDir + "JavaGit-master.csv";
//		P = 2;
//		N = 1;
//		String proj8Name = "JavaGit-master";
//		Experimnet2_PIs.analyze_project(proj8Jar, proj8Src, proj8Csv, false, P, N);
//		for (String f : Experimnet2_PIs.SCList) {
//			fault_localization(proj8Jar, f, proj8Name, true);
//		}
//		print_all_results(proj8Name);
		//------------------------------------------
		// UNREACHABLE Or line---------------------- mariadb ----------------
//				String proj9Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/mariadb-connector-j-master/src";
//				String proj9Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/mariadb.jar";
//				String proj9Csv = CsvDir + "mariadb.csv";
//				P= 18; N = 1744; 
//				String proj9Name = "mariadb";
//				Experimnet2_PIs.analyze_project(proj9Jar, proj9Src, proj9Csv, false, P, N);
//				for (String f : Experimnet2_PIs.SCList) {
//					fault_localization(proj9Jar, f, proj9Name, true);
//				}
//				print_all_results(proj9Name);

		// w/ all failed to find line (222) method ----------XXXX------------ TFG_PC-master ----------------
//		String proj10Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src";
//		String proj10Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/TFG_PC-master.jar";
//		String proj10Csv = CsvDir + "TFG_PC-master.csv";
//		P= 77; N = 1;
//		Experimnet2_PIs.analyze_project(proj10Jar, proj10Src, proj10Csv, false, P, N); 
//
//		String proj10Name = "TFG_PC-master";
//		Experimnet2_PIs.analyze_project(proj10Jar, proj10Src, proj10Csv, false, P, N);
//		for (String f : Experimnet2_PIs.SCList) {
//			fault_localization(proj10Jar, f, proj10Name, true);
//		}
//		print_all_results(proj10Name);
		// UNREACHABLE or LONG LOOP w/ all---------------------- DCA_bookstore ----------------
//		
//		String projCsv;
//		String proj01Src= "/Users/Dareen/NetBeansProjects/DCAFixer_bookstore2/src"; 
//		String proj01Jar= "/Users/Dareen/NetBeansProjects/DCAFixer_bookstore2/dist/DCAFixer_bookstore2.jar";
//		projCsv = CsvDir +"DCA_bookstore.csv";
//		P= 1; N = 5;
//
//		String proj01Name = "DCA_bookstore";
//		Experimnet2_PIs.analyze_project(proj01Jar, proj01Src, projCsv, false, P, N);
//		for (String f : Experimnet2_PIs.SCList) {
//			fault_localization(proj01Jar, f, proj01Name, true);
//		}
//		print_all_results(proj01Name);

		// UNREACHABLE or LONG LOOP w/ all ---------------------- DCA_NewsPaper ----------------
		
//		String proj02Src= "/Users/Dareen/NetBeansProjects/DCAFixer_NewsPaper/src/dcafixer_newspaper";
//		String proj02Jar= "/Users/Dareen/NetBeansProjects/DCAFixer_NewsPaper/dist/DCAFixer_NewsPaper.jar";
//		String proj02Csv = CsvDir + "DCA_NewsPaper.csv";
//		P= 5; N =9; 
//		String proj02Name = "DCA_NewsPaper";
//		Experimnet2_PIs.analyze_project(proj02Jar, proj02Src, proj02Csv, false, P, N);
//		for (String f : Experimnet2_PIs.SCList) {
//			fault_localization(proj02Jar, f, proj02Name, true);
//		}
//		print_all_results(proj02Name);
//		
		// UNREACHABLE or LONG LOOP w/ all ---------------------- DCA_FuelOrders ----------------
//		String proj03Src= "/Users/Dareen/NetBeansProjects/DCAFixer_FuelOrdersClient/src/fuelordersclient"; 
//		String proj03Jar= "/Users/Dareen/NetBeansProjects/DCAFixer_FuelOrdersClient/dist/DCAFixer_FuelOrdersClient2.jar";
//		String proj03Csv = CsvDir + "DCA_FuelOrders.csv";
//		P= 5; N = 2; 
//
//		String proj03Name = "DCA_FuelOrders";
//		Experimnet2_PIs.analyze_project(proj03Jar, proj03Src, proj03Csv, false, P, N);
//		for (String f : Experimnet2_PIs.SCList) {
//			fault_localization(proj03Jar, f, proj03Name, true);
//		}
//		print_all_results(proj03Name);
		
		// Done ---------------------- DCA_hospital ----------------
//		String proj04Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/hospital-database-with-JDBC-client-master/JDBC_CLIENT/src";
//		String proj04Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/hospital-database-with-JDBC-client-master/JDBC_CLIENT/dist/DCAFixer_Hospital.jar";
//		String proj04Csv = CsvDir + "DCA_hospital.csv";
//		P= 3; N = 5; 
//
//		String proj04Name = "DCA_hospital";
//		Experimnet2_PIs.analyze_project(proj04Jar, proj04Src, proj04Csv, false, P, N);
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL_Detection.fault_localization(proj04Jar, f, proj04Name, true);
//		}
//		print_all_results(proj04Name);
		// ----------------------  ----------------
//		String proj04Src= "/Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/Apps_Before/Dummy_before_1116.java";//"/Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/Apps_Before";
//		String proj04Jar= "/Users/Dareen/Fixer/Experiments/JARS/apps-before.jar";
////		/Users/Dareen/Fixer/Experiments/JARS/apps-before.jar
//		String proj04Csv = CsvDir + "synthetic.csv";
//		P= 3; N = 5; 
//
//		String proj04Name = "synthetic";
////		Experimnet2_PIs.analyze_project(proj04Jar, proj04Src, proj04Csv, false, P, N);
////		for (String f : Experimnet2_PIs.SCList) {
////			System.out.print(f);
//			fault_localization(proj04Jar, f, proj04Name, true);
////		}
//		print_all_results(proj04Name);
		
		String projCsv;
//		String proj01Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/zjjava-master/JDBC/src/com/baiyi/jdbc/_11clob"; 
//		String proj01Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/vSync.jar";
		String proj01Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/vSync-master/vSync/src";
		String proj01Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/vSync.jar";
		
		projCsv = CsvDir +"vSync.csv";
		String proj01Name = "vSync";
		P= 1; N = 5;
		Experimnet2_PIs.analyze_project(proj01Jar, proj01Src, projCsv, false, P, N);
		for (String f : Experimnet2_PIs.SCList) 
			fault_localization(proj01Jar, f, proj01Name, true);
		print_all_results(proj01Name);
		
		long end = System.currentTimeMillis();
		float sec = (end - start) / 1000F;
		System.out.println("Time elapsed to locate and fix vuls " +sec + " seconds");
	}
}
