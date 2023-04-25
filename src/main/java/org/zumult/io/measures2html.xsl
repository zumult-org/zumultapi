<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:exmaralda="http://www.exmaralda.org"
    exclude-result-prefixes="xs"
    version="2.0">

   
    <xsl:function name="exmaralda:stdv">
        <xsl:param name="TYPE_NAME"/>
        <xsl:variable name="AVG" select="avg(root()/descendant::measure[@type='transcript-tokens'])"/>
        <xsl:value-of select="math:sqrt((sum(for $i in root()/descendant::measure[@type='$TYPE_NAME'] return ((number($i/text()) - $AVG) * (number($i/text()) - $AVG))) div count(root()/descendant::measure[@type=$TYPE_NAME])))"/>
    </xsl:function>

    <xsl:template match="/">
        
        <xsl:variable name="TYPE_TOKEN_RATIOS">
            <ratios>
                <xsl:for-each select="//measures">
                    <ratio><xsl:value-of select="@lemmas div @tokens"/></ratio>
                </xsl:for-each>
            </ratios>
        </xsl:variable>
        <!-- ************************************************* -->
        <xsl:variable name="A1_COVERAGE">
            <coverages>
                <xsl:for-each select="//measure[@type='intersection' and @reference='GOETHE_A1']">
                    <coverage><xsl:value-of select="@lemmas div parent::measures/@lemmas"/></coverage>
                </xsl:for-each>
            </coverages>
        </xsl:variable>
        <!-- ************************************************* -->
        <xsl:variable name="A2_COVERAGE">
            <coverages>
                <xsl:for-each select="//measure[@type='intersection' and @reference='GOETHE_A2']">
                    <coverage><xsl:value-of select="@lemmas div parent::measures/@lemmas"/></coverage>
                </xsl:for-each>
            </coverages>
        </xsl:variable>
        <!-- ************************************************* -->
        <xsl:variable name="B1_COVERAGE">
            <coverages>
                <xsl:for-each select="//measure[@type='intersection' and @reference='GOETHE_B1']">
                    <coverage><xsl:value-of select="@lemmas div parent::measures/@lemmas"/></coverage>
                </xsl:for-each>
            </coverages>
        </xsl:variable>
        <!-- ************************************************* -->
        <xsl:variable name="count" select="count(//measures)"/>
        <xsl:variable name="middle" select="ceiling($count div 2)"/>
        <xsl:variable name="quarter" select="ceiling($count div 4)"/>
        <xsl:variable name="three_quarter" select="ceiling($count * 3 div 4)"/>
        <!-- ************************************************* -->
        <xsl:variable name="sorted_tokens" as="xs:double*">
            <xsl:perform-sort select="//measures/@tokens">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_tokens" select="$sorted_tokens[$middle]"/>
        <xsl:variable name="quartile_tokens" select="$sorted_tokens[$quarter]"/>
        <xsl:variable name="three_quartile_tokens" select="$sorted_tokens[$three_quarter]"/>
        
        <!-- ************************************************* -->
        <xsl:variable name="sorted_types" as="xs:double*">
            <xsl:perform-sort select="//measures/@lemmas">
                <xsl:sort data-type="number"/>
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
        
        <!-- ************************************************* -->
        <xsl:variable name="sorted_a1_coverage" as="xs:double*">
            <xsl:perform-sort select="$A1_COVERAGE/descendant::coverage">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:message select="$sorted_a1_coverage"/>
        <xsl:variable name="median_a1_coverage" select="$sorted_a1_coverage[$middle]"/>
        <xsl:variable name="quartile_a1_coverage" select="$sorted_a1_coverage[$quarter]"/>
        <xsl:variable name="three_quartile_a1_coverage" select="$sorted_a1_coverage[$three_quarter]"/>

        <!-- ************************************************* -->
        <xsl:variable name="sorted_a1" as="xs:double*">
            <xsl:perform-sort select="//measure[@type='intersection' and @reference='GOETHE_A1']/@lemmas">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_a1" select="$sorted_a1[$middle]"/>
        <xsl:variable name="quartile_a1" select="$sorted_a1[$quarter]"/>
        <xsl:variable name="three_quartile_a1" select="$sorted_a1[$three_quarter]"/>
        <!-- ************************************************* -->
        <xsl:variable name="sorted_a2_coverage" as="xs:double*">
            <xsl:perform-sort select="$A2_COVERAGE/descendant::coverage">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_a2_coverage" select="$sorted_a2_coverage[$middle]"/>
        <xsl:variable name="quartile_a2_coverage" select="$sorted_a2_coverage[$quarter]"/>
        <xsl:variable name="three_quartile_a2_coverage" select="$sorted_a2_coverage[$three_quarter]"/>
        
        <!-- ************************************************* -->
        <xsl:variable name="sorted_a2" as="xs:double*">
            <xsl:perform-sort select="//measure[@type='intersection' and @reference='GOETHE_A2']/@lemmas">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_a2" select="$sorted_a2[$middle]"/>
        <xsl:variable name="quartile_a2" select="$sorted_a2[$quarter]"/>
        <xsl:variable name="three_quartile_a2" select="$sorted_a2[$three_quarter]"/>
        
        <!-- ************************************************* -->
        <xsl:variable name="sorted_b1_coverage" as="xs:double*">
            <xsl:perform-sort select="$B1_COVERAGE/descendant::coverage">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_b1_coverage" select="$sorted_b1_coverage[$middle]"/>
        <xsl:variable name="quartile_b1_coverage" select="$sorted_b1_coverage[$quarter]"/>
        <xsl:variable name="three_quartile_b1_coverage" select="$sorted_b1_coverage[$three_quarter]"/>
        
        <!-- ************************************************* -->
        <xsl:variable name="sorted_b1" as="xs:double*">
            <xsl:perform-sort select="//measure[@type='intersection' and @reference='GOETHE_B1']/@lemmas">
                <xsl:sort data-type="number"/>
            </xsl:perform-sort>
        </xsl:variable>
        <xsl:variable name="median_b1" select="$sorted_b1[$middle]"/>
        <xsl:variable name="quartile_b1" select="$sorted_b1[$quarter]"/>
        <xsl:variable name="three_quartile_b1" select="$sorted_b1[$three_quarter]"/>
        
        
        <table>
            <tr>
                <th></th>
                <th colspan="3">Types and Tokens</th>
                <th colspan="2">Goethe A1</th>
                <th colspan="2">Goethe A2</th>
                <th colspan="2">Goethe B1</th>
            </tr>
            <tr>
                <th></th>
                <th>Tokens</th>
                <th>Types</th>
                <th>Ratio</th>
                <th>Intersect</th>
                <th>Cover</th>
                <th>Intersect</th>
                <th>Cover</th>
                <th>Intersect</th>
                <th>Cover</th>
            </tr>
            <tr>
                <th>Median</th>
                <td><xsl:value-of select="$median_tokens"/></td>
                <td><xsl:value-of select="$median_types"/></td>
                <td><xsl:value-of select="format-number($median_ratios, '0.00')"/></td>
                <td><xsl:value-of select="$median_a1"/></td>
                <td><xsl:value-of select="format-number($median_a1_coverage, '0.00')"/></td>
                <td><xsl:value-of select="$median_a2"/></td>
                <td><xsl:value-of select="format-number($median_a2_coverage, '0.00')"/></td>
                <td><xsl:value-of select="$median_b1"/></td>
                <td><xsl:value-of select="format-number($median_b1_coverage, '0.00')"/></td>
                
            </tr>
            <xsl:apply-templates select="//measures">
                <xsl:with-param name="quartile_tokens" select="$quartile_tokens"/>
                <xsl:with-param name="median_tokens" select="$median_tokens"/>
                <xsl:with-param name="three_quartile_tokens" select="$three_quartile_tokens"/>
                <xsl:with-param name="quartile_types" select="$quartile_types"/>
                <xsl:with-param name="median_types" select="$median_types"/>
                <xsl:with-param name="three_quartile_types" select="$three_quartile_types"/>
                <xsl:with-param name="quartile_ratios" select="$quartile_ratios"/>
                <xsl:with-param name="median_ratios" select="$median_ratios"/>
                <xsl:with-param name="three_quartile_ratios" select="$three_quartile_ratios"/>
                <xsl:with-param name="quartile_a1" select="$quartile_a1"/>
                <xsl:with-param name="median_a1" select="$median_a1"/>
                <xsl:with-param name="three_quartile_a1" select="$three_quartile_a1"/>
                <xsl:with-param name="quartile_a1_coverage" select="$quartile_a1_coverage"/>
                <xsl:with-param name="median_a1_coverage" select="$median_a1_coverage"/>
                <xsl:with-param name="three_quartile_a1_coverage" select="$three_quartile_a1_coverage"/>
                <xsl:with-param name="quartile_a2" select="$quartile_a2"/>
                <xsl:with-param name="median_a2" select="$median_a2"/>
                <xsl:with-param name="three_quartile_a2" select="$three_quartile_a2"/>
                <xsl:with-param name="quartile_a2_coverage" select="$quartile_a2_coverage"/>
                <xsl:with-param name="median_a2_coverage" select="$median_a2_coverage"/>
                <xsl:with-param name="three_quartile_a2_coverage" select="$three_quartile_a2_coverage"/>
                <xsl:with-param name="quartile_b1" select="$quartile_b1"/>
                <xsl:with-param name="median_b1" select="$median_b1"/>
                <xsl:with-param name="three_quartile_b1" select="$three_quartile_b1"/>
                <xsl:with-param name="quartile_b1_coverage" select="$quartile_b1_coverage"/>
                <xsl:with-param name="median_b1_coverage" select="$median_b1_coverage"/>
                <xsl:with-param name="three_quartile_b1_coverage" select="$three_quartile_b1_coverage"/>
            </xsl:apply-templates>
        </table>

    </xsl:template>
    
    <xsl:template match="measures">
        <xsl:param name="quartile_tokens"/>
        <xsl:param name="median_tokens"/>
        <xsl:param name="three_quartile_tokens"/>
        <xsl:param name="quartile_types"/>
        <xsl:param name="median_types"/>
        <xsl:param name="three_quartile_types"/>
        <xsl:param name="quartile_ratios"/>
        <xsl:param name="median_ratios"/>
        <xsl:param name="three_quartile_ratios"/>
        <xsl:param name="quartile_a1"/>
        <xsl:param name="median_a1"/>
        <xsl:param name="three_quartile_a1"/>
        <xsl:param name="quartile_a1_coverage"/>
        <xsl:param name="median_a1_coverage"/>
        <xsl:param name="three_quartile_a1_coverage"/>
        <xsl:param name="quartile_a2"/>
        <xsl:param name="median_a2"/>
        <xsl:param name="three_quartile_a2"/>
        <xsl:param name="quartile_a2_coverage"/>
        <xsl:param name="median_a2_coverage"/>
        <xsl:param name="three_quartile_a2_coverage"/>
        <xsl:param name="quartile_b1"/>
        <xsl:param name="median_b1"/>
        <xsl:param name="three_quartile_b1"/>
        <xsl:param name="quartile_b1_coverage"/>
        <xsl:param name="median_b1_coverage"/>
        <xsl:param name="three_quartile_b1_coverage"/>
        <!-- 
            <measure type="transcript-types">673</measure>
            <measure type="transcript-tokens">2822</measure>
            <measure type="intersection-types-A1">259</measure>
            <measure type="intersection-tokens-A1">1984</measure>
            <measure type="intersection-types-A2">360</measure>
            <measure type="intersection-tokens-A2">2236</measure>
            <measure type="intersection-types-B1">457</measure>
            <measure type="intersection-tokens-B1">2378</measure>        
        -->
        <tr>
            <th>
                <a> 
                    <xsl:attribute name="href">
                        <xsl:text>intersectwordlist.jsp?corpusID=</xsl:text>
                        <xsl:value-of select="substring(@speechEventID,1,4)"/>
                        <xsl:text>&amp;speechEventID=</xsl:text>
                        <xsl:value-of select="@speechEventID"/>
                        <xsl:text>&amp;wordlistID=GOETHE_A1</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="target">_blank</xsl:attribute>
                    <xsl:value-of select="@speechEventID"/>
                </a>
            </th>
            <td>
                <xsl:variable name="thisValue" select="@tokens"/>                    
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_tokens">red;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_tokens">green;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_tokens) and ($thisValue &lt; $median_tokens)">yellow;</xsl:when>
                        <xsl:otherwise>orange;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="$thisValue"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="@lemmas"/>                    
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_types">red;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_types">green;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_types) and ($thisValue &lt; $median_types)">yellow;</xsl:when>
                        <xsl:otherwise>orange;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="$thisValue"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="@types div @tokens"/>
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_ratios">red;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_ratios">green;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_ratios) and ($thisValue &lt; $median_ratios)">yellow;</xsl:when>
                        <xsl:otherwise>orange;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="format-number($thisValue, '0.00')"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="measure[@type='intersection' and @reference='GOETHE_A1']/@lemmas"/>                    
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_a1">green;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_a1">red;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_a1) and ($thisValue &lt; $median_a1)">orange;</xsl:when>
                        <xsl:otherwise>yellow;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="$thisValue"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="measure[@type='intersection' and @reference='GOETHE_A1']/@lemmas div @lemmas"/>
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_a1_coverage">green;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_a1_coverage">red;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_a1_coverage) and ($thisValue &lt; $median_a1_coverage)">orange;</xsl:when>
                        <xsl:otherwise>yellow;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="format-number($thisValue, '0.00')"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="measure[@type='intersection' and @reference='GOETHE_A2']/@lemmas"/>                    
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_a2">green;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_a2">red;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_a2) and ($thisValue &lt; $median_a2)">orange;</xsl:when>
                        <xsl:otherwise>yellow;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="$thisValue"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="measure[@type='intersection' and @reference='GOETHE_A2']/@lemmas div @lemmas"/>
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_a2_coverage">green;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_a2_coverage">red;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_a2_coverage) and ($thisValue &lt; $median_a2_coverage)">orange;</xsl:when>
                        <xsl:otherwise>yellow;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="format-number($thisValue, '0.00')"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="measure[@type='intersection' and @reference='GOETHE_B1']/@lemmas"/>                    
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_b1">green;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_b1">red;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_b1) and ($thisValue &lt; $median_b1)">orange;</xsl:when>
                        <xsl:otherwise>yellow;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="$thisValue"/>
            </td>
            <!-- ****************************************** -->
            <td>
                <xsl:variable name="thisValue" select="measure[@type='intersection' and @reference='GOETHE_B1']/@lemmas div @lemmas"/>
                <xsl:attribute name="style">
                    <xsl:text>background:</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$thisValue &gt; $three_quartile_b1_coverage">green;</xsl:when>
                        <xsl:when test="$thisValue &lt; $quartile_b1_coverage">red;</xsl:when>
                        <xsl:when test="($thisValue &gt; $quartile_b1_coverage) and ($thisValue &lt; $median_b1_coverage)">orange;</xsl:when>
                        <xsl:otherwise>yellow;</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="format-number($thisValue, '0.00')"/>
            </td>
            
        </tr>
    </xsl:template>
</xsl:stylesheet>