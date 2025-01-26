<%-- 
    Document   : index.jsp
    Created on : 28.01.2020, 14:30:31
    Author     : thomas.schmidt
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.util.Locale"%>
<%@page import="java.util.Random"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>




<!DOCTYPE html>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">        
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <title>ZuMult - <%=myResources.getString("PrototypeApplications")%></title>
        <style type="text/css">
            .wrapper {
                display: flex;
                width: 100%;
                align-items: stretch;
            }           
            #sidebar {
                min-width: 250px;
                max-width: 250px;

            }            
        </style>
        
        <link rel="stylesheet" href="css/overview.css"/>       
        
        
        <script>
            $(document).ready(function(){
        
                $("#selectLang").on("change", function(){
                    var value = $(this).val();
                    var urlTest = new URL(window.location.href);
                    urlTest.searchParams.set('lang',value);
                    window.location = urlTest;
                });
            });
                        
        </script>
        
    </head>
    
    <%
       String pageTitle = myResources.getString("testApp"); 
       BackendInterface bi = BackendInterfaceFactory.newBackendInterface();
       Random random = new Random();
       IDList allCorpusIDs = bi.getCorpora();
       String randomCorpusID = allCorpusIDs.get(random.nextInt(allCorpusIDs.size()));
       IDList allTranscriptIDs =  bi.getTranscripts4Corpus(randomCorpusID);
       String randomTranscriptID = allTranscriptIDs.get(random.nextInt(allTranscriptIDs.size()));
    %>

    <body style="background-image: url('./images/talking.png');height: 1080px;
            /* Center and scale the image nicely */
            background-position: center;
            background-repeat: no-repeat;
            background-size: cover;">
        
        <% String pageName = "ZuMult"; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        <div class="container">
            <div class="row">
                <div class="col-1"></div>
                <div class="col-10" style="background:white;padding-top: 20px; border-radius: 15px; min-width: 1920px;">
                    <div class="container">
                        <div class="row">
                            <div class="m-1 text-center">
                                This ZuMult instance has <%= allCorpusIDs.size() %> corpora: <%= String.join(" / ", allCorpusIDs) %>
                                <a href="./jsp/corpusoverview.jsp?lang=<%=currentLocale.getLanguage()%>" class="btn btn-outline-info ml-3" target="_blank">Corpus Overview</a>                    
                            </div>                        
                        </div>
                    </div>

                    <div class="container">
                        <div class="row">
                            <div class="col-12">
                                <div class="card-deck">
                                    <div class="card mb-3" style="min-width:800px;">
                                        <img class="card-img-top" src="./images/query.png" alt="Card image cap">
                                      <div class="card-body">
                                        <h5 class="card-title">ZuRecht</h5>
                                        <p class="card-text"><%=myResources.getString("ZuRechtShort")%></p>
                                            <a href="./jsp/zuRecht.jsp?lang=<%=currentLocale.getLanguage()%>" class="btn btn-primary" target="_blank">ZuRecht</a>
                                      </div>
                                    </div>            
                                    <div class="card mb-3" style="min-width:800px;">
                                      <img class="card-img-top" src="./images/transcript2.png" alt="Card image cap">
                                      <div class="card-body">
                                        <h5 class="card-title">ZuViel / ZuPass / ZuAnn</h5>
                                        <p class="card-text"><%=myResources.getString("ZuVielShort")%></p>
                                        <a href="./jsp/zuViel.jsp?transcriptID=<%= randomTranscriptID %>" class="btn btn-primary" target="_blank">ZuViel</a>
                                        <a href="./jsp/zuPass.jsp?transcriptID=<%= randomTranscriptID %>" class="btn btn-primary" target="_blank">ZuPass</a>
                                        <a href="./jsp/zuAnn.jsp?transcriptID=<%= randomTranscriptID %>" class="btn btn-primary" target="_blank">ZuAnn</a>
                                        <small class="text-muted ml-2"><%= randomCorpusID %> / <%= randomTranscriptID %> </small>
                                      </div>
                                    </div>                      
                                </div>
                            </div>
                        </div>
                    </div>


                <div class="col-1"></div>
            </div>
        </div>
            

        <!-- Creates the bootstrap modal where the image will appear -->
        <div class="modal fade" id="imagemodal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
          <div class="modal-dialog modal-lg">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="myModalLabel">Image preview</h4>
              </div>
              <div class="modal-body">
                <img src="" id="imagepreview" style="width: 400px; height: 264px;" >
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>
          </div>
        </div>

    <script>    
        $(function() {
            $('.pop').on('click', function() {
                $('#imagepreview').attr('src', $(this).find('img').attr('src'));
                $('#imagemodal').modal('show');   
            });     
        });        
    </script>
    </body>
</html>