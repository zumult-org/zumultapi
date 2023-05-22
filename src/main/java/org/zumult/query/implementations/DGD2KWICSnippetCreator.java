/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.ISOTEIAnnotationBlock;
import org.zumult.query.Hit;
import org.zumult.query.KWICContext;
import org.zumult.query.implementations.DGD2KWICSnippet.DGD2KWICSnippetToken;
import org.zumult.query.KWICSnippet.KWICSnippetToken;

/**
 *
 * @author Elena
 */
public class DGD2KWICSnippetCreator {
    BackendInterface backendInterface;
    XPath xPath =  XPathFactory.newInstance().newXPath();
    
    public DGD2KWICSnippetCreator(){
        try {
            backendInterface = BackendInterfaceFactory.newBackendInterface();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(DGD2KWICSnippetCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DGD2KWICSnippet apply(String transcriptID, String leftMatchId, ArrayList<Hit.Match> matches, KWICContext leftContext, KWICContext rightContext) throws IOException {
        Transcript transcript = backendInterface.getTranscript(transcriptID);
        Document transcriptDoc = transcript.getDocument();
        return apply(transcriptDoc, leftMatchId, matches, leftContext, rightContext);
            
    }
    
    public DGD2KWICSnippet apply(Document transcriptDoc, String leftMatchId, ArrayList<Hit.Match> matches, KWICContext leftContext, KWICContext rightContext) throws IOException {
        
        int leftContextLength = leftContext.getLength();
        int rightContextAfterFirstMatch = getRightContextAfterFirstMatch(getHitLengthInTokens(matches) + rightContext.getLength());
            
        DGD2KWICSnippet matchSnippet = new DGD2KWICSnippet();
        ArrayList<KWICSnippetToken> content = new ArrayList();
        Set<String> speakers = new HashSet<>();
        xPath.setNamespaceContext(new ISOTEINamespaceContext());

        String xPathString = "//tei:*[@xml:id = '" + leftMatchId + "']";
    
        try{
            Node nNode = ((NodeList) xPath.compile(xPathString).evaluate(transcriptDoc, XPathConstants.NODESET)).item(0);
            Element firstElem = (Element) nNode;
            Node firstMatch = nNode;
                
            if (firstElem.getParentNode().getLocalName().equals(Constants.ELEMENT_NAME_BODY)){                  
                    
                // get left context
                setLeftContextForBodyChild(matches, firstMatch, matchSnippet, content, leftContextLength, speakers, 0, transcriptDoc);
                   
                // set first match
                DGD2KWICSnippetToken special = new DGD2KWICSnippetToken(firstElem);
                special.markAsMatch();
                content.add(special);
                    
                // get right context 
                setRightContextForBodyChild(matches, firstMatch, matchSnippet, content, rightContextAfterFirstMatch, speakers, 0, transcriptDoc);
                   
            }else{
                    
                // get first annotationBlock
                String xpathString = "//tei:"+ Constants.ELEMENT_NAME_ANNOTATION_BLOCK +"[descendant::*[@xml:id='" + leftMatchId + "']]";
                //Node firstAnnotationBlock = (Element) xPath.evaluate(xpathString, TranscriptDoc, XPathConstants.NODE);
                Node firstAnnotationBlock = (Node) xPath.compile(xpathString).evaluate(transcriptDoc, XPathConstants.NODE);
                Element firstAnnotationBlockElem = (Element) firstAnnotationBlock;
                String speaker = firstAnnotationBlockElem.getAttribute(Constants.ATTRIBUTE_NAME_WHO);
                speaker = getSpeakerInitials(speaker, transcriptDoc);
                // get left context
                int index = 0;
                Node sibling;
                while (( sibling = nNode.getPreviousSibling()) != null){

                    nNode = sibling;
                    if (Arrays.asList(Constants.TOKENS).contains (nNode.getLocalName())){
                        if (index < leftContextLength) {   

                            Element leftElem = (Element) nNode;
                            DGD2KWICSnippetToken token = new DGD2KWICSnippetToken(leftElem);
                            token.setParentId(getParentId(leftElem));
                            content.add(0, token);

                            if (nNode.getLocalName().equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
                                ++index;
                            }

                        }else{
                            matchSnippet.setStartMore(true);
                            break;
                        }
                    }                  
                }
                
                if (index < leftContextLength) {
                    // get more left context
                    setLeftContextForBodyChild(matches, firstAnnotationBlock, matchSnippet, content, leftContextLength, speakers, index, transcriptDoc);
                }

                // set first match
                DGD2KWICSnippetToken first = new DGD2KWICSnippetToken(firstElem);
                first.setParentId(getParentId(firstElem));
                first.markAsMatch();
                content.add(first);
                speakers.add(speaker);
                
                if (firstElem.getLocalName().equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
                    --rightContextAfterFirstMatch;
                }
                    
                // go back to first match
                index = 0;
                nNode = firstMatch;
                  
                // get rigth context
                while (( sibling = nNode.getNextSibling()) != null){
                    nNode = sibling;
                    if (Arrays.asList(Constants.TOKENS).contains (nNode.getLocalName())){
                        if (index < rightContextAfterFirstMatch) {  
                            Element rightElem = (Element) nNode;
                            DGD2KWICSnippetToken token = new DGD2KWICSnippetToken(rightElem);
                            token.setParentId(getParentId(rightElem));

                            for (Hit.Match match: matches){
                                if(match.getID().equals(rightElem.getAttributeNS(Constants.XML_NAMESPACE_URL, "id"))){
                                    token.markAsMatch();
                                }
                            }
                            content.add(token);

                            if (nNode.getLocalName().equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
                                ++index;
                            }
                        }else {
                            matchSnippet.setEndMore(true);
                            break;
                        }
                    }
                }
                    
                    
                if (index < rightContextAfterFirstMatch){
                    // get more right context   
                    setRightContextForBodyChild(matches, firstAnnotationBlock, matchSnippet, content, rightContextAfterFirstMatch, speakers, index, transcriptDoc);
                }
            }

        }catch (XPathExpressionException | NullPointerException | TransformerException ex) {
            throw new IOException("Can not create a snippet of " +  leftMatchId
                +"' from transcript '" + transcriptDoc.getDocumentURI() + "' " + ex);
        } 

        IDList speakerList = new IDList("speakers");
        for (String speaker : speakers){
            speakerList.add(speaker);
        }

        matchSnippet.setContent(content);
        matchSnippet.setSpeakerIds(speakerList);
        return matchSnippet;     
    }
    
    private String getSpeakerInitials(String speaker, Document transcriptDoc){
        
        String xpathString = "//tei:person[@xml:id='" + speaker + "']";
        Node personNode = null;
        try {
            personNode = (Node) xPath.compile(xpathString).evaluate(transcriptDoc, XPathConstants.NODE);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2KWICSnippetCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (personNode!=null){
            Element personElement = (Element) personNode;
            String speakerInitials = personElement.getAttribute("n");
            return speakerInitials;
        }else{
            return speaker;
        }
        
    /*    Element personElement = null; 
        try {
            personElement = ((Element)xPath.evaluate("//tei:person[@xml:id='" + speaker + "']", transcriptDoc, XPathConstants.NODE));
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DGD2KWICSnippetCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (personElement!=null){
            return personElement.getAttribute("n");
        }else{
            return speaker;
        }*/
    }
    
    private String getParentId(Element elem){
        Element parent = (Element) elem.getParentNode();
        return parent.getAttributeNS(Constants.XML_NAMESPACE_URL, "id");
    }
        
    private static int getHitLengthInTokens(ArrayList<Hit.Match> matches){
        int length = 0;
        for (Hit.Match match: matches){
            if(match.getID().startsWith(Constants.ELEMENT_NAME_WORD_TOKEN)){
                length++;
            }
        }
        return length;    
    }
    
    private int getRightContextAfterFirstMatch(int rightContextAfterFirstMatch){
        if (rightContextAfterFirstMatch > Constants.KWIC_TOKEN_CONTEXT_LENGTH_AFTER_FIRST_MATCH_MAX){
            rightContextAfterFirstMatch = Constants.KWIC_TOKEN_CONTEXT_LENGTH_AFTER_FIRST_MATCH_MAX;
        }
        return rightContextAfterFirstMatch;
    }
    
    private void setLeftContextForBodyChild(ArrayList<Hit.Match> matches, Node nNode, 
            DGD2KWICSnippet matchSnippet, ArrayList<KWICSnippetToken> content, 
        int leftContextLength, Set<String> speakers, Integer index, Document transcriptDoc) throws TransformerException, XPathExpressionException{
        Node sibling;   
        loop:
        while (( sibling = nNode.getPreviousSibling()) != null){
            nNode = sibling;
            if (index < leftContextLength){
                if (sibling.getNodeType()==1){
                    Element siblingElem = (Element) sibling;
                    if(sibling.getLocalName().equals(Constants.ELEMENT_NAME_ANNOTATION_BLOCK)){
           
                        AnnotationBlock annotationBlock = new ISOTEIAnnotationBlock(IOHelper.ElementToString(siblingElem));
                        Document annotationBlockDoc = annotationBlock.getDocument();
                        String speaker = annotationBlockDoc.getDocumentElement().getAttribute(Constants.ATTRIBUTE_NAME_WHO);
                        speaker = getSpeakerInitials(speaker, transcriptDoc);
                        boolean containsMatches = false;

                        //NodeList nodeList = (NodeList) xPath.compile("//*[count(./*) = 0]").evaluate(annotationBlockDoc, XPathConstants.NODESET);
                        NodeList nodeList = (NodeList) xPath.compile("//tei:"+ Constants.ELEMENT_NAME_SEG +"/tei:*").evaluate(annotationBlockDoc, XPathConstants.NODESET);

                        for(int j = nodeList.getLength()-1; j >= 0; j--) {
                            Node node = nodeList.item(j);
                            if (Arrays.asList(Constants.TOKENS).contains (node.getLocalName())){
                                if (index < leftContextLength) {  
                                    Element rightElem = (Element) node;
                                    DGD2KWICSnippetToken token = new DGD2KWICSnippetToken(rightElem);
                                    token.setParentId(getParentId(rightElem));

                                    for (Hit.Match match: matches){
                                        if(match.getID().equals(rightElem.getAttributeNS(Constants.XML_NAMESPACE_URL, "id"))){
                                            token.markAsMatch();
                                            containsMatches = true;
                                        }
                                    }
                                            
                                    content.add(0, token);

                                    if (node.getLocalName().equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
                                        ++index;
                                    }
                                }else {
                                    matchSnippet.setEndMore(true);
                                    if(containsMatches){                           
                                        speakers.add(speaker); 
                                    }
                                    break loop;
                                }
                            }
                        }
            
                        if(containsMatches){                           
                            speakers.add(speaker);
                        }
                    }else{
                                                               
                        DGD2KWICSnippetToken specialSibling = new DGD2KWICSnippetToken(siblingElem);
                                
                        for (Hit.Match match: matches){
                            if(match.getID().equals(siblingElem.getAttributeNS(Constants.XML_NAMESPACE_URL, "id"))){
                                specialSibling.markAsMatch();
                                break;
                            }
                        }

                        content.add(0, specialSibling);
                            
                    }
                    
                }
            }else {
                matchSnippet.setEndMore(true);
                break;
            }  
                       
        }
    }
        
        
    private void setRightContextForBodyChild(ArrayList<Hit.Match> matches, Node nNode, 
           DGD2KWICSnippet matchSnippet, ArrayList<KWICSnippetToken> content, 
            int rightContextLength, Set<String> speakers, Integer index, Document transcriptDoc) throws TransformerException, XPathExpressionException{
        Node sibling;  
        loop:
        while (( sibling = nNode.getNextSibling()) != null){
                
            nNode = sibling;
            if (index < rightContextLength){
                if (sibling.getNodeType()==1){
                    Element siblingElem = (Element) sibling;

                    if(sibling.getLocalName().equals(Constants.ELEMENT_NAME_ANNOTATION_BLOCK)){

                        AnnotationBlock annotationBlock = new ISOTEIAnnotationBlock(IOHelper.ElementToString(siblingElem));
                        Document annotationBlockDoc = annotationBlock.getDocument();
                        String speaker = annotationBlockDoc.getDocumentElement().getAttribute(Constants.ATTRIBUTE_NAME_WHO);
                        speaker = getSpeakerInitials(speaker, transcriptDoc);
                        boolean containsMatches = false;
                        

                        NodeList nodeList = (NodeList) xPath.compile("//tei:"+ Constants.ELEMENT_NAME_SEG +"/tei:*").evaluate(annotationBlockDoc, XPathConstants.NODESET);

                        for(int j = 0; j < nodeList.getLength(); j++) {
                            Node node = nodeList.item(j);
                            if (Arrays.asList(Constants.TOKENS).contains (node.getLocalName())){
                                if (index < rightContextLength) {  
                                    Element rightElem = (Element) node;
                                    DGD2KWICSnippetToken token = new DGD2KWICSnippetToken(rightElem);
                                    token.setParentId(getParentId(rightElem));

                                    for (Hit.Match match: matches){
                                        if(match.getID().equals(rightElem.getAttributeNS(Constants.XML_NAMESPACE_URL, "id"))){
                                            token.markAsMatch();
                                            containsMatches = true;
                                        }
                                    }
                                            
                                    content.add(token);

                                    if (node.getLocalName().equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
                                        ++index;
                                    }
                                }else {
                                    matchSnippet.setEndMore(true);
                                    if(containsMatches){   
                                        speakers.add(speaker);
                                    }
                                    break loop;
                                }
                            }
                        }

                        if(containsMatches){     
                            speakers.add(speaker); 
                        }
                     
                    } else if (sibling.getLocalName().equals(Constants.ELEMENT_NAME_SPAN_GRP)){
                        //ignore
                        
                    } else{

                        DGD2KWICSnippetToken specialSibling = new DGD2KWICSnippetToken(siblingElem);

                        for (Hit.Match match: matches){
                            if(match.getID().equals(siblingElem.getAttributeNS(Constants.XML_NAMESPACE_URL, "id"))){
                                specialSibling.markAsMatch();
                                break;
                            }
                        }                   

                        content.add(specialSibling);
     
                    }
                }
            }else {
                matchSnippet.setEndMore(true);
                break;
            }          
        }
    }       
    
}