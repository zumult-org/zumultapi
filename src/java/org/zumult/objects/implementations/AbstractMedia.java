/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.io.MediaUtilities;
import org.zumult.objects.Media;

/**
 *
 * @author thomasschmidt
 */
public abstract class AbstractMedia implements Media {

    String id;
    String urlString;

    public AbstractMedia(String id, String urlString) {
        this.id = id;
        this.urlString = urlString;
    }
    
    

    String[] cut(double startInSeconds, double endInSeconds) {
        String thisUrlString = "";
        String thisID = getID() + "_" + UUID.randomUUID();
        switch (getType()) {
            case VIDEO:
                //File outputFile = new File(new File(Configuration.getMediaSnippetsPath()), thisID + ".mp4");
                try {
                    File outputFile = File.createTempFile(getID() + "_", ".mp4");
                    // issue #70
                    outputFile.deleteOnExit();
                    MediaUtilities.cutVideo(startInSeconds, endInSeconds, getURL(), outputFile.getAbsolutePath());
                    thisUrlString = outputFile.getAbsolutePath();
                } catch (IOException ex) {
                    Logger.getLogger(DGD2Media.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case AUDIO:
                //File outputFile2 = new File(new File(Configuration.getMediaSnippetsPath()), thisID + ".wav");
                try {
                    File outputFile2 = File.createTempFile(getID() + "_", ".wav");
                    // issue #70
                    outputFile2.deleteOnExit();
                    MediaUtilities.cutAudio(startInSeconds, endInSeconds, getURL(), outputFile2.getAbsolutePath());
                    thisUrlString = outputFile2.getAbsolutePath();
                } catch (IOException ex) {
                    Logger.getLogger(DGD2Media.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
        String[] returnValue = {thisID, thisUrlString};
        return returnValue;        
    }
    
    String[] still(double positionInSeconds){
        try {
            String thisUrlString = "";
            String thisID = getID() + "_" + UUID.randomUUID();
            switch (getType()) {
                case VIDEO:
                    File outputFile = File.createTempFile(getID() + "_", ".png");
                    // issue #70
                    outputFile.deleteOnExit();
                    MediaUtilities.getVideoImage(positionInSeconds, getURL(), outputFile.getAbsolutePath());
                    thisUrlString = outputFile.getAbsolutePath();
                    String[] returnValue = {thisID, thisUrlString};
                    return returnValue;        
            }
        } catch (IOException ex) {
            Logger.getLogger(DGD2Media.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }
    


    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getURL() {
        return urlString;
    }

    @Override
    public double getDuration() {
        return MediaUtilities.getMediaDuration(getURL());
    }
    
}
