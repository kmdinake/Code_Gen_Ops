package parser;
public class TableItem extends TreeNode {
    public char type;
    public int scope;
    public char defined;

    public TableItem(int number, String tokenClass, String snippet){
        super(number, tokenClass, snippet);
        type = '\0';
        defined = 'u';
        newName = "";
    }

    @Override
    public String toString() {
        return tokenNo + "\t" + tokenClass + "\t" + snippet + "\t" + type + "\t" + scope + "\t" + defined + "\t" + newName;
    }
}
