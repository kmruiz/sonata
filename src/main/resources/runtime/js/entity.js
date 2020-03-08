function ENTITYCLASS(className, contracts) {
    let obj = {};
    obj._id = className + (+(new Date())) + Math.random();
    obj.class = className;
    obj.contracts = contracts;
    obj.__context = { stacktrace: [] };
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
}