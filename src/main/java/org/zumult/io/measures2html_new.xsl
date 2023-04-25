<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:zumult="http://zumult.org"
    exclude-result-prefixes="xs"
    version="2.0">
    
    
    
    
    <xsl:template match="/">

        <xsl:variable name="count" select="count(//measures)"/>
        <xsl:variable name="middle" select="ceiling($count div 2)"/>
        <xsl:variable name="quarter" select="ceiling($count div 4)"/>
        <xsl:variable name="three_quarter" select="ceiling($count * 3 div 4)"/>
        <!-- ************************************************* -->
        
        

        <xsl:variable name="MEDIANS_FOR_MEASURES">
            <medians-for-measures>
                <xsl:for-each-group select="//measure" group-by="@reference">
                    <xsl:sort select="current-grouping-key()"/>
                    <xsl:variable name="SORTED" as="xs:integer*">
                        <xsl:perform-sort select="current-group()/@types">
                            <xsl:sort/>
                        </xsl:perform-sort>
                    </xsl:variable>
                    <median>
                        <xsl:attribute name="reference" select="current-grouping-key()"/>
                        <xsl:attribute name="median" select="$SORTED[$middle]"></xsl:attribute>
                    </median>
                </xsl:for-each-group>                
                
            </medians-for-measures>
            
        </xsl:variable>
    
    
        <xsl:variable name="TYPE_TOKEN_RATIOS">
            <ratios>
                <xsl:for-each select="//measures">
                    <ratio><xsl:value-of select="@types div @tokens"/></ratio>
                </xsl:for-each>
            </ratios>
        </xsl:variable>
        <!-- ************************************************* -->
        <xsl:variable name="sorted_tokens" as="xs:integer*">
            <xsl:perform-sort select="//measures/@tokens">
                <xsl:sort/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_tokens" select="$sorted_tokens[$middle]"/>
        <xsl:variable name="quartile_tokens" select="$sorted_tokens[$quarter]"/>
        <xsl:variable name="three_quartile_tokens" select="$sorted_tokens[$three_quarter]"/>
        
        <!-- ************************************************* -->
        <xsl:variable name="sorted_types" as="xs:integer*">
            <xsl:perform-sort select="//measures/@types">
                <xsl:sort/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_types" select="$sorted_types[$middle]"/>
        <xsl:variable name="quartile_types" select="$sorted_types[$quarter]"/>
        <xsl:variable name="three_quartile_types" select="$sorted_types[$three_quarter]"/>
        
        <!-- ************************************************* -->
        <xsl:variable name="sorted_ratios" as="xs:double*">
            <xsl:perform-sort select="$TYPE_TOKEN_RATIOS/descendant::ratio">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_ratios" select="$sorted_ratios[$middle]"/>
        <xsl:variable name="quartile_ratios" select="$sorted_ratios[$quarter]"/>
        <xsl:variable name="three_quartile_ratios" select="$sorted_ratios[$three_quarter]"/>
        
        
        <table>
            <tr>
                <th></th>
                <th colspan="3">Types and Tokens</th>
                <xsl:for-each-group select="//measure" group-by="@reference">
                    <xsl:sort select="current-grouping-key()"/>
                    <th colspan="2"><xsl:value-of select="current-grouping-key()"/></th>
                </xsl:for-each-group>                
            </tr>
            
            <tr>
                <th></th>
                <th>Tokens</th>
                <th>Types</th>
                <th>Ratio</th>
                
                <xsl:for-each-group select="//measure" group-by="@reference">
                    <th>Intersect</th>
                    <th>Cover</th>
                </xsl:for-each-group>                                
            </tr>
            
            <tr>
                <th>Median</th>
                
                <td><xsl:value-of select="$median_tokens"/></td>
                <td><xsl:value-of select="$median_types"/></td>
                <td><xsl:value-of select="format-number($median_ratios, '0.00')"/></td>
                
                <xsl:for-each-group select="//measure" group-by="@reference">
                    <th><xsl:value-of select="$MEDIANS_FOR_MEASURES/descendant::median[@reference=current-grouping-key()]/@median"/></th>
                    <th>Cover</th>
                </xsl:for-each-group>                                
                
                
                
                <!-- <td><xsl:value-of select="$median_a1"/></td>
                <td><xsl:value-of select="format-number($median_a1_coverage, '0.00')"/></td>
                <td><xsl:value-of select="$median_a2"/></td>
                <td><xsl:value-of select="format-number($median_a2_coverage, '0.00')"/></td>
                <td><xsl:value-of select="$median_b1"/></td>
                <td><xsl:value-of select="format-number($median_b1_coverage, '0.00')"/></td> -->
                                
            </tr>
        </table>
    </xsl:template>    
</xsl:stylesheet>