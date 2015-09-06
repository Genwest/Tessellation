#! /bin/bash -
# File-Name: makeTriJar.sh
#####################################################
# This is an example of a Linux shell script.
# If run with no command line arguments it will create
# a jar file for the package /hbl/jag/tri/lib called 
# "trilib.jar"
# If the script is called with a single "string" argument 
# that string will be assumed  to be the name of a user 
# written calling program and the script will create
# an executable "string".jar" 
#####################################################
# determine if a user file is envolked
#####################################################
if [ $# -lt 1 ]
then 
  run=library
  echo script will create $run
else
  run=standalone
  echo script will create $run $1
fi
#####################################################
# define variables for library package
#####################################################
libPkg=hbl/jag/tri/lib/
printf "the library package is ${libPkg}\n"
#####################################################
# rebuild class files for library
#####################################################
rm ${libPkg}*.class
echo "rebuilding library class files"
javac ${libPkg}*.java
#####################################################
# check main branch library or standalone
#####################################################
if [ ${run} = library ]
then 

#####################################################
#####################################################
# clean the old trilib file
#####################################################
if [ -f trilib.jar ]
then
  printf "deleting old trilib.jar\n"
  rm trilib.jar
fi
printf "building new trilib.jar\n"
#####################################################
# create a temporary manifest file 
#####################################################
cat <<- EOF > tempmanifest.txt
Class-Path: . 
Manifest-Version: hbl.jag.tri.3.1.4
Version-Date: 20150801
EOF
cat tempmanifest.txt
#####################################################
# build basic jar file
#####################################################
jar cvfm trilib.jar  tempmanifest.txt ${libPkg}*.class
chmod a+x trilib.jar
rm tempmanifest.txt
#####################################################
# recreate jar file as zip for examination
#####################################################
if [ -d LibraryJARcontent ] 
then
  rm -r LibraryJARcontent
fi
mkdir LibraryJARcontent
unzip -q trilib.jar -d LibraryJARcontent 
#####################################################
#####################################################

else 

#####################################################
#####################################################
# define shell variables
#####################################################
samPkg=hbl/jag/tri/sam
printf "the sample package is ${samPkg}\n"
target=$1
printf "the main entry target is ${target}\n"
#####################################################
# clean the old trilib file
#####################################################
if [ -f ${target}.jar ]
then
  printf "deleting old ${target}.jar\n"
  rm ${target}.jar
fi
printf "building new ${target}.jar\n"
#####################################################
# rebuild class file for target
#####################################################
rm ${samPkg}/${target}.class
echo "rebuilding target class file"
javac ${samPkg}/${target}.java
#####################################################
# create a temporary manifest file 
#####################################################
cat <<- EOF > tempmanifest.txt
Class-Path: . 
Manifest-Version: hbl.jag.tri.3.1.4
Creation-Date: 20150801
Main-Class: ${samPkg}.${target}
EOF
cat tempmanifest.txt
#####################################################
# build basic jar file
#####################################################
jar cvfm ${target}.jar  tempmanifest.txt ${libPkg}*.class ${samPkg}/${target}.class
chmod a+x ${target}.jar
rm tempmanifest.txt
#####################################################
# recreate jar file as zip for examination
#####################################################
if [ -d SampleJARcontent ] 
then
  rm -r SampleJARcontent
fi
mkdir SampleJARcontent
unzip -q ${target}.jar -d SampleJARcontent 
#####################################################
#####################################################

fi

  
