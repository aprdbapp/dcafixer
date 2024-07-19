package slicer.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrike.shrikeCT.*;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;

import slicer.datatypes.CodeLine;
import slicer.datatypes.SeedList;
import slicer.datatypes.SliceLine;

public class SrcCodeUtil {

	/*
	 * TO DO: fun: load source code - return list of strings. fun: convert lines to
	 * source code fun: Write slice to a file XX.java fun:
	 */

	static int findCodeLine(String funcName, int use, String filePath) {
		int lno = 0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			int line_count = 1;
			boolean main_started = false;
			int funcUse = 0;
			while (line != null) {
				if (line.contains("main") && line.contains("args")) {
					main_started = true;
					System.out.println(line_count + " " + line + " MAIN oppening");
				} else {
					System.out.println(line_count + " " + line);
				}

				// If the function is created after main, this code won't work well
				// We assume that the called function is in main
				// & main is at the end of the file
				if (line.contains(funcName) && main_started) {
					funcUse++;
					if (use > 0) {
						if (funcUse == use) {
							lno = line_count;
							break;
						}
					} else if (use == 0) {
						lno = line_count; // First call is used
						break;
					}
				}
//				System.out.println(line_count + " " + line);
				line_count++;
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lno;

	}

	public static List<String> find_all_class_methods_class(String appJar, String className, String EXCLUSIONS)
			throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
		List<String> methods = new

		ArrayList<>();
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes(StandardCharsets.UTF_8))));
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		CallGraphBuilder<?> cgb = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope,
				null, null);
		CallGraph cg = cgb.makeCallGraph(options, null);
		Iterator<? extends CGNode> it = cg.iterator();
		while (it.hasNext()) {
			CGNode n = it.next();
			if (n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {

				methods.add(n.getMethod().getName().toString());
			}
		}
		return methods;
	}

	public static ArrayList<CodeLine> get_src_lines(String path) throws IOException {
		int lno = 0;
		ArrayList<CodeLine> lines = new ArrayList<CodeLine>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;

			while ((line = reader.readLine()) != null) {
				lno++;
				CodeLine cl = new CodeLine();
				cl.setLno(lno);
				cl.setStmt(line);
				lines.add(cl);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		reader.close();
		return lines;
	}

	public static SeedList find_class_seeds_key(List<CodeLine> srcLines, String key) {
		SeedList seeds = new SeedList();
		seeds.key = key;
		for (CodeLine line : srcLines) {

			// TO DO: try to implement this function using the CG.
			if (line.getStmt().contains(key)
					&& !(line.getStmt().contains("public") || line.getStmt().contains("static")
							|| line.getStmt().contains("import"))
					&& !((line.getStmt().replace(" ", "").replace("\t", "")).startsWith("//"))) {
//				System.out.println("######## " + line.getStmt().replace(" ", ""));
				seeds.is_empty = false;
				seeds.LinesNo.add(line.getLno());
			}
		}
		return seeds;
	}

	public static void write_slice_to_file(String className, String slicePath, String slicefile,
			List<SliceLine> sliceLines) throws IOException {
		System.out.println("****  #0");
		String slice_srclines = "\npublic class " + className + " {" + format_methods_lines(sliceLines) + "\n}";
		System.out.println("****  #1");
		/*
		 * write slice_lines to a file in
		 * ../Fixer/Experiments/TrainingCases/example_i/slices
		 */
//		String slicePath = path + testNo + "/slices/slice" + sliceNo + ".java";
		System.out.println("****  #2");
		System.out.println("+++++ " + slicePath + slicefile);
		System.out.println("****  #3");
		File file = new File(slicePath + slicefile);
//		file.createNewFile();
		// Create the file
		if (file.createNewFile()) {
			System.out.println("File is created!");
		} else {
			System.out.println("File already exists.");
		}

		// Write Content
		FileWriter writer = new FileWriter(file);
		writer.write(slice_srclines);
		writer.close();

	}

	// =====================
	public static String format_methods_lines(List<SliceLine> sliceLines) {
		String Methods_lines = "";
		String MethodName = "";
		for (SliceLine line : sliceLines) {
			// System.out.println("$$$$ #0" + line);
			if (MethodName.isEmpty()) {// isEmpty works when the string's value is "" not when it's null.
//				//				System.out.println("$$$$ #1.1");
				// Start the first method
				MethodName = line.method;
				if (MethodName.equals("main")) {
					// System.out.println("$$$$ #1.11" + MethodName);
					Methods_lines = "\n\n\tpublic static void " + MethodName + "() {\n";
				} else {
					// System.out.println("$$$$ #1.12" + MethodName);
					Methods_lines = "\n\n\tpublic void " + MethodName + " () {\n";
				}

			}
///				System.out.println("$$$$ #1.2");
			if (!(line.method.equals(MethodName))) {
//				//				System.out.println("$$$$ #1.3");
				// close previous method
				Methods_lines += "\n\t}";
				// start the new method
				MethodName = line.method;
				if (MethodName == "main") {
					Methods_lines += "\n\n\tpublic static void " + MethodName + " (){\n";
				} else {
					Methods_lines += "\n\n\tpublic void " + MethodName + " (){\n";
				}
			}
			String cleanLine = line.stmt.replace("\t", "");
			cleanLine = cleanLine.replace("\n", "");
///				System.out.println("$$$$ #2.1" + cleanLine);
			if (cleanLine.contains("try (")) {// &&
//					System.out.println("$$$$ #2.2" + cleanLine);
				cleanLine = line.stmt.replace("try (", "");
				if (cleanLine.endsWith(")")) {
//					System.out.println("$$$$ #2.22" + cleanLine);
					cleanLine = cleanLine.substring(0, cleanLine.length() - 1) + ";";
				}
				if (cleanLine.endsWith(") {")) {
//					System.out.println("$$$$ #2.23" + cleanLine);
					cleanLine = cleanLine.replace(") {", "") + ";";
				}
			}
			cleanLine = cleanLine.replace("\t", "");
///				System.out.println("$$$$ #2.3" + cleanLine);
//			System.out.println("\t\t" + cleanLine + "\n");
			Methods_lines += "\t\t" + cleanLine + "\n";

		}
		// close last method
		if (!Methods_lines.isEmpty())
			Methods_lines += "\n\t}";
		return Methods_lines;
	}

}

//public static List<String> find_class_seeds(String appJar, String className, String EXCLUSIONS, String key)
//throws ClassHierarchyException, CallGraphBuilderCancelException, IOException, InvalidClassFileException {
//List<String> methods = new ArrayList<>();
//CallGraph cg = SlicerUtil.generate_call_graph( appJar, className, EXCLUSIONS);
//Iterator<? extends CGNode> it = cg.iterator();
//while (it.hasNext()) {
//CGNode n = it.next();
//IR ir = n.getIR();
//
//SSAInstruction[] insts = ir.getInstructions();
//for (int i=0; i<insts.length; i++) {
//	System.out.println(insts[i].toString());
//}
//
////IMethod method = n.getMethod();
//// java.util.Iterator<CallSiteReference>	csit = n.iterateCallSites();
//// System.out.println("########");
//// while (csit.hasNext()) {
////	 System.out.println(csit.next().toString());
//// }
//
////IR ir = n.getIR();
////for (int instIndex = 0; instIndex < ir.getInstructions().length; instIndex++) {
////	
////	IBytecodeMethod<?> method = (IBytecodeMethod<?>) ir.getMethod();
//////	method.
////	//Seed line no.
////	int bytecodeIndex = method.getBytecodeIndex(instIndex);
////	int sourceLineNum = method.getLineNumber(bytecodeIndex);
////}
//////if (n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {
//////
//////	methods.add(n.getMethod().getName().toString());
//////}
//}
//return methods;
//}
//
//