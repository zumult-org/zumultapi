<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:exmaralda="http://www.exmaralda.org"        
    exclude-result-prefixes="xs math"
    version="3.0">
    
    <!-- whether to include a th for controls -->
    <xsl:param name="CONTROLS">FALSE</xsl:param>
    
    
    <xsl:variable name="TIMELINE_COPY">
        <timeline>
            <xsl:for-each select="//tli">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:attribute name="position" select="count(preceding-sibling::tli) + 1"/>
                </xsl:copy>
            </xsl:for-each>
        </timeline>
    </xsl:variable>
    
    <xsl:variable name="SPEAKERTABLE_COPY">
        <speakertable>
            <xsl:for-each select="//speaker">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:attribute name="position" select="count(preceding-sibling::speaker) + 1"/>
                </xsl:copy>
            </xsl:for-each>            
        </speakertable>    
    </xsl:variable>
    
    <xsl:variable name="TIER_CATEGORIES">
        <tier-categories>
            <xsl:for-each-group select="//tier" group-by="@category">
                <xsl:sort select="."/>
                <category>
                    <xsl:attribute name="type" select="current-group()[1]/@type"/>
                    <xsl:value-of select="current-grouping-key()"/>
                </category>
            </xsl:for-each-group>
        </tier-categories>
    </xsl:variable>
    
    <xsl:template match="/">
            <div>
                <div style="width:100%; overflow:auto;" >
                    <table class="w-100 d-block d-md-table">
                        <tr>
                            <td class="empty"> </td>
                            <xsl:apply-templates select="//tli"/>
                        </tr>
                        <xsl:apply-templates select="//tier">
                            <xsl:sort select="exmaralda:tierSorter(.)" data-type="number" order="ascending"/>
                        </xsl:apply-templates>
                    </table>
                </div>
            </div>
        
    </xsl:template>
    
    <xsl:template match="tli">
        <td class="tli">
            <xsl:attribute name="data-start">
                <xsl:value-of select="@time"/>
            </xsl:attribute>
            <xsl:attribute name="data-end">
                <xsl:value-of select="following-sibling::tli[1]/@time"/>
            </xsl:attribute>
            <xsl:value-of select="position()"/>
        </td>
    </xsl:template>
    
    <xsl:template match="tier">
        <xsl:variable name="TIER_COPY">
            <xsl:copy-of select="."/>
        </xsl:variable>
        <tr>
            <xsl:if test="$CONTROLS='TRUE'">
                <xsl:call-template name="INSERT_CONTROLS">
                    <xsl:with-param name="TIER" select="."/>
                </xsl:call-template>
            </xsl:if>
            <td>
                <xsl:attribute name="class">
                    <xsl:value-of select="@type"/><xsl:text> label</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="@display-name"/>
            </td>
            <xsl:apply-templates select="event"/>
        </tr>
    </xsl:template>
    
    <xsl:template match="event">
        <xsl:variable name="START" select="@start"/>
        <xsl:choose>
            <xsl:when test="preceding-sibling::event[1]">
                <xsl:variable name="PRECEDING_END" select="preceding-sibling::event[1]/@end"/>
                <xsl:if test="not(@start=$PRECEDING_END)">
                    <td class="empty">
                        <xsl:attribute name="colspan">
                            <xsl:value-of select="$TIMELINE_COPY/descendant::tli[@id=$START]/@position - $TIMELINE_COPY/descendant::tli[@id=$PRECEDING_END]/@position"/>
                        </xsl:attribute>
                    </td>                    
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$TIMELINE_COPY/descendant::tli[@id=$START]/@position &gt;1">
                    <td class="empty">
                        <xsl:attribute name="colspan">
                            <xsl:value-of select="$TIMELINE_COPY/descendant::tli[@id=$START]/@position - 1"/>
                        </xsl:attribute>
                    </td>                    
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
        
        <xsl:variable name="END" select="@end"/>
        <xsl:variable name="COLSPAN" select="$TIMELINE_COPY/descendant::tli[@id=$END]/@position - $TIMELINE_COPY/descendant::tli[@id=$START]/@position"/>
        <td>
            <xsl:attribute name="colspan">
                <xsl:value-of select="$COLSPAN"/>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>event </xsl:text>
                <xsl:text> </xsl:text>
                <xsl:value-of select="../@type"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="exmaralda:getCSSCategory(../@category, ../@type)"/>
            </xsl:attribute>
            <xsl:attribute name="data-start">
                <xsl:value-of select="$TIMELINE_COPY/descendant::tli[@id=$START]/@time"/>
            </xsl:attribute>
            <xsl:attribute name="data-end">
                <xsl:value-of select="$TIMELINE_COPY/descendant::tli[@id=$END]/@time"/>
            </xsl:attribute>
            <xsl:value-of select="text()"/>
        </td>
        
        <xsl:if test="not(following-sibling::event[1])">
            <td class="empty">
                <xsl:variable name="COLSPAN" select="$TIMELINE_COPY/descendant::tli[last()]/@position - $TIMELINE_COPY/descendant::tli[@id=$END]/@position"/>
                <xsl:attribute name="colspan">
                    <xsl:value-of select="$COLSPAN"/>
                </xsl:attribute>
            </td>                    
            
        </xsl:if>
        
    </xsl:template>
    
    <xsl:template name="INSERT_CONTROLS">
        <xsl:param name="TIER"/>
        <td class="controls">
            <!-- **************************************************** -->
            <!-- *** Buttons to do things with this tier         *** -->    
            <!-- **************************************************** -->
            <div class="container">
                <div class="btn-group m-3" role="group"> 
                    <button id="next-event-btn-{$TIER/@id}" class="btn btn-outline-primary btn-sm"
                        title="Click to find next event in this tier" onclick="findNextEvent('{$TIER/@id}')">
                        <i class="fa-solid fa-right-to-line"></i>
                    </button>              
                    <button id="hide-tier-btn-{$TIER/@id}" class="btn btn-outline-primary btn-sm"
                        title="Click to hide tier" onclick="hideTier('{$TIER/@id}')">
                        <i class="fa-solid fa-eye-slash"></i>
                    </button>                                    
                </div>
            </div>
        </td>
        
    </xsl:template>

    <xsl:function name="exmaralda:getCSSCategory" as="xs:string">
        <xsl:param name="CATEGORY"/>
        <xsl:param name="TYPE"/>
        <xsl:value-of select="concat($TYPE, '-', count($TIER_CATEGORIES/descendant::category[text()=$CATEGORY]/preceding-sibling::category[@type=$TYPE])+1)"/>
    </xsl:function>
        

    <xsl:function name="exmaralda:tierSorter" as="xs:integer">
        <xsl:param name="TIER"/>
        <xsl:variable name="TIER_SPEAKER" select="$TIER/@speaker"/>
        <xsl:variable name="TIER_CATEGORY" select="$TIER/@category"/>
        <xsl:variable name="SPEAKER_WEIGHT" as="xs:integer">
            <xsl:choose>
                <xsl:when test="$SPEAKERTABLE_COPY/descendant::speaker[@id=$TIER_SPEAKER]">
                    <xsl:variable name="POS" select="$SPEAKERTABLE_COPY/descendant::speaker[@id=$TIER_SPEAKER]/@position"/>
                    <xsl:value-of select="xs:integer($POS)"/>                    
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="floor(count($SPEAKERTABLE_COPY/descendant::speaker) + 1)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="TYPE_WEIGHT" as="xs:integer">
            <xsl:choose>
                <xsl:when test="$TIER/@type='t'">2</xsl:when>
                <xsl:when test="$TIER/@type='d'">4</xsl:when>
                <xsl:when test="$TIER/@type='a'">6</xsl:when>
                <xsl:otherwise>8</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>        
        <xsl:variable name="CATEGORY_WEIGHT" select="count($TIER_CATEGORIES/descendant::category[text()=$TIER_CATEGORY]/preceding-sibling::category) + 1" as="xs:integer"/>
        <xsl:value-of select="xs:integer(100 * $SPEAKER_WEIGHT + 10 * $TYPE_WEIGHT + $CATEGORY_WEIGHT)"/>           
    </xsl:function>
    
    
    
</xsl:stylesheet>