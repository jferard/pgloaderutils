package com.github.jferard.pgloaderutils.csvsniffer.csd;

/**
* An Iterable with a size.
 */
interface SizedIterable<E> extends Iterable<E> {
    int size();
}
