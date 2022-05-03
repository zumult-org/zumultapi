/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.zumult.objects.TranscriptMetadata;

/**
 *
 * @author josip.batinic
 */
public class DGD2TranscriptMetadata extends AbstractXMLObject implements TranscriptMetadata {

    XPath xPath = XPathFactory.newInstance().newXPath();

    public DGD2TranscriptMetadata(Document xmlDocument) {
        super(xmlDocument);
    }

    @Override
    public String getDuration() {
        return getDocument().getElementsByTagName("Dauer").item(0).getTextContent();
    }

    @Override
    public String getID() {
        return getDocument().getDocumentElement().getAttribute("Kennung");
    }

    @Override
    public String getTypes() {
        String type = null;
        try {
            String xPathString = "Transkript/Annotation/Erstellung/Ergebnisse_Umfang/Types";
            type = xPath.evaluate(xPathString, getDocument());
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2MediaMetadata.class.getName()).log(Level.SEVERE, null, ex);
        }
        return type;
    }

    @Override
    public String getTokens() {
        String tokens = null;
        try {
            String xPathString = "Transkript/Annotation/Erstellung/Ergebnisse_Umfang/Tokens";
            tokens = xPath.evaluate(xPathString, getDocument());
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2MediaMetadata.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tokens;
    }

    @Override
    public String getTranscriptFileSizeInBytes() {
        return getDocument().getElementsByTagName("Dateigröße").item(0).getTextContent();
    }
    
}
