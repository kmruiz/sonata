"use strict";function _(){let y,x=new Promise(function(r){y=r});return[x,y]}function _$(p){return Array.prototype.slice.call(p)}function SI(a,b){return setInterval(a,b)}function CI(a){clearInterval(a)}console.log(disemvowel('This website is for losers LOL!'));function disemvowel(text){return text.split('').map(process).join('')};function process(char){if(vowel(char)){return '';}return char};function vowel(char){return ['a','e','i','o','u',].indexOf(char.toLowerCase())>=0};