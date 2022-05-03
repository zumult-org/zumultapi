/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zumult.objects.AdditionalMaterialMetadata;

/**
 *
 * @author josip.batinic
 */
public class DGD2AdditionalMaterialMetadata extends AbstractXMLObject implements AdditionalMaterialMetadata {
    
    XPath xPath = XPathFactory.newInstance().newXPath();

    public DGD2AdditionalMaterialMetadata(Document xmlDocument) {
        super(xmlDocument);
    }

    @Override
    public int getAmountOfAdditionalMaterial() {
        return getDocument().getElementsByTagName("Zusatzmaterial").getLength();
    }

    @Override
    public Document getAdditionalMaterialMetadataByIndex(int indexOfMaterial) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.newDocument();
            
            Node node = getDocument().getElementsByTagName("Zusatzmaterial").item(indexOfMaterial);
            
            Node importedNode = doc.importNode(node, true);
            doc.appendChild(importedNode);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2AdditionalMaterialMetadata.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    @Override
    public ArrayList<String> getListOfCategories() {
        NodeList materialsNodeList = getDocument().getElementsByTagName("Zusatzmaterial");
        ArrayList<String> materialsList = new ArrayList<>();
        for (int i = 0; i < materialsNodeList.getLength(); i++) {
            Node material = materialsNodeList.item(i);
            String category = material.getAttributes().item(0).getNodeValue();
            materialsList.add(category);
            
        }
        return materialsList;
    }

    @Override
    public String getCategoryByIndex(int indexOfMaterial) {
        NodeList materialsNodeList = getDocument().getElementsByTagName("Zusatzmaterial");
        Node material = materialsNodeList.item(indexOfMaterial);
        String category = material.getAttributes().item(0).getNodeValue();
        return category;
    }

    @Override
    public String getFormatByIndex(int indexOfMaterial) {
        NodeList materialsNodeList = getDocument().getElementsByTagName("Format");
        String format = materialsNodeList.item(indexOfMaterial).getTextContent();
        return format;
    }

    @Override
    public String getID() {
        // doesn't exist yet
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
