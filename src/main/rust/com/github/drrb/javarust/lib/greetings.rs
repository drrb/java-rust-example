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
#[link(name = "greetings", vers = "0.1")];
#[crate_type = "lib"];

use std::vec;
use std::ptr;
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

#[no_mangle]
extern fn printGreeting(name: CString) {
    let name = from_c_str(name);
    do rust::run_in_runtime {
        println(fmt!("Hello, %s", name));
    }
}

#[no_mangle]
extern fn renderGreeting(name: CString) -> CString {
    let name = from_c_str(name);
    ("Hello, " + name + "!").to_c_str()
}

#[no_mangle]
extern fn callMeBack(callback: extern "C" fn(CString)) {
    callback("Hello there!".to_c_str());
}

#[no_mangle]
extern fn greet(person_ptr: *mut Person) -> CString {
    let person = unsafe { ptr::read_ptr(person_ptr) };
    let first_name = from_c_str(person.first_name);
    ("Hello, " + first_name + "!").to_c_str()
}

#[no_mangle]
extern fn renderGreetings() -> ~GreetingSet {
    let greetings = [ "Hello!".to_c_str(), "Hello again!".to_c_str() ];
    ~GreetingSet {
        greetings: vec::raw::to_ptr(greetings),
        number_of_greetings: greetings.len() as c_int
    }
}

#[no_mangle]
extern fn renderGreetingsInParallel(number_of_greetings: c_int, name: CString) -> ~GreetingSet {
    let number_of_greetings = number_of_greetings as int;
    let name = match name.as_str() {
        Some(string) => string.to_owned(),
        None => fail!("Couldn't get string from C-string")
    };

    let greetings = do rust::run_in_runtime {
        let (port, chan) = stream();
        let chan = SharedChan::new(chan);

        for index in range(0, number_of_greetings) {
            let name = name.clone();
            let chan = chan.clone();
            do spawn {
                println(fmt!("Sending a greeting from task %i", index));
                chan.send((index, fmt!("Greeting number %i for %s", index, name)));
            }
        }

        do vec::build(None) |push_result| {
            for _ in range(0, number_of_greetings) {
                let (task_number, greeting) = port.recv();
                println(fmt!("Received a greeting from a task %i", task_number));
                push_result(greeting);
            }
        }
    };

    let raw_greetings = do greetings.map |greeting| {
        greeting.to_c_str()
    };

    ~GreetingSet {
        greetings: vec::raw::to_ptr(raw_greetings),
        number_of_greetings: greetings.len() as c_int
    }
}

fn from_c_str(c_string: CString) -> ~str {
    match c_string.as_str() {
        Some(string) => string.to_owned(),
        None => fail!("Couldn't get string from C-string")
    }
}
