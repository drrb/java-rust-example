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

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public interface Greetings extends Library {

    /**
     * JNA will load a libarary with this name from the classpath.
     *
     * E.g. on OSX, the library needs to be in /darwin/libgreetings.dylib
     */
    String JNA_LIBRARY_NAME = "greetings";
    NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(JNA_LIBRARY_NAME);
    Greetings INSTANCE = (Greetings) Native.loadLibrary(JNA_LIBRARY_NAME, Greetings.class);

    /**
     * A callback function to pass to Rust
     *
     * @see #callMeBack
     */
    interface GreetingCallback extends Callback {

        void apply(String greeting);
    }

    void printGreeting(String name);

    String renderGreeting(String name);

    void callMeBack(GreetingCallback callback);

    String greet(Person john);

    GreetingSet renderGreetings();

    GreetingSet renderGreetingsInParallel(int numberOfGreetings, String name);

}
