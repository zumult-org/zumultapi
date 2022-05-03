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
import org.zumult.io.XMLReader;
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
public class Measure_3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_3().doit();
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

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("measures-document");
            doc.appendChild(rootElement);
            
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();     

            TokenList posFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/MEASURE_3_POS_FILTER.xml");
            TokenFilter filter = new TokenListTokenFilter("lemma", posFilterTokenList);

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
                
                IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                TokenList contentWordList4SpeechEvent = new DefaultTokenList("lemma");
                TokenList tokenList4SpeechEvent = new DefaultTokenList("lemma");
                
                for (String transcriptID : transcriptIDs){
                    Transcript transcript = backendInterface.getTranscript(transcriptID);
                    TokenList tokenList4Transcript = transcript.getTokenList("lemma");
                    TokenList ContentWordList4Transcript = transcript.getTokenList("lemma", filter);
                    tokenList4SpeechEvent = tokenList4SpeechEvent.merge(tokenList4Transcript);
                    contentWordList4SpeechEvent = contentWordList4SpeechEvent.merge(ContentWordList4Transcript);
                }
                
                int tokens = tokenList4SpeechEvent.getNumberOfTokens();
                int contentWords = contentWordList4SpeechEvent.getNumberOfTokens();
                double ratio = (double)contentWords/tokens;
                
                Element measure1 = doc.createElement("measure");
                measure1.setAttribute("tokens", Integer.toString(tokens));
                Element measure2 = doc.createElement("measure");
                measure2.setAttribute("contentWords", Integer.toString(contentWords));
                Element measure3 = doc.createElement("measure");
                measure3.setAttribute("ratio", String.format("%.2f", ratio));

                measures.appendChild(measure1);
                measures.appendChild(measure2);
                measures.appendChild(measure3);

                System.out.println("\n" + IOUtilities.documentToString(doc));
            }
//        } //if
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_3_" + corpusID + ".xml"));

            transformer.transform(domSource, xmlResult);

            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure3_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_3_" + corpusID + ".xml");

            System.out.println("exists?: " + xmlFilename.exists());
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_3_" + corpusID + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_3_" + corpusID + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Measure_3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Measure_3.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}

