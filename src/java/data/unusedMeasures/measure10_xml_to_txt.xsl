<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" encoding="UTF-8"/>

<!--    <xsl:template match="/">
        <xsl:for-each select="measure">
            <xsl:for-each select="@*">
                <xsl:value-of select="name()"/>
                <xsl:text>&#9;</xsl:text>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>-->
    
    <xsl:template match="/">        
        <xsl:text>Speech Event&#9;duration&#9;highestPortionSpokenWords&#9;secondHighestPortionSpokenWords&#9;totalSpeakers&#9;speaker&#9;occupation&#9;who&#9;&#37; contributions pro total contributions&#9;&#37; spoken words pro total words&#9;&#37; contributions of 1-2 words pro contribution size of speaker&#10;</xsl:text>
        <xsl:apply-templates select="//measures"/>
        
    </xsl:template>
    
    <xsl:template match="measures">
        <xsl:value-of select="@speechEventID"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@duration"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@highestPortionSpokenWords"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@secondHighestPortionSpokenWords"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@totalSpeakers"/>
        <xsl:for-each select="speaker">
            <xsl:text>&#10;</xsl:text>
            <xsl:text>&#9;</xsl:text>
            <xsl:text>&#9;</xsl:text>
            <xsl:text>&#9;</xsl:text>
            <xsl:text>&#9;</xsl:text>
            <xsl:text>&#9;</xsl:text>
            <xsl:for-each select="@*">
                <xsl:value-of select="."/>
                <xsl:text>&#9;</xsl:text>
            </xsl:for-each>
            <xsl:for-each select="measure">
                <xsl:for-each select="@*">
                    <xsl:value-of select="."/>
                    <xsl:text>&#9;</xsl:text>
                </xsl:for-each>        
            </xsl:for-each>
        </xsl:for-each>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>