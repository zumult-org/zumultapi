/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.query.serialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    // was private, changed this for #182
    static final String DONE = "DONE";
    
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
    public String displayKWICinXML(KWIC kwicObj) {

            Document document = db.newDocument();
            Element root = document.createElement(XML_ROOT);
            root.setAttribute("type", "kwic");

            Element query = document.createElement(QUERY_PART);
            query.setTextContent(kwicObj.getSearchQuery().getQueryString());

            Element metadataQuery = document.createElement(ADDITIONAL_METADATA_QUERY_PART);
            metadataQuery.setTextContent(kwicObj.getMetadataQuery().getAdditionalMetadata());
            
            Element corpusQuery = document.createElement(CORPUS_QUERY_PART);
            corpusQuery.setTextContent(kwicObj.getMetadataQuery().getCorpusQuery());
            
            root.appendChild(query);
            root.appendChild(metadataQuery);
            root.appendChild(corpusQuery);

            Element meta = document.createElement(META_PART);

            Element cutoffElem = document.createElement(CUTOFF);
            Boolean cutoff = kwicObj.getCutoff();
            cutoffElem.setTextContent(String.valueOf(cutoff));
            meta.appendChild(cutoffElem);
            Element total = document.createElement(TOTAL_RESULTS);
            Element totalTranscripts = document.createElement(TOTAL_TRANSCRIPTS);

            if (cutoff){
                 total.setTextContent(String.valueOf(kwicObj.getTotalHits()));
                 totalTranscripts.setTextContent(String.valueOf(kwicObj.getTotalTranscripts()));
            }else {
                total.setTextContent(String.valueOf(-1));
                totalTranscripts.setTextContent(String.valueOf(-1));
            }
            
            Element itemsPerPage = document.createElement(ITEMS_PER_PAGE);
            itemsPerPage.setTextContent(String.valueOf(kwicObj.getPagination().getItemsPerPage()));
            meta.appendChild(itemsPerPage);

            Element mode = document.createElement(MODE);
            Element code = document.createElement(CODE);
            code.setTextContent(kwicObj.getSearchMode());
            mode.appendChild(code);
            meta.appendChild(mode);
                        
            meta.appendChild(total);
            meta.appendChild(totalTranscripts);
            
            Element searchTime = document.createElement(SEARCH_TIME);
            searchTime.setTextContent(TimeUtilities.format(kwicObj.getSearchTime()));
            meta.appendChild(searchTime);
            
            Element pageStartIndex = document.createElement(PAGE_START_INDEX);
            pageStartIndex.setTextContent(String.valueOf(kwicObj.getPagination().getPageStartIndex()));
            meta.appendChild(pageStartIndex);

            Element context = document.createElement(CONTEXT);
            Element left = document.createElement(CONTEXT_LEFT);
            Element right = document.createElement(CONTEXT_RIGHT);
            Element leftItem = document.createElement(CONTEXT_ITEM);
            Element leftLength = document.createElement(CONTEXT_LENGTH);
            Element rightItem = document.createElement(CONTEXT_ITEM);
            Element rightLength = document.createElement(CONTEXT_LENGTH);
            leftItem.setTextContent(kwicObj.getLeftContext().getType());
            rightItem.setTextContent(kwicObj.getRightContext().getType());
            leftLength.setTextContent(String.valueOf(kwicObj.getLeftContext().getLength()));
            rightLength.setTextContent(String.valueOf(kwicObj.getRightContext().getLength()));
            left.appendChild(leftItem);
            left.appendChild(leftLength);
            right.appendChild(rightItem);
            right.appendChild(rightLength);
            context.appendChild(left);
            context.appendChild(right);
            meta.appendChild(context);
            
            root.appendChild(meta);
            
            if(kwicObj.getAdditionalSearchConstraints()!=null){
                Element additionalSearchConstraints = document.createElement(ADDITIONAL_SAERCH_CONSTRAINTS);
                
                for (AdditionalSearchConstraint additionalSearchConstraint: kwicObj.getAdditionalSearchConstraints()){
                    NodeList nodes = additionalSearchConstraint.getDocument().getChildNodes();
                    for (int i=0; i<nodes.getLength(); i++){
                        Node node = document.importNode(nodes.item(i), true);
                        additionalSearchConstraints.appendChild(node);
                    }
                }
                
                root.appendChild(additionalSearchConstraints);
            }

            Element hits = document.createElement(HITS_PART);
            ArrayList<Hit> rows = kwicObj.getHits();
            ArrayList<KWICSnippet> snippets = (ArrayList<KWICSnippet>) kwicObj.getKWICSnippets();
            
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
          
    public File createKWICDownloadFile(KWIC ke, String fileType, BackendInterface backendInterface) throws IOException {

        File file = createTmpFile(fileType);
        ISOTEIKWICSnippetCreator creator = new ISOTEIKWICSnippetCreator();
        OutputStreamWriter bw = null;
        
        KWICContext leftContext = ke.getLeftContext();
        KWICContext rightContext = ke.getRightContext();

        try {

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
   
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(DefaultQuerySerializer.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            
        }

        return file;
    }
    
    /*private File createTmpFile(String fileType) throws IOException{
        File file = null;
        String actualPath = DefaultQuerySerializer.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        File target = new File(IOHelper.getProjectFile(actualPath), "downloads"); 
            try {         
                file = File.createTempFile("tmp", "." + fileType, target);
                file.deleteOnExit();
            } catch(IOException ex){
                throw new IOException("Temporary file with KWIC could not be created: " + target.getAbsolutePath() + " does not exist ", ex);
            }
        
        return file;
    }*/
    
    // TS, 2024-01-19, changed this for issue #182
    private File createTmpFile(String fileType) throws IOException{
        File file = File.createTempFile("tmp", "." + fileType);
        file.deleteOnExit();
        return file;
    }    
    
    protected String getKWICLine(String docID, 
            ArrayList<Hit.Match> matchArray, String firstMatchID, String lastMatchID, Document transcriptDoc, ISOTEIKWICSnippetCreator creator,
            KWICContext leftContext, KWICContext rightContext, HashMap<String, String> metadata) throws IOException{
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<kwic-line>");
        
        // create snippet           
        KWICSnippet snippetObj = creator.apply(transcriptDoc, firstMatchID, matchArray, leftContext, rightContext);
        
        //add metadata
        for (String key: metadata.keySet()){
            sb.append("<"+key+">");
            sb.append(metadata.get(key));
            sb.append("</"+key+">");
        }
        
        sb.append("</kwic-line>");
        return sb.toString();
        
    }
    
   
    
    // was private, changed for #182
    class Consumer implements Runnable {

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

