/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.indexing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.exmaralda.folker.utilities.TimeStringFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.backend.BackendInterface;
import org.zumult.backend.BackendInterfaceFactory;
import org.zumult.backend.Configuration;
import org.zumult.io.IOUtilities;
import org.zumult.objects.IDList;
import org.zumult.objects.Media;

/**
 *
 * @author josip.batinic
 */
public class OutputPrototypeJson implements Indexer {

//    String corpusID = "FOLK";
//    String corpusID = "GWSS";
    String[] CORPORA = {"FOLK", "GWSS"};
    String DATA_PATH = "src\\main\\java\\data\\";
    String IDLISTS_PATH = DATA_PATH + "IDLists\\";
    XPath xPath = XPathFactory.newInstance().newXPath();


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OutputPrototypeJson().index();
        } catch (IOException ex) {
            Logger.getLogger(OutputPrototypeJson.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void index() throws IOException {
        try {
            BackendInterface backend;
            backend = BackendInterfaceFactory.newBackendInterface();
            long start = System.currentTimeMillis();

            for (String corpusID : CORPORA) {
                JSONArray jsonArray = new JSONArray();

                // speech event ids
                Path speechEventsFilePath = new File(IDLISTS_PATH + corpusID + "/" + corpusID + "_speechEvents.txt").toPath();
                IDList speechEventIDs = new IDList("speechEvents");
                speechEventIDs.addAll(Files.readAllLines(speechEventsFilePath));
                
                Path measure12Path = new File(DATA_PATH + "measure_12_" + corpusID + ".txt").toPath();
                List listOfRowsMeasure12 = new ArrayList<String>();
                listOfRowsMeasure12.addAll(Files.readAllLines(measure12Path));


                speechEventIDs.forEach((String speechEventId) -> {
                    System.out.println("**************************************************");
                    System.out.println(speechEventId + " *******************************");
                    // speechEventId
                    JSONObject speechEventJson = new JSONObject();
                    speechEventJson.put("id", speechEventId);

                    // eventId it belongs to
                    String parentEventId = speechEventId.substring(0, 12);
                    speechEventJson.put("elternereignis_id", parentEventId);

    //////////////////GET DATA FROM API
    //                String apiDauer = "http://localhost:8080/DGDRESTTest/api/corpora/" + corpusID + "/events/" + parentEventId + ";metadatum=e_dauer";
    //                Client client = ClientBuilder.newClient();
    //                WebTarget target = client.target(apiDauer);
    //                String dauer = target.request(MediaType.TEXT_PLAIN).get(String.class);

    //                System.out.println("dauer: " + dauer);



    ////////////////GET DATA FROM CALCULATD MEASURES
                    String measuresBaseXPath = "//measures[@speechEventID='" + speechEventId + "']/";

                    // paths to measure files
                    //String measure10_2FilePath = DATA_PATH + "Measure_10_2_" + corpusID + ".xml";
                    //System.out.println("#########" + measure10_2FilePath);
                    //String measure9FilePath = DATA_PATH + "Measure_9_" + corpusID + ".xml";
                    String measure8FilePath = DATA_PATH + "Measure_8_" + corpusID + ".xml";
                    String measure7FilePath = DATA_PATH + "Measure_7_" + corpusID + ".xml";
                    //String measure6FilePath = DATA_PATH + "Measure_6_" + corpusID + ".xml";
                    //String measure5FilePath = DATA_PATH + "Measure_5_" + corpusID + ".xml";
                    //String measure4NormFilePath = DATA_PATH + "Measure_4_" + corpusID + "_norm.xml";
                    //String measure4TransFilePath = DATA_PATH + "Measure_4_" + corpusID + "_transcription.xml";
                    //String measure3FilePath = DATA_PATH + "Measure_3_" + corpusID + ".xml";
                    //String measure2FilePath = DATA_PATH + "Measure_2_" + corpusID + ".xml";
                    String measure1FilePath = DATA_PATH + "Measure_1_" + corpusID + ".xml";

                    // measure 9
                    /*Map<String, String> measures9 = new HashMap<>();
                    measures9.put("anzahl_beiträge_pro_minute", "[@nrOfContributionsPerMinute]/@nrOfContributionsPerMinute"); // measure/@nrOfContributionsPerMinute
                    measures9.put("durchschnittliche_beitragslänge", "[@averageNrOfWordsInContribution]/@averageNrOfWordsInContribution"); // measure/@nrOfContributionsPerMinute
                    measures9.put("anteil_kurzer_beiträge", "[@perCentContributionsWith1To2Words]/@perCentContributionsWith1To2Words"); // measure/@nrOfContributionsPerMinute
                    Object measures9Array[] = {measures9, "measure", measure9FilePath};
                    Map<String, Object> mapOfMapsMeasure9 = new HashMap<>();
                    mapOfMapsMeasure9.put("maße", measures9Array); // map or string
                    Object list9[] = {mapOfMapsMeasure9, measure9FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file*/

                    // measure 8
                    Map<String, String> measures8 = new HashMap<>();
                    measures8.put("anzahl_überlappungen", "[@perMilOverlaps]/@perMilOverlaps");
                    measures8.put("durchschnittliche_anzahl_der_überlappungen", "[@averageNrOverlappingWords]/@averageNrOverlappingWords");
                    measures8.put("anteil_Überlappungen_mit_mehr_als_2_wörtern", "[@perCentOverlapsWithMoreThan2Words]/@perCentOverlapsWithMoreThan2Words");
                    measures8.put("anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens", "[@perMilTokensOverlapsWithMoreThan2Words]/@perMilTokensOverlapsWithMoreThan2Words");
                    Object measures8Array[] = {measures8, "measure", measure8FilePath};
                    Map<String, Object> mapOfMapsMeasure8 = new HashMap<>();
                    mapOfMapsMeasure8.put("maße", measures8Array); // map or string
                    Object list8[] = {mapOfMapsMeasure8, measure8FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file

                    // measure 7
                    Map<String, String> measures7 = new HashMap<>();
                    measures7.put("normalisierungsrate", "[@normRate]/@normRate");
                    Object measures7Array[] = {measures7, "measure", measure7FilePath};
                    Map<String, Object> mapOfMapsMeasure7 = new HashMap<>();
                    mapOfMapsMeasure7.put("maße", measures7Array); // map or string
                    Object list7[] = {mapOfMapsMeasure7, measure7FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file

                    // measure 6
                    /*Map<String, String> measures6 = new HashMap<>();
                    measures6.put("anzahl_PRELS", "[@perMilPRELS]/@perMilPRELS");
                    measures6.put("anzahl_PDAT", "[@perMilPDAT]/@perMilPDAT");
                    measures6.put("anzahl_ART_ADJ_NN", "[@perMilArtAdjNn]/@perMilArtAdjNn");
                    measures6.put("anzahl_APPR_ART_ADJ_NN", "[@perMilApprArtAdjNn]/@perMilApprArtAdjNn");
                    Object measures6Array[] = {measures6, "measure", measure6FilePath};
                    Map<String, Object> mapOfMapsMeasure6 = new HashMap<>();
                    mapOfMapsMeasure6.put("maße", measures6Array); // map or string
                    Object list6[] = {mapOfMapsMeasure6, measure6FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file*/

                    // measure 5
                    /*Map<String, String> measures5 = new HashMap<>();
                    measures5.put("bildungen_mit_ung", "[@perMilUng]/@perMilUng");
                    measures5.put("bildungen_mit_heit", "[@perMilHeit]/@perMilHeit");
                    measures5.put("bildungen_mit_keit", "[@perMilKeit]/@perMilKeit");
                    measures5.put("bildungen_mit_schaft", "[@perMilSchaft]/@perMilSchaft");
                    measures5.put("bildungen_mit_tät", "[@perMilTat]/@perMilTat");
                    measures5.put("bildungen_mit_ion", "[@perMilIon]/@perMilIon");
                    measures5.put("bildungen_mit_ieren", "[@perMilIeren]/@perMilIeren");
                    Object nominalisationsList[] = {measures5, "", measure5FilePath};
                    Map<String, Object> measures5Map = new HashMap<>();
                    measures5Map.put("nominalisierung", nominalisationsList);
                    Object measures5Array[] = {measures5Map, "measure", measure5FilePath};
                    Map<String, Object> mapOfMapsMeasure5 = new HashMap<>();
                    mapOfMapsMeasure5.put("maße", measures5Array); // map or string
                    Object list5[] = {mapOfMapsMeasure5, measure5FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file*/

                    // measure 4 norm
                    /*Map<String, String> measures4Norm = new HashMap<>();
                    measures4Norm.put("normalisierten_tokens_mit_mehr_als_10_buchstaben", "[@TokensWith11OrMoreLetters]/@TokensWith11OrMoreLetters");
                    measures4Norm.put("durschnittliche_länge_normalisierten_token", "[@averageTokenLength]/@averageTokenLength");
                    measures4Norm.put("normalisierten_NN_mit_mehr_als_10_buchstaben", "[@NounsWith11OrMoreLetters]/@NounsWith11OrMoreLetters");
                    measures4Norm.put("durschnittliche_länge_normalisierten_NN", "[@averageNounLength]/@averageNounLength");
                    measures4Norm.put("normalisierten_ADJ_mit_mehr_als_10_buchstaben", "[@AdjectivesWith11OrMoreLetters]/@AdjectivesWith11OrMoreLetters");
                    measures4Norm.put("durschnittliche_länge_normalisierten_ADJ", "[@averageAdjectiveLength]/@averageAdjectiveLength");
                    Object measures4NormArray[] = {measures4Norm, "measure", measure4NormFilePath};
                    Map<String, Object> mapOfMapsMeasure4Norm = new HashMap<>();
                    mapOfMapsMeasure4Norm.put("maße", measures4NormArray); // map or string
                    Object list4Norm[] = {mapOfMapsMeasure4Norm, measure4NormFilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file

                    // measure 4 transcription
                    Map<String, String> measuresTrans4 = new HashMap<>();
                    measuresTrans4.put("transkribierte_tokens_mit_mehr_als_10_buchstaben", "[@TokensWith11OrMoreLetters]/@TokensWith11OrMoreLetters");
                    measuresTrans4.put("durschnittliche_länge_transkribierten_token", "[@averageTokenLength]/@averageTokenLength");
                    measuresTrans4.put("transkribierten_NN_mit_mehr_als_10_buchstaben", "[@NounsWith11OrMoreLetters]/@NounsWith11OrMoreLetters");
                    measuresTrans4.put("durschnittliche_länge_transkribierten_NN", "[@averageNounLength]/@averageNounLength");
                    measuresTrans4.put("transkribierten_ADJ_mit_mehr_als_10_buchstaben", "[@AdjectivesWith11OrMoreLetters]/@AdjectivesWith11OrMoreLetters");
                    measuresTrans4.put("durschnittliche_länge_transkribierten_ADJ", "[@averageAdjectiveLength]/@averageAdjectiveLength");
                    Object measures4TransArray[] = {measuresTrans4, "measure", measure4TransFilePath};
                    Map<String, Object> mapOfMapsMeasure4Trans = new HashMap<>();
                    mapOfMapsMeasure4Trans.put("maße", measures4TransArray); // map or string
                    Object list4Trans[] = {mapOfMapsMeasure4Trans, measure4TransFilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file*/

                    // measure 3
                    /*Map<String, String> measures3 = new HashMap<>();
                    measures3.put("tokens", "[@tokens]/@tokens");
                    measures3.put("contentWords", "[@contentWords]/@contentWords");
                    measures3.put("contentWordsPerTotalTokenRatio", "[@ratio]/@ratio");
                    Object measures3Array[] = {measures3, "measure", measure3FilePath};
                    Map<String, Object> mapOfMapsMeasure3 = new HashMap<>();
                    mapOfMapsMeasure3.put("maße", measures3Array); // map or string
                    Object list3[] = {mapOfMapsMeasure3, measure3FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file*/

                    // measure 2
                    /*Map<String, String> measures2 = new HashMap<>();
                    measures2.put("lemmas", "[@lemmas]/@lemmas"); // filtered lemmas, use lemmas_unfiltered if desired
                    measures2.put("tokens", "[@tokens]/@tokens"); // filtered tokens, use tokens_unfiltered if desired
                    measures2.put("lemma_token_ratio", "[@lemma_token_ratio]/@lemma_token_ratio"); // filtered lemma token ratio, use lemma_token_ratio_unfiltered if desired
                    Object measures2Array[] = {measures2, "measure", measure2FilePath};
                    Map<String, Object> mapOfMapsMeasure2 = new HashMap<>();
                    mapOfMapsMeasure2.put("maße", measures2Array); // map or string
                    Object list2[] = {mapOfMapsMeasure2, measure2FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file*/

                    // measure 1
                    Map<String, String> goetheA1Values = new HashMap<>();
                    goetheA1Values.put("lemmas", "[@reference='GOETHE_A1']/@lemmas");
                    goetheA1Values.put("tokens", "[@reference='GOETHE_A1']/@tokens");
                    goetheA1Values.put("lemmas_ratio", "[@reference='GOETHE_A1']/@lemmas_ratio");
                    goetheA1Values.put("tokens_ratio", "[@reference='GOETHE_A1']/@tokens_ratio");
                    Object[] goetheA1Array = {goetheA1Values, "", measure1FilePath};

                    Map<String, String> goetheA2Values = new HashMap<>();
                    goetheA2Values.put("lemmas", "[@reference='GOETHE_A2']/@lemmas");
                    goetheA2Values.put("tokens", "[@reference='GOETHE_A2']/@tokens");
                    goetheA2Values.put("lemmas_ratio", "[@reference='GOETHE_A2']/@lemmas_ratio");
                    goetheA2Values.put("tokens_ratio", "[@reference='GOETHE_A2']/@tokens_ratio");
                    Object[] goetheA2Array = {goetheA2Values, "", measure1FilePath};

                    Map<String, String> goetheB1Values = new HashMap<>();
                    goetheB1Values.put("lemmas", "[@reference='GOETHE_B1']/@lemmas");
                    goetheB1Values.put("tokens", "[@reference='GOETHE_B1']/@tokens");
                    goetheB1Values.put("lemmas_ratio", "[@reference='GOETHE_B1']/@lemmas_ratio");
                    goetheB1Values.put("tokens_ratio", "[@reference='GOETHE_B1']/@tokens_ratio");
                    Object[] goetheB1Array = {goetheB1Values, "", measure1FilePath};

                    Map<String, String> HERDER_1000Values = new HashMap<>();
                    HERDER_1000Values.put("lemmas", "[@reference='HERDER_1000']/@lemmas");
                    HERDER_1000Values.put("tokens", "[@reference='HERDER_1000']/@tokens");
                    HERDER_1000Values.put("lemmas_ratio", "[@reference='HERDER_1000']/@lemmas_ratio");
                    HERDER_1000Values.put("tokens_ratio", "[@reference='HERDER_1000']/@tokens_ratio");
                    Object[] HERDER_1000Array = {HERDER_1000Values, "", measure1FilePath};

                    Map<String, String> HERDER_2000Values = new HashMap<>();
                    HERDER_2000Values.put("lemmas", "[@reference='HERDER_2000']/@lemmas");
                    HERDER_2000Values.put("tokens", "[@reference='HERDER_2000']/@tokens");
                    HERDER_2000Values.put("lemmas_ratio", "[@reference='HERDER_2000']/@lemmas_ratio");
                    HERDER_2000Values.put("tokens_ratio", "[@reference='HERDER_2000']/@tokens_ratio");
                    Object[] HERDER_2000Array = {HERDER_2000Values, "", measure1FilePath};

                    Map<String, String> HERDER_3000Values = new HashMap<>();
                    HERDER_3000Values.put("lemmas", "[@reference='HERDER_3000']/@lemmas");
                    HERDER_3000Values.put("tokens", "[@reference='HERDER_3000']/@tokens");
                    HERDER_3000Values.put("lemmas_ratio", "[@reference='HERDER_3000']/@lemmas_ratio");
                    HERDER_3000Values.put("tokens_ratio", "[@reference='HERDER_3000']/@tokens_ratio");
                    Object[] HERDER_3000Array = {HERDER_3000Values, "", measure1FilePath};

                    Map<String, String> HERDER_4000Values = new HashMap<>();
                    HERDER_4000Values.put("lemmas", "[@reference='HERDER_4000']/@lemmas");
                    HERDER_4000Values.put("tokens", "[@reference='HERDER_4000']/@tokens");
                    HERDER_4000Values.put("lemmas_ratio", "[@reference='HERDER_4000']/@lemmas_ratio");
                    HERDER_4000Values.put("tokens_ratio", "[@reference='HERDER_4000']/@tokens_ratio");
                    Object[] HERDER_4000Array = {HERDER_4000Values, "", measure1FilePath};

                    Map<String, String> HERDER_5000Values = new HashMap<>();
                    HERDER_5000Values.put("lemmas", "[@reference='HERDER_5000']/@lemmas");
                    HERDER_5000Values.put("tokens", "[@reference='HERDER_5000']/@tokens");
                    HERDER_5000Values.put("lemmas_ratio", "[@reference='HERDER_5000']/@lemmas_ratio");
                    HERDER_5000Values.put("tokens_ratio", "[@reference='HERDER_5000']/@tokens_ratio");
                    Object[] HERDER_5000Array = {HERDER_5000Values, "", measure1FilePath};

                    Map<String, Object> measures1 = new TreeMap<>();
                    measures1.put("GOETHE_A1", goetheA1Array); // value is an object[]
                    measures1.put("GOETHE_A2", goetheA2Array); // value is an object
                    measures1.put("GOETHE_B1", goetheB1Array); // value is an object
                    measures1.put("HERDER_1000", HERDER_1000Array); // value is an object
                    measures1.put("HERDER_2000", HERDER_2000Array); // value is an object
                    measures1.put("HERDER_3000", HERDER_3000Array); // value is an object
                    measures1.put("HERDER_4000", HERDER_4000Array); // value is an object
                    measures1.put("HERDER_5000", HERDER_5000Array); // value is an object
                    Object wordlistsArray[] = {measures1, "", measure1FilePath};

                    Map<String, Object> measures1Map = new HashMap<>();
                    measures1Map.put("wortschatz", wordlistsArray);
                    Object measures1Array[] = {measures1Map, "measure", measure1FilePath};
                    Map<String, Object> mapOfMapsMeasure1 = new HashMap<>();                
                    mapOfMapsMeasure1.put("maße", measures1Array); // map or string
//                    mapOfMapsMeasure1.put("lemmas", "@lemmas"); // map or string
//                    mapOfMapsMeasure1.put("tokens", "@tokens"); // map or string
                    Object list1[] = {mapOfMapsMeasure1, measure1FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file

                    // measure 10
                    // still need to translate theeese
                    /*Map<String, Object> speakersMap = new HashMap<>();
                    Map<String, String> speakersMeasures = new HashMap<>();
                    speakersMeasures.put("perCentSpeakerContributionsProTotalContributions", "[@perCentSpeakerContributionsProTotalContributions]/@perCentSpeakerContributionsProTotalContributions");
                    speakersMeasures.put("perCentSpeakerSpokenWordsProTotalWords", "[@perCentSpeakerSpokenWordsProTotalWords]/@perCentSpeakerSpokenWordsProTotalWords");
                    speakersMeasures.put("perCentContributions1To2WordsProSpeakerContributionSize", "[@perCentContributions1To2WordsProSpeakerContributionSize]/@perCentContributions1To2WordsProSpeakerContributionSize");
                    Object speakerMeasures[] = {speakersMeasures, "/measure", measure10_2FilePath}; //THIS OBJECT IS GOETHE ETX*/

                    Map<String, Object> dauerMap = new HashMap<>();
                    /*Map<String, Object> totalSpeakeersMap = new HashMap<>();*/
                    
 // measure 3
//                    Map<String, String> measures3 = new HashMap<>();
//                    measures3.put("tokens", "[@tokens]/@tokens");
//                    measures3.put("contentWords", "[@contentWords]/@contentWords");
//                    measures3.put("contentWordsPerTotalTokenRatio", "[@ratio]/@ratio");
//                    Object measures3Array[] = {measures3, "measure", measure3FilePath};
//                    Map<String, Object> mapOfMapsMeasure3 = new HashMap<>();
//                    mapOfMapsMeasure3.put("maße", measures3Array); // map or string
//                    Object list3[] = {mapOfMapsMeasure3, measure3FilePath, measuresBaseXPath};

                    
                    /*speakersMap.put("id", "/@AGD-ID"); ///measures-document/measures[1]/speaker[1]/@AGD-ID
                    speakersMap.put("berufe", "/@speakerOccupation");
                    speakersMap.put("sprecher_kennung", "/@who");
                    speakersMap.put("measures", speakerMeasures);*/
                    dauerMap.put("dauer", "");
                    /*totalSpeakeersMap.put("anzahl_sprecher", "");
                    Object measures10Array[] = {speakersMap, "speaker", measure10_2FilePath};
                    Object measures10DauerArray[] = {dauerMap, "@duration", measure10_2FilePath};
                    Object measures10TotalSpeakersArray[] = {totalSpeakeersMap, "@totalSpeakers", measure10_2FilePath};
                    Map<String, Object> mapOfMapsMeasure10_2 = new HashMap<>();
                    Map<String, Object> mapOfMapsMeasureTotalSpeakers10_2 = new HashMap<>();
                    mapOfMapsMeasure10_2.put("sprecher_liste", measures10Array); // map or string
                    mapOfMapsMeasure10_2.put("maße", measures10DauerArray); // map or string
                    mapOfMapsMeasureTotalSpeakers10_2.put("maße", measures10TotalSpeakersArray); // map or string
                    Object list10_2[] = {mapOfMapsMeasure10_2, measure10_2FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file
                    Object listTotalSpeakers10_2[] = {mapOfMapsMeasureTotalSpeakers10_2, measure10_2FilePath, measuresBaseXPath}; // the measure map an dth epath to the measure file*/


                    // add lists of measures data to the collection
                    Map<String, Object[]> setsOfData = new HashMap<>(); // not sure why dis is not a simple list
                    List listOfExtractedMetadataLists = new ArrayList(); // not sure why dis is not a simple list
                    //listOfExtractedMetadataLists.add(list10_2);
                    //listOfExtractedMetadataLists.add(listTotalSpeakers10_2);
                    //listOfExtractedMetadataLists.add(list9);
                    listOfExtractedMetadataLists.add(list8);
                    listOfExtractedMetadataLists.add(list7);
                    //listOfExtractedMetadataLists.add(list6); 
                    //listOfExtractedMetadataLists.add(list5);
                    //listOfExtractedMetadataLists.add(list4Norm);
                    //listOfExtractedMetadataLists.add(list4Trans);
                    //listOfExtractedMetadataLists.add(list3);
                    //listOfExtractedMetadataLists.add(list2);
                    listOfExtractedMetadataLists.add(list1);
    //                listOfExtractedMetadataLists.put("m1", list1);


    ////////////////GET DATA FROM LOCAL XML
                    String eventMetadataPath = Configuration.getMetadataPath() + "\\events\\extern\\" + corpusID + "\\" + parentEventId + "_extern.xml";
                    String xmlBaseXPath = "/Ereignis/";
                    String speechEventBaseXPath = xmlBaseXPath + "Sprechereignis[@Kennung='" + speechEventId + "']/";

                    if (new File(eventMetadataPath).exists()) {
                        //System.out.println("WE ARE HERE!!!");
                        Map<String, Object> themesMap = new HashMap<>();
                        themesMap.put("themen", "Inhalt/Themen"); // map or string
                        Object themesMaterial[] = {themesMap, eventMetadataPath, speechEventBaseXPath}; 

                        Map<String, Object> languagesMap = new HashMap<>();
                        languagesMap.put("sprachen", "Basisdaten/Sprachen"); // map or string
                        Object languagesMaterial[] = {languagesMap, eventMetadataPath, speechEventBaseXPath};

                        Map<String, Object> descriptionMap = new HashMap<>();
                        descriptionMap.put("beschreibung", "Inhalt/Beschreibung"); // map or string
                        Object descriptionMaterial[] = {descriptionMap, eventMetadataPath, speechEventBaseXPath};

                        Map<String, Object> typeOfConversationMap = new HashMap<>();
                        typeOfConversationMap.put("art", "Basisdaten/Art"); // map or string
                        Object typeOfConversationMaterial[] = {typeOfConversationMap, eventMetadataPath, speechEventBaseXPath};

                        Map<String, Object> domainOfInteractionMap = new HashMap<>();
                        if (applyXPath(speechEventBaseXPath + "Basisdaten/Interaktionsdomäne", eventMetadataPath).getLength() != 0) {
                            domainOfInteractionMap.put("interaktionsdomäne", "Basisdaten/Interaktionsdomäne"); // map or string
                        }
                        Object domainOfInteractionMaterial[] = {domainOfInteractionMap, eventMetadataPath, speechEventBaseXPath};

                        Map<String, Object> areaOfLifeMap = new HashMap<>();
                        if (applyXPath(speechEventBaseXPath + "Basisdaten/Lebensbereich", eventMetadataPath).getLength() != 0) {
                            areaOfLifeMap.put("lebensbereich", "Basisdaten/Lebensbereich"); // map or string
                        }
                        Object areaOfLifeMaterial[] = {areaOfLifeMap, eventMetadataPath, speechEventBaseXPath};

                        Map<String, Object> activityMap = new HashMap<>();                    
                        if (applyXPath(speechEventBaseXPath + "Basisdaten/Aktivität", eventMetadataPath).getLength() != 0) {
                            activityMap.put("aktivität", "Basisdaten/Aktivität"); // map or string
                        }
                        Object activityMaterial[] = {activityMap, eventMetadataPath, speechEventBaseXPath};

                        Map<String, String> geo = new HashMap<>();                    
                        if (applyXPath(xmlBaseXPath + "Basisdaten/Ort/Land", eventMetadataPath).getLength() != 0) { // it would be more systematic to check with a loop, but it is more understandable so
                            geo.put("land", "/Land");
                        }
                        if (applyXPath(xmlBaseXPath + "Basisdaten/Ort/Region[@Name='Dialektregion Wiesinger']", eventMetadataPath).getLength() != 0) {
                            geo.put("dialektalregion_wiesinger", "/Region[@Name='Dialektregion Wiesinger']");
                        }
                        if (applyXPath(xmlBaseXPath + "Basisdaten/Ort/Region[@Name='Dialektregion Lameli']", eventMetadataPath).getLength() != 0) {
                            geo.put("dialektalregion_lameli", "/Region[@Name='Dialektregion Lameli']");
                        }
                        if (applyXPath(xmlBaseXPath + "Basisdaten/Ort/Kreis", eventMetadataPath).getLength() != 0) {
                            geo.put("kreis", "/Kreis");
                        }
                        if (applyXPath(xmlBaseXPath + "Basisdaten/Ort/Ortsname", eventMetadataPath).getLength() != 0) {
                            geo.put("ortsname", "/Ortsname");
                        }
                        if (applyXPath(xmlBaseXPath + "Basisdaten/Ort/Ortsteil", eventMetadataPath).getLength() != 0) {
                            geo.put("ortsteil", "/Ortsteil");
                        }
                        if (applyXPath(xmlBaseXPath + "Basisdaten/Ort/Ortsbeschreibung", eventMetadataPath).getLength() != 0) {
                        geo.put("ortsbeschreibung", "/Ortsbeschreibung");
                        }

                        Object geoList[] = {geo, "Basisdaten/Ort", eventMetadataPath};

                        Map<String, Object> geoMap = new HashMap<>();
                        geoMap.put("geo", geoList); // map or string
                        Object geoMaterial[] = {geoMap, eventMetadataPath, xmlBaseXPath};                    


                        NodeList additionalMaterialIdNodes = applyXPath(speechEventBaseXPath + "Zusatzmaterial", eventMetadataPath);
                        if (additionalMaterialIdNodes.getLength() > 0) { // TODO check if there are more than one zusatzmaterial elements, then iterate through both                            
                            Map<String, String> additionalMaterial = new HashMap<>();
                            additionalMaterial.put("id", "/@Kennung");
                            additionalMaterial.put("art", "/Basisdaten/Art");
                            additionalMaterial.put("dateiname", "/Digitale_Fassung/Basisdaten/Dateiname");
                            Object additionalMaterialList[] = {additionalMaterial, "Zusatzmaterial", eventMetadataPath};

                            Map<String, Object> additionalMaterialMap = new HashMap<>();
                            additionalMaterialMap.put("zusatzmaterial", additionalMaterialList); // map or string
                            Object additionalMaterialMaterial[] = {additionalMaterialMap, eventMetadataPath, speechEventBaseXPath};

                            listOfExtractedMetadataLists.add(additionalMaterialMaterial);
                        }

                          // speakerAge ==> later, do gesprachstyp first 
    //                    Map<String, Object> speakerAgeMap = new HashMap<>();
    //                    speakerAgeMap.put("age", "Basisdaten/Aktivität"); // map or string
    //                    Object activityMaterial[] = {speakerAgeMap, eventMetadataPath, speechEventBaseXPath};                    

                        listOfExtractedMetadataLists.add(themesMaterial);
                        listOfExtractedMetadataLists.add(languagesMaterial);
                        listOfExtractedMetadataLists.add(descriptionMaterial);
                        listOfExtractedMetadataLists.add(typeOfConversationMaterial);
                        listOfExtractedMetadataLists.add(domainOfInteractionMaterial);
                        listOfExtractedMetadataLists.add(areaOfLifeMaterial);
                        listOfExtractedMetadataLists.add(activityMaterial);
                        listOfExtractedMetadataLists.add(geoMaterial);
                    }


                    // iterate through lists of materials
                    listOfExtractedMetadataLists.forEach(materialList -> {
                        try {
                            Object[] list = (Object[])materialList;
                            Map<String, Object> measureMap = (Map) list[0]; // is always a map
                            String filePath = (String) list[1];
                            String baseXPath = (String) list[2];
                            //                    System.out.println("baseXPath: " + baseXPath);
                            
                            measureMap.entrySet().forEach(measurePair -> {
                                String key = measurePair.getKey();
                                Object theObject = measurePair.getValue();
                                //                        System.out.println("key: " + key);
                                
                                Object value = checkValueRecursively(theObject, baseXPath, filePath); // return Object={String/JSONObject}
                                
                                if (speechEventJson.get(key) == null) { // if the field doeas not yet exist
                                    speechEventJson.put(key, value);
                                } else { // if exists add to it
                                    JSONObject existingJsonObject = (JSONObject) speechEventJson.get(key);
                                    existingJsonObject.putAll((JSONObject) value);
                                    speechEventJson.put(key, existingJsonObject);
                                }
                            });
                            // duration as date
                            double totalDuration = 0;
                            IDList mediaIDs = backend.getAudios4SpeechEvent(speechEventId);
                            for (String mediaID : mediaIDs){
                                totalDuration+=backend.getMedia(mediaID, Media.MEDIA_FORMAT.WAV).getDuration();
                            }
                            String durationString = TimeStringFormatter.formatSeconds(totalDuration, true, 0);
                            System.out.println("========== Duration " + durationString);
                            speechEventJson.put("dauer", durationString);
                            if (speechEventJson.get("dauer") != null) {
                                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss yyyy");
                                Date dateDuration = null;
                                try {
                                    dateDuration = formatter.parse(speechEventJson.get("dauer") + " 1900");
                                } catch (ParseException ex) {
                                    Logger.getLogger(OutputPrototypeJson.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                speechEventJson.put("dauer_als_datum", dateDuration.toString());
                                
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(OutputPrototypeJson.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    
                    
                    // get data from eva's measures 12 and 13
                    String artikulationsrate = "";
                    for (int i = 0; i < listOfRowsMeasure12.size(); i++) {
                        String row = (String) listOfRowsMeasure12.get(i);
                        if (row.split("\t")[0].contains(speechEventId)) {
                            double artik = Double.parseDouble(row.toString().split("\t")[1]);
                            artikulationsrate = String.format("%.2f", artik);
                        }
                    }
                    JSONObject artikulationsrateObject = (JSONObject)speechEventJson.get("maße");
                    artikulationsrateObject.put("artikulationsrate", artikulationsrate);
                    

                    // nice print
    //                ObjectMapper mapper = new ObjectMapper();
    //                try {
    //                    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(speechEventJson));
    //                } catch (JsonProcessingException ex) {
    //                    Logger.getLogger(OutputPrototypeJson.class.getName()).log(Level.SEVERE, null, ex);
    //                }

                    jsonArray.add(speechEventJson);
                });

                // write to json
                FileWriter file = new FileWriter(DATA_PATH + "prototypeJson/" + corpusID + ".json");
                //FileWriter file = new FileWriter("D:\\Dropbox\\IDS\\ZuMult\\" + corpusID + ".json");
                file.write(jsonArray.toString());
                file.flush();

                long end = System.currentTimeMillis();
                DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:sss");
                Date timeNeeded = new Date(end - start);
                System.out.println("time needed: " + dateFormat.format(timeNeeded));
            }
        } catch (Exception ex) {
            Logger.getLogger(OutputPrototypeJson.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }

    }

    public NodeList applyXPath(String xPathString, String documentPath) {
//        System.out.println("applyXPath passed xPathString: " + xPathString);
//        System.out.println("applyXPath passed documentPath: " + documentPath);
        Document xmlDocument = null;
        NodeList nodeList = null;
        try {
            xmlDocument = IOUtilities.documentFromLocalFile(documentPath);
            System.out.println("Reading " + documentPath);
            nodeList = (NodeList) xPath.evaluate(xPathString, xmlDocument.getDocumentElement(), XPathConstants.NODESET);
        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(OutputPrototypeJson.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeList;
    }

    public Object checkValueRecursively(Object passedObject, String baseXPath, String pathToFile) {
        System.out.println("****** checkValueRecursively *******");
        System.out.println("pathToFile: " + pathToFile);
        Object returnedObject = null;
        if (passedObject instanceof String) {
//            System.out.println("*********************");
//            System.out.println("is a string");
            String path = (String) passedObject;

            String xPathExp = baseXPath + path;
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            System.out.println("xPathExp | path: " + path);
            System.out.println("xPathExp | String: " + xPathExp);
            NodeList nodes = applyXPath(xPathExp, pathToFile);

            Object value = nodes.item(0).getTextContent();
            
//            if (((String)value).contains(" ; ")) { // for lists
            if (path.equals("Inhalt/Themen") || path.equals("Basisdaten/Sprachen") || path.equals("/@speakerOccupation") || path.equals("Basisdaten/Lebensbereich")) { // for string lists
                String array[] = ((String)value).split(";");
                String trimmedArray[] = new String[array.length];
                for (int i = 0; i < array.length; i++) {
                    trimmedArray[i] = array[i].trim();
                }
                value = Arrays.asList(trimmedArray);
            }

//            System.out.println("value " + value);
            returnedObject = value;
        } else if (passedObject instanceof Object[]) {
            System.out.println("-------------------");
            System.out.println("is an array");
            Object[] array = (Object[])passedObject;

            Map<String, Object> map = (Map)array[0];
            String xPathExp = baseXPath  + (String) array[1];
            String path = (String) array[2];

            System.out.println("path: " + path);
            System.out.println("xPathExp | Array: " + xPathExp);
            NodeList nodes = applyXPath(xPathExp, pathToFile);
//            System.out.println("nodes nr: " + nodes.getLength());
            System.out.println("FIELD: " + ((String)array[1]));
            JSONArray ja = new JSONArray();

            if (((String)array[1]).equals("speaker") || ((String)array[1]).equals("Zusatzmaterial")) { // BECAUSE IT CONTAINS A LIST OF MAPS, NOT A MAP
//                System.out.println("IS SPEAKER/ZUSAZT");
                for (int i = 0; i < nodes.getLength(); i++) { // for each of the speakers
//                    Node node = nodes.item(i);
                    int j = i + 1;
                    String nodeIndex = "[" + j + "]";
                    String newXPath = xPathExp + nodeIndex;
//                    System.out.println("SPEAKER/ZUSAZT newXPath " + newXPath); // //measures[@speechEventID='FOLK_E_00001_SE_01']/speaker[1]/measure[1]
//                    System.out.println("SPEAKER/ZUSAZT getNodeName " + node.getNodeName()); // measure
//                    System.out.println("SPEAKER/ZUSAZT getNodeName " + node.getAttributes().item(0));
                    JSONObject jo = new JSONObject();
                    for (Entry mapData : map.entrySet()) {
//                        System.out.println("###### SPEAKER/ZUSAZT ##################");
//                        System.out.println("SPEAKER/ZUSAZT KEY: " + mapData.getKey()); // perCentSpokenWordsProTotalWords
                        Object value = checkValueRecursively(mapData.getValue(), newXPath, path);
                        jo.put(mapData.getKey(), value);
                    }
                    ja.add(jo);
                }
                returnedObject = ja;
            } else {
                JSONObject jo = new JSONObject();
                for (Entry mapData : map.entrySet()) {
//                    System.out.println("#############################");
//                    System.out.println("KEY: " + mapData.getKey()); // perCentSpokenWordsProTotalWords
                    Object value = checkValueRecursively(mapData.getValue(), xPathExp, path);
                    jo.put(mapData.getKey(), value);
                }
                returnedObject = jo;
            }
        } else {
            System.out.println("NOT A STRING OR ARRAY!!");
//            System.out.println(passedObject);
        }
        return returnedObject;
    }
}
