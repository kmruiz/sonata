"use strict";
console.log(say(['hello', 'John']));
console.log(say(['bye', 'Doe']));

function say(command) {
    return (function () {
        var a = command[0];
        var whom = command[1];
        var a = command[0];
        var whom = command[1];
        if (a === 'bye') {
            return 'Bye ' + whom + '. I am really sad.'
        }
        if (a === 'hello') {
            return 'Hello ' + whom + '!'
        }
        return undefined;
    })();
};