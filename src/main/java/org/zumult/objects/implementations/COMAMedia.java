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
 * @author thomas.schmidt
 */
public class COMAMedia extends AbstractMedia {

    
    String fileString;
    
    public COMAMedia(String id, String urlString){
        super(id, urlString);
    }
        

    public COMAMedia(String id, String urlString, String fileString){
        super(id, urlString);
        this.fileString = fileString;
    }
    
    public String getFileString(){
        return fileString;
    }

    @Override
    public MEDIA_TYPE getType() {
        String url = super.getURL();
        int index = url.lastIndexOf(".");
        String suffix = url.substring(index+1).toLowerCase();
        switch(suffix){
            case "mp3" :
            case "wav" : 
                return MEDIA_TYPE.AUDIO;
            default :
                return MEDIA_TYPE.VIDEO;
        }
    }
    
    @Override
    public Media getPart(double startInSeconds, double endInSeconds) {
        String[] idAndUrl = cut(startInSeconds, endInSeconds);
        return new COMAMedia(idAndUrl[0], idAndUrl[1]);
    }
    
    @Override
    public Media getVideoImage(double positionInSeconds) {
        String[] idAndUrl = still(positionInSeconds); 
        if (idAndUrl!=null){
            return new COMAMedia(idAndUrl[0], idAndUrl[1]);            
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
