/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.jdom.Element;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;
import org.zumult.objects.TranscriptMetadata;

/**
 *
 * @author josip.batinic
 */
@Path("/transcripts")
@Produces(MediaType.APPLICATION_XML)
public class TranscriptsResource {
    BackendInterface backendInterface;
    Response buildResponse = null;
    StringBuilder sb = new StringBuilder();
    final String zumultApiBaseURL = Configuration.getRestAPIBaseURL();
    final String transcriptPath = Configuration.getTranscriptPath();
    IDList corpora;
    String xmlString;
    Element response = new Element("response");
    Element xmlDocument = new Element("xml_document");


    @Context
    private UriInfo context;

    public TranscriptsResource() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
            this.corpora = backendInterface.getCorpora();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @GET
    public Response getCorpora() {
        for (String id : corpora) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(id).build().toString();
            Element listElement = new Element(corpora.getObjectName().toLowerCase() + "_url");
            listElement.setAttribute("id", id).addContent(linkUrl);
            response.addContent(listElement);
        }
        xmlString = IOUtilities.elementToString(response);
        return Response.ok(xmlString).build();
    }

    @GET
    @Path("/{corpusID}")
    public Response getEvents4Corpus(@PathParam("corpusID") String corpusID) throws IOException {
        IDList events = backendInterface.getEvents4Corpus(corpusID);

        for (String event : events) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(event).build().toString();
            Element listElement = new Element(events.getObjectName().toLowerCase() + "_metadata_url");
            listElement.setAttribute("id", event).addContent(linkUrl);
            response.addContent(listElement);
        }

        xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : Response.ok(xmlString).build();
    }

    @GET
    @Path("/{corpusID}/{eventID}")
    public Response getSpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID, @PathParam("eventID") String eventID) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        for (String speechEvent : speechEventsList) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(speechEvent).build().toString();
            Element listElement = new Element("speechEvent_url");//(speechEventsList.getObjectName().toLowerCase());
            listElement.setAttribute("id", speechEvent).addContent(linkUrl);
            response.addContent(listElement);
        }
        xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID) || !eventsList.contains(eventID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : Response.ok(xmlString).build();
    }

    @GET
    @Path("/{corpusID}/{eventID}/{speechEventID}")
    public Response getTranscripts4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID,
                                                             @PathParam("eventID") String eventID,
                                                             @PathParam("speechEventID") String speechEventID) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList transcriptsList = backendInterface.getTranscripts4SpeechEvent(speechEventID);

        for (String transcript : transcriptsList) {
            if (!transcript.isEmpty()) {
                String transcriptUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(transcript).build().toString();
                Element transcriptUrlElement = new Element("transcript_url");
                transcriptUrlElement.addContent(transcriptUrl);
                response.addContent(transcriptUrlElement);
            }
        }
        xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/{eventID}/{speechEventID}/{transcriptID}")
    public Response getTranscript4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID,
                                                               @PathParam("eventID") String eventID,
                                                               @PathParam("speechEventID") String speechEventID,
                                                               @PathParam("transcriptID") String transcriptID) throws IOException, Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList transcriptsList = backendInterface.getTranscripts4SpeechEvent(speechEventID);
        Transcript transcript = backendInterface.getTranscript(transcriptID);
        String transcriptXml = transcript.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(transcriptXml));
        
                String transcriptMetadataUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("metadata").build().toString();
                String transcriptFileUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("file").build().toString();

                Element listElement = new Element("transcript");
                Element transcriptFile = new Element("transcript_file_url");
                Element transcriptMetadata = new Element("transcript_metadata_url");

                transcriptMetadata.addContent(transcriptMetadataUrl);
                transcriptFile.addContent(transcriptFileUrl);

                listElement.addContent(transcriptMetadata);
                listElement.addContent(transcriptFile);
                listElement.setAttribute("id", transcriptID);

                response.addContent(listElement);

        String xmlString = IOUtilities.elementToString(response);
        return buildResponse = !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID) || !transcriptsList.contains(transcriptID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/{eventID}/{speechEventID}/{transcriptID}/metadata")
    public Response getTranscriptMetadata4SpeechEvent4Event4Transcripts(@PathParam("corpusID") String corpusID,
                                                                     @PathParam("eventID") String eventID,
                                                                     @PathParam("speechEventID") String speechEventID,
                                                                     @PathParam("transcriptID") String transcriptID) throws IOException, Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList transcriptsList = backendInterface.getTranscripts4SpeechEvent(speechEventID);

        TranscriptMetadata transcriptMetadata = backendInterface.getTranscriptMetadata4Transcript(eventID, transcriptID);
        String transcriptMetadataXml = transcriptMetadata.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(transcriptMetadataXml));
        xmlString = IOUtilities.elementToString(xmlDocument);

        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID) || !transcriptsList.contains(transcriptID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/{eventID}/{speechEventID}/{transcriptID}/file")
    public Response getTranscriptFile4SpeechEvent4Event4Transcripts(@PathParam("corpusID") String corpusID,
                                                                     @PathParam("eventID") String eventID,
                                                                     @PathParam("speechEventID") String speechEventID,
                                                                     @PathParam("transcriptID") String transcriptID) throws IOException, Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList transcriptsList = backendInterface.getTranscripts4SpeechEvent(speechEventID);

        Transcript transcript = backendInterface.getTranscript(transcriptID);
        String transcriptXml = transcript.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(transcriptXml));
        xmlString = IOUtilities.elementToString(xmlDocument);

        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID) || !transcriptsList.contains(transcriptID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/transcript/{transcriptID}")
    public Response getTranscript(@PathParam("transcriptID") String transcriptID,
                                           @DefaultValue("xml") @QueryParam("format") String format) {
        try {
            Transcript transcript = backendInterface.getTranscript(transcriptID);
            buildResponse = Response.ok(transcript.toXML()).build();
        } catch (IOException ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    
    // New 10-07-2020, Elena
    @GET
    @Path("/transcript/{transcriptID}/numberOfTokens")
    public Response getNumberOfTokens4Transcript(@PathParam("transcriptID") String transcriptID,
                                           @DefaultValue("string") @QueryParam("format") String responseFormat) {
        
        try {
            Transcript transcript = backendInterface.getTranscript(transcriptID);
            String result = Integer.toString(transcript.getNumberOfTokens());
            
            switch (responseFormat){
                    case "xml":
                        String xml = "<metadata-value key=\"" + "measure_word_tokens" + "\" id=" + transcriptID + "\">" + result + "</metadata>";
                        buildResponse = Response.ok(xml, MediaType.APPLICATION_XML).build();
                        break;
                    case "string":
                        buildResponse = Response.ok(result, MediaType.TEXT_PLAIN).build();
                        break;
                    default:
                        buildResponse = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                        break;
            }

        } catch (IOException ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    
    // New 27-07-2020, Elena
    @GET
    @Path("/transcript/{transcriptID}/audio")
    public Response getAudioFileID4Transcript(@PathParam("transcriptID") String transcriptID,
                                           @DefaultValue("string") @QueryParam("format") String responseFormat) {
        
        try {
            String result = backendInterface.getAudios4Transcript(transcriptID).get(0);
            //String xml = "<media id=\"" + mediaID + "\">" "</media>";
           
                
            switch (responseFormat){
                    case "xml":
                        String xml = "<metadata-value key=\"" + "a_dgd_kennung" + "\" id=" + transcriptID + "\">" + result + "</metadata-value>";
                        buildResponse = Response.ok(xml, MediaType.APPLICATION_XML).build();
                        break;
                    case "string":
                        buildResponse = Response.ok(result, MediaType.TEXT_PLAIN).build();
                        break;
                    default:
                        buildResponse = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                        break;
            }

        } catch (IOException ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    // New 27-01-2021, Elena
    @GET
    @Path("/transcript/{transcriptID}/video")
    public Response getVideoFileID4Transcript(@PathParam("transcriptID") String transcriptID,
                                           @DefaultValue("string") @QueryParam("format") String responseFormat) {
        
        try {
            String mediaID = "";
            IDList videos = backendInterface.getVideos4Transcript(transcriptID);
            if(videos.size()>0){
                mediaID = videos.get(0);  
            }
                       
            //String xml = "<media id=\"" + mediaID + "\">" "</media>";
           
            switch (responseFormat){
                    case "xml":
                        String xml = "<metadata-value key=\"" + "v_dgd_kennung" + "\" id=" + transcriptID + "\">" + mediaID + "</metadata-value>";
                        buildResponse = Response.ok(xml, MediaType.APPLICATION_XML).build();
                        break;
                    case "string":
                        buildResponse = Response.ok(mediaID, MediaType.TEXT_PLAIN).build();
                        break;
                    default:
                        buildResponse = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                        break;
            }

        } catch (IOException ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    @GET
    @Path("/transcript/{transcriptID}/annotationBlocks/{annotationBlockID}/startTime")
    public Response getVideoFileID4Transcript(@PathParam("transcriptID") String transcriptID,
                                            @PathParam("annotationBlockID") String annotationBlockID,
                                           @DefaultValue("string") @QueryParam("format") String responseFormat) {
        
        try {
 
            Transcript transcript = backendInterface.getTranscript(transcriptID);
            double startTime = transcript.getTimeForID(annotationBlockID);   
            
            //String xml = "???";
           
            switch (responseFormat){
                    case "xml":
                        buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Sorry! XML is not supported yet").build();
                        //buildResponse = Response.ok(xml, MediaType.APPLICATION_XML).build();
                        break;
                    case "string":
                        buildResponse = Response.ok(startTime, MediaType.TEXT_PLAIN).build();
                        break;
                    default:
                        buildResponse = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                        break;
            }

        } catch (IOException ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(TranscriptsResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    
    
}
