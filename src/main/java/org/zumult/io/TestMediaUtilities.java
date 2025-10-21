/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package org.zumult.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bernd
 */
public class TestMediaUtilities {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestMediaUtilities().doit();
        } catch (IOException ex) {
            Logger.getLogger(TestMediaUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException {
        MediaUtilities mu = new MediaUtilities("");
        mu.cutAudio(1, 20, "https://cocoon.huma-num.fr/data/eslo/masters/ESLO1_CONF_502.wav", "C:\\Users\\bernd\\OneDrive\\Desktop\\TEST.wav");
    }
    
}
