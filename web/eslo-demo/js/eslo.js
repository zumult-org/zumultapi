/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


var KWIC_SERVICE_URL = "http://localhost:8080/DGDRESTTest/api/SearchService/kwic"

function doQuery(){
    // alert('DO QUERY!');
    // http://localhost:8080/DGDRESTTest/api/SearchService/kwic?q=[word=%22maintenant%22]&cq=corpusSigle=%22ESLO-DEMO%22
    var queryExpression = document.getElementById('queryInputField').value;
    $.post(
        KWIC_SERVICE_URL, 
        { 
            q: queryExpression, 
            cq: "corpusSigle=\"ESLO-DEMO\""
        },
        function( data ) {
            var htmlFragment = xslTransform(new XMLSerializer().serializeToString(data), "zuRechtKwic2Html.xsl", null);

            $('#kwic_display').html(new XMLSerializer().serializeToString(htmlFragment));
            //document.getElementById("kwic_display").appendChild(html);
            
        }
    );                
    
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


