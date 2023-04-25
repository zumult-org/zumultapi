<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
  
    
    <xsl:output method="text" encoding="UTF-8"/>

    <xsl:param name="TOKEN_LIST_URL"/>
    <xsl:param name="TOKEN_LIST_ARRAY"/> 
    
    <!-- one of : wordformsLemma | wordformsTranscribed -->
    <xsl:param name="WORD_FORMS">wordformsLemma</xsl:param>
    
    <!-- one of : selectionAll | selectionSelected | selectionUnselected -->
    <xsl:param name="SELECTION">selectionAll</xsl:param>
   
    <!-- one of : sortABCFreq | sortABC | sort321 | sortChrono -->
    <xsl:param name="SORTING">sortABCFreq</xsl:param>
    
    <xsl:variable name="TAB"><xsl:text>&#x9;</xsl:text></xsl:variable>
    <xsl:variable name="NL"><xsl:text>&#xD;&#xA;</xsl:text></xsl:variable>
    
    
    

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
    <!-- <xsl:variable name="TOKEN_LIST" select="document($TOKEN_LIST_URL)/*"/> --> 
    
    
    <xsl:template match="/">
        
        <xsl:variable name="TYPE" select="/*/@type"/>
        
        <table class="wordlist table-striped table-sm">
            <!--  <token form="rechnen" frequency="2"/> -->
            <xsl:variable name="SORTED_TOKENS">
                <tokenList>
                    <xsl:choose>
                        <xsl:when test="$SORTING='sortABCFreq' or $SORTING='sortABC'">
                            <xsl:for-each select="//token[not(@form='#' or @form='%' or @form='&amp;')]">
                                <xsl:sort select="upper-case(@form)"/>
                                <xsl:variable name="IS_COVERED" as="xs:boolean" select="exists($TOKEN_LIST/descendant::token[@form=current()/@form])"/>
                                <xsl:if test="$SELECTION='selectionAll' or ($SELECTION='selectionSelected' and $IS_COVERED=true()) or ($SELECTION='selectionUnselected' and $IS_COVERED=false())">
                                    <xsl:copy-of select="."/>                                    
                                </xsl:if>
                            </xsl:for-each>                                                   
                        </xsl:when>
                        <xsl:when test="$SORTING='sort321'">
                            <xsl:for-each select="//token[not(@form='#' or @form='%' or @form='&amp;')]">
                                <xsl:sort select="@frequency" data-type="number" order="descending"/>
                                <xsl:variable name="IS_COVERED" as="xs:boolean" select="exists($TOKEN_LIST/descendant::token[@form=current()/@form])"/>
                                <xsl:if test="$SELECTION='selectionAll' or ($SELECTION='selectionSelected' and $IS_COVERED=true()) or ($SELECTION='selectionUnselected' and $IS_COVERED=false())">
                                    <xsl:copy-of select="."/>                                    
                                </xsl:if>
                            </xsl:for-each>                                                   
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- chronological, we shouldn't get here -->
                            <xsl:for-each select="//token[not(@form='#' or @form='%' or @form='&amp;')]">
                                <xsl:copy-of select="."/>
                            </xsl:for-each>                                                                               
                        </xsl:otherwise>
                    </xsl:choose>
                    
                </tokenList>
            </xsl:variable>
            
            
            <xsl:for-each select="$SORTED_TOKENS/descendant::token">
               <!-- checkmark if it's in the other wordlist -->
               <!-- <xsl:if test="$TOKEN_LIST/descendant::token[@form=current()/@form]"><span style="color: green">&#x2713; </span></xsl:if> -->
               <xsl:if test="$ARRAY/token[@form=current()/@form]">&#x2713; </xsl:if>
              
               <xsl:value-of select="@form"/>                        
                <xsl:if test="$SORTING='sortABCFreq' or $SORTING='sort321'">                    
                    <xsl:value-of select="$TAB"/>
                    <xsl:value-of select="@frequency"/>
                </xsl:if>
                <xsl:value-of select="$NL"/>
                
            </xsl:for-each>
        </table>            
    </xsl:template>
    
    <xsl:template match="token">
    </xsl:template>
    
    
</xsl:stylesheet>