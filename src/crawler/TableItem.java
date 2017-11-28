package crawler;

import parser.*;
public class TableItem extends TreeNode {
    public char type;
    public TableItem(int number, String tokenClass, String snippet){
        super(number, tokenClass, snippet);
        type = '\0';
    }

    public TableItem getChild(int i)
    {
        return (TableItem) children.get(i);
    }

    public void setChild(TreeNode node){
        children.add(node);
    }
}
