package org.zumult.webservices;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.jdom.Element;
import org.zumult.backend.Configuration;
import org.zumult.io.IOUtilities;

@Path("/")
@Produces(MediaType.APPLICATION_XML)
public class HomeResource {
    Response buildResponse = null;
    String xmlString;
    Element response = new Element("response");
    Element xmlDocument = new Element("xml_document");

    final String zumultApiBaseURL = Configuration.getRestAPIBaseURL();
    
    @Context
    private UriInfo context;
    
    @Context
    private HttpServletRequest servletRequest;    
    
    @Context 
    private ServletContext servletContext;
    
    public HomeResource() {
    }
    
    @GET
    public Response getHome() {
        String corporaUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("corpora").build().toString();
        String metadataUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("metadata").build().toString();
        String transcriptsUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("transcripts").build().toString();
        String mediaUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("media").build().toString();
        String additionalMaterialUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("additional-material").build().toString();
        String searchServiceUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("SearchService").build().toString();

        response.addContent(new Element("corpora_url").addContent(corporaUri));
        response.addContent(new Element("metadata_url").addContent(metadataUri));
        response.addContent(new Element("transcripts_url").addContent(transcriptsUri));
        response.addContent(new Element("media_url").addContent(mediaUri));
        response.addContent(new Element("additional_material_url").addContent(additionalMaterialUri));
        response.addContent(new Element("query_url").addContent(searchServiceUri));

        xmlString = IOUtilities.elementToString(response);
        return Response.ok(xmlString).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/TEST/{corpusID}")
    public Response test(){
        String absolutePathPuilder = context.getAbsolutePathBuilder().path("corpora").build().toString();
        String baseURIPathBuilder = context.getBaseUriBuilder().path("corpora").build().toString();
        String requestUriBuilder = context.getRequestUriBuilder().path("corpora").build().toString();

        String servletContextPath = servletContext.getContextPath();
        String servletRequestContextPath = servletRequest.getContextPath();
        String servletRemoteHost = servletRequest.getRemoteHost();
        
        Element response = new Element("response");
        response.addContent(new Element("absolutePathPuilder").addContent(absolutePathPuilder));
        response.addContent(new Element("baseURIPathBuilder").addContent(baseURIPathBuilder));
        response.addContent(new Element("requestUriBuilder").addContent(requestUriBuilder));
        response.addContent(new Element("servletContextPath").addContent(servletContextPath));
        response.addContent(new Element("servletRequestContextPath").addContent(servletRequestContextPath));
        
        String someString = IOUtilities.elementToString(response);
        buildResponse = Response.ok(someString).build();
        return buildResponse;
        
    }
    
    
    
}
