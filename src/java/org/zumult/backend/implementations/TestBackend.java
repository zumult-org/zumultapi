/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.folker.utilities.TimeStringFormatter;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.io.XMLReader;
import org.zumult.objects.AnnotationBlock;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.Location;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.TokenFilter;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.NegatedFilter;
import org.zumult.objects.implementations.TokenListTokenFilter;

/**
 *
 * @author Thomas_Schmidt
 */
public class TestBackend {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestBackend().doit();
        } catch (IOException ex) {
            Logger.getLogger(TestBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestBackend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit() throws IOException, Exception {
        //DGD2Oracle dgd2Oracle = new DGD2Oracle();

        

        BackendInterface bi = BackendInterfaceFactory.newBackendInterface();
        
        Transcript tr = bi.getTranscript("FOLK_E_00121_SE_01_T_04");
        System.out.println(tr.getAnnotationBlockID("c766", 3));

        System.exit(0);

        String sStart = bi.getNearestAnnotationBlockID4TokenID("FOLK_E_00024_SE_01_T_03", "w2763");
        String eEnd = bi.getNearestAnnotationBlockID4TokenID("FOLK_E_00024_SE_01_T_03", "w2765");
        System.out.println(sStart + " / " + eEnd + " / " );
        
        String annotationBlockID = tr.getAnnotationBlockID(sStart, -3);
        String annotationBlockID2 = tr.getAnnotationBlockID(eEnd, +3);
        Transcript pP = tr.getPart(annotationBlockID, annotationBlockID2, true);
        System.out.println(pP.toXML());

        double tT = pP.getStartTime();
        
        System.out.println(annotationBlockID + " / " +annotationBlockID2 + " / " + tT);



        //bi.getProtocol4SpeechEvent(speechEventID);
        bi.getProtocol("FOLK_E_00349_SE_01_P_01");
        
        IDList mediaIDs = bi.getAudios4SpeechEvent("FOLK_E_00126_SE_01");
        double totalDuration = 0.0;
        for (String mediaID : mediaIDs){
            totalDuration+=bi.getMedia(mediaID, Media.MEDIA_FORMAT.WAV).getDuration();
        }
        String durationString = TimeStringFormatter.formatSeconds(totalDuration, true, 0);
        
        System.out.println(totalDuration + " / " + durationString);
        
        
        MetadataKey mkey = bi.findMetadataKeyByID("v_e_se_inhalt");
        String inhalt = bi.getSpeechEvent("FOLK_E_00001_SE_01").getMetadataValue(mkey);
        System.out.println(inhalt);

        //String tid = bi.getAnnotationBlockID4TokenID("FOLK_E_00001_SE_01_T_01", "w5");
        String tid = "c10";
        int amount = -7;
        String abid = tr.getAnnotationBlockID(tid, amount);
        System.out.println(tid + " " + amount + " = " + abid);
        
        System.exit(0);
        
        
        String idx = bi.getAnnotationBlockID4TokenID("FOLK_E_00047_SE_01_T_01", "c694");
        System.out.println(idx);
        
        
        
        //SearchResult sr = bi.search("[word=\"kannst\"]", "cqp", "3.4", "FOLK", "0-t,0-t", 10000, 1, Boolean.FALSE, idx);
        
        
        /*ArrayList<Hit> hits = sr.getHits();
        for (Hit hit : hits){
            ArrayList<Match> matches = hit.getMatches();
            for (Match match : matches){
                System.out.println(match.getID());
            }
        }*/
        
        //String xml = DGD2SearchResultSerializer.displayKWICinXML(sr);
        //System.out.println(xml);
        
        
        Transcript t = bi.getTranscript("FOLK_E_00069_SE_01_T_01");
        System.out.println(t.getID());
        System.out.println(t.getSpeakerIDBySpeakerInitials("HG"));
        
        System.out.println(t.getAnnotationBlockID("c44", -3));
        
        
        System.out.println(bi.getDescription());
        
        System.out.println(bi.getCorpus("GDSA").getName("de"));
        System.out.println(bi.getCorpus("GDSA").getName("en"));
        System.out.println(bi.getCorpus("GDSA").getDescription("de"));
        System.out.println(bi.getCorpus("GDSA").getDescription("en"));
        
        System.out.println(bi.getCorpus("FOLK").getDescription("en"));
        
        
        IDList audios = bi.getAudios4Transcript("FOLK_E_00001_SE_01_T_01");
        Media audio = bi.getMedia(audios.get(0), Media.MEDIA_FORMAT.WAV);
        System.out.println("Duration " + audio.getDuration());
        String url = audio.getURL();
        System.out.println(url);
        Media partAudio = audio.getPart(0.5, 10.0);
        System.out.println(partAudio.getURL());
        

        IDList videos = bi.getVideos4Transcript("FOLK_E_00069_SE_01_T_01");
        Media video = bi.getMedia(videos.get(0), Media.MEDIA_FORMAT.MPEG4_ARCHIVE);
        String url2 = video.getURL();
        System.out.println(url2);
        Media partVideo = video.getPart(0.5, 10.0);
        System.out.println(partVideo.getURL());


        System.exit(0);
        
        Set<MetadataKey> speechEventMetadataKeys2 = bi.getCorpus("FOLK").getSpeechEventMetadataKeys();
        for (MetadataKey key : speechEventMetadataKeys2){
            String keyID = key.getID();
            String keyName = key.getName("de");
        }
        /*Event e = bi.getEvent("ZW--_E_00099");
        Location loc = e.getLocation();
        System.out.println(loc.getPlacename() + " " + loc.getLatitude());*/
        
        //System.exit(0);
        
        
        
        Transcript parti = t.getPart("w27", "w336", true);
        System.out.println(parti.toXML());
        System.out.println(parti.getStartTime());
        System.out.println(parti.getEndTime());
        
        //System.exit(0);
        
        
        bi.getSpeakerInSpeechEvent("FOLK_E_00001_SE_01", "FOLK_S_00001");
        
        //System.exit(0);
        Event e = bi.getEvent("DR--_E_00004");
        IDList l = e.getSpeechEvents();
        System.out.println(l.toXML());
        
        SpeechEvent se = bi.getSpeechEvent("FOLK_E_00069_SE_01");
        IDList l2 = se.getTranscripts();
        System.out.println(l2.toXML());
        
        IDList l3 = se.getMedia();
        System.out.println(l3.toXML());

        IDList l4 = se.getSpeakers();
        System.out.println(l4.toXML());



        //dgd2Oracle.getAudios4Transcript("FOLK_E_00001_SE_01_T_01");
        //dgd2Oracle.getVideos4Transcript("FOLK_E_00069_SE_01_T_01");
        
        Transcript transcript = bi.getTranscript("FOLK_E_00001_SE_01_T_01");
        System.out.println(transcript.getNumberOfTokens());
        //System.out.println(transcript.getStartTime());
        //System.out.println(transcript.getEndTime());
        
        Transcript partTranscript = transcript.getPart(60, 120, true);
        //System.out.println(partTranscript.toXML());
        System.out.println(partTranscript.getNumberOfTokens());
        System.out.println(partTranscript.getStartTime());
        System.out.println(partTranscript.getEndTime());
        System.exit(0);

        Corpus c = bi.getCorpus("FOLK");
        //System.out.println(c.toXML());
        
        IDList ll = bi.getAudios4Transcript("FOLK_E_00001_SE_01_T_01");
        ll.forEach((id) -> {
            System.out.println(id);
        });
        
        AnnotationBlock ab = bi.getAnnotationBlock("FOLK_E_00001_SE_01_T_01", "c2");
        System.out.println(ab.toXML());
        
        System.out.println("--> " + bi.getAnnotationBlockID4TokenID("FOLK_E_00001_SE_01_T_01", "w9"));
        
        Transcript pfTranscript = bi.getTranscript("FOLK_E_00001_SE_01_T_01");
        System.out.println(pfTranscript.toXML());
        System.out.println(pfTranscript.getPart(0, 30, true).toXML());
        
        Set<MetadataKey> speechEventMetadataKeys = bi.getCorpus("FOLK").getSpeechEventMetadataKeys();
        for (MetadataKey metadataKey : speechEventMetadataKeys){
            System.out.println("\n--------------------");
            System.out.println(metadataKey.getID());
            System.out.println("--------------------");
            IDList values = bi.getAvailableValues("FOLK", metadataKey);
            for (String value : values){
                System.out.println(value);
            }
        }
        
        
        //System.exit(0);
        
        
        long start = System.currentTimeMillis();
        TokenList tokenList = pfTranscript.getTokenList("lemma");
        int origSize = tokenList.keySet().size();
        System.out.println((System.currentTimeMillis() - start) + "ms for token listing");
        
      /* FOLK_LEMMALIST_1000.xml does exit any more  
        TokenList otherTokenList = XMLReader.readTokenListFromInternalResource("/data/FOLK_LEMMALIST_1000.xml");
        tokenList.intersect(otherTokenList);
        System.out.println(tokenList.toXML());*/
        
        TokenList filterTokenList = XMLReader.readTokenListFromInternalResource("/data/MEASURE_1_POS_FILTER.xml");
        TokenFilter filter = new NegatedFilter(new TokenListTokenFilter("lemma", filterTokenList));
        TokenList filteredTokenList = pfTranscript.getTokenList("lemma", filter);
        System.out.println(filteredTokenList.toXML());
        int filteredSize = filteredTokenList.keySet().size();
        
        System.out.println(origSize + " ===> " + filteredSize);

        //System.exit(0);
        
        
        System.out.println(pfTranscript.toXML());
        Transcript part = pfTranscript.getPart(0.0, 30.0, true);
        
        
        //System.exit(0);
        
        Media media = bi.getMedia("BETV_E_00001_SE_01_V_01_DF_01");
        //System.out.println(media.getURL());
        
        
        System.out.println("******************************");
        
        
        IDList corpora = bi.getCorpora();
        for (String corpusID : corpora){
            //System.out.println(corpusID);
            Corpus corpus = bi.getCorpus(corpusID);
            System.out.println("============ Corpus: " + corpusID);
            Set<MetadataKey> eventMeta = corpus.getEventMetadataKeys();
            for (MetadataKey em : eventMeta){
                System.out.println(em.getID() + " / " + em.getName("de"));
            }
            
            //System.out.println(corpus.toXML());
            
            IDList events = bi.getEvents4Corpus(corpusID);
            for (String eventID : events){
                try {
                    Event event = bi.getEvent(eventID);                
                } catch (Throwable ex){
                    System.out.println("**************************");
                    System.out.println(eventID);
                    ex.printStackTrace();
                    System.out.println("**************************");
                }
            }
          
            IDList speakers = bi.getSpeakers4Corpus(corpusID);
            for (String speakerID : speakers){
                try{
                    Speaker speaker = bi.getSpeaker(speakerID);
                } catch (Throwable ex){
                    System.out.println("**************************");
                    System.out.println(speakerID);
                    ex.printStackTrace();
                    System.out.println("**************************");                    
                }
            }
        }
        
        
        System.out.println("******************************");
        

        /*SpeechEvent speechEvent = dgd2Oracle.getSpeechEvent("FOLK_E_00001_SE_01");
        System.out.println(speechEvent.toXML());*/
        
        /*Event event = dgd2Oracle.getEvent("AD--_E_00086");
        System.out.println(event.toXML());
        System.out.println(event.getDate());*/

        Corpus zw = bi.getCorpus("ZW--");
        Speaker s = bi.getSpeaker("ZW--_S_00021");
        Set<String> lt = zw.getSpeakerLocationTypes();
        for (String type : lt) {
            List<Location> locations = s.getLocations(type);
            for (Location loc : locations){
                System.out.println(loc.getPlacename());
                //System.out.println(l.toXML());
            }
        }


    }
    
}
