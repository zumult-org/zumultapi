<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"     
    exclude-result-prefixes="xs"
    version="2.0">
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
        
        <table class="wordlist table-striped table-sm">
            <xsl:apply-templates select="//tei:w"/>                    
        </table>   
        
    </xsl:template>

    <xsl:template match="tei:w">
        <xsl:variable name="IS_COVERED" as="xs:boolean" select="exists($TOKEN_LIST/descendant::token[@form=current()/@lemma])"/>
        <xsl:choose>
            <xsl:when test="$SELECTION='selectionAll' or ($SELECTION='selectionSelected' and $IS_COVERED) or ($SELECTION='selectionUnselected' and not($IS_COVERED))">
                <tr>
                    <td class="wordlist-entry">
                        <!-- checkmark if it's in the other wordlist -->
                        <xsl:if test="$ARRAY/token[@form=current()/@lemma]"><span style="color: black">&#x2713; </span></xsl:if>
                        
                        <xsl:choose>
                            <xsl:when test="$WORD_FORMS='wordformsLemma'">
                                <xsl:value-of select="@lemma"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates/>
                            </xsl:otherwise>
                        </xsl:choose>                        
                    </td>
                    
                    <xsl:if test="$WORD_FORMS='wordformsTranscribed'">
                        <td class="wordlist-entry">
                            <xsl:value-of select="@norm"/>
                        </td>
                        <td class="wordlist-entry">
                            <xsl:value-of select="@lemma"/>
                        </td>
                        
                    </xsl:if>
                </tr>                
            </xsl:when>
            <xsl:otherwise>
                <!-- do nothing - we do not want this entry -->
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
</xsl:stylesheet>