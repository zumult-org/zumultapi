/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var skipUpdate = false;

function getMasterMediaPlayer(){
	if (document.getElementById('master-video')){  
		return $("#master-video")[0];
	}
	return $("#master-audio")[0];
}

function getCurrentPosition(){
    return getMasterMediaPlayer().currentTime;
}

function getVideoImage(videoID){
    var WEBSERVICE = "/ZumultDataServlet?command=getVideoImage";
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
    //preventMediaContextMenu();
    registerMediaListener();
    addOnDblClicks();
    console.log("media initialised");
}

/* this will give every token a double click handler so that it can initialise playback */
function addOnDblClicks(){
    $("span.token").dblclick(function(e) {
        e.preventDefault();
        clearSelection();        
        jump(this.dataset.start);
    });       
    console.log("double clicks added");
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
    
    if (document.getElementById('video-1')){        
        player.addEventListener('seeking', seek, true);
        player.addEventListener('seeked', seek, true);

        player.addEventListener('play', function () {
            document.getElementById('video-1').play();
          }, true);

        player.addEventListener('pause', function () {
            document.getElementById('video-1').pause();
        }, true);
    }
    
    if (document.getElementById('video-2')){        
        player.addEventListener('play', function () {
            document.getElementById('video-2').play();
          }, true);

        player.addEventListener('pause', function () {
            document.getElementById('video-2').pause();
        }, true);
    }
}

function updateSVGDummy(time){

   const circle = document.getElementById("circle1");

    var traceElement = document.createElementNS("http://www.w3.org/2000/svg", 'circle'); //Create a path in SVG's namespace
    traceElement.setAttribute("r","1"); //Set path's data
    traceElement.setAttribute("cx", circle.getAttribute("cx"));
    traceElement.setAttribute("cy", circle.getAttribute("cy"));
    traceElement.style.fill="gray";

    document.getElementById("measurement").appendChild(traceElement);
    

   circle.setAttribute("cx", time*10);  
   const randomNumber = Math.random();
   if (randomNumber<0.15){
       circle.setAttribute("cy", Math.max(0, +circle.getAttribute("cy") - 10));        
       circle.style.fill="yellow";
   } else if (randomNumber>0.85){
       circle.setAttribute("cy", Math.min(200, +circle.getAttribute("cy") + 10));        
       circle.style.fill="red";
   } else {
       if (randomNumber<0.5){
           circle.style.fill="green";
       } else if (randomNumber>0.5) {
           circle.style.fill="blue";           
       }
   }
   
   
   const randomNumber2 = Math.random();
   const rect = document.getElementById("rect1");
   rect.setAttribute("x", time*10);  
   if (randomNumber2<0.25){
       rect.setAttribute("y", Math.max(0, +rect.getAttribute("y") - 10));        
   } else if (randomNumber2>0.75){
       rect.setAttribute("y", Math.min(200, +rect.getAttribute("y") + 10));        
   } 

   const line1 = document.getElementById("line1");
   line1.setAttribute("y1", circle.getAttribute("cy"));        
   line1.setAttribute("x1", circle.getAttribute("cx"));        
   line1.setAttribute("y2", rect.getAttribute("y"));        
   line1.setAttribute("x2", rect.getAttribute("x"));        

}

function updateTime(){
    var player = getMasterMediaPlayer();
    var elapsedTime = player.currentTime;                        
    var scrollOnce = false;
    
    //updateSVGDummy(elapsedTime);
    
    
    if (!skipUpdate){
        updateSVGCursor(elapsedTime);
    }
    
    skipUpdate = false;

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
        var newX = (time - startTime) * xPerSecond;
        cursor.setAttribute('x1', newX);
        cursor.setAttribute('x2', newX);
        
        cursor.scrollIntoView({inline: "center"});
    }
}



function moveSVGCursor(evt){
    /*var cursor = document.getElementById('svg_cursor')    
    pt.x = evt.clientX;

    // The cursor point, translated into svg coordinates
    var cursorpt =  pt.matrixTransform(svg.getScreenCTM().inverse());
    cursor.setAttribute('x1', cursorpt.x);
    cursor.setAttribute('x2', cursorpt.x);*/
    
}

function setSVGCursor(evt){
    var cursor = document.getElementById('svg_cursor')    
    pt.x = evt.clientX;
    var cursorpt =  pt.matrixTransform(svg.getScreenCTM().inverse());

    skipUpdate = true;
    var player = getMasterMediaPlayer();
    let newTime = startTime + (cursorpt.x / xPerSecond );
    player.currentTime=newTime;

    // The cursor point, translated into svg coordinates
    cursor.setAttribute('x1', cursorpt.x);
    cursor.setAttribute('x2', cursorpt.x);
    
}

function seek() {
  const time = getMasterMediaPlayer().currentTime;
  document.getElementById('video-1').currentTime = time;
  document.getElementById('video-2').currentTime = time;
  updateSVGDummy(time);
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




   



