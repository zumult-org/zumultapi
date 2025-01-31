<%-- 
    Document   : corpusoverview
    Created on : 01.05.2018, 12:13:30
    Author     : Thomas_Schmidt
--%>

<%@page import="java.io.File"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.zumult.objects.Corpus"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     
<html>
    <%
       BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
       String backendName = backendInterface.getName();
       String backendAcronym = backendInterface.getAcronym();
       String language = request.getParameter("lang");
       if (language==null){
           language="de";
       }
       IDList corpora = backendInterface.getCorpora();       
    %>
    <head>  
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuMult: Corpus Overview</title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                

        <link rel="stylesheet" href="../css/overview.css"/>       
        
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
    <body>
        <% String pageTitle = "Korpusübersicht"; 
           if ("en".equals(language)) {
               pageTitle = "Corpus overview";
           }
           String pageName = "ZuMult";
        %>
            
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        <div class="row">
            <div class="col-sm-2">
            </div>
            <div class="col-sm-8">
                <p class="text-justify">
                <% if ("en".equals(language)) { %>
                    This page gives an overview of the <%= corpora.size() %> corpora available in this 
                    <a href="http://zumult.org" target="_blank">ZuMult</a> instance.
                <% } else { %>
                    Diese Seite gibt einen Überblick über die <%= corpora.size() %> Korpora, die in dieser
                    <a href="http://zumult.org" target="_blank">ZuMult</a>-Instanz
                    zugänglich sind. 
                <% } %>
                </p>
            </div>
            <div class="col-sm-2">
            </div>
        </div>
        <div class="row">
            <div class="col-sm-2">
            </div>
            <div class="col-sm-8">
        
        <%
            for (String corpusID : corpora){
                Corpus corpus = backendInterface.getCorpus(corpusID); 
                String acronym = corpus.getAcronym();
                String name = corpus.getName(language);
                String description = corpus.getDescription(language);
        %>
                <div class="card mb-3" >
                  <div class="row no-gutters">
                    <div class="col-md-2">
                        <figure class="figure">
                            <%
                                String corpusImgSrc = "../images/words.jpg";
                                String tryPath = "/images/corpora/" + corpusID + ".png";
                                String path = request.getSession().getServletContext().getRealPath(tryPath);
                                if (path!=null){
                                    File image = new File(path);    
                                    if (image.exists()){
                                        corpusImgSrc = ".." + tryPath;
                                    }
                                }
                            %>
                            <img src="<%= corpusImgSrc %>" class="card-img-top rounded" alt="...">
                        </figure>
                    </div>
                    <div class="col-md-10">                  
                        <div class="card-body">
                            <h5 class="card-title"><%=acronym%></h5>
                            <h6 class="card-subtitle mb-2 text-muted"><%=name%></h6>
                            <p class="card-text"><%=description%></p>
                            <a class="card-link"  target="_blank" href="speecheventstable.jsp?corpusID=<%=corpusID%>">
                                <%= backendInterface.getSpeechEvents4Corpus(corpusID).size() %>
                                <% if ("en".equals(language)) { %>
                                    Speech events
                                <% } else { %>
                                    Sprechereignisse
                                <% } %>
                                
                            </a>
                            <a class="card-link"  target="_blank" href="speakerstable.jsp?corpusID=<%=corpusID%>">
                                <%= backendInterface.getSpeakers4Corpus(corpusID).size() %> 
                                <% if ("en".equals(language)) { %>
                                    Speakers
                                <% } else { %>
                                    Sprecher
                                <% } %>
                                
                            </a>
                      </div>
                    </div>
                </div>
                </div>
                <!-- <h2><%=acronym%></h2>
                <h3><%=name%></h3>
                <p><%=description%></p>
                <p>
                    <a target="_blank" href="eventstable.jsp?corpusID=<%=corpusID%>">Events</a>
                    <span>  *  </span>
                    <a target="_blank" href="speecheventstable.jsp?corpusID=<%=corpusID%>">Speech Events</a>
                    <span>  *  </span>
                    <a target="_blank" href="speakerstable.jsp?corpusID=<%=corpusID%>">Speakers</a>
                </p> -->
        <%
            }
        %>
            </div>
            <div class="col-sm-2">
            </div>
            

        </div>
        
    </body>
</html>
