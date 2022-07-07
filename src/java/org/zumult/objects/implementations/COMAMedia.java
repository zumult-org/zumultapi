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
public class COMAMedia extends AbstractMedia {

    
    public COMAMedia(String id, String urlString){
        super(id, urlString);
    }

    @Override
    public MEDIA_TYPE getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Media getPart(double startInSeconds, double endInSeconds) {
        String[] idAndUrl = cut(startInSeconds, endInSeconds);
        return new COMAMedia(idAndUrl[0], idAndUrl[0]);
    }
    
    @Override
    public Media getVideoImage(double positionInSeconds) {
        String[] idAndUrl = still(positionInSeconds);
        if (idAndUrl!=null){
            return new COMAMedia(idAndUrl[0], idAndUrl[0]);            
        }
        return null;
    }


    
    
}
