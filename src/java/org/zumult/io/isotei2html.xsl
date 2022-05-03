<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
        <html>
            <head>
                <style type="text/css">
                    td {
                        vertical-align:top;
                    }
                    td.token{}
                    td.norm {
                        font-size:8pt;
                        color:blue;
                    }
                    td.lemma {
                        font-size:8pt;
                        color:green;
                    }
                    td.pos{
                        font-size:8pt;
                        font-weight:bold;
                        co1or:gray;
                    }
                    td.time{
                        font-size:8pt;
                        co1or:gray;
                    }
                    td.speaker {
                        font-weight:bold;
                    }
                </style>
            </head>
            <body>
                <table id="transcript" class="transcript">
                    <xsl:apply-templates select="//tei:body/*"/>
                </table>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="tei:annotationBlock">
        <xsl:variable name="SPEAKER_ID" select="@who"/>
        <xsl:variable name="START_ID" select="@start"/>
        <xsl:variable name="END_ID" select="@end"/>
            
        <tr class="anotationBlock">
            <xsl:attribute name="id" select="@xml:id"/>
            <xsl:attribute name="data-start" select="//tei:when[@xml:id=$START_ID]/@interval"/>
            <xsl:attribute name="data-end" select="//tei:when[@xml:id=$END_ID]/@interval"/>
            <td class="time">
                <xsl:variable name="START-TIME" select="//tei:when[@xml:id=$START_ID]/@interval"/>
                <xsl:attribute name="onclick">jump(<xsl:value-of select="$START-TIME"/>)</xsl:attribute>
                <xsl:text>[</xsl:text>
                <xsl:value-of select="$START-TIME"/>
                <xsl:text>]&#x00A0;</xsl:text>
            </td>
            <td class="speaker">
                <xsl:value-of select="//tei:person[@xml:id=$SPEAKER_ID]/@n"/>
            </td>
            <td>
                <table class="trans_ann">
                    <xsl:apply-templates/>
                </table>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="tei:u">
          <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="tei:seg">
        <tr>
            <xsl:for-each select="child::*[not(self::tei:anchor)]">
                <td class="token">            
                    <xsl:attribute name="data-start" select="tei:getStartTimeForToken(current())"/>
                    <xsl:attribute name="data-end" select="tei:getEndTimeForToken(current())"/>
                    <xsl:value-of select="tei:getTokenText(current())"/>
                </td>                
            </xsl:for-each>
        </tr>        
        <tr>
            <xsl:for-each select="child::*[not(self::tei:anchor)]">
                <td class="norm">            
                    <xsl:choose>
                        <xsl:when test="@norm"><xsl:value-of select="@norm"/></xsl:when>
                        <xsl:otherwise>-</xsl:otherwise>
                    </xsl:choose>
                </td>                
            </xsl:for-each>
        </tr>        
        <tr>
            <xsl:for-each select="child::*[not(self::tei:anchor)]">
                <td class="lemma">            
                    <xsl:choose>
                        <xsl:when test="@lemma"><xsl:value-of select="@lemma"/></xsl:when>
                        <xsl:otherwise>-</xsl:otherwise>
                    </xsl:choose>
                </td>                
            </xsl:for-each>
        </tr>        
        <tr>
            <xsl:for-each select="child::*[not(self::tei:anchor)]">
                <td class="pos">            
                    <xsl:choose>
                        <xsl:when test="@pos"><xsl:value-of select="@pos"/></xsl:when>
                        <xsl:otherwise>-</xsl:otherwise>
                    </xsl:choose>
                </td>                
            </xsl:for-each>
        </tr>        
    </xsl:template>

    <xsl:template match="tei:w">
        <xsl:variable name="ID" select="@xml:id"/>
        <span class="token">            
            <xsl:attribute name="data-start" select="tei:getStartTimeForToken(current())"/>
            <xsl:attribute name="data-end" select="tei:getEndTimeForToken(current())"/>
            <!-- <xsl:attribute name="id" select="@xml:id"/> -->
            <xsl:if test="starts-with(@pos,'ADJ')">
                <!-- adjectives in bold and blue -->
                <xsl:attribute name="style">font-weight:bold; color:blue;</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="."/><xsl:text> </xsl:text>
        </span>
    </xsl:template>

    
    
    
    
    <xsl:template match="tei:pause[not(ancestor::tei:annotationBlock)]">
        <xsl:variable name="START_ID" select="@start"/>
        <tr class="anotationBlock">
            <td class="time">
                <xsl:text>[</xsl:text>
                <xsl:value-of select="//tei:when[@xml:id=$START_ID]/@interval"/>
                <xsl:text>]&#x00A0;</xsl:text>
            </td>
            <td class="speaker">&#x00A0;</td>
            <td>
                <span class="pause">
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
            </td>
        </tr>        
    </xsl:template>
    
    <xsl:template match="tei:pause[ancestor::tei:annotationBlock]">
        <td class="pause">
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
        </td>
    </xsl:template>
    
    
    
    <xsl:template match="tei:desc[not(ancestor::tei:annotationBlock)]">
        <xsl:variable name="START_ID" select="ancestor::*[@start]/@start"/>
        <tr class="anotationBlock">
            <td class="time">
                <xsl:text>[</xsl:text>
                <xsl:value-of select="//tei:when[@xml:id=$START_ID]/@interval"/>
                <xsl:text>]&#x00A0;</xsl:text>
            </td>
            <td class="speaker">&#x00A0;</td>
            <td>
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
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="tei:span/text()"/>

    <xsl:function name="tei:getStartTimeForToken">
        <xsl:param name="ELEMENT"/>
        <xsl:variable name="PRECEDING-ANCHOR-ID" select="$ELEMENT/preceding-sibling::tei:anchor[1]/@synch"/>
        <xsl:value-of select="root($ELEMENT)/descendant::tei:when[@xml:id=$PRECEDING-ANCHOR-ID]/@interval"/>
    </xsl:function>

    <xsl:function name="tei:getEndTimeForToken">
        <xsl:param name="ELEMENT"/>
        <xsl:variable name="FOLLOWING-ANCHOR-ID" select="$ELEMENT/following-sibling::tei:anchor[1]/@synch"/>
        <xsl:value-of select="root($ELEMENT)/descendant::tei:when[@xml:id=$FOLLOWING-ANCHOR-ID]/@interval"/>
    </xsl:function>
    
    <xsl:function name="tei:getTokenText">
        <xsl:param name="ELEMENT"/>
        <xsl:choose>
            <xsl:when test="$ELEMENT/self::tei:w">
                <xsl:value-of select="$ELEMENT"/><xsl:text> </xsl:text>                            
            </xsl:when>
            <xsl:when test="$ELEMENT/self::tei:pause">
                <xsl:choose>
                    <xsl:when test="$ELEMENT[@rend]">
                        <xsl:value-of select="$ELEMENT/@rend"/>
                        <xsl:text> </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$ELEMENT/@type='micro'">(.) </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>(</xsl:text><xsl:value-of select="substring-before(substring-after($ELEMENT/@dur, 'PT'), 'S')"/><xsl:text>) </xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
        
    </xsl:function>
    
    


</xsl:stylesheet>