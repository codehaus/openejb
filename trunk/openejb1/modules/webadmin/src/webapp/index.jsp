<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB Integration/1.0</title>
    <link href="default.css" rel="stylesheet">
    <!-- $Id$ -->
    <!-- Author: David Blevins (david.blevins@visi.com) -->
</head>
    <body marginwidth="0" marginheight="0" leftmargin="0" bottommargin="0" topmargin="0" vlink="#6763a9" link="#6763a9" bgcolor="#ffffff">
    <a name="top"></a>
    <table width="712" cellspacing="0" cellpadding="0" border="0">
        <tr>
            <td bgcolor="#5A5CB8" align="left" valign="top" width="7"><img height="9" width="1" border="0" src="images/dotTrans.gif"></td>
            <td bgcolor="#5A5CB8" align="left" valign="top" width="40"><img border="0" height="6" width="40" src="images/dotTrans.gif"></td>
            <td bgcolor="#5A5CB8" align="left" valign="top" height="2" width="430"><img border="0" height="6" width="430" src="images/top_2.gif"></td>
            <td bgcolor="#E24717" align="left" valign="top" height="2" width="120"><img src="images/top_3.gif" width="120" height="6" border="0"></td>
        </tr>
        <tr>
            <td bgcolor="#5A5CB8" align="left" valign="top" bgcolor="#ffffff" width="13"><img border="0" height="15" width="13" src="images/dotTrans.gif"></td>
            <td align="left" valign="top" width="40"><img border="0" height="1" width="1" src="images/dotTrans.gif"></td>
            <td align="left" valign="middle" width="430"><a href="http://openejb.sourceforge.net"><span class="menuTopOff">OpenEJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="index.html"><span class="menuTopOff">Index</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewjndi.jsp"><span class="menuTopOff">JNDI</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewejb.jsp"><span class="menuTopOff">EJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewclass.jsp"><span class="menuTopOff">Class</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="invokeobj.jsp"><span class="menuTopOff">Invoke</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"></td>
            <td align="left" valign="top" height="20" width="120"><img border="0" height="2" width="10" src="images/dotTrans.gif"></td>
        </tr>
        <tr>
            <td align="left" valign="top" bgcolor="#a9a5de" width="7"><img border="0" height="3" width="7" src="images/line_sm.gif"></td>
            <td align="left" valign="top" height="3" width="40"><img border="0" height="3" width="40" src="images/line_light.gif"></td>
            <td align="left" valign="top" height="3" width="430"><img border="0" height="3" width="430" src="images/line_light.gif"></td>
            <td align="left" valign="top" height="3" width="120"><img height="1" width="1" border="0" src="images/dotTrans.gif"></td>
        </tr>
        <tr>
            <td align="left" valign="top" bgcolor="#a9a5de" width="7">&nbsp;</td>
            <td align="left" valign="top" width="40">&nbsp;</td>
            <td valign="top" width="430" rowspan="4">
                <table width="430" cellspacing="0" cellpadding="0" border="0" rows="2" cols="1">
                    <tr>
                        <td align="left" valign="top"><br>
                            <img width="200" vspace="0" src="./images/logo_ejb2.gif" hspace="0" height="55" border="0">
                            <br>
                            <img src="images/dotTrans.gif" hspace="0" height="7" border="0"><br>
                            <span class="pageTitle">
                            OpenEJB and Tomcat Integration Page
                            </span>
                            <br>
                            <img src="images/dotTrans.gif" hspace="0" height="1" border="0"></td>
                    </tr>
                </table>
                <p>
                <FONT SIZE='2'>
                <B>Welcome to the OpenEJB/Tomcat integration page!</B><br><BR>
<% 
   String openejbHome = org.openejb.OpenEJB.getProperty("openejb.home"); 
   String openejbBase = org.openejb.OpenEJB.getProperty("openejb.base"); 
   String openejbConf = org.openejb.OpenEJB.getProperty("openejb.configuration");
   String localCopy = org.openejb.OpenEJB.getProperty("openejb.localcopy");
   
   boolean openejbHomeSet = (openejbHome != null);
   boolean openejbBaseSet = (openejbBase != null);
   boolean openejbConfSet = (openejbConf != null);
   boolean localCopySet = (localCopy != null);
   
   final String EMPTY = "&lt;empty&gt;";
%>
<% 
   if (!openejbHomeSet) {
%>
                It seems you haven't yet set the <B>openejb.home</B> init-param in the 
                web.xml of this application.  Setting the openejb.home correctly is the most
                important thing to do.  In fact, it is the only thing you have to do.
                <BR><BR>
                Please set the variable, restart Tomcat instance and the message will not show up again!
<%
   } else {
%>
                Important OpenEJB properties:
                <UL>
                   <LI>openejb.home: <%= openejbHome %></LI>
                   <LI>openejb.base: <%= (openejbBaseSet ? openejbBase : EMPTY) %></LI>
                   <LI>openejb.configuration: <%= (openejbConfSet ? openejbConf : EMPTY) %></LI>
                   <LI>openejb.localcopy: <%= (localCopySet ? localCopy : Boolean.FALSE.toString()) %></LI>
                </UL>
                If you think you have the variables set correctly, click on 
                the "Testing your setup" link below to verify it.  When everything
                is setup well, feel free to play around with the tools provided below!
                <BR><BR>
                <B>Setup</B><BR>
                <A HREF="testhome.jsp">Testing your setup</A><BR>
                <BR>
                <B>Tools</B><BR>
                <A HREF="viewjndi.jsp">OpenEJB JNDI Browser</A><BR>
                <A HREF="viewclass.jsp">OpenEJB Class Viewer</A><BR>
                <A HREF="viewejb.jsp">OpenEJB EJB Viewer</A><BR>
                <A HREF="invokeobj.jsp">OpenEJB Object Invoker</A><BR>
                <BR>
                <B>FAQs</B><BR>
                <A HREF="howitworks.html">How does the integration work</A><BR>
                <A HREF="ejbclasses.html">Where to put your bean classes</A><BR>
                <A HREF="ejbref.html">How to configure java:comp/env lookups</A><BR>
                <BR>
<%
   }
%>
                </FONT>
                </p>
                <p>
                </p>
                <br>
                <br>
            </td>
            <td align="left" valign="top" height="5" width="120">


                &nbsp;</td>
        </tr>
    </table>
    </body>
</html>