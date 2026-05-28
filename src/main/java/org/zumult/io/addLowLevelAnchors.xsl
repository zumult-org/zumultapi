<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:tei="http://www.tei-c.org/ns/1.0"    
    exclude-result-prefixes="xs math"
    version="3.0">
    
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="tei:seg[not(tei:seg) and not(child::*[1][self::tei:anchor]) or not(child::*[last()][self::tei:anchor])]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="not(child::*[1][self::tei:anchor])">
                <xsl:copy-of select="preceding::tei:anchor[1]"/>                
            </xsl:if>
            <xsl:apply-templates select="node()"/>
            <xsl:if test="not(child::*[last()][self::tei:anchor])">
                <xsl:copy-of select="following::tei:anchor[1]"/>                
            </xsl:if>            
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>