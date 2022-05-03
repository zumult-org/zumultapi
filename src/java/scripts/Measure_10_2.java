/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import static jdk.nashorn.internal.objects.NativeMath.round;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.Speaker;
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class Measure_10_2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_10_2().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        try {
            String corpusID = "FOLK";
//            String corpusID = "GWSS";
            
            String data_path = "src\\java\\data\\";
            String IDLists_path = data_path + "IDLists\\";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // root elements for measure 10
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("measures-document");
            doc.appendChild(rootElement);

            // measure 9
            Document doc9 = docBuilder.newDocument();
            Element rootElement9 = doc9.createElement("measures-document");
            doc9.appendChild(rootElement9);

            // Connect to DGD
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            
            Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
            IDList speechEventIDs = new IDList("speechEvents");
            speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));

            for (String speechEventID : speechEventIDs) {
//                if (speechEventID.equals("FOLK_E_00001_SE_01")) {
//                if (speechEventID.equals("GWSS_E_00001_SE_01")) {
                    
//                    String speechEventID = "FOLK_E_00303_SE_01";
//                    String speechEventID = "GWSS_E_00224_SE_01";
                    System.out.println(speechEventID);

                    // measures element
                    Element measures = doc.createElement("measures");
                    measures.setAttribute("speechEventID", speechEventID);
                    rootElement.appendChild(measures);
                    
                    Element measures9 = doc9.createElement("measures");
                    measures9.setAttribute("speechEventID", speechEventID);
                    rootElement9.appendChild(measures9);

                    IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                    
                    Map<String, Map<String, Object>> finalMap = new TreeMap<String, Map<String, Object>>();
                    
                    long durationSpeechEvent = 0;
                    
                    int nrOfContributionsSpeechEvent = 0; // yes
                    int nrOfWordsInContributionsSpeechEvent = 0; // yes
                    int nrOfContributionsWith1to2WordsSpeechEvent = 0; // yes
                    Map speakersMap = new TreeMap();

                    for (String transcriptID : transcriptIDs) {
                        System.out.println(transcriptID);
                        Transcript transcript = backendInterface.getTranscript(transcriptID);
                        
                        double endTime = (long)transcript.getEndTime();
                        double startTime = (long)transcript.getStartTime();

                        durationSpeechEvent += (endTime - startTime) * 1000;

                        NodeList persons = transcript.getXmlDocument().getElementsByTagName("person");
                                                        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$");

                        System.out.println("persons length" + persons.getLength());
                        for (int n = 0; n < persons.getLength(); n++) {
                            String initials = persons.item(n).getAttributes().getNamedItem("xml:id").getNodeValue();
                            String agdID = null;
                            String occupation = null;
                            System.out.println("BLABLABLABLAB");
                            if (getIdNoNode(persons.item(n)) != null) {
                                agdID = getIdNoNode(persons.item(n)).getTextContent();
                                Speaker thisSpeaker = backendInterface.getSpeaker(agdID);
                                occupation = thisSpeaker.getOccupation(agdID);
                                System.out.println("OCCUPATION: " + occupation);
                            }
                            System.out.println("initials: " + initials);
                            int speakerContributions = 0;
                            int speakerSpokenWords = 0;
                            int speakerContributions1To2Words = 0;

                            Map<String, Object> initialsMap = new TreeMap<String, Object>();
                            initialsMap.put("speakerContributions", 0);
                            initialsMap.put("speakerSpokenWords", 0);
                            initialsMap.put("speakerContributions1To2Words", 0);
                            initialsMap.put("occupation", "");

                            if (agdID != null) {
                                System.out.println("AGDID NOT NULL");
                                speakersMap.put(agdID, initials);
                                initialsMap.put("AGD-ID", agdID);

                                if (!finalMap.containsKey(initials)) {
                                    finalMap.put(initials, initialsMap);
                                }
                            }

                            NodeList speakerAnnotationBlocks = transcript.getAnnotationBlocksBySpeaker(initials);

    //                        // ITERATE THROUGH ALL THE CONTRIBUTIONS
                            for (int i = 0; i < speakerAnnotationBlocks.getLength(); i++) {
                                Node item = speakerAnnotationBlocks.item(i);
                                
                                Node nextItem = speakerAnnotationBlocks.item(i+1) != null
                                    ? speakerAnnotationBlocks.item(i+1)
                                    : null;
                                
                                // this
                                String nextItemId = nextItem != null ? nextItem.getAttributes().getNamedItem("xml:id").getNodeValue() : null;
                                
                                System.out.println("INITIAL item: " + item.getAttributes().getNamedItem("xml:id").getNodeValue());
                                System.out.println("INITIAL nextItemId: " + nextItemId);
                                
                                Node currentElement = (Node)transcript.getElementById(item.getAttributes().getNamedItem("xml:id").getNodeValue());
                                System.out.println("INITIAL currentElement content: " + currentElement.getTextContent());
                                
                                Node nextAnnotationBlock = currentElement.getNextSibling();

                                // find next annotationBlock element
                                while (nextAnnotationBlock != null && !nextAnnotationBlock.getNodeName().equals("annotationBlock")) {
                                    nextAnnotationBlock = nextAnnotationBlock.getNextSibling();
                                }
                                // this
                                String nextAnnotationBlockId = nextAnnotationBlock != null ? nextAnnotationBlock.getAttributes().getNamedItem("xml:id").getNodeValue() : null;
                                System.out.println("INITIAL nextAnnotationBlockID: " + nextAnnotationBlockId);
                                
                                String who = item.getAttributes().getNamedItem("who").getNodeValue();

                                boolean continuedContribution = nextItemId != null && nextAnnotationBlockId != null && nextItemId.equals(nextAnnotationBlockId) ? true : false;

                                System.out.println("continuedContribution: " + continuedContribution);
                                System.out.println("contribution: " + getContributionOfAnnotationBlock(speakerAnnotationBlocks.item(i)));
                                
                                List<Node> contributionChildren = new ArrayList<>();
                                NodeList contributionChildrenNodes = getContributionOfAnnotationBlock(speakerAnnotationBlocks.item(i)).getChildNodes() != null
                                        ? getContributionOfAnnotationBlock(speakerAnnotationBlocks.item(i)).getChildNodes()
                                        : null;
                                for (int k = 0; k < contributionChildrenNodes.getLength(); k++) {
                                    contributionChildren.add(contributionChildrenNodes.item(k));
                                }

                                // rewrite the whole thing to make snese
                                int nextCount = 2;
                                while (continuedContribution && speakerAnnotationBlocks.item(i + nextCount) != null) {
                                    System.out.println("i: " + i);
                                    System.out.println("nextCount: " + nextCount);
                                    int newCount = i + nextCount;
                                    System.out.println("i after: " + i);
                                    System.out.println("newCount: " + newCount);
                                    nextItem = speakerAnnotationBlocks.item(newCount) != null
                                            ? speakerAnnotationBlocks.item(newCount)
                                            : null;

                                    NodeList nextContributionChildrenNodes = null;
                                    Node qwerty = null;
                                    if (nextItem != null) {
                                        System.out.println("YELLOOO");
                                        nextContributionChildrenNodes = getContributionOfAnnotationBlock(nextItem).getChildNodes();
                                    }

                                    if (nextContributionChildrenNodes != null) {
                                        System.out.println("GGJJJGJGJGJGJGGJ");
                                        nextContributionChildrenNodes = getContributionOfAnnotationBlock(nextItem).getChildNodes();
                                    }

                                    for (int o = 0; o < nextContributionChildrenNodes.getLength(); o++) {
                                        contributionChildren.add(nextContributionChildrenNodes.item(o));
                                    }
                                    
                                    nextItemId = nextItem != null ? nextItem.getAttributes().getNamedItem("xml:id").getNodeValue() : null;
                                    System.out.println("nextItemId: " + nextItemId);

                                    nextAnnotationBlock = nextAnnotationBlock.getNextSibling();

                                    System.out.println("nextAnnotationBlock: " + nextAnnotationBlock.getTextContent());
                                    // find next annotationBlock element
                                    while (nextAnnotationBlock != null && !nextAnnotationBlock.getNodeName().equals("annotationBlock")) {
                                        nextAnnotationBlock = nextAnnotationBlock.getNextSibling();
                                    }
                                    
                                    // this
                                    nextAnnotationBlockId = nextAnnotationBlock != null ? nextAnnotationBlock.getAttributes().getNamedItem("xml:id").getNodeValue() : null;
                                    System.out.println("nextAnnotationBlockID: " + nextAnnotationBlockId);

                                    continuedContribution = nextItemId != null && nextAnnotationBlockId != null && nextItemId.equals(nextAnnotationBlockId) ? true : false;
                                    System.out.println("continuedContribution: " + continuedContribution);
                                    if (!continuedContribution) i = newCount - 1;
                                    nextCount++;
                                }
                                System.out.println("----------");


                                int nrOfWordsInContribution = 0;
                                boolean firstWord = true;
//                                    System.out.println("seg id: " + annotationBlock.item(i).getAttributes().item(1));

    //                            // ITERATE THROUGH THE CONTENTS OF THE CONTRIBUTION
                                for (int j = 0; j < contributionChildren.size(); j++) {
                                    String contributionChildName = contributionChildren.get(j) != null ? contributionChildren.get(j).getNodeName() : "NUUULLLL";

                                    if (contributionChildName.equals("w")) {
                                        nrOfWordsInContribution++;
                                        if (firstWord) {
                                            nrOfContributionsSpeechEvent++;

                                            speakerContributions++;
                                            firstWord = false;   
                                        }
                                    }
                                }
                                if (nrOfWordsInContribution > 0 && nrOfWordsInContribution < 3) {
//                                    nrOfContributionsWith1to2WordsSpeechEvent++;
                                    speakerContributions1To2Words++;
                                }
                                nrOfWordsInContributionsSpeechEvent += nrOfWordsInContribution;   
                                speakerSpokenWords += nrOfWordsInContribution;

                            }
                            nrOfContributionsWith1to2WordsSpeechEvent += speakerContributions1To2Words;
                            
                            if (agdID != null) {
                                finalMap.get(initials).put("speakerContributions", (Integer) finalMap.get(initials).get("speakerContributions") + speakerContributions);
                                finalMap.get(initials).put("speakerSpokenWords", (Integer) finalMap.get(initials).get("speakerSpokenWords") + speakerSpokenWords);
                                finalMap.get(initials).put("speakerContributions1To2Words", (Integer) finalMap.get(initials).get("speakerContributions1To2Words") + speakerContributions1To2Words);
                                finalMap.get(initials).put("occupation", occupation);
                            }


//                            } // if archived
                        } // for person

                        System.out.println("nrOfContributionsWith1to2WordsSpeechEvent: " + nrOfContributionsWith1to2WordsSpeechEvent);
                        System.out.println("finalMap: " + finalMap);
                        
                    } // for transcript
                    
                    List<Integer> speakerSpokenWordsArray = new ArrayList<Integer>();
                    
                    System.out.println("finalMap size: " + finalMap.size());
                    // MEASURE 10
                    for (Map.Entry<String, Map<String, Object>> entry : finalMap.entrySet()) {
                        String speakerWho = entry.getKey();
                        String speakerAGDId = (String)entry.getValue().get("AGD-ID");
                        String speakerOccupation = (String)entry.getValue().get("occupation");
                                                
                        int speakerContributions = (Integer)entry.getValue().get("speakerContributions");
                        int speakerSpokenWords = (Integer)entry.getValue().get("speakerSpokenWords");
                        int speakerContributions1To2Words = (Integer)entry.getValue().get("speakerContributions1To2Words");
                        
                        speakerSpokenWordsArray.add(speakerSpokenWords);
                        
                        double perCentSpeakerContributionsProTotalContributions = (double) speakerContributions / (double) nrOfContributionsSpeechEvent * 100;
                        double perCentSpeakerSpokenWordsProTotalWords = (double) speakerSpokenWords / (double) nrOfWordsInContributionsSpeechEvent * 100;
                        double perCentContributions1To2WordsProSpeakerContributionSize = (double) speakerContributions1To2Words / (double) speakerContributions * 100;
                        
                        
                                                
                        Element speaker = doc.createElement("speaker");
                        Element measure1 = doc.createElement("measure");
                        Element measure2 = doc.createElement("measure"); 
                        Element measure3 = doc.createElement("measure");
                        
                        measure1.setAttribute("perCentSpeakerContributionsProTotalContributions", String.format("%.2f", perCentSpeakerContributionsProTotalContributions));
                        measure2.setAttribute("perCentSpeakerSpokenWordsProTotalWords", String.format("%.2f", perCentSpeakerSpokenWordsProTotalWords));
                        measure3.setAttribute("perCentContributions1To2WordsProSpeakerContributionSize", String.format("%.2f", perCentContributions1To2WordsProSpeakerContributionSize));

                        speaker.setAttribute("AGD-ID", speakerAGDId);
                        speaker.setAttribute("who", speakerWho);
                        speaker.setAttribute("speakerOccupation", speakerOccupation);
                        speaker.appendChild(measure1);
                        speaker.appendChild(measure2);
                        speaker.appendChild(measure3);
                        
                        measures.appendChild(speaker);
                    }
                    
                    Collections.sort(speakerSpokenWordsArray);
                    Collections.reverse(speakerSpokenWordsArray);

                    double highestPortionSpokenWords = speakerSpokenWordsArray.size() > 0
                            ? (double) speakerSpokenWordsArray.get(0) / (double) nrOfWordsInContributionsSpeechEvent * 100
                            : 0.0;
                    double secondHighestPortionSpokenWords = speakerSpokenWordsArray.size() > 1
                            ? (double) speakerSpokenWordsArray.get(1) / (double) nrOfWordsInContributionsSpeechEvent * 100
                            : 0.0;

                    measures.setAttribute("totalSpeakers", Integer.toString(speakersMap.size()));
                    measures.setAttribute("highestPortionSpokenWords", String.format("%.2f", highestPortionSpokenWords));
                    measures.setAttribute("secondHighestPortionSpokenWords", String.format("%.2f", secondHighestPortionSpokenWords));
                    
                    System.out.println("durationSpeechEvent: " + durationSpeechEvent);

                    String durationFormatted =  String.format("%d:%02d:%02d",
                            (durationSpeechEvent / 3600000), // hours
                            (durationSpeechEvent / 60000) % 60, // mins
                            ((durationSpeechEvent / 1000) % 60)); // secs

                    measures.setAttribute("duration", durationFormatted);
                    
                    /////////////
                    // MEASURE 9
                    double totalMinutes = ((double)durationSpeechEvent / 60000);
                    System.out.println("total minutes: " + totalMinutes);
                    double nrOfContributionsPerMinute = (double) nrOfContributionsSpeechEvent / totalMinutes;
                    double averageNrOfWordsInContribution = (double) nrOfWordsInContributionsSpeechEvent / (double) nrOfContributionsSpeechEvent;
                    double perCentContributionsWith1To2Words = (double) nrOfContributionsWith1to2WordsSpeechEvent / (double) nrOfContributionsSpeechEvent * 100;

                    System.out.println("nrOfContributionsPerMinute: " + nrOfContributionsPerMinute);
                    System.out.println("averageNrOfWordsInContribution: " + averageNrOfWordsInContribution);
                    System.out.println("perCentContributionsWith1To2Words: " + perCentContributionsWith1To2Words);
//
                    Element measure91 = doc9.createElement("measure");
                    measure91.setAttribute("nrOfContributionsPerMinute", String.format("%.2f", nrOfContributionsPerMinute));
                    Element measure92 = doc9.createElement("measure");
                    measure92.setAttribute("averageNrOfWordsInContribution", String.format("%.2f", averageNrOfWordsInContribution));
                    Element measure93 = doc9.createElement("measure");
                    measure93.setAttribute("perCentContributionsWith1To2Words", String.format("%.2f", perCentContributionsWith1To2Words));
//
                    measures9.appendChild(measure91);
                    measures9.appendChild(measure92);
                    measures9.appendChild(measure93);
                    
                    
                    System.out.println("\n" + IOUtilities.documentToString(doc));
                    System.out.println("\n" + IOUtilities.documentToString(doc9));
//                } // if
                
            } // for speech event
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            TransformerFactory factory = TransformerFactory.newInstance();
            
            // measure 10
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_10_2_" + corpusID + ".xml"));

            transformer.transform(domSource, xmlResult);
            
            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure10_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_10_2_" + corpusID + ".xml");
            
            System.out.println("exists?: " + xmlFilename.exists());
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_10_2_" + corpusID + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_10_2_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
            
            // measure 9
            DOMSource domSource9 = new DOMSource(doc9);
            Result xmlResult9 = new StreamResult(new File(data_path + "Measure_9_" + corpusID + ".xml"));
            
            transformer.transform(domSource9, xmlResult9);

            File xslFilename9 = new File("src\\java\\org\\zumult\\io\\measure9_xml_to_txt.xsl");
            File xmlFilename9 = new File(data_path + "Measure_9_" + corpusID + ".xml");

            System.out.println("exists?: " + xmlFilename9.exists());
            Templates template9 = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename9)));
            Transformer xformer9 = template9.newTransformer();
            Source xmlSource9 = new StreamSource(new FileInputStream(new File(data_path + "Measure_9_" + corpusID + ".xml")));
            Result txtResult9 = new StreamResult(new FileOutputStream(new File(data_path + "Measure_9_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer9.transform(xmlSource9, txtResult9);

            System.out.println("File saved!");
            
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Measure_10_2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Measure_10_2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Node getContributionOfAnnotationBlock(Node annotationBlock) {
        Node contribution = null;
        for (int j = 0; j < annotationBlock.getChildNodes().getLength(); j++) {
            Node nod = annotationBlock.getChildNodes().item(j);
            if (nod.getNodeName() == "u") {
                for (int k = 0; k < nod.getChildNodes().getLength(); k++) {
                    Node nodd = nod.getChildNodes().item(k);
                    if (nodd.getNodeName() == "seg") {
                        contribution = nodd;
                        break;
                    }
                }
            }
        }
        return contribution;
    }
    public Node getIdNoNode(Node person) {
        Node idNo = null;
        for (int i = 0; i < person.getChildNodes().getLength(); i++) {
            Node node = person.getChildNodes().item(i);
            System.out.println("nodeName" + node.getNodeName());
            if (node.getNodeName() == "idno") {
                idNo = node;
                break;
            }
        }
        return idNo;
    }
}