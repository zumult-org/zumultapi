<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">
    <xsl:output method="xhtml" encoding="UTF-8" omit-xml-declaration="yes"/>
    <!-- all values, sorted alphabetically -->
    <xsl:variable name="vNodes">
        <abc>
            <xsl:for-each select="//distinct-value">
                <xsl:sort select="text()"/>
                <xsl:copy-of select="."/>
            </xsl:for-each>
        </abc>
    </xsl:variable>

    <!-- int numberOfColumns = (int) Math.min(Math.floor(values.size()/20)+1,4); -->
    <!-- int columnSize = (int) Math.ceil(values.size()/numberOfColumns); -->
    <!-- <xsl:variable name="vNumCols" select="min()"/> -->

    <!-- number of rows that I will have -->
    <xsl:variable name="vNumParts" select="count($vNodes/descendant::distinct-value) div 4"/>

    <xsl:variable name="vNumCols"
        select="ceiling(count($vNodes/descendant::distinct-value) div $vNumParts)"/>

    <xsl:template match="/">
        <div style="font-size:smaller">
            <table class="availableValues" id="POSValueTable">
                <xsl:call-template name="INSERT-PREDEFINED-REGEX"/>
                <!-- <xsl:for-each select="//category[tag[not(starts-with(@name,'$'))] and (not(category) or count(category)&gt;1)]"> -->
                <xsl:for-each select="/annotation-specification/annotation-set/category/category">
                    <xsl:sort select="@tag"/>
                    <xsl:choose>
                        <xsl:when test="category">
                            <tr><td colspan="3" style="background:rgb(220,220,220); font-weight:bold; border-top: 1px solid black;"><xsl:value-of select="@name"/></td></tr>
                            <xsl:for-each select="./descendant::category[tag and (not(category) or count(category) &gt; 1)]">
                                <xsl:sort select="tag/@name"/>
                                <xsl:apply-templates select="."/>
                            </xsl:for-each>                            
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="."/>                            
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>                
            </table>
        </div>
    </xsl:template>
    
    <xsl:template match="category">
        <tr name="POSRow">
            <td>
                <xsl:if
                    test="not(ancestor::category[tag]) or (not(preceding-sibling::category) and not(following-sibling::category))">
                    <xsl:attribute name="style">
                        <xsl:text>border-top: 1px solid gray;</xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <button type="button" class="btn btn-dark btn-sm" style="min-width:100px; font-size:9pt;">
                    <xsl:attribute name="onclick">javascript:insertText('<xsl:text>pos=</xsl:text><xsl:value-of
                        select="tag/@name"/>', 'searchBox', this)</xsl:attribute>
                    <xsl:value-of select="tag/@name"/>
                </button>                    
            </td>
            <td>
                <xsl:attribute name="style">
                    <xsl:text>padding-left: 5px;</xsl:text>
                    <xsl:if
                        test="not(ancestor::category[tag]) or (not(preceding-sibling::category) and not(following-sibling::category))">
                            <xsl:text> border-top: 1px solid gray;</xsl:text>
                    </xsl:if>
                </xsl:attribute>
                <xsl:value-of select="@name"/>
            </td>
            <td>
                <xsl:if
                    test="not(ancestor::category[tag]) or (not(preceding-sibling::category) and not(following-sibling::category))">
                    <xsl:attribute name="style">
                        <xsl:text>border-top: 1px solid gray;</xsl:text>
                    </xsl:attribute>
                </xsl:if>
                <i>
                    <xsl:value-of select="description/text()"/>
                </i>
            </td>
        </tr>
        
    </xsl:template>
    
    <xsl:template name="INSERT-PREDEFINED-REGEX">
        <tr><td colspan="3" style="background:rgb(220,220,220); font-weight:bold; border-top: 1px solid black;">POS-Klassen</td></tr>
        <tr name="POSRow">
            <td>
                <button type="button" class="btn btn-dark btn-sm" style="min-width:100px; font-size:9pt;">
                    <xsl:attribute name="onclick">javascript:insertText('pos=ADJ.', 'searchBox', this)</xsl:attribute>
                    ADJ.
                </button>                    
            </td>
            <td style="padding-left: 5px;">Alle Adjektive</td>
            <td><i></i></td>
        </tr>
        <tr name="POSRow">
            <td>
                <button type="button" class="btn btn-dark btn-sm" style="min-width:100px; font-size:9pt;">
                    <xsl:attribute name="onclick">javascript:insertText('pos=V.+', 'searchBox', this)</xsl:attribute>
                    V.+
                </button>                    
            </td>
            <td style="padding-left: 5px;">Alle Verben</td>
            <td><i></i></td>
        </tr>
        <tr name="POSRow">
            <td>
                <button type="button" class="btn btn-dark btn-sm" style="min-width:100px; font-size:9pt;">
                    <xsl:attribute name="onclick">javascript:insertText('pos=(SEDM|PTKMA|PTKIFG|SEQU|NGIRR)', 'searchBox', this)</xsl:attribute>
                    SEDM etc.
                </button>                    
            </td>
            <td style="padding-left: 5px;">Mündlichkeitsphänomene</td>
            <td><i></i></td>
        </tr>
        
    </xsl:template>

</xsl:stylesheet>
