/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.zumult.objects.Corpus;
import org.zumult.objects.implementations.DGD2Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Speaker;
import org.zumult.objects.Transcript;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.Constants;
import org.zumult.io.HTTPMethodHelper;
import org.zumult.io.IOHelper;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.AnnotationTagSet;
import org.zumult.objects.Measure;
import org.zumult.objects.Protocol;
import org.zumult.objects.implementations.DGD2Event;
import org.zumult.objects.implementations.DGD2Speaker;
import org.zumult.objects.implementations.ISOTEIAnnotationBlock;
import org.zumult.objects.implementations.ISOTEITranscript;

/**
 *
 * @author Thomas_Schmidt
 */
public class DGD2Oracle extends AbstractIDSBackend {

    String sessionID;
    private Connection connection;
    private Statement stmt;
    
    public DGD2Oracle(){ 
        try {
            // establish a connection to the backend 
            // this is of course not the right way to do it...
            String[][] parameters = {
                {"v_user", "thomas.schmidt@uni-hamburg.de"},
                {"v_pass", "Bernd!Moos"}
            };
            String result = HTTPMethodHelper.callCommand("getNewSessionID", parameters);
            Document doc = IOHelper.DocumentFromText(result);
            sessionID = doc.getDocumentElement().getChildNodes().item(0).getTextContent();
            System.out.println("Session ID " + sessionID);
            // ... and from now on we can use the session ID to query the DB
            
                        
        } catch (IOException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } /* catch (NamingException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } */
        
    }
    

    @Override
    public IDList getEvents4Corpus(String corpusID) throws IOException {
        try {
            IDList list = new IDList("Event");
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_corpus_id", corpusID}
            };
            String xml = HTTPMethodHelper.callCommand("getCorpusEventIDs", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("e_id");
            for (int i=0; i<childNodes.getLength(); i++){
                Element element = (Element)(childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
            return list;
        } catch (SAXException ex) {
            throw new IOException(ex);
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getSpeakers4Corpus(String corpusID) throws IOException {
        try {
        IDList list = new IDList("Speaker");
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_corpus_id", corpusID}
            };
            String xml = HTTPMethodHelper.callCommand("getCorpusSpeakerIDs", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("s_id");
            for (int i=0; i<childNodes.getLength(); i++){
                Element element = (Element)(childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
            return list;
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getSpeechEvents4Event(String eventID) throws IOException {
        try {
            IDList list = new IDList("SpeechEvent");
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_e_id", eventID}
            };
            String xml = HTTPMethodHelper.callCommand("getSpeechEvents4Event", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("se_id");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
            return list;
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }



    @Override
    public Corpus getCorpus(String corpusID) throws IOException {
        try {
//            MAKES NAMING CONVENTIONS INCONSISTENT 
//            while (corpusID.length() < 4) {
//                corpusID += "-";
//            }
            
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_doc_id", corpusID}                    
            };
            String xml = HTTPMethodHelper.callCommand("getCorpusXML", parameters);
            xml = HTTPMethodHelper.stripXMLResponse(xml);
            Document doc = IOHelper.DocumentFromText(xml);
            Corpus corpus = new DGD2Corpus(doc);
            return corpus;
        } catch (ParserConfigurationException | SAXException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    public Event getEvent(String eventID) throws IOException {
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_doc_id", eventID}                    
            };
            String xml = HTTPMethodHelper.callCommand("getEventXML", parameters);
            xml = HTTPMethodHelper.stripXMLResponse(xml);
            Document doc = IOHelper.DocumentFromText(xml);
            DGD2Event event = new DGD2Event(doc);
            return event;
        } catch (ParserConfigurationException | SAXException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Speaker getSpeaker(String speakerID) throws IOException {
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_doc_id", speakerID}                    
            };
            String xml = HTTPMethodHelper.callCommand("getSpeakerXML", parameters);
            xml = HTTPMethodHelper.stripXMLResponse(xml);
            Document doc = IOHelper.DocumentFromText(xml);
            DGD2Speaker speaker = new DGD2Speaker(doc);
            
            return speaker;
        } catch (ParserConfigurationException | SAXException ex) {
            throw new IOException(ex);
        }
    }

    
    
    public boolean getTranscriptsViaFLN = false;
    
    @Override
    public Transcript getTranscript(String transcriptID) throws IOException {
            // first version: get FLN and transform
            if (getTranscriptsViaFLN){
                try {
                    String[][] parameters = {
                        {"v_session_id", sessionID},
                        {"v_doc_id", transcriptID}                    
                    };
                    String xml = HTTPMethodHelper.callCommand("getTranscriptXML", parameters);
                    if (xml.contains("</xml_error>")){
                        throw new IOException("Error retrieving transcript " + transcriptID + " from backend:\n" + xml);
                    }                        
                    xml = HTTPMethodHelper.stripXMLResponse(xml);           
                    StreamSource stylesource = new StreamSource(getClass().getResourceAsStream(Constants.FLN2ISO_STYLESHEET));             
                    Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource);  
                    StreamSource source = new StreamSource(new StringReader(xml));
                    DOMResult result = new DOMResult();
                    transformer.transform(source, result);
                    Document transformedDoc = (Document) result.getNode();            
                    Transcript transcript = new ISOTEITranscript(transformedDoc);
                    return transcript;
                } catch (TransformerException ex) {
                    throw new IOException(ex);
                }
            }
            
            // 08-05-2019: better version -- get ISO/TEI XML directly
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_doc_id", transcriptID + "_DF_01"}                    
            };
            String xml = HTTPMethodHelper.callCommand("getISOTranscriptXML", parameters);
            if (xml.contains("</error>")){
                throw new IOException("Error retrieving transcript " + transcriptID + " from backend:\n" + xml);
            }                        
            //xml = HTTPMethodHelper.stripXMLResponse(xml);   
            // ATTENTION: the ISOs in Ora have no namespace declarations!
            // AS OF 11-06-2019 (see Joachim's mail), they do!
            //xml = xml.replaceAll("<TEI>", "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" xmlns:tei=\"http://www.tei-c.org/ns/1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">");
            
            Transcript transcript = new ISOTEITranscript(xml);
            return transcript;
    }

    
    @Override
    public IDList getTranscripts4SpeechEvent(String speechEventID) throws IOException {
        try {
            IDList list = new IDList("Transcript");
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_se_id", speechEventID}
            };
            String xml = HTTPMethodHelper.callCommand("getTranscripts4SpeechEvent", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("t_id");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
            return list;
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getAudios4SpeechEvent(String speechEventID) throws IOException {
        IDList result = new IDList("Media");
        String audioID = "";
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_se_id", speechEventID}
            };
            String xml = HTTPMethodHelper.callCommand("getAudios4SpeechEvent", parameters);
            //System.out.println("xml: " + xml);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList audioTags = doc.getDocumentElement().getElementsByTagName("a_id");
            System.out.println("audioTagsaudioTags");
            for (int i = 0; i < audioTags.getLength(); i++) {
                audioID = audioTags.item(i).getTextContent().trim().substring(0,23);
                System.out.println("audioID: " + audioID);

                result.add(audioID);
            }
            // never more than one audio in the current backend...
            //FOLK_E_00076_SE_01_A_01_DF_01.WAV 
            
            // ==> 2019-12-11 J.B.: not true in the case of mp3 at least. changed below because audio list not complete
//            if (audioTags.getLength() > 0) {
//                audioID = audioTags.item(0).getTextContent().trim().substring(0,23);
//            }
        } catch (SAXException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getVideos4SpeechEvent(String speechEventID) throws IOException {
        IDList result = new IDList("Media");
        String videoID = "";
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_se_id", speechEventID}
            };
            String xml = HTTPMethodHelper.callCommand("getVideos4SpeechEvent", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList videoTags = doc.getDocumentElement().getElementsByTagName("v_id");
            // never more than one audio in the current backend...
            //FOLK_E_00076_SE_01_A_01_DF_01.WAV 
            if (videoTags.getLength() > 0) {
                videoID = videoTags.item(0).getTextContent().trim().substring(0, 23);
            } 
            result.add(videoID);
        } catch (SAXException | ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;    }

    @Override
    public IDList getTranscripts4Audio(String audioID) throws IOException {
        try {
            IDList list = new IDList("Transcript");
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_a_id", audioID}
            };
            String xml = HTTPMethodHelper.callCommand("getTranscripts4Audio", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("t_id");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
            return list;
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }    
    }

    @Override
    public IDList getTranscripts4Video(String videoID) throws IOException {
        try {
            IDList list = new IDList("Transcript");
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_v_id", videoID}
            };
            String xml = HTTPMethodHelper.callCommand("getTranscripts4Video", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("t_id");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
            return list;
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public IDList getAudios4Transcript(String transcriptID) throws IOException {
        IDList result = new IDList("audio");
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_doc_id", transcriptID}
            };
            String xml = HTTPMethodHelper.callCommand("getTranscriptAudioID", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList audioTags = doc.getDocumentElement().getElementsByTagName("audio");
            // never more than one audio in the current backend...
            //FOLK_E_00076_SE_01_A_01_DF_01.WAV 
            String audioID = audioTags.item(0).getTextContent().trim().substring(0,23);
            result.add(audioID);
        } catch (SAXException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public IDList getVideos4Transcript(String transcriptID) throws IOException {
        IDList result = new IDList("audio");
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_doc_id", transcriptID}
            };
            String xml = HTTPMethodHelper.callCommand("getTranscriptVideoIDs", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList videoTags = doc.getDocumentElement().getElementsByTagName("video");
            
            // 0, 1 or 2 videos in the current backend
            for (int i=0; i<videoTags.getLength(); i++){
                //FOLK_E_00076_SE_01_A_01_DF_01.WAV 
                String videoID = videoTags.item(i).getTextContent().trim().substring(0,23);
                result.add(videoID);
            }
        } catch (SAXException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }



    @Override
    public String getDescription() {
        return "AGD@IDS Backend using the Oracle database";
    }


    @Override
    public IDList getSpeakers4SpeechEvent(String speechEventID) {
        IDList list = new IDList("Speaker");
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_se_id", speechEventID}
            };
            String xml = HTTPMethodHelper.callCommand("getSpeakers4SpeechEvent", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("s_id");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
        }   catch (IOException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @Override
    public IDList getAvailableValues(String corpusID, String metadataKeyID) {
        IDList list = new IDList("AvailableValue");
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_corpus_id", corpusID},
                {"v_meta_field", metadataKeyID}
            };
            String xml = HTTPMethodHelper.callCommand("getAvailableValues", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("distinct-value");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                String id = element.getTextContent();
                list.add(id);
            }
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    /*@Override
    public String getMetadataValue(String level, String DGDObjectID, String metadatum) {
        String meta = null;
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {level, DGDObjectID},
                {"v_meta_field", metadatum}
            };
            String xml = HTTPMethodHelper.callCommand("getMetadataValue", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("xml_data");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                meta = element.getTextContent();
//                meta.add(id);
            }
        } catch (IOException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return meta;
    }    */
    
    
    /*@Override
    public String getMetadataValue(String level, String DGDObjectID, String metadatum, String speakerID) {
        String meta = null;
        try {
            String[][] parameters = {
                {"v_session_id", sessionID},
                {level, DGDObjectID},
                {"v_s_id", speakerID},
                {"v_meta_field", metadatum}
            };
            String xml = HTTPMethodHelper.callCommand("getMetadataValue", parameters);
            Document doc = IOHelper.DocumentFromText(xml);
            NodeList childNodes = doc.getElementsByTagName("xml_data").item(0).getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element element = (Element) (childNodes.item(i));
                meta = element.getTextContent();
            }
        } catch (IOException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return meta;
    }*/
    
    
    

    @Override
    public String getAnnotationBlockID4TokenID(String transcriptID, String tokenID) {
        try {
            // Added 18-03-2019
            // Using statement we can now also do direct SQL queries
            // on the Oracle database
            // we may not need this in the long run
            // for now, it serves the purpose of determining c-ids for w-ids after queries
            Context initContext = new InitialContext();
            // There is an error here:
            /*Jun 17, 2019 1:12:13 PM org.zumult.backend.implementations.DGD2Oracle getAnnotationBlockID4TokenID
            SCHWERWIEGEND: null
            javax.naming.NoInitialContextException: Need to specify class name in environment or system property, or as an applet parameter, or in an application resource file:  java.naming.factory.initial
            at javax.naming.spi.NamingManager.getInitialContext(NamingManager.java:662)
            at javax.naming.InitialContext.getDefaultInitCtx(InitialContext.java:313)
            at javax.naming.InitialContext.getURLOrDefaultInitCtx(InitialContext.java:350)
            at javax.naming.InitialContext.lookup(InitialContext.java:417)
            at org.zumult.backend.implementations.DGD2Oracle.getAnnotationBlockID4TokenID(DGD2Oracle.java:666)
            at org.zumult.backend.implementations.TestBackend.doit(TestBackend.java:83)
            at org.zumult.backend.implementations.TestBackend.main(TestBackend.java:41)*/
            Context envContext  = (Context)initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource)envContext.lookup("jdbc/TestDB");
            connection = ds.getConnection();
            stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String query = "SELECT C_ID FROM PRAGDB.T_FOLKER_DGD_W WHERE T_ID='" + transcriptID + "' AND W_ID='" + tokenID + "'";
            ResultSet srs = stmt.executeQuery(query);
            
            if (srs.next()){
                return srs.getString(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(DGD2Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ERROR";
    }
    
    @Override
    public AnnotationBlock getAnnotationBlock(String transcriptID, String annotationBlockId) throws IOException {

        String[][] parameters = {
                {"v_session_id", sessionID},
                {"v_doc_id", transcriptID},
                {"v_c_id", annotationBlockId}
        };    
        
        try{              
            String xml = HTTPMethodHelper.callCommand("getAnnotationBlockXML", parameters);        
            xml = HTTPMethodHelper.stripXMLResponse(xml);
            Document doc = IOHelper.DocumentFromText(xml);
            AnnotationBlock annotationBlock = new ISOTEIAnnotationBlock(doc);
            return annotationBlock;
        } catch (ParserConfigurationException | SAXException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Protocol getProtocol(String protocolID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProtocol4SpeechEvent(String speechEventID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Measure getMeasure4SpeechEvent(String speechEventID, String type,String reference) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDList getMeasures4Corpus(String corpus) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IDList getAvailableValuesForAnnotationLayer(String corpusID, String annotationLayerID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationTagSet getAnnotationTagSet(String annotationTagSetID) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
