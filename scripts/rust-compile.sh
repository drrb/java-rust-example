#!/bin/bash
# Copyright (C) 2013 drrb
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

set -ex

PATH="$PATH:/usr/local/bin"
SOURCE_ROOT="src/main/rust"
TARGET_ROOT="target/classes"
TMP_DIR="target/rustc$$"
BASE_DIR=`dirname "${BASH_SOURCE[0]}"`/..

echo "OSTYPE = $OSTYPE"
if [[ "$OSTYPE" =~ ^darwin ]]
then
    LIBRARY_SUFFIX=dylib
    OS_ARCH=darwin
else
    LIBRARY_SUFFIX=so
    if [[ "`uname -m`" == "x86_64" ]]
    then
	OS_ARCH=linux-amd64
    else
	OS_ARCH=linux-x86
    fi
fi

cd "$BASE_DIR"
for source_file in `find "$SOURCE_ROOT" -type f -name "*.rs"`
do
    if grep -E '#\[link.*\];' $source_file > /dev/null
    then
        output_dir="$TARGET_ROOT/$OS_ARCH"
        output_file_name=lib`basename $source_file | sed -E 's/\.rs$/.'$LIBRARY_SUFFIX'/'`
        output_file=$output_dir/$output_file_name
        mkdir -p "$output_dir"
        mkdir -p "$TMP_DIR"
        rustc -o "$TMP_DIR/$output_file_name" "$source_file"
	cp $TMP_DIR/*.$LIBRARY_SUFFIX "$output_file"
	rm -rf $TMP_DIR
    else
	echo "Not compiling '$source_file' because it's not a crate"
    fi
done
