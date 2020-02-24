/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e.validation;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class ContractsTest extends E2ETest {
    @Test
    public void canNotBeRedefined() {
        assertSyntaxError("Can not redefine a contract, however, contract 'Pinger' has been already defined", "validation/contracts/must-not-be-redefined");
    }

    @Test
    public void canOnlyContainContractsWithLetDeclarations() {
        assertSyntaxError("Contracts only allow let function declarations.", "validation/contracts/must-not-contain-let-variables");
    }

    @Test
    public void canNotContainContractsWithMethodDefinitions() {
        assertSyntaxError("Contracts only allow let function declarations, meaning let functions without body.", "validation/contracts/must-not-contain-let-definitions");
    }

    @Test
    public void canNotImplementAContractWithoutImplementingMethods() {
        assertSyntaxError("To implement a contract, you must implement all methods defined. Missing method 'ping' definition in contract 'Pinger'", "validation/contracts/must-implement-all-contract-methods");
    }

    @Test
    public void canNotImplementAContractWithoutImplementingMethodsFromParentContracts() {
        assertSyntaxError("To implement a contract, you must implement all methods defined. Missing method 'ping' definition in contract 'Game'", "validation/contracts/must-implement-all-multiple-contracts");
    }
}
