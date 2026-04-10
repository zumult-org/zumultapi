<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="3.0">
    <xsl:template match="/">
        <div class="container mt-4">
            <ul class="nav nav-tabs" id="zumult_query_help_tabs" role="tablist">
                <xsl:apply-templates select="//section" mode="tabs"/>
            </ul>
            <div class="tab-content border border-top-0 p-3" id="zumult_query_help_tab_content">                
                <xsl:apply-templates select="//section" mode="tab_content"/>
            </div>
        </div>
    </xsl:template>
    
    <xsl:template match="section" mode="tabs">
        <li class="nav-item">
            <a id="home-tab" data-toggle="tab" role="tab">
                <xsl:attribute name="href"><xsl:value-of select="concat('#tab_', @name)"/></xsl:attribute>
                <xsl:attribute name="class">nav-link<xsl:if test="not(preceding-sibling::section)"> active</xsl:if></xsl:attribute>
                <xsl:value-of select="@name"/>
                <xsl:text> (</xsl:text>
                <xsl:value-of select="@corpus"/>
                <xsl:text>, </xsl:text>
                <xsl:value-of select="@language"/>
                <xsl:text>) </xsl:text>                
            </a>
        </li>        
    </xsl:template>
    
    <xsl:template match="section" mode="tab_content">
        <div role="tabpanel">
            <xsl:attribute name="id"><xsl:value-of select="concat('tab_', @name)"/></xsl:attribute>
            <xsl:attribute name="class">tab-pane fade<xsl:if test="not(preceding-sibling::section)"> show active</xsl:if></xsl:attribute>
            
            <p>The most <b>basic query (no CQP)</b> is just a word or a sequence of words, e.g.:</p>
            <ul>
                <xsl:apply-templates select="basic-query/query"/>
            </ul>
            
            <p>
                For more complex queries on different annotation levels, you can use CQP queries. 
                <b>Simple CQP queries</b> for individual token annotations can look like this:
            </p>
            <ul>
                <xsl:apply-templates select="simple-cqp-query/query"/>
            </ul>
            
            <p>
                <b>More complex CQP</b> queries can <b>combine one or several tokens</b> and/or use <b>regular expressions</b>, e.g.:
            </p>
            <ul>
                <xsl:apply-templates select="complex-cqp-query/query"/>
            </ul>
            
            <p>
                <b>Custom annotation layers</b> can be queried using <b>CQP expressions for spans</b>, e.g.:
            </p>
            <ul>
                <xsl:apply-templates select="annotation-cqp-query/query"/>
            </ul>
            
            <p>
                You can add <b>metadata restriction</b> to a query, e.g.:                            
            </p>
            <ul>
                <xsl:apply-templates select="metadata-cqp-query/query"/>
            </ul>
            
            
        </div>        
    </xsl:template>
    
    <xsl:template match="query">
        <li>
            <xsl:apply-templates/>
        </li>
    </xsl:template>
    
    <xsl:template match="query-expression">
        <span class="text-monospace query-expression" onclick="insertQuery(this)">
            <xsl:attribute name="data-corpus" select="ancestor::section[1]/@corpus"/>
            <xsl:value-of select="text()"/>
        </span>
    </xsl:template>

    <xsl:template match="explanation/text()">
        <span class="explanation"><xsl:value-of select="."/></span>
    </xsl:template>
    
    <xsl:template match="explanation/form">
        <span class="form"><xsl:value-of select="."/></span>        
    </xsl:template>
        
</xsl:stylesheet>