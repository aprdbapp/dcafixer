package dcafixer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import flocalization.SignSlice;
import net.sf.jsqlparser.JSQLParserException;

import java.time.Duration;
import java.time.Instant;

import slicer.datatypes.Sig;
import slicer.tool.ExtractQuery;
import slicer.utilities.StrUtil;

/*
 * I) Go over each project": 
 * 	1- Print #vulnerabilities using 
 -----> * 		a. spotbugs 
 * 		b. dcafixer
 * 	2- Fix one vul. at a time, and test until no vul exist
 *  3- 
 * 
 * */
public class LocatingPIs {
	//public class Exp2PIs
	//public class Experiment2_PIs {
	
	// * L1 = files_srcCode_list
	static String expPath ="/Users/Dareen/Desktop/DCAFixer_Experimets/";
//	static boolean print =true;
	static List<String> SCList = new ArrayList<>();
//	static int  DP, DN;
	static int All_DP =0, All_DN =0;
//	public static void resetValues () {
//		DN = 0; DP = 0;
//		
//	}
	public static List<String> getSCList() {
		return SCList;
	}
	public static void setSCList(List<String> sCList) {
		SCList = sCList;
	}
	
	public static void addToSCList(String e) {
		SCList.add(e);
	}

	public static void clearSCList() {
		SCList.clear();
	}

	public static void printSCList_fullpaths() {
		if (SCList.isEmpty()) {
			System.out.println("No files with IPs!");
		} else {
			int i = 0;
			for (String e : SCList) {
				i++;
				System.out.println(e.replace(expPath, ""));
//				System.out.println(e);
				// System.out.println("String file"+i+"_path = \""+e+"\"");
			}
			System.out.println("Number of files: "+SCList.size());
		}
	}
	
	public static void printSCList_className() {
		if (SCList.isEmpty()) {
			System.out.println("No files with IPs!");
		} else {
			int i = 0;
			for (String e : SCList) {
				i++;
				
				System.out.println(StrUtil.get_classname(e));
				// System.out.println("String file"+i+"_path = \""+e+"\"");
			}
			System.out.println("Number of files: "+SCList.size());
		}
	}

	public static void printSCList_dir_className() {
		if (SCList.isEmpty()) {
			System.out.println("No files with IPs!");
		} else {
			int i = 0;
			for (String e : SCList) {
				i++;
				System.out.print(StrUtil.get_folder_path(e)+",");
				//System.out.print(StrUtil.get_folder_path(e).replace(expPath, "")+",");
				System.out.print(StrUtil.get_classname(e)+"\n");
				// System.out.println("String file"+i+"_path = \""+e+"\"");
			}
			System.out.println("Number of files: "+SCList.size());
		}
	}
	
	public static void writetocvs_SCList_dir_className(String path) throws IOException {
		if (SCList.isEmpty()) {
			System.out.println("No files with IPs!");
		} else {
			List<String> lines = new ArrayList<>();

			for (String e : SCList) {
				lines.add(StrUtil.get_folder_path(e)+","+StrUtil.get_classname(e));
//				System.out.print(StrUtil.get_folder_path(e)+",");
//				System.out.print(StrUtil.get_classname(e)+"\n");
			}
			StrUtil.write_tofile(path, lines);
		}
	}
	static String jarFile="";
	
	public static String getJarFile() {
		return jarFile;
	}
	public static void setJarFile(String jarFile) {
		LocatingPIs.jarFile = jarFile;
	}
	// * L2 = files_jar_list
	public static void locate_vuls() {
		//Print Stats & collect PIs
		//TODO: Loop over SCList and use jarfile to slice and locate vuls!
		
	}
	
	public static boolean search_for_IPs(String path, boolean print) throws IOException {
		
		// Read the lines. Once an IP is found break (for speed up), and add to SCList for further analysis
		
		List<String> lines = StrUtil.read_lines_list(path);
		boolean import_exist =false;
		boolean IP_exist =false;
		
		for (String line: lines) {
			if(!import_exist) {
//				if(line.contains("import") && (line.contains("java.sql.*") || line.contains("java.sql.Statement") 
//						|| line.contains("javax.sql.*") || line.contains("javax.sql.Statement")) ) {
				if(line.contains("import") && (line.contains("java.sql.") || line.contains("javax.sql.") ) ) {
					if(print) System.out.println(line);
					import_exist = true;
				}
			}
			
			if(import_exist) {
				//line.contains(".createStatement(")
				if(line.contains(".executeQuery(")|| line.contains(".executeUpdate(")||line.contains(".execute(")
						||line.contains(".getConnection(")|| line.contains("createStatement")) {//++++ 02.28.23
					if(print) System.out.println(line);
					IP_exist = true;
					break;
				}
			}
		}
		if (import_exist && IP_exist) {
			if(print) {
				System.out.println(path);
				System.out.println("-----------------");
				}
			return true;
		}
		return false;
		
	}
	public static void visit_all_files(String srcDirPath, boolean print) throws IOException {
		// * loop over java project files
				File directoryPath = new File(srcDirPath);
				
				String[] contents = directoryPath.list();
				for (String content : contents) {
					if(content.endsWith(".java")) {
						// 1- Search the file for IPs, 
						//		 2- if there are IPs addToSCList  
						if(search_for_IPs(srcDirPath+"/"+content, print)) {
							addToSCList(srcDirPath+"/"+content);
						}
						
					}else {
						// check if it's a subfolder, add to the 
						
						File subdirectoryPath = new File(srcDirPath+"/"+content);
						if(subdirectoryPath.isDirectory()) {
							visit_all_files(srcDirPath+"/"+content, print);
						}
					}
					
				}
	}
	public static void analyze_file(String jarPath, String srcDirPath,  boolean print) throws IOException {
		// TODO: Use DCAFixer to locate vulnerabilities and count DP & DN
		// if(sig is vulnerable) DP++ else DN++
		
		
	}
	
	public static void test_one(String dirPath, String file, boolean print) throws IOException, JSQLParserException {
		String path = dirPath + "/" + file;
		List<String> key_query = new ArrayList<>();
		key_query = ExtractQuery.ExtractorExp1(path);
		
		if (key_query.size() > 0) {
			if(print)System.out.println("BRANCH 1");
			for (String kq : key_query) {
				String[] parts = kq.split(":");
				if (parts.length == 2) {
					if(print)System.out.println("BRANCH 2");
					String seed = parts[0];
					String query = parts[1];
					// Get signature
					Sig s = new Sig();
					boolean is_ps = false;
					if (seed.equals("prepareStatement")) {
						if(print)System.out.println("BRANCH 3 - PS");
						char sliceDirection = 'F';
						s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
								StrUtil.replace_questionmarks(query), print, null);
						is_ps = true;
//			System.out.println(s.toString());
					} else if (seed.equals("executeQuery") || seed.equals("executeUpdate")) {
						if(print)System.out.println("BRANCH 4 - EQ EU");
						char sliceDirection = 'B';
						s = SignSlice.sign_slice_querystring_exp1_temp(path, seed, sliceDirection,
								StrUtil.replace_questionmarks(query), print, null);
					}
					// System.out.println(content +" : "+s.toString());
					if (SignSlice.qp.get_qs_ql() == null) {if(print) {
						System.err.println("Parser wasn't able to parse the query." + SignSlice.qp.get_msg());}
					} else if (s.get_V() == '_') {// -,-,null
						if(print)System.out.println("BRANCH 5 " + s.toString());
						if (s.get_T() == '_') {
							System.out.println("** Secure (PS is used): "+s.toString());// -,-,null
						} else {
							System.out.println("** Secure (WL is used): "+s.toString());// -,?,[v,?]
						}
					} else if (s.get_V() == 'q' || s.get_V() == 'Q') {
						if (s.get_I().contains('v') && !s.get_I().contains('c') && !s.get_I().contains('t')) {
//				QV++; //Q,?,[v]
							System.out.println("** Column value is input: "+s.toString()); // Q,?,[v]
						} else if (!s.get_I().contains('v') && (s.get_I().contains('c') || s.get_I().contains('t'))) {
//				QCT = 0; //Q,?,[c&|t]
							System.out.println("** Column/Table name is input: "+s.toString()); // Q,?,[c&|t]
						} else if (s.get_I().contains('a')) {

							System.out.println("** All query is user input: "+s.toString()); // Q,?,[a]
						}
					} else if (s.get_V() == 'S') {
						if (is_ps) {
							System.out.println(
									"** Prepared stmt is used incorrectly (Column/Table name is user input)"+s.toString());// S,?,[c&|t]
						} else {
							System.out.println("** Wrong fucntion used with the query"+s.toString());// S,?,[v]
						}
					} else if (s.get_V() == 'F') {
//			Qparser++;
//			failedCasesFiles.add(content);
						
						System.out.println("**** Failed to parse the query:"+s.toString()+"\n" + query);
					}

				}
			}
		}

	}
	
	public static void analyze_project(String jarPath, String srcDirPath, String CsvPath, boolean print, int P, int N) throws IOException {
		System.out.println("*******************************");
		System.out.println("Scanning " +StrUtil.get_filename_no_extention(CsvPath)+"\n----------------------------");
		int  DP = 0, DN = 0;
		int TP = 0 , TN = 0 , FP = 0 , FN = 0, sample_size = 0; // Per file
		// ============= Clear gloabal vars.
		LocatingPIs.clearSCList();
//		Experimnet2.resetValues();
		// * Analyzing the whole project
		Instant start = Instant.now();
		// =============  1) Search for IPs
		LocatingPIs.visit_all_files(srcDirPath, print);
		Instant end = Instant.now();
//		Experimnet2_PIs.printSCList_className();
//		Experimnet2_PIs.printSCList_fullpaths();
		if(print)	LocatingPIs.printSCList_dir_className();
		// =============  2) Write files with IPs in csv file, then add manually 3 columns (vul?, notes, dcafixer results)
		if(print)	LocatingPIs.writetocvs_SCList_dir_className(CsvPath);
		
		System.out.println("Number of files: "+SCList.size());
		Duration timeElapsed = Duration.between(start, end);
		System.out.println("Time taken to find files with IPs: "+ timeElapsed.toMillis() +" milliseconds");
		System.out.println("Time taken to find files with IPs: "+ timeElapsed.toMillis()/1000 +" seconds");
		// =============  3) Locate vuls and count DP & DN
		// --------------- 1. Go over SCList for further analysis
		for(String file : SCList) {
			// TODO: --------------- 1.a) Compute DP, DN
			analyze_file(jarPath, file, print);	
		}
		
		// TODO: --------------- 2. Compute TP, TN, FP, FN
		// TODO: --------------- 3. Compute and print Accuracy
		//	computeAndPrintAccuracy (TP, TN, FP, FN, (N+P) , CsvPath.replace(".csv", ""));
		
		// ============= Add to all projects counters 
		All_DP = All_DP + DP; 
		All_DN = All_DN + DN;
		
		// =============  4)

	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Finding IPs ....");
		// TODO : For each project: input (project src folder)
//					write paths to a csv file
//					edit files to have 3 more colomns vul?, note, dcafixer results
		// Scan Hiberante files
//		String proj1_src = "/Users/Dareen/Desktop/DCAFixer_Experimets/hibernate-orm-main";
//		String proj1_jar = "/Users/Dareen/Desktop/DCAFixer_Experimets/hibernate-orm-main.jar";
//		String proj1_Csv = "/Users/Dareen/Desktop/DCAFixer_Experimets/Csv_files/" + StrUtil.get_classname(proj1_src)+".csv";
//		analyzing_project(proj1_jar, proj1_src, proj1_Csv, true);

		//================= Accuracy vars ==================
//		int N, P, DN, DP, TP, TN, FP, FN; // Per file
//		int All_N, All_P, All_DN, All_DP, All_TP, All_TN, All_FP, All_FN;
		int N = 0 , P = 0 , DN = 0 , DP = 0 , TP = 0 , TN = 0 , FP = 0 , FN = 0, sample_size = 0; // Per file
		int All_DN = 0 , All_DP = 0 , All_TP = 0 , All_TN = 0 , All_FP = 0 , All_FN = 0;
		int All_N = 1855 , All_P = 332; 
		int All_sample_size = All_N + All_P; //2187
		
		//sample_size = N + P; // For each file
		
		//--------------------------------------------------
		
		
		String CsvDir = "/Users/Dareen/Desktop/DCAFixer_Experimets/Excel_files/";
//		String proj1Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src";
//		String proj1Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app.jar";
//		String proj1Csv = CsvDir + "banking-app.csv";
//		P= 4; N = 11; 
//		analyze_project(proj1Jar, proj1Src, proj1Csv, false, P, N);
//
//		
//		String proj2Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace";
//		String proj2Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/BookStore.jar";
//		String proj2Csv = CsvDir + "BookStore.csv";
//		P= 46; N = 0; 
//		analyze_project(proj2Jar, proj2Src, proj2Csv, false, P, N);
//
//		
//		String proj3Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/crawledemo-master/property/src/com/property";
//		String proj3Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/crawledemo_property.jar";
//		String proj3Csv = CsvDir + "crawledemo_property.csv";
//		P= 3; N = 0; 
//		analyze_project(proj3Jar, proj3Src, proj3Csv, false, P, N);
//
//
//		String proj4Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/Ecomerce--master/Ecomerce/src/ecomerce";
//		String proj4Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/Ecomerce.jar";
//		String proj4Csv = CsvDir + "Ecomerce.csv";
//		P= 79; N = 15; 
//		analyze_project(proj4Jar, proj4Src, proj4Csv, false, P, N);
//
//
//		String proj5Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/fiftyfiftystockscreener-master/src/com/eddiedunn";
//		String proj5Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/fiftyfiftystockscreener.jar";
//		String proj5Csv = CsvDir + "fiftyfiftystockscreener.csv";
//		P= 32; N = 4; 
//		analyze_project(proj5Jar, proj5Src, proj5Csv, false, P, N);
//
//
//		String proj6Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/GUI-DBMS-master/TableEasy/src/tableeasy";
//		String proj6Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/GUI-DBMS.jar";
//		String proj6Csv = CsvDir + "GUI-DBMS.csv";
//		P= 33; N = 20; 
//		analyze_project(proj6Jar, proj6Src, proj6Csv, false, P, N);
//
//
//		String proj7Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/InventarioWeb-master/src/java";
//		String proj7Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/InventarioWeb.jar";
//		String proj7Csv = CsvDir + "InventarioWeb.csv";
//		P= 1; N = 31; 
//		analyze_project(proj7Jar, proj7Src, proj7Csv, false, P, N);
//
//
//		String proj8Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/JavaGit-master/src";
//		String proj8Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/JavaGit-master.jar";
//		String proj8Csv = CsvDir + "JavaGit-master.csv";
//		P= 2; N = 1;
//		analyze_project(proj8Jar, proj8Src, proj8Csv, false, P, N); 
//
//		
//		String proj9Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/mariadb-connector-j-master/src";
//		String proj9Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/mariadb.jar";
//		String proj9Csv = CsvDir + "mariadb.csv";
//		P= 18; N = 1744; 
//		analyze_project(proj9Jar, proj9Src, proj9Csv, false, P, N);
//
//
//		String proj10Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src";
//		String proj10Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/TFG_PC-master.jar";
//		String proj10Csv = CsvDir + "TFG_PC-master.csv";
//		P= 77; N = 1;
//		analyze_project(proj10Jar, proj10Src, proj10Csv, false, P, N); 
//
//
//		String proj11Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/vSync-master/vSync/src";
//		String proj11Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/vSync.jar";
//		String proj11Csv = CsvDir + "vSync.csv";
//		P= 20; N = 1;
//		analyze_project(proj11Jar, proj11Src, proj11Csv, false, P, N); 
//
//
//		String proj12Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/zjjava-master";
//		String proj12Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/zjjavaJDBC.jar";
//		String proj12Csv = CsvDir + "zjjavaJDBC.csv";
//		P= 3; N = 6;
//		analyze_project(proj12Jar, proj12Src, proj12Csv, false, P, N);

		
		//====================
		String projCsv;
//		String proj01Src= "/Users/Dareen/NetBeansProjects/DCAFixer_bookstore2/src"; 
//		String proj01Jar= "/Users/Dareen/NetBeansProjects/DCAFixer_bookstore2/dist/DCAFixer_bookstore2.jar";
//		projCsv = CsvDir +"DCA_bookstore.csv";
//		P= 1; N = 5;
//		analyze_project(proj01Jar, proj01Src, projCsv, false, P, N);
//
//		
//		String proj02Src= "/Users/Dareen/NetBeansProjects/DCAFixer_NewsPaper/src/dcafixer_newspaper";
//		String proj02Jar= "/Users/Dareen/NetBeansProjects/DCAFixer_NewsPaper/dist/DCAFixer_NewsPaper.jar";
//		projCsv = CsvDir + "DCA_NewsPaper.csv";
//		P= 5; N =9; 
//		analyze_project(proj02Jar, proj02Src, projCsv, false, P, N);
//
//		
//		String proj03Src= "/Users/Dareen/NetBeansProjects/DCAFixer_FuelOrdersClient/src/fuelordersclient"; 
//		String proj03Jar= "/Users/Dareen/NetBeansProjects/DCAFixer_FuelOrdersClient/dist/DCAFixer_FuelOrdersClient2.jar";
//		projCsv = CsvDir + "DCA_FuelOrders.csv";
//		P= 5; N = 2; 
//		analyze_project(proj03Jar, proj03Src, projCsv, false, P, N);
//		
//		
//		String proj04Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/hospital-database-with-JDBC-client-master/JDBC_CLIENT/src";
//		String proj04Jar= "/Users/Dareen/Desktop/DCAFixer_Experimets/Github_Dataset/Used_in_Fixer/hospital-database-with-JDBC-client-master/JDBC_CLIENT/dist/DCAFixer_Hospital.jar";
//		projCsv = CsvDir + "DCA_hospital.csv";
//		P= 3; N = 5; 
//		analyze_project(proj04Jar, proj04Src, projCsv, false, P, N);
		
		
		//============== Test Dataset ==================
//		String test01Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/EPL441Clinic-master";
//		String test01Jar= "/Users/Dareen/Fixer/Experiments/JARS/Clinic.jar";
//		projCsv = CsvDir + "EPL441Clinic.csv";
//		P= 0; N = 0; 
//		analyze_project(test01Jar, test01Src, projCsv, true, P, N);
		
		String test02Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/Hotel-Management---Java-master/src";
		String test02Jar= "/Users/Dareen/Fixer/Experiments/JARS/Hotel-Management.jar";
		projCsv = CsvDir + "Hotel-Management.csv";
		P= 0; N = 0; 
		analyze_project(test02Jar, test02Src, projCsv, true, P, N);
		
		String test03Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/iTrust-v23-master/iTrust/src";
		String test03Jar= "/Users/Dareen/Fixer/Experiments/JARS/iTrust.jar";
		projCsv = CsvDir + "iTrust-v23.csv";
		P= 0; N = 0; 
		analyze_project(test03Jar, test03Src, projCsv, true, P, N);
	
		String test04Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/Java_BBDD-master/src";
		String test04Jar= "/Users/Dareen/Fixer/Experiments/JARS/Java_BBDD.jar";
		projCsv = CsvDir + "Java-BBDD.csv";
		P= 0; N = 0; 
		analyze_project(test04Jar, test04Src, projCsv, true, P, N);
		
		String test05Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/java-homework-10-master/src";
		String test05Jar= "/Users/Dareen/Fixer/Experiments/JARS/homework-10.jar";
		projCsv = CsvDir + "homework-10.csv";
		P= 0; N = 0; 
		analyze_project(test05Jar, test05Src, projCsv, true, P, N);
		
		String test06Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/Momow1-master/src";
		//"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/Momow1-master/src/java";
		String test06Jar= "/Users/Dareen/Fixer/Experiments/JARS/Momow1.jar";
		projCsv = CsvDir + "Momow1.csv";
		P= 0; N = 0; 
		analyze_project(test06Jar, test06Src, projCsv, true, P, N);
		
		String test07Src= "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/Naegling-GUI-master/src";
		String test07Jar= "/Users/Dareen/Fixer/Experiments/JARS/NaeglingGUI.jar";
		projCsv = CsvDir + "Naegling-GUI.csv";
		P= 0; N = 0; 
		analyze_project(test07Jar, test07Src, projCsv, true, P, N);
		
		//-----------------------------------------------------
//		String f1 = "/Users/Dareen/Desktop/DCAFixer_Experimets/hibernate-orm-main/hibernate-core/src/test/java/org/hibernate/orm/test/cache/RefreshUpdatedDataTest.java";
//		search_for_IPs(f1, true);
		
		//================= Accuracy Computation ==================
		// TODO: Compute All_TP, All_TN, All_FP, All_FN
		computeAndPrintAccuracy (All_TP, All_TN, All_FP, All_FN, All_sample_size ,  "Total");
//		Double Precision = All_TP * 1.0 / (All_TP+ All_FP);
//		Double	Recall = All_TP * 1.0 / (All_TP + All_FN);
//		Double	Accuracy = (All_TP + All_TN) * 1.0 / (All_sample_size);
//		Double F1_Score = 2 * (Precision * Recall) / (Precision + Recall);
//		//--------------------------------------------------
//		System.out.println("Total Precision: " + Precision);
//		System.out.println("Total Recall: "+ Recall);
//		System.out.println("Total Accuracy: "+ Accuracy);
//		System.out.println("Total F1_Score: "+ F1_Score);
		

	}

	public static void computeAndPrintAccuracy (int TP, int TN, int FP, int FN, int sample_size ,  String projectName) {
		Double Precision = TP * 1.0 / (TP+ FP);
		Double	Recall = TP * 1.0 / (TP + FN);
		Double	Accuracy = (TP + TN) * 1.0 / (sample_size);
		Double F1_Score = 2 * (Precision * Recall) / (Precision + Recall);		

		//--------------------------------------------------
		System.out.println(" ----------- "+projectName+" Results -----------  "); 
		System.out.println("\tPrecision: " + Precision);
		System.out.println("\tRecall: "+ Recall);
		System.out.println("\tAccuracy: "+ Accuracy);
		System.out.println("\tF1_Score: "+ F1_Score);
	}
}
