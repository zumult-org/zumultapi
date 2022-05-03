/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

/**
 *
 * @author josip.batinic
 */
public interface TranscriptMetadata extends XMLSerializable, Identifiable {
//    public String getType();
    public String getDuration();
    public String getTypes();
    public String getTokens();
    public String getTranscriptFileSizeInBytes();
//    etc
}
