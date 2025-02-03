<%-- 
    Document   : index_new
    Created on : 26.01.2025, 15:32:21
    Author     : bernd
--%>

<%@page import="org.zumult.objects.Transcript"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="java.util.Random"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="java.util.ResourceBundle"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">        
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <!-- <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">        -->
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <!-- <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM" crossorigin="anonymous"></script>        -->
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
       Transcript transcript = bi.getTranscript(randomTranscriptID);
       String randomAnnotationBlockID = transcript.getFirstAnnotationBlockIDForTime(10.0);
    %>

    <body>
        
        <% String pageName = "ZuMult"; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>   
        
        <div class="container">
            <div class="jumbotron py-4" style="border-radius:10px;">
                <!-- <h4 class="display-4">Hello, world!</h4> -->
                <p class="lead">
                    <b>ZuMult</b> is a corpus platform for audiovisual corpora. You can get further information and news about
                    ZuMult on <a target="_blank" href="https://zumult.org">zumult.org</a>
                </p>
                <hr class="my-4">
                <p>
                    This is a demo installation of ZuMult, provided by <a href="https://linguisticbits.de" target="_blank">linguisticbits.de</a>. 
                    Other installations of ZuMult are:
                    <ul>
                        <li>
                            <a href="https://zumult.ids-mannheim.de" target="_blank">
                                ZuMult at the Archive for Spoken German
                            </a>
                        </li>
                        <li>
                            <a href="https://tgdp-zumult.la.utexas.edu/" target="_blank">
                                ZuMult at the Texas German Dialect Archive
                            </a>
                        </li>
                    </ul>                    
                    Learn more about ZuMult by exploring the individual apps listed below. 
                </p>
            </div>
            <div class="row">
                <div class="col-1"></div>
                <div class="col-10">
                    <ul class="list-unstyled">
                        
                      <li class="media">
                        <img src="./images/start-corpusoverview.png" class="mr-3 start" alt="..." 
                             style="width:480px; border: 2px solid gray; border-radius: 5px;">
                        <div class="media-body">
                          <h5 class="mt-0 mb-1">Corpora: Overview</h5>
                          <p>
                              This ZuMult instance has <%= allCorpusIDs.size() %> corpora: <br/>
                              <%= String.join(" / ", allCorpusIDs) %>. <br>
                              The corpus overview lists all corpora and has links to 
                              speech event and speaker overviews. 
                          </p>
                          <a href="./jsp/corpusoverview.jsp?lang=<%=currentLocale.getLanguage()%>" class="btn btn-primary float-right" target="_blank">
                                   Corpus Overview
                          </a>
                        </div>
                      </li>

                      <li class="media my-4">
                        <img src="./images/query.png" class="mr-3 start" alt="..." 
                             style="width:480px; border: 2px solid gray; border-radius: 5px;">
                        <div class="media-body">
                          <h5 class="mt-0 mb-1">Corpus: Query (ZuRecht)</h5>
                          <p>
                              Corpora in ZuMult can be queried on all annotation levels with 
                              ZuRecht, using <b>CQP</b> as a query language. Query results will be
                              displayed in a <b>KWIC</b> giving access to underlying audio or video
                              and to the wider transcript context.
                          </p>
                          <a href="./jsp/zuRecht.jsp?lang=<%=currentLocale.getLanguage()%>" class="btn btn-primary float-right" target="_blank">
                                   Corpus Query with ZuRecht
                          </a>
                        </div>
                      </li>
                      
                      <li class="media my-4">
                        <img src="./images/transcript2.png" class="mr-3 start" alt="..."  
                             style="width:480px; border: 2px solid gray; border-radius: 5px;">
                        <div class="media-body">
                          <h5 class="mt-0 mb-1">Transcript: View and Explore (ZuViel)</h5>
                          <p>
                              Transcripts are visualised by ZuViel in an <b>interactive</b>, <b>configurable</b> view with
                              <b>synchronisation</b> of audio/video and transcript text.
                              A density viewer, a wordlist and a search field provide further 
                              means of navigating the transcript. <br/>
                              The following transcript was chosen at random: <br/>
                              <small class="text-muted ml-2"><%= randomCorpusID %> / <%= randomTranscriptID %> </small> 
                              <a href="./jsp/zuViel.jsp?transcriptID=<%= randomTranscriptID %>" class="btn btn-primary float-right mt-3" target="_blank">
                                Transcript Visualisation with ZuViel
                              </a>
                          </p>
                        </div>
                      </li>

                      <li class="media my-4">
                        <img src="./images/zumin.png" class="mr-3 start" alt="..."  
                             style="width:480px; border: 2px solid gray; border-radius: 5px;">
                        <div class="media-body">
                          <h5 class="mt-0 mb-1">Transcript: Details (ZuMin)</h5>
                          <p>
                              For any contribution in a transcript, details can be explored 
                              by zooming in with ZuMin. The view provides
                              a <b>phoneme level alignment</b> (using MAUS),
                              a visualisation of the <b>pitch contour</b> (using Praat),
                              and <b>video stills</b> extracted each 0.5 seconds.
                              The following contribution was chosen at random: <br/>
                              <small class="text-muted ml-2">
                                  <%= randomCorpusID %> / <%= randomTranscriptID %> / <%= randomAnnotationBlockID %> 
                              </small> 
                              <a href="./jsp/zuMin.jsp?transcriptID=<%= randomTranscriptID %>&annotationBlockID=<%= randomAnnotationBlockID %>" 
                                 class="btn btn-primary float-right mt-3" target="_blank">
                                Transcript Details with ZuMin
                              </a>
                          </p>
                        </div>
                      </li>

                      <li class="media my-4">
                        <img src="./images/zupass.png" class="mr-3 start" alt="..."  
                             style="width:480px; border: 2px solid gray; border-radius: 5px;">
                        <div class="media-body">
                          <h5 class="mt-0 mb-1">Transcript: Other views (ZuPass &amp; ZuAnn)</h5>
                          <p>
                              ZuPass visualises transcripts in <b>musical score (Partitur)</b> notation. 
                              ZuAnn visualises transcripts with all <b>annotations</b>.
                              The following transcript was chosen at random: <br/>
                              <small class="text-muted ml-2"><%= randomCorpusID %> / <%= randomTranscriptID %> </small> 
                              <a href="./jsp/zuPass.jsp?transcriptID=<%= randomTranscriptID %>" class="btn btn-primary float-right mt-3" target="_blank">
                                Transcript Visualisation with ZuPass
                              </a>
                              <a href="./jsp/zuAnn.jsp?transcriptID=<%= randomTranscriptID %>" class="btn btn-primary float-right mt-3" target="_blank">
                                Transcript Visualisation with ZuAnn
                              </a>
                              <br/>
                          </p>
                        </div>
                      </li>
                      
                      
                    </ul>                    
                </div>
                <div class="col-1"></div>
            </div>
        </div>
                                   
        <footer class="footer mx-5">
            <hr class="mx-12">
            <div class="container text-center">
                <span class="text-muted">This ZuMult installation is provided by</span>
                <a href="https://linguisticbits.de" target="_blank">linguisticbits.de</a>.
                <span class="text-muted">See</span>
                <a target="_blank" href="https://zumult.org">zumult.org</a>
                <span class="text-muted">for more info and news about ZuMult.</span>
                
            </div>
        </footer>                                   
        
        
        
    </body>
</html>
