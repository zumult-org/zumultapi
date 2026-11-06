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
    <xsl:param name="CUSTOM_ACTIONS_ID"/>
    
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
                        or descendant::Key[some $t in $METADATA_KEY_NAMES_TOKENIZED satisfies $t = @Name and contains(lower-case(text()), lower-case($SEARCH_TERM))]]">
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
        <!-- <xsl:apply-templates select="$SORTED_COMMUNICATIONS/descendant::Communication[position() &gt;= $START and position() &lt; ($START + $LENGTH)]"/> -->
        <xsl:apply-templates
            select="$SORTED_COMMUNICATIONS/descendant::Communication[
            position() &gt;= $START and position() &lt; ($START + $LENGTH)
            ]/descendant::Media"/>        
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
    
    <xsl:template match="Media">
        <xsl:variable name="DESCRIPTION">
            <xsl:copy-of select="Description"/>
        </xsl:variable>
        <xsl:variable name="MEDIA_ID" select="@Id"/>
        <xsl:variable name="MEDIA_TYPE">
            <xsl:choose>
                <xsl:when test="ends-with(NSLink, 'mp3') or ends-with(NSLink, 'wav')">AUDIO</xsl:when>
                <xsl:otherwise>VIDEO</xsl:otherwise>
            </xsl:choose>            
        </xsl:variable>
        <xsl:text>{</xsl:text>
        <xsl:text>"ID": "</xsl:text><xsl:value-of select="@Id"/><xsl:text>",</xsl:text>
        <xsl:for-each select="$METADATA_KEY_NAMES_TOKENIZED">
            <xsl:if test="not(current() = ('ID', 'Actions'))">
                <xsl:text>"</xsl:text><xsl:value-of select="replace(current(), '&quot;', '\\&quot;')"/><xsl:text>": "</xsl:text><xsl:value-of select="normalize-space(replace($DESCRIPTION/descendant::Key[@Name=current()]/text(),'&quot;', '\\&quot;'))"/><xsl:text>",</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:call-template name="ACTIONS">
            <xsl:with-param name="SPEECH_EVENT_ID" select="@Id"/>
            <xsl:with-param name="MEDIA_ID" select="$MEDIA_ID"/>
            <xsl:with-param name="MEDIA_TYPE" select="$MEDIA_TYPE"/>
        </xsl:call-template>        
        <xsl:text>}</xsl:text>
        <xsl:if test="position() != last()">,</xsl:if>        
    </xsl:template>
    
    <xsl:template name="ACTIONS">
        <xsl:param name="SPEECH_EVENT_ID"/>
        <xsl:param name="MEDIA_ID"/>
        <xsl:param name="MEDIA_TYPE"/>
        <xsl:text>"Actions": "</xsl:text>
        
        <!-- Metadata -->
        <xsl:text><![CDATA[<button onclick=\"openMediaMetadata(']]></xsl:text>
        <xsl:value-of select="$MEDIA_ID"/>
        <xsl:text><![CDATA[')\" type=\"button\" class=\"btn btn-sm py-0 px-1\" title=\"Show all metadata\"><i class=\"fa-solid fa-circle-info\"></i></button>]]></xsl:text>
        
        <xsl:choose>
            <xsl:when test="upper-case(normalize-space($MEDIA_TYPE)) = 'VIDEO'">
                <!-- Video -->
                <xsl:text><![CDATA[<button onclick=\"playVideo(']]></xsl:text>
                <xsl:value-of select="$MEDIA_ID"/>
                <xsl:text><![CDATA[', this)\" type=\"button\" class=\"btn btn-sm py-0 px-1\" title=\"Play video\"><i class=\"fa-solid fa-video\"></i></button>]]></xsl:text>                        
            </xsl:when>
            <xsl:otherwise>
                <!-- Audio -->
                <xsl:text><![CDATA[<button onclick=\"playAudio(']]></xsl:text>
                <xsl:value-of select="$MEDIA_ID"/>
                <xsl:text><![CDATA[', this)\" type=\"button\" class=\"btn btn-sm py-0 px-1\" title=\"Play audio\"><i class=\"fa-solid fa-play\"></i></button>]]></xsl:text>                        
            </xsl:otherwise>
        </xsl:choose>                                
        <xsl:text>"</xsl:text>                
    </xsl:template>
    
    
</xsl:stylesheet>