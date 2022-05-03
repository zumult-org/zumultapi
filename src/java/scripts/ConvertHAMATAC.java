/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.exmaralda.partitureditor.jexmaralda.BasicTranscription;
import org.exmaralda.partitureditor.jexmaralda.JexmaraldaException;
import org.exmaralda.partitureditor.jexmaralda.convert.StylesheetFactory;
import org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.transform.XSLTransformException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import org.zumult.io.FileIO;

/**
 *
 * @author Thomas.Schmidt
 */
public class ConvertHAMATAC {

    String HAMATAC_COMA = "N:\\Workspace\\HZSK\\hamatac\\hamatac.coma";
    
    String TIME2TOKEN_SPAN_REFERENCES = "/org/exmaralda/tei/xml/time2tokenSpanReferences.xsl";
    String REMOVE_TIME = "/org/exmaralda/tei/xml/removeTimepointsWithoutAbsolute.xsl";
    String SPANS2_ATTRIBUTES = "/org/exmaralda/tei/xml/spans2attributes.xsl";
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new ConvertHAMATAC().doit();
        } catch (JDOMException ex) {
            Logger.getLogger(ConvertHAMATAC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConvertHAMATAC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(ConvertHAMATAC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConvertHAMATAC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws JDOMException, IOException, SAXException, JexmaraldaException, XSLTransformException, Exception {
        StylesheetFactory ssf = new StylesheetFactory(true);
        Namespace teiNamespace = Namespace.getNamespace("tei", "http://www.tei-c.org/ns/1.0");
        TEIConverter teiConverter = new org.exmaralda.partitureditor.jexmaralda.convert.TEIConverter();
        teiConverter.setLanguage("de");
        // read COMA doc
        Document comaDoc = FileIO.readDocumentFromLocalFile(new File(HAMATAC_COMA));
        // select communication elements in COMA xml
        List<Element> communicationsList = XPath.selectNodes(comaDoc, "//Communication");
        // iterate through communications
        for (Element communicationElement : communicationsList){
            // select basic transcriptions
            List<Element> transcriptionsList = XPath.selectNodes(communicationElement, "descendant::Transcription[ends-with(Filename,'.exb')]");
            // iterate through basic transcriptions
            for (Element transcriptionElement : transcriptionsList){
                String transcriptID = transcriptionElement.getAttributeValue("Id");
                String nsLink = transcriptionElement.getChildText("NSLink");
                String fullPath = new File(HAMATAC_COMA).getParent() + "/" + nsLink;
                
                File tempFile = File.createTempFile("HAMATAC", ".tei");
                tempFile.deleteOnExit();                
                teiConverter.writeHIATISOTEIToFile(new BasicTranscription(fullPath), tempFile.getAbsolutePath());
                
                //Document intermediateTEIDoc = FileIO.readDocumentFromLocalFile(new File(fullPath));
                
                String finalDocString = ssf.applyInternalStylesheetToExternalXMLFile(TIME2TOKEN_SPAN_REFERENCES, tempFile.getAbsolutePath());
                finalDocString = ssf.applyInternalStylesheetToString(REMOVE_TIME, finalDocString);
                finalDocString = ssf.applyInternalStylesheetToString(SPANS2_ATTRIBUTES, finalDocString);
                Document finalDoc = IOUtilities.readDocumentFromString(finalDocString);
                
                // <idno type="AGD-ID">FOLK_E_00011_SE_01_T_04_DF_01</idno>
                Element transcriptIdnoElement = new Element("idno", teiNamespace);
                transcriptIdnoElement.setAttribute("type", "HZSK-ID");
                transcriptIdnoElement.setText(transcriptID);                
                finalDoc.getRootElement().addContent(0, transcriptIdnoElement);
                
                XPath xp1 = XPath.newInstance("//tei:person"); 
                xp1.addNamespace(teiNamespace);
                List<Element> personL = xp1.selectNodes(finalDoc);
                for (Element personE : personL){
                    // <person xml:id="SPK0" n="Sh" sex="2">
                    String personSigle = personE.getAttributeValue("n");
                    String xp2 = "//Speaker[Sigle='" + personSigle + "']";
                    Element speakerE = (Element) XPath.selectSingleNode(comaDoc, xp2);
                    String speakerID = speakerE.getAttributeValue("Id");
                    Element speakerIdnoElement = new Element("idno", teiNamespace);
                    speakerIdnoElement.setAttribute("type", "HZSK-ID");
                    speakerIdnoElement.setText(speakerID);                
                    personE.addContent(0, speakerIdnoElement);
                    
                }
                
                
                File outFile = new File(new File(fullPath).getParentFile(), new File(fullPath).getName().replaceAll("\\.exb$", ".xml"));
                //File otherOutFile = new File(new File("N:\\Workspace\\HZSK\\hamatac\\TEI"), outFile.getName());
                System.out.println("---------------> " + outFile.getAbsolutePath());
                //Files.copy(tempFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                FileIO.writeDocumentToLocalFile(outFile, finalDoc);
                //FileIO.writeDocumentToLocalFile(otherOutFile, finalDoc);
                
            }
        }
    }
    
}
