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
            <table class="table table-hover table-sm borderless" style="background:white;">
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
            <!--<td class="transcript"><xsl:value-of select="substring-before(@source, '_DF_01')"/></td>-->
            
            <td class="transcript">
                <a target="_blank">
                    <xsl:attribute name="title">
                        <xsl:value-of select = "$transcriptIdToolTip" />
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:text>../DGDLink?command=showData&amp;id=</xsl:text>
                        <xsl:value-of select="substring(@source,0, 13)"/>
                    </xsl:attribute>
                    <xsl:value-of select="@source"/>
                </a>
            </td>
            
            <td class="speaker" style="white-space: nowrap">              
                <xsl:apply-templates select="snippet/@who">
                    <xsl:with-param name="source" select="@source" /> 
                </xsl:apply-templates> 
            </td>
            <td class="hit">
                <xsl:if test="snippet/@isStartMore='true'"><xsl:text>... </xsl:text></xsl:if>
                <xsl:apply-templates select="snippet/*[not(@match='true') and not(preceding-sibling::*[@match='true'])]"/>
                <xsl:apply-templates select="snippet/*[@match='true' or (following-sibling::*[@match='true'] and preceding-sibling::*[@match='true'])]"/>
                <xsl:apply-templates select="snippet/*[not(@match='true') and not(following-sibling::*[@match='true'])]"/>
                <xsl:if test="snippet/@isEndMore='true'"><xsl:text> ...</xsl:text></xsl:if>    
            </td>
            <!--
            <td class="left_context">
                <xsl:if test="snippet/@isStartMore='true'"><xsl:text>...</xsl:text></xsl:if>
                <xsl:apply-templates select="snippet/*[not(@match='true') and not(preceding-sibling::*[@match='true'])]"/>
            </td>
            <td class="matches">
                <xsl:apply-templates select="snippet/*[@match='true' or (following-sibling::*[@match='true'] and preceding-sibling::*[@match='true'])]"/>                
            </td>
            <td class="right_context">
                <xsl:apply-templates select="snippet/*[not(@match='true') and not(following-sibling::*[@match='true'])]"/>
                <xsl:if test="snippet/@isEndMore='true'"><xsl:text>...</xsl:text></xsl:if>            
            </td>
            -->
           <td class="signal">
                <button type="button" class="btn btn-sm py-0 px-1 btn-open-oscillogram" title="Open oscillogram" style="display: none;">
                <xsl:attribute name="data-value-start">
                    <xsl:value-of select="@startInterval"/>
                </xsl:attribute>
                <xsl:attribute name="data-value-end">
                    <xsl:value-of select="@endInterval"/>
                </xsl:attribute>
                <xsl:attribute name="data-value-source">
                    <!--<xsl:value-of select="@media"/>-->
                    <xsl:value-of select="@source"/>
                </xsl:attribute>
                <span class="icon">
                    <i class="fa fa-signal"></i>
                </span>
                </button>
            </td>
            <!-- <td class="dgd-link">
                
                <xsl:variable name="testId">
                    <xsl:for-each select="snippet/*[@match='true']">
                        <xsl:if test="@parent !=''">
                            <xsl:value-of select="@xml:id"/>
                            <xsl:if test="not(position()=last())"><xsl:text> </xsl:text></xsl:if> 
                        </xsl:if>
                    </xsl:for-each>
                </xsl:variable>
                
                <xsl:if test="normalize-space($testId) != ''">
                    <a target="_blank">
                        <xsl:attribute name="title">
                            <xsl:value-of select = "$dgdToolTip" />
                        </xsl:attribute> 
                        <xsl:attribute name="href">
                            <xsl:text>../DGDLink?command=showTranscriptExcerpt&amp;transcriptID=</xsl:text>
                            <xsl:value-of select="@source"/>
                            <xsl:text>&amp;tokenIDs=</xsl:text>
                            <xsl:copy-of select="$testId" />
                        </xsl:attribute>
                        DGD
                    </a>
                </xsl:if>
            </td> -->
            <td class="zumult-link">
            <!--    <a title="Show excerpt in ZuMult" target="_blank">
                    <xsl:attribute name="href">
                        <xsl:text>../jsp/zuViel.jsp?transcriptIDWithHighlights=</xsl:text>        
                        <xsl:value-of select="@source"/>
                        <xsl:text>&amp;form=norm&amp;howMuchAround=3&amp;aroundTokenID=</xsl:text>
                        <xsl:value-of select="snippet/*[@match='true'][1]/@xml:id"/>
                        <xsl:text>&amp;highlightIDs1=</xsl:text>
                        <xsl:for-each select="snippet/*[@match='true']">
                            <xsl:value-of select="@xml:id"/>
                            <xsl:if test="not(position()=last())"><xsl:text> </xsl:text></xsl:if> 
                        </xsl:for-each>
                    </xsl:attribute>
                        ZuMult
                </a>-->
                <form target='_blank' action='../jsp/zuViel.jsp' method='post'>                
                        <!-- <input type='hidden' name='transcriptIDWithHighlights'> -->
                        <input type='hidden' name='transcriptID'>
                            <xsl:attribute name="value">
                                <xsl:value-of select="@source"/>
                            </xsl:attribute>
                        </input>
                        <input type='hidden' name='form'>
                            <xsl:attribute name="value">
                                <xsl:text>norm</xsl:text>
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
                        <button onclick='openTranscript(this)' type="button" class="btn btn-link my-0 py-0 btn-sm">
                            <xsl:attribute name="title">
                                <xsl:value-of select = "$zuMultToolTip" />
                            </xsl:attribute>
                            ZuViel
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
                    <xsl:attribute name="href">
                        <xsl:text>../DGDLink?command=showSpeakerMetadata&amp;speakerInitials=</xsl:text>
                        <xsl:value-of select="$firstSpeaker"/>
                        <xsl:text>&amp;transcriptID=</xsl:text>
                        <xsl:value-of select="$docID"/>
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