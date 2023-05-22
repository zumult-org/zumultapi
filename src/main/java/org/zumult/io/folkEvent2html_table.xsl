<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
        <div class="container" style="margin-bottom: 10px; font-size:smaller">
            <xsl:value-of select="//Sprechereignis/Inhalt/Beschreibung"/>
        </div>            
        <div class="table">
            <table class="table-striped" style="font-size: smaller">
                <tr>
                    <td>Datum</td>
                    <td><xsl:value-of select="//YYYY-MM-DD"/></td>
                </tr>
                <tr>
                    <td>Art</td>
                    <td><xsl:value-of select="//Sprechereignis/Basisdaten/Art"/></td>
                </tr>
                <tr>
                    <td>DGD-Kennung</td>
                    <td><xsl:value-of select="//Sprechereignis/@Kennung"/></td>
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