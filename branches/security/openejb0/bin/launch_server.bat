@echo off
REM $Id$

if "%OPENEJB_HOME%"=="" set OPENEJB_HOME=%CD%

set JAVA=%JAVA_HOME%\bin\java

set CP=
for %%i in (%OPENEJB_HOME%\dist\openejb*.jar) do call cp.bat %%i
for %%i in (%OPENEJB_HOME%\lib\openejb*.jar) do call cp.bat %%i

set CP=%OPENEJB_HOME%\lib\castor-0.9.3.9.jar;%CP%
set CP=%OPENEJB_HOME%\lib\ejb-1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\idb_3.26.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jakarta-regexp-1.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jca_1.0.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jdbc2_0-stdext.jar;%CP%
set CP=%OPENEJB_HOME%\lib\jta_1.0.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\junit_3.5.jar;%CP%
set CP=%OPENEJB_HOME%\lib\log4j-1.2.1.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xercesImpl-2.0.2.jar;%CP%
set CP=%OPENEJB_HOME%\lib\xmlParserAPIs-2.0.2.jar;%CP%

set CP=%JAVA_HOME%\lib\tools.jar;%CP%

REM   Launch the naming server
REM ######################################
REM  Launch the OpenEJB/CORBA adapter
set OPENORB_OPTIONS= -Djava.naming.factory.initial=org.openorb.rmi.jndi.CtxFactory -Dorg.omg.CORBA.ORBClass=org.openorb.CORBA.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton -Djavax.rmi.CORBA.StubClass=org.openorb.rmi.system.StubDelegateImpl -Djavax.rmi.CORBA.UtilClass=org.openorb.rmi.system.UtilDelegateImpl -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.openorb.rmi.system.PortableRemoteObjectDelegateImpl

REM -----------------------------------------------------------
REM  Classic Assembler w/ new Configuration Factory 
REM -----------------------------------------------------------
REM  The location of an OpenEJB .conf configuration file.
REM 
REM set OPENEJB_OPTION_1= -Dorg/openejb/configuration_source=conf\default.openejb.conf
REM set OPENEJB_OPTION_X= -Dorg/openejb/configuration_factory=org.openejb.alt.config.ConfigurationFactory

REM -----------------------------------------------------------
REM  Classic Assembler w/ XML Configuration Factory 
REM -----------------------------------------------------------
REM  The location of an OpenEJB xml configuration file.
REM 
REM OPENEJB_OPTION_1="-Dorg/openejb/configuration_source=test\conf\openejb-server-config.xml"


REM -----------------------------------------------------------
REM  Thread Context
REM -----------------------------------------------------------
REM  By default the ThreadContext class uses its own class 
REM  definition for instances but this can overriden by binding
REM  this variable to fully qualified class name of a type that 
REM  subclasses ThreadContext. The binding should be added to 
REM  the System Properties.
REM 
REM OPENEJB_OPTION_2="-Dorg/openejb/core/ThreadContext/IMPL_CLASS=org.openejb.sp.tyrex.TyrexThreadContext"
set OPENEJB_OPTION_2= -Dorg/openejb/core/ThreadContext/IMPL_CLASS=org.openejb.tyrex.TyrexThreadContext

REM -----------------------------------------------------------
REM  Logging Configuration
REM -----------------------------------------------------------
REM  Specifies the file to use as Log4j's configuration.
REM 
REM set OPENEJB_OPTION_3= -Dlog4j.configuration=file:conf/default.logging.conf

REM -----------------------------------------------------------
REM  Testing Server helper  -- For Testing Only
REM -----------------------------------------------------------
REM  Used when running the test suite on this server.
REM  The test server class helps the server gather the needed
REM  information about how to connect with this server.
REM 
set OPENEJB_OPTION_4= -Dtest.server.class=org.openejb.test.CorbaTestServer

REM #####################################
REM  startup options      
REM         
REM set OPTIONS=%OPENEJB_OPTION_X% %OPENEJB_OPTION_1% %OPENEJB_OPTION_2% %OPENORB_OPTIONS%
set OPTIONS=%OPENEJB_OPTION_2% %OPENEJB_OPTION_3% %OPENORB_OPTIONS%
REM echo %JAVA% %OPENEJB_OPTION_X% %OPENEJB_OPTION_1% %OPENEJB_OPTION_2% %OPENORB_OPTIONS% -classpath %CP% org.openejb.corba.Server conf\xopenejb_startup.props -ORBProfile=ejb -domain conf\tyrex_resources.xml
REM %JAVA% %OPENEJB_OPTION_X% %OPENEJB_OPTION_1% %OPENEJB_OPTION_2% %OPENORB_OPTIONS% -classpath %CP% org.openejb.corba.Server conf\xopenejb_startup.props -ORBProfile=ejb -domain conf\tyrex_resources.xml
%JAVA% %OPTIONS% -classpath %CP% org.openejb.corba.Server -ORBProfile=ejb -domain conf\tyrex_resources.xml

