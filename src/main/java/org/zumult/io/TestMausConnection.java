/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package org.zumult.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;

/**
 *
 * @author bernd
 */
public class TestMausConnection {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestMausConnection().doit();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | JDOMException ex) {
            Logger.getLogger(TestMausConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, JDOMException {
        MausConnection mausConnection = new MausConnection(null);
        for (int i=0; i<20; i++){
            long start = System.currentTimeMillis();
            String mausAligment = mausConnection.getMausAligment("Beckhams", "au1_seg2", "EXB");
            long end = System.currentTimeMillis();
            double time = (end - start) / 1000.0;
            System.out.println(time + " seconds");
        }
    }
    
}
