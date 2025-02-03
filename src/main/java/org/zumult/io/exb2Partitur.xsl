<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="3.0">
    
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
    
    <xsl:template match="/">
            <div>
                
                
                <div style="width:100%; overflow:auto;" >
                    <table class="w-100 d-block d-md-table">
                        <tr>
                            <td class="empty"> </td>
                            <xsl:apply-templates select="//tli"/>
                        </tr>
                        <xsl:apply-templates select="//tier"/>
                    </table>
                </div>
                
                <!-- <div style="width:100%; text-align:center; margin-top:10px;">
                    <svg id="measurement" width="2400" height="200" xmlns="http://www.w3.org/2000/svg" style="background:rgb(220,220,220); border: dotted 3px gray">
                        <text x="5" y="15" fill="black">Some measurement</text>
                        <line x1="0" y1="100" x2="2400" y2="100" style="stroke:gray;stroke-width:2" stroke-dasharray="4" />
                        <circle id="circle1" cx="0" cy="100" r="10" style="fill:red;" />
                        <rect id="rect1" x ="0" y="80" height="12" width ="12" stroke-width ="1px" stroke ="black" fill="white" />
                        <line id="line1" x1="0" y1="100" x2="0" y2="80" style="stroke:black;stroke-width:1"/>
                    </svg>
                    
                </div> -->
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
            <td>
                <xsl:attribute name="class">
                    <!-- <xsl:value-of select="@category"/>--><xsl:text> label</xsl:text>
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
                <xsl:value-of select="../@category"/>
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
    
    
    
</xsl:stylesheet>