/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.zumult.backend.BackendInterface;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.ISOTEITranscript;

/**
 *
 * @author thomas.schmidt
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TransformerException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        //new IOHelper().applyInternalStylesheetToString(Constants.ISOTEI2HTML_STYLESHEET2, "<root/>");
        //MediaUtilities.getVideoImage(1000.0, "N:\\ARCHIV\\FOLK\\FOLK_E_00069\\FOLK_E_00069_SE_01_V_02_DF_01.mp4", "N:\\ARCHIV\\FOLK\\FOLK_E_00069\\Test3.png");
        new Test().doit();
    }

    private void doit() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        BackendInterface backend = org.zumult.backend.BackendInterfaceFactory.newBackendInterface();
        Transcript transcript = backend.getTranscript("FOLK_E_00372_SE_01_T_01");
        Transcript part = transcript.getPart(0.0, 20.0, true);
        String converted = new ISOTEITranscriptConverter((ISOTEITranscript) part).convert(ISOTEITranscriptConverter.FORMATS.TXT);
        System.out.println(converted);
        
    }
    
}
