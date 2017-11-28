package lexer;


public class TokenList
{
	private TokenNode head;
	private TokenNode tail;
	private int count;
	
	public TokenList()
	{
		head = null;
		tail = null;
		count = 0;		
	}
	
	public void addToken(String tokenClass, String snippet)
	{
		TokenNode newNode = new TokenNode(count, tokenClass, snippet);
		
		if (isEmpty())
		{
			head = newNode;
			tail = newNode;
		}
		else
		{
			tail.next = newNode;
			tail = newNode;
		}
		count ++;
	}
	
	boolean isEmpty()
	{
		return head == null;
	}
	
	public String toString()
	{
		TokenNode cur = head;
		String outString = "";
		while (cur != null)
		{
			outString += cur. tokenNo + "\t" + cur.tokenClass + "\t" + cur.snippet + "\n";
			cur = cur.next;
		}
		return outString;
	}
	
}