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
#[crate_id = "greetings#0.1"];
#[crate_type = "lib"];

use std::ptr;
use std::c_str::CString;
use std::libc::c_int;

mod rust;

pub struct GreetingSet {
    greetings: *CString,
    number_of_greetings: c_int
}

pub struct Greeting {
    text: CString
}

pub struct Person {
    first_name: CString,
    last_name: CString
}

/// Example of starting the runtime
#[no_mangle] // "no_mangle", so that our Java code can still see the Rust funciton after it's compiled
pub extern fn printGreeting(name: CString) {
    // Convert the C string to a Rust one
    let name = from_c_str(name);

    // Start the runtime so that we can use the IO library
    do rust::run {
        println(format!("Hello, {}", name));
    }
}

/// Example of passing and returning a value
#[no_mangle]
pub extern fn renderGreeting(name: CString) -> CString {
    let name = from_c_str(name);

    // Convert the Rust string back to a C string so that we can return it
    ("Hello, " + name + "!").to_c_str()
}

/// Example of passing a callback
#[no_mangle]
#[cfg(not(windows))]
pub extern fn callMeBack(callback: extern "C" fn(CString)) { // The function argument here is an "extern" one, so that we can pass it in from Java
    // Call the Java method
    callback("Hello there!".to_c_str());
}

/// Example of passing a callback (Windows version)
#[no_mangle]
#[cfg(windows)]
pub extern fn callMeBack(callback: extern "stdcall" fn(CString)) { // "stdcall" is the calling convention Windows uses
    callback("Hello there!".to_c_str());
}

/// Example of passing a struct to Rust
#[no_mangle]
pub extern fn greet(person_ptr: *Person) -> CString {
    // Read the raw pointer as a struct
    let person = unsafe { ptr::read_ptr(person_ptr) };
    let first_name = from_c_str(person.first_name);
    //TODO: how do we get the last name too, without being able to clone the Person?
    ("Hello, " + first_name + "!").to_c_str()
}

/// Example of returning a struct from Rust by value
#[no_mangle]
pub extern fn getGreetingByValue() -> Greeting {
    Greeting { text: "Hello from Rust!".to_c_str() }
}

/// Example of returning a struct from Rust by reference
#[no_mangle]
pub extern fn getGreetingByReference() -> ~Greeting {
    // Make sure we forget the pointer, otherwise, we get "java(34950,0x11b25b000) malloc: *** error for object 0x7fa535419260: pointer being freed was not allocated"
    do rust::run {
        ~Greeting { text: "Hello from Rust!".to_c_str() }
    }
}

/// Example of returning a struct from Rust
#[no_mangle]
pub extern fn renderGreetings() -> ~GreetingSet {
    let greetings = [ "Hello!".to_c_str(), "Hello again!".to_c_str() ];

    do rust::run {
        ~GreetingSet {
            // Get a raw pointer to the vector, so that we can pass it back to Java
            greetings: greetings.as_ptr(),
            // Also return the length of the array, so that we can create the array back in Java
            number_of_greetings: greetings.len() as c_int
        }
    }
}

#[no_mangle]
pub extern fn sendGreetings(callback: extern "C" fn(~GreetingSet)) { // The function argument here is an "extern" one, so that we can pass it in from Java
    let greetings = [ "Hello!".to_c_str(), "Hello again!".to_c_str() ];

    do rust::run {
        let set = ~GreetingSet {
            // Get a raw pointer to the vector, so that we can pass it back to Java
            greetings: greetings.as_ptr(),
            // Also return the length of the array, so that we can create the array back in Java
            number_of_greetings: greetings.len() as c_int
        };
        callback(set);
    };
}

fn from_c_str(c_string: CString) -> ~str {
    match c_string.as_str() {
        Some(string) => string.to_owned(),
        None => fail!("Couldn't get string from C-string")
    }
}
