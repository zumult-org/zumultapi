/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.w3c.dom.Element;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.query.KWIC;
import org.zumult.io.FileIO;
import org.zumult.query.Hit;
import org.zumult.query.KWICContext;
import org.zumult.query.KWICSnippet;
import org.zumult.query.implementations.DGDSearchIndexType.DGD2SearchIndexTypeEnum;
import org.zumult.query.implementations.ISOTEIKWICSnippetCreator;

/**
 *
 * @author Frick
 */
public class DGD2QuerySerializer extends DefaultQuerySerializer {
    
    @Override
    public String displayKWICinXML(KWIC obj) {
        String str = super.displayKWICinXML(obj);
        if (obj.getSearchMode().startsWith(
                DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name())){           
            try {
                Document document = FileIO.readDocumentFromString(str);
                document.getRootElement()
                        .getChild(DefaultQuerySerializer.META_PART)
                        .getChild(DefaultQuerySerializer.TOTAL_TRANSCRIPTS)
                        .setText(String.valueOf(-1)); 
                str = FileIO.getDocumentAsString(document);
            } catch (JDOMException | IOException ex) {
                Logger.getLogger(DGD2QuerySerializer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        } 
        return str;
    }
    
    protected String getKWICLine(String docID, 
            ArrayList<Hit.Match> matchArray, String firstMatchID, String lastMatchID, org.w3c.dom.Document transcriptDoc, ISOTEIKWICSnippetCreator creator,
            KWICContext leftContext, KWICContext rightContext, HashMap<String, String> metadata) throws IOException{
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<kwic-line>");
        
        // create snippet           
        KWICSnippet snippetObj = creator.apply(transcriptDoc, firstMatchID, matchArray, leftContext, rightContext);
        
        // add speakers, match dgd and zumult links
        sb.append(getAdditionalInfos(docID, firstMatchID, lastMatchID, matchArray, snippetObj));
        
        //add metadata
        for (String key: metadata.keySet()){
            sb.append("<").append(key).append(">");
            sb.append(metadata.get(key));
            sb.append("</").append(key).append(">");
        }
        
        sb.append("</kwic-line>");
        return sb.toString();
        
    }
    
        private String getAdditionalInfos(String docID, String firstMatchID, String lastMatchID, 
            ArrayList<Hit.Match> matchArray, KWICSnippet snippetObj) throws IOException{

        StringBuilder bw = new StringBuilder();
        //add doc ID
        bw.append("<transcript-id>");
        bw.append(docID);
        bw.append("</transcript-id>");
                
        // add speaker
        bw.append("<speaker-id>");
        StringBuilder sb = new StringBuilder();
        for (String id : snippetObj.getSpeakerIds()){
            sb.append(id);
            sb.append(" ");
        }
        bw.append(sb.toString().substring(0, sb.length()-1));
        bw.append("</speaker-id>");

        // add snippet
        Iterator<KWICSnippet.KWICSnippetToken> iterator = snippetObj.getContent().iterator(); 

        // add left context
        bw.append("<left-context>");
        KWICSnippet.KWICSnippetToken actualToken = null;
        while (iterator.hasNext()) { 
            KWICSnippet.KWICSnippetToken token = iterator.next();
            if(!token.belongsToMatch()){
                bw.append(getContentOfToken(token));
                bw.append(" ");
            }else{
                actualToken = token;
                break;
            }
        }

        bw.append("</left-context>");

        // add match
        bw.append("<match>");
        bw.append(getContentOfToken(actualToken));
        bw.append(" ");
        actualToken = null;
        while (iterator.hasNext()) { 
            KWICSnippet.KWICSnippetToken token = iterator.next();
            if(token.belongsToMatch()){
                bw.append(getContentOfToken(token));
                bw.append(" ");
            }else{
               // check if there are more matches
                int index = snippetObj.getContent().indexOf(token);
                Iterator<KWICSnippet.KWICSnippetToken> iteratorTmp = snippetObj.getContent().listIterator(index);

                Boolean moreMatches = false;                      
                while (iteratorTmp.hasNext()) { 
                    KWICSnippet.KWICSnippetToken tokenTpm = iteratorTmp.next();
                    if(tokenTpm.belongsToMatch()){
                        moreMatches=true;
                        break;   
                    }
                }
                if(moreMatches){
                    bw.append(getContentOfToken(token));
                    bw.append(" ");
                }else{
                    actualToken = token;            
                    break;
                } 
            }
        }

        bw.append("</match>");

        // add right context
        bw.append("<right-context>");
        if(actualToken!= null){
            bw.append(getContentOfToken(actualToken));
            bw.append(" ");
        }
        while (iterator.hasNext()) { 
            KWICSnippet.KWICSnippetToken token = iterator.next();
            bw.append(getContentOfToken(token));
            bw.append(" ");
        }
        bw.append("</right-context>");

        // add dgd link
        bw.append("<dgd-link>");
        bw.append(Configuration.getWebAppBaseURL());
        bw.append("/DGDLink?command=showTranscriptExcerpt&amp;transcriptID=");
        bw.append(docID);
        bw.append("&amp;tokenIDs=");
        bw.append(firstMatchID);
        bw.append("</dgd-link>");
        
        // add zumult link
        bw.append("<zumult-link>");
        bw.append(Configuration.getWebAppBaseURL());
        bw.append("/jsp/zuViel.jsp?");
        bw.append("transcriptID=");
        bw.append(docID);
        bw.append("&amp;");
        bw.append("form=norm");
        bw.append("&amp;");
        bw.append("highlightIDs1=");
        for(Hit.Match m:matchArray){
            bw.append(m.getID());
            bw.append(" ");
        }
        bw.append("&amp;");
        bw.append("startTokenID=");
        bw.append(firstMatchID);
        bw.append("&amp;");
        bw.append("endTokenID=");
        bw.append(lastMatchID);
        bw.append("&amp;howMuchAround=3");
        bw.append("</zumult-link>");
        
        return bw.toString();
    }
        
            private String getContentOfToken(KWICSnippet.KWICSnippetToken token){
        Element tokenElement = token.asXMLElement();
        if(tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
            return token.asXMLElement().getTextContent();
        }else if(tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_PAUSE)){
            return tokenElement.getAttribute(Constants.ATTRIBUTE_NAME_REND);
        }else if (tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_INCIDENT) || tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_VOCAL)){
            try{
                Element desc =  (Element) tokenElement.getElementsByTagName(Constants.ATTRIBUTE_NAME_DESC).item(0);
                return desc.getAttribute(Constants.ATTRIBUTE_NAME_REND);
            }catch(java.lang.ClassCastException ex){
                Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(token.toString());
            }
        }
        return null;
        
    }
   
   
}
