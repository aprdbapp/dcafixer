package dcafixer;



import flocalization.G;
public class Main {

	
	public static void main(String[] args) throws Exception {


		String projectSrc = G.projectsPath  + "smallBank/src/TSet2/";
		String projectJar = G.projectsJarsPath + "smallBank.jar";
		//String paperExCsv = csvDir + "PaperExample_vul.csv";

		String projectName = "PaperExample_vul";

		
		
		
		Fixer.start_dcafixer(projectName,projectSrc, projectJar);
	}

}
