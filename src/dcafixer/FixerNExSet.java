package dcafixer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.shrike.shrikeCT.*;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import flocalization.G;
import flocalization.SignSlice;
import net.sf.jsqlparser.JSQLParserException;
import patchgenerator.AppMD;
import patchgenerator.PatternGen;
import patchgenerator.RplacePHs;
import queryparser.QParser;
import slicer.datatypes.Sig;
import slicer.datatypes.SliceLine;
import slicer.tool.CGClass;
import slicer.tool.EBCreation;
import slicer.tool.ExtractQuery;
import slicer.tool.ExtractQuery_BU2;
import slicer.tool.Info;
import slicer.tool.SlicerTool;
import slicer.utilities.StrUtil;

public class FixerNExSet {
//	public class Experiment2_FL {	
//	static boolean Print = false;
	//==============
	static int GFailedButSigned = 0;// sig is NOT null but parser failed
	static int GFailedQparser = 0;// sig is null
	static int GFailedSlicer = 0;// sig is null
	static int GSEC_PS = 0;// -,-,null
	static int GSEC_WL = 0;// -,?,[v,?]
	static int GSEC_CONST = 0;
	static int GQV = 0; // Q,?,[v]
	static int GQCT = 0; // Q,?,[c&|t]
	static int GQA = 0; // Q,?,[a]
	static int GSyntaxE_PS = 0;// S,?,[c&|t]
	static int GSyntaxE_EU = 0;// S,?,[v]
//	static int GCON_PassHC=0;
//	static int GCON_UserHC=0;
	
	//==============
	static int FailedButSigned = 0;// sig is NOT null but parser failed
	static int FailedQparser = 0;// sig is null
	static int FailedSlicer = 0;
	static int SEC_PS = 0;// -,-,null
	static int SEC_WL = 0;// -,?,[v,?]
	static int SEC_CONST = 0;
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
		 FailedSlicer = 0;
		 SEC_PS = 0;// -,-,null
		 SEC_WL = 0;// -,?,[v,?]
		 SEC_CONST = 0;
		 QV = 0; // Q,?,[v]
		 QCT = 0; // Q,?,[c&|t]
		 QA = 0; // Q,?,[a]
		 SyntaxE_PS = 0;// S,?,[c&|t]
		 SyntaxE_EU = 0;// S,?,[v]

	}
	
	public static void clearGloCounters() {
		 GFailedButSigned = 0;
		 GFailedQparser = 0;// sig is null
		 GFailedSlicer = 0;
		 GSEC_PS = 0;// -,-,null
		 GSEC_WL = 0;// -,?,[v,?]
		 GSEC_CONST = 0;
		 GQV = 0; // Q,?,[v]
		 GQCT = 0; // Q,?,[c&|t]
		 GQA = 0; // Q,?,[a]
		 GSyntaxE_PS = 0;// S,?,[c&|t]
		 GSyntaxE_EU = 0;// S,?,[v]
	}
	
	public static void addCountersto_GCounters() {
		GFailedQparser = GFailedQparser + FailedQparser;// sig is null
		GFailedSlicer = GFailedSlicer + FailedSlicer;
		GFailedButSigned = GFailedButSigned + FailedButSigned;
		GSEC_PS = GSEC_PS+ SEC_PS;// -,-,null
		GSEC_WL = GSEC_WL + SEC_WL;// -,?,[v,?]
		GSEC_CONST = GSEC_CONST + SEC_CONST;
		GQV = GQV + QV; // Q,?,[v]
		GQCT = GQCT+ QCT; // Q,?,[c&|t]
		GQA = GQA+ QA; // Q,?,[a]
		GSyntaxE_PS = GSyntaxE_PS + SyntaxE_PS;// S,?,[c&|t]
		GSyntaxE_EU = GSyntaxE_EU + SyntaxE_EU;// S,?,[v]
	}
	
	public static void print_file_results(String filePath) {
		System.out.print("Path: "+ filePath);
		int V=0, S=0; 
		System.out.println(
				"\nFailedQparser  "+ FailedQparser +
				"\nFailedSlicer   "+ FailedSlicer+
				"\nFailedButSigned: "+ FailedButSigned+
				"\nSEC_PS  "+ SEC_PS+		
				"\nSEC_WL  "+ SEC_WL+
				"\nSEC_CONST	"+SEC_CONST+
				"\nV - QV  "+ QV +	
				"\nV - QCT  "+ QCT+
				"\nV - QA  "+ QA+
				"\nSyntaxE_PS  "+ SyntaxE_PS +
				"\nSyntaxE_EU  "+ SyntaxE_EU +
				"\nSecure: " +(SEC_PS+SEC_WL+SEC_CONST)+
				"\nVulnerable: "+(QV+ QCT+ QA)+
				"\n****************************************************************\n");
	}
	
	public static void print_all_results(String projectName) {
		System.out.println("************ "+projectName+ " Stats ************" +
		"\nGFailedQparser  "+ GFailedQparser +
		"\nGFailedSlicer   "+ GFailedSlicer+
		"\nGFailedButSigned: "+ GFailedButSigned+
		"\nGSEC_PS  "+ GSEC_PS+	
		"\nGSEC_WL  "+ GSEC_WL+	
		"\nGSEC_CONST	"+ GSEC_CONST+
		"\nGQV  "+ GQV +		
		"\nGQCT  "+ GQCT+
		"\nGQA  "+ GQA+
		"\nGSyntaxE_PS  "+ GSyntaxE_PS +		
		"\nGSyntaxE_EU  "+ GSyntaxE_EU +
		"\nSecure: " +(GSEC_PS+GSEC_WL + GSEC_CONST)+
		"\nVulnerable: "+(GQV+ GQCT+ GQA)+
		"\n****************************************************************\n");
		clearGloCounters();
	}
	
	public static void collect_stat(Sig s, boolean print, boolean is_ps, boolean slicerfailed, String content) {
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
				if(slicerfailed)
					FailedSlicer++;
			}else if(s.get_I().get(0).equals('-')) {
				SEC_CONST++;
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
			if(slicerfailed) {
				FailedSlicer++;
			}else {
				FailedQparser++;
			}
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
	
public static boolean print_sliceLines (List<SliceLine> sliceLines) {
		
	for(SliceLine l : sliceLines) {
		 System.out.println(l.lno + " @ " + l.stmt);
	 }
	 System.out.println(" ----------------\n");
		return false;
	}
	
	public static List<SliceLine> createBackupSlice(Info info, String srcPath, String slicePath, String contextPath, boolean print) throws IOException{
		if(print) System.out.println("@@ createBackupSlice");
		List<SliceLine> sliceLines = new ArrayList<>();
		List<String> codeLines = new ArrayList<>();
		ArrayList<Integer> callLines = info.getCall_lines();
		ArrayList<Integer> queryLines =info.getQuery_lines();
		ArrayList<Integer> sliceCodeLineNos = new ArrayList<>();
		int clast = callLines.size()-1;
		int qlast = queryLines.size()-1;

		if( (callLines.get(0) < queryLines.get(0) || callLines.get(0) == queryLines.get(0)) && 
				(queryLines.get(0)< callLines.get(clast)|| queryLines.get(0) == callLines.get(clast))  ) {
//			overlab = true;
			sliceCodeLineNos = callLines;
		} else if(queryLines.get(0) > callLines.get(clast) ) {
//			overlab = false;
//		 	callLines + queryLines
			sliceCodeLineNos = callLines;
			sliceCodeLineNos.addAll(queryLines);
		}else if( queryLines.get(qlast)<callLines.get(0)) {
			sliceCodeLineNos = queryLines;
			sliceCodeLineNos.addAll(callLines);
		}
//		2- get lines in array list to get code lines
		codeLines = StrUtil.read_lines_list(srcPath);
//		3- build sliceLines
		
		for(Integer i :sliceCodeLineNos) {
			if(print)
				System.out.print(i+", ");	
			SliceLine sl = new SliceLine();
			sl.lno = i;
			sl.stmt = codeLines.get(i-1);
			sl.method = info.getMethod();
			sl.walaStmt = null;
			sliceLines.add(sl);
				
		}
		if(sliceLines.size()>0)
		{
			String slice_srclines = "";
			String context_srclines = "";
			String md_cq_setStrings = "";
			Sig s = new Sig();
			int sf_lno = 1; // slice file lno
				slice_srclines = "public class Slice {public static void main() {";//lno= 1			
			for (SliceLine sl : sliceLines) {
//				System.out.println(">>> "+sl);
				//+++++
				if(sl.stmt.contains("createStatement"))
					continue;
				sf_lno++;
				//++++++
				if (sf_lno == 2) {
//					slice_srclines = slice_srclines + sl.stmt.trim();
					context_srclines = context_srclines + sf_lno + "," + sl.method + "," + sl.lno;
					
					// Line format: slice_file_lno, method where line exists, line in original
					// source code,
				} else {
					context_srclines = context_srclines + "\n" + sf_lno + "," + sl.method + "," + sl.lno;
				}
				// Add slice lines to a java class.
				slice_srclines = slice_srclines + "\n" + sl.stmt.trim();
			}
			slice_srclines = slice_srclines + "\n}}";
			
			//Write slice & context to files
			StrUtil.write_tofile(slicePath, slice_srclines);
			
			StrUtil.write_tofile(contextPath, context_srclines);
			

		}
		if(sliceLines.size()>1 && print){
			System.out.println("-- Backup SLice :");
			print_sliceLines(sliceLines);
			 
		}
		return sliceLines;
	}
	public static void sort_info(List<Info> infoSet) {
		 
        infoSet.sort((o1, o2)
                  -> Integer.valueOf(o1.getLno()).compareTo( Integer.valueOf(o2.getLno())));
    }
	public static void fault_localization(int cpLength,CGClass appCG, String appJar,String filePath,  String projName, boolean print ) throws Exception {
		//TODO: $$$43 check if location to reset values is correct
		G.LastLineModified = 0;
		G.AppLines_partlyFixed.clear();
//	public static void fault_localization(int cpLength,int cgt, String appJar,String filePath,  String projName, boolean print ) throws Exception {
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
		System.out.println("################## " + file_name +" ##################");
		String file_folder = StrUtil.get_folder_path(filePath);// *
		ExtractQuery.Extractor(file_folder, file_name, 0);
//		ExtractQuery.printAll();
//		System.out.println("*** assignList ***\n"+ExtractQuery.assignList.toString());
//		System.out.println("*** assignListCalls ***\n"+ExtractQuery.assignListCalls.toString());
//		System.out.println("*** infoSet ***\n"+ExtractQuery.infoSet.toString());
//		System.out.println("=====================================\n");
		// ================= 2.	Slice & Sign =================
		
//		Collections.sort((List<Info>) ExtractQuery.infoSet);
		
		sort_info(ExtractQuery.infoSet);
//		for(Info info : ExtractQuery.infoSet) {
//			System.out.println("LN:"+info.getLno());
//		}
		for(Info info : ExtractQuery.infoSet) {
			QParser.clear_all_values("");
		//	--------------- 2.1)	slice check that query is complete, path: /Users/Dareen/Fixer/tmp/TSet/Slices/AppVSlice =================
			int v_lno = info.getLno();
			String v_appSrc = filePath;
			String slices_and_context_path =G.slicesTmpPath+projName;
			String md_file = "/"+file_name.replace(".java", "")+"_md.txt";
			String v_slice_file = "/"+file_name.replace(".java", "")+v_lno+"_VSlice.java";
			String v_context_file = "/"+file_name.replace(".java", "")+"_context.txt";
			String mdPath = slices_and_context_path + md_file;// "/md.txt";// +
			String v_slicePath = slices_and_context_path +v_slice_file;// "/VSlice.java";// + v_slice_file;
			String v_contextPath = slices_and_context_path + v_context_file;//"/context.txt";
//			String mdPath = G.sliceTmpPath + "/md.txt";//md_file;
//			String v_slicePath = G.sliceTmpPath + "/VSlice.java";//v_slice_file;
//			String v_contextPath = G.sliceTmpPath + "/context.txt";//v_context_file;
			String[] v_subpath = v_appSrc.split("/");
			

			String v_className_only = (v_subpath[v_subpath.length - 1].split("\\."))[0];// v_subpath[v_subpath.length - 1].replace(".java", "");
			String v_path_className = "";
			String v_classPath = "";
			if(cpLength == 1) {
//				//----------------------- Option 1
				v_classPath = v_subpath[v_subpath.length - 2] + "/";// TSet/
				v_path_className =v_classPath +v_className_only;

			}
			else if(cpLength == 2) {
//				//----------------------- Option 2
				v_classPath = v_subpath[v_subpath.length - 3] + "/"+v_subpath[v_subpath.length - 2] + "/";// TSet/
				v_path_className =v_classPath +v_className_only;

			}
			else if(cpLength == 3) {
				//----------------------- Option 3 - "src" folder			
				boolean src_found = false;
				for(int i =0; i< v_subpath.length-1;i++) {
					if(src_found)
						v_path_className = v_path_className +v_subpath[i] + "/";
					if(v_subpath[i].equals("src"))//For other cases
						src_found = true;
				}
				if(!src_found)
					System.err.println("NO \"src\" folder in the path");
				v_path_className = v_path_className + v_className_only;
				//-----------------------
			}
			else {
				//----------------------- Option N			
				boolean src_found = false;
				for(int i =0; i< v_subpath.length-1;i++) {
					if(src_found)
						v_path_className = v_path_className +v_subpath[i] + "/";
//					if(v_subpath[i].equals("src"))//For other cases
					if(v_subpath[i].equals("java"))// for mariadb and Momow1
						src_found = true;
				}
				if(!src_found)
					System.err.println("NO \"java\" folder in the path");
				v_path_className = v_path_className + v_className_only;
				//-----------------------
				
			}
			
	
			if(print )
				System.out.println("v_path_className: "+ v_path_className +", lno:" + v_lno+ " key: "+info.getKey() + "\n Slice path: "+ v_slicePath);
			
			boolean failToParse =false, failToSlice =false , allQueryisUI =false,codeIsSecure = false, query_is_constant = false; 
			String key = info.getKey();
			Sig s = new Sig();	
//			--------------- 2.2) Sign to mark SQL Vuls ---------
			List<SliceLine> sliceLines = new ArrayList<>();
			boolean is_ps= false;
//			System.out.println("KKKKKey: "+key);
//			"getConnection", "Properties"
			if(G.ConnIPList.contains(key) ){//---huna
				//Handle password cases!
				
				char v_sliceDir, v_sstype;
				if(key.equals("getConnection")) {
					v_sliceDir = 'B';
				}else {
					v_sliceDir = 'F';
				}
				v_sstype = 's';			

				sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
						v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery(), appCG);
				
				System.out.println(info.toString());
				System.out.println("###############");
				
				
				//------ TODO: Sign the IPs:
				//No Enc
				if(info.getConn_url_value().toLowerCase().contains(G.encrSetting)) {
					s.setvalues('C', 'E', null);
					System.out.println(s.toString() +" @ "+info.getLno()+"\n"+G.MSG_Conn_noEnc); 
				}
				//PW HC or/and datatype
				if(info.getConn_pass_value_type().equals(G.constantValue) && info.getConn_pass_dt().equals("String")) {
//					List<Character> I = new ArrayList<>();
//					I.add('d');
//					I.add('h');
					s.setvalues('P', 'B', null);// 'B' stands for both HC and DT
					System.out.println(s.toString() +" @ "+info.getLno() +"\n"+G.MSG_P_HD);
					
				}else if(info.getConn_pass_value_type().equals(G.constantValue)) {
					s.setvalues('P', 'h', null);
					System.out.println(s.toString() +" @ "+info.getLno() +"\n"+G.MSG_P_H);
				}else if(info.getConn_pass_dt().equals("String")) {
					s.setvalues('P', 'd', null);
					System.out.println(s.toString() +" @ "+info.getLno() +"\n"+G.MSG_P_D);
				}
				//UN is HC
				if(info.getConn_user_value_type().equals(G.constantValue)) {
					s.setvalues('U', 'h', null);
					System.out.println(s.toString() +" @ "+info.getLno() +"\n"+G.MSG_U_H);
				}
 
				continue;
			}
			// No need to handle createStatement case. The slices, even w/ full CD and DD, will have only createStatement line!
			// Also, we don't want to touch the create statement as it could be used with other cases!
//			else if(key.contains("createStatement")) {
//				char v_sliceDir = 'F';
//				char v_sstype = 's';
//				sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
//						v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery(), appCG);	
//			}
			else if(key.contains("executeUpdate") || key.contains("executeQuery") ||key.contains("execute")) {//if(G.ExIPList.contains(key)) {//
				
//				System.out.println("HERE 1!");
				char v_sliceDir = 'B';
				char v_sstype = 's';
				if(info.getQuery().equals("ps")) {
					
					// Slice to find ps line# from the slice
					if (print)System.out.println("HERE 1!");//,   "+ info.toString());
					s.setvalues('_', '_', null);
					codeIsSecure = true;
					/*find_slice_and_context(String appJar, String appSrc, String app_folder_className, char sliceDirection, int lno, String seed,
			char sstype, String slicePath, String contextPath, String mdPath,DataDependenceOptions dOptions,
			ControlDependenceOptions cOptions, char appType)*/
					
					// TODO: Search the infoSet for the corresponding ps by line#
					
				}else if(info.getQuery().equals("parameter")) {
					
					if (print) System.out.println("HERE 2!");//,   "+ info.toString());
					// All query is an arg
					// slice to check if the value in the other function is constant
					// parse and check if there is a value for the query
					//find_slice_and_context_query_sent_cgtype
					sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
							v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery(), appCG);
					if (sliceLines != null) {
						if (has_ps(sliceLines)) {

							s.setvalues('_', '_', null);// No vulnerability
							codeIsSecure = true;
						} else {
							// F,_,_ TODO: create a signature
							// Fail to parse query
							List<String> uiVar = new ArrayList<>();
							uiVar.add(info.getArg());
							QParser.parse_query(null, print, uiVar);
							List<Character> Is = new ArrayList<>();
							Is.add('a');
							s.setvalues('Q', '_', Is);
							allQueryisUI = true;
							System.out.println("s: " + s.toString() + ", QParser.qs_ql: " + QParser.qs_ql.toString());
						}
					} else {
//						//F,_,_ TODO: create a signature
//						// Fail to Slice the code
//						s.setvalues('F', '_', null);
						List<Character> Is = new ArrayList<>();
						Is.add('a');
						s.setvalues('Q', '_', Is);
						allQueryisUI = true;
						failToSlice = true;// Fail to Slice
						sliceLines = createBackupSlice(info,v_appSrc, v_slicePath,v_contextPath,print);
						//List<SliceLine>
					}
					
					
				}else if(info.getQuery().equals(null) || info.getQuery().equals("")  ) {
					if (print) System.out.println("HERE 3!,   "+ info.toString());
					// TODO: create a signature F,_, _ failed to parse it
					//- Try from the slice
					s.setvalues('F', '_', null);
					failToParse = true;
				}else {
					if (print) 
						System.out.println("HERE 4!");//,   "+ info.toString());
					// There is a complete query
					// ---- Sent directly 
					// ---- Stored in an arg
//					if(!info.getArg().equals(null) && !info.getArg().equals("")) {
//						
//					}
//					sliceLines = EBCreation.find_slice_and_context_query_sent(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
//							v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery());
					if (sliceLines != null) {
						if (has_ps(sliceLines)) {
							s.setvalues('_', '_', null);//No vulnerability
							codeIsSecure = true;
						} else {
							if(print)System.out.println("HERE 4.1! " + info.getQuery());
//					s = SignSlice.sign_slice_querystring_exp1_temp(v_slicePath,key, v_sliceDir, info.getQuery(), false, null );
							sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc,
									v_path_className, v_sliceDir, v_lno, key, v_sstype, v_slicePath, v_contextPath,
									mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery(), appCG);
							if(sliceLines == null) {
								failToSlice = true;
								sliceLines = createBackupSlice(info,v_appSrc, v_slicePath,v_contextPath,print);
							}
								
							QParser.parse_query(info.getQuery(), false, null);
							s = QParser.qs_ql;
							if (s.get_I().get(0).equals('-')) {
								query_is_constant = true;
								if(print) System.out.println(info.toString() + "\n" + s.toString() + "\n&&&&&&&&&&ffff&&&&&&\n");
							}
							if(QParser.ParserFail) {
								failToParse = true;
							}
//					String path, String key, char dir, String query, boolean print, List<String>uiVars
						}
					}else {
//						// Fail to Slice the code
						s.setvalues('F', '_', null);
						failToSlice = true;
						sliceLines = createBackupSlice(info,v_appSrc, v_slicePath,v_contextPath,print);
//						if(sliceLines != null) {
//							failToSlice = false;// Fail to Slice
//						}else{
//							failToSlice = true;// Fail to Slice
//						}
					}
				}
//				if(s != null) {	
				collect_stat(s,false, is_ps,failToSlice,filePath);// Secure and failed cases signatures are to collect stats
//				if (print) 
				
					System.out.println(info.toString() +"\n"+s.toString() + "\nPrefered sol: "+QParser.prefered_sol + 
							"\nParser failed? "+QParser.ParserFail+ " " + failToParse +
							"\nSlicer failed? "+failToSlice+
							"\n=======================\n");
//				}	
			}// --- End of Execute cases EQ, EU, and E
			
//			if(sliceLines.size()>0)
//				System.out.println(sliceLines.toString());
//				for(SliceLine l :sliceLines )System.out.println(l.toString());
			// TODO: --------- Connections Vuls ---------
			if(key.equals("getConnection")) {
				System.out.println(info.toString());
			}
			
			//TODO: collect stats here and add counters
//			collect_stat(s,false, is_ps,filePath);// Secure and failed cases signatures are to collect stats
//			================= 3.	get SSlicesPath =================
			//===== After reading one vulnerability , we try to fix one at a time.
//			if(!(failToParse || allQueryisUI|| codeIsSecure || query_is_constant || failToSlice ) && s != null ) {
//			if(!(failToParse || allQueryisUI|| codeIsSecure || query_is_constant ) && s != null && sliceLines != null ) {	
//				
			if(!(allQueryisUI|| codeIsSecure || query_is_constant ) && s != null && sliceLines != null ) {	
//				
//			List<String> set_strings = new ArrayList<String>();
			String vSlicesPath =G.SQLI_VSlicesPath;
//			String SSlicesPath = SignSlice.get_slices_folder_path(s,"");
//			SSlicesPath = SignSlice.get_slices_folder_path_exp4(s,"");
//			set_strings = QParser.set_strings;//SignSlice.qp.get_set_strings();
//			 String s2= QParser.qs_ql.toString();//SignSlice.qp.toString();
			 if(print) {
				 System.out.println("SSlicesPath: "+vSlicesPath);
//				 System.out.println("~~~~~"+SignSlice.qp.get_pstmt_query());
//				 System.out.println("~~~~~"+SignSlice.qp.get_set_strings().toString());
			}
			 if(vSlicesPath == null) {
					System.err.println("vSlicesPath: is null");// due to: ("+s.toString() +"|"+s2+"), "+s2);
					//continue;
			}
			 //TODO: from the patch get the number of the new fixed "execute" call. 
			 //		This will help to check if the new code is fixed
			 
//				================= 4.	replace phs and create new SSlices in SSlicesPath+"/tmp” =================
			 	// TODO: SSLICES path for DDL commands (Alter, drop ,...
			 	//TODO: For patches uncomment below call. RplacePHs.replacePlaceHolders.. , & PatternGen.findPatternsAndA ...
//				RplacePHs.replacePlaceHolders(v_slicePath, SSlicesPath, info.getApp_conn(), 
//						info.getApp_rs(), info.getArg(), info.getStmtVar(),info.getApp_rs_dt(),info.getKey(), false);//"" should be stmt var name
			 //replace
			 //%%%%%%%
			 File sf = new File(v_slicePath); 
				if(sf.exists()) {
			 RplacePHs.replacePlaceHolders_and_createTmpVSlices( vSlicesPath, info, false);
//			 replacePlaceHolders_info
//				================= 5.	Call find diff all && Apply patches  =================
				AppMD app_md = new AppMD();
				app_md.app_sql = info.getArg();
				app_md.app_stmt = info.getStmtVar();
				app_md.ps_query = QParser.PS_query;// SignSlice.qp.get_pstmt_query();
				app_md.wl_query = QParser.WL_query;// SignSlice.qp.getWL_query();
				app_md.soltion = QParser.prefered_sol;
				String setStrings = "";
				boolean firstSet = true;
				for (String set : QParser.set_strings) {// SignSlice.qp.get_set_strings()) {
					if(set.contains("; ;"))
					set = set.replace("; ;", ";");
							
					if (firstSet) {
						firstSet = false;
						setStrings = set;
					} else {
						setStrings = setStrings + "\n" + set;
					}
				}
				app_md.setStrings = setStrings;
//				 String vulApp , String vsPath, String sslicesPath,  Info info,  AppMD amd, boolean print
				//%%%%%%%%%%%
				if (print) System.out.println("BBB findPatternsAndApply_usingExistingPatches");
				//TODO: $$$43 check results
				 if(PatternGen.findPatternsAndApply_usingExistingPatches(v_appSrc,v_slicePath, G.SQLI_TmpVSlicesPath, info, app_md, false )) {
					 System.out.println("Vul at line: " +info.getLno()+" was patched with No errors!");
				 }else {
					 System.out.println("Vul at line: " +info.getLno()+" was Not patched!");
				 }
				 System.out.println("================================================================");
				}
			      //============ 10/12th  XXXXXXXXXXXXXXXXXXXXXX
			    //TODO: For patches uncomment below calls.
//			      if (QParser.prefered_sol == 1)
//			      PatternGen.findPatternsAndApply_exp4(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, info.getQuery_lines(), QParser.PS_query, false);
//			      else if (QParser.prefered_sol == 2)
//			    	  PatternGen.findPatternsAndApply_exp4(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, info.getQuery_lines(), QParser.WL_query, true);	  
				//================= 6.	count Number of file that doesn’t have syntax error =================
			}else {
				if(s == null)
					System.out.println("s in Null");
//				if(failToParse && s.get_I().get(0).equals('t')) {
//					//TODO: Apply WL sol
//				}
				
			}
			 failToParse =false ; allQueryisUI =false;codeIsSecure = false; 
			 codeIsSecure = false;failToSlice = false;
			 //************************************
			


				
		}// ---------- END of loop over infoSet
		System.out.println("###############################################################");
		print_file_results(filePath);
		addCountersto_GCounters();
		//TODO print Stats
		
		
		
	}
	
	public static void main(String[] args) throws Exception {
//		/Users/Dareen/Desktop/DCAFixer_Experimets/Results
		// G.ResultsPath;
//        PrintStream o = new PrintStream(new File("/Users/Dareen/Desktop/DCAFixer_Experimets/mariadb_ouput.txt"));
//        System.setOut(o);
        

//		String CsvDir = "/Users/Dareen/Desktop/DCAFixer_Experimets/Csv_files/";//
		String CsvDir = G.CsvDir;
		int N, P;
		long start = System.currentTimeMillis();

//		// ****************************************************************************************************
//		// ******************************    Apps used for training in SQLIFix   ******************************
//		// ****************************************************************************************************
//	
//
//		// Done CG4 ---------------------- "banking-app" ----------------
//		String proj1Src = G.projectsPath + "banking-app-devkala48-master/src";
//		String proj1Jar = G.projectsPath + "jar_files/banking-app.jar";
//		String proj1Csv = CsvDir + "banking-app.csv";
//		String proj1Name = "banking-app";
//		PrintStream o1 = new PrintStream(new File(G.ResultsPath + proj1Name + ".txt"));
//		System.setOut(o1);
//
////		main/java/com/revature/Account
//		P = 4;
//		N = 11;
//		Experimnet2_PIs.analyze_project(proj1Jar, proj1Src, proj1Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG1 = EBCreation.BuildCG(proj1Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG1.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			// "com/revature/Staff" -> 2 subfolders
////			Experiment2_FL.fault_localization(2,appCG, G.CG_AllEPoints_MyExGUI,proj1Jar, f, proj1Name, false);//CG_AllEPoints
//			Experiment2_FL.fault_localization(2, appCG1, proj1Jar, f, proj1Name, false);
//		}
//		print_all_results(proj1Name);
//
//		// DONE ---------------------- BookStore ----------------
//		String proj2Src = G.projectsPath + "Online-Book-Store-System-master/src/workspace";
//		String proj2Jar = // G.projectsPath +"jar_files/BookStore.jar";
//				G.projectsJarsPath + "Online-Book-Store-System-master.jar";
//		String proj2Csv = CsvDir + "BookStore.csv";
//		String proj2Name = "BookStore";
//		P = 46;
//		N = 0;
//
//		PrintStream o2 = new PrintStream(new File(G.ResultsPath + proj2Name + ".txt"));
//		System.setOut(o2);
//
//		System.out.println("Start building the CG .......");
//		CGClass appCG2 = EBCreation.BuildCG(proj2Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG2.getCg()));
//		System.out.println("Done building the CG .......");
//
//		Experimnet2_PIs.analyze_project(proj2Jar, proj2Src, proj2Csv, false, P, N);
//		for (String f : Experimnet2_PIs.SCList) {
//			// Experiment2_FL.fault_localization(1, G.CG_MainEPoints_MyEx,proj2Jar, f,
//			// proj2Name, false);
//			Experiment2_FL.fault_localization(1, appCG2, proj2Jar, f, proj2Name, true);
//		}
//		print_all_results(proj2Name);
//
//		// Slicer Failed "Failed to find line (" ----------------------
//		// ------------ crawledemo_property ---------------- it has a syntax error
//		// Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/crawledemo-master/property/
//		String proj3Src = G.projectsPath + "crawledemo-master/property/src/com/property";
//		String proj3Jar = G.projectsPath + "jar_files/crawledemo_property.jar";
//		String proj3Csv = CsvDir + "crawledemo_property.csv";
//		P = 3;
//		N = 0;
//		String proj3Name = "crawledemo_property";
//
//		PrintStream o3 = new PrintStream(new File(G.ResultsPath + proj3Name + ".txt"));
//		System.setOut(o3);
//
//		Experimnet2_PIs.analyze_project(proj3Jar, proj3Src, proj3Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass appCG3 = EBCreation.BuildCG(proj3Jar, G.CG_AllEPoints_MyExGUI);// G.CG.. other options didn't work
//		System.out.println(CallGraphStats.getStats(appCG3.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(1, appCG3, proj3Jar, f, proj3Name, false);
//			// Experiment2_FL.fault_localization(3, G.CG_AllEPoints,proj3Jar, f, proj3Name,
//			// false);
//			// "com/property/util/JdbcHelper" --> more than 2 subfolders, we put 3 here to
//			// go else part
//		}
//		print_all_results(proj3Name);
//
//		// ---- Update! All slices worked ---------------------- Ecomerce
//		// ----------------
//		String proj4Src = G.projectsPath + "Ecomerce--master/Ecomerce/src/ecomerce";
//		String proj4Jar = // G.projectsPath +"jar_files/Ecomerce.jar";
//				G.projectsJarsPath + "Ecomerce.jar";
//		String proj4Csv = CsvDir + "Ecomerce.csv";
//		P = 79;
//		N = 15;
//		String proj4Name = "Ecomerce";
//
//		PrintStream o4 = new PrintStream(new File(G.ResultsPath + proj4Name + ".txt"));
//		System.setOut(o4);
//
//		System.out.println("Start building the CG .......");
//		CGClass appCG4 = EBCreation.BuildCG(proj4Jar, G.CG_AllEPoints_MyExGUI);// G.CG.. other options didn't work
//		System.out.println(CallGraphStats.getStats(appCG4.getCg()));
//		System.out.println("Done building the CG .......");
//
//		Experimnet2_PIs.analyze_project(proj4Jar, proj4Src, proj4Csv, false, P, N);
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG4, proj4Jar, f, proj4Name, false);
//			// Experiment2_FL.fault_localization(1,G.CG_AllEPoints,proj4Jar, f, proj4Name,
//			// false);
//		}
//		print_all_results(proj4Name);
//		// Done , nested classes---------------------- fiftyfiftystockscreener
//		// ----------------
//		// some cases didn't work because of nested classes, BUT could be fixed by
//		// sending name of the method
//		// TODO: Add the name of the method to get more slices. (Look AT
//		// BWSCombinations)
//		String proj5Src = G.projectsPath + "fiftyfiftystockscreener-master/src/com/eddiedunn";
//		String proj5Jar = G.projectsPath + "jar_files/fiftyfiftystockscreener.jar";
//		String proj5Csv = CsvDir + "fiftyfiftystockscreener.csv";
//		P = 32;
//		N = 4;
//		String proj5Name = "fiftyfiftystockscreener";
//
//		PrintStream o5 = new PrintStream(new File(G.ResultsPath + proj5Name + ".txt"));
//		System.setOut(o5);
//
//		System.out.println("Start building the CG .......");
//		CGClass appCG5 = EBCreation.BuildCG(proj5Jar, G.CG_AllEPoints_MyExGUI);// G.CG.. other options didn't work
//		System.out.println(CallGraphStats.getStats(appCG5.getCg()));
//		System.out.println("Done building the CG .......");
//
//		Experimnet2_PIs.analyze_project(proj5Jar, proj5Src, proj5Csv, false, P, N);
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG5, proj5Jar, f, proj5Name, false);
////			Experiment2_FL.fault_localization(3,G.CG_AllEPoints,proj5Jar, f, proj5Name, false);
//		}
//		print_all_results(proj5Name);
////
//		// Done, 27 cases slicer failed; didn't find the line ----------------------
//		// GUI-DBMS ----------------
//		String proj6Src = G.projectsPath + "GUI-DBMS-master/TableEasy/src/tableeasy";
//		String proj6Jar = G.projectsPath + "jar_files/GUI-DBMS.jar";
//		String proj6Csv = CsvDir + "GUI-DBMS.csv";
//		P = 33;
//		N = 20;
//		String proj6Name = "GUI-DBMS";
//
//		PrintStream o6 = new PrintStream(new File(G.ResultsPath + proj6Name + ".txt"));
//		System.setOut(o6);
//
//		Experimnet2_PIs.analyze_project(proj6Jar, proj6Src, proj6Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG6 = EBCreation.BuildCG(proj6Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG6.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG6, proj6Jar, f, proj6Name, false);
//		}
//		print_all_results(proj6Name);
//
//		// ==== Done ---------------------- InventarioWeb ----------------
//		String proj7Src = G.projectsPath + "InventarioWeb-master/src/java";
//		String proj7Jar = G.projectsPath + "jar_files/InventarioWeb.jar";
//		String proj7Csv = CsvDir + "InventarioWeb.csv";
//		P = 1;
//		N = 31;
//		String proj7Name = "InventarioWeb";
//
//		PrintStream o7 = new PrintStream(new File(G.ResultsPath + proj7Name + ".txt"));
//		System.setOut(o7);
//
//		Experimnet2_PIs.analyze_project(proj7Jar, proj7Src, proj7Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		// CGClass appCG7 = EBCreation.BuildCG( proj7Jar, G.CG_AllEPoints_MyEx);
//		CGClass appCG7 = EBCreation.BuildCG(proj7Jar, G.CG_MainEPoints_CGTUtilEx);
//		System.out.println(CallGraphStats.getStats(appCG7.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG7, proj7Jar, f, proj7Name, true);
//		}
//		print_all_results(proj7Name);
//
//		// Done ---------------------- JavaGit-master ----------------
////		String appJar = G.projectsJarsPath +"JavaGit-master.jar";
////		String appSrc = G.projectsPath +"JavaGit-master/src/gs/veepeek/assessment/Assesment.java";
////		String classPath = "gs/veepeek/assessment/Assesment";
//
//		String proj8Src = G.projectsPath + "JavaGit-master/src";
//		String proj8Jar = G.projectsPath + "jar_files/JavaGit-master.jar";
//		String proj8Csv = CsvDir + "JavaGit-master.csv";
//		P = 2;
//		N = 1;
//		String proj8Name = "JavaGit-master";
//
//		PrintStream o8 = new PrintStream(new File(G.ResultsPath + proj8Name + ".txt"));
//		System.setOut(o8);
//
//		Experimnet2_PIs.analyze_project(proj8Jar, proj8Src, proj8Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG8 = EBCreation.BuildCG(proj8Jar, G.CG_AllEPoints_MyEx);
//		System.out.println(CallGraphStats.getStats(appCG8.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG8, proj8Jar, f, proj8Name, true);
//		}
//		print_all_results(proj8Name);
//		// ------------------------------------------
//		// ==== Done, worked with "4", 1149 sliced correctly, 234 cases failed (45
//		// Failed to find line, 189 Failed to find call to ??? in Node)
//		// ---------------------- mariadb ----------------
//		String proj9Src = G.projectsPath + "mariadb-connector-j-master/src";
//		String proj9Jar = G.projectsPath + "jar_files/mariadb.jar";
//		String proj9Csv = CsvDir + "mariadb.csv";
//		P = 18;
//		N = 1744;
//		String proj9Name = "mariadb";
//
//		PrintStream o9 = new PrintStream(new File(G.ResultsPath + proj9Name + ".txt"));
//		System.setOut(o9);
//
//		Experimnet2_PIs.analyze_project(proj9Jar, proj9Src, proj9Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG9 = EBCreation.BuildCG(proj9Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG9.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(4, appCG9, proj9Jar, f, proj9Name, true);
//		}
//		print_all_results(proj9Name);
//		// === Done all worked!---------------------- TFG_PC-master ----------------
//		String proj10Src = G.projectsPath + "TFG_PC-master/src";
//		String proj10Jar = G.projectsPath + "jar_files/TFG_PC-master.jar";
//		String proj10Csv = CsvDir + "TFG_PC-master.csv";
//		P = 77;
//		N = 1;
//		String proj10Name = "TFG_PC-master";
//
//		PrintStream o10 = new PrintStream(new File(G.ResultsPath + proj10Name + ".txt"));
//		System.setOut(o10);
//
//		Experimnet2_PIs.analyze_project(proj10Jar, proj10Src, proj10Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass appCG10 = EBCreation.BuildCG(proj10Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG10.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG10, proj10Jar, f, proj10Name, true);
//		}
//		print_all_results(proj10Name);
//
//		// ==== Done ---------------------- vSync ----------------
//		String proj11Src = G.projectsPath + "vSync-master/vSync/src";
//		String proj11Jar = G.projectsPath + "jar_files/vSync.jar";
//		String proj11Csv = CsvDir + "vSync.csv";
//		P = 18;
//		N = 1744;
//		String proj11Name = "vSync";
//
//		PrintStream o11 = new PrintStream(new File(G.ResultsPath + proj11Name + ".txt"));
//		System.setOut(o11);
//
//		Experimnet2_PIs.analyze_project(proj11Jar, proj11Src, proj11Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG11 = EBCreation.BuildCG(proj11Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG11.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG11, proj11Jar, f, proj11Name, true);
//		}
//		print_all_results(proj11Name);
//
//		// ==== Done ---------------------- zjjavaJDBC ----------------
//		String proj12Src = G.projectsPath + "zjjava-master";
//		String proj12Jar = G.projectsPath + "jar_files/zjjavaJDBC.jar";
//		String proj12Csv = CsvDir + "zjjavaJDBC.csv";
//		P = 18;
//		N = 1744;
//		String proj12Name = "zjjavaJDBC";
//
//		PrintStream o12 = new PrintStream(new File(G.ResultsPath + proj12Name + ".txt"));
//		System.setOut(o12);
//
//		Experimnet2_PIs.analyze_project(proj12Jar, proj12Src, proj12Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG12 = EBCreation.BuildCG(proj12Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG12.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG12, proj12Jar, f, proj12Name, true);
//		}
//		print_all_results(proj12Name);
//
//		// ==== Slicer didn't work, 3 cases "Failed to find line" ----------------------
//		// summer-migration ----------------
//		String proj13Src = G.projectsPath + "summer-migration-master/src";
//		String proj13Jar = G.projectsJarsPath + "summer.jar";
//		String proj13Csv = CsvDir + "summer-migration.csv";
//		P = 18;
//		N = 1744;
//		String proj13Name = "summer-migration";
//
//		PrintStream o13 = new PrintStream(new File(G.ResultsPath + proj13Name + ".txt"));
//		System.setOut(o13);
//
//		Experimnet2_PIs.analyze_project(proj13Jar, proj13Src, proj13Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG13 = EBCreation.BuildCG(proj13Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG13.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(1, appCG13, proj13Jar, f, proj13Name, true);
//		}
//		print_all_results(proj13Name);
//		// ==== All slices failed "Failed to find line", but signed correctly
//		// ---------------------- UserVerificationSystem ----------------
//		String proj14Src = G.projectsPath + "UserVerificationSystem-master/UserVerificationSystem/src";
//		String proj14Jar = G.projectsJarsPath + "UserVerificationSystem.jar";
//		String proj14Csv = CsvDir + "UserVerificationSystem.csv";
//		P = 18;
//		N = 1744;
//		String proj14Name = "UserVerificationSystem";
//
//		PrintStream o14 = new PrintStream(new File(G.ResultsPath + proj14Name + ".txt"));
//		System.setOut(o14);
//
//		Experimnet2_PIs.analyze_project(proj14Jar, proj14Src, proj14Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG14 = EBCreation.BuildCG(proj14Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG14.getCg()));
//		System.out.println("Done building the CG .......");
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(2, appCG14, proj14Jar, f, proj14Name, true);
//		}
//		print_all_results(proj14Name);
//		// ------------------------------------------
//		// ****************************************************************************************************
//		// ******************************    Apps used for Testing in SQLIFix    ******************************
//		// ****************************************************************************************************
//		P = 0;
//		N = 0;
//		// ===== Done, slicer failed in 17 cases, and succeed in 66 case
//		// ==============================
//		String testProj01Src = G.projectsPath + "test/EPL441Clinic-master";
//		String testProj01Jar = G.projectsJarsPath + "Clinic.jar";
//		String testProj01Csv = CsvDir + "Clinic";
//		String testProj01Name = "Clinic";
//
//		PrintStream o19 = new PrintStream(new File(G.ResultsPath + testProj01Name + ".txt"));
//		System.setOut(o19);
//
//		Experimnet2_PIs.analyze_project(testProj01Jar, testProj01Src, testProj01Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass testApp01CG = EBCreation.BuildCG(testProj01Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(testApp01CG.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, testApp01CG, testProj01Jar, f, testProj01Name, true);
//		}
//		print_all_results(testProj01Name);
//
//		// ===== Done - All SLices worked !==============================
//		String testProj02Src = G.projectsPath + "test/Hotel-Management---Java-master/src";
//		String testProj02Jar = G.projectsJarsPath + "Hotel-Management.jar";
//		String testProj02Csv = CsvDir + "Hotel-Management";
//		String testProj02Name = "Hotel-Management";
//
//		PrintStream o20 = new PrintStream(new File(G.ResultsPath + testProj02Name + ".txt"));
//		System.setOut(o20);
//
//		Experimnet2_PIs.analyze_project(testProj02Jar, testProj02Src, testProj02Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass testApp02CG = EBCreation.BuildCG(testProj02Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(testApp02CG.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, testApp02CG, testProj02Jar, f, testProj02Name, true);
//		}
//		print_all_results(testProj02Name);
//
//		// ===== Done, 49 cases were sliced correctly, Most of the methods names were
//		// not extracted. Slicer didn't work even when I entered the method name
//		// manually! ==============================
//		// Same results w/ different types of CGs
//		// check if data type List<??> is the reason
//		String testProj03Src = G.projectsPath + "test/iTrust-v23-master/iTrust/src";
//		String testProj03Jar = G.projectsJarsPath + "iTrust.jar";
//		String testProj03Csv = CsvDir + "iTrust";
//		String testProj03Name = "iTrust";
//
//		PrintStream o21 = new PrintStream(new File(G.ResultsPath + testProj03Name + ".txt"));
//		System.setOut(o21);
//
//		Experimnet2_PIs.analyze_project(testProj03Jar, testProj03Src, testProj03Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass testApp03CG = EBCreation.BuildCG(testProj03Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(testApp03CG.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, testApp03CG, testProj03Jar, f, testProj03Name, true);
//		}
//		print_all_results(testProj03Name);
//		// ===== Done - All slices worked ==============================
//		String testProj04Src = G.projectsPath + "test/Java_BBDD-master/src";
//		String testProj04Jar = G.projectsJarsPath + "Java_BBDD.jar";
//		String testProj04Csv = CsvDir + "Java_BBDD";
//		String testProj04Name = "Java_BBDD";
//
//		PrintStream o22 = new PrintStream(new File(G.ResultsPath + testProj04Name + ".txt"));
//		System.setOut(o22);
//
//		Experimnet2_PIs.analyze_project(testProj04Jar, testProj04Src, testProj04Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass testApp04CG = EBCreation.BuildCG(testProj04Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(testApp04CG.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, testApp04CG, testProj04Jar, f, testProj04Name, true);
//		}
//		print_all_results(testProj04Name);
//
//		// ===== Done - all slices worked ==============================
//		String testProj05Src = G.projectsPath + "test/java-homework-10-master/src";
//		String testProj05Jar = G.projectsJarsPath + "homework-10.jar";
//		String testProj05Csv = CsvDir + "homework-10";
//		String testProj05Name = "homework-10";
//		P = 0;
//		N = 0;
//
//		PrintStream o23 = new PrintStream(new File(G.ResultsPath + testProj05Name + ".txt"));
//		System.setOut(o23);
//
//		Experimnet2_PIs.analyze_project(testProj05Jar, testProj05Src, testProj05Csv, true, P, N);
//		System.out.println(Experimnet2_PIs.getSCList().toString());
//		System.out.println("Start building the CG ............");
////				System.out.println("CG from SrcDir ..");
////				CGClass testApp05CG_srcDir = EBCreation.BuildSrcDirCG(G.projectsPath +"test/java-homework-10-master/src", "Lcom/company/Main");
////				System.out.println(CallGraphStats.getStats(testApp05CG_srcDir.getCg()));
////				System.out.println("CG from JAR ..");
//		CGClass testApp05CG = EBCreation.BuildCG(testProj05Jar, G.CG_AllEPoints_MyExGUI);// G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(testApp05CG.getCg()));
//		System.out.println("Done building the CG ............");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, testApp05CG, testProj05Jar, f, testProj05Name, true);
//		}
//		print_all_results(testProj05Name);
//		// Done ==============================
//		String testProj06Src = G.projectsPath + "test/Momow1-master/src/java";
//		String testProj06Jar = G.projectsJarsPath + "Momow1.jar";
//		String testProj06Csv = CsvDir + "Momow1";
//		String testProj06Name = "Momow1";
//
//		PrintStream o24 = new PrintStream(new File(G.ResultsPath + testProj06Name + ".txt"));
//		System.setOut(o24);
//
//		Experimnet2_PIs.analyze_project(testProj06Jar, testProj06Src, testProj06Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass testApp06CG = EBCreation.BuildCG(testProj06Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(testApp06CG.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(4, testApp06CG, testProj06Jar, f, testProj06Name, true);
//		}
//		print_all_results(testProj06Name);
//
//		// Done - one slice failed! (Failed to find call
//		// to)==============================
//
//		String testProj07Src = G.projectsPath + "test/Naegling-GUI-master/src";
//		String testProj07Jar = G.projectsJarsPath + "NaeglingGUI.jar";
//		String testProj07Csv = CsvDir + "NaeglingGUI";
//		String testProj07Name = "NaeglingGUI";
//
//		PrintStream o25 = new PrintStream(new File(G.ResultsPath + testProj07Name + ".txt"));
//		System.setOut(o25);
//		P=0; N=0;
//		Experimnet2_PIs.analyze_project(testProj07Jar, testProj07Src, testProj07Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
////				System.out.println("CG from SrcDir ..");
////				CGClass testApp07CG_srcDir = EBCreation.BuildSrcDirCG(testProj07Src, "LAddTemplateDialog");
////				System.out.println(CallGraphStats.getStats(testApp07CG_srcDir.getCg()));
////				System.out.println("CG from Jar ..");
//		CGClass testApp07CG = EBCreation.BuildCG(testProj07Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(testApp07CG.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, testApp07CG, testProj07Jar, f, testProj07Name, true);
//		}
//		print_all_results(testProj07Name);
//		// ==============================
//		// ****************************************************************************************************
//		// ******************************        Executable DataSet Apps         ******************************
//		// ****************************************************************************************************
//
//		// ==== Done ---------------------- DCA_bookstore ---------------- All secure ,
//		// check HC pass
//
//		String projCsv;
//		String proj01Src = G.NBProjectsPath + "DCAFixer_bookstore2/src";
//		String proj01Jar = G.NBProjectsPath + "DCAFixer_bookstore2/dist/DCAFixer_bookstore2.jar";
//		projCsv = CsvDir + "DCA_bookstore.csv";
//		P = 1;
//		N = 5;
//
//		String proj01Name = "DCA_bookstore";
//		PrintStream o15 = new PrintStream(new File(G.ResultsPath + proj01Name + ".txt"));
//		System.setOut(o15);
//
//		Experimnet2_PIs.analyze_project(proj01Jar, proj01Src, projCsv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG01 = EBCreation.BuildCG(proj01Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG01.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG01, proj01Jar, f, proj01Name, true);
//		}
//		print_all_results(proj01Name);
//
//		// === Done , all worked ---------------------- DCA_NewsPaper ----------------
//		String proj02Src = G.NBProjectsPath + "DCAFixer_NewsPaper/src/dcafixer_newspaper";
//		String proj02Jar = G.NBProjectsPath + "DCAFixer_NewsPaper/dist/DCAFixer_NewsPaper.jar";
//		String proj02Csv = CsvDir + "DCA_NewsPaper.csv";
//		P = 5;
//		N = 9;
//
//		String proj02Name = "DCA_NewsPaper";
//		PrintStream o16 = new PrintStream(new File(G.ResultsPath + proj02Name + ".txt"));
//		System.setOut(o16);
//
//		Experimnet2_PIs.analyze_project(proj02Jar, proj02Src, proj02Csv, false, P, N);
//		System.out.println("Start building the CG .......");
//		CGClass appCG02 = EBCreation.BuildCG(proj02Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG02.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG02, proj02Jar, f, proj02Name, true);
//		}
//		print_all_results(proj02Name);
//
//		// ==== Done, all cases succeed with makeRTABuilder ----------------------
		// DCA_FuelOrders ----------------
		String proj03Src = G.NBProjectsPath + "DCAFixer_FuelOrdersClient/src/fuelordersclient";
		String proj03Jar = G.NBProjectsPath + "DCAFixer_FuelOrdersClient/dist/DCAFixer_FuelOrdersClient2.jar";
		String proj03Csv = CsvDir + "DCA_FuelOrders.csv";
		P = 5;
		N = 2;

		String proj03Name = "DCA_FuelOrders";
		PrintStream o17 = new PrintStream(new File(G.ResultsPath + proj03Name + ".txt"));
		System.setOut(o17);

		LocatingPIs.analyze_project(proj03Jar, proj03Src, proj03Csv, false, P, N);
		System.out.println("Start building the CG .......");
//			 	System.out.println("CG from SrcDir ..");
//			 	///Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/FuelOrdersClient-master/src/main/java/fuelordersclient/PetrolInsertForm.java
//				CGClass testApp05CG_srcDir = EBCreation.BuildSrcDirCG(proj03Src, "LPetrolInsertForm");
//				System.out.println(CallGraphStats.getStats(testApp05CG_srcDir.getCg()));
		System.out.println("CG from JAR ..");
		CGClass appCG03 = EBCreation.BuildCG(proj03Jar, G.CG_AllEPoints_MyExGUI);
		System.out.println(CallGraphStats.getStats(appCG03.getCg()));
		System.out.println("Done building the CG .......");

		for (String f : LocatingPIs.SCList) {
			FixerNExSet.fault_localization(3, appCG03, proj03Jar, f, proj03Name, true);
		}
		print_all_results(proj03Name);
//
//		// Done ---------------------- DCA_hospital ----------------
//		String proj04Src = "/Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/hospital-database-with-JDBC-client-master/JDBC_CLIENT/src";
//		String proj04Jar = "/Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/hospital-database-with-JDBC-client-master/JDBC_CLIENT/dist/DCAFixer_Hospital.jar";
//		String proj04Csv = CsvDir + "DCA_hospital.csv";
//		P = 3;
//		N = 5;
//
//		String proj04Name = "DCA_hospital";
//		PrintStream o18 = new PrintStream(new File(G.ResultsPath + proj04Name + ".txt"));
//		System.setOut(o18);
//
//		Experimnet2_PIs.analyze_project(proj04Jar, proj04Src, proj04Csv, false, P, N);
//
//		System.out.println("Start building the CG .......");
//		CGClass appCG04 = EBCreation.BuildCG(proj04Jar, G.CG_AllEPoints_MyExGUI);
//		System.out.println(CallGraphStats.getStats(appCG04.getCg()));
//		System.out.println("Done building the CG .......");
//
//		for (String f : Experimnet2_PIs.SCList) {
//			Experiment2_FL.fault_localization(3, appCG04, proj04Jar, f, proj04Name, true);
//		}
//		print_all_results(proj04Name);
//		// ****************************************************************************************************
//		// ------------------ synthetic --------------------
//				String proj04Src= "/Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/Apps_Before/Dummy_before_1116.java";//"/Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/Apps_Before";
//				String proj04Jar= G.projectsJarsPath +"apps-before.jar";
////				/Users/Dareen/Fixer/Experiments/JARS/apps-before.jar
//				String proj04Csv = CsvDir + "synthetic.csv";
//				P= 3; N = 5; 
//		
//				String proj04Name = "synthetic";
////				Experimnet2_PIs.analyze_project(proj04Jar, proj04Src, proj04Csv, false, P, N);
////				for (String f : Experimnet2_PIs.SCList) {
////					System.out.print(f);
//					Experiment2_FL.fault_localization(3,appCG,proj04Jar, f, proj04Name, true);
////				}
//				print_all_results(proj04Name);	
//				String projCsv;
//				String proj01Src= G.NBProjectsPath +"DCAFixer_bookstore2/src"; 
//				String proj01Jar= G.NBProjectsPath +"DCAFixer_bookstore2/dist/DCAFixer_bookstore2.jar";
//				projCsv = CsvDir +"DCA_bookstore.csv";
//				P= 1; N = 5;
//				analyze_project(proj01Jar, proj01Src, projCsv, false, P, N);
				

		// ----------------------------------------

		
		//Dirs to clean from _fixed.java
		//"/Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/hospital-database-with-JDBC-client-master/JDBC_CLIENT/src";
		//"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/";
		//"/Users/Dareen/NetBeansProjects/";
		
		long end = System.currentTimeMillis();
		float sec = (end - start) / 1000F;
		PrintStream console = System.out;
        System.setOut(console);
        System.out.println("Done!\nTime elapsed to locate and fix vuls " + sec + " seconds");
        java.awt.Toolkit.getDefaultToolkit().beep();
        java.awt.Toolkit.getDefaultToolkit().beep();
        java.awt.Toolkit.getDefaultToolkit().beep();
        java.awt.Toolkit.getDefaultToolkit().beep();
        java.awt.Toolkit.getDefaultToolkit().beep();
	}
}
