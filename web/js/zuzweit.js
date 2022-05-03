/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function corpusSelectionChanged(){
    var corpusDropdown = document.getElementById('corpusSelector');
    var corpusID = corpusDropdown.options[corpusDropdown.selectedIndex].value;
    $.post(
        BASE_URL + "/ZumultDataServlet",
        { 
            command: 'getMetadataKeys',
            corpusID: corpusID
        },
        function( data ) {
            $('#metaField1Selector').html(data);
            $('#metaField2Selector').html(data);
        }
    );                
}

function meta1Changed(){
    
}

function meta2Changed(){
    
}

function unitChanged(){
    
}




