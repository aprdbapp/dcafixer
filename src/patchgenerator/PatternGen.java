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
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import slicer.utilities.StrUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author dpc100
 */
public class PatternGen {
	static boolean print =false;
//	static class AppMD{
//		String className;
//		String app_sql;
//		String app_stmt;
//		int no_pstmt=0; 
//		String ps_query;
//		String wl_query;
//		String setStrings;
//		
//	}
	//static AppMD appmd = new AppMD();
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
		if(RplacePHs.ps_cq_ph != null && amd.ps_query!= null)
		if(line.contains(RplacePHs.ps_cq_ph) ) {
			newLine = newLine.replace(RplacePHs.ps_cq_ph, amd.ps_query);
		} 
		if(RplacePHs.wl_cq_ph!= null && amd.wl_query!= null)
		if(line.contains(RplacePHs.wl_cq_ph)) {
			newLine = newLine.replace(RplacePHs.wl_cq_ph, amd.wl_query);//???
		}
		//if(RplacePHs.sets_ph!= null && amd.setStrings != null)
		if(line.contains(RplacePHs.sets_ph)) {
			newLine = newLine.replace(RplacePHs.sets_ph, amd.setStrings );
		}
		if(!newLine.trim().endsWith(";")) {
			newLine = newLine +";";
		}
//		if(line.contains(RplacePHs.ps_ph)) {
//			// handling declaring more than one PS variable
//			newLine = newLine.replace(RplacePHs.ps_ph, RplacePHs.ps_ph+ln );
//		}
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
					if(print) System.out.println("Calling the operationProcessor");
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
						if(print) System.out.println("**** 2");
						Patch l = new Patch();
						l.add = "";
						l.ln = vul_curr_line;
						l.codeLine = clean_code_line(line, amd,l.ln);
						patch.add(l);
//					}else if(!newLine.equals(line) && newLine.contains("\n"+line)) {
					}else if(!newLine.equals(line) && newLine.contains(line)) {
						if(print) System.out.println("**** 3");
						String []parts = newLine.split("\n", 2);
						if (parts.length>1) {
							if(print) System.out.println("part0 : "+ parts[0] );
							if(print) System.out.println("part1 : "+ parts[1] );
							if(parts[0].startsWith("+")|| parts[0].startsWith("-")) {
								if(print) System.out.println("**** 3.1");
							
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
								if(print) System.out.println("**** 3.2");
								if(next_pos == curr_pos) {
								line = parts[0];// for next "operationProcessor" call
								nextLine = parts[1];}
							}
							
						}else {
							if(print) System.out.println("**** 3.3, "+ newLine);
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
						if(print) System.out.println("**** 4");
						Patch l = new Patch();
						String tmp = newLine;
//						char add =' ';
						if(newLine.startsWith("+")) {
							if(print) System.out.println("**** 4.1 +");
							tmp = tmp.substring(1).trim();
							l.ln = curr_pos;//vul_curr_line;
							l.codeLine = clean_code_line(tmp, amd, l.ln);
							l.add = "+";
							
							patch.add(l);
							line = null;
						}else if(newLine.startsWith("-")) {
							if(print) System.out.println("**** 4.2 -");
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
				if(print) {
					System.out.println("Before Sort:");
				for(Patch p: patch) {
					System.out.println( p.ln+ " "+p.add +p.codeLine );	
				}
		//map patch
				System.out.println("after Sort:");
				Collections.sort(patch);
				for(Patch p: patch) {
					System.out.println( p.ln+ " "+p.add +p.codeLine );	
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
					if(print) System.out.println("Calling the operationProcessor");
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
						if(print) System.out.println("**** 2");
						Patch l = new Patch();
						l.add = "";
						l.ln = vul_curr_line;
						l.codeLine = clean_code_line(line, amd,l.ln);
						patch.add(l);
//					}else if(!newLine.equals(line) && newLine.contains("\n"+line)) {
					}else if(!newLine.equals(line) && newLine.contains(line)) {
						if(print) System.out.println("**** 3");
						String []parts = newLine.split("\n", 2);
						if (parts.length>1) {
							if(print) System.out.println("part0 : "+ parts[0] );
							if(print) System.out.println("part1 : "+ parts[1] );
							if(parts[0].startsWith("+")|| parts[0].startsWith("-")) {
								if(print) System.out.println("**** 3.1");
							
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
								if(print) System.out.println("**** 3.2");
								if(next_pos == curr_pos) {
								line = parts[0];// for next "operationProcessor" call
								nextLine = parts[1];}
							}
							
						}else {
							if(print) System.out.println("**** 3.3, "+ newLine);
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
						if(print) System.out.println("**** 4");
						Patch l = new Patch();
						String tmp = newLine;
//						char add =' ';
						if(newLine.startsWith("+")) {
							if(print) System.out.println("**** 4.1 +");
							tmp = tmp.substring(1).trim();
							l.ln = curr_pos;//vul_curr_line;
							l.codeLine = clean_code_line(tmp, amd, l.ln);
							l.add = "+";
							
							patch.add(l);
							line = null;
						}else if(newLine.startsWith("-")) {
							if(print) System.out.println("**** 4.2 -");
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
					if(print)System.out.println("^^^ "+p.ln + ": "+p.srcln+" " + p.add + p.codeLine +" Query@"+querylines.get(q) +"querylines.size() "+querylines.size());if(print)
					if(print)System.out.println("^^^ HERE1");
					if( q<querylines.size() &&p.srcln.equals(querylines.get(q)) && p.add.equals("+") ) {
					if(print)
						System.out.println("^^^ HERE2");
					QlineFound = true;
					vln = p.ln;
					q++;
					p.setLn(vln);
					String parts[] = p.getCodeLine().split("\\(", 2);
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
							if(print)System.out.println("^^^vln 1 "+p.ln + " " + p.add + p.codeLine);
							patch2.add(p);
							i++;
							
						} else { // add the extra lines that the slicer didn't get
//							vln = vln -1;
							Patch newP = new Patch();
							newP.setAdd("-"); 
							newP.setCodeLine("drop");// = " ";
							newP.setSrcln(p.srcln);//querylines.get(q));
							
							newP.setLn(vln);// = vln;
							if(print)System.out.println("^^^vln 2 "+newP.ln + " " + 
							newP.add + newP.codeLine);

							patch2.add(newP);
						}
						q++;
						
						if(i< newPatch.size())
						p = newPatch.get(i);
					} 
					//else {
						QlineFound = false;
					//}
				}
				

				
			}
			int shift = querylines.size();
			boolean newLinesAdded = false;
//			for (Patch p : patch2) {
			for(int p =0; p < patch2.size(); p++) {
				
				if(patch2.get(p).ln < 0) {
					int tmp = patch2.get(p).ln;
					patch2.get(p).setLn(tmp * -1);
					//p.ln =  p.ln * -1;
					newLinesAdded = true;
				}
				if(newLinesAdded) {
					if(patch2.get(p).ln > 0) {
						int tmp = patch2.get(p).ln;
						patch2.get(p).setLn(tmp + shift);
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
		String path = p.replace(parts[parts.length - 1],"");;
		return path;
	}
	public static List<Patch>  mapSliceToSrc (String context, List<Patch> patch) throws IOException{		
		//Map patch to source code
		List<Patch> newPatch = patch;
		//System.out.print(p);
		List<String> contextLines = StrUtil.read_lines_list(context);
		int i= 0;//, j=0;
		for (int j=0;j<contextLines.size();j++) {
				
				String []cParts = contextLines.get(j).split(",");
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
		for (int j=0;j<contextLines.size();j++) {
				
				String []cParts = contextLines.get(j).split(",");
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
		for (int j=0;j<contextLines.size();j++) {
				
				String []cParts = contextLines.get(j).split(",");
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
		for (int i = 0; i < contents.length; i++) {
			if (contents[i].endsWith(".java")) {
				find_diff(vsPath, slicesPath + "/" + contents[i]);
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
		
		System.out.println("Best Pattern is: " + getClass( PatternsRanks.get(PatternsRanks.size()-1).slice) + "\nWith LCS length: " + PatternsRanks.get(PatternsRanks.size()-1).lcsSize);
		System.out.println(PatternsRanks.get(PatternsRanks.size()-1).actions.toString());
		for (Ops op : PatternsRanks.get(PatternsRanks.size()-1).actions) {
			System.out.println("--------------------");
			System.out.println(op.position + ": " + op.operation);
		}
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
		
		if(print)
		for(Patch p: newPatch) {
			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );	
		}
//		Apply the patch
		int pi =0;
		for(int l =1; l<= vulLines.size();l++) {
			if(print)System.out.println("#### 1 " );
//			if(pi<newPatch.size()) {
			if(pi < newPatch.size() && l == newPatch.get(pi).srcln ) {
				if(print)System.out.println("#### 2");
//				int xxx= 0;
				while (l == newPatch.get(pi).srcln && pi < newPatch.size()) {
					if(newPatch.get(pi).add.equals("-")) {
						if(print)System.out.println("#### 2.1");
						pi++;
						continue;
					}
					if(newPatch.get(pi).add.equals("+")) {

						if(print)System.out.println("#### 2.2"+ newPatch.get(pi).codeLine);
						fixedAppLines.add(newPatch.get(pi).codeLine);// Add patch lines.
						pi++;
						if(pi==newPatch.size()) {
							if(print)System.out.println("#### 2.2.1");
							break;
						}
					}
				}
				
//			}
			}else {
				if(print)System.out.println("#### 3");
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
		String fixedAppPath ="/Users/dpc100/eclipse-workspace/DCAFixer/src/datasets/AppsAfter/"+vulCName.replace("before", "after")+".java";
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
		if(print)
		System.out.println("==== @applyPatch2 ====");
		List<String> vulLines = StrUtil.read_lines_list(vulApp);
		List<Patch> newPatch = mapSliceToSrc2 (context, patch);
		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
		
		if(print)
		for(Patch p: newPatch) {
			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );	
		}
//		Apply the patch
		int pi =0;boolean foundMliLineQuery= false;
		for(int l =1; l<= vulLines.size();l++) {
			if(print)System.out.println("#### 1 " );
//			if(pi<newPatch.size()) {
			String tmp = vulLines.get(l-1);
			if (foundMliLineQuery && !tmp.endsWith(";")) {
				if(print)System.out.println("#### 1.1");
				continue;
			}
			if (foundMliLineQuery && tmp.endsWith(";")) {
				if(print)System.out.println("#### 1.2");
				foundMliLineQuery = false;
				continue;
			} 
			if(pi < newPatch.size() && l == newPatch.get(pi).srcln ) {
				if(print)System.out.println("#### 2"+tmp +"@"+(l-1));
//				int xxx= 0;
//				if(pi < newPatch.size()) {
				while (pi < newPatch.size() &&l == newPatch.get(pi).srcln ) {
					if(newPatch.get(pi).add.equals("-")) {
						if(print)System.out.println("#### 2.1 ");
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

							if(print)System.out.println("#### 2.2"+ newPatch.get(pi).codeLine);
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
							if(print)System.out.println("#### 2.1.1");
							foundMliLineQuery = true;
							break;
						}
						
						if((tmp.contains("executeQuery") || tmp.contains("executeUpdate")|| tmp.contains(".execute(")) 
										&& !tmp.endsWith(";") && 
										(tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))
								){
							if(print)System.out.println("#### 2.1.2");
							foundMliLineQuery = true;
							break;
						}
						pi++;
						continue;
				
				
			}
			}else {
				if(print)System.out.println("#### 3");
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
		String fixedAppPath ="/Users/dpc100/eclipse-workspace/DCAFixer/src/datasets/AppsAfter/"+vulCName.replace("before", "after")+".java";
		
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
		if(print)
		System.out.println("==== @applyPatch2 ====");
		List<String> vulLines = StrUtil.read_lines_list(vulApp);
//		List<String> vulLines = new ArrayList<>();;
		
		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
//		int shift = queryLines.size();
		
		
		if(print)
		for(Patch p: patch) {
			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );	
//			if(p.ln < -1) {
////				vulLines.ad
//			}
		}
//		Apply the patch
		int pi =0, shift = 0;
		//boolean foundMliLineQuery= false;
		fixedAppLines.add(vulLines.get(0));
		for(int l =1; l<= vulLines.size();l++) {
			if(print)System.out.println("#### 1 " );
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
				if (print)
					System.out.println("#### 2 @" + l + " and " + patch.get(pi).getSrcln());
//				int xxx= 0;
//				if(pi < newPatch.size()) {
				while (pi < patch.size() && l == patch.get(pi).getSrcln()) {
					if (patch.get(pi).add.equals("-")) {
						if (print)
							System.out.println("#### 2.1 " + patch.get(pi).add);
						if ((pi + 1) < patch.size()) {
							if (patch.get(pi + 1).srcln == l) {
								pi++; 
								shift++;
								if (print)
									System.out.println("#### 2.1.a " + patch.get(pi).add + pi);
								if (pi == patch.size()) {
									break;
								}
								continue;
							}
						}
					}
					if (patch.get(pi).add.equals("+")) {

						if (print)
							System.out.println("#### 2.2" + patch.get(pi).codeLine);
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
				if (print)
					System.out.println("#### 3");
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
		String fixedAppPath = "/Users/dpc100/eclipse-workspace/DCAFixer/src/datasets/AppsAfter/"
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
		if(print)
		System.out.println("==== @applyPatch2 ====");
		List<String> vulLines = StrUtil.read_lines_list(vulApp);
		List<Patch> newPatch = mapSliceToSrc2 (context, patch);
		String vulCName = getClass(vulApp);
		List<String> fixedAppLines = new ArrayList<>();
		
		if(print)
		for(Patch p: newPatch) {
			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );	
		}
//		Apply the patch
		int pi =0;boolean foundMliLineQuery= false;
		for(int l =1; l<= vulLines.size();l++) {
			if(print)System.out.println("#### 1 " );
//			if(pi<newPatch.size()) {
			String tmp = vulLines.get(l-1);
			if (foundMliLineQuery && !tmp.endsWith(";")) {
				if(print)System.out.println("#### 1.1");
				continue;
			}
			if (foundMliLineQuery && tmp.endsWith(";")) {
				if(print)System.out.println("#### 1.2");
				foundMliLineQuery = false;
				continue;
			} 
			if(pi < newPatch.size() && l == newPatch.get(pi).srcln ) {
				if(print)System.out.println("#### 2"+tmp +"@"+(l-1));
//				int xxx= 0;
				
				while (l == newPatch.get(pi).srcln && pi < newPatch.size()) {
					if(newPatch.get(pi).add.equals("-")) {
						if(print)System.out.println("#### 2.1 ");
						if((pi+1)<newPatch.size()) {
							if(newPatch.get(pi+1).srcln == l) {
								pi++;
								continue;
							}
						}}
						if(newPatch.get(pi).add.equals("+")) {

							if(print)System.out.println("#### 2.2"+ newPatch.get(pi).codeLine);
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
							if(print)System.out.println("#### 2.1.1");
							foundMliLineQuery = true;
							break;
						}
						
						if((tmp.contains("executeQuery") || tmp.contains("executeUpdate")|| tmp.contains(".execute(")) 
										&& !tmp.endsWith(";") && 
										(tmp.toLowerCase().contains("select")|| tmp.toLowerCase().contains("update")
								|| tmp.toLowerCase().contains("insert")||tmp.toLowerCase().contains("delete"))
								){
							if(print)System.out.println("#### 2.1.2");
							foundMliLineQuery = true;
							break;
						}
						pi++;
						continue;
				}
				
//			}
			}else {
				System.out.println("#### 3");
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
//		String fixedAppPath ="/Users/dpc100/eclipse-workspace/DCAFixer/src/datasets/AppsAfter/"+vulCName.replace("before", "after")+".java";
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
        if(print)
        for(Patch p: patch) {
			System.out.println( p.add +" "+p.ln+":"+p.srcln+" "+p.codeLine );	
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
	//============== 
	//test the other patches & perform the experiment
	public static void main(String[] args) throws Exception {
		// Paths to each SSlice and VSlice
		String VSlicesPath = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/VSlices/";
		String SSlicesPath ="/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp";
		// Theorically SSlice7 is the best
		String vslicePath = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/q_s_v_vul.java";
		String vslicePath2 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/VSliceNoCS.java";

//        findAllDiffs(vslicePath,SSlicesPath);
		//findPatternsAndApply( String vulApp , String vsPath, String slicesPath, String context, String app_sql, String app_stmt, boolean print);
		 String vulApp="/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/CV1_vul.java";
	      String context="/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/context_test.txt";
	      AppMD app_md = new AppMD(); 
	      app_md.app_sql = "sql";
	      app_md.app_stmt = "stmt";
	      app_md.ps_query ="\"Select * from ACCOUNTS where name = ?;\"";
	      app_md.setStrings = "pstmt_dcafixer.setObject(1, colval);";
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice2.java", SSlicesPath, context, app_md, true);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice3.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice4.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice5.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice6.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice7.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice8.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice9.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice10.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice11.java", SSlicesPath, context, app_md, false);
		findPatternsAndApply_exp3(  vulApp , VSlicesPath+"VSlice12.java", SSlicesPath, context, app_md, false);
//        findAllDiffs(VSlicesPath+"VSlice1.java",SSlicesPath);
//        //TODO: loop over the patterns
//        createPatchFromSelectedPattern (VSlicesPath+"VSlice1.java", PatternsRanks.get(PatternsRanks.size()-1), "sql", "stmt", true);

        
//        createPatchFromSelectedPattern(vulApp, VSlicesPath+"VSlice1.java", 
//        		context, PatternsRanks.get(PatternsRanks.size()-1));
//        findAllDiffs(VSlicesPath+"VSlice2.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice3.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice4.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice5.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice6.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice7.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice8.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice9.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice10.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice11.java",SSlicesPath);
//        findAllDiffs(VSlicesPath+"VSlice12.java",SSlicesPath);
        

//		String sslicePath1 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice1.java";
//		String sslicePath2 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice2.java";
//		String sslicePath3 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice3.java";
//		String sslicePath4 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice4.java";
//		String sslicePath5 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice5.java";
//		String sslicePath6 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice6.java";
//		String sslicePath7 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice7.java";
//		String sslicePath8 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice8.java";
//		String sslicePath9 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice9.java";
//		String sslicePath10 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice10.java";
//		String sslicePath11 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice11.java";
//		String sslicePath12 = "/Users/dpc100/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/tmp/SSlice12.java";

//		PatternsRanks.clear();
//        find_diff(vslicePath, sslicePath1);
//        find_diff(vslicePath2, sslicePath3);
//        find_diff(vslicePath2, sslicePath4);
//        find_diff(vslicePath2, sslicePath6);
//        find_diff(sslicePath1, vslicePath);
//        find_diff(vslicePath, sslicePath11);
//        find_diff(vslicePath2, sslicePath1);
//		find_diff(vslicePath, sslicePath4);
//		find_diff(vslicePath, sslicePath9);
//		find_diff(vslicePath, sslicePath2);
//        find_diff(vslicePath, sslicePath3);

//        find_diff(vslicePath, sslicePath5);
//        find_diff(vslicePath, sslicePath6);
//        find_diff(vslicePath, sslicePath7);
//        find_diff(vslicePath, sslicePath8);
//		System.out.println("********************************");
//        System.out.println("Best Slice is: "+SSliceWithLCS+"\nWith LCS length: "+lcsLength);
		
		// loop over the patterns to fix the error
		
	}

}
