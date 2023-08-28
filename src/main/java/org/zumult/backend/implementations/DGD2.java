/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;


import java.io.File;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.zumult.backend.Configuration;
import org.zumult.io.IOHelper;
import org.zumult.objects.Corpus;
import org.zumult.objects.CrossQuantification;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Protocol;
import org.zumult.objects.ResourceServiceException;
import org.zumult.objects.Speaker;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DGD2CrossQuantification;


/**
 *
 * @author Elena Frick
 */
public class DGD2 extends AbstractIDSBackend {
    
    File quantificationPath = null;

    public DGD2() {
        quantificationPath = new File(Configuration.getQuantificationPath());
    }

    @Override
    public CrossQuantification getCrossQuantification4Corpus(String corpusID, 
            MetadataKey metadataKey1, MetadataKey metadataKey2,
            String unit) throws ResourceServiceException, IOException {
        
        if(!metadataKey1.isQuantified()){
            throw new ResourceServiceException("Parameter " + metadataKey1 +" is not quantifiable!");
        }
        
        if(!metadataKey2.isQuantified()){
            throw new ResourceServiceException("Parameter " + metadataKey2 +" is not quantifiable!");
        }
        
        if (unit==null){
            unit = "TOKENS";
        }
                    
        String[][] PARAM = {
        {"META_FIELD_1", "v_" + metadataKey1.getID()},
        {"META_FIELD_2", "v_" + metadataKey2.getID()},
        {"UNITS", unit}
        };
    
        String QUANT_FILENAME = corpusID + "_QUANT.xml";

        
        try {
            String html = new IOHelper().applyInternalStylesheetToFile("/org/zumult/io/Quantify2Dimensions.xsl", 
                new File(quantificationPath, QUANT_FILENAME).getAbsolutePath(), PARAM);
                
            CrossQuantification crossQuantification = new DGD2CrossQuantification(html);
            return crossQuantification;
            
        } catch (TransformerException ex) {
            throw new IOException(ex); 
        }
    }   

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Corpus getCorpus(String corpusID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Event getEvent(String eventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Speaker getSpeaker(String speakerID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Transcript getTranscript(String transcriptID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Protocol getProtocol(String protocolID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getEvents4Corpus(String corpusID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getSpeakers4Corpus(String corpusID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getSpeechEvents4Event(String eventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getTranscripts4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getAudios4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getVideos4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getTranscripts4Audio(String audioID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getTranscripts4Video(String videoID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getAudios4Transcript(String transcriptID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getVideos4Transcript(String transcriptID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getProtocol4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getSpeakers4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IDList getAvailableValues(String corpusID, String metadataKeyID) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}