/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

"use strict";

function ECP(c, C) {
    let o = {};
    o._p$ = false;
    o._s$ = 0;
    o.class = c;
    o.contracts = C;
    o._m$ = [];
    o._i$ = SI(DQ(o), 0);
    return o
}

function _P() {
    let z, y, x = new Promise(function (r, R) {
        y = r;
        z = R;
    });
    return [x, y, z]
}

function _$(p) {
    return Array.prototype.slice.call(p)
}

function SI(a, b) {
    return setInterval(a, b)
}

function CI(a) {
    clearInterval(a)
}

function ST(s) {
    const F = function () {
        s._s$ = 1;
        if (s._m$.length > 0) {
            setTimeout(s.stop, 0)
        } else {
            CI(s._i$)
        }
    };
    F.messageName = 'stop';
    return F;
}

function PS(s, f) {
    return function () {
        const a = _$(arguments);
        const v = _P();
        const p = v[0];
        const r = v[1];
        if (s._s$ == 0) s._m$.push(function () {
            r(f.apply(null, a))
        }); else r(undefined);
        return p
    }
}

function DQ(s) {
    return function () {
        if (s._m$.length > 0) {
            s._m$.shift()()
        }
    }
}

function VCE(a, b) {
    return JSON.stringify(a) == JSON.stringify(b)
}

(async function () {
    function println(text) {
        return console.log(text)
    }

    function vowel(char) {
        return ['a', 'e', 'i', 'o', 'u',].indexOf(char.toLowerCase()) >= 0
    }

    function process(char) {
        if (vowel(char)) {
            return '';
        }
        if ((typeof char === 'string')) {
            return char;
        }
    }

    function disemvowel(text) {
        return text.split('').map(process).join('')
    }println(disemvowel('This website is for losers LOL!'));
})();