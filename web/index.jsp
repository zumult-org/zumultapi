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
        
        <%@include file="WEB-INF/jspf/matomoTracking.jspf" %>       
        
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
       String pageTitle = myResources.getString("PrototypeApplications"); 
    %>

    <body style="background-image: url('./images/doors.jpg');">
        
        <% String pageName = "ZuMult"; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        <div class="row">
            <div class="col-sm-2">
            </div>
            <div class="col-sm-8" style="background:white;padding-top: 20px; border-radius: 15px;">
                
                <div class="card-deck">
                    <div class="card mb-3">
                      <a href="#" class="pop">
                        <img class="card-img-top" src="./images/preselection.png" alt="Card image cap">
                      </a>
                      <div class="card-body">
                            <h5 class="card-title">ZuMal</h5>
                            <p class="card-text">
                                <%=myResources.getString("ZuMalShort")%>                                
                            </p>
                            <!-- <a href="./jsp/zuMal.jsp" class="btn btn-primary" target="_blank">ZuMal</a> -->
                            <a href="prototype/dist/zuMal.jsp" class="btn btn-primary" target="_blank">ZuMal</a>
                      </div>
                    </div>            
                    <div class="card mb-3">
                      <img class="card-img-top" src="./images/query.png" alt="Card image cap">
                      <div class="card-body">
                        <h5 class="card-title">ZuRecht</h5>
                        <p class="card-text"><%=myResources.getString("ZuRechtShort")%></p>
                            <a href="./jsp/zuRecht.jsp?lang=<%=currentLocale.getLanguage()%>" class="btn btn-primary" target="_blank">ZuRecht</a>
                      </div>
                    </div>            
                    <div class="card mb-3">
                      <img class="card-img-top" src="./images/transcript2.png" alt="Card image cap">
                      <div class="card-body">
                        <h5 class="card-title">ZuViel</h5>
                        <p class="card-text"><%=myResources.getString("ZuVielShort")%></p>
                        <a href="./jsp/zuViel.jsp?transcriptID=FOLK_E_00349_SE_01_T_01" class="btn btn-primary" target="_blank">ZuViel</a>
                      </div>
                    </div>            
                    
                </div>
                
                
                <div class="card-deck">
                    <div class="card mb-3">
                      <img class="card-img-top" src="./images/actions.png" alt="Card image cap">
                      <div class="card-body">
                        <h5 class="card-title">ZuHand</h5>
                        <p class="card-text"><%=myResources.getString("ZuHandShort")%></p>
                        <a href="./jsp/zuHand.jsp" class="btn btn-primary" target="_blank">ZuHand</a>
                      </div>
                    </div>                                
                    <div class="card mb-3">
                      <img class="card-img-top" src="./images/crossquantification.png" alt="Card image cap">
                      <div class="card-body">
                        <h5 class="card-title">ZuZweit</h5>
                        <p class="card-text"><%=myResources.getString("ZuZweitShort")%></p>
                        <a href="./jsp/zuZweit.jsp?metaField1=v_e_se_interaktionsdomaene&metaField2=v_s_geschlecht"" class="btn btn-primary" target="_blank">ZuZweit</a>
                      </div>
                    </div>    
                   <div class="card mb-3">
                      <img class="card-img-top" src="./images/corpusoverview.png" alt="Card image cap">
                      <div class="card-body">
                        <h5 class="card-title"><%=myResources.getString("CorpusOverview")%></h5>
                        <p class="card-text"><%=myResources.getString("CorpusOverviewShort")%></p>
                        <a href="./jsp/corpusoverview.jsp" class="btn btn-primary" target="_blank"><%=myResources.getString("CorpusOverview")%></a>
                        </div>
                    </div>         
                </div>
                <!--
                <div class="card-deck">
                    <div class="card mb-3">
                      <img class="card-img-top" src="./images/Rest-API.png" style="width: 400px;" alt="Card image cap">
                      <div class="card-body">
                        <h5 class="card-title">REST API</h5>
                        <p class="card-text"><%=myResources.getString("RestAPIShort")%></p>
                        <a href="api" class="btn btn-primary" target="_blank">Rest API</a>
                      </div>
                    </div>                                
                </div>-->
                
            </div>
            <div class="col-sm-2">
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
