/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.zumult.objects.AdditionalMaterial;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author josip.batinic
 */
public class DGD2AdditionalMaterial implements AdditionalMaterial {

    String id;
    String urlString;

    public DGD2AdditionalMaterial(String id, String urlString) {
        this.id = id;
        this.urlString = urlString;
    }
    
    @Override
    public FILE_TYPE getType() {
        if (getURL().endsWith(".txt")) {
            return FILE_TYPE.TXT;
        }
        return FILE_TYPE.PDF;    }

    @Override
    public String getURL() {
        return urlString;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getMetadataValue(MetadataKey key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
