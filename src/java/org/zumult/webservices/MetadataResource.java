/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
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
import org.zumult.objects.AdditionalMaterialMetadata;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.MediaMetadata;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.TranscriptMetadata;

/**
 *
 * @author josip.batinic
 */
@Path("/metadata")
@Produces(MediaType.APPLICATION_XML)
public class MetadataResource {
    
    BackendInterface backendInterface;
    CorporaResource corporaResource = new CorporaResource();
    Response buildResponse = null;
    final String zumultApiBaseURL = Configuration.getRestAPIBaseURL();
    IDList corpora;
    String xmlString;
    Element response = new Element("response");
    Element xmlDocument = new Element("xml_document");


    @Context
    private UriInfo context;

    public MetadataResource() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
            this.corpora = backendInterface.getCorpora();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @GET
    public Response getMetadataContents() {
        String[] branchNames = {"corpora", "speakers", "transcripts", "media", "events", "additionalMaterial"};
        
        for (int i = 0; i < branchNames.length; i++) {
            String branchName = branchNames[i];
            String branchUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(branchName).build().toString();
            response.addContent(new Element(branchName + "_metadata_url").addContent(branchUri));
        }
              
        xmlString = IOUtilities.elementToString(response);
        return Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/events")
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
    @Path("/events/{corpusID}")
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
    @Path("/events/{corpusID}/{eventID}")
    public Response getMetadataXml4Event(@PathParam("corpusID") String corpusID,
                                         @PathParam("eventID") String eventID) throws IOException, Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        Event event = backendInterface.getEvent(eventID);
        String eventXml = event.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(eventXml));

        xmlString = IOUtilities.elementToString(xmlDocument);
        return !corpora.contains(corpusID) || !eventsList.contains(eventID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/speakers")
    public Response getCorpora4Speakers() {
        return getCorpora();
    }

    @GET
    @Path("/speakers/{corpusID}")
    public Response getSpeakers4Corpus(@PathParam("corpusID") String corpusID) throws IOException {
        IDList speakers = backendInterface.getSpeakers4Corpus(corpusID);

        for (String speaker : speakers) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(speaker).build().toString();
            Element listElement = new Element(speakers.getObjectName().toLowerCase() + "_metadata_url");
            listElement.setAttribute("id", speaker).addContent(linkUrl);
            response.addContent(listElement);
        }

        xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : Response.ok(xmlString).build();
    }

    @GET
    @Path("/speakers/{corpusID}/{speakerID}")
    public Response getMetadataXml4Speaker(@PathParam("corpusID") String corpusID,
                                           @PathParam("speakerID") String speakerID) throws IOException, Exception {
        IDList speakersList = backendInterface.getSpeakers4Corpus(corpusID);
        Speaker speaker = backendInterface.getSpeaker(speakerID);
        String speakerXml = speaker.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(speakerXml));

        xmlString = IOUtilities.elementToString(xmlDocument);
        return !corpora.contains(corpusID) || !speakersList.contains(speakerID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : Response.ok(xmlString).build();
    }
    
    
    
    
    @GET
    @Path("/corpora")
    public Response getCorpora4Corpora() {
        return getCorpora();
    }


    @GET
    @Path("/corpora/{corpusID}")
    public Response getMetadataXml4Corpus(@PathParam("corpusID") String corpusID) throws IOException, Exception {
        Corpus corpus = backendInterface.getCorpus(corpusID);
        String corpusXml = corpus.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(corpusXml));

        xmlString = IOUtilities.elementToString(xmlDocument);    
        return !corpora.contains(corpusID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/media")
    public Response getCorpora4Media() {
        return getCorpora();
    }

    @GET
    @Path("/media/{corpusID}")
    public Response getEvents4Corpus4Media(@PathParam("corpusID") String corpusID) throws IOException {
        return !corpora.contains(corpusID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : getEvents4Corpus(corpusID);
    }

    @GET
    @Path("/media/{corpusID}/{eventID}")
    public Response getSpeechEvent4Event(@PathParam("corpusID") String corpusID,
                                         @PathParam("eventID") String eventID) throws IOException {
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
    @Path("/media/{corpusID}/{eventID}/{speechEventID}")
    public Response getMedia4SpeechEvent4Event4Media(@PathParam("corpusID") String corpusID,
                                                     @PathParam("eventID") String eventID,
                                                     @PathParam("speechEventID") String speechEventID) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList audiosList = backendInterface.getAudios4SpeechEvent(speechEventID);
        IDList videosList = backendInterface.getVideos4SpeechEvent(speechEventID);

        for (String audio : audiosList) {
            if (!audio.isEmpty()) {
                String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(audio).build().toString();

                Element listElement = new Element("audio");
                Element audioMetadata = new Element("audio_metadata_url");
                Element metadataElement = new Element("metadata");
                Element metadatumElement1 = new Element("metadatum").setAttribute("key", "duration");
                Element metadatumElement2 = new Element("metadatum").setAttribute("key", "size_in_bytes");

                audioMetadata.addContent(linkUrl);
                // 07-07-2022, changed for issue #41
                //String durationMetadata = backendInterface.getMediaMetadata4Media(eventID, audio).getDuration();
                //String fileSizeMetadata = backendInterface.getMediaMetadata4Media(eventID, audio).getMediaFileSizeInBytes();
                String durationMetadata = Double.toString(backendInterface.getMedia(audio).getDuration());
                String fileSizeMetadata = "0";
                metadatumElement1.addContent(durationMetadata);
                metadatumElement2.addContent(fileSizeMetadata);
                metadataElement.addContent(metadatumElement1);
                metadataElement.addContent(metadatumElement2);

                listElement.addContent(metadataElement);
                listElement.addContent(audioMetadata);
                listElement.setAttribute("id", audio);

                response.addContent(listElement);
            }
        }

        for (String video : videosList) {
            if (!video.isEmpty()) {
                String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(video).build().toString();
                Element listElement = new Element("video_metadata_url");//(speechEventsList.getObjectName().toLowerCase());
                listElement.setAttribute("id", video).addContent(linkUrl);
                response.addContent(listElement);
            }
        }

        xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID)
            ? Response.status(Response.Status.BAD_REQUEST).build()
            : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/media/{corpusID}/{eventID}/{speechEventID}/{mediaID}")
    public Response getMediaMetadata4SpeechEvent4Event4Media(@PathParam("corpusID") String corpusID,
                                                             @PathParam("eventID") String eventID,
                                                             @PathParam("speechEventID") String speechEventID,
                                                             @PathParam("mediaID") String mediaID) throws IOException, Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList audiosList = backendInterface.getAudios4SpeechEvent(speechEventID);
        IDList videosList = backendInterface.getVideos4SpeechEvent(speechEventID);
        // changed 07-07-2022, issue #41
        /*MediaMetadata mediaMetadata = backendInterface.getMediaMetadata4Media(eventID, mediaID);
        String mediaMetadataXml = mediaMetadata.toXML();*/
        String mediaMetadataXml = "<MediaMetadata/>";
        xmlDocument.addContent(IOUtilities.readElementFromString(mediaMetadataXml));
        xmlString = IOUtilities.elementToString(xmlDocument);

        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID) || !audiosList.contains(mediaID) && !videosList.contains(mediaID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/transcripts")
    public Response getCorpora4Transcripts() {
        return getCorpora();
    }

    @GET
    @Path("/transcripts/{corpusID}")
    public Response getEvents4Corpus4Transcripts(@PathParam("corpusID") String corpusID) throws IOException {
        return !corpora.contains(corpusID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : getEvents4Corpus(corpusID);
    }

    @GET
    @Path("/transcripts/{corpusID}/{eventID}")
    public Response getSpeechEvent4Event4Transcripts(@PathParam("corpusID") String corpusID,
                                                     @PathParam("eventID") String eventID) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        return !corpora.contains(corpusID) || !eventsList.contains(eventID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : getSpeechEvent4Event(corpusID, eventID);
    }

    @GET
    @Path("/transcripts/{corpusID}/{eventID}/{speechEventID}")
    public Response getMedia4SpeechEvent4Event4Transcripts(@PathParam("corpusID") String corpusID,
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
    @Path("/transcripts/{corpusID}/{eventID}/{speechEventID}/{transcriptID}")
    public Response getTranscriptMetadata4SpeechEvent4Event4Transcripts(@PathParam("corpusID") String corpusID,
                                                                        @PathParam("eventID") String eventID,
                                                                        @PathParam("speechEventID") String speechEventID,
                                                                        @PathParam("transcriptID") String transcriptID) throws IOException, Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList transcriptsList = backendInterface.getTranscripts4SpeechEvent(speechEventID);

        // changed 07-07-2022, issue #41
        /*TranscriptMetadata transcriptMetadata = backendInterface.getTranscriptMetadata4Transcript(eventID, transcriptID);
        String transcriptMetadataXml = transcriptMetadata.toXML();*/
        String transcriptMetadataXml = "<TranscriptMetadata/>";
        xmlDocument.addContent(IOUtilities.readElementFromString(transcriptMetadataXml));
        xmlString = IOUtilities.elementToString(xmlDocument);

        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID) || !transcriptsList.contains(transcriptID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/additionalMaterial")
    public Response getCorpora4Material() {
        return getCorpora();
    }
    
    // 07-07-2022 : changed because of issue #45
    @GET
    @Path("/additionalMaterial/{corpusID}")
    public Response getMaterial4Corpora(@PathParam("corpusID") String corpusID) throws Exception {
        // 07-07-2022 : changed because of issue #45
        //AdditionalMaterialMetadata material = backendInterface.getAdditionalMaterialMetadata4Corpus(corpusID);
        //String materialXml = material.toXML();
        String materialXml = "<AdditionalMaterial/>";
        System.out.println(materialXml);
        xmlDocument.addContent(IOUtilities.readElementFromString(materialXml));
        xmlString = IOUtilities.elementToString(xmlDocument);
        return !corpora.contains(corpusID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    // Added 24-11-2019 -- simple versions to just get objects for IDs
    
    @GET
    @Path("/event/{eventID}")
    public Response getEvent(@PathParam("eventID") String eventID,
                                           @DefaultValue("xml") @QueryParam("format") String format) {
        try {
            Event event = backendInterface.getEvent(eventID);
            buildResponse = Response.ok(event.toXML()).build();
        } catch (IOException ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    @GET
    @Path("/speaker/{speakerID}")
    public Response getSpeaker(@PathParam("speakerID") String speakerID,
                                           @DefaultValue("xml") @QueryParam("format") String format) {
        try {
            Speaker speaker = backendInterface.getSpeaker(speakerID);
            buildResponse = Response.ok(speaker.toXML()).build();
        } catch (IOException ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    
    // New 10-07-2020, Elena
    @GET
    @Path("/transcript/{transcriptID}/{metadataKeyID}")
    public Response getMetadataKeyValue4Transcript(@PathParam("transcriptID") String transcriptID, 
                                                   @PathParam("metadataKeyID") String metadataKeyID, //e.g. e_se_art
                                                   @DefaultValue("string") @QueryParam("format") String responseFormat){
        
        try {
            
            String result = null;
            //System.out.println(metadataKeyID); e.g. 'e_se_art'
            
            MetadataKey metadataKey = backendInterface.findMetadataKeyByID("v_" + metadataKeyID);
            
            if (metadataKey!=null){
                if (metadataKey.getLevel().equals(ObjectTypesEnum.EVENT)){
                    // changed 07-07-2022, issue #45
                    //String eventID = backendInterface.getEvent4Transcript(transcriptID);
                    String eventID = backendInterface.getEvent4SpeechEvent(backendInterface.getSpeechEvent4Transcript(transcriptID));
                    Event event = backendInterface.getEvent(eventID);
                    if (event!=null){
                        result = event.getMetadataValue(metadataKey);
                    }
                }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEECH_EVENT)){
                    String speechEventID = backendInterface.getSpeechEvent4Transcript(transcriptID);
                    SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
                    if (speechEvent!=null){    
                        result = speechEvent.getMetadataValue(metadataKey);
                    }
                }else if (metadataKey.getLevel().equals(ObjectTypesEnum.SPEAKER) || metadataKey.getLevel().equals(ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT)){
                    result = "not identifiable (speaker id required)";
                }
                
                if (result==null){
                    result = "not documented";
                }
                
                switch (responseFormat){
                    case "xml":
                        String xml = "<metadata-value key=\"" + metadataKeyID + "\" id=" + transcriptID + "\">" + result + "</metadata>";
                        buildResponse = Response.ok(xml, MediaType.APPLICATION_XML).build();
                        break;
                    case "string":
                        buildResponse = Response.ok(result, MediaType.TEXT_PLAIN).build();
                        break;
                    default:
                        buildResponse = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                        break;
                }
            }else{
                buildResponse = Response.status(Response.Status.BAD_REQUEST).entity("MetadataKey \"" + metadataKeyID + "\" does not exist!").build();
            }

        } catch (IOException ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        
        return buildResponse;
     
    }
    
    // New 23-02-2021, Elena
    @GET
    @Path("/speechEvent/{speechEventID}/measureValue")
    public Response getMeasureValueForSpeechEvent(@QueryParam("type") String type, @PathParam("speechEventID") String speechEventID,
                             @QueryParam("reference") String reference, @QueryParam("key") String key) throws Exception {

        String measureValue = backendInterface.getMeasure4SpeechEvent(speechEventID, type, reference).getStatistics().get(key).replace(",", ".");
        Formatter formatter = new Formatter(new StringBuilder(), Locale.US);
        measureValue = String.valueOf(formatter.format("%.2f", Double.parseDouble(measureValue)));
        
        buildResponse = Response.ok(measureValue, MediaType.TEXT_PLAIN).build();
        return buildResponse;
    }
    
}
