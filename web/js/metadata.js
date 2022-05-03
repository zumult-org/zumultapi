/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//var BASE_URL = "http://zumult.ids-mannheim.de/ProtoZumult"; // for deploying on zumult.ids-mannheim.de
//var BASE_URL = "http://localhost:8080/DGDRESTTest"; // for deploying on localhost

function openMetadataModal(speechEventID){
    var eventID = speechEventID.substring(0,12);
    $.get(BASE_URL + "/ZumultDataServlet?command=getSpeechEventMetadataHTML&speechEventID=" + speechEventID, function( data ) {
            $("#metadata-body").html(data);
    });
    $.get(BASE_URL + "/ZumultDataServlet?command=getEventMetadataTitle&eventID=" + eventID, function( data ) {
            $("#metadata-title").html(data);
    });
    $("#metadata-transcript-link").attr("href", "zuViel.jsp?transcriptID=" + speechEventID + "_T_01");
    $("#metadata-transcript-dgd-link").attr("href", "https://dgd.ids-mannheim.de/DGD2Web/ExternalAccessServlet?command=displayData&id=" + eventID + "_SE_01_T_01");
}

function handleCollectionRadioChange(){
    $("#newCollectionTextField").toggle();
    $("#existingCollectionDropdown").toggle();
    $("#newCollectionTextArea").toggle();
}


