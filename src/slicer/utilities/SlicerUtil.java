package slicer.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
//import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
//import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.core.util.strings.Atom;
//import com.ibm.wala.util.strings.Atom;
//import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
//import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
//import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.ParamCaller;
import com.ibm.wala.ipa.slicer.Statement;
//import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;

import slicer.datatypes.CodeLine;
import slicer.datatypes.SliceLine;

public class SlicerUtil {
	public static boolean Print =false;
	// find the method where the seed statement is included.
	public static CGNode find_targeted_method(CallGraph cg, String methodName, String className) {
//		if(Print) System.out.println("MM :"+methodName);
		Atom name = Atom.findOrCreateUnicodeAtom(methodName);
		if(Print) {
			System.out.println("Hello !");
		}
		for (CGNode n : cg) {
			if(Print) {
				System.out.println("Hello !!");
			}
			if(Print) {
				System.out.println("node :"+n.toString());
			}
			if (n.getMethod().getName().equals(name)
					&& n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {
//                if(Print) System.out.println("HHH "+n.getMethod().getDeclaringClass().getName().toString() + " " + n.getMethod().getName().toString());
				return n;
			}
		}
		if(Print) {
			System.out.println("Hello !!!");
		}
		System.out.println("failed to find " + methodName + " method");
//		Assertions.UNREACHABLE("failed to find " + methodName + " method");
		return null;
	}

	public static String find_seed_method(CallGraph cg, String className, int lno) throws InvalidClassFileException {
//		Atom name = Atom.findOrCreateUnicodeAtom(methodName);
		String method = null;
		if(Print) {
			System.out.println("L" + className);
		}
		for (CGNode n : cg) {


//			if(Print) System.out.println(n.getMethod().getDeclaringClass().toString());
			//+++++++++
			//+++++++++
//			if ( !(n.getMethod().getDeclaringClass().getName().toString().equals("L" + className))) {
//				if(Print) System.out.println("- " + n.getMethod().getSignature());
//				continue;
//			} else
//			if(Print)
//			System.out.println("mmmmmm@ "+n.getMethod().getName().toString());
//			if(Print) System.out.println("cccc@ "+n.getMethod().getDeclaringClass().getName().toString());
//			if(Print)
//				System.out.println("--- L" + className);

			if (n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {//=====03/03/2023 commented
//				if(n.getClass().getName().toString().equals("L" + className)) {// didn't work
//				if (n.getMethod().getDeclaringClass().toString().contains("L" + className)) {
//				System.out.println("L" + className);
				if(Print) {
					System.out.println("HERE ! ! !");
				}
					//------------------------
//				n.getClass().getName().toString()
//					for(int i=0; i<1000;i++ )
//						n.getMethod(
//								//getLineNumber(i)==lno)
//							System.out.println(n.getMethod());
					//------------------------
//				if(Print) System.out.println("+ " + n.getMethod().getSignature());
//				if(Print) System.out.println(n.getMethod().getSignature());
				if(Print) {
					System.out.println("HERE !!!!");
				}
				IR ir = n.getIR();

				SSAInstruction[] insts = ir.getInstructions();
				if(Print) {
					System.out.println("HERE ! !");
				}
				for (SSAInstruction inst : insts) {
					if (inst != null) {
						int irLineNo = StmtFormater.IRIdexToLineNumber_stmtFormat(ir, inst.iIndex());
						if (Print) {
							System.out.println("@@@@ irLineNo= " + irLineNo + ", and lno= " + lno
									+ "\ninst: " + inst);
						}
						if (irLineNo == lno) {
							method = n.getMethod().getName().toString();
							return method;
						}
					}
				}
			}
//            if (n.getMethod().getName().equals(name)  && n.getMethod().getDeclaringClass().getName().toString().equals("L" + "testcases/" + appJarName + "/" + className)) {
//			if (n.getMethod().getName().equals(name)
//					&& n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {
////                if(Print) System.out.println(n.getMethod().getDeclaringClass().getName().toString() + " " + n.getMethod().getName().toString());
//				return n;
//			}
		}
//		Assertions.UNREACHABLE("failed to find line (" + lno + ") method");
		System.out.println("Failed to find line (" + lno + ") method");
		System.out.println("L" + className);
		return method;
	}



	public static String find_seed_method2(CallGraph cg, String className, int lno) throws InvalidClassFileException {
//		Atom name = Atom.findOrCreateUnicodeAtom(methodName);
		String method = null;
		for (CGNode n : cg) {
			//			System.out.println("@@@"+n.getMethod().getDeclaringClass().getName().getClassName().toString());
//			System.out.println("### "+n.getMethod().getDeclaringClass().getName().toString());
//			if (n.getMethod().getDeclaringClass().getName().toString().equals(className)) {
			if (n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {
//				if (n.getMethod().getDeclaringClass().toString().contains("L" + className)) {

					if(Print) {
						System.out.println("HERE ! ! !");
					}
				if(Print) {
					System.out.println("HERE !!!!");
				}
				IR ir = n.getIR();
//				System.out.println(ir.toString());
//				System.out.println("*********************\n");
//				System.out.println("CFG:\n"+ir.getControlFlowGraph().toString());

//				SSAInstruction[] insts = ir.getInstructions();
//				if(Print) System.out.println("HERE ! !");
//				for (SSAInstruction inst : insts) {
//					if (inst != null) {
//
//						int irLineNo = StmtFormater.IRIdexToLineNumber_stmtFormat(ir, inst.iIndex());
//						 if(Print)
//							 System.out.println("@@@@ irLineNo= " +irLineNo+", and lno= "+lno +"\ninst: "+inst.toString());
//						if (irLineNo == lno) {
//							method = n.getMethod().getName().toString();
//							return method;
//						}
//					}
//				}// insts it

//				// Results are like ir.getInstructions();
//				for(Iterator<SSAInstruction> allIsnt =ir.iterateAllInstructions();allIsnt.hasNext();) {
//					SSAInstruction inst = allIsnt.next();
//					if (inst != null) {
//
//						int irLineNo = StmtFormater.IRIdexToLineNumber_stmtFormat(ir, inst.iIndex());
//						 if(Print)
//							 System.out.println("@@@ irLineNo= " +irLineNo+", and lno= "+lno +"\ninst: "+inst.toString());
//						if (irLineNo == lno) {
//							method = n.getMethod().getName().toString();
//							return method;
//						}
//					}
//				}

				for (Iterator<ISSABasicBlock> bit = ir.getBlocks(); bit.hasNext();){
					ISSABasicBlock bb = bit.next();
					//Iterator<SSAInstruction> java.lang.Iterable.iterator()
					for (SSAInstruction inst : bb) {
						if (inst != null) {
							int irLineNo = StmtFormater.IRIdexToLineNumber_stmtFormat(ir, inst.iIndex());
							 if(Print) {
								System.out.println("### irLineNo= " +irLineNo+", and lno= "+lno +"\ninst: "+ inst);
							}
							if (irLineNo == lno) {
								method = n.getMethod().getName().toString();
								return method;
							}
						}
					}

//					if(bb.isCatchBlock() ) {
//						System.out.println("CCCCCatch");
//					}
//					if( bb.isExitBlock()) {
//						System.out.println("EEExit");
//					}
//					if( bb.isEntryBlock() ) {
//						System.out.println("EEEEntry");
//					}
				}

			}
		}// End CGNode it
//		Assertions.UNREACHABLE("failed to find line (" + lno + ") method");
		System.out.println("failed to find line (" + lno + ") method");

		return method;
	}


	public static String find_seed_method_in_node(CGNode n, int lno) throws InvalidClassFileException {

		String method = null;

				if(Print) {
					System.out.println("HERE ! ! !");
				}

				IR ir = n.getIR();
//				System.out.println(ir.toString());
//				System.out.println("*********************\n");
//				System.out.println("CFG:\n"+ir.getControlFlowGraph().toString());

//				SSAInstruction[] insts = ir.getInstructions();
//				if(Print) System.out.println("HERE ! !");
//				for (SSAInstruction inst : insts) {
//					if (inst != null) {
//
//						int irLineNo = StmtFormater.IRIdexToLineNumber_stmtFormat(ir, inst.iIndex());
//						 if(Print)
//							 System.out.println("@@@@ irLineNo= " +irLineNo+", and lno= "+lno +"\ninst: "+inst.toString());
//						if (irLineNo == lno) {
//							method = n.getMethod().getName().toString();
//							return method;
//						}
//					}
//				}// insts it

//				// Results are like ir.getInstructions();
//				for(Iterator<SSAInstruction> allIsnt =ir.iterateAllInstructions();allIsnt.hasNext();) {
//					SSAInstruction inst = allIsnt.next();
//					if (inst != null) {
//
//						int irLineNo = StmtFormater.IRIdexToLineNumber_stmtFormat(ir, inst.iIndex());
//						 if(Print)
//							 System.out.println("@@@ irLineNo= " +irLineNo+", and lno= "+lno +"\ninst: "+inst.toString());
//						if (irLineNo == lno) {
//							method = n.getMethod().getName().toString();
//							return method;
//						}
//					}
//				}
				if(Print) {
					System.out.println("HERE !!!!");
				}
				for (Iterator<ISSABasicBlock> bit = ir.getBlocks(); bit.hasNext();){

					ISSABasicBlock bb = bit.next();
					//Iterator<SSAInstruction> java.lang.Iterable.iterator()
					for (SSAInstruction inst : bb) {
						if (inst != null) {
							int irLineNo = StmtFormater.IRIdexToLineNumber_stmtFormat(ir, inst.iIndex());
							 if(Print) {
								System.out.println("### irLineNo= " +irLineNo+", and lno= "+lno +"\ninst: "+ inst);
							}
							if (irLineNo == lno) {
								method = n.getMethod().getName().toString();
								return method;
							}
						}
					}

				}

//		Assertions.UNREACHABLE("failed to find line (" + lno + ") method");
		System.out.println("failed to find line (" + lno + ") method");

		return method;
	}


	// You have to generate the cg to use this method.
	public static ArrayList<String> find_all_class_methods_from_cg(CallGraph cg, String appJar, String className)
			throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
		ArrayList<String> methods = new ArrayList<>();

//		if(Print) System.out.println(it.hashCode());
		for (CGNode n : cg) {


//			if(Print) System.out.println(n.getMethod().getSignature());

			// if (n.getMethod().getDeclaringClass().getName().toString().equals("L" +
			// "testcases/" + appJar.split("\\\\")[appJar.split("\\\\").length -
			// 1].split("\\.")[0] + "/" + className)) {
			if (n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {

				methods.add(n.getMethod().getName().toString());
			}
		}
		return methods;
	}

	public static AnalysisOptions GenerateAnalysisOptions(String appJar, String EXCLUSIONS)
			throws IOException, ClassHierarchyException {
		AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes(StandardCharsets.UTF_8))));
		//(new FileProvider()).getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS) // another option for exclusion
		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
//		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(cha);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		return options;
	}

	// IRindex to LineNumber
	public static int ir_index_to_line_no(IR ir, int instructionIndex) throws InvalidClassFileException {
		IBytecodeMethod<?> method = (IBytecodeMethod<?>) ir.getMethod();
		int bytecodeIndex = method.getBytecodeIndex(instructionIndex);

		int sourceLineNum = method.getLineNumber(bytecodeIndex);
		return sourceLineNum;
	}

	// According to LineNumber , find the seed statement
	public static Statement find_seed_by_lno_and_key(CGNode n, int LineNumber, String key)
			throws InvalidClassFileException {
//		if(Print) System.out.println("I'm IN find_seed_by_lno_and_key ");
		IR ir = n.getIR();
		for (int i = 0; i < ir.getInstructions().length; i++) {
//			/********** Print all instructions line numbers - empty & not empty */
			if(Print) {
				System.out.println(SlicerUtil.ir_index_to_line_no(ir, i));
			}
//			/********** Print all not empty instructions */
//			if (ir.getInstructions()[i] != null) {
//				if(Print) System.out.println("Line(" + SlicerUtil.ir_index_to_line_no(ir, i) + "): " + "iIndex: [ "
//						+ ir.getInstructions()[i].iIndex() + " ], " + ir.getInstructions()[i].toString());
//			}
			if (SlicerUtil.ir_index_to_line_no(ir, i) == LineNumber && ir.getInstructions()[i] != null
					&& ir.getInstructions()[i].toString().contains(key)) {
				/********** Print the seed instruction */
				if(Print) {
					System.out.println("Line(" + SlicerUtil.ir_index_to_line_no(ir, i) + "): " + "iIndex: [ "
							+ ir.getInstructions()[i].iIndex() + " ], " + ir.getInstructions()[i].toString());
				}

				if(Print) {
					System.out.println("Found the seed!!");
				}
				return new NormalStatement(n, i);
//				//#################################################
//
				/********** Print the seed instruction */
//				if(Print) System.out.println("Line(" + SlicerUtil.ir_index_to_line_no(ir, j) + "): " + "iIndex: [ "
//				+ ir.getInstructions()[j].iIndex() + " ], " + ir.getInstructions()[j].toString());
//
//				if(Print) System.out.println("Found the seed!!");
//
//				return new NormalStatement(n, j);
				// #################################################
			}
		}
		if(Print) {
			System.out.println("I'm leaving find_seed_by_lno_and_key ");
		}
//		Assertions.UNREACHABLE("failed to find call to " + " in " + n);
		System.out.println("Failed to find call to " + key + " in " + n);
		return null;
	}

//back up of the old function
//	// According to LineNumber , find the seed statement
//	public static Statement find_seed_by_lno_and_key(CGNode n, int LineNumber) throws InvalidClassFileException {
//		IR ir = n.getIR();
//		for (int i = 0; i < ir.getInstructions().length; i++) {
//
////			if(Print) System.out.println(SlicerUtil.ir_index_to_line_no(ir, i));
////			/********** Print all not empty instructions */
////			if (ir.getInstructions()[i] != null) {
////				if(Print) System.out.println("Line(" + SlicerUtil.ir_index_to_line_no(ir, i) + "): " + "iIndex: [ "
////						+ ir.getInstructions()[i].iIndex() + " ], " + ir.getInstructions()[i].toString());
////			}
//			if (SlicerUtil.ir_index_to_line_no(ir, i) == LineNumber && ir.getInstructions()[i] != null) {
//////				/********** Print the seed instruction */
//////				if(Print) System.out.println("Line(" + SlicerUtil.ir_index_to_line_no(ir, i) + "): " + "iIndex: [ "
//////				+ ir.getInstructions()[i].iIndex() + " ], " + ir.getInstructions()[i].toString());
//////
//////				if(Print) System.out.println("Found the seed!!");
////				return new NormalStatement(n, i);
//				//#################################################
//				int j;
//				for( j = i+1; j<ir.getInstructions().length ;j++) {
//					if (SlicerUtil.ir_index_to_line_no(ir, j) == LineNumber && ir.getInstructions()[j] != null) {
//						if(Print) System.out.println("found another instruction connected to the same line!");
//						if(Print) System.out.println("Line(" + SlicerUtil.ir_index_to_line_no(ir, j) + "): " + "iIndex: [ "
//								+ ir.getInstructions()[j].iIndex() + " ], " + ir.getInstructions()[j].toString());
//
//					}else {
//						j--;
//						break;
//					}
//				}
//				/********** Print the seed instruction */
//				if(Print) System.out.println("Line(" + SlicerUtil.ir_index_to_line_no(ir, j) + "): " + "iIndex: [ "
//				+ ir.getInstructions()[j].iIndex() + " ], " + ir.getInstructions()[j].toString());
//
//				if(Print) System.out.println("Found the seed!!");
//
//				return new NormalStatement(n, j);
//				//#################################################
//			}
//		}
//		Assertions.UNREACHABLE("failed to find call to " + " in " + n);
//		return null;
//	}

	/*
	 * slice_to_lnumbers - take as Input : a slice. Output: a list of source code
	 * line numbers
	 */

	public static Set<Integer> map_filtered_slice_stmts_to_line_no(Collection<Statement> slice,
			List<String> methods_list) {

		Set<Integer> source_lines = new HashSet<>();

		for (Statement s : slice) {
			if (s != null) {

				// ignore special kinds of statements. Focus only on normal and parameter
				// callers.
				if (s.getKind() == Statement.Kind.NORMAL) {
					NormalStatement n = (NormalStatement) s;
					IMethod method = s.getNode().getMethod();
					if (methods_list.contains(method.getName().toString())) {
						int bcIndex, instructionIndex = n.getInstructionIndex();
						try {
							bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
							try {
								int src_line_number = method.getLineNumber(bcIndex);
								// if(Print) System.out.println("Source line number = " + src_line_number);
								source_lines.add(src_line_number);

							} catch (Exception e) {
								System.err.println("Bytecode index no good1");
								System.err.println(e.getMessage());
							}

						} catch (Exception e) {
							System.out.println("it's probably not a BT method (e.g. it's a fakeroot method)");
							System.out.println(e.getMessage());
						}
					}
				} else if (s.getKind() == Statement.Kind.PARAM_CALLER) {

					ParamCaller pc = (ParamCaller) s;
					IMethod method = s.getNode().getMethod();
					if (methods_list.contains(method.getName().toString())) {
						int bcIndex, instructionIndex = pc.getInstructionIndex();
						try {
							bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
							try {
								int src_line_number = method.getLineNumber(bcIndex);
								// if(Print) System.out.println("Source line number = " + src_line_number);
								source_lines.add(src_line_number);

							} catch (Exception e) {
								System.err.println("Bytecode index no good2");
								System.err.println(e.getMessage());
							}
						} catch (Exception e) {
							System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
							System.err.println(e.getMessage());
						}
					}

				}
			}
		}
		return source_lines;
	}

	public static List<SliceLine> map_filtered_slice_stmts_to_SliceLine(Collection<Statement> slice,
			List<String> methods_list, ArrayList<CodeLine> lines) {

//		Set<SliceLine> source_lines = new HashSet<>();
		List<SliceLine> sortedList = new ArrayList<>();

		for (Statement s : slice) {
			SliceLine sl = StmtFormater.format_slice_stmt(s, methods_list, lines);
			// if(Print) System.out.println("$$ "+sl);
			if (sl.lno > 0) {
				sortedList.add(sl);
			}
		}

		Collections.sort(sortedList);
		// Remove duplicate lines
		for (int i = sortedList.size() - 1; i > 0; i--) {
			if (sortedList.get(i).lno == sortedList.get(i - 1).lno) {
				sortedList.remove(i);
			}

		}
		return sortedList;
	}

	public static List<SliceLine> map_filtered_bf_slice_stmts_to_SliceLine(Collection<Statement> bwslice,
			Collection<Statement> fwslice, List<String> methods_list, ArrayList<CodeLine> lines) {

		Set<SliceLine> source_lines = new HashSet<>();

		for (Statement s : bwslice) {
			SliceLine sl = StmtFormater.format_slice_stmt(s, methods_list, lines);
			if (sl.lno > 0) {
				source_lines.add(sl);
			}
		}
		for (Statement s : fwslice) {
			SliceLine sl = StmtFormater.format_slice_stmt(s, methods_list, lines);
			if (sl.lno > 0)
			 {
				source_lines.add(sl);
			// source_lines.add(StmtFormater.format_slice_stmt(s, methods_list, lines));
			}
		}
		// Sort the code & write it to a list!.
		// if(Print) System.out.println(source_lines);
		List<SliceLine> sortedList = new ArrayList<>(source_lines);
		Collections.sort(sortedList);

		// Remove duplicate lines
		for (int i = sortedList.size() - 1; i > 0; i--) {
			if (sortedList.get(i).lno == sortedList.get(i - 1).lno) {
				sortedList.remove(i);
			}

		}
//		if(Print) System.out.println(sortedList);
		return sortedList;
	}

	public static List<SliceLine> merge_two_slices(List<SliceLine> slice1,
			List<SliceLine> slice2) {

		Set<SliceLine> source_lines = new HashSet<>();

		for (SliceLine s : slice1) {
			//****************************************************************
			s.walaStmt.getNode().getGraphNodeId();//----> Check if it's unique! *************************************
			//****************************************************************
//			SliceLine sl = StmtFormater.format_slice_stmt(s, methods_list, lines);
//			if (sl.lno > 0)
				source_lines.add(s);
		}

		for (SliceLine s : slice2) {
				source_lines.add(s);
		}


		// Sort the code & write it to a list!.
		// if(Print) System.out.println(source_lines);
		List<SliceLine> sortedList = new ArrayList<>(source_lines);
		Collections.sort(sortedList);

		// Remove duplicate lines
		for (int i = sortedList.size() - 1; i > 0; i--) {
			if (sortedList.get(i).lno == sortedList.get(i - 1).lno) {
				sortedList.remove(i);
			}

		}
//		if(Print) System.out.println(sortedList);
		return sortedList;
	}

	public static Statement findCallTo(CGNode n, String methodName) {
		IR ir = n.getIR();
		for (SSAInstruction s : Iterator2Iterable.make(ir.iterateAllInstructions())) {
			if (s instanceof SSAAbstractInvokeInstruction call) {
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
					IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
					return new NormalStatement(n, indices.intIterator().next());
				}
			}
		}
		Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
		return null;
	}

// From: https://github.com/wala/WALA/wiki/Slicer
	public static Statement findCallTo_new(CGNode n, String methodName) {
	      IR ir = n.getIR();
	      for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
	        SSAInstruction s = it.next();
	        if (s instanceof SSAAbstractInvokeInstruction call) {
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
	            com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
	            com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
	            return new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());
	          }
	        }
	      }
	      Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
	      return null;
	    }

	public static Statement findCallTo_method_lno(CGNode n, String methodName, int lno) {
	      IR ir = n.getIR();
	      for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
	        SSAInstruction s = it.next();
	        if (s instanceof SSAAbstractInvokeInstruction call) {
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
	            com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
	            com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
	            NormalStatement ns = new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());

	            int bcIndex, instructionIndex = ns.getInstructionIndex();
	            try {
	                bcIndex = ((ShrikeBTMethod) ns.getNode().getMethod()).getBytecodeIndex(instructionIndex);
	                try {
	                  int src_line_number = ns.getNode().getMethod().getLineNumber(bcIndex);
	                 if(Print) {
						System.out.println ( "Source line number = " + src_line_number );
					}
	                  if (src_line_number == lno) {
						return ns;
					}

	                } catch (Exception e) {
	                  System.err.println("Bytecode index no good3");
	                  System.err.println(e.getMessage());
	                }
	              } catch (Exception e ) {
	                System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
	                System.err.println(e.getMessage());
	              }
//	            return new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());
	          }
	        }
	      }
	      Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
	      return null;
	    }

	public static void dumpSlice(Collection<Statement> slice) {
		for (Statement s : slice) {
//			if(Print) System.out.println("===================");
//			StmtFormater.format(s);
//			if(Print) System.out.println("-------------------");
			if (s != null) {
				System.out.println(s); // if(Print) System.out.println(StmtFormater.format(s));
			}
//			if(Print) System.out.println("===================");
		}
	}

	public static void dumpSlice2(Collection<Statement> slice) {
		for (Statement s : slice) {
//			if(Print) System.out.println("===================");
//			StmtFormater.format(s);
//			if(Print) System.out.println("-------------------");
			if (s != null && s.toString().contains("nextLine")) {
				 System.out.println(s); // if(Print) System.out.println(StmtFormater.format(s));
			}
//			if(Print) System.out.println("===================");
		}
	}
	// extend_slice --> Gave me wrong lines!
//	public static Set<Integer> extend_slice(Collection<Statement> slice, List<String> methods_list) {
//		Set<Integer> source_lines = new HashSet<>();
//		for (Statement s : slice) {
//			if (s != null) {
//
//				// ignore special kinds of statements. Focus only on normal and parameter
//				// callers.
//				if (s.getKind() == Statement.Kind.NORMAL) {
//					NormalStatement n = (NormalStatement) s;
//					IMethod method = s.getNode().getMethod();
//					if (methods_list.contains(method.getName().toString())) {
//						int bcIndex, instructionIndex = n.getInstructionIndex();
//						try {
//							bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
//							int first_bcIndex= s.getNode().getIR().getBasicBlockForInstruction(n.getInstruction()).getFirstInstructionIndex();
//							int last_bcIndex= s.getNode().getIR().getBasicBlockForInstruction(n.getInstruction()).getLastInstructionIndex();
//							try {
//								int slice_lno = method.getLineNumber(bcIndex);
//								int first_lno = method.getLineNumber(first_bcIndex);
//								int last_lno = method.getLineNumber(last_bcIndex);
//
//
//								 if(Print) System.out.println("@@@ Source line number = " + slice_lno + ", Block_fl: "+ first_lno +
//										 ", Block_ll: "+ last_lno);
//								source_lines.add(slice_lno);
//								source_lines.add(first_lno);
//								source_lines.add(last_lno);
//
//							} catch (Exception e) {
//								if(Print) System.out.println("Bytecode index no good");
//							}
//
//						} catch (Exception e) {
//							if(Print) System.out.println("it's probably not a BT method (e.g. it's a fakeroot method)");
//						}
//					}
//				}
//			}
//		}
//
//		return source_lines;
//	}
// 	// ******************** I didn't need the following methods!
//	public static boolean CanBeSeedStatement(CGNode n, int LineNumber) throws InvalidClassFileException {
//  return find_seed_by_line_no(n , LineNumber) == null ? false : true;
//}
//
//public static boolean CanBeSeedStatement(CallGraph cg , String appJarName , String methodName , String className , int LineNumber) throws InvalidClassFileException {
//  return CanBeSeedStatement(find_targeted_method(cg , appJarName , methodName , className) , LineNumber);
//}
//
//public static boolean CanBeSeedStatement(String appJar , String appJarName , String methodName , String className , int LineNumber) throws ClassHierarchyException, CallGraphBuilderCancelException, IOException, InvalidClassFileException {
//  return CanBeSeedStatement(generate_call_graph(appJar) , appJarName ,methodName , className , LineNumber);
//}

//	public static CallGraph generate_call_graph(String appJar, String EXCLUSIONS)
//			throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
//
//		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, null);
//		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
//		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
//		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
//		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
//		CallGraphBuilder<?> cgb = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope,
//				null, null);
//		CallGraph cg = cgb.makeCallGraph(options, null);
//
//		return cg;
//	}

//	// ******************** The methods below can help to find all the methods in a class without passing the cg.
//	public static List<String> find_all_class_methods(String appJar, String className, String EXCLUSIONS)
//			throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
//		List<String> methods = new
//
//		ArrayList<>();
//		CallGraph cg = generate_call_graph(appJar, EXCLUSIONS);
//		Iterator<? extends CGNode> it = cg.iterator();
//		while (it.hasNext()) {
//			CGNode n = it.next();
//			// if (n.getMethod().getDeclaringClass().getName().toString().equals("L" + "testcases/" + appJar.split("\\\\")[appJar.split("\\\\").length - 1].split("\\.")[0] + "/" + className)) {
//			if (n.getMethod().getDeclaringClass().getName().toString().equals("L" + className)) {
//
//				methods.add(n.getMethod().getName().toString());
//			}
//		}
//		return methods;
//	}
//
//	public static CallGraph generate_call_graph(AnalysisOptions options, String appJar, String EXCLUSIONS)
//			throws IOException, ClassHierarchyException, CallGraphBuilderCancelException {
//		return generate_call_graph_builder(appJar, EXCLUSIONS).makeCallGraph(options, null);
//	}
//
//	public static CallGraph generate_call_graph(String appJar, String EXCLUSIONS)
//			throws IOException, ClassHierarchyException, CallGraphBuilderCancelException {
//		return SlicerUtil.generate_call_graph(GenerateAnalysisOptions(appJar, EXCLUSIONS), appJar, EXCLUSIONS);
//	}

//	public static CallGraphBuilder generate_call_graph_builder(String appJar, String EXCLUSIONS)
//			throws IOException, ClassHierarchyException {
//		// create ananalysis scope representing the appJar as a J2SE application
//		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, null);
//		// AnalysisScopeReader.readJavaScope(appJar , null ,
//		// slicer.class.getClassLoader());
//		scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
//		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
//
//		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
//		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
//		CallGraphBuilder cgb = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope,
//				null, null);
//		return cgb;
//	}

}
