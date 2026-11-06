<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : communication2HTML.xsl
    Created on : 19 December 2024, 14:17
    Author     : bernd
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>

    <xsl:template match="/">        
        <div>
            <span class="badge text-bg-primary">Metadata</span>    
            <xsl:apply-templates select="//Communication/Description"/>            
        </div>
        
        <div>
            <span class="badge text-bg-primary mb-2">
                <xsl:value-of select="count(descendant::Person)"/> Speakers
            </span><br/>    
            <i class="fa-solid fa-users"></i>
            <xsl:for-each select="descendant::Person">
                <span class="ms-2"><xsl:value-of select="text()"/><xsl:text> </xsl:text></span>
            </xsl:for-each>
        </div>
        
        <xsl:if test="descendant::Media[ends-with(descendant::NSLink, '.mp4')]">
            <div>
                <span class="badge text-bg-secondary me-2"><xsl:value-of select="count(descendant::Media[ends-with(descendant::NSLink, '.mp4')])"/> Videos</span>
                <table class="table table-sm">
                    <xsl:apply-templates select="descendant::Media[ends-with(descendant::NSLink, '.mp4')]"/>
                </table>
            </div>
        </xsl:if>
        
        <xsl:if test="descendant::Media[ends-with(descendant::NSLink, '.wav')]">
            <div>
                <span class="badge text-bg-secondary me-2"><xsl:value-of select="count(descendant::Media[ends-with(descendant::NSLink, '.wav')])"/> Audios</span>    
                <table class="table table-sm">
                    <xsl:apply-templates select="descendant::Media[ends-with(descendant::NSLink, '.wav')]"/>
                </table>            
            </div>
        </xsl:if>

        <div>
            <span class="badge text-bg-secondary me-2"><xsl:value-of select="count(descendant::Transcription[ends-with(descendant::NSLink, '.xml')])"/> Transcripts</span>    
            <table class="table table-sm">
                <xsl:apply-templates select="descendant::Transcription[ends-with(descendant::NSLink, '.xml')]"/>
            </table>            
        </div>
        
    </xsl:template>
    
    <xsl:template match="Description">
        <table class="table table-striped table-sm" style="font-size: 0.75rem;">
            <xsl:apply-templates select="Key">
                <xsl:sort select="@Name"/>
            </xsl:apply-templates>
        </table>
    </xsl:template>
    
    <xsl:template match="Key">
        <tr>
            <td class="metadatakey"><xsl:value-of select="@Name"/></td>
            <td class="metadatavalue"><xsl:value-of select="text()"/></td>
        </tr>
    </xsl:template>
    
    <xsl:template match="Media">
        <tr>
            <td class="me-2" style="width:100px;">
                <xsl:choose>
                    <xsl:when test="ends-with(descendant::NSLink, '.mp4')">
                        <i class="fa-solid fa-video"></i>
                    </xsl:when>
                    <xsl:otherwise>
                        <i class="fa-solid fa-volume-high"></i>           
                    </xsl:otherwise>
                </xsl:choose>                
            </td>
            <td>
                <xsl:value-of select="NSLink"/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="Transcription">
        <tr>
            <td class="me-2" style="width:100px;">
                <i class="fa-regular fa-file-lines"></i>
            </td>
            <td>
                <xsl:value-of select="NSLink"/>
            </td>
        </tr>
    </xsl:template>
    

</xsl:stylesheet>
