/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author Thomas_Schmidt
 */
public class ISOTEINamespaceContext implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) return Constants.TEI_NAMESPACE_URL;
        else if ("tei".equals(prefix)) return Constants.TEI_NAMESPACE_URL;
        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
        return XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
