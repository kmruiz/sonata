"use strict";console.log('f(1)',f(1));console.log('f(2)',f(2));console.log('f(3)',f(3));console.log('f(4)',f(4));console.log('f(5)',f(5));function f(a){if(a<=1){return 1;}return f(a-1)+f(a-2)};