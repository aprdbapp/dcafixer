#!/bin/bash


sh clean-examples.sh

# Usage: java -jar yourapp.jar <projectSrc> <projectJar> <projectName> <reportDir> <patternsPath>");

# Get the current working directory
current_directory=$(pwd)

# Print the current and new directory paths
#echo "Current Directory: ${current_directory}"

# ================ Example 1
# Append the string to the current directory
projectSrc="${current_directory}/simpletest1/src/"
projectJar="${current_directory}/simpletest1/lib/simpletest1.jar"
projectName="Simpletest1"
reportDir="${current_directory}/out/dcafixer-report/"
patternsPath="${current_directory}/patterns/"
# Print the current and new directory paths
#echo "${projectSrc} ${projectJar} ${projectName} ${reportDir} ${patternsPath}"

java -jar build/DCAFixer.jar ${projectSrc} ${projectJar} ${projectName} ${reportDir} ${patternsPath}

# ================ Example 2
# Append the string to the current directory
 projectSrc="${current_directory}/simpletest2/src/"
 projectJar="${current_directory}/simpletest2/lib/simpletest2.jar"
 projectName="Simpletest2"
 reportDir="${current_directory}/out/dcafixer-report/"
 patternsPath="${current_directory}/patterns/"
# Print the current and new directory paths
# echo "${projectSrc} ${projectJar} ${projectName} ${reportDir} ${patternsPath}"

 java -jar build/DCAFixer.jar ${projectSrc} ${projectJar} ${projectName} ${reportDir} ${patternsPath}

echo "Done!"