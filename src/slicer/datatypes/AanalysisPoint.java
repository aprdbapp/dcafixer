package slicer.datatypes;

public class AanalysisPoint {
	public int lno;
	public char type; // C = getConnection, Q = executeQuery , P = prepareStatement
	public char UIT; // user input type S = scanner, B = bufferReader, C = constant

	public int getLno() {
		return lno;
	}

	public void setLno(int lno) {
		this.lno = lno;
	}
	/*Comparator for sorting the list by roll no*/
//    public static Comparator<Student> StuRollno = new Comparator<Student>() {

	public int compare(AanalysisPoint s1, AanalysisPoint s2) {

	   int rollno1 = s1.getLno();
	   int rollno2 = s2.getLno();

	   /*For ascending order*/
	   return rollno1-rollno2;

	   /*For descending order*/
	   //rollno2-rollno1;
   }
	public String getType() {
		if (type == 'C' || type == 'c') {
//			System.out.println("Type is getConnection");
			return "getConnection";
		} else if (type == 'Q' || type == 'q') {
//			System.out.println("Type is executeQuery");
			return "executeQuery";
		} else if (type == 'U' ||type == 'u') {
			return "executeUpdate";
		}else if (type == 'E' ||type == 'e') {
			return "execute";
		}else if (type == 'P' || type == 'p') {
			System.out.println("Type is prepareStatement");
			return "prepareStatement";
		} else if (type == 'O' || type == 'o') {
			return "OutPutFunction";
		} else if (type == 'R' || type == 'r') {
			return "Properties";
		}

		return null;
	}

	public void setType(char t) {
		this.type = t;
	}

	public void setUIT(char uit) {
		this.UIT = uit;
	}

	public char getUIT() {
		return UIT;
	}
}
