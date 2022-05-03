/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function sortTable(tableID, columnNumber, sortNumber, desc) {
    var table = document.getElementById(tableID);
    var goAbove;           
    var sort = true;
    while (sort) {
        sort = false;
        var rows = table.rows;
        for (var i = 1; i < (rows.length - 1); i++) {
            goAbove = false;
            var x = rows[i].getElementsByTagName("td")[columnNumber];
            var y = rows[i + 1].getElementsByTagName("td")[columnNumber];
            if (sortNumber){
                if (desc){
                    if(parseInt(x.innerHTML) < parseInt(y.innerHTML)){
                        goAbove = true;
                        break;
                    }
                }else {
                    if(parseInt(x.innerHTML) > parseInt(y.innerHTML)){
                        goAbove = true;
                        break;
                    }
                }
            }else{
                if (desc){
                    if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
                        goAbove = true;
                        break;
                    }
                }else {
                    if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
                        goAbove = true;
                        break;
                    } 
                }
            }
        }
        if (goAbove) {
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            sort = true;
        }
    }
}