package slicer.tool;


//import java.io.ByteArrayInputStream;
import java.io.IOException;
//import java.util.stream.Collectors;
import java.util.List;

//import com.ibm.wala.classLoader.Language;
//import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
//import com.ibm.wala.ipa.callgraph.*;
//import com.ibm.wala.ipa.callgraph.impl.Util;
//import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
//import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
//import com.ibm.wala.ipa.callgraph.util.CallGraphSearchUtil;
//import com.ibm.wala.ipa.cha.ClassHierarchy;
//import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
//import com.ibm.wala.ipa.cha.IClassHierarchy;
//import com.ibm.wala.ipa.slicer.NormalReturnCaller;
//import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Slicer;
//import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
//import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
//import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
//import com.ibm.wala.util.config.AnalysisScopeReader;
//import com.ibm.wala.util.config.FileOfClasses;

import slicer.datatypes.SeedList;
import slicer.datatypes.SliceLine;
import slicer.utilities.SrcCodeUtil;

public class SlicerTool2 {
//	private static final String EXCLUSIONS =
//	"java\\/awt\\/.*\n"+
//	"javax\\/swing\\/.*\n"+
//	"sun\\/awt\\/.*\n"+
//	"sun\\/swing\\/.*\n"+
//	"com\\/sun\\/.*\n"+
//	"sun\\/.*\n";

//	private static final String EXCLUSIONS = "java\\/awt\\/.*\n" + "javax\\/swing\\/.*\n" + "sun\\/awt\\/.*\n"
//			+ "sun\\/swing\\/.*\n" + "com\\/sun\\/.*\n" + "sun\\/.*\n" + "org\\/netbeans\\/.*\n"
//			+ "org\\/openide\\/.*\n" + "com\\/ibm\\/crypto\\/.*\n" + "com\\/ibm\\/security\\/.*\n"
//			+ "org\\/apache\\/xerces\\/.*\n" + "java\\/security\\/.*\n";

//	public static List<SliceLine> do_slice_bw(String appJar, String className, int lineNumber, String appSrc,
//			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type)
//			throws WalaException, CancelException, IOException, InvalidClassFileException {
//
//		// similar to @Test - testList() .
//		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java
//
//		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, null);
//		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
//		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
//		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
//				"L" + className);
//		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);// new
//																							// AnalysisOptions(scope,
//																							// entrypoints);
//
//		CallGraphBuilder<InstanceKey> builder;
//		if (cg_type == 1) {
//			builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
//		} else {
//			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope, null, null);
//		}
//		//
//		CallGraph cg = builder.makeCallGraph(options, null);
//
//		SrcCode src = new SrcCode();
//		ArrayList<String> methods_list = new ArrayList<>();
//		methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
//		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
//		// System.out.print("methods_list_final:" + methods_list_final);
//		src.set_values(appSrc, className, methods_list);
//		// System.out.println("##### 0\n");
//
//		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
//		// System.out.println("methodName:" + methodName);
//		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
////		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);
//
//		Statement seed_s = SlicerUtil.find_seed_by_line_no(targeted_method_node, lineNumber);
//		System.out.println("Seed stmt: " + seed_s.toString());
//		NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_line_no(targeted_method_node, lineNumber);
//		// we need a NormalReturnCaller statement to slice from the return value
//		NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());
//		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
//		Collection<Statement> backward_slice;
//
//		// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
//		if (seed_type == 'n') {
//			backward_slice = Slicer.computeBackwardSlice(seed_ns, cg, pa, dOptions, cOptions);
//		} else if (seed_type == 'r') {
//			backward_slice = Slicer.computeBackwardSlice(seed_nrc, cg, pa, dOptions, cOptions);
//
//		} else {// if(seed_type == 's')
//			backward_slice = Slicer.computeBackwardSlice(seed_s, cg, pa, dOptions, cOptions);
//
//		}
//		SlicerUtil.dumpSlice2(backward_slice);
////		for (Statement s : backward_slice) {
////			if (s != null) {
////
////				String line = StmtFormater.format_filtered_stmt(s, methods_list);
////				if (line == null) {
////
////					if (s.toString().contains("setString")) {
////						System.out.println("**** setString : "+StmtFormater.format(s));
////					}
////
////					if (s.toString().contains("valueOf")) {
////						System.out.println("**** valueOf : "+StmtFormater.format(s));
////					}
////					//nextInt
////					if (s.toString().contains("nextInt")) {
////						System.out.println("**** valueOf : "+StmtFormater.format(s));
////					}
////				}
//////				else {
//////					System.out.println(line);
//////				}
////			}
////		}
////		System.out.println("\n############################################# backward_slice: ");
////		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.FULL,
////				Slicer.ControlDependenceOptions.NONE);
////		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed , cg, pa, Slicer.DataDependenceOptions.FULL,
////				Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);//I got only the seed
//
////		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed_nrc , cg, pa, dOptions,
////				cOptions);//
//
//		// ------------------- makeZeroOneCFABuilder
//
//		// seed_ns , ... , Slicer.DataDependenceOptions.FULL,
//		// Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES --> Only the SEED
//		// nrc_seed ,...,
//		// DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
//		// --> Only the SEED
//		// nrc_seed , ..., Slicer.DataDependenceOptions.FULL,
//		// Slicer.ControlDependenceOptions.FULL --> EMPTY
//		// seed_ns , ..., Slicer.DataDependenceOptions.FULL,
//		// Slicer.ControlDependenceOptions.FULL --> 5 hours and still no results!
//		// seed_ns , ...,
//		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE-->
//		// prepared + execute
//
//		// ------------------- makeZeroCFABuilder
//		// nrc_seed ,...,
//		// Slicer.DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
//		// --> ?? long
//		// nrc_seed ,...,
//		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE
//
////		List<Statement> normalsInSlice = backward_slice.stream()
////				.filter(s -> s instanceof NormalReturnCaller && s.getNode().equals(targeted_method_node))
////				.collect(Collectors.toList());
////		System.out.print(normalsInSlice);
////		SlicerUtil.dumpSlice(backward_slice);
//		List<SliceLine> sliceLines = new ArrayList<>();
//		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(backward_slice, src.methods, src.lines);
//		return sliceLines;
//		// -------------------------------------------------
////		System.out.println("\n\n\n############################################# forward_slice: ");
////		Collection<Statement> forward_slice = Slicer.computeForwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
////				Slicer.ControlDependenceOptions.NONE);
////
////		for (Statement s : forward_slice) {
////			if (s != null) {
////				String line = StmtFormater.format_filtered_stmt(s, methods);
////				if (line != null)
////					System.out.println(line);
////			}
////		}
////
////		List<SliceLine> sliceLines = new ArrayList<>();
////		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice,src.methods, src.lines);
////		return sliceLines;
//		// -------------------------------------------------
////		System.out.println("################################################## 4");
////
////		//In case of two slices bw & fw
////		List<SliceLine> sortedList = new ArrayList<>();
////		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine( backward_slice, forward_slice,
////				src.methods, src.lines);
////		System.out.println("##### 5");
////		return sortedList;
//		// 1 ==================================
//	}
//
//	public static List<SliceLine> do_slice_fw(String appJar, String className, int lineNumber, String appSrc,
//			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type)
//			throws WalaException, CancelException, IOException, InvalidClassFileException {
//
//		// similar to @Test - testList() .
//		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java
//
//		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, null);
//		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
//		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
//		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
//				"L" + className);
//		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);// new
//																							// AnalysisOptions(scope,
//																							// entrypoints);
//
//		CallGraphBuilder<InstanceKey> builder;
//		if (cg_type == 1) {
//			builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
//		} else {
//			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope, null, null);
//		}
//		//
//		CallGraph cg = builder.makeCallGraph(options, null);
//
//		SrcCode src = new SrcCode();
//		ArrayList<String> methods_list = new ArrayList<>();
//		methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
//		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
//		// System.out.print("methods_list_final:" + methods_list_final);
//		src.set_values(appSrc, className, methods_list);
//		// System.out.println("##### 0\n");
//
//		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
//		// System.out.println("methodName:" + methodName);
//		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
////		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);
//
//		Statement seed_s = SlicerUtil.find_seed_by_line_no(targeted_method_node, lineNumber);
//		System.out.println("Seed stmt: " + seed_s.toString());
//		NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_line_no(targeted_method_node, lineNumber);
//		// we need a NormalReturnCaller statement to slice from the return value
//		NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());
//		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
//		Collection<Statement> forward_slice;
//
//		// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
//		if (seed_type == 'n') {
//			forward_slice = Slicer.computeForwardSlice(seed_ns, cg, pa, dOptions, cOptions);
//		} else if (seed_type == 'r') {
//			forward_slice = Slicer.computeForwardSlice(seed_nrc, cg, pa, dOptions, cOptions);
//
//		} else {// if(seed_type == 's')
//			forward_slice = Slicer.computeForwardSlice(seed_s, cg, pa, dOptions, cOptions);
//
//		}
////		for (Statement s : forward_slice) {
////			if (s != null) {
////
////				String line = StmtFormater.format_filtered_stmt(s, methods_list);
////				if (line == null) {
////
////					if (s.toString().contains("setString")) {
////						System.out.println("**** setString : "+StmtFormater.format(s));
////					}
////
////					if (s.toString().contains("valueOf")) {
////						System.out.println("**** valueOf : "+StmtFormater.format(s));
////					}
////					//nextInt
////					if (s.toString().contains("nextInt")) {
////						System.out.println("**** valueOf : "+StmtFormater.format(s));
////					}
////				}
//////				else {
//////					System.out.println(line);
//////				}
////			}
////		}
//		SlicerUtil.dumpSlice2(forward_slice);
//		List<SliceLine> sliceLines = new ArrayList<>();
//		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice, src.methods, src.lines);
//		return sliceLines;
//		// -------------------------------------------------
//
//	}
//
	public static void main(String[] args)
			throws IOException, WalaException, CancelException, InvalidClassFileException {
//        find_all_class_methods("src\\main\\java\\CWE15_External_Control_of_System_or_Configuration_Setting.jar" , "CWE15_External_Control_of_System_or_Configuration_Setting__connect_tcp_02");
//        System.out.println(do_thin_slicer("src\\main\\java\\CWE15_External_Control_of_System_or_Configuration_Setting.jar" , "goodG2B1" , "CWE15_External_Control_of_System_or_Configuration_Setting__connect_tcp_02" ,166));
//		String trainingPath = "/Users/dareen/Fixer/Experiments/TrainingCases/example_";
//		String testingPath = "/Users/dareen/Fixer/Experiments/TestCases/test_";
		// ---------------------------------------------- test regular apps
//		String appSrc = "/Users/dareen/Fixer/Experiments/TrainingCases/example_1/add5.java";
//		String appJar = "/Users/dareen/Fixer/Experiments/TrainingCases/example_1/calculator.jar";
//		String[] subpath = appSrc.split("/");
//		String classPath = "";
//		String className_only =  (subpath[subpath.length-1].split("\\."))[0];
//		String className = classPath + className_only;
//
//		int tr_i = 1;
//		String key ="add2nums";
//		SeedList sl = SrcCodeUtil.find_class_seeds_key(SrcCodeUtil.get_src_lines(appSrc), key);
//
//		if (!sl.is_empty) {
//			System.out.println(sl.key);
//			System.out.println(sl.LinesNo);
//			for(int i = 0; i<sl.LinesNo.size();i++) {
//
//				System.out.println("**** "+sl.LinesNo.get(i));
//				// sl.LinesNo.get(i).method
//				System.out.println("=========== Slice # "+ i);
//
//				List<SliceLine> sliceLines = do_thin_slicer_by_only_lno( appJar,  className, sl.LinesNo.get(i),  appSrc);
//				//do_thin_slicer_SliceLineList( appJar,"main" ,  className, sl.LinesNo.get(i),  appSrc);//needs method name
//
//				System.out.println(sliceLines);
//
//
//				String slicePath = trainingPath+ tr_i + "/slices/"; // Could be testingPath
//				String slicefile =  className_only + "_"+ "slice_" +i + ".java";
//				SrcCodeUtil.write_slice_to_file(className, slicePath ,  slicefile,  sliceLines);
//			}
//		}
		// ---------------------------------------------- test client apps
//		String appSrc = "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/test.java";
		String appSrc = "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/Simple2.java";
		String appJar = "/Users/dareen/Fixer/Experiments/TrainingCases/example_2/smallbank.jar";
		String[] subpath = appSrc.split("/");
		String classPath = subpath[subpath.length - 2] + "/";
		String className_only = (subpath[subpath.length - 1].split("\\."))[0];
		String className = classPath + className_only;

//		int tr_i = 3;
//		String key = "getConnection";
		String key = "executeQuery";
//		String key = "prepareStatement";

		SeedList sl = SrcCodeUtil.find_class_seeds_key(SrcCodeUtil.get_src_lines(appSrc), key);

		if (!sl.is_empty) {
			System.out.println(sl.key);
			System.out.println(sl.LinesNo);
			for (int i = 0; i < sl.LinesNo.size(); i++) {

				System.out.println("**** " + sl.LinesNo.get(i));
				// sl.LinesNo.get(i).method
				System.out.println("=========== Slice # " + i);
				// To do : write a slicer that receives a ncr (normal call return)

//				List<SliceLine> sliceLines = do_thin_slicer_by_only_lno( appJar,  className, sl.LinesNo.get(i),  appSrc);
				// List<SliceLine> sliceLines = do_thin_slicer_by_lno_nrc(appJar, className,
				// sl.LinesNo.get(i), appSrc);
				// do_thin_slicer_SliceLineList( appJar,"main" , className, sl.LinesNo.get(i),
				// appSrc);
				int ln = sl.LinesNo.get(i);
				List<SliceLine> sliceLines_bw = SlicerTool.do_slice_bw(appJar, className, ln, appSrc, 1,
						Slicer.DataDependenceOptions.NO_HEAP, Slicer.ControlDependenceOptions.NONE, 's', key);
				List<SliceLine> sliceLines_fw = SlicerTool.do_slice_fw(appJar, className, ln, appSrc, 1,
						Slicer.DataDependenceOptions.NO_HEAP, Slicer.ControlDependenceOptions.NONE, 'r', key);
				System.out.println("sliceLines_bw: \n" + sliceLines_bw);
				System.out.println("sliceLines_fw: \n" + sliceLines_fw);
//				String slicePath = trainingPath+ tr_i + "/slices/"; // Could be testingPath
//				String slicefile =  className_only + "_"+ "slice_" +i + ".java";
//				SrcCodeUtil.write_slice_to_file(className_only, slicePath ,  slicefile,  sliceLines);
			}
		}

		System.out.println("className :" + className);
		// TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT TO DO: How would use the
		// method name?
		// 1- organize the output to a file.
		// 2- Remove un-necessary data like "try{", "{" , ...
		// 3- Write the slice to a file; unify functions types and return value!??

		// TO DO: start a loop to go over all training cases:

		// TO DO: start a loop to go over all test cases:

//		System.out.println(do_thin_slicer(appJar, "main",src.className , 18));
		System.out.println("==================");

		System.out.print("Done!");
	}
}

/*
 * //1 ================================== Collection<Statement> slice;
 *
 * // context-sensitive traditional slice ModRef<InstanceKey> modRef =
 * ModRef.make(); SDG<?> sdg = new SDG<>(cg, pa, modRef,
 * Slicer.DataDependenceOptions.REFLECTION,
 * Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES, null);
 *
 *
 *
 *
 * // context-sensitive thin slice slice =
 * Slicer.computeBackwardSlice(statement, cg, pa,
 * Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE);
 *
 * for (Statement s : slice) { if(s != null ){ String line =
 * StmtFormater.format_filtered_stmt(s, methods); if(line!=null)
 * System.out.println(line); } }
 * System.out.println("End of the Slice *******************");
 *
 * SlicerUtil.dumpSlice(slice);
 *
 * SDG<InstanceKey> sd=new SDG<InstanceKey>(cg, pa,
 * Slicer.DataDependenceOptions.NO_BASE_NO_HEAP ,
 * Slicer.ControlDependenceOptions.NONE);
 *
 * // filter primordial stmt
 *
 * Predicate<Statement> filter = o -> slice.contains(o) &&
 * !o.toString().contains("Primordial") && o.getKind() == Statement.Kind.NORMAL;
 * Graph<Statement> graph = GraphSlicer.prune(sdg, filter);
 *
 * Set<Integer> sourcelines = new HashSet<>(); for (Statement s : graph) { if
 * (methods.contains(s.getNode().getMethod().getName().toString())) {
 * sourcelines.add(StmtFormater.getStatementSourcePosition(s));
 *
 * } } sourcelines.add(lineNumber); return sourcelines;
 */
