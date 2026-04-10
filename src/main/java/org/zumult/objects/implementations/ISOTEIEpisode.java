/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import java.util.Arrays;
import org.w3c.dom.Document;
import org.zumult.objects.Episode;
import org.zumult.objects.IDList;

/**
 *
 * @author bernd
 */
public class ISOTEIEpisode extends AbstractXMLObject implements Episode {

    String name;
    String from;
    String to;
    String description;
    IDList restrictionSpeakerIDs;

    public ISOTEIEpisode(Document xmlDocument, String name) {
        super(xmlDocument);
        getVariables();
        this.name = name;
    }

    public ISOTEIEpisode(String xmlString, String name) {
        super(xmlString);
        getVariables();
        this.name = name;
    }
    

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public IDList getRestrictionSpeakerIDs() {
        return restrictionSpeakerIDs;
    }

    private void getVariables() {
        /*
            <span from="ts32" to="ts760"
                select="manv_2017_e.NA-01 manv_2017_e.NFS-01 manv_2017_e.PAT-01"
                >2017_09_E_TR-01_NA-01+NFS-01_PAT-01_ErS_G</span>
        
        */
        
        from = getDocument().getDocumentElement().getAttribute("from");
        to = getDocument().getDocumentElement().getAttribute("from");
        description = getDocument().getDocumentElement().getTextContent();
        String select = getDocument().getDocumentElement().getAttribute("select");
        restrictionSpeakerIDs = new IDList(name);
        if (select!=null){
            String[] selects = select.split(" ");
            restrictionSpeakerIDs.addAll(Arrays.asList(selects));
        }
    }

    
}
