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
import static com.github.drrb.javarust.test.Matchers.*;
import com.github.drrb.javarust.test.MethodPrintingRule;
import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
        Greeting greeting = library.getGreetingByValue();
        assertThat(greeting.text, is("Hello from Rust!"));
    }

    @Test
    public void shouldGetAStructFromRustByReference() {
        Greeting greeting = library.getGreetingByReference();

        assertThat(greeting.text, is("Hello from Rust!"));

        // Free the memory after using it. We need to do this because JNA assumes
        // that the memory is owned by Rust, so Rust must clean it up.
        library.dropGreeting(greeting);
    }

    @Test
    public void shouldGetAStringFromRustInACallback() {
        final List<String> greetings = new LinkedList<>();
        library.callMeBack(new GreetingCallback() {
            public void apply(String greeting) {
                greetings.add(greeting);
            }
        });
        assertThat(greetings, is(asList("Hello there!")));
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
        for (Greeting greeting: greetings) {
            greetingStrings.add(greeting.getText());
        }

        assertThat(greetingStrings, is(asList("Hello!", "Hello again!")));
        for (Greeting greeting: greetings) {
            library.dropGreeting(greeting);
        }
    }

    @Test
    public void shouldGetAStructFromRustContainingAnArrayOfStructs() {
        GreetingSet result = library.renderGreetings();
        List<String> greetings = new LinkedList<>();
        for (Greeting greeting: result.getGreetings()) {
            greetings.add(greeting.getText());
        }

        assertThat(greetings, is(asList("Hello!", "Hello again!")));
        library.dropGreetingSet(result);
    }
}
