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

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class GreetingSet extends Structure {

    public static class ByReference extends GreetingSet implements Structure.ByReference {
    }

    public static class ByValue extends GreetingSet implements Structure.ByValue {
    }

    public Pointer greetings;
    public int numberOfGreetings;

    public List<String> getGreetings() {
        List<String> greetingList = new LinkedList<>();
        // Every second one is one of the strings. Why??
        String[] greetingArray = greetings.getStringArray(0, numberOfGreetings * 2);
        for (int i = 0; i < greetingArray.length; i++) {
            if (i % 2 == 0) {
                greetingList.add(greetingArray[i]);
            }
        }
        return greetingList;
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("greetings", "numberOfGreetings");
    }
}
