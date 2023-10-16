/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


//var KWIC_SERVICE_URL = "http://localhost:8080/DGDRESTTest/api/SearchService/kwic";
var KWIC_SERVICE_URL = "../api/SearchService/kwic";

function doQuery(){
    // alert('DO QUERY!');
    // http://localhost:8080/DGDRESTTest/api/SearchService/kwic?q=[word=%22maintenant%22]&cq=corpusSigle=%22ESLO-DEMO%22
    var queryExpression = document.getElementById('queryInputField').value;
    /*var ajaxSearchRequest= $.post(
        KWIC_SERVICE_URL, 
        { 
            q: queryExpression, 
            cq: "corpusSigle=\"ESLO-DEMO\""
        },
        function( data ) {
            var htmlFragment = xslTransform(new XMLSerializer().serializeToString(data), "zuRechtKwic2Html.xsl", null);
            var html = new XMLSerializer().serializeToString(htmlFragment);
            $('#kwic_display').html(html);
            //document.getElementById("kwic_display").appendChild(html);
            
        }
    );                
    alert("html");*/
    
    //alert("Trying " + KWIC_SERVICE_URL);
    
    ajaxSearchRequest = $.ajax({
        type: "POST",
        url: KWIC_SERVICE_URL,
        data: { q: queryExpression, cq : "corpusSigle=\"TGDP\"",
        dataType: "text"},

        success: function(xml, status) { 
            //alert(xml);
            var htmlFragment = xslTransform(new XMLSerializer().serializeToString(xml), "zuRechtKwic2Html.xsl", null);
            var html = new XMLSerializer().serializeToString(htmlFragment);
            $('#kwic_display').html(html);

        },
        error: function(xhr, status, error){
                alert(error);
        }
    });
    
    return false;
    
    
}

function xslTransform(xml, xslFileName, data){
       

    // make an xml object from string
    var parser = new DOMParser();
    var xmlDoc = parser.parseFromString(xml, "text/xml");
    
    // load the xsl file using synchronous (third param is set to false) XMLHttpRequest
    var myXMLHTTPRequest = new XMLHttpRequest();
    myXMLHTTPRequest.open("GET", xslFileName, false);
    myXMLHTTPRequest.send("");
    var xslRef = myXMLHTTPRequest.responseXML;
    
    
    // create xslt processor
    var xsltProcessor = new XSLTProcessor();
    xsltProcessor.importStylesheet(xslRef);
    
    // add parameters
    if(data!==null){
        for(var pair of data.entries()) {
            xsltProcessor.setParameter(null, pair[0], pair[1]);
        }
    }
    
    // transform
    var output = xsltProcessor.transformToFragment(xmlDoc, document); 
    return output;
                
}


function openTranscript(obj){
       $(obj).closest('form').submit();    
}


