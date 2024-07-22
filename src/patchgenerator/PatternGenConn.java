/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patchgenerator;

//import static astgumtree.ASTGumtree.*;
//import static astgumtree.ASTGumtree.operationPartsPrint;
//import static astgumtree.ASTGumtree.operationTree;
import static astgumtree.LCSFinder.comput_lcsLength;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dcafixer.Fixer;
import flocalization.G;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import patchgenerator.PatternGen.Patch;
import slicer.SlicerUtil;
import slicer.tool.Info;
import slicer.utilities.StrUtil;

/**
 *
 * @author Dareen
 */
public class PatternGenConn {
	static boolean print = false;
	static class Patch implements Comparable<Patch> {
		public Integer getSrcln() {
			return srcln;
		}
		public void setSrcln(Integer srcln) {
			this.srcln = srcln;
		}
		public void setLn(Integer ln) {
			this.ln = ln;
		}
		private Integer ln=0;
		private String add= "";
		private String codeLine = "";
		private Integer srcln = -1;
		public String getAdd() {
			return add;
		}
		public void setAdd(String add) {
			this.add = add;
		}
		public String getCodeLine() {
			return codeLine;
		}
		public void setCodeLine(String codeLine) {
			this.codeLine = codeLine;
		}
		public Integer getLn() {
			return ln;
		}
		public void setLn(int ln) {
			this.ln = ln;
		}

		public void setSrcln(int ln) {
			this.srcln = ln;
		}
		@Override
		public int compareTo(Patch o) {
			// TODO Auto-generated method stub
			return this.getLn().compareTo(o.getLn());
		}
//================
//			@Override
//			public int compareTo(Patch o) {
//				int result = this.getLn().compareTo(o.getLn());
//				if (result == 0) {
//					if (this.add.equals("-") && o.add.equals("+")) {
//						return -1;
//					} else if (this.add.equals("+") && o.add.equals("-")) {
//						return 1;
//					}
//				}
//			return result;
//		}
//================
//		@Override
//		public int compareTo(Patch o) {
//			int result = this.getLn().compareTo(o.getLn());
//			Integer x = null, y = null;
//			if (this.add.equals("-")) {
//				x = new Integer(10);
//			}
//			if (this.add.equals("+")) {
//				x = new Integer(20);
//			}
//			if (o.add.equals("-")) {
//				y = new Integer(10);
//			}
//			if (o.add.equals("+")) {
//				y = new Integer(20);
//			}
//
//			if (result == 0) {
//				return x.compareTo(y);
//			}
//			return result;
//		}
	}
	static class Ops implements Comparable<Ops> {
		private Operation operation;
		private Integer position;

		public Operation getOperation() {
			return operation;
		}

		public Integer getPosition() {
			return position;
		}

		public void setOperation(Operation o) {
			operation = o;
		}

		public void setPosition(int p) {
			position = p;
		}

		@Override
		public int compareTo(Ops o) {
			return this.getPosition().compareTo(o.getPosition());
		}
	}

	static class Pattern implements Comparator<Pattern> {
		private Integer lcsSize = -1;
		private Integer noActions = 9999;
		private String slice = "";
		private List<Ops> actions = new ArrayList<>();
		public List<Ops> getActions() {
			return actions;
		}
		public void setActions(List<Ops> actions) {
			this.actions = actions;
		}
		public Integer getLcsSize() {
			return lcsSize;
		}
		public void setLcsSize(int lcsSize) {
			this.lcsSize = lcsSize;
		}
		public Integer getNoActions() {
			return noActions;
		}
		public void setNoActions(int noActions) {
			this.noActions = noActions;
		}
		public String getSSliceWithLCS() {
			return slice;
		}
		public void setSSliceWithLCS(String sSliceWithLCS) {
			slice = sSliceWithLCS;
		}

		@Override
		public int compare(Pattern customer1, Pattern customer2) {
			// TODO Auto-generated method stub
			// Comparing customers
            int lcsCompare = customer1.getLcsSize().compareTo(
                customer2.getLcsSize()); //

            int noActionsCompare = customer2.getNoActions().compareTo(
                customer1.getNoActions()); // reverse order (descending order)

            // 2nd level comparison
            return (lcsCompare == 0) ? noActionsCompare
                                      : lcsCompare;
		}


	}

//	static int lcsLength = -1;
//	static int noActions = 9999;
//	static String SSliceWithLCS = "";
	static List<Pattern> PatternsRanks = new ArrayList<>();
	public static String clean_code_line(String line, AppMD amd, int ln) {
//		String vl
		String newLine = line.replaceAll("###", "");
		if(RplacePHs.ps_cq_ph != null && amd.ps_query!= null) {
			if(line.contains(RplacePHs.ps_cq_ph) ) {
				newLine = newLine.replace(RplacePHs.ps_cq_ph, amd.ps_query);
			}
		}
		if(RplacePHs.wl_cq_ph!= null && amd.wl_query!= null) {
			if(line.contains(RplacePHs.wl_cq_ph)) {
				newLine = newLine.replace(RplacePHs.wl_cq_ph, amd.wl_query);//???
			}
		}
		//if(RplacePHs.sets_ph!= null && amd.setStrings != null)
		if(line.contains(RplacePHs.sets_ph)) {
			newLine = newLine.replace(RplacePHs.sets_ph, amd.setStrings );
		}
		if(!newLine.trim().endsWith(";")) {
			newLine = newLine +";";
		}
		return newLine;
	}
	public static List<Patch> createPatchFromSelectedPattern (String vSlice,
			Pattern selPattern, AppMD amd, boolean print) throws IOException {
		boolean firstAction = true;
		//1- Create new Secure Slice (Patch) for the Vul App.
			// read the Vul slice
			List<String>vsLines = StrUtil.read_lines_list(vSlice);
			List<Patch>patch = new ArrayList<>();
			// Apply actions
			String line = "",  newLine="", nextLine=null;
			int curr_pos =-1, next_pos=-1,pre_pos=-1;
			int vul_curr_line = -1;
//			for(Ops action : selPattern.actions) {
				for(int i=0; i<selPattern.actions.size(); i++) {
					Ops curr_action = selPattern.actions.get(i);
					Ops next_action = new Ops();
					Ops pre_action = new Ops();

					curr_pos = curr_action.getPosition();
					if(firstAction) {
						firstAction = false;
						vul_curr_line=curr_action.getPosition();
						line = vsLines.get(vul_curr_line-1);
						pre_action = null;
						pre_pos = -1;
					}
					else {
						pre_action = selPattern.actions.get(i-1);
						pre_pos = pre_action.position;
//						line = previous value;
						if(line == null && nextLine == null) {
							vul_curr_line++;
							line = vsLines.get(vul_curr_line-1);
						}else if(line == null && nextLine != null) {
							line = nextLine;
							nextLine = null;
						}
					}
					if(i<selPattern.actions.size()-1) {
						next_action = selPattern.actions.get(i+1);
						next_pos = next_action.position;

					}else {
						next_action = null;
						next_pos = -1;
					}
					if(print) {
						System.out.println("Calling the operationProcessor");
					}
					if(next_action != null) {
					newLine = PatternProcessor.operationProcessor(curr_action.operation, next_action.operation, line, pre_pos, curr_pos, next_pos,  amd.app_sql,  amd.app_stmt, print);
					}else {
						newLine = PatternProcessor.operationProcessor(curr_action.operation, null, line, pre_pos, curr_pos, next_pos,  amd.app_sql,  amd.app_stmt, print);

					}
//					operationProcessor(Operation op, String line, int curr, int next, String app_sql, String app_stmt) {
					//How we should change "vul_curr_line"?

					if(newLine == null || newLine =="") {
						break;
//						if(print) System.out.println("**** 1");
//						Patch l = new Patch();
//						l.codeLine = line;
//						l.add = "-";
//						l.ln = vul_curr_line;
//						patch.add(l);
					}

					//else
					if(newLine.equals(line)) {
						if(print) {
							System.out.println("**** 2");
						}
						Patch l = new Patch();
						l.add = "";
						l.ln = vul_curr_line;
						l.codeLine = clean_code_line(line, amd,l.ln);
						patch.add(l);
//					}else if(!newLine.equals(line) && newLine.contains("\n"+line)) {
					}else if(!newLine.equals(line) && newLine.contains(line)) {
						if(print) {
							System.out.println("**** 3");
						}
						String []parts = newLine.split("\n", 2);
						if (parts.length>1) {
							if(print) {
								System.out.println("part0 : "+ parts[0] );
							}
							if(print) {
								System.out.println("part1 : "+ parts[1] );
							}
							if(parts[0].startsWith("+")|| parts[0].startsWith("-")) {
								if(print) {
									System.out.println("**** 3.1");
								}

								Patch l = new Patch();
								String add ;
								if(parts[0].startsWith("+")) {
									add = "+";
								}else {
									add="-";
								}

								l.codeLine = clean_code_line(parts[0].substring(1).trim(), amd, l.ln);
								l.add = add;
								l.ln = curr_pos;
								patch.add(l);

								if(parts[1].startsWith("+")|| parts[1].startsWith("-")) {
									Patch l2 = new Patch();
									String add2 ;
									if(parts[1].startsWith("+")) {
										add2 = "+";
									}else {
										add2="-";
									}

									l2.codeLine = clean_code_line(parts[1].substring(1).trim(), amd, l2.ln);
									l2.add = add2;
									l2.ln = curr_pos;
									patch.add(l2);
									line = null;
									nextLine = null;
								}else {
								line = null;
								nextLine = parts[1];}
								//==================
//								Patch l = new Patch();
//								String add ="";
//								if (parts[0].startsWith("+")) {
//									add = "+";
//								}
//
//								if (parts[0].startsWith("-")) {
//									add = "-";
//								}
//								l.codeLine = parts[0].substring(1).trim();;
//								l.add = add;
//								l.ln = vul_curr_line;
//								patch.add(l);
//								line = parts[1];
//								nextLine = "";
								//==================
							}else {
								if(print) {
									System.out.println("**** 3.2");
								}
								if(next_pos == curr_pos) {
								line = parts[0];// for next "operationProcessor" call
								nextLine = parts[1];}
							}

						}else {
							if(print) {
								System.out.println("**** 3.3, "+ newLine);
							}
							if(newLine.startsWith("-")) {
							Patch l = new Patch();
							l.ln = curr_pos;
							l.codeLine = clean_code_line(newLine.substring(1).trim(), amd, l.ln);
							l.add = "-";

							patch.add(l);
							line = nextLine;//??
							}else if(newLine.startsWith("+")) {
								Patch l = new Patch();
								l.ln = curr_pos;
								l.codeLine = clean_code_line(newLine.substring(1).trim(), amd, l.ln);
								l.add = "+";

								patch.add(l);
								line = nextLine;//??
							}
							else{
								//middle step
								line = newLine;

							}
						}
						//line for next action and line?
					}else {
						if(print) {
							System.out.println("**** 4");
						}
						Patch l = new Patch();
						String tmp = newLine;
//						char add =' ';
						if(newLine.startsWith("+")) {
							if(print) {
								System.out.println("**** 4.1 +");
							}
							tmp = tmp.substring(1).trim();
							l.ln = curr_pos;//vul_curr_line;
							l.codeLine = clean_code_line(tmp, amd, l.ln);
							l.add = "+";

							patch.add(l);
							line = null;
						}else if(newLine.startsWith("-")) {
							if(print) {
								System.out.println("**** 4.2 -");
							}
							tmp = tmp.substring(1).trim();
							l.codeLine = clean_code_line(tmp, amd,l.ln);//tmp.replaceAll("###", "");;
							l.add = "-";
							l.ln = vul_curr_line;
							patch.add(l);
							line = null;
						}else {
							line = newLine;
//							if(nextLine == null || nextLine== "")
//							nextLine = "";
						}


					}


//				curr_pos = curr_action.getPosition();
//				if(pre_pos != curr_pos) {
//						line = vsLines.get(action.getPosition());
//				} else {
//					line = currline;
//				}
//
//				pre_pos = action.getPosition();

				//List<Operation> actions
			}
				//Print patch
				if (print) {
					System.out.println("Before Sort:");
					for (Patch p : patch) {
						System.out.println(p.ln + " " + p.add + p.codeLine);
					}
					// map patch
					System.out.println("after Sort:");
					Collections.sort(patch);
					for (Patch p : patch) {
						System.out.println(p.ln + " " + p.add + p.codeLine);
					}
				}



				//2- Apply Patch to the vulApp

		//3- Compile and return the result

		return patch;


	}


	public static List<Patch> createPatchFromSelectedPattern_exp4(String vSlice,
			Pattern selPattern, AppMD amd, ArrayList<Integer> querylines,String context, String query, boolean print) throws IOException {
		System.out.println(selPattern.actions.toString());
		boolean firstAction = true;
		//1- Create new Secure Slice (Patch) for the Vul App.
			// read the Vul slice
			List<String>vsLines = StrUtil.read_lines_list(vSlice);
			List<Patch>patch = new ArrayList<>();
			// Apply actions
			String line = "",  newLine="", nextLine=null;
			int curr_pos =-1, next_pos=-1,pre_pos=-1;
			int vul_curr_line = -1;
//			for(Ops action : selPattern.actions) {
				for(int i=0; i<selPattern.actions.size(); i++) {
					Ops curr_action = selPattern.actions.get(i);
//					curr_action.operation.
//					PatternProcessor.PrintOpPartsAndGetPos(curr_action.operation, true);
//					System.out.println("\nAction  "+i+" : "+curr_action.toString());
					Ops next_action = new Ops();
					Ops pre_action = new Ops();

					curr_pos = curr_action.getPosition();
					if(firstAction) {
						firstAction = false;
						vul_curr_line=curr_action.getPosition();
						line = vsLines.get(vul_curr_line-1);
						pre_action = null;
						pre_pos = -1;
					}
					else {
						pre_action = selPattern.actions.get(i-1);
						pre_pos = pre_action.position;
//						line = previous value;
						if(line == null && nextLine == null) {
							vul_curr_line++;
							line = vsLines.get(vul_curr_line-1);
						}else if(line == null && nextLine != null) {
							line = nextLine;
							nextLine = null;
						}
					}
					if(i<selPattern.actions.size()-1) {
						next_action = selPattern.actions.get(i+1);
						next_pos = next_action.position;

					}else {
						next_action = null;
						next_pos = -1;
					}
					if(print) {
						System.out.println("Calling the operationProcessor");
					}
					if(next_action != null) {
					newLine = PatternProcessor.operationProcessor_exp4(curr_action.operation, next_action.operation,
							line, pre_pos, curr_pos, next_pos,  amd.app_sql,  amd.app_stmt, querylines, print);
					}else {
						newLine = PatternProcessor.operationProcessor_exp4(curr_action.operation, null, line, pre_pos, curr_pos, next_pos,  amd.app_sql,  amd.app_stmt,querylines, print);

					}
//					operationProcessor(Operation op, String line, int curr, int next, String app_sql, String app_stmt) {
					//How we should change "vul_curr_line"?

					if(newLine == null || newLine =="") {
						break;
//						if(print) System.out.println("**** 1");
//						Patch l = new Patch();
//						l.codeLine = line;
//						l.add = "-";
//						l.ln = vul_curr_line;
//						patch.add(l);
					}

					//else
					if(newLine.equals(line)) {
						if(print) {
							System.out.println("**** 2");
						}
						Patch l = new Patch();
						l.add = "";
						l.ln = vul_curr_line;
						l.codeLine = clean_code_line(line, amd,l.ln);
						patch.add(l);
//					}else if(!newLine.equals(line) && newLine.contains("\n"+line)) {
					}else if(!newLine.equals(line) && newLine.contains(line)) {
						if(print) {
							System.out.println("**** 3");
						}
						String []parts = newLine.split("\n", 2);
						if (parts.length>1) {
							if(print) {
								System.out.println("part0 : "+ parts[0] );
							}
							if(print) {
								System.out.println("part1 : "+ parts[1] );
							}
							if(parts[0].startsWith("+")|| parts[0].startsWith("-")) {
								if(print) {
									System.out.println("**** 3.1");
								}

								Patch l = new Patch();
								String add ;
								if(parts[0].startsWith("+")) {
									add = "+";
								}else {
									add="-";
								}

								l.codeLine = clean_code_line(parts[0].substring(1).trim(), amd, l.ln);
								l.add = add;
								l.ln = curr_pos;
								patch.add(l);

								if(parts[1].startsWith("+")|| parts[1].startsWith("-")) {
									Patch l2 = new Patch();
									String add2 ;
									if(parts[1].startsWith("+")) {
										add2 = "+";
									}else {
										add2="-";
									}

									l2.codeLine = clean_code_line(parts[1].substring(1).trim(), amd, l2.ln);
									l2.add = add2;
									l2.ln = curr_pos;
									patch.add(l2);
									line = null;
									nextLine = null;
								}else {
								line = null;
								nextLine = parts[1];}
								//==================
//								Patch l = new Patch();
//								String add ="";
//								if (parts[0].startsWith("+")) {
//									add = "+";
//								}
//
//								if (parts[0].startsWith("-")) {
//									add = "-";
//								}
//								l.codeLine = parts[0].substring(1).trim();;
//								l.add = add;
//								l.ln = vul_curr_line;
//								patch.add(l);
//								line = parts[1];
//								nextLine = "";
								//==================
							}else {
								if(print) {
									System.out.println("**** 3.2");
								}
								if(next_pos == curr_pos) {
								line = parts[0];// for next "operationProcessor" call
								nextLine = parts[1];}
							}

						}else {
							if(print) {
								System.out.println("**** 3.3, "+ newLine);
							}
							if(newLine.startsWith("-")) {
							Patch l = new Patch();
							l.ln = curr_pos;
							l.codeLine = clean_code_line(newLine.substring(1).trim(), amd, l.ln);
							l.add = "-";

							patch.add(l);
							line = nextLine;//??
							}else if(newLine.startsWith("+")) {
								Patch l = new Patch();
								l.ln = curr_pos;
								l.codeLine = clean_code_line(newLine.substring(1).trim(), amd, l.ln);
								l.add = "+";

								patch.add(l);
								line = nextLine;//??
							}
							else{
								//middle step
								line = newLine;

							}
						}
						//line for next action and line?
					}else {
						if(print) {
							System.out.println("**** 4");
						}
						Patch l = new Patch();
						String tmp = newLine;
//						char add =' ';
						if(newLine.startsWith("+")) {
							if(print) {
								System.out.println("**** 4.1 +");
							}
							tmp = tmp.substring(1).trim();
							l.ln = curr_pos;//vul_curr_line;
							l.codeLine = clean_code_line(tmp, amd, l.ln);
							l.add = "+";

							patch.add(l);
							line = null;
						}else if(newLine.startsWith("-")) {
							if(print) {
								System.out.println("**** 4.2 -");
							}
							tmp = tmp.substring(1).trim();
							l.codeLine = clean_code_line(tmp, amd,l.ln);//tmp.replaceAll("###", "");;
							l.add = "-";
							l.ln = vul_curr_line;
							patch.add(l);
							line = null;
						}else {
							line = newLine;
//							if(nextLine == null || nextLine== "")
//							nextLine = "";
						}


					}


//				curr_pos = curr_action.getPosition();
//				if(pre_pos != curr_pos) {
//						line = vsLines.get(action.getPosition());
//				} else {
//					line = currline;
//				}
//
//				pre_pos = action.getPosition();

				//List<Operation> actions
			}
			// Print patch
			if (print) {
				System.out.println("\nBefore Sort:");
				for (Patch p : patch) {
					System.out.println(p.ln + " " + p.add + p.codeLine);
				}
				System.out.println("\n======================");
			}
			// map patch

			Collections.sort(patch);
			if (print) {
				System.out.println("\nAfter Sort:");
				for (Patch p : patch) {
					System.out.println(p.ln + " " + p.add + p.codeLine);
				}
				System.out.println("\n======================");
			}
//			querylines
			///&&&&&&&&&&&&&&&&&&& TEST the lines below
			List<Patch> newPatch = mapSliceToSrc2_exp4 (context, patch, querylines);
			List<Patch>patch2 = new ArrayList<>();
			System.out.println("querylines: "+querylines);
			if(querylines.size()>1) {
//			for (Patch p : patch) {
				 int q = 0; boolean QlineFound = false;
				 int vln= 0;
			for (int i =0 ;i< newPatch.size(); i++) {
				Patch p = newPatch.get(i);

				if(q<querylines.size() ) {//
					if(print) {
						System.out.println("^^^ "+p.ln + ": "+p.srcln+" " + p.add + p.codeLine +" Query@"+querylines.get(q) +"querylines.size() "+querylines.size());
					}if(print) {
						if(print) {
							System.out.println("^^^ HERE1");
						}
					}
					if( q<querylines.size() &&p.srcln.equals(querylines.get(q)) && p.add.equals("+") ) {
					if(print) {
						System.out.println("^^^ HERE2");
					}
					QlineFound = true;
					vln = p.ln;
					q++;
					p.setLn(vln);
					String[] parts = p.getCodeLine().split("\\(", 2);
					if(parts.length ==2 ) {
						if(parts[0].contains("executeUpdate") ||parts[0].contains("executeQuery")||parts[0].contains(".execute(")) {

							p.setCodeLine(p.getCodeLine().replace(parts[1], query + ");"));
						}
					}

					patch2.add(p);
				}else {
					patch2.add(p);
				}}
				if (QlineFound) {
					while (q < querylines.size() && i< newPatch.size() ) {
						if (p.srcln.equals(querylines.get(q)) ) {
//							vln = vln;// -1;
							p.setLn(vln);
							if(print) {
								System.out.println("^^^vln 1 "+p.ln + " " + p.add + p.codeLine);
							}
							patch2.add(p);
							i++;

						} else { // add the extra lines that the slicer didn't get
//							vln = vln -1;
							Patch newP = new Patch();
							newP.setAdd("-");
							newP.setCodeLine("drop");// = " ";
							newP.setSrcln(p.srcln);//querylines.get(q));

							newP.setLn(vln);// = vln;
							if(print) {
								System.out.println("^^^vln 2 "+newP.ln + " " +
								newP.add + newP.codeLine);
							}

							patch2.add(newP);
						}
						q++;

						if(i< newPatch.size()) {
							p = newPatch.get(i);
						}
					}
					//else {
						QlineFound = false;
					//}
				}



			}
			int shift = querylines.size();
			boolean newLinesAdded = false;
//			for (Patch p : patch2) {
			for (Patch element : patch2) {

				if(element.ln < 0) {
					int tmp = element.ln;
					element.setLn(tmp * -1);
					//p.ln =  p.ln * -1;
					newLinesAdded = true;
				}
				if(newLinesAdded) {
					if(element.ln > 0) {
						int tmp = element.ln;
						element.setLn(tmp + shift);
					}
				}
				//System.out.println(p.ln + " " + p.add + p.codeLine);
			}
			}else {
				patch2  = patch;
			}

			if (print) {
				System.out.println("\nFinal patch:");
				for (Patch p : patch2) {
					System.out.println(p.ln + ": "+ p.srcln+" " + p.add + p.codeLine);
				}
				System.out.println("\n======================");
			}


		//2- Apply Patch to the vulApp

		//3- Compile and return the result

//		return patch;
			return patch2;


	}


	//	public static List<Patch> createPatchFromSelectedPattern_exp4(String vSlice, Pattern selPattern, AppMD amd,
//			ArrayList<Integer> querylines, boolean print) throws IOException {

	public static void find_diff(String vslPath, String sslPath) throws Exception {
//        ,  "vul: " +getClass(vslicePath) + "sec: "+getClass(sslicePath2));
//		System.out.println("************************* vslice: " + getClass(vslPath) + ", sslice: " + getClass(sslPath)
//				+ " *************************");
		File f1 = new File(vslPath);
		File f2 = new File(sslPath);


		// Compute LCS
		int lcs_size = comput_lcsLength(vslPath, sslPath);

//		System.out.println("LCS = " + lcs_size);
		// Find Diff
		AstComparator diff = new AstComparator();
		Diff result = diff.compare(f1, f2); // diffs to transform f1 to be like f2
		List<Operation> actions = result.getRootOperations();
		List<Ops> actionsWithPos = new ArrayList<>();
//        List<Operation> all_actions = result.getRootOperations();
//         List<Operation> actions = all_actions.stream()
//                                      .distinct()
//                                      .collect(Collectors.toList());
//		System.out.println("## Actions (" + actions.size() + ")");
//         actions.sort(null); // Didn't work

		// System.out.println(actions);
		// Print Operations
		for (Operation o : actions) {
//            System.out.println("--------------- o.toString():");//+o);
//            System.out.println(o.toString());
//            //System.out.println(o); // same as o.toString
//            //System.out.println("\t---- dst: "+o.getDstNode().toString());
//            //System.out.println("\t---- src: "+o.getSrcNode().toString());
//            System.out.println("--------------- PrintOpPartsAndGetPos:");
			Ops p = new Ops();
			p.operation = o;
			p.position = PatternProcessor.PrintOpPartsAndGetPos(o, false);
			actionsWithPos.add(p);
//            System.out.println("--------------- operationTree:");
//            operationTree(o);
//            System.out.println("@@@@@@@@@@@@@@@@@@@@@@");
		}
		actionsWithPos.sort(null);
//		System.out.println("## actionsWithPos size: " + actionsWithPos.size());
//		for (Ops op : actionsWithPos) {
//			System.out.println("--------------------");
//			System.out.println(op.position + ": " + op.operation);
//
//		}
		// System.out.println(actionsWithPos);

		Pattern pa = new Pattern();
//		pa.lcsSize = lcs_size;
		pa.setLcsSize(lcs_size);
		pa.setSSliceWithLCS(sslPath);
		pa.setNoActions(actions.size());
		pa.setActions(actionsWithPos);
//		pa.slice = sslPath;
//		pa.noActions = actions.size();
		PatternsRanks.add(pa);
//		if (lcs_size > lcsLength && actions.size() < noActions) {
//			lcsLength = lcs_size;
//			SSliceWithLCS = sslPath;
//			noActions = actions.size();
//		}

	}

	public static String getClass(String p) {//input: "/xxx/yyy/zzz.java", output: "zzz.java"
		String[] parts = p.split("/");
		String msg = parts[parts.length - 1].replace(".java", "");
		return msg;
	}
	public static String getPath(String p) {//input: "/xxx/yyy/zzz.java", output: "/xxx/yyy/"
//      String msg = v.split(".")[0];
		String[] parts = p.split("/");
		String path = p.replace(parts[parts.length - 1],"");
        return path;
	}
	public static List<Patch>  mapSliceToSrc (String context, List<Patch> patch) throws IOException{
		//Map patch to source code
		List<Patch> newPatch = patch;
		//System.out.print(p);
		List<String> contextLines = StrUtil.read_lines_list(context);
		int i= 0;//, j=0;
		for (String contextLine : contextLines) {

				String []cParts = contextLine.split(",");
				int slln = -1, srcln=-1;
				if(cParts.length == 3) {
					slln = Integer.parseInt(cParts[0].trim());
					srcln = Integer.parseInt(cParts[2].trim());
				}

				if(newPatch.get(i).ln == slln && newPatch.get(i).add.equals("-")) {
					newPatch.get(i).srcln = srcln;
					if(newPatch.get(i+1).ln == newPatch.get(i).ln && newPatch.get(i+1).add.equals("+")) {
						newPatch.get(i+1).srcln = srcln;
						i++;
					}
					i++;
				}
		}

//		for (Patch np: newPatch){
		for(int k = 1 ; k< newPatch.size();k++) {
			if(newPatch.get(k).srcln == -1) {
				newPatch.get(k).srcln = newPatch.get(k-1).srcln;
			}

		}
		int ln = 0;
		for(int k = 0 ; k< newPatch.size();k++) {
			Patch p = newPatch.get(k);

			if(p.add.equals("+") && p.codeLine.contains("PreparedStatement")) {
				ln = p.srcln;
				Patch p2 = p;//
				p2.codeLine = p.codeLine.replace(RplacePHs.ps_ph, RplacePHs.ps_ph+ln);
				newPatch.set(k, p2);
			}else if(p.add.equals("+") && p.codeLine.contains(RplacePHs.ps_ph)){
				Patch p2 = p;//
				p2.codeLine = p.codeLine.replace(RplacePHs.ps_ph, RplacePHs.ps_ph+ln);
				newPatch.set(k, p2);
			}
		}

		return newPatch;

}


	public static List<Patch>  mapSliceToSrc2 (String context, List<Patch> patch) throws IOException{
		//Map patch to source code
		List<Patch> newPatch = patch;
		//System.out.print(p);
		List<String> contextLines = StrUtil.read_lines_list(context);
		//int i= 0;//, j=0;
		for (String contextLine : contextLines) {

				String []cParts = contextLine.split(",");
				int slln = -1, srcln=-1;
				if(cParts.length == 3) {
					slln = Integer.parseInt(cParts[0].trim());
					srcln = Integer.parseInt(cParts[2].trim());
				}
				for(int i=0; i<newPatch.size()-1;i++) {
					if(newPatch.get(i).ln == slln ) {//&& newPatch.get(i).add.equals("-")
						newPatch.get(i).srcln = srcln;
					}
				}
//				if(newPatch.get(i).ln == slln && newPatch.get(i).add.equals("-")) {
//					newPatch.get(i).srcln = srcln;
//					if(newPatch.get(i+1).ln == newPatch.get(i).ln && newPatch.get(i+1).add.equals("+")) {
//						newPatch.get(i+1).srcln = srcln;
//						i++;
//					}
//					i++;
//				}
		}

//		for (Patch np: newPatch){
		for(int k = 1 ; k< newPatch.size();k++) {
			if(newPatch.get(k).srcln == -1) {
				newPatch.get(k).srcln = newPatch.get(k-1).srcln;
			}

		}
		int ln = 0;
		for(int k = 0 ; k< newPatch.size();k++) {
			Patch p = newPatch.get(k);

			if(p.add.equals("+") && p.codeLine.contains("PreparedStatement")) {
				ln = p.srcln;
				Patch p2 = p;//
				p2.codeLine = p.codeLine.replace(RplacePHs.ps_ph, RplacePHs.ps_ph+ln);
				newPatch.set(k, p2);
			}else if(p.add.equals("+") && p.codeLine.contains(RplacePHs.ps_ph)){
				Patch p2 = p;//
				p2.codeLine = p.codeLine.replace(RplacePHs.ps_ph, RplacePHs.ps_ph+ln);
				newPatch.set(k, p2);
			}
		}
		return newPatch;

}


	public static List<Patch>  mapSliceToSrc2_exp4 (String context, List<Patch> patch, ArrayList<Integer> queryLines) throws IOException{
		//Map patch to source code
		List<Patch> newPatch = patch;
		//System.out.print(p);
		List<String> contextLines = StrUtil.read_lines_list(context);
		//int i= 0;//, j=0;
		for (String contextLine : contextLines) {

				String []cParts = contextLine.split(",");
				int slln = -1, srcln=-1;
				if(cParts.length == 3) {
					slln = Integer.parseInt(cParts[0].trim());
					srcln = Integer.parseInt(cParts[2].trim());
				}
				for(int i=0; i<newPatch.size()-1;i++) {
					if(newPatch.get(i).ln == slln ) {//&& newPatch.get(i).add.equals("-")
						newPatch.get(i).srcln = srcln;
					}
				}
		}

		for(int k = 1 ; k< newPatch.size();k++) {
			if(newPatch.get(k).srcln == -1) {
				newPatch.get(k).srcln = newPatch.get(k-1).srcln;
			}

		}
		int ln = 0;
		for(int k = 0 ; k< newPatch.size();k++) {
			Patch p = newPatch.get(k);

			if(p.add.equals("+") && p.codeLine.contains("PreparedStatement")) {
				ln = p.srcln;
				Patch p2 = p;//
				p2.codeLine = p.codeLine.replace(RplacePHs.ps_ph, RplacePHs.ps_ph+ln);
				newPatch.set(k, p2);
			}else if(p.add.equals("+") && p.codeLine.contains(RplacePHs.ps_ph)){
				Patch p2 = p;//
				p2.codeLine = p.codeLine.replace(RplacePHs.ps_ph, RplacePHs.ps_ph+ln);
				newPatch.set(k, p2);
			}
//			else if(p.add.equals("-") && queryLines.contains(p.ln)) {
////				xx
//			}
		}
		return newPatch;
}


	public static void findAllDiffs(String vsPath, String slicesPath) throws Exception {
		PatternsRanks.clear();
		// Read files from slicesPath
		File directoryPath = new File(slicesPath);
		String[] contents = directoryPath.list();
		for (String content : contents) {
			if (content.endsWith(".java")) {
				find_diff(vsPath, slicesPath + "/" + content);
			}
		}
		Collections.sort(PatternsRanks,new Pattern());
//		for(Pattern pt : PatternsRanks) {
//			System.out.println(pt.lcsSize +":"+pt.noActions+":"+pt.slice);
//		}
		if(print) {
		System.out.println("****************** vslice: " + getClass(vsPath) + ", sslice: " + getClass(PatternsRanks.get(PatternsRanks.size()-1).slice)
		+ " ******************");
		//===== Print ranked patterns

		for(int i = PatternsRanks.size()-1; i>-1; --i) {
			System.out.println(PatternsRanks.get(i).lcsSize+":"+PatternsRanks.get(i).noActions+":"+PatternsRanks.get(i).slice);
		}

		if(print) {
			System.out.println("Best Pattern is: " + getClass( PatternsRanks.get(PatternsRanks.size()-1).slice) + "\nWith LCS length: " + PatternsRanks.get(PatternsRanks.size()-1).lcsSize);
		}
		//System.out.println(PatternsRanks.get(PatternsRanks.size()-1).actions.toString());
//		for (Ops op : PatternsRanks.get(PatternsRanks.size()-1).actions) {
//			System.out.println("--------------------");
//			System.out.println(op.position + ": " + op.operation);
//		}
		System.out.println("********************************************************* ");
		}
	}


	public static boolean applyPatch(String vulApp ,String context, List<Patch> patch, AppMD amd) throws IOException {
		//Doesn't handle when query has multiple lines
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines

		System.out.println("==== @applyPatch ====");
		List<String> vulLines = StrUtil.read_lines_list(vulApp);
		List<Patch> newPatch = mapSliceToSrc2 (context, patch);
		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();

		if(print) {
			for(Patch p: newPatch) {
				System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
			}
		}
//		Apply the patch
		int pi =0;
		for(int l =1; l<= vulLines.size();l++) {
			if(print) {
				System.out.println("#### 1 " );
			}
//			if(pi<newPatch.size()) {
			if(pi < newPatch.size() && l == newPatch.get(pi).srcln ) {
				if(print) {
					System.out.println("#### 2");
				}
//				int xxx= 0;
				while (l == newPatch.get(pi).srcln && pi < newPatch.size()) {
					if(newPatch.get(pi).add.equals("-")) {
						if(print) {
							System.out.println("#### 2.1");
						}
						pi++;
						continue;
					}
					if(newPatch.get(pi).add.equals("+")) {

						if(print) {
							System.out.println("#### 2.2"+ newPatch.get(pi).codeLine);
						}
						fixedAppLines.add(newPatch.get(pi).codeLine);// Add patch lines.
						pi++;
						if(pi==newPatch.size()) {
							if(print) {
								System.out.println("#### 2.2.1");
							}
							break;
						}
					}
				}

//			}
			}else {
				if(print) {
					System.out.println("#### 3");
				}
				String line = vulLines.get(l-1);
				if(line.contains("class") && line.contains(vulCName)) {
					//line= line.replace(vulCName, vulCName+"_fixed");// ****** for other cases
					line= line.replace(vulCName, vulCName.replace("before", "after"));
				}
				//++++ for exp3
				if(line.contains("package datasets.Apps_Before;")) {
					//line= line.replace(vulCName, vulCName+"_fixed");// ****** for other cases
					line= line.replace("package datasets.Apps_Before;", "package datasets.AppsAfter;");
				}
//				if(print)System.out.println("#### 3.1 "+ line);
				fixedAppLines.add(line);
			}
		}

	//Print fixed App
		///Users/dareen/DCAFixer
		String fixedAppPath ="/Users/dareen/DCAFixer/src/datasets/AppsAfter/"+vulCName.replace("before", "after")+".java";
//		String fixedAppPath = getPath (vulApp)+  vulCName+"_fixed.java";// ****** for other cases
//		for(String l :fixedAppLines ) {
//			System.out.println(l);
//		}
		StrUtil.write_tofile(fixedAppPath, fixedAppLines);
//		write_tofile(fixedAppPath, fixedAppLines);

		return false;

	}

	public static boolean applyPatch_exp3(String vulApp ,String context, List<Patch> patch, AppMD amd) throws IOException {
		//applyPatch2 handles sql on mlti lines!
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines
		if(print) {
			System.out.println("==== @applyPatch2 ====");
		}
		List<String> vulLines = StrUtil.read_lines_list(vulApp);
		List<Patch> newPatch = mapSliceToSrc2 (context, patch);
		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();

		if(print) {
			for(Patch p: newPatch) {
				System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
			}
		}
//		Apply the patch
		int pi =0;boolean foundMliLineQuery= false;
		for(int l =1; l<= vulLines.size();l++) {
			if(print) {
				System.out.println("#### 1 " );
			}
//			if(pi<newPatch.size()) {
			String tmp = vulLines.get(l-1);
			if (foundMliLineQuery && !tmp.endsWith(";")) {
				if(print) {
					System.out.println("#### 1.1");
				}
				continue;
			}
			if (foundMliLineQuery && tmp.endsWith(";")) {
				if(print) {
					System.out.println("#### 1.2");
				}
				foundMliLineQuery = false;
				continue;
			}
			if(pi < newPatch.size() && l == newPatch.get(pi).srcln ) {
				if(print) {
					System.out.println("#### 2"+tmp +"@"+(l-1));
				}
//				int xxx= 0;
//				if(pi < newPatch.size()) {
				while (pi < newPatch.size() &&l == newPatch.get(pi).srcln ) {
					if(newPatch.get(pi).add.equals("-")) {
						if(print) {
							System.out.println("#### 2.1 ");
						}
						if((pi+1)<newPatch.size()) {
							if(newPatch.get(pi+1).srcln == l) {
								pi++;
								if(pi==newPatch.size()) {
									break;
								}
								continue;
							}
						}}
						if(newPatch.get(pi).add.equals("+")) {

							if(print) {
								System.out.println("#### 2.2"+ newPatch.get(pi).codeLine);
							}
							fixedAppLines.add(newPatch.get(pi).codeLine);// Add patch lines.
//							pi++;
//							if(pi==newPatch.size()) {
//								//if(print)System.out.println("#### 2.2.1");
//								break;
//							}
							if((pi+1)<newPatch.size()) {
								if(newPatch.get(pi+1).srcln == l) {
									pi++;
									if(pi==newPatch.size()) {
										break;
									}
									continue;
								}
							}
						}

						//newPatch.get(pi).codeLine;
//						if(print)System.out.println("#### 2.1 :" +tmp +"@"+(l-1));
						if(tmp.contains(amd.app_sql) && !tmp.endsWith(";") && (tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))) {
							if(print) {
								System.out.println("#### 2.1.1");
							}
							foundMliLineQuery = true;
							break;
						}

						if((tmp.contains("executeQuery") || tmp.contains("executeUpdate")|| tmp.contains(".execute("))
										&& !tmp.endsWith(";") &&
										(tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))
								){
							if(print) {
								System.out.println("#### 2.1.2");
							}
							foundMliLineQuery = true;
							break;
						}
						pi++;
						continue;


			}
			}else {
				if(print) {
					System.out.println("#### 3");
				}
				String line = vulLines.get(l-1);
				if(line.contains("class") && line.contains(vulCName)) {
					//line= line.replace(vulCName, vulCName+"_fixed");// ****** for other cases
					//for exp3
					line= line.replace(vulCName, vulCName.replace("before", "after"));
				}
				//++++ for exp3
				if(line.contains("package datasets.Apps_Before;")) {
					//line= line.replace(vulCName, vulCName+"_fixed");// ****** for other cases
					line= line.replace("package datasets.Apps_Before;", "package datasets.AppsAfter;");
				}
//				if(print)System.out.println("#### 3.1 "+ line);
				fixedAppLines.add(line);
			}

		}

	//Print fixed App
		// For exp3
		String fixedAppPath ="/Users/dareen/DCAFixer/src/datasets/AppsAfter/"+vulCName.replace("before", "after")+".java";

//		for(String l :fixedAppLines ) {
//			System.out.println(l);
//		}
		StrUtil.write_tofile(fixedAppPath, fixedAppLines);

		return false;

	}

	public static boolean applyPatch_exp4(String vulApp ,String context, List<Patch> patch,
			AppMD amd, ArrayList<Integer> queryLines, String query) throws IOException {
		//applyPatch2 handles sql on mlti lines!
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines
		if(print) {
			System.out.println("==== @applyPatch2 ====");
		}
		List<String> vulLines = StrUtil.read_lines_list(vulApp);
//		List<String> vulLines = new ArrayList<>();;

		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
//		int shift = queryLines.size();


		if(print) {
			for(Patch p: patch) {
				System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
//			if(p.ln < -1) {
////				vulLines.ad
//			}
			}
		}
//		Apply the patch
		int pi =0, shift = 0;
		//boolean foundMliLineQuery= false;
		fixedAppLines.add(vulLines.get(0));
		for(int l =1; l<= vulLines.size();l++) {
			if(print) {
				System.out.println("#### 1 " );
			}
//			if(pi<newPatch.size()) {
//			String tmp = vulLines.get(l-1);
//			if (foundMliLineQuery && !tmp.endsWith(";")) {
//				if(print)System.out.println("#### 1.1");
//				continue;
//			}
//			if (foundMliLineQuery && tmp.endsWith(";")) {
//				if(print)System.out.println("#### 1.2");
//				foundMliLineQuery = false;
//				continue;
//			}
			if(pi < patch.size() && l == patch.get(pi).getSrcln() ) {
				if (print) {
					System.out.println("#### 2 @" + l + " and " + patch.get(pi).getSrcln());
				}
//				int xxx= 0;
//				if(pi < newPatch.size()) {
				while (pi < patch.size() && l == patch.get(pi).getSrcln()) {
					if (patch.get(pi).add.equals("-")) {
						if (print) {
							System.out.println("#### 2.1 " + patch.get(pi).add);
						}
						if ((pi + 1) < patch.size()) {
							if (patch.get(pi + 1).srcln == l) {
								pi++;
								shift++;
								if (print) {
									System.out.println("#### 2.1.a " + patch.get(pi).add + pi);
								}
								if (pi == patch.size()) {
									break;
								}
								continue;
							}
						}
					}
					if (patch.get(pi).add.equals("+")) {

						if (print) {
							System.out.println("#### 2.2" + patch.get(pi).codeLine);
						}
						fixedAppLines.add(patch.get(pi).codeLine);// Add patch lines.
//							pi++;
//							if(pi==newPatch.size()) {
//								//if(print)System.out.println("#### 2.2.1");
//								break;
//							}
						if ((pi + 1) < patch.size()) {
							if (patch.get(pi + 1).srcln == l) {
								pi++;
								if (pi == patch.size()) {
									break;
								}
								continue;
							}
						}
					}

					// newPatch.get(pi).codeLine;
//						if(print)System.out.println("#### 2.1 :" +tmp +"@"+(l-1));
//						if(tmp.contains(amd.app_sql) && !tmp.endsWith(";") && (tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
//								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))) {
//							if(print)System.out.println("#### 2.1.1");
//							foundMliLineQuery = true;
//							break;
//						}

//						if((tmp.contains("executeQuery") || tmp.contains("executeUpdate")|| tmp.contains(".execute("))
//										&& !tmp.endsWith(";") &&
//										(tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
//								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))
//								){
//							if(print)System.out.println("#### 2.1.2");
//							foundMliLineQuery = true;
//							break;
//						}
					pi++;
					continue;
				}
			} else {
				if (print) {
					System.out.println("#### 3");
				}
				if((l - 1+shift) >0 && (l - 1+shift )< vulLines.size() ) {
				String line = vulLines.get(l - 1+shift);


				if (line.contains("class") && line.contains(vulCName)) {
					// line= line.replace(vulCName, vulCName+"_fixed");// ****** for other cases
					// for exp4
					line = line.replace(vulCName, vulCName+"_fixed");
				}
				// ++++ for exp3
				if (line.contains("package datasets.Apps_Before;")) {
					// line= line.replace(vulCName, vulCName+"_fixed");// ****** for other cases
					line = line.replace("package datasets.Apps_Before;", "package datasets.AppsAfter;");
				}
//				if(print)System.out.println("#### 3.1 "+ line);
				fixedAppLines.add(line);}
			}

		}

		// Print fixed App
		// For exp3
		String fixedAppPath = "/Users/dareen/DCAFixer/src/datasets/AppsAfter/"
				+ vulCName.replace("before", "after") + ".java";
		if (print) {
			System.out.println("fixedAppPath: " + fixedAppPath);
//			for (String l : fixedAppLines) {
//				System.out.println(l);
//			}
		}
		StrUtil.write_tofile(fixedAppPath, fixedAppLines);

		return false;

	}


	public static boolean applyPatch_all(String vulApp ,String context, List<Patch> patch, AppMD amd) throws IOException {
		//applyPatch2 handles sql on mlti lines!
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines
		if(print) {
			System.out.println("==== @applyPatch2 ====");
		}
		List<String> vulLines = StrUtil.read_lines_list(vulApp);
		List<Patch> newPatch = mapSliceToSrc2 (context, patch);
		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();

		if(print) {
			for(Patch p: newPatch) {
				System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
			}
		}
//		Apply the patch
		int pi =0;boolean foundMliLineQuery= false;
		for(int l =1; l<= vulLines.size();l++) {
			if(print) {
				System.out.println("#### 1 " );
			}
//			if(pi<newPatch.size()) {
			String tmp = vulLines.get(l-1);
			if (foundMliLineQuery && !tmp.endsWith(";")) {
				if(print) {
					System.out.println("#### 1.1");
				}
				continue;
			}
			if (foundMliLineQuery && tmp.endsWith(";")) {
				if(print) {
					System.out.println("#### 1.2");
				}
				foundMliLineQuery = false;
				continue;
			}
			if(pi < newPatch.size() && l == newPatch.get(pi).srcln ) {
				if(print)
				 {
					System.out.println("#### 2"+tmp +"@"+(l-1));
//				int xxx= 0;
				}

				while (l == newPatch.get(pi).srcln && pi < newPatch.size()) {
					if(newPatch.get(pi).add.equals("-")) {
						if(print) {
							System.out.println("#### 2.1 ");
						}
						if((pi+1)<newPatch.size()) {
							if(newPatch.get(pi+1).srcln == l) {
								pi++;
								continue;
							}
						}}
						if(newPatch.get(pi).add.equals("+")) {

							if(print) {
								System.out.println("#### 2.2"+ newPatch.get(pi).codeLine);
							}
							fixedAppLines.add(newPatch.get(pi).codeLine);// Add patch lines.
							if((pi+1)<newPatch.size()) {
								if(newPatch.get(pi+1).srcln == l) {
									pi++;
									continue;
								}
							}
						}

						//newPatch.get(pi).codeLine;
//						if(print)System.out.println("#### 2.1 :" +tmp +"@"+(l-1));
						if(tmp.contains(amd.app_sql) && !tmp.endsWith(";") && (tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))) {
							if(print) {
								System.out.println("#### 2.1.1");
							}
							foundMliLineQuery = true;
							break;
						}

						if((tmp.contains("executeQuery") || tmp.contains("executeUpdate")|| tmp.contains(".execute("))
										&& !tmp.endsWith(";") &&
										(tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))
								){
							if(print) {
								System.out.println("#### 2.1.2");
							}
							foundMliLineQuery = true;
							break;
						}
						pi++;
						continue;
				}

//			}
			}else {
				if(print) {
					System.out.println("#### 3");
				}
				String line = vulLines.get(l-1);
				if(line.contains("class") && line.contains(vulCName)) {
					line= line.replace(vulCName, vulCName+"_fixed");// ****** for other cases
				}

//				if(print)System.out.println("#### 3.1 "+ line);
				fixedAppLines.add(line);
			}
		}

	//Print fixed App
		// For exp3
//		String fixedAppPath ="/Users/dareen/DCAFixer/src/datasets/AppsAfter/"+vulCName.replace("before", "after")+".java";
		String fixedAppPath = getPath (vulApp)+  vulCName+"_fixed.java";// ****** for other cases
//		for(String l :fixedAppLines ) {
//			System.out.println(l);
//		}
		StrUtil.write_tofile(fixedAppPath, fixedAppLines);
//		write_tofile(fixedAppPath, fixedAppLines);

		return false;

	}


	public static boolean findPatternsAndApply_exp3( String vulApp , String vsPath, String sslicesPath, String context, AppMD amd, boolean print) throws Exception  {
		//String app_sql, String app_stmt,
        findAllDiffs(vsPath,sslicesPath);
        //TODO: loop over the patterns
        List<Patch> patch =createPatchFromSelectedPattern ( vsPath, PatternsRanks.get(PatternsRanks.size()-1), amd, print);
        if(print) {
			for(Patch p: patch) {
				System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
			}
		}
        //Apply Patch to the vulApp & Compile. Then, return the result
        applyPatch_exp3(vulApp, context, patch, amd);
		//TODO: test
		return false;
	}

	public static boolean findPatternsAndApply_exp4( String vulApp , String vsPath,
			String sslicesPath, String context, AppMD amd, ArrayList<Integer> queryLines, String query, boolean print) throws Exception  {
		//String app_sql, String app_stmt,
        findAllDiffs(vsPath,sslicesPath);
        //TODO: loop over the patterns
        List<Patch> patch = createPatchFromSelectedPattern_exp4 ( vsPath, PatternsRanks.get(PatternsRanks.size()-1), amd,queryLines, context,query, print);
        if(print) {
        	System.out.println("createPatchFromSelectedPattern_exp4 - patch:");
        for(Patch p: patch) {

			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
		}}
        //Apply Patch to the vulApp & Compile. Then, return the result
        applyPatch_exp4(vulApp, context, patch, amd, queryLines, query);
		//TODO: test
		return false;
	}
	//========================================================

	public static void printPatternsRanks () {
		for (Pattern p : PatternsRanks) {
			System.out.println(p.getSSliceWithLCS());
		}
//		//Another way to print same results
//		for (int i = 0; i < PatternsRanks.size(); i++) {
//			System.out.println(i + ") " + PatternsRanks.get(i).getSSliceWithLCS());
//		}
	}

	public static boolean findPatternsAndApply_usingExistingPatchesConn(String vulApp, String vsPath, String sslicesPath,
			Info info, AppMD amd, boolean connHasNoEnc, boolean cedLfixed,boolean print) throws Exception {

		if (print) {
			System.out.println("@@@ findPatternsAndApply_usingExistingPatchesConn");
		}

//		if(amd.soltion == G.NOSOL || amd.soltion == G.SEC) {
//			return false;
//		}
		// String app_sql, String app_stmt,
		boolean patch_found = false;
		findAllDiffs(vsPath, sslicesPath);
		int pi = PatternsRanks.size() - 1;// PatternsRanks.size();
//		if (print)	System.out.println("==== 1st Pattern: " + PatternsRanks.get(pi).getSSliceWithLCS());
//		if (print)	printPatternsRanks ();
		// TODO: loop over the patterns
		while (pi > -1 && !patch_found) {
			List<Patch> patch = createPatchFromSelectedPattern_usingExistingPatches(PatternsRanks.get(pi), info, amd,
					print);
//		if (print) {

			if(patch.size()==0) {
				if(print) {
					System.out.println("======== patch size : "+patch.size());
				}
				pi--;
				continue;
			}else {
				patch = sortPatch2(patch);
				if (print) {
					System.out.println("********* patch size : " + patch.size());
//				patch.sort();
//				Collections.sort(patch);

					for (Patch p : patch) {
						System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
					}
					System.out.println("***************************");
				}
			}
//		}

			// Apply Patch to the vulApp & Compile. Then, return the result
			// TODO: "" is context, build it from ql & cl from info
//			int applyResult = applyPatch_usingExistingPatches(vulApp, "", patch, "");

			int applyResult =applyPatch_usingExistingPatchesConn(vulApp, "", patch, "");
			if(print) System.out.println("applyResult: "+applyResult);
			if ( applyResult == G.noSynErr || applyResult == G.vul_and_fixedAppshaveSynErr) {
				patch_found = true;
				if (pi > -1) {
					int trial = Math.abs(pi - 12 + 1);
					System.out.println("Found patch at " + trial + " try!");
					System.out.println("**** Generated Patch ***");
					for (Patch p : patch) {
//						if(p.ln > 0)
							System.out.println(p.add + p.srcln +" "+p.codeLine);
					}
					System.out.println("=======================");
				}
				break;
			}else if ( applyResult == G.fixedApphasSynErr ) {
				//try with next patch
				pi--;
				continue;
			}else if(applyResult == G.vulApphasSynErr) {
				return false;

			}
		}

		if (pi == -1) {
			System.out.println("No patch was found!");
			System.out.println("=======================");
		} 
//		else {
//			int trial = Math.abs(pi - 12 + 1) ;
//			System.out.println("Found patch at " +  trial + " try!");
//			System.out.println("=======================");
//		}

		// TODO: Collect stats here. For example, create a int[size of patterns] and count
		// each try!
		// TODO: test

        return patch_found;
	}

	public static boolean hasLine(List<Patch> patch, Patch p) {
		for (Patch pa : patch) {
//			if (p.equals(pa)) {
				if(p.add.equals(pa.add) && p.codeLine.equals(pa.codeLine) &&
						p.ln.equals(pa.ln) && p.srcln.equals(pa.srcln) ) {
				//System.out.println("RRRRRR I found a line repeated in the patch!!!");
				return true;
			}
		}
		return false;
	}


	public static  List<Patch> createPatchFromSelectedPattern_usingExistingPatches( Pattern selPattern, Info info, AppMD amd, boolean print) throws IOException{

		List<Patch>patch = new ArrayList<>();
		boolean credLinesFixed = false;
		// TODO: Print steps

		String existingPatchPath = G.CONN_patchesPath + "/"+ StrUtil.get_classname(selPattern.getSSliceWithLCS()) +".txt";
		if (print) {
			System.out.println("---------- existingPatchPath: \n\t"+ existingPatchPath);
		}
		String patchLines = StrUtil.read_lines(existingPatchPath);
		if (print) {
			System.out.println("---------- patchLines: \n"+patchLines);
		}
		patchLines = RplacePHsConn.replacePlaceHolders_patch(existingPatchPath, info, null, null);
//		if(amd.soltion == G.SOLPS) {
//			patchLines = RplacePHs.replacePlaceHolders_patch(existingPatchPath, info,amd.setStrings , amd.ps_query);
//			}
//		else if(amd.soltion == G.SOLWL)
//			patchLines = RplacePHs.replacePlaceHolders_patch(existingPatchPath, info,null , amd.wl_query);
		if (print) {
			System.out.println("--vv-------- patchLines after replace: \n"+patchLines);
		}
		String[] lines = patchLines.split("\n");

		// **************** Overlaps between queryLines & callLines

//		FixerNExSet.credLines;
		//TODO: fix url

		ArrayList<Integer> credLines = SlicerUtil.get_credintal_lines(info);//info.getQuery_lines();
		if(credLines.equals(Fixer.credLines) && Fixer.credLFixed) {
			credLinesFixed = true;
		}
		ArrayList<Integer> callLines = info.getCall_lines();

//		for(String l:lines) {
//			if(l.contains("+(ql)") && credLinesFixed) {
//				continue;
//			}
//			String[] parts = l.split(",", 2);
//			if (parts.length == 2 && parts[0].contains("ql")) {
//
//			}
//		}
//		if (credLines.size() > 0 && callLines.size() > 0) {
//			//check if credLines are previously fixed
//			//if yes, ignore them and work on cll lines only.
//			//else TODO
//		}else if(credLines.size() == 0 && callLines.size() > 0) {
//			//remove call lines and add patch lines
//		}
//			boolean overlab = false;
//			int last = callLines.size() - 1;
//			if ((callLines.get(0) < credLines.get(0) || callLines.get(0) == credLines.get(0))
//					&& (credLines.get(0) < callLines.get(last) || credLines.get(0) == callLines.get(last))) {
//				overlab = true;
//			} else if (credLines.get(0) > callLines.get(last)
//					|| credLines.get(credLines.size() - 1) < callLines.get(0)) {
//				overlab = false;
//			}
//
//			int c = 0, q = 0;
//			if (overlab) {
//
//				// TODO: **************** Create patch line
//				c = callLines.get(0);
//				// - first call line
////				if (print) System.out.println("sub...0");
//				Patch tmpp = createPatchLine("-", callLines.get(0), 0,"");
//				if(!hasLine(patch, tmpp)) {
//					if (print) System.out.println("sub ... O1, "+tmpp.getCodeLine());
//				patch.add(tmpp);}
////				patch.add(createPatchLine("-", callLines.get(0), 0,""));
//				int patchln = 0;
//				for (String l : lines) {
//					String[] parts = l.split(",", 2);
//
//					if (parts.length == 2) {
//						if (print) System.out.println("add...1");
//						patch.add(createPatchLine("+", callLines.get(0), ++patchln, parts[1].trim()));}
//					else {
//						if (print) System.out.println("add...2");
////						patch.add(createPatchLine("+", callLines.get(0), ++patchln, parts[1].trim()));
//						patch.add(createPatchLine("+", callLines.get(0), ++patchln,l));
//						}
//				}
//				// - rest call lines
//				for (int j = 1; j < callLines.size(); j++) {
//					if (print) System.out.println("sub...n");
//					Patch tmpp0 = createPatchLine("-", callLines.get(j), 0, "");
//					if(!hasLine(patch, tmpp0)) {
//						if (print) System.out.println("sub ... O2, "+tmpp0.getCodeLine());
//					patch.add(tmpp0);}
//				}
			//} else if (!overlab) {
				// handle ql, cl, and new lines
				//if (print) System.out.println("!overlab");
				int ql = -1, cl = -1;
				int patchln=0;
				for (String l : lines) {
					if(l.contains("+(ql)") && credLinesFixed) {
						continue;
					}
					String[] parts = l.split(",",2);
//					if (print) System.out.println("l: "+l);
					if (print) {
						System.out.println("parts size = "+parts.length+", parts[0]: "+parts[0]);
					}
					if (parts.length == 2 && parts[0].contains("ql") &&  !credLines.isEmpty()) {
						if (print) {
							System.out.println("l: "+l);
						}
						if (print) {
							System.out.println("l has ql");
						}
						// - 1st line in ql
						ql = credLines.get(0);
						cl = -1;
						for (Integer qline : credLines) {
							Patch tmpp = createPatchLine("-", qline, 0, "");
//							if(!patch.contains(tmpp)) {
								if(!hasLine(patch, tmpp)) {
								if (print) {
									System.out.println("sub ... 1, "+tmpp.getCodeLine());
								}
								patch.add(tmpp);
								}
						}
						if (print) {
							System.out.println("add ... 3");
						}
						patch.add(createPatchLine("+", ql, ++patchln, parts[1].trim()));
						continue;
					} if (parts.length == 2 && parts[0].contains("cl")) {
						if (print) {
							System.out.println("l: "+l);
						}
						if (print) {
							System.out.println("l has cl");
						}
						ql = -1;
						cl = callLines.get(0);

						for (Integer cline : callLines) {
							Patch tmpp = createPatchLine("-", cline, 0, "");
//							if (!patch.contains(tmpp)) {
								if(!hasLine(patch, tmpp)) {
								if (print) {
									System.out.println("sub ... 2, "+tmpp.getCodeLine());
								}
								patch.add(tmpp);
							}
						}
						if (print) {
							System.out.println("add ... 4");
						}
						patch.add(createPatchLine("+", cl, ++patchln, parts[1].trim()));
						continue;
					} else {
						if (print) {
							System.out.println("l: "+l);
						}
						if (print) {
							System.out.println("l has no ql and cl");
						}
						if (ql > 0 && cl == -1) {
							if (print) {
								System.out.println("add ... 5");
							}
							patch.add(createPatchLine("+", ql, ++patchln, l));
						} else if (ql == -1 && cl > 0) {
							if (print) {
								System.out.println("add ... 6");
							}
							patch.add(createPatchLine("+", cl, ++patchln, l));
						}
						continue;
					}
				}
//
//			}
//		}

		return patch;
	}

	public static Patch createPatchLine(String action, int srcln, int ln, String codeLine) {
		Patch p = new Patch();
		p.setCodeLine(codeLine);//p.codeLine = codeLine;
		p.setSrcln(srcln);//p.srcln = srcln;
		p.setLn(ln);//p.ln = ln;
		p.setAdd(action);//p.add = action;
//		System.out.println(action+ " " + ln + " " + codeLine);
		return p;
	}


public static List<Patch> sortPatch2 (List<Patch> op){
	if(print) {
		System.out.println("@@@@@@patch sortPatch2");
	}
		List<Patch> np = new ArrayList<>();
		List<Patch> sub = new ArrayList<>();
		List<Patch> plus = new ArrayList<>();

		for(Patch pl : op) {
			if(pl.add.equals("+")) {
				plus.add(pl);
			} else if(pl.add.equals("-")) {
				sub.add(pl);
			}
		}
		if (print) {
			System.out.println("---------------------------");
			for (Patch p : sub) {
				System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
			}
			System.out.println("++++++++++++++++++++++++++++");
			for (Patch p : plus) {
				System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
			}
		}

		int n1 = sub.size(), n2= plus.size();
		int s = 0, pl = 0, k = 0;
		while (s < n1 && pl < n2) {
			// Check if current element of first
			// array is smaller than current element
			// of second array. If yes, store first
			// array element and increment first array
			// index. Otherwise do same with second array
//            if (sub.get(i).srcln.equals(plus.get(j).srcln) )//<
			if (sub.get(s).srcln.intValue() < plus.get(pl).srcln.intValue()) {// <
				np.add(sub.get(s));
				k++;
				s++;
			} else if (sub.get(s).srcln.intValue() > plus.get(pl).srcln.intValue()) {// <
				np.add(plus.get(pl));
				k++;
				pl++;
			} else if (sub.get(s).srcln.intValue() == plus.get(pl).srcln.intValue()) {// <
				np.add(sub.get(s));
				k++;
				s++;
				np.add(plus.get(pl));
				k++;
				pl++;
			}

		}
		// Store remaining elements of first array
		while (s < n1) {
			np.add(sub.get(s));
			k++;
			s++;
        }

		// Store remaining elements of second array
		while (pl < n2) {
			np.add(plus.get(pl));
			k++;
			pl++;
		}
		if(k == op.size()) {
			if(print) {
				System.out.println("All lines were mereged!");
			}
		}

		if(np.size()==0) {
			return op;
		}
		if (print) {
			System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
			for (Patch p : np) {
				System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
			}
			System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNN End");
		}
		return np;
	}

	public static List<Patch> sortPatch (List<Patch> op){
		List<Patch> np = new ArrayList<>();
		List<Patch> sub = new ArrayList<>();
		List<Patch> plus = new ArrayList<>();

		for(Patch pl : op) {
			if(pl.add.equals("+")) {
				plus.add(pl);
			} else if(pl.add.equals("-")) {
				sub.add(pl);
			}
		}
		if (print) {
			System.out.println("---------------------------");
			for (Patch p : sub) {
				System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
			}
			System.out.println("++++++++++++++++++++++++++++");
			for (Patch p : plus) {
				System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
			}
		}
		int j=0;

		for(int i =0; i<sub.size();i++) {
			if(sub.get(i).srcln < plus.get(j).srcln) {
				np.add(sub.get(i));

//				continue;
			}//else
			if(sub.get(i).srcln > plus.get(j).srcln) {
				np.add(plus.get(j)); j++;
//				continue;
			}//else
				if(sub.get(i).srcln == plus.get(j).srcln) {
				np.add(sub.get(i));
				np.add(plus.get(j)); j++;
//				continue;
			}
		}
		if (print) {
		System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNN 1");
		for (Patch p : np) {
			System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
		}
		System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNN Add rest of +");
		}


		if(j<plus.size()) {
			//j++;
			for(int y = j; y<plus.size();y++) {
				np.add(plus.get(y));
			}
		}
		if(np.size()==0) {
			return op;
		}

		if (print) {
			System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
			for (Patch p : np) {
				System.out.println(p.add + " " + p.srcln + ":" + p.ln + " " + p.codeLine);
			}
			System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNN End");
		}

		return np;
	}

	public static int applyPatch_usingExistingPatchesConn(String vulApp ,String context, List<Patch> patch,  String query) throws IOException {//conn
		//TODO: $$$43 test this function correctness! and all todo here!
		//applyPatch2 handles sql on mlti lines!
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines

		if(print) {
			System.out.println("==== @applyPatch EPs ====");
		}
		List<String> vulAppLines = StrUtil.read_lines_list(vulApp);
//		List<String> vulLines = new ArrayList<>();;

		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
		List<Integer> fixedAppLinesNos = new ArrayList<>();
		List<String> AppLines_partlyFixed = new ArrayList<>();
		List<Integer> AppLines_partlyFixedNos = new ArrayList<>();


		if(G.AppLines_partlyFixed.size()>0)
		 {
			AppLines_partlyFixed.addAll(G.AppLines_partlyFixed);//DO I need it?
		}
		int llm = G.LastLineModified + 1;//Last Line Modified

		if(G.AppLines_partlyFixedNos.size()>0){//+++
			AppLines_partlyFixedNos.addAll(G.AppLines_partlyFixedNos);//DO I need it?
		}

		if(llm>1 && AppLines_partlyFixed.size()>1) {
			//System.out.println("==== @XXXX ====");
			fixedAppLines.clear();
			fixedAppLines.addAll(AppLines_partlyFixed);
			fixedAppLinesNos.clear();
			fixedAppLinesNos.addAll(AppLines_partlyFixedNos);
			//= AppLines_partlyFixed;

		}

//		if(print)
//		for(Patch p: patch) {
//			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
//		}
//		System.out.println("--------------");

//		=========== Apply the patch
		int pi = 0;
//		fixedAppLines.add(vulAppLines.get(0));//1
		int psImportExist = 0;
		int scannerExist = 0;
		for(int srcln = llm; srcln<= vulAppLines.size();srcln++) {
//			if(print)System.out.println("#### 1 , llm: "+llm );
			if(pi < patch.size() && srcln == patch.get(pi).getSrcln() ) {
				if (print) {
					System.out.println("#### 2 @" + srcln + " and " + patch.get(pi).getSrcln());
				}
				while (pi < patch.size() && srcln == patch.get(pi).getSrcln()) {
					if (patch.get(pi).add.equals("-")) {
						//skip no add to fixedAppLines
						if (print) {
							System.out.println("#### 2.1 " + patch.get(pi).add);
							System.out.println("888888888 srcln: " + srcln + " ------ " + vulAppLines.get(srcln));
						}
						//if (pi < (patch.size()-1))
						//	srcln++;  //DIDN't work. why?
						//continue;//endless loop!

					}
					if (patch.get(pi).add.equals("+")) {

						if (print) {
							System.out.println("#### 2.2" + patch.get(pi).codeLine);
						}
						fixedAppLines.add(patch.get(pi).codeLine);// Add patch lines.
						fixedAppLinesNos.add(0);
					}

					if ((pi + 1) < patch.size()) {
						if (patch.get(pi + 1).srcln == (srcln + 1)) {
							srcln++;
							pi++;
							continue;
						} else if (patch.get(pi + 1).srcln > (srcln + 1)) {// To handle gaps in the patch
							pi++; //No increase. I want to come back later to this line
							break;
						}
//						else {
//							pi++;
//							continue;
//						}
					}

//					if((pi+1)<patch.size()) {
//						if(patch.get(pi+1).srcln == (srcln+1))
//							srcln++;
//					}

					if(pi == patch.size()-1) {
						llm = patch.get(pi).getSrcln();
						if (print) {
							System.out.println("llm: "+llm);
						}
					}
					pi++;
					continue;
				}
				// TODO: safe as global || to a file,
				//Here, after applying the patch we safe a copy of the code and line number
				if(pi >= patch.size()-1 ) {// I applied the patch
					AppLines_partlyFixed.clear();
					AppLines_partlyFixed.addAll(fixedAppLines);
					AppLines_partlyFixedNos.clear();
					AppLines_partlyFixedNos.addAll(fixedAppLinesNos);

				}
				if (print)
				 {
					System.out.println("srcln: "+llm+ ", Compare to llm?");
//				lastLineModified = srcln;
				}

			} else {
				//if (print) System.out.println("#### 3");
//				if((l - 1+shift) >0 && (l - 1+shift )< vulLines.size() ) {
				String line ="";
				if(srcln <= vulAppLines.size()) {
					line =  vulAppLines.get(srcln-1);

					if((psImportExist != 0 && psImportExist != 2 ) && !line.startsWith("import") && !line.startsWith("package")){// add ps import before the class
						fixedAppLines.add("import java.sql.PreparedStatement;");
						fixedAppLinesNos.add(0);
						psImportExist = 2;
					}

					if(scannerExist  != 0 &&  scannerExist!= 2  && !line.startsWith("import") && !line.startsWith("package")) {
						fixedAppLines.add("import java.util.Scanner;");
						fixedAppLinesNos.add(0);
						scannerExist = 2;
					}
					if ((line.contains("class ") || line.contains("public "))//TODO: check if you have to handle private classes
							&& (line.contains(vulCName+" ") || line.contains(vulCName+"{") //To handle class name
					)) {
//					if(!psImportExist){// add ps import before the class
//						fixedAppLines.add("import java.sql.PreparedStatement;");
//						psImportExist = true;
//					}
						line = line.replace(vulCName, vulCName+"_fixed");
					}


					if ( line.contains("public ")//TODO: check if you have to handle private classes
							&& (line.contains(vulCName+" ") || line.contains(vulCName+"(") )) {//To handle constructors
						line = line.replace(vulCName, vulCName+"_fixed");
					}

//				if( line.contains(" "+vulCName+"(") ||line.contains("("+vulCName+"(") // Handles calls to constructor
//						|| line.contains(" "+vulCName+".") || line.contains("("+vulCName+".")) { // Handles calls to class
//					line = line.replace(vulCName, vulCName+"_fixed");
//				}

					if(line.contains(" "+vulCName+"(")) { // Handles calls to constructor
						line = line.replace(" "+vulCName+"(", " "+vulCName+"_fixed(");
					}

					if(line.contains("("+vulCName+"(")) { // Handles calls to constructor
						line = line.replace( "("+vulCName+"(" , "("+vulCName+"_fixed(" );
					}
					if(line.contains(" "+vulCName+".")) { // Handles calls to class
						line = line.replace(" "+vulCName+"." , " "+vulCName+"_fixed.");
					}

					if(line.contains("("+vulCName+".")) { // Handles calls to class
						line = line.replace("("+vulCName+"." , "("+vulCName+"_fixed.");
					}
					//To handle GUI-DBMS
					if( line.contains("<"+vulCName+">")) {  // Handles objects ArrayList<vulCName>

						line = line.replace("<"+vulCName+">", "<"+vulCName+"_fixed>");
					}//------------------
					if(line.trim().startsWith("import ")) {
						psImportExist = 1;
						scannerExist = 1;
						if(line.contains(".sql.*") || line.contains(".PreparedStatement")){
							psImportExist = 2;
						}
						if(line.contains(".Scanner")) {
							scannerExist = 2;
						}
					}
					fixedAppLines.add(line);
					fixedAppLinesNos.add(srcln);

				}
			}

		}

		// Print fixed App
		String fixedAppPath = vulApp.replace(vulCName+".java", vulCName+"_fixed.java");
//				"/Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/AppsAfter/"
//				+ vulCName.replace("before", "after") + ".java";
		if (print) {
			System.out.println("fixedAppPath: " + fixedAppPath);
//			for (String l : fixedAppLines) {
//				System.out.println(l);
//			}
		}
		StrUtil.write_tofile(fixedAppPath, fixedAppLines);

		//Test java file (fixedAppPath).
		//If no Syntax error,
		//		G.AppLines_partlyFixed.clear()
		//		& G.AppLines_partlyFixed = AppLines_partlyFixed
		//		& G.LastLineModified = llm &return true
		//else return false

		int test =TestFixedFiles.patchIntroducedSynError(vulApp,fixedAppPath, false );
		if(test == G.noSynErr  || test == G.vul_and_fixedAppshaveSynErr) {
//			if (print)
			System.out.println("No Syntax error!");
			G.AppLines_partlyFixed.clear();
			G.AppLines_partlyFixed.addAll(AppLines_partlyFixed);
			G.AppLines_partlyFixedNos.clear();
			G.AppLines_partlyFixedNos.addAll(AppLines_partlyFixedNos);
			G.LastLineModified = llm;
			
		} else if(test == G.fixedApphasSynErr) {
//			if (print)
			System.out.println("Patch introduced Syntax error(s)!");//There is/are syntax error(s)!

		} else if(test == G.vulApphasSynErr) {
//			if (print)
			System.out.println("Original code has error(s) or not compiled! llm agin is :"+ G.LastLineModified + 1);
		}
		return test;

	}


	public static int applyPatch_usingExistingPatchesConn_old(String vulApp ,String context, List<Patch> patch,  String query) throws IOException {
		//TODO: $$$43 test this function correctness! and all todo here!
		//applyPatch2 handles sql on mlti lines!
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines

		if(print) {
			System.out.println("==== @applyPatch EPs ====");
		}
		List<String> vulAppLines = StrUtil.read_lines_list(vulApp);
//		List<String> vulLines = new ArrayList<>();;

		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
		List<Integer> fixedAppLinesNos = new ArrayList<>();
		List<String> AppLines_partlyFixed = new ArrayList<>();
		List<Integer> AppLines_partlyFixedNos = new ArrayList<>();


		if(G.AppLines_partlyFixed.size()>0)
		 {
			AppLines_partlyFixed.addAll(G.AppLines_partlyFixed);//DO I need it?
		}
		int llm = G.LastLineModified + 1;//Last Line Modified

		if(G.AppLines_partlyFixedNos.size()>0){//+++
			AppLines_partlyFixedNos.addAll(G.AppLines_partlyFixedNos);//DO I need it?
		}

		if(llm>1 && AppLines_partlyFixed.size()>1) {
			//System.out.println("==== @XXXX ====");
			fixedAppLines.clear();
			fixedAppLines.addAll(AppLines_partlyFixed);
			fixedAppLinesNos.clear();
			fixedAppLinesNos.addAll(AppLines_partlyFixedNos);
			//= AppLines_partlyFixed;

		}

//		if(print)
//		for(Patch p: patch) {
//			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
//		}
//		System.out.println("--------------");

//		=========== Apply the patch
		int pi = 0;
//		fixedAppLines.add(vulAppLines.get(0));//1
		int psImportExist = 0;
		int scannerExist = 0;
		for(int srcln = llm; srcln<= vulAppLines.size();srcln++) {
//			if(print)System.out.println("#### 1 , llm: "+llm );
			if(pi < patch.size() && srcln == patch.get(pi).getSrcln() ) {
				if (print) {
					System.out.println("#### 2 @" + srcln + " and " + patch.get(pi).getSrcln());
				}
				while (pi < patch.size() && srcln == patch.get(pi).getSrcln()) {
					if (patch.get(pi).add.equals("-")) {
						//skip no add to fixedAppLines
						if (print) {
							System.out.println("#### 2.1 " + patch.get(pi).add);
							System.out.println("888888888 srcln: " + srcln + " ------ " + vulAppLines.get(srcln));
						}
						//if (pi < (patch.size()-1))
						//	srcln++;  //DIDN't work. why?
						//continue;//endless loop!

					}
					if (patch.get(pi).add.equals("+")) {

						if (print) {
							System.out.println("#### 2.2" + patch.get(pi).codeLine);
						}
						fixedAppLines.add(patch.get(pi).codeLine);// Add patch lines.
						fixedAppLinesNos.add(0);
					}

					if ((pi + 1) < patch.size()) {
						if (patch.get(pi + 1).srcln == (srcln + 1)) {
							srcln++;
							pi++;
							continue;
						} else if (patch.get(pi + 1).srcln > (srcln + 1)) {// To handle gaps in the patch
							pi++; //No increase. I want to come back later to this line
							break;
						}
//						else {
//							pi++;
//							continue;
//						}
					}

//					if((pi+1)<patch.size()) {
//						if(patch.get(pi+1).srcln == (srcln+1))
//							srcln++;
//					}

					if(pi == patch.size()-1) {
						llm = patch.get(pi).getSrcln();
						if (print) {
							System.out.println("llm: "+llm);
						}
					}
					pi++;
					continue;
				}
				// TODO: safe as global || to a file,
				//Here, after applying the patch we safe a copy of the code and line number
				if(pi >= patch.size()-1 ) {// I applied the patch
					AppLines_partlyFixed.clear();
					AppLines_partlyFixed.addAll(fixedAppLines);
				}
				if (print)
				 {
					System.out.println("srcln: "+llm+ ", Compare to llm?");
//				lastLineModified = srcln;
				}

			} else {
				//if (print) System.out.println("#### 3");
//				if((l - 1+shift) >0 && (l - 1+shift )< vulLines.size() ) {
				String line ="";
				if(srcln <= vulAppLines.size()) {
					line =  vulAppLines.get(srcln-1);

					if((psImportExist != 0 && psImportExist != 2 ) && !line.startsWith("import") && !line.startsWith("package")){// add ps import before the class
						fixedAppLines.add("import java.sql.PreparedStatement;");
						fixedAppLinesNos.add(0);
						psImportExist = 2;
					}

					if(scannerExist  != 0 &&  scannerExist!= 2  && !line.startsWith("import") && !line.startsWith("package")) {
						fixedAppLines.add("import java.util.Scanner;");
						fixedAppLinesNos.add(0);
						scannerExist = 2;
					}
					if ((line.contains("class ") || line.contains("public "))//TODO: check if you have to handle private classes
							&& (line.contains(vulCName+" ") || line.contains(vulCName+"{") //To handle class name
					)) {
//					if(!psImportExist){// add ps import before the class
//						fixedAppLines.add("import java.sql.PreparedStatement;");
//						psImportExist = true;
//					}
						line = line.replace(vulCName, vulCName+"_fixed");
					}


					if ( line.contains("public ")//TODO: check if you have to handle private classes
							&& (line.contains(vulCName+" ") || line.contains(vulCName+"(") )) {//To handle constructors
						line = line.replace(vulCName, vulCName+"_fixed");
					}

//				if( line.contains(" "+vulCName+"(") ||line.contains("("+vulCName+"(") // Handles calls to constructor
//						|| line.contains(" "+vulCName+".") || line.contains("("+vulCName+".")) { // Handles calls to class
//					line = line.replace(vulCName, vulCName+"_fixed");
//				}

					if(line.contains(" "+vulCName+"(")) { // Handles calls to constructor
						line = line.replace(" "+vulCName+"(", " "+vulCName+"_fixed(");
					}

					if(line.contains("("+vulCName+"(")) { // Handles calls to constructor
						line = line.replace( "("+vulCName+"(" , "("+vulCName+"_fixed(" );
					}
					if(line.contains(" "+vulCName+".")) { // Handles calls to class
						line = line.replace(" "+vulCName+"." , " "+vulCName+"_fixed.");
					}

					if(line.contains("("+vulCName+".")) { // Handles calls to class
						line = line.replace("("+vulCName+"." , "("+vulCName+"_fixed.");
					}
					//To handle GUI-DBMS
					if( line.contains("<"+vulCName+">")) {  // Handles objects ArrayList<vulCName>

						line = line.replace("<"+vulCName+">", "<"+vulCName+"_fixed>");
					}//------------------
					if(line.trim().startsWith("import ")) {
						psImportExist = 1;
						scannerExist = 1;
						if(line.contains(".sql.*") || line.contains(".PreparedStatement")){
							psImportExist = 2;
						}
						if(line.contains(".Scanner")) {
							scannerExist = 2;
						}
					}
					fixedAppLines.add(line);
					fixedAppLinesNos.add(srcln);

				}
			}

		}

		// Print fixed App
		String fixedAppPath = vulApp.replace(vulCName+".java", vulCName+"_fixed.java");
//				"/Users/Dareen/eclipse-workspace/DCAFixer/src/datasets/AppsAfter/"
//				+ vulCName.replace("before", "after") + ".java";
		if (print) {
			System.out.println("fixedAppPath: " + fixedAppPath);
//			for (String l : fixedAppLines) {
//				System.out.println(l);
//			}
		}
		StrUtil.write_tofile(fixedAppPath, fixedAppLines);

		//Test java file (fixedAppPath).
		//If no Syntax error,
		//		G.AppLines_partlyFixed.clear()
		//		& G.AppLines_partlyFixed = AppLines_partlyFixed
		//		& G.LastLineModified = llm &return true
		//else return false

		int test =TestFixedFiles.patchIntroducedSynError(vulApp,fixedAppPath, false );
		if(test == G.noSynErr  || test == G.vul_and_fixedAppshaveSynErr) {
//			if (print)
			System.out.println("No Syntax error!");
			G.AppLines_partlyFixed.clear();
			G.AppLines_partlyFixed = AppLines_partlyFixed;
			G.LastLineModified = llm;
		} else if(test == G.fixedApphasSynErr) {
//			if (print)
			System.out.println("Patch introduced Syntax error(s)!");//There is/are syntax error(s)!

		} else if(test == G.vulApphasSynErr) {
//			if (print)
			System.out.println("Original code has error(s) or not compiled! llm agin is :"+ G.LastLineModified + 1);
		}
		return test;

	}


	/**
	 * @return (G.vApphasSynErr) original code is not compiled. (G.noSynErr) no syntax error was introduced, fixedApp is compiled correctly. (G.fApphasSynErr) There is syntax error in the fixedApp.
	 */
	public static int applyPatch_usingExistingPatches(String vulApp ,String context, List<Patch> patch,  String query) throws IOException {

		//TODO: $$$43 test this function correctness! and all todo here!
		//applyPatch2 handles sql on mlti lines!
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines

		if(print) {
			System.out.println("==== @applyPatch EPsConn ====");
		}
		List<String> vulAppLines = StrUtil.read_lines_list(vulApp);
//		List<String> vulLines = new ArrayList<>();;

		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
		List<String> AppLines_partlyFixed = new ArrayList<>();
		//+++++++++++++
		List<String> temp = new ArrayList<>();
		List<Integer> fixedAppLinesNos = new ArrayList<>();
		List<Integer> AppLines_partlyFixedNos = new ArrayList<>();
		//+++++++++++++

		if(G.AppLines_partlyFixed.size()>0)
		 {
			AppLines_partlyFixed.addAll(G.AppLines_partlyFixed);//DO I need it?
		}

		if(G.AppLines_partlyFixedNos.size()>0)
		 { //+++
			AppLines_partlyFixedNos.addAll(G.AppLines_partlyFixedNos);//DO I need it?
		}

		System.out.println("+++++++++++++++++++++++++");
		for(int i= 0 ; i<G.AppLines_partlyFixedNos.size(); i++) {
			System.out.println(G.AppLines_partlyFixedNos.get(i) + "@ " + G.AppLines_partlyFixed.get(i));
		}
		System.out.println("+++++++++++++++++++++++++");

		int llm = G.LastLineModified + 1;//Last Line Modified

		if(llm>1 && AppLines_partlyFixed.size()>1) {


			//System.out.println("==== @XXXX ====");
			fixedAppLines.clear();
			fixedAppLines.addAll(AppLines_partlyFixed);

			fixedAppLinesNos.clear();
			fixedAppLinesNos.addAll(AppLines_partlyFixedNos);

		}

//		if(print)
//		for(Patch p: patch) {
//			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
//		}
//		System.out.println("--------------");

//		=========== Apply the patch
		//========== Handling case when we have to go back a few lines
		boolean patchHasPreLine = false;
		boolean reachLlmLine = false;
		int srcln ;//= llm;
		if(llm >patch.get(0).getSrcln()) {
			patchHasPreLine = true;
			temp.addAll(AppLines_partlyFixed);
			srcln = AppLines_partlyFixedNos.indexOf(patch.get(0).getSrcln());//patch.get(0).getSrcln();
		}else {
			temp.addAll(vulAppLines);
			srcln = llm;
		}
		int pi = 0;
//		fixedAppLines.add(vulAppLines.get(0));//1
		int psImportExist = 0;
		int scannerExist = 0;


//		for(int srcln = llm; srcln<= vulAppLines.size();srcln++) {
		while(srcln<= vulAppLines.size()) {
//			if(print)System.out.println("#### 1 , llm: "+llm );
			if(patchHasPreLine && AppLines_partlyFixedNos.get(srcln) == llm ) {/// llm or llm+1
				reachLlmLine = true;
				temp.clear();
				temp.addAll(vulAppLines);
			}
			if(pi < patch.size() && srcln == patch.get(pi).getSrcln() ) {
				if (print) {
					System.out.println("#### 2 @" + srcln + " and " + patch.get(pi).getSrcln());
				}
				while (pi < patch.size() && srcln == patch.get(pi).getSrcln()) {
					if (patch.get(pi).add.equals("-")) {
						//skip no add to fixedAppLines
						if (print) {
							System.out.println("#### 2.1 " + patch.get(pi).add);
							System.out.println("888888888 srcln: " + srcln + " ------ " + vulAppLines.get(srcln));
						}
						//if (pi < (patch.size()-1))
						//	srcln++;  //DIDN't work. why?
						//continue;//endless loop!

					}
					if (patch.get(pi).add.equals("+")) {

						if (print) {
							System.out.println("#### 2.2" + patch.get(pi).codeLine);
						}
						fixedAppLines.add(patch.get(pi).codeLine);// Add patch lines.
						fixedAppLinesNos.add(0);
					}

					if ((pi + 1) < patch.size()) {
						if (patch.get(pi + 1).srcln == (srcln + 1)) {
							srcln++;
							pi++;
							continue;
						} else if (patch.get(pi + 1).srcln > (srcln + 1)) {// To handle gaps in the patch
							pi++; //No increase. TODO: I want to come back later to this line
							break;
						}
					}


					if(pi == patch.size()-1 && !patchHasPreLine) {
						llm = patch.get(pi).getSrcln();
						if (print) {
							System.out.println("llm: "+llm);
						}
					}
					pi++;
					continue;
				}
				// TODO: safe as global || to a file,
				//Here, after applying the patch we safe a copy of the code and line number
				if(pi >= patch.size()-1 ) {// I applied the patch
					AppLines_partlyFixed.clear();
					AppLines_partlyFixed.addAll(fixedAppLines);
					AppLines_partlyFixedNos.clear();
					AppLines_partlyFixedNos.addAll(fixedAppLinesNos);
				}
				if (print)
				 {
					System.out.println("srcln: "+llm+ ", Compare to llm?");
//				lastLineModified = srcln;
				}

			} else {
				//if (print) System.out.println("#### 3");
//				if((l - 1+shift) >0 && (l - 1+shift )< vulLines.size() ) {
				String line ="";
				int linNo;
				//if(srcln <= vulAppLines.size()) {
				if(srcln <= temp.size()) {
					//==== handling old lines
					//patchHasPreLine reachLlmLine
					if(patchHasPreLine && !reachLlmLine) {
						//get if from partily fixed

						linNo = AppLines_partlyFixedNos.get(srcln);
						System.out.println("srcln .....t6 "+linNo);
						line = temp.get(AppLines_partlyFixedNos.indexOf(linNo));
					}else {
						linNo = srcln;
						System.out.println("srcln ..... "+srcln);
						line =  temp.get(linNo-1);

					}

				//line =  vulAppLines.get(srcln-1);
					//line =  temp.get(srcln-1);
//					if(reachLlmLine || !patchHasPreLine) {
//						line =  temp.get(linNo-1);
//					}else if(patchHasPreLine ){
//						//Integer ln = srcln;
//						System.out.println("srcln .....t6 "+linNo);
//						line = temp.get(AppLines_partlyFixedNos.indexOf(linNo));
//					}

				if((psImportExist != 0 && psImportExist != 2 ) && !line.startsWith("import") && !line.startsWith("package")){// add ps import before the class
					fixedAppLines.add("import java.sql.PreparedStatement;");
					fixedAppLinesNos.add(0);
					psImportExist = 2;
				}
				if(scannerExist  != 0 &&  scannerExist!= 2  && !line.startsWith("import") && !line.startsWith("package")) {
					fixedAppLines.add("import java.util.Scanner;");
					fixedAppLinesNos.add(0);
					scannerExist = 2;
				}
				if ((line.contains("class ") || line.contains("public "))//TODO: check if you have to handle private classes
						&& (line.contains(vulCName+" ") || line.contains(vulCName+"{") //To handle class name
								 )) {
//					if(!psImportExist){// add ps import before the class
//						fixedAppLines.add("import java.sql.PreparedStatement;");
//						psImportExist = true;
//					}
					line = line.replace(vulCName, vulCName+"_fixed");
				}


				if ( line.contains("public ")//TODO: check if you have to handle private classes
						&& (line.contains(vulCName+" ") || line.contains(vulCName+"(") )) {//To handle constructors
					line = line.replace(vulCName, vulCName+"_fixed");
				}

//				if( line.contains(" "+vulCName+"(") ||line.contains("("+vulCName+"(") // Handles calls to constructor
//						|| line.contains(" "+vulCName+".") || line.contains("("+vulCName+".")) { // Handles calls to class
//					line = line.replace(vulCName, vulCName+"_fixed");
//				}

				if(line.contains(" "+vulCName+"(")) { // Handles calls to constructor
					line = line.replace(" "+vulCName+"(", " "+vulCName+"_fixed(");
				}

				if(line.contains("("+vulCName+"(")) { // Handles calls to constructor
					line = line.replace( "("+vulCName+"(" , "("+vulCName+"_fixed(" );
				}
				if(line.contains(" "+vulCName+".")) { // Handles calls to class
					line = line.replace(" "+vulCName+"." , " "+vulCName+"_fixed.");
				}

				if(line.contains("("+vulCName+".")) { // Handles calls to class
					line = line.replace("("+vulCName+"." , "("+vulCName+"_fixed.");
				}
				//To handle GUI-DBMS
				if( line.contains("<"+vulCName+">")) {  // Handles objects ArrayList<vulCName>

					line = line.replace("<"+vulCName+">", "<"+vulCName+"_fixed>");
				}//------------------
				if(line.trim().startsWith("import ")) {
					psImportExist = 1;
					scannerExist = 1;
					if(line.contains(".sql.*") || line.contains(".PreparedStatement")){
						psImportExist = 2;
					}
					if(line.contains(".Scanner")) {
						scannerExist = 2;
					}
				}
				fixedAppLines.add(line);
				fixedAppLinesNos.add(linNo);

				}
			}

			srcln++;
		}

		// Print fixed App
		String fixedAppPath = vulApp.replace(vulCName+".java", vulCName+"_fixed.java");
		if (print) {
			System.out.println("fixedAppPath: " + fixedAppPath);
//			for (String l : fixedAppLines) {
//				System.out.println(l);
//			}
		}
			StrUtil.write_tofile(fixedAppPath, fixedAppLines);

		//Test java file (fixedAppPath).
		//If no Syntax error,
				//		G.AppLines_partlyFixed.clear()
				//		& G.AppLines_partlyFixed = AppLines_partlyFixed
				//		& G.LastLineModified = llm &return true
				//else return false

		int test =TestFixedFiles.patchIntroducedSynError(vulApp,fixedAppPath, false );
		if(test == G.noSynErr  || test == G.vul_and_fixedAppshaveSynErr) {
//			if (print)
			System.out.println("No Syntax error!");
			G.AppLines_partlyFixed.clear();
			G.AppLines_partlyFixed.addAll(AppLines_partlyFixed);
			G.AppLines_partlyFixedNos.clear();
			G.AppLines_partlyFixedNos.addAll(AppLines_partlyFixedNos);
			G.LastLineModified = llm;

			System.out.println("CCC++++++++++++++++++++++");
			for(int i= 0 ; i<G.AppLines_partlyFixed.size() ; i++) {
				System.out.println(G.AppLines_partlyFixedNos.get(i) + "@ " + G.AppLines_partlyFixed.get(i));
			}
			System.out.println("CCC++++++++++++++++++++++");
		} else if(test == G.fixedApphasSynErr) {
//			if (print)
			System.out.println("Patch introduced Syntax error(s)!");//There is/are syntax error(s)!

		} else if(test == G.vulApphasSynErr) {
//			if (print)
			System.out.println("Original code has error(s) or not compiled! llm agin is :"+ G.LastLineModified + 1);
		}
		return test;

	}



	public static int applyPatch_usingExistingPatchesLLM(String vulApp ,String context, List<Patch> patch,  String query) throws IOException {
		//TODO: $$$43 test this function correctness! and all todo here!
		//applyPatch2 handles sql on mlti lines!
		//3- Compile and return the result
		//apply sort on patch by line number
//		Collections.sort(patch);
		//read vulApp lines

		if(print) {
			System.out.println("==== @applyPatch EPs ====");
		}
		List<String> vulAppLines = StrUtil.read_lines_list(vulApp);
//		List<String> vulLines = new ArrayList<>();;

		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
		List<String> AppLines_partlyFixed = new ArrayList<>();
		if(G.AppLines_partlyFixed.size()>0)
		 {
			AppLines_partlyFixed.addAll(G.AppLines_partlyFixed);//DO I need it?
		}
		int llm = G.LastLineModified + 1;//Last Line Modified

		if(llm>1 && AppLines_partlyFixed.size()>1) {
			//System.out.println("==== @XXXX ====");
			fixedAppLines.clear();
			fixedAppLines.addAll(AppLines_partlyFixed);
			//= AppLines_partlyFixed;

		}

//		if(print)
//		for(Patch p: patch) {
//			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );
//		}
//		System.out.println("--------------");

//		=========== Apply the patch
		int pi = 0;
//		fixedAppLines.add(vulAppLines.get(0));//1
		int psImportExist = 0;
		for(int srcln = llm; srcln<= vulAppLines.size();srcln++) {
//			if(print)System.out.println("#### 1 , llm: "+llm );
			if(pi < patch.size() && srcln == patch.get(pi).getSrcln() ) {
				if (print) {
					System.out.println("#### 2 @" + srcln + " and " + patch.get(pi).getSrcln());
				}
				while (pi < patch.size() && srcln == patch.get(pi).getSrcln()) {
					if (patch.get(pi).add.equals("-")) {
						//skip no add to fixedAppLines
						if (print) {
							System.out.println("#### 2.1 " + patch.get(pi).add);
							System.out.println("888888888 srcln: " + srcln + " ------ " + vulAppLines.get(srcln));
						}
						//if (pi < (patch.size()-1))
						//	srcln++;  //DIDN't work. why?
						//continue;//endless loop!

					}
					if (patch.get(pi).add.equals("+")) {

						if (print) {
							System.out.println("#### 2.2" + patch.get(pi).codeLine);
						}
						fixedAppLines.add(patch.get(pi).codeLine);// Add patch lines.
					}

					if ((pi + 1) < patch.size()) {
						if (patch.get(pi + 1).srcln == (srcln + 1)) {
							srcln++;
							pi++;
							continue;
						} else if (patch.get(pi + 1).srcln > (srcln + 1)) {// To handle gaps in the patch
							pi++; //No increase. I want to come back later to this line
							break;
						}
//						else {
//							pi++;
//							continue;
//						}
					}

//					if((pi+1)<patch.size()) {
//						if(patch.get(pi+1).srcln == (srcln+1))
//							srcln++;
//					}

					if(pi == patch.size()-1) {
						llm = patch.get(pi).getSrcln();
						if (print) {
							System.out.println("llm: "+llm);
						}
					}
					pi++;
					continue;
				}
				// TODO: safe as global || to a file,
				//Here, after applying the patch we safe a copy of the code and line number
				if(pi >= patch.size()-1 ) {// I applied the patch
					AppLines_partlyFixed.clear();
					AppLines_partlyFixed.addAll(fixedAppLines);
				}
				if (print)
				 {
					System.out.println("srcln: "+llm+ ", Compare to llm?");
//				lastLineModified = srcln;
				}

			} else {
				//if (print) System.out.println("#### 3");
//				if((l - 1+shift) >0 && (l - 1+shift )< vulLines.size() ) {
				String line ="";
				if(srcln <= vulAppLines.size()) {
				line =  vulAppLines.get(srcln-1);

				if((psImportExist != 0 && psImportExist != 2 ) && !line.startsWith("import") && !line.startsWith("package")){// add ps import before the class
					fixedAppLines.add("import java.sql.PreparedStatement;");
					psImportExist = 2;
				}
				if ((line.contains("class ") || line.contains("public "))//TODO: check if you have to handle private classes
						&& (line.contains(vulCName+" ") || line.contains(vulCName+"{") //To handle class name
								 )) {
//					if(!psImportExist){// add ps import before the class
//						fixedAppLines.add("import java.sql.PreparedStatement;");
//						psImportExist = true;
//					}
					line = line.replace(vulCName, vulCName+"_fixed");
				}


				if ( line.contains("public ")//TODO: check if you have to handle private classes
						&& (line.contains(vulCName+" ") || line.contains(vulCName+"(") )) {//To handle constructors
					line = line.replace(vulCName, vulCName+"_fixed");
				}

//				if( line.contains(" "+vulCName+"(") ||line.contains("("+vulCName+"(") // Handles calls to constructor
//						|| line.contains(" "+vulCName+".") || line.contains("("+vulCName+".")) { // Handles calls to class
//					line = line.replace(vulCName, vulCName+"_fixed");
//				}

				if(line.contains(" "+vulCName+"(")) { // Handles calls to constructor
					line = line.replace(" "+vulCName+"(", " "+vulCName+"_fixed(");
				}

				if(line.contains("("+vulCName+"(")) { // Handles calls to constructor
					line = line.replace( "("+vulCName+"(" , "("+vulCName+"_fixed(" );
				}
				if(line.contains(" "+vulCName+".")) { // Handles calls to class
					line = line.replace(" "+vulCName+"." , " "+vulCName+"_fixed.");
				}

				if(line.contains("("+vulCName+".")) { // Handles calls to class
					line = line.replace("("+vulCName+"." , "("+vulCName+"_fixed.");
				}
				//To handle GUI-DBMS
				if( line.contains("<"+vulCName+">")) {  // Handles objects ArrayList<vulCName>

					line = line.replace("<"+vulCName+">", "<"+vulCName+"_fixed>");
				}//------------------
				if(line.trim().startsWith("import ")) {
					psImportExist = 1;
					if(line.contains(".sql.*") || line.contains(".PreparedStatement")){
						psImportExist = 2;
					}
				}
				fixedAppLines.add(line);}
			}

		}

		// Print fixed App
		String fixedAppPath = vulApp.replace(vulCName+".java", vulCName+"_fixed.java");
//				"/Users/dareen/DCAFixer/src/datasets/AppsAfter/"
//				+ vulCName.replace("before", "after") + ".java";
		if (print) {
			System.out.println("fixedAppPath: " + fixedAppPath);
//			for (String l : fixedAppLines) {
//				System.out.println(l);
//			}
		}
			StrUtil.write_tofile(fixedAppPath, fixedAppLines);

		//Test java file (fixedAppPath).
		//If no Syntax error,
				//		G.AppLines_partlyFixed.clear()
				//		& G.AppLines_partlyFixed = AppLines_partlyFixed
				//		& G.LastLineModified = llm &return true
				//else return false

		int test =TestFixedFiles.patchIntroducedSynError(vulApp,fixedAppPath, false );
		if(test == G.noSynErr  || test == G.vul_and_fixedAppshaveSynErr) {
//			if (print)
			System.out.println("No Syntax error!");
			G.AppLines_partlyFixed.clear();
			G.AppLines_partlyFixed.addAll(AppLines_partlyFixed);
			G.LastLineModified = llm;
		} else if(test == G.fixedApphasSynErr) {
//			if (print)
			System.out.println("Patch introduced Syntax error(s)!");//There is/are syntax error(s)!

		} else if(test == G.vulApphasSynErr) {
//			if (print)
			System.out.println("Original code has error(s) or not compiled! llm agin is :"+ G.LastLineModified + 1);
		}
		return test;

	}






//	//========================================================
//	//test the other patches & perform the experiment
//	public static void main(String[] args) throws Exception {
//
//
//	}

}
