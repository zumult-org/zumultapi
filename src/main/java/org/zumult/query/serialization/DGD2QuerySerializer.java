/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.serialization;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.zumult.query.KWIC;
import org.zumult.io.FileIO;
import org.zumult.query.implementations.DGDSearchIndexType.DGD2SearchIndexTypeEnum;

/**
 *
 * @author Frick
 */
public class DGD2QuerySerializer extends DefaultQuerySerializer {
    
    @Override
    public String displayKWICinXML(KWIC obj) {
        String str = super.displayKWICinXML(obj);
        if (obj.getSearchMode().startsWith(
                DGD2SearchIndexTypeEnum.SPEAKER_BASED_INDEX.name())){           
            try {
                Document document = FileIO.readDocumentFromString(str);
                document.getRootElement()
                        .getChild(DefaultQuerySerializer.META_PART)
                        .getChild(DefaultQuerySerializer.TOTAL_TRANSCRIPTS)
                        .setText(String.valueOf(-1)); 
                str = FileIO.getDocumentAsString(document);
            } catch (JDOMException | IOException ex) {
                Logger.getLogger(DGD2QuerySerializer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        } 
        return str;
    }
}
