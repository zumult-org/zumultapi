<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    
    <xsl:param name="speakerInitialsToolTip"/>
    <xsl:param name="transcriptIdToolTip"/>
    <xsl:param name="zuMultToolTip"/>
    <xsl:param name="dgdToolTip"/>
    
    <xsl:template match="/">
        <xsl:if test = "//meta/total > 0"> 
            <table class="table table-hover table-sm borderless w-auto">
                <!--<tr>
                    <th></th>
                    <th>Transcript</th>
                    <th>Speaker</th>
                    <th style="text-align:right;">Left context</th>
                    <th>Matches</th>
                    <th>Right context</th>
                    <th>DGD Link</th>
                </tr>-->
                <tbody>
                    <xsl:apply-templates select="//hit"/>
                </tbody>   
            </table>
        </xsl:if>
    </xsl:template>
    

    <xsl:template match="hit">
        <tr>
            <td class="numbering"><xsl:value-of select="@row"/></td>
            
            <td class="transcript">
                <a onclick="openMetadata(this)">
                    <xsl:attribute name="data-transcriptid">
                        <xsl:value-of select="@source"/>                        
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select = "$transcriptIdToolTip" />
                    </xsl:attribute>
                    <xsl:value-of select="@source"/>
                </a>
            </td>
            
            <td class="speaker" style="white-space: nowrap">              
                <a onclick="openSpeakerMetadata(this)">
                    <xsl:attribute name="data-transcriptid">
                        <xsl:value-of select="@source"/>                        
                    </xsl:attribute>
                    <xsl:attribute name="data-speakerid">
                        <xsl:value-of select="snippet/@who"/>                        
                    </xsl:attribute>
                    <xsl:attribute name="title">Show speaker metadata</xsl:attribute>
                    <xsl:value-of select="snippet/@who"/>
                </a>
                <!-- Do not understand what this does -->
                <!-- <xsl:apply-templates select="snippet/@who">
                    <xsl:with-param name="source" select="@source" /> 
                </xsl:apply-templates> -->
            </td>

            <td class="left-context">
                <xsl:if test="snippet/@isStartMore='true'"><xsl:text>... </xsl:text></xsl:if>
                <xsl:apply-templates select="snippet/*[not(@match='true') and not(preceding-sibling::*[@match='true'])]"/>
            </td>
            <td class="hit">
                <xsl:apply-templates select="snippet/*[@match='true' or (following-sibling::*[@match='true'] and preceding-sibling::*[@match='true'])]"/>
            </td>
            <td class="right-context">
                <xsl:apply-templates select="snippet/*[not(@match='true') and not(following-sibling::*[@match='true'])]"/>
                <xsl:if test="snippet/@isEndMore='true'"><xsl:text> ...</xsl:text></xsl:if>    
            </td>

            
            <td class="signal">
                <button type="button" onclick="playbackAudio(this)" class="btn btn-sm py-0 px-1" title="Playback audio">
                    <xsl:attribute name="data-tokenid">
                        <xsl:value-of select="snippet/*[@match='true']/@xml:id"/>
                    </xsl:attribute>
                    <xsl:attribute name="data-transcriptid">
                        <xsl:value-of select="@source"/>
                    </xsl:attribute>
                    <i class="fa-solid fa-play"></i>
                </button>
            </td>
            
            <td class="signal">
                <button type="button" onclick="playbackVideo(this)" class="btn btn-sm py-0 px-1" title="Playback video">
                    <xsl:attribute name="data-tokenid">
                        <xsl:value-of select="snippet/*[@match='true']/@xml:id"/>
                    </xsl:attribute>
                    <xsl:attribute name="data-transcriptid">
                        <xsl:value-of select="@source"/>
                    </xsl:attribute>
                    <i class="fa-solid fa-video"></i>
                </button>
            </td>
           
            <td class="signal">
                <button type="button" onclick="foldoutTranscript(this)" class="btn btn-sm py-0 px-1" title="Foldout transcript">
                    <!-- <xsl:attribute name="onclick">
                        <xsl:text>foldoutTranscript(this, '</xsl:text>
                        <xsl:value-of select="@source"/>
                        <xsl:text>','</xsl:text>
                        <xsl:value-of select="snippet/*[@match='true'][1]/@xml:id"/>                        
                        <xsl:text>','</xsl:text>
                        <xsl:value-of select="snippet/*[@match='true'][position()=last()]/@xml:id"/>
                        <xsl:text>','</xsl:text>
                        <xsl:for-each select="snippet/*[@match='true']">
                            <xsl:value-of select="@xml:id"/>
                            <xsl:if test="not(position()=last())"><xsl:text> </xsl:text></xsl:if> 
                        </xsl:for-each>
                        <xsl:text>')</xsl:text>
                    </xsl:attribute> -->
                    <xsl:attribute name="data-transcriptid">
                        <xsl:value-of select="@source"/>
                    </xsl:attribute>
                    <xsl:attribute name="data-starttokenid">
                        <xsl:value-of select="snippet/*[@match='true'][1]/@xml:id"/>                        
                    </xsl:attribute>
                    <xsl:attribute name="data-endtokenid">
                        <xsl:value-of select="snippet/*[@match='true'][position()=last()]/@xml:id"/>
                    </xsl:attribute>
                    <xsl:attribute name="data-highlightids1">
                        <xsl:for-each select="snippet/*[@match='true']">
                            <xsl:value-of select="@xml:id"/>
                            <xsl:if test="not(position()=last())"><xsl:text> </xsl:text></xsl:if> 
                        </xsl:for-each>
                    </xsl:attribute>
                    <i class="fa-regular fa-square-caret-right"></i>
                </button>
            </td>

            <td class="zumult-link">
                <form target='_blank' action='../jsp/zuViel.jsp' method='post'>                
                        <!-- <input type='hidden' name='transcriptIDWithHighlights'> -->
                        <input type='hidden' name='transcriptID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="@source"/>
                            </xsl:attribute>
                        </input>
                        <input type='hidden' name='form'>
                            <xsl:attribute name="value">
                                <xsl:text>trans</xsl:text>
                            </xsl:attribute>
                        </input>
                       <input type='hidden' name='howMuchAround'>
                            <xsl:attribute name="value">
                                <xsl:text>3</xsl:text>
                            </xsl:attribute>
                        </input>
                        <!--<input type='hidden' name='aroundTokenID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="snippet/*[@match='true'][1]/@xml:id"/>
                            </xsl:attribute>
                        </input>-->
                        <input type='hidden' name='highlightIDs1'>
                            <xsl:attribute name="value">
                                <xsl:for-each select="snippet/*[@match='true']">
                                    <xsl:value-of select="@xml:id"/>
                                    <xsl:if test="not(position()=last())"><xsl:text> </xsl:text></xsl:if> 
                                </xsl:for-each>
                            </xsl:attribute>
                        </input>
                        
                        <input type='hidden' name='startTokenID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="snippet/*[@match='true'][1]/@xml:id"/>
                            </xsl:attribute>
                        </input>
                        <input type='hidden' name='endTokenID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="snippet/*[@match='true'][position()=last()]/@xml:id"/>
                            </xsl:attribute>
                        </input>
                        <button onclick='openTranscript(this)' type="button" class="btn btn-sm py-0 px-1">
                            <xsl:attribute name="title">
                                <xsl:value-of select = "$zuMultToolTip" />
                            </xsl:attribute>
                            <i class="fa-regular fa-file-lines"></i>
                        </button>
                </form>
                
            </td>    
        </tr>
    </xsl:template>
    
    <xsl:template match="@who">
        <xsl:param name="source" />
        <xsl:call-template name="tokenize"> 
            <xsl:with-param name="csv" select="." /> 
            <xsl:with-param name="docID" select="$source" /> 
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="tokenize">
        <xsl:param name="csv" />
        <xsl:param name="docID" />
        
        <xsl:variable name="firstSpeaker" select="normalize-space( 
           substring-before( concat( $csv, ' '), ' '))" />
         
        <xsl:if test="$firstSpeaker">
            <a target="_blank">
                    <xsl:attribute name="title">
                        <xsl:value-of select = "$speakerInitialsToolTip" />
                    </xsl:attribute>
                    <xsl:value-of select="$firstSpeaker"/>
            </a>
            <xsl:text> </xsl:text>

            <xsl:call-template name="tokenize"> 
                <xsl:with-param name="csv" select="substring-after($csv,' ')" />
                <xsl:with-param name="docID" select="$docID" /> 
            </xsl:call-template>        
        </xsl:if>  
    </xsl:template>
    
    <xsl:template match="snippet/*">
        <span>
            <xsl:choose>
                <xsl:when test="@match='true'">
                    <xsl:attribute name="class">match</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="name()='pause' or name()='incident' or name()='vocal'" >
                        <xsl:attribute name="class">non-speech</xsl:attribute>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="name()='pause'">
                    <xsl:value-of select="@rend" />
                </xsl:when>

                <xsl:when test="name()='incident'">
                    <xsl:choose>
                        <xsl:when test="desc/@rend">
                            <xsl:value-of select="desc/@rend" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>((</xsl:text>
                                <xsl:apply-templates/>
                            <xsl:text>))</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:when>
                
                <xsl:when test="name()='vocal'">
                    <xsl:value-of select="*/@rend" />
                    <!--<xsl:apply-templates/>-->
                </xsl:when>
                
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
                        
        </span>
        <xsl:if test="following-sibling::*"><xsl:text> </xsl:text></xsl:if>
    </xsl:template>
    
    

    
</xsl:stylesheet>