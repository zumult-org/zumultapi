package org.zumult.globalServices;

import java.io.StringReader;
import javax.ws.rs.core.UriInfo;
import org.zumult.globalServices.TransformService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jdom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class GlobalServices {

    public GlobalServices() {
    }
    
    public String toTitleCase(String s) {
        final String ACTIONABLE_DELIMITERS = " '-_/"; // these cause the character following to be capitalized
        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString().replaceAll("[ '/]", "_");
    }
    
    public String getRootString(String xml) {
        StringBuilder sb = new StringBuilder();
        TransformService ts = new TransformService();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(new InputSource(new StringReader(xml)));
            Node theRoot = xmlDoc.getDocumentElement().cloneNode(true);
            sb.append(ts.nodeToString(theRoot));
        } catch (Exception e) {
            System.out.println(e);
        }
        return sb.toString();
    }
    
    public String getElementString(String xml, String elementName) {
        StringBuilder sb = new StringBuilder();
        TransformService ts = new TransformService();
        try {
            System.out.println("xml: " + xml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(new InputSource(new StringReader(xml)));
//            System.out.println("ADADADADAD" + toTitleCase(elementName));

            NodeList elements = xmlDoc.getElementsByTagName(toTitleCase(elementName));
            sb.append(ts.nodeListToString(elements));
        } catch (Exception e) {
            System.out.println(e);
        }
        return sb.toString();
    }
    
    public Element xmlError(UriInfo context) {
        Element error = new Element("error");
        Element message = new Element("message");
        Element homeUrl = new Element("home_url");

        message.addContent("Requested document does not exist");
        homeUrl.addContent(context.getBaseUri().toString());
        error.addContent(message);
        error.addContent(homeUrl);

        return error;
    }
}
