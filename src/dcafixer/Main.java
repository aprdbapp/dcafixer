package dcafixer;

public class Main {


	public static void main(String[] args) throws Exception {

		String projectSrc = "simpletest/src/";
		String projectJar = "simpletest/lib/simpletest.jar";
		String projectName = "VulExample";

		Fixer.start_dcafixer(projectName,projectSrc, projectJar);

	}

}
