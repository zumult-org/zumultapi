/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.zumult.query.searchEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;
import org.zumult.objects.IDList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.COMATranscript;
import org.zumult.query.SearchServiceException;
import static org.zumult.query.searchEngine.MTASBasedSearchEngine.FIELD_TRANSCRIPT_CONTENT;
import static org.zumult.query.searchEngine.MTASBasedSearchEngine.FIELD_TRANSCRIPT_ID_FROM_FILE_NAME;
import static org.zumult.query.searchEngine.MTASBasedSearchEngine.FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG;
import static org.zumult.query.searchEngine.MTASBasedSearchEngine.FIELD_TRANSCRIPT_TOKEN_TOTAL;
import static org.zumult.query.searchEngine.MTASBasedSearchEngine.XML_FILE_FORMAT;

/**
 *
 * @author Frick
 */
public class COMASearchEngine extends MTASBasedSearchEngine {

    @Override
    public SearchEngineResponseHitList searchRepetitions(ArrayList<String> indexPaths, String queryString, String metadataQueryString, Integer from, Integer to, Boolean cutoff, IDList metadataIDs, ArrayList<Repetition> repetitions, HashMap<String, HashSet> synonyms, HashMap<String, String[]> customWordLists) throws SearchServiceException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    public Document getDocument(File f) throws IOException {
        
        Document doc = new Document();

        Transcript transcript = null;
        try {
            transcript = new COMATranscript(IOHelper.readDocument(f));
        } catch (SAXException | ParserConfigurationException | IOException ex) {
            throw new IOException("Unable to parse the file " + f.getAbsolutePath(), ex);
        }
        String transcriptID = transcript.getID();
        String filename = f.getName().substring(0, f.getName().length() - XML_FILE_FORMAT.length());         
            
        doc.add(new StringField(FIELD_TRANSCRIPT_ID_FROM_FILE_NAME, filename, Field.Store.YES)); // is not used for searching
        doc.add(new StringField(FIELD_TRANSCRIPT_METADATA_T_DGD_KENNUNG, transcriptID, Field.Store.YES));
        doc.add(new TextField(FIELD_TRANSCRIPT_CONTENT, f.getAbsolutePath(), Field.Store.YES));
        doc.add(new TextField(FIELD_TRANSCRIPT_TOKEN_TOTAL, Integer.toString(transcript.getNumberOfTokens()), Field.Store.YES));
        
        return doc;
    }
    
}
