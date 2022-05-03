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
            <td class="my-0 py-0" >
                <form target='_blank' action='../jsp/zuViel.jsp' method='post' class="py-0" style="margin-top: -2px;">                
                        <input type='hidden' name='transcriptID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="@source"/>
                            </xsl:attribute>
                        </input>
                        <input type='hidden' name='startTokenID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="snippet/*[@match='true'][1]/@xml:id"/>
                            </xsl:attribute>
                        </input>
                        <input type='hidden' name='endTokenID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="snippet/*[@match='true'][position()=last()]/@xml:id"/>
                            </xsl:attribute>
                        </input>
                        <button onclick='openExcerpt(this)' title="Show excerpt in ZuMult" type="button" class="btn btn-link my-0 py-0 btn-sm" >
                            öffnen
                        </button>
                </form>                
            </td>    
        </tr>
    </xsl:template>
    
</xsl:stylesheet>