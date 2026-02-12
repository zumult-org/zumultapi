/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zumult.io;

/**
 *
 * @author Schmidt
 */
public class Constants {
    
    // new for #265
    public static String BASIC_LAYER_NAME = "norm";

    public static final String ZUMULT_PROJECT_ULR = "https://zumult.org";
    
    public static String ISOTEI2HTML_STYLESHEET = "/org/zumult/io/isotei2html.xsl";
    // now configurable as per #174
    public static String ISOTEI2HTML_STYLESHEET2 = "/org/zumult/io/isotei2html_table.xsl";
    public static String ISOTEI2HTML_ANNOTATIONS_STYLESHEET = "/org/zumult/io/isotei2html_annotations.xsl";
    public static String WORDLIST2HTML_STYLESHEET = "/org/zumult/io/tokenlist2html_table.xsl";
    
    // new for #175
    public static String EVENT2HTML_STYLESHEET = "/org/zumult/io/communication2HTML_full.xsl";
    public static String SPEECHEVENT2HTML_STYLESHEET = "/org/zumult/io/communication2HTML.xsl";
    public static String SPEAKER2HTML_STYLESHEET = "/org/zumult/io/speaker2HTML.xsl";
    public static String EVENT_TITLE_METADATAKEY = "v_e_se_art";
    
    // issue #55
    public static String WORDLIST2HTML_PRINT_STYLESHEET = "/org/zumult/io/tokenlist2html_print.xsl";
    public static String WORDLIST2TXT_DOWNLOAD_STYLESHEET = "/org/zumult/io/tokenlist2txt_download.xsl";
    public static String TRANSCRIPT2CHRONOWORDLISTHTML_STYLESHEET = "/org/zumult/io/transcript2chronowordlisthtml_table.xsl";
    public static String TRANSCRIPT2CHRONOWORDLISTTXT_STYLESHEET = "/org/zumult/io/transcript2chronowordlisttxt_table.xsl";
    
    public static String ISOTEI2SVG_STYLESHEET = "/org/zumult/io/isotei2score_svg.xsl";
    public static String ISOTEI2HTML_HIGHLIGHT_TOKENS_STYLESHEET = "/org/zumult/io/isotei2html_highlightTokens.xsl";
    public static String MEASURE2HTML_STYLESHEET = "/org/zumult/io/measures2html.xsl";
    public static String STATISTICS2HTML_STYLESHEET = "/org/zumult/io/statistics2HTML.xsl";
    public static String PROTOCOL2HTML_STYLESHEET = "/org/zumult/io/protocol2html.xsl";
    public static String SPEECHEVENTXML2JSON = "/org/zumult/indexing/zumal/SpeechEventXML2Json.xsl";
    
    public static String SAMPLE_QUERIES_FOR_TRASCRIPT_BASED_SEARCH = "org/zumult/query/examples/sample_queries_12.xml";
    public static String SAMPLE_QUERIES_FOR_SPEAKER_BASED_SEARCH = "org/zumult/query/examples/sample_queries_11.xml";
    public static String WITHOUT_PUNCTUTION_EXT ="without_Punct";
    public static String WITH_PUNCTUTION_EXT ="with_Punct";
    
    public static final String DATA_PATH = "/data/";
    public static final String DATA_ANNOTATIONS_PATH = "/data/annotations/";
    public static final String DATA_MEASURES_PATH = "/data/measures/";
    public static final String DATA_QUANTIFICATIONS_PATH = "/data/quantifications/";
    public static final String DATA_ZUMAL_PATH = "/data/zumal/";
    public static final String DATA_POS_PATH = "/data/pos/";

    public static String[] LEIPZIG_WORDLISTS =
        {
            "GOETHE_A1", "GOETHE_A2", "GOETHE_B1",
            "HERDER_1000", "HERDER_2000", "HERDER_3000", "HERDER_4000", "HERDER_5000" ,
            "ESSEN", "HAUS_UND_WOHNUNG", "SCHULE_UND_AUSBILDUNG"
        };
    
    public static final String WORDFIELD_PATTERN = "WORTFELD_(.+?)\\.txt";
    public static final String CORPUS_DELIMITER = "|";
    public static final String SPEAKER_DELIMITER = " ";
    public static final String TOKEN_DELIMITER = "|";
    public static final String EMPTY_TOKEN = "[ ]";
    public static final String TOKEN_INTERVAL_DELIMITER = " - ";
    public static final String CORPUS_SIGLE_PATTERN = "corpusSigle=(.*)";
    public static final String CORPUS_SIGLE = "corpusSigle";
            
    public static final String METADATA_KEY_MATCH_TYPE_WORD ="word";
    public static final String METADATA_KEY_MATCH_TYPE_WORD_TYPE ="word.type";
    public static final String METADATA_KEY_MATCH_TYPE_PARA ="para";
    public static final String METADATA_KEY_MATCH_TYPE_PAUSE_DURATION = "pause.dur";
    public static final String METADATA_KEY_MATCH_TYPE_PAUSE_DURATION_CEIL = "pause.dur.ceil";
    public static final String METADATA_KEY_MATCH_TYPE_PAUSE_TYPE = "pause.type";
    public static final String METADATA_KEY_MATCH_TYPE_ANNOTATION_BLOCK_SPEAKER = "annotationBlock.speaker";
    public static final String METADATA_KEY_MATCH_TYPE_ANNOTATION_BLOCK_ID = "annotationBlock.id";
    
    public static final String METADATA_KEY_MATCH_LOWERCASE ="_lc";
    
    
    // additional metadata
    public static final String METADATA_KEY_EVENT_DGD_ID = "e_dgd_kennung";
    public static final String METADATA_KEY_SPEAKER_DGD_ID = "s_dgd_kennung";
    public static final String METADATA_KEY_TRANSCRIPT_DGD_ID = "t_dgd_kennung";
    public static final String METADATA_KEY_SPEECH_EVENT_SPEECH_NOTES = "e_se_grad_der_muendlichkeit";
    public static final String METADATA_KEY_SPEECH_EVENT_DGD_ID = "e_se_dgd_kennung";
    public static final String METADATA_KEY_SPEAKER_YEAR_OF_BIRTH = "s_geb";
    public static final String METADATA_KEY_EVENT_DAUER_SEC =  "e_dauer_sek";
    public static final String METADATA_KEY_EVENT_DURATION = "e_dauer";
    public static final String METADATA_KEY_SPEAKER_BIRTH_DATE = "s_geb_jahr";
    public static final String METADATA_KEY_SPEAKER_BIRTH_AGE = "ses_alter_s";
    public static final String METADATA_KEY_EVENT_NUMBER_VIDEOS = "t_video_anzahl";

    
    // not indexed speaker metadata (in TB mode)
    public static final String METADATA_KEY_SPEAKER_NAME = "s_name";
    public static final String METADATA_KEY_SPEAKER_OTHER_NAMES = "s_sonstige_bezeichnungen";
    public static final String METADATA_KEY_SPEAKER_PSEUDONYM = "s_pseudonym";
    
    public static final String SPANGRP_SUBTYPE_TIME_BASED = "time-based";
    public static final String SPANGRP_TYPE_SPEAKER_OVERLAP = "speaker-overlap";
    public static final String SPANGRP_TYPE_OVERLAP = "overlap";
    public static final String SPANGRP_TYPE_META = "meta";
    public static final String SPANGRP_TYPE_OVERLAP_WITH_COMMON_EVENTS = "overlap-with-common-events";
    public static final String SPANGRP_TYPE_TOKEN_OVERLAP = "token-overlap";
    public static final String SPANGRP_TYPE_ANOTHER_SPEAKER = "another-speaker";// if current speaker is silent, but another one s talking
    public static final String SPANGRP_TYPE_REPETITION = "repetition";
    
    public static final String SEARCH_TYPE_DOWNLOAD = "download";
    public static final String SEARCH_TYPE_STANDARD = "standard";
    
    // kwic context configuration (example for url parameter: context=3-t,3-t)
    public static final String KWIC_CONTEXT_ITEM_FOR_TOKEN = "t";
    public static final String KWIC_CONTEXT_ITEM_FOR_CHARACTERS = "c";
    public static final String KWIC_DEFAULT_CONTEXT_ITEM = KWIC_CONTEXT_ITEM_FOR_TOKEN;
    public static final int KWIC_DEFAULT_CONTEXT_LENGTH = 5;
    public static final String KWIC_CONTEXT_DELIMITER = "-";
    public static final String KWIC_LEFT_RIGHT_CONTEXT_DELIMITER = ",";
    public static final int KWIC_TOKEN_LEFT_CONTEXT_LENGTH_MAX = 25;
    public static final int KWIC_TOKEN_RIGHT_CONTEXT_LENGTH_MAX = 50;
    public static final int KWIC_TOKEN_CONTEXT_LENGTH_AFTER_FIRST_MATCH_MAX = 150;
    
    public static final int MAX_DISTANCE_BETWEEN_REPETITIONS = 50; /* for user interface see maxDistanceBetweenRepetitions in zuRechtRepetitionXMLCreator.jps */
    
    // result page configuration
    public static final boolean DEFAULT_CUTOFF = true;
    public static final int DEFAULT_PAGE_LENGTH = 10; 
    public static final int PAGE_LENGTH_MAX = 250;
    public static final int DEFAULT_PAGE_INDEX = 0;
    
    //kwic download configuration
    public static final int DEFAUL_NUMBER_FOR_KWIC_DOWNLOAD = 1000;
    public static final int MAX_NUMBER_FOR_KWIC_DOWNLOAD = 50000;

    // query language configuration
    public static final String DEFAULT_QUERY_LANGUAGE = "cqp";
    public static final String DEFAULT_CQP_QUERY_LANGUAGE_VERSION = "MTAS 8.4.1.2";
    
    public static final String TEI_NAMESPACE_URL = "http://www.tei-c.org/ns/1.0";
    
    public static final String ATTRIBUTE_NAME_START = "start";
    public static final String ATTRIBUTE_NAME_END = "end";
    public static final String ATTRIBUTE_NAME_TYPE = "type";
    public static final String ATTRIBUTE_NAME_SUBTYPE = "subtype";
    public static final String ATTRIBUTE_NAME_FROM = "from";
    public static final String ATTRIBUTE_NAME_TO = "to";
    public static final String ATTRIBUTE_NAME_WHO = "who";
    public static final String ATTRIBUTE_NAME_TARGET = "target";
    public static final String ATTRIBUTE_NAME_SYNCH = "synch";
    public static final String ATTRIBUTE_NAME_N = "n";
    public static final String ATTRIBUTE_NAME_DUR = "dur";
    public static final String ATTRIBUTE_NAME_REND = "rend";
    public static final String ATTRIBUTE_NAME_DESC = "desc";
    public static final String ATTRIBUTE_NAME_ITERVAL = "interval";
    public static final String ATTRIBUTE_NAME_CLASS = "class";
    public static final String ATTRIBUTE_NAME_PROXY_START = "proxy_start";
    public static final String ATTRIBUTE_NAME_PROXY_END = "proxy_end";
    public static final String ATTRIBUTE_VALUE_PROXY = "proxy";
    public static final String ATTRIBUTE_NAME_DUR_ROUND = "dur-round";
    public static final String ATTRIBUTE_NAME_DUR_CEIL = "dur-ceil";
    public static final String ATTRIBUTE_NAME_ID = "id";
    public static final String ATTRIBUTE_NAME_LEMMA = "lemma";
    public static final String ATTRIBUTE_NAME_NORM ="norm";
    public static final String ATTRIBUTE_NAME_POS ="pos";
    public static final String ATTRIBUTE_NAME_PHON ="phon";
    
    public static final String ELEMENT_NAME_BODY = "body";
    public static final String ELEMENT_NAME_ANNOTATION_BLOCK = "annotationBlock";
    public static final String ELEMENT_NAME_U = "u";
    public static final String ELEMENT_NAME_SEG = "seg";
    public static final String ELEMENT_NAME_SPAN_GRP = "spanGrp";
    public static final String ELEMENT_NAME_SPAN = "span";
    public static final String ELEMENT_NAME_PAUSE = "pause";
    public static final String ELEMENT_NAME_INCIDENT = "incident";
    public static final String ELEMENT_NAME_VOCAL = "vocal";
    public static final String ELEMENT_NAME_TEXT = "text";
    public static final String ELEMENT_NAME_PERSON = "person";
    public static final String ELEMENT_NAME_WHEN = "when";
    public static final String ELEMENT_NAME_ANCHOR = "anchor";
    public static final String ELEMENT_NAME_WORD_TOKEN ="w";
    public static final String ELEMENT_NAME_IDNO = "idno";
    public static final String ELEMENT_NAME_PC = "pc";
    public static final String ELEMENT_NAME_TEI_HEADER = "teiHeader";
    public static final String ELEMENT_NAME_PARTIC_DESC = "particDesc";
    public static final String ELEMENT_NAME_PROFILE_DESC = "profileDesc";
    
    
    public static final String[] TOKENS =
        {
            ELEMENT_NAME_WORD_TOKEN, ELEMENT_NAME_PAUSE, ELEMENT_NAME_INCIDENT,
            ELEMENT_NAME_VOCAL, ELEMENT_NAME_PC /*, "kinesic"??? */           
        };
        
    public static final String TOKEN_INTERVAL = "token.interval"; 
        
    
       
    // token annotation layers not specified in ZuMultAvailableAnnotationValues.xml
    public static final String[][] PROXI_TOKEN_ANNOTATION_LAYERS = 
    {
        {METADATA_KEY_MATCH_TYPE_WORD, "TranscribedForm"},
        {METADATA_KEY_MATCH_TYPE_WORD+METADATA_KEY_MATCH_LOWERCASE, "TranscribedFormInLowercase"},
        {ATTRIBUTE_NAME_NORM + METADATA_KEY_MATCH_LOWERCASE,"NormalizedFormInLowercase"},
        {ATTRIBUTE_NAME_LEMMA + METADATA_KEY_MATCH_LOWERCASE,"LemmaInLowercase"},
        {METADATA_KEY_MATCH_TYPE_PAUSE_DURATION, "PauseDuration"},
        {METADATA_KEY_MATCH_TYPE_PAUSE_DURATION_CEIL, "PauseDurationCeil"},
        {METADATA_KEY_MATCH_TYPE_PAUSE_TYPE, "PauseType"},
        {METADATA_KEY_MATCH_TYPE_PARA, "AllNonWords"}
    };

    public static final String[][] PROXI_SPAN_ANNOTATION_LAYERS = 
    {
        {ELEMENT_NAME_ANNOTATION_BLOCK, "SpeakerContrubution"},
        {METADATA_KEY_MATCH_TYPE_ANNOTATION_BLOCK_SPEAKER, "SpeakerInitials"},
        {SPANGRP_TYPE_SPEAKER_OVERLAP, "SpeakerOverlap"},
        {SPANGRP_TYPE_REPETITION, "Repetition"}
    };
                
    public static final String DEFAULT_LOCALE = "en-US";
    public static final String DEFAULT_CORPUS = "EXMARaLDA-DemoKorpus";
    public static final String RANDOM_ID = "RANDOM-ID";
    public static final String DEFAULT_METADATA_KEY_SPEECH_EVENT = "e_se_art";
    public static final String DEFAULT_POS_TAGSET = "STTS_2_0";
    
    public static final String DIFF_NORM = "diffNorm";
    
    /* xml element names for repetition objects 
       (used for the object serialization between backend and frontend)*/
    public static final String REPETITIONS = "repetitions";
    public static final String REPETITION = "repetition";
    public static final String REPETITION_SPEAKER ="speaker";
    public static final String REPETITION_SPEAKER_METADATA="speakerMetadata";
    public static final String REPETITION_TYPE = "repetitionType";
    public static final String REPETITION_SIMILARITY_TYPE="repetitionSimilarityType";
    public static final String REPETITION_GERMANET_ITEMS="germaNetItems";
    public static final String REPETITION_MIN_DISTANCE="minDistance";
    public static final String REPETITION_MAX_DISTANCE="maxDistance";
    public static final String REPETITION_IGNORE_FUNCTIONAL_WORDS="ignoreFunctionalWords";
    public static final String REPETITION_IGNORED_POS="ignoredCustomPOS";
    public static final String REPETITION_IGNORE_TOKEN_ORDER="ignoreTokenOrder";
    public static final String REPETITION_SPEAKER_CHANGE="speakerChange";
    public static final String REPETITION_POSITION_TO_OVERLAP="positionToOverlap";
    public static final String REPETITION_POSITION_TO_SPEAKER_CHANGE_TYPE="positionToSpeakerChangeType";
    public static final String REPETITION_POSITION_TO_SPEAKER_CHANGE_MIN_DISTANCE="positionToSpeakerChangeMin";
    public static final String REPETITION_POSITION_TO_SPEAKER_CHANGE_MAX_DISTANCE="positionToSpeakerChangeMax";
    public static final String REPETITION_CONTEXT_LEFT="precededby";
    public static final String REPETITION_CONTEXT_RIGHT="followedby";
    public static final String REPETITION_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_LEFT="withinSpeakerContributionLeft";
    public static final String REPETITION_CONTEXT_WITHIN_SPEAKER_CONTRIBUTION_RIGHT="withinSpeakerContributionRight";
    public static final String REPETITION_SYNONYMS="synonyms"; 
    public static final String REPETITION_CONTEXT_LEFT_MIN_DISTANCE="minDistanceToContextLeft";
    public static final String REPETITION_CONTEXT_LEFT_MAX_DISTANCE="maxDistanceToContextLeft";
    public static final String REPETITION_CONTEXT_RIGHT_MIN_DISTANCE="minDistanceToContextRight";
    public static final String REPETITION_CONTEXT_RIGHT_MAX_DISTANCE="maxDistanceToContextRight";

    public static final String PIPE_SYMBOL ="\\|";
    
    public static final String CUSTOM_WORDLISTS_KEY = "wordLists";
    
    public static final String CUSTOM_WORDLISTS_TOKEN_DELIMITER = "COMMA";
    public static final String CUSTOM_WORDLISTS_VARIABLE_TOKEN_DELIMITER = "DOPPELPUNKT";
    public static final String CUSTOM_WORDLISTS_VARIABLE_DELIMITER = "STRICHPUNKT";
    
    
    
    public static final String DEFAULT_HELP_URL = "https://docs.google.com/document/d/1ZMhqHD6YnNRcKvku9ZpOFe2zQ5jF2HEZBE0DTuFU1q8/edit?usp=sharing";
}
