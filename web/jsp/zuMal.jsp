<%-- 
    Document   : preselection
    Created on : 27.04.2020, 23:19:43
    Author     : thomas.schmidt
--%>

<%@page import="org.zumult.backend.Configuration"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     

      <%
            //RequestDispatcher dispatcher = getServletContext()
            //    .getRequestDispatcher("/prototype/dist/zuMal.jsp");
            //dispatcher.forward(request, response);            
      %>

<html>
    <head>
        <meta http-equiv="refresh" content="0; url=<%= Configuration.getWebAppBaseURL()%>/prototype/dist/zuMal.jsp" />
    </head>
    <body>
        <!-- nothing -->
    </body>
</html>

