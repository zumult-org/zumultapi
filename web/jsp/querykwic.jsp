<%-- 
    Document   : querykwic
    Created on : 10.01.2020, 14:07:25
    Author     : Elena_Frick
--%>

<%@ page import = "java.io.*,java.util.*" %>

<html>
   <head>
      <title>Page Redirection to ZuRecht</title>              
   </head>
   
   <body>
      <%
         response.setStatus(response.SC_MOVED_TEMPORARILY);
         response.setHeader("Location", new String("zuRecht.jsp"));
      %>
   </body>
</html>