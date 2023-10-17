/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.webservices;

import java.io.IOException;
import java.util.Iterator;
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

@Path("/QuantificationService")

public class QuantificationService {
    Response buildResponse = null;
    final String DGD = "DGD";
    final String ZUMULT = "ZuMult";
    
    @Context
    private UriInfo context;
        
    public QuantificationService() {

    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getURLs() {

        String[] urls =
        {
            "/corpora",
            "/corpora?backendID=DGD",
            "/GWSS/metadataKeys",
            "/AD--/metadataKeys",
            "/FOLK/metadataKeys?type=EVENT",
            "/FOLK/crossQuantification?metaField1=e_se_mediale_realisierung&metaField2=s_geschlecht",
            "/FOLK/crossQuantification?metaField1=e_se_mediale_realisierung&metaField2=s_geschlecht&backendID=DGD",
            "/FOLK/crossQuantification?metaField1=e_se_interaktionsdomaene&metaField2=s_geschlecht&units=TOKENS&format=html"
        };
        
        String[] descriptions =
        {
            "Corpora available in the ZUMULT backend",
            "Corpora available in the DGD backend",
            "Quantifiable metadata keys for GWSS",
            "Quantifiable metadata keys for AD",
            "Quantifiable event metadata keys for FOLK",
            "Cross-quantification for FOLK in the ZUMULT backend (Metadata keys: e_se_mediale_realisierung and s_geschlecht)",
            "Cross-quantification for FOLK in the DGD backend (Metadata keys: e_se_mediale_realisierung and s_geschlecht)",
            "Cross-quantification for FOLK in the ZUMULT backend (Metadata keys: e_se_interaktionsdomaene and s_geschlecht, units=TOKENS)"
        };
    
        Element response = new Element("response");
        response.setAttribute("type", "urls");
        response.setAttribute("subtype", "examples");
        
        for(int i = 0; i<urls.length; i++){
            Element element = new Element("url");
            element.setAttribute("number", String.valueOf(i+1));
            element.addContent(Configuration.getRestAPIBaseURL() + "/" + context.getPath() + urls[i]);
            Element description = new Element("description");
            description.addContent(descriptions[i]);
            element.addContent(description);
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
    public Response getCorpora(@QueryParam("backendID") String backendID) {
        try {
            IDList corpora;
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            if(backendID==null || backendID.equals(ZUMULT)){
                corpora = backendInterface.getCorporaForSearch(null);
                buildResponse =  Response.ok(corpora.toXML()).build();
            }else if (backendID.equals(DGD)){
                corpora = backendInterface.getCorpora();
                buildResponse =  Response.ok(corpora.toXML()).build();
            }else{
                buildResponse = Response.status(Response.Status.BAD_REQUEST).entity(backendID + " is not supported").build();
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |IOException ex) {
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
            
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            Set<MetadataKey> metadataKeys = backendInterface.getMetadataKeys4Corpus(corpusID, objectTypesEnum);
            
            for (Iterator<MetadataKey> i = metadataKeys.iterator(); i.hasNext();) {
                MetadataKey mk = i.next();
                if(!mk.isQuantified()){
                    i.remove();
                }
            }
            
            Element response = new Element("response");
            response.setAttribute("type", "metadata");
            Element corpusElement = new Element("corpusID");
            corpusElement.setText(corpusID);
            response.addContent(corpusElement);
            response.addContent(XMLSerialization.createElementForMetadataKeys(metadataKeys, locale));

            buildResponse =  Response.ok(IOUtilities.elementToString(response), MediaType.APPLICATION_XML).build();
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
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
                             @QueryParam("backendID") String backendID,
                             @QueryParam("metaField1") String metaField1,
                             @QueryParam("metaField2") String metaField2,
                             @QueryParam("units") String units,
                             @DefaultValue("xml") @QueryParam("format") String responseFormat){
        
        try {
            BackendInterface backendInterface = null;
            
            if(backendID==null || backendID.equals(ZUMULT)){
                backendInterface = BackendInterfaceFactory.newBackendInterface();
            }else if (backendID.equals(DGD)){
                backendInterface = BackendInterfaceFactory.newBackendInterface("org.zumult.backend.implementations.DGD2");
            }else{
                buildResponse = Response.status(Response.Status.BAD_REQUEST).entity(backendID + " is not supported").build();
            }
            
            if(backendInterface!=null){
                MetadataKey metadataKey1 = backendInterface.findMetadataKeyByID(metaField1);
                MetadataKey metadataKey2 = backendInterface.findMetadataKeyByID(metaField2);

                CrossQuantification crossQuantification = backendInterface.getCrossQuantification4Corpus(corpusID, metadataKey1, metadataKey2, units);

                buildResponse = switch (responseFormat) {
                    case "html" -> Response.ok(crossQuantification.toXML(), MediaType.TEXT_HTML).build();
                    case "xml" -> Response.ok(crossQuantification.toXML(), MediaType.APPLICATION_XML).build();
                    default -> Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                }; 
            }else {
                buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Backend could not be Backend could not be initialized!").build();
            }
            
        } catch (Exception ex) {
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return buildResponse;
    }

}

