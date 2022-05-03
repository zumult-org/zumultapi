
package org.zumult.globalServices;

import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TransformService {

    public TransformService() {
    }
    
    public String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            javax.xml.transform.Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
            t.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }

    public String nodeListToString(NodeList nodes) {
        StringWriter sw = new StringWriter();
        try {
            javax.xml.transform.Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            for (int i = 0; i < nodes.getLength(); i++) {
                t.transform(new DOMSource(nodes.item(i)), new StreamResult(sw));
            }
            t.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }
}
