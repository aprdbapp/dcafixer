/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patchgenerator;

import com.github.gumtreediff.gen.Registry.Entry;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author Dareen
 */
public final class VulMD {

    private String v_sl;
    private String v_src;
    private String v_con;
    private String s_sl;
    private String s_src;
    private String s_con;
    HashMap<Integer, Integer> sl_to_src_map = new HashMap<>();
    HashMap<Integer, String> srcln_to_method = new HashMap<>();
//    ArrayList<String> sl_lines = new ArrayList();

    public String get_v_sl_path() {
        return v_sl;
    }

    public String get_v_src_path() {
        return v_src;
    }

    public String get_v_con_path() {
        return v_con;
    }

    public String get_s_sl_path() {
        return s_sl;
    }

    public String get_s_src_path() {
        return s_src;
    }

    public String get_s_con_path() {
        return s_con;
    }
    
    public void set_v_sl_path(String vsl) {
         v_sl = vsl;
    }

    public void set_v_src_path(String value) {
         v_src = value;
    }

    public void set_v_con_path(String value) {
         v_con= value;
    }

    public void set_s_sl_path(String value) {
         s_sl= value;
    }

    public void set_s_src_path(String value) {
         s_src= value;
    }

    public void set_s_con_path(String value) {
         s_con= value;
    }

    public VulMD(String sig, String type) {
        String slices_and_context_path = "/Users/Dareen/Fixer/tmp/TSet/Slices/" + type + "/";
        String md_subPath = sig.replace(",", "_").replace(" ", "").replace("[", "").replace("]", "") + "_md.txt";
        String mdPath = slices_and_context_path + md_subPath;
        srcln_to_method.clear();
        srcln_to_method.clear();
//        System.out.println(mdPath);
        getfiles_paths(mdPath);
        get_v_con_data();
//        this.sl_lines = get_v_sl_code_lines();
        

    }

    public void get_v_con_data() {
        File myObj = new File(this.v_con);
        try (Scanner myReader = new Scanner(myObj)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
//                System.out.println(data);
                String[] str = data.split(",");
                
                this.sl_to_src_map.put(Integer.parseInt(str[0].trim()), Integer.parseInt(str[2].trim()));
                this.srcln_to_method.put(Integer.parseInt(str[2].trim()), str[1]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
        }
        
//        System.out.println(this.sl_to_src_map.toString());
//        int srcln = sl_to_src_map.get(2); 
//        String method= srcln_to_method.get(srcln);
//        System.out.println("line 2 in slic is line "+ srcln+"@"+method +" in src");
//        System.out.println(this.srcln_to_method.toString());
    }
    public void getfiles_paths(String mdPath){
    String var, path;
        try {
            File myObj = new File(mdPath);
            try (Scanner myReader = new Scanner(myObj)) {
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
//                    System.out.println(data);
                    String[] str = data.split(":");
                    var = str[0];
                    path = str[1];
//                    path = data.substring(data.lastIndexOf(':') + 1);
                    switch (var) {
                        case "":
                            break;
                        case "v_sl":
                            this.v_sl = path;
                            break;
                        case "v_src":
                            this.v_src = path;
                            break;
                        case "v_con":
                            this.v_con = path;
                            break;
                        case "s_sl":
                            this.s_sl = path;
                            break;
                        case "s_src":
                            this.s_src = path;
                            break;
                        case "s_con":
                            this.s_con = path;
                            break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    public ArrayList<String> get_v_sl_code_lines() {
        
        File myObj = new File(this.v_sl);
            ArrayList<String> sl_lines = new ArrayList<String>();

        try (Scanner myReader = new Scanner(myObj)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println(data);
                sl_lines.add(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
        }
        
    return sl_lines;
    }
    
    public ArrayList<String> get_v_src_code_lines() {
        
        File myObj = new File(this.v_src);
            ArrayList<String> sl_lines = new ArrayList<String>();

        try (Scanner myReader = new Scanner(myObj)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
//                System.out.println(data);
                sl_lines.add(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
        }
        
    return sl_lines;
    }
 public int map_sllno_to_srcln(int sllno){
     return sl_to_src_map.get(sllno);
 }
}
