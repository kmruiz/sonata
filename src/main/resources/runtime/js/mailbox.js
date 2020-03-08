/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

let _mailbox = [];
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

    const message = _mailbox.pop();
    const actor = message.actor;
    const execution = message.execution;
    const context = message.context;
    if (!_directory.has(actor._id)) {
        const msgPrintable = { execution: execution, context: context };
        console.error('Could not deliver message', msgPrintable, ' to actor ', actor._id, ' because it does not exist. Message will be delivered to the deadletter.');
        _deadletter.push(message);
    }

    DELIVER(message);
}

function DELIVER(message) {
    const entity = message.actor;
    entity.__context = message.context;
    const execution = message.execution;
    entity[execution.method].apply(null, execution.arguments);
}

function ENQUEUEFN(self, method, frame) {
    return function () {
        const args = Array.prototype.slice.apply(arguments);
        const execution = { method: method, arguments: args };
        const context = Object.assign({}, self.__context);
        PUSHFRAME(frame, context);
        _mailbox.push({ actor: self, context: context, execution: execution });
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
}