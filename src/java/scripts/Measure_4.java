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
public class Measure_4 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Measure_4().doit();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Measure_1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException {
        try {
            String corpusID = "FOLK";
//            String corpusID = "GWSS";
//            String type = "norm";
            String type = "transcription";
            
            String data_path = "src\\java\\data\\";
            String IDLists_path = data_path + "IDLists\\";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("measures-document");
            doc.appendChild(rootElement);
            
            TokenList posNouns = new DefaultTokenList("pos");
            posNouns.put("NE", 0);
            posNouns.put("NN", 0);

            TokenList posAdjectives = new DefaultTokenList("pos");
            posAdjectives.put("ADJA", 0);
            posAdjectives.put("ADJD", 0);
            
            TokenFilter nounFilter = new TokenListTokenFilter(type, posNouns);
            TokenFilter adjectiveFilter = new TokenListTokenFilter(type, posAdjectives);

            // Connect to DGD
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
//            System.out.print("transcript\t\t\tglobal\tnouns\tadjectives\n");

            Path speechEventsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
            IDList speechEventIDs = new IDList("speechEvents");
            speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));

//                    Path transcriptsFilePath = new File(IDLists_path + corpusID + "/" + corpusID + "_transcripts.txt").toPath();
//                    IDList transcriptIDs = new IDList("transcripts");
//                    transcriptIDs.addAll(Files.readAllLines(transcriptsFilePath));


            for (String speechEventID : speechEventIDs) {
                System.out.println(speechEventID);

                // measures element
                Element measures = doc.createElement("measures");
                measures.setAttribute("speechEventID", speechEventID);
                rootElement.appendChild(measures);

                IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
//                TokenList tokenListSpeechEvent = new DefaultTokenList(type);
//                TokenList nounListSpeechEvent = new DefaultTokenList(type);
//                TokenList adjectiveListSpeechEvent = new DefaultTokenList(type);
                
                int nrOfTokensSpeechEvent = 0;
                int nrOfTokensNounsSpeechEvent = 0;
                int nrOfTokensAdjectivesSpeechEvent = 0;
                
                int totalLengthSpeechEvent = 0;
                int totalLengthNounsSpeechEvent = 0;
                int totalLengthAdjectivesSpeechEvent = 0;
                int longWordCountSpeechEvent = 0;
                int longWordCountNounsSpeechEvent = 0;
                int longWordCountAdjectivesSpeechEvent = 0;
                
                for (String transcriptID : transcriptIDs) {
                    Transcript transcript = backendInterface.getTranscript(transcriptID);

                    TokenList tokenList4Transcript = transcript.getTokenList(type);
                    TokenList nounList4Transcript = transcript.getTokenList(type, nounFilter);
                    TokenList adjectiveList4Transcript = transcript.getTokenList(type, adjectiveFilter);

                    int nrOfTokens = transcript.getNumberOfTokens();
                    int nrOfTokensNouns = nounList4Transcript.getNumberOfTokens();
                    int nrOfTokensAdjectives = adjectiveList4Transcript.getNumberOfTokens();

                    int totalLength = 0;
                    int totalLengthNouns = 0;
                    int totalLengthAdjectives = 0;
                    int longWordCount = 0;
                    int longWordCountNouns = 0;
                    int longWordCountAdjectives = 0;
                    
                    for (Map.Entry<String, Integer> entry : tokenList4Transcript.entrySet()) {
                        String token = entry.getKey();
                        Integer occurances = entry.getValue();
                        int tokenLength = token.length();
                        int tokensLength = tokenLength * occurances;

                        if (tokenLength > 10) {
                            longWordCount += occurances;
                        }

                        totalLength += tokensLength;
                    }
                    
                    for (Map.Entry<String, Integer> entry : nounList4Transcript.entrySet()) {
                        String token = entry.getKey();
                        Integer occurances = entry.getValue();
                        int tokenLength = token.length();
                        int tokensLength = tokenLength * occurances;

                        if (tokenLength > 10) {
                            longWordCountNouns += occurances;
                        }

                        totalLengthNouns += tokensLength;
                    }

                    for (Map.Entry<String, Integer> entry : adjectiveList4Transcript.entrySet()) {
                        String token = entry.getKey();
                        Integer occurances = entry.getValue();
                        int tokenLength = token.length();
                        int tokensLength = tokenLength * occurances;

                        if (tokenLength > 10) {
                            longWordCountAdjectives += occurances;
                        }

                        totalLengthAdjectives += tokensLength;
                    }
                    
                    nrOfTokensSpeechEvent += nrOfTokens;
                    nrOfTokensNounsSpeechEvent += nrOfTokensNouns;
                    nrOfTokensAdjectivesSpeechEvent += nrOfTokensAdjectives;

                    totalLengthSpeechEvent += totalLength;
                    totalLengthNounsSpeechEvent += totalLengthNouns;
                    totalLengthAdjectivesSpeechEvent += totalLengthAdjectives;
                    longWordCountSpeechEvent += longWordCount;
                    longWordCountNounsSpeechEvent += longWordCountNouns;
                    longWordCountAdjectivesSpeechEvent += longWordCountAdjectives;
                }
                double averageTokenLength = (double) totalLengthSpeechEvent / (double) nrOfTokensSpeechEvent;
                double averageTokenLengthNouns = (double) totalLengthNounsSpeechEvent / (double) nrOfTokensNounsSpeechEvent;
                double averageTokenLengthAdjectives = (double) totalLengthAdjectivesSpeechEvent / (double) nrOfTokensAdjectivesSpeechEvent;
                // element for average word length per transcript
                Element measure1 = doc.createElement("measure");
                measure1.setAttribute("averageTokenLength", String.format("%.2f", averageTokenLength));
                measure1.setAttribute("TokensWith11OrMoreLetters", Integer.toString(longWordCountSpeechEvent));

                // element for average length per NN
                Element measure2 = doc.createElement("measure");
                measure2.setAttribute("averageNounLength", String.format("%.2f", averageTokenLengthNouns));
                measure2.setAttribute("NounsWith11OrMoreLetters", Integer.toString(longWordCountNounsSpeechEvent));

                // element for average length per ADJ
                Element measure3 = doc.createElement("measure");
                measure3.setAttribute("averageAdjectiveLength", String.format("%.2f", averageTokenLengthAdjectives));
                measure3.setAttribute("AdjectivesWith11OrMoreLetters", Integer.toString(longWordCountAdjectivesSpeechEvent));

                measures.appendChild(measure1);
                measures.appendChild(measure2);
                measures.appendChild(measure3);
                
//                System.out.println("\n" + IOUtilities.w3cDocumentToString(doc));
            }            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            Result xmlResult = new StreamResult(new File(data_path + "Measure_4_" + corpusID + "_" + type + ".xml"));

            transformer.transform(domSource, xmlResult);
            
            
            File xslFilename = new File("src\\java\\org\\zumult\\io\\measure4_xml_to_txt.xsl");
            File xmlFilename = new File(data_path + "Measure_4_" + corpusID + "_" + type + ".xml");
            
            System.out.println("exists?: " + xmlFilename.exists());
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            Transformer xformer = template.newTransformer();
            Source xmlSource = new StreamSource(new FileInputStream(new File(data_path + "Measure_4_" + corpusID + "_" + type + ".xml")));
            Result txtResult = new StreamResult(new FileOutputStream(new File(data_path + "Measure_4_" + corpusID + "_" + type + ".txt")));
            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(xmlSource, txtResult);

            System.out.println("File saved!");
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(Measure_4.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(Measure_4.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
}

