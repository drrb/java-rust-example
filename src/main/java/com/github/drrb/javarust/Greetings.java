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

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public interface Greetings extends Library {
    String JNA_LIBRARY_NAME = "greetings";
    NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(JNA_LIBRARY_NAME);
    
    /**
     * JNA will load this library (a Rust crate) from the classpath.
     *
     * The location differs per platform.
     * E.g. on OSX, the library needs to be in /darwin/libgreetings.dylib
     * 
     * In this project, the crate source lives in 
     * src/main/rust/com/github/drrb/javarust/lib/greetings.rs . During the build,
     * Maven will run scripts/rust-compile.sh, which will compile the crate and
     * copy it into target/classes/&lt;platform-specific-name&gt;.
     */
    Greetings INSTANCE = (Greetings) Native.loadLibrary(JNA_LIBRARY_NAME, Greetings.class);

    /**
     * Passing a parameter to a Rust function
     */
    void printGreeting(String name);

    /**
     * Getting a return value back from Rust
     */
    String renderGreeting(String name);

    /**
     * Passing a struct into Rust
     */
    String greet(Person john);

    /**
     * Getting a pointer to a struct from Rust.
     * 
     * This is the same as returning a {@link Greeting.ByReference}. JNA assumes
     * it's by reference when it's returned from a native function.
     */
    Greeting getGreetingByReference();

    /**
     * Getting a struct back from Rust.
     * 
     * NB: we must specify that this is not a pointer (i.e. return an instance of
     * Greeting.ByValue instead of just Greeting) because JNA assumes that return
     * values that are structs are pointers unless we say otherwise.
     */
    Greeting.ByValue getGreetingByValue();

    /**
     * Getting a pointer to a struct that contains an array of structs.
     * 
     * This is the same as returning a {@link GreetingSet.ByReference}. JNA assumes
     * it's by reference when it's returned from a native function.
     */
    GreetingSet renderGreetings();

    /**
     * Passing a callback that will be called from Rust with individual strings
     */
    void callMeBack(GreetingCallback callback);

    /**
     * Passing a callback that will be called from Rust with an array of strings
     */
    void sendGreetings(GreetingSetCallback callback);

    /**
     * A callback function to pass to Rust
     *
     * @see #callMeBack
     */
    interface GreetingCallback extends Callback {

        void apply(String greeting);
    }

    /**
     * A callback function to pass to Rust
     *
     * @see #sendGreetings
     */
    interface GreetingSetCallback extends Callback {

        void apply(GreetingSet.ByReference greetingSet);
    }

    /**
     * Free the memory used by a Greeting
     */
    void dropGreeting(Greeting greeting);

    /**
     * Free the memory used by a GreetingSet
     */
    void dropGreetingSet(GreetingSet greetingSet);
}
