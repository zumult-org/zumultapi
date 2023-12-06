/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.exmaralda.partitureditor.interlinearText.HTMLParameters;
import org.exmaralda.partitureditor.interlinearText.InterlinearText;
import org.exmaralda.partitureditor.interlinearText.ItBundle;
import org.exmaralda.partitureditor.interlinearText.RTFParameters;
import org.exmaralda.partitureditor.jexmaralda.AbstractEventTier;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.TierFormatTable;
import org.exmaralda.partitureditor.jexmaralda.convert.ELANConverter;
import org.exmaralda.partitureditor.jexmaralda.convert.ItConverter;
import org.exmaralda.partitureditor.jexmaralda.convert.PraatConverter;
import static org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter.ISOTEI2EXMARaLDA_1_ATTRIBUTES2SPANS_XSL;
import static org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter.ISOTEI2EXMARaLDA_1b_AUGMENTTIMELINE_XSL;
import static org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter.ISOTEI2EXMARaLDA_2_TOKEN2TIMEREFS_XSL;
import static org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter.ISOTEI2EXMARaLDA_2b_REMOVE_TIMEPOINTS_XSL;
import static org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter.ISOTEI2EXMARaLDA_3_DETOKENIZE_XSL;
import static org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter.ISOTEI2EXMARaLDA_4_TRANSFORM_XSL;
import org.xml.sax.SAXException;
import org.zumult.objects.implementations.ISOTEITranscript;

/**
 *
 * @author thomas.schmidt
 */
public class ISOTEITranscriptConverter {
    
    ISOTEITranscript transcript;


    public enum FORMATS {FLN, EXB, EAF, PRAAT, HTML, TXT, PARTITUR_HTML, PARTITUR_ENDLESS_HTML, PARTITUR_RTF}
    
    String XSL_FLN = "/org/exmaralda/tei/xml/isotei2folker.xsl";
    String XSL_EXB = "/org/exmaralda/tei/xml/isotei2exmaralda.xsl";
    String XSL_HTML = Constants.ISOTEI2HTML_STYLESHEET2;
    String XSL_TXT = "/org/exmaralda/tei/xml/isotei2txt.xsl";
    
    String[] XSL_EXB_SEQUENCE = {
        //ISOTEI2EXMARaLDA_0_NORMALIZE,
        ISOTEI2EXMARaLDA_1_ATTRIBUTES2SPANS_XSL,
        ISOTEI2EXMARaLDA_1b_AUGMENTTIMELINE_XSL,
        ISOTEI2EXMARaLDA_2_TOKEN2TIMEREFS_XSL,
        ISOTEI2EXMARaLDA_2b_REMOVE_TIMEPOINTS_XSL,
        ISOTEI2EXMARaLDA_3_DETOKENIZE_XSL,
        ISOTEI2EXMARaLDA_4_TRANSFORM_XSL
    };
    
    public ISOTEITranscriptConverter(ISOTEITranscript transcript) {
        this.transcript = transcript;
    }
    
    
    public String convert(FORMATS format) throws IOException{
        try {
            switch (format){
                case FLN : return convertFLN();
                case EXB : return convertEXB(); 
                case EAF : return convertEAF();
                case PRAAT : return convertPRAAT();
                case HTML : return convertHTML();
                case TXT : return convertTXT();
                case PARTITUR_HTML : return convertPartiturHTML();
                case PARTITUR_ENDLESS_HTML : return convertPartiturEndlessHTML();
                case PARTITUR_RTF : return convertPartiturRTF();
            }
            return "";
        } catch (TransformerException | SAXException | JexmaraldaException | ParserConfigurationException ex) {
            Logger.getLogger(ISOTEITranscriptConverter.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException();
        }
    }
    
    private String convertFLN() throws TransformerException {
        return new IOHelper().applyInternalStylesheetToString(XSL_FLN, transcript.toXML());
    }
    
    private String convertEXB() throws TransformerException {        
        
        IOHelper ioHelper = new IOHelper();
        String currentString = transcript.toXML();
        int count = 1;
        for (String XSL : XSL_EXB_SEQUENCE){
            /*try {
                IOHelper.writeUTF8(new File(new File("C:\\Users\\thomas.schmidt\\Desktop\\DEBUG\\ZUMULT_EXB"), "Out_" + Integer.toString(count) + ".xml"), currentString);
            } catch (IOException ex) {
                Logger.getLogger(ISOTEITranscriptConverter.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            String nextString = ioHelper.applyInternalStylesheetToString(XSL, currentString);
            currentString = nextString;
            count++;
        }
        /*try {
            IOHelper.writeUTF8(new File(new File("C:\\Users\\thomas.schmidt\\Desktop\\DEBUG\\ZUMULT_EXB"), "Out_" + Integer.toString(count) + ".xml"), currentString);
        } catch (IOException ex) {
            Logger.getLogger(ISOTEITranscriptConverter.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return currentString;
        
    }


    private String convertHTML() throws TransformerException {
        String[][] parameters = {
            {"DROPDOWN", "FALSE"}
        };
        return new IOHelper().applyInternalStylesheetToString(XSL_HTML, transcript.toXML(), parameters);
    }
    
    private String convertTXT() throws TransformerException {
        return new IOHelper().applyInternalStylesheetToString(XSL_TXT, transcript.toXML());
    }

    


    private String convertEAF() throws TransformerException, SAXException, JexmaraldaException, IOException, ParserConfigurationException {
        String exb = convertEXB();
        BasicTranscription bt = new BasicTranscription();
        bt.BasicTranscriptionFromString(exb);
        ELANConverter converter = new ELANConverter();
        File tempFile = File.createTempFile("EAF", "eaf");
        tempFile.deleteOnExit();
        converter.writeELANToFile(bt, tempFile.getAbsolutePath());
        List<String> readAllLines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (String line : readAllLines){
            sb.append(line);
        }
        return sb.toString();
    }

    private String convertPRAAT() throws SAXException, JexmaraldaException, TransformerException {
        String exb = convertEXB();
        BasicTranscription bt = new BasicTranscription();
        bt.BasicTranscriptionFromString(exb);
        PraatConverter converter = new PraatConverter();
        return converter.BasicTranscriptionToPraat(bt);
    }
    
    private String convertPartiturHTML() throws TransformerException, SAXException, JexmaraldaException {
        InterlinearText it = convertInterlinearText();

        HTMLParameters htmlParameters = new HTMLParameters();
        htmlParameters.setWidth(480.0);
        htmlParameters.smoothRightBoundary = true;
        htmlParameters.includeSyncPoints = false;
        htmlParameters.putSyncPointsOutside = false;

        it.trim(htmlParameters);
        it.reorder();
        
        return it.toHTML(htmlParameters);        
    }

    private String convertPartiturEndlessHTML() throws TransformerException, SAXException, JexmaraldaException {
        InterlinearText it = convertInterlinearText();

        HTMLParameters htmlParameters = new HTMLParameters();
        //htmlParameters.setWidth(480.0);
        htmlParameters.smoothRightBoundary = true;
        htmlParameters.includeSyncPoints = true;
        htmlParameters.putSyncPointsOutside = true;

        //it.trim(htmlParameters);
        //it.reorder();
        
        return it.toHTML(htmlParameters);        
    }

    
    private String convertPartiturRTF() throws TransformerException, SAXException, JexmaraldaException {
        InterlinearText it = convertInterlinearText();

        RTFParameters rtfParameters = new RTFParameters();
        rtfParameters.saveSpace=true;
        rtfParameters.removeEmptyLines=true;
        it.trim(rtfParameters);               
        
        return it.toRTF(rtfParameters);
    }


    private InterlinearText convertInterlinearText() throws TransformerException, SAXException, JexmaraldaException {
        String exb = convertEXB();
        BasicTranscription bt = new BasicTranscription();
        bt.BasicTranscriptionFromString(exb);
        bt.getBody().stratify(AbstractEventTier.STRATIFY_BY_DISTRIBUTION);
        TierFormatTable tft = TierFormatTable.makeTierFormatTableForDGD2(bt);
        InterlinearText it = ItConverter.BasicTranscriptionToInterlinearText(bt, tft);

        //it.markOverlaps("[", "]");

        int frameEndPosition = -1;
        for (int pos=0; pos<bt.getBody().getNumberOfTiers();pos++){
            if (bt.getBody().getTierAt(pos).getSpeaker()==null){
                frameEndPosition = pos-1;
                break;
            }
        }
        if (frameEndPosition>=0){
            ((ItBundle)it.getItElementAt(0)).frameEndPosition=frameEndPosition;
        } 
        
        //System.out.println(it.toXML());
        
        return it;
    }
    
    
    
}
