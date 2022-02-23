"use strict"

console.log("JS loaded!");

let acc = document.getElementsByClassName("accordion");
for (let i = 0; i < acc.length; i++) {
	acc[i].onclick = function(){
		this.classList.toggle("active");
		this.nextElementSibling.classList.toggle("show");
	}
}

document.getElementById('reg-date').valueAsDate = new Date();
