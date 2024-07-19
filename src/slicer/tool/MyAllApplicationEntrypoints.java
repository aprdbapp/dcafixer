package slicer.tool;


import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import java.util.HashSet;
import java.util.function.Function;

/** Includes all application methods in an analysis scope as entrypoints. */
public class MyAllApplicationEntrypoints extends HashSet<Entrypoint> {

  private static final long serialVersionUID = 6541081454519490199L;
  private static final boolean DEBUG = false;

  /**
   * @param scope governing analyais scope
   * @param cha governing class hierarchy
   * @throws IllegalArgumentException if cha is null
   */
  public MyAllApplicationEntrypoints(
      AnalysisScope scope,
      final IClassHierarchy cha,
      Function<IClass, Boolean> isApplicationClass) {

    if (cha == null) {
      throw new IllegalArgumentException("cha is null");
    }
    for (IClass klass : cha) {
    	
      if (!klass.isInterface()) {
    	 System.out.println("NOT Interface klass name (not an interface): "+klass.getName().toString());
    	 
        if (isApplicationClass.apply(klass)) {
        	System.out.println("----- Methods: ");
          for (IMethod method : klass.getDeclaredMethods()) {
        	  
            if (!method.isAbstract()) {
            	System.out.println(method.getName().toString()+"@"+method.getLineNumber(0));
              add(new ArgumentTypeEntrypoint(method, cha));
            }
          }
        }
      } else {
    	  System.out.println("Interface klass name: "+klass.getName().toString());
      
      if (isApplicationClass.apply(klass)) {
    	  System.out.println("----- Methods: ");
          for (IMethod method : klass.getDeclaredMethods()) {
        	  
            if (!method.isAbstract()) {
            	System.out.println(method.getName().toString()+"@"+method.getLineNumber(0));
            }
          }
        }
      }
    }
//    if (DEBUG) {
      System.err.println((getClass() + "Number of EntryPoints:" + size()));
//    }
  }

  public MyAllApplicationEntrypoints(AnalysisScope scope, final IClassHierarchy cha) {
    this(
        scope,
        cha,
        (IClass klass) ->
            scope.getApplicationLoader().equals(klass.getClassLoader().getReference()));
  }
}