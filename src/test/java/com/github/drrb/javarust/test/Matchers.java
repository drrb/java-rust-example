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
package com.github.drrb.javarust.test;

import java.util.Collection;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 *
 */
public class Matchers extends org.hamcrest.Matchers {

    public static Matcher<String> matchesRegex(final String regex) {
        return new TypeSafeMatcher<String>() {

            @Override
            protected boolean matchesSafely(String string) {
                return string.matches(regex);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("String matching regex ").appendValue(regex);
            }
        };
    }

    public static Matcher<Collection<?>> hasSize(final int expectedSize) {
        return new TypeSafeMatcher<Collection<?>>() {

            @Override
            protected boolean matchesSafely(Collection<?> item) {
                return item.size() == expectedSize;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Collection of size ").appendValue(expectedSize);
            }
        };
    }
}
