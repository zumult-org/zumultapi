<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:exmaralda="http://www.exmaralda.org"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:param name="META_FIELD_1">v_e_in_dgd_seit</xsl:param>
    <xsl:param name="META_FIELD_2">v_ses_alter_s</xsl:param>
    <xsl:param name="UNITS">TOKENS</xsl:param>
    
    <xsl:decimal-format name="WesternEurope" decimal-separator="," grouping-separator="."/>
    
    <xsl:function name="exmaralda:sortkey">
        <xsl:param name="META_FIELD"/>
        <xsl:param name="KEY"/>
        <xsl:choose>
            <xsl:when test="$META_FIELD='v_e_in_dgd_seit'">
                <xsl:value-of select="replace(replace($KEY, '(\.)([1-9]$)', '.0$2'), '2\.0_beta', '1.0')"/>                      
            </xsl:when>
            <xsl:when test="$META_FIELD='v_e_se_anzahl_s'">
                <xsl:choose>
                    <xsl:when test="$KEY castable as xs:integer">
                        <xsl:value-of select="format-number(xs:integer($KEY), '0000')"/>
                    </xsl:when>
                    <xsl:otherwise>0000</xsl:otherwise>
                </xsl:choose>                
            </xsl:when>
            <xsl:when test="$META_FIELD='v_ses_alter_s'">
                <xsl:choose>
                    <xsl:when test="$KEY castable as xs:integer">
                        <xsl:value-of select="format-number(xs:integer($KEY), '0000')"/>
                    </xsl:when>
                    <xsl:otherwise>0000</xsl:otherwise>
                </xsl:choose>                
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$KEY"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    <xsl:template match="/">
        <table class="table table-striped table-hover table-bordered">
            <xsl:choose>
                <xsl:when test="contains($META_FIELD_1, '_e_') and contains($META_FIELD_2, '_e_')">
                    <xsl:call-template name="EVENT_EVENT"/>
                </xsl:when>
                <xsl:when test="contains($META_FIELD_1, '_s_') and contains($META_FIELD_2, '_s_')">
                    <xsl:call-template name="SPEAKER_SPEAKER"/>
                </xsl:when>
                <xsl:when test="contains($META_FIELD_1, '_e_') and contains($META_FIELD_2, '_s_')">
                    <xsl:call-template name="EVENT_SPEAKER"/>
                </xsl:when>
                <xsl:when test="contains($META_FIELD_1, '_e_') and contains($META_FIELD_2, '_ses_')">
                    <xsl:call-template name="EVENT_SPEAKER"/>
                </xsl:when>
                <xsl:when test="contains($META_FIELD_1, '_s_') and contains($META_FIELD_2, '_ses_')">
                    <p>Diese Funktion wird demnächst verfügbar sein!</p>
                </xsl:when>
                <xsl:otherwise>
                    <p>Bitte Parameterreihenfolge umkehren!</p>
                </xsl:otherwise>
                
            </xsl:choose>
        </table>
    </xsl:template>
    
    <!-- ************************************ -->
    <!-- ************************************ -->
    <!-- ************************************ -->
    
    
    <!-- This one works for all combinations of _e_ / _se_ -->       
    <xsl:template name="EVENT_EVENT">
        <tr>
            <th></th>
            <xsl:for-each-group select="//speechEvent" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                <xsl:sort select="exmaralda:sortkey($META_FIELD_1, current-grouping-key())"/>  
                <th style="width:100px;">
                    <xsl:value-of select="current-grouping-key()"/>
                    <!-- <xsl:value-of select="replace(current-grouping-key(), '(\.)([1-9])', '.0$2')" /> -->
                </th>
            </xsl:for-each-group>
            <th>Total</th>
        </tr>
        <xsl:for-each-group select="//speechEvent" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2][1]]/property[@dgd-parameter=$META_FIELD_2]">
            <xsl:sort select="exmaralda:sortkey($META_FIELD_2, current-grouping-key())"/>                   
            <xsl:variable name="CURRENT_VALUE" select="current-grouping-key()"/>
            <tr>
                <th><xsl:value-of select="current-grouping-key()"/></th>
                <xsl:for-each-group select="//speechEvent" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                    <xsl:sort select="exmaralda:sortkey($META_FIELD_1, current-grouping-key())"/>                   
                    <td style="text-align: right; font-size: smaller;">
                        <xsl:choose>
                            <xsl:when test="$UNITS='OBJECTS'">
                                <xsl:value-of select="count(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE][1])"/>                                
                            </xsl:when>
                            <xsl:when test="$UNITS='TIME'">
                                <xsl:value-of select="exmaralda:format_time(sum(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE][1]/@audio-duration), 'true')"/>                                
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="format-number(sum(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE][1]/@tokens), '###.###', 'WesternEurope')"/>                                
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </xsl:for-each-group>
                <td style="text-align: right; font-size: smaller; font-weight:bold;">
                    <xsl:choose>
                        <xsl:when test="$UNITS='OBJECTS'">
                            <xsl:value-of select="count(//speechEvent/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE])"/>
                        </xsl:when>
                        <xsl:when test="$UNITS='TIME'">
                            <xsl:value-of select="exmaralda:format_time(sum(//speechEvent/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE]/@audio-duration), 'true')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="format-number(sum(//speechEvent/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE]/@tokens), '###.###', 'WesternEurope')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>                
        </xsl:for-each-group>
        <tr>
            <th>Total</th>
            <xsl:for-each-group select="//speechEvent" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                <xsl:sort select="exmaralda:sortkey($META_FIELD_1, current-grouping-key())"/>                   
                <td style="text-align: right; font-size: smaller; font-weight:bold;">
                    <xsl:choose>
                        <xsl:when test="$UNITS='OBJECTS'">
                            <xsl:value-of select="count(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]])"/>
                        </xsl:when>
                        <xsl:when test="$UNITS='TIME'">
                            <xsl:value-of select="exmaralda:format_time(sum(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]]/@audio-duration), 'true')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="format-number(sum(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]]/@tokens), '###.###', 'WesternEurope')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:for-each-group>
        </tr>
    </xsl:template>
    
    <!-- ************************************ -->
    <!-- ************************************ -->
    <!-- ************************************ -->
    
    <!-- This one works for all combinations of _s_ / _s_ -->       
    <xsl:template name="SPEAKER_SPEAKER">
        <tr>
            <th></th>
            <xsl:for-each-group select="//speaker" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                <!-- <xsl:sort select="substring-after(current-grouping-key(),'.')" data-type="number"/> -->
                <xsl:sort select="current-grouping-key()"/>
                <th style="width:100px;">
                    <xsl:value-of select="current-grouping-key()"/>
                    <!-- <xsl:value-of select="replace(current-grouping-key(), '(\.)([1-9])', '.0$2')" /> -->
                </th>
            </xsl:for-each-group>
            <th>Total</th>
        </tr>
        <xsl:for-each-group select="//speaker" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2][1]]/property[@dgd-parameter=$META_FIELD_2]">
            <xsl:sort select="current-grouping-key()"/>
            <xsl:variable name="CURRENT_VALUE" select="current-grouping-key()"/>
            <tr>
                <th><xsl:value-of select="current-grouping-key()"/></th>
                <xsl:for-each-group select="//speaker" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                    <xsl:sort select="current-grouping-key()"/>
                    <td style="text-align: right; font-size: smaller;">
                        <xsl:choose>
                            <xsl:when test="$UNITS='OBJECTS'">
                                <xsl:value-of select="count(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE][1])"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="format-number(sum(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE][1]/@tokens), '###.###', 'WesternEurope')"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </xsl:for-each-group>
                <td style="text-align: right; font-size: smaller; font-weight:bold;">
                    <xsl:choose>
                        <xsl:when test="$UNITS='OBJECTS'">
                            <xsl:value-of select="count(//speaker/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE])"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="format-number(sum(//speaker/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE]/@tokens), '###.###', 'WesternEurope')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>                
        </xsl:for-each-group>
        <tr>
            <th>Total</th>
            <xsl:for-each-group select="//speaker" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                <xsl:sort select="current-grouping-key()"/>
                <td style="text-align: right; font-size: smaller; font-weight:bold;">
                    <xsl:choose>
                        <xsl:when test="$UNITS='OBJECTS'">
                            <xsl:value-of select="count(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]])"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="format-number(sum(current-group()/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]]/@tokens), '###.###', 'WesternEurope')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:for-each-group>
        </tr>
    </xsl:template>
    
    <!-- ************************************ -->
    <!-- ************************************ -->
    <!-- ************************************ -->
    
    <!-- This one works for all combinations of _e_ / _s_ // no: it does not! // yes it does!-->       
    <xsl:template name="EVENT_SPEAKER">
        <tr>
            <th></th>
            <xsl:for-each-group select="//speechEvent" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                <xsl:sort select="exmaralda:sortkey($META_FIELD_1, current-grouping-key())"/>                   
                <!-- <xsl:sort select="substring-after(current-grouping-key(),'.')" data-type="number"/> -->
                <!-- <xsl:sort select="replace(replace(current-grouping-key(), '(\.)([1-9]$)', '.0$2'), '2\.0_beta', '1.0')" data-type="text"/> -->
                <th style="width:100px;">
                    <xsl:value-of select="current-grouping-key()"/>
                    <!-- <xsl:value-of select="replace(current-grouping-key(), '(\.)([1-9])', '.0$2')" /> -->
                </th>
            </xsl:for-each-group>
            <th>Total</th>
        </tr>
        <xsl:for-each-group select="//speaker" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2][1]]/property[@dgd-parameter=$META_FIELD_2]">
            <xsl:sort select="exmaralda:sortkey($META_FIELD_2, current-grouping-key())"/>                   
            <!-- <xsl:sort select="current-grouping-key()"/> -->
            <xsl:variable name="CURRENT_VALUE" select="current-grouping-key()"/>
            <xsl:variable name="SPEAKER_GROUP" select="current-group()"/>
            <tr>
                <th><xsl:value-of select="current-grouping-key()"/></th>
                <xsl:for-each-group select="//speechEvent" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
<!--                        <xsl:sort select="replace(replace(current-grouping-key(), '(\.)([1-9]$)', '.0$2'), '2\.0_beta', '1.0')" data-type="text"/>-->
                    <xsl:sort select="exmaralda:sortkey($META_FIELD_1, current-grouping-key())"/>                   
                    <td style="text-align: right; font-size: smaller;">
                        <xsl:choose>
                            <xsl:when test="contains($META_FIELD_2, '_ses_')">
                                <xsl:value-of 
                                    select="format-number(sum(for $i in distinct-values($SPEAKER_GROUP/@id) return current-group()/descendant::speaker[@id=$i and property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE]/@tokens), '###.###', 'WesternEurope')"/>                                 
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of 
                                    select="format-number(sum(for $i in distinct-values($SPEAKER_GROUP/@id) return current-group()/descendant::speaker[@id=$i]/@tokens), '###.###', 'WesternEurope')"/>                                                                 
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </xsl:for-each-group>
                <td style="text-align: right; font-size: smaller; font-weight:bold;">
                    <xsl:value-of select="format-number(sum(//speaker/ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_2]=$CURRENT_VALUE]/@tokens), '###.###', 'WesternEurope')"/>
                </td>
            </tr>                
        </xsl:for-each-group>
        <tr>
            <th>Total</th>
            <xsl:for-each-group select="//speechEvent" group-by="ancestor-or-self::*[property[@dgd-parameter=$META_FIELD_1]][1]/property[@dgd-parameter=$META_FIELD_1]">
                <xsl:sort select="exmaralda:sortkey($META_FIELD_1, current-grouping-key())"/>                   
                <td style="text-align: right; font-size: smaller; font-weight:bold;">
<!--                    <xsl:value-of select="format-number(sum(for $i in /*/speaker/@id return current-group()/descendant::speaker[@id=$i]/@tokens), '###.###', 'WesternEurope')"/> <br/>-->
                    <xsl:value-of select="format-number(sum(for $i in distinct-values(current-group()/descendant::speaker/@id) return current-group()/descendant::speaker[@id=$i]/@tokens), '###.###', 'WesternEurope')"/> <br/>
                    
                </td>
            </xsl:for-each-group>
        </tr>
    </xsl:template>
    

    <xsl:function name="exmaralda:format_time">
        <xsl:param name="time_sec"/>
        <xsl:param name="include_hours"/>
        <xsl:variable name="totalseconds">
            <xsl:value-of select="0 + $time_sec"/>
        </xsl:variable>
        <xsl:variable name="hours">
            <xsl:value-of select="0 + floor($totalseconds div 3600)"/>
        </xsl:variable>
        <xsl:variable name="minutes">
            <xsl:value-of select="0 + floor(($totalseconds - 3600*$hours) div 60)"/>
        </xsl:variable>
        <xsl:variable name="seconds">
            <xsl:value-of select="0 + ($totalseconds - 3600*$hours - 60*$minutes)"/>
        </xsl:variable>
        <xsl:if test="$include_hours='true'">
            <xsl:if test="$hours+0 &lt; 10 and $hours &gt;0">
                <xsl:text>0</xsl:text>
                <xsl:value-of select="$hours"/>
            </xsl:if>
            <xsl:if test="$hours + 0 = 0">
                <xsl:text>00</xsl:text>
            </xsl:if>
            <xsl:if test="$hours + 0 &gt;= 10">
                <xsl:value-of select="$hours"/>                
            </xsl:if>
            <xsl:text>:</xsl:text>
        </xsl:if>
        <xsl:if test="$minutes+0 &lt; 10">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:value-of select="$minutes"/>
        <xsl:text>:</xsl:text>
        <xsl:variable name="roundsec">
            <xsl:value-of select="round($seconds)"/>
        </xsl:variable>
        <!-- changed 04-03-2010 -->
        <!-- <xsl:value-of select="format-number($seconds, '00.00')"/> -->
        <xsl:value-of select="format-number($roundsec, '00')"/>
    </xsl:function>


</xsl:stylesheet>