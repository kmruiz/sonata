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

function ENQUEUEFN(self, contract, method) {
    return function () {
        return new Promise((resolve, reject) => {
            const args = Array.prototype.slice.apply(arguments);
            const execution = { method: method, arguments: args, resolve: resolve, reject: reject };
            const context = { stacktrace: _globalST };
            PUSHFRAME(self.frames[contract], context);

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
    Array.prototype.slice.apply(_directory.values()).forEach(function (e) { e.stop() });
    setTimeout(function () {
        console.error('Could not stop in less than 1 second. Hard exit.');
        END();
    }, 1000);
}