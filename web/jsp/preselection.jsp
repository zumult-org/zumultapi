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

      <%
            RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/jsp/zuMal.jsp");
            dispatcher.forward(request, response);            
      %>

    <%@include file="../WEB-INF/jspf/locale.jspf" %>     

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        
        <link href="../prototype/dist/css/app.448cc2a9.css" rel="preload" as="style">
        <link href="../prototype/dist/css/chunk-vendors.f359bde9.css" rel="preload" as="style">
        <link href="../prototype/dist/js/app.311eaed1.js" rel="preload" as="script">
        <link href="../prototype/dist/js/chunk-vendors.c9629119.js" rel="preload" as="script">
        <link href="../prototype/dist/css/chunk-vendors.f359bde9.css" rel="stylesheet">
        <link href="../prototype/dist/css/app.448cc2a9.css" rel="stylesheet">

        <!-- <link href="../prototype/dist/css/app.edcbf7fa.css" rel="preload" as="style">
        <link href="../prototype/dist/css/chunk-vendors.f359bde9.css" rel="preload" as="style">
        <link href="../prototype/dist/js/app.e6452434.js" rel="preload" as="script">
        <link href="../prototype/dist/js/chunk-vendors.29f4ac23.js" rel="preload" as="script">
        <link href="../prototype/dist/css/chunk-vendors.f359bde9.css" rel="stylesheet">
        <link href="../prototype/dist/css/app.edcbf7fa.css" rel="stylesheet"> -->

        <!-- <link href="../prototype/dist/css/app.99fdae38.css" rel="preload" as="style"/>
        <link href="../prototype/dist/css/chunk-vendors.be97c089.css" rel="preload" as="style"/>
        <link href="../prototype/dist/js/app.3abd40c5.js" rel="preload" as="script"/>
        <link href="../prototype/dist/js/chunk-vendors.c5f4fd43.js" rel="preload" as="script"/>
        <link href="../prototype/dist/css/chunk-vendors.be97c089.css" rel="stylesheet"/>
        <link href="../prototype/dist/css/app.99fdae38.css" rel="stylesheet"/> -->
        
        
        
        
        <!-- <link href="../prototype/dist/css/app.d69d9cf0.css" rel="preload" as="style">
        <link href="../prototype/dist/css/chunk-vendors.f7fe4e03.css" rel="preload" as="style">
        <link href="../prototype/dist/js/app.3877f00e.js" rel="preload" as="script">
        <link href="../prototype/dist/js/chunk-vendors.b31a022c.js" rel="preload" as="script">
        <link href="../prototype/dist/css/chunk-vendors.f7fe4e03.css" rel="stylesheet">
        <link href="../prototype/dist/css/app.d69d9cf0.css" rel="stylesheet"> -->
        
        
        
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <link rel="stylesheet" href="../css/overview.css"/>       
        
        
        <title>ZuMal: Speech event selection</title>
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
        
    </head>
    <body>
        <% String pageName = "ZuMal"; %>
        <% String pageTitle = "Auswahl von Daten aus FOLK und GWSS"; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        
       <noscript><strong>We're sorry but vue-d3-prototype doesn't work properly without JavaScript enabled. Please enable
            it to continue.</strong>
       </noscript>
        <div class="row" id="app"></div>
        
        <!-- <script src="../prototype/dist/js/chunk-vendors.29f4ac23.js"></script>
        <script src="../prototype/dist/js/app.e6452434.js"></script> -->
        
        <script src="../prototype/dist/js/chunk-vendors.c9629119.js"></script>
        <script src="../prototype/dist/js/app.311eaed1.js"></script>
        


    </body>
</html>

