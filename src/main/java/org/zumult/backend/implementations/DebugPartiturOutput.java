/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package org.zumult.backend.implementations;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEITranscriptConverter;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.COMATranscript;
import org.zumult.objects.implementations.ISOTEITranscript;

/**
 *
 * @author bernd
 */
public class DebugPartiturOutput {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new DebugPartiturOutput().doit();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(DebugPartiturOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void doit() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SAXException, ParserConfigurationException{
        
        
        BackendInterface backend = BackendInterfaceFactory.newBackendInterface();
        
        // Das hier verwenden, wenn das HMAT-Transkript aus dem Backend geholt werden kann
        //Transcript transcript = backend.getTranscript("HMAT_E_00024_SE_01_T_01");
        //Transcript partTranscript = transcript.getPart("au147","au26", true);

        // .. sonst das hier verwenden
        String path = "C:\\Users\\bernd\\Dropbox\\work\\EXMARaLDA_Support\\2026-02-26_ZUMULT_PARTITUR\\"
                + "HMAT_E_00018_SE_01_T_01_REDUCED_1.xml";
        File file = new File(path);
        Document xmlDoc = IOHelper.readDocument(file);
        Transcript partTranscript = new COMATranscript(xmlDoc);

        ISOTEITranscriptConverter converter = new ISOTEITranscriptConverter((ISOTEITranscript)partTranscript);
        // wahrscheinlich kein Fehler hier
        String exb = converter.convert(ISOTEITranscriptConverter.FORMATS.EXB);
        System.out.println("=== EXB conversion successful");
        IOHelper.writeUTF8(new File(new File(path).getParentFile(), "EXB_OUT.exb"), exb);
        // vermutlich auch kein Fehler hier
        String partiturHTMLEndless = converter.convert(ISOTEITranscriptConverter.FORMATS.PARTITUR_ENDLESS_HTML);
        System.out.println("=== HTML Partitur Endless conversion successful");
        // Der bebobachtete Fehler hier?
        String partiturRTF = converter.convert(ISOTEITranscriptConverter.FORMATS.PARTITUR_RTF);
        
    }
    
}
