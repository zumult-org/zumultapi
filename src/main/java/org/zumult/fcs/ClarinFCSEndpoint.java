/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.fcs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.IOHelper;
import org.zumult.objects.IDList;
import org.zumult.query.KWIC;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.SearchServiceException;
import org.zumult.query.Searcher;

/**
 *
 * @author bernd
 */
@Path("zumultfcs")    
@Produces(MediaType.APPLICATION_XML)
public class ClarinFCSEndpoint {
    
    BackendInterface backendInterface;

    public ClarinFCSEndpoint() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ClarinFCSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
    // https://clarin.ids-mannheim.de/korapsru?operation=explain
    // https://clarin.ids-mannheim.de/korapsru?operation=explain&x-fcs-endpoint-description=true
    // https://clarin.ids-mannheim.de/korapsru?operation=searchRetrieve&query=Buch&version=1.2    
    // https://clarin.ids-mannheim.de/korapsru?operation=searchRetrieve&query=%5Btt%3Alemma%3D%22.*heit%22%5D&queryType=fcs
    
    @GET
    public Response Endpoint (
            @QueryParam("operation") String operation,
            @QueryParam("x-fcs-endpoint-description") String xFcsEndpointDescription,            
            @QueryParam("query") String query,            
            @QueryParam("version") String version,            
            @QueryParam("queryType") String queryType            
    )             
    {
        try {
            if (operation==null){
                return generateErrorResponse(Response.Status.BAD_REQUEST, "\"Parameter 'operation' not provided.\"");
            }
            switch (operation) {
                case "explain" :
                    Response explainResponse = explain(xFcsEndpointDescription);
                    return explainResponse;
                case "searchRetrieve"  :
                    Response searchRetrieveResponse = searchRetrieve(query, version, queryType);
                    return searchRetrieveResponse;
                default :
                    return generateErrorResponse(Response.Status.BAD_REQUEST, "Operation '" + operation + "' unknown.");                    
            }
        } catch (IOException | SearchServiceException | ParserConfigurationException ex) {
            Logger.getLogger(ClarinFCSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            return generateErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error" + ex.getMessage());
        }
    } 

    private Response explain(String xFcsEndpointDescription) throws IOException {
        boolean isXFcsEndpointDescription = Boolean.parseBoolean(xFcsEndpointDescription);
        String path = "/org/zumult/fcs/clarinFCSsru.xml";
        String customPath = Configuration.getConfigurationVariable("path-to-sru-explain");
        if (customPath!=null){
            path = customPath;
        }
        String xml = IOHelper.readUTF8(path);
        Response response = Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_XML)
                .entity(xml)
                .build();  
        return response;
    }

    /*
        The searchRetrieve operation of the SRU protocol is used for searching in the Resources that are provided by the Endpoint. 
        The SRU protocol defines the serialization of request and response formats in [ref:OASIS-SRU20] for SRU version 2.0 and [ref:OASIS-SRU12] for SRU version 1.2. 
        An Endpoint MUST respond in the correct format, i.e. when Endpoint also supports SRU 1.2 and the request is issued in SRU version 1.2, the response must be encoded accordingly. 
        For SRU 2.0 we introduce the queryType parameter to tell which query language to use. For Contextual Query Language the value is cql and for FCS-QL the value is fcs.    
    */
    private Response searchRetrieve(String query, String version, String queryType) throws SearchServiceException, IOException, ParserConfigurationException {
        // version has to be one of 1.1 / 1.2 / 2.0
        if (!(version.matches("^((1\\.1)|(1\\.2)|(2\\.0))$"))){
            return generateErrorResponse(Response.Status.BAD_REQUEST, "Version '" + version + "' not permitted.");                            
        }
        // Now: 1.1 and 1.2 will always be CQL
        // and for 2.0, the parameter queryType becomes relevant
        if (version.equals("2.0") && queryType.equals("fcs")){
            // translate fcs-ql to cqp
            // do the query
            // transform the query result to SRU 2.0
            String cqpQuery = FCSQL2CQPTranslator.translateFCSQL2CQP(query);
            IDList corpora = backendInterface.getCorpora();
            String corpusQuery = String.join("|", corpora);
            SearchResultPlus searchResultPlus = backendInterface.search(cqpQuery, "cqp", "", corpusQuery, "", 
                50, 0, null, null, null, null);
            KWIC result = backendInterface.getKWIC(searchResultPlus, "5-t,5-t");
            String resultXML = result.toXML();
            Response response = Response
                    .status(Response.Status.OK)
                    .type(MediaType.APPLICATION_XML)
                    .entity(resultXML)
                    .build();  
            return response;            
        }
        return null;
    }

    private Response generateErrorResponse(Response.Status status, String message) {
        Response response = Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_XML)
                .entity("<error>" + message  +  "</error>")                      
                .build();  
        return response;
    }
    
    
}
