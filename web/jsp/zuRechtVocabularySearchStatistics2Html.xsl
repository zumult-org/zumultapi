<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:param name="openTranscriptInZuMultToolTipParam"/>
    <xsl:param name="transcriptIdToolTipParam"/>
    <xsl:param name="openTranscriptParam"/>
    <xsl:param name="viewHitsAsKWICOnTheQueryTabParam"/>
    <xsl:param name="viewHitsAsLemmaTableParam"/>
    <xsl:param name="totalColumnNameParam"/>
    <xsl:param name="hitsParam"/>
    <xsl:param name="transcriptIDParam"/>


    <xsl:template match="/">
        <xsl:if test = "//meta/total > 0"> 
            <table id="statistic_table" class="table table-hover table-sm borderless">
                <thead>
                    <tr>   
                        <th class="numbering"><xsl:value-of select="$totalColumnNameParam"/>: <xsl:value-of select="//meta/totalTranscripts"/></th>
                        <th class="transcript"><xsl:value-of select="$transcriptIDParam"/></th>
                        <th class="numberOfAbsAndRelHits"><xsl:value-of select="$hitsParam"/></th>                          
                        <th class="statistic-link"></th>
                        <th class="kwic-link"></th>
                        <th class="zumult-link"></th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:apply-templates select="//item"/>
                </tbody>   
            </table>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template match="item">
        <tr>
            <td class="numbering"><xsl:value-of select="@row"/></td>
            <td class="transcript">
                <a target="_blank">
                    <xsl:attribute name="title">
                        <xsl:value-of select="$transcriptIdToolTipParam"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:text>../DGDLink?command=showData&amp;id=</xsl:text>
                        <xsl:value-of select="substring(@metadataValue,0, 13)"/>
                    </xsl:attribute>
                    <xsl:value-of select="@metadataValue"/>
                </a>
            </td>
            
            <td class="numberOfAbsAndRelHits">
                    <xsl:value-of select="@numberOfHits"/>
            </td>

            <td class="statistic-link">            
                <form target='_blank' action='../jsp/zuRechtHitStatisticView.jsp' method='post'>
                    <button type="button" class="btn btn-link my-0 py-0 btn-sm btn-open-lemma-table">
                        <xsl:attribute name="title">
                            <xsl:value-of select="$viewHitsAsLemmaTableParam"/>
                        </xsl:attribute> 
                        <xsl:attribute name="data-value-source">
                            <xsl:value-of select="@metadataValue"/>
                        </xsl:attribute>
                        <xsl:value-of select="$viewHitsAsLemmaTableParam"/>
                    </button>
                </form>
            </td>
            
            <td class="kwic-link">
                <button type="button" class="btn btn-link my-0 py-0 btn-sm btn-open-kwic-tab"> 
                    <xsl:attribute name="data-value-source">
                        <xsl:value-of select="@metadataValue"/>
                    </xsl:attribute>
                    <xsl:value-of select="$viewHitsAsKWICOnTheQueryTabParam"/>
                </button>
            </td>
            
            <td class="zumult-link" style="width:150px;">
                <form target='_blank' action='../jsp/zuViel.jsp' method='post'>
                        <input type='hidden' name='transcriptID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="@metadataValue"/>
                            </xsl:attribute>
                        </input>
                        <input type='hidden' name='q'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="//query"/>
                            </xsl:attribute>
                        </input>
                        <button onclick='openTranscript(this)' type="button" class="btn btn-link my-0 py-0 btn-sm">
                            <xsl:attribute name="title">
                                <xsl:value-of select="$openTranscriptInZuMultToolTipParam"/>
                            </xsl:attribute> 
                            <xsl:value-of select="$openTranscriptParam"/>
                        </button>
                </form>
            </td> 
            
        </tr>
    </xsl:template>
    
   
</xsl:stylesheet>