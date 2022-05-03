/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.zumult.objects.AnnotationTagSet;

/**
 *
 * @author Frick
 */
public class DGD2AnnotationTagSet extends AbstractXMLObject implements AnnotationTagSet{
    XPath xPath = XPathFactory.newInstance().newXPath();
    
    public DGD2AnnotationTagSet(Document xmlDocument) {
        super(xmlDocument);
    }

    @Override
    public String getID() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
