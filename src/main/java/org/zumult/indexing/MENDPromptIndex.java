/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;

/**
 *
 * @author thomas.schmidt
 */
public class MENDPromptIndex implements Indexer {

    
    String OUT = "D:\\WebApplication3\\src\\java\\data\\MENDPromptIndex.xml";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new MENDPromptIndex().index();
        } catch (IOException ex) {
            Logger.getLogger(MENDPromptIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void index() throws IOException {
        try {
            BackendInterface bi = BackendInterfaceFactory.newBackendInterface();

            IDList allTranscripts = new IDList("transcript");
            IDList events = bi.getEvents4Corpus("MEND");
            for (String eventID : events){
                IDList speechEvents = bi.getEvent(eventID).getSpeechEvents();
                for (String speechEventID : speechEvents){
                    IDList transcripts = bi.getSpeechEvent(speechEventID).getTranscripts();
                    allTranscripts.addAll(transcripts);
                }
            }
            
            XPath xPath = XPathFactory.newInstance().newXPath();    
            xPath.setNamespaceContext(new ISOTEINamespaceContext());            
            
            Map<String, Map> mapAnno = new HashMap<>();
            
            for (String transcriptID : allTranscripts){
                System.out.println(transcriptID);
                Transcript transcript = bi.getTranscript(transcriptID);
                Document transcriptDoc = transcript.getDocument();
                NodeList nodes = (NodeList)xPath.evaluate("//tei:annotationBlock[descendant::tei:spanGrp[@type='prompt-reference']]", 
                                    transcriptDoc.getDocumentElement(), XPathConstants.NODESET);
                for (int i=0; i<nodes.getLength(); i++){                
                    Element ab = (Element) nodes.item(i);
                    String abID = ab.getAttribute("xml:id");
                    Element span = (Element)xPath.evaluate("descendant::tei:spanGrp[@type='prompt-reference']/descendant::tei:span", 
                                        ab, XPathConstants.NODE);
                    String target = span.getAttribute("target");
                    String annotation = span.getTextContent();
                    //System.out.println(abID + " / " + target + " / " + annotation);
                    
                    if (!(mapAnno.containsKey(annotation))){
                        mapAnno.put(annotation, new HashMap<String, ArrayList<String>>());
                    }
                    
                    HashMap<String, ArrayList<String>> mapSentenceToID = (HashMap<String, ArrayList<String>>) mapAnno.get(annotation);
                    
                    if (!(mapSentenceToID.containsKey(target))){
                        mapSentenceToID.put(target, new ArrayList<String>());
                    }
                    
                    ArrayList<String> ids = mapSentenceToID.get(target);
                    ids.add(transcriptID + "#" + abID);
                    
                }
                
                
            } 
            
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance(); 
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder(); 
            Document resultDocument = documentBuilder.newDocument();
 
            // root element
            Element root = resultDocument.createElement("index");
            resultDocument.appendChild(root);
 
             for (String key : mapAnno.keySet()){
                System.out.println("=================");
                System.out.println(key);
                System.out.println("=================");
                
                // employee element
                Element thisKeyElement = resultDocument.createElement("prompt");
                thisKeyElement.setAttribute("type", key);
                root.appendChild(thisKeyElement);

                HashMap<String, ArrayList<String>> mapSentenceToID = (HashMap<String, ArrayList<String>>) mapAnno.get(key);                    

                for (String sentenceNo : mapSentenceToID.keySet()){
                    Element thisSentenceElement = resultDocument.createElement("sentence");
                    thisSentenceElement.setAttribute("no", sentenceNo.substring(sentenceNo.indexOf("#")+1));
                    thisKeyElement.appendChild(thisSentenceElement);
                    
                    System.out.println("   " + sentenceNo);
                    ArrayList<String> ids = mapSentenceToID.get(sentenceNo);
                    for (String id : ids){
                        Element thisIDElement = resultDocument.createElement("annotationBlock");
                        int i = id.indexOf("#");
                        thisIDElement.setAttribute("t-id", id.substring(0,i));
                        thisIDElement.setAttribute("ab-id", id.substring(i+1));
                        thisSentenceElement.appendChild(thisIDElement);
                    }
                }
            }
             
            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(resultDocument);
            StreamResult streamResult = new StreamResult(new File(OUT));

            // If you use
            // StreamResult result = new StreamResult(System.out);
            // the output will be pushed to the standard output ...
            // You can use that for debugging 

            transformer.transform(domSource, streamResult);

            System.out.println("Done creating XML File");
             
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(MENDPromptIndex.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(MENDPromptIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(MENDPromptIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
