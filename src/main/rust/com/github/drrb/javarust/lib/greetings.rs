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
#![crate_type = "dylib"]

extern crate libc;

use std::ffi::CString;
use std::ffi;
use std::str;
use std::mem;
use libc::{c_int,c_char};

// This suppresses compiler warnings: Rustc thinks we don't use it because it's actually only used from Java
#[allow(dead_code)]
// This makes sure the structs are represented in memory in a way JNA can read them
#[repr(C)]
// GreetingSet corresponds to com.github.drrb.javarust.GreetingSet in Java
pub struct GreetingSet {
    greetings: Box<[Greeting]>, // This is converted to a Greeting.ByReference by JNA
    number_of_greetings: c_int  // c_int is converted to a Java int by JNA
}

#[allow(dead_code)]
#[repr(C)]
// Greeting corresponds to com.github.drrb.javarust.Greeting in Java
pub struct Greeting {
    // A pointer to the beginning of a string. Converted to a Java String by JNA
    text: *const c_char
}

impl Greeting {
    fn new(string: &str) -> Greeting {
        Greeting { text: data(string) }
    }
}

#[repr(C)]
pub struct Person {
    first_name: *const c_char,
    last_name: *const c_char
}

/// Example of just calling into Rust
#[no_mangle] // "no_mangle", so that our Java code can still see the Rust function after it's compiled
#[allow(non_snake_case)] // Names must match Java names, which are camelCased, so tell rustc not to complain
pub extern fn printGreeting(name: *const c_char) {
    // Convert the C string to a Rust one
    let name = to_string(&name);
    println!("Hello, {}", name);
}

/// Example of passing and returning a value
#[no_mangle]
#[allow(non_snake_case)]
// Argument pointer originating in Java (*const c_char) is a Rust *reference* (i.e. it's borrowed from Java)
pub extern fn renderGreeting(name: *const c_char) -> *const c_char {  // Returning a string as a pointer
    let name = to_string(&name);

    // Convert the Rust string back to a C string so that we can return it
    to_ptr(format!("Hello, {}!", name))
}

/// Example of passing a callback
#[no_mangle]
#[cfg(not(windows))]
#[allow(non_snake_case)]
pub extern fn callMeBack(callback: extern "C" fn(*const c_char)) { // The function argument here is an "extern" one, so that we can pass it in from Java
    // Call the Java method
    callback(data("Hello there!"));
}

/// Example of passing a callback (Windows version)
#[no_mangle]
#[cfg(windows)]
#[allow(non_snake_case)]
pub extern fn callMeBack(callback: extern "stdcall" fn(*const c_char)) { // "stdcall" is the calling convention Windows uses
    callback(data("Hello there!"));
}

/// Example of passing a struct to Rust
#[no_mangle]
pub extern fn greet(person: &Person) -> *const c_char {
    let first_name = to_string(&person.first_name);
    let last_name = to_string(&person.last_name);
    to_ptr(format!("Hello, {} {}!", first_name, last_name))
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
pub extern fn getGreetingByReference() -> Box<Greeting> { // Return an owned pointer to the struct. The Java code now "owns" the pointer.
    box Greeting::new("Hello from Rust!")
}

/// More complicated callback example
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn sendGreetings(callback: extern "C" fn(Box<GreetingSet>)) { // The function argument here is an "extern" one, so that we can pass it in from Java
    let greetings = vec![ Greeting::new("Hello!"), Greeting::new("Hello again!") ];
    let num_greetings = greetings.len();

    let set = box GreetingSet {
        // Get a pointer to the vector as an array, so that we can pass it back to Java
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

fn to_string(pointer: &*const c_char) -> String {
    let slice = unsafe { ffi::c_str_to_bytes(pointer) };
    str::from_utf8(slice).unwrap().to_string()
}

fn data(string: &str) -> *const c_char {
    to_ptr(string.to_string())
}

fn to_ptr(string: String) -> *const c_char {
    let cs = CString::from_slice(string.as_bytes());
    let ptr = cs.as_ptr();
    unsafe { mem::forget(cs) };
    ptr
}
