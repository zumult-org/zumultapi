/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.zumult.objects.IDList;
import org.zumult.objects.Identifiable;

/**
 *
 * @author Elena Frick
 */
public interface KWICSnippet {
    public ArrayList<KWICSnippetToken> getContent();
    public boolean isStartMore();
    public boolean isEndMore();
    public IDList getSpeakerIds();
    
    interface KWICSnippetToken extends Identifiable {
        public boolean belongsToMatch();
        public Element asXMLElement(); //returns the origin token element from transkript
        public String getParentId();
    }


}
