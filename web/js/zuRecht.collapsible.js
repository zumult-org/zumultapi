/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function openContent(obj){
    var content = obj.nextElementSibling;
    if (content.style.display === "block") {
        content.style.display = "none";
    } else {
        content.style.display = "block";
    }
    
    if(obj.textContent==="Open XML"){
        obj.textContent= "Close XML";
    }else if(obj.textContent==="Close XML"){
        obj.textContent= "Open XML";
    }
}