/*
 * IOUtilities.java
 *
 * Created on 12. Oktober 2006, 12:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.zumult.io;

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.zumult.objects.implementations.MediaStreamer;

/**
 *
 * @author thomas
 */
public class IOUtilities {

    
    /** Creates a new instance of IOUtilities */
    public IOUtilities() {
    }
    
    public static Document readDocumentFromLocalFile(String path) throws JDOMException, IOException {
        File file = new File(path);
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(file);        
        return doc;
    }
    
    public static Document readDocumentFromURL(URL url) throws JDOMException, IOException{
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(url);
        return doc;
    }
    
    public static Document readDocumentFromString(String docString) throws JDOMException, IOException{
        SAXBuilder saxBuilder = new SAXBuilder();
        java.io.StringReader sr = new java.io.StringReader(docString);
        Document doc = saxBuilder.build(sr);        
        return doc;        
    }
        
    public static Element readElementFromString(String elementString) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        java.io.StringReader sr = new java.io.StringReader(elementString);
        Document doc = saxBuilder.build(sr);        
        return doc.detachRootElement();        
    }
    
    
    public Document readDocumentFromResource(String pathToResource) throws JDOMException, IOException{
        SAXBuilder saxBuilder = new SAXBuilder();
        java.io.InputStream is = getClass().getResourceAsStream(pathToResource);
        Document doc = saxBuilder.build(is);
        return doc;
    }

    public static void writeDocumentToLocalFile(String pathToDocument, Document document) throws IOException{
        XMLOutputter xmlOutputter = new XMLOutputter();
        //String docString = xmlOutputter.outputString(document);        
        FileOutputStream fos = new FileOutputStream(new File(pathToDocument));        
        xmlOutputter.output(document,fos);
        //fos.write(docString.getBytes("UTF-8"));
        fos.close();    
    }

    public static void writeDocumentToLocalFile(String pathToDocument, Document document, boolean omitXMLDeclaration) throws IOException{
        XMLOutputter xmlOutputter = new XMLOutputter();
        FileOutputStream fos = new FileOutputStream(new File(pathToDocument));
        xmlOutputter.setFormat(xmlOutputter.getFormat().setOmitDeclaration(omitXMLDeclaration));

        xmlOutputter.output(document,fos);
        fos.close();
    }


    public static String documentToString(Document document){
        XMLOutputter xmlOutputter = new XMLOutputter();
        return xmlOutputter.outputString(document);
    }

    public static String documentToString(Document document, boolean omitXMLDeclaration){
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(xmlOutputter.getFormat().setOmitDeclaration(omitXMLDeclaration));
        return xmlOutputter.outputString(document);
    }
    
    public static String documentToString(org.w3c.dom.Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    public static String elementToString(Element element){
        return IOUtilities.elementToString(element, false);
    }
    public static String elementToString(Element element, boolean prettyPrint){
        XMLOutputter xmlOutputter = new XMLOutputter();
        if (prettyPrint) {
            xmlOutputter.setFormat(Format.getPrettyFormat());
        }
        return xmlOutputter.outputString(element);
    }
    
    public static String elementToString(org.w3c.dom.Element el) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(el), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }
    
    public static void schemaValidateLocalFile(File file, String schemaLocation) throws JDOMException, IOException{
        SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
        String schemaLocString = new File(schemaLocation).toURI().toURL().toString();
        saxBuilder.setFeature("http://apache.org/xml/features/validation/schema", true);
        saxBuilder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
                schemaLocString);
        saxBuilder.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                schemaLocString);
        Document doc = saxBuilder.build(file);                
    }
    
    // This is the same as above, but without the output flags set
    /*public static String documentToString(org.w3c.dom.Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        
        return null;
    }*/
    
    public org.w3c.dom.Document documentFromResource(String pathToResource) throws ParserConfigurationException, SAXException, IOException {
                
        InputStream is = getClass().getClassLoader().getResourceAsStream(pathToResource);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.parse(is);
        return doc;
    }
   
    public static org.w3c.dom.Document documentFromLocalFile(String path) throws ParserConfigurationException, SAXException, IOException {
        File inputFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.parse(inputFile);
        return doc;
    }
    
    public static Response buildStream(final File asset, final String range) throws Exception {
        // range not requested : Firefox, Opera, IE do not send range headers
        if (range == null) {
            StreamingOutput streamer = new StreamingOutput() {
                @Override
                public void write(final OutputStream output) throws IOException, WebApplicationException {

                    final FileChannel inputChannel = new FileInputStream(asset).getChannel();
                    final WritableByteChannel outputChannel = Channels.newChannel(output);
                    try {
                        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                    } finally {
                        // closing the channels
                        inputChannel.close();
                        outputChannel.close();
}
                }
            };
            return Response.ok(streamer).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
        }

        String[] ranges = range.split("=")[1].split("-");
        final int from = Integer.parseInt(ranges[0]);
        /**
         * Chunk media if the range upper bound is unspecified. Chrome sends
         * "bytes=0-"
         */
        final int chunk_size = 1024 * 1024; // 1MB chunks
        int to = chunk_size + from;
        if (to >= asset.length()) {
            to = (int) (asset.length() - 1);
        }
        if (ranges.length == 2) {
            to = Integer.parseInt(ranges[1]);
        }

        final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
        final RandomAccessFile raf = new RandomAccessFile(asset, "r");
        raf.seek(from);

        final int len = to - from + 1;
        final MediaStreamer streamer = new MediaStreamer(len, raf);
        Response.ResponseBuilder res = Response.status(Response.Status.PARTIAL_CONTENT).entity(streamer)
                .header("Accept-Ranges", "bytes")
                .header("Content-Range", responseRange)
                .header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
                .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
        return res.build();
    }
    
    /*public org.w3c.dom.Document documentFromResource(String pathToResource) throws ParserConfigurationException, SAXException, IOException {
                
        InputStream is = getClass().getClassLoader().getResourceAsStream(pathToResource);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.parse(is);
        return doc;
    }*/
    
}
