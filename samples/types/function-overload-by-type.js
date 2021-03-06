/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

"use strict";let _directory = new Map();

function REGISTER(entity) {
    _directory.set(entity._id, entity);
}

function UNREGISTER(entity) {
    _directory.delete(entity._id);
}

function HASENTITY(entity) {
    _directory.has(entity._id);
}

function DIRECTORYEMPTY() {
    return _directory.size === 0;
}function ENTITYCLASS(className, contracts) {
    let obj = {};
    obj._id = className + (+(new Date())) + Math.random();
    obj.class = className;
    obj.contracts = contracts;
    FREE(obj);

    REGISTER(obj);
    obj.__stop = function () {
        UNREGISTER(obj);
        STOP();
    };

    return obj;
}

function BUSY(entity) {
    entity._busy$ = true;
}

function FREE(entity) {
    entity._busy$ = false;
}

function ISBUSY(entity) {
    return false;
}function VCE(a, b) {
    return JSON.stringify(a) == JSON.stringify(b)
}let _mailbox = [];
let _deadletter = [];
let _interval = null;

function START() {
    if (!_interval) {
        _interval = setInterval(CONSUME, 0);
    }
}

function CONSUME() {
    if (_mailbox.length === 0) {
        return;
    }

    const message = _mailbox[0];
    const actor = message.actor;
    const execution = message.execution;
    const context = message.context;
    if (!HASENTITY(actor)) {
        const msgPrintable = { execution: execution, context: context };
        _deadletter.push(message);
    }

    if (ISBUSY(actor)) {
        return;
    }

    _mailbox = _mailbox.slice(1);
    DELIVER(message);
}

function DELIVER(message) {
    const entity = message.actor;
    BUSY(entity);
    entity.__context = message.context;
    const execution = message.execution;
    let result = null;

    try {
        result = entity[execution.method].apply(null, execution.arguments);
    } catch (e) {
        execution.reject(e);
        FREE(entity);
    }

    if (result && result.then) {
        result.then(execution.resolve).catch(execution.reject).finally(() => FREE(entity));
    } else {
        execution.resolve(result);
        FREE(entity);
    }
}

function ENQUEUEFN(self, method, frame) {
    return function () {
        return new Promise((resolve, reject) => {
            const args = Array.prototype.slice.apply(arguments);
            const execution = { method: method, arguments: args, resolve: resolve, reject: reject };
            const context = Object.assign({}, self.__context);
            PUSHFRAME(frame, context);

            _mailbox.push({ actor: self, context: context, execution: execution });
        });
    }
}

function STOP() {
    if (DIRECTORYEMPTY()) {
        END();
    }
}

function END() {
    clearInterval(_interval);
}

function exit() {
    setTimeout(END, 10);
}function PUSHFRAME(frame, context) {
    context.stacktrace = (context.stacktrace || []).concat([frame]);
}(async function (){function println(text){return console.log(text)}let Amazing={};(function(self){})(Amazing);async function This(){let self=ENTITYCLASS('This',['Amazing']);self.stop=ENQUEUEFN(self,'__stop', {});START();return self;}async function Other(){let self=ENTITYCLASS('Other',['Amazing']);self.stop=ENQUEUEFN(self,'__stop', {});START();return self;}function Price(amount){let self={};self.class='Price';self.amount=amount;return self;}function say(a){if((typeof a=== 'string')){return println('String');}if(!isNaN(a)){return println('Number');}if(a.class==='Price'){return println('Price');}if((a.class==='This'||a.contracts.indexOf('This')!=-1)){return println('This');}if((a.class==='Amazing'||a.contracts.indexOf('Amazing')!=-1)){return println('Amazing');}}let this1=(await This());let other2=(await Other());say('');say(1);say(Price(1));say(this1);say(other2);this1.stop();other2.stop();})();