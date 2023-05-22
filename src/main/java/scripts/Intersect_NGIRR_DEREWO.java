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
import org.zumult.io.XMLReader;
import org.zumult.objects.TokenList;

/**
 *
 * @author Thomas_Schmidt
 */
public class Intersect_NGIRR_DEREWO {

    String OUT = "F:\\WebApplication3\\src\\main\\java\\data\\FOLK_NGIRR_OHNE_DEREWO.xml";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Intersect_NGIRR_DEREWO().doit();
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(Intersect_NGIRR_DEREWO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException, SAXException, ParserConfigurationException {
        TokenList ngirr = XMLReader.readTokenListFromInternalResource("/data/FOLK_NGIRR.xml");
        TokenList derewo = XMLReader.readTokenListFromInternalResource("/data/DEREWO_100000.xml");
        
        ngirr.remove(derewo);
        
        String xml = ngirr.toXML();
        
        FileOutputStream fos = new FileOutputStream(OUT);
        fos.write(xml.getBytes("UTF-8"));
        fos.close();                
        
    }
    
}
