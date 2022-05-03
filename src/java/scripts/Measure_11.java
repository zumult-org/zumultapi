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
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class Measure_11 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_11().doit();
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
//                    String speechEventID = "GWSS_E_00224_SE_01";
                    System.out.println(speechEventID);

                    // measures element
                    Element measures = doc.createElement("measures");
                    measures.setAttribute("speechEventID", speechEventID);
                    rootElement.appendChild(measures);

                    IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                                        
                    long durationSpeechEvent = 0;

                    for (String transcriptID : transcriptIDs) {
                        System.out.println(transcriptID);
                        Transcript transcript = backendInterface.getTranscript(transcriptID);
                        
                        double endTime = (long)transcript.getEndTime();
                        double startTime = (long)transcript.getStartTime();

                        durationSpeechEvent += (endTime - startTime) * 1000;
                        
                    } // for transcript
                    
                    System.out.println("durationSpeechEvent: " + durationSpeechEvent);

                    String durationFormatted =  String.format("%d:%02d:%02d",
                            (durationSpeechEvent / 3600000), // hours
                            (durationSpeechEvent / 60000) % 60, // mins
                            ((durationSpeechEvent / 1000) % 60)); // secs

                    measures.setAttribute("duration", durationFormatted);
                    
                    System.out.println("\n" + IOUtilities.documentToString(doc));
//                    System.out.println("\n" + IOUtilities.documentToString(doc9));
//                } // if
                
            } // for speech event
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            TransformerFactory factory = TransformerFactory.newInstance();
            
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_11_" + corpusID + ".xml"));

            transformer.transform(domSource, xmlResult);
            
            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure11_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_11_" + corpusID + ".xml");
            
            System.out.println("exists?: " + xmlFilename.exists());
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_11_" + corpusID + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_11_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
            
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Measure_10_2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Measure_10_2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

