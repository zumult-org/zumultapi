<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:param name="speechEventID"/>
    
    <xsl:template match="/">        
        <div class="container" style="margin-bottom: 10px; font-size:smaller">
            <xsl:value-of select="//Sprechereignis[@Kennung=$speechEventID]/Inhalt/Beschreibung"/>
        </div>            
        <div class="table">
            <table class="table-striped" style="font-size: smaller">
                <tr>
                    <td>Ort</td>
                    <td>
                        <xsl:if test="/Ereignis/Basisdaten/Ort/Ortsname[1]!='Nicht dokumentiert'">
                            <xsl:value-of select="/Ereignis/Basisdaten/Ort/Ortsname[1]"/>
                            <xsl:text> / </xsl:text>
                        </xsl:if>
                        <xsl:if test="/Ereignis/Basisdaten/Ort/Region[last()]!='Nicht dokumentiert'">
                            <xsl:value-of select="/Ereignis/Basisdaten/Ort/Region[last()]"/>
                            <xsl:text> / </xsl:text>
                        </xsl:if>
                        <xsl:if test="/Ereignis/Basisdaten/Ort/Land!='Nicht dokumentiert'">
                            <xsl:value-of select="/Ereignis/Basisdaten/Ort/Land"/>
                        </xsl:if>
                    </td>
                </tr>
                <tr>
                    <td>Datum</td>
                    <td><xsl:value-of select="//YYYY-MM-DD"/></td>
                </tr>
                <tr>
                    <td>Art</td>
                    <td><xsl:value-of select="//Sprechereignis[@Kennung=$speechEventID]/Basisdaten/Art"/></td>
                </tr>
                <tr>
                    <td>DGD-Kennung</td>
                    <td><xsl:value-of select="//Sprechereignis[@Kennung=$speechEventID]/@Kennung"/></td>
                </tr>
                <xsl:if test="//Interaktionsdomäne">
                    <tr>
                        <td>Interaktionsdomäne</td>
                        <td><xsl:value-of select="//Interaktionsdomäne"/></td>
                    </tr>
                    <tr>
                        <td>Lebensbereich</td>
                        <td><xsl:value-of select="//Lebensbereich"/></td>
                    </tr>
                </xsl:if>
            </table>
        </div>
    </xsl:template>
</xsl:stylesheet>