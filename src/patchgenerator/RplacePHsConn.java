package patchgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import flocalization.G;
import slicer.tool.Info;
import slicer.utilities.StrUtil;

//conn_dcafixer*
//properties_dcafixer*
//username_dcafixer*
//password_dcafixer*
//url_dcafixer*
//"DCAFixer_un_litral"*
//"DCAFixer_pw_litral"*
//prop_url_dcafixer*
//prop_un_dcafixer*
//prop_pw_dcafixer*
//MethodCallExpr_un*
//MethodCallExpr_pw*
//scanner_dcafixer
public class RplacePHsConn {
	static boolean Print = true;
	static final String conn_ph = "conn_dcafixer";
	static final String prop_ph = "properties_dcafixer";
	static final String un_ph = "username_dcafixer";
	static final String pw_ph = "password_dcafixer";
	static final String stmt_ph = "stmt_dcafixer";
	static final String url_ph = "url_dcafixer";
	static final String un_lit_ph ="\"DCAFixer_un_litral\"";
	static final String pw_lit_ph ="\"DCAFixer_pw_litral\"";
	static final String mcall_un_ph = "MethodCallExpr_un";
	static final String mcall_pw_ph = "MethodCallExpr_pw";
	static final String scanner_ph = "scanner_dcafixer";
//	//TODO: find these values to fix the prop case
//	static final String prop_url_ph = "prop_url_dcafixer"; //= url_ph
//	static final String prop_un_ph ="prop_un_dcafixer"; // = un_ph
//	static final String prop_pw_ph ="prop_pw_dcafixer";// = pw_ph

	static String app_conn;
	static String app_prop;
	static String app_un;
	static String app_pw;
	static  String app_un_lit;// ="\"DCAFixer_un_litral\"";
	static  String app_pw_lit;// ="\"DCAFixer_pw_litral\"";
	static String app_stmt;
	static String app_url;
//	static String app_prop_url;
//	static String app_prop_un;
//	static String app_prop_pw;
	static String app_mcall_un;
	static String app_mcall_pw;
	


	public static void setAppVars_info(Info info) {
		app_conn = info.getApp_conn();
//		info.toString_conn();
		//System.out.print("app_conn ---:" + app_conn);
		app_prop = info.getConn_prop();
		app_un = info.getConn_user_var();
		app_pw = info.getConn_pass_var();
		app_un_lit = info.getConn_user_value();
		app_pw_lit = info.getConn_pass_value();
		app_stmt = info.getStmtVar();
		if(info.getConn_url_var()!= null && info.getConn_url_var().length()>0)
			app_url = info.getConn_url_var();
		else
			app_url = info.getConn_url_value();
		app_mcall_un = info.getConn_user_value_type();
		app_mcall_pw = info.getConn_pass_value_type();
//		app_prop_url= info.??
//		app_prop_un= info.??
//		app_prop_pw= info.??

	}
	
	public static void clearAppVars() {
		app_conn= "";//
		app_prop= "";//
		app_stmt= "";//
		app_un= "";//
		app_pw= "";//
		app_url= "";//
		app_mcall_un= "";
		app_mcall_pw= "";
//		app_prop_url= "";
//		app_prop_un= "";
//		app_prop_pw= "";

	}

	public static String replacePlaceHolders_str(String lines) {

		if (!app_conn.equals(""))
			lines = lines.replaceAll(conn_ph, app_conn);
		if (!app_prop.equals(""))
			lines = lines.replaceAll(prop_ph, app_prop);
		if (!app_stmt.equals(""))
			lines = lines.replaceAll(stmt_ph, app_stmt);
		if (!app_un.equals("")) 
			lines = lines.replaceAll(un_ph, app_un);
		if(app_mcall_un.equals("MethodCallExpr")) 
			lines = lines.replaceAll(mcall_un_ph, app_un);//if(app_mcall_un.equals("MethodCallExpr")) lines = lines.replaceAll(mcall_un_ph, "("+app_un+").toCharArray()");
		if (!app_pw.equals(""))
			lines = lines.replaceAll(pw_ph, app_pw);
		if(app_mcall_pw.equals("MethodCallExpr")) 
			lines = lines.replaceAll(mcall_pw_ph, app_pw);
		if (!app_url.equals(""))
			lines = lines.replaceAll(url_ph, app_url);
		
		//TODO: replace app_prop_.. values?
		return lines;
	}
	
	public static String replacePlaceHolders_patch(String patchPath, Info info, String set_strings, String cleanQuery) throws IOException {
		setAppVars_info(info);
		String patchlines = StrUtil.read_lines(patchPath);
		patchlines = replacePlaceHolders_str(patchlines);
		return patchlines;
		
	}

	public static void replacePlaceHolders_and_createTmpVSlices(String slicesPath, Info info,  boolean print) throws IOException {
		//TODO: test!!
		// Given the names of PHs.
		setAppVars_info(info);
		// Read files from slicesPath
		if(print)	
			System.out.println("SlicesPath: "+slicesPath );
		File directoryPath = new File(slicesPath);
		String[] contents = directoryPath.list();
		// Replace PHs & Write to Temp
		for (int i = 0; i < contents.length; i++) {
			// Open file and replace all HPs
			if (contents[i].endsWith(".java")) {
				if (print)
					System.out.println("======= " + contents[i] + " =======");
				String vSlice = StrUtil.read_lines(slicesPath + "/" + contents[i]);
				if (print)
					System.out.println("==== Before:\n" + vSlice);
				String newVSlice = replacePlaceHolders_str(vSlice);
				if (print)
					System.out.println("==== After:\n" + newVSlice);
				//StrUtil.write_tofile(slicesPath + "/tmp/" + contents[i], newVSlice);
				//StrUtil.write_tofile( G.SQLI_TmpVSlicesPath + "/" + contents[i], newVSlice);
				StrUtil.write_tofile( G.CONN_TmpVSlicesPath + "/" + contents[i], newVSlice);
			}

		}
		clearAppVars();
	}

	
	
	public static void replacePlaceHolders_patch_info( String slicesPath, Info info, String set_strings, String cleanQuery, boolean p) throws IOException {
		Print = p;
		// Given the names of PHs.

		setAppVars_info(info);
		// Read files from slicesPath
		System.out.println("slicesPath: "+slicesPath );
		File directoryPath = new File(slicesPath);
		String[] contents = directoryPath.list();
		// Replace PHs & Write to Temp
		for (int i = 0; i < contents.length; i++) {
			// Open file and replace all HPs
			if (contents[i].endsWith(".java")) {
				if (Print)
					System.out.println("======= " + contents[i] + " =======");

				String SSlice = StrUtil.read_lines(slicesPath + "/" + contents[i]);
				if (Print)
					System.out.println("==== Before:\n" + SSlice);
				String newSSlice = replacePlaceHolders_patch(SSlice,info, set_strings, cleanQuery);
				if (Print)
					System.out.println("==== After:\n" + newSSlice);
				StrUtil.write_tofile(slicesPath + "/tmp/" + contents[i], newSSlice);
			}

		}
		clearAppVars();
	}


}
