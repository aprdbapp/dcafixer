package flocalization;

import java.util.ArrayList;

import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;

public class TSet {
	public static ArrayList<TExample> Training_set = new ArrayList<TExample>();

	public static void set_TSet_MD() {

		TExample e1 = new TExample(1, "PWstr_vul_d.java", "PW_sec.java", "p,d,_");
		ArrayList<String> keys1 = new ArrayList<>();
		keys1.add("getConnection:B");// key = getConnection , direction = BW
		e1.set_keys(keys1);
//					ArrayList<SliceLine> SSlice1 = new ArrayList<SliceLine>();
//					e1.set_SSlice(SSlice1);
		Training_set.add(e1);
		// ---------------------

		TExample e2 = new TExample(2, "PWstr_vul_h.java", "PW_sec.java", "p,h,_");
		ArrayList<String> keys2 = new ArrayList<>();
		keys2.add("getConnection:B");// key = getConnection , direction = BW
		e2.set_keys(keys2);
//					ArrayList<SliceLine> SSlice2 = new ArrayList<SliceLine>();
//					e2.set_SSlice(SSlice2);
		Training_set.add(e2);
		// ---------------------

		TExample e3 = new TExample(3, "SQLIV_q_s_v.java", "PW_sec.java", "q,s,v"); // ???
		ArrayList<String> keys3 = new ArrayList<>();
		keys3.add("executeQuery:B");// key = executeQuery, direction = BW
		keys3.add("prepareStatement:B");// key = prepareStatement, direction = BW
		keys3.add("executeUpdate:B");// key = executeUpdate, direction = BW
		e3.set_keys(keys3);
//					ArrayList<SliceLine> SSlice3 = new ArrayList<SliceLine>();
//					e3.set_SSlice(SSlice3);
		Training_set.add(e3);
		// ---------------------

		TExample e4 = new TExample(4, "SQLIV_q_s_v.java", "SQLIV_q_s_v_fixed.java", "q,s,v");
		ArrayList<String> keys4 = new ArrayList<>();
		keys4.add("executeQuery:B");// key = executeQuery, direction = BW
		keys4.add("prepareStatement:B");// key = prepareStatement, direction = BW
		keys4.add("executeUpdate:B");// key = executeUpdate, direction = BW
		e4.set_keys(keys4);
//					ArrayList<SliceLine> SSlice4 = new ArrayList<SliceLine>();
//					e4.set_SSlice(SSlice4);
		Training_set.add(e4);
		// ---------------------

		TExample e5 = new TExample(5, "UConn_vul.java", "UConn_Sec.java", "c,u,_");
		ArrayList<String> keys5 = new ArrayList<>();
		keys5.add("getConnection:F");// key = getConnection , direction = FW
		e5.set_keys(keys5);
//					ArrayList<SliceLine> SSlice5 = new ArrayList<SliceLine>();
//					e5.set_SSlice(SSlice5);
		Training_set.add(e5);
		// ---------------------

		TExample e6 = new TExample(6, "UConn_vul.java", "UConn_Sec2.java", "c,u,_");
		ArrayList<String> keys6 = new ArrayList<>();
		keys6.add("getConnection:F");// key = getConnection , direction = FW
		e6.set_keys(keys6);
//					ArrayList<SliceLine> SSlice6 = new ArrayList<SliceLine>();
//					e6.set_SSlice(SSlice6);
		Training_set.add(e6);
		// ---------------------

		TExample e7 = new TExample(7, "SQLIV_q_s_all.java", "", "q,_,a"); // No solution
		ArrayList<String> keys7 = new ArrayList<>();
		keys7.add("executeQuery:B");// key = executeQuery, direction = BW
		keys7.add("prepareStatement:B");// key = prepareStatement, direction = BW
		keys7.add("executeUpdate:B");// key = executeUpdate, direction = BW
		e7.set_keys(keys7);
//					ArrayList<SliceLine> SSlice7 = new ArrayList<SliceLine>();
//					e7.set_SSlice(SSlice7);
		Training_set.add(e7);
		// ---------------------

		TExample e8 = new TExample(8, "SQLIV_q_s_t_cv.java", "SQLIV_q_s_t_cv_SEC.java", "q,s,t");// sql2
		ArrayList<String> keys8 = new ArrayList<>();
		keys8.add("executeQuery:B");// key = executeQuery, direction = BW
		keys8.add("prepareStatement:B");// key = prepareStatement, direction = BW
		keys8.add("executeUpdate:B");// key = executeUpdate, direction = BW
		e8.set_keys(keys8);
//					ArrayList<SliceLine> SSlice8 = new ArrayList<SliceLine>();
//					e8.set_SSlice(SSlice8);
		Training_set.add(e8);
		// ---------------------

		TExample e9 = new TExample(9, "SQLIV_q_s_t_cv.java", "SQLIV_q_s_t_cv_SEC.java", "q,s,[c,v]");// sql1
		ArrayList<String> keys9 = new ArrayList<>();
		keys9.add("executeQuery:B");// key = executeQuery, direction = BW
		keys9.add("prepareStatement:B");// key = prepareStatement, direction = BW
		keys9.add("executeUpdate:B");// key = executeUpdate, direction = BW
		e9.set_keys(keys9);
//					ArrayList<SliceLine> SSlice9 = new ArrayList<SliceLine>();
//					e9.set_SSlice(SSlice9);
		Training_set.add(e9);
		// ---------------------

		TExample e10 = new TExample(10, "Unencrypted_vul_prop.java", "Unencrypted_sec_prop.java", "c,e,p");
		ArrayList<String> keys10 = new ArrayList<>();
		keys10.add("getConnection:B");// key = getConnection , direction = BW
		keys10.add("Properties:F");// key = Properties , direction = FW
		e10.set_keys(keys10);
//					ArrayList<SliceLine> SSlice10 = new ArrayList<SliceLine>();
//					e10.set_SSlice(SSlice10);
		Training_set.add(e10);
		// ---------------------

		TExample e11 = new TExample(11, "Unencrypted_vul_link.java", "PW_sec.java", "c,e,l");
		ArrayList<String> keys11 = new ArrayList<>();
		keys11.add("getConnection:B");// key = getConnection , direction = BW
		keys11.add("Properties:F");// key = Properties , direction = FW
		e11.set_keys(keys11);
//					ArrayList<SliceLine> SSlice11 = new ArrayList<SliceLine>();
//					e11.set_SSlice(SSlice11);
		Training_set.add(e11);
		// ---------------------

	}

	public static void find_TSet_SSlices() {
		String appSrc_path = "/Users/Dareen/NetBeansProjects/smallBank/src/TSet/";
		DataDependenceOptions dOptions, dOptions2;
		ControlDependenceOptions cOptions, cOptions2;
		dOptions = DataDependenceOptions.NO_HEAP;
		cOptions = ControlDependenceOptions.NONE;

		dOptions2 = DataDependenceOptions.NO_HEAP;
		cOptions2 = ControlDependenceOptions.NONE;

		String sig;
		String subFolder;
		for (TExample e : Training_set) {
			sig = e.sig.replace(" ", "").replace(",", "_");
			subFolder = getSubFolderName(sig);

		}

	}

	public static void create_TExample_Object(int i) {
		System.out.println("TExample e" + i + " = new TExample(" + i + ",\"v?.java\", \"s?.java\", \"sig\");");
		System.out.println("//		ArrayList<String> keys" + i + " = new ArrayList<>();");
		System.out.println("//		keys" + i + ".add(\"?\");");
		System.out.println("//		e" + i + ".set_keys(keys" + i + ");");
		System.out.println("//		ArrayList<SliceLine> SSlice" + i + " = new ArrayList<SliceLine>();");
		System.out.println("//		e" + i + ".set_SSlice(SSlice" + i + ");");
		System.out.println("Training_set.add(e" + i + ");\n//---------------------\n");

	}

	public static String getSubFolderName(String sig) {
		String subFolder = "";
		if (sig.startsWith("q")) {
			subFolder = "SQLIV";
		} else if (sig.startsWith("p")) {
			subFolder = "PW";
		} else if (sig.startsWith("c,u")) {
			subFolder = "CONN";
		} else if (sig.startsWith("c,e")) {
			subFolder = "ENC";
		}
		return subFolder;
	}

	public static void main(String[] args) {
		// Set TSet MD : files paths, keys and slice direction
		set_TSet_MD();
		// Store SSlices
		find_TSet_SSlices();
		for (int i = 0; i < Training_set.size(); i++) {
			System.out.println(Training_set.get(i).toString());
		}

	}
}

// sig(V,T,I)
//* V denotes the type of vulnerability slice; Where : V = {'q','p','c'} //'E' 
//* 'Q' = Query FW&BW seeds(executeQuery,..) - to find Unsanitized User Input 
//* 'P' = Password BW seeds(getConnection) - to find Credentials Handling 
//* 'C' = Connection FW seeds(getConnection) - find Unclosed Connection 
// 		  || Encrypted Connection BW seeds(getConnection) - To find connection url and point unencrypted ones ('E')

//* T represents  either  type  of  the  query or the type of the vulnerability 
//* When V = 'q' , T = {'s','u','d','i'} 
//* 	s = Select ,u = Update, d = Delete, i = Insert
//* When V = 'p' , T = {'h','d'} 
//* 	h = Hard coded , d = Data type is wrong
//* When V = 'c' , T = {'u','e'} 
//* 	u = unclosed  connection , e = unencrypted  connection

//* I is used mostly with SQLIVs. It shows which part of the query is user-input
//* When V ='q', I = {'t','c', 'v', 'a'}
//		t = table name, c = column name, v= column value, a = all query
//* When V = 'c', I= {'p', 'l'}
//		p = Properties is used to handle credentials, l= link is used to send credentials	
//* When V = {'p'} , I = {'_'} 
// 		'_' = none
