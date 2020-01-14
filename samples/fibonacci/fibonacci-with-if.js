/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

"use strict";function ECP(c){let o={};o._p$=false;o.class=c;o._m$=[];o._i$=SI(DQ(o),0);return o}function _P(){let z,y,x=new Promise(function(r, R){y=r;z=R;});return[x,y,z]}function _$(p){return Array.prototype.slice.call(p)}function SI(a,b){return setInterval(a,b)}function CI(a){clearInterval(a)}function ST(s){const F=function(){if(s._m$.length>0){setTimeout(s.stop, 0)}else{CI(s._i$)}};F.messageName='stop';return F;}function PS(s,f){return function(){const a=_$(arguments);const v=_P();const p=v[0];const r=v[1];s._m$.push(function(){r(f.apply(null,a))});return p}}function DQ(s){return function(){if(s._m$.length>0){s._m$.shift()()}}}function VCE(a,b){return JSON.stringify(a)==JSON.stringify(b)}function f(a){if(a<=1){return 1;}else{return f(a-1)+f(a-2);}};println('f(1) '+f(1));println('f(2) '+f(2));println('f(3) '+f(3));println('f(4) '+f(4));println('f(5) '+f(5));function println(text){return console.log(text)};