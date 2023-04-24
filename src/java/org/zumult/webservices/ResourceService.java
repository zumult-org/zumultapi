/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.webservices;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.jdom.Element;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.IOUtilities;
import org.zumult.objects.CrossQuantification;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;

/**
 *
 * @author Frick
 */

@Path("/ResourceService")

public class ResourceService {
    BackendInterface backendInterface;
    Response buildResponse = null;
    
    @Context
    private UriInfo context;
        
    public ResourceService() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ResourceService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getURLs() {

        String[] urls =
        {
            "/corpora",
            "/GWSS/metadataKeys",
            "/FOLK/metadataKeys?type=EVENT",
            "/FOLK/crossQuantification?metaField1=e_se_mediale_realisierung&metaField2=s_geschlecht",
            "/FOLK/crossQuantification?metaField1=e_se_interaktionsdomaene&metaField2=s_geschlecht&units=TOKENS&format=html"
        };
    
        Element response = new Element("response");
        response.setAttribute("type", "urls");
        response.setAttribute("subtype", "examples");
        
        for(int i = 0; i<urls.length; i++){
            Element element = new Element("url");
            element.setAttribute("number", String.valueOf(i+1));
            element.addContent(Configuration.getRestAPIBaseURL() + "/" + context.getPath() + urls[i]);
            response.addContent(element);
        }
        
        buildResponse = Response.ok(IOUtilities.elementToString(response)).build();
        return buildResponse;
    }
    
    
    /*************************************************************************/
    /*                          Service 1 'Corpora'                          */
    /*************************************************************************/
    
    @GET
    @Path("/corpora")
    @Produces(MediaType.APPLICATION_XML)
    public Response getCorpora() {
        try {
            IDList corpora = backendInterface.getCorpora();
            System.out.println(corpora.toXML());
            buildResponse =  Response.ok(corpora.toXML()).build();
        } catch (IOException ex) {
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        return buildResponse;
    }
    
    
    /*************************************************************************/
    /*                         Service 2 'metadataKeys'                      */
    /*************************************************************************/
    
    @GET
    @Path("/{corpusID}/metadataKeys")
    @Produces(MediaType.APPLICATION_XML)
    public Response getMetadataKeys4Corpus(@PathParam("corpusID") String corpusID,
                             @QueryParam("locale") String localeStr,
                             @QueryParam("type") String metadataType) {
        
        try {
            if (localeStr==null || localeStr.isEmpty()){
                localeStr = Constants.DEFAULT_LOCALE;
            }
            Locale locale = Locale.forLanguageTag(localeStr);
            
            ObjectTypesEnum objectTypesEnum = null;
            if (metadataType != null) {
                objectTypesEnum = ObjectTypesEnum.valueOf(metadataType.toUpperCase());
            }
            
            Set<MetadataKey> metadataKeys = backendInterface.getMetadataKeys4Corpus(corpusID, objectTypesEnum);
            
            Element response = new Element("response");
            response.setAttribute("type", "metadata");
            Element corpusElement = new Element("corpusID");
            corpusElement.setText(corpusID);
            response.addContent(corpusElement);
            response.addContent(XMLSerialization.createElementForMetadataKeys(metadataKeys, locale));

            buildResponse =  Response.ok(IOUtilities.elementToString(response), MediaType.APPLICATION_XML).build();
            
        } catch (IOException ex) {
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return buildResponse;
    }
    
    
    /*************************************************************************/
    /*                         Service 3 'CrossQuantification'               */
    /*************************************************************************/
    
    @GET
    @Path("/{corpusID}/crossQuantification")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_HTML })
    public Response getCrossQuantification4Corpus(@PathParam("corpusID") String corpusID,
                             @QueryParam("metaField1") String metaField1,
                             @QueryParam("metaField2") String metaField2,
                             @QueryParam("units") String units,
                             @DefaultValue("xml") @QueryParam("format") String responseFormat){
        
        try {
            MetadataKey metadataKey1 = backendInterface.findMetadataKeyByID(metaField1);
            MetadataKey metadataKey2 = backendInterface.findMetadataKeyByID(metaField2);
            
            CrossQuantification crossQuantification = backendInterface.getCrossQuantification4Corpus(corpusID, metadataKey1, metadataKey2, units);

            //buildResponse = Response.ok(crossQuantification.toXML(), MediaType.APPLICATION_XML).build();
            
            buildResponse = switch (responseFormat) {
                case "html" -> Response.ok(crossQuantification.toXML(), MediaType.TEXT_HTML).build();
                case "xml" -> Response.ok(crossQuantification.toXML(), MediaType.APPLICATION_XML).build();
                default -> Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
            };               
            
        } catch (Exception ex) {
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return buildResponse;
    }

}

