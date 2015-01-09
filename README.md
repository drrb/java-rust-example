# Java/Rust Example

An example project showing how to call into Rust code from Java.

[![Build Status](https://travis-ci.org/drrb/java-rust-example.svg?branch=master)](https://travis-ci.org/drrb/java-rust-example)

## Contents

So far, the project contains
- Rust code and Java code
- A Java interface to the Rust code, using [JNA](https://github.com/twall/jna)
- A script to build the Rust code into a library and put it on the classpath where JNA can find it
- Examples of passing strings, structs, and callback functions between Java and Rust

## Getting Started

The best place to start looking at the examples is in the test code
[GreetingsTest.java](src/test/java/com/github/drrb/javarust/GreetingsTest.java).
The test contains lots of executable examples of calling into Rust code from
Java.

So far, it contains examples of:
- passing simple arguments from Java to Rust
- returning simple values from Rust to Java
- passing structs to Rust from Java
- returning structs from Rust by value and by reference
- passing callbacks to Rust that get called from the Rust code

From the test, you can navigate to the [Java code](src/main/java/com/github/drrb/javarust/Greetings.java) 
and the [Rust code](src/main/rust/com/github/drrb/javarust/greetings.rs). The
implementation is heavily commented to explain it.

## Building and Running the Tests

To build the project, use Maven. This will build a jar containing the Rust code 
and the Java code. This assumes you have Rust installed, and on the path.

```
mvn package
```

## Platform Support

This project is tested on OSX and Ubuntu. It should work on OSX, and any 32 bit
or 64 bit Gnu/Linux system.

## License

Java/Rust Example
Copyright (C) 2015 drrb

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
