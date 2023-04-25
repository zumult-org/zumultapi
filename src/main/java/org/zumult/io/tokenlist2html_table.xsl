<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
  
    
    <xsl:output method="xhtml" omit-xml-declaration="yes"/>

    <xsl:param name="TOKEN_LIST_URL"/>
    <xsl:param name="TOKEN_LIST_ARRAY"/> 
   
    
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
                    <xsl:for-each select="//token[not(@form='#' or @form='%' or @form='&amp;')]">
                        <xsl:sort select="upper-case(@form)"/>
                        <xsl:copy-of select="."/>
                    </xsl:for-each>                       
                </tokenList>
            </xsl:variable>
            
            
            <xsl:for-each select="$SORTED_TOKENS/descendant::token">
                <xsl:if test="upper-case(substring(@form,1,1)) != upper-case(substring(preceding-sibling::token[1]/@form,1,1))">
                    <tr>
                        <td colspan="2">
                            <xsl:attribute name="style">background-color: darkGray; color: white; font-weight:bold;</xsl:attribute>
                            <xsl:value-of select="upper-case(substring(@form,1,1))"/>
                        </td>
                    </tr>
                </xsl:if>
                <tr>
                    <td class="wordlist-entry">
                        <xsl:attribute name="onclick">javascript:findForms('<xsl:value-of select="$TYPE"/>', '<xsl:value-of select="@form"/>')</xsl:attribute>
                       <!-- checkmark if it's in the other wordlist -->
                       <xsl:if test="$TOKEN_LIST/descendant::token[@form=current()/@form]"><span style="color: green">&#x2713; </span></xsl:if>
                       <xsl:if test="$ARRAY/token[@form=current()/@form]"><span style="color: red">&#x2713; </span></xsl:if>
                      
                       <xsl:value-of select="@form"/>                        
                    </td>
                    <td><xsl:value-of select="@frequency"/></td>
                </tr>
                
            </xsl:for-each>
        </table>            
    </xsl:template>
    
    <xsl:template match="token">
    </xsl:template>
    
    
    <xsl:template name="MAKE_ALPHABET_INDEX">
        <xsl:for-each-group select="//token" group-by="upper-case(substring(@form,1,1))">
            <xsl:sort select="current-grouping-key()"/>
            <div class="abc_index">
                <xsl:attribute name="onclick">javascript:scrollToToken('<xsl:value-of select="current-grouping-key()"/>')</xsl:attribute>
                <xsl:attribute name="style">background-color: blue; color: white; font-weight:bold; margin: 3px; padding: 3px; text-align: center;</xsl:attribute>
                <xsl:value-of select="current-grouping-key()"/>
            </div>            
        </xsl:for-each-group>
        
    </xsl:template>
</xsl:stylesheet>