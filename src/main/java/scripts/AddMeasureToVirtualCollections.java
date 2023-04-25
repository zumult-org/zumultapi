/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.indexing.AddMeasuresToSpeechEventIndex;
import org.zumult.indexing.Indexer;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.io.TimeUtilities;
import org.zumult.io.XMLReader;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.AndFilter;
import org.zumult.objects.implementations.NegatedFilter;
import org.zumult.objects.implementations.TokenListTokenFilter;

/**
 *
 * @author Elena
 * 
 * This script creates different measures for virtual collections. 
 * It was used just once for virtual collections in ZuHand.
 */
public class AddMeasureToVirtualCollections implements Indexer {
    String DIR_IN = "C:\\Users\\Elena\\Desktop\\IDS\\MK\\Transformation\\in";
    String DIR_OUT = "C:\\Users\\Elena\\Desktop\\IDS\\MK\\Transformation\\out";

        
    public static void main(String[] args) {
        try {
            new AddMeasureToVirtualCollections().index();
        } catch (IOException ex) {
            Logger.getLogger(AddMeasuresToSpeechEventIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void index() throws IOException {

        try {
            ArrayList<TokenList> tokenLists = new ArrayList<>();
            for (String WL : Constants.LEIPZIG_WORDLISTS){
               tokenLists.add(XMLReader.readTokenListFromFile(new File(Configuration.getWordlistPath() + "/" + WL + ".xml")));
            }
        
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            File folder = new File(DIR_IN);
            File[] listOfFiles = folder.listFiles();
            
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().startsWith("VirtualCollection")) {
                    try {
                        
                        System.out.println("************** "+ file.getName() +" ************************");
                        Document doc = IOHelper.readDocument(file);
                        NodeList collection = doc.getElementsByTagName("virtualCollectionItem");
             
                        for (int i = 0; i < collection.getLength(); i++) {
                            Element element = (Element) collection.item(i);

                            String transcriptID = element.getAttribute("transcriptID");
                            String title = element.getElementsByTagName("title").item(0).getTextContent();
                            System.out.println(transcriptID + ": " + title);
                            Transcript transcript = backendInterface.getTranscript(transcriptID);
                            
                            String startAnnotationBlockID = element.getAttribute("startAnnotationBlockID");
                            String endAnnotationBlockID = element.getAttribute("endAnnotationBlockID");
                            
                            System.out.println("start: " + startAnnotationBlockID +  ", end: " + endAnnotationBlockID);
                            
                            XPath xPath = XPathFactory.newInstance().newXPath();
                            xPath.setNamespaceContext(new ISOTEINamespaceContext());
                            
                            String xPathStringStart = "//tei:*[@xml:id = '" + startAnnotationBlockID + "']";
                            String xPathStringEnd = "//tei:*[@xml:id = '" + endAnnotationBlockID + "']";
                            
                            Element startAnnotationBlockElement = (Element)xPath.evaluate(xPathStringStart, transcript.getDocument().getDocumentElement(), XPathConstants.NODE);
                            Element endAnnotationBlockElement = (Element)xPath.evaluate(xPathStringEnd, transcript.getDocument().getDocumentElement(), XPathConstants.NODE);
                            
                            String startID = startAnnotationBlockElement.getAttribute(Constants.ATTRIBUTE_NAME_START);
                            String endID = endAnnotationBlockElement.getAttribute(Constants.ATTRIBUTE_NAME_END);
                            System.out.println("start: " + startID +  ", end: " + endID);
                            
                            Double start = transcript.getTimeForID(startID);
                            Double end = transcript.getTimeForID(endID);
                            
                            Transcript part = transcript.getPart(startID, endID, true);                            
                                                        
                            // get number of speakers
                            HashSet<String> speakerSet =  new HashSet();
                            String xPathStringSpeaker = "//tei:annotationBlock";
                            NodeList speakerNodeList = (NodeList) xPath.evaluate(xPathStringSpeaker, part.getDocument().getDocumentElement(), XPathConstants.NODESET);
                            for (int j = 0; j<speakerNodeList.getLength(); j++){
                                Element ab = (Element) speakerNodeList.item(j);
                                speakerSet.add(ab.getAttribute(Constants.ATTRIBUTE_NAME_WHO));
                            }
                            Element speakers = doc.createElement("speakers");
                            speakers.setTextContent(String.valueOf(speakerSet.size()));
                            element.getElementsByTagName("display-hints").item(0).appendChild(speakers);
                            
                            // get duration
                            long duration = (long) (end - start);
                            Element durationElement = doc.createElement("duration");
                            durationElement.setTextContent(TimeUtilities.formatDigital(duration));
                            element.getElementsByTagName("display-hints").item(0).appendChild(durationElement);
                                                  
                            // get normRate
                            double normRate = getNormRate(part);                    
                            Element normRateElement = doc.createElement("measure");
                            normRateElement.setAttribute("type", "normRate");
                            normRateElement.setAttribute("normRate", String.format("%.2f", normRate));
                        
                            // get perMilTokensOverlapsWithMoreThan2Words
                            double perMilTokensOverlapsWithMoreThan2WordsSpeechEvent = getOverlapMeasure(part);
                            Element perMilTokensOverlapsWithMoreThan2WordsElement = doc.createElement("measure");
                            perMilTokensOverlapsWithMoreThan2WordsElement.setAttribute("type", "perMilTokensOverlapsWithMoreThan2Words");
                            perMilTokensOverlapsWithMoreThan2WordsElement.setAttribute("perMilTokensOverlapsWithMoreThan2Words", String.format("%.2f", perMilTokensOverlapsWithMoreThan2WordsSpeechEvent));
                            
                            // get articulationRate
                            double articulationRate = getArticulationRate(part);                    
                            Element articulationRateElement = doc.createElement("measure");
                            articulationRateElement.setAttribute("type", "articulationRate");
                            articulationRateElement.setAttribute("articulationRate", String.format("%.2f", articulationRate));
                            
                            // create element for measures
                            Element measures = doc.createElement("measures");
                            measures.appendChild(normRateElement);
                            measures.appendChild(perMilTokensOverlapsWithMoreThan2WordsElement);
                            measures.appendChild(articulationRateElement);
                            
                            // get intersection measure
                            TokenList lemmaList = part.getTokenList("lemma", getFilter());
                            int originalLemmas = lemmaList.getNumberOfTypes();
                            int originalTokens = lemmaList.getNumberOfTokens();                     
                        
                            int m = 0;
                            for (TokenList tl : tokenLists){                    
                                TokenList intersect = lemmaList.intersect(tl);  
                                int intersectionLemmas = intersect.getNumberOfTypes();
                                int intersectionTokens = intersect.getNumberOfTokens();
                                double lemmasRatio = (double) intersectionLemmas / originalLemmas;
                                double tokensRatio = (double) intersectionTokens / originalTokens;

                                Element intersectionElement = doc.createElement("measure");
                                intersectionElement.setAttribute("type", "intersection");
                                intersectionElement.setAttribute("reference", Constants.LEIPZIG_WORDLISTS[m]);
                                intersectionElement.setAttribute("lemmas", String.valueOf(intersectionLemmas));
                                intersectionElement.setAttribute("tokens", String.valueOf(intersectionTokens));
                                intersectionElement.setAttribute("tokens_ratio", String.format(Locale.US, "%.2f", tokensRatio));
                                intersectionElement.setAttribute("lemmas_ratio", String.format(Locale.US, "%.2f", lemmasRatio));
                                measures.appendChild(intersectionElement);
                                m++;
                            }

                            element.getElementsByTagName("display-hints").item(0).appendChild(measures);

                        }
                        IOHelper.writeDocument(doc, new File(DIR_OUT, file.getName()));
                        
                    } catch (SAXException | ParserConfigurationException | TransformerException | XPathExpressionException ex) {
                        Logger.getLogger(AddMeasureToVirtualCollections.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(AddMeasureToVirtualCollections.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }   
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(AddMeasureToVirtualCollections.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /*************** the following methods come from Measure_1.java, Measure_7.java, Meausre_8.java, Measure_12.java ***************************/
    
    TokenFilter getFilter() throws IOException, SAXException, ParserConfigurationException{                     
        TokenList posFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/MEASURE_1_POS_FILTER.xml");
        TokenFilter posFilter = new NegatedFilter(new TokenListTokenFilter("lemma", posFilterTokenList));
        TokenList ngirrFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/FOLK_NGIRR_OHNE_DEREWO.xml");
        TokenFilter ngirrFilter = new NegatedFilter(new TokenListTokenFilter("lemma", ngirrFilterTokenList));
        TokenFilter filter = new AndFilter(posFilter, ngirrFilter);
        return filter;
    }
    
    double getArticulationRate(Transcript transcript) throws XPathExpressionException, Exception{
        int countSyllables = 0;
        double sumTime = 0;
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new ISOTEINamespaceContext());

        NodeList tokens = (NodeList) xPath.evaluate("//tei:w", transcript.getDocument().getDocumentElement(), XPathConstants.NODESET);    
        for (int n=0; n<tokens.getLength(); n++){
            Element token = (Element) tokens.item(n);
            String phon = token.getAttribute("phon") + ".";
            int countSyllablesForToken = phon.length() - phon.replaceAll("\\.", "").length();
            countSyllables+=countSyllablesForToken;                                    
        }

        NodeList absWithW = (NodeList) xPath.evaluate("//tei:annotationBlock[descendant::tei:w]", transcript.getDocument().getDocumentElement(), XPathConstants.NODESET);    
        for (int t=0; t<absWithW.getLength(); t++){
            Element ab = (Element) absWithW.item(t);
            Element startWhen = (Element) xPath.evaluate("//tei:when[@xml:id='" + ab.getAttribute("start") + "']", transcript.getDocument().getDocumentElement(), XPathConstants.NODE);     
            Element endWhen = (Element) xPath.evaluate("//tei:when[@xml:id='" + ab.getAttribute("end") + "']", transcript.getDocument().getDocumentElement(), XPathConstants.NODE);     

            double startTime = Double.parseDouble(startWhen.getAttribute("interval"));
            double endTime = Double.parseDouble(endWhen.getAttribute("interval"));
            double dur = endTime - startTime;

            NodeList pauses = (NodeList) xPath.evaluate("descendant::tei:pause", ab, XPathConstants.NODESET);     
            dur-=pauses.getLength() * 0.2;
            sumTime+=dur;
        }
                        
        double articulationRate = countSyllables / sumTime;
        return articulationRate;
    }
    
    double getNormRate(Transcript transcript){
        int countNorm = 0;
        NodeList allTokens = transcript.getAllTokens();
        for (int token = 0; token < allTokens.getLength(); token++) {
            Element node = (Element)allTokens.item(token);
            boolean filtered = node.getAttribute("pos").matches("\\b(NGHES|XY|AB|\\?\\?\\?|SPELL|UI)\\b"); // || filteredNGIRRs;
            String normalisedForm = node.getAttribute("norm");
            String transcribedForm = node.getTextContent();
            if (!normalisedForm.equalsIgnoreCase(transcribedForm) && !filtered) {
                countNorm++;
            }
        }
  
        int numberOfTokens = transcript.getNumberOfTokens();
        double normRate = (double) countNorm / (double) numberOfTokens * 100;
        return normRate;
                            
    }
   
    double getOverlapMeasure(Transcript transcript){
        int numberOfTokens = transcript.getNumberOfTokens();
        int nrOfOverlapsWithMoreThan2WordsInPart = 0;
        NodeList anchors = transcript.getXmlDocument().getElementsByTagName("anchor");

        List<String> synchs = new ArrayList();
        List<String> uniqueSynchs = new ArrayList();
        List<String> doubleSynchs = new ArrayList();

        for (int a = 0; a < anchors.getLength(); a++) {
            Node anchor = anchors.item(a);
            synchs.add(anchor.getAttributes().item(0).getTextContent());
        }

        for (String synch : synchs) {
            if (!uniqueSynchs.contains(synch)) {
                uniqueSynchs.add(synch);
            } else {
                doubleSynchs.add(synch);
            }
        }

        for (int a = 0; a < doubleSynchs.size()-1; a++) {
            String doubleSynch0 = doubleSynchs.get(a);
            String doubleSynch1 = doubleSynchs.get(a+1) != null ? doubleSynchs.get(a+1) : null;

            // get the first pair of double anchors
            NodeList bundle = transcript.getAnchorsByAttribute(doubleSynch0);

            // get the second pair
            NodeList bundle1 = transcript.getAnchorsByAttribute(doubleSynch1);

            for (int j = 1; j < bundle.getLength(); j++) {
                Node a1 = bundle.item(j);
                Node a0 = bundle.item(j-1);
                Node b1 = bundle1.item(j);
                Node b0 = bundle.item(j-1);

                int nrOfOverlappingWords = 0;
                int nrOfOverlaps = 0;
                int nrOfOverlapsWithMoreThan2Words = 0;

                Node parentA0 = a0.getParentNode();
                String idStringA0 = "";
                String parentNameA0 = parentA0.getNodeName();

                while (parentNameA0 != "seg") {
                    parentA0 = parentA0.getParentNode();
                    parentNameA0 = parentA0.getNodeName();
                }
                                    
                for (int k = 0; k < parentA0.getAttributes().getLength(); k++) {
                    idStringA0 = parentA0.getAttributes().getNamedItem("xml:id").getNodeValue();
                }

                Node parentA1 = a1.getParentNode();
                String idStringA1 = "";
                String parentNameA1 = parentA1.getNodeName();

                while (parentNameA1 != "seg") {
                    parentA1 = parentA1.getParentNode();
                    parentNameA1 = parentA1.getNodeName();
                }
 
                for (int k = 0; k < parentA1.getAttributes().getLength(); k++) {
                    idStringA1 = parentA1.getAttributes().getNamedItem("xml:id").getNodeValue();
                }

                Node parentB0;
                String idStringB0 = "";
                String parentNameB0;
                if (b0 != null) {
                    parentB0 = b0.getParentNode();
                    parentNameB0 = parentB0.getNodeName();
                    while (parentNameB0 != "seg") {
                        parentB0 = parentB0.getParentNode();
                        parentNameB0 = parentB0.getNodeName();
                    }                                    
                    for (int k = 0; k < parentB0.getAttributes().getLength(); k++) {
                        idStringB0 = parentB0.getAttributes().getNamedItem("xml:id").getNodeValue();
                    }
                }

                Node parentB1;
                String idStringB1 = "";
                String parentNameB1;
                if (b1 != null) {
                    parentB1 = b1.getParentNode();
                    parentNameB1 = parentB1.getNodeName();
                    while (parentNameB1 != "seg") {
                        parentB1 = parentB1.getParentNode();
                        parentNameB1 = parentB1.getNodeName();
                    }
                    for (int k = 0; k < parentB1.getAttributes().getLength(); k++) {
                        idStringB1 = parentB1.getAttributes().getNamedItem("xml:id").getNodeValue();
                    }
                }

                if (idStringA0.equals(idStringB0) && idStringA1.equals(idStringB1)) {
                    Node nextSibling = a1.getNextSibling();

                    StringBuilder overlappingWords = new StringBuilder();
                    boolean firstIteration = true;

                    while (nextSibling != null) {
                        if (!nextSibling.getNodeName().equals("anchor")) {
                            if (nextSibling.getNodeType() == Node.ELEMENT_NODE && nextSibling.getNodeName().equals("w")) {
                                boolean filteredNGIRRs = nextSibling.getTextContent().matches("(?i:\\b(hm|hm_hm)\\b)");
                                if (!filteredNGIRRs) {
                                    if (firstIteration) {
                                        nrOfOverlaps++;
                                        firstIteration = false;
                                    }
                                    NodeList wChildren = nextSibling.getChildNodes();
                                    for (int m = 0; m < wChildren.getLength(); m++) {
                                        Node n = wChildren.item(m);
                                        if (n.getNodeName().equals("anchor")) {
                                            nextSibling = null;
                                            break;
                                        } else {
                                            nrOfOverlappingWords++;
                                            overlappingWords.append(nextSibling.getTextContent());
                                            overlappingWords.append(" ");
                                        }
                                    }
                                } else {
                                }
                            }
                        } else {
                            nextSibling = null;
                        }
                        nextSibling = nextSibling != null ? nextSibling.getNextSibling() : null;
                    }

                    if (nrOfOverlappingWords > 2) {
                        nrOfOverlapsWithMoreThan2Words++;
                    }
                }

                nrOfOverlapsWithMoreThan2WordsInPart += nrOfOverlapsWithMoreThan2Words;
            }                            
        }
                        
        double perMilTokensOverlapsWithMoreThan2WordsSpeechEvent = (double) nrOfOverlapsWithMoreThan2WordsInPart / (double) numberOfTokens * 1000;
        return perMilTokensOverlapsWithMoreThan2WordsSpeechEvent;
    }

}
