Introduction
This prototype is designed to locate and repair SQL injection vulnerabilities and hardcoded credentials in Java client applications. Please follow the instructions below to test the framework. 


Installation
To install and set up this prototype, follow these steps:
1- Clone the repository:
git clone https://github.com/aprdbapp/dcafixer.git 
cd path to dcafixer

2- Download the required dependencies:
You can download their jar files or build the tools using the instructions in their repositories.
1- WALA https://github.com/wala/WALA/blob/master/README.md
2- JSQLParser https://github.com/JSQLParser/JSqlParser
3- Javaparser https://github.com/javaparser/javaparser
4- gumtree-spoon-ast-diff https://github.com/SpoonLabs/gumtree-spoon-ast-diff


To test the prototype to fix an application (Input: Vulnerable project):
Build the jar of the project 
Provide the project src code path 
Provide the projectjar path
Provide a name for your project "project-name"; the system will use it to create a folder under out directory to store the report.

In class "src/dcafixer/Main.java" you can add the following line to start DCAFixer

Fixer.start_dcafixer(projectName,projectSrc, projectJar);

where 
projectName = It is the name of the project without spaces. It should be unique; don't name two project using the same name to avoid mixed results from two projects in the same folder.
projectSrc = It is the path where the project source code is located.

projectJar = It is the path to the project jar file. 


You will find slices of the vulnerabilities and a report of fixed and non-fixed vulnerabilities under directory out/projectName. The report file is named "report.txt"




# dcafixer
Requirements: 

1- WALA
https://github.com/wala/WALA/blob/master/README.md

2- JSQLParser
https://github.com/JSQLParser/JSqlParser

3- Javaparser
https://github.com/javaparser/javaparser

4- gumtree-spoon-ast-diff
https://github.com/SpoonLabs/gumtree-spoon-ast-diff

5- JQF 
https://github.com/rohanpadhye/JQF

For further testing you need: 

- Wireshark 
https://www.wireshark.org/

- jmap
https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr014.html

======================================
https://github.com/aprdbapp/dcafixer/blob/main/TESTING.md

Patch Validation:

To validate generated patches for SQL Injection Vulnerabilities (SQLIVs), you can fuzz the generated patches. This will help detect issues that might be introduced during the fixing process, such as syntax errors in the queries. To achieve this, you need to create two test cases: one for the original vulnerable slice and one for the generated patch. Then, use the DCAFixer SQL injection payload generator as illustrated in the following example.

First, you need to download JQF and follow the instructions provided by the developers at the following link:
https://github.com/rohanpadhye/JQF


## Vulnerable-Slice (VSlice): 

VSlice created by DCAFixer: 


Test case for the VSlice:

Fuzzing test case for VSlice:


 
Generated Patch (GPatch):
GPatch created by DCAFixer:

Test case for the GPatch: 

Fuzzing test case for GPatch: 

 
