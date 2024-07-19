package slicer.datatypes;

public class VAR_MD {
	public int lno;
	public String type; // scanner, int, double, long, string, ..
	public String name; // user input type S = scanner, B = bufferReader, C = constant
	public String fun;

	public int getLno() {
		return lno;
	}

	public void setLno(int lno) {
		this.lno = lno;
	}

	public String getType() {

		return type;
	}

	public void setType(String t) {
		this.type = t;
	}

	public void setName(String n) {
		this.name = n;
	}

	public String getName() {
		return name;
	}

	public void setFun(String f) {
		this.fun = f;
	}

	public String getFun() {
		return fun;
	}
}
