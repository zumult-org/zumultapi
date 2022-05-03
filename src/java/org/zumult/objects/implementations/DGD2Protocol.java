/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.w3c.dom.Document;
import org.zumult.objects.Protocol;

/**
 *
 * @author thomas.schmidt
 */
public class DGD2Protocol extends AbstractXMLObject implements Protocol {

    public DGD2Protocol(Document xmlDocument) {
        super(xmlDocument);
    }

    public DGD2Protocol(String xmlString) {
        super(xmlString);
    }

    @Override
    public String getID() {
        return this.getDocument().getDocumentElement().getAttribute("id");
    }
    
}
