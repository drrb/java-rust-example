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
use libc::c_int;

#[repr(C)]
pub struct GreetingSet {
    greetings: *const CString,
    number_of_greetings: c_int
}

impl Copy for GreetingSet {}

#[repr(C)]
pub struct Greeting {
    text: CString
}

//TODO: is this annotation required?
#[repr(C)]
pub struct Person {
    first_name: CString,
    last_name: CString
}

/// Example of just calling into Rust
#[no_mangle] // "no_mangle", so that our Java code can still see the Rust function after it's compiled
#[allow(non_snake_case)]
pub extern fn printGreeting(name: CString) {
    // Convert the C string to a Rust one
    let name = from_c_str(name);
    println!("Hello, {}", name);
}

/// Example of passing and returning a value
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn renderGreeting(name: CString) -> CString {
    let name = from_c_str(name);

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
pub extern fn greet(person_ptr: *mut Person) -> CString {
    // Read the raw pointer as a struct
    let person: Box<Person> = unsafe { mem::transmute(person_ptr) };
    let first_name = from_c_str(person.first_name);

    //TODO: how do we get the last name too?
    //let last_name = from_c_str(person.last_name);
    format!("Hello, {}!", first_name).to_c_str()
}

/// Example of returning a struct from Rust by value
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn getGreetingByValue() -> Greeting {
    Greeting { text: "Hello from Rust!".to_c_str() }
}

/// Example of returning a struct from Rust by reference
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn getGreetingByReference() -> *mut Greeting {
    let greeting = Greeting { text: "Hello from Rust!".to_c_str() };
    unsafe {
        mem::transmute(box greeting)
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern fn sendGreetings(callback: extern "C" fn(*mut GreetingSet)) { // The function argument here is an "extern" one, so that we can pass it in from Java
    let greetings = [ "Hello!".to_c_str(), "Hello again!".to_c_str() ];

    let set = box GreetingSet {
        // Get a raw pointer to the vector, so that we can pass it back to Java
        greetings: greetings.as_ptr(),
        // Also return the length of the array, so that we can create the array back in Java
        number_of_greetings: greetings.len() as c_int
    };
    callback(unsafe { mem::transmute(set) });
}

/// Example of returning a struct from Rust
#[no_mangle]
#[allow(non_snake_case)]
pub extern fn renderGreetings() -> *mut GreetingSet {
    let greetings = [ "Hello!".to_c_str(), "Hello again!".to_c_str() ];

    let greeting_set = box GreetingSet {
        // Get a raw pointer to the vector, so that we can pass it back to Java
        greetings: greetings.as_ptr(),
        // Also return the length of the array, so that we can create the array back in Java
        number_of_greetings: greetings.len() as c_int
    };
    unsafe { mem::transmute(greeting_set) }
}

fn from_c_str(c_string: CString) -> String {
    c_string.as_str().expect("Couldn't get string from C-string").to_string()
}
