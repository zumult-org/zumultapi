/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.objects.TokenList;
import org.zumult.objects.implementations.DefaultTokenList;

/**
 *
 * @author Thomas_Schmidt
 */
public class DeReWo2TokenList {

    //String DEREWO_PATH = "/org/exmaralda/orthonormal/lexicon/derewo_wordlist.txt";
    String DEREWO_PATH = "/data/derewo_wordlist.txt";
    String OUT = "F:\\WebApplication3\\src\\main\\java\\data\\DEREWO_100000.xml";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new DeReWo2TokenList().doit();
        } catch (IOException ex) {
            Logger.getLogger(DeReWo2TokenList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException {
        java.io.InputStream is = getClass().getResourceAsStream(DEREWO_PATH);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String nextLine = new String();
        TokenList tokenList = new DefaultTokenList("n");
        while ((nextLine = br.readLine()) != null){
            tokenList.add(nextLine.trim());
        }
        br.close(); 
        String xml = tokenList.toXML();
        
        FileOutputStream fos = new FileOutputStream(OUT);
        fos.write(xml.getBytes("UTF-8"));
        fos.close();                
        
    }
    
}
