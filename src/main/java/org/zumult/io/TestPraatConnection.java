/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package org.zumult.io;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bernd
 */
public class TestPraatConnection {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TestPraatConnection().doit();
    }

    private void doit() {
        try {
            File wavFile = new File("C:\\apache-tomcat-9.0.88\\temp\\MID8FED5BE3-D35F-691E-37CC-F420DA31913D_b660a5d5-2a6e-4b49-be9e-9859d035e4ed9550961822527993795.wav");
            PraatConnection pc = new PraatConnection();
            String xml = pc.getPitchAsXML(wavFile);
            System.out.println(xml);
        } catch (IOException ex) {
            Logger.getLogger(TestPraatConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
