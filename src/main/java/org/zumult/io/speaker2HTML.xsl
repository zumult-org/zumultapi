<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>

    <xsl:template match="/">
        
        <ul class="nav nav-tabs" id="myTab" role="tablist">
            <!-- this is the row of tabs -->
            <xsl:for-each select="//Speaker">
                <li class="nav-item" role="presentation">
                  <a aria-current="page" data-toggle="tab">
                      <xsl:attribute name="class">nav-link<xsl:if test="position()=1"> active</xsl:if></xsl:attribute>
                      <xsl:attribute name="href" select="concat('#', concat('TAB_', replace(@Id, '\.', '_')))"/>
                      <xsl:value-of select="@Id"/>                  
                  </a>
                </li>                
            </xsl:for-each>
        </ul>
        <div class="tab-content" id="myTabContent">
            <xsl:apply-templates select="//Speaker"/>
        </div>
    </xsl:template>
    
    <xsl:template match="Speaker">
        <div role="tabpanel" aria-labelledby="home-tab">
            <xsl:attribute name="class">tab-pane fade<xsl:if test="not(preceding-sibling::Speaker)"> show active</xsl:if></xsl:attribute>
            <xsl:attribute name="id" select="concat('TAB_', replace(@Id, '\.', '_'))"/>
            <table class="table table-striped table-sm">
                <xsl:apply-templates select="Description/Key">
                    <xsl:sort select="@Name"/>
                </xsl:apply-templates>
            </table>
        </div>
    </xsl:template>



    <xsl:template match="Key">
        <tr>
            <td class="metadatakey"><xsl:value-of select="@Name"/></td>
            <td class="metadatavalue"><xsl:value-of select="text()"/></td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
