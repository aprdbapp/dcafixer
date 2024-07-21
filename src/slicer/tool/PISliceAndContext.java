package slicer.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import flocalization.SignSlice;
import net.sf.jsqlparser.JSQLParserException;
import slicer.datatypes.Sig;
import slicer.datatypes.SliceLine;
import slicer.utilities.SlicerUtil;
import slicer.utilities.StrUtil;

public class PISliceAndContext {
	public static List<SliceLine> sliceLines = new ArrayList<>();
	public static List<String> setStrings = new ArrayList<>();
	public static String c_slice_srclines = "";
	public static String c_context_srclines = "";
	public static String c_cleaned_query = "";
	public static Sig c_s = new Sig();

	public PISliceAndContext() {

	}
	public  void set_cs(Sig s) {
		c_s = s;
	}
	public  Sig set_cs() {
		return c_s;
	}
	public  void set_setStrings(List<String> sets) {
		setStrings = sets;
	}

	public  List<String> get_setStrings() {
		return setStrings;
	}

	public  void set_slice_srclines(String slines) {
		c_slice_srclines = slines;
	}

	public  String get_slice_srclines() {
		return c_slice_srclines;
	}

	public  void set_context_srclines(String csl) {
		c_context_srclines = csl;
	}

	public  String get_context_srclines() {
		return c_context_srclines;
	}

	public  void set_cleaned_query(String cq) {
		c_cleaned_query = cq;
	}

	public  String get_cleaned_query() {
		return c_cleaned_query;
	}

	public  void set_sig(Sig signature) {
		c_s = signature;
	}

	public  Sig get_sig() {
		return c_s;
	}

	public  String get_sig_str() {
		return c_s.toString();
	}
	// TODO: Write another copy od find_slice_and_context() that returns the slice lines
		public boolean find_slice_and_context(String appJar, String appSrc, String app_folder_className, char sliceDirection, int lno, String seed,
				char sstype, String slicePath, String contextPath, String mdPath,DataDependenceOptions dOptions,
				ControlDependenceOptions cOptions, char appType)
				throws WalaException, CancelException, IOException, InvalidClassFileException, JSQLParserException {
			// ================ Compute the slice
			System.out.println("************** Compute a new slice **************" );
			boolean sliceWritten = false, contextWritten = false, mdWritten = false;
			int pstmt_lno = 0;
			List<SliceLine> sliceLines = new ArrayList<>();

			if ((seed.equals("executeQuery") || seed.equals("executeUpdate")) && (sliceDirection == 'B' || sliceDirection == 'b')) {

				List<SliceLine> sliceLines1;
				sliceLines1 = SlicerTool.do_slice_bw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

				for (SliceLine s : sliceLines1) {
//					System.out.println(" ----------> " + s.lno + ") " + s.stmt);

					if (s.stmt.contains("prepareStatement")) {
						pstmt_lno = s.lno;
						System.out.println(">>>>  BW Slice ( preparedStatement@" + s.lno + " )");
						List<SliceLine> sliceLines2;
						List<SliceLine> sliceLines3;
						String seed2 = "prepareStatement";
						char sstype2 = 'r';
						sliceLines2 = SlicerTool.do_slice_fw (appJar, app_folder_className, s.lno, appSrc, 0, dOptions, cOptions,
								sstype2, seed2);
						sliceLines3 = SlicerTool.do_slice_bw (appJar, app_folder_className, s.lno, appSrc, 0,dOptions, cOptions,
								sstype2, seed2);
//						ExtractQuery.ExtractorPStmt(s_appSrc, s_appSrc_java, slices_and_context_path, s_slice_file,s_seed, 0, s_lno);
						// merge the lines
						System.out.println(">>>>> Merge Lines <<<<<");
						sliceLines = SlicerUtil.merge_two_slices(sliceLines1, sliceLines2);
						sliceLines = SlicerUtil.merge_two_slices(sliceLines, sliceLines3);
						break;
					}
				}

				if (sliceLines.size() == 0) {
					sliceLines = sliceLines1;
				}

			} else if (sliceDirection == 'B' || sliceDirection == 'b') {
				System.out.println(">>>>  BW Slice ( " + seed + "@" + lno + " )");
				sliceLines = SlicerTool.do_slice_bw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

			} else {
				System.out.println(">>>>  FW Slice( \" +seed +\"@\"+lno+ \" )");
				sliceLines = SlicerTool.do_slice_fw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

			}


			//======== Building the slice source code & context
			// System.out.println(sliceLines.toString());
			String slice_srclines = "";
			String context_srclines = "";
			String md_cq_setStrings = "";
			Sig s = new Sig();
			int sf_lno = 1; // slice file lno
//			if(appType == 'v' || appType == 'V')
//				slice_srclines = "public class VSlice {public static void main() {";//lno= 1
//			else if(appType == 's' || appType == 'S')
//				slice_srclines = "public class SSlice {public static void main() {";//lno= 1
//			else
				slice_srclines = "public class Slice {public static void main() {";//lno= 1
			for (SliceLine sl : sliceLines) {
//				System.out.println(">>> "+sl);
				sf_lno++;
				if (sf_lno == 2) {
//					slice_srclines = slice_srclines + sl.stmt.trim();
					context_srclines = context_srclines + sf_lno + "," + sl.method + "," + sl.lno;
					// Line format: slice_file_lno, method where line exists, line in original
					// source code,
				} else {
					context_srclines = context_srclines + "\n" + sf_lno + "," + sl.method + "," + sl.lno;
				}
				// Add slice lines to a java class.
				slice_srclines = slice_srclines + "\n" + sl.stmt.trim();
			}
			slice_srclines = slice_srclines + "\n}}";

			//-------  Write slice & context to files
//			c_slice_srclines = slice_srclines;
			set_slice_srclines(slice_srclines);
			sliceWritten = StrUtil.write_tofile(slicePath, slice_srclines);

			contextWritten = StrUtil.write_tofile(contextPath, context_srclines);
			set_context_srclines(context_srclines);
//			c_context_srclines = context_srclines;
			// ================= 2- Extract query to get the signature
			// ------------------- From Vul. Example
			if ((seed.equals("executeQuery") || seed.equals("executeUpdate"))
					&& slice_srclines.contains(".prepareStatement")) {
				ExtractQuery.infoSet2.clear();
				String seed2 = "prepareStatement";
				String query_str = ExtractQuery.ExtractorPStmt(appSrc, seed2, pstmt_lno);
				c_cleaned_query = query_str;
//							System.out.println("query_str " + query_str);
//							Sig s2 = new Sig();
				if (query_str != null) {
					System.out.println("query_str" + query_str);
					s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection,
							StrUtil.replace_questionmarks(query_str), false,null);

					md_cq_setStrings = "\ncq:" + query_str;
					// ====== The query should be already clean! So we don't need to get setObject()

					// ig sign_slice_querypath(String path, String key, char dir, String query_path,
					// boolean print)
//								System.out.println("S2S2S2S2S2S2S2S2S2S2S2S2S2S2S2 SIG: " + s.toString());

				}

			}
			// ------------------- From Sec. Example
			else if (seed.equals("executeQuery")
					|| seed.equals("executeUpdate") && !slice_srclines.contains(".prepareStatement")) {
				// Extract query to get the signature from Vul. Example
				ExtractQuery.infoSet.clear();
//							slicePath = slices_and_context_path + "/" + s_slice_file;
				String slice_file = StrUtil.get_filename(slicePath);
				String slice_folder = StrUtil.get_folder_path(slicePath);// *
//				String query_file_path = ExtractQuery.exp1Extractor(slicePath, slice_file, lno);// *
				String query_file_path = ExtractQuery.exp1Extractor(slice_folder, slice_file, lno);
				// ExtractQuery.Extractor(slice_folder, slice_file, 1);// *
//				String query_file_path = slice_folder + "/" + StrUtil.get_classname(slicePath) + "/Query" + n + ".txt";
//							System.out.println("query_file_path: " + query_file_path + ", slice_file: " + slice_file);

				// sign_slice_querypath(String path, String key, String dir, String query_path)
				if (query_file_path != null) {
					s = SignSlice.sign_slice_querypath(slicePath, seed, sliceDirection, query_file_path, false, null);

					if (SignSlice.qp.get_pstmt_query() != null && SignSlice.qp.get_pstmt_query().length() > 0) {
						md_cq_setStrings = "\ncq:" + SignSlice.qp.get_pstmt_query();
						c_cleaned_query = SignSlice.qp.get_pstmt_query();
//								System.out.println("EEE new_cq is NOT empty!\n"+md_cq_setStrings);
					}
//							else {
//								System.out.println("EEE new_cq is empty!");
//							}
					if (SignSlice.qp.get_set_strings().size() > 0) {
//						setStrings = SignSlice.qp.get_set_strings();
						set_setStrings(SignSlice.qp.get_set_strings());
						for (String str : SignSlice.qp.get_set_strings()) {
							md_cq_setStrings = md_cq_setStrings + "\nsets:" + str;
						}
					}
				} else {
					// TODO whitelisting cases
					System.out.println("&&&&&&& query_file_path is Empty");
				}
//							System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& SIG: " + s.toString());

			} else {
				// TODO: Other cases.

			}
			//c_s = s;
			set_cs(s);
			// Write to a file the slice and its context using slicePath and

			String md ="";
			if(appType == 'v' || appType == 'V' ) {
				md ="sig:" + s.toString() + "\nv_sl:" + slicePath + "\nv_src:" + appSrc + "\nv_con:" + contextPath;
			} else if(appType == 's' || appType == 'S') {
				md = "\ns_sl:" + slicePath + "\ns_src:" + appSrc + "\ns_con:" + contextPath;
			}

			if (md_cq_setStrings != null || md_cq_setStrings.length() > 0) {
//						System.out.println("sub_md: "+cq_setstrings_data);
				md = md + md_cq_setStrings;
//						System.out.println("md: "+md);
			}



			mdWritten = StrUtil.append_tofile(mdPath, md);

            return sliceWritten && contextWritten && mdWritten;

		}


}
