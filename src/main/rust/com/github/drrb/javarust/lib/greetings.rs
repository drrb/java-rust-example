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

use std::vec;
use std::ptr;
use std::comm::Chan;
use std::comm::SharedChan;
use std::c_str::CString;
use std::libc::c_int;

mod rust;

pub struct GreetingSet {
    greetings: *CString,
    number_of_greetings: c_int
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
    rust::run_in_runtime(proc() {
        println(format!("Hello, {}", name));
    })
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

/// Example of returning a struct from Rust
#[no_mangle]
pub extern fn renderGreetings() -> ~GreetingSet {
    let greetings = [ "Hello!".to_c_str(), "Hello again!".to_c_str() ];

    ~GreetingSet {
        // Get a raw pointer to the vector, so that we can pass it back to Java
        greetings: greetings.as_ptr(),
        // Also return the length of the array, so that we can create the array back in Java
        number_of_greetings: greetings.len() as c_int
    }
}

///// More complex example with tasks
//#[no_mangle]
//extern fn renderGreetingsInParallel(number_of_greetings: c_int, name: CString) -> ~GreetingSet {
//    let number_of_greetings = number_of_greetings as int;
//    let name = from_c_str(name);
//
//    let greetings = do rust::run_in_runtime {
//        let (port, chan) = Chan::new();
//        //let chan = SharedChan::new(chan);
//
//        for index in range(0, number_of_greetings) {
//            spawn(proc() {
//                println(format!("Sending a greeting from task {}", index));
//                chan.send((index, format!("Greeting number {} for {}", index, name)));
//            })
//        }
//
//        do vec::build(None) |push_result| {
//            for _ in range(0, number_of_greetings) {
//                let (task_number, greeting) = port.recv();
//                println(format!("Received a greeting from a task {}", task_number));
//                push_result(greeting);
//            }
//        }
//    };
//
//    let raw_greetings = do greetings.map |greeting| {
//        greeting.to_c_str()
//    };
//
//    ~GreetingSet {
//        greetings: raw_greetings.as_ptr(),
//        number_of_greetings: raw_greetings.len() as c_int
//    }
//}

fn from_c_str(c_string: CString) -> ~str {
    match c_string.as_str() {
        Some(string) => string.to_owned(),
        None => fail!("Couldn't get string from C-string")
    }
}
