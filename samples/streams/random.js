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
}(async function (){function _StreamToken(value,complete){let self={};self.class='_StreamToken';self.value=value;self.complete=complete;return self;}function _StreamValue(value){return _StreamToken(value,false)}function _StreamComplete(){return _StreamToken('',true)}let _Provider={};(function(self){})(_Provider);async function _ProvideFromArray(data,index){let self=ENTITYCLASS('_ProvideFromArray',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.data=data;self.index=index;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.index>=self.data.length){return (await _StreamComplete())}else{let dataIndex=self.index;self.index+=1;let dataValue=self.data[dataIndex];return (await _StreamValue(dataValue))}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _ProvideFromFunction(previousValue,provider){let self=ENTITYCLASS('_ProvideFromFunction',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previousValue=previousValue;self.provider=provider;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){let current=(await provider(self.previousValue));self.previousValue=current;return (await _StreamValue(current))}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _Concat(previous,stream,isPreviousComplete,isCurrentComplete){let self=ENTITYCLASS('_Concat',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.stream=stream;self.isPreviousComplete=isPreviousComplete;self.isCurrentComplete=isCurrentComplete;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.isPreviousComplete&&self.isCurrentComplete){return (await _StreamComplete())}else{if(self.isPreviousComplete){let val=(await self.stream.poll());if(val.complete){self.isCurrentComplete=true;return (await _StreamComplete())}else{return val}}else{let val=(await self.previous.poll());if(val.complete){self.isPreviousComplete=true;return (await self.poll())}else{return val}}}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){self.previous.close();self.stream.close();return (await self.stop())}START();return self;}async function _Drop(previous,number,processed){let self=ENTITYCLASS('_Drop',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.number=number;self.processed=processed;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.processed<=self.number){self.previous.poll();self.processed+=1;return (await self.poll())}else{return (await self.previous.poll())}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _DropWhile(previous,condition,complete){let self=ENTITYCLASS('_DropWhile',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.condition=condition;self.complete=complete;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.complete){return (await self.previous.poll())}else{let token=(await self.previous.poll());if((VCE(token.complete,true))){self.complete=true;return (await _StreamComplete())}else{if((await self.condition(token.value))){self.complete=true;return token}else{return (await self.poll())}}}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _Filter(previous,fn){let self=ENTITYCLASS('_Filter',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.fn=fn;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){let val=(await self.previous.poll());if(val.complete){return (await _StreamComplete())}else{if((await self.fn(val.value))){return val}else{return (await self.poll())}}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _FlatMap(previous,currentStream,isCurrentComplete,fn){let self=ENTITYCLASS('_FlatMap',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.currentStream=currentStream;self.isCurrentComplete=isCurrentComplete;self.fn=fn;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.isCurrentComplete){self.currentStream.close();let newValue=(await self.previous.poll());if(newValue.complete){self.isCurrentComplete=true;return (await _StreamComplete())}else{self.isCurrentComplete=false;self.currentStream=(await self.fn(newValue.value));return (await self.pollCurrent())}}else{return (await self.pollCurrent())}}self.pollCurrent=ENQUEUEFN(self,'pollCurrent$', {});self.pollCurrent$=pollCurrent$;async function pollCurrent$(){let streamValue=(await self.currentStream.poll());if(streamValue.complete){self.isCurrentComplete=true;return (await self.poll())}else{self.isCurrentComplete=false;return streamValue}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){self.currentStream.close();return (await self.stop())}START();return self;}async function _Fold(previous,apply,current,complete){let self=ENTITYCLASS('_Fold',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.apply=apply;self.current=current;self.complete=complete;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.complete){return (await _StreamComplete())}else{let token=(await previous.poll());if(token.complete){self.complete=true;return (await _StreamValue(self.current))}else{self.current=(await self.apply(self.current,token.value));return (await self.poll())}}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){self.previous.close();return (await self.stop())}START();return self;}async function _ForEach(previous,apply){let self=ENTITYCLASS('_ForEach',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.apply=apply;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){let token=(await self.previous.poll());if((VCE(token.complete,false))){self.apply(token.value);return (await self.poll())}else{return (await _StreamComplete())}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _Map(previous,fn){let self=ENTITYCLASS('_Map',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.fn=fn;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){let prev=(await self.previous.poll());return (await self.process(prev))}self.process=ENQUEUEFN(self,'process$', {});self.process$=process$;async function process$(token){let value=token.value;let complete=token.complete;if(complete===true){return await _StreamComplete();}if(token.class==='_StreamToken'){return await _StreamValue((await self.fn(token.value)));}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _Take(previous,number,processed){let self=ENTITYCLASS('_Take',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.number=number;self.processed=processed;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.processed>=self.number){return (await _StreamComplete())}else{self.processed+=1;return (await self.previous.poll())}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _TakeWhile(previous,condition,complete){let self=ENTITYCLASS('_TakeWhile',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.condition=condition;self.complete=complete;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.complete){return (await _StreamComplete())}else{let token=(await previous.poll());if((VCE(token.complete,false))){if((await self.condition(token.value))){return token}else{self.complete=true;return (await _StreamComplete())}}else{self.complete=true;return (await _StreamComplete())}}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _WaitForResult(previous){let self=ENTITYCLASS('_WaitForResult',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){let token=(await previous.poll());if(token.complete){return []}else{return (await [token.value,].concat((await self.poll())))}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){return (await self.stop())}START();return self;}async function _ZipFirst(previous,other,complete){let self=ENTITYCLASS('_ZipFirst',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.other=other;self.complete=complete;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if(self.complete){return (await _StreamComplete())}else{let all=(await Promise.all([self.previous,self.other,].map(async function(a){return (await a.poll())})));let left=all[0];let right=all[1];if(left.complete||right.complete){self.complete=true;return (await _StreamComplete())}else{return (await _StreamValue([left.value,right.value,]))}}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){self.previous.close();self.other.close();return (await self.stop())}START();return self;}function println(text){return console.log(text)}async function _ZipLast(previous,other,leftCompleted,rightCompleted,leftDefault,rightDefault){let self=ENTITYCLASS('_ZipLast',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.other=other;self.leftCompleted=leftCompleted;self.rightCompleted=rightCompleted;self.leftDefault=leftDefault;self.rightDefault=rightDefault;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){if((await self.complete())){return (await _StreamComplete())}else{let all=(await Promise.all([self.previous,self.other,].map(async function(a){return (await a.poll())})));let left=all[0];let right=all[1];self.whenComplete(left,function(){return self.leftCompleted=true});self.whenComplete(right,function(){return self.rightCompleted=true});if((await self.complete())){return (await _StreamComplete())}else{return (await _StreamValue([(await self.valueOrDefault(left,self.leftDefault)),(await self.valueOrDefault(right,self.rightDefault)),]))}}}self.complete=ENQUEUEFN(self,'complete$', {});self.complete$=complete$;async function complete$(){return self.leftCompleted&&self.rightCompleted}self.valueOrDefault=ENQUEUEFN(self,'valueOrDefault$', {});self.valueOrDefault$=valueOrDefault$;async function valueOrDefault$(token, defaultVal){let value=token.value;let complete=token.complete;if(complete===true&&true){return defaultVal;}if(token.class==='_StreamToken'&&true){return token.value;}}self.whenComplete=ENQUEUEFN(self,'whenComplete$', {});self.whenComplete$=whenComplete$;async function whenComplete$(token, apply){let value=token.value;let complete=token.complete;if(complete===true&&apply.apply!==undefined){return await apply();}if(token.class==='_StreamToken'&&apply.apply!==undefined){return ({})}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){self.previous.close();self.other.close();return (await self.stop())}START();return self;}async function _ZipWithIndex(previous,count){let self=ENTITYCLASS('_ZipWithIndex',['_Provider']);self.stop=ENQUEUEFN(self,'__stop', {});self.previous=previous;self.count=count;self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){let val=(await self.previous.poll());let index=self.count;self.count+=1;if(val.complete){return (await _StreamComplete())}else{return (await _StreamValue([val.value,index,]))}}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){self.previous.close();return (await self.stop())}START();return self;}function Range(from, to){if(from>=to){return [];}else{return [from,].concat(Range(from+1,to));}}let Stream={};(function(self){self.fromArray=async function fromArray(array){return (await _PollBasedStream([(await _ProvideFromArray(array,0)),]))};self.fromSupplier=async function fromSupplier(firstValue, fn){return (await _PollBasedStream([(await _ProvideFromFunction(firstValue,fn)),]))};self.fromRange=async function fromRange(from, to){return (await Stream.fromArray(Range(from,to)))};})(Stream);async function _PollBasedStream(chain){let self=ENTITYCLASS('_PollBasedStream',['_Provider','Stream']);self.stop=ENQUEUEFN(self,'__stop', {});self.chain=chain;self.array=ENQUEUEFN(self,'array$', {});self.array$=array$;async function array$(){self.chain.push((await _WaitForResult((await self.last()))));let result=(await (await self.last()).poll());self.close();return result}self.fold=ENQUEUEFN(self,'fold$', {});self.fold$=fold$;async function fold$(initial, apply){self.chain.push((await _Fold((await self.last()),apply,initial,false)));let result=(await (await self.last()).poll());self.close();return result.value}self.forEach=ENQUEUEFN(self,'forEach$', {});self.forEach$=forEach$;async function forEach$(apply){self.chain.push((await _ForEach((await self.last()),apply)));let _=(await (await self.last()).poll());return (await self.close())}self.drop=ENQUEUEFN(self,'drop$', {});self.drop$=drop$;async function drop$(num){self.chain.push((await _Drop((await self.last()),num,0)));return self}self.take=ENQUEUEFN(self,'take$', {});self.take$=take$;async function take$(num){self.chain.push((await _Take((await self.last()),num,0)));return self}self.dropWhile=ENQUEUEFN(self,'dropWhile$', {});self.dropWhile$=dropWhile$;async function dropWhile$(condition){self.chain.push((await _DropWhile((await self.last()),condition,false)));return self}self.takeWhile=ENQUEUEFN(self,'takeWhile$', {});self.takeWhile$=takeWhile$;async function takeWhile$(condition){self.chain.push((await _TakeWhile((await self.last()),condition,false)));return self}self.concat=ENQUEUEFN(self,'concat$', {});self.concat$=concat$;async function concat$(stream){self.chain.push((await _Concat((await self.last()),stream,false,false)));return self}self.map=ENQUEUEFN(self,'map$', {});self.map$=map$;async function map$(fn){self.chain.push((await _Map((await self.last()),fn)));return self}self.flatMap=ENQUEUEFN(self,'flatMap$', {});self.flatMap$=flatMap$;async function flatMap$(fn){self.chain.push((await _FlatMap((await self.last()),(await Stream.fromArray([])),true,fn)));return self}self.zipFirst=ENQUEUEFN(self,'zipFirst$', {});self.zipFirst$=zipFirst$;async function zipFirst$(other){self.chain.push((await _ZipFirst((await self.last()),other,false)));return self}self.zipLast=ENQUEUEFN(self,'zipLast$', {});self.zipLast$=zipLast$;async function zipLast$(other, defaultLeft, defaultRight){self.chain.push((await _ZipLast((await self.last()),other,false,false,defaultLeft,defaultRight)));return self}self.zipWithIndex=ENQUEUEFN(self,'zipWithIndex$', {});self.zipWithIndex$=zipWithIndex$;async function zipWithIndex$(){self.chain.push((await _ZipWithIndex((await self.last()),0)));return self}self.filter=ENQUEUEFN(self,'filter$', {});self.filter$=filter$;async function filter$(predicate){self.chain.push((await _Filter((await self.last()),predicate)));return self}self.poll=ENQUEUEFN(self,'poll$', {});self.poll$=poll$;async function poll$(){return (await (await self.last()).poll())}self.close=ENQUEUEFN(self,'close$', {});self.close$=close$;async function close$(){self.chain.forEach(async function(a){return (await a.close())});return (await self.stop())}self.last=ENQUEUEFN(self,'last$', {});self.last$=last$;async function last$(){return self.chain[self.chain.length-1]}START();return self;}function printRandomValue(v){let index=v[1];let value=v[0];if(value&&index){return println(({index:index,value:value}));}if(v.slice!==undefined){return 0;}}(await (await (await Stream.fromSupplier(1,Math.random)).zipWithIndex()).take(10)).forEach(printRandomValue);})();