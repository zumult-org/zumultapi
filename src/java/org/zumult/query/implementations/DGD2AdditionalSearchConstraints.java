/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.w3c.dom.Document;
import org.zumult.objects.implementations.AbstractXMLObject;
import org.zumult.query.AdditionalSearchConstraints;

/**
 *
 * @author Frick
 */
public class DGD2AdditionalSearchConstraints extends AbstractXMLObject implements AdditionalSearchConstraints {
    
    public DGD2AdditionalSearchConstraints(Document xmlDocument) {
        super(xmlDocument);
    }

    DGD2AdditionalSearchConstraints(String repetitionsStr) {
        super(repetitionsStr);
    }
}
