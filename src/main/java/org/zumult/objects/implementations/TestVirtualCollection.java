/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects.implementations;

import org.zumult.backend.implementations.FileSystemVirtualCollectionStore;
import org.zumult.objects.VirtualCollection;

/**
 *
 * @author thomas.schmidt
 */
public class TestVirtualCollection {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new TestVirtualCollection().doit();
    }

    private void doit() throws Exception {
        VirtualCollection vc = new ZumultVirtualCollection(null, "TestCollection", "thomas.schmidt@ids-mannheim.de");
        vc.setDescription("Eine Kollektion zum Träumen!");
        /*vc.addVirtualCollectionItem(new VirtualCollectionItemTranscriptExcerpt("FOLK_E_00001_SE_01_T_01", "c1", "c14"));
        vc.addVirtualCollectionItem(new VirtualCollectionItemTranscriptExcerpt("FOLK_E_00007_SE_01_T_01", "c7", "c21"));*/
        
        FileSystemVirtualCollectionStore store = new FileSystemVirtualCollectionStore();
        String id = store.addVirtualCollection(vc);
        System.out.println("Added as " + id);
        VirtualCollection vc2 = store.getVirtualCollection(id);
        System.out.println(vc2.toXML());
        
        for (String idx : store.listVirtualCollections("thomas.schmidt@ids-mannheim.de")){
            System.out.println(idx);
        }
        
    }
    
}
