<%-- 
    Document   : mediaOverview
    Created on : 26.03.2025, 10:56:18
    Author     : bernd
--%>

<%@page import="java.net.URL"%>
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
        <script src="https://kit.fontawesome.com/ed5adda70b.js" crossorigin="anonymous"></script>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                

        <link rel="stylesheet" href="../css/overview.css"/>  
        
            <script>
                function setSelectMedia(mediaID, select){
                    const mediaDiv = $('#media-div-' + mediaID);
                    const titleDiv = $('#title-div-' + mediaID);
                    const borderDiv = $('#border-div-' + mediaID);
                    if (select){
                        mediaDiv.attr('data-selected', 'true');
                        titleDiv.css('background-color' , 'blue');
                        titleDiv.css('color' , 'white');
                        borderDiv.removeClass('border-secondary');
                        borderDiv.addClass('border-primary');
                        borderDiv.css('background-color', '#F1FFFF');
                    } else {
                        mediaDiv.attr('data-selected', 'false');
                        titleDiv.css('background-color' , 'lightgray');
                        titleDiv.css('color' , 'black');
                        borderDiv.removeClass('border-primary');
                        borderDiv.addClass('border-secondary');
                        borderDiv.css('background-color', 'white');
                    }                    
                    updateCounts();                    
                }

                function toggleSelectMedia(mediaID){
                    const mediaDiv = $('#media-div-' + mediaID);
                    const isSelected = (mediaDiv.attr('data-selected') === 'true');
                    setSelectMedia(mediaID, !isSelected);
                }

                // ************************
                function setMuteMedia(mediaID, mute){
                    const media = $('#videoPlayer-' + mediaID);
                    const button = $('#toggle-mute-btn-' + mediaID);
                    if (mute){
                        media.attr('data-muted', 'true');                        
                        button.find('i').first().removeClass('fa-volume').addClass('fa-volume-slash');                        
                    } else {
                        media.attr('data-muted', 'false');
                        button.find('i').first().removeClass('fa-volume-slash').addClass('fa-volume');                        
                    }                    
                    media.prop('muted', function(i, val) {
                        return mute; // toggle the current muted state
                    });            
                    updateCounts();                                        
                }
                
                function toggleMuteMedia(mediaID){
                    const media = $('#videoPlayer-' + mediaID);
                    const isMute = (media.attr('data-muted') === 'true');
                    setMuteMedia(mediaID, !isMute);
                }
    
                // ************************
                function setShowPlayer(mediaID, show){
                    const button = $('#toggle-player-btn-' + mediaID);
                    const playerDiv = $('#player-div-' + mediaID);
                    if (show){
                        playerDiv.css('display', 'block');
                        button.find('i').first().removeClass('fa-eye').addClass('fa-eye-slash');                        
                    } else {
                        playerDiv.css('display', 'none');
                        button.find('i').first().removeClass('fa-eye-slash').addClass('fa-eye');                                                
                    }
                    
                }

                function toggleShowPlayer(mediaID){
                    const playerDiv = $('#player-div-' + mediaID);
                    setShowPlayer(mediaID, playerDiv.css('display') === 'none');
                }
                
                
                // ************************
                function moveUpDown(mediaID){
                    const button = $('#move-up-down-btn-' + mediaID);
                    const mediaDiv = $('#media-div-' + mediaID);
                    const infoTable = $('#info-table-' + mediaID);
                    if (mediaDiv.data('position') === 'down'){
                        $('#up-row-div').append(mediaDiv)
                        infoTable.css('display', 'none');
                        button.find('i').first().removeClass('fa-up-to-line').addClass('fa-down-to-line');      
                        mediaDiv.data('position', 'up');
                        setShowPlayer(mediaID, true);
                        setSelectMedia(mediaID, true);
                        var count = $('#up-row-div > div').length;    
                        if (count===1){
                            setMuteMedia(mediaID, false);
                        }
                    } else {
                        $('#down-row-div').append(mediaDiv)
                        infoTable.css('display', 'table');
                        button.find('i').first().removeClass('fa-down-to-line').addClass('fa-up-to-line');                        
                        mediaDiv.data('position', 'down');
                        setShowPlayer(mediaID, false);
                        setSelectMedia(mediaID, false);
                    }
                    updateCounts();
                }
                
                // ************************
                function updateCounts(){
                    const countSelected = $('[data-selected="true"]').length;
                    const countUnmuted = $('[data-muted="false"]').length;
                    $('#selectedCount').html(countSelected);
                    $('#unmutedCount').html(countUnmuted);
                    
                }
                
            </script>
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
                <div class="row">
                    <div class="col-4">
                        <b><%= videoList.size() %></b> Video(s)<br/>
                        <b id="selectedCount">0</b> selected<br/>
                        <b id="unmutedCount">0</b> unmuted
                    </div>
                    <div class="col-8">
                        <div class="btn-group m-3" role="group"> 
                             <button
                                class="btn btn-outline-dark btn-lg"
                                title="Click to show/hide player"
                                onclick="backward-fast()"
                                >
                              <i class="fa-solid fa-backward-fast"></i>
                            </button>                                    

                            <button
                                class="btn btn-outline-dark btn-lg"
                                title="Click to (un)mute player"
                                onclick="backward()"
                                >
                              <i class="fa-solid fa-backward"></i>
                            </button>                                    

                            <button
                                class="btn btn-outline-dark btn-lg"
                                title="Click to (un)mute player"
                                onclick="backward-step()"
                                >
                              <i class="fa-solid fa-backward-step"></i>
                            </button>                                    

                            <button
                                class="btn btn-dark btn-lg pl-5 pr-5"
                                title="Play or pause"
                                onclick="playPause()"
                                >
                              <i class="fa-solid fa-play"></i>
                            </button>                                    

                            <button
                                class="btn btn-outline-dark btn-lg"
                                title="Click to (un)mute player"
                                onclick="forward-step()"
                                >
                              <i class="fa-solid fa-forward-step"></i>
                            </button>                                    

                            <button
                                class="btn btn-outline-dark btn-lg"
                                title="Click to (un)mute player"
                                onclick="forward()"
                                >
                              <i class="fa-solid fa-forward"></i>
                            </button>     
                            
                             <button
                                class="btn btn-outline-dark btn-lg"
                                title="Click to show/hide player"
                                onclick="forward-fast()"
                                >
                              <i class="fa-solid fa-forward-fast"></i>
                            </button>                                    
                            

                        </div>
                    </div>
                </div>
                <div class="d-flex flex-wrap align-items-stretch bg-light border border-primary border-5 rounded" id="up-row-div">
                            
                </div>
                <div class="d-flex flex-wrap align-items-stretch" id="down-row-div">
                    <%
                        int index = 0;
                        for (String videoID : videoList){
                            Media video = backendInterface.getMedia(videoID);
                            String path = new URL(video.getURL()).getPath();
                            String filename = path.substring(path.lastIndexOf('/') + 1);                            
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
                    <div class="media my-4" id="media-div-<%= videoID %>" data-selected="false" data-position="down">
                        <div class="border border-5 border-secondary rounded p-3 m-2" id="border-div-<%= videoID %>">
                            <div class="row">
                               <div class="col-md-auto">    
                                    <span class="badge badge-info"><%= videoID %></span>
                               </div>
                            </div>
                            <div class="row">
                               <div class="col-md-auto">                                    
                                   <div class="mb-2 p-3" style="font-weight:bold; background:lightgray; cursor: grab;" 
                                        title="Click to (un)select"
                                        onclick="toggleSelectMedia('<%= videoID %>')"
                                        id = "title-div-<%= videoID %>"
                                        >
                                       <i class="fa-solid fa-film"></i>
                                        <%= filename %>
                                   </div> 
                               </div>
                            </div>
                            <div class="row" style="display: block;">
                                <div class="col-md-auto">                                    
                                    <table class="table table-striped table-sm" 
                                           id="info-table-<%= videoID %>"
                                           style="white-space:nowrap; font-size: 9pt;">
                                        <% for (String[] metadataPair : metadata){ %>
                                        <tr>
                                            <td><b><%= metadataPair[0] %></b></td>
                                            <td><%= metadataPair[1] %></td>
                                        </tr>
                                        <% } %>
                                    </table>
                                    <div class="btn-group m-3" role="group"> 
                                         <button id="toggle-player-btn-<%= videoID %>" 
                                            class="btn btn-outline-primary btn-sm"
                                            title="Click to show/hide player"
                                            onclick="toggleShowPlayer('<%= videoID %>')"
                                            >
                                          <i class="fa-solid fa-eye"></i>
                                        </button>                                    

                                        <button id="toggle-mute-btn-<%= videoID %>" 
                                            class="btn btn-outline-primary btn-sm"
                                            title="Click to (un)mute player"
                                            onclick="toggleMuteMedia('<%= videoID %>')"
                                            >
                                          <i class="fa-solid fa-volume-slash"></i>
                                        </button>              
                                            
                                        <button id="move-up-down-btn-<%= videoID %>" 
                                            class="btn btn-outline-primary btn-sm"
                                            title="Click to move to/from top row"
                                            onclick="moveUpDown('<%= videoID %>')"
                                            >
                                            <i class="fa-solid fa-up-to-line"></i>                                            
                                        </button>              
                                            

                                    </div>
                                </div>
                                <div class="col-md-auto">
                                    <div id="player-div-<%= videoID %>" style="display:none;">
                                        <video id="videoPlayer-<%= videoID %>" 
                                               width="480" height="270" controls="controls"
                                               muted="muted"
                                               data-muted="true"
                                               >
                                            <source src="<%=video.getURL()%>" type="video/mp4">
                                        </video>                                  
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>                        
                    <%
                        }
                    %>    
                </div>
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
