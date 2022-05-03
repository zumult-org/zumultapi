<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:param name="openKWICParam"/>
    <xsl:param name="totalParam"/>
    <xsl:param name="hitsParam"/>
    
    <xsl:template match="/">
        <xsl:if test = "//meta/total > 0"> 
            <table id="metadata-table" class="table table-sm borderless metadata-table">
                <thead>
                    <tr style="text-align: center; background-color: #d9edf7; color: #337AB7;">   
                        <th class="numbering"><xsl:value-of select="$totalParam"/>: <xsl:value-of select="//meta/distinctValues"/></th>
                        <th class="metadataValues"><xsl:value-of select="//meta/metadataKey"/></th>                     
                        <th class="numberOfHits"><xsl:value-of select="$hitsParam"/>, <xsl:value-of select="$totalParam"/>: <xsl:value-of select="//meta/total"/></th>
                        <th class="open-kwic-link"></th>    
                    </tr>
                </thead>
                <tbody class="metadata-table-body">
                    <xsl:apply-templates select="//item"/>
                </tbody>   
            </table>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template match="item">
        <tr class="metadata-table-row">
            <td class="numbering"><xsl:value-of select="@row"/></td>
            <td class="metadataValues" style="color: black; font-weight: bold; text-align: left;">
                <xsl:attribute name="data-value">
                    <xsl:value-of select="@metadataValue"/>                    
                </xsl:attribute>
                <xsl:value-of select="@metadataValue"/>
            </td>         
            <td class="numberOfHits" style="text-align: center;"><xsl:value-of select="@numberOfHits"/></td>  
            <td class="open-kwic-link">
                <button type="button" class="btn btn-link my-0 py-0 btn-sm btn-open-kwic" onclick='openKWIC(this)'> 
                     <xsl:value-of select="$openKWICParam"/>
                </button>
            </td>                    
        </tr>
    </xsl:template>
    
   
</xsl:stylesheet>