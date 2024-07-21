package slicer.datatypes;

import com.ibm.wala.ipa.slicer.Statement;

public class VariableMData {
	public String name;
	public String type;
	public ClassUsed classused = ClassUsed.NA;
	public String methodused;
	public int lno;
	public Statement stmt;

	public void set_class_value(String str) {
		switch (str) {
		case "Scanner":
		case "scanner":
			classused = ClassUsed.SCAN;
			break;
		case "BufferedReader":
		case "bufferedreader":
			classused = ClassUsed.BUFREADER;
			break;
		case "InputStreamReader":
		case "inputstreamreader":
			classused = ClassUsed.ISREADER;
			break;
		case "FileReader":
		case "filereader":
			classused = ClassUsed.FILEREADER;
			break;
		}

	}

	public String getUsedClass(ClassUsed cu) {
		String className = "";
		switch (cu) {
		case SCAN:
			className = "Scanner";
			break;
		case BUFREADER:
			className = "BufferedReader";

			break;
		case ISREADER:
			className = "InputStreamReader";

			break;
		case FILEREADER:
			className = "FileReader";
			break;
		default:
			break;

		}
		return className;
	}
}
