<%-- 
    Document   : intersectwordlist
    Created on : 06.11.2018, 09:32:17
    Author     : Thomas_Schmidt
--%>

<%@page import="org.zumult.backend.Configuration"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.io.File"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Collections"%>
<%@page import="org.zumult.objects.implementations.DefaultTokenList"%>
<%@page import="org.zumult.objects.TokenList"%>
<%@page import="org.zumult.io.XMLReader"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.objects.Transcript"%>
<%@page import="org.zumult.io.Constants"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
    <%
       BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
       String corpusID = request.getParameter("corpusID");
       String speechEventID = request.getParameter("speechEventID");
       String thisEventID = request.getParameter("speechEventID").substring(0, 12);
       String wordlistID = request.getParameter("wordlistID");
       final String zumultApiBaseURL = Configuration.getRestAPIBaseURL();
       if (corpusID==null) {corpusID = "FOLK";}
       if (speechEventID==null) {speechEventID = corpusID + "_E_00001_SE_01";}
       if (wordlistID==null) {wordlistID = "HERDER_1000";}
//       int endTime = 0;
       
       // redirect this to new page
       String redirectLocation = "./zuViel.jsp";
       redirectLocation+="?transcriptID=" + speechEventID + "_T_01";
       redirectLocation+="&wordlistID=" + wordlistID;
       response.sendRedirect(redirectLocation);
    %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Wordlist intersection</title>
        
        <script type="text/javascript">
            var currentStart = 0.0; // -> updateTime
            var increment = 0.0; // -> getMoreTranscript
            var checkboxes = document.getElementsByClassName("highlight-checkbox");


            function reloadWithParameters(){
                var corpusID = document.getElementById("corpusSelection").options[document.getElementById("corpusSelection").selectedIndex].value;
                var speechEventID = document.getElementById("speechEventSelection").options[document.getElementById("speechEventSelection").selectedIndex].value;
                var wordlistID = document.getElementById("wordlistSelection").options[document.getElementById("wordlistSelection").selectedIndex].value;
                window.location.search = "corpusID=" + corpusID 
                        + "&speechEventID="  + speechEventID
                        + "&wordlistID="  + wordlistID;
                reload();
            }
            
            
            function jump(time, player) {
                player.currentTime = time;
                player.play();
            }

            function stop(player) {
                player.pause();
            }
            
            function highlightToken(container, isChecked, dataAttr, highlightClass) {
                var tokensList = container.querySelectorAll("span.token[" + dataAttr + "='true']");
                for (var i = 0; i < tokensList.length; i++) {
                    isChecked
                        ? tokensList[i].classList.add(highlightClass)
                        : tokensList[i].classList.remove(highlightClass);
                }
            }

//            function highlight(id) {
//                var elements = document.getElementsByName(id);
//                for (var i = 0; i < elements.length; i++) {
//                    element = elements[i];
//                    element.style.background = 'rgb(135,206,250)';
//                }
//            }
//
//            function lowlight(id) {
//                var elements = document.getElementsByName(id);
//                for (var i = 0; i < elements.length; i++) {
//                    element = elements[i];
//                    element.style.background = 'inherit';
//                }
//            }

            function registerAudioListener(audioPlayer) {
                audioPlayer.addEventListener("timeupdate", function() { updateTime(this);}, false);
                audioPlayer.addEventListener("onpause", function() {updateTime(this);}, false);
            }

            function updateTime(player) {
                var elapsedTime = player.currentTime;
                var playerID = player.id;
                document.getElementById('current_position_' + playerID).innerHTML = formatSeconds(currentStart + elapsedTime);
                //Math.floor((currentStart + elapsedTime)*100)/100;

                var elements = document.getElementsByClassName('token');
                for (var i = 0; i < elements.length; i++) {
                    var element = elements[i];
                    var start = element.getAttribute('data-start');
                    var end = element.getAttribute('data-end');
                    if ((start !== null) && (end !== null)) {
                        if ((!player.paused) && (start < currentStart + elapsedTime) && (end > currentStart + elapsedTime)) {
                            element.style.textDecoration = 'underline blue';
                        //element.scrollIntoView(false);
                        } else {
                            element.style.textDecoration = 'none';
                        }
                    }
                }
            }

            function formatSeconds(timeInSeconds) {
                var dt = new Date(timeInSeconds * 1000);
                var hours = dt.getHours() - 1;
                var minutes = dt.getMinutes();
                var seconds = dt.getSeconds();
                var milliseconds = dt.getMilliseconds() + '';
                // the above dt.get...() functions return a single digit
                // so I prepend the zero here when needed
                if (hours < 10)
                        hours = '0' + hours;
                if (minutes < 10)
                        minutes = '0' + minutes;
                if (seconds < 10)
                        seconds = '0' + seconds;
                return hours + ":" + minutes + ":" + seconds + "." + milliseconds.substring(0, 2);
            }
            
            function handleCheckboxes(element) {
                var isChecked = element.checked;
                var dataAttr;
                var highlightClass;
                var containerID = element.dataset.container;
                var container = document.getElementById(containerID);
                switch (element.dataset.ref) {
                    case "highlight_normalised_checkbox":
                        dataAttr = "data-is-trans";
                        highlightClass = "yellow-background";
                        break;
                    case "highlight_wordlist_checkbox":
                        dataAttr = "data-is-in-wordlist";
                        highlightClass = "red-text";
                        break;
                    default:
                }
                highlightToken(container, isChecked, dataAttr, highlightClass);
            };
            
            // get list of speechevents per corpus
            function getSpeechEvents(corpus_ID) {
                return new Promise(function(resolve, reject) {
                    var xhttp = new XMLHttpRequest();
                    xhttp.open("GET", "../getSpeechEvents?corpusID=" + corpus_ID, true);

                    xhttp.onload = function() {
                        if (this.readyState == 4 && this.status == 200) {
                            resolve(this.responseText);
                        } else {
                            reject(Error(this.statusText));
                        }
                    };
                    xhttp.onerror = function() { reject(Error("Network Error")); };
                    xhttp.send();
                });                
            }
            // this one could be more efficient as it does not load the whole transcript from the start, but the parts overlap at the beginning
//            function getMoreTranscript(transcriptID, endTime, wordlistID) {
//                var xhttp = new XMLHttpRequest();
//                xhttp.onreadystatechange = function() {
//                    if (this.readyState == 4 && this.status == 200) {
//                        document.getElementById(transcriptID +"_excerpt").insertAdjacentHTML("beforeend", this.responseText);
//                        for (var i = 0; i < checkboxes.length; i++) {
//                            handleCheckboxes(checkboxes[i]);
//                        }
//                    }
//                }
//                xhttp.open("GET", "../getMoreTranscript?endTime=" + endTime + "&transcriptID=" + transcriptID + "&wordlistID=" + wordlistID, true);
//                xhttp.send();
//            }
            function getMoreTranscript(transcriptID, endTime, wordlistID) {
                increment += 60;
                var currentTime = endTime + increment;

                var xhttp = new XMLHttpRequest();
                xhttp.onreadystatechange = function() {
                    if (this.readyState == 4 && this.status == 200) {
                        document.getElementById(transcriptID +"_excerpt").innerHTML = this.responseText;
                        for (var i = 0; i < checkboxes.length; i++) {
                            handleCheckboxes(checkboxes[i]);
                        }
                    }
                }
                xhttp.open("GET", "../getMoreTranscript?endTime=" + currentTime + "&transcriptID=" + transcriptID + "&wordlistID=" + wordlistID, true);
                xhttp.send();
            }
            
        </script>
        
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
        <script src='https://kit.fontawesome.com/a076d05399.js'></script>
        
        <style type="text/css">
            div#main {
                display: flex;
            }

            div#col_right {
                flex: 15%;
            }            
            div#center {
                flex: 65%;
            }
            div#col_left {
                flex : 20%;
                margin-left : 20px;
                display : flex;
            }
            div.transcriptContainer {
                margin-bottom: 20px;
            }
            div.transcriptOuter {
                /*border-radius: 25px;*/
                border-radius: 3px;
                border: 1px solid gray;  
                max-height: 400px;
                overflow: auto;
                padding: 0px 15px 15px 15px;
            }
            div.header-bar {
                /*display: none;*/
                background: white;
                padding-top: 15px;
                border-bottom: .5px solid black;
            }
            div.header-bar div.header-group {
                display: flex;
            }
            div.header-bar span.containerTitle {
                flex: 50%;
            }
            div.transcriptOuter div.header-bar span.current-position {
                flex: 40%;
            }
            div.header-bar a.settingsGroup {
                flex: 10%;
                text-align: right;
            }
            button.moreTranscriptButton {
                margin: auto;
                display: block;
            }
            .containerTitle {
                /*padding: 15px 0;*/
                font-size: 1em;
                font-weight: bold;
/*                border-top: 1px solid black;
                border-bottom: 1px solid black;
                background: lightGray;*/
            }
            div.wordlistOuter {
                /*border-radius: 25px;*/
                border-radius: 3px;
                border: 1px solid gray;  
                max-height: 800px;
                overflow: auto;
                padding: 0px 15px 15px 15px;
                margin-right: 20px; 
                flex : 45%;
            }
            span.speaker {
                font-weight:bold;
            }
            span.pause, span.desc {
                font-size:smaller;
                color:gray;
            }
            .yellow-background {
                background-color: yellow;
            }
            .red-text {
                color: red;
            }
            div#main table.table td {
                border-top: none;
            }
        </style>
        <%@include file="../WEB-INF/jspf/matomoTracking.jspf" %>                
    </head>
    <body>
        <h1>Word list intersection</h1>
        <div id="main">
            <div id="col_right">
                <form action="javascript:reloadWithParameters()">
                    <table class="table">
                        <tr>
                            <td>Corpus: </td>
                            <td>
                                <select id="corpusSelection">
                                    <%
                                        IDList corpora = backendInterface.getCorpora();
                                        for (String corpus : corpora){
                                    %>
                                    <option value="<%=corpus%>" <% 
                                            if (corpus.equals(corpusID)) {%>
                                            selected="selected"    
                                            <%}%>><%=corpus%></option>
                                    <%
                                        }                        
                                    %>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>Speech Event </td>
                            <td>
                                <select id="speechEventSelection">
                                    <%
                                        for (String eventID : backendInterface.getEvents4Corpus(corpusID)){
                                            for (String speechEvent : backendInterface.getSpeechEvents4Event(eventID)){ %>
                                                    <option value="<%=speechEvent%>" <% 
                                                            if (speechEvent.equals(speechEventID)) {%>
                                                            selected="selected"    
                                                            <%}%>><%=speechEvent%></option>

                                    <%      }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>Wordlist: </td>
                            <td>
                                <select id="wordlistSelection">
                                    <%
                                        for (String wordlist : Constants.LEIPZIG_WORDLISTS){
                                    %>
                                    <option value="<%=wordlist%>" <% 
                                            if (wordlist.equals(wordlistID)) {%>
                                            selected="selected"    
                                            <%}%>><%=wordlist%></option>
                                    <%
                                        }                        
                                    %>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><input type="submit" value="Go!"/></td>
                        </tr>
                    </table>
                </form>
            </div>
            <div id="center">
                <%
                    IDList transcripts = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                    TokenList lemmaList4SpeechEvent = new DefaultTokenList("lemma");
                    String pathToWordList = new File(getServletContext().getRealPath("/data/" + wordlistID + ".xml"))
                            .toURI().toString();
                    
                    for (String transcriptID : transcripts){
                        Transcript transcript = backendInterface.getTranscript(transcriptID); 
                        double startTime = transcript.getStartTime();
                        double endTime = transcript.getEndTime() > (startTime + 60.0)
                                ? startTime + 60.0
                                : transcript.getEndTime();
                        Transcript partTranscript = transcript.getPart(startTime, endTime, true);
                        String partTranscriptXML = partTranscript.toXML();
                        String audioID = backendInterface.getAudios4Transcript(transcriptID).get(0);
                        System.out.println(audioID);
                        
                        String[][] xslParameters = {
                                {"TOKEN_LIST_URL", pathToWordList},
                            };
                                                
                        String partTranscriptHTML = 
                                new IOHelper().applyInternalStylesheetToString(
                                        Constants.ISOTEI2HTML_HIGHLIGHT_TOKENS_STYLESHEET, 
                                        partTranscriptXML,
                                        xslParameters);
                        // ... and get its lemma list, applying the filter defined above
                        TokenList lemmaList4Transcript = transcript.getTokenList("lemma");
                        // merge this transcript's lemma list with the lemmalist for the entire speech event
                        lemmaList4SpeechEvent = lemmaList4SpeechEvent.merge(lemmaList4Transcript);
                %>
                        <div class="transcriptContainer">
                            <div class="transcriptOuter">
                                <div id="<%= transcriptID %>_container">
                                    <div class="header-bar sticky-top">
                                        <div class="header-group">
                                            <span class="containerTitle">
                                                <%= transcriptID %>
                                            </span>
                                            
                                            <span class="current-position">
                                                <a href="#"><i id="play_pause_audio_player_<%= transcriptID %>" class="fas fa-play play-pause" data-audioplayerid="audio_player_<%= transcriptID %>"audio-player-id></i></a>
                                                <span id="current_position_audio_player_<%= transcriptID %>"></span>
                                            </span>
                                            
                                            <div class="settingsGroup">
                                                <a class="settings" data-toggle="popover" data-container="body" data-html="true" href="#">
                                                    <i class="fas fa-cog"></i>
                                                </a>
                                                <div class="d-none popover-content-wrapper">
                                                    <div class="popover-content">
                                                        <div class="form-check transcript-checkboxes">
                                                            <div class="custom-control custom-checkbox">
                                                                <input class="custom-control-input highlight-checkbox" type="checkbox" value="" id="highlightNormalisedCheckbox_<%= transcriptID %>" data-container="<%= transcriptID %>_container" data-ref="highlight_normalised_checkbox" />
                                                                <label class="custom-control-label" for="highlightNormalisedCheckbox_<%= transcriptID %>">
                                                                  Transkribierte Formen hervorheben, die sich von zugehörigen normalisierten Formen unterscheiden
                                                                </label>
                                                            </div>
                                                            <div class="custom-control custom-checkbox">
                                                              <input class="custom-control-input highlight-checkbox" type="checkbox" value="" id="highlightWordlistCheckbox_<%= transcriptID %>" data-container="<%= transcriptID %>_container" data-ref="highlight_wordlist_checkbox" />
                                                              <label class="custom-control-label" for="highlightWordlistCheckbox_<%= transcriptID %>" >
                                                                Formen hervorheben, die in der ausgewählten Referenz-Wortliste auftauchen
                                                              </label>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>



                                            <div class="audioPlayerContainer">
                                                <!--<audio preload="auto" class="audio-player" id="audio_player_<%= transcriptID %>" type="audio/mp3" src="<%= zumultApiBaseURL %>/media/<%= corpusID %>/<%= thisEventID %>/<%= speechEventID %>/<%= audioID %>_DF_01.mp3">-->
                                                <% 
                                                    String audioURL = backendInterface.getMedia(audioID).getURL();
                                                %>
                                                <audio preload="auto" class="audio-player" id="audio_player_<%= transcriptID %>" type="audio/mp3" src="<%= zumultApiBaseURL %>/media/<%= corpusID %>/<%= thisEventID %>/<%= speechEventID %>/<%= audioID %>_DF_01.mp3">
                                                </audio>
                                                <!-- <audio preload="auto" class="audio-player" id="audio_player_<%= transcriptID %>" type="audio/mp3" 
                                                       src="<%= audioURL %>">
                                                </audio>-->
                                            </div>
                                                
                                        </div>
                                    </div>
                                    <div id="<%= transcriptID %>_excerpt">
                                        <%= partTranscriptHTML %>
                                    </div>
                                </div>
                            </div>
                            <button type="button" class="btn btn-primary moreTranscriptButton"
                                onclick="getMoreTranscript('<%= transcriptID %>', <%= (int) endTime %>, '<%= wordlistID %>')">
                                +60 s
                            </button>
                        </div>
                    <% }
                %>

            </div>
            <div id="col_left">
                <%
                    String pathToInternalResource = "/data/" + wordlistID + ".xml";
                    TokenList selectedWordList = XMLReader.readTokenListFromInternalResource(pathToInternalResource);
                %>
                <div class="wordlistOuter">
                    <div id="<%= wordlistID %>_container">
                        <div class="header-bar sticky-top">
                            <div class="header-group">
                                <span class="containerTitle">
                                    <%= wordlistID %>
                                </span>
                                <div class="settingsGroup">
                                    <a class="settings" data-toggle="popover" data-container="body" data-html="true" href="#">
                                        <i class="fas fa-cog"></i>
                                    </a>
                                    <div class="d-none popover-content-wrapper">
                                        <div class="popover-content">
                                            <div class="form-check transcript-checkboxes">
                                                <div class="custom-control custom-checkbox">
                                                  <input class="custom-control-input highlight-checkbox" type="checkbox" value="" id="highlightWordlistCheckbox_<%= wordlistID %>" data-container="<%= wordlistID %>_container" data-ref="highlight_wordlist_checkbox" />
                                                  <label class="custom-control-label" for="highlightWordlistCheckbox_<%= wordlistID %>" >
                                                    Formen hervorheben, die im Transkript auftauchen
                                                  </label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    <%
                        for (String token : new TreeMap<>(selectedWordList).keySet()){
                            boolean contains = lemmaList4SpeechEvent.keySet().contains(token);
                            if (contains){ %>
                                <span class="token" data-is-in-wordlist="true">
                    <%      } else { %>
                                <span class="token" data-is-in-wordlist="false">
                    <%      }      %>
                        <%= token %>
                        </span>
                        <br/>
                    <%
                        }
                    %>
                    </div>
                </div>
                <div class="wordlistOuter">
                    <div id="<%= speechEventID %>_container">
                        <div class="header-bar sticky-top">
                            <div class="header-group">
                                <span class="containerTitle">
                                    <%= speechEventID %>
                                </span>
                                <div class="settingsGroup">
                                    <a class="settings" data-toggle="popover" data-container="body" data-html="true" href="#">
                                        <i class="fas fa-cog"></i>
                                    </a>
                                    <div class="d-none popover-content-wrapper">
                                        <div class="popover-content">
                                            <div class="form-check transcript-checkboxes">
                                                <div class="custom-control custom-checkbox">
                                                  <input class="custom-control-input highlight-checkbox" type="checkbox" value="" id="highlightWordlistCheckbox_<%= speechEventID %>" data-container="<%= speechEventID %>_container" data-ref="highlight_wordlist_checkbox" />
                                                  <label class="custom-control-label" for="highlightWordlistCheckbox_<%= speechEventID %>" >
                                                    Formen hervorheben, die in der ausgewählten Referenz-Wortliste auftauchen
                                                  </label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    <%
                        for (String token : new TreeMap<>(lemmaList4SpeechEvent).keySet()){
                            boolean contains = selectedWordList.keySet().contains(token);
                            if (contains){ %>
                                <span class="token" data-is-in-wordlist="true">
                    <%      } else { %>
                                <span class="token" data-is-in-wordlist="false">
                    <%      }      %>
                        <%= token %>
                        </span>
                        <br/>
                    <%
                        }
                    %>
                    </div>
                </div>
            </div>
        </div>
        <script type="text/javascript">
            window.addEventListener("DOMContentLoaded", function() {
                var transcriptOuterElements = document.querySelectorAll("div.transcriptContainer");
                var wordlistOuterElements = document.querySelectorAll("div.wordlistOuter");
                var audioPlayers = document.querySelectorAll("audio.audio-player");
                var playPauseButtons = document.getElementsByClassName("play-pause");
                var checkboxes = document.querySelectorAll("input.highlight-checkbox");
                
                // transcript containers
                for (var i = 0; i < transcriptOuterElements.length; i++) {
                    var audioPlayer = transcriptOuterElements[i].querySelector("div.audioPlayerContainer audio");
                    var settingsButton = transcriptOuterElements[i].querySelector("a.settings");
                    var popoverContent = transcriptOuterElements[i].querySelector("div.popover-content");

                    registerAudioListener(audioPlayer);
                    
                    settingsButton.onclick = function(e) { e.preventDefault(); };
                    $(settingsButton).popover({
                        html: true,
                        content: popoverContent,
                        placement: "bottom"
                    });
                        
                    // on doubleclicking inside a transcript area
                    transcriptOuterElements[i].ondblclick = function(e) {
                        var thisAudioPlayer = this.querySelector("div.audioPlayerContainer audio");
                        this.querySelector("i.play-pause").classList.remove("fa-play");
                        this.querySelector("i.play-pause").classList.add("fa-pause");

                        if (e.target.classList.contains("token")) jump(+e.target.dataset.start, thisAudioPlayer);
                    };
                }
                // wordlist containers
                for (var i = 0; i < wordlistOuterElements.length; i++) {
                    var settingsButton = wordlistOuterElements[i].querySelector("a.settings");
                    var popoverContent = wordlistOuterElements[i].querySelector("div.popover-content");
                    
                    settingsButton.onclick = function(e) { e.preventDefault(); };
                    $(settingsButton).popover({
                        html: true,
                        content: popoverContent,
                        placement: "bottom"
                    });                        
                }
                // on clicking on the document except play/pause
                document.onclick = function(e) {
                    if (!e.target.classList.contains("play-pause")) {
                        for (var i = 0; i < audioPlayers.length; i++) {
                            stop(audioPlayers[i]) ;
                        }
                        for (var i = 0; i < playPauseButtons.length; i++) {
                            playPauseButtons[i].classList.remove("fa-pause");
                            playPauseButtons[i].classList.add("fa-play");
                        }
                    }
                }
                // on clicking play/pause buttons
                for (var i = 0; i < playPauseButtons.length; i++) {
                    playPauseButtons[i].onclick = function(e) {
                        e.preventDefault();
                        var playerID = this.dataset.audioplayerid;
                        var player = document.getElementById(playerID);
                        if (player.paused) {
                            player.play();
                            this.classList.remove("fa-play")
                            this.classList.add("fa-pause")
                        } else {
                            player.pause();
                            this.classList.remove("fa-pause")
                            this.classList.add("fa-play")
                        }
                    }
                }
                
                // handle checkboxes
                for (var i = 0; i < checkboxes.length; i++) {
                    checkboxes[i].onchange = function(e) {
                        handleCheckboxes(e.target);
                    }
                }
                
                // update list of speechevents on corpus selection
                document.getElementById("corpusSelection").onchange = function() {
                    var selectedCorpus = this.value;
                    getSpeechEvents(selectedCorpus).then(function(response) {
                        var speechEvents = response.trim().split(" ");
                        var speechEventSelection = document.getElementById("speechEventSelection");
                        speechEventSelection.innerHTML = "";
                        speechEvents.forEach( function(speechEventID) {
                            var option = "<option value='" + speechEventID + "'>" + speechEventID + "</option>"
                            speechEventSelection.insertAdjacentHTML("beforeend", option); 
                        });
                    }, function(error) {
                        console.log("Failed", error)
                    });
                }
            }, false);
        </script>
        <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js" integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>
    </body>
</html>
