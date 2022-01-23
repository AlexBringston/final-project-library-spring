"use strict"

console.log("JS loaded!");

let acc = document.getElementsByClassName("accordion");
for (let i = 0; i < acc.length; i++) {
	acc[i].onclick = function(){
		this.classList.toggle("active");
		this.nextElementSibling.classList.toggle("show");
	}
}

document.getElementById('date').valueAsDate = new Date();
