package dcafixer;

public class Main {


	public static void main(String[] args) throws Exception {

		//====== Exmaple 1
		String projectSrc = "simpletest1/src/";
		String projectJar = "simpletest1/lib/simpletest1.jar";
		String projectName = "Simpletest1";

		Fixer.start_dcafixer(projectName,projectSrc, projectJar);
		
		//====== Exmaple 2
		String projectSrc2 = "simpletest2/src/";
		String projectJar2 = "simpletest2/lib/simpletest2.jar";
		String projectName2 = "Simpletest2";

		Fixer.start_dcafixer(projectName2,projectSrc2, projectJar2);

	}

}
