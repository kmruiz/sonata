/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

"use strict";let _directory = new Map();function ENTITYCLASS(className, contracts) {
    let obj = {};
    obj._id = className + (new Date()) + Math.random();
    obj.class = className;
    obj.contracts = contracts;
    FREE(obj);

    _directory.set(obj._id, obj);
    obj.__stop = function () {
        _directory.delete(obj._id);
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
    if (!_directory.has(actor._id)) {
        const msgPrintable = { execution: execution, context: context };
        console.error('Could not deliver message', msgPrintable, ' to actor ', actor._id, ' because it does not exist. Message will be delivered to the deadletter.');
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
    if (_directory.size === 0) {
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
}(async function (){function println(text){return console.log(text)}function vowel(char){return ['a','e','i','o','u',].indexOf(char.toLowerCase())>=0}function process(char){if(vowel(char)){return '';}if((typeof char=== 'string')){return char;}}function disemvowel(text){return text.split('').map(process).join('')}println(disemvowel('This website is for losers LOL!'));})();