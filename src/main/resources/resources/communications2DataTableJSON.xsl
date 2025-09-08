<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="2.0">
    
    <xsl:output method="text"/>
    
    
    <xsl:param name="METADATA_KEY_NAMES"/>
    <xsl:param name="ORDER_COLUMN_NAME"/>
    <xsl:param name="ORDER_DIRECTION"/>
    <xsl:param name="START" as="xs:integer"/>
    <xsl:param name="LENGTH" as="xs:integer"/>
    <xsl:param name="SEARCH_TERM"/>
    
    <xsl:variable name="METADATA_KEY_NAMES_TOKENIZED" select="tokenize($METADATA_KEY_NAMES, ';')"/>
    <xsl:variable name="ORDER_DIRECTION_XSL">
        <xsl:choose>
            <xsl:when test="$ORDER_DIRECTION='desc'">descending</xsl:when>
            <xsl:otherwise>ascending</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    
    <xsl:variable name="SORTED_COMMUNICATIONS">
        <communications>
            <xsl:choose>
                <xsl:when test="string-length($ORDER_COLUMN_NAME) &gt; 0 and $ORDER_COLUMN_NAME!='ID'">
                    <xsl:for-each select="//Communication[contains(lower-case(@Id),lower-case($SEARCH_TERM)) 
                        or descendant::Key[some $t in $METADATA_KEY_NAMES_TOKENIZED satisfies $t = @Name and contains(loewr-case(text()), lower-case($SEARCH_TERM))]]">
                        <xsl:sort select="Description/Key[@Name=$ORDER_COLUMN_NAME]" order="{$ORDER_DIRECTION_XSL}"/>
                        <xsl:copy-of select="."/>                       
                    </xsl:for-each>                
                </xsl:when>
                <xsl:when test="$ORDER_COLUMN_NAME='ID'">
                    <xsl:for-each select="//Communication[contains(lower-case(@Id),lower-case($SEARCH_TERM)) or descendant::Key[some $t in $METADATA_KEY_NAMES_TOKENIZED satisfies $t = @Name and contains(lower-case(text()), lower-case($SEARCH_TERM))]]">
                        <xsl:sort select="@Id" order="{$ORDER_DIRECTION_XSL}"/>
                        <xsl:copy-of select="."/>                       
                    </xsl:for-each>                                    
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="//Communication[contains(lower-case(@Id),lower-case($SEARCH_TERM)) or descendant::Key[some $t in $METADATA_KEY_NAMES_TOKENIZED satisfies $t = @Name and contains(lower-case(text()), lower-case($SEARCH_TERM))]]">
                        <xsl:copy-of select="."/>                                               
                    </xsl:for-each>                    
                </xsl:otherwise>
            </xsl:choose>
        </communications>
    </xsl:variable>
    
    <xsl:template match="/">
        <xsl:text>{</xsl:text>
        "recordsFiltered": <xsl:value-of select="count($SORTED_COMMUNICATIONS/descendant::Communication)"/>,         
        "data":
        <xsl:text>[</xsl:text>
        <xsl:apply-templates select="$SORTED_COMMUNICATIONS/descendant::Communication[position() &gt;= $START and position() &lt; ($START + $LENGTH)]"/>
        <xsl:text>]</xsl:text>
        <xsl:text>}</xsl:text>
    </xsl:template>
    
    <!-- 
        "data": [
            {
                "ID": "none",
                "sexe": "sexe",
                "lieu-naissance": "birth/location",
                "annee-naissance": "birth/date",
                "tranche-age": "age",
                "quartier": "location"
            },    
    -->
    <xsl:template match="Communication">
        <xsl:variable name="DESCRIPTION">
            <xsl:copy-of select="Description"/>
        </xsl:variable>
        <xsl:variable name="AUDIO_ID" select="descendant::Media[ends-with(NSLink, 'mp3')][1]/@Id"/>
        <xsl:variable name="TRANSCRIPT_ID" select="descendant::Transcription[1]/@Id"/>
        <xsl:text>{</xsl:text>
        <xsl:text>"ID": "</xsl:text><xsl:value-of select="@Id"/><xsl:text>",</xsl:text>
        <xsl:for-each select="$METADATA_KEY_NAMES_TOKENIZED">
            <xsl:if test="not(current()='ID')">
                <xsl:text>"</xsl:text><xsl:value-of select="replace(current(), '&quot;', '\\&quot;')"/><xsl:text>": "</xsl:text><xsl:value-of select="normalize-space(replace($DESCRIPTION/descendant::Key[@Name=current()]/text(),'&quot;', '\\&quot;'))"/><xsl:text>",</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:call-template name="ACTIONS">
            <xsl:with-param name="SPEECH_EVENT_ID" select="@Id"/>
            <xsl:with-param name="AUDIO_ID" select="$AUDIO_ID"/>
            <xsl:with-param name="TRANSCRIPT_ID" select="$TRANSCRIPT_ID"/>
        </xsl:call-template>        
        <xsl:text>}</xsl:text>
        <xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>
    
    <xsl:template name="ACTIONS">
        <xsl:param name="SPEECH_EVENT_ID"/>
        <xsl:param name="AUDIO_ID"/>
        <xsl:param name="TRANSCRIPT_ID"/>
        "Actions" : "<![CDATA[<button onclick=\"openMetadata(']]><xsl:value-of select="$SPEECH_EVENT_ID"/><![CDATA[')\" type=\"button\" class=\"btn btn-sm py-0 px-1\" title=\"Show all metadata\"><i class=\"fas fa-info-circle\"></i></button><button onclick=\"showSpeakers(']]><xsl:value-of select="$SPEECH_EVENT_ID"/><![CDATA[')\" type=\"button\" class=\"btn btn-sm py-0 px-1\" title=\"Show speakers for this speech event\"><i class=\"fa-solid fa-people\"></i></button><button onclick=\"playAudio(']]><xsl:value-of select="$AUDIO_ID"/><![CDATA[', this)\" type=\"button\" class=\"btn btn-sm py-0 px-1\" title=\"Play audio\"><i class=\"fa-solid fa-play\"></i></button><button onclick=\"openTranscript(']]><xsl:value-of select="$TRANSCRIPT_ID"/><![CDATA[')\" type=\"button\" class=\"btn btn-sm py-0 px-1\" title=\"Display transcript\"><i class=\"fa-regular fa-file-lines\"></i></button>]]>"   
    </xsl:template>
    
    
</xsl:stylesheet>