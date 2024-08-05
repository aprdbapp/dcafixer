# DCAFixer
This prototype is designed to locate and repair SQL injection vulnerabilities and hardcoded credentials in Java client applications. Please follow the instructions below to test the framework.

## Getting Started

We recommend using Eclipse as it is the IDE we used for developing and testing.

### 1. Download the Code

Download the code using the following command:

```sh
git clone https://github.com/aprdbapp/dcafixer.git
```

### 2. Open the Project in Eclipse

1. From Eclipse:
   - Select `File` -> `Open Projects From File System`.
   - Browse to the place where you downloaded the code in `/path/to/dcafixer`.
   - Press `Open`, then `Finish`.

### 3. Add All the Required Dependencies

1. Go to the project in Eclipse and right-click on the project.
2. Select `Build Path` -> `Configure Build Path`.
3. In the `Libraries` tab, click on `Modulepath` then press on the right side `Add External JARs` button.
4. Browse to where you downloaded `dcafixer` to the `lib` folder (`/path/to/dcafixer/lib`).
5. Select all the dependencies and press `Open`, then `Apply and Close`.

## Running the Examples

Now the system is ready to be used. You can go to the file `/path/to/dcafixer/src/dcafixer/Main.java`. You will find that the code for two examples is written and you can run it directly.

1. Right-click on `Main.java` and select `Run As` -> `Java Application`.

### Example Descriptions

#### Simpletest1

Each example has a separate folder:
- `dcafixer/simpletest1`
- `dcafixer/simpletest2`

Each folder contains the source code, which can be found in:
- `dcafixer/simpletest1/src`
- `dcafixer/simpletest2/src`

Their JAR files are also provided to simplify testing the code. You can find the JAR files in:
- `dcafixer/simpletest1/lib`
- `dcafixer/simpletest2/lib`

After running `/path/to/dcafixer/src/dcafixer/Main.java`, the system will create fixed code and generate a report about the vulnerabilities.

## Expected Output

**For `simpletest1`:**
1. You'll find the original code in `dcafixer/simpletest1/src/VulExample.java` and its fixed copy in `dcafixer/simpletest1/src/VulExample_fixed.java`.
2. Under `dcafixer/out/dcafixer-report/Simpletest1`, you will find the following files:
   - `VulExample20_VSlice.java`
   - `VulExample48_VSlice.java`
   - `VulExample25_VSlice.java`
   - `VulExample36_VSlice.java`
   - `report.txt`

**For `simpletest2`:**
1. You'll find the original code in `dcafixer/simpletest2/src/QExecute.java` and its fixed copy in `dcafixer/simpletest2/src/QExecute_fixed.java`.
2. You'll find the original code in `dcafixer/simpletest2/src/VulExample2.java` and its fixed copy in `dcafixer/simpletest2/src/VulExample2_fixed.java`.

3. Under `dcafixer/out/dcafixer-report/Simpletest2`, you will find the following files:
   - `QExecute13_VSlice.java`
   - `VulExample216_VSlice.java`
   - `QExecute27_VSlice.java`
   - `report.txt`


**You can find a copy of all expected output from both `simpletest1` and `simpletest2` in the folder `dcafixer/expected-output`, so you can compare the results you got to the expected ones.**


You can run your own example by updating the code in `/path/to/dcafixer/src/dcafixer/Main.java`. Simply add the following code:

```java
Fixer.start_dcafixer(projectName, projectSrc, projectJar);
```

Where:

- `projectName`: The name of the project without spaces. It should be unique; do not name two projects with the same name to avoid mixed results from two projects in the same folder.

- `projectSrc`: The path where the project source code is located.

- `projectJar`: The path to the project JAR file.


## Note:
You can test the system using a runnable jar file by following the instructions [HERE](https://github.com/aprdbapp/dcafixer/edit/main/runnablejar.md).

