public class Slice {public static void main() {
ResultSet rs1 = stmt.executeQuery("Select * from " + table + " where custid<=10;");//sig: (q,s,t)
}}