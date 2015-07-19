/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 *
 * @author Administrator
 */
public class FragmentedArrayList<E> extends ArrayList<E> {
    private PriorityQueue<Integer> holes;
    
    public FragmentedArrayList() {
        super();
    }
    
    public boolean add(E e) {
        if (!holes.isEmpty()) {
            set(holes.poll(), e);
        } else {
            add(e);
        }
        return true;
    }
    
    public E remove(int index) {
        E result = get(index);
        set(index, null);
        holes.add(index);
        return result;
    }
}
