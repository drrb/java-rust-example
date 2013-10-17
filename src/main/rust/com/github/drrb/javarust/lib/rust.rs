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
#[allow(ctypes)];

use std::rt;
use std::ptr;
use std::unstable::intrinsics;
use std::unstable::sync::UnsafeArc;
use std::unstable::atomics::{AtomicPtr, SeqCst};

extern {
    pub fn write(fd: i32, buf: *u8, nbyte: uint) -> uint;
}

#[fixed_stack_segment]
pub fn println_outside_runtime(message: &str) {
    let line = message.to_owned() + "\n";
    unsafe {
        write(0, ptr::to_unsafe_ptr(&line[0]), line.len());
    }
}

///Start the runtime and run the block
pub fn run_in_runtime<T>(function: ~fn() -> T) -> T {
    let result_setter = UnsafeArc::new(AtomicPtr::new(ptr::mut_null()));
    let result_getter = result_setter.clone();

    init_runtime();
    do rt::run_on_main_thread {
        let mut result = ~function();
        unsafe {
            (*result_setter.get()).store(ptr::to_mut_unsafe_ptr(result), SeqCst);
            // Don't release the pointer. We'll manage it ourselves.
            intrinsics::forget(result);
        }
    };
    rt::cleanup();

    unsafe {
        ptr::read_ptr((*result_getter.get()).load(SeqCst))
    }
}

fn init_runtime() {
    unsafe {
        rt::args::init(0, ptr::null());
    }
    //This causes a segfault:
    //rt::logging::init();
    rt::env::init();
}

