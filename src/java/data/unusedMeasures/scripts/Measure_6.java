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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
public class Measure_6 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_6().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_6.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        try {
//            String corpusID = "FOLK";
            String corpusID = "GWSS";
            String type = "norm";
            
            String data_path = "src\\java\\data\\";
            String IDLists_path = data_path + "IDLists\\";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("measures-document");
            doc.appendChild(rootElement);
            
            TokenList posPRELS = new DefaultTokenList("pos");
            posPRELS.put("PRELS", 0);
            TokenList posPDAT = new DefaultTokenList("pos");
            posPDAT.put("PDAT", 0);
            

            TokenFilter PRELSFilter = new TokenListTokenFilter(type, posPRELS);
            TokenFilter PDATFilter = new TokenListTokenFilter(type, posPDAT);

            // Connect to DGD
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
//            System.out.print("transcript\t\t\tglobal\tnouns\tadjectives\n");

            Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
            IDList speechEventIDs = new IDList("speechEvents");
            speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));

            for (String speechEventID : speechEventIDs) {
//                if (speechEventID.equals("GWSS_E_00001_SE_01")) {
                    
//                    String speechEventID = "FOLK_E_00001_SE_01";
                    System.out.println(speechEventID);

                    // measures element
                    Element measures = doc.createElement("measures");
                    measures.setAttribute("speechEventID", speechEventID);
                    rootElement.appendChild(measures);

                    IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);

                    int nrOfTokensSpeechEvent = 0;
                    int prels = 0;
                    int pdat = 0;
                    int artAdjNn = 0;
                    int apprArtAdjNn = 0;

                    for (String transcriptID : transcriptIDs) {
                        System.out.println(transcriptID);
                        Transcript transcript = backendInterface.getTranscript(transcriptID);
                        int nrOfTokens = transcript.getNumberOfTokens();
                        nrOfTokensSpeechEvent += nrOfTokens;
                        TokenList prelsList = transcript.getTokenList(type, PRELSFilter);
                        TokenList pdatList = transcript.getTokenList(type, PDATFilter);
                        
                        for (Map.Entry<String, Integer> entry : prelsList.entrySet()) {
                            Integer occurances = entry.getValue();
                            prels += occurances;
                        }
                        for (Map.Entry<String, Integer> entry : pdatList.entrySet()) {
                            Integer occurances = entry.getValue();
                            pdat += occurances;
                        }

                        NodeList adjaNodes = transcript.getTokensByPOS("ADJA");
                        
                        for (int i = 0; i < adjaNodes.getLength(); i++) {
                            System.out.println("inside for");
                            Element node = (Element)adjaNodes.item(i);
                            
                            Node prevElement = node.getPreviousSibling();
                            Node nextElement = node.getNextSibling();
                            
                            // find previous w element
                            while (prevElement != null && !prevElement.getNodeName().equals("w")) {
                                prevElement = prevElement.getPreviousSibling();
                            }
                            String prevElementId = prevElement != null ? prevElement.getAttributes().getNamedItem("xml:id").getNodeValue() : null;
                            
                            Node prevPrevElement = prevElement != null ? prevElement.getPreviousSibling() : null;
                            while (prevPrevElement != null && !prevPrevElement.getNodeName().equals("w")) {
                                prevPrevElement = prevPrevElement.getPreviousSibling();
                            }
                            String prevPrevElementId = prevPrevElement != null ? prevPrevElement.getAttributes().getNamedItem("xml:id").getNodeValue() : null;

                            while (nextElement != null && !nextElement.getNodeName().equals("w")) {
                                nextElement = nextElement.getNextSibling();
                            }
                            String nextElementId = nextElement != null ? nextElement.getAttributes().getNamedItem("xml:id").getNodeValue() : null;
                            
                            
                            
//                            System.out.println(transcript.getTokenById("w" + apprPosition).getAttribute("pos") + " " + apprPosition);
                            
                            boolean isAppr = (transcript.getElementById(prevPrevElementId) != null) ? transcript.getElementById(prevPrevElementId).getAttribute("pos").equals("APPR") : false;
                            boolean isArt = (transcript.getElementById(prevElementId) != null) ? transcript.getElementById(prevElementId).getAttribute("pos").equals("ART") : false;
                            boolean isApprart = (transcript.getElementById(prevElementId) != null) ? transcript.getElementById(prevElementId).getAttribute("pos").equals("APPRART") : false;
                            boolean isNn = (transcript.getElementById(nextElementId) != null) ? transcript.getElementById(nextElementId).getAttribute("pos").equals("NN"): false;
                                                        
                            if (isNn) {
                                if (isArt) {
                                    // ART ADJA NN
                                    artAdjNn++;
                                    System.out.println("case 1: " + transcript.getElementById(prevElementId).getAttribute("norm")
                                            + " " + node.getAttribute("norm")
                                            + " " + transcript.getElementById(nextElementId).getAttribute("norm"));
                                    if (isAppr) {
                                    // APPR ART ADJA NN 
                                    apprArtAdjNn++;
                                        System.out.println("case 1.1: " + transcript.getElementById(prevPrevElementId).getAttribute("norm")
                                        + " " + transcript.getElementById(prevElementId).getAttribute("norm")
                                        + " " + node.getAttribute("norm")
                                        + " " + transcript.getElementById(nextElementId).getAttribute("norm"));
                                    }

                                } 
                                
                                if (isApprart) {
                                    // APPRART ADJA NN
                                    apprArtAdjNn++;
                                    System.out.println("case 2: " + transcript.getElementById(prevElementId).getAttribute("norm")
                                            + " " + node.getAttribute("norm")
                                            + " " + transcript.getElementById(nextElementId).getAttribute("norm"));
                                }
                            }
                            
//                            System.out.println("BUBUBUB\n" + IOUtilities.elementToString(node));
//                            System.out.println("xml:id number: " + node.getAttribute("xml:id").replaceAll("[^\\d]", ""));
//                            System.out.println("xml:id" + node.getAttribute("xml:id"));
//                            e.appendChild(node);
                        }
//                        String l = la.getAttributes().getNamedItem("id").getTextContent();
//                        System.out.println("LALALALALAL\n" + IOUtilities.elementToString(e));
                    }

                    double perMilPRELS = (double) prels / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilPDAT = (double) pdat / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilArtAdjNn = (double) artAdjNn / (double) nrOfTokensSpeechEvent * 1000;
                    double perMilApprArtAdjNn = (double) apprArtAdjNn / (double) nrOfTokensSpeechEvent * 1000;

//                    measures.setAttribute("perMilAll", String.format("%.2f", perMilAll));

                    Element measure1 = doc.createElement("measure");
                    measure1.setAttribute("perMilPRELS", String.format("%.2f", perMilPRELS));
                    Element measure2 = doc.createElement("measure");
                    measure2.setAttribute("perMilPDAT", String.format("%.2f", perMilPDAT));
                    Element measure3 = doc.createElement("measure");
                    measure3.setAttribute("perMilArtAdjNn", String.format("%.2f", perMilArtAdjNn));
                    Element measure4 = doc.createElement("measure");
                    measure4.setAttribute("perMilApprArtAdjNn", String.format("%.2f", perMilApprArtAdjNn));

                    measures.appendChild(measure1);
                    measures.appendChild(measure2);
                    measures.appendChild(measure3);
                    measures.appendChild(measure4);

                    System.out.println("\n" + IOUtilities.documentToString(doc));
//                } // if
                
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_6_" + corpusID + ".xml"));

            transformer.transform(domSource, xmlResult);
            
            
            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure6_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_6_" + corpusID + ".xml");
            
            System.out.println("exists?: " + xmlFilename.exists());
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_6_" + corpusID + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_6_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Measure_6.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Measure_6.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

