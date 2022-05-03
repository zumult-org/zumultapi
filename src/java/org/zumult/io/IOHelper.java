/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.implementations.DGD2AnnotationLayer;
import org.zumult.query.SampleQuery;
import org.zumult.query.implementations.DGD2SampleQuery;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.zumult.objects.AnnotationTypeEnum;
import org.zumult.objects.MetadataKey;
import org.zumult.query.SearchServiceException;

/**
 *
 * @author Thomas_Schmidt
 */
public class IOHelper {
    
    public static String DocumentToString(Document xmlDocument) throws TransformerConfigurationException, TransformerException{
        DOMSource domSource = new DOMSource(xmlDocument);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sw.toString();        
    }

    public static String ElementToString(Element xmlElement) throws TransformerConfigurationException, TransformerException{
        DOMSource domSource = new DOMSource(xmlElement);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sw.toString();        
    }

    
    public static Document DocumentFromText(String xml) throws IOException, SAXException, ParserConfigurationException{
        InputStream inputStream = new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"));
        DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        inputStream.close();
        return doc;
    }

    public static String readUTF8(File file) throws FileNotFoundException {
        return new Scanner(new FileInputStream(file), "UTF-8").useDelimiter("\\A").next();    
    }
    
    public static void writeUTF8(File file, String text) throws FileNotFoundException, IOException{
        OutputStreamWriter writer =
             new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writer.write(text);
        writer.close();
    }
    
    public static Document readDocument(File file) throws IOException, SAXException, ParserConfigurationException{
        return DocumentFromText(readUTF8(file));
    }
    
    
    public static void writeDocument(Document document, File file) throws TransformerException, IOException{
        Files.write(file.toPath(), DocumentToString(document).getBytes("UTF-8"));        
    }
    
        
    public String applyInternalStylesheetToString(String pathToStylesheet, String xml) throws TransformerException{
        StreamSource stylesource = new StreamSource(getClass().getResourceAsStream(pathToStylesheet)); 

        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource);  
        //DOMSource source = new DOMSource(doc);
        StreamSource source = new StreamSource(new StringReader(xml));
        // can't use DOMResult when XSLT has 'text" as output?
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        //DOMResult result = new DOMResult();
        transformer.transform(source, result);
        //Document transformedDoc = (Document) result.getNode();                    
        //return IOHelper.DocumentToString(transformedDoc);
        return writer.toString();
    }

    public String applyInternalStylesheetToInternalFile(String pathToStylesheet, String pathToXml) throws TransformerException{
        StreamSource stylesource = new StreamSource(getClass().getResourceAsStream(pathToStylesheet)); 

        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource);  
        //DOMSource source = new DOMSource(doc);
        StreamSource source = new StreamSource(getClass().getResourceAsStream(pathToXml));
        // can't use DOMResult when XSLT has 'text" as output?
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        //DOMResult result = new DOMResult();
        transformer.transform(source, result);
        //Document transformedDoc = (Document) result.getNode();                    
        //return IOHelper.DocumentToString(transformedDoc);
        return writer.toString();
    }

    public String applyInternalStylesheetToInternalFile(String pathToStylesheet, String pathToXml, Object[][] parameters) throws TransformerException{
        StreamSource stylesource = new StreamSource(getClass().getResourceAsStream(pathToStylesheet)); 

        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource); 
        for (Object[] parameterPair : parameters){
            transformer.setParameter((String) parameterPair[0], parameterPair[1]);
            
        }
        
        //DOMSource source = new DOMSource(doc);
        StreamSource source = new StreamSource(getClass().getResourceAsStream(pathToXml));
        // can't use DOMResult when XSLT has 'text" as output?
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        //DOMResult result = new DOMResult();
        transformer.transform(source, result);
        //Document transformedDoc = (Document) result.getNode();                    
        //return IOHelper.DocumentToString(transformedDoc);
        return writer.toString();
    }


    public String applyInternalStylesheetToString(String pathToStylesheet, String xml, String[][] parameters) throws TransformerException{
        StreamSource stylesource = new StreamSource(getClass().getResourceAsStream(pathToStylesheet)); 

        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource);  
        for (String[] parameterPair : parameters){
            //System.out.println("Setting parameter " + parameterPair[0]);
            transformer.setParameter(parameterPair[0], parameterPair[1]);
        }
        //DOMSource source = new DOMSource(doc);
        StreamSource source = new StreamSource(new StringReader(xml));
        // can't use DOMResult when XSLT has 'text" as output?
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        //DOMResult result = new DOMResult();
        transformer.transform(source, result);
        //Document transformedDoc = (Document) result.getNode();                    
        //return IOHelper.DocumentToString(transformedDoc);
        return writer.toString();
    }
    
    public String readInternalResource(String pathToResource) throws IOException{
        InputStream in = getClass().getResourceAsStream(pathToResource); 
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8")); 
        String line;
        String content = "";
        while ((line = reader.readLine()) != null) {
            content+=line;
        }
        reader.close();
        in.close();            
        return content;
    }
    
    /*public static String getQueryXML(String queryString, String corpusQuery, Integer pageLength, Integer pageIndex) throws IOException {
        try {
            URL url = new URL(Configuration.getRestAPIBaseURL() 
                    + "/SearchService/kwic?q=" + queryString.replaceAll("\\+", "%2B").replaceAll(" & ", "%26").replaceAll("<", "%3C").replaceAll(">", "%3E").replaceAll(" ","%20").replaceAll("#","%23") 
                    + "&cq=" + corpusQuery
                    + "&count=" + pageLength
                    + "&offset=" + pageIndex); //your url i.e fetch data from .
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");
            conn.setRequestProperty("Accept-Charset", "UTF-8");             
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : "
                        + conn.getResponseCode());
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream(), "UTF-8");
            BufferedReader br = new BufferedReader(in);
            String output;
            String xml = "";
            while ((output = br.readLine()) != null) {
                xml+=output;
            }
            conn.disconnect();
            
            return xml;
        } catch (MalformedURLException ex) {
            Logger.getLogger(IOHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(IOHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } catch (IOException ex) {
            Logger.getLogger(IOHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }*/
    
    public static ArrayList<SampleQuery> getQueriesFromFile(String path) throws ParserConfigurationException, SAXException, IOException {
              
        ArrayList <SampleQuery> queries = new ArrayList();
        Document doc = new IOUtilities().documentFromResource(path);

        NodeList nList = doc.getElementsByTagName("query");
        
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                Element elem = (Element) nNode;
                DGD2SampleQuery query = new DGD2SampleQuery();
                String str = elem.getElementsByTagName("string").item(0).getTextContent();
                String description = elem.getElementsByTagName("description").item(0).getTextContent();
                String corpus = elem.getElementsByTagName("corpus").item(0).getTextContent();
                query.setQueryString(str);
                query.setDescription(description);
                query.setCorpus(corpus);
                queries.add(query);
            }
        return queries;
    }
    
    public static LinkedHashMap<String, String> getQueryStringsFromFile(String path) throws ParserConfigurationException, SAXException, IOException {
        LinkedHashMap<String, String> queryList = new LinkedHashMap<String, String>();
        ArrayList <SampleQuery> queries = getQueriesFromFile(path);
        for (SampleQuery query : queries){
            queryList.put(query.getQueryString(), query.getCorpus());
        }
        return queryList;        
    }
    
    public static File getProjectFile(String classPath) throws IOException{
        String regex = ".*WEB-INF"; 
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(classPath);
        if(m.find()){
            return (new File(classPath.substring(m.start(), m.end())).getAbsoluteFile()).getParentFile();
        }else{
            throw new IOException("Project file could not be found!");
        }
    }
    
    public static String IPA2HTML(String s){
        String result = s.replace("ɔ", "&#596;")
                .replace("ɛ", "&#603;")
                .replace("ʔ", "&#660;")
                .replace("ɪ", "&#618;")
                .replace("ʃ", "&#643;")
                .replace("ʊ", "&#650;")
                .replace("ə", "&#601;")
                .replace("ɐ", "&#592;")
                .replace("ː", "&#058;")
                .replace("ʏ", "&#655;")
                .replace("ŋ", "&#331;")
                .replace("ɡ", "&#609;");
                //.replace("ç", "&#231;");
        return result;
    }
    
    public static String HTML2IPA(String s){
        String result = s.replace("&#596;", "ɔ")
                .replace("&#603;", "ɛ")
                .replace("&#660;", "ʔ")
                .replace("&#618;", "ɪ")
                .replace("&#643;", "ʃ")
                .replace("&#650;", "ʊ")
                .replace("&#601;","ə")
                .replace("&#592;", "ɐ")
                .replace("&#058;", "ː")
                .replace("&#655;", "ʏ")
                .replace("&#331;", "ŋ")
                .replace("&#609;", "ɡ");
                //.replace("&#231;", "ç");
        return result;
    }
    
    public static Set<String> getCorporaIDsFromCorpusQuery(String corpusQuery){
        Pattern reg = Pattern.compile(Constants.CORPUS_SIGLE_PATTERN);
        Matcher match = reg.matcher(corpusQuery);
        Set<String> set = new HashSet();
        if (match.find( )) {
            String corpora = match.group(1).replace("\"", "").trim();
                if (corpora.contains("|")){
                    
                    String[] corporaIDs = corpora.split(Pattern.quote(Constants.CORPUS_DELIMITER));
                    for (String corpusID : corporaIDs){  
                    
                        set.add(corpusID.trim());
                    }
                    
                }else{
                    set.add(corpora.trim());
                }
        }
        return set;
        
    }
    
    public static String decodeUmlauts(String s){
         String result = s.replace("Ã„", "Ä")
                 .replace("Ã–", "Ö")
                 .replace("Ãœ", "Ü")
                 .replace("Ã¤", "ä")
                 .replace("Ã¶", "ö")
                 .replace("Ã¼", "ü")
                 .replace("ÃŸ", "ß")
                 .replace("Â€", "€")
                 .replace("%C3%9F", "ß")
                 .replace("%C3%96", "Ö");
         return result;
     }    
    
    
    public static Set<AnnotationLayer> getAnnotationLayersForCorpus(String corpusID, String annotationLayerType) {
        AnnotationTypeEnum annotationType = null;
        Set<AnnotationLayer> result = new HashSet<>();
        String path = "/data/ZuMultAvailableAnnotationValues.xml";
        String xPathString = "//available-values/corpus[@corpus=\""+corpusID+"\"]/key";
        XPath xPath = XPathFactory.newInstance().newXPath();
            
        try{
            // check annotation type exists
            if (annotationLayerType!=null){
                annotationType = AnnotationTypeEnum.valueOf(annotationLayerType.toUpperCase());
            }

            String xml = new Scanner(IOHelper.class.getResourceAsStream(path), "UTF-8").useDelimiter("\\A").next();
            Document doc = IOHelper.DocumentFromText(xml);    
            NodeList nodes = (NodeList)xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<nodes.getLength(); i++){               
                Element keyElement = ((Element)(nodes.item(i)));
                AnnotationTypeEnum type = AnnotationTypeEnum.valueOf(keyElement.getAttribute("type").toUpperCase());
                if (annotationType==null || annotationType.equals(type)){
                    
                    String id = keyElement.getAttribute("id");
                    
                    NodeList names = keyElement.getElementsByTagName("name");
                    Map<String, String> map = new HashMap();
                    for (int j=0; j<names.getLength(); j++){ 
                        Element nameElement = ((Element)(names.item(j)));
                        map.put(nameElement.getAttribute("lang"),nameElement.getTextContent());
                    }  

                    AnnotationLayer key = new DGD2AnnotationLayer(id, map, type);
                    result.add(key);
                }
            }
            
            return result;

        }catch (NullPointerException ex){
            StringBuilder sb = new StringBuilder();
            sb.append(". There is no annotation layer of the type ").append(annotationLayerType).append(". Supported types are: ");
                for (AnnotationTypeEnum ob : AnnotationTypeEnum.values()){
                        sb.append(ob.name());
                        sb.append(", ");
                    }
            throw new NullPointerException(sb.toString().trim().replaceFirst(",$",""));
        }catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
            Logger.getLogger(IOHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;

    }
    
    public static Map<String, String> sortMapByValue(Map<String, String> map){
        List<Map.Entry<String, String>> list = new ArrayList(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {                          
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        Map<String, String> sorted = new LinkedHashMap();
        
        for(Map.Entry<String, String> element : list) {
            sorted.put(element.getKey(), element.getValue());
        }
        
        return sorted;
    }
    
    public static void emptyDir(File dir){
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()){
                deleteDir(file);
            }else{
                file.delete();
            }
        }
    }
    
    public static void deleteDir(File dir) {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        dir.delete();
    }
    
    public static Map<String, String> getMetadataMapFromMetadataQuery(String metadataQueryString) throws SearchServiceException{
        Map<String, String> metadataMap = new HashMap();
        if(metadataQueryString!=null && !metadataQueryString.isEmpty()){
            try{metadataMap = Arrays.stream(metadataQueryString.split("&"))
                            .map(s -> s.split("=")).collect(Collectors.toMap(s -> s[0], s-> s[1].replaceAll("^\"|\"$", "")));
            }catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e){
                throw new SearchServiceException("Please check the syntax of your metadata query!");
            }
        }
                
        return metadataMap;
    }
    
    
}
