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
public interface AdditionalMaterial extends Identifiable, Metadatable {
    public static enum FILE_TYPE {
        PDF, TXT
    };

    public FILE_TYPE getType();
    public String getURL();
    
}
