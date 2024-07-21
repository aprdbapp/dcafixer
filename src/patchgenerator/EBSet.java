/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patchgenerator;

import static astgumtree.ASTGumtree.operationParts;

import java.util.ArrayList;
import java.util.List;

import gumtree.spoon.diff.operations.Operation;

/**
 *
 * @author Dareen
 */
public class EBSet {
//    Update = + & -
//    get data from context file
//[Update, Invocation, 2, conn.createStatement() to conn.prepareStatement(sql)]

//[Delete, FieldRead, 4, sql]
//[Delete, Assignment, 3, sql = ("Select * from ACCOUNTS where custid = " + colval) + ";"]
//[Delete, LocalVariable, 4,  rs = ]
//--------------------
//
//[Insert, Invocation, 3, stmt.setString(?, ?)]
//--------------------
//
//[Insert, TryWithResource, 4, try ( rs = ) {, }]
//--------------------
//
//[Move, TypeReference, 4, ResultSet]
//--------------------
//
//[Move, Invocation, 4, stmt.executeQuery(sql)]
//
    public static void EB_selector(List<Operation> actions, VulMD vmd, String sig, int EBI, int ps_id) {
        //  - Choose by signature (cluster checking)
        if (sig.equals("q_s_v")) {
            //TODO:
            //  - LCS and based on it choose EBI
            EB_q_s_v(actions, vmd, ps_id);
        }//Other signatures

    }

    public static void EB_q_s_v(List<Operation> actions, VulMD vmd , int ps_id) {
        int EQln, PSln, SQLln;
        String CONN ="", cleaned_query ="", SQLVar ="";
        SQLln = -1;
        EQln = -1;

        ArrayList<String> sl_lines = vmd.get_v_sl_code_lines();
        int li = 0;
        for (String sl : sl_lines) {
            li++;
            if (sl.contains("createStatement")) {
                System.out.println(sl);
                String CS = sl.substring(sl.lastIndexOf('=') + 1);
                String[] strs = CS.trim().split("createStatement");
                CONN = strs[0]; //TODO: check inlining
//
            }

            if (sl.contains("executeQuery")) {
                EQln = vmd.map_sllno_to_srcln(li);
            }

        }
        for (Operation o : actions) {
            String operatin, opType, stmtPart;
            int lno;
            List<String> parts = operationParts(o);
            operatin = parts.get(0);
            opType = parts.get(1);
            lno = Integer.parseInt(parts.get(2).trim());
            stmtPart = parts.get(3);

            if (operatin.equals("Delete") && opType.equals("Assignment")) {
//                String stmt = stmtPart.replace("(", "").replace(")", "").trim();
                if (stmtPart.toLowerCase().contains("select") || stmtPart.toLowerCase().contains("insert")
                        || stmtPart.toLowerCase().contains("delete") || stmtPart.toLowerCase().contains("update")) {
                    //It's the sql stmt. Look for
                    SQLln = vmd.map_sllno_to_srcln(lno);
                }
                // get cleaned query
                cleaned_query= "?";
            }
            if (sl_lines.get(lno - 1).contains("executeQuery")) {
                //check if sql is passed as an argument to "executeQuery"
                if (operatin.equals("Delete") && opType.equals("BinaryOperator")) {
                    if (stmtPart.toLowerCase().contains("select") || stmtPart.toLowerCase().contains("insert")
                            || stmtPart.toLowerCase().contains("delete") || stmtPart.toLowerCase().contains("update")) {
                        SQLln = -1;
                        // get cleaned query
                        cleaned_query= "?";
                    }
                }

                if(operatin.equals("Delete") && opType.equals("FieldRead")){//FieldRead
                    SQLVar = stmtPart.trim();
            }
            }
            ArrayList<String> src_lines = vmd.get_v_src_code_lines();
            ArrayList<String> fixed_src_lines = new ArrayList();
            int lineCounter = 0;
//            for (int i = 0 ; i<src_lines.size(); i++){
//            }
            for(String old_srcl : src_lines ){
                lineCounter++;
                if(lineCounter  == SQLln ){
                    //Don't add this line to the fixed code
                    continue;
                }
                if(lineCounter == EQln){
                    //Add PS & setString list;
                    String ps_var = "pstmt_"+ps_id;
                    if(SQLln == -1){
                        fixed_src_lines.add("PreparedStatement "+ps_var+" = "+CONN+".prepareStatement("+cleaned_query+");");
                    }else{
                        fixed_src_lines.add(SQLVar + " = " + cleaned_query+";");
                        fixed_src_lines.add("PreparedStatement "+ps_var+" = "+CONN+".prepareStatement("+SQLVar+");");
                        //loop over setStrings:
                        {int i=1;
                        String cvname ="";
                        //stmt.setString(1, colval);
                        fixed_src_lines.add(ps_var + ".setString("+i+", "+ cvname+");");
                        i++;}
                    }
                }
                fixed_src_lines.add(old_srcl);

            }

//            System.out.print("[0]" + + parts.get(0));
//            System.out.print("[1]" + parts.get(1));
//            System.out.print("[2]" + parts.get(2));
//            System.out.print("[3]" + parts.get(3) + "\n\n");
//            System.out.println();
        }

    }
}
