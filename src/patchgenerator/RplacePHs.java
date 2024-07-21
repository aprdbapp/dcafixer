package patchgenerator;

import java.io.File;
import java.io.IOException;

import flocalization.G;
import slicer.tool.Info;
import slicer.utilities.StrUtil;

public class RplacePHs {
	static boolean Print = true;
	static final String conn_ph = "conn_dcafixer";
	static final String rs_ph = "rs_dcafixer";
	static final String sql_var_ph = "sql_dcafixer";
	static final String ps_ph = "pstmt_dcafixer";
	static final String stmt_ph = "stmt_dcafixer";
	static final String ps_cq_ph = "\"cleaned_query\"";
	static final String sql_command_ph = "\"sql_command_dcafixer\"";//++
	static final String wl_cq_ph = "\"query_with_replace_calls\"";
	static final String sets_ph = "setObject_dcafixer";
	static final String executeCall_ph = "executeCall_dcafixer";
	static final String rs_datatype_ph = "ResultType_dcafixer";
//	String setObjectph = "pstmt_dcafixer.setObject(1, \"\")";
	static String app_conn;
	static String app_rs;
	static String app_var_sql;
	static String app_stmt;
	static String app_rsdatatype;
	static String app_executeCall;
//	static String app_ps = "";

	public static void setAppVars(String c, String r, String sql, String stmt, String dt, String exec) {
		app_conn = c;
		app_rs = r;
		app_var_sql = sql;
		app_stmt = stmt;
		app_rsdatatype =dt;
		app_executeCall = exec;
	}

	public static void setAppVars_info(Info info) {
		app_conn = info.getApp_conn();
		app_rs = info.getApp_rs();
		app_var_sql = info.getArg();
		app_stmt = info.getStmtVar();
		app_rsdatatype = info.getApp_rs_dt();
		app_executeCall = info.getKey();
	}

	public static void clearAppVars() {
		app_conn = "";
		app_rs = "";
		app_var_sql = "";
		app_stmt = "";
		app_rsdatatype="";
		app_executeCall="";
	}

	public static String replacePlaceHolders_str(String lines) {

		if (!app_conn.equals("")) {
			lines = lines.replaceAll(conn_ph, app_conn);
		}
		if (!app_rs.equals("")) {
			lines = lines.replaceAll(rs_ph, app_rs);
		}
		if (!app_var_sql.equals("")) {
			lines = lines.replaceAll(sql_var_ph, app_var_sql);
		}
		if (!app_stmt.equals("")) {
			lines = lines.replaceAll(stmt_ph, app_stmt);
		}
		if (!app_executeCall.equals("")) {
			lines = lines.replaceAll(executeCall_ph, app_executeCall);
		}
		if (!app_rsdatatype.equals("")) {
			lines = lines.replaceAll(rs_datatype_ph, app_rsdatatype);
		}
		return lines;
	}

	public static String replacePlaceHolders_patch(String patchPath, Info info, String set_strings, String cleanQuery) throws IOException {
		//TODO: test!!
		setAppVars_info(info);
		String patchlines = StrUtil.read_lines(patchPath);
//		System.out.println("!!!!!!!!!!!!!!!!$$$ Before: \n"+patchlines);
		if (set_strings != null && set_strings.length() > 1) {
			set_strings = set_strings.replaceAll(";;", ";");
			//patchlines = patchlines.replace(sets_ph, set_strings.trim());
			String parts[] = set_strings.trim().split("\\R");
			String newSetStrings = set_strings;
			if(parts.length >1) {
				newSetStrings = "";
				for(String setCall : parts) {

					newSetStrings = newSetStrings + "\n+(cl), "+setCall ;//+";";
				}
			}
			//sets_ph
			patchlines = patchlines.replace(sets_ph, newSetStrings).replace("+(cl), +(cl), ", "+(cl), ");
		}
		if(patchlines.contains(wl_cq_ph)) {
			patchlines = patchlines.replace(wl_cq_ph, cleanQuery);
		} else {
			patchlines = patchlines.replace(sql_command_ph, cleanQuery);
		}

		if (!app_conn.equals("")) {
			patchlines = patchlines.replaceAll(conn_ph, app_conn);// .replaceAll(rs_ph, app_rs).replaceAll(sql_var_ph,
																	// app_var_sql).replaceAll(executeCall_ph,
																	// app_executeCall).replaceAll(rs_datatype_ph,
																	// app_rsdatatype);//.replaceAll(stmt_ph, app_stmt);
		} else {
			patchlines = patchlines.replaceAll(conn_ph, (app_stmt + ".getConnection()"));
		}
//		patchlines = patchlines.replaceAll(rs_ph, app_rs).replaceAll(sql_var_ph, app_var_sql).replaceAll(executeCall_ph, app_executeCall).replaceAll(rs_datatype_ph, app_rsdatatype);
//
		patchlines = patchlines.replaceAll(ps_ph, ("pstmt" + info.getLno()));// generate new "pstmt_dcafixer". Try to test the
																	// used method
		patchlines = replacePlaceHolders_str(patchlines);
//		System.out.println("!!!!!!!!!!!!!!!!$$$After: \n"+patchlines);
		return patchlines;

	}

	public static void replacePlaceHolders_and_createTmpVSlices(String slicesPath, Info info,  boolean print) throws IOException {
		//TODO: test!!
		// Given the names of PHs.
		setAppVars_info(info);
		// Read files from slicesPath
		if(print) {
			System.out.println("SlicesPath: "+slicesPath );
		}
		File directoryPath = new File(slicesPath);
		String[] contents = directoryPath.list();
		// Replace PHs & Write to Temp
		for (String content : contents) {
			// Open file and replace all HPs
			if (content.endsWith(".java")) {
				if (print) {
					System.out.println("======= " + content + " =======");
				}
				String vSlice = StrUtil.read_lines(slicesPath + "/" + content);
				if (print) {
					System.out.println("==== Before:\n" + vSlice);
				}
				String newVSlice = replacePlaceHolders_str(vSlice);
				if (print) {
					System.out.println("==== After:\n" + newVSlice);
				}
				//StrUtil.write_tofile(slicesPath + "/tmp/" + contents[i], newVSlice);
				StrUtil.write_tofile( G.SQLI_TmpVSlicesPath + "/" + content, newVSlice);
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
		for (String content : contents) {
			// Open file and replace all HPs
			if (content.endsWith(".java")) {
				if (Print) {
					System.out.println("======= " + content + " =======");
				}

				String SSlice = StrUtil.read_lines(slicesPath + "/" + content);
				if (Print) {
					System.out.println("==== Before:\n" + SSlice);
				}
				String newSSlice = replacePlaceHolders_patch(SSlice,info, set_strings, cleanQuery);
				if (Print) {
					System.out.println("==== After:\n" + newSSlice);
				}
				StrUtil.write_tofile(slicesPath + "/tmp/" + content, newSSlice);
			}

		}
		clearAppVars();
	}

	public static void replacePlaceHolders(String vsPath, String slicesPath, String c, String r, String sql, String stmt,String dt, String exec,  boolean p) throws IOException {
		Print = p;
		// Given the names of PHs.
		setAppVars(c, r, sql, stmt, dt, exec);
		// Read files from slicesPath
		System.out.println("slicesPath: "+slicesPath );
		File directoryPath = new File(slicesPath);
		String[] contents = directoryPath.list();
		// Replace PHs & Write to Temp
		for (String content : contents) {
			// Open file and replace all HPs
			if (content.endsWith(".java")) {
				if (Print) {
					System.out.println("======= " + content + " =======");
				}

				String SSlice = StrUtil.read_lines(slicesPath + "/" + content);
				if (Print) {
					System.out.println("==== Before:\n" + SSlice);
				}
				String newSSlice = replacePlaceHolders_str(SSlice);
				if (Print) {
					System.out.println("==== After:\n" + newSSlice);
				}
				StrUtil.write_tofile(slicesPath + "/tmp/" + content, newSSlice);
			}

		}
		clearAppVars();
	}

	public static void main(String[] args) throws IOException {
		String vs = "";
		String slicespath = "/Users/dareen/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/";

		//replacePlaceHolders(vs, slicespath, "conn", "rs", "sql", false);

	}

}
