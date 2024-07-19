package slicer.utilities;

import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
//import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.*;
//import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.shrike.shrikeCT.*;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
//import com.ibm.wala.util.debug.Assertions;

import slicer.datatypes.CodeLine;
import slicer.datatypes.SliceLine;

public class StmtFormater {
	public static String format(Statement stmt) {
		String stmt_string = "";
		switch (stmt.getKind()) {
		case HEAP_PARAM_CALLEE:
		case HEAP_PARAM_CALLER:
		case HEAP_RET_CALLEE:
		case HEAP_RET_CALLER:
			HeapStatement h = (HeapStatement) stmt;
			stmt_string = stmt.getKind() + ": " + h.getNode() + "\\n" + h.getLocation();
			return stmt_string;
		case NORMAL:
			stmt_string = stmt_string + stmt.getKind().toString() + ": ";
			NormalStatement n = (NormalStatement) stmt;
			SSAInstruction instruction = n.getInstruction();
			System.out.println("instruction: " + instruction.toString());

			String lineStr = "";
			try {
				IMethod method = n.getNode().getMethod();
				lineStr += "in method:" + method.getName() + ", at line:"
						+ method.getSourcePosition(n.getInstructionIndex()).getFirstLine();
			} catch (InvalidClassFileException e) {
				// TODO
			}

			lineStr += ", inst:" + instruction;
			stmt_string = stmt_string + lineStr;
//			return lineStr;
			return stmt_string;
		case PARAM_CALLEE:
			stmt_string = stmt_string + stmt.getKind().toString() + ": ";
			ParamCallee paramCallee = (ParamCallee) stmt;
			stmt_string = stmt_string + stmt.getKind() + " " + paramCallee.getValueNumber() + "\\n"
					+ stmt.getNode().getMethod().getName();

			IMethod method = paramCallee.getNode().getMethod();
			stmt_string += ",  in method: " + method.getName();

			return stmt_string;
		case PARAM_CALLER:
			stmt_string = stmt_string + stmt.getKind().toString() + ": ";
			ParamCaller paramCaller = (ParamCaller) stmt;
			stmt_string = stmt_string + stmt.getKind() + " " + paramCaller.getValueNumber() + "\\n"
					+ stmt.getNode().getMethod().getName() + "\\n"
					+ paramCaller.getInstruction().getCallSite().getDeclaredTarget().getName();
			IMethod method2 = paramCaller.getNode().getMethod();
			stmt_string += ",  in method: " + method2.getName();
			return stmt_string;
		case EXC_RET_CALLEE:
		case EXC_RET_CALLER:
		case NORMAL_RET_CALLEE:
		case NORMAL_RET_CALLER:
		case PHI:
		default:

			stmt_string = stmt_string + stmt.getKind().toString() + ": " + stmt;
			IMethod method3 = stmt.getNode().getMethod();
			stmt_string += ",  in method: " + method3.getName();
			return stmt_string;
		}
	}

	public static String format_normal(Statement stmt) {

		NormalStatement n = (NormalStatement) stmt;
		SSAInstruction instruction = n.getInstruction();
//            System.out.println("instruction: " + instruction.toString());

		String lineStr = null;
		try {
			IMethod method = n.getNode().getMethod();
			if (method.getName().toString().equals(("main"))) {
				lineStr += "stmt: " + stmt + "\n";
				lineStr += "in method:" + method.getName();

				lineStr += ", at line:" + method.getSourcePosition(n.getInstructionIndex()).getFirstLine();
				lineStr += ", inst:" + instruction;
			}
		} catch (InvalidClassFileException e) {
			// TODO
		}

		return lineStr;

	}

	public static int IRIdexToLineNumber_stmtFormat(IR ir, int instructionIndex) throws InvalidClassFileException {
		IBytecodeMethod<?> method = (IBytecodeMethod<?>) ir.getMethod();
		int bytecodeIndex = method.getBytecodeIndex(instructionIndex);
		int sourceLineNum = method.getLineNumber(bytecodeIndex);
		return sourceLineNum;
	}

	/* Format only Normal and parameter caller statemants */
	public static String format_filtered_stmt(Statement stmt, List<String> methods_list)
			throws InvalidClassFileException {
		String type = null;
		String method_name = null;
		String target = null;

		int fLine, LLine;
		int irLineNo;

		type = stmt.getKind().toString();

		switch (stmt.getKind()) {
		case NORMAL:
			NormalStatement n = (NormalStatement) stmt;
			SSAInstruction n_instruction = n.getInstruction();

			IMethod method = n.getNode().getMethod();

			method_name = method.getName().toString();
			if (methods_list.contains(method_name)) {
				fLine = method.getSourcePosition(n.getInstructionIndex()).getFirstLine();
				LLine = method.getSourcePosition(n.getInstructionIndex()).getLastLine();
				irLineNo = IRIdexToLineNumber_stmtFormat(n.getNode().getIR(), n_instruction.iIndex());
				return "in method:" + method_name + " @@@line(" + fLine + " , " + LLine + "), irLine @ " + irLineNo
						+ ", type: " + type + ", instruction: " + n_instruction;
			} else {
				return null;
			}

		case PARAM_CALLEE:
			ParamCallee paramCallee = (ParamCallee) stmt;

			IMethod method2 = paramCallee.getNode().getMethod();
			method_name = method2.getName().toString();

			if (methods_list.contains(method_name)) {
				fLine = method2.getSourcePosition(paramCallee.getValueNumber()).getFirstLine();
				LLine = method2.getSourcePosition(paramCallee.getValueNumber()).getLastLine();

				return "in method:" + method_name + " @@@line(" + fLine + " , " + LLine + "), type: " + type
						+ ", stmt: " + stmt;
			} else {
				return null;
			}
		case PARAM_CALLER:
			ParamCaller paramCaller = (ParamCaller) stmt;
			SSAInstruction pc_instruction = paramCaller.getInstruction();

			IMethod method3 = paramCaller.getNode().getMethod();

			method_name = method3.getName().toString();
			if (methods_list.contains(method_name)) {
				fLine = method3.getSourcePosition(paramCaller.getValueNumber()).getFirstLine();
				LLine = method3.getSourcePosition(paramCaller.getValueNumber()).getLastLine();
				target = paramCaller.getInstruction().getCallSite().getDeclaredTarget().getName().toString();
				irLineNo = IRIdexToLineNumber_stmtFormat(paramCaller.getNode().getIR(), pc_instruction.iIndex());
				return "in method:" + method_name + " -> target: " + target + " @@@line(" + fLine + " , " + LLine
						+ "), irLine @ " + irLineNo + ", type: " + type + ", instruction: " + pc_instruction;

			} else {
				return null;
			}
		case NORMAL_RET_CALLER:
			NormalReturnCaller nrc = (NormalReturnCaller) stmt;
			SSAInstruction nrc_instruction = nrc.getInstruction();

			IMethod method4 = nrc.getNode().getMethod();

			method_name = method4.getName().toString();
			if (methods_list.contains(method_name)) {
				fLine = method4.getSourcePosition(nrc.getValueNumber()).getFirstLine();
				LLine = method4.getSourcePosition(nrc.getValueNumber()).getLastLine();
				target = nrc.getInstruction().getCallSite().getDeclaredTarget().getName().toString();
				irLineNo = IRIdexToLineNumber_stmtFormat(nrc.getNode().getIR(), nrc_instruction.iIndex());
				return "in method:" + method_name + " -> target: " + target + " @@@line(" + fLine + " , " + LLine
						+ "), irLine @ " + irLineNo + ", type: " + type + ", instruction: " + nrc_instruction;

			} else {
				return null;
			}
		default:
			return null;// stmt.toString();
		}

	}

	// getSourcePosition,
	public static int getStatementSourcePosition(Statement statement) throws InvalidClassFileException {
		NormalStatement n = (NormalStatement) statement;
		IMethod method = n.getNode().getMethod();
		// getSourcePosition : This interface encapsulates the source position of an ast
		// node in its source file
		return method.getSourcePosition(n.getInstructionIndex()).getFirstLine();
	}

	public static SliceLine format_slice_stmt(Statement s, List<String> methods_list, ArrayList<CodeLine> lines) {
		SliceLine sl = new SliceLine();
		if (s != null) {
			sl.walaStmt = s;
			// ignore special kinds of statements. Focus only on normal and parameter
			// callers.
			if (s.getKind() == Statement.Kind.NORMAL) {
				NormalStatement n = (NormalStatement) s;
				IMethod method = s.getNode().getMethod();
				if (methods_list.contains(method.getName().toString())) {
					sl.method = method.getName().toString();

					int bcIndex, instructionIndex = n.getInstructionIndex();
					try {
						bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
						try {
							int srclno = method.getLineNumber(bcIndex);
							if (lines.get(srclno - 1).getLno() == srclno) {
								sl.lno = srclno;
								sl.stmt = lines.get(srclno - 1).getStmt();
//								souce_lines.add(sl);
								// System.out.println("@@"+sl);
							}
//							else {
//								System.out.println("$$" + sl);
//							}

						} catch (Exception e) {
							System.out.println("Bytecode index no good");
							System.out.println(e.getMessage());
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
					sl.method = method.getName().toString();

					int bcIndex, instructionIndex = pc.getInstructionIndex();
					try {
						bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
						try {
							int srclno = method.getLineNumber(bcIndex);
							if (lines.get(srclno - 1).getLno() == srclno) {
								sl.lno = srclno;
								sl.stmt = lines.get(srclno - 1).getStmt();
//								souce_lines.add(sl);
//								System.out.println("@@"+sl);
							}
//							else {
//								System.out.println("$$" + sl);
//							}
						} catch (Exception e) {
							System.out.println("Bytecode index no good");
							System.out.println(e.getMessage());
						}
					} catch (Exception e) {
						System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
						System.err.println(e.getMessage());
					}
				}

			} else if (s.getKind() == Statement.Kind.NORMAL_RET_CALLER) {
				NormalReturnCaller n = (NormalReturnCaller) s;
				IMethod method = s.getNode().getMethod();
				if (methods_list.contains(method.getName().toString())) {
					sl.method = method.getName().toString();

					int bcIndex, instructionIndex = n.getInstructionIndex();
					try {
						bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
						try {
							int srclno = method.getLineNumber(bcIndex);
							if (lines.get(srclno - 1).getLno() == srclno) {
								sl.lno = srclno;
								sl.stmt = lines.get(srclno - 1).getStmt();
//								souce_lines.add(sl);
								// System.out.println("@@"+sl);
							}
//							else {
//								System.out.println("$$" + sl);
//							}

						} catch (Exception e) {
							System.out.println("Bytecode index no good");
							System.out.println(e.getMessage());
						}

					} catch (Exception e) {
						System.out.println("it's probably not a BT method (e.g. it's a fakeroot method)");
						System.out.println(e.getMessage());
					}
				}
			}
		}
		return sl;

	}

//	public static SliceLine format_strBuilder_stmt(Statement s) {
//		//SliceLine sl = new SliceLine();
//		
//			// ignore special kinds of statements. Focus only on normal and parameter
//			// callers.
//			if (s.getKind() == Statement.Kind.NORMAL) {
//				NormalStatement n = (NormalStatement) s;
//				IMethod method = s.getNode().getMethod();
//				if (methods_list.contains(method.getName().toString())) {
//					sl.method =method.getName().toString();
//					
//					int bcIndex, instructionIndex = n.getInstructionIndex();
//					try {
//						method.getDescriptor().get
//						bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
//						 ((ShrikeBTMethod) method).
//						try {
//							int srclno = method.getLineNumber(bcIndex);
//							method.getLocalVariableName(bcIndex, arg1)
////							if(lines.get(srclno-1).getLno() == srclno) {
////								sl.lno = srclno;
////								sl.stmt = lines.get(srclno-1).getStmt();
//////								souce_lines.add(sl);
////								//System.out.println("@@"+sl);
////							}
////							else {
////								System.out.println("$$" + sl);
////							}
//
//						} catch (Exception e) {
//							System.out.println("Bytecode index no good");
//							System.out.println(e.getMessage());
//						}
//
//					} catch (Exception e) {
//						System.out.println("it's probably not a BT method (e.g. it's a fakeroot method)");
//						System.out.println(e.getMessage());
//					}
//				}
//			} else if (s.getKind() == Statement.Kind.PARAM_CALLER) {
//
//				ParamCaller pc = (ParamCaller) s;
//				IMethod method = s.getNode().getMethod();
//				if (methods_list.contains(method.getName().toString())) {
//					sl.method =method.getName().toString();
//					
//					int bcIndex, instructionIndex = pc.getInstructionIndex();
//					try {
//						bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
//						try {
//							int srclno = method.getLineNumber(bcIndex);
//							if(lines.get(srclno-1).getLno() == srclno) {
//								sl.lno = srclno;
//								sl.stmt = lines.get(srclno-1).getStmt();
////								souce_lines.add(sl);
////								System.out.println("@@"+sl);
//							}
////							else {
////								System.out.println("$$" + sl);
////							}
//						} catch (Exception e) {
//							System.out.println("Bytecode index no good");
//							System.out.println(e.getMessage());
//						}
//					} catch (Exception e) {
//						System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
//						System.err.println(e.getMessage());
//					}
//				}
//
//			} else if (s.getKind() == Statement.Kind.NORMAL_RET_CALLER) {
//				NormalReturnCaller n = (NormalReturnCaller) s;
//				IMethod method = s.getNode().getMethod();
//				if (methods_list.contains(method.getName().toString())) {
//					sl.method =method.getName().toString();
//					
//					int bcIndex, instructionIndex = n.getInstructionIndex();
//					try {
//						bcIndex = ((ShrikeBTMethod) method).getBytecodeIndex(instructionIndex);
//						try {
//							int srclno = method.getLineNumber(bcIndex);
//							if(lines.get(srclno-1).getLno() == srclno) {
//								sl.lno = srclno;
//								sl.stmt = lines.get(srclno-1).getStmt();
////								souce_lines.add(sl);
//								//System.out.println("@@"+sl);
//							}
////							else {
////								System.out.println("$$" + sl);
////							}
//
//						} catch (Exception e) {
//							System.out.println("Bytecode index no good");
//							System.out.println(e.getMessage());
//						}
//
//					} catch (Exception e) {
//						System.out.println("it's probably not a BT method (e.g. it's a fakeroot method)");
//						System.out.println(e.getMessage());
//					}
//				}
//			}
//		
//		return sl;
//
//	}
//

}