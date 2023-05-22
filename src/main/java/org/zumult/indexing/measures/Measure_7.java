/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing.measures;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class Measure_7 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_7().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    String[] corpusIDs = {"FOLK", "GWSS"};
    String OUTPUT_PATH = System.getProperty("user.dir") + Constants.JAVA_FOLDER_PATH + Constants.DATA_MEASURES_PATH;
    


    public void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
        for (String corpusID : corpusIDs){
            try {
                long start = System.currentTimeMillis();

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                // root elements
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("measures-document");
                doc.appendChild(rootElement);

    //            System.out.print("transcript\t\t\tglobal\tnouns\tadjectives\n");

                /*Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
                IDList speechEventIDs = new IDList("speechEvents");
                speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));*/

                IDList speechEventIDs = backendInterface.getSpeechEvents4Corpus(corpusID);

                for (String speechEventID : speechEventIDs) {
    //                if (speechEventID.equals("GWSS_E_00001_SE_01")) {

    //                    String speechEventID = "FOLK_E_00001_SE_01";
                        System.out.println("   " + speechEventID);

                        // measures element
                        Element measures = doc.createElement("measures");
                        measures.setAttribute("speechEventID", speechEventID);
                        rootElement.appendChild(measures);
                        
                        Element measure = doc.createElement("measure");
                                                
                        // get value for e_se_sprachen, important for non-German data from GWSS
                        SpeechEvent speechEvent = backendInterface.getSpeechEvent(speechEventID);
                        MetadataKey e_se_sprachen = backendInterface.findMetadataKeyByID("v_e_se_sprachen");
                        String e_se_sprachen_value = speechEvent.getMetadataValue(e_se_sprachen);
                        
                        if (corpusID.equals("GWSS") && !e_se_sprachen_value.startsWith("Deutsch")){
                            measure.setAttribute("normRate", "nicht verfügbar");
                        }else{
                        
                        IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);

                        int nrOfTokensSpeechEvent = 0;
                        int count = 0;

                        for (String transcriptID : transcriptIDs) {
                            Transcript transcript = backendInterface.getTranscript(transcriptID);
                            NodeList allTokens = transcript.getAllTokens();
                            int nrOfTokens = transcript.getNumberOfTokens();
                            nrOfTokensSpeechEvent += nrOfTokens;

                            for (int i = 0; i < allTokens.getLength(); i++) {
                                Element node = (Element)allTokens.item(i);
    //                            boolean filteredNGIRRs = node.getAttribute("pos").equals("NGIRR")
    //                                    && node.getAttribute("norm").matches(
    //                                    "(?i:\\b(mh|achso|a|aua|bä|ba|bah|wäh|moah|hö|he|heieiei|haihaihai|hoppala|hoi|huiuiui|ich|"
    //                                            + "jippieh jeah|mei|mai|mäi|naja|o|oh|ouh|ohje|oh je|ui je|oweiowei|pf|pft|ph|pieps"
    //                                            + "|prosit|pscht|pist|sst|scht|tsch|rumms|tz tz|tzz tzz|tschüs|tschüß|uf|oi|eu|ujujuj|uoah|oah|uups)\\b)");


                                //System.out.println("POS: " + node.getAttribute("pos"));
                                boolean filtered = node.getAttribute("pos").matches("\\b(NGHES|XY|AB|\\?\\?\\?|SPELL|UI)\\b"); // || filteredNGIRRs;
                                //System.out.println("filtered: " + filtered);
    //                            int idNr = Integer.parseInt(node.getAttribute("xml:id").replaceAll("[^\\d]", ""));
                                String normalisedForm = node.getAttribute("norm");
                                String transcribedForm = node.getTextContent();

                                if (!normalisedForm.equalsIgnoreCase(transcribedForm) && !filtered) {
                                    //System.out.println("normalisedForm: " + normalisedForm);
                                    //System.out.println("transcribedForm: " + transcribedForm);
                                    count++;
                                }
                            }
    //                        String l = la.getAttributes().getNamedItem("id").getTextContent();
    //                        System.out.println("LALALALALAL\n" + IOUtilities.elementToString(e));
                        }
                        
                        double normRate = (double) count / (double) nrOfTokensSpeechEvent * 100;
    //                    measures.setAttribute("normRate", String.format("%.2f", normRate));
                        
                        measure.setAttribute("normRate", String.format("%.2f", normRate));
                        
                        }

                        measures.appendChild(measure);

                        //System.out.println("\n" + IOUtilities.documentToString(doc));
    //                }

                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(doc);
                Result xmlResult = new StreamResult(new File(OUTPUT_PATH + "Measure_7_" + corpusID + ".xml"));
                
                transformer.transform(domSource, xmlResult);


                /*File xslFilename = new File("src\\java\\org\\zumult\\io\\measure7_xml_to_txt.xsl");
                File xmlFilename = new File(data_path + "Measure_7_" + corpusID + ".xml");

                System.out.println("exists?: " + xmlFilename.exists());
                TransformerFactory factory = TransformerFactory.newInstance();
                Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
                Transformer xformer = template.newTransformer();
                Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_7_" + corpusID + ".xml")));
                Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_7_" + corpusID + ".txt")));
                // Apply the xsl file to the source file and write the result to the output file
                xformer.transform(xmlSource, txtResult);

                System.out.println("File saved!");*/

                long end = System.currentTimeMillis();
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
                System.out.println("time elapsed: " + formatter.format(new Date(end - start)));
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(Measure_7.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(Measure_7.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

