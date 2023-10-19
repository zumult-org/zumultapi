/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.implementations;

import org.w3c.dom.Document;
import org.zumult.objects.implementations.AbstractXMLObject;
import org.zumult.query.AdditionalSearchConstraint;

/**
 *
 * @author Frick
 */
public class DafaultAdditionalSearchConstraint extends AbstractXMLObject implements AdditionalSearchConstraint {
    
    public DafaultAdditionalSearchConstraint(Document xmlDocument) {
        super(xmlDocument);
    }

    DafaultAdditionalSearchConstraint(String repetitionsStr) {
        super(repetitionsStr);
    }
}
