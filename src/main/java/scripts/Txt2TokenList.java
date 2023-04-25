/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
public class Txt2TokenList {

    //String IN = "F:\\Dropbox\\IDS\\ZuMult\\Wordlists\\Schnittmenge_FOLK_00001_Goethe_A1.csv";
    //String OUT = "F:\\Dropbox\\IDS\\ZuMult\\Wordlists\\Schnittmenge_FOLK_00001_Goethe_A1.xml";
    
    //String IN = "F:\\Dropbox\\IDS\\ZuMult\\Herder_1000.txt";
    //String OUT = "F:\\WebApplication3\\src\\java\\data\\HERDER_1000.xml";
    
    String IN = "F:\\Dropbox\\IDS\\ZuMult\\Interjektionen-zum-Ausfiltern.csv";
    String OUT = "F:\\Dropbox\\IDS\\ZuMult\\FOLK_NGIRR_OHNE_DEREWO.xml";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Txt2TokenList().doit();
        } catch (IOException ex) {
            Logger.getLogger(Txt2TokenList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException {
        java.io.InputStream is = new FileInputStream(new File(IN));
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String nextLine = new String();
        //TokenList tokenList = new DefaultTokenList("n");
        TokenList tokenList = new DefaultTokenList("lemma");
        while ((nextLine = br.readLine()) != null){
            if (nextLine.trim().length()>0){
                tokenList.add(nextLine.trim());
            }
        }
        br.close(); 
        String xml = tokenList.toXML();
        
        FileOutputStream fos = new FileOutputStream(OUT);
        fos.write(xml.getBytes("UTF-8"));
        fos.close();                
        
    }
    
}
