<%@ page import="
javax.naming.InitialContext,
javax.naming.Context,
javax.naming.*,
java.util.Properties,
javax.naming.Context,
javax.naming.InitialContext,
javax.servlet.ServletConfig,
javax.servlet.ServletException,
javax.servlet.http.HttpServlet,
javax.servlet.http.HttpServletRequest,
javax.servlet.http.HttpServletResponse,
javax.servlet.jsp.JspWriter,
java.io.PrintWriter,
java.util.*,
java.io.*,
java.lang.reflect.Method,
java.lang.reflect.InvocationTargetException,
java.lang.reflect.Modifier
"%>
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>OpenEJB Tomcat Integration/1.0</title>
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
            <td bgcolor="#5A5CB8" align="left" valign="top" height="2" width="530"><img border="0" height="6" width="530" src="images/top_2.gif"></td>
            <td bgcolor="#E24717" align="left" valign="top" height="2" width="120"><img src="images/top_3.gif" width="120" height="6" border="0"></td>
        </tr>
        <tr>
            <td bgcolor="#5A5CB8" align="left" valign="top" bgcolor="#ffffff" width="13"><img border="0" height="15" width="13" src="images/dotTrans.gif"></td>
            <td align="left" valign="top" width="40"><img border="0" height="1" width="1" src="images/dotTrans.gif"></td>
            <td align="left" valign="middle" width="530"><a href="http://www.openejb.org"><span class="menuTopOff">OpenEJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="index.html"><span class="menuTopOff">Index</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewjndi.jsp"><span class="menuTopOff">JNDI</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewejb.jsp"><span class="menuTopOff">EJB</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="viewclass.jsp"><span class="menuTopOff">Class</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"><a href="invokeobj.jsp"><span class="menuTopOff">Invoke</span></a><img border="0" height="2" width="20" src="images/dotTrans.gif"></td>
            <td align="left" valign="top" height="20" width="120"><img border="0" height="2" width="10" src="images/dotTrans.gif"></td>
        </tr>
        <tr>
            <td align="left" valign="top" bgcolor="#a9a5de" width="7"><img border="0" height="3" width="7" src="images/line_sm.gif"></td>
            <td align="left" valign="top" height="3" width="40"><img border="0" height="3" width="40" src="images/line_light.gif"></td>
            <td align="left" valign="top" height="3" width="530"><img border="0" height="3" width="530" src="images/line_light.gif"></td>
            <td align="left" valign="top" height="3" width="120"><img height="1" width="1" border="0" src="images/dotTrans.gif"></td>
        </tr>
        <tr>
            <td align="left" valign="top" bgcolor="#a9a5de" width="7">&nbsp;</td>
            <td align="left" valign="top" width="40">&nbsp;</td>
            <td valign="top" width="530" rowspan="4">
                <table width="530" cellspacing="0" cellpadding="0" border="0" rows="2" cols="1">
                    <tr>
                        <td align="left" valign="top"><br>
                            <img width="200" vspace="0" src="./images/logo_ejb2.gif" hspace="0" height="55" border="0">
                            <br>
                            <img src="images/dotTrans.gif" hspace="0" height="7" border="0"><br>
                            <span class="pageTitle">
                            Testing an Enterprise JavaBean
                            </span>
                            <br>
                            <img src="images/dotTrans.gif" hspace="0" height="1" border="0"></td>
                    </tr>
                </table>
                <p>
                </p>
                <FONT SIZE="2">
<%
    try{
        synchronized (this) {
            main(request, session, out);
        }
    } catch (Exception e){
        out.println("FAIL");
        //throw e;
        return;
    }
%>
<BR><BR>
<BR>
</FONT>

            </td>
            <td align="left" valign="top" height="5" width="120">


                &nbsp;</td>
        </tr>
    </table>
    </body>
</html>

<%!
    String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";
    
    static String invLock = "lock";
    static int invCount;

    HttpSession session;
    HttpServletRequest request;
    JspWriter out;
    
    String OK   = "<td><font size='2' color='green'><b>OK</b></font></td></tr>";
    String FAIL = "<td><font size='2' color='red'><b>FAIL</b></font></td></tr>";
    String HR = "<img border='0' height='3' width='340' src='images/line_light.gif'><br>";
    String pepperImg = "<img src='images/pepper.gif' border='0'>";
    
    /**
     * The main method of this JSP
     */ 
    public void main(HttpServletRequest request, HttpSession session, JspWriter out) throws Exception{
        this.request = request;
        this.session = session;
        this.out = out;

        InitialContext ctx = null;
        ClassLoader myLoader = null;
        try{
            myLoader = this.getClass().getClassLoader();
            Properties p = new Properties();

            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.LocalInitialContextFactory");

            ctx = new InitialContext( p );
            
        } catch (Exception e){
            formatThrowable(e);
        }
        
        try{
            out.print(HR);
            out.print("<table width='350' cellspacing='4' cellpadding='4' border='0'>");
            

            // ---------------------------------------------------
            //  Can I lookup a home interface from the testsuite?
            // ---------------------------------------------------

            printTest("Looking up an ejb home  ");

            Object ejbHome = null;
            try{
                ejbHome = ctx.lookup( "client/tests/stateless/BasicStatelessHome" );
                if (ejbHome instanceof java.rmi.Remote) out.println(OK);
            } catch (Exception e){
                out.println(FAIL);
                return;
            }

            // ---------------------------------------------------
            //  Is the home interface visible?
            // ---------------------------------------------------

            printTest("Checking for the home interface class definition ");

            Class homeInterface;
            try{
                homeInterface = Class.forName("org.openejb.test.stateless.BasicStatelessHome",true, myLoader);
                out.println(OK);
            } catch (Exception e){
                out.println(FAIL);
                return;
            }

            // ---------------------------------------------------
            //  Can I invoke a create method on the ejb home?
            // ---------------------------------------------------

            printTest("Invoking the create method on the ejb home  ");

            Object ejbObject = null;
            try{
                Class[] params = new Class[0];
                Method create = null;
                create = homeInterface.getDeclaredMethod("create", params);
                ejbObject = create.invoke(ejbHome, new Object[0]);

                if (ejbObject instanceof java.rmi.Remote) out.println(OK);

            } catch (Exception e){
                out.println(FAIL);
                return;
            }

            // ---------------------------------------------------
            //  Is the remote interface visible?
            // ---------------------------------------------------

            printTest("Checking for the remote interface class definition ");

            Class remoteInterface;
            try{
                remoteInterface = Class.forName("org.openejb.test.stateless.BasicStatelessObject",true, myLoader);
                out.println(OK);
            } catch (Exception e){
                out.println(FAIL);
                return;
            }

            // ---------------------------------------------------
            //  Can I invoke a business method on the ejb object?
            // ---------------------------------------------------

            printTest("Invoking a business method on the ejb object ");

            Object returnValue = null;
            try{
                String message = "!snoitalutargnoC ,skroW gnihtyrevE";
                Class[] params = new Class[]{String.class};
                Method businessMethod = null;
                businessMethod = remoteInterface.getDeclaredMethod("businessMethod", params);
                returnValue = businessMethod.invoke(ejbObject, new Object[]{message});

                if (returnValue instanceof java.lang.String) out.println(OK);

            } catch (Exception e){
                out.println(FAIL);
                return;
            }
            out.print("</table>");
            out.print(HR);
            
            out.println("<br>The Enterprise Bean returned the following message:<br><br>");

            out.println("<b>"+returnValue+"</b>");



        } catch (Exception e){
            out.print(FAIL);
            out.print("</table>");
            out.print(HR);

            out.print(e.getMessage());
        }
    }

    protected void printTest(String test) throws IOException{
        out.print("<tr><td><font size='2'>"  );
        out.print(test);
        out.print("</font></td>");
    }

    public String formatThrowable(Throwable err) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        err.printStackTrace(new PrintStream(baos));
        byte[] bytes = baos.toByteArray();
        StringBuffer sb = new StringBuffer(bytes.length);
        for (int i=0; i < bytes.length; i++){
            char c = (char)bytes[i];
            switch (c) {
            case ' ': sb.append("&nbsp;"); break;
            case '\n': sb.append("<br>"); break;
            case '\r': break;
            default: sb.append(c);
            }
        }
        return sb.toString();
    }
%>

