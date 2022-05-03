<%-- 
    Document   : queryActionAnno
    Created on : 30.09.2020, 11:34:14
    Author     : Elena
--%>

<%@ page import = "java.io.*,java.util.*" %>

<html>
   <head>
      <title>Page Redirection to ZuHand</title>
      <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
   </head>
   
   <body>
      <%
         response.setStatus(response.SC_MOVED_TEMPORARILY);
         response.setHeader("Location", new String("zuHand.jsp"));
      %>
   </body>
</html>
