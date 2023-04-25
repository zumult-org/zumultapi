/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend;

import org.zumult.objects.IDList;
import org.zumult.objects.VirtualCollection;

/**
 *
 * @author thomas.schmidt
 */
public interface VirtualCollectionStore {
    
    /**
     *
     * @param virtualCollection
     * @return
     */
    public String addVirtualCollection(VirtualCollection virtualCollection);

    /**
     *
     * @param id
     * @return
     */
    public VirtualCollection getVirtualCollection(String id);

    /**
     *
     * @param username
     * @return
     */
    public IDList listVirtualCollections(String username);

    /**
     *
     * @param username
     * @param type
     * @return
     */
    public IDList listVirtualCollections(String username, String type);
    
}
