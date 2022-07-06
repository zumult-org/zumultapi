/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.Constants;
import org.zumult.io.IOUtilities;
import org.zumult.io.ISOTEINamespaceContext;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.Corpus;
import org.zumult.objects.IDList;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.DefaultTokenList;
import org.zumult.query.SearchResult;
import org.zumult.query.SearchServiceException;
import org.zumult.query.SearchStatistics;
import org.zumult.query.StatisticEntry;

/**
 *
 * @author Frick
 */
public class AGDAvailableAnnotationValues implements Indexer {
    
    BackendInterface backend;
    String FILE_NAME = "AGDAvailableAnnotationValues.xml";
    String OUTPUT = System.getProperty("user.dir") + "/src/java" + Constants.DATA_ANNOTATIONS_PATH  + FILE_NAME;
    
    /*  false - the values are counted from the transcripts, 
        true - the values are counted from the search index */
    boolean search = true;  

    public static void main(String[] args) {
        new AGDAvailableAnnotationValues().index();
    }


 @Override
    public void index() {        

        try {
            backend = BackendInterfaceFactory.newBackendInterface(); 
            IDList corpora = backend.getCorporaForSearch(null);
            
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element root = document.createElement("available-values");
            document.appendChild(root);
            
            for (String corpusID : corpora){               
                System.out.println("Indexing corpus " + corpusID);
                
                Element corpusE = document.createElement("corpus");
                corpusE.setAttribute("corpus", corpusID);

                Corpus corpus = backend.getCorpus(corpusID);
                
                if(search){
                    appendChilds(document, corpusID, corpus.getTokenBasedAnnotationLayers(), corpusE, null);
                    appendChilds(document, corpusID, corpus.getSpanBasedAnnotationLayers(), corpusE, null);
                }else{
                    IDList transcripts = backend.getTranscripts4Corpus(corpusID);
                    appendChilds(document, corpusID, corpus.getTokenBasedAnnotationLayers(), corpusE, transcripts);
                    appendChilds(document, corpusID, corpus.getSpanBasedAnnotationLayers(), corpusE, transcripts);
                }
                
                root.appendChild(corpusE);
            }


            String path = new File(OUTPUT).getPath();
            System.out.println(FILE_NAME + " is written to " + path);
            String xmlString = IOUtilities.documentToString(document);
            Files.write(Paths.get(path), xmlString.getBytes("UTF-8"));

            
        } catch (IOException | ParserConfigurationException | ClassNotFoundException | InstantiationException | IllegalAccessException | SearchServiceException | XPathExpressionException ex) {
            Logger.getLogger(AGDAvailableAnnotationValues.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    

    private void appendChilds(Document document, String corpusID, Set<AnnotationLayer> annotations, Element corpusE, IDList transcripts) throws IOException, SearchServiceException, XPathExpressionException{
        for (AnnotationLayer annotationLayer : annotations){
            Element keyE = document.createElement("key");
            keyE.setAttribute("id", annotationLayer.getID());
            keyE.appendChild(makeNameElement(document, annotationLayer, "de"));
            keyE.appendChild(makeNameElement(document, annotationLayer, "en"));
            String annoLayer = annotationLayer.getID();
            
            if(transcripts!=null){
                int insgesamt = 0;
                TokenList tokenListForCorpus = new DefaultTokenList(annoLayer);

                for (String transcriptId: transcripts){
                    Transcript transcript = backend.getTranscript(transcriptId);
                    
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    xPath.setNamespaceContext(new ISOTEINamespaceContext());
                    StringBuilder xPathString = new StringBuilder();
                    switch (annoLayer){
                        case "word.type" : 
                            xPathString.append("//tei:w/@type");
                            break;
                        case "norm" :
                        case "lemma" :
                        case "pos" :
                        case "phon" :
                            xPathString.append("//tei:w/@");
                            xPathString.append(annoLayer);
                            break;
                        case "incident" :
                        case "vocal" :
                            xPathString.append("//tei:");
                            xPathString.append(annoLayer);
                            xPathString.append("/tei:desc/text()");
                            break;
                        case "pc":
                            xPathString.append("//tei:");
                            xPathString.append(annoLayer);
                            xPathString.append("/text()");
                            break;
                        default :
                            xPathString.append("//tei:spanGrp[@type='").append(annoLayer).append("']/tei:span/text()");
                     }
                    
                    Document doc = transcript.getDocument();
                    System.out.println(xPathString.toString() + " in "+ transcriptId);
                    
                    NodeList nodes = (NodeList)xPath.evaluate(xPathString.toString(), doc.getDocumentElement(), XPathConstants.NODESET);

                    insgesamt = insgesamt + nodes.getLength();
                    for (int i=0; i<nodes.getLength(); i++){
                        String key = nodes.item(i).getTextContent();
                        int n = 1;
                        if(tokenListForCorpus.keySet().contains(key)){
                            n = n + tokenListForCorpus.get(key);
                        }
                        tokenListForCorpus.put(key, n);
                    }
                        
                }

                System.out.println(annoLayer + ": "+ insgesamt);
                addChild(document, keyE, insgesamt, tokenListForCorpus);

            }else{

                Map<String, Integer> mapForThisKey = new HashMap();
          
                String corpusQuery = "corpusSigle=\""+ corpusID + "\"";
                String query = "<"+annoLayer+"/>";
                SearchResult sr = backend.search(query, null, null, corpusQuery, null, "TRANSCRIPT_BASED_INDEX");
                int size = sr.getTotalHits();
                if(size > 7000000){
                    System.out.println(annoLayer + ": too many values: " + size);
                }else {
                    System.out.println(annoLayer + ": "+ size);
                    SearchStatistics searchStatistics = backend.getSearchStatistics(query, null, null, corpusQuery, null, annoLayer,
                                size, 0, "TRANSCRIPT_BASED_INDEX", "ABS_DESC");
                    ArrayList<StatisticEntry> statistics = searchStatistics.getStatistics();

                    statistics.forEach(se -> {
                        mapForThisKey.put(se.getMetadataValue(), se.getNumberOfHits());
                    }); 
                }

                addChild(document, keyE, size, mapForThisKey);

            }
            
            corpusE.appendChild(keyE);
        }

    }
    
    private void addChild(Document document, Element keyE, int size, Map<String, Integer> map){
        keyE.setAttribute("freg", String.valueOf(size));
        keyE.setAttribute("values", String.valueOf(map.size()));
        for (String value : map.keySet()){
            Element valueE = document.createElement("value");
            valueE.setTextContent(value);
            valueE.setAttribute("freq", Integer.toString(map.get(value)));
            keyE.appendChild(valueE);
        }
    }
    
    private Element makeNameElement(Document document, AnnotationLayer annotationLayer, String lang){
        Element nameE = document.createElement("name");
        nameE.setAttribute("lang", lang);
        nameE.setTextContent(annotationLayer.getName(lang));
        return nameE;
    }
    
}