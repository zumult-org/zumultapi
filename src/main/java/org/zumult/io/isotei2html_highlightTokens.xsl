<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    exclude-result-prefixes="xs"
    version="2.0">
    <!--<xsl:param name="TRANSCRIPT_ID"/>-->
    <xsl:param name="TOKEN_LIST_URL"/>
<!--    <xsl:param name="CORPUS_ID"/>
    <xsl:param name="SPEECH_EVENT_ID"/>
    <xsl:param name="AUDIO_ID"/>-->
    
    <xsl:variable name="TOKEN_LIST" select="document($TOKEN_LIST_URL)/*"/> 
    <!-- <xsl:variable name="TOKEN_LIST">
        <tokenList>
            <token form="begrüßen"/>
        </tokenList>
    </xsl:variable> -->
    
        
    
    <xsl:template match="/">
        <!--<xsl:variable name="SPEAKER_ID" select="@who"/>-->
        <div class="transcript">
            <xsl:apply-templates select="//tei:body/*"/>
        </div>
    </xsl:template>
    
    <xsl:template match="tei:annotationBlock">
        <xsl:variable name="SPEAKER_ID" select="@who"/>
        <div class="anotationBlock">
            <xsl:attribute name="id" select="@xml:id"/>
            <span class="speaker"><xsl:value-of select="//tei:person[@xml:id=$SPEAKER_ID]/@n"/></span>
            <xsl:text>: </xsl:text>
            <xsl:apply-templates select="tei:u/child::tei:*"/>
        </div>
    </xsl:template>
    
    <xsl:template match="tei:u"/>
    
    <xsl:template match="tei:w">
        <xsl:variable name="ID" select="@xml:id"/>
        <xsl:variable name="norm" select="@norm"/>
        <xsl:variable name="lemma" select="@lemma"/>
        <xsl:variable name="trans" select="."/>
        <xsl:variable name="startAnchor" select="preceding-sibling::tei:anchor[1]/@synch"/>        
        <xsl:variable name="endAnchor" select="following-sibling::tei:anchor[1]/@synch"/>        
        <xsl:variable name="start" select="//tei:when[@xml:id=$startAnchor]/@interval"/>
        <xsl:variable name="end" select="//tei:when[@xml:id=$endAnchor]/@interval"/>
        <span class="token" data-start="{$start}" data-end="{$end}" data-norm="{$norm}" data-lemma="{$lemma}">

            <xsl:choose>
                <xsl:when test="$TOKEN_LIST/descendant::token[@form=current()/@lemma]">
                    <xsl:attribute name="data-is-in-wordlist">true</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="data-is-in-wordlist">false</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="lower-case($norm) != lower-case($trans)">
                    <xsl:attribute name="data-is-trans">true</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="data-is-trans">false</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            
            
<!--            <xsl:when test="$TOKEN_LIST/descendant::token[@form=current()/@lemma] and not(lower-case($norm) != lower-case($trans))">
                    <xsl:attribute name="class">token wordlist-hit</xsl:attribute>
                </xsl:when>
                <xsl:when test="not($TOKEN_LIST/descendant::token[@form=current()/@lemma]) and lower-case($norm) != lower-case($trans)">
                    <xsl:attribute name="class">token trans</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">token</xsl:attribute>
                </xsl:otherwise>-->
<!--            <xsl:choose>
                <xsl:when test="$TOKEN_LIST/descendant::token[@form=current()/@lemma] and lower-case($norm) != lower-case($trans)">
                    <xsl:attribute name="class">token trans wordlist-hit</xsl:attribute>
                </xsl:when>
                <xsl:when test="$TOKEN_LIST/descendant::token[@form=current()/@lemma] and not(lower-case($norm) != lower-case($trans))">
                    <xsl:attribute name="class">token wordlist-hit</xsl:attribute>
                </xsl:when>
                <xsl:when test="not($TOKEN_LIST/descendant::token[@form=current()/@lemma]) and lower-case($norm) != lower-case($trans)">
                    <xsl:attribute name="class">token trans</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">token</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>-->
            
            <xsl:attribute name="title">
                <xsl:text>lemma: </xsl:text>                
                <xsl:value-of select="$lemma"></xsl:value-of>
                <xsl:text>&#10;</xsl:text>
                <xsl:text>normalisiert: </xsl:text>                
                <xsl:value-of select="$norm"></xsl:value-of> 
            </xsl:attribute>
            <xsl:value-of select="."/><xsl:text> </xsl:text>
        </span>
    </xsl:template>
    
    <xsl:template match="tei:pause">
        <span class="pause">
            <xsl:attribute name="id" select="@xml:id"/>
            <xsl:choose>
                <xsl:when test="@rend">
                    <xsl:value-of select="@rend"/>
                    <xsl:text> </xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="@type='micro'">(.) </xsl:when>
                        <xsl:otherwise><xsl:text>(</xsl:text><xsl:value-of select="substring-before(substring-after(@dur, 'PT'), 'S')"/><xsl:text>) </xsl:text></xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </span>
    </xsl:template>
    
    <xsl:template match="tei:desc">
        <span class="desc">
            <xsl:choose>
                <xsl:when test="@rend">
                    <xsl:value-of select="@rend"/>
                    <xsl:text> </xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>((</xsl:text>
                    <xsl:value-of select="."/>
                    <xsl:text>)) </xsl:text>                    
                </xsl:otherwise>
            </xsl:choose>
        </span>
    </xsl:template>

</xsl:stylesheet>