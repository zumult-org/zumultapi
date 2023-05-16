/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.measures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import static jdk.nashorn.internal.objects.NativeMath.round;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class Measure_8 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_8().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    String[] corpusIDs = {"FOLK", "GWSS"};
    String data_path = "src\\main\\java\\data\\measures\\";

    public void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        // Connect to DGD
        BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
        for (String corpusID : corpusIDs){
            try {

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                // root elements
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("measures-document");
                doc.appendChild(rootElement);


                /*Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
                IDList speechEventIDs = new IDList("speechEvents");
                speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));*/

                IDList speechEventIDs = backendInterface.getSpeechEvents4Corpus(corpusID);

                for (String speechEventID : speechEventIDs) {
    //                if (speechEventID.equals("FOLK_E_00001_SE_01")) {
    //                if (speechEventID.equals("GWSS_E_00001_SE_01")) {

    //                    String speechEventID = "FOLK_E_00001_SE_01";
                        System.out.println("   " + speechEventID);

                        // measures element
                        Element measures = doc.createElement("measures");
                        measures.setAttribute("speechEventID", speechEventID);
                        rootElement.appendChild(measures);

                        IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);

                        int nrOfTokensSpeechEvent = 0;
                        int nrOfOverlapsSpeechEvent = 0;
                        int nrOfOverlappingWordsSpeechEvent = 0;
                        int nrOfOverlapsWithMoreThan2WordsSpeechEvent = 0;


                        for (String transcriptID : transcriptIDs) {
                            Transcript transcript = backendInterface.getTranscript(transcriptID);
                            int nrOfTokens = transcript.getNumberOfTokens();
                            nrOfTokensSpeechEvent += nrOfTokens;

                            NodeList anchors = transcript.getXmlDocument().getElementsByTagName("anchor");

                            List<String> synchs = new ArrayList();
                            List<String> uniqueSynchs = new ArrayList();
                            List<String> doubleSynchs = new ArrayList();

                            for (int i = 0; i < anchors.getLength(); i++) {
                                Node anchor = anchors.item(i);
                                synchs.add(anchor.getAttributes().item(0).getTextContent());
                            }

                            for (String synch : synchs) {
                                if (!uniqueSynchs.contains(synch)) {
                                    uniqueSynchs.add(synch);
                                } else {
                                    doubleSynchs.add(synch);
                                }
                            }

                            for (int i = 0; i < doubleSynchs.size()-1; i++) {
                                String doubleSynch0 = doubleSynchs.get(i);
                                String doubleSynch1 = doubleSynchs.get(i+1) != null ? doubleSynchs.get(i+1) : null;

    //                            System.out.println(doubleSynch0 + " " + transcript.getAnchorsByAttribute(doubleSynch0).getLength());
                                // get the first pair of double anchors
                                NodeList bundle = transcript.getAnchorsByAttribute(doubleSynch0);

    //                            System.out.println(doubleSynch1 + " " + transcript.getAnchorsByAttribute(doubleSynch1).getLength());
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


    /////////////////////////////// A0
                                    Node parentA0 = a0.getParentNode();
                                    String idStringA0 = "";
                                    String parentNameA0 = parentA0.getNodeName();
    //                                System.out.println("a0) parent name before while: " + parentNameA0);

                                    while (parentNameA0 != "seg") {
    //                                    System.out.println("a0) not seg");
                                        parentA0 = parentA0.getParentNode();
                                        parentNameA0 = parentA0.getNodeName();
                                    }
                                    for (int k = 0; k < parentA0.getAttributes().getLength(); k++) {
                                        idStringA0 = parentA0.getAttributes().getNamedItem("xml:id").getNodeValue();
                                    }

    //                                System.out.println("a0) parent name after while: " + parentNameA0);
    //                                System.out.println("a0) parent id: " + idStringA0);

    /////////////////////////////// A1
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

    /////////////////////////////// B0
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

    /////////////////////////////// B1
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

    /////////////////////////////// check if they overlap
                                    if (idStringA0.equals(idStringB0) && idStringA1.equals(idStringB1)) {
    //                                    System.out.println("*******************\nOVERLAP");
                                        Node nextSibling = a1.getNextSibling();

                                        StringBuilder overlappingWords = new StringBuilder();
                                        boolean firstIteration = true;

                                        while (nextSibling != null) {
    //                                        System.out.println("HAS SIBLING");
                                            if (!nextSibling.getNodeName().equals("anchor")) {
                                                if (nextSibling.getNodeType() == Node.ELEMENT_NODE && nextSibling.getNodeName().equals("w")) {
                                                    //System.out.println("TEXT CONTENT: " + nextSibling.getTextContent());
                                                    boolean filteredNGIRRs = nextSibling.getTextContent().matches("(?i:\\b(hm|hm_hm)\\b)");
                                                    //System.out.println("MHMHMHMHMHMHMHMMHMH");

                                                    if (!filteredNGIRRs) {
                                                        if (firstIteration) {
                                                            nrOfOverlaps++;
                                                            firstIteration = false;
                                                        }

                                                        NodeList wChildren = nextSibling.getChildNodes();
                                                        for (int m = 0; m < wChildren.getLength(); m++) {
                                                            Node n = wChildren.item(m);
    //                                                    System.out.println(m + " NODE NAME: " + n.getNodeName());
                                                            if (n.getNodeName().equals("anchor")) {
    //                                                        System.out.println("IS ANCHOR YOOOO");
                                                                nextSibling = null;
                                                                break;
                                                            } else {
    //                                                        System.out.println("TEXT CONTENT: " + nextSibling.getTextContent());
                                                                nrOfOverlappingWords++;

                                                                overlappingWords.append(nextSibling.getTextContent());
                                                                overlappingWords.append(" ");
                                                            }
                                                        }
                                                    } else {
                                                        //System.out.println("FILTEREDDDD");
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

    //                                    System.out.println("NR OF OVERLAPS: " + nrOfOverlaps);
    //                                    System.out.println("NR OF OVERLAPPING WORDS: " + nrOfOverlappingWords);
    //                                    System.out.println("NR OF OVERLAPS WITH MORE THAN 2 WORDS: " + nrOfOverlapsWithMoreThan2Words);
    //                                    System.out.println("OVERLAPPING WORDS: " + overlappingWords.toString());
    //                                    System.out.println("*******************");
                                    }
                                    nrOfOverlappingWordsSpeechEvent += nrOfOverlappingWords;
                                    nrOfOverlapsWithMoreThan2WordsSpeechEvent += nrOfOverlapsWithMoreThan2Words;
                                    nrOfOverlapsSpeechEvent += nrOfOverlaps;

    //                                System.out.println("NR OF OVERLAPPING WORDS PER SPEECH EVENT: " + nrOfOverlappingWordsSpeechEvent);
    //                                System.out.println("NR OF OVERLAPS WITH MORE THAN 2 WORDS SPEECH EVENT: " + nrOfOverlapsWithMoreThan2WordsSpeechEvent);
    //                                System.out.println("NR OVERLAPS SPEECH EVENT: " + nrOfOverlapsSpeechEvent);
    //                                System.out.println("");

                                }                            
                            }
                        }
                        double averageNrOverlappingWords = (double) nrOfOverlappingWordsSpeechEvent / (double) nrOfOverlapsSpeechEvent;
                        double perMilOverlaps = (double) nrOfOverlapsSpeechEvent / (double) nrOfTokensSpeechEvent * 1000;
                        double perCentOverlapsWithMoreThan2WordsSpeechEvent = (double) nrOfOverlapsWithMoreThan2WordsSpeechEvent / (double) nrOfOverlapsSpeechEvent * 100;
                        double perMilTokensOverlapsWithMoreThan2WordsSpeechEvent = (double) nrOfOverlapsWithMoreThan2WordsSpeechEvent / (double) nrOfTokensSpeechEvent * 1000;



                        //System.out.println("averageNrOverlappingWords: " + averageNrOverlappingWords);
                        //System.out.println("perMilOverlaps: " + perMilOverlaps);
                        //System.out.println("perCentOverlapsWithMoreThan2WordsSpeechEvent: " + perCentOverlapsWithMoreThan2WordsSpeechEvent);
                        //System.out.println("perMilTokensOverlapsWithMoreThan2WordsSpeechEvent: " + perMilTokensOverlapsWithMoreThan2WordsSpeechEvent);
    //
                        Element measure1 = doc.createElement("measure");
                        measure1.setAttribute("perMilOverlaps", String.format("%.2f", perMilOverlaps));
                        Element measure2 = doc.createElement("measure");
                        measure2.setAttribute("averageNrOverlappingWords", String.format("%.2f", averageNrOverlappingWords));
                        Element measure3 = doc.createElement("measure");
                        measure3.setAttribute("perCentOverlapsWithMoreThan2Words", String.format("%.2f", perCentOverlapsWithMoreThan2WordsSpeechEvent));
                        Element measure4 = doc.createElement("measure");
                        measure4.setAttribute("perMilTokensOverlapsWithMoreThan2Words", String.format("%.2f", perMilTokensOverlapsWithMoreThan2WordsSpeechEvent));
    //
                        measures.appendChild(measure1);
                        measures.appendChild(measure2);
                        measures.appendChild(measure3);
                        measures.appendChild(measure4);

                        //System.out.println("\n" + IOUtilities.documentToString(doc));
    //                } // if

                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(doc);
                Result xmlResult = new StreamResult(new File(data_path + "Measure_8_" + corpusID + ".xml"));

                transformer.transform(domSource, xmlResult);


                /*File xslFilename = new File("src\\java\\org\\zumult\\io\\measure8_xml_to_txt.xsl");
                File xmlFilename = new File(data_path + "Measure_8_" + corpusID + ".xml");

                //System.out.println("exists?: " + xmlFilename.exists());
                TransformerFactory factory = TransformerFactory.newInstance();
                Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
                Transformer xformer = template.newTransformer();
                Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_8_" + corpusID + ".xml")));
                Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_8_" + corpusID + ".txt")));
                // Apply the xsl file to the source file and write the result to the output file
                xformer.transform(xmlSource, txtResult);

                //System.out.println("File saved!");*/
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(Measure_8.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(Measure_8.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

