/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.Configuration;
import org.zumult.objects.Corpus;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.ObjectTypesEnum;
import org.zumult.objects.Speaker;
import org.zumult.objects.SpeechEvent;
import org.zumult.objects.Transcript;

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
            System.out.println(":" + Configuration.getMetadataPath());
            BackendInterface bi = new COMAFileSystem();
            
            
            Corpus corpus = bi.getCorpus("ESLO-DEMO");
            System.out.println(corpus.getDescription("de"));
            
            Transcript tr = bi.getTranscript("ESLO2_ENT_1001_C");
            

            IDList audios4Transcript = bi.getAudios4Transcript("ESLO2_ENT_1001_C");
            for (String a : audios4Transcript) {
                System.out.println("Audio " + a);
            }
            System.exit(0);
            
            IDList transcripts4Audio = bi.getTranscripts4Audio("MID7D516D45-D94A-24EF-8D21-4B0A4DC891CA");
            for (String t : transcripts4Audio) System.out.println(t);
            
            
            
            SpeechEvent se = bi.getSpeechEvent("CID06C693EF-494A-913E-EBF1-6C9618CDCC46");
            //System.out.println(se.toXML());
            
            
            
            IDList audios = bi.getAudios4SpeechEvent("CID06C693EF-494A-913E-EBF1-6C9618CDCC46");
            for (String audio : audios){
                System.out.println(audio);
                Media m = bi.getMedia(audio);
                System.out.println(m.getURL());
            }
            
            
            Speaker s = bi.getSpeaker("IDCFE47938-ECCF-C666-4B4C-167C67319AB1");
            System.out.println(s.toXML());
            
            Transcript t = bi.getTranscript("CIDID93045167-C4FF-8EF6-36B8-F08AC9F9E331");
            System.out.println(t.toXML());
            
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
                for (MetadataKey mk : corpus.getSpeechEventMetadataKeys()){
                    String v = bi.getEvent(id).getMetadataValue(mk);
                    //System.out.println(bi.getEvent(id).getLocation().getCountry());
                    System.out.println(mk.getID() + " / " + mk.getName("de") + " : " + v);
                    //System.out.println(bi.getEvent(id).getDate().toString());
                }
            }

            IDList speakers = bi.getSpeakers4Corpus("hamatac");
            for (String id : speakers) {
                System.out.println(id);
                for (MetadataKey mk : corpus.getSpeakerMetadataKeys()){
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
