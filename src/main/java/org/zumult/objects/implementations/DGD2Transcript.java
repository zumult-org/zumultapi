/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import org.w3c.dom.Document;
import org.zumult.objects.MetadataKey;


/**
 *
 * @author Frick
 */
public class DGD2Transcript  extends ISOTEITranscript  {
        public DGD2Transcript(Document transcriptDocument) {
        super(transcriptDocument);            
    }
    
    public DGD2Transcript(String transcriptXML) {
        super(transcriptXML);
    }
        
    public DGD2Transcript(String transcriptXML, String metadataXML) {
        super(transcriptXML, metadataXML);          
    }
    
    public DGD2Transcript(Document transcriptDocument, Document metadataDocument) {
        super(transcriptDocument, metadataDocument);
    }
    
    
    @Override
    public ISOTEITranscript createNewInstance(Document transcriptDocument, Document metadataDocument){
        return new DGD2Transcript(transcriptDocument, metadataDocument);
    }
    
    @Override
    public ISOTEITranscript createNewInstance(Document transcriptDocument){
        return new DGD2Transcript(transcriptDocument);
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setTimelineToZero() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
