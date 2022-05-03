/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

/**
 *
 * @author Thomas_Schmidt
 */
public interface Media extends Identifiable {
    
    public static enum MEDIA_TYPE {AUDIO, VIDEO, IMAGE};
    public static enum MEDIA_FORMAT {WAV, MP3, MPEG4_ARCHIVE, MPEG4_WEB, JPG, PNG};
    public static enum IMAGE_SIZE {SMALL, LARGE};
        
    public MEDIA_TYPE getType();
    public String getURL();
    public Media getPart(double startInSeconds, double endInSeconds);
    public Media getVideoImage(double positionInSeconds);
    public double getDuration();
    
    
}
