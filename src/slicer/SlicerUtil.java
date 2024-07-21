package slicer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ibm.wala.ipa.slicer.Statement;

import slicer.datatypes.Sig;
import slicer.datatypes.SliceLine;
import slicer.tool.Info;
import slicer.utilities.StrUtil;

public class SlicerUtil {
	public static ArrayList<Integer> get_credintal_lines(Info info) {
		ArrayList<Integer> credintalLines = new ArrayList<>();
		//get_credintal_lines
		//credintalLines.addAll(info.getConn_pass_lines());
		if(info.getConn_pass_lines()!= null && info.getConn_pass_lines().size() > 0) {
			credintalLines.addAll(info.getConn_pass_lines());
		}
		if( info.getConn_user_lines() != null && info.getConn_user_lines().size() > 0) {
			credintalLines.addAll(info.getConn_user_lines());
		}
		if(info.getConn_pass_ln()>0) {
			credintalLines.add(info.getConn_pass_ln());
		}
		if(info.getConn_user_ln()>0) {
			credintalLines.add(info.getConn_user_ln());
		}
		credintalLines = StrUtil.removeDuplicates(credintalLines);
		Collections.sort(credintalLines);
		return credintalLines;

	}
	public static boolean write_slice_to_file(List<SliceLine> sliceLines, String slicePath, String contextPath) throws IOException {
		if (sliceLines.size() > 0) {
			String slice_srclines = "";
			String context_srclines = "";
			//String md_cq_setStrings = "";
			Sig s = new Sig();
			int sf_lno = 1; // slice file lno
			slice_srclines = "public class Slice {public static void main() {";// lno= 1
			for (SliceLine sl : sliceLines) {
//				System.out.println(">>> "+sl);
				// +++++
				if (sl.stmt.contains("createStatement")) {
					continue;
				}
				sf_lno++;
				// ++++++
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

			// Write slice & context to files


			return (StrUtil.write_tofile(slicePath, slice_srclines) &&
					StrUtil.write_tofile(contextPath, context_srclines));

		}
		return false;
	}

	public static void dumpSlice(Collection<Statement> slice) {
		for (Statement s : slice) {
			System.err.println(s);
		}
	}

	public static List<Integer> get_slice_line_numbers(String fpath, Collection<Statement> slice) {
		List<Integer> slicelines = null;
		// Get src lines in a list
		List<String> srcLines = get_src_lines(fpath);
		for (Statement s : slice) {
			System.err.println(s);
			// TODO: 1 - lno = Map s to line number
			// 2 - line = Get the line from $srcLines, Print for debug
			// 3 - Store <lno, line> to $slicelines
		}
		return slicelines;

	}

	public static List<String> get_src_lines(String srcFilepath) {
		List<String> srcLines = null;
		// Read the file

		return srcLines;
	}

//	 public static Iterable<Entrypoint> computeEntryPoints(final String fileName, final String funcName, final String desc,
//             final AnalysisScope scope, final ClassHierarchy cha) {
//
//
//
//	 }

}
