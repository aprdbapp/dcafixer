package slicer.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Sig {
	// TODO: Create a global list of all signatures and connect them to the files of
	// data set!
	public static char V; //V - old S
	public static char T;// T -old Q
	public static List<Character> I = new ArrayList<>();//I old T
//	public List<String> Tables = new ArrayList<>();
//	public List<String> Columns = new ArrayList<>();
	// TODO - get assosiated test cases - return files names
	public Sig(){
//		V = '_';
//		T = '_';
//		I.clear();
		setvalues('_', '_', null);
	}
	
	public char get_V() {
		return V;
	}
	public char get_T() {
		return T;
	}
	public List<Character> get_I() {
		return I;
	}
	
	
	public void set_V(char v) {
		V = v;
	}
	public void set_T(char t) {
		T = t;
	}
	public  void addto_I(char i) {
		I.add(i);
	}
	public  void clear_I() {
		I.clear();
	}
	
	public boolean equals(Sig s1, Sig s2) {
		// TODO:compare 2 signatures
		return false;
	}
	public  void setvalues(char s, char q, List<Character> Is) {
		V = s;
		T = q;
		if(Is == null || Is.size() == 0)
			I.clear();
		else
			I = new ArrayList<>(Is);
		
	}
	
	public String toString() {
		// "signature(S,Q,[T1,T2 ..]): " + S + "," + Q + "," + formatT().toString();
		String str = " " + V + "," + T + "," + formatI();//.toString();
		return str.replace(" ", "");
	}

//	public String formatI() {
//		String input ="";
//		Collections.sort(I);
//		int i = 0;
//		for(char c : I) {
//			if(i==0)
//				input = c+"";
//			else
//				input = input + ","+c;
//			i++;
//		}
//		if(I.size()>1)
//			input = "["+input+"]";
//		return input;
//	}
	public List<Character> formatI() {
		Collections.sort(I);
		return I;
	}
	
	
	
	public void convert_to_sig(String str_sig) {
//		char s='-'; 
//		char q='-';
		List<Character> Is = new ArrayList<>(); 
		String [] sig_parts = str_sig.split(",",3);
		if(sig_parts.length == 3) {
//			s= sig_parts[0].charAt(0);
//			q= sig_parts[1].charAt(0);
			if(sig_parts[2].startsWith("[")) {
				String I_list = sig_parts[2].substring(1, sig_parts[2].length() - 1);
				String [] I_parts = I_list.split(",");
				for(String Ip : I_parts) {
					Is.add(Ip.trim().charAt(0));
				}
			}else {
				Is.add(sig_parts[2].trim().charAt(0));
			}
			setvalues(sig_parts[0].trim().charAt(0),sig_parts[1].trim().charAt(0),Is);
		}
//		setvalues(s,q,ts);
		
	}
	public static List<ArrayList<Character>> subStrings(char str[], int n) {
		// Pick starting point
		List<ArrayList<Character>> all_ss = new ArrayList<ArrayList<Character>>();

		for (int len = 1; len <= n; len++) {
			// Pick ending point
			for (int i = 0; i <= n - len; i++) {
				String temp = "";
				int j = i + len - 1;
				for (int k = i; k <= j; k++) {
					temp = temp + str[k];
//                    System.out.print(str[k]);
				}
				ArrayList<Character> t = new ArrayList<>(
						temp.chars().mapToObj(c -> (char) c).collect(Collectors.toList()));
				all_ss.add(t);
//                System.out.println(temp);
			}
		}
		return all_ss;
	}

	public static void print_all_signatures() {
		char[] all_V = { 'Q', 'P', 'C', 'E' };
		char[] all_T1 = { 's', 'u', 'd', 'i' };// DML queries
		// TODO: study the difference between them
		char Q2 = '_';
		char[] i1 = { 't', 'c', 'v' };
		char[] i2 = { 'h', 'd' };
		char[] i3 = { 'u', 'r' };
		char[] i4 = { 'n', 'e', '_' };
		char i5 = '_';

		for (char v : all_V) {
			//**** In all the comments convert S->V, Q->T, T->I
			// TODO: build a function that create all possible combinations of T1, T2, ...
			// S,Q,T
			if (v == 'Q') {

				// 'Q', all_Q1, T1[]|T5
				for (char q1 : all_T1) {
					// T1[]
					List<ArrayList<Character>> T1_ss = subStrings(i1, i1.length);
					for (ArrayList<Character> t1 : T1_ss) {
						Sig temp_sig = new Sig();
						temp_sig.V = v;
						temp_sig.T = q1;
						temp_sig.I.addAll(t1);// *** create_all_combinations
						System.out.println(temp_sig.toString());
					}
					// T5
					Sig temp_sig = new Sig();
					temp_sig.V = v;
					temp_sig.T = q1;
					temp_sig.I.add(i5);// *** create_all_combinations
					System.out.println(temp_sig.toString());

				}
			}
			if (v == 'P') {
				// 'P', Q2, T2[]|T5
				// T2[]
				List<ArrayList<Character>> T2_ss = subStrings(i2, i2.length);
				for (ArrayList<Character> t2 : T2_ss) {

					Sig temp_sig = new Sig();
					temp_sig.V = v;
					temp_sig.T = Q2;
					temp_sig.I.addAll(t2);
					System.out.println(temp_sig.toString());
				}
				// T5
				Sig temp_sig = new Sig();
				temp_sig.V = v;
				temp_sig.T = Q2;
				temp_sig.I.add(i5);// *** create_all_combinations
				System.out.println(temp_sig.toString());

			}
			if (v == 'C') {
				// 'C', Q2, T3[]
				for (char t3 : i3) {
					Sig temp_sig = new Sig();
					temp_sig.V = v;
					temp_sig.T = Q2;
					temp_sig.I.add(t3);
					System.out.println(temp_sig.toString());
				}

			}
			if (v == 'E') {
				// 'E', Q2, T4[]
				for (char t4 : i4) {
					Sig temp_sig = new Sig();
					temp_sig.V = v;
					temp_sig.T = Q2;
					temp_sig.I.add(t4);
					System.out.println(temp_sig.toString());
				}

			}

		}
	}
}

// * S stands for vulnerability Slice Where : S = {'Q','P','C','E' } 
// * 'Q' = Query FW&BW seeds(executeQuery,..) - to find Unsanitized User Input 
// * 'P' = Password BW seeds(getConnection) - to find Credentials Handling 
// * 'C' = Connection FW seeds(getConnection) - find Unclosed Connection 
// * 'E' = Encrypted Connection BW seeds(getConnection) - To find connection url and point unencrypted ones

// * Q stands for Query type
// * When S = 'Q' , Q1 = {'s','u','d','i'} 
// * 		s = Select ,u = Update, d = Delete, i =
// * Insert With other values of S = {'P','C','E'} , Q2 = {'_'} 
// * 		'_' = none

// * T stands for Type of vulnerability 
// * With S = 'Q', T1 = {'t','c', 'v', '_'} 
// * 		t = table name, c = column name, v= column value, '_' = none 
// * With S = 'P', T2 = {'h','d', '_'} 
// * 		h = hard coded, d= bad data type(e.g., String), '_' = none
// * With S = 'C', T3 = {'u', 'r' } 
// * 		u = unreleased connection, r= released connection 
// * With S = 'E', T4 = {'n', 'e', '_' } 
// * 		n = no encryption, e = encrypted, '_' = unknown

/*
 * All possible signatures are
 */

//Q,s,[t]
//Q,s,[c]
//Q,s,[v]
//Q,s,[c, t]
//Q,s,[c, v]
//Q,s,[c, t, v]
//Q,s,[_]
//Q,u,[t]
//Q,u,[c]
//Q,u,[v]
//Q,u,[c, t]
//Q,u,[c, v]
//Q,u,[c, t, v]
//Q,u,[_]
//Q,d,[t]
//Q,d,[c]
//Q,d,[v]
//Q,d,[c, t]
//Q,d,[c, v]
//Q,d,[c, t, v]
//Q,d,[_]
//Q,i,[t]
//Q,i,[c]
//Q,i,[v]
//Q,i,[c, t]
//Q,i,[c, v]
//Q,i,[c, t, v]
//Q,i,[_]
//P,_,[h]
//P,_,[d]
//P,_,[d, h]
//P,_,[_]
//C,_,[u]
//C,_,[r]
//E,_,[n]
//E,_,[e]
//E,_,[_]
