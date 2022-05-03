/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.zumult.objects.MediaMetadata;

/**
 *
 * @author josip.batinic
 */
public class DGD2MediaMetadata extends AbstractXMLObject implements MediaMetadata{

    XPath xPath = XPathFactory.newInstance().newXPath();
    
    public DGD2MediaMetadata(Document xmlDocument) {
        super(xmlDocument);
    }

    @Override
    public String getDuration() {
        String duration = getDocument().getElementsByTagName("Dauer").item(0).getTextContent();
        return duration;
    }

    @Override
    public String getID() {
        return getDocument().getDocumentElement().getAttribute("Kennung");
    }

    @Override
    public String getMediaFileSizeInBytes() {
        // return 0 because <Dateigröße> is no longer available 
        return "0";
        /*String fileSize = getDocument().getElementsByTagName("Dateigröße").item(0).getTextContent();
        return fileSize;*/
    }

    @Override
    public String getMediaDigitalFileID() {
        return getDocument().getDocumentElement().getElementsByTagName("Digitale_Fassung").item(0).getAttributes().item(0).getNodeValue();
    }
    
}
