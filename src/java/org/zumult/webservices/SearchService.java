/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

/**
 *
 * @author Elena Frick
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import org.jdom.Element;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.IOUtilities;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SampleQuery;
import org.zumult.query.SearchStatistics;
import org.zumult.query.KWIC;
import org.zumult.query.SearchResultPlus;
/**
 *
 * @author Elena
 */
@Path("/SearchService")

/* Sample URL Requests: http://zumult.ids-mannheim.de/ProtoZumult/api/SearchService */

public class SearchService {
    BackendInterface backendInterface;
    Response buildResponse = null;
    final String zumultApiBaseURL = Configuration.getRestAPIBaseURL();
    //private static final String IP_NOT_VALID_MASSAGE = "IP address is not valid!";
    
    /*@Context
    private HttpServletRequest httpServletRequest;
    */
    @Context
    private UriInfo context;
    
    public SearchService() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getURLs() {
        Element response = new Element("response");
        
        String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("queries").build().toString();
        Element listElement = new Element("sample_queries_url");
        listElement.addContent(linkUrl);
        response.addContent(listElement);
        
        String linkUrlDoc = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("documentation").build().toString();
        Element listElementDoc = new Element("search_service_documentation_url");
        listElementDoc.addContent(linkUrlDoc);
        response.addContent(listElementDoc);
        
        buildResponse = Response.ok(IOUtilities.elementToString(response)).build();
        return buildResponse;
    }

    @GET
    @Path("/queries")
    @Produces(MediaType.APPLICATION_XML)
    public Response getCorpora() {
        Element response = new Element("response");
        IDList corpusIds = backendInterface.getCorporaForSearch(null);
        for (String id : corpusIds) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(id).build().toString();
            Element listElement = new Element("available_corpus_url");
            listElement.setAttribute("id", id).addContent(linkUrl);
            response.addContent(listElement);
        }
        buildResponse = Response.ok(IOUtilities.elementToString(response)).build();
        return buildResponse;
    }
    
    @GET
    @Path("/documentation")
    @Produces(MediaType.APPLICATION_XML)
    public Response getDocumentation() {
        Element response = new Element("response");

        String linkUrl = "https://docs.google.com/document/d/13ky5QGYpuEOGpe8z1dykpGulsY4G-CUP-1VZKgb3pwg/edit#heading=h.g2omykpu5hy2";
        Element listElement = new Element("available_documentation_url");
        listElement.addContent(linkUrl);
        response.addContent(listElement);
        buildResponse = Response.ok(IOUtilities.elementToString(response)).build();
        return buildResponse;
    }
    
    
    /*************************************************************************/
    /*                      Search Service 1 'Corpora'                       */
    /*************************************************************************/
    
    @GET
    @Path("/corpora")
    @Produces(MediaType.APPLICATION_XML)
    public Response getIndexedCorpora() {
        IDList corpusIds = backendInterface.getCorporaForSearch(null);
        String xmlString = corpusIds.toXML();
        return Response.ok(xmlString).build();
    }
    
    
    /*************************************************************************/
    /*             Search Service 2 'Search Query Examples'                  */
    /*************************************************************************/
    
    @GET
    @Path("/queries/{corpusID}")
    /* Sample URL Requests: 
    .../SearchService/queries/FOLK" 
    .../SearchService/queries/DH--"
    */
    @Produces(MediaType.APPLICATION_XML)
    public Response getQueriesForCorpus(@PathParam("corpusID") String corpusID) {
        Element response = new Element("response");
        response.setAttribute("type", "queries");
        
        Element corpusQueryElement = new Element("corpusQuery");
        corpusQueryElement.setText(Constants.CORPUS_SIGLE + "=\"" + corpusID + "\"");
        response.addContent(corpusQueryElement);
        
        Element queriesElement = new Element("queries");
        
        try{           
            List<SampleQuery> queries = backendInterface.getSampleQueries(corpusID, null);            
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).build().toString();

            int counter = 0;
            for(int i=0; i<queries.size(); i++){           
                SampleQuery q = queries.get(i);
                String link = linkUrl + "/SearchService/kwic?q=" + 
                          q.getQueryString().replaceAll("#","%23")
                                            .replaceAll(" & ","%26")
                                            .replaceAll("\\+", "%2B")
                                            .replaceAll("\\\\\\\\", "\\\\") 
                                            .replaceAll("\\{","%7B")
                                            .replaceAll("\\}","%7D")
                                            .replaceAll("\\|","%7C")
                                            .replaceAll("\\\\","%5C")
                            + "&cq=corpusSigle=\"" 
                            + corpusID
                            + "\"";
                Element query = new Element("query");
                Element string = new Element("string");
                Element description = new Element("description");
                Element url = new Element("url");

                string.addContent(q.getQueryString());
                description.addContent(q.getDescription());
                url.addContent(link);
                query.addContent(url);
                counter = counter +1;
                query.setAttribute("number", String.valueOf(counter));
                query.addContent(string);
                query.addContent(description);
                queriesElement.addContent(query);
            }

            response.addContent(queriesElement);
            buildResponse = Response.ok(IOUtilities.elementToString(response)).build();
            
        }catch (SearchServiceException ex){
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        return buildResponse;
    }
           
    /*************************************************************************/
    /*              Search Service 3 'KWIC' (GET and POST)                   */
    /*************************************************************************/
    
    @GET
    @Path("/kwic")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    /* Sample URL Requests: 
    .../SearchService/kwic?q=%5Bword="wann"%5D&cq=corpusSigle="FOLK"&cutoff=false
    .../SearchService/kwic?q=[pos="ADJA"]&format=json&cq=corpusSigle="FOLK" 
    .../SearchService/kwic?q=[lemma="du"][]{2}[pos="VV.*"]&cq=corpusSigle="FOLK|GWSS"
    .../SearchService/kwic?q=[w="de" %26 pos="PPER"]&count=20&offset=5&context=3-t,3-t&cq=corpusSigle="FOLK"
    .../SearchService/kwic?q=[w="die"]&cutoff=false&count=20&offset=10&cq=corpusSigle="FOLK"
    .../SearchService/kwic?q=[w="die"]&cutoff=false&count=20&cq=corpusSigle="FOLK|GWSS"
    */
    public Response getKWICviaULR( @QueryParam("q") String queryString,
                             @QueryParam("ql") String queryLanguage,
                             @QueryParam("v") String queryLanguageVersion,
                             @QueryParam("cq") String corpusQuery,
                             @QueryParam("meta") String metadataQuery,
                             @DefaultValue("xml") @QueryParam("format") String responseFormat,
                             @QueryParam("context") String context,
                             @QueryParam("count") Integer pageLength,
                             @QueryParam("offset") Integer pageStartIndex, // starts with 0
                             @QueryParam("cutoff") Boolean cutoff,
                             @QueryParam("mode") String searchMode,
                             @QueryParam("customWordLists") String wordLists) throws Exception {
        buildResponse = getKWIC(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery,
                        context, pageLength, pageStartIndex, cutoff, searchMode, responseFormat, wordLists);
        
        return buildResponse;
   
    }
    
    @POST
    @Path("/kwic")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getKWICperPost( @FormParam("q") String queryString,
                             @FormParam("ql") String queryLanguage,
                             @FormParam("v") String queryLanguageVersion,
                             @FormParam("cq") String corpusQuery,
                             @FormParam("meta") String metadataQuery,
                             @DefaultValue("xml") @FormParam("format") String responseFormat,
                             @FormParam("context") String context,
                             @FormParam("count") Integer pageLength,
                             @FormParam("offset") Integer pageStartIndex, // starts with 0
                             @FormParam("cutoff") Boolean cutoff,
                             @FormParam("mode") String searchMode,
                             @FormParam("customWordLists") String wordLists) throws Exception {
        
        buildResponse = getKWIC(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery,
                        context, pageLength, pageStartIndex, cutoff, searchMode, responseFormat, wordLists);
        
        return buildResponse;
          
    }
    
    /*************************************************************************/
    /*        Search Service 4 'Search Statistics' (GET and POST)            */
    /*************************************************************************/
    
    @GET
    @Path("/statistics")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getStatisticsViaURL( @QueryParam("q") String queryString,
                             @QueryParam("ql") String queryLanguage,
                             @QueryParam("v") String queryLanguageVersion,
                             @QueryParam("cq") String corpusQuery,
                             @QueryParam("meta") String metadataQuery,
                             @DefaultValue("xml") @QueryParam("format") String responseFormat,
                             @QueryParam("metadataKeyID") String metadataKeyID,
                             @QueryParam("count") Integer pageLength,
                             @QueryParam("offset") Integer pageStartIndex, // starts with 0
                             @QueryParam("sort") String sortType,
                             @QueryParam("mode") String searchMode,
                             @QueryParam("customWordLists") String wordLists) throws Exception {
        
        buildResponse = getStatistics(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery, metadataKeyID, pageLength, pageStartIndex, searchMode, sortType, responseFormat, wordLists);
        return buildResponse;
  
    }

    
    @POST
    @Path("/statistics")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getStatisticsPerPost ( @FormParam("q") String queryString,
                                        @FormParam("ql") String queryLanguage,
                                        @FormParam("v") String queryLanguageVersion,
                                        @FormParam("cq") String corpusQuery,
                                        @FormParam("meta") String metadataQuery,
                                        @DefaultValue("xml") @FormParam("format") String responseFormat,
                                        @FormParam("metadataKeyID") String metadataKeyID,
                                        @FormParam("count") Integer pageLength,
                                        @FormParam("offset") Integer pageStartIndex, // starts with 0
                                        @FormParam("sort") String sortType,
                                        @FormParam("mode") String searchMode,
                                        @FormParam("customWordLists") String wordLists) throws Exception {
        
        buildResponse = getStatistics(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery, metadataKeyID, pageLength, pageStartIndex, searchMode, sortType, responseFormat, wordLists);
        return buildResponse;

    }
    
    /*************************************************************************/
    /*        Search Service 5 'Search in Transcript' (POST)                 */
    /*************************************************************************/
    
    @POST
    @Path("/transcript")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response searchTokensForTranscriptPerPost ( @FormParam("q") String queryString,
                                        @FormParam("ql") String queryLanguage,
                                        @FormParam("v") String queryLanguageVersion,
                                        @FormParam("cq") String corpusQuery,
                                        @FormParam("meta") String metadataQuery,
                                        @DefaultValue("xml") @FormParam("format") String responseFormat,
                                        @FormParam("transcriptID") String transcriptID,
                                        @FormParam("tokenAttribute") String tokenAttribute,                              
                                        @FormParam("mode") String searchMode,
                                        @FormParam("customWordLists") String wordLists) throws Exception {
        

        buildResponse = searchTokensForTranscript(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery, 
            searchMode, transcriptID, tokenAttribute, responseFormat, wordLists);
        
        return buildResponse;

    }
    
    /*************************************************************************/
    /*             Search Service 6 'Distinct Values' (POST)                 */
    /*************************************************************************/
    
    @POST
    @Path("/statistics/distinctValues")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getDistinctValues ( @FormParam("q") String queryString,
                                        @FormParam("ql") String queryLanguage,
                                        @FormParam("v") String queryLanguageVersion,
                                        @FormParam("cq") String corpusQuery,
                                        @FormParam("meta") String metadataQuery,
                                        @DefaultValue("string") @FormParam("format") String responseFormat,
                                        @FormParam("metadataKeyID") String metadataKeyID,
                                        @FormParam("count") Integer pageLength,
                                        @FormParam("offset") Integer pageStartIndex, // starts with 0
                                        @FormParam("sort") String sortType,
                                        @FormParam("mode") String searchMode, 
                                        @FormParam("customWordLists") String wordLists) throws Exception {       
        
        
    /*    String client_ip = httpServletRequest.getRemoteAddr();
        if (!(ClientIPValidator.validateClientIP(client_ip))){
            buildResponse = Response.status(Response.Status.BAD_REQUEST).entity(IP_NOT_VALID_MASSAGE).build();  
            return buildResponse;
        }
      */ 
        
        Map<String, String> additionalSearchConstraints = createAdditionalSearchConstraintsMap(wordLists);
        
        try {
            SearchStatistics result = backendInterface.getSearchStatistics(queryString, queryLanguage, 
                    queryLanguageVersion, corpusQuery, metadataQuery, metadataKeyID, pageLength, pageStartIndex, searchMode, sortType, additionalSearchConstraints);
                
            switch (responseFormat){
                case "json":
                    buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sorry! JSON is not supported yet").build();
                    break;
                case "string":
                    buildResponse = Response.ok(result.getNumberOfDistinctValues(), MediaType.TEXT_PLAIN).build();
                    break;                  
                case "xml":
                    
                   /* <response type="statistics"><distinctValues>62</distinctValues></response> */
                            
                    Element response = new Element("response");
                    response.setAttribute("type", "statistics");
                    Element corpusQueryElement = new Element("distinctValues");
                    corpusQueryElement.setText(String.valueOf(result.getNumberOfDistinctValues()));
                    response.addContent(corpusQueryElement);

                    buildResponse =  Response.ok(IOUtilities.elementToString(response), MediaType.APPLICATION_XML).build();
                    break;
                default:
                    buildResponse = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                    break;
            }               
        } catch (SearchServiceException ex){
            buildResponse = Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return buildResponse;

    }
    
    
    /*************************************************************************/
    /*             Search Service 7 'KWIC Download' (POST)                   */
    /*************************************************************************/
    
    
    @POST  
    @Path("/kwic/download")  
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response downloadKWICperPost( @FormParam("q") String queryString,
                             @FormParam("ql") String queryLanguage,
                             @FormParam("v") String queryLanguageVersion,
                             @FormParam("cq") String corpusQuery,
                             @FormParam("meta") String metadataQuery,
                             @DefaultValue("xml") @FormParam("fileFormat") String fileFormat,
                             @DefaultValue("xml") @FormParam("format") String responseFormat,
                             @FormParam("context") String context,
                             @FormParam("count") Integer pageLength,
                             @FormParam("offset") Integer pageStartIndex, // starts with 0
                             @FormParam("cutoff") Boolean cutoff,
                             @FormParam("mode") String searchIndex,
                             @FormParam("addMeta") String metadata,
                             @FormParam("customWordLists") String wordLists) throws Exception {
        
        
    /*    String client_ip = httpServletRequest.getRemoteAddr();
        if (!(ClientIPValidator.validateClientIP(client_ip))){
            buildResponse = Response.status(Response.Status.BAD_REQUEST).entity(IP_NOT_VALID_MASSAGE).build();  
            return buildResponse;
        }
      */  
    
        Map<String, String> additionalSearchConstraints = createAdditionalSearchConstraintsMap(wordLists);
        
        IDList metadataIDs = new IDList("metadataIDs");
        if(metadata!=null && !metadata.isEmpty()){      
            for (String metadataKey: Arrays.asList(metadata.split(" "))){
                metadataIDs.add(metadataKey);
            } 
        }
        
        try {
            SearchResultPlus searchResultPlus = backendInterface.search(queryString, queryLanguage, queryLanguageVersion, 
                    corpusQuery, metadataQuery, pageLength, pageStartIndex, cutoff, searchIndex, metadataIDs, additionalSearchConstraints);
            KWIC result = backendInterface.exportKWIC(searchResultPlus, context, fileFormat);
            
            switch (responseFormat){
                case "json":
                    buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sorry! JSON is not supported yet").build();
                    break;
                case "xml":      
                    buildResponse = Response.ok(result.toXML(), MediaType.APPLICATION_XML).build();
                    break;
                default:
                    buildResponse = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                    break;
            }               
        } catch (SearchServiceException ex){
            buildResponse = Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return buildResponse;
   
    }
    
    
    /*************************************************************************/
    /*             Search Service 8 'MetadataKeys/IDs' (GET)                 */
    /*************************************************************************/
    
    @GET
    @Path("/metadataKeys/IDs")
    @Produces({MediaType.APPLICATION_XML})
    public Response getMetadataKeysForSearch(@QueryParam("cq") String corpusQuery,
                             @QueryParam("mode") String searchIndex,
                             @QueryParam("locale") String localeStr,
                             @QueryParam("type") String metadataType) throws Exception {
        
        if (localeStr==null || localeStr.isEmpty()){
            localeStr = Constants.DEFAULT_LOCALE;
        }
        Locale locale = Locale.forLanguageTag(localeStr);
        Set<MetadataKey> metadataKeys = backendInterface.getMetadataKeysForSearch(corpusQuery, searchIndex, metadataType);
        
        Element response = new Element("response");
        response.setAttribute("type", "metadata");
        
        Element corpusQueryElement = new Element("corpusQuery");
        corpusQueryElement.setText(corpusQuery);
        response.addContent(corpusQueryElement);
        
        Element metadata = new Element("metadata");
        
        List<MetadataKey> metadataKeysList = metadataKeys.stream().sorted((o1, o2) -> 
        o1.getID().compareTo(o2.getID())).collect(Collectors.toList());
        
        for (MetadataKey key : metadataKeysList) {
            Element listElement = new Element("metadata-key");
            listElement.setAttribute("id", key.getID());
            listElement.setAttribute("name", key.getName(locale.getLanguage()));
            listElement.setAttribute("type", key.getLevel().name());
            metadata.addContent(listElement);
        }    
        
        response.addContent(metadata);
        
        buildResponse =  Response.ok(IOUtilities.elementToString(response), MediaType.APPLICATION_XML).build();
        return buildResponse;
    }

    /*************************************************************************/
    /*             Search Service 9 'AnnotationLayers' (GET)                 */
    /*************************************************************************/
    
    @GET
    @Path("/annotationLayers")
    @Produces({MediaType.APPLICATION_XML})
    public Response getAnnotationLayersForSearch(@QueryParam("cq") String corpusQuery,
                             @QueryParam("mode") String searchIndex,
                             @QueryParam("locale") String localeStr,
                             @QueryParam("type") String annotationType) throws Exception {
        
        if (localeStr==null || localeStr.isEmpty()){
            localeStr = Constants.DEFAULT_LOCALE;
        }
        Locale locale = Locale.forLanguageTag(localeStr);
        
        Element response = new Element("response");
        response.setAttribute("type", "annotations");
        
        Element corpusQueryElement = new Element("corpusQuery");
        corpusQueryElement.setText(corpusQuery);
        response.addContent(corpusQueryElement);
        
        Element metadata = new Element("annotations");
        Set<AnnotationLayer> annotationLayers = backendInterface.getAnnotationLayersForSearch(corpusQuery, searchIndex, annotationType);
        
        List<AnnotationLayer> annotationLayerList = annotationLayers.stream().sorted((o1, o2) -> 
        o1.getID().compareTo(o2.getID())).collect(Collectors.toList());
        
        for (AnnotationLayer annotationLayer : annotationLayerList) {
            Element listElement = new Element("annotation-tier");
            listElement.setAttribute("id", annotationLayer.getID());
            listElement.setAttribute("name", annotationLayer.getName(locale.getLanguage()));
            listElement.setAttribute("type", annotationLayer.getType().name());
            metadata.addContent(listElement);
        }   
        
        response.addContent(metadata);
        buildResponse =  Response.ok(IOUtilities.elementToString(response), MediaType.APPLICATION_XML).build();
        return buildResponse;
    }

    
    /*************************************************************************/
    /*             Search Service 10 'MetadataKeys/Values' (GET)             */
    /*************************************************************************/
  
    @GET
    @Path("/metadataKeys/values")
    @Produces({MediaType.APPLICATION_XML})
    public Response getValuesForMetadataKeyForSearch(@QueryParam("cq") String corpusQuery,
                             @QueryParam("mode") String searchIndex,
                             @QueryParam("metadataKeyID") String metadataKeyID) throws Exception {

  
        Set<String> corporaIDs = IOHelper.getCorporaIDsFromCorpusQuery(corpusQuery);
        Set<String> metadataValues = new HashSet();
        corporaIDs.stream().map(corpusID -> backendInterface.getAvailableValues(corpusID, metadataKeyID)).forEachOrdered(idList -> {
            idList.forEach(id -> {
                metadataValues.add(id);
            });
        });
        
        List<String> metadataValuesList = new ArrayList<>(metadataValues);
        Collections.sort(metadataValuesList);
        

        Element response = new Element("response");
        response.setAttribute("type", "metadata");
        
        Element corpusQueryElement = new Element("corpusQuery");
        corpusQueryElement.setText(corpusQuery);
        response.addContent(corpusQueryElement);
        
        Element metadataKeyIDElement = new Element("metadataKey");
        metadataKeyIDElement.setText(metadataKeyID);
        response.addContent(metadataKeyIDElement);
        
        Element metadataValuesElement = new Element("metadataValues");
        
        for (String value : metadataValuesList) {
            Element listElement = new Element("metadataValue");
            listElement.setText(value);
            metadataValuesElement.addContent(listElement);
        }    
        
        response.addContent(metadataValuesElement);
        
        buildResponse =  Response.ok(IOUtilities.elementToString(response), MediaType.APPLICATION_XML).build();
        return buildResponse;
    }

    /*************************************************************************/
    /*             Search Service 11 'AnnotationLayers/Values' (GET)         */
    /*************************************************************************/
  
    
    @GET
    @Path("/annotationLayers/values")
    @Produces({MediaType.APPLICATION_XML})
    public Response getValuesForAnnotationLayerForSearch(@QueryParam("cq") String corpusQuery,
                             @QueryParam("mode") String searchIndex,
                             @QueryParam("metadataKeyID") String annotationLayerID) throws Exception {
        Set<String> corporaIDs = IOHelper.getCorporaIDsFromCorpusQuery(corpusQuery);
        Set<String> annotationValues = new HashSet();
        for(String corpusID: corporaIDs){
            IDList values = backendInterface.getAvailableValuesForAnnotationLayer(corpusID, annotationLayerID);
            for (String id: values){
                annotationValues.add(id);
            }
        }


        List<String> annotationValuesList = new ArrayList<>(annotationValues);
        Collections.sort(annotationValuesList);
        
        Element response = new Element("response");
        response.setAttribute("type", "annotations");
        
        Element corpusQueryElement = new Element("corpusQuery");
        corpusQueryElement.setText(corpusQuery);
        response.addContent(corpusQueryElement);
        
        Element metadataKeyIDElement = new Element("annotationLayerID");
        metadataKeyIDElement.setText(annotationLayerID);
        response.addContent(metadataKeyIDElement);
        
        Element metadataValuesElement = new Element("annotationValues");
        
        for (String value : annotationValuesList) {
            Element listElement = new Element("annotationValue");
            listElement.setText(value);
            metadataValuesElement.addContent(listElement);
        }    
        
        response.addContent(metadataValuesElement);
        
        buildResponse =  Response.ok(IOUtilities.elementToString(response), MediaType.APPLICATION_XML).build();
        return buildResponse;
    }
    
    /*************************************************************************/
    /*             Search Service 12 'Repetitions' (POST)                    */
    /*************************************************************************/
    
    @POST
    @Path("/repetitions")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getRepetitionsPerPost( @FormParam("q") String queryString,
                             @FormParam("ql") String queryLanguage,
                             @FormParam("v") String queryLanguageVersion,
                             @FormParam("cq") String corpusQuery,
                             @FormParam("meta") String metadataQuery,
                             @DefaultValue("xml") @FormParam("format") String responseFormat,
                             @FormParam("context") String context,
                             @FormParam("count") Integer pageLength,
                             @FormParam("offset") Integer pageStartIndex, // starts with 0
                             @FormParam("cutoff") Boolean cutoff,
                             @FormParam("mode") String searchMode,
                             @FormParam("repetitions") String repetitions,
                             @FormParam("synonyms") String synonyms, 
                             @FormParam("customWordLists") String wordLists) throws Exception {

        buildResponse = getRepetitions(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery,
                        context, pageLength, pageStartIndex, cutoff, searchMode, responseFormat, repetitions, synonyms, wordLists);
        
        return buildResponse;
          
    }
    
    /**************************************************************************/
    /*                        private methods                                 */
    /**************************************************************************/
    
    private Response getRepetitions(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, String context, Integer pageLength, 
            Integer pageIndex, Boolean cutoff, String mode, String responseFormat, String repetitions, String synonyms, String wordLists){

        Response response;
        Map<String, String> additionalSearchConstraints = createAdditionalSearchConstraintsMap(wordLists);
        
     /*   String client_ip = httpServletRequest.getRemoteAddr();
        if (!(ClientIPValidator.validateClientIP(client_ip))){
            response = Response.status(Response.Status.BAD_REQUEST).entity(IP_NOT_VALID_MASSAGE).build();  
            return response;
        }
       */ 
        try {

            SearchResultPlus searchResultPlus = backendInterface.searchRepetitions(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery, 
                pageLength, pageIndex, cutoff, mode, null, repetitions, synonyms, additionalSearchConstraints);
            KWIC result = backendInterface.getKWIC(searchResultPlus, context);
            
            switch (responseFormat){
                case "json":
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sorry! JSON is not supported yet").build();
                    break;
                case "xml":
                    response = Response.ok(result.toXML(), MediaType.APPLICATION_XML).build();
                    break;
                default:
                    response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                    break;
            }
        } catch (SearchServiceException ex){
            response = Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();                
        } catch (IOException | ParserConfigurationException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return response;
        
    }
    
    private Response getKWIC(String queryString, String queryLanguage, String queryLanguageVersion, 
            String corpusQuery, String metadataQuery, String context, Integer pageLength, 
            Integer pageIndex, Boolean cutoff, String mode, String responseFormat, String wordLists){

        Response response;
        Map<String, String> additionalSearchConstraints = createAdditionalSearchConstraintsMap(wordLists);
        
     /*   String client_ip = httpServletRequest.getRemoteAddr();
        if (!(ClientIPValidator.validateClientIP(client_ip))){
            response = Response.status(Response.Status.BAD_REQUEST).entity(IP_NOT_VALID_MASSAGE).build();  
            return response;
        }
      */  
        try {

            /*KWIC result = backendInterface.getKWIC(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery,
                        pageLength, pageIndex, cutoff, mode, context);*/
            
            SearchResultPlus searchResultPlus = backendInterface.search(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery, 
                pageLength, pageIndex, cutoff, mode, null, additionalSearchConstraints);
            KWIC result = backendInterface.getKWIC(searchResultPlus, context);
                    
            switch (responseFormat){
                case "json":
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sorry! JSON is not supported yet").build();
                    break;
                case "xml":
                    response = Response.ok(result.toXML(), MediaType.APPLICATION_XML).build();
                    break;
                default:
                    response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                    break;
            }
        } catch (SearchServiceException ex){
            response = Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();                
        } catch (IOException | ParserConfigurationException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return response;
        
    }
    
    private Response getStatistics(String queryString, String queryLanguage, 
            String queryLanguageVersion, String corpusQuery, String metadataQuery, String metadataKeyID, 
            Integer pageLength, Integer pageStartIndex, String mode, String sortType, String responseFormat, String wordLists){

        Response response;
        Map<String, String> additionalSearchConstraints = createAdditionalSearchConstraintsMap(wordLists);
   /*     String client_ip = httpServletRequest.getRemoteAddr();
        if (!(ClientIPValidator.validateClientIP(client_ip))){
            response = Response.status(Response.Status.BAD_REQUEST).entity(IP_NOT_VALID_MASSAGE).build();  
            return response;
        }
    */    

        try {
            SearchStatistics result = backendInterface.getSearchStatistics(queryString, queryLanguage, 
                    queryLanguageVersion, corpusQuery, metadataQuery, metadataKeyID, 
                    pageLength, pageStartIndex, mode, sortType, additionalSearchConstraints);
                
            switch (responseFormat){
                case "json":
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sorry! JSON is not supported yet").build();
                    break;
                case "xml":
                    response = Response.ok(result.toXML(), MediaType.APPLICATION_XML).build();
                    break;
                default:
                    response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                    break;
            }               
        } catch (SearchServiceException ex){
            response = Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return response;
    }
    
    private Response searchTokensForTranscript(String queryString, String queryLanguage, String queryLanguageVersion, String corpusQuery, String metadataQuery, 
            String searchMode, String transcriptID, String tokenAttribute, String responseFormat, String wordLists){
       
        Response response; 
        Map<String, String> additionalSearchConstraints = createAdditionalSearchConstraintsMap(wordLists);
        
    /*     String client_ip = httpServletRequest.getRemoteAddr();
        if (!(ClientIPValidator.validateClientIP(client_ip))){
            response = Response.status(Response.Status.BAD_REQUEST).entity(IP_NOT_VALID_MASSAGE).build();  
            return response;
        }
    */   
        try {

            IDList result = backendInterface.searchTokensForTranscript(queryString, queryLanguage, queryLanguageVersion, corpusQuery, metadataQuery, 
            searchMode, transcriptID, tokenAttribute, additionalSearchConstraints);
            
            switch (responseFormat){
                case "json":
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sorry! JSON is not supported yet").build();
                    break;
                case "xml":
                    response = Response.ok(result.toXML(), MediaType.APPLICATION_XML).build();                   
                    break;
                default:
                    response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                    break;
            }               
        } catch (SearchServiceException ex){
            response = Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return response;
    }
    
    private Map<String, String> createAdditionalSearchConstraintsMap(String wordLists){
        if (wordLists!=null && !wordLists.isEmpty()){
            Map<String, String> map = new HashMap();
            map.put(Constants.CUSTOM_WORDLISTS_KEY, wordLists);
            return map;
        }else{
            return null;
        }
        
    }

}
