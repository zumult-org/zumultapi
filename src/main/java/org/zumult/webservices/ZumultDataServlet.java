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
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEITranscriptConverter;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
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
            case "getEventMetadataHTML" : 
                getEventMetadataHTML(request, response);
                break;
            case "getSpeechEventMetadataHTML" : 
                getSpeechEventMetadataHTML(request, response);
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
            case "getVideoImage" :
                getVideoImage(request, response);
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

    private void getEventMetadataHTML(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String eventID = request.getParameter("speechEventID");
            if (eventID!=null){
            Event event = backend.getEvent(eventID.substring(0,12));
                String eventXML = event.toXML();        
                String eventHTML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/folkEvent2html_table.xsl", eventXML);
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");                            
                response.getWriter().write(eventHTML);             
                response.getWriter().close();            
            } else {
                String errorHTML = "<div>Event " + eventID + " not found.</div>";
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

    private void getSpeechEventMetadataHTML(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String speechEventID = request.getParameter("speechEventID");
            String eventXML = backend.getEvent(speechEventID.substring(0,12)).toXML();  
            String[][] param = {
                {"speechEventID", speechEventID}
            };
            String eventHTML = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/speechEvent2Table.xsl", eventXML, param);
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");                            
            response.getWriter().write(eventHTML);             
            response.getWriter().close();            
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
            String title = backend.getSpeechEvent(eventID + "_SE_01").getMetadataValue(backend.findMetadataKeyByID("v_e_se_art"));           
            String html = "<span>" + title + "</span>";
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");                            
            response.getWriter().write(html);             
            response.getWriter().close();            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ZumultDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (Exception ex) {
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
            String[][] parameters = {
                {"TYPE", subtitleType}
            };
            String vtt = new IOHelper().applyInternalStylesheetToString("/org/zumult/io/isotei2vtt.xsl", transcript.toXML(), parameters);
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
    
    private void getVideoImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String videoID = request.getParameter("videoID");
            String position = request.getParameter("position");
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Media video = backend.getMedia(videoID, Media.MEDIA_FORMAT.MPEG4_ARCHIVE);
            Media videoImage = video.getVideoImage(Double.parseDouble(position));
            
            File downloadDirectory = new File(getServletContext().getRealPath("/downloads/"));
            File targetFile = new File(downloadDirectory, "ZuMult-Image_" + UUID.randomUUID() + ".png");
            //Files.move(Paths.get(new URL(videoImage.getURL()).toURI()), targetFile.toPath());
            //System.out.println("*************" + videoImage.getURL
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
            if (videoArchive && videoIDs.size()>0){
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
            String transcriptID = request.getParameter("transcriptID");
            String wordlistID = request.getParameter("wordlistID");
            String startAnnotationBlockID = request.getParameter("startAnnotationBlockID");
            String endAnnotationBlockID = request.getParameter("endAnnotationBlockID");

            String form = request.getParameter("form");
            String showNormDev = request.getParameter("showNormDev");
            String visSpeechRate = request.getParameter("visSpeechRate");
            
            String highlightIDs1 = request.getParameter("highlightIDs1");
            String highlightIDs2 = request.getParameter("highlightIDs2");
            String highlightIDs3 = request.getParameter("highlightIDs3");
            
           /* String pathToWordList = new File(getServletContext().getRealPath("/data/" + wordlistID + ".xml"))
                    .toURI().toString();*/
            String pathToWordList = pathToWordList(wordlistID);
            
            
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            
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
            };
            
            
            String transcriptHTML = new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2HTML_STYLESHEET2, transcript.toXML(), parameters); 
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
            
            System.out.println("getAnnotations called with TRANSCRIPT ID=" + transcriptID + " and START_ANNOTATION_BLOCK_ID=" + startAnnotationBlockID);
            
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            Transcript transcript = backend.getTranscript(transcriptID);
            
            String[][] parameters = {
                {"START_ANNOTATION_BLOCK_ID", startAnnotationBlockID},
                {"END_ANNOTATION_BLOCK_ID", endAnnotationBlockID},
            };

            String annotationHTML = new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2HTML_ANNOTATIONS_STYLESHEET, transcript.toXML(), parameters);
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
        } catch (Exception ex) {
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
        if (wordlistID.equals("NONE")){
            pathToWordList = "";
        }else if(wordlistID.startsWith("GOETHE")){
            pathToWordList = new File(Constants.WORDLISTS_GOETHE_PATH + "/" + wordlistID + ".xml").toURI().toString();
        }else if(wordlistID.startsWith("HERDER")){
            pathToWordList = new File(Constants.WORDLISTS_HERDER_PATH + "/"+ wordlistID + ".xml").toURI().toString();
        }else {
            pathToWordList = new File(Constants.WORDLISTS_THEMATIC_VOCABULARY_PATH + "/"+ wordlistID + ".xml").toURI().toString();
        }
        return pathToWordList;
    }
}
