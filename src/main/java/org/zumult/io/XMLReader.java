/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.objects.TokenList;
import org.zumult.objects.implementations.DefaultTokenList;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Thomas_Schmidt
 */
public class XMLReader {
    
    public static TokenList readTokenListFromInternalResource(String pathToInternalResource) throws IOException, SAXException, ParserConfigurationException{
        String xml = IOHelper.readUTF8(XMLReader.class.getResourceAsStream(pathToInternalResource));
        return xmlString2TokenList(xml);
    }
    
    public static TokenList readTokenListFromFile(File file) throws IOException, SAXException, ParserConfigurationException{
        String xml = IOHelper.readUTF8(file);
        return xmlString2TokenList(xml);
    }
    
    private static TokenList xmlString2TokenList(String xml) throws IOException, SAXException, ParserConfigurationException{
        Document doc = IOHelper.DocumentFromText(xml);
        TokenList result = new DefaultTokenList(doc.getDocumentElement().getAttribute("type"));
        NodeList childNodes = doc.getElementsByTagName("token");
        for (int i=0; i<childNodes.getLength(); i++){
            Element element = (Element)(childNodes.item(i));
            String form = element.getAttribute("form");
            Integer frequency = Integer.parseInt(element.getAttribute("frequency"));
            result.put(form, frequency);
        }
        return result;        
    }
    
    public static String format(String xml) throws Exception {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);

        Source xmlInput = new StreamSource(new StringReader(xml));
        transformer.transform(xmlInput, xmlOutput);

        return xmlOutput.getWriter().toString();
    }
    
    

}
