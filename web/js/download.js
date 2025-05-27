/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function requestDownload(){
    
    $('#downloadSpinner').toggle();
    //alert("Download " + startSelection + " / " + endSelection);
    /* var WEBSERVICE = "/ZumultDataServlet?command=download"
    var PARAMETERS =    "&transcriptID=" + transcriptID
                     +  "&startSelection=" + startSelection
                     +  "&endSelection=" + endSelection;
    $.get(BASE_URL + WEBSERVICE + PARAMETERS, function( data ) {
            $('#downloadSpinner').toggle();
            $('#downloadModal').modal("toggle");
            var downloadURL = "../downloads/" + $(data).find("file").text();
            document.getElementById("secretIFrame").setAttribute("src", downloadURL);
        }
    ); */
    
    //$('#downloadStopperModal').modal('show');
    
    $.post(
            BASE_URL + "/ZumultDataServlet",
            { 
                command: 'download',
                transcriptID: transcriptID, 
                startSelection: startSelection,
                endSelection: endSelection,
                audioArchive : $('#audioArchive').prop('checked'),
                videoArchive : $('#videoArchive').prop('checked'),
                video2Archive : $('#video2Archive').prop('checked'),
                transcriptISO : $('#transcriptISO').prop('checked'),
                transcriptFLN : $('#transcriptFLN').prop('checked'),
                transcriptEXB : $('#transcriptEXB').prop('checked'),
                transcriptEAF : $('#transcriptEAF').prop('checked'),
                transcriptPraat : $('#transcriptPraat').prop('checked'),
                transcriptHTML : $('#transcriptHTML').prop('checked'),
                transcriptTXT : $('#transcriptTXT').prop('checked'),
                transcriptPartiturHTML : $('#transcriptPartiturHTML').prop('checked'),
                transcriptPartiturRTF : $('#transcriptPartiturRTF').prop('checked')
            },
            function( data ) {
                $('#downloadSpinner').toggle();
                $('#downloadModal').modal("toggle");
                var downloadURL = "../downloads/" + $(data).find("file").text();
                document.getElementById("secretIFrame").setAttribute("src", downloadURL);
            }
          );                
    
    
}

function printDownloadWordlist(){
    /*
        String wordForms = request.getParameter("wordForms");
        String selection = request.getParameter("selection");
        String sorting = request.getParameter("sorting");
        String output = request.getParameter("download");
    
     */
    var wordForms = $("input[name='wordformsRadioOptions']:checked").val();
    var selection = $("input[name='selectionRadioOptions']:checked").val();
    var sorting = $("input[name='sortRadioOptions']:checked").val();
    var output = $("input[name='outputRadioOptions']:checked").val();
    
    var wordlistDropdown = document.getElementById('refwordlist');
    var selectedWordlist = wordlistDropdown.options[wordlistDropdown.selectedIndex].value;       
    

    $.post(
            BASE_URL + "/ZumultDataServlet",
            { 
                command: 'printDownloadWordlist',
                transcriptID: transcriptID, 
                startSelection: startSelection,
                endSelection: endSelection,
                
                wordlistID: selectedWordlist,
                tokenList : tokenList,                
                
                
                wordForms : wordForms,
                selection : selection,
                sorting : sorting,
                output : output
            },
            function( data ) {
                //$('#downloadSpinner').toggle();
                //$('#downloadModal').modal("toggle");
                if (output === 'outputDownload'){
                    var downloadURL = "../downloads/" + $(data).find("file").text();
                    document.getElementById("secretIFrame").setAttribute("src", downloadURL);
                } else {
                    var w = window.open();
                    $(w.document.body).html(data);
                }
            }
          );                
    
}
