let _directory = new Map();

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
}