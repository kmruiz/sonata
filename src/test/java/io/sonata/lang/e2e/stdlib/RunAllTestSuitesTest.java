/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e.stdlib;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.sonata.lang.e2e.NodeDockerTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class RunAllTestSuitesTest extends NodeDockerTest {
    private static final Gson GSON = new Gson();

    @Test
    public void std_lib_stream() throws Exception {
        assertTestsRunSuccessfully("stream");
    }

    @Test
    public void std_lib_io_file() throws Exception {
        assertTestsRunSuccessfully("io/file");
    }

    private void assertTestsRunSuccessfully(String module) throws Exception {
        String output = runScriptAndGetOutput("/lib/std/" + module + "_test");
        TestSuiteResult result = null;
        try {
            result = GSON.fromJson(output, TestSuiteResult.class);
        } catch (JsonSyntaxException e) {
            fail("We couldn't run the tests. The output has been: " + output);
            return;
        }

        reportResult(module, result);

        if (result.failed) {
            fail("At least one of the assertions failed. Please check the report for more information.");
        }
    }

    private void reportResult(String module, TestSuiteResult result) {
        System.out.println("================================================================================");
        System.out.println("[ Report for module '" + module + "']");
        System.out.println("================================================================================");
        result.tests.forEach(test -> {
            if (test.failed) {
                System.err.println("✘ " + test.test);
                test.assertions.forEach(assertion -> {
                    if (!assertion.success) {
                        System.err.println("\t ✘ " + assertion.message);
                    } else {
                        System.out.println("\t ✔ " + assertion.message);
                    }
                });
            } else {
                System.out.println("✔ " + test.test);
            }
        });
    }

    private static class AssertionStatus {
        public String message;
        public boolean success;
    }
    private static class TestResult {
        public String test;
        public List<AssertionStatus> assertions;
        public boolean failed;
    }
    private static class TestSuiteResult {
        public String suite;
        public List<TestResult> tests;
        public boolean failed;
    }
}
