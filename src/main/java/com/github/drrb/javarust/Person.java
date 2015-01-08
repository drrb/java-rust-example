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

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/**
 * A struct that we pass from Java into Rust.
 * 
 * This is the Java representation of the Person struct in Rust.
 */
public class Person extends Structure {

    public static class ByReference extends Person implements Structure.ByReference {
    }

    public static class ByValue extends Person implements Structure.ByValue {
    }

    public String firstName;
    public String lastName;

    /**
     * Specify the order of the struct's fields.
     * 
     * The order here needs to match the order of the fields in the Rust code.
     * The astute will notice that the field names only match the field names in the
     * Java class, but not the equivalent Rust struct (the Rust one's are in 
     * snake_case, but could equally have had completely different names).
     * This is because the fields are mapped from the Rust representation to the 
     * Java one by their order (i.e. their relative location in memory), not by their names.
     */
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("firstName", "lastName");
    }

}
