# Java/Rust Example

An example project showing how to call into Rust code from Java.

| OSX | Linux | Windows |
| --- | ----- | ------- |
| ![OSX Build Status](https://img.shields.io/badge/build-passing%20on%20my%20laptop-brightgreen.svg) | [![Linux Build Status](https://travis-ci.org/drrb/java-rust-example.svg?branch=master)](https://travis-ci.org/drrb/java-rust-example) | [![Windows Build status](https://ci.appveyor.com/api/projects/status/4yygb3925k7p87de/branch/master?svg=true)](https://ci.appveyor.com/project/drrb/java-rust-example/branch/master) |

## Requirements

- Java 7+
- Rust (tested with 1.0, nightly)

## Contents

So far, the project contains
- Rust code and Java code
- A Java interface to the Rust code, using [JNA](https://github.com/twall/jna)
- A script to build the Rust code into a library and put it on the classpath where JNA can find it
- Examples of passing strings, structs, and callback functions between Java and Rust

## Getting Started

The best place to start looking at the examples is in the test code
([GreetingsTest.java](src/test/java/com/github/drrb/javarust/GreetingsTest.java)).
The test contains lots of executable examples of calling into Rust code from
Java.  From the test, you can navigate to the [Java code](src/main/java/com/github/drrb/javarust/Greetings.java)
and the [Rust code](src/main/rust/com/github/drrb/javarust/lib/greetings.rs). The
implementation is heavily commented to explain it.

So far, it contains examples of the following (click the links to see!):
- *[Arguments](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L45)*: passing simple arguments from Java to Rust ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L44) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L81))
- *[Return values](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L50)*: returning simple values from Rust to Java ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L49) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L91))
- *[Struct arguments](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L56)*: passing structs to Rust from Java ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L54) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L100))
- *[Returning structs (2 examples)](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L65)*: returning structs from Rust by value and by reference ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L71) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L109))
- *[Callbacks (3 examples)](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L80)*: passing callbacks to Rust that get called from the Rust code ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L84) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L129))
- *[Freeing memory](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L67)*: freeing memory allocated in Rust ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L114) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L171))

## Building and Running the Tests

To build the project, and run the tests, use Maven. This will build a jar
containing the Rust code and the Java code. This assumes you have Rust
installed, and on the path.

```
$ mvn package
```

You can then run the jar that is produced to see the integration work.

```
$ java -jar target/greeter.jar John
Hello from Rust, John
```

## Platform Support

This project is tested on OSX, Ubuntu, and Windows. It should also work on any 32 bit or 64 bit Gnu/Linux system.

## Limitations

Some of the examples leak memory. Any memory that is allocated in Rust needs to be freed manually because it's not managed by JNA. Some examples pass objects back into Rust to be dropped for this reason, but we don't clean up everything properly (strings, for example). This is almost certainly not a limitation of Rust, but a limitation of my current understanding of Rust.

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
