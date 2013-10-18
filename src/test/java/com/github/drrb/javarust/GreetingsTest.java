/*
 * Copyright (C) 2013 drrb
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
import com.sun.jna.Pointer;
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
    @Ignore("Causes a segmentation fault on Linux")
    public void shouldGreetAPerson() {
        Person john = new Person.ByReference();
        john.firstName = "John";
        john.lastName = "Smith";
        String greeting = library.greet(john);
        assertThat(greeting, is("Hello, John!"));
    }

    @Test
    @Ignore
    public void shouldRenderListOfGreetings() {
        GreetingSet result = library.renderGreetings();
        List<String> greetings = result.getGreetings();

        assertThat(greetings, is(asList("Hello!", "Hello again!")));
    }

    @Test
    @Ignore
    public void shouldRenderGreetingsInTasks() throws Exception {
        GreetingSet result = Greetings.INSTANCE.renderGreetingsInParallel(5, "World");

        System.out.println("received " + result.numberOfGreetings + " greetings");

        String dump = result.greetings.dump(0, 10);
        System.out.println("dump = " + dump);

        Pointer[] pointerArray = result.greetings.getPointerArray(0, result.numberOfGreetings);
        System.out.println("pointerArray = " + pointerArray);

        System.out.println("pointerArray[0] = " + pointerArray[0].getString(0));

        List<String> greetings = result.getGreetings();
//        assertThat(greetings, hasSize(5));
//
//        for (int i = 0; i < result.numberOfGreetings; i++) {
//            assertThat(greetings.get(0), matchesRegex("Greeting number \\d for World"));
//        }
    }
}
