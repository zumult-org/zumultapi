/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.objects.Transcript;

/**
 *
 * @author Thomas_Schmidt
 */
public class TestTransformation {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestTransformation().doit();
        } catch (IOException ex) {
            Logger.getLogger(TestTransformation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TestTransformation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(TestTransformation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(TestTransformation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestTransformation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
        Transcript transcript = backendInterface.getTranscript("FOLK_E_00001_SE_01_T_01");
        double startTime = transcript.getStartTime();
        Transcript partTranscript = transcript.getPart(startTime, startTime+60.0, true);
        String partTranscriptXML = partTranscript.toXML();
                    String[][] xslParameters = {
                        {"TOKEN_LIST_URL", "file:/F:/WebApplication3/src/main/java/data/HERDER_1000.xml"}
                    };
                    

        //String partTranscriptHTML = pathToWordList;
        String partTranscriptHTML = 
                new IOHelper().applyInternalStylesheetToString(
                        Constants.ISOTEI2HTML_HIGHLIGHT_TOKENS_STYLESHEET, 
                        partTranscriptXML, 
                        xslParameters);
        System.out.println(partTranscriptHTML);
        
    }
    
}
