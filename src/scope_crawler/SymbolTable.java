package scope_crawler;

import java.util.Stack;

public class SymbolTable
{
	private Stack<STNode> stack = new Stack<STNode>();
	private class STNode
	{
		public String name;
		public char type;
		
		STNode(String name, char type)
		{
			this.name = name;
			this.type = type;			
		}
	}
	
	public boolean empty()
	{
		return stack.empty();
	}
	
	public void bind(String name, char type)
	{
		if (!lookup(name, type))stack.push(new STNode(name, type));
	}
	
	public boolean lookup(String name, char type)
	{
		for (int i = 0; i < stack.size(); i++)
		{
			if (stack.elementAt(i).name.equals(name) && stack.elementAt(i).type == type)
				return true;
		}
		return false;
	}
	
	public void enter()
	{
		stack.push(new STNode("mark", '#'));		
	}
	
	public void exit()
	{
		while (!stack.empty() && stack.pop().name != "mark") {};
	}
}