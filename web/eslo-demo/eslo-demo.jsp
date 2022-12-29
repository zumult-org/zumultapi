<%-- 
    Document   : index.jsp
    Created on : 28.01.2020, 14:30:31
    Author     : thomas.schmidt
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
        
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <title>ZuMult - ESLO Demo</title>
        <style type="text/css">
        </style>
        
        <link rel="stylesheet" href="css/overview.css"/>       
        
    </head>
    
    <%
       String pageTitle = myResources.getString("PrototypeApplications"); 
    %>

    <body style="background-image: url('./img/Orleans_bg.jpg'); background-repeat: no-repeat; background-size: cover">
        
        <% String pageName = "ZuMult"; %>
        <div class="row" style="margin-top:50px; margin-bottom:50px;">
            <div class="col-sm-2">
            </div>
            <div class="col-sm-8" style="background:white;padding: 20px; border-radius: 15px;">
                
                <h1 style="text-align:center;">Enquêtes SocioLinguistiques à Orléans (ESLO)</h1>
                <h4 style="color:gray; text-align: center">Demo ZuMult</h2>
                
                
                <div style="background-color:rgb(210,210,210); padding:30px; margin-top:20px; border-radius: 10px;">
                    <h4 style="margin-bottom:20px;">ZuViel: Visualisation de transcriptions</h4>
                    <div class="card-deck">
                        <div class="card" style="width:200px; max-width: 250px;">
                            <img class="card-img-top" src="img/ESLO_ENT_1001.jpg" alt="Card image" width="200px">
                          <div class="card-body">
                            <h4 class="card-title">ESLO_ENT_1001</h4>
                            <p class="card-text">Discussion en face à face entre un chercheur et un locuteur témoin à partir d’une trame d’entretien. Enregistrée le 20/01/2010 par Olivier Baude. </p>
                            <a target="_blank" href="../jsp/zuViel.jsp?transcriptID=ESLO2_ENT_1001_C" class="btn btn-primary">Transcription</a>
                          </div>
                        </div>


                        <div class="card" style="width:200px; max-width: 250px;">
                          <img class="card-img-top" src="img/ESLO_ENT_1005.jpg" alt="Card image">
                          <div class="card-body">
                            <h4 class="card-title">ESLO_ENT_1005</h4>
                            <p class="card-text">Discussion en face à face entre un chercheur et une locutrice témoine à partir d’une trame d’entretien. Enregistrée le 01/03/2010 par Olivier Baude.</p>
                            <a target="_blank" href="../jsp/zuViel.jsp?transcriptID=ESLO2_ENT_1005_C" class="btn btn-primary">Transcription</a>
                          </div>
                        </div>


                        <div class="card" style="width:200px; max-width: 250px;">
                          <img class="card-img-top" src="img/ESLO_ENT_1013.jpg" alt="Card image">
                          <div class="card-body">
                            <h4 class="card-title">ESLO_ENT_1013</h4>
                            <p class="card-text">Discussion en face à face entre une chercheuse et un locuteur témoin à partir d’une trame d’entretien. Enregistrée le 01/03/2010 par Pauline Philardeau.</p>
                            <a target="_blank" href="../jsp/zuViel.jsp?transcriptID=ESLO2_ENT_1013_C" class="btn btn-primary">Transcription</a>
                          </div>
                        </div>
                    </div>
                </div>
                
                <!-- ******************************  -->
                <!-- ******************************  -->
                <!-- ******************************  -->
                <!-- ******************************  -->
                <!-- ******************************  -->
                
                <div style="background-color:rgb(210,210,210); padding:30px; margin-top:20px; border-radius: 10px;">
                    <h4 style="margin-bottom:20px;">ZuRecht: Recherche CQP</h4>
                    <div class="card-deck">
                        <div class="card">
                            <div class="card-body">
                                <form id="kwic-search-form" autocomplete="off">
                                    <p>Saisissez une expression de recherche CQP</p>
                                    <div class="input-group mb-3">

                                        <!-- input group prepend with search mode options -->
                                        <div class="input-group-prepend searchTypeSelect">
                                            <button type="button" class="btn btn-success border-success btn-sm dropdown-toggle" data-toggle="dropdown"> 
                                                <span class="currentVal">Mode recherche</span>
                                            </button>
                                            <ul class="dropdown-menu" role="menu">
                                                <li>
                                                    <a class="dropdown-item" data-text="Recherche par locuteur individuel" data-value="speaker-based" href="#">
                                                        <span><i class="fa fa-user"></i></span>
                                                        <span>Recherche par locuteur individuel</span>
                                                    </a>
                                                </li>
                                                <li>
                                                    <a class="dropdown-item" data-text="Recherche dans la transcription entière" data-value="transcript-based" href="#">
                                                        <span><i class="fa fa-copy"></i></span>
                                                        <span>Recherche dans la transcription entière</span>
                                                    </a>
                                                </li>
                                            </ul>
                                        </div>

                                        <!-- cqp input field -->
                                        <input type="text" id="queryInputField" class="form-control form-control-sm border-success inputFieldWithAutocomplete" required="required" 
                                               placeholder="Expression CQP, p.ex. [norm=&quot;je&quot;][norm=&quot;cherche&quot;]"/>


                                        <!-- input group append with buttons for search, search options and search help -->
                                        <div class="input-group-append">
                                            <button type="submit" class="btn btn-success border-success btn-sm mb-1" title="SearchBtn">
                                                <span><i class="fa fa-search"></i></span>
                                            </button>
                                            <button type="button" class="btn border-success btn-sm px-3 mb-1 btn-open-search-options" title="OpenPanelWithSearchOptions">
                                                <span><i class="fa fa-ellipsis-v"></i></span>
                                            </button>
                                            <button type="button" class="btn border-success btn-sm px-3 mb-1 btn-open-help" title="Help">
                                                <span><i class="fa fa-question"></i></span>
                                            </button>
                                        </div>                            
                                    </div>
                                </form>



                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-sm-2">
            </div>
        </div>
            

    </body>
</html>
