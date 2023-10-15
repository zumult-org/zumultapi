<%-- 
    Document   : index
    Created on : 15.10.2023, 12:17:11
    Author     : bernd
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>


<!DOCTYPE html>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">        
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
        <link rel="stylesheet" href="css/query.css">
        
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <script src="js/tgdp.js"></script>
        <title>Texas German Dialect Project: ZuMult Start</title>
        <style type="text/css">
        </style>
        
        <!-- <link rel="stylesheet" href="css/overview.css"/> -->
        
    </head>
    
    <%
       String pageTitle = myResources.getString("PrototypeApplications"); 
    %>

    <body style="background-image: url('./img/desert.jpg'); background-repeat: no-repeat; background-size: cover">
        
        <% String pageName = "ZuMult"; %>
        <div class="row" style="margin-top:50px; margin-bottom:50px;">
            <div class="col-sm-2">
            </div>
            <div class="col-sm-8" style="background:white;padding: 20px; border-radius: 15px;">
                
                <h1 style="text-align:center;">Texas German Dialect Project (TGDP)</h1>
                <h4 style="color:gray; text-align: center">ZuMult Apps</h2>
                
                
                <div style="background-color:rgb(210,210,210); padding:30px; margin-top:20px; border-radius: 10px;">
                    <div class="card-deck">
                        <div class="card" style="width:350px; max-width: 500px;">
                            <img class="card-img-top" src="img/browse_preview.png" alt="Card image" width="350px" style="max-width:500px; padding: 10px;">
                          <div class="card-body">
                            <h4 class="card-title">Browse</h4>
                            <p class="card-text">
                                Browse interview recordings with their transcripts and metadata.
                            </p>
                            <a target="_blank" href="./browse.jsp" class="btn btn-primary">Go to Browsing page</a>
                          </div>
                        </div>


                        <div class="card" style="width:350px; max-width: 500px;">
                          <img class="card-img-top" src="img/query_preview.png" alt="Card image" width="350px" style="max-width:500px; padding: 10px;">
                          <div class="card-body">
                            <h4 class="card-title">Query</h4>
                            <p class="card-text">
                                Query transcripts with their annotations and metadata using CQP.
                            </p>
                            <a target="_blank" href="./query.jsp" class="btn btn-primary">Go to Query Page</a>
                          </div>
                        </div>

                    </div>
                </div>
                

    </body>
</html>
