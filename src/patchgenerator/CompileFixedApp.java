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

public class CompileFixedApp {
	public static boolean Print = false;

	public static String compiledClassExist(String fileName) {
		String dirPath = G.testTmpPath;
		String absPath = null;
		File root = new File(dirPath);
		try {
			boolean recursive = true;
			Collection files = FileUtils.listFiles(root, null, recursive);

			for (Iterator iterator = files.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				if (file.getName().equals(fileName)) {
					if (Print)
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

	public static boolean fixedAppisCompiled(String fullPath, boolean print) {
		Print = print;
		boolean compiled = false;
//		String filePath  = StrUtil.get_folder_path(fullPath);
//		String fileName = StrUtil.get_filename(fullPath);
		String classFileName = StrUtil.get_classname(fullPath) + ".class";
		String tmpClassPath = G.testTmpPath;
		String filePathToDelete = null;
		// Check file does not exist previously
		filePathToDelete = compiledClassExist(classFileName);
		if (filePathToDelete != null) { // if exist delete
			File f = new File(filePathToDelete);// tmpClassPath
			f.delete();
		}
		// Compile the app
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("javac -d " + tmpClassPath + " " + fullPath);
			String s = null;

			BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			if (stdError != null) {
				if (Print)
					System.out.println("Standard error of the command: \n");
				while ((s = stdError.readLine()) != null) {
					System.out.println(s);
				}
			}
			pr.destroy();
			filePathToDelete = compiledClassExist(classFileName);
			if (filePathToDelete != null) {
				// Delete it after checking that it exists
				if (Print)
					System.out.println("Compiled!");
				compiled = true;
				File fileToBeDeleted = new File(filePathToDelete);// tmpClassPath
				if (fileToBeDeleted.delete()) {
					if (Print) System.out.println("File is deleted!");
				} else {
					if (Print) System.out.println("Failed to delete file!");
				}
			} else {
				if (Print)
					System.out.println("No compiled file!");
			}
		} catch (IOException ex) {
			if (Print) {
				System.out.println(ex);
				System.out.println("Failed to compile!");
			}
		}

		return compiled;
	}

	public static void main(String[] args) throws IOException {

		String filePath = "/Users/Dareen/NetBeansProjects/smallBank/src/smallbank/CV1_01.java";

		if (fixedAppisCompiled(filePath, false))
			System.out.println("App was compiled successfully!");
	}
}
