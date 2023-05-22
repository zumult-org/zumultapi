<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xs"
    version="2.0">

    <!-- start and end of the transcript ; empty for whole transcript -->
    <xsl:param name="START_ANNOTATION_BLOCK_ID"/>
    <xsl:param name="END_ANNOTATION_BLOCK_ID"/>
    
    <xsl:param name="AROUND_ANNOTATION_BLOCK_ID"/>
    <xsl:param name="HOW_MUCH_AROUND"/>
    
    <xsl:param name="SIZE">small</xsl:param>

    <xsl:variable name="START">
        <xsl:choose>
            <xsl:when test="$START_ANNOTATION_BLOCK_ID and $END_ANNOTATION_BLOCK_ID">
                <xsl:value-of select="//tei:body/*[@xml:id=$START_ANNOTATION_BLOCK_ID]/@start"/>                                    
            </xsl:when>
            <xsl:when test="$AROUND_ANNOTATION_BLOCK_ID">
                <xsl:variable name="AROUND_POSITION" select="count(//tei:body/*[@xml:id=$AROUND_ANNOTATION_BLOCK_ID]/preceding-sibling::*) + 1"/>
                <xsl:variable name="AROUND_START" select="//tei:body/*[max((1, $AROUND_POSITION - $HOW_MUCH_AROUND))]/@xml:id"/>
                <xsl:value-of select="//tei:body/*[@xml:id=$AROUND_START]/@start"/>                                                                        
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="//tei:body/*[@start][1]/@start"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable> 
    <xsl:variable name="END">
        <xsl:choose>
            <xsl:when test="$START_ANNOTATION_BLOCK_ID and $END_ANNOTATION_BLOCK_ID">
                <xsl:value-of select="//tei:body/*[@xml:id=$END_ANNOTATION_BLOCK_ID]/@start"/>                                    
            </xsl:when>
            <xsl:when test="$AROUND_ANNOTATION_BLOCK_ID">
                <xsl:variable name="AROUND_POSITION" select="count(//tei:body/*[@xml:id=$AROUND_ANNOTATION_BLOCK_ID]/preceding-sibling::*) + 1"/>
                <xsl:variable name="AROUND_END" select="//tei:body/*[min((count(//tei:body/*), $AROUND_POSITION + $HOW_MUCH_AROUND))]/@xml:id"/>                    
                <xsl:value-of select="//tei:body/*[@xml:id=$AROUND_END]/@start"/>                                                                        
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="//tei:body/*[@end][last()]/@end"/>
            </xsl:otherwise>
        </xsl:choose>                            
    </xsl:variable> 
    
    <xsl:variable name="START_TIME" select="//tei:when[@xml:id=$START]/@interval"/> <!-- 0 / 1800 -->
    <xsl:variable name="END_TIME" select="//tei:when[@xml:id=$END]/@interval"/> <!-- 1800/ 3060  -->
    

    <xsl:variable name="ZERO" select="$START"/>
    <xsl:variable name="ZERO_TIME" select="//tei:when[@xml:id=$ZERO]/@interval"/>
    
    <xsl:variable name="FONT_SIZE_TICKMARKS">
        <xsl:choose>
            <xsl:when test="$SIZE='small'">8pt</xsl:when>
            <xsl:when test="$SIZE='medium'">12pt</xsl:when>
            <xsl:when test="$SIZE='large'">16pt</xsl:when>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="FONT_SIZE_SPEECHRATE">
        <xsl:choose>
            <xsl:when test="$SIZE='small'">6pt</xsl:when>
            <xsl:when test="$SIZE='medium'">9pt</xsl:when>
            <xsl:when test="$SIZE='large'">14pt</xsl:when>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="LINE_HEIGHT" as="xs:integer">
        <xsl:choose>
            <xsl:when test="$SIZE='small'">10</xsl:when>
            <xsl:when test="$SIZE='medium'">25</xsl:when>
            <xsl:when test="$SIZE='large'">50</xsl:when>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="OFFSET" as="xs:integer">
        <xsl:choose>
            <xsl:when test="$SIZE='small'">25</xsl:when>
            <xsl:when test="$SIZE='medium'">30</xsl:when>
            <xsl:when test="$SIZE='large'">40</xsl:when>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="BUFFER" as="xs:integer">
        <xsl:choose>
            <xsl:when test="$SIZE='small'">1</xsl:when>
            <xsl:when test="$SIZE='medium'">2</xsl:when>
            <xsl:when test="$SIZE='large'">4</xsl:when>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="ROUNDING" as="xs:integer">
        <xsl:choose>
            <xsl:when test="$SIZE='small'">1</xsl:when>
            <xsl:when test="$SIZE='medium'">3</xsl:when>
            <xsl:when test="$SIZE='large'">5</xsl:when>
        </xsl:choose>
    </xsl:variable>

    

    <xsl:template match="/">
        <!-- <html>
            <head>
                <style type="text/css">
                    .color-1 {fill: blue; stroke: blue;}
                    .color-2 {fill: red; stroke: red;}
                    .color-3 {fill: green;}
                    .color-4 {fill: yellow;}
                    .color-5 {fill: purple;}
                    .color-6 {fill: cyan;}
                    .color-7 {fill: orange;}
                    .pause {fill: lightGray; fill-opacity: 0.1; stroke-dasharray: 1 4; stroke: black;}
                </style>
            </head>
            <body>
                <div id="svg" style="width:100%; overflow: auto;"> -->
                    <svg xmlns="http://www.w3.org/2000/svg">
                        
                        <xsl:variable name="WIDTH" select="10 * ($END_TIME - $START_TIME) + 20" />
                        <xsl:variable name="HEIGHT" select="count(//tei:person)*$LINE_HEIGHT + $OFFSET"/>
                        
                        <xsl:attribute name="width"><xsl:value-of select="$WIDTH"/>px</xsl:attribute>
                        <xsl:attribute name="height"><xsl:value-of select="$HEIGHT"/>px</xsl:attribute>
                        <!-- <xsl:for-each select="xs:integer(floor($START_TIME div 10)) to xs:integer(ceiling($END_TIME div 10))"> -->
                        
                        <!-- tickmarks for the timeline -->
                        <xsl:variable name="TEXT_Y" select="xs:integer(substring-before($FONT_SIZE_TICKMARKS, 'pt')) + 2"/>
                        <xsl:variable name="HEIGHT_TICKMARK" select="$LINE_HEIGHT div 2"/>
                        <xsl:for-each select="0 to xs:integer(ceiling($END_TIME - $START_TIME) div 10)"> <!-- 0 to 180 / 0 to 120 -->
                            <line style="stroke:rgb(200,200,200);stroke-width:1">
                                <xsl:attribute name="x1" select="current() * 100"/>
                                <xsl:attribute name="x2" select="current() * 100"/>
                                <xsl:attribute name="y1" select="0"/>
                                <xsl:attribute name="y2" select="5"/>                                
                            </line>
                            <text style="font-size:{$FONT_SIZE_TICKMARKS}; fill: gray;">
                                <xsl:attribute name="x" select="current() * 100 + 2"/>
                                <xsl:attribute name="y" select="$TEXT_Y"/>        
                                <xsl:call-template name="format-duration">
                                    <xsl:with-param name="value" select="round(current() * 10 + $START_TIME)"/> 
                                </xsl:call-template>
                            </text>                            
                        </xsl:for-each>
                        
                        <!-- the crosshair cursor -->
                        <line id="svg_cursor" x1="0" x2="0" y1="0" y2="{$WIDTH}" style="stroke:rgb(180,200,200);stroke-width:1"></line>
                        
                        <!-- <xsl:apply-templates select="//tei:body/*"/> -->
                        <xsl:choose>                
                            <xsl:when test="$START_ANNOTATION_BLOCK_ID and $END_ANNOTATION_BLOCK_ID">
                                <xsl:apply-templates select="//tei:body/*[not(following-sibling::tei:*[@xml:id=$START_ANNOTATION_BLOCK_ID]) and not(preceding-sibling::tei:*[@xml:id=$END_ANNOTATION_BLOCK_ID])]"></xsl:apply-templates>    
                            </xsl:when>
                            <xsl:when test="$AROUND_ANNOTATION_BLOCK_ID and $HOW_MUCH_AROUND">
                                <xsl:variable name="AROUND_POSITION" select="count(//tei:body/*[@xml:id=$AROUND_ANNOTATION_BLOCK_ID]/preceding-sibling::*) + 1"/>
                                <xsl:variable name="AROUND_START" select="//tei:body/*[max((1, $AROUND_POSITION - $HOW_MUCH_AROUND))]/@xml:id"/>
                                <xsl:variable name="AROUND_END" select="//tei:body/*[min((count(//tei:body/*), $AROUND_POSITION + $HOW_MUCH_AROUND))]/@xml:id"/>                    
                                <xsl:apply-templates select="//tei:body/*[not(following-sibling::tei:*[@xml:id=$AROUND_START]) and not(preceding-sibling::tei:*[@xml:id=$AROUND_END])]"></xsl:apply-templates>    
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="//tei:body/*"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        
                    </svg>
              <!-- </div>
                
            </body>
        </html> -->
        
    </xsl:template>
    
    <xsl:template match="tei:body/*[@who]">
        <xsl:variable name="THIS_START" select="@start"/>
        <xsl:variable name="THIS_END" select="@end"/>
        <!-- <xsl:variable name="ZERO" select="//tei:body/*[@start][1]/@start"/> -->
        
        <xsl:variable name="WHO" select="@who"/>
        <xsl:variable name="WHO_POSITION" select="count(//tei:person[@xml:id=$WHO]/preceding-sibling::tei:person)"/>
        <!-- <xsl:variable name="ZERO_TIME" select="//tei:when[@xml:id=$ZERO]/@interval"/> -->
        <xsl:variable name="THIS_START_TIME" select="//tei:when[@xml:id=$THIS_START]/@interval"/>
        <xsl:variable name="THIS_END_TIME" select="//tei:when[@xml:id=$THIS_END]/@interval"/>
        
        <!-- individual entry for an annotation block -->
        <rect xmlns="http://www.w3.org/2000/svg" rx="{$ROUNDING}" ry="{$ROUNDING}">
            <xsl:attribute name="style">fill-opacity:
                <xsl:variable name="COUNT_WORDS" select="count(descendant::tei:w)"/>
                <xsl:choose>
                    <xsl:when test="$COUNT_WORDS=0">1.0</xsl:when>
                    <xsl:otherwise><xsl:value-of select="1 - count(descendant::tei:w[lower-case(.)!=lower-case(@norm)]) div count(descendant::tei:w)"/></xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="x" select="($THIS_START_TIME - $ZERO_TIME) * 10"/>
            <xsl:attribute name="y" select="$OFFSET + $LINE_HEIGHT * $WHO_POSITION"/>
            <xsl:attribute name="width" select="($THIS_END_TIME - $THIS_START_TIME) * 10"/>
            <xsl:attribute name="height"><xsl:value-of select="$LINE_HEIGHT - $BUFFER"/></xsl:attribute>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="$WHO">
                        <xsl:text>color-</xsl:text>
                        <xsl:value-of select="count(//tei:person[@xml:id=$WHO]/preceding-sibling::tei:person) + 1"/>                        
                    </xsl:when>
                    <xsl:otherwise>pause</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="onclick">
                <xsl:text>makeVisible('</xsl:text>
                <xsl:value-of select="@xml:id"/>
                <xsl:text>')</xsl:text>
            </xsl:attribute>
            <title>
                <xsl:value-of select="$WHO"/>
                <xsl:text>: </xsl:text>
                <xsl:apply-templates select="descendant::tei:w"/>
            </title>
        </rect>
        
        <!-- visualization of the speech rate -->
        <xsl:if test="descendant::tei:spanGrp[@type='speech-rate']">
            <xsl:variable name="SPEECH-RATE" select="descendant::tei:spanGrp[@type='speech-rate']/tei:span/text()"/>
            <xsl:if test="($THIS_END_TIME - $THIS_START_TIME) &gt; 1.5">
                <text style="font-size:{$FONT_SIZE_SPEECHRATE}; fill: white;">
                    <xsl:attribute name="x" select="($THIS_START_TIME - $ZERO_TIME) * 10 + 2"/>
                    <xsl:attribute name="y" select="$OFFSET + $LINE_HEIGHT * $WHO_POSITION + 0.8 * $LINE_HEIGHT"/>
                    <xsl:attribute name="onclick">
                        <xsl:text>makeVisible('</xsl:text>
                        <xsl:value-of select="@xml:id"/>
                        <xsl:text>')</xsl:text>
                    </xsl:attribute>
                    <xsl:variable name="HOW_MANY">
                        <xsl:choose>
                            <xsl:when test="$SPEECH-RATE &lt; 3.5">1</xsl:when>
                            <xsl:when test="$SPEECH-RATE &gt;= 3.5 and $SPEECH-RATE &lt; 5.5">2</xsl:when>
                            <xsl:when test="$SPEECH-RATE &gt;= 5.5 and $SPEECH-RATE &lt; 7.0">3</xsl:when>
                            <xsl:otherwise>4</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:for-each select="1 to $HOW_MANY">
                        <xsl:text>&#x276D;</xsl:text>
                    </xsl:for-each>                
                </text>            
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="tei:w">
        <xsl:apply-templates/><xsl:text> </xsl:text>
        <!-- <xsl:text> [</xsl:text><xsl:value-of select="@norm"/><xsl:text>] </xsl:text> -->
    </xsl:template>
    
    <xsl:template name="format-duration">
        <xsl:param name="value" />
        
        <xsl:variable name="minutes" select="floor($value div 60) mod 60" />
        <xsl:variable name="seconds" select="$value mod 60" />
        <xsl:variable name="hours" select="floor($value div 3600)" />
        
        
        <xsl:if test="$hours">
            <xsl:if test="$hours &lt; 10">
                <xsl:text>0</xsl:text>
            </xsl:if>
            <xsl:value-of select="$hours" />
            
            <xsl:text>:</xsl:text>
        </xsl:if>
        
        
        <xsl:if test="$minutes &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$minutes" />
        <xsl:text></xsl:text>
    
        <xsl:text>:</xsl:text>
    
        <xsl:if test="$seconds &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$seconds" />
        <xsl:text></xsl:text>
    </xsl:template>    
</xsl:stylesheet>