"use strict";console.log(disemvowel('This website is for losers LOL!'));function process(char){const r$ =(function () {if(vowel(char)){return '';}return char;})();return r$;}function vowel(char){const r$ =['a','e','i','o','u'].indexOf(char.toLowerCase())>=0;return r$;}function disemvowel(text){const r$ =text.split('').map(process).join('');return r$;}