package slicer.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import slicer.datatypes.SeedList;
import slicer.datatypes.SliceLine;
import slicer.datatypes.VariableMData;
import slicer.tool.SlicerTool;

public class FixerUtils {
	public static void train(String SSrc, String SJar, String SCName, String VSrc, String VJar, String VCName)
			throws WalaException, CancelException, IOException, InvalidClassFileException {
		// ===========Training steps:
		// Build the slices and collect data analysis
		// Step 1 - Taint analysis: Find all vars that has data input
		// scanner slice

		find_all_in_slices(SSrc, SJar, SCName);
		// parse the slice to build List<VariableMData> vars
		// EBCreation
		// Step 2 - Find the slices
		// Vul 1 - execute query
		// Parse the query : Column Value, Column/Table Name, All the Query
		// Create a signature
		// Vul 2 - Credentials Handling
		// hard coded credentials
		// data structure
		// Vul 3 - Ureleased resourses: Unclosed Connection

		// Vul 4 - Unencrypted Connection

		// Step 3 - Find the AST difference between the slices of secure and vulnerable
		// apps.
		// result: Edit blocks
		// How to create the edit blocks to patches and how do you create place holders
		// Step 4 -

	}

	public static List<VariableMData> find_all_in_slices(String appSrc, String appJar, String className)
			throws WalaException, CancelException, IOException, InvalidClassFileException {
		List<VariableMData> vars = new ArrayList<>();
		List<SliceLine> slices_Lines = new ArrayList<>();
		String[] SeedsWorkds = { "Scanner", "BufferedReader", "InputStreamReader", "FileReader" };// console didn't work
		for (String key : SeedsWorkds) {
			SeedList sl = SrcCodeUtil.find_class_seeds_key(SrcCodeUtil.get_src_lines(appSrc), key);
			if (!sl.is_empty) {
				System.out.println("\t\tSlice seed is " + sl.key + " @line " + sl.LinesNo);
				for (Integer lno : sl.LinesNo) {
					// Get lines for one key
					List<SliceLine> sliceLines = SlicerTool.do_slice_fw(appJar, className, lno, appSrc, 0,
							DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', key);
//					for (SliceLine s: sliceLines ) {
//						if(s.walaStmt)
//
//					}
					// .NO_EXCEPTIONS TOOK FEW MINUTES And gave thin result
					// System.out.println("@@@@@@@@ 343434@@@@ ");
//					System.out.println (sliceLines.toString());
					// Add the lines to all keys slices
					slices_Lines.addAll(sliceLines);
				}

			}

		}
//		Collections.sort(slices_Lines);
		// Remove duplicate lines
//				for (int i = slices_Lines.size() - 1; i > 0; i--) {
//					if (slices_Lines.get(i).lno == slices_Lines.get(i - 1).lno) {
//						slices_Lines.remove(i);
//					}
//				}
		System.out.println(slices_Lines);
		return vars;
	}

}
