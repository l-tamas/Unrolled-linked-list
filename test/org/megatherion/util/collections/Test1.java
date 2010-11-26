/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.megatherion.util.collections;

import java.util.ListIterator;
import java.util.Random;

public class Test1 {

    public static void main(String[] args) {

        UnrolledLinkedList<Integer> ull = new UnrolledLinkedList<Integer>(8);
        for (int i = 0; i < 100; i++) {
            ull.add(i);
        }
        Random generator = new Random(System.currentTimeMillis());
        System.out.println(">>> get(int index) - test");
        for (int i = 0; i < 20; i++) {
            int rand = generator.nextInt(100);
            System.out.println(rand + " -> " + ull.get(rand));
        }
        System.out.println(">>> Iteration - test 1");
        ListIterator<Integer> it = ull.listIterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        System.out.println("----------------------");
        while (it.hasPrevious()) {
            System.out.println(it.previous());
        }
        System.out.println(">>> Iteration - test 2");
        it = ull.listIterator(27);
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        System.out.println(">>> add(int index, E element) - test");
        for (int i = 0; i < 50; i++) {
            int rand = generator.nextInt(100);
            int val = (-generator.nextInt(10000) - 1);
            ull.add(rand, val);
            System.out.println(rand + " -> " + val);
        }
        System.out.println("----------------------");
        for (int i = 0; i < ull.size(); i++) {
            System.out.println(i + " -> " + ull.get(i));
        }
        System.out.println(">>> indexOf - test");
        for (int i = 0; i < 20; i++) {
            int rand = generator.nextInt(100);
            System.out.println("<>" + rand + " -> " + ull.indexOf(rand) + " -> " + ull.lastIndexOf(rand));
        }
        ull.clear();

    }

}
