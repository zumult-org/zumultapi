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
            <td class="speakerNumber my-0 py-0" style="white-space: nowrap">              
                <xsl:value-of select="string-length(normalize-space(snippet/@who)) - string-length(translate(normalize-space(snippet/@who),' ','')) +1"/> 
            </td>   
        </tr>
    </xsl:template>
    

    
</xsl:stylesheet>