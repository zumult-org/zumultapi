<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
            <div class="container mt-5">
                <!-- <event Art="Studentischer Vortrag"
                    Aufnahmeort__Ortsname_="Jyväskylä"
                    Inhalt__Themen_="Universität Bonn"
                    Sonstige_Bezeichnungen="SV_FI_019"
                    id="GWSS_E_00318"
                    video="false"/> -->
                
                <div id="accordion">
                    <xsl:for-each-group select="//event" group-by="@Art">
                        <div class="card">
                            <div class="card-header" id="headingOne">
                                <xsl:attribute name="id"><xsl:text>heading</xsl:text><xsl:value-of select="translate(current-grouping-key(), ' ', '_')"/></xsl:attribute>
                                <h5 class="mb-0">
                                    <button class="btn btn-link" data-toggle="collapse" data-target="#collapseOne" aria-expanded="true">
                                        <xsl:attribute name="data-target"><xsl:text>#collapse</xsl:text><xsl:value-of select="translate(current-grouping-key(), ' ', '_')"/></xsl:attribute>
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
                                
                                <xsl:attribute name="id"><xsl:text>collapse</xsl:text><xsl:value-of select="translate(current-grouping-key(), ' ', '_')"/></xsl:attribute>
                                <xsl:attribute name="aria-labelledby"><xsl:text>heading</xsl:text><xsl:value-of select="translate(current-grouping-key(), ' ', '_')"/></xsl:attribute>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-striped table-hover">
                                            <thead class="thead-dark">
                                                <th style="width: auto !important"></th>
                                                <th class="width: auto !important"></th>
                                                <th class="width: auto !important">GeWiss-Kennung</th>
                                                <th class="width: auto !important">Aufnahmeland (-ort)</th>
                                                <th class="width: auto !important">Themen</th>
                                                <th class="width: auto !important">DGD-Kennung</th>
                                            </thead>                                                
                                            <xsl:for-each select="current-group()">
                                                <xsl:sort select="@Aufnahmeort__Land_"/>
                                                <xsl:sort select="@Aufnahmeort__Ortsname_"/>
                                                <xsl:sort select="@Sonstige_Bezeichnungen"/>
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
                                                            <xsl:value-of select="substring-before(concat(@Sonstige_Bezeichnungen, ' ; '), ' ; ')"/>
                                                        </a>
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="@Aufnahmeort__Land_"/>
                                                        <xsl:text> (</xsl:text>
                                                        <xsl:value-of select="@Aufnahmeort__Ortsname_"/>
                                                        <xsl:text>)</xsl:text>
                                                    </td>
                                                    <td style="font-size:10pt;"><xsl:value-of select="@Inhalt__Themen_"/></td>
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