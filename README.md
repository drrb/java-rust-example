# Java/Rust Example

An example project containing a Java wrapper of a Rust library.

[![Build Status](https://travis-ci.org/drrb/java-rust-example.png?branch=master)](https://travis-ci.org/drrb/java-rust-example)

NOTE: currently only works with OSX :(

## Contents
So far, the project contains
- A Java interface to the Rust library, using [JNA](https://github.com/twall/jna)
- An example script to build the Rust library and put it on the classpath where JNA can find it
- Examples of passing strings, structs, and callback functions between Java and Rust

## License

Java/Rust Example
Copyright (C) 2013 drrb

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
