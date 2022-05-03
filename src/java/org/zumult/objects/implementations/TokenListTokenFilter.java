/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;

/**
 *
 * @author Thomas_Schmidt
 */
public class TokenListTokenFilter implements TokenFilter {

    XPath xPath = XPathFactory.newInstance().newXPath();

    String sourceType;
    String filterType;
    TokenList filterTokenList;
    String preFilterXPath;
    String idXPath;

    public TokenListTokenFilter(String sourceType, TokenList filterTokenList) {
        xPath.setNamespaceContext(new ISOTEINamespaceContext());            
        this.filterType = filterTokenList.getType();
        this.sourceType = sourceType;
        this.filterTokenList = filterTokenList;
        preFilterXPath="//tei:u/descendant::tei:w";
        idXPath = "@xml:id";
        if (!(("transcription").equals(sourceType) || 
               "norm".equals(sourceType) ||
               "lemma".equals(sourceType) ||
               "pos".equals(sourceType)
                )){
            preFilterXPath = "//tei:spanGrp[@type='" + sourceType + "']/descendant::tei:span[not(*)]";
            idXPath = "ancestor-or-self::tei:span[@from][1]/@from";
        }        
    }
    
    @Override
    public String getPreFilterXPath() {
        return preFilterXPath;
    }

    @Override
    public boolean accept(Element tokenNode) {
        if (sourceType.equals(filterType)){
            String tokenNodeValue;
            if (tokenNode instanceof Element){
                tokenNodeValue = tokenNode.getTextContent();
            } else {
                tokenNodeValue = tokenNode.getNodeValue();
            }
            String[] tokens = (tokenNodeValue + " ").split(" ");
            boolean contains = false;
            for (String token : tokens){
                contains = contains || filterTokenList.containsKey(token);
            }
            return contains;
        }
        // sourceType and filterType are different, so we have to navigate from the tokenNode
        // to the targetNode 
        // this is pretty inefficient
        try {
            String xpathToTarget = "";
            switch(filterType){
                case "transcription" : 
                    xpathToTarget = ".";
                    break;
                case "norm":
                case "lemma":
                case "pos" :
                    xpathToTarget = "@" + filterType;
                    break;
                default :
                    String startID = ((Node)xPath.evaluate(idXPath, tokenNode, XPathConstants.NODE)).getNodeValue();            
                    if (startID.startsWith("#")) startID = startID.substring(1);
                    System.out.println("StartID: " + startID);
                    xpathToTarget = "ancestor::tei:annotationBlock/descendant::tei:spanGrp[@type='" + filterType + "']/tei:span[@from='" + startID + "']";                
            }
            //System.out.println("XPath to target: " + xpathToTarget);
            Node targetNode = ((Node)xPath.evaluate(xpathToTarget, tokenNode, XPathConstants.NODE));
            String form;
            if (targetNode instanceof Element){
                form = targetNode.getTextContent();
            } else {
                form = targetNode.getNodeValue();
            }
            return filterTokenList.containsKey(form);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(TokenListTokenFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
}
