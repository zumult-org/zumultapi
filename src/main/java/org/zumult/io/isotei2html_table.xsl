<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    xmlns:exmaralda="http://www.exmaralda.org"
    xmlns:math="http://exslt.org/math"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="xml" omit-xml-declaration="yes" indent="no"/>
    <xsl:strip-space elements="*"/>

    <!-- whether to include a column for the dropdown menu -->
    <xsl:param name="DROPDOWN">TRUE</xsl:param>
    <!-- whether to include a column for numbering -->
    <xsl:param name="NUMBERING">TRUE</xsl:param>
    <!-- whether to include a row for translation -->    
    <xsl:param name="TRANSLATION">en</xsl:param>
    <!-- 
        which form of tokens to display
        trans : the transcribed form
        norm: the normalized form
        lemma: the lemma
        pos: the pos tag
        phon: the phonetic transcription
    -->
    <xsl:param name="FORM">trans</xsl:param>
    <!-- whether to highlight forms where transcribed and normalized form deviate -->
    <xsl:param name="SHOW_NORM_DEV">TRUE</xsl:param>
    <!-- whether to visualize speech rate via different letter spacing -->
    <xsl:param name="VIS_SPEECH_RATE">TRUE</xsl:param>
    <!-- whether or not to display pauses inside utterances -->
    <xsl:param name="VIS_PAUSE_INSIDE_U">TRUE</xsl:param>
    <!-- Types of incident on top level to be ignored (new 16-06-2025) -->
    <!-- let's expect a semicolon separated list here -->
    <xsl:param name="VIS_INCIDENT_NOT_TYPES"></xsl:param>
    
    
    <!-- start and end of the transcript ; empty for whole transcript -->
    <xsl:param name="START_ANNOTATION_BLOCK_ID"/>
    <xsl:param name="END_ANNOTATION_BLOCK_ID"/>
    
    <xsl:param name="AROUND_ANNOTATION_BLOCK_ID"/>
    <xsl:param name="HOW_MUCH_AROUND"/>
    
    <xsl:param name="HIGHLIGHT_IDS_1"/>
    <xsl:param name="HIGHLIGHT_IDS_2"/>
    <xsl:param name="HIGHLIGHT_IDS_3"/>
    <xsl:param name="HIGHLIGHT_ANNOTATION_BLOCK"/>
    
    <xsl:variable name="HIGHLIGHT">
        <ids>
            <xsl:for-each select="tokenize($HIGHLIGHT_IDS_1, ' ')">
                <id><xsl:value-of select="current()"/></id>
            </xsl:for-each>
        </ids>
    </xsl:variable>
    
    <xsl:variable name="EXTRA_HIGHLIGHT">
        <ids>
            <xsl:for-each select="tokenize($HIGHLIGHT_IDS_2, ' ')">
                <id><xsl:value-of select="current()"/></id>
            </xsl:for-each>
        </ids>
    </xsl:variable>
    

    <xsl:variable name="EXTRA_EXTRA_HIGHLIGHT">
        <ids>
            <xsl:for-each select="tokenize($HIGHLIGHT_IDS_3, ' ')">
                <id><xsl:value-of select="current()"/></id>
            </xsl:for-each>
        </ids>
    </xsl:variable>
    
    <xsl:param name="TOKEN_LIST_URL"/>
    <xsl:variable name="TOKEN_LIST" select="document($TOKEN_LIST_URL)/*"/> 
    
    <xsl:variable name="VIS_INCIDENT_NOT_TYPES_LIST">
        <ids>
            <xsl:for-each select="tokenize($VIS_INCIDENT_NOT_TYPES, ';')">
                <id><xsl:value-of select="current()"/></id>
            </xsl:for-each>                
        </ids>
    </xsl:variable>
    
    <!-- New 16-06-2025 -->
    <!-- Sort in order of appearance? -->
    <xsl:variable name="SORTED_SPEAKERS">
        <tei:particDesc>
            <xsl:for-each-group select="//tei:annotationBlock" group-by="@who">
                <xsl:copy-of select="//tei:person[@xml:id=current-grouping-key()]"/>
            </xsl:for-each-group>
        </tei:particDesc>
    </xsl:variable>
    
    
    <xsl:template match="/">
        <!-- <xsl:message select="$VIS_INCIDENT_NOT_TYPES_LIST"/> -->
        <xsl:apply-templates select="//tei:text"/>
    </xsl:template>
    
    <xsl:template match="tei:text">
        <table class="transcript table-striped table-sm" id="transcript-table">
            <!-- <tr><td><xsl:value-of select="$TOKEN_LIST_URL"/></td></tr> -->
            <xsl:choose>                
                <xsl:when test="$START_ANNOTATION_BLOCK_ID and $END_ANNOTATION_BLOCK_ID">
                    <xsl:apply-templates select="//tei:body/*[not(following-sibling::tei:*[@xml:id=$START_ANNOTATION_BLOCK_ID]) and not(preceding-sibling::tei:*[@xml:id=$END_ANNOTATION_BLOCK_ID])]"/>    
                </xsl:when>
                <xsl:when test="$AROUND_ANNOTATION_BLOCK_ID and $HOW_MUCH_AROUND">
                    <xsl:variable name="AROUND_POSITION" select="count(//tei:body/*[@xml:id=$AROUND_ANNOTATION_BLOCK_ID]/preceding-sibling::*) + 1"/>
                    <xsl:variable name="AROUND_START" select="//tei:body/*[max((1, $AROUND_POSITION - $HOW_MUCH_AROUND))]/@xml:id"/>
                    <xsl:variable name="AROUND_END" select="//tei:body/*[min((count(//tei:body/*), $AROUND_POSITION + $HOW_MUCH_AROUND))]/@xml:id"/>                    
                    <xsl:apply-templates select="//tei:body/*[not(following-sibling::tei:*[@xml:id=$AROUND_START]) and not(preceding-sibling::tei:*[@xml:id=$AROUND_END])]"/>    
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="//tei:body/*"/>
                </xsl:otherwise>
            </xsl:choose>
            
        </table>
    </xsl:template>
    
    <xsl:template match="tei:annotationBlock">
        <xsl:variable name="SPEAKER_ID" select="@who"/>
        
        <tr class="annotationBlock">
            <xsl:attribute name="id" select="concat('tr', @xml:id)"/>            
            <xsl:attribute name="data-annotation-block-id" select="@xml:id"/>
            <xsl:if test="$HIGHLIGHT_ANNOTATION_BLOCK = @xml:id">
                <xsl:attribute name="style">background:lightGreen; font-size:larger;</xsl:attribute>
            </xsl:if>
            
            <td class="tablerow_cursor">
                <xsl:variable name="startAnchor" select="@start"/>
                <xsl:variable name="endAnchor" select="@end"/>
                <xsl:variable name="start" select="//tei:when[@xml:id=$startAnchor]/@interval"/>
                <xsl:variable name="end" select="//tei:when[@xml:id=$endAnchor]/@interval"/>
                <xsl:attribute name="data-start" select="$start"/>
                <xsl:attribute name="data-end" select="$end"/>
                <xsl:text>&#x2009;</xsl:text>
            </td>

            <xsl:if test="$DROPDOWN='TRUE'">
                <xsl:call-template name="MAKE_DROPDOWN"/>
            </xsl:if>

            <xsl:if test="$NUMBERING='TRUE'">
                <td class="numbering">
                    <xsl:variable name="NUMBER" select="count(preceding-sibling::*) + 1"/>
                    <xsl:if test="$NUMBER &lt; 10">0</xsl:if>
                    <xsl:if test="$NUMBER &lt; 100">0</xsl:if>
                    <xsl:if test="$NUMBER &lt; 1000">0</xsl:if>
                    <xsl:value-of select="$NUMBER"/>
                </td>
            </xsl:if>
            
            <td>
                <xsl:attribute name="class">
                    <xsl:text>speaker</xsl:text>
                    <xsl:if test="preceding-sibling::tei:annotationBlock[@who][1][@who=$SPEAKER_ID]">
                        <xsl:text> cont</xsl:text>
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="onclick">showSpeaker('<xsl:value-of select="//tei:person[@xml:id=$SPEAKER_ID]/tei:idno[1]"/>')</xsl:attribute>
                <xsl:attribute name="title">Click to show speaker metadata</xsl:attribute>
                <xsl:attribute name="style">cursor: pointer;</xsl:attribute>
                <xsl:value-of select="//tei:person[@xml:id=$SPEAKER_ID]/@n"/>
            </td>      
            <td class="transcript-text">
                <xsl:attribute name="class">
                    <xsl:text>transcript-text</xsl:text>
                    <xsl:text> color-</xsl:text>
                    <!-- <xsl:value-of select="count(//tei:person[@xml:id=$SPEAKER_ID]/preceding-sibling::tei:person) + 1"/> -->
                    <xsl:value-of select="count($SORTED_SPEAKERS/descendant::tei:person[@xml:id=$SPEAKER_ID]/preceding-sibling::tei:person) + 1"/>
                </xsl:attribute>
                <xsl:if test="$VIS_SPEECH_RATE='TRUE' and descendant::tei:spanGrp[@type='speech-rate']">
                    <xsl:variable name="SPEECH-RATE" select="descendant::tei:spanGrp[@type='speech-rate']/tei:span/text()"/>
                    <xsl:variable name="LETTER-SPACING">
                        <xsl:choose>
                            <xsl:when test="$SPEECH-RATE &lt; 3.5">0.1em</xsl:when>
                            <xsl:when test="$SPEECH-RATE &gt;= 3.5 and $SPEECH-RATE &lt; 5.5">0.05em</xsl:when>
                            <xsl:when test="$SPEECH-RATE &gt;= 5.5 and $SPEECH-RATE &lt; 7.0">0em</xsl:when>
                            <xsl:otherwise>-0.07em</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:attribute name="style">
                        <xsl:text>letter-spacing:</xsl:text>
                        <xsl:value-of select="$LETTER-SPACING"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:apply-templates select="tei:u/child::tei:*"/>                
                <xsl:if test="string-length($TRANSLATION)&gt;0 and descendant::tei:spanGrp[@type=$TRANSLATION]">
                    <br/>
                    <span class="translation">
                        <xsl:apply-templates select="descendant::tei:spanGrp[@type=$TRANSLATION]/tei:span"/>
                    </span>
                </xsl:if>
            </td>
        </tr>
    </xsl:template>
    
    
    <xsl:template match="tei:u"/>
    
    
    <xsl:template match="tei:w">
        <xsl:variable name="ID" select="@xml:id"/>
        <xsl:variable name="norm" select="@norm"/>
        <xsl:variable name="lemma" select="@lemma"/>
        <xsl:variable name="pos" select="@pos"/>
        <xsl:variable name="trans" select="."/>
        <!-- <xsl:variable name="startAnchor" select="preceding-sibling::tei:anchor[1]/@synch"/>        
        <xsl:variable name="endAnchor" select="following-sibling::tei:anchor[1]/@synch"/>      -->   
        <xsl:variable name="startAnchor" select="preceding::tei:anchor[1]/@synch"/>        
        <xsl:variable name="endAnchor" select="following::tei:anchor[1]/@synch"/>
        <xsl:variable name="start" select="//tei:when[@xml:id=$startAnchor]/@interval"/>
        <xsl:variable name="end" select="//tei:when[@xml:id=$endAnchor]/@interval"/>
        <span data-start="{$start}" data-end="{$end}" data-norm="{$norm}" data-lemma="{$lemma}" data-pos="{$pos}" data-id="{$ID}">
            <!-- use this if you have values for intonation level -->
            <!-- <xsl:attribute name="style">
                <xsl:text>vertical-align:</xsl:text><xsl:value-of select="1 - ((position() mod 3) div 5)" /><xsl:text>em</xsl:text>
            </xsl:attribute> -->
            <xsl:attribute name="class">
                <xsl:text>token</xsl:text>
                <xsl:if test="$SHOW_NORM_DEV='TRUE' and upper-case(@norm)!=upper-case(concat(text()[1], text()[2], text()[3]))">
                    <xsl:text> normdev</xsl:text>
                </xsl:if>
                <xsl:if test="$TOKEN_LIST_URL">
                    <xsl:if test="$TOKEN_LIST/descendant::token[@form=$lemma]"> highlight1</xsl:if>
                </xsl:if>
                <xsl:if test="$HIGHLIGHT/descendant::id[text()=$ID]">
                    <xsl:text> highlight2</xsl:text>
                </xsl:if>
                <xsl:if test="$EXTRA_HIGHLIGHT/descendant::id[text()=$ID]">
                    <xsl:text> highlight3</xsl:text>
                </xsl:if>
                <xsl:if test="$EXTRA_EXTRA_HIGHLIGHT/descendant::id[text()=$ID]">
                    <xsl:text> highlight4</xsl:text>
                </xsl:if>
            </xsl:attribute>

            <!-- <xsl:if test="contains(@type, 'ol-in')">
                <xsl:variable name="PRECEDING-TIMEPOINT">
                    <xsl:value-of select="preceding::tei:anchor[1]/@synch"/>
                </xsl:variable>
                <xsl:variable name="OVERLAP-ID">OL-<xsl:value-of select="$PRECEDING-TIMEPOINT"/></xsl:variable>
                <xsl:attribute name="name" select="$OVERLAP-ID"/>
                <xsl:attribute name="onmouseover">highlight('<xsl:value-of select="$OVERLAP-ID"/>')</xsl:attribute>
                <xsl:attribute name="onmouseout">lowlight('<xsl:value-of select="$OVERLAP-ID"/>')</xsl:attribute>
            </xsl:if> -->
            
            <xsl:attribute name="title">
                <xsl:value-of select="@norm"/>
                <xsl:text> / </xsl:text>
                <xsl:value-of select="@lemma"/>
                <xsl:text> / </xsl:text>
                <xsl:value-of select="@pos"/>
                <xsl:if test="@phon">
                    <xsl:text> / </xsl:text>
                    <xsl:value-of select="@phon"/>                    
                </xsl:if>
            </xsl:attribute>
            

            <xsl:choose>
                <xsl:when test="$FORM='norm'">
                    <xsl:choose>
                        <xsl:when test="@norm='#' or @norm='&amp;' or @norm='%'"><!-- do not display dummies --></xsl:when>
                        <xsl:otherwise><xsl:value-of select="@norm"/></xsl:otherwise>
                    </xsl:choose>
                                        
                </xsl:when>
                <xsl:when test="$FORM='lemma'">
                    <xsl:value-of select="@lemma"/>                    
                </xsl:when>
                <xsl:when test="$FORM='pos'">
                    <xsl:value-of select="@pos"/>                    
                </xsl:when>
                <xsl:when test="$FORM='phon'">
                    <xsl:value-of select="@phon"/>                    
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>                    
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="@type='repair'">/</xsl:if>
            <xsl:if test="not(following-sibling::*[1][self::tei:pc]) and following-sibling::*">
                <xsl:text> </xsl:text>                
            </xsl:if>
        </span>
    </xsl:template>
    
    <xsl:template match="tei:pc">
        <xsl:apply-templates/>                    
        <xsl:text> </xsl:text>                
    </xsl:template>
    
    <xsl:template match="tei:w/text()">
        <!-- inside overlap? -->
        <xsl:choose>
            <!-- <w id="w898" pos="VVIMP" lemma="trinken" n="trink">tr<time timepoint-reference="TLI_496" time="667.475" ol="start"/>ink</w> -->
            <xsl:when test="contains(parent::*/@type,'in') and not(preceding-sibling::tei:anchor[@type='ol-end']) and not(following-sibling::tei:anchor[@type='ol-start'])">
                <span>
                    <xsl:variable name="PRECEDING-TIMEPOINT">
                        <xsl:value-of select="preceding::tei:anchor[1]/@synch"/>
                    </xsl:variable>
                    <xsl:variable name="OVERLAP-ID">OL-<xsl:value-of select="$PRECEDING-TIMEPOINT"/></xsl:variable>
                    <xsl:attribute name="name" select="$OVERLAP-ID"/>
                    <xsl:attribute name="onmouseover">highlight('<xsl:value-of select="$OVERLAP-ID"/>')</xsl:attribute>
                    <xsl:attribute name="onmouseout">lowlight('<xsl:value-of select="$OVERLAP-ID"/>')</xsl:attribute>
                    <xsl:value-of select="."/>
                </span>                                
            </xsl:when>
            <xsl:when test="string-length(normalize-space())=0"></xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>                
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template match="tei:body/*[not(self::tei:annotationBlock)]">
        <xsl:variable name="THIS_TYPE" select="@type"/>
        <xsl:if test="not(@type) or not($VIS_INCIDENT_NOT_TYPES_LIST/descendant::id[text()=$THIS_TYPE])">
            <xsl:variable name="ID" select="@xml:id"/>
            <tr class="nonAnnotationBlock">
                <xsl:attribute name="id" select="concat('tr', @xml:id)"/>            
                <xsl:attribute name="data-annotation-block-id" select="@xml:id"/>            
                <td class="tablerow_cursor">
                    <xsl:variable name="startAnchor" select="@start"/>
                    <xsl:variable name="endAnchor" select="@end"/>
                    <xsl:variable name="start" select="//tei:when[@xml:id=$startAnchor]/@interval"/>
                    <xsl:variable name="end" select="//tei:when[@xml:id=$endAnchor]/@interval"/>
                    <xsl:attribute name="data-start" select="$start"/>
                    <xsl:attribute name="data-end" select="$end"/>
                    <xsl:text>&#x2009;</xsl:text>
                    <xsl:value-of select="@type"/>
                </td>
                <xsl:if test="$DROPDOWN='TRUE'">
                    <xsl:call-template name="MAKE_DROPDOWN"/>
                </xsl:if>
                <xsl:if test="$NUMBERING='TRUE'">
                    <td class="numbering">
                        <xsl:variable name="NUMBER" select="count(preceding-sibling::*) + 1"/>
                        <xsl:if test="$NUMBER &lt; 10">0</xsl:if>
                        <xsl:if test="$NUMBER &lt; 100">0</xsl:if>
                        <xsl:if test="$NUMBER &lt; 1000">0</xsl:if>
                        <xsl:value-of select="$NUMBER"/>
                    </td>
                </xsl:if>
                
                <td> </td>
                <td>
                    <span>
                        <xsl:attribute name="class">
                            <xsl:text>transcript-text </xsl:text>
                            <xsl:choose>
                                <xsl:when test="self::tei:pause">pause</xsl:when>
                                <xsl:otherwise>desc</xsl:otherwise>
                            </xsl:choose>
                            <xsl:if test="$EXTRA_HIGHLIGHT/descendant::id[text()=$ID]">
                                <xsl:text> highlight-search-extra</xsl:text>
                            </xsl:if>
                            <xsl:if test="$HIGHLIGHT/descendant::id[text()=$ID]">
                                <xsl:text> highlight-search</xsl:text>
                            </xsl:if>
                        </xsl:attribute>
                        <xsl:attribute name="id" select="@xml:id"/>
                        <xsl:choose>
                            <xsl:when test="descendant-or-self::*[@rend]">
                                <xsl:value-of select="descendant-or-self::*[@rend]/@rend"/>                            
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>((</xsl:text>
                                <xsl:value-of select="descendant::tei:desc"/>
                                <xsl:text>)) </xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text> </xsl:text>
                    </span>                
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="tei:seg[parent::tei:seg and not(child::tei:seg)]">
        <xsl:apply-templates/>
        <!-- <xsl:choose>
            <xsl:when test="@type='utterance' and @subtype='interrogative'">
                <xsl:text>? </xsl:text>                
            </xsl:when>
            <xsl:when test="@type='utterance' and @subtype='interrupted'">
                <xsl:text>... </xsl:text>                
            </xsl:when>
            <xsl:when test="@type='utterance' and @subtype='modeless'">
                <xsl:text>&#x02d9; </xsl:text>                
            </xsl:when>
            <xsl:when test="@type='utterance' and @subtype='declarative'">
                <xsl:text>. </xsl:text>                
            </xsl:when>
            <xsl:when test="@type='utterance' and @subtype='exclamative'">
                <xsl:text>! </xsl:text>                
            </xsl:when>
        </xsl:choose> -->
        <xsl:if test="following-sibling::tei:seg">
            <br/>
        </xsl:if>        
    </xsl:template>
    
    <!-- to do : highlight -->
    <xsl:template match="tei:seg//tei:pause">
        <xsl:if test="$VIS_PAUSE_INSIDE_U='TRUE'">
            <xsl:variable name="startAnchor" select="preceding-sibling::tei:anchor[1]/@synch"/>        
            <xsl:variable name="endAnchor" select="following-sibling::tei:anchor[1]/@synch"/>        
            <xsl:variable name="start" select="//tei:when[@xml:id=$startAnchor]/@interval"/>
            <xsl:variable name="end" select="//tei:when[@xml:id=$endAnchor]/@interval"/>
            <span class="pause">
                <xsl:attribute name="id" select="@xml:id"/>
                <xsl:attribute name="data-start" select="$start"/>
                <xsl:attribute name="data-end" select="$end"/>
                <xsl:choose>
                    <xsl:when test="@rend">
                        <xsl:value-of select="@rend"/>
                        <xsl:text> </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="@type='micro'">(.) </xsl:when>
                            <xsl:when test="@type='short'">(-) </xsl:when>
                            <xsl:when test="@type='medium'">(--) </xsl:when>
                            <xsl:when test="@type='long'">(--) </xsl:when>
                            <xsl:otherwise><xsl:text>(</xsl:text><xsl:value-of select="substring-before(substring-after(@dur, 'PT'), 'S')"/><xsl:text>) </xsl:text></xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </span>
        </xsl:if>
    </xsl:template>
    
    
    
    <xsl:template match="tei:anchor[@type]">
        <xsl:choose>
            <xsl:when test="@type='ol-start'"><span class="ol-marker">[</span></xsl:when>
            <xsl:otherwise><span class="ol-marker">]</span></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- to do : highlight -->
    <xsl:template match="tei:seg//tei:desc">
        <span class="desc">
            <xsl:choose>
                <xsl:when test="@rend">
                    <xsl:value-of select="@rend"/>
                    <xsl:text> </xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>((</xsl:text>
                    <xsl:value-of select="."/>
                    <xsl:text>)) </xsl:text>                    
                </xsl:otherwise>
            </xsl:choose>
        </span>
    </xsl:template>
    
    <xsl:template name="MAKE_DROPDOWN">
        <td class="td-dropdown">
            <div class="dropdown">
                <button class="btn btn-secondary dropdown-toggle btn-sm" type="button" id="dropdownMenuButton" 
                        data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"
                        style="font-size:0.5rem;"
                >
                    <i class="fa fa-bars fa-xs"></i>                    
                </button>
                <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                    <a class="dropdown-item" onclick="javascript:setStartSelection(this)">Start Selection</a>
                    <a class="dropdown-item" onclick="javascript:setEndSelection(this)">End Selection</a>
                    <a class="dropdown-item" onclick="javascript:showAnnotations(this)">Annotations...</a>
                    <a class="dropdown-item" onclick="javascript:showPartitur(this)">Partitur...</a>
                    <a class="dropdown-item" onclick="javascript:showZuMin(this)">Micro View (ZuMin)...</a>
                </div>
            </div>                
        </td>        
    </xsl:template>
    
    <xsl:function name="exmaralda:wordText">
        <xsl:param name="W_ELEMENT"/>
        <xsl:for-each select="$W_ELEMENT/descendant::text()">
            <xsl:value-of select="."/>
        </xsl:for-each>
    </xsl:function>

</xsl:stylesheet>