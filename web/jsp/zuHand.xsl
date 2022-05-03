<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:template match="/">
        <xsl:if test = "//meta/total > 0"> 
            <table id="metadata-table" class="table table-sm borderless metadata-table">
                <thead>
                    <tr style="text-align: center; background-color: #d9edf7; color: #337AB7;">   
                       <!-- <th class="numbering">Total: <xsl:value-of select="//meta/distinctValues"/></th>-->
                        <th class="numbering"></th>
                        <th class="transcript">Spechereignis ID</th>                     
                        <th class="numberOfHits">Handlungssequenzen, <br/>Insgesamt: <xsl:value-of select="//meta/total"/></th> 
                        <th class="transcriptID">Transkript ID</th>
                        <th class="speaker" style="width: 100px;">Anzahl der Sprecher</th> 
                        <th class="open"></th>
                        <th class="close"></th>
                    </tr>
                </thead>
                <tbody class="metadata-table-body">
                    <xsl:apply-templates select="//item"/>
                </tbody>   
            </table>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template match="item">
        <tr class="metadata-table-row">
            <td class="numbering"><xsl:value-of select="@row"/></td>
            <td class="transcript" style="color: black; font-weight: bold; text-align: left;">
                <xsl:attribute name="data-value">
                    <xsl:value-of select="@metadataValue"/>                    
                </xsl:attribute>
                <xsl:value-of select="@metadataValue"/>
            </td>         
            <td class="numberOfHits" style="text-align: center;"><xsl:value-of select="@numberOfHits"/></td>  
            <td class="transcriptID" style="text-align: center;"></td>  
            <td class="speaker" style="text-align: center;"></td>      
            <td class="open" style="width:150px;">             
                <button onclick='loadActionSequences(this)' type="button" class="btn btn-link my-0 py-0 btn-sm"> 
                    anzeigen
                </button>
            </td> 
            <td class="close"></td>       
        </tr>
    </xsl:template>
    
   
</xsl:stylesheet>