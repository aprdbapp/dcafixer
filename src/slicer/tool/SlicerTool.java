package slicer.tool;

//import slicer.MyAnalyzer;
//import slicer.datatypes.CodeLine;
//import slicer.datatypes.SeedList;
import slicer.datatypes.SliceLine;
import slicer.datatypes.SrcCode;
//import slicer.datatypes.AanalysisPoint;
import slicer.utilities.SlicerUtil;
//import slicer.utilities.SrcCodeUtil;
import slicer.utilities.StmtFormater;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
//import com.ibm.wala.ipa.callgraph.util.CallGraphSearchUtil;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalReturnCaller;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.shrike.shrikeCT.*;
//import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.*;
//import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.core.util.io.FileProvider;
import com.ibm.wala.util.config.FileOfClasses;
//import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.io.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
//import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SlicerTool {
	private static final boolean Print = false;
	private static final String EXCLUSIONS = "java\\/awt\\/.*\n" + "javax\\/swing\\/.*\n" + "sun\\/awt\\/.*\n"
			+ "sun\\/swing\\/.*\n" + "com\\/sun\\/.*\n" + "sun\\/.*\n" + "org\\/netbeans\\/.*\n"
			+ "org\\/openide\\/.*\n" + "com\\/ibm\\/crypto\\/.*\n" + "com\\/ibm\\/security\\/.*\n"
			+ "org\\/apache\\/xerces\\/.*\n" + "java\\/security\\/.*\n";

	public List<Integer> do_thin_slicer(String appJar, String methodName, String className, int lineNumber, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {
//		List<String> methods = new ArrayList<>();
//		methods = SlicerUtil.find_all_class_methods(appJar, className, EXCLUSIONS);
//		if(Print) System.out.println(methods);
		ArrayList<String> methods = new ArrayList<>();

		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);

		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);

		CallGraphBuilder<?> cgb = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope,
				null, null);
		
		CallGraph cg = cgb.makeCallGraph(options, null);
		methods = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		if(Print) System.out.println(methods);

		PointerAnalysis<?> pa = cgb.getPointerAnalysis();
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
		Statement statement = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);

		Collection<Statement> backward_slice;
		Collection<Statement> forward_slice;

		backward_slice = Slicer.computeBackwardSlice(statement, cg, pa, Slicer.DataDependenceOptions.FULL,
				Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);
//		backward_slice = Slicer.computeBackwardSlice(statement, cg, pa, Slicer.DataDependenceOptions.FULL,
//				Slicer.ControlDependenceOptions.NO_INTERPROC_NO_EXCEPTION);
		// DataDependenceOptions.FULL,
		// ControlDependenceOptions.NO_INTERPROC_NO_EXCEPTION
		for (Statement s : backward_slice) {
			if (s != null) {
				String line = StmtFormater.format_filtered_stmt(s, methods);
				if (line != null)
					if(Print) System.out.println(line);
			}
		}
//		Set<SliceLine> slice_lines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine( backward_slice,
//				methods, ArrayList<CodeLine> lines);

		forward_slice = Slicer.computeForwardSlice(statement, cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
				Slicer.ControlDependenceOptions.NONE);
		// Filter the slice to stmts that belong to the class, and consider only normal
		// and
		// parameter caller
		Set<Integer> souce_lines = new HashSet<>();
		souce_lines.addAll(SlicerUtil.map_filtered_slice_stmts_to_line_no(backward_slice, methods));
		souce_lines.addAll(SlicerUtil.map_filtered_slice_stmts_to_line_no(forward_slice, methods));
		if(Print) System.out.println(souce_lines);
		List<Integer> sortedList = new ArrayList<>(souce_lines);
		Collections.sort(sortedList);
		if(Print) System.out.println(sortedList);
		return sortedList;
		// 1 ==================================
	}

//TO DO: build a slicer without the method name!
	public static List<SliceLine> do_thin_slicer_SliceLineList(String appJar, String methodName, String className,
			int lineNumber, String appSrc, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {
		ArrayList<String> methods = new ArrayList<>();

//		Set<SliceLine> slice_lines = new HashSet<SliceLine>();
		List<SliceLine> sortedList = new ArrayList<>();
//		CallGraphBuilder cgb = SlicerUtil.generate_call_graph_builder( appJar,  EXCLUSIONS);
//		CallGraph cg =   SlicerUtil.generate_call_graph(appJar,  EXCLUSIONS);
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		CallGraphBuilder<?> cgb = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope,
				null, null);
		CallGraph cg = cgb.makeCallGraph(options, null);

		SrcCode src = new SrcCode();
		methods = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		src.set_values(appSrc, className, methods);
//		if(Print) System.out.println(src.methods);
		PointerAnalysis<?> pa = cgb.getPointerAnalysis();
		if(Print) System.out.println("##### 0\n");

		// TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT TO DO: How would use the
		// method name?

		if(Print) System.out.println(SlicerUtil.find_seed_method(cg, className, lineNumber));

		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
		Statement statement = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
//		if(Print) System.out.println("##### 2");
//		if(Print) System.out.println( statement.toString());

		Collection<Statement> backward_slice;
		Collection<Statement> forward_slice;

		backward_slice = Slicer.computeBackwardSlice(statement, cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
				Slicer.ControlDependenceOptions.NONE);

		forward_slice = Slicer.computeForwardSlice(statement, cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
				Slicer.ControlDependenceOptions.NONE);

//		if(Print) System.out.println("##### 4");

		// In case of two slices bw & fw
		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine(backward_slice, forward_slice, src.methods,
				src.lines);

//		if(Print) System.out.println("##### 5");

		return sortedList;
		// 1 ==================================
	}

	public static List<SliceLine> do_thin_slicer_by_only_lno(String appJar, String className, int lineNumber,
			String appSrc, String key) throws WalaException, CancelException, IOException, InvalidClassFileException {
		ArrayList<String> methods = new ArrayList<>();

//		Set<SliceLine> slice_lines = new HashSet<SliceLine>();
		List<SliceLine> sortedList = new ArrayList<>();
//		CallGraphBuilder cgb = SlicerUtil.generate_call_graph_builder( appJar,  EXCLUSIONS);
//		CallGraph cg =   SlicerUtil.generate_call_graph(appJar,  EXCLUSIONS);
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		CallGraphBuilder<?> cgb = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope,
				null, null);
		CallGraph cg = cgb.makeCallGraph(options, null);

		SrcCode src = new SrcCode();
		methods = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		src.set_values(appSrc, className, methods);
//		if(Print) System.out.println(src.methods);
		PointerAnalysis<?> pa = cgb.getPointerAnalysis();
		if(Print) System.out.println("##### 0\n");

		// TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT TO DO: How would use the
		// method name?

		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);

//		if(Print) System.out.println("##### jar name: " +appJar.split("/")[appJar.split("/").length - 1].split("\\.")[0]);
		if(Print) System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
		Statement statement = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
//		if(Print) System.out.println("##### 2");
//		if(Print) System.out.println( statement.toString()); //It worked correctly with "executeQuery"

		Collection<Statement> backward_slice;
		Collection<Statement> forward_slice;
		if(Print) System.out.println("\n############################################# backward_slice: ");
		backward_slice = Slicer.computeBackwardSlice(statement, cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
				Slicer.ControlDependenceOptions.NONE);

		for (Statement s : backward_slice) {
			if (s != null) {
				String line = StmtFormater.format_filtered_stmt(s, methods);
				if (line != null)
					if(Print) System.out.println(line);
			}
		}
//		SlicerUtil.dumpSlice(backward_slice);

		if(Print) System.out.println("\n\n\n\n\n##### forward_slice: ");
		forward_slice = Slicer.computeForwardSlice(statement, cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
				Slicer.ControlDependenceOptions.NONE);

		for (Statement s : forward_slice) {
			if (s != null) {
				String line = StmtFormater.format_filtered_stmt(s, methods);
				if (line != null)
					if(Print) System.out.println(line);
			}
		}
//		if(Print) System.out.println("##### 4");

		// In case of two slices bw & fw
		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine(backward_slice, forward_slice, src.methods,
				src.lines);

//		if(Print) System.out.println("##### 5");

		return sortedList;
		// 1 ==================================
	}

	public static List<SliceLine> do_thin_slicer_by_lno_nrc(String appJar, String className, int lineNumber,
			String appSrc, String key) throws WalaException, CancelException, IOException, InvalidClassFileException {

		// similar to @Test - testList() .
		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java

//		CallGraphBuilder cgb = SlicerUtil.generate_call_graph_builder( appJar,  EXCLUSIONS);
//		CallGraph cg =   SlicerUtil.generate_call_graph(appJar,  EXCLUSIONS);
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
				"L" + className);
		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);

		CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneCFABuilder(Language.JAVA, options,
				new AnalysisCacheImpl(), cha, scope);
		// Util.makeZeroCFABuilder(Language.JAVA, options,new AnalysisCacheImpl(), cha,
		// scope, null, null);
		CallGraph cg = builder.makeCallGraph(options, null);

		SrcCode src = new SrcCode();
		ArrayList<String> methods_list = new ArrayList<>();
		methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
		if(Print) System.out.print("methods_list_final:" + methods_list_final);
		src.set_values(appSrc, className, methods_list);
//		if(Print) System.out.println(src.methods);
//		PointerAnalysis<?> pa = cgb.getPointerAnalysis();
		if(Print) System.out.println("##### 0\n");

		// TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT TO DO: How would use the
		// method name?

		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
		if(Print) System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
//		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);
//		Statement statement = SlicerUtil.find_seed_by_line_no(targeted_method_node, lineNumber);
		Statement seed_s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);

		NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node,
				lineNumber, key);
//		NormalStatement getCall = (NormalStatement) SlicerUtil.findCallTo(main, "get");
		// we need a NormalReturnCaller statement to slice from the return value
		NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());

		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
//		if(Print) System.out.println("##### 2");
//		if(Print) System.out.println( statement.toString()); //It worked correctly with "executeQuery"

		// -------------------------------------------------
		if(Print) System.out.println("\n############################################# backward_slice: ");
//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.FULL,
//				Slicer.ControlDependenceOptions.NONE);
//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed , cg, pa, Slicer.DataDependenceOptions.FULL,
//				Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);//I got only the seed
		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed_nrc, cg, pa,
				Slicer.DataDependenceOptions.NO_HEAP, Slicer.ControlDependenceOptions.NONE);//

		// ------------------- makeZeroOneCFABuilder

		// seed_ns , ... , Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES --> Only the SEED
		// nrc_seed ,...,
		// DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
		// --> Only the SEED
		// nrc_seed , ..., Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.FULL --> EMPTY
		// seed_ns , ..., Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.FULL --> 5 hours and still no results!
		// seed_ns , ...,
		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE-->
		// prepared + execute

		// ------------------- makeZeroCFABuilder
		// nrc_seed ,...,
		// Slicer.DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
		// --> ?? long
		// nrc_seed ,...,
		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE

//		for (Statement s : backward_slice) {
//			if (s != null) {
//				String line = StmtFormater.format_filtered_stmt(s, methods);
//				if (line != null)
//					if(Print) System.out.println(line);
//			}
//		}
		List<Statement> normalsInSlice = backward_slice.stream()
				.filter(s -> s instanceof NormalReturnCaller && s.getNode().equals(targeted_method_node))
				.collect(Collectors.toList());
		// .filter((s -> s instanceof NormalStatement|| s -> s instanceof ParamCaller ||
		// s -> s instanceof NormalReturnCaller ) &&
		// methods_list_final.contains(s.getNode().getMethod().getName().toString()))
//		if(Print) System.out.println("\n^^^^^^^^^^^^^^^^^^^^");
//		if(Print) System.out.print(normalsInSlice);
//		if(Print) System.out.println("\n^^^^^^^^^^^^^^^^^^^^");
		SlicerUtil.dumpSlice(backward_slice);
		List<SliceLine> sliceLines = new ArrayList<>();
		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(backward_slice, src.methods, src.lines);
		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("\n\n\n############################################# forward_slice: ");
//		Collection<Statement> forward_slice = Slicer.computeForwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
//				Slicer.ControlDependenceOptions.NONE);
//		
//		for (Statement s : forward_slice) {
//			if (s != null) {
//				String line = StmtFormater.format_filtered_stmt(s, methods);
//				if (line != null)
//					if(Print) System.out.println(line);
//			}
//		}
//		
//		List<SliceLine> sliceLines = new ArrayList<>();
//		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice,src.methods, src.lines);
//		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("################################################## 4");
//
//		//In case of two slices bw & fw
//		List<SliceLine> sortedList = new ArrayList<>();
//		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine( backward_slice, forward_slice,
//				src.methods, src.lines);
//		if(Print) System.out.println("##### 5");
//		return sortedList;
		// 1 ==================================
	}

	public static List<SliceLine> do_slice_bwfw(String appJar, String className, int lineNumber, String appSrc,
			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {

		// similar to @Test - testList() .
		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java

		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
				"L" + className);
		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);// new
																							// AnalysisOptions(scope,
																							// entrypoints);

		CallGraphBuilder<InstanceKey> builder;
		if (cg_type == 1) {
			builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
		} else {
			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope, null, null);
		}
		//
		CallGraph cg = builder.makeCallGraph(options, null);

		SrcCode src = new SrcCode();
		ArrayList<String> methods_list = new ArrayList<>();
		methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
		// if(Print) System.out.print("methods_list_final:" + methods_list_final);
		src.set_values(appSrc, className, methods_list);
		// if(Print) System.out.println("##### 0\n");

		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
		// if(Print) System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
//		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);

		Statement seed_s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
		NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node,
				lineNumber, key);
		// we need a NormalReturnCaller statement to slice from the return value
		NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());
		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
		Collection<Statement> backward_slice;
		Collection<Statement> forward_slice;
		// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
		if (seed_type == 'n') {
			backward_slice = Slicer.computeBackwardSlice(seed_ns, cg, pa, dOptions, cOptions);
			forward_slice = Slicer.computeForwardSlice(seed_ns, cg, pa, dOptions, cOptions);
			// Slicer.computeForwardSlice(nrc_seed , cg, pa,
			// Slicer.DataDependenceOptions.NO_HEAP,
//					Slicer.ControlDependenceOptions.NONE);
		} else if (seed_type == 'r') {
			backward_slice = Slicer.computeBackwardSlice(seed_nrc, cg, pa, dOptions, cOptions);
			forward_slice = Slicer.computeForwardSlice(seed_nrc, cg, pa, dOptions, cOptions);

		} else {// if(seed_type == 's')
			backward_slice = Slicer.computeBackwardSlice(seed_s, cg, pa, dOptions, cOptions);
			forward_slice = Slicer.computeForwardSlice(seed_s, cg, pa, dOptions, cOptions);
		}

//		

//		List<Statement> normalsInSlice = backward_slice.stream()
//				.filter(s -> s instanceof NormalReturnCaller && s.getNode().equals(targeted_method_node))
//				.collect(Collectors.toList());
//		if(Print) System.out.print(normalsInSlice);
//		SlicerUtil.dumpSlice(backward_slice);
		List<SliceLine> sliceLines = new ArrayList<>();
		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(backward_slice, src.methods, src.lines);
		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("\n\n\n############################################# forward_slice: ");
//		Collection<Statement> forward_slice = Slicer.computeForwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
//				Slicer.ControlDependenceOptions.NONE);
//		
//		
//		List<SliceLine> sliceLines = new ArrayList<>();
//		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice,src.methods, src.lines);
//		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("################################################## 4");
//
//		//In case of two slices bw & fw
//		List<SliceLine> sortedList = new ArrayList<>();
//		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine( backward_slice, forward_slice,
//				src.methods, src.lines);
//		if(Print) System.out.println("##### 5");
//		return sortedList;
		// 1 ==================================
	}
	
	// ===== First working code. It works with some apps perfectly.
	public static List<SliceLine> do_slice_bw2(String appJar, String className, int lineNumber, String appSrc,
			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {

		// similar to @Test - testList() .
		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java
		long start_cg = System.currentTimeMillis();
		
		// ------- Original code
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
				"L" + className); // --------  made this change on 9/28th
		
		// -------- made the changes below on 9/28th Start @@ Didn't work
//		AnalysisScope scope = AnalysisScopeReader.makePrimordialScope((new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
//		IClassHierarchy cha = ClassHierarchyFactory.make(scope); //Same
//		Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
		// -------- End @@ 9/28th

		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);// new
																							// AnalysisOptions(scope,
																							// entrypoints);
//        com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = Util.makeVanillaZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope, null, null);

		CallGraphBuilder<InstanceKey> builder;
		if (cg_type == 1) {
			builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
			//Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
		} else {
			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
			//Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope, null, null);
		}
		// --------  commented line below  on 9/28th - original code
		CallGraph cg = builder.makeCallGraph(options, null);
		// -------- made the changes below on 9/28th Start @@@ Didn't work
//		CallGraph cg = CallGraphTestUtil.buildZeroOneCFA(options, new AnalysisCacheImpl(), cha, scope, false);
//		CallGraph cg = CallGraphTestUtil.buildRTA(options, new AnalysisCacheImpl(), cha, scope);
//		CallGraph cg = CallGraphTestUtil.buildVanillaZeroOneCFA(options, new AnalysisCacheImpl(), cha, scope);		
		// -------- End @@@ 9/28th

		long finish_cg = System.currentTimeMillis();
		long timeElapsed_cg = finish_cg - start_cg;
		double sec = timeElapsed_cg / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to build cg: " + timeElapsed_cg + ", in sec: " + sec);

		SrcCode src = new SrcCode();
		ArrayList<String> methods_list = new ArrayList<>();
		methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
		 if(Print) 
			 System.out.print("methods_list:" + methods_list.toString());
		src.set_values(appSrc, className, methods_list);
		// if(Print) System.out.println("##### 0\n");

		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
		
		// if(Print) System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
//		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);

		Statement seed_s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
		if(seed_s != null)
			
		if(Print) 
			System.out.println("----> Seed stmt: " + seed_s.toString() + "\n===============");
		NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node,
				lineNumber, key);
		// we need a NormalReturnCaller statement to slice from the return value
		NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());
		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
		Collection<Statement> backward_slice;
		long start_slice = System.currentTimeMillis();
		// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
		
		if (seed_type == 'n') {
			System.out.println("Hi n");
			backward_slice = Slicer.computeBackwardSlice(seed_ns, cg, pa, dOptions, cOptions);
			
		} else if (seed_type == 'r') {
			System.out.println("Hi r");
			backward_slice = Slicer.computeBackwardSlice(seed_nrc, cg, pa, dOptions, cOptions);
			
		} else {// if(seed_type == 's')
//			System.out.println("Hi else");
			backward_slice = Slicer.computeBackwardSlice(seed_s, cg, pa, dOptions, cOptions);

		}
//		System.out.println("Hi dump");
		if(Print)SlicerUtil.dumpSlice(backward_slice);
//System.out.println("Hi dump");
		long finish_slice = System.currentTimeMillis();
		long timeElapsed_slice = finish_slice - start_slice;
		double sec_slice = timeElapsed_slice / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to compute the slice: " + timeElapsed_slice + ", in sec: " + sec_slice);
//		for (Statement s : backward_slice) {
//			if (s != null) {
//
//				String line = StmtFormater.format_filtered_stmt(s, methods_list);
//				if (line == null) {
//
//					if (s.toString().contains("setString")) {
//						if(Print) System.out.println("**** setString : "+StmtFormater.format(s));
//					}
//
//					if (s.toString().contains("valueOf")) {
//						if(Print) System.out.println("**** valueOf : "+StmtFormater.format(s));
//					}
//					//nextInt
//					if (s.toString().contains("nextInt")) {
//						if(Print) System.out.println("**** valueOf : "+StmtFormater.format(s));
//					}
//				}
////				else {
////					if(Print) System.out.println(line);
////				}
//			}
//		}
//		if(Print) System.out.println("\n############################################# backward_slice: ");
//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.FULL,
//				Slicer.ControlDependenceOptions.NONE);
//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed , cg, pa, Slicer.DataDependenceOptions.FULL,
//				Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);//I got only the seed

//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed_nrc , cg, pa, dOptions,
//				cOptions);//

		// ------------------- makeZeroOneCFABuilder

		// seed_ns , ... , Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES --> Only the SEED
		// nrc_seed ,...,
		// DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
		// --> Only the SEED
		// nrc_seed , ..., Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.FULL --> EMPTY
		// seed_ns , ..., Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.FULL --> 5 hours and still no results!
		// seed_ns , ...,
		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE-->
		// prepared + execute

		// ------------------- makeZeroCFABuilder
		// nrc_seed ,...,
		// Slicer.DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
		// --> ?? long
		// nrc_seed ,...,
		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE

//		List<Statement> normalsInSlice = backward_slice.stream()
//				.filter(s -> s instanceof NormalReturnCaller && s.getNode().equals(targeted_method_node))
//				.collect(Collectors.toList());
//		if(Print) System.out.print(normalsInSlice);
//		SlicerUtil.dumpSlice(backward_slice);

		long start_map_slice_src = System.currentTimeMillis();

		List<SliceLine> sliceLines = new ArrayList<>();
		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(backward_slice, src.methods, src.lines);

		long finish_map_slice_src = System.currentTimeMillis();
		long timeElapsed_map_slice_src = finish_map_slice_src - start_map_slice_src;
		double sec_slice_src = timeElapsed_map_slice_src / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to map the slice to src code: " + timeElapsed_map_slice_src + ", in sec: "
				+ sec_slice_src);

		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("\n\n\n############################################# forward_slice: ");
//		Collection<Statement> forward_slice = Slicer.computeForwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
//				Slicer.ControlDependenceOptions.NONE);
//		
//		for (Statement s : forward_slice) {
//			if (s != null) {
//				String line = StmtFormater.format_filtered_stmt(s, methods);
//				if (line != null)
//					if(Print) System.out.println(line);
//			}
//		}
//		
//		List<SliceLine> sliceLines = new ArrayList<>();
//		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice,src.methods, src.lines);
//		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("################################################## 4");
//
//		//In case of two slices bw & fw
//		List<SliceLine> sortedList = new ArrayList<>();
//		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine( backward_slice, forward_slice,
//				src.methods, src.lines);
//		if(Print) System.out.println("##### 5");
//		return sortedList;
		// 1 ==================================
	}


	public static List<SliceLine> do_slice_bw(String appJar, String className, int lineNumber, String appSrc,
			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {

		// similar to @Test - testList() .
		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java
		if(Print)System.out.println("In do_slice_bw");
		long start_cg = System.currentTimeMillis();
		
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);

		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
		CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
		CallGraph cg = builder.makeCallGraph(options, null);

		System.out.println("done");
        System.out.println(CallGraphStats.getStats(cg));
        
        
		long finish_cg = System.currentTimeMillis();
		long timeElapsed_cg = finish_cg - start_cg;
		double sec = timeElapsed_cg / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to build cg: " + timeElapsed_cg + ", in sec: " + sec);

		SrcCode src = new SrcCode();
		/* *** */ ArrayList<String> methods_list = new ArrayList<>();
		/* *** */ methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
		//* *** */ if(Print) System.out.print("methods_list:" + methods_list.toString());
		/* *** */ src.set_values(appSrc, className, methods_list);
		// if(Print) System.out.println("##### 0\n");

		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
		if(methodName != null) {
		// if(Print) System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
//		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);

		Statement seed_s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
		if(seed_s != null)
			
		if(Print) 
			System.out.println("----> Seed stmt: " + seed_s.toString() + "\n===============");
		NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node,
				lineNumber, key);
		// we need a NormalReturnCaller statement to slice from the return value
		NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());
		
//		CallGraphBuilder<InstanceKey> builder;
//		builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
		
		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
		Collection<Statement> backward_slice;
		long start_slice = System.currentTimeMillis();
		// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
		
		if (seed_type == 'n') {
			System.out.println("Hi n");
			backward_slice = Slicer.computeBackwardSlice(seed_ns, cg, pa, dOptions, cOptions);
			
		} else if (seed_type == 'r') {
			System.out.println("Hi r");
			backward_slice = Slicer.computeBackwardSlice(seed_nrc, cg, pa, dOptions, cOptions);
			
		} else {// if(seed_type == 's')
//			System.out.println("Hi else");
			backward_slice = Slicer.computeBackwardSlice(seed_s, cg, pa, dOptions, cOptions);

		}
//		System.out.println("Hi dump");
//		SlicerUtil.dumpSlice(backward_slice);
//		SlicerUtil.dumpSlice2(backward_slice);
		
//		System.out.println(backward_slice.toString());
		long finish_slice = System.currentTimeMillis();
		long timeElapsed_slice = finish_slice - start_slice;
		double sec_slice = timeElapsed_slice / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to compute the slice: " + timeElapsed_slice + ", in sec: " + sec_slice);
//		for (Statement s : backward_slice) {
//			if (s != null) {
//
//				String line = StmtFormater.format_filtered_stmt(s, methods_list);
//				if (line == null) {
//
//					if (s.toString().contains("setString")) {
//						if(Print) System.out.println("**** setString : "+StmtFormater.format(s));
//					}
//
//					if (s.toString().contains("valueOf")) {
//						if(Print) System.out.println("**** valueOf : "+StmtFormater.format(s));
//					}
//					//nextInt
//					if (s.toString().contains("nextInt")) {
//						if(Print) System.out.println("**** valueOf : "+StmtFormater.format(s));
//					}
//				}
////				else {
////					if(Print) System.out.println(line);
////				}
//			}
//		}
//		if(Print) System.out.println("\n############################################# backward_slice: ");
//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.FULL,
//				Slicer.ControlDependenceOptions.NONE);
//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed , cg, pa, Slicer.DataDependenceOptions.FULL,
//				Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);//I got only the seed

//		Collection<Statement> backward_slice = Slicer.computeBackwardSlice(seed_nrc , cg, pa, dOptions,
//				cOptions);//

		// ------------------- makeZeroOneCFABuilder

		// seed_ns , ... , Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES --> Only the SEED
		// nrc_seed ,...,
		// DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
		// --> Only the SEED
		// nrc_seed , ..., Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.FULL --> EMPTY
		// seed_ns , ..., Slicer.DataDependenceOptions.FULL,
		// Slicer.ControlDependenceOptions.FULL --> 5 hours and still no results!
		// seed_ns , ...,
		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE-->
		// prepared + execute

		// ------------------- makeZeroCFABuilder
		// nrc_seed ,...,
		// Slicer.DataDependenceOptions.FULL,Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES
		// --> ?? long
		// nrc_seed ,...,
		// Slicer.DataDependenceOptions.NO_HEAP,Slicer.ControlDependenceOptions.NONE

//		List<Statement> normalsInSlice = backward_slice.stream()
//				.filter(s -> s instanceof NormalReturnCaller && s.getNode().equals(targeted_method_node))
//				.collect(Collectors.toList());
//		if(Print) System.out.print(normalsInSlice);
//		SlicerUtil.dumpSlice(backward_slice);

		long start_map_slice_src = System.currentTimeMillis();

		List<SliceLine> sliceLines = new ArrayList<>();
		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(backward_slice, src.methods, src.lines);

		long finish_map_slice_src = System.currentTimeMillis();
		long timeElapsed_map_slice_src = finish_map_slice_src - start_map_slice_src;
		double sec_slice_src = timeElapsed_map_slice_src / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to map the slice to src code: " + timeElapsed_map_slice_src + ", in sec: "
				+ sec_slice_src);

		return sliceLines;
		}
		return null;
		// -------------------------------------------------
//		if(Print) System.out.println("\n\n\n############################################# forward_slice: ");
//		Collection<Statement> forward_slice = Slicer.computeForwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
//				Slicer.ControlDependenceOptions.NONE);
//		
//		for (Statement s : forward_slice) {
//			if (s != null) {
//				String line = StmtFormater.format_filtered_stmt(s, methods);
//				if (line != null)
//					if(Print) System.out.println(line);
//			}
//		}
//		
//		List<SliceLine> sliceLines = new ArrayList<>();
//		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice,src.methods, src.lines);
//		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("################################################## 4");
//
//		//In case of two slices bw & fw
//		List<SliceLine> sortedList = new ArrayList<>();
//		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine( backward_slice, forward_slice,
//				src.methods, src.lines);
//		if(Print) System.out.println("##### 5");
//		return sortedList;
		// 1 ==================================
	}


	public static List<SliceLine> do_slice_bw_cg(CallGraph cg, CallGraphBuilder<InstanceKey> builder, String appJar, String className, int lineNumber, String appSrc,
			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {

		// similar to @Test - testList() .
		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java
		if(Print)System.out.println("In do_slice_bw_cg ...");
		
		
        //System.out.println(CallGraphStats.getStats(cg));
        
		SrcCode src = new SrcCode();
		/* *** */ ArrayList<String> methods_list = new ArrayList<>();
		/* *** */ methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
		//* *** */ if(Print) System.out.print("methods_list:" + methods_list.toString());
		/* *** */ src.set_values(appSrc, className, methods_list);
		// if(Print) System.out.println("##### 0\n");

		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
		if(methodName != null) {
		// if(Print) System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
//		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);

		Statement seed_s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
		if(seed_s != null) {
			
			if(Print) 
				System.out.println("----> Seed stmt: " + seed_s.toString() + "\n===============");
			NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node,
					lineNumber, key);
			// we need a NormalReturnCaller statement to slice from the return value
			NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());
			
//			CallGraphBuilder<InstanceKey> builder;
//			builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
			
			final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
			Collection<Statement> backward_slice;
			long start_slice = System.currentTimeMillis();
			// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
			
			if (seed_type == 'n') {
				System.out.println("Hi n");
				backward_slice = Slicer.computeBackwardSlice(seed_ns, cg, pa, dOptions, cOptions);
				
			} else if (seed_type == 'r') {
				System.out.println("Hi r");
				backward_slice = Slicer.computeBackwardSlice(seed_nrc, cg, pa, dOptions, cOptions);
				
			} else {// if(seed_type == 's')
//				System.out.println("Hi else");
				backward_slice = Slicer.computeBackwardSlice(seed_s, cg, pa, dOptions, cOptions);

			}
//			System.out.println("Hi dump");
//			SlicerUtil.dumpSlice(backward_slice);
//			SlicerUtil.dumpSlice2(backward_slice);
			
//			System.out.println(backward_slice.toString());
			long finish_slice = System.currentTimeMillis();
			long timeElapsed_slice = finish_slice - start_slice;
			double sec_slice = timeElapsed_slice / 1000.0;
			if(Print) System.out.println("\nTime Elapsed to compute the slice: " + timeElapsed_slice + ", in sec: " + sec_slice);

			long start_map_slice_src = System.nanoTime();

			List<SliceLine> sliceLines = new ArrayList<>();
			sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(backward_slice, src.methods, src.lines);

			long finish_map_slice_src = System.nanoTime();
			long timeElapsed_map_slice_src = finish_map_slice_src - start_map_slice_src;
			double sec_slice_src = timeElapsed_map_slice_src / 1000000000.0;
//			if(Print) 
			System.out.println("\nTime Elapsed to map the slice to src code: " + timeElapsed_map_slice_src + ", in sec: "
					+ sec_slice_src);

			return sliceLines;

		}
			}
		return null;
		// -------------------------------------------------
//		if(Print) System.out.println("\n\n\n############################################# forward_slice: ");
//		Collection<Statement> forward_slice = Slicer.computeForwardSlice(nrc_seed , cg, pa, Slicer.DataDependenceOptions.NO_HEAP,
//				Slicer.ControlDependenceOptions.NONE);
//		
//		for (Statement s : forward_slice) {
//			if (s != null) {
//				String line = StmtFormater.format_filtered_stmt(s, methods);
//				if (line != null)
//					if(Print) System.out.println(line);
//			}
//		}
//		
//		List<SliceLine> sliceLines = new ArrayList<>();
//		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice,src.methods, src.lines);
//		return sliceLines;
		// -------------------------------------------------
//		if(Print) System.out.println("################################################## 4");
//
//		//In case of two slices bw & fw
//		List<SliceLine> sortedList = new ArrayList<>();
//		sortedList = SlicerUtil.map_filtered_bf_slice_stmts_to_SliceLine( backward_slice, forward_slice,
//				src.methods, src.lines);
//		if(Print) System.out.println("##### 5");
//		return sortedList;
		// 1 ==================================
	}


	public static List<SliceLine> do_slice_fw(String appJar, String className, int lineNumber, String appSrc,
			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {

		// similar to @Test - testList() .
		// https://github.com/wala/WALA/blob/743f4d7457128ea2a4e6d1e8f0f8676a1cbfa435/com.ibm.wala.core/src/test/java/com/ibm/wala/core/tests/slicer/SlicerTest.java
		long start_cg = System.currentTimeMillis();
		
		// ------- Original code
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
//		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
//				"L" + className); // --------  made this change on 9/28th - Deprecated
		Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(cha, "L"+className);// New
		// -------- made the changes below on 9/28th Start @@
//		AnalysisScope scope = AnalysisScopeReader.makePrimordialScope((new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
//		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8")))); // --------  made this change on 9/28th
//		Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
		// -------- End @@ 9/28th
		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);// new
		// AnalysisOptions(scope,
		// entrypoints);

		
		CallGraphBuilder<InstanceKey> builder;
		builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
//		if (cg_type == 1) {
//			builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
//		} else {
//			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope, null, null);
//			
//		}
		// --------  commented line below  on 9/28th - original code
		CallGraph cg = builder.makeCallGraph(options, null);
		// -------- made the changes below on 9/28th Start @@@
//		CallGraph cg = CallGraphTestUtil.buildZeroOneCFA(options, new AnalysisCacheImpl(), cha, scope, false);
//		CallGraph cg = CallGraphTestUtil.buildRTA(options, new AnalysisCacheImpl(), cha, scope);
//		CallGraph cg = CallGraphTestUtil.buildVanillaZeroOneCFA(options, new AnalysisCacheImpl(), cha, scope);
		
		// -------- End @@@ 9/28th
		long finish_cg = System.currentTimeMillis();
		long timeElapsed_cg = finish_cg - start_cg;
		double sec = timeElapsed_cg / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to build cg: " + timeElapsed_cg + ", in sec: " + sec);

		SrcCode src = new SrcCode();
		ArrayList<String> methods_list = new ArrayList<>();
		methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
		// final ArrayList<String> methods_list_final = new ArrayList<>(methods_list);
		// if(Print) System.out.print("methods_list_final:" + methods_list_final);
		src.set_values(appSrc, className, methods_list);
		// if(Print) System.out.println("##### 0\n");

		String methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
		// if(Print) System.out.println("methodName:" + methodName);
		CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
//		CGNode targeted_method_node =CallGraphSearchUtil.findMethod(cg, methodName);

		Statement seed_s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
		// if(Print) System.out.println("Seed stmt: " + seed_s.toString());
//		if(Print) System.out.println("----> Seed stmt: " + seed_s.toString() + "\n===============");

		NormalStatement seed_ns = (NormalStatement) SlicerUtil.find_seed_by_lno_and_key(targeted_method_node,
				lineNumber, key);
		// we need a NormalReturnCaller statement to slice from the return value
		NormalReturnCaller seed_nrc = new NormalReturnCaller(targeted_method_node, seed_ns.getInstructionIndex());
		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
		Collection<Statement> forward_slice;

		long start_slice = System.currentTimeMillis();

		// 's' = Statement, 'n'= NormalStatement, 'r' = NormalReturnCaller
		if (seed_type == 'n') {
			forward_slice = Slicer.computeForwardSlice(seed_ns, cg, pa, dOptions, cOptions);
		} else if (seed_type == 'r') {
			forward_slice = Slicer.computeForwardSlice(seed_nrc, cg, pa, dOptions, cOptions);

		} else {// if(seed_type == 's')
			forward_slice = Slicer.computeForwardSlice(seed_s, cg, pa, dOptions, cOptions);

		}
		long finish_slice = System.currentTimeMillis();
		long timeElapsed_slice = finish_slice - start_slice;
		double sec_slice = timeElapsed_slice / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to compute the slice: " + timeElapsed_slice + ", in sec: " + sec_slice);
//		for (Statement s : forward_slice) {
//			if (s != null) {
//
//				String line = StmtFormater.format_filtered_stmt(s, methods_list);
//				if (line == null) {
//
//					if (s.toString().contains("setString")) {
//						if(Print) System.out.println("**** setString : "+StmtFormater.format(s));
//					}
//
//					if (s.toString().contains("valueOf")) {
//						if(Print) System.out.println("**** valueOf : "+StmtFormater.format(s));
//					}
//					//nextInt
//					if (s.toString().contains("nextInt")) {
//						if(Print) System.out.println("**** valueOf : "+StmtFormater.format(s));
//					}
//				}
////				else {
////					if(Print) System.out.println(line);
////				}
//			}
//		}
		// SlicerUtil.dumpSlice(forward_slice);
		long start_map_slice_src = System.currentTimeMillis();

		List<SliceLine> sliceLines = new ArrayList<>();
		sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(forward_slice, src.methods, src.lines);

		long finish_map_slice_src = System.currentTimeMillis();
		long timeElapsed_map_slice_src = finish_map_slice_src - start_map_slice_src;
		double sec_slice_src = timeElapsed_map_slice_src / 1000.0;
		if(Print) System.out.println("\nTime Elapsed to map the slice to src code: " + timeElapsed_map_slice_src + ", in sec: "
				+ sec_slice_src);
		return sliceLines;
		// -------------------------------------------------

	}

	public static void main(String[] args)
			throws IOException, WalaException, CancelException, InvalidClassFileException {
//        find_all_class_methods("src\\main\\java\\CWE15_External_Control_of_System_or_Configuration_Setting.jar" , "CWE15_External_Control_of_System_or_Configuration_Setting__connect_tcp_02");
//        if(Print) System.out.println(do_thin_slicer("src\\main\\java\\CWE15_External_Control_of_System_or_Configuration_Setting.jar" , "goodG2B1" , "CWE15_External_Control_of_System_or_Configuration_Setting__connect_tcp_02" ,166));
		String trainingPath = "/Users/Dareen/Fixer/Experiments/TrainingCases/example_";
		String testingPath = "/Users/Dareen/Fixer/Experiments/TestCases/test_";
		int lno ;
		
		// ---------------------------------------------- test client apps
//		String appSrc= //"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src/main/java/com/revature/Staff.java";
//						"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src/main/java/com/revature/Staff.java";
////		"/Users/Dareen/Fixer/Experiments/SRC/banking-app-devkala48-master/src/main/java/com/revature/Staff.java";
//		String appJar= //"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app-devkala48-master.jar";
//		"/Users/Dareen/Fixer/Experiments/JARS/banking-app-devkala48-master.jar";
//		String key = "executeQuery"; lno = 27;
		
		
		String appJar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/JavaGit-master.jar";
//		"/Users/Dareen/Fixer/Experiments/JARS/JavaGit-master.jar";
		String appSrc= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/JavaGit-master/src/gs/veepeek/assessment/Assesment.java";
//		String key = "executeUpdate"; lno = 122;
		String key = "executeUpdate"; lno = 131;
//		String classPath = "gs/veepeek/assessment/Assesment";
		
//		String appSrc =  "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/PaperExample_vul.java";
//		String appJar = "/Users/Dareen/Fixer/Experiments/TrainingCases/smallbank.jar";
//		String key = "createStatement";lno = 32;//, sstype = 'r'
		
		
//		String appSrc = "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/PaperExample_vul.java";
//		String appSrc = "/Users/Dareen/NetBeansProjects/smallBank/src/TSet/" + "SQLIV_q_s_v.java";;
		/// Users/Dareen/NetBeansProjects/smallBank/src/TSet/SQLIV_q_s_v.java
		
//		String appJar = "/Users/Dareen/Fixer/Experiments/TrainingCases/example_2/smallbank.jar";
		
//		
		String[] subpath = appSrc.split("/");
		String className_only = (subpath[subpath.length - 1].split("\\."))[0];
//		String classPath = subpath[subpath.length - 2] + "/";
//		String classPath = subpath[subpath.length - 3] + "/"+ subpath[subpath.length - 2] + "/";
//		String classPath = "main/java/com/revature/";
		String classPath = "gs/veepeek/assessment/";
//		"com/revature/";
//		main/java/com/revature/Staff
		String className = classPath + className_only;
		
//		String className = 
//				"com/revature/Staff";
//		"java/com/revature/Staff";
//		"com/revature/Staff";
//		"Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src/main/java/com/revature/Staff";
//		"banking-app-devkala48-master/src/main/java/com/revature/Staff";

//		List<SliceLine> sliceLines = do_thin_slicer_by_only_lno(appJar, className, 14, appSrc, "");
//		if(Print) System.out.print(sliceLines.toString());
		// ===========================================

		
//		String key = "executeQuery"; lno = 13;
//		//temp.contains("")
		char sstype = 's';
//		
//		NOTE: key= "Scanner" or "StringBuilder", use seed_type ='s'.
//		NOTE: key= "append(I)Ljava/lang/StringBuilder;", use seed_type ='r' not 's'.
		if(Print) System.out.println("&&&& appSrc: "+appSrc);
		if(Print) System.out.println("&&&& className: "+className);
		List<SliceLine> sliceLines = SlicerTool.do_slice_bw2(appJar, className, lno, appSrc, 0,
				DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, sstype, key);
		if(Print) System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@ FW Slice\n" + sliceLines.toString());

//		List<SliceLine> sliceLines2 = SlicerTool.do_slice_bw(appJar, className, lno, appSrc, 0, 
//				DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', key);

//				//do_slice_fw(appJar, className, lno, appSrc, 0,
//				//DataDependenceOptions.NO_HEAP, ControlDependenceOptions.FULL, 's', key);
//		List<SliceLine> sliceLines3 = SlicerTool.do_thin_slicer_by_lno_nrc(appJar, className, lno, appSrc, key);
//		//.do_slice_bw(appJar, className, lno, appSrc, 0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.FULL, 's', key);
//		
//		if(Print) System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@ BW Slice\n" + sliceLines2.toString());
//		
//		if(Print) System.out.println("Thin Slice\n"+sliceLines3.toString());
		// ===========================================
//		if(Print) System.out.println("**************** First test");
//		MyAnalyzer.start_anlysis(appSrc, appJar, className);
//		for (TA_MD md : MyAnalyzer.APs) {
//			if(Print) System.out.println("IP@line: " + md.lno + ", Type is: " + md.getType() + ", UI type is: "+ md.getUIT());
//		}
		// ===========================================

//		if(Print) System.out.println("**************** Second test");
//		String appSrc2 = "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/PaperExample_vul.java";
////		String appJar = "/Users/Dareen/Fixer/Experiments/TrainingCases/example_2/smallbank.jar";
//		String appJar2 = "/Users/Dareen/Fixer/Experiments/TrainingCases/smallbank.jar";
//		String[] subpath2 = appSrc2.split("/");
//		String classPath2 = subpath2[subpath2.length - 2] + "/";
//		String className_only2 = (subpath2[subpath2.length - 1].split("\\."))[0];
//		String className2 = classPath2 + className_only2;
//		
//		MyAnalyzer.start_anlysis(appSrc2, appJar2, className2);
//		
//		for (TA_MD md : MyAnalyzer.APs) {
//			if(Print) System.out.println("IP@line: " + md.lno + ", Type is: " + md.getType());
//
//		}

//		int tr_i = 2;
////		String key = "getConnection";
////		String key ="executeQuery";
//		String key = "prepareStatement";
//
//		SeedList sl = SrcCodeUtil.find_class_seeds_key(SrcCodeUtil.get_src_lines(appSrc), key);
//
//		if (!sl.is_empty) {
//			if(Print) System.out.println(sl.key);
//			if(Print) System.out.println(sl.LinesNo);
//			for (int i = 0; i < sl.LinesNo.size(); i++) {
//
//				if(Print) System.out.println("**** " + sl.LinesNo.get(i));
//				// sl.LinesNo.get(i).method
//				if(Print) System.out.println("=========== Slice # " + i);
//				// To do : write a slicer that receives a ncr (normal call return)
//
//				List<SliceLine> sliceLines = do_thin_slicer_by_only_lno(appJar, className, sl.LinesNo.get(i), appSrc);
////				List<SliceLine> sliceLines = do_thin_slicer_by_lno_nrc( appJar,  className, sl.LinesNo.get(i),  appSrc);
//				// do_thin_slicer_SliceLineList( appJar,"main" , className, sl.LinesNo.get(i),
//				// appSrc);
//
//				if(Print) System.out.println(sliceLines);
//
////				String testNo = "" + tr_i;
////				String sliceNo = "_" + className + "_" + i;
//				String slicePath = trainingPath + tr_i + "/slices/"; // Could be testingPath
////				String slicePath = testingPath+ tr_i + "/slices/";
//				String slicefile = className_only + "_" + "slice_" + i + ".java";
////				if(Print) System.out.println("----- "+slicePath+slicefile);
//				SrcCodeUtil.write_slice_to_file(className_only, slicePath, slicefile, sliceLines);
//			}
//		}

		if(Print) System.out.println("className :" + className);
		// TODO: How would use the method name?
		// 1- organize the output to a file.
		// 2- Remove un-necessary data like "try{", "{" , ...
		// 3- Write the slice to a file; unify functions types and return value!??

		// TO DO: start a loop to go over all training cases:

		// TO DO: start a loop to go over all test cases:

//		if(Print) System.out.println(do_thin_slicer(appJar, "main",src.className , 18));
		if(Print) System.out.println("==================");

		if(Print) System.out.print("Done!");

		// ---------------------------------------------- test regular apps
//		String appSrc = "/Users/Dareen/Fixer/Experiments/TrainingCases/example_1/add5.java";
//		String appJar = "/Users/Dareen/Fixer/Experiments/TrainingCases/example_1/calculator.jar";
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
//			if(Print) System.out.println(sl.key);
//			if(Print) System.out.println(sl.LinesNo);
//			for(int i = 0; i<sl.LinesNo.size();i++) {
//				
//				if(Print) System.out.println("**** "+sl.LinesNo.get(i));
//				// sl.LinesNo.get(i).method
//				if(Print) System.out.println("=========== Slice # "+ i);
//				
//				List<SliceLine> sliceLines = do_thin_slicer_by_only_lno( appJar,  className, sl.LinesNo.get(i),  appSrc);
//				//do_thin_slicer_SliceLineList( appJar,"main" ,  className, sl.LinesNo.get(i),  appSrc);//needs method name
//				
//				if(Print) System.out.println(sliceLines);
//				
//
//				String slicePath = trainingPath+ tr_i + "/slices/"; // Could be testingPath
//				String slicefile =  className_only + "_"+ "slice_" +i + ".java";
//				SrcCodeUtil.write_slice_to_file(className, slicePath ,  slicefile,  sliceLines);
//			}
//		}
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
 * if(Print) System.out.println(line); } }
 * if(Print) System.out.println("End of the Slice *******************");
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
