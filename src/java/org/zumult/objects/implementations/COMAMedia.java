/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.zumult.objects.Media;

/**
 *
 * @author thomas.schmidt
 */
public class COMAMedia implements Media {

    String id;    
    String urlString;
    
    public COMAMedia(String id, String urlString){
        this.id = id;
        this.urlString = urlString;
    }

    @Override
    public MEDIA_TYPE getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getURL() {
        return urlString;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public Media getPart(double startInSeconds, double endInSeconds) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getDuration() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Media getVideoImage(double positionInSeconds) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
