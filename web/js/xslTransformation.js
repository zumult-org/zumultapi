/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * 
 * 
 * Document   : xslTransformation
 * Created on : 10.06.2020, 10:07:25
 * Author     : Elena_Frick
 */


/* This function transforms xml for Internet Explorer */
function ie(xml, xslFileName, data) {
    var xslt = new ActiveXObject("Msxml2.XSLTemplate.3.0");
    var xslDoc = new ActiveXObject("Msxml2.FreeThreadedDOMDocument.3.0");
    var xslProc;
    xslDoc.async = false;
    xslDoc.load(xslFileName);
    if (xslDoc.parseError.errorCode !== 0) {
        var myErr = xslDoc.parseError;
        alert("You have error " + myErr.reason);
    } else {
        xslt.stylesheet = xslDoc;
        var xmlDoc = new ActiveXObject("Msxml2.DOMDocument.3.0");
        xmlDoc.loadXML(xml);
        xslProc = xslt.createProcessor();
        xslProc.input = xmlDoc;

        // add parameters
        if(data!==null){
            for(var pair of data.entries()) {
                xslProc.addParameter(pair[0], pair[1]);
            }
        }

        xslProc.transform();
        var output = xslProc.output;
        return output; 
    }
}
            
/* This function transforms xml for Firefox */ 
function others(xml, xslFileName, data){
       
    // make an xml object from string
    var parser = new DOMParser();
    var xmlDoc = parser.parseFromString(xml, "text/xml");
    
    // load the xsl file using synchronous (third param is set to false) XMLHttpRequest
    var myXMLHTTPRequest = new XMLHttpRequest();
    myXMLHTTPRequest.open("GET", xslFileName, false);
    myXMLHTTPRequest.send(null);
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

/* This function performs the XSL transformation of xml  */
function transform(xml, xslFileName, data){
    
    var fragment;
    if (window.ActiveXObject || "ActiveXObject" in window) {
        fragment = ie(xml, xslFileName, data);          
    }else{
        fragment = others(xml, xslFileName, data);  
    }
    return fragment;
    
}
