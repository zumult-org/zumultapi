/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import org.zumult.objects.VirtualCollectionItem;

/**
 *
 * @author thomas.schmidt
 */
public class VirtualCollectionItemTranscriptExcerpt extends AbstractXMLObject implements VirtualCollectionItem {

    String transcriptID;
    String startAnnotationBlockID;
    String endAnnotationBlockID;
    
    public VirtualCollectionItemTranscriptExcerpt(String VirtualCollectionItemTranscriptExcerpt) throws IOException, SAXException, ParserConfigurationException {
        super(VirtualCollectionItemTranscriptExcerpt);
    }
    
    public VirtualCollectionItemTranscriptExcerpt(Element element) throws TransformerException {
        super(IOHelper.ElementToString(element));
    }
    
   /* public VirtualCollectionItemTranscriptExcerpt(String transcriptID, String startAnnotationBlockID, String endAnnotationBlockID) {
        this.transcriptID = transcriptID;
        this.startAnnotationBlockID = startAnnotationBlockID;
        this.endAnnotationBlockID = endAnnotationBlockID;
    }

    public VirtualCollectionItemTranscriptExcerpt(Element element) {
        transcriptID = element.getAttribute("transcriptID");
        startAnnotationBlockID = element.getAttribute("startAnnotationBlockID");
        endAnnotationBlockID = element.getAttribute("endAnnotationBlockID");
    }*/
    
    @Override
    public String getType() {
        return "transcript-excerpt";
    }

    
 /*   @Override
    public String toXML() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<virtualCollectionItem ");
        sb.append(" type=\"").append(getType()).append("\"");
        sb.append(" transcriptID=\"").append(transcriptID).append("\"");
        sb.append(" startAnnotationBlockID=\"").append(startAnnotationBlockID).append("\"");
        sb.append(" endAnnotationBlockID=\"").append(endAnnotationBlockID).append("\">");
        
        sb.append("</virtualCollectionItem>");
        
        return sb.toString();
        
    }

    @Override
    public Document getDocument() {
        try {
            return IOHelper.DocumentFromText(toXML());
        } catch (IOException ex) {
            Logger.getLogger(ZumultVirtualCollection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException | ParserConfigurationException ex) {
            Logger.getLogger(ZumultVirtualCollection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ZumultVirtualCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }*/
    
} 
