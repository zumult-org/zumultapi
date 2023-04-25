<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:exmaralda="http://www.exmaralda.org"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
        <table id="protocolTable" class="protocolTable">
            <xsl:apply-templates select="//contribution"></xsl:apply-templates>
        </table>
    </xsl:template>
    
    <xsl:template match="contribution">
        <xsl:variable name="START_ID" select="@start-reference"/>
        <xsl:variable name="END_ID" select="@end-reference"/>
        <xsl:variable name="START_TIME" select="//timepoint[@timepoint-id=$START_ID]/@absolute-time"/>
        <xsl:variable name="END_TIME" select="//timepoint[@timepoint-id=$END_ID]/@absolute-time"/>
        <tr>
            <td class="protocol-time">
                <xsl:value-of select="exmaralda:format_time($START_TIME, 'true')"/>
            </td>
            <td class="protocol-entry">
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>javascript:gotoTime('</xsl:text>
                        <xsl:value-of select="$START_TIME"/>
                        <xsl:text>')</xsl:text>
                    </xsl:attribute>
                    <xsl:apply-templates/>
                </a>
            </td>
            <td class="protocol-time">
                <xsl:value-of select="exmaralda:format_time($END_TIME, 'true')"/>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:function name="exmaralda:format_time">
        <xsl:param name="time_sec"/>
        <xsl:param name="include_hours"/>
        <xsl:variable name="totalseconds">
            <xsl:value-of select="0 + $time_sec"/>
        </xsl:variable>
        <xsl:variable name="hours">
            <xsl:value-of select="0 + floor($totalseconds div 3600)"/>
        </xsl:variable>
        <xsl:variable name="minutes">
            <xsl:value-of select="0 + floor(($totalseconds - 3600*$hours) div 60)"/>
        </xsl:variable>
        <xsl:variable name="seconds">
            <xsl:value-of select="0 + ($totalseconds - 3600*$hours - 60*$minutes)"/>
        </xsl:variable>
        <xsl:if test="$include_hours='true'">
            <xsl:if test="$hours+0 &lt; 10 and $hours &gt;0">
                <xsl:text>0</xsl:text>
                <xsl:value-of select="$hours"/>
            </xsl:if>
            <xsl:if test="$hours + 0 = 0">
                <xsl:text>00</xsl:text>
            </xsl:if>
            <xsl:if test="$hours + 0 &gt;= 10">
                <xsl:value-of select="$hours"/>                
            </xsl:if>
            <xsl:text>:</xsl:text>
        </xsl:if>
        <xsl:if test="$minutes+0 &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$minutes"/>
        <xsl:text>:</xsl:text>
        <xsl:variable name="roundsec">
            <xsl:value-of select="round($seconds)"/>
        </xsl:variable>
        <!-- changed 04-03-2010 -->
        <!-- <xsl:value-of select="format-number($seconds, '00.00')"/> -->
        <xsl:value-of select="format-number($roundsec, '00')"/>
    </xsl:function>
    
</xsl:stylesheet>