"use strict";
console.log(sum([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]));

function sum(x) {
    return (function () {
        var a = x[0];
        var tail = x.slice(1);
        var a = x[0];
        if (a && x.length >= 2) {
            return a + sum(tail)
        }
        if (a) {
            return a;
        }
        undefined;
        return 0;
    })();
};