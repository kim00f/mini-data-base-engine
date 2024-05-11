package gogo.dbapp;
import java.util.ArrayList;
import java.util.Hashtable;
import gogo.btree.BTree;
public class Test_insertIntoTable {
    public static void main(String[]args) throws DBAppException {
        Hashtable<String,String> htblColNameType = new Hashtable<>();
        htblColNameType.put("id", "java.lang.Integer");
        htblColNameType.put("name", "java.lang.String");
        htblColNameType.put("gpa", "java.lang.Double");

        DBApp dbApp = new DBApp();
        dbApp.createTable("Student", "id", htblColNameType);
        ArrayList<Hashtable<String,Object>>students=new ArrayList<>();
        fill(students);
        for(Hashtable<String,Object>student:students){
            dbApp.insertIntoTable("Student", student);
        }

        //get table from disk and print to check the inserts
        Table student=(Table)HDInterface.deserialize("Student");
        student.print();

        //test creating index on a column that does not exist
             //dbApp.createIndex("Student","major", "majorIndex");

        //test creating index on a column that already has an index
            // dbApp.createIndex("Student","id", "idIndex");
            // dbApp.createIndex("Student","id", "idIndex");

        // //test creating index on a newly created table(no pages yet)
            // Hashtable<String,String> htblColNameType2 = new Hashtable<>();
            // htblColNameType2.put("id", "java.lang.Integer");
            // htblColNameType2.put("name", "java.lang.String");
            // htblColNameType2.put("gpa", "java.lang.Double");
            // dbApp.createTable("EmptyTable", "id",htblColNameType2);
            // dbApp.createIndex("EmptyTable","id", "idIndex");

        // //test creating index on an existing table with pages
            // dbApp.createIndex("Student","gpa", "gpaIndex");

        //deserialize the index created and print the b+ tree
            // BTree tree=(BTree)HDInterface.deserialize(student.getTableName()+"."+"gpaIndex");
            // System.out.println("******************************");
            // System.out.println("idIndex: ");
            // tree.print();

        //test inserting into table and see if it reflects to the corresponding indices
            //create indices first
            dbApp.createIndex("Student","id", "idIndex");
            dbApp.createIndex("Student","name", "nameIndex");
            //then insert two new students into the table
            Hashtable<String,Object>Student11 = new Hashtable<>();
            Student11.put("id", 11);
            Student11.put("name", "test");
            Student11.put("gpa", 0.95);
            Hashtable<String,Object>Student12 = new Hashtable<>();
            Student12.put("id", 12);
            Student12.put("name", "test2");
            Student12.put("gpa", 0.7);
            dbApp.insertIntoTable("Student", Student11);
            dbApp.insertIntoTable("Student", Student12);
            //get table from disk and print to check the inserts
            student=(Table)HDInterface.deserialize("Student");
            student.print();
            //print the indices
            System.out.println("******************************");
            BTree idIndex=(BTree)HDInterface.deserialize(student.getTableName()+"."+"idIndex");
            System.out.println("idIndex: ");
            idIndex.print();
            System.out.println("******************************");
            BTree nameIndex=(BTree)HDInterface.deserialize(student.getTableName()+"."+"nameIndex");
            System.out.println("nameIndex ");
            nameIndex.print();
        
    }
   
    
    public static void fill(ArrayList<Hashtable<String,Object>> arr){
        //Add 10 students
        Hashtable<String,Object>Student1 = new Hashtable<>();
        Student1.put("id", 8);
        Student1.put("name", "Ali");
        Student1.put("gpa", 0.85);

        Hashtable<String,Object>Student2 = new Hashtable<>();
        Student2.put("id", 1);
        Student2.put("name", "Ahmed");
        Student2.put("gpa", 0.95);


        Hashtable<String,Object>Student3 = new Hashtable<>();
        Student3.put("id", 5);
        Student3.put("name", "Dalia");
        Student3.put("gpa", 0.55);

        Hashtable<String,Object>Student4 = new Hashtable<>();
        Student4.put("id", 6);
        Student4.put("name", "Caleb");
        Student4.put("gpa", 0.65);

        Hashtable<String,Object>Student5 = new Hashtable<>();
        Student5.put("id", 3);
        Student5.put("name", "Baher");
        Student5.put("gpa", 0.75);

        Hashtable<String,Object>Student6 = new Hashtable<>();
        Student6.put("id", 9);
        Student6.put("name", "Hassan");
        Student6.put("gpa", 0.75);

        Hashtable<String,Object>Student7 = new Hashtable<>();
        Student7.put("id", 10);
        Student7.put("name", "Ibrahim");
        Student7.put("gpa", 0.05);

        Hashtable<String,Object>Student8 = new Hashtable<>();
        Student8.put("id", 7);
        Student8.put("name", "Dalia");
        Student8.put("gpa", 0.25);

        Hashtable<String,Object>Student9 = new Hashtable<>();
        Student9.put("id", 4);
        Student9.put("name", "Eman");
        Student9.put("gpa", 0.45);

        Hashtable<String,Object>Student10 = new Hashtable<>();
        Student10.put("id", 2);
        Student10.put("name", "Fady");
        Student10.put("gpa", 0.35);

        arr.add(Student1);
        arr.add(Student2);
        arr.add(Student3);
        arr.add(Student4);
        arr.add(Student5);
        arr.add(Student6);
        arr.add(Student7);
        arr.add(Student8);
        arr.add(Student9);
        arr.add(Student10);
    }
}

