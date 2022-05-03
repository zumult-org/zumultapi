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
        <xsl:text>Speech Event&#9;all&#8240;&#9;ung&#8240;&#9;heit&#8240;&#9;keit&#8240;&#9;schaft&#8240;&#9;tät&#8240;&#9;ion&#8240;&#9;ieren&#8240;&#10;</xsl:text>
        <xsl:apply-templates select="//measures"/>
        
    </xsl:template>
    
    <xsl:template match="measures">
        <xsl:value-of select="@speechEventID"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@perMilAll"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:for-each select="measure">
            <xsl:for-each select="@*">
                <xsl:value-of select="."/>
                <xsl:text>&#9;</xsl:text>
            </xsl:for-each>        
        </xsl:for-each>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>