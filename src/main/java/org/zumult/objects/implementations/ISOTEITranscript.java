/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;

/**
 * 
 * @author Thomas_Schmidt
 */
public abstract class ISOTEITranscript extends AbstractXMLObject implements Transcript {

    String teiNamespaceURI = "http://www.tei-c.org/ns/1.0";
    
    XPath xPath = XPathFactory.newInstance().newXPath();
    
    XMLMetadata metadata;
    
    /* construct a new transcript from an XML document */
    public ISOTEITranscript(Document transcriptDocument) {
        super(transcriptDocument);
        xPath.setNamespaceContext(new ISOTEINamespaceContext());            
    }
    
    /* construct a new transcript from an XML string */
    public ISOTEITranscript(String transcriptXML) {
        super(transcriptXML);
        xPath.setNamespaceContext(new ISOTEINamespaceContext());            
    }

    // new for issue #148: initialise with Metadata
    public ISOTEITranscript(String transcriptXML, String metadataXML) {
        super(transcriptXML);
        metadata = new XMLMetadata(metadataXML);
        xPath.setNamespaceContext(new ISOTEINamespaceContext());            
    }

    // also new for #148
    public ISOTEITranscript(Document transcriptDocument, Document metadataDocument) {
        super(transcriptDocument);
        metadata = new XMLMetadata(metadataDocument);
        xPath.setNamespaceContext(new ISOTEINamespaceContext());            
    }


    @Override
    public int getNumberOfTokens() {
        return getTokenList("transcription").getNumberOfTokens();
    }

    @Override
    public int getNumberOfTypes() {
        return getTokenList("transcription").getNumberOfTypes();
    }
    
    
    @Override
    public Transcript getPart(int startIndex, int endIndex) {
        try {
            Element startAnnotationBlock = (Element)xPath.evaluate("//tei:annotationBlock[" + Integer.toString(startIndex) + "]",
                    getDocument().getDocumentElement(), XPathConstants.NODE);
            if (startAnnotationBlock==null){
                startAnnotationBlock = (Element)xPath.evaluate("//tei:annotationBlock[1]",
                    getDocument().getDocumentElement(), XPathConstants.NODE);
            }
            if (startAnnotationBlock==null){ return null; }
            Element endAnnotationBlock = (Element)xPath.evaluate("//tei:annotationBlock[" + Integer.toString(endIndex) + "]",
                    getDocument().getDocumentElement(), XPathConstants.NODE);
            if (endAnnotationBlock==null){
                endAnnotationBlock = (Element)xPath.evaluate("//tei:annotationBlock[last()]",
                    getDocument().getDocumentElement(), XPathConstants.NODE);
            }
            if (endAnnotationBlock==null){ return null; }
            
            String startID = startAnnotationBlock.getAttribute("xml:id");
            String endID = endAnnotationBlock.getAttribute("xml:id");
            
            return getPart(startID, endID, true);
            
            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }

    @Override
    public Transcript getPart(String id1, String id2, boolean expandToFullAnnotationBlock) {
        System.out.println("Trying " + id1 + " / " + id2);
        try {
            Element element1 = (Element)xPath.evaluate("//*[@xml:id='" + id1 + "']", getDocument().getDocumentElement(), XPathConstants.NODE);
            if (element1==null){                
                System.out.println("Do hammers");
                id1 = getAlternativeID(id1);
                element1 = (Element)xPath.evaluate("//*[@xml:id='" + id1 + "']", getDocument().getDocumentElement(), XPathConstants.NODE);                        
            }
            Element when1 = null;
            
            
            
            switch (element1.getLocalName()){
                case "when" :
                    when1 = element1;
                    break;
                case "annotationBlock" : case "pause" :
                    String startID = element1.getAttribute("start"); //.substring(1);
                    when1 = (Element)xPath.evaluate("//tei:when[@xml:id='" + startID + "']", 
                            getDocument().getDocumentElement(), XPathConstants.NODE);                    
                    //System.out.println("YES, it is clearly the KÄS " + element1.getAttribute("xml:id"));                    
                    break;
                case "w" :
                    // try to find the nearest anchor *before* that w
                    // then get its id and return the when element with that id
                    Element tryAnchor = (Element)xPath.evaluate("preceding::tei:anchor[1]", element1, XPathConstants.NODE);
                    if (tryAnchor!=null) {
                        String anchorID = tryAnchor.getAttribute("synch");
                        when1 = (Element)xPath.evaluate("//tei:when[@xml:id='" + anchorID + "']", 
                                getDocument().getDocumentElement(), XPathConstants.NODE);
                        break;
                    }
                    // nothing found, so get the start of the annotation block
                    Element annotationBlock = (Element)xPath.evaluate("ancestor::tei:annotationBlock", element1, XPathConstants.NODE);
                    String abStart = annotationBlock.getAttribute("start");
                    when1 = (Element)xPath.evaluate("//tei:when[@xml:id='" + abStart + "']", 
                            getDocument().getDocumentElement(), XPathConstants.NODE);
                    
            }
            //Element element2 = getDocument().getElementById(id2);
            Element element2 = (Element)xPath.evaluate("//*[@xml:id='" + id2 + "']", getDocument().getDocumentElement(), XPathConstants.NODE);
            if (element2==null){                
                System.out.println("Do hammers");
                id2 = getAlternativeID(id2);
                element2 = (Element)xPath.evaluate("//*[@xml:id='" + id2 + "']", getDocument().getDocumentElement(), XPathConstants.NODE);                        
            }
            
            Element when2 = null;
            switch (element2.getLocalName()){
                case "when" :
                    when2 = element2;
                    break;
                case "annotationBlock" : case "pause" :
                    String endID = element2.getAttribute("start"); //.substring(1);
                    when2 = (Element)xPath.evaluate("//tei:when[@xml:id='" + endID + "']", 
                            getDocument().getDocumentElement(), XPathConstants.NODE);
                    break;
                case "w" :
                    // try to find the nearest anchor *after* that w
                    // then get its id and return the when element with that id
                    Element tryAnchor = (Element)xPath.evaluate("following::tei:anchor[1]", element2, XPathConstants.NODE);
                    if (tryAnchor!=null) {
                        String anchorID = tryAnchor.getAttribute("synch");
                        when2 = (Element)xPath.evaluate("//tei:when[@xml:id='" + anchorID + "']", 
                                getDocument().getDocumentElement(), XPathConstants.NODE);
                        break;
                    }
                    // nothing found, so get the start of the annotation block
                    Element annotationBlock = (Element)xPath.evaluate("ancestor::tei:annotationBlock", element1, XPathConstants.NODE);
                    String abEnd = annotationBlock.getAttribute("end");
                    when1 = (Element)xPath.evaluate("//tei:when[@xml:id='" + abEnd + "']", 
                            getDocument().getDocumentElement(), XPathConstants.NODE);
                    
            }
            
            // now we should be sure that both when1 and when2 really are <when> elements
            Document copyDocument = IOHelper.DocumentFromText(toXML());
            
            // throw out annotationBlocks and other things on the same level
            NodeList nodes = (NodeList)xPath.evaluate("//tei:body/*", copyDocument.getDocumentElement(), XPathConstants.NODESET);
            /*String minTimeString = when1.getAttribute("interval");
            if (minTimeString==null || minTimeString.length()==0){
                minTimeString = "0.0";
            }
            double minTime = Double.parseDouble(minTimeString);*/
            double minTime = getInterval(when1);
            //double maxTime = Double.parseDouble(when2.getAttribute("interval"));
            double maxTime = getInterval(when2);
            
            for (int i=0; i<nodes.getLength(); i++){                
                Element node = (Element) nodes.item(i);
                String startID = node.getAttribute("start"); //.substring(1);                
                String endID = node.getAttribute("end"); // .substring(1);                  
                double startTime = getTime(startID);
                double endTime = getTime(endID);
                
                if((startTime<minTime && endTime<minTime) 
                        || (startTime>maxTime && endTime>maxTime)){
                    //System.out.println("Throwing out " + node.getAttribute("xml:id"));
                    node.getParentNode().removeChild(node);
                    
                } else {
                    if (startTime<minTime){
                        when1 = ((Element)xPath.evaluate("//*[@xml:id='" + startID + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                    }
                    if (endTime>maxTime){
                        // this is probably not quite right yet
                        // need to consider cases of overlap
                        when2 = ((Element)xPath.evaluate("//*[@xml:id='" + endID + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                        System.out.println("HERE ---" + when2.getAttribute("xml:id"));
                    }
                }
            }
            
            //System.out.println("We have START=" + when1.getAttribute("xml:id") + " and END=" + when2.getAttribute("xml:id"));
            
            // 06-01-2021, for issue #43
            String adjustedStart = ((Element)xPath.evaluate("//*[@start][1]", copyDocument.getDocumentElement(), XPathConstants.NODE)).getAttribute("start");            
            System.out.println("ADJUSTED_START " + adjustedStart);
            if (!(when1.getAttribute("xml:id").equals(adjustedStart))){
                when1 = ((Element)xPath.evaluate("//*[@xml:id='" + adjustedStart + "']", getDocument().getDocumentElement(), XPathConstants.NODE)); 
            }
            
            // throw out when elements
            NodeList whenNodes = (NodeList)xPath.evaluate("//tei:timeline/tei:when", copyDocument.getDocumentElement(), XPathConstants.NODESET);
            boolean in = false;
            boolean out = false;
            for (int i=0; i<whenNodes.getLength(); i++){                
                Element whenNode = (Element) whenNodes.item(i);
                String id = whenNode.getAttribute("xml:id");                
                in = in || id.equals(when1.getAttribute("xml:id"));
                if((!in || out) && (!(whenNode.getAttributeNode("interval")==null))){
                    whenNode.getParentNode().removeChild(whenNode);
                }
                out = out || id.equals(when2.getAttribute("xml:id"));
            }
            
            
            if(metadata!=null){
                return createNewInstance(copyDocument, metadata.getDocument());
            }else {
                return createNewInstance(copyDocument);
            }

        } catch (TransformerException | IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Transcript getPart(double time1, double time2, boolean expandToFullAnnotationBlock){
        String idOfFirst = null; // the ID of the last when element whose time is before time1
        String idOfLast = null;  // the ID of the first when element whose time is after time1
        try {
            String xPathString = "//tei:timeline/tei:when";
            //System.out.println(getDocument().getDocumentElement().toString());
            NodeList whenNodes = (NodeList)xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            // this could be done faster by using nested intervals
            for (int i=0; i<whenNodes.getLength(); i++){
                Element whenNode = (Element) whenNodes.item(i);
                String id = whenNode.getAttribute("xml:id");                
                /*String interval = whenNode.getAttribute("interval");
                double time = 0.0;
                if (interval!=null && interval.length()>0){
                    time = Double.parseDouble(interval);
                }*/
                double time = getInterval(whenNode);
                //System.out.println(id + " / " + time);
                if (time<=time1){
                    idOfFirst = id;
                }
                if (time>=time2){
                    idOfLast = id;
                    break;
                }                
            }
        } catch (XPathExpressionException ex) {
            // should never get here
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getPart(idOfFirst, idOfLast, expandToFullAnnotationBlock);
    }

    @Override
    public double getStartTime() {
        // the start time is equal to the time value of the when element which the first body element refers to via @start
        try {
            Element firstElement = (Element) xPath.evaluate("//tei:body/*[@start][1]", getDocument().getDocumentElement(), XPathConstants.NODE);
            if (firstElement==null) return -1;
            String startID = firstElement.getAttribute("start"); //.substring(1);
            //System.out.println("StartID: " + startID);
            // no idea why this is not working here. it does work above (or not?)
            //Element whenElement = xmlDocument.getElementById(startID);
            Element whenElement = (Element) xPath.evaluate("//tei:when[@xml:id='" + startID + "']", getDocument().getDocumentElement(), XPathConstants.NODE);
            //System.out.println(whenElement);
            /*String interval = whenElement.getAttribute("interval");
            if (interval==null || interval.length()==0){
                return 0.0;
            }
            return Double.parseDouble(interval);*/
            return getInterval(whenElement);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public double getEndTime() {
        try {
            NodeList allElements = (NodeList) xPath.evaluate("//tei:body/*[@end]", getDocument().getDocumentElement(), XPathConstants.NODESET);
            double maxTimeValue = 0.0;
            for (int i=0; i<allElements.getLength(); i++){
                Element element = (Element) allElements.item(i);
                String endID = element.getAttribute("end"); //.substring(1);
                Element whenElement = (Element) xPath.evaluate("//tei:when[@xml:id='" + endID + "']", getDocument().getDocumentElement(), XPathConstants.NODE);
                //System.out.println(endID);
                /*String interval = whenElement.getAttribute("interval");
                if (interval!=null || interval.length()>0){
                    double timeValue = Double.parseDouble(interval);
                    maxTimeValue = Math.max(timeValue, maxTimeValue);
                }*/
                double timeValue = getInterval(whenElement);
                maxTimeValue = Math.max(timeValue, maxTimeValue);
                
            }
            return maxTimeValue;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public TokenList getTokenList(String type) {
        String xpath;
        switch (type){
            case "transcription" : 
            case "norm" :
            case "lemma" :
            case "pos" :
                xpath = "//tei:u/descendant::tei:w";
                break;
            default :
                xpath = "//tei:spanGrp[@type='" + type + "']/descendant::tei:span[not(*)]";
         }
        final String fXpath = xpath;
        TokenFilter dummyFilter = new TokenFilter(){
            @Override
            public String getPreFilterXPath() {
                return fXpath;
            }

            @Override
            public boolean accept(Element tokenNode) {
                return true;
            }                
        };
        return getTokenList(type, dummyFilter);
    }

    @Override
    public TokenList getTokenList(String type, TokenFilter filter) {
        TokenList result = new DefaultTokenList(type); 
        try {
            NodeList allElements = (NodeList) xPath.evaluate(filter.getPreFilterXPath(), getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allElements.getLength(); i++){
                Element element = (Element) allElements.item(i);
                if (filter.accept(element)){
                    String form; 
                    switch(type){
                        case "transcription" :
                            form = element.getTextContent();
                            result.add(form);
                            break;
                        case "norm" :
                        case "lemma" :
                        case "pos" :
                            form = element.getAttribute(type);
                            String[] tokenizedForm = form.split(" ");
                            for (String token : tokenizedForm){
                                result.add(token);                                
                            }
                            break;
                        default :
                            form = element.getTextContent();
                            result.add(form);
                            break;
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
        
    }
    
    @Override
    public Element getElementById(String id) {
         return getDocument().getElementById(id);        
    }

    @Override
    public NodeList getTokensByPOS(String pos) {
        NodeList posNodes = null;
        try {
            String xPathString = "//tei:w[@pos='" + pos + "']";
            posNodes = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return posNodes;
    }
    
    @Override
    public NodeList getAnchorsByAttribute(String attribute) {
        NodeList posNodes = null;
        try {
            String xPathString = "//tei:anchor[@synch='" + attribute + "']";
            posNodes = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return posNodes;
    }
    
    
    @Override
    public NodeList getAnnotationBlocksBySpeaker(String speaker) {
        NodeList posNodes = null;
        try {
            String xPathString = "//tei:annotationBlock[@who='" + speaker + "']";
            posNodes = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return posNodes;
    }

    @Override
    public NodeList getAllTokens() {
        return getDocument().getElementsByTagName("w");
    }

    @Override
    public Document getXmlDocument() {
        return getDocument();
    }

    @Override
    public String getSpeakerInitialsBySpeakerID(String speakerID) {
        try {
            // new 04.08.2021, Elena: this is wrong
            /* 
            speakerInitials = "";
            String xPathString = "//tei:idno[@type='AGD-ID']";
            NodeList personNodes = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            speakerInitials = personNodes.item(0).getParentNode().getAttributes().getNamedItem("xml:id").getNodeValue();
            */

            Element idnoElement = ((Element)xPath.evaluate("//tei:person/tei:idno[text()='"+speakerID+"']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
            if (idnoElement!=null){
                return idnoElement.getParentNode().getAttributes().getNamedItem("n").getNodeValue();
            }
            return null;
            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    // new 13-12-2019: it is much faster this way
    
    Map<String, Double> timeIndex;
    
    private double getTime(String whenID){
        if (timeIndex==null){
            indexTime();
        }
        if (timeIndex.containsKey(whenID)){
            return timeIndex.get(whenID);
        }
        return 0.0;
    }

    // TS, added 19-01-2024: the XML schema still seems to allow first <when> without @interval
    private double getInterval(Element element){
        String interval = element.getAttribute("interval");
        if (interval==null || interval.isEmpty()){
            return 0.0;
        }
        double time = Double.parseDouble(interval);
        return time;        
    }
    
    private void indexTime() {
        timeIndex = new HashMap<>();
        try {
            String xPathString = "//tei:when";
            NodeList allWhens = (NodeList) xPath.evaluate(xPathString, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<allWhens.getLength(); i++){
                Element thisWhen = (Element) allWhens.item(i);
                String id = thisWhen.getAttribute("xml:id");
                timeIndex.put(id, getInterval(thisWhen));
            }            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public double getNextTimeForID(String id){
        if (id==null || id.length()==0) return -1;
        try {
            Element element = (Element)xPath.evaluate("//*[@xml:id='" + id + "']", getDocument().getDocumentElement(), XPathConstants.NODE);
            String name = element.getLocalName();
            switch (name){
                case "when" : 
                    Element nextWhenElement = (Element)xPath.evaluate("//following-sibling::tei:when[1]", getDocument().getDocumentElement(), XPathConstants.NODE);
                    return getInterval(nextWhenElement);
                case "annotationBlock" : 
                    String startID = element.getAttribute("end");
                    Element whenElement = ((Element)xPath.evaluate("//tei:when[@xml:id='" + startID + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                    return getInterval(whenElement);
                default :
                    if (!(element.getAttribute("end").isEmpty())){                        
                        String startID2 = element.getAttribute("end");
                        Element whenElement2 = ((Element)xPath.evaluate("//tei:when[@xml:id='" + startID2 + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                        return getInterval(whenElement2);
                    }
                    // nearest anchor?
                    Element anchorElement = ((Element)xPath.evaluate("following-sibling::tei:anchor[1]", element, XPathConstants.NODE));                   
                    if (anchorElement!=null){
                        String synch = anchorElement.getAttribute("synch");
                        Element whenElement2 = ((Element)xPath.evaluate("//tei:when[@xml:id='" + synch + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                        return getInterval(whenElement2);
                    } else {
                        //System.out.println("Case 3 : We are trying annotationBlock");
                        // superordinate annotationBlock?
                        Element annotationBlock = ((Element)xPath.evaluate("ancestor::tei:annotationBlock[1]", element, XPathConstants.NODE));                   
                        String startID2 = annotationBlock.getAttribute("end");
                        Element whenElement3 = ((Element)xPath.evaluate("//tei:when[@xml:id='" + startID2 + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                        return getInterval(whenElement3);                            
                    }
            }                        
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;        
        
        
    }
    
    @Override
    public double getTimeForID(String id) {
        if (id==null || id.length()==0) return -1;
        try {
            Element element = (Element)xPath.evaluate("//*[@xml:id='" + id + "']", getDocument().getDocumentElement(), XPathConstants.NODE);
            String name = element.getLocalName();
            //System.out.println("Name of element " + name);
            //System.out.println("ID of element " + element.getAttribute("xml:id"));
            switch (name){
                case "when" : 
                    //return Double.parseDouble(element.getAttribute("interval"));
                    return getInterval(element);
                case "annotationBlock" : 
                    String startID = element.getAttribute("start");
                    Element whenElement = ((Element)xPath.evaluate("//tei:when[@xml:id='" + startID + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                    //return Double.parseDouble(whenElement.getAttribute("interval"));
                    return getInterval(whenElement);
                default :
                    // new, issue #71 : this is, among others maybe, for pauses
                    // watch out!!! getAttribute never returns null, but empty string??
                    //if (element.getAttribute("start")!=null){
                    if (!(element.getAttribute("start").isEmpty())){                        
                        //System.out.println("Case 1 : the element does have a start attribute");
                        String startID2 = element.getAttribute("start");
                        Element whenElement2 = ((Element)xPath.evaluate("//tei:when[@xml:id='" + startID2 + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                        //return Double.parseDouble(whenElement2.getAttribute("interval"));
                        return getInterval(whenElement2);
                    }
                    // nearest anchor?
                    Element anchorElement = ((Element)xPath.evaluate("preceding-sibling::tei:anchor[1]", element, XPathConstants.NODE));                   
                    if (anchorElement!=null){
                        //System.out.println("Case 2 : there is a preceding anchor element");
                        String synch = anchorElement.getAttribute("synch");
                        //System.out.println("Anchor synch = " + synch);
                            
                        Element whenElement2 = ((Element)xPath.evaluate("//tei:when[@xml:id='" + synch + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                        //return Double.parseDouble(whenElement2.getAttribute("interval"));
                        return getInterval(whenElement2);
                    } else {
                        //System.out.println("Case 3 : We are trying annotationBlock");
                        // superordinate annotationBlock?
                        Element annotationBlock = ((Element)xPath.evaluate("ancestor::tei:annotationBlock[1]", element, XPathConstants.NODE));                   
                        String startID2 = annotationBlock.getAttribute("start");
                        Element whenElement3 = ((Element)xPath.evaluate("//tei:when[@xml:id='" + startID2 + "']", getDocument().getDocumentElement(), XPathConstants.NODE));                   
                        //return Double.parseDouble(whenElement3.getAttribute("interval"));                        
                        return getInterval(whenElement3);                            
                    }
            }                        
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;        
    }

    @Override
    public String getSpeakerIDBySpeakerInitials(String speakerInitials) {
        try {
            /*<person xml:id="SK" n="SK">
            <idno type="AGD-ID">FOLK_S_00028</idno>
            <persName><forename>Sabine</forename><abbr>SK</abbr></persName>
            </person>*/            
            Element idnoElement = ((Element)xPath.evaluate("//tei:person[@n='" + speakerInitials + "']/tei:idno", getDocument().getDocumentElement(), XPathConstants.NODE));                   
            if (idnoElement!=null){
                return idnoElement.getTextContent();
            }
            return null;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    @Override
    public String getID() {
        // 2026-02-05, remade for #268
        // first attempt: see if the root element has an xml:id attribute
        // this is the preferred solution from now on because it conforms to the generic TEI schema
        if (getDocument().getDocumentElement().hasAttributeNS("http://www.w3.org/XML/1998/namespace", "id")){
            String id = getDocument().getDocumentElement().getAttributeNS("http://www.w3.org/XML/1998/namespace", "id");
            return id;
        }
        try {
            Element idnoElement = ((Element)xPath.evaluate("//tei:TEI/tei:idno", getDocument().getDocumentElement(), XPathConstants.NODE));
            if (idnoElement!=null){
                String id = idnoElement.getTextContent();
                // workaround for issue #60
                // we do not want this here any longer
                /*if (id.contains("_DF_")){
                    int i = id.indexOf("_DF_");
                    id = id.substring(0,i);
                }
                return id;*/
            }
            return null;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getAnnotationBlockID(String annotationBlockID, int distance) {
        try {
            Element ab = (Element)xPath.evaluate("//*[@xml:id='" + annotationBlockID + "']", getDocument().getDocumentElement(), XPathConstants.NODE);
            if (ab==null){                
                System.out.println("Do hammers");
                annotationBlockID = getAlternativeID(annotationBlockID);                
            }

            String axis = "following-sibling";
            if (distance<0) axis = "preceding-sibling";
            String xp = "//tei:annotationBlock[@xml:id='" + annotationBlockID
                    + "']/" + axis +  "::tei:annotationBlock";
            //System.out.println(xp);
            NodeList nodes = (NodeList)xPath.evaluate(xp, getDocument().getDocumentElement(), XPathConstants.NODESET);
            /*for (int i=0; i<nodes.getLength(); i++){
                System.out.println(((Element)nodes.item(i)).getAttribute("xml:id"));
            }*/
            if (nodes.getLength()==0){
                return annotationBlockID;
            }
            if (distance>0){
                Element element = (Element) nodes.item(Math.min(Math.abs(distance)-1, nodes.getLength()-1));
                return element.getAttribute("xml:id");
            } else if (distance<0){
                Element element = (Element) nodes.item(Math.max(nodes.getLength() - Math.abs(distance), 0));
                return element.getAttribute("xml:id");
                
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }

    @Override
    public String getFirstAnnotationBlockIDForTime(double time) {
        try {
            NodeList nodes = (NodeList)xPath.evaluate("//tei:body/tei:annotationBlock", getDocument().getDocumentElement(), XPathConstants.NODESET);
            if ((nodes.getLength()==0)){
                return null;
            }
            // boundary condition : if the first annotation block already starts after the time, return its id
            Element node = (Element) nodes.item(0);
            String startID = node.getAttribute("start"); //.substring(1);                
            double startTime = getTime(startID);
            if (startTime>time){
                //System.out.println("The boundary!");
                return node.getAttribute("xml:id");
            }
            for (int i=0; i<nodes.getLength(); i++){                
                node = (Element) nodes.item(i);
                startID = node.getAttribute("start"); //.substring(1);                
                startTime = getTime(startID);
                String endID = node.getAttribute("end"); //.substring(1);                
                double endTime = getTime(endID);
                if ((startTime<=time) && (time<=endTime)){
                    return node.getAttribute("xml:id");
                }
            }
            // nothing found, return the last node  
            return node.getAttribute("xml:id");
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void removeAnnotations() {
        try {
            String xp = "//tei:spanGrp";
            NodeList nodes = (NodeList)xPath.evaluate(xp, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){                
                Element node = (Element) nodes.item(i);
                node.getParentNode().removeChild(node);
            }

            String xp2 = "//tei:w";
            NodeList nodes2 = (NodeList)xPath.evaluate(xp2, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes2.getLength(); i++){                
                Element node = (Element) nodes2.item(i);
                node.removeAttribute("norm");
                node.removeAttribute("lemma");
                node.removeAttribute("pos");
                node.removeAttribute("phon");
            }
            
            documentChanged();


        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);            
        }
        
    }

    private String getAlternativeID(String id) {
        // workaround for issue #58
        String alternativeID;
        if (id.startsWith("c")){
            String numberPart = id.substring(1);
            try {
                int number = Integer.parseInt(numberPart);
                if (number>1){
                    alternativeID = "c" + Integer.toString(number-1);
                } else {
                    alternativeID = "c" + Integer.toString(number+1);                            
                }
                System.out.println("Alternative " + alternativeID + " for " + id);
                return alternativeID;
            } catch (NumberFormatException nfe) {
                System.out.println("Couldn't turn " + numberPart + " into a number");
                return id;
            }
        }
        return id;
    }

    @Override
    public String getLanguage() {
        try {
            Element textElement = ((Element)xPath.evaluate("//tei:text", getDocument().getDocumentElement(), XPathConstants.NODE));
            return textElement.getAttribute("xml:lang");
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
        
    }
    
    @Override
    public void setTimelineToZero() {
        try {
            Element firstWhenElement = ((Element)xPath.evaluate("//tei:when[1]", getDocument().getDocumentElement(), XPathConstants.NODE));
            double firstTime = Double.parseDouble(firstWhenElement.getAttribute("interval"));
            String xp = "//tei:when";
            NodeList nodes = (NodeList)xPath.evaluate(xp, getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){                
                Element node = (Element) nodes.item(i);
                double thisTime = Double.parseDouble(node.getAttribute("interval"));
                double newTime = thisTime - firstTime;
                node.setAttribute("interval", Double.toString(newTime));                
            }
            documentChanged();
            indexTime();
            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public List<String> getRecordings() {
        List<String> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList)xPath.evaluate("//tei:media", getDocument().getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){                
                Element node = (Element) nodes.item(i);
                String url = node.getAttribute("url");
                result.add(url);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
        
    }

    
    
    @Override
    public void setRecordings(List<String> recordingURLs) {
        try {
            Element recordingStmt = ((Element)xPath.evaluate("//tei:recordingStmt", getDocument().getDocumentElement(), XPathConstants.NODE));
            while (recordingStmt.hasChildNodes()) {
               recordingStmt.removeChild(recordingStmt.getFirstChild());
            }
            for (String recordingURL : recordingURLs){
                boolean isAudio = (recordingURL.toLowerCase().endsWith(".wav") || recordingURL.toLowerCase().endsWith(".mp3"));                
                /* <recording type="audio">
                        <media mimeType="audio/wav"
                            url="https://cocoon.huma-num.fr/data/eslo/masters/ESLO1_CONF_502.wav"/>
                        <media mimeType="audio/mp3"
                            url="https://cocoon.huma-num.fr/data/eslo/ESLO1_CONF_502.mp3"/>
                    </recording>
                */    
                Element recording = getDocument().createElementNS(teiNamespaceURI, "recording");
                recordingStmt.appendChild(recording);
                if (isAudio){
                    recording.setAttribute("type", "audio");
                } else {
                    recording.setAttribute("type", "video");                    
                }
                Element media = getDocument().createElementNS(teiNamespaceURI, "media");
                recording.appendChild(media);
                media.setAttribute("url", recordingURL);
                String mimeType = "audio";
                if (!isAudio) mimeType = "video";
                String suffix = recordingURL.substring(recordingURL.lastIndexOf(".")+1);
                mimeType += "/" + suffix;
                media.setAttribute("mimeType", mimeType);
                
                documentChanged();
            }
            
            
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ISOTEITranscript.class.getName()).log(Level.SEVERE, null, ex);
        }
     
    }
    
    
    
    
    // what is this???
    public abstract ISOTEITranscript createNewInstance(Document transcriptDocument, Document metadataDocument);
    public abstract ISOTEITranscript createNewInstance(Document transcriptDocument);
    
}