/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.memory;

import static com.hazelcast.internal.util.Preconditions.checkNotNull;

/**
 * MemorySize represents a memory size with given value and <code>{@link MemoryUnit}</code>.
 *
 * @see MemoryUnit
 * @since 3.4
 */
public final class MemorySize {

    private static final int PRETTY_FORMAT_LIMIT = 10;

    private final long value;
    private final MemoryUnit unit;

    public MemorySize(long value) {
        this(value, MemoryUnit.BYTES);
    }

    public MemorySize(long value, MemoryUnit unit) {
        if (value < 0) {
            throw new IllegalArgumentException("Memory size cannot be negative! -> " + value);
        }

        this.value = value;
        this.unit = checkNotNull(unit, "MemoryUnit is required!");
    }

    /**
     * Returns value of memory size in its original unit.
     *
     * @return memory size in its original unit
     */
    public long getValue() {
        return value;
    }

    /**
     * Returns unit of memory size
     *
     * @return unit of memory size
     */
    public MemoryUnit getUnit() {
        return unit;
    }

    /**
     * Returns value of memory size in bytes.
     *
     * @return memory size in bytes
     */
    public long bytes() {
        return unit.toBytes(value);
    }

    /**
     * Returns value of memory size in kilo-bytes.
     *
     * @return memory size in kilo-bytes
     */
    public long kiloBytes() {
        return unit.toKiloBytes(value);
    }

    /**
     * Returns value of memory size in mega-bytes.
     *
     * @return memory size in mega-bytes
     */
    public long megaBytes() {
        return unit.toMegaBytes(value);
    }

    /**
     * Returns value of memory size in giga-bytes.
     *
     * @return memory size in giga-bytes
     */
    public long gigaBytes() {
        return unit.toGigaBytes(value);
    }

    /**
     * Parses string representation of a memory size value.
     * Value may end with one of suffixes;
     * <ul>
     * <li>'k' or 'K' for 'kilo',</li>
     * <li>'m' or 'M' for 'mega',</li>
     * <li>'g' or 'G' for 'giga'.</li>
     * </ul>
     * <p>
     * Default unit is bytes.
     * <p>
     * Examples:
     * 12345, 12345m, 12345K, 123456G
     */
    public static MemorySize parse(String value) {
        return parse(value, MemoryUnit.BYTES);
    }

    /**
     * Parses string representation of a memory size value.
     * Value may end with one of suffixes;
     * <ul>
     * <li>'k' or 'K' for 'kilo',</li>
     * <li>'m' or 'M' for 'mega',</li>
     * <li>'g' or 'G' for 'giga'.</li>
     * </ul>
     * <p>
     * Uses default unit if value does not contain unit information.
     * <p>
     * Examples:
     * 12345, 12345m, 12345K, 123456G
     */
    public static MemorySize parse(String value, MemoryUnit defaultUnit) {
        if (value == null || value.length() == 0) {
            return new MemorySize(0, MemoryUnit.BYTES);
        }

        MemoryUnit unit = defaultUnit;
        final char last = value.charAt(value.length() - 1);
        if (!Character.isDigit(last)) {
            value = value.substring(0, value.length() - 1);
            switch (last) {
                case 'g':
                case 'G':
                    unit = MemoryUnit.GIGABYTES;
                    break;

                case 'm':
                case 'M':
                    unit = MemoryUnit.MEGABYTES;
                    break;

                case 'k':
                case 'K':
                    unit = MemoryUnit.KILOBYTES;
                    break;

                default:
                    throw new IllegalArgumentException("Could not determine memory unit of " + value + last);
            }
        }

        return new MemorySize(Long.parseLong(value), unit);
    }

    /**
     * Parses string representation of a memory size.
     * <br>
     * The input must match the pattern {@code ^[1-9]([0-9]*) (B|KB|MB|GB)$}.
     * Ex, "8192 MB", "1 GB" are valid inputs, while "01 MB", "2.2 GB" are not.
     * This method expects a well-formed input and does not do any kind of input validation.
     *
     * @param memorySize memory size.
     * @return @{code MemorySize} instance.
     *
     * @since 5.1
     */
    public static MemorySize parseMemorySize(String memorySize) {
        String[] valAndUnit = memorySize.split(" ");
        long value = Long.parseLong(valAndUnit[0]);

        switch (valAndUnit[1]) {
            case "B":
                return new MemorySize(value, MemoryUnit.BYTES);
            case "KB":
                return new MemorySize(value, MemoryUnit.KILOBYTES);
            case "MB":
                return new MemorySize(value, MemoryUnit.MEGABYTES);
            case "GB":
                return new MemorySize(value, MemoryUnit.GIGABYTES);
            default:
                throw new IllegalArgumentException("Could not determine memory unit of " + memorySize);
        }
    }

    /**
     * Returns a pretty format String representation of this memory size.
     *
     * @return a pretty format representation of this memory size
     */
    public String toPrettyString() {
        return toPrettyString(value, unit);
    }

    @Override
    public String toString() {
        return value + " " + unit.toString();
    }

    /**
     * Utility method to create a pretty format representation of given value in bytes.
     *
     * @param size size in bytes
     * @return pretty format representation of given value
     */
    public static String toPrettyString(long size) {
        return toPrettyString(size, MemoryUnit.BYTES);
    }

    /**
     * Utility method to create a pretty format representation of given value in given unit.
     *
     * @param size memory size
     * @param unit memory unit
     * @return pretty format representation of given value
     */
    public static String toPrettyString(long size, MemoryUnit unit) {
        if (unit.toGigaBytes(size) >= PRETTY_FORMAT_LIMIT) {
            return unit.toGigaBytes(size) + " GB";
        }
        if (unit.toMegaBytes(size) >= PRETTY_FORMAT_LIMIT) {
            return unit.toMegaBytes(size) + " MB";
        }
        if (unit.toKiloBytes(size) >= PRETTY_FORMAT_LIMIT) {
            return unit.toKiloBytes(size) + " KB";
        }
        if (size % MemoryUnit.K == 0) {
            return unit.toKiloBytes(size) + " KB";
        }
        return size + " bytes";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MemorySize that = (MemorySize) o;

        if (value != that.value) {
            return false;
        }
        return unit == that.unit;
    }

    @Override
    public int hashCode() {
        int result = (int) (value ^ (value >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}
