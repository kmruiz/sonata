/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e.stdlib;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.sonata.lang.e2e.EndToEndTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class RunAllTestSuitesTest extends EndToEndTest {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";

    private static final Gson GSON = new Gson();

    @Test
    public void std_lib_stream() throws Exception {
        assertTestsRunSuccessfully("/lib/std/stream_test.sn");
    }

    @Test
    public void std_lib_io_file() throws Exception {
        assertTestsRunSuccessfully("/lib/std/io/file_test.sn");
    }

    private void assertTestsRunSuccessfully(String module) throws Exception {
        String output = runScriptAndGetOutput(module);
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
                System.out.println(ANSI_RED + "✘ " + test.test + ANSI_RESET);
                test.assertions.forEach(assertion -> {
                    if (!assertion.success) {
                        System.out.println(ANSI_RED + "\t ✘ " + assertion.message + ANSI_RESET);
                    } else {
                        System.out.println(ANSI_GREEN + "\t ✔ " + assertion.message + ANSI_RESET);
                    }
                });
            } else {
                System.out.println(ANSI_GREEN + "✔ " + test.test + ANSI_RESET);
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
