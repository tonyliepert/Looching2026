# Windows build script for jlooch (replaces the Linux Makefile)
#   .\build.ps1        compile into .\classes
#   .\build.ps1 go     compile, then run

param([string]$Target = "jlooch")

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# change this to reflect your own installation of java (JDK 21 still has the Applet API)
if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = (Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -Filter "jdk-21*" | Select-Object -First 1).FullName
}
$javac = Join-Path $env:JAVA_HOME "bin\javac.exe"
$java  = Join-Path $env:JAVA_HOME "bin\java.exe"

# pure-Java JSyn: the old com.softsynth.jsyn API is a layer over the new com.jsyn engine
$jsyn      = "lib\jsyn-old-api-20161206.jar;lib\jsyn-20171016.jar"
$classDir  = "classes"
$classpath = "$classDir;$jsyn"

New-Item -ItemType Directory -Force $classDir | Out-Null
& $javac -nowarn -encoding ISO-8859-1 -d $classDir -classpath $jsyn (Get-ChildItem *.java).Name
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($Target -eq "go") {
    # jlooch loads loochicon.gif relative to the working directory
    & $java -classpath $classpath jlooch
}
