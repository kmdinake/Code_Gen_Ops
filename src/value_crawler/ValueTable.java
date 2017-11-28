package value_crawler;

import java.util.Stack;

public class ValueTable
{
	private Stack<STNode> list = new Stack<STNode>();
	int nums = 0;
	int strings = 0;
	int procs = 0;

    private class STNode
	{
		public String name;
		public String newName;
		public char type;
		int scope;
		
		STNode(String name, String newName, char type, int scope)
		{
			this.name = name;
			this.newName = newName;
			this.type = type;
			this.scope = scope;
		}
	}
    void add(String name, char type, int scope)
    {
        String newType = "";
        if (type == 'n' )
        {
            newType = "n" + nums++;
        }
        else if (type == 's')
        {
            newType = "s" + strings++;
        }
        else if (type == 'p')
        {
            newType = "p" + procs++;
        }
        list.add(new STNode(name, newType, type, scope));
    }

    boolean contains(String name, char type, int scope)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.elementAt(i).name.equals(name)
                    && list.elementAt(i).type == type
                    && list.elementAt(i).scope == scope)
                return true;
        }
        return false;
    }

    String getNewName(String name, char type, int scope)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.elementAt(i).name.equals(name)
                    && list.elementAt(i).type == type
                    && list.elementAt(i).scope == scope)
                return list.elementAt(i).newName;
        }
        return "";
    }


}