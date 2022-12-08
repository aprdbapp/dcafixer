package patchgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import slicer.utilities.StrUtil;

public class RplacePHs {
	static boolean Print = true;
	static final String conn_ph = "conn_dcafixer";
	static final String rs_ph = "rs_dcafixer";
	static final String sql_ph = "sql_dcafixer";
	static final String ps_ph = "pstmt_dcafixer";
	static final String ps_cq_ph = "\"cleaned_query\"";
	static final String wl_cq_ph = "\"query_with_replace_calls\"";
	static final String sets_ph = "pstmt_dcafixer.setObject(1, \"\");";
//	String setObjectph = "pstmt_dcafixer.setObject(1, \"\")";
	static String app_conn;
	static String app_rs;
	static String app_sql;
//	static String app_ps = "";

	public static void setAppVars(String c, String r, String s) {
		app_conn = c;
		app_rs = r;
		app_sql = s;
	}

	public static void clearAppVars() {
		app_conn = "";
		app_rs = "";
		app_sql = "";
	}

	public static String replacePlaceHolders_str(String lines) {

		return lines.replaceAll(conn_ph, app_conn).replaceAll(rs_ph, app_rs).replaceAll(sql_ph, app_sql);
		// .replaceAll(ps_ph, app_ps)
	}

//	public static String read_lines(String path) throws IOException {
//		String lines = "";
//		BufferedReader reader = null;
//		File map_file = new File(path);
//		if (map_file.exists()) {
//			reader = new BufferedReader(new FileReader(map_file));
//			String line = reader.readLine();
//			boolean firstLine = true;
//			while (line != null) {
//				if(line.contains("createStatement") )//delete the line from the slice.
//					continue;
//				if (firstLine) {
//					lines = lines + line;
//					firstLine = false;
//				} else {
//					lines = lines + "\n" + line;
//				}
//				line = reader.readLine();
//			}
//			reader.close();
//		}
//
//		return lines;
//
//	}

	public static void replacePlaceHolders(String vsPath, String slicesPath, String c, String r, String s, boolean p) throws IOException {
		Print = p;
		// Given the names of PHs.
		setAppVars(c, r, s);
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
				String newSSlice = replacePlaceHolders_str(SSlice);
				if (Print)
					System.out.println("==== After:\n" + newSSlice);
				StrUtil.write_tofile(slicesPath + "/tmp/" + contents[i], newSSlice);
//				File f = new File(slicesPath + "/tmp/" + contents[i]);
//				if (!f.exists())
//					f.getParentFile().mkdir();
////                    StrUtil.write_tofile();
//				FileWriter myWriter = new FileWriter(f);
//
//				myWriter.write(newSSlice);
//				myWriter.close();
			}

		}
		clearAppVars();
	}

	public static void main(String[] args) throws IOException {
		String vs = "";
		String slicespath = "/Users/Dareen/Fixer/tmp/TSet/Slices/SQLIV/q_s_v/SSlices/";
	
		replacePlaceHolders(vs, slicespath, "conn", "rs", "sql", false);

	}

}
