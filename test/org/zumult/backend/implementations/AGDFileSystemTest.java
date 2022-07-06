/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.backend.implementations;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.zumult.objects.AnnotationLayer;
import org.zumult.objects.AnnotationTagSet;
import org.zumult.objects.Corpus;
import org.zumult.objects.Event;
import org.zumult.objects.IDList;
import org.zumult.objects.MetadataKey;
import org.zumult.objects.Protocol;
import org.zumult.objects.Speaker;
import org.zumult.objects.Transcript;

/**
 *
 * @author thomasschmidt
 */
public class AGDFileSystemTest {
    
    public AGDFileSystemTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getEvents4Corpus method, of class AGDFileSystem.
     */
    @Test
    public void testGetEvents4Corpus() throws Exception {
        System.out.println("getEvents4Corpus");
        String corpusID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getEvents4Corpus(corpusID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpeakers4Corpus method, of class AGDFileSystem.
     */
    @Test
    public void testGetSpeakers4Corpus() throws Exception {
        System.out.println("getSpeakers4Corpus");
        String corpusID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getSpeakers4Corpus(corpusID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpeechEvents4Event method, of class AGDFileSystem.
     */
    @Test
    public void testGetSpeechEvents4Event() throws Exception {
        System.out.println("getSpeechEvents4Event");
        String eventID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getSpeechEvents4Event(eventID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCorpus method, of class AGDFileSystem.
     */
    @Test
    public void testGetCorpus() throws Exception {
        System.out.println("getCorpus");
        String corpusID = "";
        AGDFileSystem instance = new AGDFileSystem();
        Corpus expResult = null;
        Corpus result = instance.getCorpus(corpusID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEvent method, of class AGDFileSystem.
     */
    @Test
    public void testGetEvent() throws Exception {
        System.out.println("getEvent");
        String eventID = "";
        AGDFileSystem instance = new AGDFileSystem();
        Event expResult = null;
        Event result = instance.getEvent(eventID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpeaker method, of class AGDFileSystem.
     */
    @Test
    public void testGetSpeaker() throws Exception {
        System.out.println("getSpeaker");
        String speakerID = "";
        AGDFileSystem instance = new AGDFileSystem();
        Speaker expResult = null;
        Speaker result = instance.getSpeaker(speakerID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTranscript method, of class AGDFileSystem.
     */
    @Test
    public void testGetTranscript() throws Exception {
        System.out.println("getTranscript");
        String transcriptID = "";
        AGDFileSystem instance = new AGDFileSystem();
        Transcript expResult = null;
        Transcript result = instance.getTranscript(transcriptID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTranscripts4SpeechEvent method, of class AGDFileSystem.
     */
    @Test
    public void testGetTranscripts4SpeechEvent() throws Exception {
        System.out.println("getTranscripts4SpeechEvent");
        String speechEventID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getTranscripts4SpeechEvent(speechEventID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAudios4SpeechEvent method, of class AGDFileSystem.
     */
    @Test
    public void testGetAudios4SpeechEvent() throws Exception {
        System.out.println("getAudios4SpeechEvent");
        String speechEventID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getAudios4SpeechEvent(speechEventID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVideos4SpeechEvent method, of class AGDFileSystem.
     */
    @Test
    public void testGetVideos4SpeechEvent() throws Exception {
        System.out.println("getVideos4SpeechEvent");
        String speechEventID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getVideos4SpeechEvent(speechEventID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTranscripts4Audio method, of class AGDFileSystem.
     */
    @Test
    public void testGetTranscripts4Audio() throws Exception {
        System.out.println("getTranscripts4Audio");
        String audioID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getTranscripts4Audio(audioID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTranscripts4Video method, of class AGDFileSystem.
     */
    @Test
    public void testGetTranscripts4Video() throws Exception {
        System.out.println("getTranscripts4Video");
        String videoID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getTranscripts4Video(videoID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAudios4Transcript method, of class AGDFileSystem.
     */
    @Test
    public void testGetAudios4Transcript() throws Exception {
        System.out.println("getAudios4Transcript");
        String transcriptID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getAudios4Transcript(transcriptID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVideos4Transcript method, of class AGDFileSystem.
     */
    @Test
    public void testGetVideos4Transcript() throws Exception {
        System.out.println("getVideos4Transcript");
        String transcriptID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getVideos4Transcript(transcriptID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpeakers4SpeechEvent method, of class AGDFileSystem.
     */
    @Test
    public void testGetSpeakers4SpeechEvent() throws Exception {
        System.out.println("getSpeakers4SpeechEvent");
        String speechEventID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getSpeakers4SpeechEvent(speechEventID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAvailableValues method, of class AGDFileSystem.
     */
    @Test
    public void testGetAvailableValues() {
        System.out.println("getAvailableValues");
        String corpusID = "";
        String metadataKeyID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getAvailableValues(corpusID, metadataKeyID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDescription method, of class AGDFileSystem.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        AGDFileSystem instance = new AGDFileSystem();
        String expResult = "";
        String result = instance.getDescription();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProtocol method, of class AGDFileSystem.
     */
    @Test
    public void testGetProtocol() throws Exception {
        System.out.println("getProtocol");
        String protocolID = "";
        AGDFileSystem instance = new AGDFileSystem();
        Protocol expResult = null;
        Protocol result = instance.getProtocol(protocolID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProtocol4SpeechEvent method, of class AGDFileSystem.
     */
    @Test
    public void testGetProtocol4SpeechEvent() throws Exception {
        System.out.println("getProtocol4SpeechEvent");
        String speechEventID = "";
        AGDFileSystem instance = new AGDFileSystem();
        String expResult = "";
        String result = instance.getProtocol4SpeechEvent(speechEventID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAvailableValuesForAnnotationLayer method, of class AGDFileSystem.
     */
    @Test
    public void testGetAvailableValuesForAnnotationLayer() {
        System.out.println("getAvailableValuesForAnnotationLayer");
        String corpusID = "";
        String annotationLayerID = "";
        AGDFileSystem instance = new AGDFileSystem();
        IDList expResult = null;
        IDList result = instance.getAvailableValuesForAnnotationLayer(corpusID, annotationLayerID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAnnotationTagSet method, of class AGDFileSystem.
     */
    @Test
    public void testGetAnnotationTagSet() throws Exception {
        System.out.println("getAnnotationTagSet");
        String annotationTagSetID = "";
        AGDFileSystem instance = new AGDFileSystem();
        AnnotationTagSet expResult = null;
        AnnotationTagSet result = instance.getAnnotationTagSet(annotationTagSetID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAnnotationLayersForCorpus method, of class AGDFileSystem.
     */
    @Test
    public void testGetAnnotationLayersForCorpus() {
        System.out.println("getAnnotationLayersForCorpus");
        String corpusID = "";
        String annotationType = "";
        AGDFileSystem instance = new AGDFileSystem();
        Set<AnnotationLayer> expResult = null;
        Set<AnnotationLayer> result = instance.getAnnotationLayersForCorpus(corpusID, annotationType);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMetadataKeysForCorpus method, of class AGDFileSystem.
     */
    @Test
    public void testGetMetadataKeysForCorpus() {
        System.out.println("getMetadataKeysForCorpus");
        String corpusID = "";
        String type = "";
        AGDFileSystem instance = new AGDFileSystem();
        Set<MetadataKey> expResult = null;
        Set<MetadataKey> result = instance.getMetadataKeysForCorpus(corpusID, type);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
