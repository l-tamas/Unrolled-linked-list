/*
 *  Copyright (C) 2010 Tamás Laiszner <laisznertamas@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.megatherion.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

public class UnrolledLinkedList<E> implements List<E>, Serializable {

    private static final long serialVersionUID = -674052309103045211L;

    private class Node {

        Node next;

        Node previous;

        int numElements = 0;

        Object[] elements;

        Node() {

            elements = new Object[threshold];

        }

        void removeElement(int ptr) {

            numElements--;
            for (int i = ptr; i < numElements; i++) {
                elements[i] = elements[i + 1];
            }
            elements[numElements] = null;
            if (next != null && next.numElements + numElements <= threshold) {
                merge(next);
            } else if (previous != null && previous.numElements + numElements <= threshold) {
                previous.merge(this);
            }
            size--;

        }

        void insertElement(int ptr, E element) {

            if (numElements < threshold) {
                for (int i = ptr; i < numElements; i++) {
                    elements[i + 1] = elements[i];
                }
                elements[ptr] = element;
                numElements++;
            } else {
                Node newNode = new Node();
                newNode.next = next;
                newNode.previous = this;
                next = newNode;
                int elementsToMove = threshold / 2;
                int startIndex = threshold - elementsToMove;
                int i;
                for (i = 0; i < elementsToMove; i++) {
                    newNode.elements[i] = elements[startIndex + i];
                    elements[startIndex + i] = null;
                }
                newNode.elements[i] = element;
                numElements -= elementsToMove;
                newNode.numElements = elementsToMove + 1;
                if (this == lastNode) {
                    lastNode = newNode;
                }
        }
        size++;

        }

        void merge(Node node) {

            for (int i = 0; i < node.numElements; i++) {
                elements[numElements + i] = node.elements[i];
                node.elements[i] = null;
            }
            numElements += node.numElements;
            next = node.next;
            if (node.next != null) {
                node.next.previous = this;
            } else {
                lastNode = this;
            }

        }

    }

    private int threshold;

    private int size = 0;

    private Node firstNode;

    private Node lastNode;

    public UnrolledLinkedList(int threshold) {

        if (threshold < 8) {
            throw new IllegalArgumentException();
        }
        this.threshold = threshold;
        firstNode = new Node();
        lastNode = firstNode;

    }

    public UnrolledLinkedList() {

        this(16);

    }

    @Override
    public int size() {

        return size;

    }

    @Override
    public boolean isEmpty() {

        return (size == 0);

    }

    @Override
    public boolean contains(Object o) {

        return (indexOf(o) != -1);

    }

    @Override
    public Iterator<E> iterator() {

        return new ULLIterator(firstNode, 0, 0);

    }

    @Override
    public Object[] toArray() {

        Object[] array = new Object[size];
        int p = 0;
        for (Node node = firstNode; node != null; node = node.next) {
            for (int i = 0; i < node.numElements; i++) {
                array[p] = node.elements[i];
                p++;
            }
        }
        return array;

    }

    @Override
    public <T> T[] toArray(T[] a) {

        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }
        Object[] result = a;
        int p = 0;
        for (Node node = firstNode; node != null; node = node.next) {
            for (int i = 0; i < node.numElements; i++) {
                result[p] = node.elements[i];
                p++;
            }
        }
        return a;

    }

    @Override
    public boolean add(E e) {

        lastNode.insertElement(lastNode.numElements, e);
        return true;

    }

    @Override
    public boolean remove(Object o) {

        int index = 0;
        Node node = firstNode;
        if (o == null) {
            while (node != null) {
                for (int ptr = 0; ptr < node.numElements; ptr++) {
                    if (node.elements[ptr] == null) {
                        node.removeElement(ptr);
                        return true;
                    }
                }
                index += node.numElements;
                node = node.next;
            }
        } else {
            while (node != null) {
                for (int ptr = 0; ptr < node.numElements; ptr++) {
                    if (o.equals(node.elements[ptr])) {
                        node.removeElement(ptr);
                        return true;
                    }
                }
                index += node.numElements;
                node = node.next;
            }
        }
        return false;

    }

    @Override
    public boolean containsAll(Collection<?> c) {

        if (c == null) {
            throw new NullPointerException();
        }
        Iterator<?> it = c.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;

    }

    @Override
    public boolean addAll(Collection<? extends E> c) {

        if (c == null) {
            throw new NullPointerException();
        }
        boolean changed = false;
        Iterator<? extends E> it = c.iterator();
        while (it.hasNext()) {
            add(it.next());
            changed = true;
        }
        return changed;

    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {

        if (c == null) {
            throw new NullPointerException();
        }
        Node node;
        int p = 0;
        if (size - index > index) {
            node = firstNode;
            while (p <= index - node.numElements) {
                p += node.numElements;
                node = node.next;
            }
        } else {
            node = lastNode;
            p = size;
            while ((p -= node.numElements) > index) {
                node = node.previous;
            }
        }


    }

    @Override
    public boolean removeAll(Collection<?> c) {

        if (c == null) {
            throw new NullPointerException();
        }
        Iterator<?> it = c.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            if (remove(it.next())) {
                changed = true;
            }
        }
        return changed;

    }

    @Override
    public boolean retainAll(Collection<?> c) {

        if (c == null) {
            throw new NullPointerException();
        }
        boolean changed = false;
        for (Node node = firstNode; node != null; node = node.next) {
            for (int i = 0; i < node.numElements; i++) {
                if (!c.contains(node.elements[i])) {
                    node.removeElement(i);
                    i--;
                    changed = true;
                }
            }
        }
        return changed;

    }

    @Override
    public void clear() {

        Node node = firstNode;
        while (node != null) {
            Node next = node.next;
            node.next = null;
            node.previous = null;
            node.elements = null;
            node = next;
        }

    }

    @Override
    public E get(int index) throws IndexOutOfBoundsException {

        indexCheck(index);
        E element = null;
        Node node;
        int p = 0;
        if (size - index > index) {
            node = firstNode;
            while (p <= index - node.numElements) {
                p += node.numElements;
                node = node.next;
            }
        } else {
            node = lastNode;
            p = size;
            while ((p -= node.numElements) > index) {
                node = node.previous;
            }
        }
        return (E) node.elements[index - p];

    }

    @Override
    public E set(int index, E element) {

        indexCheck(index);
        E el = null;
        Node node;
        int p = 0;
        if (size - index > index) {
            node = firstNode;
            while (p <= index - node.numElements) {
                p += node.numElements;
                node = node.next;
            }
        } else {
            node = lastNode;
            p = size;
            while ((p -= node.numElements) > index) {
                node = node.previous;
            }
        }
        el = (E) node.elements[index - p];
        node.elements[index - p] = element;
        return el;

    }

    @Override
    public void add(int index, E element) throws IndexOutOfBoundsException {

        indexCheck(index);
        Node node;
        int p = 0;
        if (size - index > index) {
            node = firstNode;
            while (p <= index - node.numElements) {
                p += node.numElements;
                node = node.next;
            }
        } else {
            node = lastNode;
            p = size;
            while ((p -= node.numElements) > index) {
                node = node.previous;
            }
        }
        node.insertElement(index - p, element);

    }

    @Override
    public E remove(int index) throws IndexOutOfBoundsException {

        indexCheck(index);
        E element = null;
        Node node;
        int p = 0;
        if (size - index > index) {
            node = firstNode;
            while (p <= index - node.numElements) {
                p += node.numElements;
                node = node.next;
            }
        } else {
            node = lastNode;
            p = size;
            while ((p -= node.numElements) > index) {
                node = node.previous;
            }
        }
        element = (E) node.elements[index - p];
        node.removeElement(index - p);
        return element;

    }

    @Override
    public int indexOf(Object o) {

        int index = 0;
        Node node = firstNode;
        if (o == null) {
            while (node != null) {
                for (int ptr = 0; ptr < node.numElements; ptr++) {
                    if (node.elements[ptr] == null) {
                        return index + ptr;
                    }
                }
                index += node.numElements;
                node = node.next;
            }
        } else {
            while (node != null) {
                for (int ptr = 0; ptr < node.numElements; ptr++) {
                    if (o.equals(node.elements[ptr])) {
                        return index + ptr;
                    }
                }
                index += node.numElements;
                node = node.next;
            }
        }
        return -1;

    }

    @Override
    public int lastIndexOf(Object o) {

        int index = size;
        Node node = lastNode;
        if (o == null) {
            while (node != null) {
                index -= node.numElements;
                for (int i = node.numElements - 1; i >= 0; i--) {
                    if (node.elements[i] == null) {
                        return index + i;
                    }
                }
                node = node.previous;
            }
        } else {
            while (node != null) {
                index -= node.numElements;
                for (int i = node.numElements - 1; i >= 0; i--) {
                    if (o.equals(node.elements[i])) {
                        return index + i;
                    }
                }
                node = node.previous;
            }
        }
        return -1;

    }

    @Override
    public ListIterator<E> listIterator() {

        return new ULLIterator(firstNode, 0, 0);

    }

    @Override
    public ListIterator<E> listIterator(int index) {

        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        Node node;
        int p = 0;
        if (size - index > index) {
            node = firstNode;
            while (p <= index - node.numElements) {
                p += node.numElements;
                node = node.next;
            }
        } else {
            node = lastNode;
            p = size;
            while ((p -= node.numElements) > index) {
                node = node.previous;
            }
        }
        return new ULLIterator(node, index - p, index);

    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void indexCheck(int index) throws IndexOutOfBoundsException {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

    }

    private class ULLIterator implements ListIterator<E> {

        Node currentNode;
        int ptr;
        int index;

        ULLIterator(Node node, int ptr, int index) {

            this.currentNode = node;
            this.ptr = ptr;
            this.index = index;

        }

        @Override
        public boolean hasNext() {

            return (ptr + 1 < currentNode.numElements || currentNode.next != null);

        }

        @Override
        public E next() {

            ptr++;
            if (ptr < currentNode.numElements) {
                index++;
                return (E) currentNode.elements[ptr];
            } else if (currentNode.next != null) {
                currentNode = currentNode.next;
                ptr = 0;
                index++;
                return (E) currentNode.elements[ptr];
            }
            throw new NoSuchElementException();

        }

        @Override
        public boolean hasPrevious() {

            return (ptr - 1 >= 0 || currentNode.previous != null);

        }

        @Override
        public E previous() {

            ptr--;
            if (ptr >= 0) {
                index--;
                return (E) currentNode.elements[ptr];
            } else if (currentNode.next != null) {
                currentNode = currentNode.next;
                ptr = currentNode.numElements - 1;
                index--;
                return (E) currentNode.elements[ptr];
            }
            throw new NoSuchElementException();

        }

        @Override
        public int nextIndex() {

            return (index + 1);

        }

        @Override
        public int previousIndex() {

            return (index - 1);

        }

        @Override
        public void remove() {

            currentNode.removeElement(ptr);

        }

        @Override
        public void set(E e) {

            currentNode.elements[ptr] = e;

        }

        @Override
        public void add(E e) {

            currentNode.insertElement(ptr + 1, e);

        }

    }

}
