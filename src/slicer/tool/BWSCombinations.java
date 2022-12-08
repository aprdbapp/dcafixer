package slicer.tool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.core.util.io.FileProvider;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.FileOfClasses;

import slicer.datatypes.SliceLine;
import slicer.datatypes.SrcCode;
import slicer.utilities.SlicerUtil;

public class BWSCombinations {
	//CG_AllEPoints_MyExGUI
//	+ "java\\/lang\\/.*\n";//++ Building CG fails w/o this library
//	+"\\/.*\n";
	private static final String EXCLUSIONS_GUI = 
//			"java\\/awt\\/.*\n" // === Shouldn't be excluded with GUI
//			+"javax\\/swing\\/.*\n" // === Shouldn't be excluded with GUI
//			+ 
			"com\\/sun\\/.*\n" 
			+ "sun\\/.*\n" 
			+ "org\\/netbeans\\/.*\n"
			+ "org\\/openide\\/.*\n" 
			+ "com\\/ibm\\/crypto\\/.*\n" 
			+ "com\\/ibm\\/security\\/.*\n"
			+ "org\\/apache\\/xerces\\/.*\n" 
			+ "java\\/security\\/.*\n"
			+ "java\\/sql\\/SQLException"
			+ "java\\/sql\\/Statement"
			+ "java\\/sql\\/ResultSet"
			+ "java\\/sql\\/.*\n"
			+ "java\\/io\\/.*\n"
			+ "java\\/io\\/BufferedReader\n"
			+ "java\\/io\\/IOException\n"
			+ "java\\/io\\/InputStreamReader\n"
			+ "java\\/util\\/Vector\n"
			+ "java\\/security\\/PublicKey\n"
			+ "java\\/security\\/MessageDigest\n"
			+ "java\\/math\\/.*\n"
			+ "java\\/util\\/.*\n"
			+"java\\/lang\\/Math"			
			+ "java\\/io\\/.*\n"//+++++++++
			+ "java\\/text\\/.*\n"
			+"us\\/codecraft\\/webmagic\\/.*\n" //++
			+"javax\\/annotation\\/.*\n"//++
			+"org\\/springframework\\/stereotype\\/Component\n"//++
			+"us\\/codecraft\\/webmagic\\/.*\n"//++
			+"org\\/apache\\/ibatis\\/annotations\\/Insert\n"//++
			+"org\\/springframework\\/.*\n"//++
			+"javax\\/annotation\\/.*\n"//++
			+ "org\\/apache\\/.*\n"
			+ "java\\/net\\/.*\n"
			+"com\\/mysql\\/.*\n"
			+ "org\\/junit\\/.*\n"
			+"org\\/jsoup\\/.*\n"
			+"org\\/htmlcleaner\\/.*\n"
			+"junit\\/.*\n"
			+"java\\/sql\\/.*\n"
		  	+"com\\/microsoft\\/.*\n"//++++++++++
		  	+"com\\/alibaba\\/.*\n"
		  	+"com\\/google\\/.*\n"
		  	+"org\\/assertj\\/.*\n"
		  	+"org\\/jdom2\\/.*\n"
		  	+"redis\\/clients\\/.*\n"
			+"net\\/minidev\\/.*\n"
			+"net\\/minidev\\/.*\n"
			+"com\\/jayway\\/.*\n"//========
			+"org\\/slf4j\\/.*\n"
			+"javax\\/servlet\\/.*\n"
			+"us\\/codecraft\\/.*\n" 
			+"org\\/hamcrest\\/.*\n"
			+"org\\/mybatis\\/.*\n"
			+"org\\/aopalliance\\/.*\n";

	private static final String EXCLUSIONS = 
			"java\\/awt\\/.*\n" // === Shouldn't be excluded with GUI
			+"javax\\/swing\\/.*\n" // === Shouldn't be excluded with GUI
			+ "com\\/sun\\/.*\n" 
			+ "sun\\/.*\n" 
			+ "org\\/netbeans\\/.*\n"
			+ "org\\/openide\\/.*\n" 
			+ "com\\/ibm\\/crypto\\/.*\n" 
			+ "com\\/ibm\\/security\\/.*\n"
			+ "org\\/apache\\/xerces\\/.*\n" 
			+ "java\\/security\\/.*\n"
			+ "java\\/sql\\/SQLException"
			+ "java\\/sql\\/Statement"
			+ "java\\/sql\\/ResultSet"
			+ "java\\/sql\\/.*\n"
			+ "java\\/io\\/.*\n"
			+ "java\\/io\\/BufferedReader\n"
			+ "java\\/io\\/IOException\n"
			+ "java\\/io\\/InputStreamReader\n"
			+ "java\\/util\\/Vector\n"
			+ "java\\/security\\/PublicKey\n"
			+ "java\\/security\\/MessageDigest\n"
			+ "java\\/math\\/.*\n"
			+ "java\\/util\\/.*\n"
			+"java\\/lang\\/Math"			
			+ "java\\/io\\/.*\n"//+++++++++
			+ "java\\/text\\/.*\n"
			+"us\\/codecraft\\/webmagic\\/.*\n" //++
			+"javax\\/annotation\\/.*\n"//++
			+"org\\/springframework\\/stereotype\\/Component\n"//++
			+"us\\/codecraft\\/webmagic\\/.*\n"//++
			+"org\\/apache\\/ibatis\\/annotations\\/Insert\n"//++
			+"org\\/springframework\\/.*\n"//++
			+"javax\\/annotation\\/.*\n"//++
			+ "org\\/apache\\/.*\n"
			+ "java\\/net\\/.*\n"
			+"com\\/mysql\\/.*\n"
			+ "org\\/junit\\/.*\n"
			+"org\\/jsoup\\/.*\n"
			+"org\\/htmlcleaner\\/.*\n"
			+"junit\\/.*\n"
			+"java\\/sql\\/.*\n"
		  	+"com\\/microsoft\\/.*\n"//++++++++++
		  	+"com\\/alibaba\\/.*\n"
		  	+"com\\/google\\/.*\n"
		  	+"org\\/assertj\\/.*\n"
		  	+"org\\/jdom2\\/.*\n"
		  	+"redis\\/clients\\/.*\n"
			+"net\\/minidev\\/.*\n"
			+"net\\/minidev\\/.*\n"
			+"com\\/jayway\\/.*\n"//========
			+"org\\/slf4j\\/.*\n"
			+"javax\\/servlet\\/.*\n"
			+"us\\/codecraft\\/.*\n" 
			+"org\\/hamcrest\\/.*\n"
			+"org\\/mybatis\\/.*\n"
			+"org\\/aopalliance\\/.*\n";

			

	
	public static String searchCG_class_by_method (CallGraph cg, String className, String method) {
		System.out.println("In searchCG_class_by_method .. looking for ("+ method +") in Class : " +className);
		String fullClassN = null;
		for (CGNode N : cg) {
			String methodTemp = N.getMethod().getName().toString();
			if (methodTemp.contains(method) || methodTemp.equals(method)) {
//				System.out.println("Horray found method: " +methodTemp +", in Class: "+N.getMethod().getDeclaringClass().getName().toString());
				
				String temp  = N.getMethod().getDeclaringClass().getName().toString();
				if(temp.contains(className)) {
					fullClassN = temp;
					System.out.println("Horray found the Wanted Class! " +fullClassN + ", method:"+methodTemp);
				}
			}
			
		}
		return fullClassN;
	}
	public static void searchCG_class (CallGraph cg, String className) {
		String found = null;
		for (CGNode N : cg) {
//			if(!N.getMethod().getDeclaringClass().getName().toString().contains("java/lang"))//java/lang
//				System.out.println("Class: "+N.getMethod().getDeclaringClass().getName().toString());
//			if ((N.getMethod().getDeclaringClass().getName().toString().contains(className))) {
			if ((N.getMethod().getDeclaringClass().getName().toString().equals(className))) {
//				System.out.println("Horray found the class! " +N.getMethod().getDeclaringClass().getName().toString());
				found = N.getMethod().getDeclaringClass().getName().toString();
				System.out.println("Horray found the class! " +found);
			}
			
		}
		
		return;
	}
	public static void searchCG (String method ,CallGraph cg,  String className, int lineNumber, DataDependenceOptions dOptions, ControlDependenceOptions cOptions , 
			CallGraphBuilder<InstanceKey> builder, String key, String appJar, String  appSrc) throws InvalidClassFileException, IllegalArgumentException, CancelException, ClassHierarchyException, IOException {
		String methodName ="";//;//"insertBean";
		if(method != null) {
			methodName = method;
		}else {
		for (CGNode N : cg) {
//			System.out.println("Class: "+N.getMethod().getDeclaringClass().getName().toString());
			if (N.getMethod().getDeclaringClass().getName().toString().equals("L"+className)) {
				System.out.println("Horray! " + N.getMethod().getDeclaringClass().getName().toString());
				methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
//				methodName = SlicerUtil.find_seed_method_in_node(N, lineNumber);
//				methodName = SlicerUtil.find_seed_method2(cg, N.getMethod().getDeclaringClass().getName().toString(), lineNumber);
				System.out.println("methodName1: "+SlicerUtil.find_seed_method2(cg, className, lineNumber));
				System.out.println("methodName2: "+ SlicerUtil.find_seed_method_in_node(N, lineNumber));
			}
		}
		}
		if(methodName != null && methodName.length()>0) {
			CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
			Statement s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
			System.out.println("SEED: "+s.toString());
			Collection<Statement> backward_slice;
			backward_slice = Slicer.computeBackwardSlice(s, cg, builder.getPointerAnalysis(), dOptions, cOptions);
//			SlicerUtil.dumpSlice(backward_slice);
			SrcCode src = new SrcCode();
			ArrayList<String> methods_list = new ArrayList<>();
			methods_list = SlicerUtil.find_all_class_methods_from_cg(cg, appJar, className);
			src.set_values(appSrc, className, methods_list);
			
			List<SliceLine> sliceLines = new ArrayList<>();
			sliceLines = SlicerUtil.map_filtered_slice_stmts_to_SliceLine(backward_slice, src.methods, src.lines);
			for(SliceLine l : sliceLines) {
				 System.out.println(l.lno + " @ " + l.stmt);
			 }
					//('s', cg, builder.getPointerAnalysis(), dOptions, cOptions);;
		}

	}
	public static List<SliceLine> build_cg(String method, String appJar, String className, int lineNumber, String appSrc,
			int cg_type, DataDependenceOptions dOptions, ControlDependenceOptions cOptions, char seed_type, String key)
			throws WalaException, CancelException, IOException, InvalidClassFileException {
		
			// ============== Try different combinations to build the CG, and Print the stats!
		
//			System.out.println(" **************** CG # 1");
//			try {
//				//could not resolve < Application, Lsrc/main/java/com/revature/Account, main([Ljava/lang/String;)V >
//				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
//				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
//				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
//				Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
//						"L" + className);
//
//				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);// new
//
//				CallGraphBuilder<InstanceKey> builder =  Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
//				//CallGraphBuilder<InstanceKey> builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
//				CallGraph cg = builder.makeCallGraph(options, null);
//				
//				System.out.println(CallGraphStats.getStats(cg));
//			} catch (Exception e) {
//				System.out.println("CG 1 Failed!");
//			}
		
		//========================================
		
		System.out.println(" **************** CG # 1");
		
		try {//could not resolve < Application, Lsrc/main/java/com/revature/Account, main([Ljava/lang/String;)V >
			AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
			scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
			IClassHierarchy cha = ClassHierarchyFactory.make(scope);				
//			Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints( cha, "L" + className);
//			AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
//			CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
			CHACallGraph cg = new CHACallGraph(cha);
//			CallGraph cg = builder.makeCallGraph(options, null);
			System.out.println(CallGraphStats.getStats(cg));
		} catch (Exception e) {
			System.out.println("CG 1 Failed!");
		} 
        
			//========================================
			
			System.out.println(" **************** CG # 2");
			try { // Empty CG !
				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
//				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
				Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
						"L" + className);
				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);					
				CHACallGraph cg = new CHACallGraph(cha);
				
				System.out.println(CallGraphStats.getStats(cg));

			} catch (Exception e) {
				System.out.println("CG 2 Failed!");
			} 
	        
			//========================================
			
			System.out.println(" **************** CG # 3");
			try {// Empty CG !
				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
				Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);

				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);					
				CHACallGraph cg = new CHACallGraph(cha);
				
				System.out.println(CallGraphStats.getStats(cg));

			} catch (Exception e) {
				System.out.println("CG 3 Failed!");
			} 
	        
			//========================================
			
			System.out.println(" **************** CG # 4 - G.CG_AllEPoints myEx");
			try {// ------- It Worked !!!!!!!, but itake long time with some cases!
				System.out.println("Start building CG");
				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
				Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);

				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
				CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
				CallGraph cg = builder.makeCallGraph(options, null);
				
				System.out.println("Done building CG");
				System.out.println(CallGraphStats.getStats(cg));
				//* ss */searchCG(method, cg,className, lineNumber ,dOptions, cOptions, builder, key, appJar, appSrc);
				//* cc */searchCG_class(cg, className);
				/* mm */searchCG_class_by_method(cg, className, method);

//				System.out.println(CallGraphStats.collectMethods(cg));
				
			} catch (Exception e) {
				System.out.println("CG 4 Failed!");
			} 
			
			
			System.out.println(" **************** CG # 5 - G.CG_AllEPoints myEx_GUI ");
			try {// ------- It Worked !!!!!!!, but itake long time with some cases!
				System.out.println("Start building CG");
				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS_GUI.getBytes("UTF-8"))));
				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
				Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);

				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
				CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
				CallGraph cg = builder.makeCallGraph(options, null);
				
				System.out.println("Done building CG");
				System.out.println(CallGraphStats.getStats(cg));
				//* ss */searchCG(method, cg,className, lineNumber ,dOptions, cOptions, builder, key, appJar, appSrc);
				//* cc */searchCG_class(cg, className);
				/* mm */searchCG_class_by_method(cg, className, method);

//				System.out.println(CallGraphStats.collectMethods(cg));
				
			} catch (Exception e) {
				System.out.println("CG 5 Failed!");
			} 
	        
			//========================================

//			System.out.println(" **************** CG # 6 - G.CG_AllEPoints CGTestUtilEx");
//			try {// ------- It Worked !!!!!!!, but itake long time with some cases!
//				System.out.println("Start building CG");
//				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
//				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
//				Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
//				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
//				CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
//				CallGraph cg = builder.makeCallGraph(options, null);
//				System.out.println("Done building CG");
//				System.out.println(CallGraphStats.getStats(cg));
//				//* ss */searchCG(method, cg,className, lineNumber ,dOptions, cOptions, builder, key, appJar, appSrc);
//				//* cc */searchCG_class(cg, className);
//				/* mm */searchCG_class_by_method(cg, className, method);
//
////				System.out.println(CallGraphStats.collectMethods(cg));
//				
//			} catch (Exception e) {
//				System.out.println("CG 6 Failed!");
//			} 
	        
			//========================================

//			System.out.println(" **************** CG # 6");
//			try { //could not resolve < Application, Lsrc/main/java/com/revature/Account, main([Ljava/lang/String;)V >
//				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar, null);
//				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
//				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
//				Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints( cha, "L" + className);
//				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
//				CallGraphBuilder<InstanceKey> builder = Util.makeRTABuilder(options, new AnalysisCacheImpl(), cha);
//						//makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
//				CallGraph cg = builder.makeCallGraph(options, null);
//				System.out.println(CallGraphStats.getStats(cg));
//			} catch (Exception e) {
//				System.out.println("CG 6 Failed!");
//			} 
	        
//	        //========================================
//			
//			System.out.println(" **************** CG # 7");
//			try {
//				// Less Exclusions
//				System.out.println("Start building CG");
//				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));//
////				AnalysisScope scope =AnalysisScopeReader.instance.readJavaScope(appJar, new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS), CallGraphTestUtil.class.getClassLoader());
//						
//						//AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,new FileProvider().getFile( CallGraphTestUtil.REGRESSION_EXCLUSIONS));
////				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
//				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
//				Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
//
//				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
//				CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
//				CallGraph cg = builder.makeCallGraph(options, null);
//				
//				System.out.println("Done building CG");				
//				System.out.println(CallGraphStats.getStats(cg));
//			} catch (Exception e) {
//				//Failed
//				//AnalysisScope scope =AnalysisScopeReader.instance.readJavaScope(appJar, new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS), CallGraphTestUtil.class.getClassLoader());
//				System.out.println("CG 7 Failed!");
//			} 

			System.out.println(" **************** CG # 6' - G.CG_AllEPoints no EX");
			try {// ------- It Worked !!!!!!!, but itake long time with some cases!
				System.out.println("Start building CG");
				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
				Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
				CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCacheImpl(), cha);
				CallGraph cg = builder.makeCallGraph(options, null);
				System.out.println("Done building CG");
				System.out.println(CallGraphStats.getStats(cg));
				//* ss */searchCG(method, cg,className, lineNumber ,dOptions, cOptions, builder, key, appJar, appSrc);
				//* cc */searchCG_class(cg, className);
				/* mm */searchCG_class_by_method(cg, className, method);

//				System.out.println(CallGraphStats.collectMethods(cg));
				
			} catch (Exception e) {
				System.out.println("CG 6' Failed!");
			} 
			//========================================

			System.out.println(" **************** CG # 7 - G.CG_MainEPoints_MyEx ");
			try {
				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,null);
				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
				Iterable<Entrypoint> entrypoints =
				        com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(cha);
				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
				CallGraphBuilder<InstanceKey> builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
				CallGraph cg = builder.makeCallGraph(options, null);
				
				System.out.println("Done building CG");
				System.out.println(CallGraphStats.getStats(cg));
				//* ss */searchCG(method, cg,className, lineNumber ,dOptions, cOptions, builder, key, appJar, appSrc);
				//* cc */searchCG_class(cg, className);
				/* mm */searchCG_class_by_method(cg, className, method);


			} catch (Exception e) {
				System.out.println("CG 7 Failed!");
			} 
			
//	        //========================================

			System.out.println(" **************** CG # 8 - G.CG_MainEPoints_CGTUtilEx");
			try {// It worked
				System.out.println("Start building CG");
				AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(appJar,new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
//				scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(EXCLUSIONS.getBytes("UTF-8"))));
				IClassHierarchy cha = ClassHierarchyFactory.make(scope);
				Iterable<Entrypoint> entrypoints =
				        com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(cha);
				AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
				CallGraphBuilder<InstanceKey> builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
				CallGraph cg = builder.makeCallGraph(options, null);
				
				System.out.println("Done building CG");
				System.out.println(CallGraphStats.getStats(cg));
				//* ss */searchCG(method, cg,className, lineNumber ,dOptions, cOptions, builder, key, appJar, appSrc);
				//* cc */searchCG_class(cg, className);
				/* mm */searchCG_class_by_method(cg, className, method);
			} catch (Exception e) {
				System.out.println("CG 8 Failed!");
			} 
			System.out.println(" ******** Done");
			
//			//========================================
//
//			System.out.println(" **************** CG # 10");
//			try {
//			} catch (Exception e) {
//				System.out.println("CG 10 Failed!");
//			} 
//	        
//			//========================================
//
//			System.out.println(" **************** CG # 11");
//			try {
//			} catch (Exception e) {
//				System.out.println("CG 11 Failed!");
//			} 
//	        
//			//========================================
//
//			System.out.println(" **************** CG # 12");
//			try {
//			} catch (Exception e) {
//				System.out.println("CG 12 Failed!");
//			} 
//	        
//			//========================================

		
	return null;
	}
	public static void main(String[] args) throws Exception {
		// Search by method  /* mm */, search class //* cc */, seach to slice //* ss */
		//=====================
		

		String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/mariadb-connector-j-master/src/main/java/org/mariadb/jdbc/Connection.java";
		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/mariadb.jar";
//		build_cg("getLowercaseTableNames", jar,"Connection",262 , src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");

		

		
		String src2="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/mariadb-connector-j-master/src/benchmark/java/org/mariadb/jdbc/Select_1.java";
		build_cg("run", jar,"Select_1",18 , src2,
				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");

		
		//=====================
//		String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Data.java";
//		String src2= "/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/TFG_PC-master/src/Data/Clases/Imagen.java";
//		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/TFG_PC-master.jar";
//		build_cg("LastId", jar,"Data/Data",109 , src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");

		//=====================
//		String src="";
//		String jar="";
//		build_cg("", jar,"", , src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "");

		
		//=====================
//		String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/JavaGit-master/src/gs/veepeek/assessment/Assesment.java";
//		String jar="/Users/dpc100/Fixer/Experiments/JARS/JavaGit-master.jar";
//		build_cg("main", jar,"gs/veepeek/assessment/Assesment",122 , src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeUpdate");
		//=====================
//		String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/InventarioWeb-master/src/java/Modelo/dao/FacturaProductoDAO.java";
//		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/InventarioWeb.jar";
//		build_cg("insertar", jar,"Modelo/dao/FacturaProductoDAO", 31, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeUpdate");
		
		//=====================
//		//execute @ jButton1ActionPerformed @ 141 --> Method wasn't found
//		//executeUpdate @ actionPerformed @ 280 --> Method wasn't found
//		//executeQuery @ actionPerformed @ 335
//		//executeQuery @ updateActionPerformed @ 169
//		//executeQuery @ insertActionPerformed @ 467
//		//executeQuery @ jButton2ActionPerformed @ 835
//		//String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/GUI-DBMS-master/TableEasy/src/tableeasy/graphicaluserinterface/widgets/Addcoloumn.java";
//		String src ="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/GUI-DBMS-master/TableEasy/src/tableeasy/graphicaluserinterface/widgets/TableCreationBox.java";
////				"/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/GUI-DBMS-master/TableEasy/src/tableeasy/graphicaluserinterface/widgets/OtherQueries.java";
//		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/GUI-DBMS.jar";
////		executeUpdate @ actionPerformed @ 280
//		build_cg("DatabaseSelector", jar,"tableeasy/graphicaluserinterface/widgets/DatabaseSelector",32 , src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");
//		//Ltableeasy/graphicaluserinterface/widgets/TableCreationBox$3$1
		//=====================
//		tableeasy/graphicaluserinterface/widgets/Databases
		//executeQuery @ actionPerformed @ 232
//		String src ="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/GUI-DBMS-master/TableEasy/src/tableeasy/graphicaluserinterface/widgets/Databases.java";
//		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/GUI-DBMS.jar";
//		build_cg("actionPerformed", jar,"tableeasy/graphicaluserinterface/widgets/Databases",232 , src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");
		
		//=====================
//		executeUpdate @ screenMarketWithFiles @ 175
//		String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/fiftyfiftystockscreener-master/src/com/eddiedunn/screen/Screener.java";
//		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/fiftyfiftystockscreener.jar";
//		build_cg("screenMarketWithFiles", jar,"com/eddiedunn/screen/Screener", 175, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeUpdate");
//		
		//=====================
		//v_path_className: test/NHttpClientConnManagement, lno:247 key: executeUpdate
//		String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/fiftyfiftystockscreener-master/src/com/eddiedunn/test/NHttpClientConnManagement.java";
//		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/fiftyfiftystockscreener.jar";
//		//com/eddiedunn/test/NHttpClientConnManagement/GetKeyStatisticsRequestHandler
//		//com/eddiedunn/test/NHttpClientConnManagement$GetKeyStatisticsRequestHandler
//		//
//		build_cg("handleResponse", jar,"com/eddiedunn/test/NHttpClientConnManagement$GetKeyStatisticsRequestHandler", 398, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeUpdate");
				
		//=====================
		
//		 //executeUpdate @ phone_buttonActionPerformed @ 115
//		String src= "/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/Ecomerce--master/Ecomerce/src/ecomerce/Phone.java";
//		String jar= "/Users/dpc100/Fixer/Experiments/JARS/ecomerce2.jar";//"/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/Ecomerce.jar";
////				"/Users/dpc100/Fixer/Experiments/JARS/Ecomerce.jar";
//		build_cg(null, jar,"ecomerce/Phone", 115, src,
//		0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeUpdate");
		//=====================
//		//v_path_className: util/JdbcHelper, lno:57 key: execute  --- slicer failed
//		String src ="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/crawledemo-master/property/src/com/property/util/JdbcHelper.java";
//		String jar ="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/crawledemo_property.jar";
//		//crawledemo-master/property/src/com/property/util/JdbcHelper
//		build_cg(null, jar,"com/property/util/JdbcHelper", 57, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "execute");
		//=====================		
//		//executeQuery @ orderActionPerformed @ 257
//		String src ="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/crawledemo-master/property/src/com/property/util/JdbcHelper.java";
//		String jar ="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/crawledemo_property.jar";
//		//crawledemo-master/property/src/com/property/util/JdbcHelper
//		build_cg(null, jar,"com/property/util/JdbcHelper", 257, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "execute");

		//=====================
//		//executeQuery @ querysql @ 14 --> failed to parse
//		String src ="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace/myapi.java";
//		String jar ="/Users/dpc100/Fixer/Experiments/JARS/Online-Book-Store-System-master.jar";
//
//		build_cg(null, jar,"workspace/myapi", 14, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");

		//************************** >  STABLE CASE, works with all CGs and with the new Ex
		
//		//executeQuery @ 15 
//		String src= "/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/Online-Book-Store-System-master/src/workspace/myfeedback.java";
//		String jar= "/Users/dpc100/Fixer/Experiments/JARS/bookstore.jar";
//		build_cg(null, jar,"workspace/myfeedback", 15, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");
		//=====================
		//"/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src/main/java/com/revature/App.java";
		// "/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src";
		//"/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app.jar";
//		"/Users/dpc100/Fixer/Experiments/JARS/Online-Book-Store-System-master.jar";
		//"/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app.jar";
//		main/java/com/revature/Account
		//com/revature/Account
//		"com/revature/App", 23
		
//		DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE
	//   executeQuery @  @ 172 
	//   executeQuery @ viewAllaccounts @ 212
		
		//=====================
//		//executeQuery @ login @ 87
//		String src="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/banking-app-devkala48-master/src/main/java/com/revature/UsersAuth.java";
//		String jar="/Users/dpc100/Desktop/DCAFixer_Experimets/SQLIFix-projects/jar_files/banking-app.jar";
////		main/java/com/revature/UsersAuth
//		build_cg(null, jar,"com/revature/UsersAuth", 87, src,
//				0, DataDependenceOptions.NO_HEAP, ControlDependenceOptions.NONE, 's', "executeQuery");
	}
}





////String methodName = null;
////methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);				
//String methodName ="";
//for (CGNode N : cg) {
//if ((N.getMethod().getDeclaringClass().getName().toString().equals("L"+className))) {
//	System.out.println("Horray! " + N.getMethod().getName().toString());
//	methodName = SlicerUtil.find_seed_method2(cg, className, lineNumber);
//	System.out.println("methodName: "+methodName);
//}
//
////if (N.getMethod().getName().toString().equals("main")) {
////	System.out.println(N.getMethod().getName().toString() + "\n"
////			+ N.getMethod().getDeclaringClass().getName().toString() + "\n");
//////	methodName = SlicerUtil.find_seed_method(cg, className, lineNumber);
////	System.out.println("methodName: " + methodName);
////	break;
////}
//}
//
//if(methodName != null && methodName.length()>0) {
//CGNode targeted_method_node = SlicerUtil.find_targeted_method(cg, methodName, className);
//Statement s = SlicerUtil.find_seed_by_lno_and_key(targeted_method_node, lineNumber, key);
//System.out.println(s.toString());
//Collection<Statement> backward_slice;
//backward_slice = Slicer.computeBackwardSlice(s, cg, builder.getPointerAnalysis(), dOptions, cOptions);
//}
