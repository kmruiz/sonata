"use strict";

function _() {
    let y;
    const x = new Promise(r => {
        y = r;
    });
    return [x, y];
}

function pinger() {
    let self = {};
    self.class = 'pinger';
    self._mailbox = [];
    self._interval = setInterval(function () {
        if (self._mailbox.length > 0) {
            const todo = self._mailbox.shift();
            todo();
        }
    }, 0);
    self.stop = function () {
        clearInterval(self._interval);
    };
    self.ping = function ping(ponger, time) {
        const args = Array.prototype.slice.call(arguments);
        const vs = _();
        const p = vs[0];
        const r = vs[1];
        self._mailbox.push(function () {
            ping$.apply(r, args);
        });
        return p;
    };

    async function ping$(ponger, time) {
        if (time == 10) {
            return (async function () {
                console.log('ping end');
                ponger.pong(self, time);
                self.stop();
            })();
        }
        const r$ = (async function () {
            console.log(time + '> ping');
            ponger.pong(self, time);
        })();
        this(r$);
        return r$;
    }

    return self;
}

function ponger() {
    let self = {};
    self.class = 'ponger';
    self._mailbox = [];
    self._interval = setInterval(function () {
        if (self._mailbox.length > 0) {
            const todo = self._mailbox.shift();
            todo();
        }
    }, 0);
    self.stop = function () {
        clearInterval(self._interval);
    };
    self.pong = function pong(pinger, time) {
        const args = Array.prototype.slice.call(arguments);
        const vs = _();
        const p = vs[0];
        const r = vs[1];
        self._mailbox.push(function () {
            pong$.apply(r, args);
        });
        return p;
    };

    async function pong$(pinger, time) {
        if (time == 10) {
            return (async function () {
                console.log('pong end');
                self.stop();
            })();
        }
        const r$ = (async function () {
            console.log(time + '> pong');
            pinger.ping(self, time + 1);
        })();
        this(r$);
        return r$;
    }

    return self;
}

let _pinger = pinger();
let _ponger = ponger();
_pinger.ping(_ponger, 0);
console.log('doing things asynchronously');