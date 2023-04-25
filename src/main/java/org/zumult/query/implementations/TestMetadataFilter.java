/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import java.io.IOException;
import net.sf.saxon.lib.NamespaceConstant;
import org.zumult.objects.IDList;
import org.zumult.query.MetadataFilter;

/**
 *
 * @author thomas.schmidt
 */
public class TestMetadataFilter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new TestMetadataFilter().doit();
    }

    private void doit() throws IOException {
        MetadataFilter metadataFilter = new DGDMetadataFilter();
        //IDList result = metadataFilter.filterSpeechEvents("FOLK", "e_region_wiesinger", "Rhein.*");
        IDList result = metadataFilter.filterSpeechEvents("FOLK", "measure_lemma_tokens", 0, 500);
        for (String id : result){
            System.out.println(id);
        }
    }
    
}
