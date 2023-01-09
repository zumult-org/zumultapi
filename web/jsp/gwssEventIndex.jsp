<%-- 
    Document   : folkEventIndex
    Created on : 01.01.2020, 12:49:51
    Author     : thomas.schmidt
--%>

<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<% 
    BackendInterface backend = BackendInterfaceFactory.newBackendInterface(); 
    String html = new IOHelper().applyInternalStylesheetToInternalFile("/org/zumult/io/gwssEventOverview.xsl", "/data/GWSSEventIndex.xml");
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="refresh" content="0; url=https://zumult.ids-mannheim.de/ProtoZumult/jsp/speecheventsOverview.jsp?corpusID=GWSS" />
        
        <title>GeWiss: Gesprochene Wissenschaftssprache Kontrastiv</title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <script src="../js/metadata.js"></script>          
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
    </head>
    <body>
        <h1>GeWiss: Gesprochene Wissenschaftssprache Kontrastiv</h1>
        <%= html %>

        <!-- ************************************** -->
        <!-- ************************************** -->
        <!-- ************************************** -->

        <%@include file="../WEB-INF/jspf/metadataModal.jspf" %>                                                

    </body>
</html>
