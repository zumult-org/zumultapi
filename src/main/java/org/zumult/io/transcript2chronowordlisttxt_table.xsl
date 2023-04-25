<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"     
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text"/>
    <xsl:param name="TOKEN_LIST_URL"/>
    <xsl:param name="TOKEN_LIST_ARRAY"/> 
    
    <!-- one of : wordformsLemma | wordformsTranscribed -->
    <xsl:param name="WORD_FORMS">wordformsTranscribed</xsl:param>
    
    <!-- one of : selectionAll | selectionSelected | selectionUnselected -->
    <xsl:param name="SELECTION">selectionAll</xsl:param>
    
    <!-- one of : sortABCFreq | sortABC | sort321 | sortChrono -->
    <xsl:param name="SORTING">sortChrono</xsl:param>
    
    <xsl:variable name="ARRAY">
        <xsl:for-each select="tokenize($TOKEN_LIST_ARRAY, ';')">
            <xsl:element name="token">
                <xsl:attribute name="form" select="current()"/>
            </xsl:element>
        </xsl:for-each>  
    </xsl:variable>
    
    <xsl:variable name="TAB"><xsl:text>&#x9;</xsl:text></xsl:variable>
    <xsl:variable name="NL"><xsl:text>&#xD;&#xA;</xsl:text></xsl:variable>
    
    <xsl:variable name="TOKEN_LIST">
        <xsl:choose>
            <xsl:when test="$TOKEN_LIST_URL">
                <!-- <xsl:value-of select="document($TOKEN_LIST_URL)/*"/> -->
                <xsl:copy-of select="document($TOKEN_LIST_URL)/*"/>
            </xsl:when>
            <xsl:otherwise>
                <tokenList/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable> 

    <xsl:template match="/">
        <xsl:variable name="TYPE" select="/*/@type"/>
        <xsl:apply-templates select="//tei:w"/>                    
    </xsl:template>

    <xsl:template match="tei:w">
        <xsl:variable name="IS_COVERED" as="xs:boolean" select="exists($TOKEN_LIST/descendant::token[@form=current()/@lemma])"/>
        <xsl:choose>
            <xsl:when test="$SELECTION='selectionAll' or ($SELECTION='selectionSelected' and $IS_COVERED) or ($SELECTION='selectionUnselected' and not($IS_COVERED))">
                <!-- checkmark if it's in the other wordlist -->
                <xsl:if test="$ARRAY/token[@form=current()/@form]">&#x2713; <xsl:value-of select="$TAB"/></xsl:if>                        
                    <xsl:choose>
                        <xsl:when test="$WORD_FORMS='wordformsLemma'">
                            <xsl:value-of select="@lemma"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates/>
                        </xsl:otherwise>
                    </xsl:choose>                        
                    
                    <xsl:if test="$WORD_FORMS='wordformsTranscribed'">
                        <xsl:value-of select="$TAB"/>
                        <xsl:value-of select="@norm"/>
                        <xsl:value-of select="$TAB"/>
                        <xsl:value-of select="@lemma"/>                        
                    </xsl:if>
                    
                    <xsl:value-of select="$NL"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- do nothing - we do not want this entry -->
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
</xsl:stylesheet>