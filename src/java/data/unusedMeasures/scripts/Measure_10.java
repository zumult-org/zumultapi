/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

///////////////////////////////////////////////////////////////////////////////////////////////
// THIS SCRIPT IS NOW OBSOLETE, AS THE CORRECT DATA FOR MEASURE 10 IS CALCULATED IN MEASURE 10-2
// I MOVED IT THERE AS I WAS REVISING, BECAUSED IT MADE THINGS SIMPLER AND MADE MORE SENSE
// I DID NOT CHANGE THE NAMES OFD THE FILES. I WILL CLEAN EVERYTHING UP ONCE ALL THE MEASURES
// ARE COMPLETED, CHECKED, AND APPROVED
///////////////////////////////////////////////////////////////////////////////////////////////

package data.unusedMeasures.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import static jdk.nashorn.internal.objects.NativeMath.round;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class Measure_10 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_10().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_10.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        try {
            String corpusID = "FOLK";
//            String corpusID = "GWSS";
            
            String IDLists_path = "src\\java\\IDLists\\";
            String data_path = "src\\java\\data\\";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("measures-document");
            doc.appendChild(rootElement);

            // Connect to DGD
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            
            Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
            IDList speechEventIDs = new IDList("speechEvents");
            speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));

            for (String speechEventID : speechEventIDs) {
//                if (speechEventID.equals("FOLK_E_00021_SE_01")) {
//                if (speechEventID.equals("GWSS_E_00001_SE_01")) {
                    
//                    String speechEventID = "FOLK_E_00001_SE_01";
                    System.out.println(speechEventID);

                    // measures element
                    Element measures = doc.createElement("measures");
                    measures.setAttribute("speechEventID", speechEventID);
                    rootElement.appendChild(measures);

                    IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                    
                    Map<String, Map<String, Object>> finalMap = new TreeMap<String, Map<String, Object>>();
                    
                    int nrOfContributionsSpeechEvent = 0; // yes
                    int nrOfWordsInContributionsSpeechEvent = 0; // yes
//                    int nrOfContributionsWith1to2WordsSpeechEvent = 0; // yes

                    for (String transcriptID : transcriptIDs) {
                        System.out.println(transcriptID);
                        Transcript transcript = backendInterface.getTranscript(transcriptID);                        

//                        NodeList annotationBlock = transcript.getXmlDocument().getElementsByTagName("annotationBlock");

                        NodeList persons = transcript.getXmlDocument().getElementsByTagName("person");
                        Map speakersMap = new TreeMap();
                        for (int n = 0; n < persons.getLength(); n++) {
                            String initials = persons.item(n).getAttributes().getNamedItem("xml:id").getNodeValue();
                            String agdID = null;
                            if (persons.item(n).getFirstChild().getNodeName().equals("idno")) {
                                agdID = persons.item(n).getFirstChild().getTextContent();
                            }
                            System.out.println("initials: " + initials);
                                int speakerContributions = 0;
                                int speakerSpokenWords = 0;
                                int speakerContributions1To2Words = 0;
                                
                                Map<String, Object> initialsMap = new TreeMap<String, Object>();
                                initialsMap.put("speakerContributions", 0);
                                initialsMap.put("speakerSpokenWords", 0);
                                initialsMap.put("speakerContributions1To2Words", 0);
                                
                                if (agdID != null) {
                                    speakersMap.put(agdID, initials);
                                    initialsMap.put("AGD-ID", agdID);
                                    
                                    if (!finalMap.containsKey(initials)) {
                                        finalMap.put(initials, initialsMap);
                                    }
                                }
                                
                                NodeList speakerAnnotationBlocks = transcript.getAnnotationBlocksBySpeaker(initials);

        //                        // ITERATE THROUGH ALL THE ANNOTATION BLOCKS OF SPEAKER
                                for (int i = 0; i < speakerAnnotationBlocks.getLength(); i++) {
        //                            System.out.println(IOUtilities.elementToString(measures));

                                    NodeList contributionChildren = speakerAnnotationBlocks.item(i).getFirstChild().getFirstChild().getChildNodes();
                                    int nrOfWordsInContribution = 0;
                                    boolean firstWord = true;
//                                    System.out.println("seg id: " + annotationBlock.item(i).getAttributes().item(1));
                                    
                                    for (int jj = 0; jj < contributionChildren.getLength(); jj++) {
                                        String lala = contributionChildren.item(jj).getTextContent();
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(lala);
                                        sb.append(" ");
                                        System.out.println("BUILT STRING: " + sb);
                                    }


        //                            // ITERATE THROUGH THE CONTENTS OF THE CONTRIBUTION
                                    for (int j = 0; j < contributionChildren.getLength(); j++) {
                                        String contributionChildName = contributionChildren.item(j) != null ? contributionChildren.item(j).getNodeName() : "NUUULLLL";
                                        System.out.println("contributionChildName: " + contributionChildName);
                                        System.out.println("current initials: " + initials);

                                        if (contributionChildName.equals("w")) {
                                            System.out.println("IS A WORD: " + contributionChildren.item(j).getTextContent());
                                            System.out.println("ID: " + contributionChildren.item(j).getAttributes().getNamedItem("xml:id").getNodeValue());
                                            nrOfWordsInContribution++;
                                            System.out.println("nrOfWordsInContribution: " + nrOfWordsInContribution);
                                            if (firstWord) {
                                                nrOfContributionsSpeechEvent++;
                                                
                                                speakerContributions++;
                                                firstWord = false;   
                                            }
                                        } else {
                                            System.out.println("NOT A WORD");
                                            System.out.println("nrOfWordsInContribution: " + nrOfWordsInContribution);
                                        }
                                        System.out.println("-------------------");
                                    }
                                    if (nrOfWordsInContribution > 0 && nrOfWordsInContribution < 3) {
//                                        nrOfContributionsWith1to2WordsSpeechEvent++;
                                        speakerContributions1To2Words++;
                                    }
                                    nrOfWordsInContributionsSpeechEvent += nrOfWordsInContribution;   
                                    speakerSpokenWords += nrOfWordsInContribution;
                                    
                                }
                                
                                if (agdID != null) {
//                                    System.out.println("LALALA: " + finalMap.get(initials));
                                    finalMap.get(initials).put("speakerContributions", (Integer) finalMap.get(initials).get("speakerContributions") + speakerContributions);
                                    finalMap.get(initials).put("speakerSpokenWords", (Integer) finalMap.get(initials).get("speakerSpokenWords") + speakerSpokenWords);
                                    finalMap.get(initials).put("speakerContributions1To2Words", (Integer) finalMap.get(initials).get("speakerContributions1To2Words") + speakerContributions1To2Words);
//                                System.out.println("speakersMap: " + speakersMap);
//                                System.out.println("finalMapTranscript: " + finalMapTranscript);
                                }
                                
                                
//                            } // if archived
                        } // for person

                        System.out.println("finalMap: " + finalMap);
                        
                    } // for transcript
                    
//                    System.out.println("finalMap: " + finalMap);
                    
                    for (Map.Entry<String, Map<String, Object>> entry : finalMap.entrySet()) {
                        
                        String speakerWho = entry.getKey();
                        String speakerAGDId = (String)entry.getValue().get("AGD-ID");
                        
                        int speakerContributions = (Integer)entry.getValue().get("speakerContributions");
                        int speakerSpokenWords = (Integer)entry.getValue().get("speakerSpokenWords");
                        int speakerContributions1To2Words = (Integer)entry.getValue().get("speakerContributions1To2Words");
                        
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
                        speaker.appendChild(measure1);
                        speaker.appendChild(measure2);
                        speaker.appendChild(measure3);
                        
                        measures.appendChild(speaker);
                    }//                    
                    System.out.println("\n" + IOUtilities.documentToString(doc));
//                } // if
                
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_10_" + corpusID + ".xml"));

            transformer.transform(domSource, xmlResult);
            
            
            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure10_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_10_" + corpusID + ".xml");
            
            System.out.println("exists?: " + xmlFilename.exists());
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_10_" + corpusID + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_10_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Measure_10.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Measure_10.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

