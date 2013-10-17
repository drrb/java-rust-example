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
use std::comm::SharedChan;
use std::c_str::CString;

mod rust;

#[no_mangle]
extern fn printGreetingsInParallel(name_cstr: CString) -> CString {
    let name = match name_cstr.as_str() {
        Some(string) => string.to_owned(),
        None => fail!("Couldn't get string from C-string")
    };

    let greetings = do rust::run_in_runtime {
        let (port, chan) = stream();
        let chan = SharedChan::new(chan);

        for index in range(1, 4) {
            let name = name.clone();
            let chan = chan.clone();
            do spawn {
                println(fmt!("Sending a greeting from task %i", index));
                chan.send(fmt!("Greeting number %i for %s", index, name));
            }
        }

        do vec::build(None) |push_result| {
            for _ in range(1, 4) {
                let greeting = port.recv();
                println("Received a greeting from a task");
                push_result(greeting);
            }
        }
    };

    greetings[0].to_c_str()
}
