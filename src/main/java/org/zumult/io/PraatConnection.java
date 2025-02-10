/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zumult.backend.Configuration;

/**
 *
 * @author bernd
 */
public class PraatConnection {

    String PRAAT_PATH = "C:\\Program Files\\Praat\\Praat.exe";
    String WRITE_PITCH_SCRIPT_PATH = "/org/zumult/io/ListPitch.praat";
    String DRAW_PITCH_SCRIPT_PATH = "/org/zumult/io/DrawPitch.praat";
    

    public PraatConnection() {
        PRAAT_PATH = Configuration.getPraatPath();
    }
    
    
    public void drawPitch(File audio, File textGrid, File pngOut) throws IOException {
        File tempScript = writeTempScript(DRAW_PITCH_SCRIPT_PATH);
        
        ProcessBuilder pb = new ProcessBuilder(
                PRAAT_PATH, 
                tempScript.getAbsolutePath(), 
                audio.getAbsolutePath(), 
                textGrid.getAbsolutePath(),
                pngOut.getAbsolutePath());
        System.out.println(pb.command());
        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(PraatConnection.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }                
    }
    
    
    
    
    public String getPitchAsXML(File audio) throws IOException {
        
        File tempScript = writeTempScript(WRITE_PITCH_SCRIPT_PATH);
        File pitchOut = File.createTempFile("pitch", ".txt");
        
        // 10-02-2025, added flag for #237
        ProcessBuilder pb = new ProcessBuilder(PRAAT_PATH, "--no-pref-window", tempScript.getAbsolutePath(), audio.getAbsolutePath(), pitchOut.getAbsolutePath());
        System.out.println(pb.command());
        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(PraatConnection.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }        
        
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(pitchOut))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement("pitches");
            document.appendChild(root);

            String line = null;
            int count = 0;
            while ((line = TSVReader.readLine()) != null) {
                if (count==0){
                    count++;
                    continue;
                }
                String[] lineItems = line.split(","); //splitting the line and adding its items in String[]
                Element pitch = document.createElement("pitch");
                pitch.setAttribute("time", lineItems[0]);
                pitch.setAttribute("pitch", lineItems[1]);
                root.appendChild(pitch);            
                
                count++;
            }
            return IOHelper.DocumentToString(document);
        } catch (ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(PraatConnection.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }

    private File writeTempScript(String internalPath) throws IOException {
        // Get resource as InputStream
        InputStream resourceStream = PraatConnection.class.getResourceAsStream(internalPath);
        if (resourceStream == null) {
            throw new FileNotFoundException("Resource not found: " + internalPath);
        }

        // Create a temporary file
        File tempFile = File.createTempFile("zumult-praat-script", ".praat");
        tempFile.deleteOnExit(); // Ensure temp file is deleted on JVM exit

        // Copy resource to temp file
        try (OutputStream tempFileStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                tempFileStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    
}
