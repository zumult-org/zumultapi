<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="2.0">
    
    <xsl:param name="X_PER_SECOND" as="xs:integer">600</xsl:param>
    
    <xsl:variable name="STILLS" select="//video-stills"/>    
    
    
    <xsl:template match="/">
        <xsl:variable name="WIDTH" select="xs:int(//tli[last()]/@time * $X_PER_SECOND + 100)" />
        <xsl:variable name="HEIGHT" select="650"/>
        <svg xmlns="http://www.w3.org/2000/svg" style="padding:10px;border:1px solid lightGray;margin:10px;" id="pitchSVG">            
            <xsl:attribute name="width"><xsl:value-of select="$WIDTH"/>px</xsl:attribute>
            <xsl:attribute name="height"><xsl:value-of select="$HEIGHT"/>px</xsl:attribute>
            <xsl:attribute name="data-xPerSecond"><xsl:value-of select="$X_PER_SECOND"/></xsl:attribute>
            <xsl:attribute name="data-start"><xsl:value-of select="//tli[1]/@time"/></xsl:attribute>
            <xsl:attribute name="data-end"><xsl:value-of select="//tli[last()]/@time"/></xsl:attribute>
            <xsl:attribute name="onmousemove">moveSVGCursor(evt)</xsl:attribute>
            <xsl:attribute name="onclick">setSVGCursor(evt)</xsl:attribute>
            
            <g id="outerSVGWrapper">
    
                <g id="coordinateSystem">
                    <rect x="0" y="0" width="{$WIDTH - 100}" height="350" fill="aliceblue"/>
                    <rect x="0" y="398" width="{$WIDTH}" height="104" fill="lightGray"/>
                    
                    <!-- dashed lines and captions for the y-axis -->
                    <xsl:for-each select="1 to 7">
                        <text x="{$WIDTH - 100}" y="{50 * current()}" fill="lightGray">
                            <xsl:value-of select="(7- current()) * 100"/><xsl:text>Hz</xsl:text>
                        </text>
                        <line style="stroke:lightGray;stroke-width:1" x1="0" x2="{$WIDTH - 100}" y1="{50 * current()}" y2="{50 * current()}" stroke-dasharray="5,5"/>
                    </xsl:for-each>

                    <!-- tick marks for the x-axis -->
                    <g id="x-axis">
                        <xsl:for-each select="0 to (2 * xs:int(ceiling(//tli[last()]/@time))) - 1">
                            <xsl:variable name="X" select="(current() div 2) * $X_PER_SECOND"/>
                            <line style="stroke:gray;stroke-width:1" x1="{$X}" x2="{$X}" y1="350" y2="360"/>                
                            <text x="{$X}" y="375" fill="gray">
                                <xsl:value-of select="0.5 * current()"/><xsl:text>s</xsl:text>
                            </text>                    
                        </xsl:for-each>
                    </g> 
                    
                </g>
                
                <line id="svg_cursor" x1="0" x2="0" y1="0" y2="{$HEIGHT}" style="stroke:#00008b;stroke-width:1"></line>
                
                
                <g id="pitchCurve">
                    <xsl:apply-templates select="//pitch[not(@pitch='--undefined--')]"/>
                </g>
                
                <g id="mausData">
    
                
                    <!-- The orthographic words tier -->
                    <g id="zumin-ortho-words">
                        <xsl:for-each select="//tier[@speaker='ORT-MAU']/event">
                            <xsl:variable name="START-ID" select="@start"/>
                            <xsl:variable name="START-TIME" select="//tli[@id=$START-ID]/@time"/>
                            <xsl:variable name="END-ID" select="@end"/>
                            <xsl:variable name="END-TIME" select="//tli[@id=$END-ID]/@time"/>
                            <g>
                                <rect x="{$START-TIME * $X_PER_SECOND}" y="400" width="{($END-TIME - $START-TIME) * $X_PER_SECOND}" height="30" stroke="blue" fill="white"/>
                                <text
                                    x="{$START-TIME * $X_PER_SECOND + ($END-TIME - $START-TIME) * ($X_PER_SECOND div 2)}"
                                    y="415"
                                    dominant-baseline="middle"
                                    text-anchor="middle"
                                    >
                                    <xsl:value-of select="text()"/>
                                </text>
                                <!-- lines for the start and end of each word in the contour diagram -->
                                <line style="stroke:lightGray;stroke-width:1" x1="{$START-TIME * $X_PER_SECOND}" x2="{$START-TIME * $X_PER_SECOND}" y1="0" y2="350" stroke-dasharray="1 3"/>
                                <line style="stroke:lightGray;stroke-width:1" x1="{$END-TIME * $X_PER_SECOND}" x2="{$END-TIME * $X_PER_SECOND}" y1="0" y2="350" stroke-dasharray="1 3"/>
                            </g>                    
                        </xsl:for-each>
                    </g>
        
                    <!-- The canonical pronunciation tier -->
                    <g id="zumin-word-pho">
                        <xsl:for-each select="//tier[@speaker='KAN-MAU']/event">
                            <xsl:variable name="START-ID" select="@start"/>
                            <xsl:variable name="START-TIME" select="//tli[@id=$START-ID]/@time"/>
                            <xsl:variable name="END-ID" select="@end"/>
                            <xsl:variable name="END-TIME" select="//tli[@id=$END-ID]/@time"/>
                            <g>
                                <rect x="{$START-TIME * $X_PER_SECOND}" y="435" width="{($END-TIME - $START-TIME) * $X_PER_SECOND}" height="30" stroke="green" fill="white"/>
                                <!-- <text x="{$START-TIME *300} + 2" y="455"><xsl:value-of select="text()"/></text> -->
                                <text
                                    x="{$START-TIME * $X_PER_SECOND + ($END-TIME - $START-TIME) * ($X_PER_SECOND div 2)}"
                                    y="450"
                                    dominant-baseline="middle"
                                    text-anchor="middle"
                                    >
                                    <xsl:value-of select="text()"/>
                                </text>                    
                                
                            </g>                    
                        </xsl:for-each>
                    </g>
        
        
                    <!-- The phoneme tier -->
                    <g id="zumin-phonemes">
                        <xsl:for-each select="//tier[@speaker='MAU']/event">
                            <xsl:variable name="START-ID" select="@start"/>
                            <xsl:variable name="START-TIME" select="//tli[@id=$START-ID]/@time"/>
                            <xsl:variable name="END-ID" select="@end"/>
                            <xsl:variable name="END-TIME" select="//tli[@id=$END-ID]/@time"/>
                            <g>
                                <rect x="{$START-TIME * $X_PER_SECOND}" y="470" width="{($END-TIME - $START-TIME) * $X_PER_SECOND}" height="30" stroke="red" fill="white"/>
                                <!-- <text x="{$START-TIME *300} + 2" y="455"><xsl:value-of select="text()"/></text> -->
                                <text
                                    x="{$START-TIME * $X_PER_SECOND + ($END-TIME - $START-TIME) * ($X_PER_SECOND div 2)}"
                                    y="485"
                                    font-size="smaller"
                                    dominant-baseline="middle"
                                    text-anchor="middle"
                                    >
                                    <xsl:value-of select="text()"/>
                                </text>                    
                                
                            </g>                    
                        </xsl:for-each>
                    </g>
                </g>
            </g>
            
            <!-- video stills -->
            <xsl:if test="$STILLS/descendant::video-still">
                <g id="zumin-video-stills">
                    <rect x="0" y="510" width="{$WIDTH}" height="180" fill="black"/>
                    <xsl:for-each select="0 to (2 * xs:int(ceiling(//tli[last()]/@time))) - 1">
                        <xsl:variable name="X" select="(current() div 2) * $X_PER_SECOND"/>
                        <xsl:variable name="FILENAME" select="$STILLS/descendant::video-still[current() + 1]"/>
                        <image x="{$X}" y="510" href="../downloads/{$FILENAME}" width="200" />                            
                    </xsl:for-each>
                </g> 
            </xsl:if>
            
            
        </svg>
    </xsl:template>
    
    <xsl:variable name="delta" select="0.01"/>
    
    <!-- The pitch contour -->
    <xsl:template match="pitch">
        <circle xmlns="http://www.w3.org/2000/svg" r="3" fill="red">           
            <xsl:attribute name="cx" select="@time * $X_PER_SECOND"/>
            <xsl:attribute name="cy" select="350 - @pitch div 2"/>
        </circle>                
        <xsl:choose>
           <xsl:when test="preceding-sibling::pitch[1]/@pitch='--undefined--' or not(preceding-sibling::pitch)">
               <line xmlns="http://www.w3.org/2000/svg" style="stroke:red;stroke-width:2">
                   <xsl:attribute name="x1" select="(@time - $delta) * $X_PER_SECOND"/>
                   <xsl:attribute name="x2" select="@time * $X_PER_SECOND"/>
                   <xsl:attribute name="y1" select="350 - @pitch div 2"/>
                   <xsl:attribute name="y2" select="350 - @pitch div 2"/>                                
               </line>                
            </xsl:when>
            <xsl:otherwise>
                <line xmlns="http://www.w3.org/2000/svg" style="stroke:red;stroke-width:2">
                    <xsl:attribute name="x1" select="preceding-sibling::pitch[1]/@time * $X_PER_SECOND"/>
                    <xsl:attribute name="x2" select="@time * $X_PER_SECOND"/>
                    <xsl:attribute name="y1" select="350 - preceding-sibling::pitch[1]/@pitch div 2"/>
                    <xsl:attribute name="y2" select="350 - @pitch div 2"/>                                
                </line>                
            </xsl:otherwise>
        </xsl:choose>
        
        <xsl:choose>
            <xsl:when test="following-sibling::pitch[1]/@pitch='--undefined--' or not(following-sibling::pitch)">
                <line xmlns="http://www.w3.org/2000/svg" style="stroke:red;stroke-width:2">
                    <xsl:attribute name="x1" select="@time * $X_PER_SECOND"/>
                    <xsl:attribute name="x2" select="(@time + $delta) * $X_PER_SECOND"/>
                    <xsl:attribute name="y1" select="350 - @pitch div 2"/>
                    <xsl:attribute name="y2" select="350 - @pitch div 2"/>                                
                </line>                
            </xsl:when>
            <xsl:otherwise>
                <line xmlns="http://www.w3.org/2000/svg" style="stroke:red;stroke-width:2">
                    <xsl:attribute name="x1" select="following-sibling::pitch[1]/@time * $X_PER_SECOND"/>
                    <xsl:attribute name="x2" select="@time * $X_PER_SECOND"/>
                    <xsl:attribute name="y1" select="350 - following-sibling::pitch[1]/@pitch div 2"/>
                    <xsl:attribute name="y2" select="350 - @pitch div 2"/>                                
                </line>                
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
</xsl:stylesheet>