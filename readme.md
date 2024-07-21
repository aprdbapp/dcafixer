# DCAFixer

## Introduction
This prototype is designed to locate and repair SQL injection vulnerabilities and hardcoded credentials in Java client applications. Please follow the instructions below to test the framework.

## Installation
To install and set up this prototype, follow these steps:

1. **Clone the repository**:
    ```bash
    git clone https://github.com/aprdbapp/dcafixer.git
    ```

2. **Download the required dependencies**:
    You can download their jar files or build the tools using the instructions in their repositories:
    - [WALA](https://github.com/wala/WALA/blob/master/README.md)
    - [JSQLParser](https://github.com/JSQLParser/JSqlParser)
    - [Javaparser](https://github.com/javaparser/javaparser)
    - [gumtree-spoon-ast-diff](https://github.com/SpoonLabs/gumtree-spoon-ast-diff)

## Usage
To test the prototype to fix an application (Input: Vulnerable project):

1. Build the jar of the project.
2. Provide the project source code path.
3. Provide the project jar path.
4. Provide a name for your project. The system will use this name to create a folder under the `out` directory to store the report.

In the class `src/dcafixer/Main.java`, you can add the following line to start DCAFixer:

```java
Fixer.start_dcafixer(projectName, projectSrc, projectJar);
```
where:

projectName = The name of the project without spaces. It should be unique; don't name two projects with the same name to avoid mixed results from two projects in the same folder.

projectSrc = The path where the project source code is located.

projectJar = The path to the project jar file.


## Output 
You will find slices of the vulnerabilities and a report of fixed and non-fixed vulnerabilities under the directory `out/dcafixer-report/projectName`. The report file is named `report.txt`.

Instructions on patch validation are in [HERE](https://github.com/aprdbapp/dcafixer/blob/main/patch-validation.md)
