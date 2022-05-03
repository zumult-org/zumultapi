<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
        <xsl:if test = "//meta/total > 0"> 
            <table class="table table-hover table-sm table-borderless">
                <tbody>
                    <xsl:apply-templates select="//hit"/>
                </tbody>   
            </table>
        </xsl:if>
    </xsl:template>
    

    <xsl:template match="hit">
        <tr>
            <td class="transcript py-0">
                <xsl:value-of select="@source"/>
            </td>
        </tr>
    </xsl:template>
    
</xsl:stylesheet>