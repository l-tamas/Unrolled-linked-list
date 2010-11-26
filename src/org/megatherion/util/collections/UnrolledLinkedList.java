/*
 * This source code is placed in the public domain. This means you can use it
 * without any restrictions.
 */

package org.megatherion.util.collections;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.io.Serializable;
import java.util.ConcurrentModificationException;

/**
 * An unrolled linked list implemetation of the
 * {@link java.util.List List} interface. It offers better performance
 * than the {{@link java.util.LinkedList LinkedList} class.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a linked list concurrently, and at least
 * one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more elements; merely setting the value of
 * an element is not a structural modification.)  This is typically
 * accomplished by synchronizing on some object that naturally
 * encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link java.util.Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new LinkedList(...));</pre>
 *
 * <p>The iterators returned by this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in
 * any way except through the Iterator's own <tt>remove</tt> or
 * <tt>add</tt> methods, the iterator will throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than
 * risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 * @param <E> the type of elements held in this collection
 * @see <a href="http://en.wikipedia.org/wiki/Unrolled_linked_list">Unrolled_linked_list</a>
 */

// TODO: Implement the java.util.List.addAll(int index, Collection<? extends E> c)
//       method in a more efficient way. The current implementation is inherited
//       from the java.util.AbstractList class.
// TODO: Testing

public class UnrolledLinkedList<E> extends AbstractList<E> implements List<E>, Serializable {

    /**
     * The maximum number of elements that can be stored in a single node.
     */
    private int nodeCapacity;

    /**
     * The current size of this list.
     */
    private int size = 0;

    /**
     * The first node of this list.
     */
    private Node firstNode;

    /**
     * The last node of this list.
     */
    private Node lastNode;

    /**
     * Constructs an empty list with the specified
     * {@link UnrolledLinkedList#nodeCapacity nodeCapacity}. For performance
     * reasons <tt>nodeCapacity</tt> must be greater than or equal to 8.
     *
     * @param nodeCapacity The maximum number of elements
     *        that can be stored in a single node.
     * @throws IllegalArgumentException if <tt>nodeCapacity</tt> is less than 8
     */
    public UnrolledLinkedList(int nodeCapacity) throws IllegalArgumentException {

        if (nodeCapacity < 8) {
            throw new IllegalArgumentException("nodeCapacity < 8");
        }
        this.nodeCapacity = nodeCapacity;
        firstNode = new Node();
        lastNode = firstNode;

    }

    /**
     * Constructs an empty list with
     * {@link UnrolledLinkedList#nodeCapacity nodeCapacity} of 16.
     */
    public UnrolledLinkedList() {

        this(16);

    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {

        return size;

    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    @Override
    public boolean isEmpty() {

        return (size == 0);

    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    @Override
    public boolean contains(Object o) {

        return (indexOf(o) != -1);

    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public Iterator<E> iterator() {

        return new ULLIterator(firstNode, 0, 0);

    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list
     *         in proper sequence
     */
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

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list
     *         in proper sequence
     */
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

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(E e) {

        insertIntoNode(lastNode, lastNode.numElements, e);
        return true;

    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns <tt>true</tt> if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    @Override
    public boolean remove(Object o) {

        int index = 0;
        Node node = firstNode;
        if (o == null) {
            while (node != null) {
                for (int ptr = 0; ptr < node.numElements; ptr++) {
                    if (node.elements[ptr] == null) {
                        removeFromNode(node, ptr);
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
                        removeFromNode(node, ptr);
                        return true;
                    }
                }
                index += node.numElements;
                node = node.next;
            }
        }
        return false;

    }

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of the
     * specified collection.
     *
     * @param  c collection to be checked for containment in this list
     * @return <tt>true</tt> if this list contains all of the elements of the
     *         specified collection
     * @throws NullPointerException if the specified collection is null
     * @see #contains(Object)
     */
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

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this
     * operation is undefined if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see #add(Object)
     */
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

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
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

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes
     * from this list all the elements that are not contained in the specified
     * collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean retainAll(Collection<?> c) {

        if (c == null) {
            throw new NullPointerException();
        }
        boolean changed = false;
        for (Node node = firstNode; node != null; node = node.next) {
            for (int i = 0; i < node.numElements; i++) {
                if (!c.contains(node.elements[i])) {
                    removeFromNode(node, i);
                    i--;
                    changed = true;
                }
            }
        }
        return changed;

    }

    /**
     * Removes all of the elements from this list.
     */
    @Override
    public void clear() {

        Node node = firstNode.next;
        while (node != null) {
            Node next = node.next;
            node.next = null;
            node.previous = null;
            node.elements = null;
            node = next;
        }
        lastNode = firstNode;
        for (int ptr = 0; ptr < firstNode.numElements; ptr++) {
            firstNode.elements[ptr] = null;
        }
        firstNode.numElements = 0;
        firstNode.next = null;
        size = 0;

    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) throws IndexOutOfBoundsException {

        if (index < 0 || index >= size) {
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
        return (E) node.elements[index - p];

    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public E set(int index, E element) {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
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

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public void add(int index, E element) throws IndexOutOfBoundsException {

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
        insertIntoNode(node, index - p, element);

    }

    /**
     * Removes the element at the specified position in this list.  Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public E remove(int index) throws IndexOutOfBoundsException {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
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
        removeFromNode(node, index - p);
        return element;

    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
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

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
    @Override
    public int lastIndexOf(Object o) {

        int index = size;
        Node node = lastNode;
        if (o == null) {
            while (node != null) {
                index -= node.numElements;
                for (int i = node.numElements - 1; i >= 0; i--) {
                    if (node.elements[i] == null) {
                        return (index + i);
                    }
                }
                node = node.previous;
            }
        } else {
            while (node != null) {
                index -= node.numElements;
                for (int i = node.numElements - 1; i >= 0; i--) {
                    if (o.equals(node.elements[i])) {
                        return (index + i);
                    }
                }
                node = node.previous;
            }
        }
        return -1;

    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * @return a list iterator over the elements in this list (in proper
     *         sequence)
     */
    @Override
    public ListIterator<E> listIterator() {

        return new ULLIterator(firstNode, 0, 0);

    }

    /**
     * Returns a list-iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * Obeys the general contract of <tt>List.listIterator(int)</tt>.<p>
     *
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own <tt>remove</tt> or <tt>add</tt>
     * methods, the list-iterator will throw a
     * <tt>ConcurrentModificationException</tt>.  Thus, in the face of
     * concurrent modification, the iterator fails quickly and cleanly, rather
     * than risking arbitrary, non-deterministic behavior at an undetermined
     * time in the future.
     *
     * @param index index of the first element to be returned from the
     *              list-iterator (by a call to <tt>next</tt>)
     * @return a ListIterator of the elements in this list (in proper
     *         sequence), starting at the specified position in the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see List#listIterator(int)
     */
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

    private static final long serialVersionUID = -674052309103045211L;

    private class Node {

        /**
         * The next node.
         */
        Node next;

        /**
         * The previous node.
         */
        Node previous;

        /**
         * The number of elements stored in this node.
         */
        int numElements = 0;

        /**
         * The array in which the elements are stored.
         */
        Object[] elements;

        /**
         * Constructs a new node.
         */
        Node() {

            elements = new Object[nodeCapacity];

        }

    }

    private class ULLIterator implements ListIterator<E> {

        Node currentNode;
        int ptr;
        int index;

        private int expectedModCount = modCount;

        ULLIterator(Node node, int ptr, int index) {

            this.currentNode = node;
            this.ptr = ptr;
            this.index = index;

        }

        @Override
        public boolean hasNext() {

            return (index < size - 1);

        }

        @Override
        public E next() {

            ptr++;
            if (ptr >= currentNode.numElements) {
                if (currentNode.next != null) {
                    currentNode = currentNode.next;
                    ptr = 0;
                } else {
                    throw new NoSuchElementException();
                }
            }
            index++;
            checkForModification();
            return (E) currentNode.elements[ptr];

        }

        @Override
        public boolean hasPrevious() {

            return (index > 0);

        }

        @Override
        public E previous() {

            ptr--;
            if (ptr < 0) {
                if (currentNode.previous != null) {
                    currentNode = currentNode.previous;
                    ptr = currentNode.numElements - 1;
                } else {
                    throw new NoSuchElementException();
                }
            }
            index--;
            checkForModification();
            return (E) currentNode.elements[ptr];

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

            checkForModification();
            removeFromNode(currentNode, ptr);

        }

        @Override
        public void set(E e) {

            checkForModification();
            currentNode.elements[ptr] = e;

        }

        @Override
        public void add(E e) {

            checkForModification();
            insertIntoNode(currentNode, ptr + 1, e);

        }

        private void checkForModification() {

            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

        }

    }

    /**
     * Insert an element into the specified node. If the node is already full,
     * a new node will be created and inserted into the list after
     * the specified node.
     *
     * @param node
     * @param ptr the position at which the element should be inserted
     *            into the <tt>node.elements<tt> array
     * @param element the element to be inserted
     */
    private void insertIntoNode(Node node, int ptr, E element) {

        // if the node is full
        if (node.numElements == nodeCapacity) {
            // create a new node
            Node newNode = new Node();
            // move half of the elements to the new node
            int elementsToMove = nodeCapacity / 2;
            int startIndex = nodeCapacity - elementsToMove;
            int i;
            for (i = 0; i < elementsToMove; i++) {
                newNode.elements[i] = node.elements[startIndex + i];
                node.elements[startIndex + i] = null;
            }
            node.numElements -= elementsToMove;
            newNode.numElements = elementsToMove;
            // insert the new node into the list
            newNode.next = node.next;
            newNode.previous = node;
            if (node.next != null) {
                node.next.previous = newNode;
            }
            node.next = newNode;

            if (node == lastNode) {
                lastNode = newNode;
            }

            // check whether the element should be inserted into
            // the original node or into the new node
            if (ptr > node.numElements) {
                node = newNode;
                ptr -= node.numElements;
            }
        }
        for (int i = node.numElements; i > ptr; i--) {
            node.elements[i] = node.elements[i - 1];
        }
        node.elements[ptr] = element;
        node.numElements++;
        size++;
        modCount++;

    }

    /**
     * Removes an element from the specified node.
     *
     * @param node the node from which an element should be removed
     * @param ptr the index of the element to be removed within
     * the <tt>node.elements<tt> array
     */
    private void removeFromNode(Node node, int ptr) {

        node.numElements--;
        for (int i = ptr; i < node.numElements; i++) {
            node.elements[i] = node.elements[i + 1];
        }
        node.elements[node.numElements] = null;
        if (node.next != null && node.next.numElements + node.numElements <= nodeCapacity) {
            mergeWithNextNode(node);
        } else if (node.previous != null && node.previous.numElements + node.numElements <= nodeCapacity) {
            mergeWithNextNode(node.previous);
        }
        size--;
        modCount++;

    }

    /**
     * This method does merge the specified node with the next node.
     *
     * @param node the node which should be merged with the next node
     */
    private void mergeWithNextNode(Node node) {

        Node next = node.next;
        for (int i = 0; i < next.numElements; i++) {
            node.elements[node.numElements + i] = next.elements[i];
            next.elements[i] = null;
        }
        node.numElements += next.numElements;
        if (next.next != null) {
            next.next.previous = node;
        }
        node.next = next.next.next;
        if (next == lastNode) {
            lastNode = node;
        }

    }

    /*
    // DEBUG
    void print() {

        System.out.println("SIZE: " + size);
        for (Node n = firstNode; n != null; n = n.next) {
            printNode(n);
        }

    }

    void print2() {

        System.out.println("SIZE: " + size);
        for (Node n = lastNode; n != null; n = n.previous) {
            printNode(n);
        }

    }

    void printNode(Node n) {

        System.out.print(n.numElements + " { ");
        for (int i = 0; i < n.numElements; i++) {
            System.out.print(n.elements[i] + " ");
        }
        System.out.println("}");

    }
    */

}
