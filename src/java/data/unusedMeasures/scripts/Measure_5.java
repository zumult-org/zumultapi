/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.unusedMeasures.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
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
//import static jdk.nashorn.internal.objects.NativeMath.round;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DefaultTokenList;
import org.zumult.objects.implementations.TokenListTokenFilter;

/**
 *
 * @author josip.batinic
 */
public class Measure_5 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_5().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        try {
            String corpusID = "FOLK";
//            String corpusID = "GWSS";
            String type = "norm";
            
            String data_path = "src\\java\\data\\";
            String IDLists_path = data_path + "IDLists\\";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("measures-document");
            doc.appendChild(rootElement);
            
            TokenList posNouns = new DefaultTokenList("pos");
            posNouns.put("NN", 0);
            
            TokenList posVerbs = new DefaultTokenList("pos");
            posVerbs.put("VAFIN", 0);
            posVerbs.put("VAIMP", 0);
            posVerbs.put("VAINF", 0);
            posVerbs.put("VAPP", 0);
            posVerbs.put("VMFIN", 0);
            posVerbs.put("VMINF", 0);
            posVerbs.put("VMPP", 0);
            posVerbs.put("VVFIN", 0);
            posVerbs.put("VVIMP", 0);
            posVerbs.put("VVINF", 0);
            posVerbs.put("VVIZU", 0);
            posVerbs.put("VVPP", 0);

//            final String[] endings = {"ung", "heit", "keit", "schaft", "tät", "ion", "ieren"};
            
            TokenFilter nounFilter = new TokenListTokenFilter(type, posNouns);
            TokenFilter verbFilter = new TokenListTokenFilter(type, posVerbs);

            // Connect to DGD
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
//            System.out.print("transcript\t\t\tglobal\tnouns\tadjectives\n");

            Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
            IDList speechEventIDs = new IDList("speechEvents");
            speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));

            for (String speechEventID : speechEventIDs) {
//                if (speechEventID.equals("FOLK_E_00074_SE_01")) {
//                    String speechEventID = "FOLK_E_00074_SE_01";
                    System.out.println(speechEventID);

                    // measures element
                    Element measures = doc.createElement("measures");
                    measures.setAttribute("speechEventID", speechEventID);
                    rootElement.appendChild(measures);

                    IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                    System.out.println("transcriptIDs: " + transcriptIDs);

                    int nrOfHitsSpeechEvent = 0;
                    int nrOfTokensSpeechEvent = 0;
                    int nrOfHitsUng = 0;
                    int nrOfHitsHeit = 0;
                    int nrOfHitsKeit = 0;
                    int nrOfHitsSchaft = 0;
                    int nrOfHitsTat = 0;
                    int nrOfHitsIon = 0;
                    int nrOfHitsIeren = 0;

                    for (String transcriptID : transcriptIDs) {
                        Transcript transcript = backendInterface.getTranscript(transcriptID);

                        TokenList nounList4Transcript = transcript.getTokenList(type, nounFilter);
                        TokenList verbList4Transcript = transcript.getTokenList(type, verbFilter);

                        int nrOfTokens = transcript.getNumberOfTokens();
                        System.out.println("nrOfTokens: " + nrOfTokens);
                        nrOfTokensSpeechEvent += nrOfTokens;

                        for (Map.Entry<String, Integer> entry : nounList4Transcript.entrySet()) {
                            String token = entry.getKey();
                            Integer occurances = entry.getValue();

                            if (token.endsWith("ung")) {
                                nrOfHitsUng += occurances;
                                nrOfHitsSpeechEvent += occurances;
                            } else if (token.endsWith("heit")) {
                                nrOfHitsHeit += occurances;
                                nrOfHitsSpeechEvent += occurances;
                            } else if (token.endsWith("keit")) {
                                nrOfHitsKeit += occurances;
                                nrOfHitsSpeechEvent += occurances;
                            } else if (token.endsWith("schaft")) {
                                nrOfHitsSchaft += occurances;
                                nrOfHitsSpeechEvent += occurances;
                            } else if (token.endsWith("tät")) {
                                nrOfHitsTat += occurances;
                                nrOfHitsSpeechEvent += occurances;
                            } else if (token.endsWith("ion")) {
                                nrOfHitsIon += occurances;
                                nrOfHitsSpeechEvent += occurances;
                            }
                        }
                        
                        for (Map.Entry<String, Integer> entry : verbList4Transcript.entrySet()) {
                            String token = entry.getKey();
                            Integer occurances = entry.getValue();

                            if (token.endsWith("ieren")) {
                                System.out.println("token: " + token);
                                nrOfHitsIeren += occurances;
                                nrOfHitsSpeechEvent += occurances;
                            }
                        }
                    }
                    
                    
                    System.out.println("nrOfTokensSpeechEvent: " + nrOfTokensSpeechEvent);
                    System.out.println("");
                    System.out.println("nrOfHitsSpeechEvent: " + nrOfHitsSpeechEvent);
                    System.out.println("nrOfHitsUng: " + nrOfHitsUng);
                    System.out.println("nrOfHitsHeit: " + nrOfHitsHeit);
                    System.out.println("nrOfHitsKeit: " + nrOfHitsKeit);
                    System.out.println("nrOfHitsSchaft: " + nrOfHitsSchaft);
                    System.out.println("nrOfHitsTat: " + nrOfHitsTat);
                    System.out.println("nrOfHitsIon: " + nrOfHitsIon);
                    System.out.println("nrOfHitsIeren: " + nrOfHitsIeren);

                    double perMilAll = (double) nrOfHitsSpeechEvent / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilUng = (double) nrOfHitsUng / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilHeit = (double) nrOfHitsHeit / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilKeit = (double) nrOfHitsKeit / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilSchaft = (double) nrOfHitsSchaft / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilTat = (double) nrOfHitsTat / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilIon = (double) nrOfHitsIon / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilIeren = (double) nrOfHitsIeren / (double) nrOfTokensSpeechEvent * 1000;

                    measures.setAttribute("perMilAll", String.format("%.2f", perMilAll));

                    Element measure1 = doc.createElement("measure");
                    measure1.setAttribute("perMilUng", String.format("%.2f", perMilUng));
                    Element measure2 = doc.createElement("measure");
                    measure2.setAttribute("perMilHeit", String.format("%.2f", perMilHeit));
                    Element measure3 = doc.createElement("measure");
                    measure3.setAttribute("perMilKeit", String.format("%.2f", perMilKeit));
                    Element measure4 = doc.createElement("measure");
                    measure4.setAttribute("perMilSchaft", String.format("%.2f", perMilSchaft));
                    Element measure5 = doc.createElement("measure");
                    measure5.setAttribute("perMilTat", String.format("%.2f", perMilTat));
                    Element measure6 = doc.createElement("measure");
                    measure6.setAttribute("perMilIon", String.format("%.2f", perMilIon));
                    Element measure7 = doc.createElement("measure");
                    measure7.setAttribute("perMilIeren", String.format("%.2f", perMilIeren));

                    measures.appendChild(measure1);
                    measures.appendChild(measure2);
                    measures.appendChild(measure3);
                    measures.appendChild(measure4);
                    measures.appendChild(measure5);
                    measures.appendChild(measure6);
                    measures.appendChild(measure7);

                    System.out.println("\n" + IOUtilities.documentToString(doc));
//                }
//                
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_5_" + corpusID + ".xml"));

            transformer.transform(domSource, xmlResult);
            
            
            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure5_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_5_" + corpusID + ".xml");
            
            System.out.println("exists?: " + xmlFilename.exists());
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_5_" + corpusID + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_5_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Measure_5.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Measure_5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

