/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.objects.implementations;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.jdom.Element;
import org.w3c.dom.Document;
import org.zumult.backend.Configuration;
import org.zumult.io.IOHelper;
import org.zumult.io.IOUtilities;
import org.zumult.objects.CrossQuantification;
import org.zumult.objects.MetadataKey;

/**
 *
 * @author Frick
 */
public class DGD2CrossQuantification extends AbstractXMLObject implements CrossQuantification {
    
    public DGD2CrossQuantification(String xmlString) {
        super(xmlString);
    }
    

}
