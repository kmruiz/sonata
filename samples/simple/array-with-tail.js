"use strict";function _(){let y,x=new Promise(function(r){y=r});return[x,y]}function _$(p){return Array.prototype.slice.call(p)}function SI(a,b){return setInterval(a,b)}function CI(a){clearInterval(a)}console.log(sum([1,2,3,4,5,6,7,8,9,10,]));function sum(x){let tail=x.slice(1);let a=x[0];if(a&&x.length>=2){return a+sum(tail);}if(a){return a;}return 0};