@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java

set CP=
for %%i in (%OPENEJB_HOME%\dist\openejb*.jar) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%
set CLASSPATH=%CLASSPATH%;%CP%

set OPTIONS=-Dopenejb.home=%OPENEJB_HOME%

echo --------------SUPPORT INFO-------------
echo %OS%
echo Using JAVA_HOME:     %JAVA_HOME%
echo Using OPENEJB_HOME:  %OPENEJB_HOME%
echo .
%JAVA% %OPTIONS% -classpath %CLASSPATH% org.openejb.server.Stop  %1 %2 %3 %4 %5 %6

