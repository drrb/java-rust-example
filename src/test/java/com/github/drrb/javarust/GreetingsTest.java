/*
 * Copyright (C) 2015 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.drrb.javarust;

import com.github.drrb.javarust.Greetings.GreetingCallback;
import com.github.drrb.javarust.Greetings.GreetingSetCallback;
import com.github.drrb.javarust.test.MethodPrintingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.drrb.javarust.test.Matchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class GreetingsTest {

    @Rule
    public final MethodPrintingRule printer = new MethodPrintingRule();
    private Greetings library;

    @Before
    public void setUp() {
        library = Greetings.INSTANCE;
    }

    @Test
    public void shouldAcceptStringParameterFromJavaToRust() {
        library.printGreeting("World"); //Printed in the console
    }

    @Test
    public void shouldAcceptStringFromJavaToRustAndReturnAnotherOne() {
        String greeting = library.renderGreeting("World");
        assertThat(greeting, is("Hello, World!"));
    }

    @Test
    public void shouldAcceptAStructFromJavaToRust() {
        Person john = new Person();
        john.firstName = "John";
        john.lastName = "Smith";
        String greeting = library.greet(john);
        assertThat(greeting, is("Hello, John Smith!"));
    }

    @Test
    public void shouldGetAStructFromRustByValue() {
        // Using try-with-resources so that memory gets cleaned up. See Greeting.close()
        try (Greeting greeting = library.getGreetingByValue()) {
            assertThat(greeting.text, is("Hello from Rust!"));
        }
    }

    @Test
    public void shouldGetAStructFromRustByReference() {
        try (Greeting greeting = library.getGreetingByReference()) {
            assertThat(greeting.text, is("Hello from Rust!"));
        }
    }

    @Test
    public void shouldGetAStringFromRustInACallback() {
        final List<String> greetings = new LinkedList<>();
        library.callMeBack(new GreetingCallback() {
            public void apply(String greeting) {
                greetings.add(greeting);
            }
        });
        assertThat(greetings, contains("Hello there!"));
    }

    @Test
    public void shouldGetListOfStringsFromRustInACallback() {
        final List<Greeting> greetings = new LinkedList<>();
        library.sendGreetings(new GreetingSetCallback() {
            public void apply(GreetingSet.ByReference greetingSet) {
                greetings.addAll(greetingSet.getGreetings());
            }
        });

        List<String> greetingStrings = new LinkedList<>();
        for (Greeting greeting : greetings) {
            greetingStrings.add(greeting.getText());
        }

        assertThat(greetingStrings, contains("Hello!", "Hello again!"));
    }

    @Test
    public void shouldGetAStructFromRustContainingAnArrayOfStructs() {
        try (GreetingSet result = library.renderGreetings()) {
            List<String> greetings = new LinkedList<>();
            for (Greeting greeting : result.getGreetings()) {
                greetings.add(greeting.getText());
            }

            assertThat(greetings, contains("Hello!", "Hello again!"));
        }
    }
}
