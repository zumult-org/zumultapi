<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
            <div class="container mt-5">
                <!-- <event Art="Pausenkommunikation im Theater"
                    Interaktionsdomäne="Privat"
                    Lebensbereich="Privat (nicht spezifiziert)"
                    id="FOLK_E_00081"
                    video="false"/> -->
                
                <div id="accordion">
                    <xsl:for-each-group select="//event" group-by="@Interaktionsdomäne">
                        <div class="card">
                            <div class="card-header" id="headingOne">
                                <xsl:attribute name="id"><xsl:text>heading</xsl:text><xsl:value-of select="current-grouping-key()"/></xsl:attribute>
                                <h5 class="mb-0">
                                    <button class="btn btn-link" data-toggle="collapse" data-target="#collapseOne" aria-expanded="true">
                                        <xsl:attribute name="data-target"><xsl:text>#collapse</xsl:text><xsl:value-of select="current-grouping-key()"/></xsl:attribute>
                                        <xsl:attribute name="aria-controls" select="current-grouping-key()"/>
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
                                
                                <xsl:attribute name="id"><xsl:text>collapse</xsl:text><xsl:value-of select="current-grouping-key()"/></xsl:attribute>
                                <xsl:attribute name="aria-labelledby"><xsl:text>heading</xsl:text><xsl:value-of select="current-grouping-key()"/></xsl:attribute>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-striped table-hover">
                                            <thead class="thead-dark">
                                                <th style="width: auto !important"></th>
                                                <th class="width: auto !important"></th>
                                                <th class="width: auto !important">Kurzbezeichnung</th>
                                                <th class="width: auto !important">Lebensbereich</th>
                                                <th class="width: auto !important">Konstellation</th>
                                                <th class="width: auto !important">Kennung</th>
                                            </thead>                                                
                                            <xsl:for-each select="current-group()">
                                                <xsl:sort select="@Lebensbereich"/>
                                                <xsl:sort select="@Art"/>
                                                <xsl:sort select="@id"/>
                                                
                                                <tr>
                                                    <td>
                                                        <xsl:if test="@video='true'">
                                                            <img src="../images/video_16_16.png"/>
                                                        </xsl:if>
                                                    </td>
                                                    <td><xsl:value-of select="position()"/></td>
                                                    <td>
                                                        <a class="nav-link" href="#metadataModal" data-toggle="modal">
                                                            <xsl:attribute name="onclick">openMetadataModal('<xsl:value-of select="@id"/>')</xsl:attribute>
                                                            <xsl:value-of select="@Art"/>
                                                        </a>
                                                    </td>
                                                    <td><xsl:value-of select="@Lebensbereich"/></td>
                                                    <td><xsl:value-of select="@Sprecher-Konstellation"/></td>
                                                    <td><xsl:value-of select="@id"/></td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </div>
                                    
                                </div>
                            </div>                                
                        </div>
                    </xsl:for-each-group>
                </div>
            </div>
    </xsl:template>
</xsl:stylesheet>