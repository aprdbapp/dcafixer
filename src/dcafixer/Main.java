package dcafixer;

public class Main {


	public static void main(String[] args) throws Exception {
		
		// Check if the correct number of arguments are provided
        if (args.length < 5) {
            System.out.println("Usage: java -jar yourapp.jar <projectSrc> <projectJar> <projectName> <reportDir> <patternsPath>");
            System.exit(1);
        }


		//====== Exmaple 1
		String projectSrc = args[0];
		if(!projectSrc.endsWith("/"))
			projectSrc = projectSrc + "/";
		String projectJar = args[1];
		String projectName = args[2];
		String outDir = args[3];
		if(!outDir.endsWith("/"))
			outDir = outDir + "/";
		
		String patternsPath = args[4];
		if(!patternsPath.endsWith("/"))
			patternsPath = patternsPath + "/";
		
		//Example 
		Fixer.start_dcafixer(projectName,projectSrc, projectJar, outDir, patternsPath);

	}

}

////====== Exmaple 2
//		String projectSrc2 = "simpletest2/src/";
//		String projectJar2 = "simpletest2/lib/simpletest2.jar";
//		String projectName2 = "Simpletest2";