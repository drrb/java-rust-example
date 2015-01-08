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

import static com.github.drrb.javarust.test.Matchers.*;
import com.github.drrb.javarust.test.MethodPrintingRule;
import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;
import static java.util.stream.Collectors.toList;
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
    }

    @Test
    public void shouldGetAStringFromRustInACallback() {
        final List<String> greetings = new LinkedList<>();
        library.callMeBack((greeting) -> {
            greetings.add(greeting);
        });
        assertThat(greetings, is(asList("Hello there!")));
    }

    @Test
    public void shouldGetListOfStringsFromRustInACallback() {
        final List<Greeting> greetings = new LinkedList<>();
        library.sendGreetings((greetingSet) -> {
            greetings.addAll(greetingSet.getGreetings());
        });
        List<String> greetingStrings = greetings.stream().map(Greeting::getText).collect(toList());

        assertThat(greetingStrings, is(asList("Hello!", "Hello again!")));
    }

    @Test
    public void shouldGetAStructFromRustContainingAnArrayOfStructs() {
        GreetingSet result = library.renderGreetings();
        List<String> greetings = result.getGreetings()
                .stream()
                .map(Greeting::getText)
                .collect(toList());

        assertThat(greetings, is(asList("Hello!", "Hello again!")));
    }
}
