<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
        <IDList>
        <xsl:for-each select="IDList/token">
            <xsl:value-of select="@id"/>
            <xsl:if test="not(position()=last())"><xsl:text> </xsl:text></xsl:if>
        </xsl:for-each>
        </IDList>
    </xsl:template>
</xsl:stylesheet>