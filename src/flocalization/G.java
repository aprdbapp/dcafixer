package flocalization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.core.util.io.FileProvider;

import net.sf.jsqlparser.JSQLParserException;
import slicer.datatypes.Sig;
import slicer.tool.ExtractQuery;
import slicer.utilities.StrUtil;

public class G {
	public static final String JarPath = "/Users/Dareen/Fixer/Experiments/TrainingCases/smallbank.jar";
	public static final String SrcPath ="/Users/Dareen/NetBeansProjects/smallBank/src/TSet/";
	public static final String SCPath = "/Users/Dareen/Fixer/tmp/TSet/Slices";
	public static final String testTmpPath = "/Users/Dareen/tmp";
	public static final String QSV = "Q,s,[v]";
	public static final String QIV = "Q,i,[v]";
	public static final String QUV = "Q,u,[v]";
	public static final String QDV = "Q,d,[v]";
	public static final String Q_V = "Q,_,[v]";
	public static final String MSG_SyntaxError = "SQL Syntax Error! ";
	public static final String SyntaxError_tc_in_PS = "PS can't have table/column name as user input. ";
	public static final String SyntaxError_quey_wrong_fun = "Using wrong function with SQL command .." ;//Add more details about executeUpdate & executeQuery
	public static final String MSG_WLWarning = "WL is used. However, It's better to use PS. To use PS, tables and columns name should be constant";
	public static final String MSG_UsePS_all = "Giving this power to the user is dangouris. It's better to use PS to avoid SQLIA";
	public static final String MSG_PSSol ="PS is the solution here. CV is user input";
	public static final String MSG_NoSol = "DCAFixer can't fix this vulnerability. ";
	public static final String MSG_ct = "Column/table name AND column value are user input! ";
	public static final String MSG_WLSol = "WL is the solution here. PS can't be used because table/column name is/are user input.";

	public static final int SEC =0;
	public static final int SOLPS =1;
	public static final int SOLWL =2;
	public static final int NOSOL =3;
	public static List<String> sqlParserFails = new ArrayList<>();
	public static final int CG_AllEPoints_MyEx = 0;
	public static final int CG_MainEPoints_CGTUtilEx = 1; //new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS)
	public static final int CG_MainEPoints_MyEx = 2;
	public static final int CG_AllEPoints_MyExGUI = 3;
	
	
	public static final List<String> IPList = new ArrayList<>(Arrays.asList("executeQuery", "executeUpdate", "execute", "getConnection", "createStatement"));
	public static final List<String> UIPointsList = new ArrayList<>(Arrays.asList("", "", "", "", ""));
	//TODO: add executeBatch ... etc
//	public static final String QDCV = "q,[c]";
//	public static final String QDCT = "q,[c,t]";
//	public static final String QDCTV = "q,[c,t,v]";
	
}
