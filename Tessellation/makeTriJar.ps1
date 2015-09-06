# PowerShell script
# File-Name: makeTriJar.ps1
#####################################################
# This is an example of a PowerShell script.
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
if ($args.Count -lt 1 ){
    $run="library"
    Write-Host "script will create $run"
}else{
    $run="standalone"
    Write-Host "script will create $run $args"
}
#####################################################
# define variables for library package
#####################################################
$libPkg="hbl/jag/tri/lib/"
Write-Host "the library package is $libPkg"
#####################################################
# rebuild class files for library
#####################################################
Remove-Item $libPkg*.class
Write-Host "rebuilding library class files"
javac $libPkg*.java
#####################################################
# check main branch library or standalone
#####################################################
if( $run -eq "library" ){

#####################################################
#####################################################
# clean the old trilib file
#####################################################
$filefound= Test-Path trilib.jar
if($filefound -eq "True"){
    Write-Host "deleting old trilib.jar"
    Remove-Item trilib.jar
}
Write-Host "building new trilib.jar"
#####################################################
# create a temporary manifest file 
#####################################################
Set-Content tempmanifest.txt "Class-Path: ."
Add-Content tempmanifest.txt "Manifest-Version: hbl.jag.tri.3.1.4"
Add-Content tempmanifest.txt "Version-Date: 20150801"
Write-Host "additions to library manifest"
Get-Content tempmanifest.txt 
#####################################################
# build basic jar file
#####################################################
jar cvfm "trilib.jar" "tempmanifest.txt" "$libPkg*.class"
Remove-Item tempmanifest.txt -Force
#####################################################
# recreate jar file as zip for examination
#####################################################
$filefound= Test-Path "LibraryJARcontent"
if($filefound -eq "True"){
    Write-Host "deleting old LibraryJARcontent"
    Remove-Item -Recurse  "LibraryJARcontent" -Force
}
Write-Host "building new  LibraryJARcontent"
Copy-Item "trilib.jar" "trilib.zip"
Expand-Archive -Path "trilib.zip" -DestinationPath "LibraryJARcontent"
Remove-Item "trilib.zip" -Force
#####################################################
#####################################################

}else{

#####################################################
#####################################################
# define shell variable
#####################################################
$samPkg="hbl/jag/tri/sam"
Write-Host "the sample package is $samPkg"
$target=$args
Write-Host "the main entry target is $target"
#####################################################
# rebuild class file for target
#####################################################
Remove-Item $samPkg/$target.class
Write-Host "rebuilding target class file"
javac $samPkg/$target.java
#####################################################
# create a temporary manifest file 
#####################################################
Set-Content tempmanifest.txt "Class-Path: ."
Add-Content tempmanifest.txt "Manifest-Version: hbl.jag.tri.3.1.4"
Add-Content tempmanifest.txt "Version-Date: 20150801"
Add-Content tempmanifest.txt "Main-Class: $samPkg.$target "
Write-Host "additions to standalone manifest"
Get-Content tempmanifest.txt 
#####################################################
# build basic jar file
#####################################################
jar cvfm "$target.jar"  "tempmanifest.txt"  "$libPkg*.class"  "$samPkg/$target.class"
Remove-Item tempmanifest.txt -Force
#####################################################
# recreate jar file as zip for examination
#####################################################
$filefound = Test-Path "SampleJARcontent"
if($filefound -eq "True"){
    Write-Host "deleting old SampleJARcontent"
    Remove-Item -Recurse  "SampleJARcontent" -Force
}
Write-Host "building new  SampleJARcontent"
Copy-Item "$target.jar" "$target.zip"
Expand-Archive -Path "$target.zip" -DestinationPath "SampleJARcontent"
Remove-Item "$target.zip" -Force
#####################################################
#####################################################

}







