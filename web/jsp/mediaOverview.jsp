<%-- 
    Document   : mediaOverview
    Created on : 26.03.2025, 10:56:18
    Author     : bernd
--%>

<%@page import="org.zumult.objects.ObjectTypesEnum"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.zumult.objects.Media"%>
<%@page import="org.zumult.objects.SpeechEvent"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../WEB-INF/jspf/locale.jspf" %>     

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuMult: Media Overview</title>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                

        <link rel="stylesheet" href="../css/overview.css"/>       
    </head>
    <body>
        <%
            String speechEventID = request.getParameter("speechEventID");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            String corpusID = backendInterface.getCorpus4Event(backendInterface.getEvent4SpeechEvent(speechEventID));
            SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
            IDList videoList = backendInterface.getVideos4SpeechEvent(speechEventID);
            IDList audioList = backendInterface.getAudios4SpeechEvent(speechEventID);
            Set<MetadataKey> metadataKeys = backendInterface.getMetadataKeys4Corpus(corpusID, ObjectTypesEnum.MEDIA);
        %>
        <% String pageName = "ZuMult"; %>
        <% String pageTitle = "Media overview speech event  - " + speechEventID; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        
        <div class="row">
            <div class="col-1">
            </div>
            <div class="col-10">
                <ul class="list-unstyled">
                    <%
                        int index = 0;
                        for (String videoID : videoList){
                            Media video = backendInterface.getMedia(videoID);
                            List<String[]> metadata = new ArrayList<>();
                            index++;
                            for (MetadataKey metadataKey : metadataKeys){
                                String metadataValue = video.getMetadataValue(metadataKey);
                                if (metadataValue!=null){
                                    String[] thisMeta = {
                                        metadataKey.getName("en"),
                                        metadataValue
                                    };
                                    metadata.add(thisMeta);
                                }
                            }

                    %>
                    <li class="media my-4">
                        <div class="border border-5 border-primary rounded p-3">
                            <div class="row">
                                <div class="col-md-auto">
                                    <span class="badge badge-primary"><%= videoID %></span>
                                    <div>
                                        <video id="videoPlayer-<%= Integer.toString(index)%>" width="480" height="270" controls="controls">
                                            <source src="<%=video.getURL()%>" type="video/mp4">
                                        </video>                                  
                                    </div>
                                </div>
                                <div class="col-md-auto">
                                    <table class="table table-striped table-sm" style="white-space:nowrap;">
                                        <% for (String[] metadataPair : metadata){ %>
                                        <tr>
                                            <td><b><%= metadataPair[0] %></b></td>
                                            <td><%= metadataPair[1] %></td>
                                        </tr>
                                        <% } %>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </li>                        
                    <%
                        }
                    %>    
                </ul>
            </div>
            <div class="col-1">
            </div>
        </div>
                
        <!-- ********************* -->
        
        
        <div class="row">
            <div class="col-1">
            </div>
            <div class="col-10">
                <ul class="list-unstyled">
                    <%
                        int index2 = 0;
                        for (String audioID : audioList){
                            Media audio = backendInterface.getMedia(audioID);
                            List<String[]> metadata = new ArrayList<>();
                            index2++;
                            for (MetadataKey metadataKey : metadataKeys){
                                String metadataValue = audio.getMetadataValue(metadataKey);
                                if (metadataValue!=null){
                                    String[] thisMeta = {
                                        metadataKey.getName("en"),
                                        metadataValue
                                    };
                                    metadata.add(thisMeta);
                                }
                            }

                    %>
                    <li class="media my-4">
                        <div class="border border-5 border-primary rounded p-3">
                            <div class="row">
                                <div class="col-md-auto">
                                    <span class="badge badge-primary"><%= audioID %></span>
                                    <div>
                                        <audio id="audioPlayer-<%= Integer.toString(index2)%>" width="480" height="270" controls="controls">
                                            <source src="<%=audio.getURL()%>" type="audio/wav">
                                        </video>                                  
                                    </div>
                                </div>
                                <div class="col-md-auto">
                                    <table class="table table-striped table-sm" style="white-space:nowrap;">
                                        <% for (String[] metadataPair : metadata){ %>
                                        <tr>
                                            <td><b><%= metadataPair[0] %></b></td>
                                            <td><%= metadataPair[1] %></td>
                                        </tr>
                                        <% } %>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </li>                        
                    <%
                        }
                    %>    
                </ul>
            </div>
            <div class="col-1">
            </div>
        </div>
        
    </body>
</html>
