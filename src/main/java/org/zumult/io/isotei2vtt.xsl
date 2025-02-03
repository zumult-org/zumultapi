<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    xmlns:exmaralda="http://www.exmaralda.org"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text"/>
    <xsl:strip-space elements="*"/>    
    <!-- 
        which form of tokens to display
        trans : the transcribed form
        norm: the normalized form
        lemma: the lemma
        pos: the pos tag
        phon: the phonetic transcription
    -->
    <xsl:param name="TYPE">trans</xsl:param>
    
    
    
    <xsl:template match="/">
        <xsl:text>WEBVTT</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text>&#xa;</xsl:text>        
        <xsl:apply-templates select="//tei:annotationBlock"/>
    </xsl:template>
    
    <xsl:template match="tei:annotationBlock">
        <xsl:variable name="SPEAKER_ID" select="@who"/>
        <xsl:value-of select="position()"/>
        <xsl:text>&#xa;</xsl:text>
        
        <xsl:variable name="START" select="@start"/>        
        <xsl:variable name="END" select="@end"/>
        <xsl:variable name="START_TIME" select="//tei:when[@xml:id=$START]/@interval"/>        
        <xsl:variable name="END_TIME" select="//tei:when[@xml:id=$END]/@interval"/>
        <xsl:value-of select="exmaralda:format_time($START_TIME, 'true')"/>
        <xsl:text> --&gt; </xsl:text>
        <xsl:value-of select="exmaralda:format_time($END_TIME, 'true')"/>
        <xsl:text>&#xa;</xsl:text>
        
        <!-- speaker in bold -->
        <xsl:text>&lt;b&gt;</xsl:text>
        <xsl:value-of select="//tei:person[@xml:id=$SPEAKER_ID]/@n"/>
        <xsl:text>: </xsl:text>        
        <xsl:text>&lt;/b&gt;</xsl:text>

        <xsl:choose>
            <xsl:when test="$TYPE='trans'">
                <xsl:apply-templates select="descendant::tei:seg"/>                
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="descendant::tei:seg[descendant::tei:w]"/>                                
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>
    
    <xsl:template match="tei:seg">
        <xsl:choose>
            <xsl:when test="$TYPE='norm'"><xsl:apply-templates select="descendant::tei:w[not(@norm='%' or @norm='&amp;')]"/></xsl:when>
            <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
    
    <xsl:template match="tei:w">
        <xsl:choose>
            <xsl:when test="$TYPE='norm'"><xsl:value-of select="@norm"/></xsl:when>
            <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
        </xsl:choose>
        <xsl:if test="not(following-sibling::*[1][self::tei:pc]) and following-sibling::*">
            <xsl:text> </xsl:text>                
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="tei:pc">
        <xsl:apply-templates/>                    
        <xsl:text> </xsl:text>                
    </xsl:template>
    
    
    <xsl:template match="tei:desc">
        <xsl:choose>
            <xsl:when test="@rend">
                <xsl:value-of select="@rend"/>
                <xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>((</xsl:text>
                <xsl:apply-templates/>
                <xsl:text>)) </xsl:text>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>
    
    <xsl:template match="tei:pause">
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
                    <xsl:when test="@type='long'">(---) </xsl:when>
                    <xsl:otherwise><xsl:text>(</xsl:text><xsl:value-of select="substring-before(substring-after(@dur, 'PT'), 'S')"/><xsl:text>) </xsl:text></xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>
    
    
    
    <xsl:function name="exmaralda:format_time">
        <xsl:param name="time_sec"/>
        <xsl:param name="include_hours"/>
        <xsl:variable name="totalseconds">
            <xsl:value-of select="0 + $time_sec"/>
        </xsl:variable>
        <xsl:variable name="hours">
            <xsl:value-of select="0 + floor($totalseconds div 3600)"/>
        </xsl:variable>
        <xsl:variable name="minutes">
            <xsl:value-of select="0 + floor(($totalseconds - 3600*$hours) div 60)"/>
        </xsl:variable>
        <xsl:variable name="seconds">
            <xsl:value-of select="0 + ($totalseconds - 3600*$hours - 60*$minutes)"/>
        </xsl:variable>
        <xsl:if test="$include_hours='true'">
            <xsl:if test="$hours+0 &lt; 10 and $hours &gt;0">
                <xsl:text>0</xsl:text>
                <xsl:value-of select="$hours"/>
            </xsl:if>
            <xsl:if test="$hours + 0 = 0">
                <xsl:text>00</xsl:text>
            </xsl:if>
            <xsl:text>:</xsl:text>
        </xsl:if>
        <xsl:if test="$minutes+0 &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$minutes"/>
        <xsl:text>:</xsl:text>
        <xsl:variable name="roundsec">
            <xsl:value-of select="round($seconds)"/>
        </xsl:variable>
        <!-- changed 04-03-2010 -->
        <xsl:value-of select="format-number($seconds, '00.000')"/>
    </xsl:function>
    
    
    
</xsl:stylesheet>