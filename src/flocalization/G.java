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
	public static final String constantValue = "StringLiteralExpr";
	public static final String encrSetting = "ssl=false";
	public static final String JarPath = "/Users/Dareen/Fixer/Experiments/TrainingCases/smallbank.jar";
	public static final String SrcPath ="/Users/Dareen/NetBeansProjects/smallBank/src/TSet/";
	public static final String SCPath = "/Users/Dareen/Fixer/tmp/TSet/Slices";
	public static final String ResultsPath = "/Users/Dareen/Desktop/DCAFixer_Experimets/Results/";
	public static final String CsvDir= "/Users/Dareen/Desktop/DCAFixer_Experimets/Excel_files/";
	
	public static final String projectsPath =	"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/";
	public static final String NBProjectsPath ="/Users/Dareen/NetBeansProjects/";
	
	public static final String projectsJarsPath = "/Users/Dareen/Fixer/Experiments/JARS/";
	
	public static final String SQLI_VSlicesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/Slices/vSlices";
	public static final String SQLI_TmpVSlicesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/Slices/tmp";
	public static final String SQLI_SSlicesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/Slices/sSlices";
	public static final String SQLI_patchesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/Slices/patches";//PS
	public static final String SQLI_patchesPath2 = "/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/Slices/patches2";//WL
	
	public static final String CONN_VSlicesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/CONN/Slices/vSlices";
	public static final String CONN_TmpVSlicesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/CONN/Slices/tmp";
	public static final String CONN_SSlicesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/CONN/Slices/sSlices";
	public static final String CONN_patchesPath = "/Users/Dareen/Fixer/tmp/TSet/Slices/CONN/Slices/patches";//HC
	public static final String CONN_patchesPath2 = "/Users/Dareen/Fixer/tmp/TSet/Slices/CONN/Slices/patches2";//Data type!
	
	
	public static final String testTmpPath = "/Users/Dareen/tmp";
	public static final String slicesTmpPath = "/Users/Dareen/Desktop/DCAFixer_Experimets/Slices/";
	public static final String sliceTmpPath = "/Users/Dareen/tmp/tmpslice";
	public static final String QSV = "Q,s,[v]";
	public static final String QIV = "Q,i,[v]";
	public static final String QUV = "Q,u,[v]";
	public static final String QDV = "Q,d,[v]";
	public static final String Q_V = "Q,_,[v]";
	public static final String MSG_SyntaxError = "SQL Syntax Error! ";
	public static final String SyntaxError_tc_in_PS = "PS can't have table/column name as user input. ";
	public static final String SyntaxError_quey_wrong_fun = "Using wrong function with SQL command ..";//Add more details about executeUpdate & executeQuery
	public static final String MSG_WLWarning = "WL is used. However, It's better to use PS. To use PS, tables and columns name should be constant";
	public static final String MSG_UsePS_all = "Giving this power to the user is dangouris. It's better to use PS to avoid SQLIA";
	public static final String MSG_PSSol ="PS is the solution here. CV is user input";
	public static final String MSG_NoSol = "DCAFixer can't fix this vulnerability. ";
	public static final String MSG_ct = "Column/table name AND column value are user input! ";
	public static final String MSG_WLSol = "WL is the solution here. PS can't be used because table/column name is/are user input.";
	public static final String MSG_P_HD="Password is (1) hardcoded and (2) datatype (String). This is not safe! "
			+ "\nTo solve (1): Avoid hardcoding database credentials in your Java code or configuration files. Store them securely, such as in environment variables, encrypted files, or a secure credential management system. Retrieve the credentials at runtime and provide them to the connection establishment code."
			+ "\nTo solve (2): Use either char[] or JPasswordField to store that password temporarily.";

	public static final String MSG_P_H="Password is hardcoded. This is not secure!"
			+ "\nTo solve this issue, use either char[] or JPasswordField to store that password temporarily.";
	public static final String MSG_P_D="Password datatype (String) is not safe to store password. "
			+ "\nTo solve this issue, avoid hardcoding database credentials in your Java code or configuration files. Store them securely, such as in environment variables, encrypted files, "
			+ "or a secure credential management system. Retrieve the credentials at runtime and provide them to the connection establishment code.";
	public static final String MSG_U_H="Username is hardcoded. This is not secure!"
			+ "\nTo solve this issue, avoid hardcoding database credentials in your Java code or configuration files. Store them securely, such as in environment variables, encrypted files, "
			+ "or a secure credential management system. Retrieve the credentials at runtime and provide them to the connection establishment code.";
	
	
	public static final String MSG_Conn_noEnc="You are disable encryption in the url. This is not a secure practice. You should enable the encryption by setting ssl value to ture! ";
	
	
	public static final int SEC = 0;
	public static final int SOLPS = 1;
	public static final int SOLWL = 2;
	public static final int NOSOL = 3;
	public static List<String> sqlParserFails = new ArrayList<>();
	public static final int CG_AllEPoints_MyEx = 0;
	public static final int CG_MainEPoints_CGTUtilEx = 1; //new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS)
	public static final int CG_MainEPoints_MyEx = 2;
	public static final int CG_AllEPoints_MyExGUI = 3;
	
	public static final List<String> IPList = new ArrayList<>(Arrays.asList("executeQuery", "executeUpdate", "execute", "getConnection", 
			"createStatement", "Properties", "PreparedStatement"));
	public static final List<String> ConnIPList = new ArrayList<>(Arrays.asList("getConnection", "Properties"));
	public static final List<String> ExIPList = new ArrayList<>(Arrays.asList("executeQuery", "executeUpdate", "execute","execute("));
	public static final List<String> UIPointsList = new ArrayList<>(Arrays.asList("Scanner", "nextLine", "nextFloat", "nextDouble", 
			"nextByte", "nextLine", "nextBoolean", "nextLong", "nextShort", "nextBigInteger", "nextBigDecimal", "BufferedReader", 
			"read", "readLine", "InputStream", "FileInputStream", "read", "JTextField", "getText", "getSelectedText"));
	public static final List<String> QueriesList = new ArrayList<>(Arrays.asList("select", "update", "insert","delete", "create", 
			"drop", "alter", "truncate"));
	public static List<String> AppLines_partlyFixed = new ArrayList<>();
	public static int LastLineModified = 0 ;
	public static final int vul_and_fixedAppshaveSynErr = -2;
	public static final int vulApphasSynErr = -1;
	public static final int fixedApphasSynErr = 1;
	public static final int noSynErr = 0;
	
	
	//TODO: add executeBatch ... etc
//	public static final String QDCV = "q,[c]";
//	public static final String QDCT = "q,[c,t]";
//	public static final String QDCTV = "q,[c,t,v]";
}
