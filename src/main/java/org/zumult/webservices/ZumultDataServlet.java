/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.webservices;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import org.exmaralda.folker.utilities.TimeStringFormatter;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEITranscriptConverter;
import org.zumult.io.MausConnection;
import org.zumult.io.PraatConnection;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.ISOTEITranscript;

/**
 *
 * @author thomas.schmidt
 */
public class ZumultDataServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // read the command parameter
        String command = request.getParameter("command");
        
        // delegate depending on the command
        switch (command){
            case "download" :
                download(request, response);
                break;
            case "getEventHTML" : 
                getEventHTML(request, response);
                break;
            case "getEventMetadataHTML" : 
                getEventMetadataHTML(request, response);
                break;
            case "getSpeechEventMetadata" : 
                getSpeechEventMetadata(request, response);
                break;
            // new for #175 (and for completeness sake)
            case "getSpeakerMetadata" : 
                getSpeakerMetadata(request, response);
                break;
            case "getEventMetadataTitle" : 
                getEventMetadataTitle(request, response);
                break;
            case "getVTT" : 
                getVTT(request, response);
                break;
            case "getWordlist" :
                getWordlist(request, response);
                break;
            case "getTranscript" :
                getTranscript(request, response);
                break;
            case "getAudio" :
                getAudio(request, response);
                break;
            case "getVideo" :
                getVideo(request, response);
                break;
            case "getVideoImage" :
                getVideoImage(request, response);
                break;
            case "getStillSeries" :  // new for #235
                getStillSeries(request, response);
                break;
            case "getExpansion" :
                getExpansion(request, response);
                break;
            case "getSVG" :
                getSVG(request, response);
                break;
            case "getAnnotations" :
                getAnnotations(request, response);
                break;
            case "getPartitur" :
                getPartitur(request, response);
                break;
            case "getProtocol" :
                getProtocol(request, response);
                break;
            case "getCoordinatesForTime" :
                getCoordinatesForTime(request, response);
                break;                
            case "getMetadataKeys" :
                getMetadataKeys(request, response);
                break;                
            // issue #55
            case "printDownloadWordlist" :
                printDownloadWordlist(request, response);
                break;   
            case "getMausAlignment" : 
                getMausAlignment(request, response);
                break;   
            case "getMicroView" : 
                getMicroView(request, response);
                break;   
            default : 
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write("<span>Unknown command</span>");             
                response.getWriter().close();                            
                break;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    // new for issue #175
    private void getSpeakerMetadata(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String speechEventID = request.getParameter("speechEventID");
            String speakerID = request.getParameter("speakerID");
            String transcriptID = request.getParameter("transcriptID");
            String format = request.getParameter("format");
            if (format==null || format.length()==0){
                format = "html";
            }
            
            IDList speakerIDs = new IDList("Speakers");
            // three cases
            if (speechEventID!=null){
                // 1. we have a speech event ID
                speakerIDs = backend.getSpeakers4SpeechEvent(speechEventID);                
            } else if (speakerID!=null && transcriptID!=null){
                // 2. the speakerID is a transcript sigle, not the corpus ID
                String corpusSpeakerID = backend.getTranscript(transcriptID).getSpeakerIDBySpeakerInitials(speakerID);
                speakerIDs.add(corpusSpeakerID);                
            } else {
                // 3. we have a single speaker ID
                speakerIDs.add(speakerID);
            }
            if (!speakerIDs.isEmpty()){
                String allXML = "<Speakers>";
                for (String id : speakerIDs){
                    Speaker speaker = backend.getSpeaker(id);
                    String speakerXML = IOHelper.ElementToString(IOHelper.DocumentFromText(speaker.toXML()).getDocumentElement());        
                    allXML+=speakerXML;
                }
                allXML += "</Speakers>";
                String result = allXML;
                if (format.equals("html")){
                    String xslPath = Configuration.getSpeaker2HTMLStylesheet();
                    result = new IOHelper().applyInternalStylesheetToString(xslPath, allXML);
                }
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(result);             
                response.getWriter().close();            
            } else {
                String errorHTML = "<div>Speaker " + speakerID + " not found.</div>";
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(errorHTML);             
                response.getWriter().close();            
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } 
    }
    
    private void getSpeechEventMetadata(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String eventID = request.getParameter("eventID");
            String speechEventID = request.getParameter("speechEventID");
            String transcriptID = request.getParameter("transcriptID");
            String speakerID = request.getParameter("speakerID");
            String format = request.getParameter("format");
            if (format==null || format.length()==0){
                format = "html";
            }
            
            IDList speechEventIDs = new IDList("SpeechEvents");
            
            if (eventID!=null){
                speechEventIDs = backend.getSpeechEvents4Event(eventID);
            } else if (speechEventID!=null){
                speechEventIDs.add(speechEventID);
            } else if (transcriptID!=null){
                speechEventIDs.add(backend.getSpeechEvent4Transcript(transcriptID));
            } else if (speakerID!=null){
                speechEventIDs = backend.getSpeechEvents4Speaker(speakerID);
            }
            
            if (!speechEventIDs.isEmpty()){
                String allXML = "<SpeechEvents>";
                for (String id : speechEventIDs){
                    SpeechEvent speechEvent = backend.getSpeechEvent(id);
                    String speechEventXML = IOHelper.ElementToString(IOHelper.DocumentFromText(speechEvent.toXML()).getDocumentElement());        
                    allXML+=speechEventXML;
                }
                allXML += "</SpeechEvents>";
                String result = allXML;
                if (format.equals("html")){
                    String xslPath = Configuration.getSpeechEvent2HTMLStylesheet();
                    result = new IOHelper().applyInternalStylesheetToString(xslPath, allXML);
                }
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(result);             
                response.getWriter().close();            
            } else {
                String errorHTML = "<div>Speaker " + speakerID + " not found.</div>";
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(errorHTML);             
                response.getWriter().close();            
            }
            
            
            
            if (speechEventID!=null){
                // N.B. the idea seems to be to get the corresponding event as input for the XSL transformation
                // and the XSL expects a parameter specifying the speech event
                // should be okay (just unnecessary) for COMA because events and speech events are identical there...
                // changed for issue #175
                //Event event = backend.getEvent(eventID.substring(0,12));
                Event event = backend.getEvent(backend.getEvent4SpeechEvent(speechEventID));
                String eventXML = event.toXML();        
                // changed for issue #175
                String[][] param = {
                    {"speechEventID", speechEventID},
                    {"transcriptID", transcriptID},
                    {"eventID", eventID}
                };
                // change for issue #175
                //String eventHTML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/speechEvent2Table.xsl", eventXML, param);
                String xslPath = Configuration.getSpeechEvent2HTMLStylesheet();
                String speechEventHTML = new IOHelper().applyInternalStylesheetToString(xslPath, eventXML, param);
                
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(speechEventHTML);             
                response.getWriter().close();            
            }  else {
                String errorHTML = "<div>Event " + speechEventID + " not found.</div>";
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(errorHTML);             
                response.getWriter().close();            
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } 
    }
    
    
    

    private void getEventMetadataHTML(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            // this does not really make sense : we want event metadata, so why are we passing speechEventID as parameter?
            String speechEventID = request.getParameter("speechEventID");
            if (speechEventID==null){
                String transcriptID = request.getParameter("transcriptID");
                if (transcriptID!=null){
                    speechEventID = backend.getSpeechEvent4Transcript(transcriptID);
                }                
            }
            if (speechEventID!=null){
                // changed for issue #175
                //Event event = backend.getEvent(eventID.substring(0,12));
                Event event = backend.getEvent(backend.getEvent4SpeechEvent(speechEventID));
                String eventXML = event.toXML();        
                // changed for issue #175
                //String eventHTML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/folkEvent2html_table.xsl", eventXML);
                String xslPath = Configuration.getEvent2HTMLStylesheet();
                String eventHTML = new IOHelper().applyInternalStylesheetToString(xslPath, eventXML);
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(eventHTML);             
                response.getWriter().close();            
            } else {
                String errorHTML = "<div>Event " + speechEventID + " not found.</div>";
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(errorHTML);             
                response.getWriter().close();            
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } 
    }


    private void getEventHTML(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String speechEventID = request.getParameter("speechEventID");
                       
            if (speechEventID!=null){
                Event event = backend.getEvent(backend.getEvent4SpeechEvent(speechEventID));
                String eventXML = event.toXML();        
                // change for issue #175
                String xslPath = Configuration.getEvent2HTMLStylesheet();
                String speechEventHTML = new IOHelper().applyInternalStylesheetToString(xslPath, eventXML);
                
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(speechEventHTML);             
                response.getWriter().close();            
            }  else {
                String errorHTML = "<div>Event " + speechEventID + " not found.</div>";
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(errorHTML);             
                response.getWriter().close();            
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } 
    }



    private void getEventMetadataTitle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String eventID = request.getParameter("eventID");
            // changed for #175
            // this is still FOLK specific, though: why should the event title be derived from the first speech event?? Makes no sense for other corpora...
            String firstSpeechEventID = backend.getSpeechEvents4Event(eventID).get(0);
            String eventTitleMetadataKey = Configuration.getEventTitleMetadataKey(); 
            String title = backend.getSpeechEvent(firstSpeechEventID).getMetadataValue(backend.findMetadataKeyByID(eventTitleMetadataKey));           
            String html = "<span>" + title + "</span>";
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");                            
            response.getWriter().write(html);             
            response.getWriter().close();            
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } 
    }

    private void getVTT(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String subtitleType = request.getParameter("subtitleType");
            if (subtitleType==null){
                subtitleType = "trans";
            }
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            
            String transcriptXML = transcript.toXML();
            /*boolean flattenSeg = true;
            if (flattenSeg){
                //transcriptXML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/flattenSegHierarchy.xsl", transcriptXML);
                transcriptXML = new IOHelper().applyInternalStylesheetToString("/org/exmaralda/tei/xml/flattenSegHierarchy.xsl", transcriptXML);
            }*/
            
            
            String[][] parameters = {
                {"TYPE", subtitleType}
            };
            String vtt = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/isotei2vtt.xsl", transcriptXML, parameters);
            response.setContentType("text/vtt");
            response.setCharacterEncoding("UTF-8");                            
            response.getWriter().write(vtt);             
            response.getWriter().close();            
            
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }
    
    private void getStillSeries(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            
            int numberOfImages = 6;
            
            String numerOfImagesProvided = request.getParameter("numberOfImages");
            if (numerOfImagesProvided!=null){
                numberOfImages = Integer.parseInt(numerOfImagesProvided);
            }
            
            
            String videoID = request.getParameter("videoID");
            String startTimeProvided = request.getParameter("startTime");
            String endTimeProvided = request.getParameter("endTime");

            String transcriptID = request.getParameter("transcriptID");
            String startTokenID = request.getParameter("startTokenID");
            String endTokenID = request.getParameter("endTokenID");

            
            if (videoID==null){
                if (backend.getVideos4Transcript(transcriptID).isEmpty()){
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("<div><error/><div>");
                    response.getWriter().close();             
                    return;
                }
                videoID = backend.getVideos4Transcript(transcriptID).get(0);            
            }
            
            
            Transcript transcript = null;
            if (transcriptID!=null){
                transcript = backend.getTranscript(transcriptID);
            }
            Media video = backend.getMedia(videoID, Media.MEDIA_FORMAT.MPEG4_ARCHIVE);

            File downloadDirectory = new File(getServletContext().getRealPath("/downloads/"));
            
            double startTime = 0.0;
            double endTime = -1;
            
            if (startTokenID!=null && transcript!=null){
                startTime = transcript.getTimeForID(startTokenID);
            } else if (startTimeProvided!=null){
                startTime = Double.parseDouble(startTimeProvided);
            }
            if (endTokenID!=null && transcript!=null){
                endTime = transcript.getNextTimeForID(endTokenID);
            } else if (endTimeProvided!=null){
                endTime = Double.parseDouble(endTimeProvided);
            }
            if (endTime<0){
                endTime = video.getDuration() - 0.1;
            }
            
            // make sure that startTime and endTime are at least 0.6s apart
            if (endTime - startTime < 0.6){
                double whatsMissing = 0.6 - (endTime - startTime);
                startTime = Math.max(0.0, startTime - whatsMissing / 2);
                endTime = endTime + whatsMissing / 2;
            }
            
            String resultHTML = "<div>";
            
            double delta = (endTime - startTime) / (numberOfImages - 1);
            for (int i=0; i<numberOfImages; i++){
                double thisTime = startTime + i * delta;
                Media videoImage = video.getVideoImage(thisTime);
                File targetFile = new File(downloadDirectory, "ZuMult-Image_" + UUID.randomUUID() + ".png");
                Files.move(new File(videoImage.getURL()).toPath(), targetFile.toPath());
                String imgHTML = "<img class=\"thumb-still\"  onclick=\"largerImage(this)\" src=\"../downloads/" + targetFile.getName() + "\" width=\"100px\"/>";
                resultHTML+=
                        "<div class=\"thumb-still\">" 
                        + imgHTML + "<br/>"
                        + "<span class=\"thumb-time\">" + TimeStringFormatter.formatSeconds(thisTime, true, 2) + "</span>"
                        + "</div>"
                        ;
                
            }
            
            resultHTML+="</div>";
            
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(resultHTML);
            response.getWriter().close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }
    
    private void getMausAlignment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String transcriptID = request.getParameter("transcriptID");
        String annotationBlockID = request.getParameter("annotationBlockID");
        String format = request.getParameter("format");
        String xml = new MausConnection().getMausAligment(transcriptID, annotationBlockID, format);
        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(xml);
        response.getWriter().close();
    }
    
    private void getMicroView(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String annotationBlockID = request.getParameter("annotationBlockID");
            String xPerSecond = request.getParameter("xPerSecond");
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            
            Transcript transcript = backend.getTranscript(transcriptID);
            AnnotationBlock annotationBlock = backend.getAnnotationBlock(transcriptID, annotationBlockID);
            double startTime = transcript.getTimeForID(annotationBlock.getStart());
            double endTime = transcript.getTimeForID(annotationBlock.getEnd());
            IDList audioIDs = backend.getAudios4Transcript(transcriptID);
            if (audioIDs.isEmpty()){
                throw new IOException("No audio");
            }
            Media audio = backend.getMedia(audioIDs.get(0), Media.MEDIA_FORMAT.WAV);
            Media partAudio = audio.getPart(startTime, endTime);
            File audioFile = new File(partAudio.getURL());
            
            PraatConnection praatConnection = new PraatConnection();
            String pitchXML = praatConnection.getPitchAsXML(audioFile);
            
            String[] xmlArray = new String[1];
            
            Thread mausThread = new Thread(){                
                @Override
                public void run() {
                    String mausXML = new MausConnection().getMausAligment(transcriptID, annotationBlockID, "EXB");
                    xmlArray[0] = mausXML;
                }                
            };
            mausThread.start();
            
            //String mausXML = new MausConnection().getMausAligment(transcriptID, annotationBlockID, "EXB");
            
            List<File> videoStills = new ArrayList();
            Thread ffmpegThread = new Thread(){
                @Override
                public void run() {
                    try {
                        IDList videoIDs = backend.getVideos4Transcript(transcriptID);
                        if (!(videoIDs.isEmpty())){
                            Media video = backend.getMedia(videoIDs.get(0), Media.MEDIA_FORMAT.MPEG4_ARCHIVE);
                            File downloadDirectory = new File(getServletContext().getRealPath("/downloads/"));
                            for (double time = startTime; time<endTime; time+=0.5){
                                Media videoStill = video.getVideoImage(time);
                                File videoStillFile = new File(videoStill.getURL());
                                File targetFile = new File(downloadDirectory, videoStillFile.getName());
                                Files.move(videoStillFile.toPath(), targetFile.toPath());
                                videoStills.add(targetFile);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                                
            };
            ffmpegThread.start();
            
             // Wait for threads to complete
            try {
                mausThread.join();
                ffmpegThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Main thread interrupted");
            }            
            
            String allXML = "<document>";
            allXML+=pitchXML.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            allXML+=xmlArray[0];
            allXML+="<video-stills>";
            for (File videoStill : videoStills){
                allXML+="<video-still>";
                allXML+=videoStill.getName();
                allXML+="</video-still>";
            }
            allXML+="</video-stills>";
            allXML+="</document>";
            
            //System.out.println(allXML);
            
            String[][] parameters ={
                {"X_PER_SECOND", xPerSecond}
            };
            String svg = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/pitch2SVG.xsl", allXML);
            
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(svg);
            response.getWriter().close();
            
        } catch (TransformerException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);            
        }
        
    }
    
    
    private void getVideoImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String videoID = request.getParameter("videoID");
            String position = request.getParameter("position");
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Media video = backend.getMedia(videoID, Media.MEDIA_FORMAT.MPEG4_ARCHIVE);
            Media videoImage = video.getVideoImage(Double.parseDouble(position));
            
            File downloadDirectory = new File(getServletContext().getRealPath("/downloads/"));
            File targetFile = new File(downloadDirectory, "ZuMult-Image_" + UUID.randomUUID() + ".png");
            Files.move(new File(videoImage.getURL()).toPath(), targetFile.toPath());
            
            
            String responseXML = "<download>";
            responseXML+="<videoID>" + videoID + "</videoID>";
            responseXML+="<position>" + position + "</position>";
            responseXML+="<file type=\"png\">" + targetFile.getName() + "</file>";
            responseXML+="</download>";
            
            
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(responseXML);
            response.getWriter().close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
            
        }

        
    }
    
    private void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*
                audioArchive : $('#audioArchive').prop('checked'),
                videoArchive : $('#videoArchive').prop('checked'),
                video2Archive : $('#video2Archive').prop('checked'),
                transcriptISO : $('#transcriptISO').prop('checked'),
                transcriptFLN : $('#transcriptFLN').prop('checked'),
                transcriptEXB : $('#transcriptEXB').prop('checked'),
                transcriptEAF : $('#transcriptEAF').prop('checked'),
                transcriptPraat : $('#transcriptPraat').prop('checked'),
                transcriptHTML : $('#transcriptHTML').prop('checked'),
                transcriptTXT : $('#transcriptTXT').prop('checked'),
                transcriptPartiturHTML : $('#transcriptPartiturHTML').prop('checked'),
                transcriptPartiturRTF : $('#transcriptPartiturRTF').prop('checked')
        
        */
        boolean audioArchive = Boolean.parseBoolean(request.getParameter("audioArchive"));
        boolean videoArchive = Boolean.parseBoolean(request.getParameter("videoArchive"));
        boolean video2Archive = Boolean.parseBoolean(request.getParameter("video2Archive"));
        boolean transcriptISO = Boolean.parseBoolean(request.getParameter("transcriptISO"));
        boolean transcriptFLN = Boolean.parseBoolean(request.getParameter("transcriptFLN"));
        boolean transcriptEXB = Boolean.parseBoolean(request.getParameter("transcriptEXB"));
        boolean transcriptEAF = Boolean.parseBoolean(request.getParameter("transcriptEAF"));
        boolean transcriptPraat = Boolean.parseBoolean(request.getParameter("transcriptPraat"));
        boolean transcriptHTML = Boolean.parseBoolean(request.getParameter("transcriptHTML"));
        boolean transcriptTXT = Boolean.parseBoolean(request.getParameter("transcriptTXT"));
        boolean transcriptPartiturHTML = Boolean.parseBoolean(request.getParameter("transcriptPartiturHTML"));
        boolean transcriptPartiturRTF = Boolean.parseBoolean(request.getParameter("transcriptPartiturRTF"));
        
        String transcriptID = request.getParameter("transcriptID");
        String startSelection = request.getParameter("startSelection");
        String endSelection = request.getParameter("endSelection");

        try {
            List<File> allFiles = new ArrayList<>();
            
            
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            Transcript partTranscript = transcript;
            if (startSelection!=null && endSelection!=null && startSelection.length()>0 && endSelection.length() > 0){
                partTranscript = transcript.getPart(startSelection, endSelection, true);
            }
            
            System.out.println("[Download] Got transcript");
            
            // 0. ISO TRANSCRIPT
            if (transcriptISO){
                File isoFile = File.createTempFile(transcriptID + "_", ".xml");
                isoFile.deleteOnExit();
                allFiles.add(isoFile);
                IOHelper.writeDocument(partTranscript.getDocument(), isoFile);

                System.out.println("[Download] Written ISO transcript to " + isoFile.getAbsolutePath());
            }

            
            double startTime = 0.0;
            double endTime = 60.0;
            if (startSelection!=null && endSelection!=null && startSelection.length()>0 && endSelection.length() > 0){
                startTime = transcript.getTimeForID(startSelection);
                AnnotationBlock endAnnotationBlock = backend.getAnnotationBlock(transcriptID, endSelection);
                endTime = transcript.getTimeForID(endAnnotationBlock.getEnd());
            }
            // security: not longer than 2 minutes!
            endTime = Math.min(endTime, startTime + 120.0);
            
            IDList audioIDs = backend.getAudios4Transcript(transcriptID);
            IDList videoIDs = backend.getVideos4Transcript(transcriptID);
            
            
            // AUDIO
            if (audioArchive){
                for (String audioID : audioIDs){
                    Media partAudio = backend.getMedia(audioID, Media.MEDIA_FORMAT.WAV).getPart(startTime, endTime);
                    allFiles.add(new File(partAudio.getURL()));
                    System.out.println("[Download] Added audio " + partAudio.getURL());
                }
            }
            
            // 1st VIDEO
            if (videoArchive && !videoIDs.isEmpty()){
                Media partVideo = backend.getMedia(videoIDs.get(0), Media.MEDIA_FORMAT.MPEG4_ARCHIVE).getPart(startTime, endTime);
                allFiles.add(new File(partVideo.getURL()));
                System.out.println("[Download] Added video " + partVideo.getURL());                
            }

            // 2nd VIDEO
            if (video2Archive && videoIDs.size()>1){
                Media partVideo = backend.getMedia(videoIDs.get(1), Media.MEDIA_FORMAT.MPEG4_ARCHIVE).getPart(startTime, endTime);
                allFiles.add(new File(partVideo.getURL()));
                System.out.println("[Download] Added video " + partVideo.getURL());                
            }
            
            if (transcriptFLN){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.FLN);
                File outFile = File.createTempFile(transcriptID + "_", ".fln");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("UTF-8"));
                allFiles.add(outFile);
                System.out.println("[Download] Written FLN transcript to " + outFile.getAbsolutePath());
            }
            
            if (transcriptEXB){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.EXB);
                File outFile = File.createTempFile(transcriptID + "_", ".exb");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("UTF-8"));
                allFiles.add(outFile);
                System.out.println("[Download] Written EXB transcript to " + outFile.getAbsolutePath());
            }

            if (transcriptEAF){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.EAF);
                File outFile = File.createTempFile(transcriptID + "_", ".eaf");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("UTF-8"));
                allFiles.add(outFile);
                System.out.println("[Download] Written EAF transcript to " + outFile.getAbsolutePath());
            }

            if (transcriptPraat){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.PRAAT);
                File outFile = File.createTempFile(transcriptID + "_", ".textGrid");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("UTF-8"));
                allFiles.add(outFile);
                System.out.println("[Download] Written PRAAT transcript to " + outFile.getAbsolutePath());
            }

            if (transcriptHTML){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.HTML);
                File outFile = File.createTempFile(transcriptID + "_", ".html");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("UTF-8"));
                allFiles.add(outFile);
                System.out.println("[Download] Written HTML transcript to " + outFile.getAbsolutePath());
            }

            if (transcriptTXT){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.TXT);
                File outFile = File.createTempFile(transcriptID + "_", ".txt");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("UTF-8"));
                allFiles.add(outFile);
                System.out.println("[Download] Written TXT transcript to " + outFile.getAbsolutePath());
            }

            if (transcriptPartiturHTML){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.PARTITUR_HTML);
                File outFile = File.createTempFile(transcriptID + "_", ".html");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("UTF-8"));
                allFiles.add(outFile);
                System.out.println("[Download] Written Partitur HTML transcript to " + outFile.getAbsolutePath());
            }

            if (transcriptPartiturRTF){
                String converted = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.PARTITUR_RTF);
                File outFile = File.createTempFile(transcriptID + "_", ".rtf");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), converted.getBytes("ISO-8859-1"));
                allFiles.add(outFile);
                System.out.println("[Download] Written Partitur RTF transcript to " + outFile.getAbsolutePath());
            }

            /*String debugVideoURL = "";
            for (String videoID : videoIDs){
                Media partVideo = backend.getMedia(videoID, Media.MEDIA_FORMAT.MPEG4_ARCHIVE).getPart(startTime, endTime);
                allFiles.add(new File(partVideo.getURL()));
                System.out.println("[Download] Added video " + partVideo.getURL());
                debugVideoURL = partVideo.getURL();
            }*/
            
            
            
            //File zipFile = new File(Configuration.getMediaSnippetsPath(), "ZuMult-Download_" + UUID.randomUUID() + ".zip");      
            File downloadDirectory = new File(getServletContext().getRealPath("/downloads/"));
            System.out.println("Download directory : " + downloadDirectory.getAbsolutePath());
            File zipFile = new File(downloadDirectory, "ZuMult-Download_" + UUID.randomUUID() + ".zip");
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));        
            //File[] toBeZipped = {transcriptFile, htmlTranscriptFile, audioFile, eventDocFile, htmlEventDocFile};            

            // add all files to the zip file
            for (File f : allFiles){
                FileInputStream inputStream = new FileInputStream(f);
                BufferedInputStream origin = new BufferedInputStream(inputStream);
                ZipEntry entry = new ZipEntry(f.getName());
                out.putNextEntry(entry);
                byte data[] = new byte[2048];
                int count;
                while((count = origin.read(data, 0, 2048)) != -1) {
                       out.write(data, 0, count);
                    }
                out.closeEntry();
                origin.close();
                //f.delete();
            }            
            out.close();
            
            
            String responseXML = "<download>";
            responseXML+="<transcriptID>" + transcriptID + "</transcriptID>";
            responseXML+="<startSelection>" + startSelection + "</startSelection>";
            responseXML+="<endSelection>" + endSelection + "</endSelection>";
            responseXML+="<startTime>" + startTime + "</startTime>";
            responseXML+="<endTime>" + endTime + "</endTime>";
            //responseXML+="<videoURL>" + debugVideoURL + "</videoURL>";
            for (String audioID : audioIDs){
                responseXML+="<audio>" + audioID + "</audio>";                
            }
            for (String videoID : videoIDs){
                responseXML+="<video>" + videoID + "</video>";                
            }
            responseXML+="<file type=\"zip\">" + zipFile.getName() + "</file>";
            responseXML+="</download>";
            
            
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(responseXML);            
            response.getWriter().close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }

    private void getWordlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String wordlistID = request.getParameter("wordlistID");
            String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");
            
            String tokenListArray = request.getParameter("tokenList");

            
            // get the transcript
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            // get the part of the transcript if applicable
            if (startAnnotationBlockID!=null && endAnnotationBlockID!=null && startAnnotationBlockID.length()>0 && endAnnotationBlockID.length()>0){
                transcript = transcript.getPart(startAnnotationBlockID, endAnnotationBlockID, true);
            }
            
            // get the reference wordlist if applicable
            /*String pathToWordList = new File(getServletContext().getRealPath("/data/" + wordlistID + ".xml"))
                    .toURI().toString();*/
            String pathToWordList = pathToWordList(wordlistID);
            
            // generate the wordlist for the transcript
            TokenList lemmaList4Transcript = transcript.getTokenList("lemma");

            // do an XSL transformation to get the desired HTML representation of the wordlist
            String[][] parameters2 ={
                {"TOKEN_LIST_URL", pathToWordList},
                {"TOKEN_LIST_ARRAY", tokenListArray}
            }; 
            String wordListHTML = new IOHelper().applyInternalStylesheetToString(Constants.WORDLIST2HTML_STYLESHEET, lemmaList4Transcript.toXML(), parameters2);
            
            
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");                            
            response.getWriter().write(wordListHTML);             
            response.getWriter().close();            
            
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }        
    }
    
    // issue #55
    private void printDownloadWordlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String wordlistID = request.getParameter("wordlistID");
            String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");
            
            String tokenListArray = request.getParameter("tokenList");
            
            /*
            <!-- one of : wordformsLemma | wordformsTranscribed -->
            <xsl:param name="WORD_FORMS">wordformsLemma</xsl:param>
            
            <!-- one of : selectionAll | selectionSelected | selectionUnselected -->
            <xsl:param name="SELECTION">selectionAll</xsl:param>
            
            <!-- one of : sortABCFreq | sortABC | sort321 | sortChrono -->
            <xsl:param name="SORTING">sortABCFreq</xsl:param>            
            */
            
            String wordForms = request.getParameter("wordForms");
            String selection = request.getParameter("selection");
            String sorting = request.getParameter("sorting");
            
            //System.out.println("Sorting = " + sorting);
            
            // one of outputDownload | outputPrint
            String output = request.getParameter("output");
            
            // get the transcript
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            // get the part of the transcript if applicable
            if (startAnnotationBlockID!=null && endAnnotationBlockID!=null && startAnnotationBlockID.length()>0 && endAnnotationBlockID.length()>0){
                transcript = transcript.getPart(startAnnotationBlockID, endAnnotationBlockID, true);
            }
            
            // get the reference wordlist if applicable
            /*String pathToWordList = new File(getServletContext().getRealPath("/data/" + wordlistID + ".xml"))
                    .toURI().toString();*/
            String pathToWordList = pathToWordList(wordlistID);
            
            // preparations done, now do the real job

            if (sorting.equals("sortChrono")){
                // this is actually not a wordlist, it is a kind of transcript visualisation
                String[][] parameters2 ={
                    {"TOKEN_LIST_URL", pathToWordList},
                    {"TOKEN_LIST_ARRAY", tokenListArray},
                    {"WORD_FORMS", wordForms},
                    {"SELECTION", selection}
                }; 
                if (output.equals("outputPrint")){
                    String wordListHTML = new IOHelper()
                            .applyInternalStylesheetToString(Constants.TRANSCRIPT2CHRONOWORDLISTHTML_STYLESHEET, transcript.toXML(), parameters2);
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");                            
                    response.getWriter().write(wordListHTML);             
                    response.getWriter().close();                                
                    return;
                } else {
                    // output == outputDownload
                    String wordListTXT = new IOHelper()
                            .applyInternalStylesheetToString(Constants.TRANSCRIPT2CHRONOWORDLISTTXT_STYLESHEET, transcript.toXML(), parameters2);

                    // need to write it to the download directory
                    File outFile = File.createTempFile("Wordlist_" + transcriptID + "_", ".txt");
                    outFile.deleteOnExit();
                    Files.write(outFile.toPath(), wordListTXT.getBytes("UTF-8"));
                    File downloadDirectory = new File(getServletContext().getRealPath("/downloads/"));

                    File zipFile = new File(downloadDirectory, "ZuMult-Download_" + UUID.randomUUID() + ".zip");
                    zipSingleFile(outFile, zipFile);

                    // result is not the file but a link to the file
                    String responseXML = "<download>";
                    responseXML+="<transcriptID>" + transcriptID + "</transcriptID>";
                    responseXML+="<file type=\"zip\">" + zipFile.getName() + "</file>";
                    responseXML+="</download>";
                    response.setContentType("application/xml");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(responseXML);            
                    response.getWriter().close();
                    return;
                }
            }
            
            // sorting!=sortChrono
            // do an XSL transformation to get the desired HTML representation of the wordlist
            String[][] parameters ={
                {"TOKEN_LIST_URL", pathToWordList},
                {"TOKEN_LIST_ARRAY", tokenListArray},
                {"WORD_FORMS", wordForms},
                {"SELECTION", selection},
                {"SORTING", sorting},
            }; 
            
            
            // generate the wordlist for the transcript
            
            TokenList tokenList;
            if (wordForms.equals("wordformsLemma")){
                tokenList = transcript.getTokenList("lemma");
            } else {
                tokenList = transcript.getTokenList("transcription");                
            }
            
            
            if (output.equals("outputPrint")){
                String wordListHTML = new IOHelper()
                        .applyInternalStylesheetToString(Constants.WORDLIST2HTML_PRINT_STYLESHEET, tokenList.toXML(), parameters);
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(wordListHTML);             
                response.getWriter().close();                                
                return;
            } else {
                // output == outputDownload
                String wordListTXT = new IOHelper()
                        .applyInternalStylesheetToString(Constants.WORDLIST2TXT_DOWNLOAD_STYLESHEET, tokenList.toXML(), parameters);

                System.out.println(wordListTXT);
                // need to write it to the download directory
                File outFile = File.createTempFile("Wordlist_" + transcriptID + "_", ".txt");
                outFile.deleteOnExit();
                Files.write(outFile.toPath(), wordListTXT.getBytes("UTF-8"));
                File downloadDirectory = new File(getServletContext().getRealPath("/downloads/"));

                File zipFile = new File(downloadDirectory, "ZuMult-Download_" + UUID.randomUUID() + ".zip");
                zipSingleFile(outFile, zipFile);

                // result is not the file but a link to the file
                String responseXML = "<download>";
                responseXML+="<transcriptID>" + transcriptID + "</transcriptID>";
                responseXML+="<file type=\"zip\">" + zipFile.getName() + "</file>";
                responseXML+="</download>";
                response.setContentType("application/xml");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(responseXML);            
                response.getWriter().close();
                return;
            }
            
            
            
            
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } 
    }
    
    
    private void getExpansion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");
            String expand = request.getParameter("expand");
            int expandInt =  Integer.parseInt(expand);
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            if (expandInt<0){
                startAnnotationBlockID = transcript.getAnnotationBlockID(startAnnotationBlockID, expandInt);
            } else if (expandInt>0){                        
                endAnnotationBlockID = transcript.getAnnotationBlockID(endAnnotationBlockID, expandInt);
            }
            
            double startTime = transcript.getTimeForID(startAnnotationBlockID);
            
            StringBuilder sb = new StringBuilder();
            sb.append("<result>");
            sb.append("<transcriptID>" + transcriptID + "</transcriptID>");
            sb.append("<startAnnotationBlockID>" + startAnnotationBlockID + "</startAnnotationBlockID>");
            sb.append("<endAnnotationBlockID>" + endAnnotationBlockID + "</endAnnotationBlockID>");
            sb.append("<startTime>" + Double.toString(startTime) + "</startTime>");
            sb.append("</result>");
            
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(sb.toString());
            response.getWriter().close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException();
        }
        

    }

    private void getSVG(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");
            String size = request.getParameter("size");
            if (size==null){
                size = "small";
            }
            
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            
            String[][] parameters = {
                {"START_ANNOTATION_BLOCK_ID", startAnnotationBlockID},
                {"END_ANNOTATION_BLOCK_ID", endAnnotationBlockID},
                {"SIZE", size},
            };
            
            
            String transcriptHTML = new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2SVG_STYLESHEET, transcript.toXML(), parameters);
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(transcriptHTML);            
            response.getWriter().close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }
    
    
    private void getTranscript(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            /*
            + "&transcriptID=" + transcriptID
            + "&wordlistID=" + selectedWordlist
            + "&startAnnotationBlockID=" + startAnnotationBlockID
            + "&endAnnotationBlockID=" + endAnnotationBlockID
            + "&startSelection=" + startSelection
            + "&endSelection=" + endSelection
            + "&form=" + form
            + "&showNormDev=" + showNormDev
            + "&visSpeechRate=" + visSpeechRate
            */
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();

            String transcriptID = request.getParameter("transcriptID");            
            Transcript transcript = backend.getTranscript(transcriptID);

                                   
            String wordlistID = request.getParameter("wordlistID");
            
            String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");
                        
            String startTokenID = request.getParameter("startTokenID");
            String endTokenID = request.getParameter("endTokenID");
            String howMuchAround = request.getParameter("howMuchAround");
            if (startTokenID!=null && endTokenID!=null){
                startAnnotationBlockID = backend.getNearestAnnotationBlockID4TokenID(transcriptID, startTokenID);
                endAnnotationBlockID = backend.getNearestAnnotationBlockID4TokenID(transcriptID, endTokenID);
                if(howMuchAround.length()>0){
                    startAnnotationBlockID = transcript.getAnnotationBlockID(startAnnotationBlockID, -Integer.parseInt(howMuchAround));
                    endAnnotationBlockID = transcript.getAnnotationBlockID(endAnnotationBlockID, Integer.parseInt(howMuchAround));
                }
            }
            

            String form = request.getParameter("form");
            String showNormDev = request.getParameter("showNormDev");
            String visSpeechRate = request.getParameter("visSpeechRate");
            
            String highlightIDs1 = request.getParameter("highlightIDs1");
            String highlightIDs2 = request.getParameter("highlightIDs2");
            String highlightIDs3 = request.getParameter("highlightIDs3");
            
            String visIncidentNotTypes = request.getParameter("visIncidentNotTypes");
            if (visIncidentNotTypes==null){
                // this is MANV specific, should not stay here
                visIncidentNotTypes="gaz;tri-sit;post;act;tri-kat";
            }
            
            
            String dropdown = request.getParameter("dropdown");
            if (dropdown==null){
                dropdown = "TRUE";
            }
            
           /* String pathToWordList = new File(getServletContext().getRealPath("/data/" + wordlistID + ".xml"))
                    .toURI().toString();*/
            String pathToWordList = pathToWordList(wordlistID);
            
            
            
            String transcriptXML = transcript.toXML();
            /*boolean flattenSeg = true;
            if (flattenSeg){
                //transcriptXML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/flattenSegHierarchy.xsl", transcriptXML);
                transcriptXML = new IOHelper().applyInternalStylesheetToString("/org/exmaralda/tei/xml/flattenSegHierarchy.xsl", transcriptXML);
            }*/
            
            String[][] parameters = {
                {"FORM", form},
                {"SHOW_NORM_DEV", showNormDev},
                {"VIS_SPEECH_RATE", visSpeechRate},
                {"START_ANNOTATION_BLOCK_ID", startAnnotationBlockID}, 
                {"END_ANNOTATION_BLOCK_ID", endAnnotationBlockID},
                {"TOKEN_LIST_URL", pathToWordList},
                
                {"HIGHLIGHT_IDS_1", highlightIDs1},
                {"HIGHLIGHT_IDS_2", highlightIDs2},
                {"HIGHLIGHT_IDS_3", highlightIDs3},

                {"DROPDOWN", dropdown},
                
                {"VIS_INCIDENT_NOT_TYPES", visIncidentNotTypes}
            };
            
            
            // changed for #174
            //String transcriptHTML = new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2HTML_STYLESHEET2, transcript.toXML(), parameters); 
            String xsl = Configuration.getIsoTei2HTMLStylesheet();
            System.out.println("Applying " + xsl + " to transcript.");
            String transcriptHTML = new IOHelper().applyInternalStylesheetToString(xsl, transcriptXML, parameters); 
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");                            
            response.getWriter().write(transcriptHTML);             
            response.getWriter().close();            
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | TransformerException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    private void getAnnotations(HttpServletRequest request, HttpServletResponse response) throws IOException {     
        try {
            String transcriptID = request.getParameter("transcriptID");
            String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");
            
            //System.out.println("getAnnotations called with TRANSCRIPT ID=" + transcriptID + " and START_ANNOTATION_BLOCK_ID=" + startAnnotationBlockID);
            
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            
            String[][] parameters = {
                {"START_ANNOTATION_BLOCK_ID", startAnnotationBlockID},
                {"END_ANNOTATION_BLOCK_ID", endAnnotationBlockID},
            };

            String xsl = Configuration.getIsoTei2HTMLAnnotationsStylesheet();            
            
            //String annotationHTML = new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2HTML_ANNOTATIONS_STYLESHEET, transcript.toXML(), parameters);
            String annotationHTML = new IOHelper().applyInternalStylesheetToString(xsl, transcript.toXML(), parameters);
            //System.out.println("********* ANNOTATION HTML ************\n" + annotationHTML);
            
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(annotationHTML);            
            response.getWriter().close();
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }               
    }

    private void getPartitur(HttpServletRequest request, HttpServletResponse response) throws IOException {     
        try {
            String transcriptID = request.getParameter("transcriptID");
            String aroundAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            
            System.out.println("getPartitur called with TRANSCRIPT ID=" + transcriptID + " and START_ANNOTATION_BLOCK_ID=" + aroundAnnotationBlockID);
            
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            
            int howMuchAround = 5;
            String startAnnotationBlockID = transcript.getAnnotationBlockID(aroundAnnotationBlockID, -howMuchAround);
            String endAnnotationBlockID = transcript.getAnnotationBlockID(aroundAnnotationBlockID, howMuchAround);
            Transcript partTranscript = transcript.getPart(startAnnotationBlockID, endAnnotationBlockID, true);
            partTranscript.removeAnnotations();
            
            String partiturHTML = new ISOTEITranscriptConverter((ISOTEITranscript) partTranscript).convert(ISOTEITranscriptConverter.FORMATS.PARTITUR_ENDLESS_HTML);
            
            
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(partiturHTML);            
            response.getWriter().close();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }               
    }


    private void getProtocol(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String speechEventID = request.getParameter("speechEventID");
            String transcriptID = request.getParameter("transcriptID");
            if (speechEventID==null){
                speechEventID = backend.getSpeechEvent4Transcript(transcriptID);
            }
            String protocolXML = backend.getProtocol(backend.getProtocol4SpeechEvent(speechEventID)).toXML();
            String protocolHTML = new IOHelper().applyInternalStylesheetToString(Constants.PROTOCOL2HTML_STYLESHEET, protocolXML);

            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(protocolHTML);            
            response.getWriter().close();
            
            
            
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }

    private void getCoordinatesForTime(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String speechEventID = request.getParameter("speechEventID");
            String transcriptID = request.getParameter("transcriptID");
            if (speechEventID==null){
                speechEventID = backend.getSpeechEvent4Transcript(transcriptID);
            }
            double time = Double.parseDouble(request.getParameter("time"));
            SpeechEvent speechEvent = backend.getSpeechEvent(speechEventID);
            String[] coordinates = speechEvent.getCoordinatesForTime(time);
            StringBuilder sb = new StringBuilder();
            sb.append("<result>");
            sb.append("<transcriptID>" + coordinates[0] + "</transcriptID>");
            sb.append("<startAnnotationBlockID>" + coordinates[1] + "</startAnnotationBlockID>");
            sb.append("</result>");
            
            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(sb.toString());
            response.getWriter().close();
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }

    private void getMetadataKeys(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String corpusID = request.getParameter("corpusID");
            Corpus corpus = backend.getCorpus(corpusID);

            List<MetadataKey> eventMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.EVENT), "de");                
            List<MetadataKey> speechEventMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.SPEECH_EVENT), "de");
            List<MetadataKey> speakerMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER), "de");
            List<MetadataKey> speakerInSpeechEventMetadataKeys = IOHelper.sortMetadataKeysByName(corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER_IN_SPEECH_EVENT), "de");          
            
            StringBuilder result = new StringBuilder();
            for (MetadataKey mk : eventMetadataKeys){
                if(mk.isQuantified()){
                // <option value="v_e_in_dgd_seit">E: In DGD seit Version</option>
                result.append("<option value=\"v_")
                        .append(mk.getID()).append("\">").append("E: ").append(mk.getName("de")).append("</option>");                                           
                }
            }
            for (MetadataKey mk : speechEventMetadataKeys){
                if(mk.isQuantified()){
                result.append("<option value=\"v_").append(mk.getID()).append("\">")
                        .append("SE: ").append(mk.getName("de")).append("</option>");                                        
                }
            }
            for (MetadataKey mk : speakerMetadataKeys){
                if(mk.isQuantified()){
                result.append("<option value=\"v_").append(mk.getID()).append("\">")
                        .append("S: ").append(mk.getName("de")).append("</option>");                                        
                }
            }
            for (MetadataKey mk : speakerInSpeechEventMetadataKeys){
                if(mk.isQuantified()){
                result.append("<option value=\"v_").append(mk.getID()).append("\">")
                        .append("SES: ").append(mk.getName("de")).append("</option>");                                        
                }
            }
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result.toString());
            response.getWriter().close();
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }

    private void zipSingleFile(File outFile, File zipFile) throws FileNotFoundException, IOException {
        FileOutputStream dest = new FileOutputStream(zipFile);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));        
        //File[] toBeZipped = {transcriptFile, htmlTranscriptFile, audioFile, eventDocFile, htmlEventDocFile};            

        // add file to the zip file
        FileInputStream inputStream = new FileInputStream(outFile);
        BufferedInputStream origin = new BufferedInputStream(inputStream);
        ZipEntry entry = new ZipEntry(outFile.getName());
        out.putNextEntry(entry);
        byte data[] = new byte[2048];
        int count;
        while((count = origin.read(data, 0, 2048)) != -1) {
               out.write(data, 0, count);
            }
        out.closeEntry();
        origin.close();
            //f.delete();
        out.close();
    }

    private String pathToWordList(String wordlistID){
        String pathToWordList;
        if (wordlistID==null || wordlistID.equals("NONE")){
            pathToWordList = "";
        }else if(wordlistID.startsWith("GOETHE")){
            pathToWordList = new File(Configuration.getWordlistPath() + "/goethe" + "/" + wordlistID + ".xml").toURI().toString();
        }else if(wordlistID.startsWith("HERDER")){
            pathToWordList = new File(Configuration.getWordlistPath() + "/herder" + "/"+ wordlistID + ".xml").toURI().toString();
        }else {
            pathToWordList = new File(Configuration.getWordlistPath() + "/thematic-vocabulary/lemmas" + "/"+ wordlistID + ".xml").toURI().toString();
        }
        return pathToWordList;
    }

    private void getAudio(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String tokenID = request.getParameter("tokenID");

            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            double timeForToken = transcript.getTimeForID(tokenID);
            IDList audioIDs = backend.getAudios4Transcript(transcriptID);

            StringBuilder sb = new StringBuilder();
            sb.append("<result>");
            sb.append("<transcriptID>" + transcriptID + "</transcriptID>");
            sb.append("<tokenID>" + tokenID + "</tokenID>");
            sb.append("<time>" + Double.toString(timeForToken) + "</time>");
            for (String audioID : audioIDs){
                    sb.append("<audio audioID=\"" + audioID + "\">");
                    Media audio = backend.getMedia(audioID);
                    sb.append(audio.getURL());
                    sb.append("</audio>");
            }
            sb.append("</result>");

            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(sb.toString());
            response.getWriter().close();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);            
        }
        
    }


    private void getVideo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String transcriptID = request.getParameter("transcriptID");
            String tokenID = request.getParameter("tokenID");

            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            double timeForToken = transcript.getTimeForID(tokenID);
            IDList videoIDs = backend.getVideos4Transcript(transcriptID);

            StringBuilder sb = new StringBuilder();
            sb.append("<result>");
            sb.append("<transcriptID>" + transcriptID + "</transcriptID>");
            sb.append("<tokenID>" + tokenID + "</tokenID>");
            sb.append("<time>" + Double.toString(timeForToken) + "</time>");
            for (String videoID : videoIDs){
                    sb.append("<video videoID=\"" + videoID + "\">");
                    Media video = backend.getMedia(videoID);
                    sb.append(video.getURL());
                    sb.append("</video>");
            }
            sb.append("</result>");

            response.setContentType("application/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(sb.toString());
            response.getWriter().close();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);            
        }
        
    }

    
    



}
