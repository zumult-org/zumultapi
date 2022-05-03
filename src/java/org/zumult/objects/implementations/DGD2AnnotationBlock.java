/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.w3c.dom.Document;
import org.zumult.objects.AnnotationBlock;

/**
 *
 * @author Elena
 */
// Watch out: this should be ISOTEIAnnotationBlock

public class DGD2AnnotationBlock extends AbstractXMLObject implements AnnotationBlock{

    public DGD2AnnotationBlock(Document xmlDocument) {
        super(xmlDocument);
    }
    
    public DGD2AnnotationBlock(String xmlString) {
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
    
}
