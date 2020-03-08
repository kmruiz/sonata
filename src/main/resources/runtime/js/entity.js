function ENTITYCLASS(className, contracts) {
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
}