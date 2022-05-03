<%-- 
    Document   : TestLogin
    Created on : 04.05.2020, 21:42:38
    Author     : Thomas.Schmidt
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
        <%
            String sessionID = (String) request.getSession().getAttribute("oracle_session_id");
            String tomcatSessionID = request.getSession().getId();
            
        %>
        <%= sessionID %> / <%= tomcatSessionID %>
    </body>
</html>
