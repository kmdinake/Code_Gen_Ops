package parser;

import java.util.*; 
import lexer.TokenNode; 


public class TreeNode extends TokenNode {
	protected LinkedList<TreeNode> children;
	protected TreeNode parent;
	private InfoTable tableEntry;
	public char type;
    public String newName;


    public TreeNode(int number, String tokenClass, String snippet) {
		super(number, tokenClass, snippet);
		children = new LinkedList<TreeNode>();
		tableEntry = new InfoTable();
		parent= null;
	}

	public void addChild(TreeNode node) {
		node.parent = this; 
		children.addFirst(node);
	}

	//TODO: implement addParent, so that the right hand side of the production can be linked to the left hand side 
	// Left is parent, right is children 

	public LinkedList<TreeNode> getChildren() {
		return children; 
	}
	
	public int childrenSize()
	{		
		return children.size();
	}
	
	public TreeNode getChild(int i)
	{
		return children.get(i);
	}
	
	public String toString()
	{
		String ret;
		if (parent != null)
		ret = "||Node " + tokenNo + "\t" + tokenClass 
				+ "\t" + snippet + " Parent: " + parent.tokenClass + "||";
		else 
		ret = "||Node " + tokenNo + "\t" + tokenClass 
				+ "\t" + snippet + " Parent: nun||";	
		return ret;
	}
		
	public void prune()
	{
		for (int i = 0; i < children.size(); i++)
		{
			if 	(children.get(i).snippet.equals("{") || 
				children.get(i).snippet.equals("}") || 
				children.get(i).snippet.equals("(") || 
				children.get(i).snippet.equals(")") || 
				children.get(i).snippet.equals(";") || 
				children.get(i).snippet.equals(",") )
			{
				children.remove(i);
				i = 0;
			}
		}
		
		for (int i = 0; i < children.size(); i++)
		{
			children.get(i).prune();
		}
	
	}

	public TreeNode getParent() {
		return parent;
	}
}