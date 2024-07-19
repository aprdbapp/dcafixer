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
	public static final String methodArg = "MethodArg";
	
	public static final String ResultsPath = "out/dcafixer-report/";//"/Users/dareen/Fixer/DCAFixer_Experimets/Results/";
	public static final String CsvDir= "out/dcafixer-report/";//"/Users/dareen/Fixer/DCAFixer_Experimets/Excel_files/";
	
	public static final String projectsPath = "/Users/dareen/Fixer/DCAFixer_Experimets/SQLIFix-projects/";
	//public static final String NBProjectsPath = "/Users/dareen/NetBeansProjects/";
	
	public static final String projectsJarsPath = "/Users/dareen/Fixer/Experiments/JARS/";
	//"/Users/dareen/Fixer/tmp/TSet/Slices/" to "patterns/"
	public static final String SQLI_VSlicesPath = "patterns/SQLIV/Slices/vSlices";
	public static final String SQLI_TmpVSlicesPath = "patterns/SQLIV/Slices/tmp";
	public static final String SQLI_SSlicesPath = "patterns/SQLIV/Slices/sSlices";
	public static final String SQLI_patchesPath = "patterns/SQLIV/Slices/patches";//PS
	public static final String SQLI_patchesPath2 = "patterns/SQLIV/Slices/patches2";//WL
	
	public static final String CONN_VSlicesPath = "patterns/CONN/Slices/vSlices";
	public static final String CONN_TmpVSlicesPath = "patterns/CONN/Slices/tmp";
	public static final String CONN_SSlicesPath = "patterns/CONN/Slices/sSlices";
	public static final String CONN_patchesPath = "patterns/CONN/Slices/patchesHC";//HC
	public static final String CONN_patchesPath2 = "patterns/CONN/Slices/patchesDT";//Data type!
	
	
	public static final String testTmpPath = "out";
	public static final String slicesTmpPath = "out/dcafixer-report/";//"/Users/dareen/Fixer/DCAFixer_Experimets/Slices/";
	public static final String QSV = "Q,s,[v]";
	public static final String QIV = "Q,i,[v]";
	public static final String QUV = "Q,u,[v]";
	public static final String QDV = "Q,d,[v]";
	public static final String Q_V = "Q,_,[v]";
	
	//=========================== Messages ===================
	public static final String MSG_SyntaxError = "SQL Syntax Error! ";
	public static final String SyntaxError_tc_in_PS = "PS can't have table/column name as user input. ";
	public static final String SyntaxError_quey_wrong_fun = "Using wrong function with SQL command ..";
	public static final String MSG_WLWarning = "WL is used. However, It's better to use PS. To use PS, tables and columns name should be constant";
	public static final String MSG_UsePS_all = "Giving this power to the user is dangouris. It's better to use PS to avoid SQLIA";
	public static final String MSG_PSSol ="PS is the solution here. CV is user input";
	public static final String MSG_NoSol = "DCAFixer can't fix this vulnerability. ";
	public static final String MSG_ct = "Column/table name AND column value are user input! ";
	public static final String MSG_WLSol = "WL is the solution here. PS can't be used because table/column name is/are user input.";
	public static final String MSG_P_HD= "Password is (1) hardcoded and (2) datatype (String). This is not safe! "
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
		
	public static final String MSG_Conn_noEnc="You disabled encryption in the URL. This is not a secure practice. You should enable encryption by setting the SSL value to true!";
	
	
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
	public static List<Integer> AppLines_partlyFixedNos = new ArrayList<>();
	public static int LastLineModified = 0;
	public static final int vul_and_fixedAppshaveSynErr = -2;
	public static final int vulApphasSynErr = -1;
	public static final int fixedApphasSynErr = 1;
	public static final int noSynErr = 0;
	
	//=================================
	
}
