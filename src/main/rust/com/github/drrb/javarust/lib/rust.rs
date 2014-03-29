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

extern mod native;
use std::rt;
use std::ptr;
use std::unstable::intrinsics;
use std::sync::arc::UnsafeArc;
use std::sync::atomics::AtomicPtr;
use std::sync::atomics::SeqCst;

///Start the runtime and run the block
pub fn run<T: Send>(function: proc() -> T) -> T {
    let result_setter = UnsafeArc::new(AtomicPtr::new(ptr::mut_null()));
    let result_getter = result_setter.clone();

    rt::init(0, ptr::null());
    do native::run {
        let mut result = ~function();
        unsafe {
            (*result_setter.get()).store(ptr::to_mut_unsafe_ptr(result), SeqCst);
            // Don't release the pointer. We'll manage it ourselves.
            intrinsics::forget(result);
        }
    };

    unsafe {
        rt::cleanup();
        let result_pointer = (*result_getter.get_immut()).load(SeqCst);
        ptr::read_ptr(result_pointer as *T)
    }
}
