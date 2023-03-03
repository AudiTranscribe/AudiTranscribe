/*
 * CapacityQueue.java
 * Description: Implements a queue with a maximum size and that is thread safe.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package app.auditranscribe.misc;

import app.auditranscribe.generic.exceptions.ValueException;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Implements a queue with a maximum size and that is thread safe.
 *
 * @param <T> Type of element stored in the queue.
 */
@ExcludeFromGeneratedCoverageReport
public class CapacityQueue<T> extends LinkedList<T> {
    // Attributes
    private int maxSize;

    /**
     * Initializes a new <code>CapacityQueue</code>.
     *
     * @param maxSize Maximum number of elements that can be stored in the queue.
     */
    public CapacityQueue(int maxSize) {
        updateMaxSize(maxSize);
    }

    // Getter/setter methods
    public synchronized int getMaxSize() {
        return maxSize;
    }

    // Public methods

    /**
     * Method that updates the maximum size of the queue.<br>
     * If the current maximum size is less than the new maximum size, then the excess elements will
     * be removed.
     *
     * @param newMaxSize New maximum size of the queue.
     * @throws ValueException If the new maximum size is negative.
     */
    public synchronized void updateMaxSize(int newMaxSize) {
        if (newMaxSize < 0) throw new ValueException("Maximum size of the queue must be non-negative");
        this.maxSize = newMaxSize;
        while (size() > this.maxSize) super.remove();
    }

    @Override
    public synchronized boolean add(T o) {
        super.add(o);
        while (size() > maxSize) {
            super.remove();
        }
        return true;
    }

    @Override
    public synchronized boolean offer(T o) {
        while (size() > maxSize - 1) {
            super.remove();
        }
        return super.offer(o);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends T> collection) {
        while (size() > maxSize - collection.size()) {
            super.remove();
        }
        return super.addAll(collection);
    }
}
