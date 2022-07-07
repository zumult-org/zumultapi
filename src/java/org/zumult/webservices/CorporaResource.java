package org.zumult.webservices;

//import exceptions.ResourceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import static org.zumult.io.IOUtilities.buildStream;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.MediaMetadata;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Speaker;
import org.zumult.objects.Transcript;
import org.zumult.objects.TranscriptMetadata;

@Path("corpora")
@Produces(MediaType.APPLICATION_XML)
public class CorporaResource {
    BackendInterface backendInterface;
    Response buildResponse = null;
    final String zumultApiBaseURL = Configuration.getRestAPIBaseURL();
    final String mediaPath = Configuration.getMediaPath();
    IDList corpora;
    String xmlString;
    Element response = new Element("response");
    Element xmlDocument = new Element("xml_document");



    
    @Context
    private UriInfo context;

    public CorporaResource() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
            this.corpora = backendInterface.getCorpora();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(CorporaResource.class.getName()).log(Level.SEVERE, null, ex);
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
    @Path("{corpusID}")
    public Response getCorpusContents(@PathParam("corpusID") String corpusID) {
        String corpusMetadataUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("metadata").build().toString();
        String eventsUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("events").build().toString();
        String speakersUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("speakers").build().toString();

        response.addContent(new Element("corpus_metadata_url").addContent(corpusMetadataUri));
        response.addContent(new Element("events_url").addContent(eventsUri));
        response.addContent(new Element("speakers_url").addContent(speakersUri));

        xmlString = IOUtilities.elementToString(response);
        
        return !corpora.contains(corpusID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/metadata")
    public Response getMetadata4Corpus(@PathParam("corpusID") String corpusID) throws IOException, Exception {
        Corpus corpus = backendInterface.getCorpus(corpusID);
        String corpusXml = corpus.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(corpusXml));
        xmlString = IOUtilities.elementToString(xmlDocument);
        return !corpora.contains(corpusID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/speakers")
    public Response getSpeakers4Corpus(@PathParam("corpusID") String corpusID) throws IOException {
        IDList speakersList = backendInterface.getSpeakers4Corpus(corpusID);
        for (String speaker : speakersList) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(speaker).build().toString();
            Element listElement = new Element(speakersList.getObjectName().toLowerCase() + "_url");
            listElement.setAttribute("id", speaker).addContent(linkUrl);
            response.addContent(listElement);
        }
        xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/speakers/{speakerID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response getSpeakerXml4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID,
                                                            @PathParam("speakerID") String speakerID,
                                                            @MatrixParam("metadatum") String metadatum) throws IOException {
            IDList speakersList = backendInterface.getSpeakers4Corpus(corpusID);
            Corpus corpus = backendInterface.getCorpus(corpusID);
            
            Set<MetadataKey> metadataKeys = corpus.getSpeakerMetadataKeys();

            if (!corpora.contains(corpusID) || !speakersList.contains(speakerID)) {
                buildResponse = Response.status(Response.Status.BAD_REQUEST).build();
            } else {
                if (metadatum == null) {
                    Element metadataElement = new Element("metadata");

                    for (MetadataKey metadataKey : metadataKeys) {
                        // changed 15-08-2019, TS
                        //String metadataValue = backendInterface.getMetadataValue("v_s_id", speakerID, metadataKey.getID());
                        String metadataValue = backendInterface.getSpeaker(speakerID).getMetadataValue(metadataKey);
                        Element metadatumElement = new Element("metadatum").setAttribute("key", metadataKey.getID()).addContent(metadataValue);
                        metadataElement.addContent(metadatumElement);
                    }
                    response.addContent(metadataElement);

                    Element speakerMetadataElement = new Element("speaker_metadata_url");
                    String speakerMetadataUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("metadata").build().toString();
                    speakerMetadataElement.addContent(speakerMetadataUri);
                    response.addContent(speakerMetadataElement);

                    xmlString = IOUtilities.elementToString(response);
                    buildResponse = Response.ok(xmlString).build();
                } else {
                    boolean validKey = false;
                    String metadatumValue = null;

                    for (MetadataKey metadataKey : metadataKeys) {
                        if (metadatum.equals(metadataKey.getID())) {
                            //metadatumValue = backendInterface.getMetadataValue("v_s_id", speakerID, metadataKey.getID());
                            metadatumValue = backendInterface.getSpeaker(speakerID).getMetadataValue(metadataKey);
                            validKey = true;
                        }
                    }
                    xmlString = metadatumValue;
                    buildResponse = validKey
                            ? Response.ok(xmlString, "text/html; charset=UTF-8").build()
                            : Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
        return buildResponse;
    }
    
    @GET
    @Path("/{corpusID}/speakers/{speakerID}/metadata")
    public Response getMetadata4Speaker4Corpus(@PathParam("corpusID") String corpusID,
                                               @PathParam("speakerID") String speakerID) throws IOException, Exception {
        IDList speakersList = backendInterface.getSpeakers4Corpus(corpusID);
        String speakerXml = backendInterface.getSpeaker(speakerID).toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(speakerXml));
        xmlString = IOUtilities.elementToString(xmlDocument);

        return !corpora.contains(corpusID) || !speakersList.contains(speakerID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
  
    
    @GET
    @Path("/{corpusID}/events")
    public Response getEvents4Corpus(@PathParam("corpusID") String corpusID) throws IOException {
        IDList events = backendInterface.getEvents4Corpus(corpusID);
        for (String event : events) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(event).build().toString();
            Element listElement = new Element(events.getObjectName().toLowerCase() + "_url").setAttribute("id", event);
            listElement.addContent(linkUrl);

            response.addContent(listElement);
        }

        String xmlString = IOUtilities.elementToString(response);
        return !corpora.contains(corpusID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    
    @GET
    @Path("/{corpusID}/events/{eventID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response getEventContents(@PathParam("corpusID") String corpusID, 
                                     @PathParam("eventID") String eventID,
                                     @MatrixParam("metadatum") String metadatum) throws IOException {
        corpora = backendInterface.getCorpora();
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        Corpus corpus = backendInterface.getCorpus(corpusID);
        if (!corpora.contains(corpusID) || !eventsList.contains(eventID)) {
            buildResponse = Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            Set<MetadataKey> metadataKeys = corpus.getEventMetadataKeys();
            if (metadatum == null) {                     
//              IN DGD2Corpus THE FUNCTION getEventMetadataKeys FILTERS OUT GEO DATA: not(starts-with(xpath,'Basisdaten/Ort'))                
                Element metadataElement = new Element("metadata");

                for (MetadataKey metadataKey : metadataKeys) {
                    //String metadataValue = backendInterface.getMetadataValue("v_e_id", eventID, metadataKey.getID());
                    // changed 15-08-2019, TS
                    //String metadataValue = backendInterface.getMetadataValue("v_e_id", eventID, metadataKey.getID());
                    String metadataValue = backendInterface.getEvent(eventID).getMetadataValue(metadataKey);
                    
                    Element metadatumElement = new Element("metadatum").setAttribute("key", metadataKey.getID()).addContent(metadataValue);
                    metadataElement.addContent(metadatumElement);
                }

                String eventMetadataUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("metadata").build().toString();
                String speechEventsUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("speechEvents").build().toString();

                response.addContent(metadataElement);
                response.addContent(new Element("event_metadata_url").addContent(eventMetadataUri));
                response.addContent(new Element("speech_events_url").addContent(speechEventsUri));

                xmlString = IOUtilities.elementToString(response);

                buildResponse = Response.ok(xmlString).build();
            } else {
                boolean validKey = false;
                String metadatumValue = null;

                for (MetadataKey metadataKey : metadataKeys) {
                    if (metadatum.equals(metadataKey.getID())) {
                        //metadatumValue = backendInterface.getMetadataValue("v_e_id", eventID, metadataKey.getID());
                        
                        validKey = true;
                    }
                }
                xmlString = metadatumValue;
                buildResponse = validKey
                    ? Response.ok(xmlString, "text/html; charset=UTF-8").build()
                    : Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
        return buildResponse;
    }
    
    @GET
    @Path("/{corpusID}/events/{eventID}/metadata")
    public Response getMetadata4Event4Corpus(@PathParam("corpusID") String corpusID,
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
    @Path("/{corpusID}/events/{eventID}/speechEvents")
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
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response getSpeechEventContents(@PathParam("corpusID") String corpusID, 
                                           @PathParam("eventID") String eventID, 
                                           @PathParam("speechEventID") String speechEventID,
                                           @MatrixParam("metadatum") String metadatum) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        Corpus corpus = backendInterface.getCorpus(corpusID);

        if (!corpora.contains(corpusID) 
            || !eventsList.contains(eventID) 
            || !speechEventsList.contains(speechEventID)) {
            buildResponse = Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            Set<MetadataKey> metadataKeys = corpus.getSpeechEventMetadataKeys();

            if (metadatum == null) {
                Element metadataElement = new Element("metadata");

                for (MetadataKey metadataKey : metadataKeys) {
                    // changed 15-08-2019
                    //String metadataValue = backendInterface.getMetadataValue("v_se_id", speechEventID, metadataKey.getID());
                    String metadataValue = backendInterface.getSpeechEvent(speechEventID).getMetadataValue(metadataKey);
                    Element metadatumElement = new Element("metadatum").setAttribute("key", metadataKey.getID()).addContent(metadataValue);
                    metadataElement.addContent(metadatumElement);
                }
                String transcriptsUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("transcripts").build().toString();
                String speakersUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("speakersInSpeechEvent").build().toString();
                String mediaUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("media").build().toString();

                response.addContent(metadataElement);

                Element transcriptsElement = new Element("transcripts_url").addContent(transcriptsUri);
                response.addContent(transcriptsElement);

                Element speakersElement = new Element("speakersInSpeechEvent_url").addContent(speakersUri);
                response.addContent(speakersElement);

                Element mediaElement = new Element("media_url").addContent(mediaUri);
                response.addContent(mediaElement);

                xmlString = IOUtilities.elementToString(response);

                buildResponse = Response.ok(xmlString).build();
            } else {
                boolean validKey = false;
                String metadatumValue = null;

                for (MetadataKey metadataKey : metadataKeys) {
                    if (metadatum.equals(metadataKey.getID())) {
                        //metadatumValue = backendInterface.getMetadataValue("v_se_id", speechEventID, metadataKey.getID());
                        metadatumValue = backendInterface.getSpeechEvent(speechEventID).getMetadataValue(metadataKey);
                        validKey = true;
                    }
                }
                buildResponse = validKey
                        ? Response.ok(metadatumValue, "text/html; charset=UTF-8").build()
                        : Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
        return buildResponse;
    }
    
    @GET
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/transcripts")
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
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/transcripts/{transcriptID}")
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
        
        for (String transcriptIdString : transcriptsList) {
            if (!transcriptIdString.isEmpty()) {
                String transcriptMetadataUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("metadata").build().toString();
                String transcriptFileUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("file").build().toString();

                Element listElement = new Element("transcript");
                Element transcriptFile = new Element("transcript_file_url");
                Element transcriptMetadata = new Element("transcript_metadata_url");
                Element metadatumElement1 = new Element("metadatum").setAttribute("key", "duration");
                Element metadatumElement2 = new Element("metadatum").setAttribute("key", "size_in_bytes");
                Element metadatumElement3 = new Element("metadatum").setAttribute("key", "types");
                Element metadatumElement4 = new Element("metadatum").setAttribute("key", "tokens");

                transcriptMetadata.addContent(transcriptMetadataUrl);
                transcriptFile.addContent(transcriptFileUrl);
                String durationMetadata = backendInterface.getTranscriptMetadata4Transcript(eventID, transcriptIdString).getDuration();
                String fileSizeMetadata = backendInterface.getTranscriptMetadata4Transcript(eventID, transcriptIdString).getTranscriptFileSizeInBytes();
                String typesMetadata = backendInterface.getTranscriptMetadata4Transcript(eventID, transcriptIdString).getTypes();
                String tokensMetadata = backendInterface.getTranscriptMetadata4Transcript(eventID, transcriptIdString).getTokens();
                metadatumElement1.addContent(durationMetadata);
                metadatumElement2.addContent(fileSizeMetadata);
                metadatumElement3.addContent(typesMetadata);
                metadatumElement4.addContent(tokensMetadata);

                listElement.addContent(transcriptMetadata);
                listElement.addContent(transcriptFile);
                listElement.setAttribute("id", transcriptIdString);

                response.addContent(listElement);
            }
        }

        String xmlString = IOUtilities.elementToString(response);
        return buildResponse = !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID) || !transcriptsList.contains(transcriptID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/transcripts/{transcriptID}/metadata")
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
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/transcripts/{transcriptID}/file")
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
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/speakersInSpeechEvent")
    public Response getSpeakers4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID, 
                                                          @PathParam("eventID") String eventID, 
                                                          @PathParam("speechEventID") String speechEventID) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList speakers = backendInterface.getSpeakers4SpeechEvent(speechEventID); // THIS IS NOT YET DEFINED OR IMPLEMENTED

        for (String speaker : speakers) {
            String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(speaker).build().toString();
            Element listElement = new Element("speakerInSpeechEvent_url").setAttribute("id", speaker);
            listElement.addContent(linkUrl);
            response.addContent(listElement);
        }
        xmlString = IOUtilities.elementToString(response);

        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/speakersInSpeechEvent/{speakerID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response getSpeaker4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID, 
                                                            @PathParam("eventID") String eventID, 
                                                            @PathParam("speechEventID") String speechEventID,
                                                            @PathParam("speakerID") String speakerID,
                                                            @MatrixParam("metadatum") String metadatum) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList speakers = backendInterface.getSpeakers4SpeechEvent(speechEventID);
        Corpus corpus = backendInterface.getCorpus(corpusID);

        if (!corpora.contains(corpusID)
            || !eventsList.contains(eventID)
            || !speechEventsList.contains(speechEventID)
            || !speakers.contains(speakerID)) {
            buildResponse = Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            Set<MetadataKey> metadataKeys = corpus.getSpeakerInSpeechEventMetadataKeys();

            if (metadatum == null) {
                Element metadataElement = new Element("metadata");

                for (MetadataKey metadataKey : metadataKeys) {
                    // changed 15-08-2015, TS
                    //String metadataValue = backendInterface.getMetadataValue("v_se_id", speechEventID, metadataKey.getID(), speakerID);
                    String metadataValue = backendInterface.getSpeakerInSpeechEvent(speechEventID, speakerID).getMetadataValue(metadataKey);
                    Element metadatumElement = new Element("metadatum").setAttribute("key", metadataKey.getID()).addContent(metadataValue);
                    metadataElement.addContent(metadatumElement);
//                    System.out.println("metadataValue: " + metadataValue);
                }
                response.addContent(metadataElement);

                String speakerInSpeechEventMetadataUri = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path("metadata").build().toString();
                Element speakerInSpeechEventElement = new Element("speakerInSpeechEvent_metadata_url").addContent(speakerInSpeechEventMetadataUri);

                response.addContent(speakerInSpeechEventElement);

                xmlString = IOUtilities.elementToString(response);
                buildResponse = Response.ok(xmlString).build();
            } else {
                boolean validKey = false;
                String metadatumValue = null;

                for (MetadataKey metadataKey : metadataKeys) {
                    if (metadatum.equals(metadataKey.getID())) {
                        //metadatumValue = backendInterface.getMetadataValue("v_se_id", speechEventID, speakerID, metadataKey.getID());
                        metadatumValue = backendInterface.getSpeechEvent(speechEventID).getMetadataValue(metadataKey);
                        validKey = true;
                    }
                }
                if (validKey) {
                    xmlString = metadatumValue;
                    buildResponse = Response.ok(xmlString, "text/html; charset=UTF-8").build();
                } else {
//                        someString = "Invalid metadatum key";
                    buildResponse = Response.status(Response.Status.BAD_REQUEST).build();//.entity(someString).type(MediaType.TEXT_PLAIN).build();
                }
            }
        }
        return buildResponse;
    }
    
    @GET
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/speakersInSpeechEvent/{speakerID}/metadata")
    public Response getSpeakerMetadata4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID,
                                                                 @PathParam("eventID") String eventID,
                                                                 @PathParam("speechEventID") String speechEventID,
                                                                 @PathParam("speakerID") String speakerID) throws IOException, Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList speakers = backendInterface.getSpeakers4SpeechEvent(speechEventID);

        Speaker speaker = backendInterface.getSpeaker(speakerID);
        String speakerXml = speaker.toXML();
        xmlDocument.addContent(IOUtilities.readElementFromString(speakerXml));

        xmlString = IOUtilities.elementToString(xmlDocument);
        return !corpora.contains(corpusID) || !eventsList.contains(eventID) || !speechEventsList.contains(speechEventID) || !speakers.contains(speakerID)
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : Response.ok(xmlString).build();
    }
    
    @GET
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/media")
    public Response getMedia4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID,
                                                       @PathParam("eventID") String eventID,
                                                       @PathParam("speechEventID") String speechEventID) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList audiosList = backendInterface.getAudios4SpeechEvent(speechEventID);
        IDList videosList = backendInterface.getVideos4SpeechEvent(speechEventID);
        for (String audio : audiosList) {
            if (!audio.isEmpty()) {
                String mediaDFId = backendInterface.getMediaMetadata4Media(eventID, audio).getMediaDigitalFileID();
                String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(mediaDFId + ".mp3").build().toString();
                Element listElement = new Element("audio");
                Element audioUrl = new Element("audio_file_url");//(speechEventsList.getObjectName().toLowerCase());
                Element audioMetadata = new Element("audio_metadata_url");
                Element metadataElement = new Element("metadata");
                Element metadatumElement1 = new Element("metadatum").setAttribute("key", "duration");
                Element metadatumElement2 = new Element("metadatum").setAttribute("key", "size_in_bytes");

                audioMetadata.addContent(UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(audio).path("metadata").build().toString());
                audioUrl.addContent(linkUrl);
                String durationMetadata = backendInterface.getMediaMetadata4Media(eventID, audio).getDuration();
                String fileSizeMetadata = backendInterface.getMediaMetadata4Media(eventID, audio).getMediaFileSizeInBytes();
                metadatumElement1.addContent(durationMetadata);
                metadatumElement2.addContent(fileSizeMetadata);
                metadataElement.addContent(metadatumElement1);
                metadataElement.addContent(metadatumElement2);

                listElement.addContent(metadataElement);
                listElement.addContent(audioMetadata);
                listElement.setAttribute("id", audio).addContent(audioUrl);
                response.addContent(listElement);
            }
        }

        for (String video : videosList) {
            if (!video.isEmpty()) {
                String mediaDFId = backendInterface.getMediaMetadata4Media(eventID, video).getMediaDigitalFileID();
//                        System.out.println("mediaDFId: " + mediaDFId);
                String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(mediaDFId + ".mp4").build().toString();
                Element listElement = new Element("video");
                Element audioUrl = new Element("video_file_url");//(speechEventsList.getObjectName().toLowerCase());
                Element audioMetadata = new Element("video_metadata_url");
                Element metadataElement = new Element("metadata");
                Element metadatumElement1 = new Element("metadatum").setAttribute("key", "duration");
                Element metadatumElement2 = new Element("metadatum").setAttribute("key", "size_in_bytes");

                audioMetadata.addContent(UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(video).path("metadata").build().toString());
                audioUrl.addContent(linkUrl);
                String durationMetadata = backendInterface.getMediaMetadata4Media(eventID, video).getDuration();
                String fileSizeMetadata = backendInterface.getMediaMetadata4Media(eventID, video).getMediaFileSizeInBytes();
                metadatumElement1.addContent(durationMetadata);
                metadatumElement2.addContent(fileSizeMetadata);
                metadataElement.addContent(metadatumElement1);
                metadataElement.addContent(metadatumElement2);

                listElement.addContent(metadataElement);
                listElement.addContent(audioMetadata);

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
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/media/{mediaID}/metadata")
    public Response getMediaMetadataFile4Media4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID, 
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
    @Produces({"audio/mp3", "video/mp4"})
    @Path("/{corpusID}/events/{eventID}/speechEvents/{speechEventID}/media/{mediaDigitalFileID}.{ext}")
    public Response getMediaFile4SpeechEvents4Event4Corpus(@HeaderParam("Range") String range,
                                                                @PathParam("corpusID") String corpusID,
                                                                @PathParam("eventID") String eventID,
                                                                @PathParam("speechEventID") String speechEventID,
                                                                @PathParam("mediaDigitalFileID") String mediaDigitalFileID, 
                                                                @PathParam("ext") String ext) throws Exception {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList mediaList = ext.equals("mp3")
                ? backendInterface.getAudios4SpeechEvent(speechEventID)
                : backendInterface.getVideos4SpeechEvent(speechEventID);
        String mediaID = mediaDigitalFileID.replace("_DF_01", "");
            // Thomas, 14-01-2019
            // This is backend specific!
            // Try the following instead:
            // Media media = backendInterface.getMedia(audioDigitalFileID);
            // String audioFilePath = media.getURL();
            String mediaFilePath = UriBuilder.fromPath(mediaPath).path(corpusID).path("WEB").path(eventID).path(mediaDigitalFileID + "." + ext).build().toString();
            File f = new File(mediaFilePath);
            // Thomas, 14-01-2019
            // if it does not exist, the service will return f=null, i.e. an empty file
            return !corpora.contains(corpusID) || !eventsList.contains(eventID) 
                                               || !speechEventsList.contains(speechEventID) 
                                               || !mediaList.contains(mediaID)
                                               || !f.exists()
                ? Response.status(Response.Status.BAD_REQUEST).build()
                : buildStream(f, range);
    }
}
