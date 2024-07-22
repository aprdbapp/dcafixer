package slicer.tool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.core.util.io.FileProvider;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.FileOfClasses;

import flocalization.G;
import flocalization.SignSlice;
import net.sf.jsqlparser.JSQLParserException;
import slicer.datatypes.Sig;
import slicer.datatypes.SliceLine;
import slicer.utilities.SlicerUtil;
import slicer.utilities.StrUtil;

public class EBCreation {
	private static final String EXCLUSIONS = "java\\/awt\\/.*\n"
			+ "javax\\/swing\\/.*\n"
			+ "sun\\/awt\\/.*\n"
			+ "sun\\/swing\\/.*\n"
			+ "com\\/sun\\/.*\n"
			+ "sun\\/.*\n"
			+ "org\\/netbeans\\/.*\n"
			+ "org\\/openide\\/.*\n"
			+ "com\\/ibm\\/crypto\\/.*\n"
			+ "com\\/ibm\\/security\\/.*\n"
			+ "org\\/apache\\/xerces\\/.*\n"
			+ "java\\/security\\/.*\n"
			+ "java\\/sql\\/SQLException"
			+ "java\\/sql\\/Statement"
			+ "java\\/sql\\/ResultSet"
			+ "java\\/sql\\/.*\n"
			+ "java\\/io\\/.*\n"
			+ "java\\/io\\/BufferedReader\n"
			+ "java\\/io\\/IOException\n"
			+ "java\\/io\\/InputStreamReader\n"
			+ "java\\/util\\/Vector\n"
			+ "java\\/security\\/PublicKey\n"
			+ "java\\/security\\/MessageDigest\n"
			+ "java\\/math\\/.*\n"
			+ "java\\/util\\/.*\n"
			+"java\\/lang\\/Math"
			+ "java\\/text\\/.*\n"//++++++++++++++++++
			+"us\\/codecraft\\/webmagic\\/.*\n" //++
			+"javax\\/annotation\\/.*\n"//++
//			+"org\\/springframework\\/stereotype\\/Component\n"//++
//			+"us\\/codecraft\\/webmagic\\/.*\n"//++
			+"org\\/apache\\/ibatis\\/annotations\\/Insert\n"//++
			+"org\\/springframework\\/.*\n"//++
			+"javax\\/annotation\\/.*\n"//++
			+ "org\\/apache\\/.*\n"
			+ "java\\/net\\/.*\n"
			+"com\\/mysql\\/.*\n"
			+ "org\\/junit\\/.*\n"
			+"org\\/jsoup\\/.*\n"
			+"org\\/htmlcleaner\\/.*\n"
			+"junit\\/.*\n"
			+"java\\/sql\\/.*\n"
		  	+"com\\/microsoft\\/.*\n"
		  	+"com\\/alibaba\\/.*\n"
		  	+"com\\/google\\/.*\n"
		  	+"org\\/assertj\\/.*\n"
		  	+"org\\/jdom2\\/.*\n"
		  	+"redis\\/clients\\/.*\n"
			+"net\\/minidev\\/.*\n"
			+"com\\/jayway\\/.*\n"
			+"org\\/slf4j\\/.*\n"
			+"javax\\/servlet\\/.*\n"
			+"us\\/codecraft\\/.*\n"
			+"org\\/hamcrest\\/.*\n"
			+"org\\/mybatis\\/.*\n"
			+"org\\/aopalliance\\/.*\n";

	private static final String EXCLUSIONS_GUI =
//			"java\\/awt\\/.*\n" // === Shouldn't be excluded with GUI
//			+"javax\\/swing\\/.*\n" // === Shouldn't be excluded with GUI

			"com\\/sun\\/.*\n"
			+ "sun\\/.*\n"
			+ "org\\/netbeans\\/.*\n"
			+ "org\\/openide\\/.*\n"
			+ "com\\/ibm\\/crypto\\/.*\n"
			+ "com\\/ibm\\/security\\/.*\n"
			+ "org\\/apache\\/xerces\\/.*\n"
			+ "java\\/security\\/.*\n"
			+ "java\\/sql\\/SQLException"
			+ "java\\/sql\\/Statement"
			+ "java\\/sql\\/ResultSet"
			+ "java\\/sql\\/.*\n"
			+ "java\\/io\\/.*\n"
			+ "java\\/io\\/BufferedReader\n"
			+ "java\\/io\\/IOException\n"
			+ "java\\/io\\/InputStreamReader\n"
			+ "java\\/util\\/Vector\n"
			+ "java\\/security\\/PublicKey\n"
			+ "java\\/security\\/MessageDigest\n"
			+ "java\\/math\\/.*\n"
			+ "java\\/util\\/.*\n"
			+"java\\/lang\\/Math"
			+ "java\\/io\\/.*\n"//+++++++++
			+ "java\\/text\\/.*\n"
			+"us\\/codecraft\\/webmagic\\/.*\n" //++
//			+"javax\\/annotation\\/.*\n"//++
			+"org\\/springframework\\/stereotype\\/Component\n"//++
			+"us\\/codecraft\\/webmagic\\/.*\n"//++
			+"org\\/apache\\/ibatis\\/annotations\\/Insert\n"//++
			+"org\\/springframework\\/.*\n"//++
//			+"javax\\/annotation\\/.*\n"//++
			+ "org\\/apache\\/.*\n"
			+ "java\\/net\\/.*\n"
			+"com\\/mysql\\/.*\n"
			+ "org\\/junit\\/.*\n"
			+"org\\/jsoup\\/.*\n"
			+"org\\/htmlcleaner\\/.*\n"
//			+"junit\\/.*\n"
			+"java\\/sql\\/.*\n"
		  	+"com\\/microsoft\\/.*\n"//++++++++++
		  	+"com\\/alibaba\\/.*\n"
		  	+"com\\/google\\/.*\n"
		  	+"org\\/assertj\\/.*\n"
		  	+"org\\/jdom2\\/.*\n"
		  	+"redis\\/clients\\/.*\n"
			+"net\\/minidev\\/.*\n"
			+"com\\/jayway\\/.*\n"//========
			+"org\\/slf4j\\/.*\n"
//			+"javax\\/servlet\\/.*\n"
			+"us\\/codecraft\\/.*\n"
			+"org\\/hamcrest\\/.*\n"
			+"org\\/mybatis\\/.*\n"
			+"org\\/aopalliance\\/.*\n";

	public static String appSrc_path = "/Users/Dareen/NetBeansProjects/smallBank/src/TSet/";
	public static DataDependenceOptions dOptions = DataDependenceOptions.NO_HEAP;
	public static ControlDependenceOptions cOptions = ControlDependenceOptions.NONE;
	public static DataDependenceOptions dOptions2 = DataDependenceOptions.NO_HEAP;
	public static ControlDependenceOptions cOptions2 = ControlDependenceOptions.NONE;
	public static int n = 1;
	public static boolean Print = false;



	// TODO: Write another copy of find_slice_and_context() that returns the slice lines
	public static boolean find_slice_and_context(String appJar, String appSrc, String app_folder_className, char sliceDirection, int lno, String seed,
			char sstype, String slicePath, String contextPath, String mdPath,DataDependenceOptions dOptions,
			ControlDependenceOptions cOptions, char appType)
			throws WalaException, CancelException, IOException, InvalidClassFileException, JSQLParserException {
		// ================ Compute the slice
		if(Print) {
			System.out.println("************** Compute a new slice **************" );
		}
		boolean sliceWritten = false, contextWritten = false, mdWritten = false;
		int pstmt_lno = 0;
		List<SliceLine> sliceLines = new ArrayList<>();

		if ((seed.equals("executeQuery") || seed.equals("executeUpdate")) && (sliceDirection == 'B' || sliceDirection == 'b')) {

			List<SliceLine> sliceLines1;
			sliceLines1 = SlicerTool.do_slice_bw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

			for (SliceLine s : sliceLines1) {
//				System.out.println(" ----------> " + s.lno + ") " + s.stmt);

				if (s.stmt.contains("prepareStatement")) {
					pstmt_lno = s.lno;
					if(Print) {
						System.out.println(">>>>  BW Slice ( preparedStatement@" + s.lno + " )");
					}
					List<SliceLine> sliceLines2;
					List<SliceLine> sliceLines3;
					String seed2 = "prepareStatement";
					char sstype2 = 'r';
					sliceLines2 = SlicerTool.do_slice_fw (appJar, app_folder_className, s.lno, appSrc, 0, dOptions, cOptions,
							sstype2, seed2);
					sliceLines3 = SlicerTool.do_slice_bw (appJar, app_folder_className, s.lno, appSrc, 0,dOptions, cOptions,
							sstype2, seed2);
//					ExtractQuery.ExtractorPStmt(s_appSrc, s_appSrc_java, slices_and_context_path, s_slice_file,s_seed, 0, s_lno);
					// merge the lines
					if(Print) {
						System.out.println(">>>>> Merge Lines <<<<<");
					}
					sliceLines = SlicerUtil.merge_two_slices(sliceLines1, sliceLines2);
					sliceLines = SlicerUtil.merge_two_slices(sliceLines, sliceLines3);
					break;
				}
			}

			if (sliceLines.size() == 0) {
				sliceLines = sliceLines1;
			}

		} else if (sliceDirection == 'B' || sliceDirection == 'b') {
			if(Print) {
				System.out.println(">>>>  BW Slice ( " + seed + "@" + lno + " )");
			}
			sliceLines = SlicerTool.do_slice_bw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

		} else {
			if(Print) {
				System.out.println(">>>>  FW Slice( \" +seed +\"@\"+lno+ \" )");
			}
			sliceLines = SlicerTool.do_slice_fw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

		}

		if(Print) {
			System.out.println("*** Done slicing!");
		}
		//======== Building the slice source code & context
		// System.out.println(sliceLines.toString());
		String slice_srclines = "";
		String context_srclines = "";
		String md_cq_setStrings = "";
		Sig s = new Sig();
		int sf_lno = 1; // slice file lno
//		if(appType == 'v' || appType == 'V')
//			slice_srclines = "public class VSlice {public static void main() {";//lno= 1
//		else if(appType == 's' || appType == 'S')
//			slice_srclines = "public class SSlice {public static void main() {";//lno= 1
//		else
			slice_srclines = "public class Slice {public static void main() {";//lno= 1
		for (SliceLine sl : sliceLines) {
//			System.out.println(">>> "+sl);
			//+++++
			if(sl.stmt.contains("createStatement")) {
				continue;
			}
			sf_lno++;
			//++++++
			if (sf_lno == 2) {
//				slice_srclines = slice_srclines + sl.stmt.trim();
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

		//Write slice & context to files
		sliceWritten = StrUtil.write_tofile(slicePath, slice_srclines);

		contextWritten = StrUtil.write_tofile(contextPath, context_srclines);


		// ================= 2- Extract query to get the signature
		// ------------------- From Vul. Example
		if ((seed.equals("executeQuery") || seed.equals("executeUpdate"))
				&& slice_srclines.contains(".prepareStatement")) {
			ExtractQuery.infoSet2.clear();
			String seed2 = "prepareStatement";
			String query_str = ExtractQuery.ExtractorPStmt(appSrc, seed2, pstmt_lno);
//						System.out.println("query_str " + query_str);
//						Sig s2 = new Sig();
			if (query_str != null) {
				if(Print) {
					System.out.println("query_str" + query_str);
				}
				s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection,
						StrUtil.replace_questionmarks(query_str), false,null);

				md_cq_setStrings = "\ncq:" + query_str;
				// ====== The query should be already clean! So we don't need to get setObject()

				// ig sign_slice_querypath(String path, String key, char dir, String query_path,
				// boolean print)
//							System.out.println("S2S2S2S2S2S2S2S2S2S2S2S2S2S2S2 SIG: " + s.toString());

			}

		}
		// ------------------- From Sec. Example
		else if (seed.equals("executeQuery")
				|| seed.equals("executeUpdate") && !slice_srclines.contains(".prepareStatement")) {
			// Extract query to get the signature from Vul. Example
			ExtractQuery.infoSet.clear();
//						slicePath = slices_and_context_path + "/" + s_slice_file;
			String slice_file = StrUtil.get_filename(slicePath);
			String slice_folder = StrUtil.get_folder_path(slicePath);// *
			String query_file_path = ExtractQuery.Extract_call_at_line(slice_folder, slice_file, 1);// *
//			String query_file_path = ExtractQuery.Extractor(slice_folder, slice_file, 1);// *
//			String query_file_path = slice_folder + "/" + StrUtil.get_classname(slicePath) + "/Query" + n + ".txt";
//						System.out.println("query_file_path: " + query_file_path + ", slice_file: " + slice_file);

			// sign_slice_querypath(String path, String key, String dir, String query_path)
			if(query_file_path != null) {
				s = SignSlice.sign_slice_querypath(slicePath, seed, sliceDirection, query_file_path, false,null);

			if (SignSlice.qp.get_pstmt_query() != null && SignSlice.qp.get_pstmt_query().length() > 0) {
				md_cq_setStrings = "\ncq:" + SignSlice.qp.get_pstmt_query();
//							System.out.println("EEE new_cq is NOT empty!\n"+md_cq_setStrings);
			}
//						else {
//							System.out.println("EEE new_cq is empty!");
//						}
			if (SignSlice.qp.get_set_strings().size() > 0) {
				for (String str : SignSlice.qp.get_set_strings()) {
					md_cq_setStrings = md_cq_setStrings + "\nsets:" + str;
				}
			}}else {
				//TODO whitelisting cases
				if(Print) {
					System.out.println("&&&&&&& query_file_path is Empty");
				}
			}
//						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& SIG: " + s.toString());

		} else {
			// TODO: Other cases.

		}
		// Write to a file the slice and its context using slicePath and

		String md ="";
		if(appType == 'v' || appType == 'V' ) {
			md ="sig:" + s.toString() + "\nv_sl:" + slicePath + "\nv_src:" + appSrc + "\nv_con:" + contextPath;
		} else if(appType == 's' || appType == 'S') {
			md = "\ns_sl:" + slicePath + "\ns_src:" + appSrc + "\ns_con:" + contextPath;
		}

		if (md_cq_setStrings != null || md_cq_setStrings.length() > 0) {
//					System.out.println("sub_md: "+cq_setstrings_data);
			md = md + md_cq_setStrings;
//					System.out.println("md: "+md);
		}



		mdWritten = StrUtil.append_tofile(mdPath, md);

        return sliceWritten && contextWritten && mdWritten;

	}

	public static List<SliceLine> find_slice_and_context_query_sent(String appJar, String appSrc, String app_folder_className, char sliceDirection, int lno, String seed,
			char sstype, String slicePath, String contextPath, String mdPath,DataDependenceOptions dOptions,
			ControlDependenceOptions cOptions, char appType, String query)
			throws WalaException, CancelException, IOException, InvalidClassFileException, JSQLParserException {
		// ================ Compute the slice
		if(Print) {
			System.out.println("************** Compute a new slice **************" );
		}
		boolean sliceWritten = false, contextWritten = false, mdWritten = false;
		int pstmt_lno = 0;
		List<SliceLine> sliceLines = new ArrayList<>();

		if ((seed.equals("executeQuery") || seed.equals("executeUpdate")) && (sliceDirection == 'B' || sliceDirection == 'b')) {
			List<SliceLine> sliceLines1;
			if(Print) {
				System.out.println("Calling do_slice_bw");
			}
			sliceLines1 = SlicerTool.do_slice_bw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);
			// ------ On 9/28th I commented the code below because of changes I mad that made code very slow!
//			for (SliceLine s : sliceLines1) {
////				System.out.println(" ----------> " + s.lno + ") " + s.stmt);
//				if (s.stmt.contains("prepareStatement")) {
//					pstmt_lno = s.lno;
//					if(Print)System.out.println(">>>>  FW & BW Slice ( preparedStatement@" + s.lno + " )");
//					List<SliceLine> sliceLines2;
//					List<SliceLine> sliceLines3;
//					String seed2 = "prepareStatement";
//					char sstype2 = 'r';
//					sliceLines2 = SlicerTool.do_slice_fw (appJar, app_folder_className, s.lno, appSrc, 0, dOptions, cOptions,
//							sstype2, seed2);
//					sliceLines3 = SlicerTool.do_slice_bw (appJar, app_folder_className, s.lno, appSrc, 0,dOptions, cOptions,
//							sstype2, seed2);
////					ExtractQuery.ExtractorPStmt(s_appSrc, s_appSrc_java, slices_and_context_path, s_slice_file,s_seed, 0, s_lno);
//					// merge the lines
//					if(Print)System.out.println(">>>>> Merge Lines <<<<<");
//					sliceLines = SlicerUtil.merge_two_slices(sliceLines1, sliceLines2);
//					sliceLines = SlicerUtil.merge_two_slices(sliceLines, sliceLines3);
//					break;
//				}
//			}
			if (sliceLines1 != null || sliceLines.size() == 0) {
				sliceLines = sliceLines1;
			}

		} else if (sliceDirection == 'B' || sliceDirection == 'b') {
			if(Print) {
				System.out.println(">>>>  BW Slice ( " + seed + "@" + lno + " )");
			}
			sliceLines = SlicerTool.do_slice_bw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

		} else {
			if(Print) {
				System.out.println(">>>>  FW Slice( \" +seed +\"@\"+lno+ \" )");
			}
			sliceLines = SlicerTool.do_slice_fw(appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

		}

		if(sliceLines == null || sliceLines.size() == 0 ) {
			return null;
		}
		//======== Building the slice source code & context
		System.out.println("SSSSSSS SLice # lines: " + sliceLines.size());
		 if(Print) {
			 System.out.println("SSSSSSS SLice :\n");
			 for(SliceLine l : sliceLines) {
				 System.out.println(l.lno + " @ " + l.stmt);
			 }
		 }
			 //System.out.println(sliceLines.toString());
		String slice_srclines = "";
		String context_srclines = "";
		String md_cq_setStrings = "";
		Sig s = new Sig();
		int sf_lno = 1; // slice file lno
//		if(appType == 'v' || appType == 'V')
//			slice_srclines = "public class VSlice {public static void main() {";//lno= 1
//		else if(appType == 's' || appType == 'S')
//			slice_srclines = "public class SSlice {public static void main() {";//lno= 1
//		else
			slice_srclines = "public class Slice {public static void main() {";//lno= 1
		for (SliceLine sl : sliceLines) {
//			System.out.println(">>> "+sl);
			//+++++
			if(sl.stmt.contains("createStatement")) {
				continue;
			}
			sf_lno++;
			//++++++
			if (sf_lno == 2) {
//				slice_srclines = slice_srclines + sl.stmt.trim();
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

		//Write slice & context to files
		sliceWritten = StrUtil.write_tofile(slicePath, slice_srclines);

		contextWritten = StrUtil.write_tofile(contextPath, context_srclines);


		// ================= 2- Use "query" to get the signature
		// ------------------- From Vul. Example
//		if ((seed.equals("executeQuery") || seed.equals("executeUpdate"))
//				&& slice_srclines.contains(".prepareStatement")) {
			if(slice_srclines.contains(".prepareStatement")) {
//			ExtractQuery.infoSet2.clear();
			String seed2 = "prepareStatement";
//			String query_str = ExtractQuery.ExtractorPStmt(appSrc, seed2, pstmt_lno);
//						System.out.println("query_str " + query_str);
//						Sig s2 = new Sig();
			if (query != null) {
				if(Print) {
					System.out.println("query_str" + query);
				}
				s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection,
						StrUtil.replace_questionmarks(query), true,null);

				md_cq_setStrings = "\ncq:" + query;
				// ====== The query should be already clean! So we don't need to get setObject()

				// ig sign_slice_querypath(String path, String key, char dir, String query_path,
				// boolean print)
//							System.out.println("S2S2S2S2S2S2S2S2S2S2S2S2S2S2S2 SIG: " + s.toString());

			}

		}
		// ------------------- From Sec. Example
		else {
//			if (seed.equals("executeQuery")
//				|| seed.equals("executeUpdate") && !slice_srclines.contains(".prepareStatement")) {
			// Extract query to get the signature from Vul. Example
//			ExtractQuery.infoSet.clear();
//						slicePath = slices_and_context_path + "/" + s_slice_file;
			String slice_file = StrUtil.get_filename(slicePath);
			String slice_folder = StrUtil.get_folder_path(slicePath);// *
//			String query_file_path = ExtractQuery.Extract_call_at_line(slice_folder, slice_file, 1);// *
//			String query_file_path = ExtractQuery.Extractor(slice_folder, slice_file, 1);// *
//			String query_file_path = slice_folder + "/" + StrUtil.get_classname(slicePath) + "/Query" + n + ".txt";
//						System.out.println("query_file_path: " + query_file_path + ", slice_file: " + slice_file);

			// sign_slice_querypath(String path, String key, String dir, String query_path)
			if(query != null) {
				s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection, query, false,null);

			if (SignSlice.qp.get_pstmt_query() != null && SignSlice.qp.get_pstmt_query().length() > 0) {
				md_cq_setStrings = "\ncq:" + SignSlice.qp.get_pstmt_query();
//							System.out.println("EEE new_cq is NOT empty!\n"+md_cq_setStrings);
			}
//						else {
//							System.out.println("EEE new_cq is empty!");
//						}
			if (SignSlice.qp.get_set_strings().size() > 0) {
				for (String str : SignSlice.qp.get_set_strings()) {
					md_cq_setStrings = md_cq_setStrings + "\nsets:" + str;
				}
			}}else {
				//TODO whitelisting cases
				if(Print) {
					System.out.println("&&&&&&& query_file_path is Empty");
				}
			}
//						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& SIG: " + s.toString());

		}
//		else {
//			// TODO: Other cases.
//
//		}
		// Write to a file the slice and its context using slicePath and

		String md ="";
		if(appType == 'v' || appType == 'V' ) {
			md ="sig:" + s.toString() + "\nv_sl:" + slicePath + "\nv_src:" + appSrc + "\nv_con:" + contextPath;
		} else if(appType == 's' || appType == 'S') {
			md = "\ns_sl:" + slicePath + "\ns_src:" + appSrc + "\ns_con:" + contextPath;
		}

		if (md_cq_setStrings != null || md_cq_setStrings.length() > 0) {
//					System.out.println("sub_md: "+cq_setstrings_data);
			md = md + md_cq_setStrings;
//					System.out.println("md: "+md);
		}



		mdWritten = StrUtil.append_tofile(mdPath, md);

		if (sliceWritten && contextWritten && mdWritten) {
			return sliceLines;
		} else {
			return null;
		}

	}

	public static List<SliceLine> find_slice_and_context_query_sent_cgtype(String appJar, String appSrc, String app_folder_className, char sliceDirection, int lno, String seed,
			char sstype, String slicePath, String contextPath, String mdPath,DataDependenceOptions dOptions,
			ControlDependenceOptions cOptions, char appType, String query, int cgt)
			throws WalaException, CancelException, IOException, InvalidClassFileException, JSQLParserException {
		// ================ Build CG ============

		CallGraphBuilder<InstanceKey> builder ;
		CallGraph cg;
		if(cgt == G.CG_AllEPoints_MyEx) {
			if(Print) {
				System.out.println(" AllApplicationEntrypoints MyEx .. ");
			}
			AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
			scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes(StandardCharsets.UTF_8))));
			IClassHierarchy cha = ClassHierarchyFactory.make(scope);
			Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);

			AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
			cg = builder.makeCallGraph(options, null);

		}if(cgt == G.CG_AllEPoints_MyExGUI) {
			if(Print) {
				System.out.println(" AllApplicationEntrypoints MyEx_GUI .. ");
			}
			AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
			scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS_GUI.getBytes(StandardCharsets.UTF_8))));
			IClassHierarchy cha = ClassHierarchyFactory.make(scope);
			Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);

			AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
			cg = builder.makeCallGraph(options, null);

		}else {
			AnalysisScope scope;
//
			if (cgt == G.CG_MainEPoints_MyEx){
				if(Print) {
					System.out.println(" makeMainEntrypoints & My EXCLUSIONS .. ");
				}
				scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes(StandardCharsets.UTF_8))));
			}else if(cgt == G.CG_MainEPoints_CGTUtilEx){
				if(Print) {
					System.out.println(" makeMainEntrypoints & CallGraphTestUtil EXCLUSIONS .. ");
				}
				scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
			}else {
				if(Print) {
					System.out.println(" makeMainEntrypoints & No EXCLUSIONS .. ");
				}
				scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
			}
			IClassHierarchy cha = ClassHierarchyFactory.make(scope);
			Iterable<Entrypoint> entrypoints =
			        com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(cha);
			AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
			cg = builder.makeCallGraph(options, null);
			if(Print) {
				System.out.println("Done building CG");
			}



		}
		if(Print)
		 {
			System.out.println(CallGraphStats.getStats(cg));
			// ================ Compute the slice ============
		}

		if(Print) {
			System.out.println("************** Compute a new slice **************" );
		}
		boolean sliceWritten = false, contextWritten = false, mdWritten = false;
		int pstmt_lno = 0;
		List<SliceLine> sliceLines = new ArrayList<>();

		if ((seed.equals("executeQuery") || seed.equals("executeUpdate")) && (sliceDirection == 'B' || sliceDirection == 'b')) {
			List<SliceLine> sliceLines1;
			if(Print) {
				System.out.println("Calling do_slice_bw");
			}
			sliceLines1 = SlicerTool.do_slice_bw_cg(cg, builder,appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);
			if (sliceLines1 != null || sliceLines.size() == 0) {
				sliceLines = sliceLines1;
			}

		} else if (sliceDirection == 'B' || sliceDirection == 'b') {
			if(Print) {
				System.out.println(">>>>  BW Slice ( " + seed + "@" + lno + " )");
			}
			sliceLines = SlicerTool.do_slice_bw_cg(cg, builder, appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

		}

		if(sliceLines == null || sliceLines.size() == 0 ) {
			return null;
		}
		//======== Building the slice source code & context
//		System.out.println("SSSSSSS SLice # lines: " + sliceLines.size());
//		 if(Print) {
			 System.out.println("-- SLice :");
			 for(SliceLine l : sliceLines) {
				 System.out.println(l.lno + " @ " + l.stmt);
			 }
			 System.out.println(" ----------------\n");
//		 }
			 //System.out.println(sliceLines.toString());
		String slice_srclines = "";
		String context_srclines = "";
		String md_cq_setStrings = "";
		Sig s = new Sig();
		int sf_lno = 1; // slice file lno
//		if(appType == 'v' || appType == 'V')
//			slice_srclines = "public class VSlice {public static void main() {";//lno= 1
//		else if(appType == 's' || appType == 'S')
//			slice_srclines = "public class SSlice {public static void main() {";//lno= 1
//		else
			slice_srclines = "public class Slice {public static void main() {";//lno= 1
		for (SliceLine sl : sliceLines) {
//			System.out.println(">>> "+sl);
			//+++++
			if(sl.stmt.contains("createStatement")) {
				continue;
			}
			sf_lno++;
			//++++++
			if (sf_lno == 2) {
//				slice_srclines = slice_srclines + sl.stmt.trim();
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

		//Write slice & context to files
		sliceWritten = StrUtil.write_tofile(slicePath, slice_srclines);

		contextWritten = StrUtil.write_tofile(contextPath, context_srclines);


		// ================= 2- Use "query" to get the signature
		// ------------------- From Vul. Example
//		if ((seed.equals("executeQuery") || seed.equals("executeUpdate"))
//				&& slice_srclines.contains(".prepareStatement")) {
			if(slice_srclines.contains(".prepareStatement")) {
//			ExtractQuery.infoSet2.clear();
			String seed2 = "prepareStatement";
//			String query_str = ExtractQuery.ExtractorPStmt(appSrc, seed2, pstmt_lno);
//						System.out.println("query_str " + query_str);
//						Sig s2 = new Sig();
			if (query != null) {
				if(Print) {
					System.out.println("query_str" + query);
				}
				s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection,
						StrUtil.replace_questionmarks(query), true,null);

				md_cq_setStrings = "\ncq:" + query;
				// ====== The query should be already clean! So we don't need to get setObject()

				// ig sign_slice_querypath(String path, String key, char dir, String query_path,
				// boolean print)
//							System.out.println("S2S2S2S2S2S2S2S2S2S2S2S2S2S2S2 SIG: " + s.toString());

			}

		}
		// ------------------- From Sec. Example
		else {
//			if (seed.equals("executeQuery")
//				|| seed.equals("executeUpdate") && !slice_srclines.contains(".prepareStatement")) {
			// Extract query to get the signature from Vul. Example
//			ExtractQuery.infoSet.clear();
//						slicePath = slices_and_context_path + "/" + s_slice_file;
			String slice_file = StrUtil.get_filename(slicePath);
			String slice_folder = StrUtil.get_folder_path(slicePath);// *
//			String query_file_path = ExtractQuery.Extract_call_at_line(slice_folder, slice_file, 1);// *
//			String query_file_path = ExtractQuery.Extractor(slice_folder, slice_file, 1);// *
//			String query_file_path = slice_folder + "/" + StrUtil.get_classname(slicePath) + "/Query" + n + ".txt";
//						System.out.println("query_file_path: " + query_file_path + ", slice_file: " + slice_file);

			// sign_slice_querypath(String path, String key, String dir, String query_path)
			if(query != null) {
				s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection, query, false,null);

			if (SignSlice.qp.get_pstmt_query() != null && SignSlice.qp.get_pstmt_query().length() > 0) {
				md_cq_setStrings = "\ncq:" + SignSlice.qp.get_pstmt_query();
//							System.out.println("EEE new_cq is NOT empty!\n"+md_cq_setStrings);
			}
//						else {
//							System.out.println("EEE new_cq is empty!");
//						}
			if (SignSlice.qp.get_set_strings().size() > 0) {
				for (String str : SignSlice.qp.get_set_strings()) {
					md_cq_setStrings = md_cq_setStrings + "\nsets:" + str;
				}
			}}else {
				//TODO whitelisting cases
				if(Print) {
					System.out.println("&&&&&&& query_file_path is Empty");
				}
			}
//						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& SIG: " + s.toString());

		}
//		else {
//			// TODO: Other cases.
//
//		}
		// Write to a file the slice and its context using slicePath and

		String md ="";
		if(appType == 'v' || appType == 'V' ) {
			md ="sig:" + s.toString() + "\nv_sl:" + slicePath + "\nv_src:" + appSrc + "\nv_con:" + contextPath;
		} else if(appType == 's' || appType == 'S') {
			md = "\ns_sl:" + slicePath + "\ns_src:" + appSrc + "\ns_con:" + contextPath;
		}

		if (md_cq_setStrings != null || md_cq_setStrings.length() > 0) {
//					System.out.println("sub_md: "+cq_setstrings_data);
			md = md + md_cq_setStrings;
//					System.out.println("md: "+md);
		}



		mdWritten = StrUtil.append_tofile(mdPath, md);

		if (sliceWritten && contextWritten && mdWritten) {
			return sliceLines;
		} else {
			return null;
		}

	}


	public static List<SliceLine> find_slice_and_context_query_sent_cg(String appJar,String appSrc, String app_folder_className, char sliceDirection, int lno, String seed,
			char sstype, String slicePath, String contextPath, String mdPath,DataDependenceOptions dOptions,
			ControlDependenceOptions cOptions, char appType, String query, CGClass c)
			throws WalaException, CancelException, IOException, InvalidClassFileException, JSQLParserException {
		// ================ Build CG ============

		CallGraphBuilder<InstanceKey> builder = c.getBuilder();
		CallGraph cg = c.getCg();

		// ================ Compute the slice ============

		if(Print) {
			System.out.println("************** Compute a new slice **************" );
		}
		boolean sliceWritten = false, contextWritten = false, mdWritten = false;
//		int pstmt_lno = 0;
		List<SliceLine> sliceLines = new ArrayList<>();

		if ((seed.equals("executeQuery") || seed.equals("executeUpdate")|| seed.equals("execute")) && (sliceDirection == 'B' || sliceDirection == 'b')) {
			List<SliceLine> sliceLines1;
			if(Print) {
				System.out.println("Calling do_slice_bw_cg");
			}
			sliceLines1 = SlicerTool.do_slice_bw_cg(cg, builder,appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);
			if (sliceLines1 != null || sliceLines.size() == 0) {
				sliceLines = sliceLines1;
			}

		} else if (sliceDirection == 'B' || sliceDirection == 'b') {
			if(Print) {
				System.out.println(">>>>  BW Slice ( " + seed + "@" + lno + " )");
			}
			sliceLines = SlicerTool.do_slice_bw_cg(cg, builder, appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);

//		} else if ((seed.equals("createStatement")) && (sliceDirection == 'F' || sliceDirection == 'f')) {
		} else if ( (sliceDirection == 'F' || sliceDirection == 'f')) {
			List<SliceLine> sliceLines1;
			//if(Print)System.out.println("Calling do_slice_fw_cg");
			if(Print) {
				System.out.println(">>>>  FW Slice ( " + seed + "@" + lno + " )");
			}
			sliceLines1 = SlicerTool.do_slice_fw_cg(cg, builder,appJar, app_folder_className, lno, appSrc, 0, dOptions, cOptions, sstype, seed);
			if (sliceLines1 != null || sliceLines.size() == 0) {
				sliceLines = sliceLines1;
			}
		}

		if(sliceLines == null || sliceLines.size() == 0 ) {
			return null;
		}
		//======== Building the slice source code & context
//		System.out.println("SSSSSSS SLice # lines: " + sliceLines.size());
//		 if(Print) {
			 System.out.println("-- SLice :");
			 for(SliceLine l : sliceLines) {
				 System.out.println(l.lno + " @ " + l.stmt);
			 }
			 System.out.println(" ----------------\n");
//		 }
			 //System.out.println(sliceLines.toString());
		String slice_srclines = "";
		String context_srclines = "";
		String md_cq_setStrings = "";
		Sig s = new Sig();
		int sf_lno = 1; // slice file lno
			slice_srclines = "public class Slice {public static void main() {";//lno= 1
		for (SliceLine sl : sliceLines) {
//			System.out.println(">>> "+sl);
			//+++++
			if(sl.stmt.contains("createStatement")) {
				continue;
			}
			sf_lno++;
			//++++++
			if (sf_lno == 2) {
//				slice_srclines = slice_srclines + sl.stmt.trim();
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

		//Write slice & context to files
		sliceWritten = StrUtil.write_tofile(slicePath, slice_srclines);

		contextWritten = StrUtil.write_tofile(contextPath, context_srclines);


		// ================= 2- Use "query" to get the signature
		// ------------------- From Vul. Example
//		if ((seed.equals("executeQuery") || seed.equals("executeUpdate"))
//				&& slice_srclines.contains(".prepareStatement")) {
			if(slice_srclines.contains(".prepareStatement")) {
//			ExtractQuery.infoSet2.clear();
			String seed2 = "prepareStatement";
//			String query_str = ExtractQuery.ExtractorPStmt(appSrc, seed2, pstmt_lno);
//						System.out.println("query_str " + query_str);
//						Sig s2 = new Sig();
			if (query != null) {
				if(Print) {
					System.out.println("query_str" + query);
				}
				s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection,
						StrUtil.replace_questionmarks(query), true,null);

				md_cq_setStrings = "\ncq:" + query;
				// ====== The query should be already clean! So we don't need to get setObject()

				// ig sign_slice_querypath(String path, String key, char dir, String query_path,
				// boolean print)
//							System.out.println("S2S2S2S2S2S2S2S2S2S2S2S2S2S2S2 SIG: " + s.toString());

			}

		}
		// ------------------- From Sec. Example
		else {
//			if (seed.equals("executeQuery")
//				|| seed.equals("executeUpdate") && !slice_srclines.contains(".prepareStatement")) {
			// Extract query to get the signature from Vul. Example
//			ExtractQuery.infoSet.clear();
//						slicePath = slices_and_context_path + "/" + s_slice_file;
			String slice_file = StrUtil.get_filename(slicePath);
			String slice_folder = StrUtil.get_folder_path(slicePath);// *
//			String query_file_path = ExtractQuery.Extract_call_at_line(slice_folder, slice_file, 1);// *
//			String query_file_path = ExtractQuery.Extractor(slice_folder, slice_file, 1);// *
//			String query_file_path = slice_folder + "/" + StrUtil.get_classname(slicePath) + "/Query" + n + ".txt";
//						System.out.println("query_file_path: " + query_file_path + ", slice_file: " + slice_file);

			// sign_slice_querypath(String path, String key, String dir, String query_path)

			if(query != null && query.length()>0) {
//				System.err.print("XXXXXX: "+query);
				s = SignSlice.sign_slice_querystring(slicePath, seed, sliceDirection, query, false,null);

			if (SignSlice.qp.get_pstmt_query() != null && SignSlice.qp.get_pstmt_query().length() > 0) {
				md_cq_setStrings = "\ncq:" + SignSlice.qp.get_pstmt_query();
//							System.out.println("EEE new_cq is NOT empty!\n"+md_cq_setStrings);
			}
//						else {
//							System.out.println("EEE new_cq is empty!");
//						}
			if (SignSlice.qp.get_set_strings().size() > 0) {
				for (String str : SignSlice.qp.get_set_strings()) {
					md_cq_setStrings = md_cq_setStrings + "\nsets:" + str;
				}
			}}else {
				//TODO whitelisting cases
				if(Print) {
					System.out.println("&&&&&&& query_file_path is Empty");
				}
			}
//						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& SIG: " + s.toString());

		}
//		else {
//			// TODO: Other cases.
//
//		}
		// Write to a file the slice and its context using slicePath and

		String md ="";
		if(appType == 'v' || appType == 'V' ) {
			md ="sig:" + s.toString() + "\nv_sl:" + slicePath + "\nv_src:" + appSrc + "\nv_con:" + contextPath;
		} else if(appType == 's' || appType == 'S') {
			md = "\ns_sl:" + slicePath + "\ns_src:" + appSrc + "\ns_con:" + contextPath;
		}

		if (md_cq_setStrings != null || md_cq_setStrings.length() > 0) {
//					System.out.println("sub_md: "+cq_setstrings_data);
			md = md + md_cq_setStrings;
//					System.out.println("md: "+md);
		}



		mdWritten = StrUtil.append_tofile(mdPath, md);

		if (sliceWritten && contextWritten && mdWritten) {
			return sliceLines;
		} else {
			return null;
		}

	}



	public static CGClass BuildCG(String appJar, int cgt)
			throws WalaException, CancelException, IOException {
		// ================ Build CG ============

		CallGraphBuilder<InstanceKey> builder ;
		CallGraph cg;
		if(cgt == G.CG_AllEPoints_MyEx) {
			if(Print) {
				System.out.println(" AllApplicationEntrypoints MyEx .. ");
			}
			AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
			scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes(StandardCharsets.UTF_8))));
			IClassHierarchy cha = ClassHierarchyFactory.make(scope);
			Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);

			AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
			cg = builder.makeCallGraph(options, null);

		}if(cgt == G.CG_AllEPoints_MyExGUI) {
			if(Print) {
				System.out.println(" AllApplicationEntrypoints MyEx_GUI .. ");
			}
			AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
			scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS_GUI.getBytes(StandardCharsets.UTF_8))));
			IClassHierarchy cha = ClassHierarchyFactory.make(scope);
			Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
//			Iterable<Entrypoint> entrypoints = new MyAllApplicationEntrypoints(scope, cha);
//			if(Print)
//			for(Entrypoint ep : entrypoints) {
//				String ePointMethodName = ep.getMethod().getName().toString();
//				System.out.println("EP: "+ePointMethodName);
//			}
			AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
//			builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);// This is the one I used before!
//			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
//			builder = Util.makeZeroContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
//			builder = Util.makeNCFABuilder(cgt, options, new AnalysisCacheImpl(), cha);// Nope
//			builder = Util.makeNObjBuilder(cgt, options, new AnalysisCacheImpl(), cha);// Took about an hour and didn't finish
//			builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
			builder = Util.makeRTABuilder(options, new AnalysisCacheImpl(), cha); //******* better results so far
			cg = builder.makeCallGraph(options, null);


		}else {
			AnalysisScope scope;
//
			if (cgt == G.CG_MainEPoints_MyEx){
				if(Print) {
					System.out.println(" makeMainEntrypoints & My EXCLUSIONS .. ");
				}
				scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes(StandardCharsets.UTF_8))));
			}else if(cgt == G.CG_MainEPoints_CGTUtilEx){
				if(Print) {
					System.out.println(" makeMainEntrypoints & CallGraphTestUtil EXCLUSIONS .. ");
				}
				scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
			}else {
				if(Print) {
					System.out.println(" makeMainEntrypoints & No EXCLUSIONS .. ");
				}
				scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
			}
			IClassHierarchy cha = ClassHierarchyFactory.make(scope);
			Iterable<Entrypoint> entrypoints =
			        com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(cha);
			AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
			cg = builder.makeCallGraph(options, null);
			if(Print) {
				System.out.println("Done building CG");
			}



		}
		if(Print) {
			System.out.println(CallGraphStats.getStats(cg));
		}
		CGClass newCG = new CGClass();
//		newCG.cg = cg;
		newCG.setCg(cg);
		newCG.setBuilder(builder);
		return newCG;
	}

}
