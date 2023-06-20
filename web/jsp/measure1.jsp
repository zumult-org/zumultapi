<%-- 
    Document   : measure1
    Created on : 12.11.2018, 14:20:31
    Author     : Thomas_Schmidt
--%>

<%@page import="org.zumult.backend.implementations.DGD2Oracle"%>
<%@page import="java.util.Scanner"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.io.Constants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Measure 1: Intersection with reference word lists</title>
        <style type="text/css">
            td {
                text-align: right;
                margin-left:10px;
                border: 1px solid gray; 
            }
            th {
                margin-left:10px;
                padding-right: 20px;
                border: 1px solid gray; 
            }
        </style>        
    </head>
    <body>
        <h1>Measure 1: Intersection with reference word lists</h1>
        <%
            String corpusID = request.getParameter("corpusID");
            String pathToInternalResource = "/data/Measure_1_" + corpusID + ".xml";
            Scanner scanner = new Scanner(DGD2Oracle.class.getResourceAsStream(pathToInternalResource), "UTF-8");
            String measureXML = scanner.useDelimiter("\\A").next();
            scanner.close();
            String html = new IOHelper().applyInternalStylesheetToString(
                                        Constants.MEASURE2HTML_STYLESHEET, 
                                        measureXML);

        %>
        <div>
            <%= html %>
        </div>
    </body>
</html>
