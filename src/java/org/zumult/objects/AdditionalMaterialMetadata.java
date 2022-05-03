/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

import java.util.ArrayList;
import org.w3c.dom.Document;

/**
 *
 * @author josip.batinic
 */
public interface AdditionalMaterialMetadata extends XMLSerializable, Identifiable {
    
    public int getAmountOfAdditionalMaterial();
    public Document getAdditionalMaterialMetadataByIndex(int indexOfMaterial);
    public ArrayList<String> getListOfCategories();
    public String getCategoryByIndex(int indexOfMaterial);
    public String getFormatByIndex(int indexOfMaterial);
    
}
