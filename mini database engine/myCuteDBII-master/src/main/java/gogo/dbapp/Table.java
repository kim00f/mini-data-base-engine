package gogo.dbapp;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import gogo.btree.*;


@SuppressWarnings("serial")
public class Table implements Serializable{
    private String tableName;
    private String clusteringKeyColumn;
    //created arraylist to include all table's pages as strings indicating page's .ser filename
    private ArrayList<String> pages;
    private ArrayList<Object[]>pagesMinMax;
    private int ctr=0;

    public Table(String tableName, String clusteringKeyColumn) throws DBAppException{
        this.tableName = tableName;
        this.clusteringKeyColumn = clusteringKeyColumn;
        //initialize pages arraylists
        pages=new ArrayList<String>();
        pagesMinMax=new ArrayList<Object[]>();
        //serialize table
        try {
        	HDInterface.serialize(tableName,this);
        }catch (Exception e) {
        	throw new DBAppException("Error in serializing table");
        }
    }

    public Hashtable<String, String[]> tableIndices() throws DBAppException{
        Hashtable<String, String[]> htblColNameIndex = new Hashtable<>();
        //This method reads metadata.csv and returns a hashtable of column names and an array of their corresponding column type and index name
        //Table Name, Column Name, Column Type, ClusteringKey, IndexName,IndexType
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/metadata.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(", ");
                if(values[0].equals(tableName)){
                    // store (column name) and (array of column type and index name, respectively)
                    String[] colTypeIndexName = new String[2];
                    colTypeIndexName[0] = values[2];
                    colTypeIndexName[1] = values[4];
                    htblColNameIndex.put(values[1], colTypeIndexName);
                }
            }
            br.close();
        }catch(IOException e){
            throw new DBAppException("Error in reading metadata.csv");
        }
        return htblColNameIndex;
    }

    public void createIndex(String   strColName,
                            String   strIndexName) throws DBAppException {
        //htblColNameIndex store (column name) and (array of 0)column type and 1)index name)
        Hashtable<String, String[]> htblColNameIndex = tableIndices();
        
        //check if column exists in table
        if(!htblColNameIndex.containsKey(strColName)){
            throw new DBAppException("Column does not exist in table");
        }
        //check if index already exists
        if(!htblColNameIndex.get(strColName)[1].equals("null")){
            throw new DBAppException("Index already exists");
        }
        //create index
        //colTypeIndexName is an array of 0)column type and 1)index name, respectively
        String[] colTypeIndexName = htblColNameIndex.get(strColName);
        colTypeIndexName[1] = strIndexName;
        BTree bTree = null;
        switch (colTypeIndexName[0]){
            case "java.lang.Integer":
                bTree = new BTree<Integer, Vector<String>>();
                break;
            case "java.lang.Double":
                bTree = new BTree<Double, Vector<String>>();
                break;
            case "java.lang.String":
                bTree = new BTree<String, Vector<String>>();
                break;
        }
        //loop on all table's pages and add all tuples to the index
        for(String page:pages){
            Page p=(Page)HDInterface.deserialize(page);
            for(Tuple t:p.getTuples()){
                switch (colTypeIndexName[0]){
                    case "java.lang.Integer":
                         bTree.insert((Integer)t.getTupleContent().get(strColName), page);
                        break;
                    case "java.lang.Double":
                         bTree.insert((Double)t.getTupleContent().get(strColName), page);
                        break;
                    case "java.lang.String":
                         bTree.insert((String)t.getTupleContent().get(strColName), page);
                         break;
                }
            }
        }
        try {
            HDInterface.serialize(tableName+"."+strIndexName, bTree);
            //fetch the line in metadata.csv that corresponds to the table and column then update null to index name
            //Table Name, Column Name, Column Type, ClusteringKey, IndexName,IndexType
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/metadata.csv"));
            String line;
            String newLine;
            String content = "";
            while ((line = br.readLine()) != null) {
                String[] values = line.split(", ");
                if(values[0].equals(tableName) && values[1].equals(strColName)){
                    newLine = values[0]+", "+values[1]+", "+values[2]+", "+values[3]+", "+strIndexName+", "+"B+tree";
                    content += newLine + "\n";
                }else{
                    content += line + "\n";
                }
            }
            br.close();
            FileWriter fw = new FileWriter("src/main/resources/metadata.csv");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.flush();
            bw.close();
            
        }catch (Exception e) {
            throw new DBAppException("Error in serializing index");
        }
    }

    //add page
    public Page addPageToTable(String pageName, Object min, Object max){
        Page newPage=new Page();
        pages.add(pageName);
        Object[] minmax=new Object[2];
        minmax[0]=min;
        minmax[1]=max;
        pagesMinMax.add(minmax);
        return newPage;
    }
    //delete page
    public void deletePage(String pageName) {
        int index=pages.indexOf(pageName);
    	pages.remove(pageName);
        pagesMinMax.remove(index);
    }


    public void populateMetadata(Hashtable<String,String>htblColNameType) throws DBAppException {
        //Table Name, Column Name, Column Type, ClusteringKey, IndexName,IndexType
        try{
            FileWriter fw = new FileWriter("src/main/resources/metadata.csv", false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Map.Entry<String, String> entry : htblColNameType.entrySet()) {
                bw.write(tableName+", "+entry.getKey()+", "+entry.getValue()+", "+ clusteringKeyColumn.equals(entry.getKey())+ ", "+ null+", "+null+"\n");
            }
            bw.close();
        }catch(IOException e){
            throw new DBAppException("Error in writing to metadata.csv");
        }
    }
    public boolean checkDataTypes(Hashtable<String,Object>  htblColNameValue) throws DBAppException {
        //check if data types are correct by comparing with metadata
        //Table Name, Column Name, Column Type, ClusteringKey, IndexName,IndexType
        Hashtable<String,String> htblColNameDataType=new Hashtable<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/metadata.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(", ");
                if(values[0].equals(tableName)){
                    htblColNameDataType.put(values[1], values[2]);
                }
            }
            br.close();
        }catch(IOException e){
            throw new DBAppException("Error in reading metadata.csv");
        }
        //check if data types all are correct
        for (Map.Entry<String, Object> entry : htblColNameValue.entrySet()) {
            if(!htblColNameDataType.get(entry.getKey()).equals(entry.getValue().getClass().getName())){
                return false;
            }
        }
        return true;
    }
    public void insert(Hashtable<String,Object>  htblColNameValue) throws DBAppException {
        Page targetPage;
        String targetPageNameOnDisk;
        //check if data types are correct by comparing with metadata
        if(!checkDataTypes(htblColNameValue)) {
            throw new DBAppException("Data types are not correct");
        }
        
        //binary search on pagesMinMax to find the correct page to insert the tuple
        Object clusteringKeyValue=htblColNameValue.get(clusteringKeyColumn);
        int lo=0;
        int hi=pagesMinMax.size()-1;
        int mid=0;
        while(lo<=hi){
            mid=lo+(hi-lo)/2;
            //compare based on type of clustering key column
            if(clusteringKeyValue instanceof Integer){
                if((int)clusteringKeyValue<(int)pagesMinMax.get(mid)[0]){
                    hi=mid-1;
                }else if((int)clusteringKeyValue>(int)pagesMinMax.get(mid)[1]){
                    lo=mid+1;
                }else{
                    break;
                }
            }else if(clusteringKeyValue instanceof Double){
                if((double)clusteringKeyValue<(double)pagesMinMax.get(mid)[0]){
                    hi=mid-1;
                }else if((double)clusteringKeyValue>(double)pagesMinMax.get(mid)[1]){
                    lo=mid+1;
                }else{
                    break;
                }
            }else if(clusteringKeyValue instanceof String){
                if(clusteringKeyValue.toString().compareTo(pagesMinMax.get(mid)[0].toString())<0){
                    hi=mid-1;
                }else if(clusteringKeyValue.toString().compareTo(pagesMinMax.get(mid)[1].toString())>0){
                    lo=mid+1;
                }else{
                    break;
                }
            }
        }

        //if it is the first tuple to be inserted in the table
        if(pages.isEmpty()){
            targetPage=addPageToTable(tableName+"_0",clusteringKeyValue,clusteringKeyValue);
            targetPageNameOnDisk=tableName+"_0";
        }
            
        else{
            targetPage=(Page)HDInterface.deserialize(pages.get(mid));
            targetPageNameOnDisk=pages.get(mid);
        }
        //get last tuple in the target page in case of overflow
        //Tuple lastTuple=targetPage.getTuples().get(targetPage.getTuples().size()-1);            
        
        //insert the tuple in the correct page
        targetPage.insert(new Tuple(htblColNameValue, clusteringKeyColumn),this,mid);




        //get all indices of table and insert accordingly in each index
        Hashtable<String, String[]> htblColNameIndex = tableIndices();
        for (Map.Entry<String, String[]> entry : htblColNameIndex.entrySet()) {
            String[] colTypeIndexName = entry.getValue();
            //check if no index exists, continue
            if(colTypeIndexName[1].equals("null")){
                continue;
            }
            BTree bTree = (BTree)HDInterface.deserialize(tableName+"."+colTypeIndexName[1]);

            switch (colTypeIndexName[0]){
                case "java.lang.Integer":
                    bTree.insert((Integer)htblColNameValue.get(entry.getKey()), targetPageNameOnDisk);
                    break;
                case "java.lang.Double":
                    bTree.insert((Double)htblColNameValue.get(entry.getKey()),targetPageNameOnDisk);
                    break;
                case "java.lang.String":
                    bTree.insert((String)htblColNameValue.get(entry.getKey()), targetPageNameOnDisk);
                    break;
            }
            HDInterface.serialize(tableName+"."+colTypeIndexName[1], bTree);
        }

    }

    public void print() throws DBAppException{
        //print table's name, clustering key column, and pagesMinMax
        System.out.println("******************************");
        System.out.println("Table name: "+getTableName());
        System.out.println("Clustering key column: "+getClusteringKeyColumn());
        System.out.println("Pages MinMax: ");

        for(Object[]minMax:getPagesMinMax()){
            System.out.println(minMax[0].toString()+" "+minMax[1].toString());
        }
        System.out.println("******************************");
        //print the table's pages
        for(String page:getPages()){
            //deserialize the page
            Page p=(Page)HDInterface.deserialize(page);
            System.out.println(page+": ");
            System.out.println(p);
            System.out.println("******************************");
        }
    }

    public String getTableName(){
        return this.tableName;
    }
    public String getClusteringKeyColumn(){
        return this.clusteringKeyColumn;
    }

    public void setTableName(String name){
        this.tableName = name;
    }
    public void setClusteringKeyColumn(String key){
        this.clusteringKeyColumn = key;
    }
    public ArrayList<String> getPages(){
        return this.pages;
    }
    public void setPages(ArrayList<String> pages){
        this.pages = pages;
    }
    public ArrayList<Object[]> getPagesMinMax(){
        return this.pagesMinMax;
    }
    public void setPagesMinMax(ArrayList<Object[]> pagesMinMax){
        this.pagesMinMax = pagesMinMax;
    }

    public int getCtr(){
        return this.ctr;
    }
    public void incrementCtr(){
        this.ctr++;
    }
}