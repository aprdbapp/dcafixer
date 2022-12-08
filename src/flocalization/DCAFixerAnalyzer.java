package flocalization;

import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
//import java.util.concurrent.TimeUnit;

//import com.ibm.wala.classLoader.IClass;
//import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
//import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.*;
//import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
//import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
//import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.*;
//import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.NormalReturnCaller;
import com.ibm.wala.ipa.slicer.NormalStatement;
//import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
//import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
//import com.ibm.wala.ssa.IR;
//import com.ibm.wala.ssa.SSAInstruction;
//import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;
//import com.ibm.wala.util.io.FileProvider;
//import com.ibm.wala.util.warnings.Warnings;

import slicer.utilities.SlicerUtil;
import slicer.datatypes.*;
//import slicer.tool.*;

public class DCAFixerAnalyzer {

	private static final String EXCLUSIONS = "java\\/awt\\/.*\n" + "javax\\/swing\\/.*\n" + "sun\\/awt\\/.*\n"
			+ "sun\\/swing\\/.*\n" + "com\\/sun\\/.*\n" + "sun\\/.*\n" + "org\\/netbeans\\/.*\n"
			+ "org\\/openide\\/.*\n" + "com\\/ibm\\/crypto\\/.*\n" + "com\\/ibm\\/security\\/.*\n"
			+ "org\\/apache\\/xerces\\/.*\n" + "java\\/security\\/.*\n";
	public static CallGraph cg;
	public static CallGraphBuilder<InstanceKey> builder;
	public static SrcCode src;
	//TODO : use  LinkedHashSet || TreeSet for APs
//	public static ArrayList<AanalysisPoint> APs = new ArrayList<AanalysisPoint>();
	public static List<AanalysisPoint> APs = new ArrayList<AanalysisPoint>();
	public static ArrayList<VAR_MD> VarList = new ArrayList<VAR_MD>();
	public static ArrayList<VAR_MD> InList = new ArrayList<VAR_MD>();
	public static String CName;
	public static String appSrc2;
	public static double Time_to_compute_slice;
	public static double Time_to_map_slice;
	public static double Time_to_build_cg;

	public static void start_anlysis(String appSrc, String appJar, String ClassName)
			throws IOException, IllegalArgumentException, WalaException, CancelException, InvalidClassFileException {
//				Get class name from jar file or pass it as an argument String className =
//				""; 
//				//======= Step 1: Determine what your analyzer needs to exclude & Create an
//				analysis 
//				// scope representing the appJar as a J2SE application, 
//				File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt"); File scopeFile = new FileProvider().getFile("scope.txt"); 
//				AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar, exFile); //
//				AnalysisScope scope = AnalysisScopeReader.makePrimordialScope(exFile); 
//				//AnalysisScope scope = AnalysisScopeReader.readJavaScope(jar, exFile,
//				javaLoader); 
//				// AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFile, exFile, null); 
//				//======= Step 2: Build ClassHierarchy? 
//				ClassHierarchy cha = ClassHierarchyFactory.make(scope); 
//				//======= Step 3: Build Entry point 
//				Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
//				AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
//
//				//======= Step 4: Build the call graph 
//				CallGraphBuilder<?> cgb = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope, null, null);
//
//				CallGraph cg = cgb.makeCallGraph(options, null); ArrayList<String> methods = new ArrayList<>(); methods = SlicerUtil.find_all_class_methods_from_cg(cg, jar, className); System.out.println(methods);
//
//				// Step 5: Build SDG
//
//				// Step 6: Find Seed statement
//
//				// Step 7: Compute the Slice (backward & forward)
//
//				// [6.i]: Test context-sensitive - traditional & Dump
//
//				// [6.ii]: Test context-insensitive & Dump
//
//				// [6.iii]: context-sensitive - thin slice & Dump


						 
		APs.clear();
		InList.clear();
		VarList.clear();
		CName = ClassName;
		Time_to_compute_slice = 0;
		Time_to_map_slice = 0;
		Time_to_build_cg = 0;
//		pre_taint_analysis( appSrc,  appJar,  CName);
		pre_taint_analysis(appSrc, appJar);
		// Step 1: Taint Analysis
		taint_analysis();

//		for (TA_MD md : APs) {
//			System.out.println("IP@line: " + md.lno + ", Type is: " + md.getType());
//
//		}

		// taint_analysis(appSrc, appJar, CName);

		// Step 2: Slice APs

	}

//	public static void pre_taint_analysis(String appSrc, String appJar, String CName) throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
	public static void pre_taint_analysis(String appSrc, String appJar)
			throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
		// Step 0: build the CG
		// similar to @Test - testList() .
		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java
		appSrc2 = appSrc;
		long start_cg = System.currentTimeMillis();
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
				"L" + CName);
		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);// new
																							// AnalysisOptions(scope,
																							// entrypoints);
		// CallGraphBuilder<InstanceKey>
		//Deprecated
//		builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
		builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
		//
		cg = builder.makeCallGraph(options, null);
		long finish_cg = System.currentTimeMillis();
		long timeElapsed_cg = finish_cg - start_cg;
		double sec_cg = timeElapsed_cg / 1000.0;
		Time_to_build_cg = sec_cg;
		System.out.println("\nTime Elapsed to build cg: " + timeElapsed_cg + ", sec: " + sec_cg);

		src = new SrcCode();

		ArrayList<String> methods_list = new ArrayList<>();
		methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, CName);
		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
		// System.out.print("methods_list_final:" + methods_list_final);
		src.set_values(appSrc, CName, methods_list);
//						src.load_code(appSrc);
		// System.out.println("##### 0\n");
//		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();

	}

//	public static List<SliceLine> slice_fw(String CName, int lno, String key, char seed_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions) throws InvalidClassFileException, IllegalArgumentException, CancelException {
	public static List<SliceLine> slice_fw(int lno, String key, char seed_type, DataDependenceOptions dOptions,
			ControlDependenceOptions cOptions)
			throws InvalidClassFileException, IllegalArgumentException, CancelException {

		String methodName = SlicerUtil.find_seed_method(cg, CName, lno);
		// System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, CName);
//		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);

		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
		Collection<Statement> forward_slice;

		long start_slice = System.currentTimeMillis();

		// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
		if (seed_type == 'n') {
			NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lno,
					key);

			forward_slice = Slicer.computeForwardSlice(seed_ns, cg, pa, dOptions, cOptions);
		} else if (seed_type == 'r') {
			NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lno,
					key);

			// we need a NormalReturnCaller statement to slice from the return value
			NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());

			forward_slice = Slicer.computeForwardSlice(seed_nrc, cg, pa, dOptions, cOptions);

		} else {// if(seed_type == 's')
			Statement seed_s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lno, key);
			// System.out.println("Seed stmt: " + seed_s.toString());
			System.out.println("----> Seed stmt: " + seed_s.toString() + "\n ===============");
			forward_slice = Slicer.computeForwardSlice(seed_s, cg, pa, dOptions, cOptions);
		}
		
		long finish_slice = System.currentTimeMillis();
		long timeElapsed_slice = finish_slice - start_slice;
		double sec_slice = timeElapsed_slice / 1000.0;
		Time_to_compute_slice = Time_to_compute_slice + sec_slice;
		System.out.println("\nTime Elapsed to compute the slice: " + timeElapsed_slice + ",sec: " + sec_slice);
		long start_map_slice_src = System.currentTimeMillis();

		List<SliceLine> sliceLines = new ArrayList<>();
		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice, src.methods, src.lines);

		long finish_map_slice_src = System.currentTimeMillis();
		long timeElapsed_map_slice_src = finish_map_slice_src - start_map_slice_src;
		double sec_slice_src = timeElapsed_map_slice_src / 1000.0;
		Time_to_map_slice = Time_to_map_slice + sec_slice_src;
		System.out.println("\nTime Elapsed to map the slice to src code: " + timeElapsed_map_slice_src + ", sec: "
				+ sec_slice_src);
		return sliceLines;

	}

//	public static void taint_analysis(String appSrc, String appJar, String CName)
//			throws WalaException, CancelException, IOException, InvalidClassFileException {
//		
//		public static void taint_analysis(String appSrc, String appJar)
//				throws WalaException, CancelException, IOException, InvalidClassFileException {

	public static void taint_analysis() throws WalaException, CancelException, IOException, InvalidClassFileException {

//		ArrayList<CodeLine> TA_SEEDS = new ArrayList<CodeLine>();

//		String Scanner_funs = {"nextDouble","nextFloat","nextInt","nextLine","nextLong"};
//		BufferedReader_funs = {"read"};
//		Console_funs = {"reader","readLine", "readPassword"};
//		Points_of_intrest = {"prepareStatement","executeQuery","getConnection"}; 

		// Search for lines with input class -> return list of (line#, input class
		// name)
		SrcCode appCode = new SrcCode();
		appCode.load_code(appSrc2);
//		System.out.println(">>>> xxx 1 , cname:$" + CName+"%");

		char sstype; // seed statement type
		for (CodeLine l : appCode.lines) {
			String stmt = l.getStmt();
//			System.out.println(l.lno + "#\t" + stmt);
			// System.out.println(l.getLno() + " " + l.getStmt());

			if (stmt.contains("Scanner(")) {// && !(stmt.contains("import ") && stmt.contains("java.util."))) {
				// ---------- Add var to InList --- Start
				VAR_MD in_e = new VAR_MD();
				in_e.fun = "Scanner";
				in_e.lno = l.lno;
				in_e.type = "Scanner";
				stmt = stmt.replace("try (", "");

				stmt = stmt.replace("@@@", "");

				String[] arrOfStr = stmt.split(" ", 3);
//				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@" + arrOfStr[0].replaceAll("\\s", ""));
//				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@" + arrOfStr[1].replaceAll("\\s", ""));
//				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@" + arrOfStr[2].replaceAll("\\s", ""));
				if (!arrOfStr[0].replaceAll("\\s", "").equals("Scanner"))
					in_e.name = arrOfStr[0].replaceAll("\\s", "");
				else if (arrOfStr[0].replaceAll("\\s", "").equals("Scanner"))
					in_e.name = arrOfStr[1].replaceAll("\\s", "");
				
				InList.add(in_e);
				// ---------- Add var to InList --- End
				sstype = 's';
				List<SliceLine> IN_SLICE = slice_fw(l.lno, "Scanner", sstype, DataDependenceOptions.NO_HEAP,
						ControlDependenceOptions.FULL);
//						SlicerTool.do_slice_fw(appJar, CName, l.lno, appSrc, 0,
//						DataDependenceOptions.NO_HEAP, ControlDependenceOptions.FULL, sstype, "Scanner");
//				System.out.println(">>>> xxx 2");
				// propagate_slice(appSrc, appJar, CName, IN_SLICE, 'S', l.lno);
				propagate_slice(IN_SLICE, 'S', l.lno);

			} else if (stmt.contains("BufferedReader(")) { // && !(stmt.contains("import ") &&
															// stmt.contains("java.io."))) {
				// TODO: add FileReader & Console
				// ---------- Add var to InList --- Start
				VAR_MD in_e = new VAR_MD();
				in_e.fun = "BufferedReader";
				in_e.lno = l.lno;
				in_e.type = "BufferedReader";
				String[] arrOfStr = stmt.split(" ", 3);
				if (!arrOfStr[0].replaceAll("\\s", "").equals("BufferedReader"))
					in_e.name = arrOfStr[0].replaceAll("\\s", "");
				else if (arrOfStr[0].replaceAll("\\s", "").equals("BufferedReader"))
					in_e.name = arrOfStr[1].replaceAll("\\s", "");

				InList.add(in_e);
				// ---------- Add var to InList --- End

				sstype = 's';
				List<SliceLine> IN_SLICE = slice_fw(l.lno, "BufferedReader", sstype, DataDependenceOptions.NO_HEAP,
						ControlDependenceOptions.FULL);
//				SlicerTool.do_slice_fw(appJar, CName, l.getLno(), appSrc, 0,
//						DataDependenceOptions.NO_HEAP, ControlDependenceOptions.FULL, 's', "BufferedReader");
				propagate_slice(IN_SLICE, 'B', l.lno);
//				propagate_slice(appSrc, appJar, CName, IN_SLICE, 'B', l.lno);

			} else if (stmt.contains("getConnection")) {
				AanalysisPoint md = new AanalysisPoint();
				md.setLno(l.lno);
				md.setType('C');
				md.setUIT('C'); // Constant
				boolean exist = false;
				for (AanalysisPoint mdtemp : APs) {
					if (mdtemp.lno == md.lno && mdtemp.type == md.type) {
						exist = true;
						break;
					}
				}
				if (!exist)
					APs.add(md);
			}

		}
	}

//	public static void propagate_slice(String appSrc, String appJar, String CName, List<SliceLine> IN_SLICE,
//			char UIType, int SeedLno) throws WalaException, CancelException, IOException, InvalidClassFileException {

	public static void propagate_slice(List<SliceLine> IN_SLICE, char UIType, int SeedLno)
			throws WalaException, CancelException, IOException, InvalidClassFileException {
//		String[] keywords = { "int", "double", "float", "String", "char" };
		String key = "";
		char sstype; // seed statement type
//		System.out.print("\n###############\nSeed (" + SeedLno + ") has these slice lines: [ ");
//		for (SliceLine sl0 : IN_SLICE) {
//			System.out.print(sl0.lno + ", ");
//		}
//		System.out.print(" ]\n###############\n");
		for (SliceLine sl : IN_SLICE) {

			if (sl.lno == SeedLno) {
				System.out.println(" [ Skip line (" + sl.lno + ") ]");
				continue;
			}
			String temp = sl.toString();
//			System.out.println(" >> Check line ("+sl.lno+"): " + temp);

			if (UIType == 'S') {//
				// TODO: check scanner functions e.g., nextLine ...
				add_scanner_to_var_list(sl.stmt, sl.lno);

				// Add to VarList

			} else if (UIType == 'B') {
				// add_buffer_reader_to_var_list(sl.stmt, sl.lno);
			}

			if (temp.contains("executeQuery")) {
				AanalysisPoint md = new AanalysisPoint();
				md.setLno(sl.lno);
				md.setType('Q');
				md.setUIT(UIType);
				boolean exist = false;
				for (AanalysisPoint mdtemp : APs) {
					if (mdtemp.lno == md.lno && mdtemp.type == md.type) {
						exist = true;
						break;
					}
				}
				if (!exist)
					APs.add(md);

			} else if (temp.contains("executeUpdate")) {
				AanalysisPoint md = new AanalysisPoint();
				md.setLno(sl.lno);
				md.setType('U');
				md.setUIT(UIType);
				boolean exist = false;
				for (AanalysisPoint mdtemp : APs) {
					if (mdtemp.lno == md.lno && mdtemp.type == md.type) {
						exist = true;
						break;
					}
				}
				if (!exist)
					APs.add(md);

			}else if (temp.contains(".execute(")) {
				AanalysisPoint md = new AanalysisPoint();
				md.setLno(sl.lno);
				md.setType('E');
				md.setUIT(UIType);
				boolean exist = false;
				for (AanalysisPoint mdtemp : APs) {
					if (mdtemp.lno == md.lno && mdtemp.type == md.type) {
						exist = true;
						break;
					}
				}
				if (!exist)
					APs.add(md);

			}else if (temp.contains("prepareStatement")) {

				AanalysisPoint md = new AanalysisPoint();
				md.setLno(sl.lno);
				md.setType('P');
				md.setUIT(UIType);
				boolean exist = false;
				for (AanalysisPoint mdtemp : APs) {
					if (mdtemp.lno == md.lno && mdtemp.type == md.type) {
						exist = true;
						break;
					}
				}
				if (!exist)
					APs.add(md);

			} else if (temp.contains("getConnection")) {
				// Label the statement, add to APs
				AanalysisPoint md = new AanalysisPoint();
				md.setLno(sl.lno);
				md.setType('C');
				md.setUIT(UIType);
				boolean exist = false;
				Iterator<AanalysisPoint> itr = APs.iterator();
				while (itr.hasNext()) {
					AanalysisPoint mdtemp = (AanalysisPoint) itr.next();
					if (mdtemp.lno == md.lno && mdtemp.type == md.type && mdtemp.UIT == 'C') {
						itr.remove();
						exist = false;
					} else if (mdtemp.lno == md.lno && mdtemp.type == md.type && mdtemp.UIT == md.UIT) {
						exist = true;
					}
				}

//				boolean exist = false;
//				for (TA_MD mdtemp : APs) {
//					if (mdtemp.lno == md.lno && mdtemp.type == md.type && mdtemp.UIT == md.UIT ) {
//						exist = true;
//						break;
//					}
//				}
				if (!exist)
					APs.add(md);

			}

//			else if(temp.contains("System.out.print")) {
//				TA_MD md = new TA_MD();
//				md.setLno(sl.lno);
//				md.setType('O');
//				md.setUIT(UIType);
//				boolean exist= false; 
//				for (TA_MD mdtemp : APs) {
//					if(mdtemp.lno == md.lno && mdtemp.type == md.type) {
//						exist = true;
//						break;
//					}
//				}
//			boolean exist= false; 
//			for (TA_MD mdtemp : APs) {
//				if(mdtemp.lno == md.lno && mdtemp.type == md.type) {
//					exist = true;
//					break;
//				}
//			}
//			if(!exist)
//				APs.add(md);
//			}
			else if ((temp.contains("StringBuilder, append") || temp.contains("valueOf"))
					&& !(temp.contains("String.format")) && !(temp.contains("System.out"))) {
				// propagate the slice
				key = "StringBuilder, append"; // Note: using "StringBuilder" with sstype = 's', Returns big slice!
//				key = "append"; 
				sstype = 'r';
				List<SliceLine> IN_SLICE2 = slice_fw(sl.lno, key, sstype, DataDependenceOptions.NO_HEAP,
						ControlDependenceOptions.FULL);
//						SlicerTool.do_slice_fw(appJar, CName, sl.lno, appSrc, 0,
//						DataDependenceOptions.NO_HEAP, ControlDependenceOptions.FULL, sstype, key);
				System.out.println("propagate_slice with seed (" + sl.lno + ")");
//				propagate_slice(appSrc, appJar, CName, IN_SLICE2, UIType , sl.lno);
				propagate_slice(IN_SLICE2, UIType, sl.lno);
			}
//			else if(temp.contains("StringBuilder, toString")) {}

		}

	}

	public static void add_scanner_to_var_list(String codeLine, int lno) {
//		String codeLine = sl.stmt;
		String[] arrOfStr = codeLine.split(" ", 3);
		VAR_MD var_e = new VAR_MD();
		if (codeLine.contains(".nextLine()") || codeLine.contains(".next(")) { // string vars

			if (codeLine.contains(".nextLine()"))
				var_e.fun = "nextLine";
			else
				var_e.fun = "next";
			var_e.lno = lno;
			if (codeLine.contains("nextLine().toCharArray(") || codeLine.contains("next().toCharArray()"))
				var_e.type = "char[]";
			else
				var_e.type = "String";
			if (!arrOfStr[0].replaceAll("\\s", "").equals("String"))
				var_e.name = arrOfStr[0].replaceAll("\\s", "");
			else if (arrOfStr[0].replaceAll("\\s", "").equals("String"))
				var_e.name = arrOfStr[1].replaceAll("\\s", "");
			VarList.add(var_e);

		} else if (codeLine.contains(".nextInt(")) {
			var_e.fun = "nextInt";
			var_e.lno = lno;
			var_e.type = "int";
			if (!arrOfStr[0].replaceAll("\\s", "").equals("int"))
				var_e.name = arrOfStr[0].replaceAll("\\s", "");
			else if (arrOfStr[0].replaceAll("\\s", "").equals("int"))
				var_e.name = arrOfStr[1].replaceAll("\\s", "");
			VarList.add(var_e);

		} else if (codeLine.contains(".nextDouble(")) {
			var_e.fun = "nextDouble";
			var_e.lno = lno;
			var_e.type = "double";
			if (!arrOfStr[0].replaceAll("\\s", "").equals("double"))
				var_e.name = arrOfStr[0].replaceAll("\\s", "");
			else if (arrOfStr[0].replaceAll("\\s", "").equals("double"))
				var_e.name = arrOfStr[1].replaceAll("\\s", "");
			VarList.add(var_e);

		} else if (codeLine.contains(".nextFloat(")) {
			var_e.fun = "nextFloat";

			var_e.lno = lno;
			var_e.type = "float";
			if (!arrOfStr[0].replaceAll("\\s", "").equals("float"))
				var_e.name = arrOfStr[0].replaceAll("\\s", "");
			else if (arrOfStr[0].replaceAll("\\s", "").equals("float"))
				var_e.name = arrOfStr[1].replaceAll("\\s", "");
			VarList.add(var_e);
		} else if (codeLine.contains(".nextLong(")) {
			var_e.fun = "nextLong";
			var_e.lno = lno;

			var_e.type = "long";
			if (!arrOfStr[0].replaceAll("\\s", "").equals("long"))
				var_e.name = arrOfStr[0].replaceAll("\\s", "");
			else if (arrOfStr[0].replaceAll("\\s", "").equals("long"))
				var_e.name = arrOfStr[1].replaceAll("\\s", "");
			VarList.add(var_e);
		}else {
			var_e.fun = "n/a";
			var_e.lno = lno;

			var_e.type = "n/a";
//			if (!arrOfStr[0].replaceAll("\\s", "").equals("long"))
			var_e.name = arrOfStr[0].replaceAll("\\s", "");
//			else if (arrOfStr[0].replaceAll("\\s", "").equals("long"))
//				var_e.name = arrOfStr[1].replaceAll("\\s", "");
			VarList.add(var_e);
		}
		

	}

// To test my code 
	public static void main(String[] args)
			throws IOException, WalaException, CancelException, InvalidClassFileException {

		// Test TA step: perform the analysis, then print APs.
		String appSrc = "/Users/dpc100/NetBeansProjects/smallBank/src/TSet/StringsExample.java";
//				"/Users/dpc100/NetBeansProjects/smallBank/src/TSet/PaperExample_vul.java";
//				"/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/PaperExample_vul.java"; 
//				"/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/StringsExample.java";
//		"/Users/dpc100/NetBeansProjects/smallBank/src/TSet/PaperExample_vul.java";
//		 "/Users/dpc100/NetBeansProjects/smallBank/src/TSet/PW_sec.java";
//				"/Users/dpc100/NetBeansProjects/smallBank/src/TSet/PWstr_vul_d.java";
//		 "/Users/dpc100/NetBeansProjects/smallBank/src/TSet/PWstr_vul_h.java";
		// "/Users/dpc100/NetBeansProjects/smallBank/src/TSet/SQLIV_q_s_all.java";
		// "/Users/dpc100/NetBeansProjects/smallBank/src/TSet/SQLIV_q_s_t_cv_SEC.java";
		// "/Users/dpc100/NetBeansProjects/smallBank/src/TSet/SQLIV_q_s_t_cv.java";
		// "/Users/dpc100/NetBeansProjects/smallBank/src/TSet/SQLIV_q_s_v.java";

		String appJar = "/Users/dpc100/Fixer/Experiments/TrainingCases/smallbank.jar";
		String[] subpath = appSrc.split("/");
		String appSrc_folder = subpath[subpath.length - 2] + "/";
		String appSrc_CName = (subpath[subpath.length - 1].split("\\."))[0];
		String app_folder_CName = appSrc_folder + appSrc_CName;
		long start_input_points = System.currentTimeMillis();
		start_anlysis(appSrc, appJar, app_folder_CName);

		System.out.println("===================");
		for (AanalysisPoint md : APs) {
			System.out.println("IP@line: " + md.lno + ", Type is: " + md.getType() + ", UI type is: " + md.getUIT());
		}

		for (VAR_MD v : InList) {
			System.out.println("IN Point at line: " + v.lno + ", Type is: " + v.getType() + ", Fun called is: "
					+ v.getFun() + ", Var name: " + v.getName());
		}
		for (VAR_MD v : VarList) {
			System.out.println("Var Point at line: " + v.lno + ", Type is: " + v.getType() + ", Fun called is: "
					+ v.getFun() + ", Var name: " + v.getName());
		}

		long finish_input_points = System.currentTimeMillis();
		long timeElapsed = finish_input_points - start_input_points;
		double sec = timeElapsed / 1000.0;
		System.out.println("\nTime Elapsed to build cg: " + Time_to_build_cg);
		System.out.println("\nTime Elapsed to compute slices:" + Time_to_compute_slice);
		System.out.println("\nTime Elapsed to map slices to src: " + Time_to_map_slice);
		System.out.println(
				"\nTime Elapsed to find all UI points and mark functions : " + timeElapsed + ", in sec: " + sec);

//		String str= "This#string%contains^special*_characters090909&.";   
//		str = str.replaceAll("[^a-zA-Z0-9_]", " ");  
//		System.out.println(str); 
	}
}
