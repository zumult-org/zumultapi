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
 * @author thomas.schmidt
 */
public class COMALocation extends AbstractXMLObject implements Location {

    XPath xPath = XPathFactory.newInstance().newXPath();
    /*
        <Location Type="Residence">
            <Period>
               <PeriodStart>2008-01-01T00:00:00</PeriodStart>
               <PeriodExact>false</PeriodExact>
               <PeriodDuration>0</PeriodDuration>
            </Period>
            <Country>Germany</Country>
            <Description>
               <Key Name="Precision">month and day not exact</Key>
            </Description>
         </Location>    
    */


    public COMALocation(Document xmlDocument) {
        super(xmlDocument);
    }

    public COMALocation(String xmlString) {
        super(xmlString);
    }

    @Override
    public String getType() {
        String type = getDocument().getDocumentElement().getAttribute("Type");
        if (type==null){
            return "Recording";
        }
        return type;
    }

    @Override
    public String getPlacename() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCountry() {
        try {
            String xPathString = "descendant::Country/text()";
            String countryName = xPath.evaluate(xPathString, getDocument().getDocumentElement());
            return countryName;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2Location.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return null;
    }

    @Override
    public double getLatitude() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getLongitude() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
}
