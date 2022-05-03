<%-- 
    Document   : debug
    Created on : 19.02.2021, 08:39:58
    Author     : thomas.schmidt
--%>

<%@page import="org.zumult.io.IOHelper"%>
<%@page import="java.io.File"%>
<%@page import="org.zumult.backend.implementations.AbstractIDSBackend"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>
            <%
                String actualPath = AbstractIDSBackend.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                File folder = new File(IOHelper.getProjectFile(actualPath), "WEB-INF\\classes\\data");                
            %>
            
            <p>
                String actualPath = AbstractIDSBackend.class.getProtectionDomain().getCodeSource().getLocation().getPath();<br/>
                File folder = new File(IOHelper.getProjectFile(actualPath), "WEB-INF\\classes\\data");                
            </p>
            <p>Folder.getAbsolutePath()=
                <b><%= folder.getAbsolutePath() %></b></p>
        </h1>
    </body>
</html>
