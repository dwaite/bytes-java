/**
 * Defines a {@link com.github.dwaite.bytestring.Bytes} and other supporting classes in order to represent a safe
 * primitive for a sequence of bytes. This is modeled on how java exposes a safe primitive for a sequence of
 * characters as {@link java.lang.String}.
 * 
 * The goals of the project are to provide an optimized set of primitive operations, as well as to reduce the number
 * of defensive copies needed to protect byte arrays from mutation as they are passed around code. This is due to the
 * elements of a Java array are always mutable, even if the reference to the array is declared final.
 * 
 * In addition to {@link com.github.dwaite.bytestring.Bytes}, a {@link com.github.dwaite.bytestring.ByteSequence} interface is
 * provided to allow for (potentially mutable) byte sequences to be used as arguments to ByteString methods. This is
 * similar to how {@link java.lang.CharSequence} works in Java, although unfortunately we are not able to extend
 * {@link java.nio.ByteBuffer} to support our interface.
 */
package com.github.dwaite.bytestring;
