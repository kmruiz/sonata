"use strict";function _(){let y,x=new Promise(function(r){y=r});return[x,y]}function _$(p){return Array.prototype.slice.call(p)}function SI(a,b){return setInterval(a,b)}function CI(a){clearInterval(a)}function PS(s,f){return function(){const a=_$(arguments);const v=_();const p=v[0];const r=v[1];s._m$.push(function(){r(f.apply(null,a))});return p}}function DQ(s){return function(){if(s._m$.length>0){s._m$.shift()()}}}function price(amount,currency){let self={};self.class='price';self.amount=amount;self.currency=currency;self.format=function format(){return amount+' '+currency};return self;}println(price(42,'EUR').format());function println(text){return console.log(text)};