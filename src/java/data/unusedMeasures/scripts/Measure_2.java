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
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DefaultTokenList;

/**
 *
 * @author josip.batinic
 */
public class Measure_2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_2().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        try {
//            String corpusID = "FOLK";
            String corpusID = "GWSS";

            String data_path = "src\\java\\data\\";
            String IDLists_path = data_path + "IDLists\\";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("measures-document");
            doc.appendChild(rootElement);
        
            // Connect to DGD
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();

//            System.out.print("speech event\t\tlemma types\ttokens\tratio\n");

            Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
            IDList speechEventIDs = new IDList("speechEvents");
            speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));

            for (String speechEventID : speechEventIDs){
//            if (speechEventID.equals("GWSS_E_00001_SE_01")) {

                System.out.print(speechEventID);
                // measures element
                Element measures = doc.createElement("measures");
                measures.setAttribute("speechEventID", speechEventID);
                rootElement.appendChild(measures);
                // get IDs for all transcripts belonging to the current speech event
                IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                // make a new lemma list for this speech even
                TokenList lemmaList4SpeechEvent = new DefaultTokenList("lemma");
                for (String transcriptID : transcriptIDs){
                    // get the transcript...
                    Transcript transcript = backendInterface.getTranscript(transcriptID);
                    // ... and get its lemma list, applying the filter defined above
                    TokenList lemmaList4Transcript = transcript.getTokenList("lemma");
                    // merge this transcript's lemma list with the lemmalist for the entire speech event
                    lemmaList4SpeechEvent = lemmaList4SpeechEvent.merge(lemmaList4Transcript);
                }
                // how many types do we have in the original lemma list for the speech event?
                int lemmas = lemmaList4SpeechEvent.getNumberOfTypes();        
                int tokens = lemmaList4SpeechEvent.getNumberOfTokens();
                double lemmaTokenRatio = (double)lemmas/tokens;
                
                Element measure1 = doc.createElement("measure");
                measure1.setAttribute("lemmas", Integer.toString(lemmas));
                Element measure2 = doc.createElement("measure");
                measure2.setAttribute("tokens", Integer.toString(tokens));
                Element measure3 = doc.createElement("measure");
                measure3.setAttribute("lemma_token_ratio", String.format("%.2f", lemmaTokenRatio));

                measures.appendChild(measure1);
                measures.appendChild(measure2);
                measures.appendChild(measure3);
                
                System.out.println("\n" + IOUtilities.documentToString(doc));

//            } // if
        }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_2_" + corpusID + ".xml"));

            transformer.transform(domSource, xmlResult);

            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure2_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_2_" + corpusID + ".xml");

            System.out.println("exists?: " + xmlFilename.exists());
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_2_" + corpusID + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_2_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Measure_2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Measure_2.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
