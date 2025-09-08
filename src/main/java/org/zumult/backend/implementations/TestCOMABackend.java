/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.Configuration;
import org.zumult.backend.MetadataFinderInterface;
import org.zumult.objects.Corpus;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.Transcript;
import org.zumult.query.KWIC;
import org.zumult.query.SearchResultPlus;
import org.zumult.query.serialization.DefaultQuerySerializer;

/**
 *
 * @author thomas.schmidt
 */
public class TestCOMABackend {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new TestCOMABackend().doit();
    }

    private void doit() {
        try {
            System.out.println(
                    "--- Configuration path : " +
                    Configuration.getBackendInterfaceClassPath()
            );
            
            System.out.println(
                    "--- Metadata path : " +
                    Configuration.getMetadataPath()
            );
            BackendInterface bi = new COMAFileSystem(); 
            
            Random random = new Random();
            IDList allCorpusIDs = bi.getCorpora();
            String randomCorpusID = allCorpusIDs.get(random.nextInt(allCorpusIDs.size()));
            System.out.println(randomCorpusID);
            IDList allTranscriptIDs =  bi.getTranscripts4Corpus(randomCorpusID);
            //System.out.println(String.join("  //  ", allTranscriptIDs));
            String randomTranscriptID = allTranscriptIDs.get(random.nextInt(allTranscriptIDs.size()));
            String speechEventID = bi.getSpeechEvent4Transcript(randomTranscriptID);
            
            System.out.println("SpeechEvent : " + speechEventID);
            
            System.out.println(randomCorpusID + " / " + randomTranscriptID);

            
            Set<MetadataKey> metadataKeysGTXG = bi.getMetadataKeys4Corpus("GTXG", ObjectTypesEnum.TRANSCRIPT);
            for (MetadataKey mk : metadataKeysGTXG){
                System.out.println(mk.getID() + " --- " + mk.getName("en"));
            }
            
            IDList transcriptsGTXG = bi.getSpeechEvent("GTXG_E_0001").getTranscripts();
            System.out.println(String.join("\n", transcriptsGTXG));
            
            System.exit(0);
            Corpus remCorpus = bi.getCorpus("ESLO");
            //Media media = bi.getMedia("M_COMM_ESLO2_ENT_1005_REMOTE");
            //System.out.println(media.getURL());
            
            Transcript remTrans = bi.getTranscript("ESLO1_CONF_503_C_TRANS");
            System.out.println("========= " + remTrans.getID());
            
            Media media = bi.getMedia(bi.getAudios4Transcript(remTrans.getID()).get(0));
            System.out.println(media.getURL());
            

            //System.exit(0);
            
            SearchResultPlus searchResult = bi.search("[word=\"en\"]", null, null, "ESLO", null, 1000, null, null, null, null, null);
            long t1 = System.currentTimeMillis();
            System.out.println("Search done.");
            KWIC kwic = bi.getKWIC(searchResult, "5-t,5-t");
            //KWIC kwic = bi.exportKWIC(searchResult, "3-t,3-t", "xml");
            long t2 = System.currentTimeMillis();
            DefaultQuerySerializer qs = new DefaultQuerySerializer();
            //File f = qs.createKWICDownloadFile(kwic, "xml", bi);
            String kwicXML = qs.displayKWICinXML(kwic);
            System.out.println("XML done.");
            long t3 = System.currentTimeMillis();
            System.out.println(kwicXML);
            //System.out.println(IOHelper.readUTF8(f));
            
            System.out.println("KWIC: " + (t2 - t1) + " / " + "Serialize: " + (t3-t2));
            
            System.exit(0);
            
            SpeechEvent speechEventX = bi.getSpeechEvent("RC_Study_Day1_Trial-2");
            String corpusID = bi.getCorpus4Event(bi.getEvent4SpeechEvent(speechEventX.getID()));
            IDList videoList = bi.getVideos4SpeechEvent(speechEventX.getID());
            IDList audioList = bi.getAudios4SpeechEvent(speechEventX.getID());
            Set<MetadataKey> metadataKeysX = bi.getMetadataKeys4Corpus(corpusID, ObjectTypesEnum.MEDIA);
            for (String videoID : videoList){
                Media video = bi.getMedia(videoID);
                for (MetadataKey mk : metadataKeysX){
                    String y = video.getMetadataValue(mk);
                    System.out.println(mk.getName("en") + " / " + y);
                }
            }
            
            
            System.exit(0);


            System.out.println("--- Initialised COMAFileSystem.");
            
            for (String id : bi.getCorpora()){
                System.out.println("--- Corpus : " + id);
            }
            
            
            //String transcriptID = "IDE57E5B6C-E67B-B454-E462-4E4868C79333";
            //String transcriptID = "ISO_manv_2018_e_triage";
            String transcriptID = "IDE57E5B6C-E67B-B454-E462-4E4868C79333";
            
            IDList videos4Transcript = bi.getVideos4Transcript(transcriptID);
            System.out.println("Videos : " + String.join(" / ", videos4Transcript) );
            
            String tokenID = "w120";
            Transcript transcript = bi.getTranscript(transcriptID);
            Transcript part = transcript.getPart(10.0, 20.0, true);
            System.out.println(part.toXML());
            part.setTimelineToZero();
            System.out.println(part.toXML());
            //System.out.println(IOHelper.DocumentToString(part.getDocument()));
            
            Transcript transcript2 = bi.getTranscript(transcriptID, Transcript.TranscriptFormats.EXB);
            
            //System.out.println(transcript2.toXML());
            
            System.exit(0);
            
            String audioID = transcript.getMetadataValue(bi.findMetadataKeyByID("Transcript_Recording ID"));
            String url = bi.getMedia(audioID).getURL();
            System.out.println("--- Media URL for transcript " + transcriptID + " : " + url);
            bi.getMedia(audioID).getPart(0.5, 1.5);
            double time = transcript.getTimeForID(tokenID);
            System.out.println("--- Time for token " + tokenID + " in  transcript " + transcriptID + " : " + time);
            
            
            
            System.out.println(Configuration.getSearchIndexPath());
            
            long start = System.currentTimeMillis();
            SpeechEvent speechEvent = bi.getSpeechEvent("IDC9DB990B-E34B-3EA5-41AC-FFE8D4347196");
            long now1 = System.currentTimeMillis();
            IDList transcripts = speechEvent.getTranscripts();
            long now2 = System.currentTimeMillis();            
            for (String id : transcripts){
                System.out.println("---  Transcript for " + speechEvent.getName() + " : " + id);
            }

            MetadataKey communicationTypeKey = bi.findMetadataKeyByID("SpeechEvent_Communication type");
            System.out.println("--- Key Name: " + communicationTypeKey.getName("en"));
            System.out.println("--- Key ID: " + communicationTypeKey.getID());
            IDList places = bi.getAvailableValues("EXMARaLDA-DemoKorpus", communicationTypeKey);
            for (String place : places){
                System.out.println("--- Available value for " + communicationTypeKey.getName("en") + " : "+ place);
            }
            
            IDList tvdebates = ((MetadataFinderInterface)(bi))
                    .findSpeechEventsByMetadataValue("EXMARaLDA-DemoKorpus", communicationTypeKey, "television debate");
            for (String tvdebate : tvdebates){
                System.out.println("--- TV debate : " + bi.getSpeechEvent(tvdebate).getName());
            }

            System.exit(0);
            
            // what follows is for TGDP
            
            long time1 = (now1 - start);
            long time2 = (now2 - now1);
            System.out.println(time1 + "ms for getting speech event");
            System.out.println(time2 + "ms for getting transcript IDs");
            
            
            Corpus corpus = bi.getCorpus("TGDP");
            System.out.println("Here.");
            System.out.println(corpus.getName("de"));
            System.out.println(corpus.getDescription("de"));

            Speaker sp = bi.getSpeaker("Speaker_0001");
            
            Set<MetadataKey> metadataKeys2 = corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER);
            for (MetadataKey key : metadataKeys2){
                System.out.println(key.getID() + " / " + key.getName("en") +  " : " + sp.getMetadataValue(key));
            }

            Transcript tr = bi.getTranscript("1-1-1-3-a");
            Set<MetadataKey> metadataKeys1 = corpus.getMetadataKeys(ObjectTypesEnum.TRANSCRIPT);
            for (MetadataKey key : metadataKeys1){
                System.out.println(key.getID() + " / " + key.getName("en") +  " : " + tr.getMetadataValue(key));
            }
            MetadataKey key1 = bi.findMetadataKeyByID("Transcript_Section Title");
            System.out.println(key1.getName("en"));
            System.out.println(tr.getMetadataValue(key1));

            

            IDList audios4Transcript = bi.getAudios4Transcript(tr.getID());
            for (String a : audios4Transcript) {
                System.out.println("Audio " + a);
                Media m = bi.getMedia(a, Media.MEDIA_FORMAT.MP3);
                System.out.println(m.getURL());
            }
            
            System.exit(0);
            
            IDList transcripts4Audio = bi.getTranscripts4Audio("MID89259CCE-7F0B-6F0F-46AB-B80563FFDBC9");
            for (String t : transcripts4Audio) System.out.println(t);
            
            
            
            SpeechEvent se = bi.getSpeechEvent("CID28DDACF8-8095-E84D-C97A-35BFB9DCED23");
            System.out.println(se.toXML());
            
            
            
            IDList audios = bi.getAudios4SpeechEvent("CID28DDACF8-8095-E84D-C97A-35BFB9DCED23");
            for (String audio : audios){
                System.out.println(audio);
                Media m = bi.getMedia(audio);
                System.out.println(m.getURL());
            }
            
            
            Speaker s = bi.getSpeaker("SIDC5846757-FA4D-82E5-FAAE-0187717CC022");
            System.out.println(s.toXML());
            
            Transcript t = bi.getTranscript("CIDIDB8994C55-16A2-5698-F070-DE0102C8EC3E");
            System.out.println(t.toXML());
            
            System.exit(0);
            
            Media m = bi.getMedia("MID7D516D45-D94A-24EF-8D21-4B0A4DC891CA");
            System.out.println(m.getURL());
            
            String acronym = corpus.getAcronym();
            String desc = corpus.getDescription("en");
            String name = corpus.getName("en");
            System.out.println(acronym + " / " + desc + " / " + name);
            
            System.out.println("-------------");
            
            Set<MetadataKey> metadataKeys = corpus.getMetadataKeys();            
            for (MetadataKey key : metadataKeys){
                System.out.println(key.getID());
                
                if (key.getLevel()==ObjectTypesEnum.SPEECH_EVENT){
                    System.out.println("=========>" + se.getMetadataValue(key));
                }
                
                //IDList avV = bi.getAvailableValues(corpus.getID(), key);
                //for (String av : avV) System.out.println("   " + av);
            }
            
            

            System.out.println("-------------");
            
            IDList events = bi.getEvents4Corpus("hamatac");
            for (String id : events) {
                System.out.println(id);
                for (MetadataKey mk : corpus.getMetadataKeys(ObjectTypesEnum.SPEECH_EVENT)){
                    String v = bi.getEvent(id).getMetadataValue(mk);
                    //System.out.println(bi.getEvent(id).getLocation().getCountry());
                    System.out.println(mk.getID() + " / " + mk.getName("de") + " : " + v);
                    //System.out.println(bi.getEvent(id).getDate().toString());
                }
            }

            IDList speakers = bi.getSpeakers4Corpus("hamatac");
            for (String id : speakers) {
                System.out.println(id);
                for (MetadataKey mk : corpus.getMetadataKeys(ObjectTypesEnum.SPEAKER)){
                    String v = bi.getSpeaker(id).getMetadataValue(mk);
                    //System.out.println(bi.getSpeaker(id).getLocations("Residence").get(0).getCountry());
                    System.out.println(mk.getName("de") + " : " + v);
                    //System.out.println(bi.getSpeaker(id).getDate().toString());
                }
            }
            

            
        } catch (IOException ex) {
            Logger.getLogger(TestCOMABackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestCOMABackend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
