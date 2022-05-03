/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Document   : query.stringConverter
 * Created on : 11.03.2021, 10:07:25
 * Author     : Elena_Frick
 * 
 */


function encodeQueryToURL(queryStr){

    var queryString = queryStr
        .replace(/&lt;/g, "%3C").replace(/&gt;/g, "%3E")
        .replace(/&amp;/g, "%26").replace(/&/g, "%26")
        .replace(/\+/g, "%2B").replace(/#/g, "%23")
        .replace(/\|/g, "%7C").replace(/\\/g, "%5C")
        .replace(/\{/g, "%7B").replace(/\}/g, "%7D")
        .replace(/”/g, "\""); 
    return queryString;
}

function decodeHTMLQuery(queryStr){

    var queryString = queryStr
        .replace(/&lt;/g, "<").replace(/&gt;/g, ">")
        .replace(/&amp;/g, "&")
        .replace(/”/g, "\"");
    return queryString;
}

function diplayUnicodeCharacter(queryStr){

    var queryString = queryStr
        .replace(/&#596;/g, "ɔ")
        .replace(/&#603;/g, "ɛ")
        .replace(/&#660;/g, "ʔ")
        .replace(/&#618;/g, "ɪ")
        .replace(/&#643;/g, "ʃ")
        .replace(/&#650;/g, "ʊ")
        .replace(/&#601;/g, "ə")
        .replace(/&#592;/g, "ɐ")
        .replace(/&#058;/g, "ː")
        .replace(/&#655;/g, "ʏ")
        .replace(/&#331;/g, "ŋ")
        .replace(/&#609;/g, "ɡ");
        //.replace(/&#231;/g, "ç");
    return queryString;
}

function escapeParentheses(queryStr){
    var queryString = queryStr
        .replace(/\(/g, "\\(").replace(/\)/g, "\\)");
    return queryString;
}

function removeSpaces(queryStr){
    var queryString = queryStr.replace(/<br>/g, " | ").replace(/\|\s*$/g,'').replace(/\s+/g,' ');
    return queryString;
}

