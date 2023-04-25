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
import org.zumult.objects.Location;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2Location extends AbstractXMLObject implements Location {

    XPath xPath = XPathFactory.newInstance().newXPath();

    public DGD2Location(Document xmlDocument) {
        super(xmlDocument);
    }

    
    /*
        <Ortsdaten Referenz="DGD-_O_05839" Typ="Geburtsort">
          <Land Kürzel="PL">Polen</Land>
          <Region>Pommern</Region>
          <Kreis>Nowodworski</Kreis>
          <Ortsname>Wolfsdorf an der Nogat</Ortsname>
          <Ortsname>Wierciny</Ortsname>
          <Koordinaten>
            <Geocode>
              <Geografische_Breite>54.15369</Geografische_Breite>
              <Geografische_Länge>19.25311</Geografische_Länge>
            </Geocode>
            <Planquadrat>1254</Planquadrat>
            <Anmerkungen/>
          </Koordinaten>
          <Ortsteil>Nicht dokumentiert</Ortsteil>
          <Ortsbeschreibung>Nicht vorhanden</Ortsbeschreibung>
          <Aufenthaltsdauer>Nicht dokumentiert</Aufenthaltsdauer>
          <Anmerkungen/>
        </Ortsdaten>
    
    */
    
    @Override
    public String getType() {
        String type = getDocument().getDocumentElement().getAttribute("Typ");
        if (type==null){
            return "Aufnahmeort";
        }
        return type;
    }

    @Override
    public String getPlacename() {
        try {
            String xPathString = "/*/Ortsname[last()]";
            String placeNameString = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            return placeNameString;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Location.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return null;
    }

    @Override
    public String getCountry() {
        try {
            String xPathString = "/*/Land/@Kürzel";
            String isoCode = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            return isoCode;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Location.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return null;
    }

    @Override
    public double getLatitude() {
        try {
            String xPathString = "/*/Koordinaten/Geocode/Geografische_Breite";
            String latString = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            if (latString!=null){
                return Double.parseDouble(latString);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Location.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return -1;
    }

    @Override
    public double getLongitude() {
        try {
            String xPathString = "/*/Koordinaten/Geocode/Geografische_Länge";
            String longString = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            if (longString!=null){
                return Double.parseDouble(longString);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Location.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return -1;
    }
    
}
