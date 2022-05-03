<%-- 
    Document   : preselection
    Created on : 27.04.2020, 23:19:43
    Author     : thomas.schmidt
--%>

<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="../../WEB-INF/jspf/locale.jspf" %>     
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width,initial-scale=1,shrink-to-fit=no">
        
        
        <!-- import js and css for the vue.js application -->
        <link href="css/app.678b3852.css" rel="stylesheet">        
        <link href="css/chunk-vendors.f359bde9.css" rel="stylesheet">
        <link href="js/app.be69f0de.js" rel="preload" as="script">
        <link href="js/chunk-vendors.c9629119.js" rel="preload" as="script">
        <link href="css/chunk-vendors.f359bde9.css" rel="stylesheet">
        <link href="css/app.678b3852.css" rel="stylesheet">        
        
        
        
        <!-- import bootstrap CSS and related things -->
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        
        <!-- import ZuMult CSS -->
        <link rel="stylesheet" href="../../css/overview.css"/>       
        
        
        <title>ZuMal: Speech event selection</title>

        <!-- include the code for Matomo tracking -->
        <%@include file="../../WEB-INF/jspf/matomoTracking.jspf" %>                
        
    </head>
    <body>
        <% String pageName = "ZuMal"; %>
        <% String pageTitle = "Auswahl von Daten aus FOLK und GWSS"; %>
        <!-- include the menu bar -->
        <%@include file="../../WEB-INF/jspf/zumultNav.jspf" %>                                                
        
       <noscript><strong>We're sorry but vue-d3-prototype doesn't work properly without JavaScript enabled. Please enable
            it to continue.</strong>
       </noscript>
       
       <!-- this is where the app goes -->
       <div class="row" id="app"></div>
        
       <!-- import those again! no idea why -->
       <script src="js/chunk-vendors.c9629119.js"></script>
       <script src="js/app.be69f0de.js"></script>
        


    </body>
</html>

