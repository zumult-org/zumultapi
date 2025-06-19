<%-- 
    Document   : mediaOverview
    Created on : 26.03.2025, 10:56:18
    Author     : bernd
--%>

<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.HashMap"%>
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

        <%
            String speechEventID = request.getParameter("speechEventID");
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            String corpusID = backendInterface.getCorpus4Event(backendInterface.getEvent4SpeechEvent(speechEventID));
            SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
            IDList videoList = backendInterface.getVideos4SpeechEvent(speechEventID);
            IDList audioList = backendInterface.getAudios4SpeechEvent(speechEventID);
            IDList mediaList = new IDList("media");
            mediaList.addAll(videoList);
            mediaList.addAll(audioList);
            Set<MetadataKey> metadataKeys = backendInterface.getMetadataKeys4Corpus(corpusID, ObjectTypesEnum.MEDIA);
            
            // change 10-06-2025, bring some order in those keys
            List<MetadataKey> nonFFProbeKeys = new ArrayList<>();
            List<MetadataKey> videoFFProbeKeys = new ArrayList<>();
            List<MetadataKey> audioFFProbeKeys = new ArrayList<>();
            
            for (MetadataKey metadataKey : metadataKeys){
                if (!metadataKey.getName("en").startsWith("ffprobe")){
                    nonFFProbeKeys.add(metadataKey);
                } else if (metadataKey.getName("en").startsWith("ffprobe-video")){
                    videoFFProbeKeys.add(metadataKey);                
                } else {
                    audioFFProbeKeys.add(metadataKey);
                }
            }
            
            Collections.sort(nonFFProbeKeys);
            Collections.sort(videoFFProbeKeys);
            Collections.sort(audioFFProbeKeys);
            
            List<MetadataKey> sortedMetadataKeys = new ArrayList<>();
            sortedMetadataKeys.addAll(nonFFProbeKeys);
            sortedMetadataKeys.addAll(videoFFProbeKeys);
            sortedMetadataKeys.addAll(audioFFProbeKeys);
            
            Map<MetadataKey, IDList> availableValues4Keys = new HashMap<>();
            for (MetadataKey metadataKey : nonFFProbeKeys){
                IDList availableValues = backendInterface.getAvailableValues(corpusID, metadataKey);
                Collections.sort(availableValues);
                availableValues4Keys.put(metadataKey, availableValues);
            }
        %>




<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuMult: Media Overview</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.6/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-4Q6Gf2aSP4eDXB8Miphtr37CMZZQ5oXLH2yaXMJ2w8e2ZtHTl7GptT4jmndRuHDT" crossorigin="anonymous">
        <link href="../css/query.css" rel="stylesheet"/>
        
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.6/dist/js/bootstrap.bundle.min.js" integrity="sha384-j1CDi7MgGQ12Z7Qab0qlWQ/Qqz24Gc6BM0thvEMVjHnfYGF0rmFCozFSxQBxwHKO" crossorigin="anonymous"></script>        
        <script src="https://kit.fontawesome.com/ed5adda70b.js" crossorigin="anonymous"></script>

        <link rel="stylesheet" href="../css/overview.css"/>  
        
            <script>
    
                var BASE_URL = '<%= Configuration.getWebAppBaseURL() %>';
                
                // **************************************
                // put all metadata attributes with their values into a JS variable
                // **************************************
                var availableValues4Keys = {};
                <% for (MetadataKey metadataKey : availableValues4Keys.keySet()){ 
                    IDList avValues = availableValues4Keys.get(metadataKey);
                    String joined = "";
                    for (int i=0; i<avValues.size(); i++){
                        joined+="\"" + StringEscapeUtils.escapeJavaScript(avValues.get(i)) + "\"";
                        if (i<avValues.size()-1){
                            joined+=",";
                        }
                    }
                    String list = "[" + joined + "]";
                %>
                   availableValues4Keys["<%= metadataKey.getName("en") %>"] = <%= list %>;
                <% } %>
                    
                // **************************************
                // observe videos: load only if visible
                // **************************************
                document.addEventListener("DOMContentLoaded", function () {

                    let allMediaPlayers = $('[name="syncMediaPlayer"]');                 
                    $.each(allMediaPlayers, function(){
                        let video = $(this).get(0);
                        const observer = new IntersectionObserver((entries, observer) => {
                          entries.forEach(entry => {
                            if (entry.isIntersecting) {
                              // Load video by setting the src
                              const src = video.getAttribute('data-src');
                              if (src) {
                                video.src = src;
                                video.load(); // Trigger load
                              }
                              observer.unobserve(video); // Stop observing once loaded
                            }
                          });
                        }, {
                          rootMargin: '0px',
                          threshold: 0.25 // Load when 25% visible (you can tweak this)
                        });
                        observer.observe(video);
                    });
                });                    
                
    
    
    
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
                    const media = $('#mediaPlayer-' + mediaID);
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
                    const media = $('#mediaPlayer-' + mediaID);
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
                
                // ************************
                function updateMetadataFilterValueSelect(){
                    $('#metadataFilterValueSelect').empty();
                    let selectedAttribute = $('#metadataFilterAttributeSelect').find(":selected").val();
                    if (selectedAttribute!=="None"){
                        let availableValues = availableValues4Keys[selectedAttribute];
                        //alert(availableValues);
                        $.each(availableValues, function(index, value){
                            $('#metadataFilterValueSelect').append($('<option>', {
                                value: value,
                                text: value
                            }));
                        });                        
                    }
                }

                // ************************
                function showStillImages(videoID){
                
                    let button = $('#still-series-btn-' + videoID);
                    button.find('i').first().removeClass('fa-images').addClass('fa-spinner').addClass('fa-spin'); 
                    const randomDouble = Math.random() * 9 + 1;
                    $.post(
                        BASE_URL + "/ZumultDataServlet",
                        { 
                            command: 'getStillSeries',
                            videoID: videoID,
                            numberOfImages: 4,
                            startTime: randomDouble
                        },
                        function( data ) {
                            if ($(data).find("error").length > 0){
                                alert('No video for ' + transcriptID);                            
                                return;
                            }
                            $('#still-container-' + videoID).html(data);
                            button.find('i').first().addClass('fa-images').removeClass('fa-spinner').removeClass('fa-spin'); 

                        }
                    );                    
                    
                }
                // ************************
                function largerImage(obj){                
                    let imageURL = $(obj).attr('src');
                    let imageHTML = "<img id=\"modal-video\" src=\"" + imageURL +  "\"/>";
                    $('#image-div').html(imageHTML);                
                    $('#imageModal').modal("toggle");
                }
                // ************************                
                function filterMetadata(){
                    let selectedAttribute = $('#metadataFilterAttributeSelect').find(":selected").val();
                    let selectedValue = $('#metadataFilterValueSelect').find(":selected").val();
                    //alert("Filter: " + selectedAttribute + " / " + selectedValue);
                    let mediaDivs = $('div[name="main-media-div"]');
                    let filteredAudio = 0;
                    let filteredVideo = 0;
                    $.each(mediaDivs, function(){
                        if (selectedAttribute==="None" || $(this).data(selectedAttribute)===selectedValue){
                            $(this).show();
                            if ($(this).data("media-type")==="VIDEO"){
                                filteredVideo++;
                            } else {
                                filteredAudio++;                                
                            }
                        } else {
                            $(this).hide();                                
                        }
                    });  
                    $('#filteredVideoCount').html(filteredVideo);
                    $('#filteredAudioCount').html(filteredAudio);                    
                }
                
                // ************************
                var syncPlaying = false;
                function syncPlayPause(){
                    let button = $('#sync-play-pause-button');
                    if (syncPlaying){
                        // pause
                        if (button!==null){
                            // switch the icon to "play"
                            button.find('i').first().removeClass('fa-pause').addClass('fa-play');                                                
                        }
                        let allMediaPlayers = $('[name="syncMediaPlayer"]');
                        $.each(allMediaPlayers, function(){
                            $(this).get(0).pause();
                        });
                        syncPlaying = false;
                        return;
                    }
                    if (button!==null){
                        // switch the icon to "pause"
                        button.find('i').first().removeClass('fa-play').addClass('fa-pause');                                                
                    }
                    let activeMediaPlayers = $('[data-selected="true"] [name="syncMediaPlayer"]');
                    alert(activeMediaPlayers.length + " players selected.")
                    $.each(activeMediaPlayers, function(){
                        $(this).get(0).play();
                    });
                    syncPlaying = true;
                    
                }
            </script>
    </head>
    <body>
        <% String pageName = "ZuMult"; %>
        <% String pageTitle = "Media overview speech event  - " + speechEventID; %>
        <%@include file="../WEB-INF/jspf/zumultNav.jspf" %>                                                
        
        <div class="row">
            <div class="col-1">
            </div>
            <div class="col-10">
                <div class="row align-items-end">
                    <div class="col-2 mb-3" id="countDiv">
                        <b id="filteredVideoCount"><%= videoList.size() %></b> (<%= videoList.size() %>) Video(s) / 
                        <b id="filteredAudioCount"><%= audioList.size() %></b> (<%= audioList.size() %>) Audios(s)<br/>
                        <b id="selectedCount">0</b> selected / <b id="unmutedCount">0</b> unmuted
                    </div>
                    
                    <!-- **************************************************** -->
                    <!-- *** The buttons for controlling the media player *** -->    
                    <!-- **************************************************** -->
                    <div class="col-7" id="playerControlsDiv">
                        <div class="row justify-content-center">
                            <div class="btn-group m-3" role="group"> 
                                 <button class="btn btn-outline-dark btn-lg" title="To start of recording" onclick="backward-fast()">
                                  <i class="fa-solid fa-backward-fast"></i>
                                </button>                                    

                                <button class="btn btn-outline-dark btn-lg" title="???" onclick="backward()">
                                  <i class="fa-solid fa-backward"></i>
                                </button>                                    

                                <button class="btn btn-outline-dark btn-lg" title="One frame backward" onclick="backward-step()">
                                  <i class="fa-solid fa-backward-step"></i>
                                </button>                                    

                                <button class="btn btn-dark btn-lg pl-5 pr-5" title="Play or pause" onclick="syncPlayPause()" id="sync-play-pause-button">
                                  <i class="fa-solid fa-play"></i>
                                </button>                                    

                                <button class="btn btn-outline-dark btn-lg" title="One frame forward" onclick="forward-step()">
                                  <i class="fa-solid fa-forward-step"></i>
                                </button>                                    

                                <button class="btn btn-outline-dark btn-lg" title="???" onclick="forward()">
                                  <i class="fa-solid fa-forward"></i>
                                </button>     

                                 <button class="btn btn-outline-dark btn-lg" title="To end of recording" onclick="forward-fast()">
                                  <i class="fa-solid fa-forward-fast"></i>
                                </button>                                                                
                            </div>
                        </div>
                        
                    </div>

                    <!-- **************************************************** -->
                    <!-- *** The controls for filtering metadata          *** -->    
                    <!-- **************************************************** -->
                    <div class="col-3" id="filterDiv">
                        <div class="input-group mb-3">
                          <select class="form-select" id="metadataFilterAttributeSelect" onchange="updateMetadataFilterValueSelect()">
                            <option value="None" selected="selected">None...</option>
                            <%
                            for (MetadataKey metadataKey : nonFFProbeKeys){ %>
                                <option value="<%= metadataKey.getName("en")%>"><%= metadataKey.getName("en")%></option>
                            <% } %>                                
                          </select>
                          <select class="form-select" id="metadataFilterValueSelect">
                          </select>
                          <button class="input-group-text" onclick="filterMetadata()" title="Click to filter">
                              <i class="fa-solid fa-filter"></i>
                          </button>
                        </div>
                    </div>
                        
                </div>

                          
                <!-- **************************************************** -->
                <!-- *** The upper row, empty intially                *** -->    
                <!-- **************************************************** -->
                <div class="d-flex flex-wrap align-items-stretch bg-light border border-primary border-2 rounded" id="up-row-div">
                            
                </div>

                <!-- **************************************************** -->
                <!-- *** The lower row, contains all media initally   *** -->    
                <!-- **************************************************** -->
                <div class="d-flex flex-wrap align-items-stretch" id="down-row-div">
                    <%
                        int index = 0;
                        for (String mediaID : mediaList){
                            Media media = backendInterface.getMedia(mediaID);
                            Media.MEDIA_TYPE mediaType = media.getType();
                            String textBG = "text-bg-primary";
                            if (mediaType==Media.MEDIA_TYPE.AUDIO){
                                textBG = "text-bg-success";
                            }
                            String path = new URL(media.getURL()).getPath();
                            String filename = path.substring(path.lastIndexOf('/') + 1);                            
                            List<String[]> metadata = new ArrayList<>();
                            index++;
                            //for (MetadataKey metadataKey : metadataKeys){
                            for (MetadataKey metadataKey : sortedMetadataKeys){
                                String metadataValue = media.getMetadataValue(metadataKey);
                                if (metadataValue!=null){
                                    String[] thisMeta = {
                                        metadataKey.getName("en"),
                                        metadataValue
                                    };
                                    metadata.add(thisMeta);
                                }
                            }

                    %>
                    <!-- **************************************************** -->
                    <!-- *** A single media div                           *** -->    
                    <!-- **************************************************** -->
                    <div class="media my-4" id="media-div-<%= mediaID %>" data-selected="false" data-position="down" name="main-media-div"
                        data-media-type="<%= mediaType.toString()%>"
                        <% for (String[] metadataPair : metadata){ %>
                        data-<%=metadataPair[0]%>="<%=metadataPair[1]%>" 
                        <% } %>
                    >
                        <div class="border border-5 border-secondary rounded-3 p-3 m-2" id="border-div-<%= mediaID %>">
                            <div class="row">
                               <!-- ID of the media in a badge -->
                               <div class="col-md-auto">    
                                    <span class="badge rounded-pill text-bg-warning mb-2"><%= mediaID %></span>
                               </div>
                            </div>
                            <!-- Name of the media in a clickable div -->
                            <div class="row">
                               <div class="col-md-auto">                                    
                                   <div class="mb-2 p-3 <%= textBG %>" style="font-weight:bold; cursor: grab;" 
                                        title="Click to (un)select" onclick="toggleSelectMedia('<%= mediaID %>')"
                                        id = "title-div-<%= mediaID %>">
                                       <% if (mediaType==Media.MEDIA_TYPE.VIDEO){%>
                                            <i class="fa-solid fa-film"></i>
                                       <% } else { %> 
                                            <i class="fa-solid fa-volume"></i>
                                       <% }%> 
                                       <%= filename %>
                                       
                                   </div> 
                               </div>
                            </div>
                            <div class="row" style="display: block;">
                                <div class="col-md-auto">                                    
                                    <div class="table-responsive" style="max-height: 200px; overflow-y: auto;">
                                        <table class="table table-striped table-sm" 
                                           id="info-table-<%= mediaID %>"
                                           style="white-space:nowrap; font-size: 9pt;">
                                            <% for (String[] metadataPair : metadata){ %>
                                            <tr>
                                                <td><b><%= metadataPair[0] %></b></td>
                                                <td><%= metadataPair[1] %></td>
                                            </tr>
                                        <% } %>
                                        </table>
                                    </div>
                                    
                                    <!-- **************************************************** -->
                                    <!-- *** Buttons to do things with this media         *** -->    
                                    <!-- **************************************************** -->
                                    <div class="btn-group m-3" role="group"> 
                                         <button id="toggle-player-btn-<%= mediaID %>" class="btn btn-outline-primary btn-sm"
                                            title="Click to show/hide player" onclick="toggleShowPlayer('<%= mediaID %>')">
                                          <i class="fa-solid fa-eye"></i>
                                        </button>                                    

                                        <button id="toggle-mute-btn-<%= mediaID %>" class="btn btn-outline-primary btn-sm"
                                            title="Click to (un)mute player" onclick="toggleMuteMedia('<%= mediaID %>')">
                                          <i class="fa-solid fa-volume-slash"></i>
                                        </button>              
                                            
                                        <button id="move-up-down-btn-<%= mediaID %>" class="btn btn-outline-primary btn-sm"
                                            title="Click to move to/from top row" onclick="moveUpDown('<%= mediaID %>')">
                                            <i class="fa-solid fa-up-to-line"></i>                                            
                                        </button>              
                                            
                                       <% if (mediaType==Media.MEDIA_TYPE.VIDEO){%>
                                        <button id="still-series-btn-<%= mediaID %>" class="btn btn-outline-primary btn-sm"
                                            title="Get a series of video stills" onclick="showStillImages('<%= mediaID %>')">
                                            <i class="fa-solid fa-images"></i>                                            
                                        </button>              
                                       <% }%> 
                                            
                                    </div>
                                </div>
                                       
                                <% if (mediaType==Media.MEDIA_TYPE.VIDEO){%>
                                    <div id="still-container-<%= mediaID %>"></div>
                                <% }%> 
                                       

                                <!-- **************************************************** -->
                                <!-- *** The player for this media, hidden initially  *** -->    
                                <!-- **************************************************** -->
                                <div class="col-md-auto">
                                    <div id="player-div-<%= mediaID %>" style="display:none;">
                                       <% if (mediaType==Media.MEDIA_TYPE.VIDEO){%>
                                            <video id="mediaPlayer-<%= mediaID %>" 
                                                   width="480" height="270" controls="controls"
                                                   muted="muted"
                                                   preload="none"
                                                   data-muted="true"
                                                   data-src="<%=media.getURL()%>"
                                                   name="syncMediaPlayer"
                                                   >
                                                <!-- <source src="<%=media.getURL()%>" type="video/mp4"> -->
                                            </video>                                  
                                       <% } else { %> 
                                            <audio id="mediaPlayer-<%= mediaID %>" 
                                                   width="480" height="80" controls="controls"
                                                   muted="muted"
                                                   preload="none"
                                                   data-muted="true"
                                                   data-src="<%=media.getURL()%>"
                                                   name="syncMediaPlayer"
                                                   >
                                                <!-- <source src="<%=media.getURL()%>" type="audio/wav"> -->
                                            </audio>                                  
                                       <% }%> 
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
        <%@include file="../WEB-INF/jspf/imageModal.jspf" %>
        
    </body>
</html>
