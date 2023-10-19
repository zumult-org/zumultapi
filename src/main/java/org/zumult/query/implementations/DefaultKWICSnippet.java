/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.zumult.io.Constants;
import org.zumult.objects.IDList;
import org.zumult.query.KWICSnippet;

/**
 *
 * @author Elena
 */
 public class DefaultKWICSnippet implements KWICSnippet {
    
    private boolean startMore = false;
    private boolean endMore = false;
    private ArrayList<KWICSnippetToken> content;
    private IDList speakerIds;

    @Override
    public IDList getSpeakerIds() {
        return speakerIds;
    }

    @Override
    public ArrayList<KWICSnippetToken> getContent() {
        return content;
    }

    @Override
    public boolean isStartMore() {
        return startMore;
    }

    @Override
    public boolean isEndMore() {
        return endMore;
    }

    public void setSpeakerIds(IDList speakerIds) {
        this.speakerIds = speakerIds;
    }

    public void setStartMore(boolean startMore) {
        this.startMore = startMore;
    }

    public void setEndMore(boolean endMore) {
        this.endMore = endMore;
    }

    public void setContent(ArrayList<KWICSnippetToken> content){
        this.content = content;
    }

  
        
    public static class DefaultKWICSnippetToken implements KWICSnippetToken{
            
            private boolean match = false;
            private final Element element;
            private String parentId;

            public DefaultKWICSnippetToken(Element w){
                this.element = w;
            }

            public void setParentId(String parentId){
                this.parentId = parentId;
            }

            public void markAsMatch(){
                this.match = true;
            }

            @Override
            public String getID() {
                Element el = (Element) element;
                return el.getAttributeNS(Constants.XML_NAMESPACE_URL, "id");
            }

            @Override
            public boolean belongsToMatch() {
                return match;
            }

            @Override
            public Element asXMLElement() { 
                return element;
            }
                        
            @Override
            public String toString(){
                Node node = this.element;
                DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
                LSSerializer serializer = lsImpl.createLSSerializer();
                serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
                String str = serializer.writeToString(node);
                return str;
            }

            @Override
            public String getParentId() {
                return parentId;
            }

        }
    
 }
