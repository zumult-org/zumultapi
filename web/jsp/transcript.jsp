<%-- 
    Document   : transcript
    Created on : 23.05.2018, 16:51:23
    Author     : Thomas_Schmidt
--%>

<%@page import="org.zumult.io.Constants"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.objects.Transcript"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Transcript</title>
        <style type="text/css">
            span.speaker {
                font-weight:bold;
                width: 100px;
                min-width:100px;
            }
            span.pause, span.desc, span.time {
                font-size:smaller;
                color:gray;
            }
                        
        </style>
        <script>
            function jump(time){
                player = document.getElementById('audioPlayer');
                if (player==null){
                    player = document.getElementById('videoPlayer1');
                }
                player.currentTime=time;
                player.play();
            }
            
            function registerAudioListener(){
                audioPlayer = document.getElementById('audioPlayer');
                if (audioPlayer!=null){
                    audioPlayer.addEventListener("timeupdate", updateAudioTime, true);                     
                    audioPlayer.addEventListener("onpause", updateAudioTime, true);                     
                }
                videoPlayer1 = document.getElementById('videoPlayer1');
                if (videoPlayer1!=null){
                    videoPlayer1.addEventListener("timeupdate", updateAudioTime, true);                     
                    videoPlayer1.addEventListener("onpause", updateAudioTime, true);                     
                }
            }
            
            function updateAudioTime(){
                var player = document.getElementById('audioPlayer');
                if (player==null){
                    player = document.getElementById('videoPlayer1');
                }
                var currentTime = player.currentTime;

                // highlight the spans which correspond to the current position
                var trElements = document.getElementsByTagName('tr');
                for (var i = 0; i < trElements.length; i++) {
                    element = trElements[i];
                    start = element.getAttribute('data-start');
                    end = element.getAttribute('data-end');
                    if ((start!=null) && (end!=null)){
                        if ((!player.paused) && (start < currentTime) && (end > currentTime)){
                            element.style.backgroundColor='lightGray';
                        } else {
                            element.style.backgroundColor='inherit';                           
                        }
                    }
                }                        

        }
            
            
        </script>
    </head>
    <body onload="registerAudioListener()">
        <%
            String transcriptID = request.getParameter("transcriptID");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            long time = System.currentTimeMillis();
            Transcript transcript = backendInterface.getTranscript(transcriptID);
            long time1 = System.currentTimeMillis()-time;
        %>
        <p style="font-size:8pt; color:gray;"><%= time1 %> ms for retrieval of transcript / 
        <%
            Transcript partTranscript = transcript;
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            if (startTime!=null || endTime!=null){
                if (startTime==null) startTime = Double.toString(transcript.getStartTime());
                if (endTime==null) endTime = Double.toString(transcript.getEndTime());
                partTranscript = transcript.getPart(Double.parseDouble(startTime), Double.parseDouble(endTime), true);                
            }
            
            //Transcript partTranscript = transcript.getPart(Double.parseDouble(startTime), Double.parseDouble(endTime), true);
            long time2 = System.currentTimeMillis()-time1-time;
         %>
        <%= time2 %> ms for getting part of transcript / 
        <%
            String partTranscriptXML = partTranscript.toXML();
            //String partTranscriptHTML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/isotei2html_table.xsl", partTranscriptXML);
            String partTranscriptHTML = new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2HTML_STYLESHEET, partTranscriptXML);
            long time3 = System.currentTimeMillis()-time2-time1-time;
            
        %>
        <%= time3 %> ms for XSL transforming transcript</p> 
        <h1><%= transcriptID %></h1>
         <%
            IDList videos = backendInterface.getVideos4Transcript(transcriptID); 

            int count=1;
            for (String videoID : videos){
                String videoURL = backendInterface.getMedia(videoID).getURL();
                String htmlID = "videoPlayer" + Integer.toString(count);
                count++;
         %>
        <video id="<%=htmlID%>" width="720" height="405" controls="controls">
            <source src="<%=videoURL%>" type="video/mp4">
        </video>        
        <%
            } 
        %>
        <% 
            if (videos.isEmpty()){
                IDList audios = backendInterface.getAudios4Transcript(transcriptID); 
                String audioURL = backendInterface.getMedia(audios.get(0)).getURL();
         %>
        <audio id="audioPlayer" controls="controls">
            <source src="<%=audioURL%>" type="audio/mpeg">
        </audio>       <br/> 
        <% } %>
            
        <%= partTranscriptHTML %>

    </body>
</html>
