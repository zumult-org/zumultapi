<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    
    
    
    <!-- [{
    "art": "Unterrichtsstunde in der Berufsschule",
    "elternereignis_id": "FOLK_E_00001",
    "interaktionsdomäne": "Institutionell",
    "maße": {
    "wortschatz": {
    "HERDER_1000": {
    "lemmas_ratio": "0.42",
    "lemmas": "373",
    "tokens": "4930",
    "tokens_ratio": "0.77"
    },
    
    "aktivität": "Unterricht",
    "dauer": "00:59:45",
    "dauer_als_datum": "Mon Jan 01 00:59:45 CET 1900",
    "beschreibung": "Die Unterrichtsstunde findet in Rahmen des Fachs \"Ottomotor\" statt. In diesem Fach geht es darum, das Managementsystem von Ottomotoren zu analysieren und zu diagnostizieren.",
    "geo": {
        "dialektalregion_lameli": "mittelwest",
        "kreis": "Anonym",
        "dialektalregion_wiesinger": "Rheinfränkische Sprachregion",
        "land": "Deutschland",
        "ortsname": "Anonym"
    },
    "lebensbereich": ["Bildung"],
    "sprachen": ["Deutsch"],
    "id": "FOLK_E_00001_SE_01",
    "themen": ["Ottomotoren"]
    
    
    -->
    
    
    
    <xsl:template match="/">
        <xsl:text>[</xsl:text>
        <xsl:apply-templates select="//speech-event"/>
        <xsl:text>]</xsl:text>
    </xsl:template>
    
    <!-- Object or Element Property-->
    <xsl:template match="speech-event">
        <xsl:text>{</xsl:text>
        <xsl:text>"id": "</xsl:text><xsl:value-of select="@id"/><xsl:text>",</xsl:text>
        <xsl:text>"elternereignis_id": "</xsl:text><xsl:value-of select="substring(@id,1,12)"/><xsl:text>",</xsl:text>
        
        <xsl:text>"art": "</xsl:text><xsl:value-of select="key[@id='e_se_art']"/><xsl:text>",</xsl:text>
        
        <xsl:if test="starts-with(@id, 'GWSS')">
            <xsl:text>"sprachen": "</xsl:text><xsl:value-of select="key[@id='e_se_sprachen']"/><xsl:text>",</xsl:text>
        </xsl:if>

        <xsl:text>"gesamttokenzahl": "</xsl:text><xsl:value-of select="key[@id='measure_word_tokens']"/><xsl:text>",</xsl:text>
        <xsl:text>"beschreibung": "</xsl:text>
        <xsl:call-template name="replacement">
            <xsl:with-param name="string" select="key[@id='e_se_inhalt']"/>
        </xsl:call-template>
        <xsl:text>",</xsl:text>
        
        <xsl:if test="key[@id='e_se_interaktionsdomaene']">
            <xsl:text>"interaktionsdomäne": "</xsl:text><xsl:value-of select="key[@id='e_se_interaktionsdomaene']"/><xsl:text>",</xsl:text>
            <xsl:text>"aktivität": "</xsl:text><xsl:value-of select="key[@id='e_se_aktivitaet']"/><xsl:text>",</xsl:text>
            
            <!-- ["Interprofessionelle Kommunikation", "Dienstleistung"] -->
            <xsl:text>"lebensbereich": [</xsl:text>       
             <xsl:for-each select="tokenize(key[@id='e_se_lebensbereich'], ' ; ')">
                    <xsl:text>"</xsl:text>
                <xsl:value-of select="current()"/>
                <xsl:text>"</xsl:text>
                <xsl:if test="position()!=last()">
                    <xsl:text>, </xsl:text>
                </xsl:if>
            </xsl:for-each> 
            <xsl:text>],</xsl:text>
        </xsl:if>
        
        <xsl:text>"themen": [</xsl:text>       
        <xsl:for-each select="tokenize(key[@id='e_se_themen'], ' ; ')">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="normalize-space(translate(current(), '&#x0022;', ''''))"/>
            <!-- <xsl:value-of select="normalize-space(current())"/> -->
            <xsl:text>"</xsl:text>
            <xsl:if test="position()!=last()">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:for-each> 
        <xsl:text>],</xsl:text>
        
        <!-- 
        "geo": {
            "dialektalregion_lameli": "mittelwest",
            "kreis": "Anonym",
            "dialektalregion_wiesinger": "Rheinfränkische Sprachregion",
            "land": "Deutschland",
            "ortsname": "Anonym"
        },
        
        -->
        
        <xsl:text>"geo": { </xsl:text>
        
        <xsl:if test="key[@id='e_region_lameli']">
            <xsl:text>"dialektalregion_lameli": "</xsl:text>
            <xsl:value-of select="key[@id='e_region_lameli']"/>
            <xsl:text>", "dialektalregion_wiesinger": "</xsl:text>
            <xsl:value-of select="key[@id='e_region_wiesinger']"/>
            <xsl:text>", </xsl:text>
        </xsl:if>
        
        <xsl:text>"land": "</xsl:text>
        <xsl:value-of select="key[@id='e_land']"/>
        <xsl:text>"},</xsl:text>
        
        <!-- *********************** -->
        
        <xsl:text>"maße": {</xsl:text>
        
        <!-- "normalisierungsrate": "16,51",
            "artikulationsrate": "5,39", 
            "anteil_Überlappungen_mit_mehr_als_2_wörtern": "12,82",
            "durchschnittliche_anzahl_der_überlappungen": "1,59",
            "anzahl_überlappungen": "21,58",
            "anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens": "2,77"
              <key id="measure_overlap_perMilOverlaps">21.58</key>
              <key id="measure_overlap_averageNrOverlappingWords">1.59</key>
              <key id="measure_overlap_perCentOverlapsWithMoreThan2Words">12.82</key>
              <key id="measure_overlap_perMilTokensOverlapsWithMoreThan2Words">2.77</key>
            
        
        -->
        <xsl:text>"anzahl_sprecher": "</xsl:text><xsl:value-of select="key[@id='e_se_anzahl_s']"/><xsl:text>",</xsl:text>
        <xsl:text>"normalisierungsrate": "</xsl:text><xsl:value-of select="key[@id='measure_normalisation_rate']"/><xsl:text>",</xsl:text>
        <xsl:text>"artikulationsrate": "</xsl:text><xsl:value-of select="format-number(key[@id='measure_articulation_rate'],'#0.00')"/><xsl:text>",</xsl:text>
        <xsl:text>"anteil_Überlappungen_mit_mehr_als_2_wörtern": "</xsl:text><xsl:value-of select="key[@id='measure_overlap_perCentOverlapsWithMoreThan2Words']"/><xsl:text>",</xsl:text>
        <xsl:text>"durchschnittliche_anzahl_der_überlappungen": "</xsl:text><xsl:value-of select="key[@id='measure_overlap_averageNrOverlappingWords']"/><xsl:text>",</xsl:text>
        <xsl:text>"anzahl_überlappungen": "</xsl:text><xsl:value-of select="key[@id='measure_overlap_perMilOverlaps']"/><xsl:text>",</xsl:text>
        <xsl:text>"anteil_Überlappungen_mit_mehr_als_2_wörtern_tokens": "</xsl:text><xsl:value-of select="key[@id='measure_overlap_perMilTokensOverlapsWithMoreThan2Words']"/><xsl:text>",</xsl:text>
        
        <!-- TO DO -->
        <xsl:text>"durchschnittliche_beitragslänge": "</xsl:text>1<xsl:text>",</xsl:text>
        
        <!-- "dauer": "0:59:45",  -->
        <xsl:text>"dauer": "</xsl:text>
        <!-- <xsl:value-of select="key[@id='e_se_duration']"/> -->
        <xsl:call-template name="format-duration">
            <xsl:with-param name="value" select="key[@id='e_se_duration']"/>
        </xsl:call-template>
        <xsl:text>",</xsl:text>
        
        
        <xsl:text>"wortschatz": {</xsl:text>
        
        <xsl:for-each select="key[starts-with(@id, 'measure_intersection')]">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="substring-after(@id, 'intersection_')"/>
            <xsl:text>" : { "tokens_ratio": "</xsl:text>
            <xsl:value-of select="current()"/>
            <xsl:text>"}</xsl:text>
            <xsl:if test="position()!=last()">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:for-each>
        
        <xsl:text>}</xsl:text>
        <xsl:text>,</xsl:text>
        
        <xsl:text>"wortarten": {</xsl:text>
        
        <xsl:for-each select="key[starts-with(@id, 'measure_pos')]">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="substring-after(@id, 'pos_')"/>
            <xsl:text>" : { "token_ratio": "</xsl:text>
            <xsl:value-of select="current()"/>
            <xsl:text>"}</xsl:text>
            <xsl:if test="position()!=last()">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:for-each>
        
        <xsl:text>}</xsl:text>
        
        <xsl:text>,</xsl:text>
        
        <xsl:text>"mündlichkeitsphänomene": {</xsl:text>
        
        <xsl:for-each select="key[starts-with(@id, 'measure_oral_phenomina')]">
            <xsl:text>"</xsl:text>
            <xsl:value-of select="substring-after(@id, 'oral_phenomina_')"/>
            <xsl:text>" : { "token_ratio": "</xsl:text>
            <xsl:value-of select="current()"/>
            <xsl:text>"}</xsl:text>
            <xsl:if test="position()!=last()">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:for-each>
        
        <xsl:text>}</xsl:text>
        
        <xsl:text>}</xsl:text>
        
        
        <xsl:text>}</xsl:text>
        <xsl:if test="following-sibling::speech-event">
            <xsl:text>,</xsl:text>
        </xsl:if>
    </xsl:template>
    
    <!-- Array Element -->
    <xsl:template match="*" mode="ArrayElement">
        <xsl:call-template name="Properties"/>
    </xsl:template>
    
    <!-- Object Properties -->
    <xsl:template name="Properties">
        <xsl:param name="parent"></xsl:param>
        <xsl:variable name="childName" select="name(*[1])"/>
        <xsl:choose>            
            <xsl:when test="not(*|@*)"><xsl:choose><xsl:when test="$parent='Yes'"> <xsl:text>&quot;</xsl:text><xsl:value-of select="."/><xsl:text>&quot;</xsl:text></xsl:when>
                <xsl:otherwise>"<xsl:value-of select="name()"/>":"<xsl:value-of  select="."/>"</xsl:otherwise>
            </xsl:choose>           
            </xsl:when>                
            <xsl:when test="count(*[name()=$childName]) > 1">{ "<xsl:value-of  select="$childName"/>" :[<xsl:apply-templates select="*" mode="ArrayElement"/>] }</xsl:when>
            <xsl:otherwise>{
                <xsl:apply-templates select="@*"/>
                <xsl:apply-templates select="*"/>
                }</xsl:otherwise>
        </xsl:choose>
        <xsl:if test="following-sibling::*">,</xsl:if>
    </xsl:template>
    
    <!-- Attribute Property -->
    <xsl:template match="@*">"<xsl:value-of select="name()"/>" : "<xsl:value-of select="."/>",
    </xsl:template>
    
    <xsl:template name="format-duration">
        <xsl:param name="value" />
        
        <xsl:variable name="minutes" select="floor($value div 60) mod 60" />
        <xsl:variable name="seconds" select="$value mod 60" />
        <xsl:variable name="hours" select="floor($value div 3600)" />
        
        
        <!-- <xsl:if test="$hours &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if> -->
        <xsl:value-of select="$hours" />
        
        <xsl:text>:</xsl:text>
        
        
        <xsl:if test="$minutes &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$minutes" />
        <xsl:text></xsl:text>
        
        <xsl:text>:</xsl:text>
        
        <xsl:if test="$seconds &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <!-- changed for issue #39 -->
        <xsl:value-of select="floor($seconds)" />
        <xsl:text></xsl:text>
    </xsl:template>   
    
    <xsl:template name="replacement">
        <xsl:param name="string"/>
        <xsl:variable name="quote">'</xsl:variable>
        <xsl:value-of select="replace(replace(replace(replace($string, '&quot;', $quote), '„', $quote), '“', $quote), '\s+', ' ')"/>
    </xsl:template> 
    
</xsl:stylesheet>