<%-- 
    Document   : diagnostics
    Created on : 23.01.2024, 16:03:24
    Author     : bernd
--%>

<%@page import="java.io.File"%>
<%@page import="org.zumult.io.IOHelper"%>
<%@page import="org.zumult.objects.AnnotationBlock"%>
<%@page import="org.zumult.objects.Transcript"%>
<%@page import="java.util.Random"%>
<%@page import="org.zumult.objects.IDList"%>
<%@page import="org.zumult.backend.BackendInterfaceFactory"%>
<%@page import="org.zumult.backend.BackendInterface"%>
<%@page import="org.zumult.backend.Configuration"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ZuMult Diagnostics</title>
        <style type="text/css">
            .error {color : red;}
            .fine {color : green;}
        </style>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous"/>
        <link rel="stylesheet" href="css/transcript.css"/>
        <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="crossorigin="anonymous"></script>        
        <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>                
        <script src="https://kit.fontawesome.com/ed5adda70b.js" crossorigin="anonymous"></script>
        <script src="js/tgdp.js"></script>
        
        
    </head>
    <body style="margin:15px;">
        <h1>Diagnostics</h1>
        <hr/>
        <h2>Configuration</h2>
        
        <%
            String SYS_ENV_KEY = "ZUMULT_CONFIG_PATH";
            String PATH = System.getenv(SYS_ENV_KEY);
            if (PATH==null){ 
        %>
            <p class="error">Could not find an environment variable named ZUMULT_CONFIG_PATH.<br/>
                Cannot read ZuMult configuration.
            </p>
        <% } else { %>
            <p class="fine">Reading configuration from <%= PATH %>.<p>
            <% 
                String biPath = Configuration.getBackendInterfaceClassPath();
                String metadataPath = Configuration.getMetadataPath();
                boolean metadataPathExists = new File(metadataPath).exists();
                String mediaPath = Configuration.getMediaPath();
                String searchIndexPath = Configuration.getSearchIndexPath();
                boolean indexPathExists = new File(metadataPath).exists();
                
                /*String treeTaggerPath = Configuration.getConfigurationVariable("tree-tagger-directory");
                boolean treeTaggerDirExists =  new File(treeTaggerPath).exists();                
                String treeTaggerPmDe = Configuration.getConfigurationVariable("tree-tagger-parameter-file-german");
                boolean treeTaggerPmDeExists =  new File(treeTaggerPmDe).exists();
                String treeTaggerPmEn = Configuration.getConfigurationVariable("tree-tagger-parameter-file-english");
                boolean treeTaggerPmEnExists =  new File(treeTaggerPmEn).exists();
                
                String phoLexDe = Configuration.getConfigurationVariable("phonetic-lexicon-german");
                boolean phoLexDeExists = new File(phoLexDe).exists();
                String phoLexEn = Configuration.getConfigurationVariable("phonetic-lexicon-english");
                boolean phoLexEnExists = new File(phoLexEn).exists();
                String phoLexOt = Configuration.getConfigurationVariable("phonetic-lexicon-other");
                boolean phoLexOtExists = new File(phoLexOt).exists();*/

            %>
            <ul>
                <li>
                    <b>Backend interface class path: </b> <%= biPath %>
                </li>
                <li>
                    <b>Metadata path: </b> <%= metadataPath %>
                    <% if(metadataPathExists){ %>
                        <span class="fine"> Path exists.</span>
                    <% } else { %>
                        <span class="error"> Path does not exist.</span>
                    <% } %>
                </li>
                <li><b>Media path: </b> <%= mediaPath %></li>
                <li>
                    <b>Lucene index path: </b> <%= searchIndexPath %>
                    <% if(indexPathExists){ %>
                        <span class="fine"> Path exists.</span>
                    <% } else { %>
                        <span class="error"> Path does not exist.</span>
                    <% } %>
                </li>
                
                
            </ul>
        <% } %>        
        <hr/>
        <h2>Backend</h2>
        
        <%
            BackendInterface bi = BackendInterfaceFactory.newBackendInterface();
            IDList corpora = bi.getCorpora();
            String allCorpora = "";
            for (String corpusID : corpora){
                allCorpora+=corpusID + " ";
            }
         %>
         
        <p class="fine">Backend initialised successfully. Found corpora: <%= allCorpora %>.<p>
         
         <%
            String randomCorpusID = corpora.get(new Random().nextInt(corpora.size()));
            
            IDList transcripts = bi.getTranscripts4Corpus(randomCorpusID);
            String randomTranscriptID = transcripts.get(new Random().nextInt(transcripts.size()));
            Transcript randomTranscript = bi.getTranscript(randomTranscriptID);
            String firstAnnotationBlockID = randomTranscript.getFirstAnnotationBlockIDForTime(0.0);
            AnnotationBlock annotationBlock = bi.getAnnotationBlock(randomTranscriptID, firstAnnotationBlockID);
            //String audioID = bi.getAudios4Transcript(randomTranscriptID).get(0);
            String audioID = randomTranscript.getMetadataValue(bi.findMetadataKeyByID("Transcript_Recording ID"));
            
            

        %>
            
        <p>Working with random corpus ID <b><%= randomCorpusID %></b></p>
        <p>Getting first annotation block of random transcript <b><%= randomTranscriptID %></b>:</p>
        <pre>
            <%= annotationBlock.toXML().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;") %>
        </pre>
        
        <%
            String xsl = Configuration.getIsoTei2HTMLStylesheet();
            String[][] parameters = {
                {"FORM", "trans"},
                {"SHOW_NORM_DEV", "TRUE"},
                {"VIS_SPEECH_RATE", "FALSE"},
                {"START_ANNOTATION_BLOCK_ID", ""}, 
                {"END_ANNOTATION_BLOCK_ID", ""},
                {"TOKEN_LIST_URL", ""},
                
                {"HIGHLIGHT_IDS_1", ""},
                {"HIGHLIGHT_IDS_2", ""},
                {"HIGHLIGHT_IDS_3", ""},
            };
            String transcriptHTML = new IOHelper().applyInternalStylesheetToString(xsl, randomTranscript.getPart(firstAnnotationBlockID, firstAnnotationBlockID, true).toXML(), parameters); 
        %>
        <p>
            Applying <b><%= xsl %></b> to 1st line of transcript.
            <%= transcriptHTML %>
        </p>
        
        <p>Getting audio <%= audioID %> of random transcript <b><%= randomTranscriptID %></b>:</p>
        <audio id="masterMediaPlayer" width="480" controls="controls" style="width:480px;">
            <%
                String wavURL = bi.getMedia(audioID).getURL();
                // this is not right or at least clumsy, but COMA only has WAV files
                String mp3URL = wavURL.replaceAll("\\.wav", ".mp3");
            %>
            <source src="<%=mp3URL%>" type="audio/mp3">
        </audio>                                             
        
        <hr/>
        <h2>Query</h2>
        <script>
            doPagedQuery(0,'[lemma="Kuh"]');
        </script>
        <div class="row">
            <div class="col-1">&nbsp;</div>
            <div class="col-9">
                
                <!--  loading indicator -->
                <div id="wait-query" style="display:none;"><i class="fas fa-spinner fa-spin"></i> Querying TGDP transcripts... </div>
                               
                <div class="px-4">    
                    <div class="table-wrapper table-responsive myKWIC" id="kwic_display"></div>
                </div>
                
            </div>
        </div>
        
        




    </body>
</html>
