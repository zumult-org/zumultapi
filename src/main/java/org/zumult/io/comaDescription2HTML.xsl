<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : communication2HTML.xsl
    Created on : 19 December 2024, 14:17
    Author     : bernd
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>

    <xsl:template match="/">        
        <div>
            <table class="table table-striped table-sm" style="font-size: 0.75rem;">
                <xsl:apply-templates select="//Key">
                    <xsl:sort select="@Name"/>
                </xsl:apply-templates>
            </table>
        </div>        
    </xsl:template>
      
    <xsl:template match="Key">
        <tr>
            <td class="metadatakey"><xsl:value-of select="@Name"/></td>
            <td class="metadatavalue"><xsl:value-of select="text()"/></td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
