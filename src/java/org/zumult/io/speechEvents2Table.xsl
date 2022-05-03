<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <!-- <key id="e_se_interaktionsdomaene">Institutionell</key> -->
    <xsl:param name="GROUPING_KEY"/>
    <xsl:param name="COLUMN_KEYS_STRING">
        <!-- <keys>
            <key id="e_se_art" name="Kurzbezeichnung"/>
            <key id="e_se_lebensbereich" name="Lebensbereich"/>
            <key id="e_se_konstellation" name="Konstellation"/>
        </keys> -->
        e_se_art---Kurzbezeichnung***e_se_lebensbereich---Lebensbereich
    </xsl:param>
    <xsl:variable name="COLUMN_KEYS">
        <keys>
            <xsl:for-each select="tokenize(normalize-space($COLUMN_KEYS_STRING), '\*\*\*')">
                <xsl:element name="key">
                    <xsl:attribute name="id" select="substring-before(current(),'---')"/>
                    <xsl:attribute name="name" select="substring-after(current(),'---')"/>
                </xsl:element>
            </xsl:for-each>
        </keys>
    </xsl:variable>
    
    <xsl:template match="/">
        <xsl:message select="$COLUMN_KEYS"/>
            <div class="container mt-5">
                <xsl:choose>
                    <xsl:when test="$GROUPING_KEY">
                        <div id="accordion">
                            <xsl:for-each-group select="//speech-event" group-by="key[@id=$GROUPING_KEY]">
                                <xsl:sort select="count(current-group())" order="descending"/>
                                <div class="card">
                                    <div class="card-header" id="headingOne">
                                        <xsl:attribute name="id"><xsl:text>heading</xsl:text><xsl:value-of select="replace(current-grouping-key(), '[^A-Za-z0-9]', '_')"/></xsl:attribute>
                                        <h5 class="mb-0">
                                            <button class="btn btn-link" data-toggle="collapse" data-target="#collapseOne" aria-expanded="true">
                                                <xsl:attribute name="data-target"><xsl:text>#collapse</xsl:text><xsl:value-of select="replace(current-grouping-key(), '[^A-Za-z0-9]', '_')"/></xsl:attribute>
                                                <xsl:attribute name="aria-controls" select="replace(current-grouping-key(), '[^A-Za-z0-9]', '_')"/>
                                                <xsl:value-of select="current-grouping-key()"/>
                                                <xsl:text> (</xsl:text>
                                                <xsl:value-of select="count(current-group())"/>
                                                <xsl:text>)</xsl:text>
                                            </button>
                                        </h5>
                                    </div>
                                    <div id="collapseOne" class="collapse" data-parent="#accordion">
                                        <xsl:attribute name="class">
                                            collapse
                                            <!-- <xsl:if test="position()=1"> show</xsl:if> -->
                                        </xsl:attribute>                                
                                        <xsl:attribute name="id"><xsl:text>collapse</xsl:text><xsl:value-of select="replace(current-grouping-key(), '[^A-Za-z0-9]', '_')"/></xsl:attribute>
                                        <xsl:attribute name="aria-labelledby"><xsl:text>heading</xsl:text><xsl:value-of select="replace(current-grouping-key(), '[^A-Za-z0-9]', '_')"/></xsl:attribute>
                                        <div class="card-body">
                                            <xsl:call-template name="MAKE_TABLE"/>
                                        </div>
                                    </div>                                
                                </div>
                            </xsl:for-each-group>
                        </div>                        
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:for-each-group select="//speech-event" group-by="name()">
                            <xsl:call-template name="MAKE_TABLE"/>                            
                        </xsl:for-each-group>
                    </xsl:otherwise>
                </xsl:choose>
            </div>
    </xsl:template>
    
    <xsl:template name="MAKE_TABLE">
        <div class="table-responsive">
            <table class="table table-striped table-hover speech-event-table">
                <thead class="thead-dark">
                    <xsl:if test="//speech-event[@video='true']">
                        <th style="width: auto !important"></th>                        
                    </xsl:if>
                    <th class="width: auto !important"></th>
                    <xsl:for-each select="$COLUMN_KEYS/descendant::key">
                        <th class="width: auto !important"><xsl:value-of select="@name"/></th>
                    </xsl:for-each>
                    <th class="width: auto !important">ID</th>
                </thead>                                                
                <xsl:for-each select="current-group()">      
                    <xsl:sort select="normalize-space(key[@id=$COLUMN_KEYS/descendant::key[1]/@id])"/>
                    <xsl:variable name="THIS_ONE" select="current()"/>
                    <tr>
                        <xsl:if test="//speech-event[@video='true']">
                            <td>
                                <xsl:if test="@video='true'">
                                    <img src="../images/video_16_16.png"/>
                                </xsl:if>
                            </td>
                        </xsl:if>
                        <td>
                            <a class="nav-link" href="#metadataModal" data-toggle="modal">
                                <xsl:attribute name="onclick">openMetadataModal('<xsl:value-of select="@id"/>')</xsl:attribute>
                                <xsl:value-of select="position()"/>
                            </a>
                        </td>
                        <xsl:for-each select="$COLUMN_KEYS/descendant::key">
                            <xsl:variable name="KEY_ID" select="@id"/>
                            <td>
                                <xsl:value-of select="$THIS_ONE/descendant::key[@id=$KEY_ID]"/>
                            </td>
                        </xsl:for-each>
                        
                        <td><xsl:value-of select="@id"/></td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
        
    </xsl:template>
</xsl:stylesheet>