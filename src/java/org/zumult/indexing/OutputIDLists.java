/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.implementations.AGDFileSystem;
import org.zumult.objects.IDList;

/**
 *
 * @author josip.batinic
 */
public class OutputIDLists implements Indexer {
    
    String base_path = "src/java/data/IDLists/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OutputIDLists().index();
        } catch (IOException ex) {
            Logger.getLogger(OutputIDLists.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void index() throws IOException {
        try {
            long start = System.currentTimeMillis();
            
            BackendInterface backendInterface = BackendInterfaceFactory.newBackendInterface();
            // is it much faster this way?
            // YES, by a factor of about 1 gazillion!!!!
            backendInterface = new AGDFileSystem();
            
            IDList corpora = backendInterface.getCorpora();
            String corporaList = corpora.toXML();

            // output list of corpora
            Path fileXML = Paths.get(base_path + "corpora.xml");
            Files.write(fileXML, corporaList.getBytes("UTF-8"));
            Path fileTXT = Paths.get(base_path + "corpora.txt");
            Files.write(fileTXT, corpora);
            
            for (String corpus : corpora) {
                System.out.println("corpus: " + corpus);
                IDList events = backendInterface.getEvents4Corpus(corpus);
                String eventsList = events.toXML();

                // output list of events per corpus
                Path eventListXML = Paths.get(base_path + corpus + "/" + corpus + "_events.xml");
                eventListXML.toFile().getParentFile().mkdir();
                Files.write(eventListXML, eventsList.getBytes("UTF-8"));
                Path eventListTXT = Paths.get(base_path + corpus + "/" + corpus + "_events.txt");
                Files.write(eventListTXT, events);
                
                IDList speakers = backendInterface.getSpeakers4Corpus(corpus);
                String speakersList = speakers.toXML();


                // output list of speakers per corpus
                Path speakersListXML = Paths.get(base_path + corpus + "/" + corpus + "_speakers.xml");
                Files.write(speakersListXML, speakersList.getBytes("UTF-8"));
                Path speakersListTXT = Paths.get(base_path + corpus + "/" + corpus + "_speakers.txt");
                Files.write(speakersListTXT, speakers);
                
                IDList speechEvents4Corpus = new IDList("SpeechEvent");
                IDList transcripts4Corpus = new IDList("Transcript");
                for (String event : events) {
                    System.out.println("event: " + event);
                    IDList speechEvents = backendInterface.getSpeechEvents4Event(event);
                    speechEvents4Corpus.addAll(speechEvents);

                    for (String speechEvent : speechEvents) {
                        IDList transcripts = backendInterface.getTranscripts4SpeechEvent(speechEvent);
                        transcripts4Corpus.addAll(transcripts);
                    }
                }
                String transcriptsList = transcripts4Corpus.toXML();
                // output list of transcripts per corpus
                Path transcriptsListXML = Paths.get(base_path + corpus + "/" + corpus + "_transcripts.xml");
                Files.write(transcriptsListXML, transcriptsList.getBytes("UTF-8"));
                Path transcriptsListTXT = Paths.get(base_path + corpus + "/" + corpus + "_transcripts.txt");
                Files.write(transcriptsListTXT, transcripts4Corpus);
                
                
                String speechEventsList = speechEvents4Corpus.toXML();
                // output list of speech events per corpus
                Path speechEventListXML = Paths.get(base_path + corpus + "/" + corpus + "_speechEvents.xml");
                Files.write(speechEventListXML, speechEventsList.getBytes("UTF-8"));
                Path speechEventListTXT = Paths.get(base_path + corpus + "/" + corpus + "_speechEvents.txt");
                Files.write(speechEventListTXT, speechEvents4Corpus);
                
                System.out.println("speechEvents4Corpus: " + speechEvents4Corpus);
            }
            
            long end = System.currentTimeMillis();
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:sss");
            Date timeNeeded = new Date(end - start);
            System.out.println("time needed: " + dateFormat.format(timeNeeded));

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(OutputIDLists.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
        
    }
    
}
