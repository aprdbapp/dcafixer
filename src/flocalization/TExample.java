package flocalization;

import java.util.ArrayList;

//import java.util.List;

//import com.ibm.wala.ipa.slicer.Statement;

//import slicer.datatypes.CodeLine;
import slicer.datatypes.SliceLine;

public class TExample {
	public int id;
	public String vapp_path; // vulnerable app path
	public String sapp_path; // secure app path
	public String sig; // signature
	public ArrayList<String> keys = new ArrayList<>(); // Keys used to find the slice
//	public ArrayList<CodeLine> SSlice = new ArrayList<CodeLine>();
	public ArrayList<SliceLine> SSlice = new ArrayList<SliceLine>();
	public ArrayList<SliceLine> VSlice = new ArrayList<SliceLine>();

	// TODO: Edit Block : :edit pattern and edit
	public TExample(int i, String va, String sa, String s) {
		this.id = i;
		set_vapp_path(va);
		set_sapp_path(sa);
		set_sig(s);
	}

	// Setters
	public void set_vapp_path(String vapp) {
		this.vapp_path = vapp;
	}

	public void set_sapp_path(String sapp) {
		this.sapp_path = sapp;
	}

	public void set_sig(String s) {
		this.sig = s;
	}

	public void set_keys(ArrayList<String> keys_list) {
		for (String k : keys_list) {
			this.keys.add(k);
		}
	}

	public void set_SSlice(ArrayList<SliceLine> slice) {
		for (SliceLine sl : slice) {
			this.SSlice.add(sl);
		}
	}

	public void set_VSlice(ArrayList<SliceLine> slice) {
		for (SliceLine sl : slice) {
			this.VSlice.add(sl);
		}
	}

	// Getters
	public String get_vapp_path() {
		return vapp_path;
	}

	public String get_sapp_path() {
		return sapp_path;
	}

	public String get_sig() {
		return sig;
	}

	public ArrayList<String> get_keys() {
		return keys;
	}

	public ArrayList<SliceLine> get_SSlice() {
		return SSlice;
	}

	public ArrayList<SliceLine> get_VSlice() {
		return VSlice;
	}

	public String toString() {
		return "Example" + this.id + " - sig(" + this.sig + "):" + "\n VulApp_path (" + this.vapp_path
				+ "),  SecApp_path (" + this.sapp_path + ")" + "\n Keys & directions: " + this.keys.toString()
				+ "\n--------------\n";
	}
}
