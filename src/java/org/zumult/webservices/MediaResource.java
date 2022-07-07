/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import static org.zumult.io.IOUtilities.buildStream;
import org.zumult.io.MediaUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.MediaMetadata;

/**
 *
 * @author josip.batinic
 */
@Path("/media")
@Produces(MediaType.APPLICATION_XML)
public class MediaResource {
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

    public MediaResource() {
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
            this.corpora = backendInterface.getCorpora();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(MediaResource.class.getName()).log(Level.SEVERE, null, ex);
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
    public Response getMedia4SpeechEvents4Event4Corpus(@PathParam("corpusID") String corpusID,
                                                       @PathParam("eventID") String eventID,
                                                       @PathParam("speechEventID") String speechEventID) throws IOException {
        IDList eventsList = backendInterface.getEvents4Corpus(corpusID);
        IDList speechEventsList = backendInterface.getSpeechEvents4Event(eventID);
        IDList audiosList = backendInterface.getAudios4SpeechEvent(speechEventID);
        IDList videosList = backendInterface.getVideos4SpeechEvent(speechEventID);
        System.out.println("audiosList: ");
        System.out.println(audiosList);
        for (String audio : audiosList) {
            if (!audio.isEmpty()) {
                // changed 07-07-2022, issue #41
                //String mediaDFId = backendInterface.getMediaMetadata4Media(eventID, audio).getMediaDigitalFileID();
                String mediaDFId = backendInterface.getMedia(audio).getID();
                String linkUrl = UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(mediaDFId + ".mp3").build().toString();
                Element listElement = new Element("audio");
                Element audioUrl = new Element("audio_file_url");//(speechEventsList.getObjectName().toLowerCase());
                Element audioMetadata = new Element("audio_metadata_url");
                Element metadataElement = new Element("metadata");
                Element metadatumElement1 = new Element("metadatum").setAttribute("key", "duration");
                Element metadatumElement2 = new Element("metadatum").setAttribute("key", "size_in_bytes");

                audioMetadata.addContent(UriBuilder.fromPath(zumultApiBaseURL).path(context.getPath()).path(audio).path("metadata").build().toString());
                audioUrl.addContent(linkUrl);
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
                listElement.setAttribute("id", audio).addContent(audioUrl);
                response.addContent(listElement);
            }
        }

        for (String video : videosList) {
            if (!video.isEmpty()) {
                // changed 07-07-2022, issue #41
                //String mediaDFId = backendInterface.getMediaMetadata4Media(eventID, audio).getMediaDigitalFileID();
                String mediaDFId = backendInterface.getMedia(video).getID();
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
                // 07-07-2022, changed for issue #41
                //String durationMetadata = backendInterface.getMediaMetadata4Media(eventID, audio).getDuration();
                //String fileSizeMetadata = backendInterface.getMediaMetadata4Media(eventID, audio).getMediaFileSizeInBytes();
                String durationMetadata = Double.toString(backendInterface.getMedia(video).getDuration());
                String fileSizeMetadata = "0";
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
    @Path("/{corpusID}/{eventID}/{speechEventID}/{mediaID}/metadata")
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
    
    // issue #7: this is wrong
    @GET
    @Produces({"audio/mp3", "video/mp4"})
    @Path("/{corpusID}/{eventID}/{speechEventID}/{mediaDigitalFileID}.{ext}")
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
        // changed 20-05-2020
        //String mediaFilePath = UriBuilder.fromPath(mediaPath).path(corpusID).path("WEB").path(eventID).path(mediaDigitalFileID + "." + ext).build().toString();
        String mediaFilePath = UriBuilder.fromPath(Configuration.getMediaDistributionPath()).path(corpusID).path("WEB").path(eventID).path(mediaDigitalFileID + "." + ext).build().toString();
        //System.out.println("Trying for " + mediaFilePath);
        File f = new File(mediaFilePath);
        return !corpora.contains(corpusID) || !eventsList.contains(eventID) 
                                           || !speechEventsList.contains(speechEventID) 
                                           || !mediaList.contains(mediaID)
                                           || !f.exists()
            ? Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()
            : buildStream(f, range);
    }
    
    
    // New 20-05-2020
    @GET
    @Path("/media/{mediaID}")
    public Response getMedia(@PathParam("mediaID") String mediaID,
                             @DefaultValue("mp3") @QueryParam("format") String format) {
        try {
            Media media = backendInterface.getMedia(mediaID, MediaUtilities.getFormatForExtension(format));
            String xml = "<media id=\"" + mediaID + "\">" + media.getURL() + "</media>";
            return Response.ok(xml).build();
        } catch (IOException ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }
    
    // New 20-05-2020 // changed 05-06-2020 by elena
    @GET
    @Produces({"audio/mp3", "video/mp4"})
    @Path("/media/stream/{mediaID}")
    public Response getMediaStream( @HeaderParam("Range") String range, 
                                    @PathParam("mediaID") String mediaID,
                                    @DefaultValue("mp3") @QueryParam("format") String format) {
        try {
            Media media = backendInterface.getMedia(mediaID, MediaUtilities.getFormatForExtension(format));
            String mediaFilePath = media.getURL();
            File f = new File(mediaFilePath);
            if(f.exists()){
                return buildStream(f, range);
            }else{
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(mediaID + " does not exist.").build();
            }
            
        } catch (Exception ex) {
            Logger.getLogger(MediaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    // New 27-01-2021
    @GET
    @Path("/media/{mediaID}/image/{time}/download")
    public Response downloadImage(@PathParam("mediaID") String mediaID,
                                  @PathParam("time") String time) {
        try {
            double startTime = Double.parseDouble(time);
            Media image = backendInterface.getMedia(mediaID).getVideoImage(startTime);
            String xml = "<download><file>" + new File(image.getURL()).getName() + "</file></download>";
            
            return Response.ok(xml).build();
        } catch (IOException ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, ex);
            buildResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return buildResponse;
    }

}
