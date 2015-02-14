/*
 * Copyright (C) 2015 drrb
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
package com.github.drrb.javarust;

import static java.util.Arrays.asList;
import java.util.List;

/**
 * An entry point that says hello from Rust code
 */
public class Main {
    
    private static String name;
    
    public static void main(String[] args) {
        List<String> arguments = asList(args);
        if (arguments.isEmpty()) {
            name = "World";
        } else {
            name = arguments.get(0);
        }
        Greetings.INSTANCE.printGreeting(name);
    }
    
}
