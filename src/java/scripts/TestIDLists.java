/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;

/**
 *
 * @author josip.batinic
 */
public class TestIDLists {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestIDLists().doit();
        } catch (IOException ex) {
            Logger.getLogger(TestIDLists.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    void doit() throws IOException {
        try {
            // time elapsed: 01:00:00.020 on my machine
            long start = System.currentTimeMillis();
            
            
            String lala = "lala";
//            boolean isLa = lala.matches("(?i:\\b(LALA|LAla|laLa|lalAa)\\b)");
            boolean isLa = lala.matches("\\b(LALA|LAla|laLa|lalAa)\\b");

            System.out.println("isLa: " + isLa);
            
            String transcriptID = "FOLK_E_00001_SE_01_T_01";
            Transcript transcript = BackendInterfaceFactory.newBackendInterface().getTranscript(transcriptID);
            System.out.println("end time: " + transcript.getEndTime());
            
//            Path filePath = new File("src/java/IDLists/GWSS/GWSS_transcripts.txt").toPath();
//            IDList idList = new IDList("transcripts");
//            idList.addAll(Files.readAllLines(filePath));
//            for (String string : idList) {
//                System.out.println("string: " + string);
//            }
            long end = System.currentTimeMillis();
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
            System.out.println("time elapsed: " + formatter.format(new Date(end - start)));
//            
//            
//            // time elapsed2: 01:00:14.804 on my machine; a significant difference of 14.784 seconds
            long start2 = System.currentTimeMillis();
            
//            String url = "http://zumult.ids-mannheim.de/ProtoZumult/api/transcripts/FOLK/FOLK_E_00001/FOLK_E_00001_SE_01";
            String url = "http://localhost:8080/DGDRESTTest/api/";
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(url);
//            client.
            
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("accept", "application/atom+xml");
            
//            Document doc = IOUtilities.readDocumentFromURL(url);
            
            
            
//            InputStream is = conn.getInputStream();
//            XMLReader xr = XMLReaderFactory.createXMLReader();
//            InputSource inS = new InputSource(is);
//            
//            xr.parse(inS.toString());
            
            System.out.println("inS: " + target.request(MediaType.TEXT_XML).get());
            
//            BackendInterface bi = new BackendInterfaceFactory().newBackendInterface();
//            IDList events = bi.getEvents4Corpus("GWSS");
//            IDList transcripts = new IDList("transcripts");
//            
//            for (String event : events) {
//                IDList speechEvents = bi.getSpeechEvents4Event(event);
//                for (String speechEvent : speechEvents) {
//                    transcripts.addAll(bi.getTranscripts4SpeechEvent(speechEvent));
//                }
//            }
//            for (String string : transcripts) {
//                System.out.println("string: " + string);
//            }



            long end2 = System.currentTimeMillis();
            System.out.println("time elapsed2: " + formatter.format(new Date(end2 - start2)));

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TestIDLists.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(TestIDLists.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(TestIDLists.class.getName()).log(Level.SEVERE, null, ex);
        }
            

    }
}
