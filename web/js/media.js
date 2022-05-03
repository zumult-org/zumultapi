/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function getMasterMediaPlayer(){
    return $("#masterMediaPlayer")[0];
}

function getCurrentPosition(){
    return getMasterMediaPlayer().currentTime;
}

function getVideoImage(videoID){
    var WEBSERVICE = "/ZumultDataServlet?command=getVideoImage"
    var PARAMETERS =    "&videoID=" + videoID
                     +  "&position=" + getCurrentPosition();
    $.get(BASE_URL + WEBSERVICE + PARAMETERS, function( data ) {
            var downloadURL = "../downloads/" + $(data).find("file").text();
            //document.getElementById("secretIFrame").setAttribute("src", downloadURL);
            window.open(downloadURL);
        }
    );       
}

function jump(time){
    var player = getMasterMediaPlayer();
    player.currentTime=time;
    player.play();
}	

function frameForward(){
    var player = getMasterMediaPlayer();
    player.pause();
    player.currentTime = player.currentTime + 0.04;
}

function frameBackward(){
    var player = getMasterMediaPlayer();
    player.pause();
    player.currentTime = player.currentTime - 0.04;
}


function stop(){
    getMasterMediaPlayer().pause();
}

function preventMediaContextMenu() {
     var player = getMasterMediaPlayer();
     player.addEventListener('contextmenu', function(e) {
          e.preventDefault();
          return false;
     });
     if (document.getElementById('secondaryVideoPlayer')){        
         document.getElementById('secondaryVideoPlayer').addEventListener('contextmenu', function(e) { 
            e.preventDefault();
            return false; 
        });
    }
     
}

function initialiseMedia(){
    preventMediaContextMenu();
    registerMediaListener();
    addOnDblClicks();
}

/* this will give every token a double click handler so that it can initialise playback */
function addOnDblClicks(){
    $("span.token").dblclick(function(e) {
        e.preventDefault();
        clearSelection();        
        jump(this.dataset.start);
    });       
}

function clearSelection() {
    if(document.selection && document.selection.empty) {
        document.selection.empty();
    } else if(window.getSelection) {
        var sel = window.getSelection();
        sel.removeAllRanges();
    }
}



function registerMediaListener(){
    var player = getMasterMediaPlayer();
       
    player.addEventListener("timeupdate", updateTime, true);                     
    player.addEventListener("onpause", updateTime, true);   
    player.addEventListener('seeking', scrollTranscript, true);
    player.addEventListener('seeked', scrollTranscript, true);
    //preventVideoContextMenu();
    
    if (document.getElementById('secondaryVideoPlayer')){        
        player.addEventListener('seeking', seek, true);
        player.addEventListener('seeked', seek, true);

        player.addEventListener('play', function () {
            document.getElementById('secondaryVideoPlayer').play();
          }, true);

        player.addEventListener('pause', function () {
            document.getElementById('secondaryVideoPlayer').pause();
        }, true);
    }
    
}

function updateTime(){
    var player = getMasterMediaPlayer();
    var elapsedTime = player.currentTime;                        
    var scrollOnce = false;
    
    updateSVGCursor(elapsedTime);

    // this is finding all the span elements (tokens) that are within the current time
    var spanElements = document.getElementsByTagName('span');
    for (var i = 0; i < spanElements.length; i++) {
        spanElement = spanElements[i];
        start = spanElement.getAttribute('data-start');
        end = spanElement.getAttribute('data-end');
        if ((!player.paused) && (start < elapsedTime) && (end > elapsedTime)){
            spanElement.classList.add("highlight-playback");            
            
            if (!scrollOnce){
                spanElement.scrollIntoView();
                window.scrollBy(0, -150);
                scrollOnce = true;
            }
            
        } else {
            spanElement.classList.remove("highlight-playback");            
        }
    }     
    
    var tableCursorElements = document.getElementsByClassName('tablerow_cursor');
    for (var i = 0; i < tableCursorElements.length; i++) {
        tableCursorElement = tableCursorElements[i];
        start = tableCursorElement.getAttribute('data-start');
        end = tableCursorElement.getAttribute('data-end');
        if ((!player.paused) && (start < elapsedTime) && (end > elapsedTime)){
            //tableCursorElement.style.border = "thick solid #0000FF";
            tableCursorElement.classList.add("highlight-playback");            
        } else {
            tableCursorElement.classList.remove("highlight-playback");            
        }
        
    }
    
    
}

function updateSVGCursor(time){
    var cursor = document.getElementById('svg_cursor')    
    if (cursor!==null){
        // one second is ten pixels? seems like
        var newX = (time - startTime) * 10;
        cursor.setAttribute('x1', newX);
        cursor.setAttribute('x2', newX);
        
        cursor.scrollIntoView({inline: "center"});
    }
}

function seek() {
  document.getElementById('secondaryVideoPlayer').currentTime = getMasterMediaPlayer().currentTime;
}

// this one reacts to user-initiated navigation in the audio or video
function scrollTranscript() {
    var player = getMasterMediaPlayer();
    var elements = document.getElementsByTagName('span');
    var scrollOnce = false;
    for (var i = 0; i < elements.length; i++) {
        element = elements[i];
        start = element.getAttribute('data-start');
        end = element.getAttribute('data-end');
        if ((start < player.currentTime) && (end > player.currentTime)){
            if (!scrollOnce){
                element.scrollIntoView();
                window.scrollBy(0, -150);
                scrollOnce = true;
            }            
        }
    }                        
}

function updatePlaybackSpeed(){
    var playbackSpeed = document.getElementById('playbackSpeedSlider').value;    
    var player = getMasterMediaPlayer();
    player.playbackRate = playbackSpeed / 100;
    if (document.getElementById('secondaryVideoPlayer')){        
        document.getElementById('secondaryVideoPlayer').playbackRate = playbackSpeed / 100;
    }
    document.getElementById('playbackspeedlabel2').textContent = playbackSpeed + "%";    
}


function changeSubtitleType(){
    var type = document.getElementById('subtitletype').value;
    var player = getMasterMediaPlayer();
    var transTrack = player.textTracks[0];
    var normTrack = player.textTracks[1];
    if (type==='none'){
        transTrack.mode = 'hidden';
        normTrack.mode = 'hidden';
    } else if (type==='trans'){
        transTrack.mode = 'showing';        
        normTrack.mode = 'hidden';        
    }  else if (type==='norm'){
        transTrack.mode = 'hidden';        
        normTrack.mode = 'showing';        
    } 
    
}




   



