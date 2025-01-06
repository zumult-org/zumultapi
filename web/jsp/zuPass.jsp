<%-- 
    Document   : zuPass
    Created on : 10.12.2024, 16:31:06
    Author     : bernd
--%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.zumult.backend.Configuration"%>
<%@page import="org.zumult.objects.Media"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="org.zumult.objects.Transcript"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.io.IOHelper"%>




<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%
    BackendInterface backend = BackendInterfaceFactory.newBackendInterface(); 
    String transcriptID = request.getParameter("transcriptID");
    if (transcriptID==null){
        // redirect to error page
    }
    
    String pageName = "ZuPass";
    String pageTitle = transcriptID;
    
    String speechEventID = backend.getSpeechEvent4Transcript(transcriptID);
    //String transcriptID = "ISO_robmus_2015_01_002";
    //String transcriptID = "IDE57E5B6C-E67B-B454-E462-4E4868C79333";
    Transcript exbTranscript = backend.getTranscript(transcriptID, Transcript.TranscriptFormats.EXB);
    String xml = exbTranscript.toXML();
    String[][] parameters = {
    };
    String html = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/exb2Partitur.xsl", xml, parameters);  
    
    String videoIDsParameter = request.getParameter("videoIDs");
    List<String> videoIDs = new ArrayList<>();
    if (videoIDsParameter==null || videoIDsParameter.length()==0){
        videoIDs = backend.getVideos4SpeechEvent(speechEventID);
    } else {
        String[] videoIDsSplit = videoIDsParameter.split("\\|");
        videoIDs.addAll(Arrays.asList(videoIDsSplit));
    }
    
    String audioIDsParameter = request.getParameter("audioIDs");
    List<String> audioIDs = new ArrayList<>();
    if (videoIDs.isEmpty() || (audioIDsParameter==null || audioIDsParameter.length()==0)){
        audioIDs = backend.getAudios4SpeechEvent(speechEventID);
    } else {
        String[] audioIDsSplit = audioIDsParameter.split("\\|");
        audioIDs.addAll(Arrays.asList(audioIDsSplit));
    } 

    String vttURL = Configuration.getWebAppBaseURL() + "/ZumultDataServlet?command=getVTT&transcriptID=" + transcriptID;

%>

<%@include file="../WEB-INF/jspf/locale.jspf" %> 

<html>
        <html>
            <head>
                <script src="../js/media_zupass.js"></script>
                <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>        
                <script src="https://kit.fontawesome.com/e215b03c17.js" crossorigin="anonymous"></script>
                <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
                <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
                <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                

                <script>
                    // probably all variables from the URL should be mirrored in this way
                    var speechEventID = '<%= speechEventID %>';
                    var transcriptID = '<%= transcriptID %>';
                    var vttURL = '<%= vttURL %>';
                    var BASE_URL = '<%= Configuration.getWebAppBaseURL() %>';                    
                </script>
                
                <style type="text/css">
                    table {
                        border-collapse : collapse;
                    }
                    td {
                        white-space:nowrap;
                        border: 1px solid rgb(200,200,200);;
                    }
                    td.empty {
                        background: rgb(230,230,230);
                        border: none;
                    }
                    td.ver {
                        font-size:14pt;
                        font-weight: bold;
                    }
                    td.att, td.mov, td.it-ph, td.op, td.en {
                        font-size: 10pt;
                        background : rgb(255,255,204);
                    }
                    td.walk, td.smile, td.nod, td.act {
                        font-size: 10pt;
                        background : rgb(204,255,204);
                    }
                    td.DataCoverage, td.Aktivität, td.Wahrnehmung, td.VPsichtbar {
                        font-size: 10pt;
                        background : rgb(177,233,244);
                    }
                    td.label {
                        font-weight:bold;
                        position: sticky;
                        left: 0; z-index: 1;
                        background-color: white;
                    }
                    td.tli {
                        font-size:8pt;
                        color: rgb(200,200,200);
                    }
                    td.highlight-playback{
                        border: 2px solid gray; 
                        color: red;
                    }                    
                </style>
            </head>
    <body onload="initialiseMedia()">
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
               
        <div id="video-form" class="row justify-content-center" style="margin-top:80px;">
            <div class="col-auto">        
                <table>
                    <%
                        if (!audioIDs.isEmpty() && videoIDs.isEmpty()){
                    %>
                    <tr>
                    <% for (int i=0; i<Math.min(audioIDs.size(),2); i++){
                        String audioID = audioIDs.get(i);
                        Media audio = backend.getMedia(audioID);
                        String url = audio.getURL();
                        String id = "master-audio";
                        if (i>0){
                            id = "audio-" + Integer.toString(i);
                        }
                    %>
                    <td>
                        <audio controls="controls" name="audio" id="<%= id %>" style="margin-right:30px">
                            <source src="<%= url %>" type="audio/wav"/>
                        </video>
                    </td>
                    <%
                        }
                    %>
                    </tr>
                    <%
                        }
                    %>
                    <tr>
                        <% 
                            for (int i=0; i<Math.min(videoIDs.size(),2); i++){
                                String videoID = videoIDs.get(i);
                                Media video = backend.getMedia(videoID);
                                String url = video.getURL();
                                String id = "master-video";
                                if (i>0){
                                    id = "video-" + Integer.toString(i);
                                }
                        %>
                        <td>
                            <video width="480" height="320" controls="controls" name="video" id="<%= id %>" style="margin-right:30px">
                                <source src="<%= url %>" type="video/mp4"/>
                                <track label="trans" kind="subtitles" srclang="de" src="<%= vttURL %>" default="default">
                                <track label="norm" kind="subtitles" srclang="de" src="<%= vttURL + "&subtitleType=norm"%>">                
                            </video>
                        </td>
                        <td>
                            <div style="background: #f8f9fa; height: 270px; border-radius: 3px; padding: 3px;">
                                <a href="javascript:addVideoImageToCollection('<%= videoID %>')" 
                                   title="<%=myResources.getString("AddVideoImageCollection")%>" style="color:black">
                                    <i class="far fa-plus-square"></i>
                                </a><br/>          
                                <a href="javascript:getVideoImage('<%= videoID %>')" 
                                   title="<%=myResources.getString("ExtractVideoImage")%>" style="color:black">
                                    <i class="fas fa-camera-retro"></i>
                                </a><br/>
                                <a href="javascript:frameBackward()" title="<%=myResources.getString("PrecedingFrame")%>" style="color:black">
                                    <i class="fas fa-step-backward"></i>
                                </a><br/>
                                <a href="javascript:frameForward()" title="<%=myResources.getString("NextFrame")%>" style="color:black">
                                    <i class="fas fa-step-forward"></i>
                                </a>
                            </div>                        
                        </td>
                    <%
                        }
                    %>    
                    </tr>
                </table>
            </div>
        </div>
        
        <div id="partitur-form" class="row mt-2">
            <div class="col-1"></div>
            <div class="col-10 overflow-auto" style="height: 400px;">
                    <%= html %>                
            </div>
            <div class="col-1"></div>
        </div>
    </body>
</html>
