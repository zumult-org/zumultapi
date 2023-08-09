<%-- 
    Document   : crossquantification
    Created on : 04.05.2020, 18:59:06
    Author     : Thomas.Schmidt
    Changed on : 09.08.2023 by Elena Frick
--%>

<html>
   <head>
      <title>Page Redirection to ZuZweit</title>              
   </head>
   
   <body>
      <%
         response.setStatus(response.SC_MOVED_PERMANENTLY);
         response.setHeader("Location", new String("zuZweit.jsp"));
      %>
   </body>
</html>