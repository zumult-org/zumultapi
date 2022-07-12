/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

///////////////////////////////////////////////////////////////////////////////////////////////
// THIS SCRIPT IS NOW OBSOLETE, AS THE CORRECT DATA FOR MEASURE 9 IS CALCULATED IN MEASURE 10-2
// I MOVED IT THERE AS I WAS REVISING, BECAUSED IT MADE THINGS SIMPLER AND MADE MORE SENSE
// I DID NOT CHANGE THE NAMES OFD THE FILES. I WILL CLEAN EVERYTHING UP ONCE ALL THE MEASURES
// ARE COMPLETED, CHECKED, AND APPROVED
///////////////////////////////////////////////////////////////////////////////////////////////
package data.unusedMeasures.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
public class Measure_9 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_9().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_9.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
//        try {
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
//                if (speechEventID.equals("FOLK_E_00001_SE_01")) {
//                if (speechEventID.equals("GWSS_E_00001_SE_01")) {
                    
//                    String speechEventID = "FOLK_E_00001_SE_01";
                    System.out.println(speechEventID);

                    // measures element
                    Element measures = doc.createElement("measures");
                    measures.setAttribute("speechEventID", speechEventID);
                    rootElement.appendChild(measures);

                    IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);

//                    int nrOfTokensSpeechEvent = 0;
                    double durationSpeechEvent = 0;
                    int nrOfContributionsSpeechEvent = 0;
                    int nrOfWordsInContributionsSpeechEvent = 0;
                    int nrOfContributionsWith1to2WordsSpeechEvent = 0;

                    for (String transcriptID : transcriptIDs) {
                        Transcript transcript = backendInterface.getTranscript(transcriptID);
//                        int nrOfWordsInContributionsTranscript = 0;
//                        int nrOfContributionsWithMoreThan2Words = 0;
//                        int nrOfTokens = transcript.getNumberOfTokens();
//                        nrOfTokensSpeechEvent += nrOfTokens;
                        double durationTranscript = transcript.getEndTime();
                        durationSpeechEvent += durationTranscript;
                        System.out.println("durationTranscript: " + durationTranscript);
                        System.out.println("durationSpeechEvent: " + durationSpeechEvent);
                        
                        NodeList contributions = transcript.getXmlDocument().getElementsByTagName("seg");
                        
                        // ITERATE THROUGH ALL THE CONTRIBUTIONS
                        for (int i = 0; i < contributions.getLength(); i++) {
//                            Node contribution = contributions.item(i);
                            NodeList contributionChildren = contributions.item(i).getChildNodes();
                            int nrOfWordsInContribution = 0;
                            boolean firstWord = true;
                            System.out.println("seg id: " + contributions.item(i).getAttributes().item(1));

                            
                            // ITERATE THROUGH THE CONTENTS OF THE CONTRIBUTION
                            for (int j = 0; j < contributionChildren.getLength(); j++) {
                                String contributionChildName = contributionChildren.item(j) != null ? contributionChildren.item(j).getNodeName() : "NUUULLLL";
                                System.out.println("contributionChildName: " + contributionChildName);
                                
                                if (contributionChildName.equals("w")) {
                                    nrOfWordsInContribution++;
                                    if (firstWord) {
                                        nrOfContributionsSpeechEvent++;
                                        firstWord = false;   
                                    }
                                }
                            }
                            if (nrOfWordsInContribution > 0 && nrOfWordsInContribution < 3) {
                                nrOfContributionsWith1to2WordsSpeechEvent++;
                            }
                            nrOfWordsInContributionsSpeechEvent += nrOfWordsInContribution;   
                        }
                        
                        System.out.println("nrOfContributionsSpeechEvent: " + nrOfContributionsSpeechEvent);
                        System.out.println("nrOfWordsInContributionsSpeechEvent: " + nrOfWordsInContributionsSpeechEvent);
                        System.out.println("nrOfContributionsWith1to2WordsSpeechEvent: " + nrOfContributionsWith1to2WordsSpeechEvent);
                        
                    }
                    
                    double nrOfContributionsPerMinute = (double) nrOfContributionsSpeechEvent / (double) (durationSpeechEvent / 60);
                    double averageNrOfWordsInContribution = (double) nrOfWordsInContributionsSpeechEvent / (double) nrOfContributionsSpeechEvent;
                    double perCentContributionsWith1To2Words = (double) nrOfContributionsWith1to2WordsSpeechEvent / (double) nrOfContributionsSpeechEvent * 100;
                    
                    
                    System.out.println("nrOfContributionsPerMinute: " + nrOfContributionsPerMinute);
                    System.out.println("averageNrOfWordsInContribution: " + averageNrOfWordsInContribution);
                    System.out.println("perCentContributionsWith1To2Words: " + perCentContributionsWith1To2Words);
//
                    Element measure1 = doc.createElement("measure");
                    measure1.setAttribute("nrOfContributionsPerMinute", String.format("%.2f", nrOfContributionsPerMinute));
                    Element measure2 = doc.createElement("measure");
                    measure2.setAttribute("averageNrOfWordsInContribution", String.format("%.2f", averageNrOfWordsInContribution));
                    Element measure3 = doc.createElement("measure");
                    measure3.setAttribute("perCentContributionsWith1To2Words", String.format("%.2f", perCentContributionsWith1To2Words));
//
                    measures.appendChild(measure1);
                    measures.appendChild(measure2);
                    measures.appendChild(measure3);

                    System.out.println("\n" + IOUtilities.documentToString(doc));
//                } // if
                
            }
            
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            DOMSource domSource = new DOMSource(doc);
//            Result xmlResult = new StreamResult(new File(data_path + "Measure_9_" + corpusID + ".xml"));
//
//            transformer.transform(domSource, xmlResult);
//            
//            
//            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure9_xml_to_txt.xsl");
//            File xmlFilename = new File(data_path + "Measure_9_" + corpusID + ".xml");
//            
//            System.out.println("exists?: " + xmlFilename.exists());
//            TransformerFactory factory = TransformerFactory.newInstance();
//            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
//            Transformer xformer = template.newTransformer();
//            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_9_" + corpusID + ".xml")));
//            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_9_" + corpusID + ".txt")));
//            // Apply the xsl file to the source file and write the result to the output file
//            xformer.transform(xmlSource, txtResult);

//            System.out.println("File saved!");
//        } catch (TransformerConfigurationException ex) {
//            Logger.getLogger(Measure_9.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (TransformerException ex) {
//            Logger.getLogger(Measure_9.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
}

