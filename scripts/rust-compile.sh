#!/bin/bash
# Copyright (C) 2015 drrb
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

set -e
set -u

is_osx() [[ "$OSTYPE" =~ ^darwin ]]
is_64_bit() [[ "`uname -m`" == "x86_64" ]]
is_running_on_travis() {
    env | grep TRAVIS > /dev/null
}

if is_running_on_travis
then
    set -x
fi

PATH="$PATH:/usr/local/bin"
SOURCE_ROOT="src/main/rust"
TARGET_ROOT="target/classes"
TMP_DIR="target/rustc$$"
BASE_DIR=`dirname "${BASH_SOURCE[0]}"`/..
if is_osx
then
    LIBRARY_SUFFIX=dylib
    OS_ARCH=darwin
    STAT="stat -f %m"
else
    LIBRARY_SUFFIX=so
    if is_64_bit
    then
        OS_ARCH=linux-x86-64
    else
        OS_ARCH=linux-x86
    fi
    STAT="stat --format=%Y"
fi
OUTPUT_DIR="$TARGET_ROOT/$OS_ARCH"

rust_source_files() {
    find "$SOURCE_ROOT" -type f -name "*.rs"
}

is_crate_file() {
    local rust_source_file=$1
    grep -E '#!\[crate_type.*\]' "$rust_source_file" > /dev/null
}

output_file_for() {
    local source_file=$1
    echo $OUTPUT_DIR/lib`basename "$source_file" | sed -E 's/\.rs$/.'$LIBRARY_SUFFIX'/'`
}

has_changed_since_last_compile() {
    local source_file=$1
    local output_file=`output_file_for "$source_file"`
    [ ! -f "$output_file" ] || [[ "`$STAT "$source_file"`" -gt "`$STAT "$output_file"`" ]]
}

compile() {
    local source_file=$1
    local output_file=`output_file_for "$source_file"`
    mkdir -p "$OUTPUT_DIR"
    mkdir -p "$TMP_DIR"
    rustc -o "${TMP_DIR}/`basename "$output_file"`" "$source_file"
    cp $TMP_DIR/*.$LIBRARY_SUFFIX "$output_file"
    rm -rf $TMP_DIR
}

cd "$BASE_DIR"
rust_source_files | while read source_file
do
    if is_crate_file "$source_file"
    then
        if has_changed_since_last_compile "$source_file"
        then
            echo "Compiling '$source_file': changes detected"
            compile "$source_file"
        else
            echo "Not compiling '$source_file': compiled version is up to date"
        fi
    else
        echo "Not compiling '$source_file' because it's not a crate"
    fi
done
