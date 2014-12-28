/*
 * Copyright (C) 2014 drrb
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
#![crate_type = "dylib"]

extern crate libc;

use std::mem;
use std::c_str::CString;
use libc::{c_int,c_char};

#[repr(C)]
pub struct GreetingSet {
    greetings: Box<[Greeting]>,
    number_of_greetings: c_int
}

#[repr(C)]
pub struct Greeting {
    text: *const c_char
}

impl Greeting {
    fn new(string: &str) -> Greeting {
        Greeting { text: unsafe { string.to_string().to_c_str().into_inner() } }
    }
}

//TODO: is this annotation required?
#[repr(C)]
pub struct Person {
    first_name: *const c_char,
    last_name: *const c_char
}

/// Example of just calling into Rust
#[no_mangle] // "no_mangle", so that our Java code can still see the Rust function after it's compiled
#[allow(non_snake_case)]
pub extern fn printGreeting(name: CString) {
    // Convert the C string to a Rust one
    let name = from_c_str(&name);
    println!("Hello, {}", name);
}

/// Example of passing and returning a value
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn renderGreeting(name: CString) -> CString {
    let name = from_c_str(&name);

    // Convert the Rust string back to a C string so that we can return it
    format!("Hello, {}!", name).to_c_str()
}

/// Example of passing a callback
#[no_mangle]
#[cfg(not(windows))]
#[allow(non_snake_case)]
pub extern fn callMeBack(callback: extern "C" fn(CString)) { // The function argument here is an "extern" one, so that we can pass it in from Java
    // Call the Java method
    callback("Hello there!".to_c_str());
}

/// Example of passing a callback (Windows version)
#[no_mangle]
#[cfg(windows)]
#[allow(non_snake_case)]
pub extern fn callMeBack(callback: extern "stdcall" fn(CString)) { // "stdcall" is the calling convention Windows uses
    callback("Hello there!".to_c_str());
}

/// Example of passing a struct to Rust
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn greet(person: &Person) -> *const c_char {
    let first_name = unsafe { from_c_str(&CString::new(person.first_name, true)) };
    let last_name = unsafe { from_c_str(&CString::new(person.last_name, true)) };
    //TODO: how do we get the last name too? (this line segfaults)
    //let last_name = from_c_str(&person.last_name);
    format!("Hello, {} {}!", first_name, last_name).to_c_str().as_ptr()
}

/// Example of returning a struct from Rust by value
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn getGreetingByValue() -> Greeting {
    Greeting::new("Hello from Rust!")
}

/// Example of returning a struct from Rust by reference
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn getGreetingByReference() -> Box<Greeting> {
    box Greeting::new("Hello from Rust!")
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern fn sendGreetings(callback: extern "C" fn(Box<GreetingSet>)) { // The function argument here is an "extern" one, so that we can pass it in from Java
    let greetings = vec![ Greeting::new("Hello!"), Greeting::new("Hello again!") ];
    let num_greetings = greetings.len();

    let set = box GreetingSet {
        // Get a raw pointer to the vector, so that we can pass it back to Java
        greetings: greetings.into_boxed_slice(),
        // Also return the length of the array, so that we can create the array back in Java
        number_of_greetings: num_greetings as c_int
    };
    callback(set);
}

/// Example of returning a struct from Rust
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn renderGreetings() -> Box<GreetingSet> {
    let greetings = vec![ Greeting::new("Hello!"), Greeting::new("Hello again!") ];
    let num_greetings = greetings.len();

    box GreetingSet {
        greetings: greetings.into_boxed_slice(),
        number_of_greetings: num_greetings as c_int
    }
}

fn from_c_str(c_string: &CString) -> String {
    c_string.as_str().expect("Couldn't get string from C-string").to_string()
}