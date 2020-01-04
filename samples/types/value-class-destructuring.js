"use strict";

function _() {
    let y, x = new Promise(function (r) {
        y = r
    });
    return [x, y]
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

function price(amount, currency) {
    let self = {};
    self.class = 'price';
    self.amount = amount;
    self.currency = currency;
    return self;
}

console.log(print(price(42, 'EUR')));
console.log(print(price(42, '$')));

function print(price) {
    let currency = price.currency;
    let amount = price.amount;
    if (currency === '$') {
        return currency + ' ' + amount;
    }
    return amount + ' ' + currency
}