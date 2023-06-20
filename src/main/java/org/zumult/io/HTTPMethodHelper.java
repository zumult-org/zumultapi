/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Schmidt
 */
public class HTTPMethodHelper {
    
    public static final String[] COMMANDS = {
    };
    
    // the maximum length allowed for a result to be processed
    // with a size of 928.725.043, I get reliably reproducible OOM errors
    // so I need to set this to a value below that
    //public static final long MAX_CONTENT_LENGTH = Long.MAX_VALUE;
    public static final long MAX_CONTENT_LENGTH = 500000000;
    
    // 14-04-2016
    // I wonder if DGD_HOST should be replaced with 
    // DGD_URL. The latter does not contain the port number which
    // is causing trouble elsewhere. Since it does not seem to 
    // cause trouble here, I am leaving it as it is because
    // you are not meant to change a winning team
    private static String getURLForCommand(String command){
        String urlString = "";
        if (command.equals("getCorpusXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_corpus_xml_http";
        } else if (command.equals("getEventXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_event_xml_http";            
        } else if (command.equals("getSpeakerXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_speaker_xml_http";            
        } else if (command.equals("getTranscriptXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_transcript_xml_http";
        } else if (command.equals("getContributionXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_contribution_xml_http";            
        } else if (command.equals("getContributionsXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_contributions_xml_http";            
        } else if (command.equals("queryTranscriptLemma")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.query_transcript_lemma_http";            
        } else if (command.equals("queryCLARIN")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.query_transcript_w_clarin";            
        } else if (command.equals("queryTranscriptWord")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.query_transcript_w_http";            
        } else if (command.equals("queryTranscriptXPath")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.query_transcript_struct_http";            
        } else if (command.equals("getTranscriptAudioID")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_transcript_audio_id_http";             
        } else if (command.equals("getCorpusEventIDs")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_corpus_event_ids_http";             
        } else if (command.equals("getCorpusSpeakerIDs")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_corpus_speaker_ids_http";             
        } else if (command.equals("getTranscriptVideoIDs")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_transcript_video_ids_http";             
        } else if (command.equals("querySpeakers")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.query_speakers_struct_http";            
        } else if (command.equals("queryEvents")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.query_events_struct_http";            
        } else if (command.equals("querySpeakersInEvents")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.query_se_s_struct_http";            
        } else if (command.equals("getAvailableValues")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_available_values_http";            
        } else if (command.equals("getMetadataValue")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_metadata_value_http";            
        } else if (command.equals("getSpeakerEventIDs")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_speaker_event_ids_http";            
        } else if (command.equals("getNewSessionID")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_new_session_id_http";            
        } else if (command.equals("userAction")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.proc_user_action_http";            
        } else if (command.equals("userDetails")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_user_data_http";            
        } else if (command.equals("getSharedObject")){
            // https://pragora-2.ids-mannheim.de/dgd/pragdb.dgd20.get_user_data_by_share_id_http?v_session_id=4EDABC52DD1F6937EEF781AB269EB8EE&v_share_id=Qy7WJZ
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_user_data_by_share_id_http";            
        } else if (command.equals("hasVideo")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.has_video_http";            
        }  else if (command.equals("getSpeechEvents4Event")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.getSpeechEvents4Event";            
        }  else if (command.equals("getTranscripts4SpeechEvent")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.getTranscripts4SpeechEvent";            
        }  else if (command.equals("getAudios4SpeechEvent")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.getAudios4SpeechEvent";            
        }  else if (command.equals("getVideos4SpeechEvent")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.getVideos4SpeechEvent";            
        }  else if (command.equals("getTranscripts4Audio")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.getTranscripts4Audio";            
        }  else if (command.equals("getTranscripts4Video")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.getTranscripts4Video";            
        } else if (command.equals("getSpeakers4SpeechEvent")) {
            urlString += Constants.DGD_HOST + "/dgd/pragdb.dgd20.getSpeakers4SpeechEvent";
        } else if (command.equals("getAnnotationBlockXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_annotationblock_xml_http";
        } else if (command.equals("getISOTranscriptXML")){
            urlString+=Constants.DGD_HOST + "/dgd/pragdb.dgd20.get_iso_transcript_xml_http";                
        }
                  
        return urlString;
    }

    private static String getParameterString(String[][] parameters) throws UnsupportedEncodingException{
        String parameterString = "";
        int count=0;
        for (String[] parameterPair : parameters){
            if (count>0) {
                parameterString+="&";
            }
            parameterString+=parameterPair[0];
            parameterString+="=";
            parameterString+=URLEncoder.encode(parameterPair[1], "UTF-8");
            count++;
        }
        return parameterString;
    }
    
    private static URL makeURLWithParameters(String command, String[][] parameters) throws MalformedURLException, UnsupportedEncodingException{
        String urlString = getURLForCommand(command);
        if (parameters!=null){
            String parameterString = getParameterString(parameters);
            urlString+="?"+parameterString;
        }
        return new URL(urlString);
    }
    
    public static String callCommand(String command, String[][] parameters){
        //System.out.println("1) Calling " + command);
        try {
            URL url = makeURLWithParameters(command, parameters);
            //System.out.println("==> HTTPMethodHelper Calling " + url.toString());
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
            
            // new for version 2.9 - to prevent out of memory errors
            long contentLength = yc.getContentLengthLong();
            if (contentLength>MAX_CONTENT_LENGTH){
                // return an error
                String errorString = "<response>\n" +
                                        "<session_id>E4755382F21FF60DCE89A15978562DC8</session_id>\n" +
                                        "<v_w>l(au|ief.*</v_w>\n" +
                                        "<v_pos/>\n" +
                                        "<v_lemma>.*laufen</v_lemma>\n" +
                                        "<v_n/>\n" +
                                        "<v_corpus_id>FOLK</v_corpus_id>\n" +
                                        "<v_regexp>yes</v_regexp>\n" +
                                        "<result>\n" +
                                        "<error>\n" +
                                        "Tomcat HTTPMethodHelper says the result is too large (" + Long.toString(contentLength) + ") to be processed." +
                                        "</error>\n" +
                                        "</result>\n" +
                                        "</response>";
                return errorString;
            }
                
            //System.out.println("Content length: " + yc.getContentLengthLong());
            String inputLine;
            StringBuilder result = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                // this one can cause an out of memory error
                // for ***very large*** results (like [A-Z]+ POS query
                // on FOLK
                result.append(inputLine);
            }
            in.close();
            return result.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "<error>" + ex.getLocalizedMessage() + "</error>";
        }        
    }
    
    // same as above, but passes parameters in POST request instead of in the URL
    public static String callPOSTCommand(String command, String[][] parameters){
        try {
            //String body = "param1=" + URLEncoder.encode( "value1", "UTF-8" ) + "&" +
            //              "param2=" + URLEncoder.encode( "value2", "UTF-8" );
            URL url = HTTPMethodHelper.makeURLWithParameters(command, null);
            String body = HTTPMethodHelper.getParameterString(parameters);
            //System.out.println(body);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type",
                                          "text/xml" );
            connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            
            int status = connection.getResponseCode();
            //System.out.println("Status: " + status);
            


            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder result = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                result.append(inputLine);
            }
            reader.close();
            writer.close();      

            return result.toString();


        } catch (Exception ex) {
            ex.printStackTrace();
            return "<error>" + ex.getLocalizedMessage() + "</error>";
        } 
    }
    
    public static String stripXMLResponse(String responseXML){
        try {
            Document d = FileIO.readDocumentFromString(responseXML);
            /*<response>
                <session_id>15C60ACAD97DA98F6CF2A73A84D47529</session_id>
                <xml_data>
                    <Ereignis ... */
            Element contentElement = (Element) d.getRootElement().getChild("xml_data").getContent(0);
            if (contentElement==null){
                //i.e. this is an error response
                return responseXML;
            }
            return IOUtilities.elementToString(contentElement);
        } catch (Exception ex) {
            return responseXML;
        } 
        
    }
    
    public static String getPID(String dgdObjectID){
        try {
            String urlString = "http://repos.ids-mannheim.de/cgi-bin/retrieve_handle-mysql.cgi?agdid=" + dgdObjectID;
            System.out.println(urlString);
            URL url = new URL(urlString);
            //System.out.println("2) Calling " + url.toString());
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
            
            String inputLine;
            StringBuilder result = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
            return result.toString();
        } catch (MalformedURLException ex) {
            return "Error: " + ex.getLocalizedMessage();
        } catch (IOException ex) {
            return "Error: " + ex.getLocalizedMessage();
        }        
    }
    
}
