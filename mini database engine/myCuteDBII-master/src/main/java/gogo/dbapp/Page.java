package gogo.dbapp;

import java.util.Collections;
import java.util.Vector;
import java.io.Serializable;
public class Page implements Serializable {
	Vector<Tuple> tuples;
	public Page() {
		tuples=new Vector<>();
	}


	public void insert(Tuple t, Table table,int currentPageIndex) throws DBAppException {
		String clusteringkey=table.getClusteringKeyColumn();
		String clusteringKeyValue=t.getTupleContent().get(clusteringkey).toString();
		Page nextPage=null;

		int index= Collections.binarySearch(tuples,t);
		if(index<0)
			index=-(index+1);
		else
			throw new DBAppException("Primary key of that value already exists");

		if(tuples.size()<4){
			tuples.add(index,t);
		}
		//if page is full
		else{
			//if my tuple is less than the last tuple in the page, shift the last tuple to the next page
			if(t.compareTo(tuples.get(tuples.size()-1))<0){
				Tuple lastTuple=tuples.remove(tuples.size()-1);
				//add my tuple to current page
				tuples.add(index,t);
				//if the next page is null, create a new page
				if(currentPageIndex+1>=table.getPages().size()){
					nextPage=new Page();
					table.incrementCtr();
					table.addPageToTable(table.getTableName()+"_"+table.getCtr(), lastTuple.getTupleContent().get(clusteringkey),lastTuple.getTupleContent().get(clusteringkey));
				}
				else{
					//if the next page is not null, insert the tuple in the next page
					nextPage=(Page)HDInterface.deserialize(table.getPages().get(currentPageIndex+1));
				}
					nextPage.insert(lastTuple,table,currentPageIndex+1);
				}
			else{
				//if my tuple is greater than the last tuple in the page, insert it in the next page
				if(currentPageIndex+1>=table.getPages().size()){
					nextPage=new Page();
					table.incrementCtr();
					table.addPageToTable(table.getTableName()+"_"+table.getCtr(),clusteringKeyValue,clusteringKeyValue);
				}
				else{
					nextPage=(Page)HDInterface.deserialize(table.getPages().get(currentPageIndex+1));
				}
				nextPage.insert(t,table,currentPageIndex+1);
			}
			}
		//update the pagesMinMax arraylist
		table.getPagesMinMax().get(currentPageIndex)[0]=tuples.get(0).getTupleContent().get(clusteringkey);
		table.getPagesMinMax().get(currentPageIndex)[1]=tuples.get(tuples.size()-1).getTupleContent().get(clusteringkey);

		//serialize the current page
		HDInterface.serialize(table.getTableName()+"_"+currentPageIndex,this);
		}

		public String toString() {
			String result="";
			int i=0;
			for(Tuple t:tuples) {
				if(i!=tuples.size()-1)
					result+=t.toString()+",\n";
				else
					result+=t.toString()+"\n";
				i++;
			}
			return result;
		}


        public Vector<Tuple> getTuples() {
            return tuples;
        }


}
