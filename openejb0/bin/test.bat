@echo off
REM $Id$

REM   Contributions by:
REM      David Blevins <david.blevins@visi.com>

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java

set CP=
for %%i in (%OPENEJB_HOME%\dist\openejb*.jar) do call cp.bat %%i
set CP=%JAVA_HOME%\lib\tools.jar;%CP%

set SERVER=-Dopenejb.test.server=org.openejb.test.IvmTestServer
set DATABASE=-Dopenejb.test.database=org.openejb.test.InstantDbTestDatabase
set OPTIONS=%SERVER% %DATABASE%

echo --------------SUPPORT INFO-------------
echo %OS%
echo Using JAVA_HOME:     %JAVA_HOME%
echo Using OPENEJB_HOME:  %OPENEJB_HOME%
echo .

%JAVA% %OPTIONS% -classpath %CP% org.openejb.test.Main -s src\tests-ejb\IvmServer_config.properties org.openejb.test.ClientTestSuite

