package dcafixer;



import flocalization.G;
public class Main {

	
	public static void main(String[] args) throws Exception {


		String projectSrc = "path/to/buggy/project/src/"; // should ends with "/"
		String projectJar = "path/to/buggy/project/example1.jar";
		String projectName = "example1";

		
		
		
		Fixer.start_dcafixer(projectName,projectSrc, projectJar);
	}

}
