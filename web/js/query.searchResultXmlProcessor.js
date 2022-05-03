/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Document   : query.searchResultXmlProcessor
 * Created on : 11.03.2021, 10:07:25
 * Author     : Elena_Frick
 */

function getTotalHits(xml){
    var pattern = /.*total>(\d+)<\/total.*/;
    return xml.match(pattern, "$1")[1];
}

function getSearchQuery(xml){
    var pattern = /.*query>(.+)<\/query.*/;
    return xml.match(pattern, "$1")[1];
}

function getMetadataQuery(xml){
    var pattern = /.*metadataQuery>(.+)<\/metadataQuery.*/;
    if (xml.match(pattern, "$1")){
        return xml.match(pattern, "$1")[1];
    }else{
        return "";
    }
}

function getCorpusQuery(xml){
    var pattern = /.*corpusQuery>(.+)<\/corpusQuery.*/;
    return xml.match(pattern, "$1")[1];
}

function getSearchMode(xml){
    var pattern = /.*code>(.*)<\/code.*/;
    return xml.match(pattern, "$1")[1];
}

function getItemsPerPage(xml){
    var pattern = /.*itemsPerPage>(\d+)<\/itemsPerPage.*/;
    return xml.match(pattern, "$1")[1];
}

function getCorpora(xml){
    var corpusQueryStr = getCorpusQuery(xml);
    var pattern = /corpusSigle=(.*)/;
    var match = pattern.exec(corpusQueryStr);
    var str = match[1].replace(/\"/g, "");
    return str;     //e.g. 'FOLK | GWSS | MEND' 
}

function getDistinctValues(xml){
    var pattern = /.*distinctValues>(\d+)<\/distinctValues.*/;
    return xml.match(pattern, "$1")[1];
}

function getRepetitions(xml){
    var pattern = /<repetitions>[\s\S]*?<\/repetitions>/;
    if (xml.match(pattern)){
        return xml.match(pattern)[0];
    }else{
        return null;
    }
}