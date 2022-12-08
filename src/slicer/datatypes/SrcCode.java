package slicer.datatypes;

//import java.awt.List;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SrcCode {
	public int lno = 0;
	public ArrayList<CodeLine> lines = new ArrayList<CodeLine>();// new ArrayList<EventSeat>();
	public String className = null;
	public ArrayList<String> methods = new ArrayList<>();

//	public List seeds;
	public void set_values(String path, String cname, ArrayList<String> m) {
		load_code(path);
		className = cname;
		methods.addAll(m);
	};

	public void load_code(String path) {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;

			while ((line = reader.readLine()) != null) {
				lno++;
				CodeLine cl = new CodeLine();
				cl.setLno(lno);
				cl.setStmt(line);
				lines.add(cl);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void print_lines() {
		System.out.println("# lines: " + lno);
		System.out.println(lines);
		for (CodeLine line : lines) {
			System.out.println(line.getLno() + " " + line.getStmt());
		}
	}

	public void print_metadata() {
		System.out.println("# lines: " + lno);
		System.out.println("Class name: " + className);
		System.out.println("Methods: ");
		// System.out.println(methods);
	}
}
