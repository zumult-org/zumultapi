<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0" 
    exclude-result-prefixes="xs"
    xmlns:f="urn:my-functions"
    version="2.0">

    <!-- start and end of the transcript ; empty for whole transcript -->
    <xsl:param name="START_ANNOTATION_BLOCK_ID"/>
    <xsl:param name="END_ANNOTATION_BLOCK_ID"/>

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
        <table class="annotations-table">
            <tr>
                <th class="speaker">
                    <xsl:value-of select="@who"/>
                </th>
                <td>
                    <xsl:apply-templates select="descendant::tei:seg[not(parent::tei:seg)]"/>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <xsl:template match="tei:u/tei:seg">
        <xsl:variable name="ID" select="@xml:id"/>        
        <table class="annotations">
            
            <tr class="ids">
                <th class="anno-label">id</th>
                <xsl:apply-templates select="child::*">
                    <xsl:with-param name="LEVEL">xml:id</xsl:with-param>                    
                </xsl:apply-templates>               
            </tr>
                
            
            <tr class="trans">
                <th class="anno-label">trans</th>
                <xsl:apply-templates select="child::*">
                    <xsl:with-param name="LEVEL">trans</xsl:with-param>                    
                </xsl:apply-templates>
            </tr>
            
            <xsl:variable name="CURRENT_SEG" select="." />
            
            <xsl:for-each select="('norm', 'lemma', 'pos', 'phon', 'type')">
                <xsl:variable name="CURRENT_LEVEL" select="current()"/>
                <xsl:if test="$CURRENT_SEG/descendant::tei:w[@*/name()=$CURRENT_LEVEL]">
                    <tr>
                        <xsl:attribute name="class">token-anno <xsl:value-of select="$CURRENT_LEVEL"/></xsl:attribute>
                        <th class="anno-label"><xsl:value-of select="$CURRENT_LEVEL"/></th>
                        <xsl:apply-templates select="$CURRENT_SEG/child::*">
                            <xsl:with-param name="LEVEL"><xsl:value-of select="$CURRENT_LEVEL"/></xsl:with-param>
                        </xsl:apply-templates>
                    </tr>
                </xsl:if>
            </xsl:for-each>       
            
            <!-- N.B.: This is actually wrong or at least risky : it rests on the assumption that there is only one <seg> per <u> -->
            <xsl:apply-templates select="ancestor-or-self::tei:annotationBlock/child::tei:spanGrp"/>                
            
        </table>
    </xsl:template>
    
    <xsl:template match="tei:w">
        <xsl:param name="LEVEL"/>
        <td>
            <xsl:choose>
                <xsl:when test="$LEVEL='trans'"><xsl:apply-templates/></xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$LEVEL='norm' and text()!=@norm"><xsl:attribute name="style">font-weight:bold;</xsl:attribute></xsl:when>
                        <xsl:when test="$LEVEL='lemma' and @lemma!=@norm"><xsl:attribute name="style">font-weight:bold;</xsl:attribute></xsl:when>
                    </xsl:choose>
                    <xsl:value-of select="@*[name()=$LEVEL]"/>
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template>
    
    <xsl:template match="tei:pc">
        <xsl:param name="LEVEL"></xsl:param>
        <td>
            <xsl:choose>
                <xsl:when test="$LEVEL='trans'"><xsl:apply-templates/></xsl:when>
                <xsl:otherwise><xsl:value-of select="@*[name()=$LEVEL]"/></xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template>
    
    <xsl:template match="tei:seg/tei:anchor">
<!--        <td>*</td>-->
    </xsl:template>
    
    <xsl:template match="tei:seg/tei:pause">
        <xsl:param name="LEVEL"/>
        <td class="nv">
            <xsl:choose>
                <xsl:when test="$LEVEL='trans'">
                    <xsl:choose>
                        <xsl:when test="@rend">
                            <xsl:value-of select="@rend"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="@type='micro'">(.) </xsl:when>
                                <xsl:when test="@type='short'">(-) </xsl:when>
                                <xsl:when test="@type='medium'">(--) </xsl:when>
                                <xsl:when test="@type='long'">(---) </xsl:when>
                                <xsl:otherwise><xsl:text>(</xsl:text><xsl:value-of select="substring-before(substring-after(@dur, 'PT'), 'S')"/><xsl:text>)</xsl:text></xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>                        
                </xsl:when>
                <xsl:otherwise> </xsl:otherwise>
            </xsl:choose>
        </td>            
    </xsl:template>
    
    <xsl:template match="tei:seg/tei:incident | tei:seg/tei:vocal">
        <xsl:param name="LEVEL"/>
        <td class="nv">
            <xsl:choose>
                <xsl:when test="$LEVEL='trans'">
                    <xsl:choose>
                        <xsl:when test="tei:desc/@rend">
                            <xsl:value-of select="tei:desc/@rend"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>((</xsl:text>
                            <xsl:value-of select="tei:desc"/>
                            <xsl:text>))</xsl:text>                    
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise> </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template>
    
    <xsl:template match="tei:spanGrp">
        <tr>
            <th class="anno-label">
                <xsl:value-of select="@type"/>
            </th>
            <xsl:variable name="THIS_SPAN_GRP" select="."/>
            <xsl:apply-templates 
                select="ancestor::tei:annotationBlock/descendant::tei:seg[1]/descendant::*[not(self::tei:seg or self::tei:anchor)][1]" 
                mode="START_FOR_SPAN_GRP">
                <xsl:with-param name="SPAN_GRP" select="$THIS_SPAN_GRP"/>
            </xsl:apply-templates>            
        </tr>
    </xsl:template>
    
    <!-- I think this is for <w> -->
    <xsl:template match="*" mode="START_FOR_SPAN_GRP">
        <xsl:param name="SPAN_GRP" as="node()"/>
        <xsl:variable name="ID" select="@xml:id"/>
        <xsl:variable name="SYNCH" select="preceding-sibling::*[1]/@synch"/>
<!--        <xsl:message select="$SPAN_GRP"/>-->
        <xsl:choose>
            <xsl:when test="$SPAN_GRP/descendant::tei:span[@from=$ID]">
                <!-- we are dealing with a span that refers to token ids -->
                <xsl:variable name="THE_SPAN" select="$SPAN_GRP/descendant::tei:span[@from=$ID]"/>
                <xsl:variable name="SPAN_FROM" select="$THE_SPAN/@from"/>
                <xsl:variable name="SPAN_TO" select="$THE_SPAN/@to"/>
                <xsl:variable name="FROM_POSITION" select="count(preceding::*[not(self::tei:anchor or self::tei:seg)])"/>
                <xsl:variable name="TO_POSITION" select="count(following::*[@xml:id=$SPAN_TO]/preceding::*[not(self::tei:anchor or self::tei:seg)])"/>
                <xsl:variable name="LENGTH" select="$TO_POSITION - $FROM_POSITION + 1"/>
                <td class="anno-span">
                    <xsl:attribute name="colspan">
                        <xsl:value-of select="$LENGTH"/>
                    </xsl:attribute>
                    <xsl:attribute name="title" select="concat($SPAN_GRP/@type, ' : ', $THE_SPAN/text())"/>
                    <xsl:value-of select="$THE_SPAN/text()"/>
                    <xsl:if test="$THE_SPAN/@target">
                        <br/><xsl:value-of select="$THE_SPAN/@target"/>
                    </xsl:if>
                </td>
                <xsl:choose>
                    <xsl:when test="$SPAN_FROM!=$SPAN_TO">
                        <xsl:if test="following::*[@xml:id=$SPAN_TO]/following::*[not(self::tei:anchor or self::tei:seg)]">
                            <xsl:apply-templates select="following::*[@xml:id=$SPAN_TO]/following::*[not(self::tei:anchor or self::tei:seg)][1]" mode="START_FOR_SPAN_GRP">
                                <xsl:with-param name="SPAN_GRP" select="$SPAN_GRP"/>
                            </xsl:apply-templates>
                        </xsl:if>                        
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="NEXT" select="f:next-token-in-u(.)"/>
                        <xsl:if test="$NEXT">
                            <xsl:apply-templates select="$NEXT" mode="START_FOR_SPAN_GRP">
                                <xsl:with-param name="SPAN_GRP" select="$SPAN_GRP"/>
                            </xsl:apply-templates>
                        </xsl:if>         
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            
            <xsl:when test="$SPAN_GRP/descendant::tei:span[@from=$SYNCH]">
                <!-- we are dealing with a span that refers to the synch of anchors -->
                <xsl:variable name="THE_SPAN" select="$SPAN_GRP/descendant::tei:span[@from=$SYNCH]"/>
                <xsl:variable name="SPAN_FROM" select="$THE_SPAN/@from"/>
                <xsl:variable name="SPAN_TO" select="$THE_SPAN/@to"/>
                <xsl:variable name="FROM_POSITION" select="count(preceding::*[not(self::tei:anchor or self::tei:seg)])"/>
                <xsl:variable name="TO_POSITION" select="count(following::*[@synch=$SPAN_TO]/preceding::*[not(self::tei:anchor or self::tei:seg)])"/>
                <xsl:variable name="LENGTH" select="$TO_POSITION - $FROM_POSITION"/>
                <td class="anno-span">
                    <xsl:attribute name="colspan">
                        <xsl:value-of select="$LENGTH"/>
                    </xsl:attribute>
                    <xsl:attribute name="title" select="concat($SPAN_GRP/@type, ' : ', $THE_SPAN/text())"/>
                    <xsl:value-of select="$THE_SPAN/text()"/>
                </td>                
                <xsl:variable name="TARGET" select="following::*[@synch=$SPAN_TO][ancestor::tei:u = current()/ancestor::tei:u][1]"/>
                <xsl:variable name="NEXT" select="f:next-token-in-u($TARGET)"/>
                <xsl:if test="$NEXT">
                    <xsl:apply-templates select="$NEXT" mode="START_FOR_SPAN_GRP">
                      <xsl:with-param name="SPAN_GRP" select="$SPAN_GRP"/>
                    </xsl:apply-templates>
                  </xsl:if>
            </xsl:when>
            
            <xsl:otherwise>
                <td> </td> 
                <xsl:variable name="NEXT" select="f:next-token-in-u(.)"/>
                <xsl:if test="$NEXT">
                    <xsl:apply-templates select="$NEXT" mode="START_FOR_SPAN_GRP">
                        <xsl:with-param name="SPAN_GRP" select="$SPAN_GRP"/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="f:next-token-in-u">
      <xsl:param name="node" as="element()"/>
      <xsl:variable name="U" select="$node/ancestor::tei:u[1]"/>
      <xsl:sequence select="$node/following::*[ancestor::tei:u[1] is $U][not(self::tei:anchor or self::tei:seg)][1]"/>
    </xsl:function>
    
</xsl:stylesheet>