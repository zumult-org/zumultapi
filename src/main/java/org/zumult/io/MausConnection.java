/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.convert.PraatConverter;
import org.exmaralda.partitureditor.sound.AudioProcessor;
import org.exmaralda.webservices.MAUSConnector;
import org.jdom.JDOMException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.Media;
import org.zumult.objects.Transcript;

/**
 *
 * @author bernd
 */
public class MausConnection {
    
    public String getMausAligment(String transcriptID, String annotationBlockID, String format){
        try {
            BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
            String audioID = backend.getAudios4Transcript(transcriptID).get(0);
            
            Transcript transcript = backend.getTranscript(transcriptID);
            AnnotationBlock annotationBlock = backend.getAnnotationBlock(transcriptID, annotationBlockID);
            String annotationBlockStartID = annotationBlock.getStart();
            String annotationBlockEndID = annotationBlock.getEnd();
            double startTime = transcript.getTimeForID(annotationBlockStartID);
            double endTime = transcript.getTimeForID(annotationBlockEndID);
            String text = annotationBlock.getWordText();
            
            File textFile = File.createTempFile("ZuMult_MAUS", ".txt");
            IOHelper.writeUTF8(textFile, text);
            
            Media partAudio = backend.getMedia(audioID, Media.MEDIA_FORMAT.WAV).getPart(startTime, endTime);
            File audioFile = new File(partAudio.getURL());
            
            File audioFile16kHzMono = File.createTempFile("ZuMult_", ".wav");
            AudioProcessor audioProcessor = new AudioProcessor();
            audioProcessor.stereoToMono16kHz(audioFile, audioFile16kHzMono);
            
            HashMap<String, Object> otherParameters = new HashMap<>();
            otherParameters.put("LANGUAGE", MausConnection.mapLanguageCode2ToMAUS(transcript.getLanguage()));
            
            MAUSConnector mausConnector = new MAUSConnector();
            //String praatTextString = mausConnector.callMAUS(textFile, audioFile, otherParameters);
            String praatTextString = mausConnector.callMAUS(textFile, audioFile16kHzMono, otherParameters);
            audioFile.delete();
            textFile.delete();
            
            if (format!=null && format.equals("PraatTextGrid")){
                String wrappedXML = "<praatTextGrid>\n"
                        + "<![CDATA["
                        + praatTextString
                        + "]]>"
                        + "\n</praatTextGrid>";
                return wrappedXML;
            }
            
            File praatFile = File.createTempFile("ZuMult_MAUS", ".textGrid");
            IOHelper.writeUTF8(praatFile, praatTextString);
            
            PraatConverter praatConverter = new PraatConverter();
            BasicTranscription basicTranscription = praatConverter.readPraatFromFile(praatFile.getAbsolutePath());
            praatFile.delete();
            
            return basicTranscription.toXML();
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | JDOMException | IOException ex) {
            Logger.getLogger(MausConnection.class.getName()).log(Level.SEVERE, null, ex);
            return "<error>" + ex.getLocalizedMessage() + "</error>";
        } 
    }
    
    public static String mapLanguageCode2ToMAUS(String languageCode) {
        /*
            The value en of the type LANGUAGE (Language) is not in the closed vocabulary 
            [aus-AU, afr-ZA, sqi-AL, arb, eus-ES, eus-FR, cat-ES, nld-BE, nld-NL, 
            eng-AU, eng-US, eng-GB, eng-SC, eng-NZ, ekk-EE, fin-FI, 
            fra-FR, kat-GE, deu-AT, deu-CH, deu-DE, gsw-CH, gsw-CH-BE, gsw-CH-BS, 
            gsw-CH-GR, gsw-CH-SG, gsw-CH-ZH, hun-HU, isl-IS, ita-IT, jpn-JP, gup-AU, 
            ltz-LU, mlt-MT, nor-NO, fas-IR, pol-PL, ron-RO, rus-RU, spa-ES, swe-SE, tha-TH, guf-AU, 
            cat, deu, eng, fra, hun, ita, mlt, nld, pol, nze, fin, ron, spa]! 
            For valid options please check https://clarin.phonetik.uni-muenchen.de/BASWebServices/BAS_Webservices.cmdi.xml Aborting!        
        */
        switch (languageCode) {
            case "de" : return "deu";
            case "en" : return "eng";
            case "fr" : return "fra";
            case "it" : return "ita";
            case "es" : return "spa";
            case "pt" : return "por";
            case "pl" : return "pol";
            case "ru" : return "rus-RU";
            default : return languageCode;
        }
    }
    
    
}
