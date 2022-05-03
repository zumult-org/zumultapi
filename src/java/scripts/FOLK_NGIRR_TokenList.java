/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.XMLReader;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DefaultTokenList;
import org.zumult.objects.implementations.TokenListTokenFilter;

/**
 *
 * @author Thomas_Schmidt
 */
public class FOLK_NGIRR_TokenList {
    
    String OUT = "F:\\WebApplication3\\src\\java\\data\\FOLK_NGIRR.xml";
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new FOLK_NGIRR_TokenList().doit();
        } catch (IOException | SAXException | ParserConfigurationException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(FOLK_NGIRR_TokenList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
        TokenList posFilterTokenList = XMLReader.readTokenListFromInternalResource("/data/NGIRR_POS_FILTER.xml");
        TokenFilter filter = new TokenListTokenFilter("n", posFilterTokenList){
            @Override
            public String getPreFilterXPath() {
                return "//tei:annotationBlock[descendant::tei:spanGrp[@type='pos' and descendant::tei:span='NGIRR']]/descendant::tei:spanGrp[@type='n']/descendant::tei:span[not(*)]";
            }
            
        };

        
        TokenList tokenList4Corpus = new DefaultTokenList("n");
        IDList folkEventIDs = backendInterface.getEvents4Corpus("FOLK");
        for (String eventID : folkEventIDs){
            Event event = backendInterface.getEvent(eventID);
            IDList speechEventIDs = event.getSpeechEvents();
            for (String speechEventID : speechEventIDs){
                System.out.println(speechEventID);
                IDList transcriptIDs = backendInterface.getTranscripts4SpeechEvent(speechEventID);
                TokenList tokenList4SpeechEvent = new DefaultTokenList("n");
                for (String transcriptID : transcriptIDs){
                    Transcript transcript = backendInterface.getTranscript(transcriptID);
                    TokenList tokenList4Transcript = transcript.getTokenList("n", filter);
                    tokenList4SpeechEvent.merge(tokenList4Transcript);
                    System.out.println(tokenList4Transcript.toXML());
                }
                tokenList4Corpus.merge(tokenList4SpeechEvent);
            }            
        }
        String xml = tokenList4Corpus.toXML();
        
        FileOutputStream fos = new FileOutputStream(OUT);
        fos.write(xml.getBytes("UTF-8"));
        fos.close();                
        
        
    }
    
}
