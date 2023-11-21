/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.zumult.backend.Configuration;
import org.zumult.io.MediaUtilities;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2Media extends AbstractMedia {

    
    public DGD2Media(String id, String urlString){
        super(id, urlString);
    }
    
    @Override
    public MEDIA_TYPE getType() {
        if (getID().contains("_V_")) return MEDIA_TYPE.VIDEO;
        return MEDIA_TYPE.AUDIO;
    }
    
    @Override
    public Media getPart(double startInSeconds, double endInSeconds) {
        String[] idAndUrl = cut(startInSeconds, endInSeconds);
        return new DGD2Media(idAndUrl[0], idAndUrl[1]);
    }
    
    @Override
    public Media getVideoImage(double positionInSeconds) {
        String[] idAndUrl = still(positionInSeconds);
        if (idAndUrl!=null){
            return new DGD2Media(idAndUrl[0], idAndUrl[1]);            
        }
        return null;
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public MediaUtilities getMediaUtilities(){
        return new MediaUtilities(Configuration.getFfmpegPath());
    }
    
    




    
}
