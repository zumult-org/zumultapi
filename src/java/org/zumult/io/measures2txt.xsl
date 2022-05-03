<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" encoding="UTF-8"/>
    <!-- 
        <measures speechEventID="FOLK_E_00001_SE_01" types="879" tokens="6448">
            <measure type="intersection" reference="GOETHE_A1" types="250" tokens="4681"/>
            <measure type="intersection" reference="GOETHE_A2" types="351" tokens="5028"/>
            <measure type="intersection" reference="GOETHE_B1" types="504" tokens="5445"/>
            <measure type="intersection" reference="HERDER_1000" types="372" tokens="4930"/>
            <measure type="intersection" reference="HERDER_2000" types="472" tokens="5229"/>
            <measure type="intersection" reference="HERDER_3000" types="520" tokens="5327"/>
            <measure type="intersection" reference="HERDER_4000" types="547" tokens="5378"/>
            <measure type="intersection" reference="HERDER_5000" types="558" tokens="5413"/>
        </measures>        
    -->
    <xsl:template match="/">
        <xsl:text>Speech Event&#9;lemmas&#9;tokens&#9;GOETHE_A1 lemmas&#9;GOETHE_A1 tokens&#9;GOETHE_A1 lemmas ratio&#9;GOETHE_A1 tokens ratio&#9;GOETHE_A2 lemmas&#9;GOETHE_A2 tokens&#9;GOETHE_A2 lemmas ratio&#9;GOETHE_A2 tokens ratio&#9;GOETHE_B1 lemmas&#9;GOETHE_B1 tokens&#9;GOETHE_B1 lemmas ratio&#9;GOETHE_B1 tokens ratio&#9;HERDER_1000 lemmas&#9;HERDER_1000 tokens&#9;HERDER_1000 lemmas ratio&#9;HERDER_1000 tokens ratio&#9;HERDER_2000 lemmas&#9;HERDER_2000 tokens&#9;HERDER_2000 lemmas ratio&#9;HERDER_2000 tokens ratio&#9;HERDER_3000 lemmas&#9;HERDER_3000 tokens&#9;HERDER_3000 lemmas ratio&#9;HERDER_3000 tokens ratio&#9;HERDER_4000 lemmas&#9;HERDER_4000 tokens&#9;HERDER_4000 lemmas ratio&#9;HERDER_4000 tokens ratio&#9;HERDER_5000 lemmas&#9;HERDER_5000 tokens&#9;HERDER_5000 lemmas ratio&#9;HERDER_5000 tokens ratio&#9;&#10;</xsl:text>
        <xsl:apply-templates select="//measures"/>
    </xsl:template>
    
    <xsl:template match="measures">
        <xsl:value-of select="@speechEventID"/>
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@lemmas"/>        
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@tokens"/>        
        <xsl:for-each select="measure">
            <xsl:text>&#9;</xsl:text>
            <xsl:value-of select="@lemmas"/>
            <xsl:text>&#9;</xsl:text>
            <xsl:value-of select="@tokens"/>
            <xsl:text>&#9;</xsl:text>
            <xsl:value-of select="@lemmas_ratio"/>
            <xsl:text>&#9;</xsl:text>
            <xsl:value-of select="@tokens_ratio"/>
        </xsl:for-each>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
