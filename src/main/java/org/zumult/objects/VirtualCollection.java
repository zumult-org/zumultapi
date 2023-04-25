/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.objects;

/**
 *
 * @author thomas.schmidt
 */
public interface VirtualCollection extends Identifiable, XMLSerializable{
    
    public String getOwner();
    public String getName();
    
    public void setID(String id);

    public String getDescription();
    public void setDescription(String description);
    
    public void addVirtualCollectionItem(VirtualCollectionItem collectionItem);
    
}
