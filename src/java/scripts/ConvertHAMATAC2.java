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
public class ConvertHAMATAC2 {

    String HAMATAC_COMA = "N:\\Workspace\\HZSK\\hamatac\\hamatac.coma";
    
    String SPANS2ATTRIBUTES = "/org/exmaralda/tei/xml/spans2attributes.xsl";
    String INTERPOLATE = "/org/exmaralda/tei/xml/interpolate.xsl";
    //String SPANS2_ATTRIBUTES = "/org/exmaralda/tei/xml/spans2attributes.xsl";
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new ConvertHAMATAC2().doit();
        } catch (JDOMException ex) {
            Logger.getLogger(ConvertHAMATAC2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConvertHAMATAC2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JexmaraldaException ex) {
            Logger.getLogger(ConvertHAMATAC2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConvertHAMATAC2.class.getName()).log(Level.SEVERE, null, ex);
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
                
                File inFile = new File(new File(fullPath).getParentFile(), new File(fullPath).getName().replaceAll("\\.exb$", ".xml"));
                
                String finalDocString = ssf.applyInternalStylesheetToExternalXMLFile(SPANS2ATTRIBUTES, inFile.getAbsolutePath());
                finalDocString = ssf.applyInternalStylesheetToString(INTERPOLATE, finalDocString);
                //finalDocString = ssf.applyInternalStylesheetToString(SPANS2_ATTRIBUTES, finalDocString);
                Document finalDoc = IOUtilities.readDocumentFromString(finalDocString);
                
                
                File outFile = inFile;
                //File otherOutFile = new File(new File("N:\\Workspace\\HZSK\\hamatac\\TEI"), outFile.getName());
                System.out.println("---------------> " + outFile.getAbsolutePath());
                //Files.copy(tempFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                FileIO.writeDocumentToLocalFile(outFile, finalDoc);
                //FileIO.writeDocumentToLocalFile(otherOutFile, finalDoc);
                
            }
        }
    }
    
}
