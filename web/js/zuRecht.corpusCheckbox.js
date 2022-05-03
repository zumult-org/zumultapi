/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function getCorpusQueryStr(){
                
    // validate corpus selection                    
    var checked = $("input[name='corpus']:checked").length;

    var corpusQueryStr='';
                
    if(!checked) {
        alert("Please select your corpus!");
    }else{
                     
        // crate corpus query
        var checkedCorpora = [];
        $.each($("input[name='corpus']:checked"), function(){
            checkedCorpora.push($(this).val());
        });

        corpusQueryStr = "corpusSigle=\"" + checkedCorpora.join(" | ") + "\"";             
    }
                
    return corpusQueryStr;
}

function setCorpora(corpora){
    $('input[name=corpus]').prop('checked', false);
    for (var i = 0; i < corpora.length; i++) {
        if (!($('input[id='+ corpora[i].trim() + ']').is(':checked'))){
            $('input[id='+ corpora[i].trim() + ']').prop('checked', true);
        }
    }
}