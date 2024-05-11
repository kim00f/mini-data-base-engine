package gogo.dbapp;

import gogo.btree.BTree;

import java.util.ArrayList;
import java.util.Vector;

public class Test_useBplustreePckg {
    public static void main(String[] args) {
        System.out.println("This is a package usage example");
        BTree<Integer, Vector<String>> btree = new BTree<Integer, Vector<String>>();

        btree.insert(1, "one");
        btree.insert(2, "two");
        btree.insert(3, "three");
        btree.insert(4, "four");
        btree.insert(5, "five");
        btree.insert(6, "six");
        btree.insert(7, "seven");
        btree.insert(8, "eight");
        btree.insert(9, "nine");
        btree.insert(10, "ten");
        btree.insert(10, "ten");
        btree.insert(10, "tennn");
        btree.print();
        System.out.println("*****************************************");
        System.out.println("Search for 100: ");
        
        Vector<String> values = btree.search(100);
        //print vector of strings
        if(values == null)
            System.out.println("Key not found");
        else {
            for (String value : values) 
                 System.out.println(value);
            }
        
        ArrayList<Vector<String>> result=btree.searchRangeGreaterThan(7, false);
        for(Vector<String> v:result){
            for(String s:v){
                System.out.print(s+" ");
            }
            System.out.println();
        }
}
}

