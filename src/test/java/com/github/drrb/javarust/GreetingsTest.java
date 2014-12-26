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
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Ignore;
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
    public void shouldPrintGreetingWithoutCrashing() {
        library.printGreeting("World");
    }

    @Test
    public void shouldRenderGreeting() {
        String greeting = library.renderGreeting("World");

        assertThat(greeting, is("Hello, World!"));
    }

    @Test
    public void shouldCallMeBack() {
        final List<String> greetings = new LinkedList<>();
        library.callMeBack(new Greetings.GreetingCallback() {

            @Override
            public void apply(String greeting) {
                greetings.add(greeting);
            }
        });
        assertThat(greetings, is(asList("Hello there!")));
    }

    @Test
    @Ignore
    /**
     * Gives error every couple of times:
     * java(13103,0x10335f000) malloc: *** error for object 0x11cb27dd0: pointer being freed was not allocated
     */
    public void shouldSendAStructToRust() {
        Person john = new Person.ByValue();
        john.firstName = "John";
        john.lastName = "Smith";
        String greeting = library.greet(john);
        assertThat(greeting, is("Hello, John!"));
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
    public void shouldGetListOfGreetingsInACallback() {
        final List<String> greetings = new LinkedList<>();
        library.sendGreetings(new Greetings.GreetingSetCallback() {

            @Override
            public void apply(GreetingSet.ByReference greetingSet) {
                greetings.addAll(greetingSet.getGreetings());
            }

        });

        assertThat(greetings, is(asList("Hello!", "Hello again!")));
    }

    @Test
    @Ignore
    /**
     * Causes a segfault
     */
    public void shouldRenderListOfGreetings() {
        GreetingSet result = library.renderGreetings();
        List<String> greetings = result.getGreetings();

        assertThat(greetings, is(asList("Hello!", "Hello again!")));
    }
}
