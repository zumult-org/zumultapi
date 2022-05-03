<%-- 
    Document   : testvideo
    Created on : 16.05.2018, 11:10:43
    Author     : Thomas_Schmidt
--%>

<%@page import="org.zumult.objects.Media"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%             
    response.setHeader("Access-Control-Allow-Origin", "*");
 %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Media someVideo = backendInterface.getMedia("BETV_E_00001_SE_01_V_01");
            String videoURL = someVideo.getURL();
        %>
        <h1>Hello World!</h1>
        <video id="videoplayer" width="720" height="405" controls="controls" autoplay="">
            <source src="<%=videoURL%>" type="video/mp4">
        </video>        
    </body>
</html>
