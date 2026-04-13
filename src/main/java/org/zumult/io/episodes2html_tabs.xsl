<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:tei="http://www.tei-c.org/ns/1.0"            
    xmlns:exmaralda="https://www.exmaralda.org"
    exclude-result-prefixes="xs math"
    version="3.0">
    
    <xsl:variable name="ROOT" select="/"/>

    <xsl:template match="/">
        <div>
            <ul class="nav nav-tabs" id="myTab" role="tablist">
                <!-- this is the row of tabs -->
                <xsl:for-each select="//tei:spanGrp[@n='episodes']">
                    <li class="nav-item" role="presentation">
                        <a aria-current="page" data-toggle="tab">
                            <xsl:attribute name="class">nav-link<xsl:if test="position()=1"> active</xsl:if></xsl:attribute>
                            <xsl:attribute name="href" select="concat('#', concat('TAB_', replace(@Id, '\.', '_')))"/>
                            <xsl:value-of select="@type"/>                  
                        </a>
                    </li>                
                </xsl:for-each>
            </ul>
            <div class="tab-content" id="myTabContent">
                <xsl:apply-templates select="//tei:spanGrp[@n='episodes']"/>
            </div>            
        </div>
    </xsl:template>
    
    <xsl:template match="tei:spanGrp">
        <div role="tabpanel" aria-labelledby="home-tab">
            <xsl:attribute name="class">tab-pane fade<xsl:if test="not(preceding-sibling::tei:spanGrp[@n='episodes'])"> show active</xsl:if></xsl:attribute>
            <xsl:attribute name="id" select="concat('TAB_', replace(@Id, '\.', '_'))"/>
            <table class="table table-striped table-sm episodes-table">
                <xsl:apply-templates select="tei:span"/>
            </table>
        </div>
    </xsl:template>
    
    <xsl:template match="tei:span">
        <!--
          <span from="ts6831" to="ts6952"
             select="manv_2017_e.NA-01 manv_2017_e.NA-01-ASS manv_2017_e.NFS-01 manv_2017_e.PAT-22"
           >2017_09_E_TR-23_NA-01+NA-01-ASS+NFS-01_PAT-22_ErS_R</span>           
        -->
        <xsl:variable name="FROM" select="@from"/>
        <xsl:variable name="FROM_TIME" select="id($FROM)/@interval"/>
        <xsl:variable name="TO" select="@to"/>
        <xsl:variable name="TO_TIME" select="id($TO)/@interval"/>
        <tr>
            <td class="episode-actions">
                <xsl:variable name="TRANSCRIPT_ID" select="$ROOT/descendant::tei:idno[1]"/>
                <a href="./zuViel.jsp?transcriptID={$TRANSCRIPT_ID}&amp;startTimeID={$FROM}&amp;endTimeID={$TO}&amp;speakerSelection={@select}" 
                    target="_blank"
                    title="Open episode in ZuViel"
                >
                    <i class="fa-sharp fa-regular fa-file-lines"></i>
                </a>
            </td>
            <td class="time px-2">
                <xsl:value-of select="exmaralda:formatTime($FROM_TIME)"/>
            </td>
            <td class="time px-2">
                <xsl:value-of select="exmaralda:formatTime($TO_TIME)"/>
            </td>
            <td class="episode-desc">
                <xsl:value-of select="text()"/>
            </td>
            <td class="speakers">
                <xsl:for-each select="tokenize(@select, ' ')">
                    <xsl:value-of select="$ROOT/descendant::tei:person[tei:idno[1]=current()][1]/@n"/>
                    <xsl:text> </xsl:text>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:function name="exmaralda:formatTime" as="xs:string">
        <xsl:param name="SECONDS"></xsl:param>
        <xsl:variable name="total" select="number($SECONDS)"/>
        <xsl:variable name="hours" select="floor($total div 3600)"/>
        <xsl:variable name="minutes" select="floor(($total mod 3600) div 60)"/>
        <xsl:variable name="seconds" select="$total mod 60"/>
        
        <xsl:choose>
            <xsl:when test="$hours &gt; 0">
                <xsl:value-of select="concat(
                    format-number($hours,'00'), ':',
                    format-number($minutes,'00'), ':',
                    format-number($seconds,'00.00')
                    )"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat(
                    format-number($minutes,'00'), ':',
                    format-number($seconds,'00.00')
                    )"/>                
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    
    
</xsl:stylesheet>