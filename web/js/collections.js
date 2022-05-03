/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var collectionSpeechEventID = "";
var collectionTranscriptID = "";
var collectionMediaID = "";
var collectionStartSelection = "";
var collectionEndSelection = "";
var collectionPosition = "";
var collectionItemType = "";


function clearAddCollection(){
    collectionSpeechEventID = "";
    collectionTranscriptID = "";
    collectionMediaID = "";
    collectionStartSelection = "";
    collectionEndSelection = "";
    collectionPosition = "";
    collectionItemType = "";    
}

function setAddCollection(){
    $('#collectionItemType').html('Item type=' + collectionItemType);

    $('#collectionSpeechEventID').html('speechEventID=' + collectionSpeechEventID);
    if (collectionSpeechEventID.length>0){$('#collectionSpeechEventID').show();} else {$('#collectionSpeechEventID').hide();}

    $('#collectionTranscriptID').html('transcriptID=' + collectionTranscriptID);
    if (collectionTranscriptID.length>0){$('#collectionTranscriptID').show();} else {$('#collectionTranscriptID').hide();}
    
    $('#collectionMediaID').html('mediaID=' + collectionMediaID);
    if (collectionMediaID.length>0){$('#collectionMediaID').show();} else {$('#collectionMediaID').hide();}
    
    $('#collectionStartSelection').html('startSelection=' + collectionStartSelection);
    if (collectionStartSelection.length>0){$('#collectionStartSelection').show();} else {$('#collectionStartSelection').hide();}

    $('#collectionPosition').html('position=' + collectionPosition);
    if (collectionPosition.length>0){$('#collectionPosition').show();} else {$('#collectionPosition').hide();}

    $('#collectionEndSelection').html('endSelection=' + collectionEndSelection);
    if (collectionEndSelection.length>0){$('#collectionEndSelection').show();} else {$('#collectionEndSelection').hide();}

}

function addVideoImageToCollection(videoID){
    
    clearAddCollection();
    collectionMediaID = videoID;
    collectionPosition = getCurrentPosition().toString();
    collectionItemType = "VideoImage";    
    setAddCollection();
    
    $('#collectionsModal').modal('show');
}

function addSelectionToCollection(){
    
    clearAddCollection();
    collectionTranscriptID = transcriptID;
    collectionStartSelection = startSelection;
    collectionEndSelection = endSelection;
    collectionItemType = "TranscriptSelection";    
    setAddCollection();

    $('#collectionsModal').modal('show');
}

function addSpeechEventToCollection(){

    clearAddCollection();
    collectionSpeechEventID = speechEventID;
    collectionItemType = "SpeechEvent";    
    setAddCollection();
    
    
    $('#collectionsModal').modal('show');    
}

function addTranscriptToCollection(){

    clearAddCollection();
    collectionTranscriptID = transcriptID;
    collectionItemType = "Transcript";    
    setAddCollection();

    $('#collectionsModal').modal('show');
}

function hideCollectionsModal(){
    $('#collectionsModal').modal('hide');        
}



