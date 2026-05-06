<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"            
    exclude-result-prefixes="xs"
    version="2.0">
    
    <!-- start and end of the transcript ; empty for whole transcript -->
    <xsl:param name="START_ANNOTATION_BLOCK_ID"/>
    <xsl:param name="END_ANNOTATION_BLOCK_ID"/>
    
    
    <!-- <xsl:template match="/">
        <div>
            <xsl:apply-templates select="//tei:annotationBlock"/>
        </div>
    </xsl:template> -->
    
    <xsl:template match="/">
        <div class="annotations-div" style="overflow:auto;">
            <xsl:choose>                
                <xsl:when test="$START_ANNOTATION_BLOCK_ID and $END_ANNOTATION_BLOCK_ID">
                    <xsl:apply-templates select="//tei:annotationBlock[not(following-sibling::tei:*[@xml:id=$START_ANNOTATION_BLOCK_ID]) and not(preceding-sibling::tei:*[@xml:id=$END_ANNOTATION_BLOCK_ID])]"/>    
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="//tei:annotationBlock"/>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>
    
    
    <xsl:template match="tei:annotationBlock">
        <xsl:variable name="WHO" select="@who"/>
        <xsl:variable name="START_ID" select="@start"/>
        <xsl:variable name="START_TIME" select="//tei:when[@xml:id=$START_ID]/@interval"/>
        <xsl:variable name="END_ID" select="@end"/>
        <xsl:variable name="END_TIME" select="//tei:when[@xml:id=$END_ID]/@interval"/>
        <table>
            <tr>
                <td class="numbering" data-start="{$START_TIME}" data-end="{$END_TIME}"><xsl:value-of select="count(preceding-sibling::tei:annotationBlock) + 1"/></td>
                <!-- <td class="audioplay">
                    <xsl:variable name="START_ID" select="@start"/>
                    <xsl:variable name="START_TIME" select="//tei:when[@xml:id=$START_ID]/@interval"/>
                    <span>
                        <xsl:attribute name="onclick">playAudioFromAnnotation(<xsl:value-of select="$START_TIME"/>)</xsl:attribute>
                        <i class="fa-duotone fa-circle-play"></i>            
                    </span>
                </td> -->
                <td class="speaker"><xsl:value-of select="//tei:person[@xml:id=$WHO]/@n"/></td>
                <td>
                    <xsl:apply-templates select="descendant::tei:u">
                        <xsl:with-param name="START_TIME" select="$START_TIME"/>
                    </xsl:apply-templates>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <xsl:template match="tei:u">
        <xsl:param name="START_TIME"/>
        <table class="annotations">
            <xsl:if test="descendant::tei:spanGrp[@type='original']">
                <tr>
                    <th>Original</th>
                    <td class="original">
                        <xsl:attribute name="colspan" select="count(descendant::tei:w)"/>
                        <xsl:value-of select="ancestor::tei:annotationBlock/descendant::tei:spanGrp[@type='original']/tei:span/text()"/>
                    </td>
                </tr>
            </xsl:if>
            <xsl:if test="descendant::tei:spanGrp[@type='translation' or @type='en']">
                <tr>
                    <th>Translation</th>
                    <td class="translation">
                        <xsl:attribute name="colspan" select="count(descendant::tei:w)"/>
                        <xsl:value-of select="ancestor::tei:annotationBlock/descendant::tei:spanGrp[@type='translation' or @type='en']/tei:span/text()"/>
                    </td>
                </tr>
            </xsl:if>
            <tr>
                <th>ID</th>
                <xsl:apply-templates select="descendant::tei:w" mode="ID"/>
            </tr>
            <tr>
                <th>Token</th>
                <xsl:apply-templates select="descendant::tei:w" mode="TRANS">
                    <xsl:with-param name="START_TIME" select="$START_TIME"/>
                </xsl:apply-templates>                    
            </tr>
            <xsl:if test="descendant::tei:w[@xml_lang]">
                <tr>
                    <th>Language</th>
                    <xsl:apply-templates select="descendant::tei:w" mode="LANG"/>
                </tr>
            </xsl:if>
            <xsl:if test="descendant::tei:w[@norm]">
                <tr>
                    <th>Normalisation</th>
                    <xsl:apply-templates select="descendant::tei:w" mode="NORM"/>
                </tr>
            </xsl:if>
            <xsl:if test="descendant::tei:w[@lemma]">
                <tr>
                    <th>Lemma</th>
                    <xsl:apply-templates select="descendant::tei:w" mode="LEMMA"/>
                </tr>
                <tr>
                    <th>POS</th>
                    <xsl:apply-templates select="descendant::tei:w" mode="POS"/>
                </tr>
            </xsl:if>
            <xsl:if test="descendant::tei:w[@phon]">
                <tr>
                    <th>Phon</th>
                    <xsl:apply-templates select="descendant::tei:w" mode="PHON"/>
                </tr>
            </xsl:if>
        </table>
    </xsl:template>
    
    <!-- <tei:w norm="okay" xml:id="a19_w1" xml:lang="eng" pos="FM" lemma="okay" phon="@U . k ' eI">OKAY</tei:w>  -->
    
    <xsl:template match="tei:w" mode="ID">
        <xsl:variable name="START_ID" select="preceding::tei:anchor[1]/@synch"/>
        <xsl:variable name="START_TIME" select="//tei:when[@xml:id=$START_ID]/@interval"/>
        <xsl:variable name="END_ID" select="following::tei:anchor[1]/@synch"/>
        <xsl:variable name="END_TIME" select="//tei:when[@xml:id=$END_ID]/@interval"/>
        
        <td class="id">
            <xsl:attribute name="class">id <xsl:value-of select="@xml:lang"/></xsl:attribute>
            <xsl:attribute name="data-start" select="$START_TIME"/>
            <xsl:attribute name="data-end" select="$END_TIME"/>
            <xsl:value-of select="@xml:id"/>
        </td>
    </xsl:template>
    
    <xsl:template match="tei:w" mode="TRANS">
        <xsl:param name="START_TIME"/>
        <td class="trans">
            <xsl:attribute name="class">trans <xsl:value-of select="@xml:lang"/></xsl:attribute>
            <xsl:attribute name="data-start" select="$START_TIME"/>
            <xsl:value-of select="text()"/>
        </td>
    </xsl:template>
    
    <xsl:template match="tei:w" mode="LANG">
        <td class="lang">
            <xsl:attribute name="class">lang <xsl:value-of select="@xml:lang"/></xsl:attribute>
            <xsl:value-of select="@xml:lang"/>
        </td>
    </xsl:template>
    
    <xsl:template match="tei:w" mode="NORM">
        <td class="norm"><xsl:value-of select="@norm"/></td>
    </xsl:template>
    
    <xsl:template match="tei:w" mode="LEMMA">
        <td class="lemma"><xsl:value-of select="@lemma"/></td>
    </xsl:template>
    
    <xsl:template match="tei:w" mode="POS">
        <td class="pos"><xsl:value-of select="@pos"/></td>
    </xsl:template>
    
    <xsl:template match="tei:w" mode="PHON">
        <td class="phon">
            <xsl:text>[</xsl:text>
            <xsl:value-of select="translate(@phon, ' ', '')"/>
            <xsl:text>]</xsl:text>
        </td>
    </xsl:template>
    

</xsl:stylesheet>