# Java/Rust Example

An example project showing how to call into Rust code from Java.

[![Build Status](https://travis-ci.org/drrb/java-rust-example.svg?branch=master)](https://travis-ci.org/drrb/java-rust-example)

## Requirements

- Tested on OSX and Linux. May also work on Windows!
- Java 7+
- Rust (tested againts post-alpha nightlies)

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
- *[Arguments](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L42)*: passing simple arguments from Java to Rust ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L44) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L74))
- *[Return values](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L47)*: returning simple values from Rust to Java ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L49) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L84))
- *[Struct arguments](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L54)*: passing structs to Rust from Java ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L54) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L93))
- *[Returning structs (2 examples)](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L63)*: returning structs from Rust by value and by reference ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L62) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L102))
- *[Callbacks (3 examples)](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L80)*: passing callbacks to Rust that get called from the Rust code ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L84) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L122))
- *[Freeing memory](src/test/java/com/github/drrb/javarust/GreetingsTest.java#L76)*: freeing memory allocated in Rust ([Java side](src/main/java/com/github/drrb/javarust/Greetings.java#L144) / [Rust side](src/main/rust/com/github/drrb/javarust/lib/greetings.rs#L169))

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

This project is tested on OSX and Ubuntu. It should work on OSX, and any 32 bit
or 64 bit Gnu/Linux system.

## Limitations

Some of the examples leak memory. Any memory that is allocated in Rust needs to be freed manually because it's not managed by JNA. Some examples pass objects back into Rust to be dropped for this reason, but we don't clean up every thing properly (strings, for example). This is almost certainly not a limitation of Rust, but a limitation of my current understanding of Rust.

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
