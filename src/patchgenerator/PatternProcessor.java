/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patchgenerator;

//import static astgumtree.FixPattern.getCtType;
import static astgumtree.EBSet.EB_selector;
import static astgumtree.FixPattern.getCtType;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import java.io.File;
import java.util.List;
import spoon.reflect.declaration.CtElement;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
//import com.github.gumtreediff.actions.model.Action;
//import com.github.gumtreediff.actions.model.Move;
//import com.github.gumtreediff.actions.model.Update;
//import com.github.gumtreediff.actions.model.Delete;
//import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.matchers.MappingStore;
//import com.github.gumtreediff.gen.Registry.Factory;
import com.github.gumtreediff.tree.ITree;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.impl.StaticMDCBinder;
//import org.mozilla.javascript.Token;
//import org.mozilla.javascript.ast.*;

import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import com.github.gumtreediff.matchers.Matchers;
import java.util.ArrayList;
import java.util.Arrays;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.PrettyPrinter;
import spoon.support.modelobs.ChangeCollector;
import spoon.support.sniper.SniperJavaPrettyPrinter;

/**
 *
 * @author dpc100
 */
public class PatternProcessor {

    /**
     * @param element
     */
	static class ActionParts{
		String actionName ="";
		String nodeType="";
		int pos=0;
		String codeChange="";
		String ccFrom="";
		String ccTo="";
	}
    public static String partialElementPrint(CtElement element) {
        DefaultJavaPrettyPrinter print = new DefaultJavaPrettyPrinter(element.getFactory().getEnvironment()) {
            @Override
            public DefaultJavaPrettyPrinter scan(CtElement e) {
                if (e != null && e.getMetadata("isMoved") == null) {
                    return super.scan(e);
                }
                return this;
            }
        };

        print.scan(element);
        return print.getResult();
    }

    public static String operationProcessor(Operation op, Operation nextOP, String line, int prev, int curr, int next, String app_sql, String app_stmt, boolean print) {
//    	boolean print =true;
    	if(print) System.out.println("======== @operationProcessor =========");
    	ActionParts ap = GetOpParts(op, false);
    	if(print) System.out.print("Action: "+op.toString());
    	String newLine = "";
        CtElement node = op.getNode();
        Action action = op.getAction();
//        boolean print =false;
        CtElement element = node;
        String tmpKey = "###";//"#"+ap.pos+ap.nodeType; // because of "FieldRead" & "VariableRead"
        if (ap == null) {
        	return null;
        }
        if(print) System.out.println("curr "+curr+" line: "+line+"\n next: "+next);
        // Collect the following data:
        //Action Name, Node Type, Code Change, 
        if(print) System.out.println("Action Name: "+ap.actionName);
        //String action_name = ap.actionName;
        
        if(print) System.out.println("Node Type: "+ap.nodeType);
        if(print) System.out.println("Code Change: "+ap.codeChange);
        if(print) System.out.println("CC.From: "+ap.ccFrom);
        if(print) System.out.println("CC.To: "+ap.ccTo);
        if(print) System.out.println("Pos: "+ap.pos);
       
		if (action instanceof Insert) {
			//ap.actionName.equals("Insert") &&
			if ( ap.nodeType.equals("Assignment")) {
				if(line != "")
					newLine = ap.codeChange+";\n"+line;
				else
					newLine = ap.codeChange+";";
				if(print) System.out.println(newLine);
//				if (line.contains(tmpKey)) {
//					if (ap.codeChange.trim().startsWith("=")) {
//						newLine = line.replace(tmpKey, ap.codeChange.trim().substring(1)); //remove "="
//						
//					} else {
//						newLine = line.replace(tmpKey, ap.codeChange);
//					}
//				} else {//???
//					newLine = line +" "+ap.codeChange;
//				}

			}
			
			if ( ap.nodeType.equals("LocalVariable")) {
				//newLine = "+ "+ap.codeChange+";\n"+line;
				newLine = ap.codeChange + ";";
				if (next == -1) {
					newLine = "+ " + newLine + "\n+ " + line;
				} 
				
				else if (next == curr) {
					if (nextOP != null) {
						ActionParts ap2 = GetOpParts(nextOP, false);
						if (ap2 != null) {
						if (ap2.actionName.equals("Move"))
							newLine = newLine + "\n" + line;}
					} else {
						newLine = "+ " + newLine + "\n" + line;
					}
				}
				else {
					newLine = "+ " + newLine + "\n" + line;
				}
			}
			
			if ( ap.nodeType.equals("Invocation")) {
//				if(ap.codeChange.contains("pstmt_dcafixer.close()")) {
//					newLine = null;//line;// For now. TODO: pick the right place to call close.
//				}else 
				if(ap.codeChange.contains(".setObject")) {
					//newLine = "+ "+ap.codeChange+";\n"+line;
					newLine =ap.codeChange+";";
					if( next==-1 ) {
						newLine = "+ " + newLine + "\n+ "+ line;
					}else {
						newLine = "+ " + newLine + "\n"+ line;
		        	}
				}	
			}
			if (ap.nodeType.equals("FieldRead") || ap.nodeType.equals("VariableRead")) {
				if(ap.codeChange.equals("pstmt_dcafixer")) {
					newLine = line;
				}
				// sql
			}
		} //================ End of Insert

        if (action instanceof Update) {
        	if(ap.nodeType.equals("VariableRead")){
        		if(ap.ccFrom.equals(app_sql) && ap.ccTo.equals("pstmt_dcafixer")) {
        			newLine = line.replace(app_sql, "");
        		}
        		if(print) System.out.print("Update, VariableRead: ");//+newLine);
        	}
        	
        	if(ap.nodeType.equals("LocalVariable")){
        		//newLine = "- "+ line+"\n+ "+ap.ccTo;
        		newLine = ap.ccTo+";";
        		if(print) System.out.print("Update, LocalVariable: ");
        	}
        	if(ap.nodeType.equals("Literal")){
        		newLine = line.replace(ap.ccFrom, ap.ccTo);
        		if(print) System.out.print("Update, Literal: ");
        	}
        	if(curr != next ) {
				newLine = "- " + line + "\n+ "+ newLine;
			}else {
        		newLine = "- " + line + "\n"+ newLine;
        	}
        }//================ End of Update
        if (action instanceof Move) {
        	String codeToMove="";
        	if (ap.nodeType.equals("TypeReference") ) {
        		String [] parts = ap.codeChange.split(".");
        		if(parts.length>0) {
        			codeToMove= parts[parts.length-1];
        		}else {
        			codeToMove = ap.codeChange;
        		}
        		newLine = codeToMove +" "+line;
        		if(print) System.out.print("Move, TypeReference: ");//+newLine);
        	}
        	
        	if(ap.nodeType.equals("FieldWrite")) {
        		newLine = ap.codeChange +" "+ line;
        		if(print) System.out.print("Move, FieldWrite: ");//+newLine);
        	}
        	
        	
        }//================ End of Move

        if (action instanceof Delete) {
        	//ap.actionName.equals("Delete") &&
        	
        	String codeToRemove="";
        	if (ap.nodeType.equals("Assignment") ) {
        		newLine = "- " +line;
        		if(print) System.out.print("Delete, Assignment : ");//+newLine);
//        		String parts[] = line.split("=",2);
//        		codeToRemove = parts[1].trim();
//        		if(codeToRemove.endsWith(";") && !ap.codeChange.trim().endsWith(";")) {
//        			codeToRemove = codeToRemove.replaceFirst(".$",""); //remove last letter i.e., ";"
//        		}else {
//        			//TODO: handle comments by splitting on ";"
//        		}
////        		newLine = newLine.replace(codeToRemove, tmpKey);
//        		//TODO: check if using pos is good here.
//        		newLine = line.replace(codeToRemove, tmpKey);
        	}
        	
			if (ap.nodeType.equals("FieldRead")) {// || ap.nodeType.equals("VariableRead")) {
        		codeToRemove = ap.codeChange;
        		if(codeToRemove.contains(app_sql) && (line.contains("executeUpdate")||line.contains("executeUpdate") || line.contains("execute("))) {
        			newLine = line.replace(codeToRemove, "");
        		}else if(codeToRemove.contains(app_stmt)) {
        			newLine = line.replace(codeToRemove, "pstmt_dcafixer");
        			if(curr != next ) {
        				newLine = "- " + line + "\n+ "+ newLine;
        			}else {
                		newLine = "- " + line + "\n"+ newLine;
                	}
        		}else  {
        			newLine = line.replace(codeToRemove, tmpKey);
        			if(curr != next ) {//????
        				//!newLine.startsWith("-")
                		newLine = "- " + line + "\n+ "+ newLine;
                	}if(curr == next && curr != prev ) {
//                		newLine =  newLine;
//                	}else  {
                		newLine = "- " + line + "\n"+ newLine;
                	}
        		}
        		if(print) System.out.print("Delete, FieldRead : ");//+newLine);
        		
        	}
        	
        	if (ap.nodeType.equals("LocalVariable")) {
        		newLine = "- "+ line;
        		if(print) System.out.println("Delete, LocalVariable : "+newLine);
        	}
        	
        	if(ap.nodeType.equals("BinaryOperator")) {
        		
        		if(line.contains("executeUpdate")) {
        			
        			String []parts = line.split("executeUpdate");
        			if(parts.length ==2) {
        				if(parts[0].trim().startsWith("try") ) {
        					String end="";
        					if(parts[1].trim().endsWith("{"))
        						end = " {";
        					newLine = line.replace(parts[1], "())") + end;
        				}else {
        				newLine = line.replace(parts[1], "();");
        				}
        			}
        		}
        		if(line.contains("executeQuery")) {
        			String []parts = line.split("executeQuery");
        			if(parts.length ==2) {
        				if(parts[0].trim().startsWith("try") ) {
        					String end="";
        					if(parts[1].trim().endsWith("{"))
        						end = " {";
        					newLine = line.replace(parts[1], "())") + end;
        				}else {
        				newLine = line.replace(parts[1], "();");
        				}
        			}
//        				newLine = line.replace(parts[1], "();");
        		}
        		if(line.contains("execute(")) {
        			String []parts = line.split("execute(");
        			if(parts.length ==2) {
        				if(parts[0].trim().startsWith("try") ) {
        					String end="";
        					if(parts[1].trim().endsWith("{"))
        						end = " {";
        					newLine = line.replace(parts[1], "))") + end;
        				}else {
        				newLine = line.replace(parts[1], ");");
        				}
        			}
        				//newLine = line.replace(parts[1], ");");
        		}
        		if(print) System.out.print("Delete, BinaryOperator : ");
        		if(curr != next ) {//!newLine.startsWith("-")
            		newLine = "- " + line + "\n+ "+ newLine;
            	}else {
            		newLine = "- " + line + "\n"+ newLine;
            	}
        		if(print) System.out.println("Delete, BinaryOperator: "+newLine);
        	}
        	
        	
//        	if(curr != next ) {//!newLine.startsWith("-")
//        		newLine = "- " + line + "\n+ "+ newLine;
//        	}else {
//        		newLine = "- " + line + "\n"+ newLine;
//        	}
        }//================ End of Delete
        
        if(curr != next) {
        	//clean the line.
//        	if(newLine.contains(tmpKey)) {
//        		newLine = newLine.replace(tmpKey,"");
//        	}
        	if(newLine != null)
        	if(!newLine.startsWith("+") && newLine != ""  && !newLine.startsWith("-") )
        	newLine = "+ " +newLine;
//        	TODO: Handle "query_with_replace_calls", "cleaned_query"
        }
        if(print) System.out.println(newLine);
        return newLine;
    }

    
    public static String operationProcessor_exp4(Operation op, Operation nextOP, String line, int prev, int curr, int next, String app_sql, String app_stmt,ArrayList<Integer> querylines, boolean print) {
//    	boolean print =true;
    	if(print) System.out.println("======== @operationProcessor =========");
    	ActionParts ap = GetOpParts(op, false);
    	if(print) System.out.print("Action: "+op.toString());
    	String newLine = "";
        CtElement node = op.getNode();
        Action action = op.getAction();
//        boolean print =false;
        CtElement element = node;
        String tmpKey = "###";//"#"+ap.pos+ap.nodeType; // because of "FieldRead" & "VariableRead"
        if (ap == null) {
        	return null;
        }
        if(print) System.out.println("curr "+curr+" line: "+line+"\n next: "+next);
        // Collect the following data:
        //Action Name, Node Type, Code Change, 
        if(print) System.out.println("Action Name: "+ap.actionName);
        //String action_name = ap.actionName;
        
        if(print) System.out.println("Node Type: "+ap.nodeType);
        if(print) System.out.println("Code Change: "+ap.codeChange);
        if(print) System.out.println("CC.From: "+ap.ccFrom);
        if(print) System.out.println("CC.To: "+ap.ccTo);
        if(print) System.out.println("Pos: "+ap.pos);
        
        
		if (action instanceof Insert) {
			//ap.actionName.equals("Insert") &&
			if ( ap.nodeType.equals("Assignment")) {
				if(line != "")
					newLine = ap.codeChange+";\n"+line;
				else
					newLine = ap.codeChange+";";
				if(print) System.out.println(newLine);
//				if (line.contains(tmpKey)) {
//					if (ap.codeChange.trim().startsWith("=")) {
//						newLine = line.replace(tmpKey, ap.codeChange.trim().substring(1)); //remove "="
//						
//					} else {
//						newLine = line.replace(tmpKey, ap.codeChange);
//					}
//				} else {//???
//					newLine = line +" "+ap.codeChange;
//				}

			}
			
			if ( ap.nodeType.equals("LocalVariable")) {//*****************************
				//newLine = "+ "+ap.codeChange+";\n"+line;
				newLine = ap.codeChange + ";";
				
				if (next == -1) {
					newLine = "+ " + newLine + "\n+ " + line;
				} 
				
				else if (next == curr) {
					if (nextOP != null) {
						ActionParts ap2 = GetOpParts(nextOP, false);
						if (ap2 != null) {
						if (ap2.actionName.equals("Move"))
							newLine = newLine + "\n" + line;}
					} else {
						newLine = "+ " + newLine + "\n" + line;
					}
				}
				else {
					newLine = "+ " + newLine + "\n+ " + line;
				}
				
			}
			
			if ( ap.nodeType.equals("Invocation")) {
//				if(ap.codeChange.contains("pstmt_dcafixer.close()")) {
//					newLine = null;//line;// For now. TODO: pick the right place to call close.
//				}else 
				if(ap.codeChange.contains(".setObject")) {
					//newLine = "+ "+ap.codeChange+";\n"+line;
					newLine =ap.codeChange+";";
					if( next==-1 ) {
						newLine = "+ " + newLine + "\n+ "+ line;
					}else {
						newLine = "+ " + newLine + "\n"+ line;
		        	}
				}	
			}
			if (ap.nodeType.equals("FieldRead") || ap.nodeType.equals("VariableRead")) {
				if(ap.codeChange.equals("pstmt_dcafixer")) {
					newLine = line;
					if(next == -1  && newLine.equals("}}")) {
						newLine = "";
					}
		        	
		        	
				}
				// sql
			}
		} //================ End of Insert

        if (action instanceof Update) {
        	if(ap.nodeType.equals("VariableRead")){
        		if(ap.ccFrom.equals(app_sql) && ap.ccTo.equals("pstmt_dcafixer")) {
        			newLine = line.replace(app_sql, "");
        		}
        		if(print) System.out.print("Update, VariableRead: ");//+newLine);
        	}
        	
        	if(ap.nodeType.equals("LocalVariable")){
        		//newLine = "- "+ line+"\n+ "+ap.ccTo;
        		newLine = ap.ccTo+";";
        		if(print) System.out.print("Update, LocalVariable: ");
        	}
        	if(ap.nodeType.equals("Literal")){
        		newLine = line.replace(ap.ccFrom, ap.ccTo);
        		if(print) System.out.print("Update, Literal: ");
        	}
        	if(curr != next ) {
				newLine = "- " + line + "\n+ "+ newLine;
			}else {
        		newLine = "- " + line + "\n"+ newLine;
        	}
        }//================ End of Update
        if (action instanceof Move) {
        	String codeToMove="";
        	if (ap.nodeType.equals("TypeReference") ) {
        		String [] parts = ap.codeChange.split(".");
        		if(parts.length>0) {
        			codeToMove= parts[parts.length-1];
        		}else {
        			codeToMove = ap.codeChange;
        		}
        		newLine = codeToMove +" "+line;
        		if(print) System.out.print("Move, TypeReference: ");//+newLine);
        	}
        	
        	if(ap.nodeType.equals("FieldWrite")) {
        		newLine = ap.codeChange +" "+ line;
        		if(print) System.out.print("Move, FieldWrite: ");//+newLine);
        	}
        	
        	
        }//================ End of Move

        if (action instanceof Delete) {
        	//ap.actionName.equals("Delete") &&
        	
        	String codeToRemove="";
        	if (ap.nodeType.equals("Assignment") || ap.nodeType.equals("Literal")) {
        		newLine = "- " +line;
        		if(print) System.out.print("Delete, Assignment | Literal: "+newLine);
//        		String parts[] = line.split("=",2);
//        		codeToRemove = parts[1].trim();
//        		if(codeToRemove.endsWith(";") && !ap.codeChange.trim().endsWith(";")) {
//        			codeToRemove = codeToRemove.replaceFirst(".$",""); //remove last letter i.e., ";"
//        		}else {
//        			//TODO: handle comments by splitting on ";"
//        		}
////        		newLine = newLine.replace(codeToRemove, tmpKey);
//        		//TODO: check if using pos is good here.
//        		newLine = line.replace(codeToRemove, tmpKey);
        	}
        	
			if (ap.nodeType.equals("FieldRead")) {// || ap.nodeType.equals("VariableRead")) {
        		codeToRemove = ap.codeChange;
        		if(codeToRemove.contains(app_sql) && (line.contains("executeUpdate")||line.contains("executeUpdate") || line.contains("execute("))) {
        			newLine = line.replace(codeToRemove, "");
        		}else if(codeToRemove.contains(app_stmt)) {
        			newLine = line.replace(codeToRemove, "pstmt_dcafixer");
        			if(curr != next ) {
        				newLine = "- " + line + "\n+ "+ newLine;
        			}else {
                		newLine = "- " + line + "\n"+ newLine;
                	}
        		}else  {
        			newLine = line.replace(codeToRemove, tmpKey);
        			if(curr != next ) {//????
        				//!newLine.startsWith("-")
                		newLine = "- " + line + "\n+ "+ newLine;
                	}if(curr == next && curr != prev ) {
//                		newLine =  newLine;
//                	}else  {
                		newLine = "- " + line + "\n"+ newLine;
                	}
        		}
        		if(print) System.out.print("Delete, FieldRead : ");//+newLine);
        		
        	}
        	
        	if (ap.nodeType.equals("LocalVariable")) {
        		newLine = "- "+ line;
        		if(print) System.out.println("Delete, LocalVariable : "+newLine);
        	}
        	
        	if(ap.nodeType.equals("BinaryOperator")) {
        		
        		if(line.contains("executeUpdate")) {
        			
        			String []parts = line.split("executeUpdate");
        			if(parts.length ==2) {
        				if(parts[0].trim().startsWith("try") ) {
        					String end="";
        					if(parts[1].trim().endsWith("{"))
        						end = " {";
        					newLine = line.replace(parts[1], "())") + end;
        				}else {
        				newLine = line.replace(parts[1], "();");
        				}
        			}
        		}
        		if(line.contains("executeQuery")) {
        			String []parts = line.split("executeQuery");
        			if(parts.length ==2) {
        				if(parts[0].trim().startsWith("try") ) {
        					String end="";
        					if(parts[1].trim().endsWith("{"))
        						end = " {";
        					newLine = line.replace(parts[1], "())") + end;
        				}else {
        				newLine = line.replace(parts[1], "();");
        				}
        			}
//        				newLine = line.replace(parts[1], "();");
        		}
        		if(line.contains("execute(")) {
        			String []parts = line.split("execute(");
        			if(parts.length ==2) {
        				if(parts[0].trim().startsWith("try") ) {
        					String end="";
        					if(parts[1].trim().endsWith("{"))
        						end = " {";
        					newLine = line.replace(parts[1], "))") + end;
        				}else {
        				newLine = line.replace(parts[1], ");");
        				}
        			}
        				//newLine = line.replace(parts[1], ");");
        		}
        		if(print) System.out.print("Delete, BinaryOperator : ");
        		if(curr != next ) {//!newLine.startsWith("-")
            		newLine = "- " + line + "\n+ "+ newLine;
            	}else {
            		newLine = "- " + line + "\n"+ newLine;
            	}
        		if(print) System.out.println("Delete, BinaryOperator: "+newLine);
        	}
        	
        	
//        	if(curr != next ) {//!newLine.startsWith("-")
//        		newLine = "- " + line + "\n+ "+ newLine;
//        	}else {
//        		newLine = "- " + line + "\n"+ newLine;
//        	}
        }//================ End of Delete
        
        if(curr != next) {
        	//clean the line.
//        	if(newLine.contains(tmpKey)) {
//        		newLine = newLine.replace(tmpKey,"");
//        	}
        	if(newLine != null) {
        	if(!newLine.startsWith("+") && newLine != ""  && !newLine.startsWith("-") )
        	newLine = "+ " +newLine;
        	}
        		
//        	TODO: Handle "query_with_replace_calls", "cleaned_query"
        }
        if(print) System.out.println(newLine);
        return newLine;
    }

  
    public static void operationTree(Operation op) {
        ITree x = op.getAction().getNode();

        CtElement element = op.getSrcNode();

//        CtElement element = node;
        if (element != null) {
        }
        //int pos;
        if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
//            pos = element.getPosition().getSourceEnd();
            System.out.println("getColumn = " + element.getPosition().getColumn());

            System.out.println("getEndColumn = " + element.getPosition().getEndColumn());

            System.out.println("getEndLine = " + element.getPosition().getEndLine());

            System.out.println("getLine = " + element.getPosition().getLine());

            System.out.println("getSourceEnd = " + element.getPosition().getSourceEnd());
            System.out.println("getSourceStart = " + element.getPosition().getSourceStart());

        }
    }
public static void fixSqlVul(List<Operation> actions){//, ArrayList<String> context){//recieve context
    
    System.out.println("## Actions ("+ actions.size()+")");
//        System.out.println("## Actions :"+ actions.toString());
//       System.out.println("## context:"+ context.toString());
        for (Operation o : actions) {
            System.out.println("--------------------\n");//+o);
//            o.getAction().
            List<String> parts = operationParts(o);
            System.out.println(parts);
            
        }

}

public static List<String> operationParts(Operation op) {
//        op.getSrcNode().get
    
        List<String> parts = new ArrayList<String>();
        CtElement node = op.getNode();
        Action action = op.getAction();

        String newline = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();

        // action name
//        System.out.print("\nAction Name: ");
        stringBuilder.append(action.getClass().getSimpleName());
        parts.add(action.getClass().getSimpleName());
        /*++++*/
//        System.out.print(action.getClass().getSimpleName());
        CtElement element = node; 

        if (element == null) {
            // some elements are only in the gumtree for having a clean diff but not in the Spoon metamodel
            /*++++*/
//            System.out.print(stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")");
            //return stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")";
            return null;
        }

        // node type
//        System.out.print("\nNode Type: ");
        String nodeType = element.getClass().getSimpleName();
        
        nodeType = nodeType.substring(2, nodeType.length() - 4);
        stringBuilder.append(" ").append(nodeType);
        parts.add(nodeType);//==============
        /*++++*/
//        System.out.print(nodeType);

        // action position
//        System.out.print("\nAction Position: ");
        CtElement parent = element;
        while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
            parent = parent.getParent();
        }
        String position = " at ";
        if (parent instanceof CtType) {
            position += ((CtType) parent).getQualifiedName();
        }
        if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
            position += ":" + element.getPosition().getLine();
            
            parts.add(""+element.getPosition().getLine());//==============
        } else {
//            System.out.println("#### NO POS!\n");
            parts.add("No pos");
        }
        if (action instanceof Move) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            position = " from " + element.getParent(CtClass.class).getQualifiedName();
            if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
                position += ":" + element.getPosition().getLine();
            }
            position += " to " + elementDest.getParent(CtClass.class).getQualifiedName();
            if (elementDest.getPosition() != null && !(elementDest.getPosition() instanceof NoSourcePosition)) {
                position += ":" + elementDest.getPosition().getLine();
            }
        }
        stringBuilder.append(position).append(newline);
        /*++++*/
//        System.out.print(position + newline);
        // code change
//        System.out.print("\nCode Change: ");
        String label = partialElementPrint(element);
        if (action instanceof Move) {
            label = element.toString();
        }
        if (action instanceof Update) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            label += " to " + elementDest.toString();
        }
        String[] split = label.split(newline);
        for (String s : split) {
            parts.add(s);
            stringBuilder.append("\t").append(s).append(newline);
            /*++++*/
//            System.out.print("\t" + s + newline);
        }
        /*++++*/
//        System.out.print(stringBuilder.toString());
        return parts;
    }

public static int PrintOpPartsAndGetPos(Operation op, boolean print) {
//        op.getSrcNode().get
    int pos =0;
        CtElement node = op.getNode();
        Action action = op.getAction();

        String newline = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();

        // action name
        if(print) System.out.print("\nAction Name: ");
        stringBuilder.append(action.getClass().getSimpleName());
        /*++++*/
        if(print) System.out.print(action.getClass().getSimpleName());
        CtElement element = node; 

        if (element == null) {
            // some elements are only in the gumtree for having a clean diff but not in the Spoon metamodel
            /*++++*/
            if(print) System.out.print(stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")");
            //return stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")";
            return pos;
        }

        // node type
        if(print) System.out.print("\nNode Type: ");
        String nodeType = element.getClass().getSimpleName();
        nodeType = nodeType.substring(2, nodeType.length() - 4);
        stringBuilder.append(" ").append(nodeType);
        /*++++*/
        if(print) System.out.print(nodeType);

        // action position
        if(print) System.out.print("\nAction Position: ");
        CtElement parent = element;
        while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
            parent = parent.getParent();
        }
        String position = " at ";
        if (parent instanceof CtType) {
            position += ((CtType) parent).getQualifiedName();//class name
        }
        if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
            position += ":" + element.getPosition().getLine();//line number
            pos = element.getPosition().getLine();
        } else {
            if(print) System.out.println("#### NO POS!\n");
        }
        if (action instanceof Move) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            position = " from " + element.getParent(CtClass.class).getQualifiedName();
            if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
                position += ":" + element.getPosition().getLine();
                pos = element.getPosition().getLine();
            }
            position += " to " + elementDest.getParent(CtClass.class).getQualifiedName();
            if (elementDest.getPosition() != null && !(elementDest.getPosition() instanceof NoSourcePosition)) {
                position += ":" + elementDest.getPosition().getLine();
                pos = elementDest.getPosition().getLine();
            }
        }
        stringBuilder.append(position).append(newline);
        /*++++*/
        if(print) System.out.print(position + newline);
        // code change
        if(print) System.out.print("\nCode Change: ");
        String label = partialElementPrint(element);
        if (action instanceof Move) {
            label = element.toString();
        }
        if (action instanceof Update) {
            /*+++++*/
            CtElement elementSrc = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
            label += " @@@ from " + elementSrc.toString();//element.getParent(CtClass.class).getQualifiedName();
            
            /*+++++*/
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            label += " to " + elementDest.toString();
        }
        String[] split = label.split(newline);
        for (String s : split) {
            stringBuilder.append("\t").append(s).append(newline);
            /*++++*/
            if(print) System.out.print( s + newline);
//            if(print) System.out.print(" s=" + s + ", newline= "+newline);
        }
        
        /*++++*/
        if(print) System.out.print(stringBuilder.toString());
        return pos;
    }
    

public static ActionParts GetOpParts(Operation op, boolean print) {
	ActionParts ap = new ActionParts();
//  op.getSrcNode().get
	int pos;
  CtElement node = op.getNode();
  Action action = op.getAction();

  String newline = System.getProperty("line.separator");
  StringBuilder stringBuilder = new StringBuilder();

  // action name
  if(print) System.out.print("\nAction Name: ");
  stringBuilder.append(action.getClass().getSimpleName());
  /*++++*/
  String an = action.getClass().getSimpleName();
  if(print) System.out.print(an);
  ap.actionName = an;
  CtElement element = node; 

  if (element == null) {
      // some elements are only in the gumtree for having a clean diff but not in the Spoon metamodel
      /*++++*/
      if(print) System.out.print(stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")");
      //return stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")";
      return null;
  }

  // node type
  if(print) System.out.print("\nNode Type: ");
  String nodeType = element.getClass().getSimpleName();
  nodeType = nodeType.substring(2, nodeType.length() - 4);
  stringBuilder.append(" ").append(nodeType);
  ap.nodeType = nodeType;
  /*++++*/
  if(print) System.out.print(nodeType);

  // action position
  if(print) System.out.print("\nAction Position: ");
  CtElement parent = element;
  while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
      parent = parent.getParent();
  }
  String position = " at ";
  if (parent instanceof CtType) {
      position += ((CtType) parent).getQualifiedName();//class name
  }
  if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
      position += ":" + element.getPosition().getLine();//line number
      pos = element.getPosition().getLine();
      ap.pos = pos;
  } else {
      if(print) System.out.println("#### NO POS!\n");
  }
  if (action instanceof Move) {
      CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
      position = " from " + element.getParent(CtClass.class).getQualifiedName();
      if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
          position += ":" + element.getPosition().getLine();
          pos = element.getPosition().getLine();
          ap.pos = pos;
      }
      position += " to " + elementDest.getParent(CtClass.class).getQualifiedName();
      if (elementDest.getPosition() != null && !(elementDest.getPosition() instanceof NoSourcePosition)) {
          position += ":" + elementDest.getPosition().getLine();
          pos = elementDest.getPosition().getLine();
          ap.pos = pos;
      }
  }
  stringBuilder.append(position).append(newline);
  /*++++*/
  if(print) System.out.print(position + newline);
  // code change
  if(print) System.out.print("\nCode Change: ");
  String label = partialElementPrint(element);
  if (action instanceof Move) {
      label = element.toString();
      ap.codeChange = label; // Move code change
  }
  if (action instanceof Update) {
      /*+++++*/
      CtElement elementSrc = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
      label += " @@@ from " + elementSrc.toString();//element.getParent(CtClass.class).getQualifiedName();
      ap.ccFrom = elementSrc.toString();// update from code change
      /*+++++*/
      CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
      label += " to " + elementDest.toString();
      ap.ccTo = elementDest.toString();// update to code change
  }
  String[] split = label.split(newline);
  for (String s : split) {
      stringBuilder.append("\t").append(s).append(newline);
      /*++++*/
      if(print) System.out.print( s + newline);
      ap.codeChange = s;// Insert & Delete code change
//      if(print) System.out.print(" s=" + s + ", newline= "+newline);
  }
  
  /*++++*/
  if(print) System.out.print(stringBuilder.toString());
  return ap;
}

public static void operationPartsPrint(Operation op) {
//        op.getSrcNode().get
        CtElement node = op.getNode();
        Action action = op.getAction();

        String newline = System.getProperty("line.separator");
        StringBuilder stringBuilder = new StringBuilder();

        // action name
        System.out.print("\nAction Name: ");
        stringBuilder.append(action.getClass().getSimpleName());
        /*++++*/
        System.out.print(action.getClass().getSimpleName());
        CtElement element = node; 

        if (element == null) {
            // some elements are only in the gumtree for having a clean diff but not in the Spoon metamodel
            /*++++*/
            System.out.print(stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")");
            //return stringBuilder.toString() + " fake_node(" + action.getNode().getMetadata("type") + ")";
            return;
        }

        // node type
        System.out.print("\nNode Type: ");
        String nodeType = element.getClass().getSimpleName();
        nodeType = nodeType.substring(2, nodeType.length() - 4);
        stringBuilder.append(" ").append(nodeType);
        /*++++*/
        System.out.print(nodeType);

        // action position
        System.out.print("\nAction Position: ");
        CtElement parent = element;
        while (parent.getParent() != null && !(parent.getParent() instanceof CtPackage)) {
            parent = parent.getParent();
        }
        String position = " at ";
        if (parent instanceof CtType) {
            position += ((CtType) parent).getQualifiedName();
        }
        if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
            position += ":" + element.getPosition().getLine();//TODO \ group by line number
        } else {
            System.out.println("#### NO POS!\n");
        }
        if (action instanceof Move) {
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            position = " from " + element.getParent(CtClass.class).getQualifiedName();
            if (element.getPosition() != null && !(element.getPosition() instanceof NoSourcePosition)) {
                position += ":" + element.getPosition().getLine();
            }
            position += " to " + elementDest.getParent(CtClass.class).getQualifiedName();
            if (elementDest.getPosition() != null && !(elementDest.getPosition() instanceof NoSourcePosition)) {
                position += ":" + elementDest.getPosition().getLine();
            }
        }
        stringBuilder.append(position).append(newline);
        /*++++*/
        System.out.print(position + newline);
        // code change
        System.out.print("\nCode Change: ");
        String label = partialElementPrint(element);
        if (action instanceof Move) {
            label = element.toString();
        }
        if (action instanceof Update) {
            /*+++++*/
            CtElement elementSrc = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
            label += " @@@ from " + elementSrc.toString();//element.getParent(CtClass.class).getQualifiedName();
            
            /*+++++*/
            CtElement elementDest = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT_DEST);
            label += " to " + elementDest.toString();
        }
        String[] split = label.split(newline);
        for (String s : split) {
            stringBuilder.append("\t").append(s).append(newline);
            /*++++*/
            System.out.print("\t" + s + newline);
        }
        /*++++*/
        System.out.print(stringBuilder.toString());
    }

    public static void main(String[] args) throws Exception {
        // TODO code application logic here
//        gumtree.spoon.AstComparator
        AstComparator diff = new AstComparator();
        
//        ArrayList<String> context = new ArrayList<String>(); 
//        //0,69,72,74,0
//        context.add("0");
//        context.add("69");
//        context.add("72");
//        context.add("74");
//        context.add("0");
        
        
        
        
        VulMD vmd = new VulMD("q,s,[v]", "SQLIV");
        System.out.println("-------------------------");
        String vslPath = vmd.get_v_sl_path();
        String sslPath = vmd.get_s_sl_path();
        System.out.println("vslPath: ("+ vslPath + ")"+"\nsslPath: (" + sslPath +")");
        String vconPath = vmd.get_v_con_path();
        vmd.get_v_con_data();
        ArrayList<String> sl_lines = vmd.get_v_sl_code_lines();
        int i=1;
        for (String l : sl_lines){
            System.out.println(i +" # "+l);
            i++;
        }
        System.out.println(vconPath+"\n-------------------------");
        ArrayList<String> src_lines = vmd.get_v_src_code_lines();
//        int j=1;
//        for (String l : src_lines){
//            System.out.println(j +" # "+l);
//            j++;
//        }
        System.out.println(vconPath+"\n-------------------------");
        File f1 = new File(vslPath);
        File f2 = new File(sslPath);
        
        
        
//        File f1 = new File( "/Users/dpc100/Fixer/Experiments/TrainingCases/example_2/slices/test1_vul.java");
//        File f2 = new File("/Users/dpc100/Fixer/Experiments/TrainingCases/example_2/slices/test1.java");
//        File f1 = new File( "/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/CV1_vul.java";
//        File f2 = new File("/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/CV1_01.java";

//        File f1 = new File("/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS.java");
//        File f2 = new File("/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS_withFin_and_stmt.java");

        //"/Users/dpc100/NetBeansProjects/smallBank/src/smallbank/diff_examples/PS_NoFin.java
        Diff result = diff.compare(f1, f2); //diffs to transform  f1 to be like f2
        
//                Diff result = diff.compare(f2, f1);
        // ===================== Another way to find the diff:
//        CtType f1_sat = getCtType(f1);
//        CtType f2_sat = getCtType(f2);
//        Diff result2 = new AstComparator().compare(f1_sat, f2_sat);
//         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!\n"+f1_sat.getAllMethods());
//        String path = f1_sat.getPosition().getFile().getAbsolutePath();
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!\n"+path);
        // ===================== 
        
        List<Operation> actions = result.getRootOperations();
//        List<Operation> actions = result.getAllOperations();
        
        System.out.println("## Actions ("+ actions.size()+")");

        for (Operation o : actions) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");//+o);
            operationPartsPrint(o);
//            operationTree(o);
//           System.out.println("######### dst: "+o.getDstNode().toString());
//           System.out.println("$$$$$$$$$ src: "+o.getSrcNode().toString());
        }
       // Operation o9 = new Operation();
        
        fixSqlVul( actions);//, context);
        
        EBSet.EB_selector(actions, vmd, "q_s_v", 1, 1);
        System.out.println("&&&&&&\n");
        
//        actions.add(null);
       // TreeUtils.
//        System.out.print("\n===============\n");
//        System.out.print(actions.get(1).toString());
//        System.out.print("@@@@@\n" );
//        operationPartsPrint(actions.get(1));
        //===========================
//        CtElement element = actions.get(1).getNode();
//        if (element == null) {
//            System.out.print("fake node");
//        }

//        System.out.print(actions.get(1));
        //AstComparator x = (AstComparator) 
//        System.out.print(new AstComparator().compare(el1, el2));
//        AstComparator.main(new String[] { el1.getAbsolutePath(), el2.getAbsolutePath() });
//        System.out.print(new AstComparator().main(new String[]{el1.getAbsolutePath(), el2.getAbsolutePath()}));
        //.compare((CtElement) el1, (CtElement) el2);
//        gumtree.spoon.AstComparator;//<> <>;
        //final Diff result = new AstComparator().compare(f1, f2);
        CtElement ancestor = result.commonAncestor();
        Factory factory = (Factory) ancestor.getFactory();
        Environment env = factory.getEnvironment();
        env.setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(env));

        //attach ChangeCollector, an Exception will be throwed
        ChangeCollector changeCollector = new ChangeCollector();
        changeCollector.attachTo(env);

        //for my test file, the common ancestor is an instance of CtClass
        CtClass type = ancestor.getParent(CtClass.class);
        if (type == null) {
            if (CtClass.class.isAssignableFrom(ancestor.getClass())) {
                type = (CtClass) ancestor;
            }
        }

        //TODO: the replace part here-----------------
//        CtClass type5 = result.commonAncestor().getParent(CtClass.class);
//        List<Operation> opertations = result.getAllOperations();
//        for (Operation op : opertations) {
//            op.getSrcNode().replace(op.getDstNode());
//            
//        }

        //--------------------------------------------
        Launcher launcher = new Launcher(factory);
        PrettyPrinter printer = launcher.getEnvironment().createPrettyPrinter();

        List typeList = Arrays.asList(type);

        //If type is null,  an Exception will be throwed
        printer.calculate(factory.CompilationUnit().getOrCreate(type), typeList);

        String output = printer.getResult();

        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println(output);
    }

}


//System.out.println(str.replaceFirst(".$",""));//remove last letter
//System.out.println(str.substring(1));//remove first letter
