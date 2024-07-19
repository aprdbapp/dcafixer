## Patch Validation:

To validate generated patches for SQL Injection Vulnerabilities (SQLIVs), you can fuzz the generated patches. This will help detect issues that might be introduced during the fixing process, such as syntax errors in the queries. To achieve this, you need to create two test cases: one for the original vulnerable slice and one for the generated patch. Then, use the DCAFixer SQL injection payload generator as illustrated in the following example.

First, you need to download JQF and follow the instructions provided by the developers at the following link:
https://github.com/rohanpadhye/JQF

**Vulnerable-Slice (VSlice):**

VSlice created by DCAFixer:
```java
String sql3= "Select * from ACCOUNTS where name = '" + custname + "';";
ResultSet rs3 = stmt.executeQuery(sql3);
```

Test case for the VSlice:
```java
public ResultSet vslice(Statement stmt, String custname) {
	String sql3= "Select * from ACCOUNTS where name = '" + custname + "';";
	ResultSet rs3 = stmt.executeQuery(sql3);
	return rs3;
}
```


VSlice assertion:


**Generated Patch (GPatch):**
GPatch created by DCAFixer:
```java
String sql3 = "SELECT * FROM ACCOUNTS WHERE name = ?";			
PreparedStatement pstmt50 = stmt.getConnection().prepareStatement(sql3);
pstmt50.setObject(1, custname);;
ResultSet rs3 = pstmt50.executeQuery();
```

Test case for the GPatch:
```java
public ResultSet gpatch(Connection conn, String custname) {
	String sql3 = "SELECT * FROM ACCOUNTS WHERE name = ?";	
	PreparedStatement pstmt50 = conn.prepareStatement(sql3);
	pstmt50.setObject(1, custname);;
	ResultSet rs3 = pstmt50.executeQuery();
	Return rs3;
}
```

**Using Assertion:**
Create Fuzz test cases:
```java
public class FuzzTest{
   //Add all necessary information for connection 

   @Fuzz
   public void fuzz_test_vslice(@From(SqlInjectonGenerator.class) String var1)
			throws SQLException, ClassNotFoundException {
	Connection conn = null;
	Class.forName("com.mysql.cj.jdbc.Driver");
	conn = DriverManager.getConnection(DB_URL, DB_UN, DB_PW);
	Statement stmt = conn.createStatement();
	assertTrue(var1 + ", " + " is/are the input to perform SQLi attack on VSlice!", vslice(conn, var1).next() == false);
	if(stmt != null)
		stmt.close();
	if (conn != null)
		conn.close();		
   }
   @Fuzz
   public void fuzz_test_gpatch(@From(SqlInjectonGenerator.class) String var1)
			throws SQLException, ClassNotFoundException {
	Class.forName("com.mysql.cj.jdbc.Driver");
	Connection conn = DriverManager.getConnection(DB_URL, DB_UN, DB_PW);
	Statement stmt = conn.createStatement();
	assertTrue(var1 + ", " + " is/are the input to perform SQLi attack on gpatch!", gpatch(conn, var1).next() == false);
	if(stmt != null)
		stmt.close();
	if (conn != null)
		conn.close();
		
   }
}
```


How to compile the fuzzer: 
```bash
javac -cp .:$(path to/jqf/scripts/classpath.sh) SqlInjectonGenerator.java FuzzTest.java 
```

How to run the fuzzer: 
```bash
path to/jqf/bin/jqf-zest -c .:$(path to/jqf/scripts/classpath.sh) FuzzTest fuzz_test_gpatch
```

To test the found payload if there is one:
```bash
path to/jqf/bin/jqf-repro -c .:$(path to/jqf/scripts/classpath.sh) FuzzTest fuzz_test_gpatch fuzz-results/failures/id..
```

To test the found payload if there is one:
```bash
path to/jqf/bin/jqf-repro -c .:$(path to/jqf/scripts/classpath.sh) FuzzTest fuzz_test_vslice fuzz-results/failures/id..
```
You can find DCAFixer generator in `dcafixer/DataSet/SqlInjectonGenerator.java`


**Unit testing:**

We can use the test cases to perform unit tests. In these tests, we use a benign valid input and compare the results of both the original vulnerable slice (vslice) and the generated patch (gpatch). If the gpatch behaves differently, it indicates that the SQL command changes introduced errors.
```java
public void unit_test(String var1) throws SQLException, ClassNotFoundException {
	// Create conn and stmt

	ResultSet rs1 = vslice(stmt, var1);
	ResultSet rs2 = gpatch(conn, var1);
	//Compre rs1 and rs2
	// Close conn and stmt
		
}
```
