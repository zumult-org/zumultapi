/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import org.zumult.objects.VirtualCollection;
import org.zumult.objects.VirtualCollectionItem;

/**
 *
 * @author thomas.schmidt
 */
public class ZumultVirtualCollection  extends ArrayList<VirtualCollectionItem> implements VirtualCollection {

    String id;
    String name;
    String owner;
    String description;
    String title;

    public ZumultVirtualCollection(String id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
    
    public ZumultVirtualCollection(String virtualCollectionXML) throws IOException, SAXException, ParserConfigurationException, TransformerException{
        Document document = IOHelper.DocumentFromText(virtualCollectionXML);
        id = document.getDocumentElement().getAttribute("id");
        name = document.getDocumentElement().getAttribute("name");
        owner = document.getDocumentElement().getAttribute("owner");
        description = document.getDocumentElement().getFirstChild().getTextContent();
        title = ((Element) document.getDocumentElement().getElementsByTagName("title").item(0)).getTextContent();
        
        NodeList vcis = document.getElementsByTagName("virtualCollectionItem");
        for (int i=0; i<vcis.getLength(); i++){
            add(new VirtualCollectionItemTranscriptExcerpt((Element) vcis.item(i)));
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    
    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addVirtualCollectionItem(VirtualCollectionItem collectionItem) {
        this.add(collectionItem);
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public String toXML() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<virtualCollection ");
        sb.append(" name=\"").append(getName()).append("\"");
        sb.append(" id=\"").append(getID()).append("\"");
        sb.append(" owner=\"").append(getOwner()).append("\">");
        
        sb.append("<description>");
        sb.append(getDescription());
        sb.append("</description>");
        
        for (VirtualCollectionItem vci : this){
            sb.append(vci.toXML());
        }        
        
        sb.append("</virtualCollection>");
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
    }

    
}
