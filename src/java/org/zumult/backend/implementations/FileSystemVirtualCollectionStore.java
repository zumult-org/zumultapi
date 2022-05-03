/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import org.zumult.backend.VirtualCollectionStore;
import org.zumult.io.IOHelper;
import org.zumult.objects.IDList;
import org.zumult.objects.VirtualCollection;
import org.zumult.objects.implementations.ZumultVirtualCollection;

/**
 *
 * @author thomas.schmidt
 */
public class FileSystemVirtualCollectionStore implements VirtualCollectionStore {

    File dir = new File("D:\\Dropbox\\IDS\\ZuMult\\VirtualCollectionStore");
    
    @Override
    public String addVirtualCollection(VirtualCollection virtualCollection) {
        try {
            String id = UUID.randomUUID().toString();
            if (virtualCollection.getID()!=null){
                id = virtualCollection.getID();
            } else {
                virtualCollection.setID(id);
            }
            
            // write it
            File writeFile = new File(dir, id);
            FileOutputStream fos = new FileOutputStream(writeFile);
            fos.write(virtualCollection.toXML().getBytes("UTF-8"));
            fos.close();
            return id;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FileSystemVirtualCollectionStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(FileSystemVirtualCollectionStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public VirtualCollection getVirtualCollection(String id) {
        try {
            String collectionXML = IOHelper.readUTF8(new File(dir, id));
            VirtualCollection vs = new ZumultVirtualCollection(collectionXML);
            return vs;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileSystemVirtualCollectionStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileSystemVirtualCollectionStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(FileSystemVirtualCollectionStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(FileSystemVirtualCollectionStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(FileSystemVirtualCollectionStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public IDList listVirtualCollections(String username) {
        IDList idList = new IDList("virtual collections");
        /*File[] files = dir.listFiles();
        for (File file : files){
            idList.add(file.getName());
        }*/
        idList.add("Kollektion_1");
        idList.add("Kollektion_2");
        return idList;
    }

    @Override
    public IDList listVirtualCollections(String username, String type) {
        return listVirtualCollections(username);
    }
    
}
