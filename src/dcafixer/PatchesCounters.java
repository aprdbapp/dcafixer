package dcafixer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Longs;

import flocalization.G;
import slicer.utilities.StrUtil;

public class PatchesCounters {
	public PatchesCounters() {
		set_GPatches_counters();
		set_GPatches_times();
		
	}
	public List<Long> getTimesList() {
		return TimesList;
	}
	public void setTimesList(List<Long> timesList) {
		TimesList = timesList;
	}
	
	public void addToTimesList(Long time) {
		TimesList.add(time);
	}
	public int getGPatches() {
		return GPatches;
	}
	public void setGPatches(int gPatches) {
		GPatches = gPatches;
	}
	public int getFSGPatches() {
		return FSGPatches;
	}
	public void setFSGPatches(int fSGPatches) {
		FSGPatches = fSGPatches;
	}
	public int getFSGPatches_PS() {
		return FSGPatches_PS;
	}
	public void setFSGPatches_PS(int fSGPatches_PS) {
		FSGPatches_PS = fSGPatches_PS;
	}
	public int getFSGPatches_WL() {
		return FSGPatches_WL;
	}
	public void setFSGPatches_WL(int fSGPatches_WL) {
		FSGPatches_WL = fSGPatches_WL;
	}
	public int getFGPatches() {
		return FGPatches;
	}
	public void setFGPatches(int fGPatches) {
		FGPatches = fGPatches;
	}
	public int getNoGPatches() {
		return NoGPatches;
	}
	public void setNoGPatches(int noGPatches) {
		NoGPatches = noGPatches;
	}
	public int getNoGPatches_NoSol() {
		return NoGPatches_NoSol;
	}
	public void setNoGPatches_NoSol(int noGPatches_NoSol) {
		NoGPatches_NoSol = noGPatches_NoSol;
	}
	public int getNoGPatches_PS() {
		return NoGPatches_PS;
	}
	public void setNoGPatches_PS(int noGPatches_PS) {
		NoGPatches_PS = noGPatches_PS;
	}
	public int getNoGPatches_WL() {
		return NoGPatches_WL;
	}
	public void setNoGPatches_WL(int noGPatches_WL) {
		NoGPatches_WL = noGPatches_WL;
	}
	public double getAvg_time_to_GPatch() {
		return Avg_time_to_GPatch;
	}
	public void setAvg_time_to_GPatch(int avg_time_to_GPatch) {
		Avg_time_to_GPatch = avg_time_to_GPatch;
	}
	
	public void setAvg_time_to_GPatch(double avg_time_to_GPatch) {
		Avg_time_to_GPatch = avg_time_to_GPatch;
	}

	public double computeAvg_time_to_GPatch() {
		long[] TimesList_long = Longs.toArray(TimesList);
		long sum =0;
		for(long t: TimesList_long) {
			sum = sum + t;
		}
		if (TimesList.size() > 0)
			Avg_time_to_GPatch = (sum / TimesList.size()) / 1000F;
		return Avg_time_to_GPatch;
	}
	//============================
	public long getGPatch_time() {
		return GPatch_time;
	}
	public void setGPatch_time(long gPatch_time) {
		GPatch_time = gPatch_time;
	}
	public long computeGPatch_time_ms() {
		GPatch_time = GPatch_end - GPatch_start;
		return GPatch_time;
	}
	public float computeGPatch_time_second() {
		GPatch_time = GPatch_end - GPatch_start;
		return GPatch_time/ 1000F;
	}
//	public long computeGPatch_time(long end, long start) {
//		GPatch_time = end - start;
//		return GPatch_time;
//	}
	
	public long getGPatch_start() {
		return GPatch_start;
	}
	public void setGPatch_start() {
		GPatch_start = System.currentTimeMillis();
	}
	public long getGPatch_end() {
		return GPatch_end;
	}
	public void setGPatch_end() {
		GPatch_end = System.currentTimeMillis();
	}
	public int getFSGPatches_Conn() {
		return FSGPatches_Conn;
	}
	public void setFSGPatches_Conn(int fSGPatches_Conn) {
		FSGPatches_Conn = fSGPatches_Conn;
	}
	public int getNoGPatches_Conn() {
		return NoGPatches_Conn;
	}
	public void setNoGPatches_Conn(int noGPatches_Conn) {
		NoGPatches_Conn = noGPatches_Conn;
	}
	//============================
	public void set_GPatches_counters() {
		TimesList.clear();
		GPatches = 0; 
		FSGPatches = 0;
		FSGPatches_PS = 0;
		FSGPatches_WL = 0;
		FSGPatches_Conn = 0;
		FGPatches = 0;
		NoGPatches = 0;
		NoGPatches_NoSol = 0;
		NoGPatches_PS = 0;
		NoGPatches_WL = 0;
		Avg_time_to_GPatch = 0;		
	}
	
	public void set_GPatches_times() {
		GPatch_time = 0;
		GPatch_start = 0;
		GPatch_end = 0;
	}
	
	//============
	public void print_all_GPatches_results(String projName, int all_SQLIVs) throws IOException {
		System.out.println("======== Generated patches stats for Project ( "+projName+" ) ======== ");
		System.out.println("GPatches = "+ GPatches ); 
		System.out.println("FSGPatches = "+ FSGPatches );
		System.out.println("FSGPatches_PS = "+ FSGPatches_PS );
		System.out.println("FSGPatches_WL = "+ FSGPatches_WL );
		System.out.println("FSGPatches_Conn = "+ FSGPatches_Conn );
		System.out.println("FGPatches = "+ FGPatches );
		System.out.println("\nNoGPatches = "+ NoGPatches );
		System.out.println("NoGPatches_NoSol = "+ NoGPatches_NoSol );
		System.out.println("NoGPatches_PS = "+ NoGPatches_PS );
		System.out.println("NoGPatches_WL = "+ NoGPatches_WL );
		System.out.println("NoGPatches_Conn = "+ NoGPatches_Conn );
		computeAvg_time_to_GPatch();
		System.out.println("Avg_time_to_GPatch = "+ Avg_time_to_GPatch +" sec" );
		System.out.println("========================================"); 
//		String str1  = "App_name,all_SQLIVs,GPatches,FSGPatches,FSGPatches_PS,FSGPatches_WL,FGPatches,NoGPatches,NoGPatches_NoSol,NoGPatches_PS,NoGPatches_WL,Avg Time\n" ;
		String content = projName  +","+ all_SQLIVs +","+ GPatches+","+FSGPatches+","+FSGPatches_PS+","+FSGPatches_WL+","+FGPatches+","+ FSGPatches_Conn +","+NoGPatches+","+NoGPatches_NoSol+","+NoGPatches_PS+","+NoGPatches_WL+","+ NoGPatches_Conn+","+Avg_time_to_GPatch+"\n";
//		String content = str1 + str2 ;
		//StrUtil.append_tofile(G.GPatchesTmpPath, content);
	}
	//================================= Variables needed to count generated patches
	List<Long> TimesList =  new ArrayList<Long>();
	int GPatches = 0; 
	int FSGPatches = 0;
	int FSGPatches_PS = 0;
	int FSGPatches_WL = 0;
	int FGPatches = 0;
	int FSGPatches_Conn = 0;//++
	
	
	int NoGPatches = 0;
	int NoGPatches_NoSol = 0;
	int NoGPatches_PS = 0;
	int NoGPatches_WL = 0;
	int NoGPatches_Conn = 0;//++
	
	double Avg_time_to_GPatch = 0;
	//========
	long GPatch_time = 0;
	long GPatch_start = 0;
	long GPatch_end = 0;
	

}
