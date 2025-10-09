<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:linguisticbits="http://linguisticbits.de"
    exclude-result-prefixes="xs math"
    version="3.0">
    <xsl:template match="/">
        <table class="table table-striped" style="width:auto">
            <tr>
                <td>Total number of speech events</td>
                <td><xsl:value-of select="/*/@speech-events"/></td>
            </tr>
            <tr>
                <xsl:variable name="DUR_IN_SEC" select="sum(//transcript/@duration)"/>
                <td>Total duration of recordings</td>
                <td>
                    <xsl:value-of select="linguisticbits:TIME-FORMATTER($DUR_IN_SEC)"/>                    
                </td>
            </tr>
            <tr>
                <td>Total number of transcripts</td>
                <td><xsl:value-of select="format-number(count(//transcript), '###,###')"/></td>
            </tr>
            <tr>
                <td>Total number of speakers</td>
                <td><xsl:value-of select="/*/@speakers"/></td>
            </tr>
            <tr>
                <td>Total transcribed tokens</td>
                <td><xsl:value-of select="format-number(sum(//transcript/@tokens), '###,###')"/></td>
            </tr>
            <tr>
                <td>Total transcribed types</td>
                <td><xsl:value-of select="format-number(/*/@types, '###,###')"/></td>
            </tr>            
        </table>
        
        <hr/>
        
        <table class="table table-striped table-sm" style="width: auto;">
            <thead>
                <th>Speech event</th>
                <th>#Transcripts</th>
                <th>#Tokens</th>
                <th>Duration</th>
            </thead>
            <tbody>
                <xsl:apply-templates select="//speech-event">
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>
            </tbody>
        </table>
    </xsl:template>
    
    <xsl:template match="speech-event">
        <tr>
            <td class="text-right" style="vertical-align:top">
                <xsl:value-of select="@id"/>
            </td>
            <td>
                <xsl:value-of select="count(transcript)"/>
            </td>
            <td class="text-right" style="vertical-align:top">
                <xsl:value-of select="format-number(sum(transcript/@tokens), '###,###')"/>
            </td>
            <td class="text-right" style="vertical-align:top">
                <xsl:variable name="DUR_IN_SEC" select="sum(transcript/@duration)"/>
                <xsl:value-of select="linguisticbits:TIME-FORMATTER($DUR_IN_SEC)"/>
            </td>
            <!-- <td colspan="2">
                <table>
                    <xsl:apply-templates select="transcript"/>
                </table>
            </td> -->
        </tr>
    </xsl:template>
    
    <xsl:template match="transcript">
        <tr style="font-size:8pt;">
            <td><xsl:value-of select="substring-after(@id, 'TRS_')"/></td>
            <td><xsl:value-of select="@tokens"/></td>
            <td><xsl:value-of select="@types"/></td>            
        </tr>
    </xsl:template>
    
    <xsl:function name="linguisticbits:TIME-FORMATTER">
        <xsl:param name="DUR_IN_SEC"/>
        <xsl:variable name="minutes" select="floor($DUR_IN_SEC div 60) mod 60" />
        <xsl:variable name="seconds" select="$DUR_IN_SEC mod 60" />
        <xsl:variable name="hours" select="floor($DUR_IN_SEC div 3600)" />
        
        
        <xsl:if test="$hours &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$hours" />                    
        <xsl:text>:</xsl:text>
        
        
        <xsl:if test="$minutes &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$minutes" />
        <xsl:text></xsl:text>
        
        <xsl:text>:</xsl:text>
        
        <xsl:if test="$seconds &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="substring-before(xs:string($seconds), '.')" />
        <xsl:text></xsl:text>
        
    </xsl:function>
    
    
</xsl:stylesheet>