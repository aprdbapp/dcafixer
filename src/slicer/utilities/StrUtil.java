package slicer.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import flocalization.G;
import slicer.datatypes.CodeLine;
import slicer.datatypes.SrcCode;

public class StrUtil {
	
	public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) 
    { 
  
        // Create a new ArrayList 
        ArrayList<T> newList = new ArrayList<T>(); 
  
        // Traverse through the first list 
        for (T element : list) { 
  
            // If this element is not present in newList 
            // then add it 
            if (!newList.contains(element)) { 
  
                newList.add(element); 
            } 
        } 
  
        // return the new list 
        return newList; 
    } 
	public static int countOccurrencesOf (String s, char c) {
		String someString = s;
		char someChar = c;
		int count = 0;
		 
		for (int i = 0; i < someString.length(); i++) {
		    if (someString.charAt(i) == someChar) {
		        count++;
		    }
		}
		return count;
	}
	// ConnIPList
	public static boolean isConnIP(String str) {
        return G.ConnIPList.contains(str);
    }

	public static boolean containsConnIP(String str) {
		for (String cip : G.ConnIPList) {
			if (str.contains(cip))
				return true;
		}
		return false;
	}

	// ExIPList
	public static boolean isExIP(String str) {
        return G.ExIPList.contains(str);
    }

	public static boolean containsExIP(String str) {
		for (String exip : G.ExIPList) {
			if (str.contains(exip))
				return true;
		}
		return false;
	}

	// IPList
	public static boolean isIP(String str) {
        return G.IPList.contains(str);
    }

	public static boolean containsIP(String str) {
		for (String ip : G.IPList) {
			if (str.contains(ip))
				return true;
		}
		return false;
	}

	// UIPointsList
	public static boolean isUIPoint(String str) {
        return G.UIPointsList.contains(str);
    }

	public static boolean containsUIPoint(String str) {
		for (String ui : G.UIPointsList) {
			if (str.contains(ui))
				return true;
		}
		return false;
	}

	// ==========================
 	public static String sig_removecommas(String sig) {
		
		String [] parts = sig.split("\\[");
//		System.out.println(parts.length);
//		System.out.println(parts[0]);
		String new_sig= parts[0].replace(",", "_")+parts[1].replace("]", "").replace(",", "");
		return new_sig.toLowerCase();
//		return null;
	}
 	
 	

public static String get_path_class_from_src(String appSrc, int cpLength){
 		String[] subpath = appSrc.split("/");
		

		String className_only = (subpath[subpath.length - 1].split("\\."))[0];// subpath[subpath.length - 1].replace(".java", "");
		String path_className ="";
		String classPath = "";
		if(cpLength == 1) {
//			//----------------------- Option 1
			classPath = subpath[subpath.length - 2] + "/";// TSet/
			path_className =classPath +className_only;

		}
		else if(cpLength == 2) {
//			//----------------------- Option 2
			classPath = subpath[subpath.length - 3] + "/"+subpath[subpath.length - 2] + "/";// TSet/
			path_className =classPath +className_only;

		}
		else if(cpLength == 3) {
			//----------------------- Option 3 - "src" folder			
			boolean src_found = false;
			for(int i =0; i< subpath.length-1;i++) {
				if(src_found)
					path_className = path_className +subpath[i] + "/";
				if(subpath[i].equals("src"))//For other cases
					src_found = true;
			}
			if(!src_found)
				System.err.println("NO \"src\" folder in the path");
			path_className = path_className + className_only;
			//-----------------------
		}
		else {
			//----------------------- Option N			
			boolean src_found = false;
			for(int i =0; i< subpath.length-1;i++) {
				if(src_found)
					path_className = path_className +subpath[i] + "/";
//				if(subpath[i].equals("src"))//For other cases
				if(subpath[i].equals("java"))// for mariadb
					src_found = true;
			}
			if(!src_found)
				System.err.println("NO \"java\" folder in the path");
			path_className = path_className + className_only;
			//-----------------------
			
		}
		return path_className;
 		
 	}
	
	
 	/**
	 * 
	 * @return from a fullPath i.e., (f1/../fn/file.ext) , it returns (f1/../fn) 
	 * */
	public static String get_folder_path(String fullPath) {

		String[] parts = fullPath.split("/");
		String classname = parts[parts.length - 1];
		String folder_path = fullPath.replace("/" + classname, "");

		return folder_path;
	}

	/**
	 * 
	 * @return from a fullPath i.e., (f1/../fn/file.ext) , it returns (file.ext) 
	 * */
	public static String get_filename(String fullPath) {

		String[] parts = fullPath.split("/");
		String classname = parts[parts.length - 1];

		return classname;
	}

	/**
	 * 
	 * @return from a fullPath i.e., (f1/../fn/file.ext) , it returns (file) 
	 * */
	public static String get_filename_no_extention(String fullPath) {
		String filename = null;
		String[] parts = fullPath.split("/");
		filename =  parts[parts.length - 1];
		if (filename.contains(".")) {
			String[] parts2 = filename.split("\\.");
			if (parts2.length == 2) {
				filename = parts2[0];
				}
		}
		return filename;
	}
	/**
	 * 
	 * @return from a fullPath i.e., (f1/../fn/file.ext) , it returns (file) 
	 * */
	public static String get_classname(String fullPath) {

		return get_filename(fullPath).replace(".java", "");
	}

	public static String replace_questionmarks(String text) {

		text = text.replace("?\"", "#");
//		System.out.println(text);
		String word1 = "?";
		String word2 = "#";
		int newvar = 0;
		// query_str = query_str.replace(" ? ", " \"+ X +\" ").replace(" ?\"", " \"+ Y
		// ");
		for (int i = -1; (i = text.indexOf(word1, i + 1)) != -1; i++) {
//			System.out.println(i);
			text = text.substring(0, i) + "\"+ X" + newvar + " +\"" + text.substring(i + word1.length());
			newvar++;
		} // prints "4", "13", "22"
		for (int i = -1; (i = text.indexOf(word2, i + 1)) != -1; i++) {
//			System.out.println(i);
			text = text.substring(0, i) + "\"+ X" + newvar + text.substring(i + word2.length());
			newvar++;
		}

		return text;

	}

	static boolean twoStringsOverlap(String s1, String s2) {
		int MAX_CHAR = 100;
		// vector for storing character occurrences
		boolean[] v = new boolean[MAX_CHAR];
		Arrays.fill(v, false);

		// increment vector index for every
		// character of str1
		for (int i = 0; i < s1.length(); i++)
			v[s1.charAt(i) - 'a'] = true;

		// checking common substring of str2 in str1
		for (int i = 0; i < s2.length(); i++)
			if (v[s2.charAt(i) - 'a'])
				return true;

		return false;
	}

	// ----------------------------------
	// Java implementation of finding length of longest
	// Common substring using Dynamic Programming

	/*
	 * Returns length of longest common substring of X[0..m-1] and Y[0..n-1]
	 */
	static int LCSubStr(String strX, String strY) {
		char[] X = (strX.replaceAll("[-+^];.<>@()_=/:*", "")).toCharArray();
		char[] Y = strY.toCharArray();
		int m = strX.length();
		int n = strY.length();
		String commen = "";
		// Create a table to store lengths of longest common suffixes of
		// substrings. Note that LCSuff[i][j] contains length of longest
		// common suffix of X[0..i-1] and Y[0..j-1]. The first row and
		// first column entries have no logical meaning, they are used only
		// for simplicity of program
		int[][] LCStuff = new int[m + 1][n + 1];
		int result = 0; // To store length of the longest common substring

		// Following steps build LCSuff[m+1][n+1] in bottom up fashion
		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n; j++) {
				if (i == 0 || j == 0)
					LCStuff[i][j] = 0;
				else if (X[i - 1] == Y[j - 1]) {
					commen = commen + X[i - 1];
					LCStuff[i][j] = LCStuff[i - 1][j - 1] + 1;
					result = Integer.max(result, LCStuff[i][j]);
				} else
					LCStuff[i][j] = 0;
			}
		}
		System.err.println(commen + "~");
		return result;
	}

	static int findCommonStr(String stmt, String codeLine, String delima) {
		String commonStr = "";
//			System.out.println(" commonStr length Before: "+commonStr.length());
		String[] tokens = stmt.split("[" + delima + "]");
		for (String token : tokens) {
//		         System.err.println(token);		     
			if (codeLine.contains(token.replace(" ", ""))) {
				commonStr = token;
//		        	 System.err.println("~~~~~~~~~"+commonStr+"~");
				System.err.println("~~~ commonStr After: (" + commonStr + "), Length =[" + commonStr.length() + "]\n");
				return commonStr.length();
			} else if (token.contains("(")) {
				System.err.println(token);
				return findCommonStr(token, codeLine, "(");
			}

		}

		return 0;

	}

	// "/Users/Dareen/eclipse-workspace/Calculator/src/add2.java"

	static int countOccurences(String str, String word) {
		// split the string by spaces in a
		String[] a = str.split("\n");

		// search for pattern in a
		int count = 0;
		for (int i = 0; i < a.length; i++) {
//		    	System.out.println( "@@@@@ " + a[i]);
			// if match found increase count
			if (a[i].contains(word))
				count++;
		}

		return count;
	}

	static void countOccurences_nextint_and_printf(String str, String nextint_word, String printf_word) {
		// split the string by spaces in a
		String[] a = str.split("\n");

		// search for pattern in a
		int nextintCount = 0;
		int printfCount = 0;
		for (int i = 0; i < a.length; i++) {
			System.out.println(i + " # " + a[i]);
			// if match found increase count
//		    System.out.println(i);	
			if (a[i].contains(nextint_word)) {
				System.out.println("***** Has a nextInt\n");
				nextintCount++;
			}

			if (a[i].contains(printf_word)) {
				System.out.println("*****  Has a printf\n");
				printfCount++;
			}

		}
		System.out.print("\nThe slice has [" + nextintCount + "] nextInt & [" + printfCount + "] printf.");
//		    if(nextintCount == 2) {
//				System.out.print("\nThe slice has 2 nextInt ");
//				if(printfCount == 0) {
//					System.out.print("and 0 printf!!!!!\n");
//				}else if(printfCount == 1) {
//					System.out.print("and 1 printf\n");
//				}
//			}
	}

	static void map_slice_to_code(int[] sliceLines) {
		System.out.print(sliceLines);
		// TODO:
		// get the slice line numbers
		// get the corresponding code lines
		// write the code lines to a file

	}
	
	/**
	 * Creates a File if the file does not exist, or returns a
	 * reference to the File if it already exists.
	 */
	public static File createOrRetrieve(final String target) throws IOException {
		  final File answer;
		  Path path = Paths.get(target);
		  Path parent = path.getParent();
		  if(parent != null && Files.notExists(parent)) {
		    Files.createDirectories(path);
		  }
		  if(Files.notExists(path)) {
			  System.out.println("Target file \"" + target + "\" will be created.");
		    answer = Files.createFile(path).toFile();
		  } else {
			  System.out.println("Target file \"" + target + "\" will be retrieved.");
		    answer = path.toFile();
		  }
		  return answer;
		}
	
	public static File createOrRetrieveFile(String path) throws IOException {
		String[] splittedFileName = path.split("/");
		String simpleFileName = splittedFileName[splittedFileName.length-1];
		String directory  = path.replace(simpleFileName, "");
		File dir = new File(directory);
		if (!dir.exists()) dir.mkdirs();
		File file = new File(path);  
		return file;
		
	}
	/**
	 * 
	 * Write a string to a file that it creates using the passed "path".
	 * @return true if the files was written successfully, otherwise, it returns false.
	 * */
	public static boolean write_tofile(String path, String content) throws IOException {
//		File file = createOrRetrieve(path); //new File(path);
//		if (file.createNewFile()) {
//			System.out.println("File is created!");
//		} else {
//			System.out.println("File already exists.");
//		}
		// Write Content
		String[] splittedFileName = path.split("/");
		String simpleFileName = splittedFileName[splittedFileName.length-1];
		String directory  = path.replace(simpleFileName, "");
		File dir = new File(directory);
		if (!dir.exists()) dir.mkdirs();
		File file = new File(path);
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}
	
	
	/**
	 * 
	 * Write list of strings (List<String> lines) to a file, which it creates using the passed "path".
	 * @return true if the files was written successfully, otherwise, it returns false.
	 * */
	public static boolean write_tofile(String path, List<String> lines) throws IOException {
		File file = new File(path);
//		if (file.createNewFile()) {
//			System.out.println("File is created!");
//		} else {
//			System.out.println("File already exists.");
//		}
		// Write Content
		try (FileWriter writer = new FileWriter(file)) {
			
//			for(String line: lines)
			for (int i=0; i<lines.size();i++) {
				if(i==0)
					writer.write(lines.get(i));
				else
					writer.write("\n"+lines.get(i));
			}
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}
	/**
	 * 
	 * Write to a File object a string
	 * @return true if the files was written successfully, otherwise, it returns false.
	 * */
	public static boolean write_tofile(File file, String content) throws IOException {
//		File file = new File(path);
//		if (file.createNewFile()) {
//			System.out.println("File is created!");
//		} else {
//			System.out.println("File already exists.");
//		}
		// Write Content
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}
	
	public static int findSeedLine(String appSrc, String key, int occ) {
		int lno = -1;
		int i = 0;
		SrcCode srcCode = new SrcCode();
		srcCode.load_code(appSrc);
		for (CodeLine cl : srcCode.lines) {
			if (cl.stmt.contains(key)) {
				i++;
				if (i == occ) {
					return cl.lno;
				}
			}
		}
		return lno;
	}

	public static boolean append_tofile(String path, String Lines) {
		//System.out.println(path);

		try {
			// ======== Both ways below work to append text.
//			File f = new File(path);
//			PrintWriter out = null;
//			if ( f.exists() && !f.isDirectory() ) 
//			    out = new PrintWriter(new FileOutputStream(new File(path), true));
//			else 
//			    out = new PrintWriter(path);
//			out.append(Lines);
//			out.close();

			FileWriter fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(Lines);
//			bw.newLine();
			bw.close();
			return true;
		} catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				System.out.println("could not find the file. " + path);
			}
		}
		return false;
	}

	public static void append_tofile(String Lines, String path, String msg, boolean print) {

		try {
			FileWriter fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(Lines);
//			bw.newLine();
			bw.close();
			if (print)
				System.out.println(msg + " ... Successfully appended to the file (" + path + ")");
		} catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				System.out.println("could not find the file. " + path);
			}
		}
	}

//	public static String read_lines(String path) throws IOException {
//		String slice="";
//		BufferedReader reader = null;
//		File map_file = new File(path);
//		if (map_file.exists()) {
//			reader = new BufferedReader(new FileReader(map_file));
//			String line = reader.readLine();
//			while (line != null) {
//				slice = slice +line;
//				line = reader.readLine();
//			}
//			reader.close();
//		}
//		
//		return slice;
//
//	}
	
	public static String read_lines(String path) throws IOException {
		String lines = "";
		BufferedReader reader = null;
		File map_file = new File(path);
		if (map_file.exists()) {
			reader = new BufferedReader(new FileReader(map_file));
			String line = reader.readLine();
			boolean firstLine = true;
			while (line != null) {
				if(line.contains("createStatement") )//delete the line from the slice.
					continue;
				if (firstLine) {
					lines = lines + line;
					firstLine = false;
				} else {
					lines = lines + "\n" + line;
				}
				line = reader.readLine();
			}
			reader.close();
		}

		return lines;

	}
	
	public static List<String> read_lines_list(String path) throws IOException {
		List<String> slice=new ArrayList<>();
		BufferedReader reader = null;
		File map_file = new File(path);
		if (map_file.exists()) {
			reader = new BufferedReader(new FileReader(map_file));
			String line = reader.readLine();
			while (line != null) {
				slice.add(line);
				line = reader.readLine();
			}
			reader.close();
		}
		
		return slice;

	}
}
