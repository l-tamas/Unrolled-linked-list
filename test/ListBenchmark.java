/*
 *  Copyright (C) 2010 laiszner
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

import java.util.LinkedList;

import org.megatherion.util.collections.UnrolledLinkedList;

public class ListBenchmark {


    public static void main(String[] args) {

        LinkedList<Integer> ll = new LinkedList<Integer>();
        long endTime;
        // Láncolt lista - feltöltés
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            ll.add(i);
        }
        endTime = System.currentTimeMillis();
        System.out.println("LL: FILL -> " + (endTime - startTime));

        // Lánsolt lista - törlés
        startTime = System.currentTimeMillis();
        ll.remove(2500000);
        endTime = System.currentTimeMillis();
        System.out.println("LL: REMOVE -> " + (endTime - startTime));

        ll = null;
        System.gc();

        // ULL - feltöltés
        UnrolledLinkedList<Integer> ull = new UnrolledLinkedList<Integer>(8);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            ull.add(i);
        }
        endTime = System.currentTimeMillis();
        System.out.println("ULL: FILL -> " + (endTime - startTime));

        // ull - törlés
        startTime = System.currentTimeMillis();
        System.out.println(ull.remove(20));
        endTime = System.currentTimeMillis();
        System.out.println("ULL: REMOVE -> " + (endTime - startTime));

        System.out.println(ull.lastIndexOf(4999999));
        
    }

}
