/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zumult.objects.AnnotationBlock;

/**
 *
 * @author Elena
 */
// Watch out: this should be ISOTEIAnnotationBlock

public class ISOTEIAnnotationBlock extends AbstractXMLObject implements AnnotationBlock{

    public ISOTEIAnnotationBlock(Document xmlDocument) {
        super(xmlDocument);
    }
    
    public ISOTEIAnnotationBlock(String xmlString) {
        super(xmlString);
    }    

    @Override
    public String getID() {
        return getDocument().getDocumentElement().getAttribute("xml:id");
    }

    @Override
    public String getSpeaker() {
        return super.getDocument().getDocumentElement().getAttribute("who");
    }

    @Override
    public String getStart() {
        return super.getDocument().getDocumentElement().getAttribute("start");
    }

    @Override
    public String getEnd() {
        return super.getDocument().getDocumentElement().getAttribute("end");
    }

    @Override
    public String getWordText() {
        String result = "";
        NodeList wElements = super.getDocument().getElementsByTagName("w");
        for (int i=0; i<wElements.getLength(); i++){
            Element e = (Element)(wElements.item(i));
            String words = e.getTextContent();
            result+=words + " ";
        }
        return result.trim();
    }
    
}
