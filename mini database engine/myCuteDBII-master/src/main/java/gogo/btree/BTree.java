package gogo.btree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A B+ tree
 * Since the structures and behaviors between internal node and external node are different, 
 * so there are two different classes for each kind of node.
 * @param <TKey> the data type of the key
 * @param <TValue> the data type of the value
 */
public class BTree<TKey extends Comparable<TKey>, TValue> implements Serializable {
	private BTreeNode<TKey> root;
	
	public BTree() {
		this.root = new BTreeLeafNode<TKey, TValue>();
	}

	/**
	 * Insert a new key and its associated value into the B+ tree.
	 */
	public void insert(TKey key, String value) {
		Vector<String> values = new Vector<String>();
		values.add(value);
		insert(key, (TValue)values);
		}
		
	public void insert(TKey key, TValue value) {
		//search first if key already exists, if it does, update the value
		if(search(key)!=null){
			Vector<String> values = (Vector<String>)search(key);
			String val = ((Vector<String>)value).get(0);
			//check if val already exists, if it does, do nothing
			if(values.contains(val))
				 return;
			values.add((String)val);
			return;
		}
		//else insert vector of a single string value
		BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);
		leaf.insertKey(key, value);
		
		if (leaf.isOverflow()) {
			BTreeNode<TKey> n = leaf.dealOverflow();
			if (n != null)
				this.root = n; 
		}
	}
	
	/**
	 * Search a key value on the tree and return its associated value.
	 */
	public TValue search(TKey key) {
		BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);
		
		int index = leaf.search(key);
		return (index == -1) ? null : leaf.getValue(index);
	}
	/**
	 * update the value of the key
	 */
	public void update(TKey key, TValue value) {
		BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);
		int index = leaf.search(key);
		if (index != -1) {
			leaf.setValue(index, value);
		}
	}
	/**
	 * search by range of keys
	 */
	public ArrayList<TValue> searchRangeGreaterThan(TKey key, boolean inclusive){
		ArrayList<TValue> result = new ArrayList<TValue>();

		BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);
			
		int index = leaf.search(key);
		if(index==-1)
			index = 0;

		while (leaf != null) {
			for (int i = index; i < leaf.getKeyCount(); i++) {
				if(inclusive){
					if(leaf.getKey(i).compareTo(key)>=0) result.add(leaf.getValue(i));
				}
				else
					if(leaf.getKey(i).compareTo(key)>0) result.add(leaf.getValue(i));
			}
			leaf = leaf.getNext();
			index = 0;
		}
		return result;
	}

	public ArrayList<TValue> searchRangeLessThan(TKey key, boolean inclusive) {
		ArrayList<TValue> result = new ArrayList<TValue>();

		BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);

		int index = leaf.search(key);
		if (index == -1)
			index = leaf.getKeyCount() - 1;

		while (leaf != null) {
			for (int i = index; i >= 0; i--) {
				if (inclusive) {
					if (leaf.getKey(i).compareTo(key) <= 0)
						result.add(leaf.getValue(i));
				} else {
					if (leaf.getKey(i).compareTo(key) < 0)
						result.add(leaf.getValue(i));
				}
			}
			leaf = leaf.getPrevious();
			if (leaf != null)
				index = leaf.getKeyCount() - 1;
		}
		return result;
	}

	public ArrayList<TValue> searchRange(TKey key1, TKey key2) {
		ArrayList<TValue> result = new ArrayList<TValue>();
		BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key1);
		int index = leaf.search(key1);
		while (leaf != null) {
			for (int i = index; i < leaf.getKeyCount(); i++) {
				if (leaf.getKey(i).compareTo(key2) <= 0) {
					result.add(leaf.getValue(i));
				} else {
					return result;
				}
			}
			leaf = leaf.getNext();
			index = 0;
		}
		return result;
	}
	
	/**
	 * Delete a key and its associated value from the tree.
	 */
	public void delete(TKey key) {
		BTreeLeafNode<TKey, TValue> leaf = this.findLeafNodeShouldContainKey(key);
		
		if (leaf.delete(key) && leaf.isUnderflow()) {
			BTreeNode<TKey> n = leaf.dealUnderflow();
			if (n != null)
				this.root = n; 
		}
	}
	/**
	 * print the tree by level order showing the keys in each node in a pyramid shape
	 */
	public void print() {
		Hashtable<TKey, TValue> result = new Hashtable<TKey, TValue>();
		ArrayList<BTreeNode<TKey>> list = new ArrayList<BTreeNode<TKey>>();
		list.add(this.root);
		while (!list.isEmpty()) {
			ArrayList<BTreeNode<TKey>> next = new ArrayList<BTreeNode<TKey>>();
			for (BTreeNode<TKey> node : list) {
				if (node.getNodeType() == TreeNodeType.InnerNode) {
					BTreeInnerNode<TKey> inner = (BTreeInnerNode<TKey>)node;
					System.out.print("[ ");
					for (int i = 0; i < inner.getKeyCount(); i++) {
						System.out.print(inner.getKey(i) + " ");
					}
					System.out.print("]");
					for (int i = 0; i <= inner.getKeyCount(); i++) {
						next.add(inner.getChild(i));
					}
				} else {
					@SuppressWarnings("unchecked")
					BTreeLeafNode<TKey, TValue> leaf = (BTreeLeafNode<TKey, TValue>)node;
					System.out.print("[ ");
					for (int i = 0; i < leaf.getKeyCount(); i++) {
						result.put(leaf.getKey(i), leaf.getValue(i));
						System.out.print(leaf.getKey(i) + " ");
					}
					System.out.print("]");
					System.out.print("->");
				}
			}
			System.out.println();
			list = next;
		}
		//print the entries in the hashtable
		System.out.println("Entries in the tree:");
		for(TKey key:result.keySet()){
			System.out.println("Key: "+key+" ---> "+result.get(key));
		}
	}
	
	/**
	 * Search the leaf node which should contain the specified key
	 */
	@SuppressWarnings("unchecked")
	private BTreeLeafNode<TKey, TValue> findLeafNodeShouldContainKey(TKey key) {
		BTreeNode<TKey> node = this.root;
		while (node.getNodeType() == TreeNodeType.InnerNode) {
			node = ((BTreeInnerNode<TKey>)node).getChild( node.search(key) );
		}
		
		return (BTreeLeafNode<TKey, TValue>)node;
	}
}
