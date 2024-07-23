#!/bin/bash

# Usage: java -jar yourapp.jar <projectSrc> <projectJar> <projectName> <reportDir> <patternsPath>");

# Get the current working directory
current_directory=$(pwd)

# Print the current and new directory paths
#echo "Current Directory: ${current_directory}"

# ================ Example 1
# Append the string to the current directory
projectSrc="${current_directory}/simpletest1/src"
projectName="Simpletest1"
find ${projectSrc} -type fr -name '*_fixed.java' -exec rm -i {} \;
rm -r ${current_directory}/out/dcafixer-report/${projectName}

# ================ Example 2
# Append the string to the current directory
 projectSrc="${current_directory}/simpletest2/src"
 projectName="Simpletest2"

find ${projectSrc} -type fr -name '*_fixed.java' -exec rm -i {} \;
rm -r ${current_directory}/out/dcafixer-report/${projectName}