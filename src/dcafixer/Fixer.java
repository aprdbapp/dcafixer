package dcafixer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;

import flocalization.G;
import flocalization.SignSlice;
import patchgenerator.AppMD;
import patchgenerator.PatternGen;
import patchgenerator.PatternGenConn;
import patchgenerator.RplacePHs;
import patchgenerator.RplacePHsConn;
//import patchgenerator.AppMD;
//import patchgenerator.PatternGen;
//import patchgenerator.RplacePHs;
//import patchgenerator.RplacePHsConn;
import queryparser.QParser;
import slicer.SlicerUtil;
import slicer.datatypes.Sig;
import slicer.datatypes.SliceLine;
import slicer.tool.CGClass;
import slicer.tool.EBCreation;
import slicer.tool.ExtractQuery;
import slicer.tool.Info;
import slicer.utilities.StrUtil;

public class Fixer {
//	static boolean Print = false;
	public static boolean sySetFlag = false;
	public static boolean credLFixed = false;
//	String credLines = "";
	public static ArrayList<Integer> credLines = new ArrayList<>();
	// ==============
	static PatchesCounters PC = new PatchesCounters();
	static int GFailedButSigned = 0;// sig is NOT null but parser failed
	static int GFailedQparser = 0;// sig is null
	static int GFailedSlicer = 0;// sig is null
	static int GSEC_PS = 0; // -,-,null and is_ps
	static int GSEC = 0; // -,-,null
	static int GSEC_WL = 0; // -,?,[v,?]
	static int GSEC_CONST = 0;
	static int GQV = 0; // Q,?,[v]
	static int GQCT = 0; // Q,?,[c&|t]
	static int GQA = 0; // Q,?,[a] ,OR Q,?,[t,v]
	static int GCONN = 0;// P is hc or/and dt || U is hc|| C enc
	static int GSyntaxE_PS = 0;// S,?,[c&|t]
	static int GSyntaxE_EU = 0;// S,?,[v]
//	static int GCON_PassHC=0;
//	static int GCON_UserHC=0;

	// ==============
	static int FailedButSigned = 0;// sig is NOT null but parser failed
	static int FailedQparser = 0;// sig is null
	static int FailedSlicer = 0;
	static int SEC_PS = 0;// -,-,null
	static int SEC= 0;// -,-,null
	static int SEC_WL = 0;// -,?,[v,?]
	static int SEC_CONST = 0;
	static int QV = 0; // Q,?,[v]
	static int QCT = 0; // Q,?,[c&|t]
	static int QA = 0; // Q,?,[a]
	static int CONN = 0;// P is hc or/and dt || U is hc|| C enc
	static int SyntaxE_PS = 0;// S,?,[c&|t]
	static int SyntaxE_EU = 0;// S,?,[v]
	static List<String> failedCasesFiles = new ArrayList<>();
	static List<String> QVFiles = new ArrayList<>();

	public static void clearCounters() {//It's called for each file
		FailedButSigned = 0;
		FailedQparser = 0;// sig is null
		FailedSlicer = 0;
		SEC_PS = 0;// -,-,null
		SEC = 0;// -,-,null
		SEC_WL = 0;// -,?,[v,?]
		SEC_CONST = 0;
		QV = 0; // Q,?,[v]
		QCT = 0; // Q,?,[c&|t]
		QA = 0; // Q,?,[a]
		CONN = 0;
		SyntaxE_PS = 0;// S,?,[c&|t]
		SyntaxE_EU = 0;// S,?,[v]
		//============
		credLFixed = false;
		credLines = new ArrayList<>();

	}

	public static void clearGloCounters() {
		GFailedButSigned = 0;
		GFailedQparser = 0;// sig is null
		GFailedSlicer = 0;
		GSEC_PS = 0;// -,-,null
		GSEC = 0;// -,-,null
		GSEC_WL = 0;// -,?,[v,?]
		GSEC_CONST = 0;
		GQV = 0; // Q,?,[v]
		GQCT = 0; // Q,?,[c&|t]
		GQA = 0; // Q,?,[a]
		GCONN = 0;
		GSyntaxE_PS = 0;// S,?,[c&|t]
		GSyntaxE_EU = 0;// S,?,[v]
	}

	public static void addCountersto_GCounters() {
		GFailedQparser = GFailedQparser + FailedQparser;// sig is null
		GFailedSlicer = GFailedSlicer + FailedSlicer;
		GFailedButSigned = GFailedButSigned + FailedButSigned;
		GSEC_PS = GSEC_PS + SEC_PS;// -,-,null
		GSEC =  GSEC + SEC;
		GSEC_WL = GSEC_WL + SEC_WL;// -,?,[v,?]
		GSEC_CONST = GSEC_CONST + SEC_CONST;
		GQV = GQV + QV; // Q,?,[v]
		GQCT = GQCT + QCT; // Q,?,[c&|t]
		GQA = GQA + QA; // Q,?,[a]
		GCONN = GCONN + CONN;
		GSyntaxE_PS = GSyntaxE_PS + SyntaxE_PS;// S,?,[c&|t]
		GSyntaxE_EU = GSyntaxE_EU + SyntaxE_EU;// S,?,[v]
	}

	public static void print_file_results(String filePath) {
		System.out.print("Path: " + filePath);
		int V = 0, S = 0;
		System.out.println("\nFailedQparser  " + FailedQparser + "\nFailedSlicer   " + FailedSlicer
				+ "\nFailedButSigned: " + FailedButSigned + "\nSEC_PS  " + SEC_PS + "\nSEC  " + SEC + "\nSEC_WL  " + SEC_WL
				+ "\nSEC_CONST	" + SEC_CONST + "\nV - QV  " + QV + "\nV - QCT  " + QCT + "\nV - QA  " + QA
				+ "\nSyntaxE_PS  " + SyntaxE_PS + "\nSyntaxE_EU  " + SyntaxE_EU + "\nSecure: "
				+ (SEC_PS +SEC + SEC_WL + SEC_CONST) + "\nSQLI Vulnerable: " + (QV + QCT + QA) + "\nConn Vulnerable: "+ CONN
				+ "\n****************************************************************\n");
	}

	public static void print_all_results(String projectName) throws IOException {
		int all_SQLIVs = GQV + GQCT + GQA;
		System.out.println("************ " + projectName + " Stats ************" + "\nGFailedQparser  " + GFailedQparser
				+ "\nGFailedSlicer   " + GFailedSlicer + "\nGFailedButSigned: " + GFailedButSigned + "\nGSEC_PS  "
				+ GSEC_PS + "\nGSEC  " + GSEC + "\nGSEC_WL  " + GSEC_WL + "\nGSEC_CONST	" + GSEC_CONST + "\nGQV  " + GQV + "\nGQCT  " + GQCT
				+ "\nGQA  " + GQA + "\nGSyntaxE_PS  " + GSyntaxE_PS + "\nGSyntaxE_EU  " + GSyntaxE_EU + "\nSecure: "
				+ (GSEC_PS + GSEC + GSEC_WL + GSEC_CONST) + "\nSQLI Vulnerable: " + all_SQLIVs  // (GQV+ GQCT+ GQA)+
				+ "\nConn Vulnerable: "+ GCONN +
				"\n****************************************************************\n");

		PC.print_all_GPatches_results(projectName, all_SQLIVs);
		PC.set_GPatches_counters();// For each App

		clearGloCounters();
	}

	public static AppMD getAppMD(Info info) {
		AppMD app_md = new AppMD();
		app_md.app_sql = info.getArg();
		app_md.app_stmt = info.getStmtVar();
		app_md.ps_query = QParser.PS_query;// SignSlice.qp.get_pstmt_query();
		app_md.wl_query = QParser.WL_query;// SignSlice.qp.getWL_query();
		app_md.soltion = QParser.prefered_sol;
		String setStrings = "";
		boolean firstSet = true;
		for (String set : QParser.set_strings) {// SignSlice.qp.get_set_strings()) {
			if (set.contains("; ;")) {
				set = set.replace("; ;", ";");
			}

			if (firstSet) {
				firstSet = false;
				setStrings = set;
			} else {
				setStrings = setStrings + "\n" + set;
			}
		}
		app_md.setStrings = setStrings;
		return app_md;
	}
	public static void collect_GPatches_stat(Sig s, boolean print, boolean is_ps, boolean slicerfailed,
			boolean patch_is_found) {
		// 1- compute time
		PC.setGPatch_end();
		PC.computeGPatch_time_ms();
		PC.addToTimesList(PC.getGPatch_time());

		if (print) {
			System.out.print("Time needed to locate and fix the vulnerable = " + PC.getGPatch_time() + " ms");
			System.out
					.print("Time needed to locate and fix the vulnerable = " + PC.computeGPatch_time_second() + " sec");
		}
		// 2- classify patches use "s" and "QParser"
		// Sig s = new Sig();
		if (patch_is_found) {
			PC.GPatches++;

			if (s.get_V() == 'P' || s.get_V() == 'U' || s.get_V() == 'C') {
				PC.FSGPatches++;//??
				PC.FSGPatches_Conn++;
				return;
			}
			if (QParser.prefered_sol == G.SOLPS || is_ps) {
				PC.FSGPatches++;
				PC.FSGPatches_PS++;
				if (print) System.out.println("============> PS");
			} else if (QParser.prefered_sol == G.SOLWL) {
				if (!QParser.ParserFail && s.get_I().contains('t')) {
					PC.FSGPatches_WL++;
					if (print) System.out.println("============> WL???");
				} else if (QParser.ParserFail) {
					PC.FGPatches++;
					if (print) System.out.println("============> WL_QPFailed???");
				}
			}

		} else {// if(!patch_is_found)
			PC.NoGPatches++;
			if (s.get_V() == 'P' || s.get_V() == 'U' || s.get_V() == 'C') {
				PC.NoGPatches_Conn++;
				return;
			}
			if (QParser.prefered_sol == G.SOLPS || is_ps) {
				PC.NoGPatches_PS++;
			} else if (QParser.prefered_sol == G.SOLWL) {
				PC.NoGPatches_WL++;
			} else if (QParser.prefered_sol == G.NOSOL || s.get_I().contains('a')
					|| (s.get_I().contains('v') && s.get_I().contains('t'))) {
				PC.NoGPatches_NoSol++;
				System.out.println("============> No GP NOSOL (" + s.get_I().toString() + ")");
			}
//			 else if(s.get_I().contains('a')) {
//				 PC.NoGPatches_NoSol++;
//				 System.out.println("============> 'a' all SQL is UI");
//			 }
		}

	}

	public static void collect_stat(Sig s, boolean print, boolean is_ps, boolean slicerfailed, String content) {
		if (s == null) {
			if (print) {
				System.err.println("Parser wasn't able to parse the query." + SignSlice.qp.get_msg());
			}
			FailedQparser++;
		} else if (s.get_V() == '_') {// -,-,null
			if (print) {
				System.out.println("BRANCH 5 " + s);
			}
			if ((s.get_T() == '_' && (s.get_I() == null || s.get_I().size() == 0))) {// || is_ps) {
				if (print) {
					System.out.println("BRANCH 5.1 " + s);
				}
				if (is_ps) {
					SEC_PS++;
				}else {
					SEC++;
				}
			} else if ((s.get_T() == '_' && s.get_I().contains('_'))) {// ++++++ 10/25
				if (print) {
					System.out.println("BRANCH 5.2 " + s);
				}
				SEC_CONST++;
			} else {// ++++ ??
				if (print) {
					System.out.println("BRANCH 5.3 " + s);
				}
				SEC_WL++;
			}
		} else if (s.get_V() == 'q' || s.get_V() == 'Q') {
			if (print) {
				System.out.println("BRANCH 6 " + s);
			}
//			if (SignSlice.qp.parserFailed()) {///+++++9/30
			if (QParser.ParserFail) {// || SignSlice.qp.parserFailed()) {///+++++9/30
				FailedButSigned++;
			}
			if (s.get_I().contains('v') && !s.get_I().contains('c') && !s.get_I().contains('t')) {
				if (print) {
					System.out.println("BRANCH 6.1 " + s);
				}
				QV++; // Q,?,[v]
				QVFiles.add(content);
			} else if (!s.get_I().contains('v') && (s.get_I().contains('c') || s.get_I().contains('t'))) {
				if (print) {
					System.out.println("BRANCH 6.2 " + s);
				}
				QCT++; // Q,?,[c&|t]
			} else if (s.get_I().contains('a') || (s.get_I().contains('v') && s.get_I().contains('t'))) {
				if (print) {
					System.out.println("BRANCH 6.3 " + s);
				}
				QA++; // Q,?,[a]
				if (slicerfailed) {
					FailedSlicer++;
				}
			} else if (s.get_I().get(0).equals('-')) {
				SEC_CONST++;
			}
		} else if (s.get_V() == 'S') {
			if (print) {
				System.out.println("BRANCH 7 " + s);
			}
			if (is_ps) {
				if (print) {
					System.out.println("BRANCH 7.1 " + s);
				}
				SyntaxE_PS++;// S,?,[c&|t]
			} else {
				if (print) {
					System.out.println("BRANCH 7.2 " + s);
				}
				SyntaxE_EU++;// S,?,[v]
			}
		} else if (s.get_V() == 'F') {
			if (print) {
				System.out.println("BRANCH 8 " + s);
			}
			if (slicerfailed) {
				FailedSlicer++;
			} else {
				FailedQparser++;
			}
			failedCasesFiles.add(content);
			// System.out.println(content + ":\t" + query);
		}else if (s.get_V() == 'P' || s.get_V() == 'U' || s.get_V() == 'C') {//
			CONN++;
		}
	}

	public static boolean has_ps(List<SliceLine> sliceLines) {

		for (SliceLine sl : sliceLines) {
			if (sl.stmt.contains("prepareStatement")) {
				return true;
			}
		}
		return false;
	}

	public static boolean print_sliceLines(List<SliceLine> sliceLines) {

		for (SliceLine l : sliceLines) {
			System.out.println(l.lno + " @ " + l.stmt);
		}
		System.out.println(" ----------------\n");
		return false;
	}

	public static List<SliceLine> createBackupSlice(Info info, String srcPath, String slicePath, String contextPath,
			boolean print) throws IOException {
		if (print) {
			System.out.println("@@ createBackupSlice");
		}
		List<SliceLine> sliceLines = new ArrayList<>();
		List<String> codeLines = new ArrayList<>();
		ArrayList<Integer> callLines = new ArrayList<>();
		callLines.addAll(info.getCall_lines());
		ArrayList<Integer> queryLines = new ArrayList<>();
		queryLines.addAll(info.getQuery_lines());
		System.out.println("Slice: callLines : " + callLines + ", queryLines: " + queryLines);
		ArrayList<Integer> sliceCodeLineNos = new ArrayList<>();
		int clast = callLines.size() - 1;
		int qlast = queryLines.size() - 1;
		if (callLines.size() > 0 && queryLines.size() > 0) {
			if ((callLines.get(0) < queryLines.get(0) || callLines.get(0) == queryLines.get(0))
					&& (queryLines.get(0) < callLines.get(clast) || queryLines.get(0) == callLines.get(clast))) {
//			overlab = true;
				sliceCodeLineNos = callLines;
			} else if (queryLines.get(0) > callLines.get(clast)) {
//			overlab = false;
//		 	callLines + queryLines
				sliceCodeLineNos = callLines;
				sliceCodeLineNos.addAll(queryLines);
			} else if (queryLines.get(qlast) < callLines.get(0)) {
				sliceCodeLineNos = queryLines;
				sliceCodeLineNos.addAll(callLines);
			}
		} else {
			sliceCodeLineNos.addAll(callLines);
		}
//		2- get lines in array list to get code lines
		codeLines = StrUtil.read_lines_list(srcPath);
//		3- build sliceLines

		for (Integer i : sliceCodeLineNos) {
			if (print) {
				System.out.print(i + ", ");
			}
			SliceLine sl = new SliceLine();
			sl.lno = i;
			sl.stmt = codeLines.get(i - 1);
			sl.method = info.getMethod();
			sl.walaStmt = null;
			sliceLines.add(sl);

		}
		
		if (sliceLines.size() > 1 ) {//&& print
			System.out.println("--  SLice :");
			print_sliceLines(sliceLines);

		}
		if (sliceLines.size() > 0) {
			String slice_srclines = "";
			String context_srclines = "";
			String md_cq_setStrings = "";
			Sig s = new Sig();
			int sf_lno = 1; // slice file lno
			slice_srclines = "public class Slice {public static void main() {";// lno= 1
			for (SliceLine sl : sliceLines) {
//				System.out.println(">>> "+sl);
				// +++++
				if (sl.stmt.contains("createStatement")) {
					continue;
				}
				sf_lno++;
				// ++++++
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

			// Write slice & context to files
			StrUtil.write_tofile(slicePath, slice_srclines);

			//StrUtil.write_tofile(contextPath, context_srclines);

		}
		
		return sliceLines;
	}


	public static List<SliceLine> createBackupSliceConn(Info info, String srcPath, String slicePath, String contextPath,
			boolean print) throws IOException {
		if (print) {
			System.out.println("@@ createBackupSliceConn");
		}
		List<SliceLine> sliceLines = new ArrayList<>();

		List<String> codeLines = new ArrayList<>();
		// ----- getconnection lines
		ArrayList<Integer> callLines = new ArrayList<>();
		callLines.addAll(info.getCall_lines());
		// ----- user and pass lines
		ArrayList<Integer> credintalLines = SlicerUtil.get_credintal_lines(info);

		ArrayList<Integer> sliceCodeLineNos = new ArrayList<>();
		sliceCodeLineNos.addAll(callLines);
		sliceCodeLineNos.addAll(credintalLines);
		sliceCodeLineNos = StrUtil.removeDuplicates(sliceCodeLineNos);
		Collections.sort(sliceCodeLineNos);


		System.out.println("Slice: callLines : " + callLines + ", credintalLines: " + credintalLines + ", allLinesNos:" +sliceCodeLineNos);
//		2- get lines in array list to get code lines
		codeLines = StrUtil.read_lines_list(srcPath);
		//=========== get g  credLines
		if(credintalLines.size()>0 && !credLFixed) {
			credLines.addAll(credintalLines);
//		for(Integer cl: credintalLines) {
//			credLines.add(codeLines.get(cl - 1));
//		}
		}
//		3- build sliceLines

		for (Integer i : sliceCodeLineNos) {
			if (print) {
				System.out.print(i + ", ");
			}
			SliceLine sl = new SliceLine();
			sl.lno = i;
			sl.stmt = codeLines.get(i - 1);
			sl.method = info.getMethod();
			sl.walaStmt = null;
			sliceLines.add(sl);

		}
//
		SlicerUtil.write_slice_to_file(sliceLines, slicePath, contextPath);
		if (sliceLines.size() > 1 ) {//&& print
			System.out.println("--  SLice Lines:");
			print_sliceLines(sliceLines);

		}
		return sliceLines;
	}

	public static void sort_info(List<Info> infoSet) {

		infoSet.sort((o1, o2) -> Integer.valueOf(o1.getLno()).compareTo(Integer.valueOf(o2.getLno())));
	}

	public static void fault_localization(int cpLength, CGClass appCG, String appJar, String filePath, String projName,
			boolean print) throws Exception {
		// TODO: $$$43 check if location to reset values is correct
		// ===== For each class clear counters and global values
		G.LastLineModified = 0;
		G.AppLines_partlyFixed.clear();
		G.AppLines_partlyFixedNos.clear();

		clearCounters();// Per class
		// 1- Time to locate a vul, and fix
		//long start_locate_one_vul = System.currentTimeMillis();

		// 2- TP, TN, FP, FN
		// P =

		// ============ DCAFIXER STEPS:
		// ================= 1. Find the IP lines =================
		// -------- Use Extractor, then loop over IPs
		System.out.println(filePath);
		String file_name = StrUtil.get_filename(filePath);
		System.out.println("################## " + file_name + " ##################");
		String file_folder = StrUtil.get_folder_path(filePath);// *
		ExtractQuery.Extractor(file_folder, file_name, 0);
//		ExtractQuery.printAll();
//		System.out.println("*** assignList ***\n"+ExtractQuery.assignList.toString());
//		System.out.println("*** assignListCalls ***\n"+ExtractQuery.assignListCalls.toString());
//		System.out.println("*** infoSet ***\n"+ExtractQuery.infoSet.toString());
//		System.out.println("=====================================\n");
		// ================= 2. Slice & Sign =================

//		Collections.sort((List<Info>) ExtractQuery.infoSet);

		sort_info(ExtractQuery.infoSet);
//		for(Info info : ExtractQuery.infoSet) {
//			System.out.println("LN:"+info.getLno());
//		}
		for (Info info : ExtractQuery.infoSet) {
			// ===== For each IP in a class
			QParser.clear_all_values("");

			PC.set_GPatches_times();
			PC.setGPatch_start();// Start Time to locate a vul, and fix
			// --------------- 2.1) slice check that query is complete, path:
			// /Users/dareen/Fixer/tmp/TSet/Slices/AppVSlice =================
			int v_lno = info.getLno();
			String v_appSrc = filePath;
			String slices_and_context_path = G.slicesTmpPath + projName;
			String md_file = "/" + file_name.replace(".java", "") + "_md.txt";
			String v_slice_file = "/" + file_name.replace(".java", "") + v_lno + "_VSlice.java";
			String v_context_file = "/" + file_name.replace(".java", "") + "_context.txt";
			String mdPath = slices_and_context_path + md_file;// "/md.txt";// +
			String v_slicePath = slices_and_context_path + v_slice_file;// "/VSlice.java";// + v_slice_file;
			String v_contextPath = slices_and_context_path + v_context_file;// "/context.txt";
//			String mdPath = G.sliceTmpPath + "/md.txt";//md_file;
//			String v_slicePath = G.sliceTmpPath + "/VSlice.java";//v_slice_file;
//			String v_contextPath = G.sliceTmpPath + "/context.txt";//v_context_file;
			String[] v_subpath = v_appSrc.split("/");

			String v_className_only = (v_subpath[v_subpath.length - 1].split("\\."))[0];// v_subpath[v_subpath.length -
																						// 1].replace(".java", "");
			String v_path_className = "";
			String v_classPath = "";
			if (cpLength == 1) {
//				//----------------------- Option 1
				v_classPath = v_subpath[v_subpath.length - 2] + "/";// TSet/
				v_path_className = v_classPath + v_className_only;

			} else if (cpLength == 2) {
//				//----------------------- Option 2
				v_classPath = v_subpath[v_subpath.length - 3] + "/" + v_subpath[v_subpath.length - 2] + "/";// TSet/
				v_path_className = v_classPath + v_className_only;

			} else if (cpLength == 3) {
				// ----------------------- Option 3 - "src" folder
				boolean src_found = false;
				for (int i = 0; i < v_subpath.length - 1; i++) {
					if (src_found) {
						v_path_className = v_path_className + v_subpath[i] + "/";
					}
					if (v_subpath[i].equals("src")) { // For other cases
						src_found = true;
					}
				}
				if (!src_found) {
					System.err.println("NO \"src\" folder in the path");
				}
				v_path_className = v_path_className + v_className_only;
				// -----------------------
			} else {
				// ----------------------- Option N
				boolean src_found = false;
				for (int i = 0; i < v_subpath.length - 1; i++) {
					if (src_found) {
						v_path_className = v_path_className + v_subpath[i] + "/";
					}
//					if(v_subpath[i].equals("src"))//For other cases
					if (v_subpath[i].equals("java")) { // for mariadb and Momow1
						src_found = true;
					}
				}
				if (!src_found) {
					System.err.println("NO \"java\" folder in the path");
				}
				v_path_className = v_path_className + v_className_only;
				// -----------------------

			}

			if (print) {
				System.out.println("v_path_className: " + v_path_className + ", lno:" + v_lno + " key: " + info.getKey()
						+ "\n Slice path: " + v_slicePath);
			}

			boolean failToParse = false, failToSlice = false, allQueryisUI = false, codeIsSecure = false,
					query_is_constant = false;
			String key = info.getKey();
			Sig s = new Sig();
//			--------------- 2.2) Sign to mark SQL Vuls ---------
			List<SliceLine> sliceLines = new ArrayList<>();
			boolean is_ps = false;
//			System.out.println("KKKKKey: "+key);
//			"getConnection", "Properties"
			if (G.ConnIPList.contains(key)) {// ---huna
				// Handle password cases!

				char v_sliceDir, v_sstype;
				if (key.equals("getConnection")) {
					v_sliceDir = 'B';
				} else {
					v_sliceDir = 'F'; //Properties
				}
				v_sstype = 's';
				if(sySetFlag) {
					continue;
				}


//				sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc, v_path_className,
//						v_sliceDir, v_lno, key, v_sstype, v_slicePath, v_contextPath, mdPath,
//						DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery(), appCG);

				System.out.println(info);
				sliceLines = createBackupSliceConn(info, v_appSrc, v_slicePath, v_contextPath, print);
				System.out.println("###############");

				// Sign the IPs:
				s.setvalues('_', '_', null);
				// No Enc
				boolean connHasNoEnc = false;
				if (info.getConn_url_value().toLowerCase().contains(G.encrSetting)) {
					s.setvalues('C', 'E', null);
					connHasNoEnc = true;
					System.out.println(s + " @ " + info.getLno() + "\n" + G.MSG_Conn_noEnc);
				}
				// PW HC or/and datatype
				if (info.getConn_pass_value_type().equals(G.constantValue) && info.getConn_pass_dt().equals("String")) {
//					List<Character> I = new ArrayList<>();
//					I.add('d');
//					I.add('h');
					s.setvalues('P', 'B', null);// 'B' stands for both HC and DT
					System.out.println(s + " @ " + info.getLno() + "\n" + G.MSG_P_HD);

				} else if (info.getConn_pass_value_type().equals(G.constantValue)) {
					s.setvalues('P', 'h', null);
					System.out.println(s + " @ " + info.getLno() + "\n" + G.MSG_P_H);
				} else if (info.getConn_pass_dt().equals("String")) {
					s.setvalues('P', 'd', null);
					System.out.println(s + " @ " + info.getLno() + "\n" + G.MSG_P_D);
				}
				// UN is HC
				if (info.getConn_user_value_type().equals(G.constantValue)) {
					s.setvalues('U', 'h', null);
					System.out.println(s + " @ " + info.getLno() + "\n" + G.MSG_U_H);
				}
				//P-B, P-h
				collect_stat(s, false, is_ps, failToSlice, filePath);

				// #20201# Find and Apply patches for conn vulnerabilities
				//continue;
				String vSlicesPath = G.CONN_VSlicesPath;
				if (print) {
					System.out.println("CONN VSlicesPath: " + vSlicesPath);
				}
//				File sf = new File(v_slicePath);
//				if (!sf.exists()) {
//					System.out.print("CONN VSlices File does exist!");
//					continue;
//				}
					if (s.get_V() != '_') {// if not secure
						RplacePHsConn.replacePlaceHolders_and_createTmpVSlices(vSlicesPath, info, false);
						// Find app_md
						if (PatternGenConn.findPatternsAndApply_usingExistingPatchesConn(v_appSrc, v_slicePath,
								G.CONN_TmpVSlicesPath, info, getAppMD(info), connHasNoEnc, credLFixed, false)) {
//							System.out.println("+ddd++++++++++++++++++++");
							if(print) {
								for(int i= 0 ; i<G.AppLines_partlyFixedNos.size(); i++) {
									System.out.println(G.AppLines_partlyFixedNos.get(i) + "@ " + G.AppLines_partlyFixed.get(i));
								}
							}
//							System.out.println("++dddd++++++++++++++++++");
							System.out.println("Conn vulnerability at line: " + info.getLno() + " was patched with No errors!");
							// ======================== %4040% Patch is Found
							//
							// TODO: create collect_GPatches_stat();
							// boolean print, boolean is_ps, boolean slicerfailed
							collect_GPatches_stat(s, print, is_ps, failToSlice, true);

						} else {
							System.out.println("Vulnerability at line: " + info.getLno() + " was Not patched!");
							collect_GPatches_stat(s, print, is_ps, failToSlice, false);
						}
					}
					System.out.println("================================================================");

			}
			// No need to handle createStatement case. The slices, even w/ full CD and DD,
			// will have only createStatement line!
			// Also, we don't want to touch the create statement as it could be used with
			// other cases!
//			else if(key.contains("createStatement")) {
//				char v_sliceDir = 'F';
//				char v_sstype = 's';
//				sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc, v_path_className, v_sliceDir, v_lno, key, v_sstype,
//						v_slicePath, v_contextPath, mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery(), appCG);
//			}
			else if (key.contains("executeUpdate") || key.contains("executeQuery") || key.contains("execute")) {// if(G.ExIPList.contains(key))
																												// {//
//				System.out.println("HERE 1!");
				char v_sliceDir = 'B';
				char v_sstype = 's';
				if (info.getQuery().equals("ps")) {

					// Slice to find ps line# from the slice
					if (print) {
						System.out.println("HERE 1!");// , "+ info.toString());
					}
					s.setvalues('_', '_', null);
					codeIsSecure = true;
					/*
					 * find_slice_and_context(String appJar, String appSrc, String
					 * app_folder_className, char sliceDirection, int lno, String seed, char sstype,
					 * String slicePath, String contextPath, String mdPath,DataDependenceOptions
					 * dOptions, ControlDependenceOptions cOptions, char appType)
					 */

					// TODO: Search the infoSet for the corresponding ps by line#

				} else if (info.getQuery().equals("parameter")) {

					if (print) {
						System.out.println("HERE 2!");// , "+ info.toString());
					}
					// All query is an arg
					// slice to check if the value in the other function is constant
					// parse and check if there is a value for the query
					// find_slice_and_context_query_sent_cgtype
					sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc, v_path_className,
							v_sliceDir, v_lno, key, v_sstype, v_slicePath, v_contextPath, mdPath,
							DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v', info.getQuery(), appCG);
					if (sliceLines != null) {
						if (has_ps(sliceLines)) {

							s.setvalues('_', '_', null);// No vulnerability
							codeIsSecure = true;
						} else {
							// TODO: create a signature
							// Fail to parse query
							List<String> uiVar = new ArrayList<>();
							uiVar.add(info.getArg());
							QParser.parse_query_dt(info.getQuery(), print, uiVar, info.getQuery_type());
//							List<Character> Is = new ArrayList<>();
//							Is.add('a');
//							s.setvalues('Q', '_', Is);

							s = QParser.qs_ql;
							if (s.get_I().get(0).equals('-')) {
								query_is_constant = true;
							} else if (s.get_I().contains('a')) {
								allQueryisUI = true;
							}
							System.out.println("s: " + s + ", QParser.qs_ql: " + QParser.qs_ql.toString());
						}
					} else {// sliceLines is null, #2020# but we could know from the parser
//						// Fail to Slice the code
//						s.setvalues('F', '_', null);
						List<Character> Is = new ArrayList<>();
						Is.add('a');
						s.setvalues('Q', '_', Is);
						allQueryisUI = true;
						failToSlice = true;// Fail to Slice
						sliceLines = createBackupSlice(info, v_appSrc, v_slicePath, v_contextPath, print);
						// List<SliceLine>
					}

				} else if (info.getQuery().equals(null) || info.getQuery().equals("")) {
					if (print) {
						System.out.println("HERE 3!,   " + info);
					}
					// TODO: create a signature F,_, _ failed to parse it
					// - Try from the slice
					s.setvalues('F', '_', null);
					failToParse = true;
				} else {
					if (print) {
						System.out.println("HERE 4!");// , "+ info.toString());
					}
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
							s.setvalues('_', '_', null);// No vulnerability
							codeIsSecure = true;
						} else {
							if (print) {
								System.out.println("HERE 4.1! " + info.getQuery());
							}
//					s = SignSlice.sign_slice_querystring_exp1_temp(v_slicePath,key, v_sliceDir, info.getQuery(), false, null );
							sliceLines = EBCreation.find_slice_and_context_query_sent_cg(appJar, v_appSrc,
									v_path_className, v_sliceDir, v_lno, key, v_sstype, v_slicePath, v_contextPath,
									mdPath, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 'v',
									info.getQuery(), appCG);
							if (sliceLines == null) {
								failToSlice = true;
								sliceLines = createBackupSlice(info, v_appSrc, v_slicePath, v_contextPath, print);
							}

							QParser.parse_query_dt(info.getQuery(), false, null, info.getQuery_type());
							s = QParser.qs_ql;
							if (s.get_I().get(0).equals('-')) {
								query_is_constant = true;
								if (print) {
									System.out.println(info + "\n" + s + "\n&&&&&&&&&&ffff&&&&&&\n");
								}
							} else if (s.get_I().contains('a')) {
								allQueryisUI = true;
							}
							if (QParser.ParserFail) {
								failToParse = true;
							}
//					String path, String key, char dir, String query, boolean print, List<String>uiVars
						}
					} else {
//						// Fail to Slice the code
						s.setvalues('F', '_', null);
						failToSlice = true;
						sliceLines = createBackupSlice(info, v_appSrc, v_slicePath, v_contextPath, print);
//						if(sliceLines != null) {
//							failToSlice = false;// Fail to Slice
//						}else{
//							failToSlice = true;// Fail to Slice
//						}
					}
				}
//				if(s != null) {
				collect_stat(s, false, is_ps, failToSlice, filePath);// Secure and failed cases signatures are to
																		// collect stats

				System.out.println(info + "\n" + s + "\nPreferred sol: " + QParser.prefered_sol );
				if (print) {
					System.out.println(info + "\n" + s + "\nPrefered sol: " + QParser.prefered_sol + "\nParser failed? "
						+ QParser.ParserFail + " " + failToParse + "\nSlicer failed? " + failToSlice
						+ "\n=======================\n");
				}
			} // --- End of Execute cases EQ, EU, and E

//			if(sliceLines.size()>0)
//				System.out.println(sliceLines.toString());
//				for(SliceLine l :sliceLines )System.out.println(l.toString());
//			collect_stat(s,false, is_ps,filePath);// Secure and failed cases signatures are to collect stats

			// ===================================================================

			// ============== collect stats here and add counters

			if (allQueryisUI) {// s.get_I()!= null && s.get_I().contains('a')) {
				collect_GPatches_stat(s, print, false, failToSlice, false);
			}
			if (sliceLines == null) {
				collect_GPatches_stat(s, print, false, true, false);
			}

			// #2020# ========================= Find patches here for SQLI Vuls...
			if (!(allQueryisUI || codeIsSecure || query_is_constant)
					&& s != null && sliceLines != null && !key.equals("getConnection")) {
//				================= 3.	get SSlicesPath =================
				// ===== After reading one vulnerability , we try to fix one at a time.
//				if(!(failToParse || allQueryisUI|| codeIsSecure || query_is_constant || failToSlice ) && s != null ) {
//				if(!(failToParse || allQueryisUI|| codeIsSecure || query_is_constant ) && s != null && sliceLines != null ) {

//			List<String> set_strings = new ArrayList<String>();
				String vSlicesPath = G.SQLI_VSlicesPath;
//			String SSlicesPath = SignSlice.get_slices_folder_path(s,"");
//			SSlicesPath = SignSlice.get_slices_folder_path_exp4(s,"");
//			set_strings = QParser.set_strings;//SignSlice.qp.get_set_strings();
//			 String s2= QParser.qs_ql.toString();//SignSlice.qp.toString();
				if (print) {
					System.out.println("VSlicesPath: " + vSlicesPath);
//				 System.out.println("~~~~~"+SignSlice.qp.get_pstmt_query());
//				 System.out.println("~~~~~"+SignSlice.qp.get_set_strings().toString());
				}
				if (vSlicesPath == null) {
					System.err.println("VSlicesPath: is null");// due to: ("+s.toString() +"|"+s2+"), "+s2);
					// continue;
				}
				// TODO: from the patch get the number of the new fixed "execute" call.
				// This will help to check if the new code is fixed

//				================= 4.	replace phs and create new SSlices in SSlicesPath+"/tmp” =================
				// TODO: SSLICES path for DDL commands (Alter, drop ,...
				// TODO: For patches uncomment below call. RplacePHs.replacePlaceHolders.. , &
				// PatternGen.findPatternsAndA ...
//				RplacePHs.replacePlaceHolders(v_slicePath, SSlicesPath, info.getApp_conn(),
//						info.getApp_rs(), info.getArg(), info.getStmtVar(),info.getApp_rs_dt(),info.getKey(), false);//"" should be stmt var name
				// replace
				// %%%%%%%

//			 try (BufferedReader br = new BufferedReader(new FileReader(v_slicePath))) {}
//			 catch (FileNotFoundException e){
//				    System.out.println(e);
//				}catch (IOException e) {System.out.println(e);}

				File sf = new File(v_slicePath);
				if (sf.exists()) {
					RplacePHs.replacePlaceHolders_and_createTmpVSlices(vSlicesPath, info, false);
//			 replacePlaceHolders_info
//				================= 5.	Call find diff all && Apply patches  =================

//				 String vulApp , String vsPath, String sslicesPath,  Info info,  AppMD amd, boolean print
					// %%%%%%%%%%%
					if (print) {
						System.out.println("BBB findPatternsAndApply_usingExistingPatches");
					}
					// TODO: $$$43 check results
					if (PatternGen.findPatternsAndApply_usingExistingPatches(v_appSrc, v_slicePath,
							G.SQLI_TmpVSlicesPath, info, getAppMD(info), false)) {
						System.out.println("Vulnerability at line: " + info.getLno() + " was patched with No errors!");
						// ======================== %4040% Patch is Found
						//
						// TODO: create collect_GPatches_stat();
						// boolean print, boolean is_ps, boolean slicerfailed
						collect_GPatches_stat(s, print, is_ps, failToSlice, true);

					} else {
						System.out.println("Vulnerability at line: " + info.getLno() + " was Not patched!");
						collect_GPatches_stat(s, print, is_ps, failToSlice, false);
					}
					System.out.println("================================================================");
				}
				// ============ 10/12th XXXXXXXXXXXXXXXXXXXXXX
				// TODO: For patches uncomment below calls.
//			      if (QParser.prefered_sol == 1)
//			      PatternGen.findPatternsAndApply_exp4(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, info.getQuery_lines(), QParser.PS_query, false);
//			      else if (QParser.prefered_sol == 2)
//			    	  PatternGen.findPatternsAndApply_exp4(  v_appSrc ,v_slicePath, SSlicesPath+"/tmp", v_contextPath, app_md, info.getQuery_lines(), QParser.WL_query, true);
				// ================= 6. count Number of file that doesn’t have syntax error
				// =================
			} else {
				if (s == null) {
					System.out.println("s in Null");
//				if(failToParse && s.get_I().get(0).equals('t')) {
//					//TODO: Apply WL sol
//				}
				}

			}
			failToParse = false;
			allQueryisUI = false;
			codeIsSecure = false;
			codeIsSecure = false;
			failToSlice = false;
			// ************************************

		} // ---------- END of loop over infoSet
		System.out.println("###############################################################");
		print_file_results(filePath);
		addCountersto_GCounters();
		// TODO print Stats

	}

	public static void start_dcafixer(String projectName, String projectSrc, String projectJar) throws Exception {
		String CsvDir = G.CsvDir;
		long start = System.currentTimeMillis();
		// ----- GPatches stats file header
//		StrUtil.write_tofile(G.GPatchesTmpPath,
//				"App_name,all_SQLIVs,GPatches,FSGPatches,FSGPatches_PS,FSGPatches_WL,FGPatches,NoGPatches,NoGPatches_NoSol,NoGPatches_PS,NoGPatches_WL,Avg Time\n");

		String resultCsv = G.CsvDir+ projectName+".csv";

		String reportDir = G.ResultsPath +"/"+projectName+"/report.txt";
		File outFile = StrUtil.createOrRetrieveFile(reportDir);

		PrintStream o100 = new PrintStream(outFile);
		System.setOut(o100);
		LocatingPIs.analyze_project(projectJar, projectSrc, resultCsv, false, 0, 0);

		System.out.println("Start building the CG .......");
		CGClass appCGPEx = EBCreation.BuildCG(projectJar, G.CG_AllEPoints_MyExGUI);
		System.out.println(CallGraphStats.getStats(appCGPEx.getCg()));
		System.out.println("Done building the CG .......");

		for (String f : LocatingPIs.SCList) {
			Fixer.fault_localization(3, appCGPEx, projectJar, f, projectName, false);
		}
		Fixer.print_all_results(projectName);

		long end = System.currentTimeMillis();
		float sec = (end - start) / 1000F;
		System.out.println("Done!\nTime elapsed to locate and fix the vulnerabilities is " + sec + " seconds");
		PrintStream console = System.out;
		System.setOut(console);
	}

}