package patchgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import flocalization.G;
import slicer.utilities.StrUtil;

public class TestFixedFiles {
public static void deleteFile(String filePathToDelete, boolean print) {
	//Remove file to avoid filling memory
			if(filePathToDelete != null) {
//				deleteFileCmd(filePathToDelete);
				File directoryToBeDeleted = new File (filePathToDelete);//tmpClassPath
				if(directoryToBeDeleted.delete()) {
//				if(deleteDirectory(directoryToBeDeleted)) {
					if(print)System.out.println(StrUtil.get_filename(filePathToDelete)+" is deleted!");
				}else {
					if(print)System.out.println("Failed to delete "+StrUtil.get_filename(filePathToDelete)+"! ");
				}
			}else {
				if(print)System.out.println("Path is null");
			}
}
	public static String getFileEncoding (String path, boolean print) {
		Runtime rt = Runtime.getRuntime();
		String enc = null;
		//file -I , get file encoding
//		Process pr;
		try {
			Process pr = rt.exec("file -I "+ path);
//			String rs = pr0.getOutputStream().toString();
			BufferedReader stdOutput = new BufferedReader(new 
					InputStreamReader(pr.getInputStream()));
			String s0 = null;
			
			try {
				while ((s0 = stdOutput.readLine()) != null) {
					if(print) System.out.println(s0);
				    if(s0.contains("charset=")) {
				    	enc = s0.split("charset=")[1].trim();
				    }
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pr.destroy();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return enc;
	}
	public static String compileClass (String path, boolean print) {
		Runtime rt = Runtime.getRuntime();
		Process pr ;//= new Process();
		String enc = getFileEncoding(path, print);
		
			try {
				if( enc != null)
					pr = rt.exec("javac -encoding "+ enc +" -d "+G.testTmpPath +" "+ path);
				else
					pr = rt.exec("javac -d "+G.testTmpPath +" "+ path);
				//Process pr = rt.exec("javac -d "+tmpClassPath +" "+filePath+fileName);
				
				String s = null;
				String lastLine= null;
				BufferedReader stdError = new BufferedReader(new 
					     InputStreamReader(pr.getErrorStream()));
				
				// Read any errors from the attempted command
				if(print) System.out.println("Here is the standard error of the command (if any):\n");
				while ((s = stdError.readLine()) != null) {
					if(print) System.out.println(s);
				    lastLine = s;
				}
				pr.destroy();
				return lastLine;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
		
	}
//	public static final int vApphasSynErr = -1;
//	public static final int fApphasSynErr = 1;
//	public static final int noSynErr = 0;
	/**
	 * @return (G.vApphasSynErr) original code is not compiled. (G.noSynErr) no syntax error was introduced, fixedApp is compiled correctly. (G.fApphasSynErr) There is syntax error in the fixedApp.  
	 */
	public static int patchIntroducedSynError (String vulApp, String fixedApp, boolean print) throws IOException {
	
//			int result;
			String vul_stdError = compileClass (vulApp, print);
			String vul_classFile = compiledClassExist(G.testTmpPath, StrUtil.get_classname(vulApp)+".class");
			
			String fixed_stdError = compileClass (fixedApp, print);
			String fixed_classFile = compiledClassExist(G.testTmpPath, StrUtil.get_classname(fixedApp)+".class");
			if (print) {
				System.out.println("vul_classFile: " + vul_classFile);
				System.out.println("fixed_classFile: " + fixed_classFile);
			}
			
//			if (print) {
				System.out.println("vul_stdError: " + vul_stdError);
				System.out.println("fixed_stdError: " + fixed_stdError);
//			}
			
			if(vul_classFile!=null)
				deleteFile(vul_classFile, print);
			if(fixed_classFile!=null)
				deleteFile(fixed_classFile, print);
			
			if(vul_stdError!= null && fixed_stdError!= null && vul_stdError.equals(fixed_stdError)) {
//				if(print) 
					System.out.print("Original code & fixed code have similar errors! ");
				return G.vul_and_fixedAppshaveSynErr;//No new SyErrors
			}
			
			
			
			if(fixed_classFile != null && fixed_stdError == null) {
				System.out.print("Fixed app is Compiled. ");
				return G.noSynErr;
			}else {//if ( fixed_classFile == null ||fixed_stdError != null ){
				System.out.print("Fixed app is Not Compiled. ");
				return G.fixedApphasSynErr;
			}
			
//			if( ( vul_stdError != null && vul_stdError.contains("error")) || vul_classFile == null) {
//				//if(print) System.out.println("Original code has errors or not compiled! ...");
//				return G.vulApphasSynErr;
//			}
	}
	public static String compiledClassExist(String dirPath, String fileName) {
		String absPath = null;
		File root = new File(dirPath);
//        String fileName = "a.txt";
        try {
            boolean recursive = true;

            Collection files = FileUtils.listFiles(root, null, recursive);

            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                if (file.getName().equals(fileName)) {
                    System.out.println(file.getAbsolutePath());
                    absPath = file.getAbsolutePath();
//                    return true;
                 }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return absPath;
        
	}
	public static boolean deleteDirectory(File directoryToBeDeleted) {
		
//	    File[] allContents = directoryToBeDeleted.listFiles();
//	    if (allContents != null) {
//	        for (File file : allContents) {
//	            deleteDirectory(file);
//	        }
//	    }
	    return directoryToBeDeleted.delete();
	}
//	public static boolean deleteFileCmd(String filePath) { //Didn't work
//		//
//		try {
//			Runtime rt = Runtime.getRuntime();
//			Process pr = rt.exec("rm -i " + filePath);
//			return true;
//			}catch (IOException ex){
//				System.out.println (ex.toString());
//				System.out.println("Failed to delete");
//				return false;
//			}
//		
//	}
	public static void main(String[] args) throws IOException  {
		String vapp, fapp;
		vapp = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/java-homework-10-master/src/com/company/task2/mysql/MySQLBookDao.java";
		fapp = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/java-homework-10-master/src/com/company/task2/mysql/MySQLBookDao_fixed.java";
		System.out.println( "patchIntroducedSynError? "+patchIntroducedSynError(vapp,fapp,true));
//		String jarFile = "/Users/Dareen/JAR_FILES/sqljdbc42.jar";
//		String filePath = "/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/java-homework-10-master/src/com/company/task1/";
////					 "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/";
//		String fileName = "DBConnection.java";
////		"CV1_01.java";
//		String className ;
//		String fileToCompile = //filePath+fileName;
//				"/Users/Dareen/Desktop/DCAFixer_Experimets/SQLIFix-projects/test/java-homework-10-master/src/com/company/task1/DBConnection.java";
//		if(fileName.endsWith(".java")) {
//			className = fileName.replace("java", "class");
//		}else {
//			className = fileName+".class";
//		}
//		System.out.println("className: "+className +"\nStrUtil: "+ StrUtil.get_classname(fileToCompile)+".class");
//		String tmpClassPath = "/Users/Dareen/tmp";
//		String filePathToDelete = null;
//		try {
//			Runtime rt = Runtime.getRuntime();
////			Process pr = rt.exec("javac -cp /Users/Dareen/JAR_FILES/sqljdbc42.jar /Users/Dareen/NetBeansProjects/smallBank/src/smallbank/CV1_01.java");
////			Process pr = rt.exec("javac -cp "+jarFile+" -d "+tmpClassPath +" "+filePath+fileName);
//			//file -I 
//			Process pr0 = rt.exec("file -I "+ filePath+fileName);
//			String rs = pr0.getOutputStream().toString();
//			BufferedReader stdOutput = new BufferedReader(new 
//					InputStreamReader(pr0.getInputStream()));
//			String s0 = null;
//			String enc = null;
//			while ((s0 = stdOutput.readLine()) != null) {
//			    System.out.println(s0);
//			    if(s0.contains("charset=")) {
//			    	enc = s0.split("charset=")[1].trim();
//			    }
//			}
//			String [] parts = rs.split("charset=");
//			Process pr ;//= new Process();
//			if(enc != null)
//				pr = rt.exec("javac -encoding "+ enc +" -d "+tmpClassPath +" "+filePath+fileName);
//			else
//				pr = rt.exec("javac -d "+tmpClassPath +" "+filePath+fileName);
//			//Process pr = rt.exec("javac -d "+tmpClassPath +" "+filePath+fileName);
//			
//			String s = null;
//			String last = null;
////			BufferedReader stdInput = new BufferedReader(new 
////				     InputStreamReader(pr.getInputStream()));
////			// Read the output from the command
////			System.out.println("Here is the standard output of the command:\n");
////			while ((s = stdInput.readLine()) != null) {
////			    System.out.println(s);
////			}
//			
//			BufferedReader stdError = new BufferedReader(new 
//				     InputStreamReader(pr.getErrorStream()));
//			// Read any errors from the attempted command
//			System.out.println("Here is the standard error of the command (if any):\n");
//			while ((s = stdError.readLine()) != null) {
//			    System.out.println(s);
//			    last = s;
//			}
//			if(last != null)
//				System.out.println("last line:" + last);
////			if (pr.getErrorStream() != null) {
////			System.out.println("ErrorStream Not empty!");
////			System.out.println(pr.getOutputStream().toString());
////			if(compiledClassExist(tmpClassPath, fileName.replace("java", "class"))) {
//			pr.destroy();
//			pr0.destroy();
//			filePathToDelete = compiledClassExist(tmpClassPath, className);
//			if(filePathToDelete != null) {
//				
//				System.out.println("Compiled!");
//			}else {
//				
//				System.out.println("Not Compiled!");
//			}
//		}catch (IOException ex){
//			System.out.println (ex.toString());
//			System.out.println("Failed to compile");
//		}
//		
//		//Remove file to avoid filling memory
//		if(filePathToDelete != null) {
////			deleteFileCmd(filePathToDelete);
//			File directoryToBeDeleted = new File (filePathToDelete);//tmpClassPath
//			if(directoryToBeDeleted.delete()) {
////			if(deleteDirectory(directoryToBeDeleted)) {
//				System.out.println("File is deleted!");
//			}else {
//				System.out.println("Failed to delete file!");
//			}
//		}
	}
}
