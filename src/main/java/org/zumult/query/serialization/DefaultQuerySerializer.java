/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.serialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zumult.io.IOUtilities;
import org.zumult.query.Hit;
import org.zumult.query.Hit.Match;
import org.zumult.query.StatisticEntry;
import org.zumult.query.SearchStatistics;
import org.zumult.query.KWIC;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.ListUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.Constants;
import org.zumult.io.IOHelper;
import org.zumult.io.TimeUtilities;
import org.zumult.objects.Transcript;
import org.zumult.query.AdditionalSearchConstraint;
import org.zumult.query.KWICContext;
import org.zumult.query.KWICSnippet;
import org.zumult.query.KWICSnippet.KWICSnippetToken;
import org.zumult.query.implementations.ISOTEIKWICSnippetCreator;

/**
 *
 * @author Elena Frick
 */
public class DefaultQuerySerializer implements QuerySerializer {
    
    protected static final String START_INTERVAL = "startInterval";
    protected static final String END_INTERVAL = "endInterval";
    protected static final String QUERY_PART = "query";
    protected static final String CORPUS_QUERY_PART = "corpusQuery";
    protected static final String ADDITIONAL_METADATA_QUERY_PART = "metadataQuery";
    protected static final String ADDITIONAL_SAERCH_CONSTRAINTS = "additionalSearchConstraints";
    protected static final String CUTOFF = "cutoff";
    protected static final String TOTAL_RESULTS = "total";
    protected static final String TOTAL_TRANSCRIPTS = "totalTranscripts";
    protected static final String SEARCH_TIME = "searchTime";
    protected static final String DISTINCT_VALUES = "distinctValues";
    protected static final String META_PART = "meta";
    protected static final String XML_ROOT = "response";
    protected static final String ITEMS_PER_PAGE = "itemsPerPage";
    protected static final String PAGE_START_INDEX = "pageStartIndex";
    protected static final String CONTEXT = "context";
    protected static final String CONTEXT_LENGTH = "length";
    protected static final String CONTEXT_ITEM = "item";
    protected static final String CONTEXT_LEFT = "left";
    protected static final String CONTEXT_RIGHT = "right";
    protected static final String HIT = "hit";
    protected static final String HITS_PART = "hits";
    protected static final String SNIPPET = "snippet";
    protected static final String START_MORE_MARKER = "isStartMore";
    protected static final String END_MORE_MARKER = "isEndMore";
    protected static final String ROW = "row";
    protected static final String SOURCE = "source";
    protected static final String MEDIA = "media";
    protected static final String MATCH = "match";
    protected static final String PARENT = "parent";
    protected static final String MODE = "searchMode";
    protected static final String CODE = "code";
    protected static final String SPEAKER_ID_MARKER = "who";
    protected static final String STATISTICS_TYPE = "metadataKey";
    protected static final String ITEMS = "items";
    protected static final String ITEM = "item";
    protected static final String METADATA_VALUE = "metadataValue";
    protected static final String NUMBER_OF_HITS = "numberOfHits";
    protected static final String FILE = "file";
    
    private static final String DONE = "DONE";
    
    DocumentBuilder db;
    
    public DefaultQuerySerializer() {
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex){
            Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String displayKWICinXML(KWIC obj) {

            Document document = db.newDocument();
            Element root = document.createElement(XML_ROOT);
            root.setAttribute("type", "kwic");

            Element query = document.createElement(QUERY_PART);
            query.setTextContent(obj.getSearchQuery().getQueryString());

            Element metadataQuery = document.createElement(ADDITIONAL_METADATA_QUERY_PART);
            metadataQuery.setTextContent(obj.getMetadataQuery().getAdditionalMetadata());
            
            Element corpusQuery = document.createElement(CORPUS_QUERY_PART);
            corpusQuery.setTextContent(obj.getMetadataQuery().getCorpusQuery());
            
            root.appendChild(query);
            root.appendChild(metadataQuery);
            root.appendChild(corpusQuery);

            Element meta = document.createElement(META_PART);

            Element cutoffElem = document.createElement(CUTOFF);
            Boolean cutoff = obj.getCutoff();
            cutoffElem.setTextContent(String.valueOf(cutoff));
            meta.appendChild(cutoffElem);
            Element total = document.createElement(TOTAL_RESULTS);
            Element totalTranscripts = document.createElement(TOTAL_TRANSCRIPTS);

            if (cutoff){
                 total.setTextContent(String.valueOf(obj.getTotalHits()));
                 totalTranscripts.setTextContent(String.valueOf(obj.getTotalTranscripts()));
            }else {
                total.setTextContent(String.valueOf(-1));
                totalTranscripts.setTextContent(String.valueOf(-1));
            }
            
            Element itemsPerPage = document.createElement(ITEMS_PER_PAGE);
            itemsPerPage.setTextContent(String.valueOf(obj.getPagination().getItemsPerPage()));
            meta.appendChild(itemsPerPage);

            Element mode = document.createElement(MODE);
            Element code = document.createElement(CODE);
            code.setTextContent(obj.getSearchMode());
            mode.appendChild(code);
            meta.appendChild(mode);
                        
            meta.appendChild(total);
            meta.appendChild(totalTranscripts);
            
            Element searchTime = document.createElement(SEARCH_TIME);
            searchTime.setTextContent(TimeUtilities.format(obj.getSearchTime()));
            meta.appendChild(searchTime);
            
            Element pageStartIndex = document.createElement(PAGE_START_INDEX);
            pageStartIndex.setTextContent(String.valueOf(obj.getPagination().getPageStartIndex()));
            meta.appendChild(pageStartIndex);

            Element context = document.createElement(CONTEXT);
            Element left = document.createElement(CONTEXT_LEFT);
            Element right = document.createElement(CONTEXT_RIGHT);
            Element leftItem = document.createElement(CONTEXT_ITEM);
            Element leftLength = document.createElement(CONTEXT_LENGTH);
            Element rightItem = document.createElement(CONTEXT_ITEM);
            Element rightLength = document.createElement(CONTEXT_LENGTH);
            leftItem.setTextContent(obj.getLeftContext().getType());
            rightItem.setTextContent(obj.getRightContext().getType());
            leftLength.setTextContent(String.valueOf(obj.getLeftContext().getLength()));
            rightLength.setTextContent(String.valueOf(obj.getRightContext().getLength()));
            left.appendChild(leftItem);
            left.appendChild(leftLength);
            right.appendChild(rightItem);
            right.appendChild(rightLength);
            context.appendChild(left);
            context.appendChild(right);
            meta.appendChild(context);
            
            root.appendChild(meta);
            
            if(obj.getAdditionalSearchConstraints()!=null){
                Element additionalSearchConstraints = document.createElement(ADDITIONAL_SAERCH_CONSTRAINTS);
                
                for (AdditionalSearchConstraint additionalSearchConstraint: obj.getAdditionalSearchConstraints()){
                    NodeList nodes = additionalSearchConstraint.getDocument().getChildNodes();
                    for (int i=0; i<nodes.getLength(); i++){
                        Node node = document.importNode(nodes.item(i), true);
                        additionalSearchConstraints.appendChild(node);
                    }
                }
                
                root.appendChild(additionalSearchConstraints);
            }

            Element hits = document.createElement(HITS_PART);
            ArrayList<Hit> rows = obj.getHits();
            ArrayList<KWICSnippet> snippets = (ArrayList<KWICSnippet>) obj.getKWICSnippets();
            
            int index = 0;
            for (Hit row : rows){
                Element hit = document.createElement(HIT);

                hit.setAttribute(ROW, String.valueOf(row.getPosition()));
                hit.setAttribute(SOURCE, row.getDocId());

                hit.setAttribute(START_INTERVAL, String.valueOf(row.getFirstMatch().getStartInterval()));
                hit.setAttribute(END_INTERVAL, String.valueOf(row.getLastMatch().getEndInterval()));

                KWICSnippet snippetObj = snippets.get(index);

                Element snippet = document.createElement(SNIPPET);
                snippet.setAttribute(START_MORE_MARKER, String.valueOf(snippetObj.isStartMore()));
                snippet.setAttribute(END_MORE_MARKER, String.valueOf(snippetObj.isEndMore()));
                snippet.setAttribute(SPEAKER_ID_MARKER, String.join(Constants.SPEAKER_DELIMITER, snippetObj.getSpeakerIds()));

                ArrayList<KWICSnippetToken> tokens = snippetObj.getContent();
                tokens.stream().map((token) -> {
                    Element elem = token.asXMLElement();
                    if(token.belongsToMatch()){
                        elem.setAttribute(MATCH, String.valueOf(token.belongsToMatch()));
                    }
                    elem.setAttribute(PARENT, token.getParentId());
                    return elem;
                }).map((elem) -> document.importNode(elem, true)).forEachOrdered((tokenNode) -> {
                    snippet.appendChild(tokenNode);
                });

                hit.appendChild(snippet);

                hits.appendChild(hit);
                
                index++;
            }
            
            root.appendChild(hits);
            

            document.appendChild(root);
            return IOUtilities.documentToString(document); 
    }

    public String displayStatiscticsInXML(SearchStatistics obj) {

        Document document = db.newDocument();
        Element root = document.createElement(XML_ROOT);
        root.setAttribute("type", "statistics");
        
        Element query = document.createElement(QUERY_PART);
        query.setTextContent(obj.getSearchQuery().getQueryString());
        
        Element metadataQuery = document.createElement(ADDITIONAL_METADATA_QUERY_PART);
        metadataQuery.setTextContent(obj.getMetadataQuery().getAdditionalMetadata());
            
        Element corpusQuery = document.createElement(CORPUS_QUERY_PART);
        corpusQuery.setTextContent(obj.getMetadataQuery().getCorpusQuery());
        
        Element meta = document.createElement(META_PART);
        
        Element type = document.createElement(STATISTICS_TYPE);
        type.setTextContent(String.valueOf(obj.getMetadataKey().getID()));
        meta.appendChild(type);
        
        Element totalHits = document.createElement(TOTAL_RESULTS);
        totalHits.setTextContent(String.valueOf(obj.getTotalHits()));
        meta.appendChild(totalHits);
        
        Element totalTranscripts = document.createElement(TOTAL_TRANSCRIPTS);
        totalTranscripts.setTextContent(String.valueOf(obj.getTotalTranscripts()));
        meta.appendChild(totalTranscripts);
                
        Element distinctValues = document.createElement(DISTINCT_VALUES);
        distinctValues.setTextContent(String.valueOf(obj.getNumberOfDistinctValues()));
        meta.appendChild(distinctValues);
        
        Element searchTime = document.createElement(SEARCH_TIME);
        searchTime.setTextContent(TimeUtilities.format(obj.getSearchTime()));
        meta.appendChild(searchTime);
        
        Element items = document.createElement(ITEMS);
        ArrayList<StatisticEntry> rows = obj.getStatistics();
        
        int index = 0;
        Pattern r = Pattern.compile("^\\d+$");
        for (StatisticEntry row : rows){
            Element item = document.createElement(ITEM);
    
            index = index+1;
            item.setAttribute(ROW, String.valueOf(index));
            
            Matcher m = r.matcher(row.getMetadataValue());
            if (m.matches( )) {
                int num = Integer.parseInt(row.getMetadataValue());
                item.setAttribute(METADATA_VALUE, String.valueOf(num));
            }else{
                item.setAttribute(METADATA_VALUE, row.getMetadataValue());
            }
            item.setAttribute(NUMBER_OF_HITS, String.valueOf(row.getNumberOfHits()));
            items.appendChild(item);
        }
        
        
        root.appendChild(query);
        root.appendChild(metadataQuery);
        root.appendChild(corpusQuery);
        root.appendChild(meta);
        root.appendChild(items);
        
        document.appendChild(root);
        
        return IOUtilities.documentToString(document);

    }
    
    public String displayKWICExportInXML(KWIC ke){
        Document document = db.newDocument();
        Element root = document.createElement(XML_ROOT);
        root.setAttribute("type", "download");
        Element file = document.createElement(FILE);
        file.setTextContent(((File) ke.getKWICSnippets()).getName());
        root.appendChild(file);
        document.appendChild(root);
        return IOUtilities.documentToString(document);
    }
    
    private String getContentOfToken(KWICSnippet.KWICSnippetToken token){
        Element tokenElement = token.asXMLElement();
        if(tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_WORD_TOKEN)){
            return token.asXMLElement().getTextContent();
        }else if(tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_PAUSE)){
            return tokenElement.getAttribute(Constants.ATTRIBUTE_NAME_REND);
        }else if (tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_INCIDENT) || tokenElement.getNodeName().equals(Constants.ELEMENT_NAME_VOCAL)){
            try{
                Element desc =  (Element) tokenElement.getElementsByTagName(Constants.ATTRIBUTE_NAME_DESC).item(0);
                return desc.getAttribute(Constants.ATTRIBUTE_NAME_REND);
            }catch(java.lang.ClassCastException ex){
                Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(token.toString());
            }
        }
        return null;
        
    }
 

    public File createKWICDownloadFile(KWIC ke, String fileType) throws IOException {

        File file = createTmpFile(fileType);
        ISOTEIKWICSnippetCreator creator = new ISOTEIKWICSnippetCreator();
        OutputStreamWriter bw = null;
        
        KWICContext leftContext = ke.getLeftContext();
        KWICContext rightContext = ke.getRightContext();

        try {
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            String transcriptId = "";
            Document transcriptDoc = null;
            bw = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
            
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.write("<kwic>");
            
            ArrayList<Hit> hitArray = ke.getHits();
            
            for (Hit hit : hitArray) {

                    String docID = hit.getDocId();
                    String firstMatchID = hit.getFirstMatch().getID();
                    String lastMatchID = hit.getLastMatch().getID();
                    ArrayList<Hit.Match> matchArray = hit.getMatches();
                    HashMap<String, String> metadata = hit.getMetadata();

                    
        
                    if(!transcriptId.equals(docID)){
                        transcriptId = docID;
                        //System.out.println("Opening " + transcriptId);
                        Transcript transcript = backendInterface.getTranscript(transcriptId);
                        transcriptDoc = transcript.getDocument();
                    }
                
                    bw.write(getKWICLine(docID, matchArray, firstMatchID, lastMatchID, 
                            transcriptDoc, creator, leftContext, rightContext, metadata));
            }  

            bw.write("</kwic>");
   
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | FileNotFoundException | UnsupportedEncodingException ex){
            Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (bw!=null){
                bw.close();
            }
        }

        return file;
    }
        
    public File createKWICDownloadFileWithThreads(KWIC ke, String fileType) throws IOException {

        File file = createTmpFile(fileType);
        ISOTEIKWICSnippetCreator creator = new ISOTEIKWICSnippetCreator();
        OutputStreamWriter bw = null;
        
        KWICContext leftContext = ke.getLeftContext();
        KWICContext rightContext = ke.getRightContext();

        try {
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();

            bw = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
            
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.write("<kwic>");
            
            List<Hit> hitArray = ke.getHits();
            
            int targetSize = 50;
            LinkedBlockingQueue<String> linkedQueue   = new LinkedBlockingQueue<String>(); 
            
            new Thread(new Consumer(linkedQueue, bw)).start();
            
            List<List<Hit>> largeList = ListUtils.partition(hitArray, targetSize);
            
            
            largeList.parallelStream().forEach((List<Hit> x) -> {
                String transcriptId = "";
                Document transcriptDoc = null;
                for (Hit hit : x) {
                    String docID = hit.getDocId();
                    String firstMatchID = hit.getFirstMatch().getID();
                    String lastMatchID = hit.getLastMatch().getID();
                    ArrayList<Hit.Match> matchArray = hit.getMatches();
                    HashMap<String, String> metadata = hit.getMetadata();

                    if(!transcriptId.equals(docID)){
                        try {
                            transcriptId = docID;
                            //System.out.println("Opening " + transcriptId);
       
                            Transcript transcript = backendInterface.getTranscript(transcriptId);
                            transcriptDoc = transcript.getDocument();
                        } catch (IOException ex) {
                            Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    String line;
                    try {
                        line = getKWICLine(docID, matchArray, firstMatchID, lastMatchID, transcriptDoc, creator, 
                                leftContext, rightContext, metadata);
                        linkedQueue.put(line);
                    } catch (IOException ex) {
                        Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                            
                } 
            });
                    
        linkedQueue.put(DONE);
   
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | FileNotFoundException | UnsupportedEncodingException ex){
            Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            
        }finally {
            
        }

        return file;
    }
    
    private File createTmpFile(String fileType) throws IOException{
        File file = null;
        String actualPath = DefaultQuerySerializer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        //String actualPath = SearchResultSerializer.class.getResource("SearchResultSerializer.class").getPath();
        
        File target = new File(IOHelper.getProjectFile(actualPath), "downloads"); 
            try {         
                file = File.createTempFile("tmp", "." + fileType, target);
                file.deleteOnExit();
            } catch(IOException ex){
                throw new IOException("Temporary file with KWIC could not be created: " + target.getAbsolutePath() + " does not exist ", ex);
            }
        
        return file;
    }
    
    private String getKWICLine(String docID, 
            ArrayList<Hit.Match> matchArray, String firstMatchID, String lastMatchID, Document transcriptDoc, ISOTEIKWICSnippetCreator creator,
            KWICContext leftContext, KWICContext rightContext, HashMap<String, String> metadata) throws IOException{
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<kwic-line>");
        
        // create snippet           
        KWICSnippet snippetObj = creator.apply(transcriptDoc, firstMatchID, matchArray, leftContext, rightContext);
        
        // add speakers, match dgd and zumult links
        sb.append(getAdditionalInfos(docID, firstMatchID, lastMatchID, matchArray, snippetObj));
        
        //add metadata
        for (String key: metadata.keySet()){
            sb.append("<"+key+">");
            sb.append(metadata.get(key));
            sb.append("</"+key+">");
        }
        
        sb.append("</kwic-line>");
        return sb.toString();
        
    }
    
    private String getAdditionalInfos(String docID, String firstMatchID, String lastMatchID, 
            ArrayList<Hit.Match> matchArray, KWICSnippet snippetObj) throws IOException{

        StringBuilder bw = new StringBuilder();
        //add doc ID
        bw.append("<transcript-id>");
        bw.append(docID);
        bw.append("</transcript-id>");
                
        // add speaker
        bw.append("<speaker-id>");
        StringBuilder sb = new StringBuilder();
        for (String id : snippetObj.getSpeakerIds()){
            sb.append(id);
            sb.append(" ");
        }
        bw.append(sb.toString().substring(0, sb.length()-1));
        bw.append("</speaker-id>");

        // add snippet
        Iterator<KWICSnippet.KWICSnippetToken> iterator = snippetObj.getContent().iterator(); 

        // add left context
        bw.append("<left-context>");
        KWICSnippet.KWICSnippetToken actualToken = null;
        while (iterator.hasNext()) { 
            KWICSnippet.KWICSnippetToken token = iterator.next();
            if(!token.belongsToMatch()){
                bw.append(getContentOfToken(token));
                bw.append(" ");
            }else{
                actualToken = token;
                break;
            }
        }

        bw.append("</left-context>");

        // add match
        bw.append("<match>");
        bw.append(getContentOfToken(actualToken));
        bw.append(" ");
        actualToken = null;
        while (iterator.hasNext()) { 
            KWICSnippet.KWICSnippetToken token = iterator.next();
            if(token.belongsToMatch()){
                bw.append(getContentOfToken(token));
                bw.append(" ");
            }else{
               // check if there are more matches
                int index = snippetObj.getContent().indexOf(token);
                Iterator<KWICSnippet.KWICSnippetToken> iteratorTmp = snippetObj.getContent().listIterator(index);

                Boolean moreMatches = false;                      
                while (iteratorTmp.hasNext()) { 
                    KWICSnippet.KWICSnippetToken tokenTpm = iteratorTmp.next();
                    if(tokenTpm.belongsToMatch()){
                        moreMatches=true;
                        break;   
                    }
                }
                if(moreMatches){
                    bw.append(getContentOfToken(token));
                    bw.append(" ");
                }else{
                    actualToken = token;            
                    break;
                } 
            }
        }

        bw.append("</match>");

        // add right context
        bw.append("<right-context>");
        if(actualToken!= null){
            bw.append(getContentOfToken(actualToken));
            bw.append(" ");
        }
        while (iterator.hasNext()) { 
            KWICSnippet.KWICSnippetToken token = iterator.next();
            bw.append(getContentOfToken(token));
            bw.append(" ");
        }
        bw.append("</right-context>");

        // add dgd link
        bw.append("<dgd-link>");
        bw.append(Configuration.getWebAppBaseURL());
        bw.append("/DGDLink?command=showTranscriptExcerpt&amp;transcriptID=");
        bw.append(docID);
        bw.append("&amp;tokenIDs=");
        bw.append(firstMatchID);
        bw.append("</dgd-link>");
        
        // add zumult link
        bw.append("<zumult-link>");
        bw.append(Configuration.getWebAppBaseURL());
        bw.append("/jsp/zuViel.jsp?");
        bw.append("transcriptID=");
        bw.append(docID);
        bw.append("&amp;");
        bw.append("form=norm");
        bw.append("&amp;");
        bw.append("highlightIDs1=");
        for(Match m:matchArray){
            bw.append(m.getID());
            bw.append(" ");
        }
        bw.append("&amp;");
        bw.append("startTokenID=");
        bw.append(firstMatchID);
        bw.append("&amp;");
        bw.append("endTokenID=");
        bw.append(lastMatchID);
        bw.append("&amp;howMuchAround=3");
        bw.append("</zumult-link>");
        
        return bw.toString();
    }
      
    private class Consumer implements Runnable {

        private final BlockingQueue<String> queue;
        private final OutputStreamWriter bw;

        @Override
        public void run() {

            try {
                while (true) {
                    String take = queue.take();
                    if (DONE.equals(take)) {
                        bw.write("</kwic>");
                        if (bw!=null){
                            bw.close();
                        }
                        return;
                    }
                    process(take);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private void process(String take) {
            try {
                bw.write(take);
            } catch (IOException ex) {
                Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public Consumer(BlockingQueue<String> queue,OutputStreamWriter bw) {
            this.queue = queue;
            this.bw = bw;
        }
    }
}

