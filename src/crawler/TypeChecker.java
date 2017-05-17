package crawler;

import parser.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeChecker {
	private char [] validTypes = {
		'\0', // signals error state i.e unbounded 0
		'w', // well-typed 1
		'p', // procedure 2
		'n', // numbers 3
		'o', // output 4
		's', // strings 5
		'b', // booleans 6
		'k' // keywords 7
	};

	public void doTypeChecking(TreeNode node, InfoTable table){
		System.out.println("Starting type checking process:\n---------------------------------------------");
		initializeTable(node, table);
		visitAST(node, table);
		checkTree(table);
		writeToFile(node);
	}

	private void writeToFile(TreeNode node) {
		try{
			String t_c_r = "TypeCheckerReport";
			FileWriter fw = new FileWriter(t_c_r);
			fw.write(stringyfi(node));
			fw.close();
			System.out.println("Type Checker report saved to file " + t_c_r);
			System.out.println("---------------------------------------------");
		} catch (IOException e){
			System.out.println("Ooops.. failed to write type checks to file");
		}
	}

	private String stringyfi(TreeNode node) {
		return DFS_print(node, 0);
	}

	private String DFS_print(TreeNode node, int depth) {
		String str = "";
		if(node != null){
			for (int i = 0; i < depth; i++)
				str += "\t";
			str += ("Node [" + node.tokenNo + "]\n");
			for (int i = 0; i < depth; i++)
				str += "\t";
			str += ("|\tClass: " + node.tokenClass + "\n");
			for (int i = 0; i < depth; i++)
				str += "\t";
			str += ("|\tSnippet: " + node.snippet + "\n");
			for (int i = 0; i < depth; i++)
				str += "\t";
			str += ("|\tType: " + getTypeOf(node.type) + "\n");
			if(node.childrenSize() > 0){
				for (int i = 0; i < depth; i++)
					str += "\t";
				str += "|\tChildren:\n";
			}
			for (int i = 0; i < node.childrenSize(); i++)
				str += DFS_print(node.getChild(i), depth + 1);
		}
		return str;
	}

	private void checkTree(InfoTable table) {
		if(table != null) {
			int i = 0;
			while (table.get(i) != null) {
				if (table.get(i).type == '\0' &&
					!table.get(i).tokenClass.equals("keyword") &&
					!table.get(i).tokenClass.equals("assignment operator") &&
					!table.get(i).tokenClass.equals("comparison symbol") &&
					!table.get(i).tokenClass.equals("user-defined name"))
				{
					System.out.println("Type Error Occurred at:");
					System.out.println("Node [" + table.get(i).tokenNo + "]");
					System.out.println("|\tClass: " + table.get(i).tokenClass);
					System.out.println("|\tSnippet: " + table.get(i).snippet);
					System.out.println("---------------------------------------------");
					System.exit(0);
				}
				i++;
			}
			System.out.println("Type Checking Successful");
		}
	}

	/**
	 * This function will recursively walk through the Abstract Syntaxt Tree (AST)
	 * and create table entries for each node within the AST
	 * @param node
	 * @param table
	 */
	private void initializeTable(TreeNode node, InfoTable table) {
		if(node == null){
			System.out.println("Ooops.. Current node of the syntax tree is undefined!\nType Checking Aborted.");
			System.out.println("---------------------------------------------");
			System.exit(0);
		} else if(table == null){
			System.out.println("Ooops.. Information table from the parsing phase is undefined!\nType Checking Aborted.");
			System.out.println("---------------------------------------------");
			System.exit(0);
		} else {
			table.insert(node.tokenNo, node.tokenClass, node.snippet);
			if (node.childrenSize() > 0) {
				int size = node.childrenSize();
				for (int i = 0; i < size; i++) {
					initializeTable(node.getChild(i), table);
				}
			}
		}
	}

	/**
	 * This function will recursively walk through the Abstract Syntaxt Tree (AST)
	 * and based on the Grammar rules defined for the SPL language,
	 * it will assign types to each node in the AST if successful, or report an error
	 * @param node
	 * @param table
	 */
	private void visitAST(TreeNode node, InfoTable table){
		//TableItem node = new TableItem(node1.tokenNo, node1.tokenClass, node1.snippet);

		// trivial case
		if(node == null){ return; }

		// Number Syntactic Category, Symbol: n
		if(node.tokenClass.equals("number") || node.tokenClass.equals("integer")){
			if(node.type == '\0'){
				node.type = validTypes[3];
				table.setType(findIndex(node, table), validTypes[3]);
                                
			}
		}
                //io??
		// user-defined name Syntactic Category, Symbol: u
		if(node.tokenClass.equals("user-defined name")){
			if(node.getParent().tokenClass.equals("N")){
				node.type = validTypes[3]; // number
				table.setType(findIndex(node, table), validTypes[3]);
			} else if(node.getParent().tokenClass.equals("S")){
				node.type = validTypes[5]; // string
				table.setType(findIndex(node, table), validTypes[5]);
			}  else if(node.getParent().tokenClass.equals("Y")){
				node.type = validTypes[2]; // procedure
				table.setType(findIndex(node, table), validTypes[2]);
			} else {
				node.type = validTypes[7];
				table.setType(findIndex(node, table), validTypes[7]);
			}
                        
		}

		// short string Syntactic Category, Symbol: s
		if(node.tokenClass.equals("short string")){
			if(node.type == '\0'){
				node.type = validTypes[5];
				table.setType(findIndex(node, table), validTypes[5]);
			}
                    
		}
                
		if(node.tokenClass.equals("keyword")
		|| node.tokenClass.equals("assignment operator")
		|| node.tokenClass.equals("comparison symbol"))
		{

			if(node.type == '\0'){
				node.type = validTypes[7]; // keywords
				table.setType(findIndex(node, table), validTypes[7]);
			}
		}
               
                //Boolean Syntactic Category, Symbol: e, n, a, o
		Pattern p = Pattern.compile("eq|not|and|or|comparison symbol");
		Matcher m = p.matcher(node.tokenClass);
		if(m.find()){

                        if(node.type == '\0' || node.type == validTypes[7] && !node.snippet.equals("halt") ){
				node.type = validTypes[6]; // boolean
				table.setType(findIndex(node, table), validTypes[6]);
			}
		}
                
		// NVAR Syntactic Category, Symbol: N
		if(node.tokenClass.equals("N")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				if (node.getChild(0).type == validTypes[3]){
					node.type = validTypes[3]; // number
					table.setType(findIndex(node, table), validTypes[3]);
				} else {
					reportError(node, "number", getTypeOf(node.type));
				}
			} else {
				node.getChild(0).type = node.type; // propagate type ends here
				table.setType(findIndex(node.getChild(0), table), node.type);
			}
                        return;
		}

		// SVAR Syntactic Category, Symbol: S
		if(node.tokenClass.equals("S")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				if(node.getChild(0).type == validTypes[5]){
					node.type = validTypes[5]; //string
					table.setType(findIndex(node, table), validTypes[5]);
				} else {
					reportError(node, "String", getTypeOf(node.getChild(0).type));
				}
			} else {
				node.getChild(0).type = node.type; // propagate type ends here
				table.setType(findIndex(node.getChild(0), table), node.type);
			}
                        return;
		}

		// Intermediate Syntactic Category, Symbol: T, U
		if(node.tokenClass.equals("T") || node.tokenClass.equals("U")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				if(node.getChild(0).type == validTypes[5]){ // SVAR
					node.type = validTypes[5]; // string
					table.setType(findIndex(node, table), validTypes[5]);
				} else if(node.getChild(0).type == validTypes[3]){ // NVAR
					node.type = validTypes[3]; // number
					table.setType(findIndex(node, table), validTypes[3]);
				} else {
					if(node.getChild(0).tokenClass.equals("S"))
						reportError(node, "String", getTypeOf(node.getChild(0).type));
					else if (node.getChild(0).tokenClass.equals("N"))
						reportError(node, "Number", getTypeOf(node.getChild(0).type));
					else
						reportError(node, "", "");
				}
			} else if(node.type != '\0' && node.tokenClass.equals("T")){
				node.getChild(0).type = node.type;
				table.setType(findIndex(node.getChild(0), table), node.type);
				visitAST(node.getChild(0), table); // propagate type
			}
                        return;
		}

		// Calculation Syntactic Category Symbol: L
		if(node.tokenClass.equals("L")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				visitAST(node.getChild(1), table);
				visitAST(node.getChild(2), table);
				if(node.getChild(1).type == validTypes[3] && node.getChild(2).type == validTypes[3]){
					node.type = validTypes[3]; // number
					table.setType(findIndex(node, table), validTypes[3]);
				} else {
					reportError(node, "Number", getTypeOf(node.getChild(1).type));
				}
			}
                        
                        return;
		}

		// NUMEXPR Syntactic Category, Symbol: X
		if(node.tokenClass.equals("X")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				if(node.getChild(0).type == validTypes[3]){
					node.type = validTypes[3]; // number
					table.setType(findIndex(node, table), validTypes[3]);
				} else {
					reportError(node, "Number", getTypeOf(node.getChild(0).type));
				}
			}
                        
                       
		}

		// Boolean Syntactic Category, Symbol: B
		if(node.tokenClass.equals("B")){
			if(node.type == '\0'){
				if(node.childrenSize() == 2){ // not
					visitAST(node.getChild(0), table);
					visitAST(node.getChild(1), table);
					if(node.getChild(1).type == validTypes[6]){
						node.type = validTypes[6]; // boolean
						table.setType(findIndex(node, table), validTypes[6]);
					} else {
                                          
						reportError(node, "Boolean", getTypeOf(node.getChild(1).type));
					}
				} else if (node.childrenSize() == 3){ // eq, and, or, <, >
                                    
					visitAST(node.getChild(0), table);
					visitAST(node.getChild(1), table);
					visitAST(node.getChild(2), table);
					if(node.getChild(0).type == validTypes[4] && node.getChild(2).type == validTypes[4]){ // VAR
						node.type = validTypes[6]; // boolean
						table.setType(findIndex(node, table), validTypes[6]);
                                        }
                                        else if(node.getChild(0).type == validTypes[7] && node.getChild(2).type == validTypes[7]){
                                                node.type = validTypes[6]; // boolean
						table.setType(findIndex(node, table), validTypes[6]);
                                                       
					} else if(node.getChild(0).type == validTypes[3] && node.getChild(2).type == validTypes[3]){ // NVAR
						node.type = validTypes[6]; // boolean
						table.setType(findIndex(node, table), validTypes[6]);
					} else if(node.getChild(0).type == validTypes[6]){ // Boolean
						if(node.getChild(1).type == validTypes[4] && node.getChild(2).type == validTypes[4]){ // VAR
							node.type = validTypes[6]; // boolean
							table.setType(findIndex(node, table), validTypes[6]);
						} else if(node.getChild(1).type == validTypes[3] && node.getChild(2).type == validTypes[3]){ // NVAR
							node.type = validTypes[6]; // boolean
							table.setType(findIndex(node, table), validTypes[6]);
						} else{
							reportError(node, "", "");
						}
					} else {
						if(node.getChild(0).type != validTypes[4] || node.getChild(0).type != validTypes[3] || node.getChild(0).type != validTypes[6])
							reportError(node, "Boolean", getTypeOf(node.getChild(0).type));
						else if (node.getChild(2).type != validTypes[4] || node.getChild(2).type != validTypes[3] || node.getChild(2).type != validTypes[6])
							reportError(node, "Boolean", getTypeOf(node.getChild(2).type));
						else
							reportError(node, "", "");
					}
				} else {
					System.out.println("Boolean Expression Error: Too many arguments");
				}
			}
                     
		}

		// Variable Syntactic Category, Symbol: V
		if(node.tokenClass.equals("V")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				if(node.getChild(0).type == validTypes[3] || node.getChild(0).type == validTypes[5]){
					node.type = validTypes[4];
					table.setType(findIndex(node, table), validTypes[4]); // output
				} else {
					reportError(node, "Output", getTypeOf(node.getChild(0).type));
				}
			}
                      
		}

		// Y Syntactic Category : CALL
		if(node.tokenClass.equals("Y")){
			if (node.childrenSize() > 0){
				if (node.type == '\0'){
					visitAST(node.getChild(0), table);
					if(node.getChild(0).type == validTypes[2]) { // procedure
						node.type = validTypes[1];
						table.setType(findIndex(node, table), validTypes[1]);
					} else {
						reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
					}
				}
			}
                     
		}

		// Conditional Loop Syntactic Category, Symbol: Z
		if(node.tokenClass.equals("Z")){
			if(node.type == '\0'){
				if(node.childrenSize() == 3){
					visitAST(node.getChild(0), table);
					visitAST(node.getChild(1), table);
					visitAST(node.getChild(2), table);
					if(node.getChild(1).type == validTypes[6] && node.getChild(2).type == validTypes[1]){
						node.type = validTypes[1]; // well-typed
						table.setType(findIndex(node, table), validTypes[1]);
					} else {
						if(node.getChild(1).type != validTypes[6])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(1).type));
						else if(node.getChild(2).type != validTypes[1])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(2).type));
						else
							reportError(node, "", "");
					}
				} else if (node.childrenSize() == 13){
					Boolean isOk = true;
					visitAST(node.getChild(0), table);
					visitAST(node.getChild(1), table);
					visitAST(node.getChild(2), table);
					visitAST(node.getChild(3), table);
					if(node.getChild(1).type != validTypes[3] ||
							node.getChild(3).type != validTypes[3]){
						isOk = false;
						if(node.getChild(1).type == validTypes[3])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(3).type));
						else
							reportError(node, "Well-Typed", getTypeOf(node.getChild(1).type));
					}
					if(isOk){
						visitAST(node.getChild(4), table);
						visitAST(node.getChild(5), table);
						visitAST(node.getChild(6), table);
						if(node.getChild(4).type != validTypes[3] ||
								node.getChild(6).type != validTypes[3]){
							isOk = false;
							if(node.getChild(4).type == validTypes[3])
								reportError(node, "Well-Typed", getTypeOf(node.getChild(6).type));
							else
								reportError(node, "Well-Typed", getTypeOf(node.getChild(4).type));
						}
						if(isOk){
							visitAST(node.getChild(7), table);
							visitAST(node.getChild(8), table);
							visitAST(node.getChild(9), table);
							visitAST(node.getChild(10), table);
							visitAST(node.getChild(11), table);
							if(node.getChild(7).type != validTypes[3] ||
									node.getChild(10).type != validTypes[3] ||
									node.getChild(11).type != validTypes[3]){
								isOk = false;
								if(node.getChild(7).type == validTypes[3]) {
									if (node.getChild(10).type == validTypes[3])
										reportError(node, "Well-Typed", getTypeOf(node.getChild(11).type));
									else
										reportError(node, "Well-Typed", getTypeOf(node.getChild(10).type));
								} else if(node.getChild(10).type == validTypes[3]) {
									if (node.getChild(7).type == validTypes[3])
										reportError(node, "Well-Typed", getTypeOf(node.getChild(11).type));
									else
										reportError(node, "Well-Typed", getTypeOf(node.getChild(7).type));
								} else if(node.getChild(11).type == validTypes[3]) {
									if (node.getChild(7).type == validTypes[3])
										reportError(node, "Well-Typed", getTypeOf(node.getChild(10).type));
									else
										reportError(node, "Well-Typed", getTypeOf(node.getChild(7).type));
								} else {
									reportError(node, "", "");
								}
							}
							if(isOk){
								visitAST(node.getChild(12), table);
								if(node.getChild(12).type != validTypes[1]){
									isOk = false;
									reportError(node, "Well-Typed", getTypeOf(node.getChild(12).type));
								}
							}
						}
					}

					if(isOk){
						node.type = validTypes[1]; // well-typed
						table.setType(findIndex(node, table), validTypes[1]);
					} else {
						reportError(node, "", "");
					}
				} else {
					System.out.println("Conditional Loop Expression Error: Invalid number of arguments");
				}
			}
                        
                 
		}

		// Conditional Branch Syntactic Category, Symbol: W
		if(node.tokenClass.equals("W")){
			if(node.type == '\0'){
				visitAST(node.getChild(1), table); // bool
				visitAST(node.getChild(3), table); // code
				if(node.childrenSize() == 4) {
					if(node.getChild(1).type == validTypes[6] && node.getChild(3).type == validTypes[1]){
						node.type = validTypes[1]; // well-typed
						table.setType(findIndex(node, table), validTypes[1]);
					} else {
						if(node.getChild(1).type != validTypes[6])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(1).type));
						else if (node.getChild(3).type != validTypes[1])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(3).type));
						else
							reportError(node, "", "");
					}
				} else if(node.childrenSize() == 6){
					visitAST(node.getChild(5), table);
					if(node.getChild(1).type == validTypes[6] &&
						node.getChild(3).type == validTypes[1] &&
						node.getChild(5).type == validTypes[1])
					{
						node.type = validTypes[1]; // well-typed
						table.setType(findIndex(node, table), validTypes[1]);
					} else {
						if(node.getChild(1).type != validTypes[6])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(1).type));
						else if (node.getChild(3).type != validTypes[1])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(3).type));
						else if (node.getChild(5).type != validTypes[1])
							reportError(node, "Well-Typed", getTypeOf(node.getChild(5).type));
						else
							reportError(node, "", "");
					}
				} else {
					System.out.println("Conditional Branch Expression Error: Invalid number of arguments");
				}
			}
                   
		}

		// Assign Syntactic Category, Symbol: A
		if(node.tokenClass.equals("A")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				visitAST(node.getChild(1), table);
				visitAST(node.getChild(2), table);
				if(node.getChild(0).type == validTypes[5] && node.getChild(2).type == validTypes[5]){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else if(node.getChild(0).type == validTypes[3] && node.getChild(2).type == validTypes[3]){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					if(node.getChild(2).type != validTypes[3] && node.getChild(2).type != validTypes[5]){
						reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
					} else {
						node.type = validTypes[1];
						table.setType(findIndex(node, table), validTypes[1]);
						node.getChild(0).type = node.getChild(2).type;
						table.setType(findIndex(node.getChild(0), table), node.getChild(2).type);
						visitAST(node.getChild(0), table);  // propagate starts here
					}
				}
			}
                       
		}

		// O Syntactic Category : IO
		if(node.tokenClass.equals("O")){
			if (node.childrenSize() >0){
				if (node.type == '\0'){
					visitAST(node.getChild(0), table);
					visitAST(node.getChild(1), table);
					if(node.getChild(1).type == validTypes[4]) {// output
						node.type = validTypes[1]; // well-typed
						table.setType(findIndex(node, table), validTypes[1]);
					} else {
						reportError(node, "Well-Typed", getTypeOf(node.getChild(1).type));
					}
				}
			}
                        
                        
		}

		// I Syntactic Category : INSTR
		if(node.tokenClass.equals("I")){
			if (node.childrenSize() >0){
				if (node.getChild(0).tokenClass.equals("keyword")){
					if (node.type == '\0'){
						visitAST(node.getChild(0), table);
						if(node.getChild(0).type == validTypes[7]) { // for halt -> keyword
							node.type = validTypes[1];
							table.setType(findIndex(node, table), validTypes[1]);
						} else {
							reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
						}
					}
				}
				else  { //for O A W Z Y
					visitAST(node.getChild(0), table);
					if (node.type == '\0' && node.getChild(0).type == 'w'){
						node.type = validTypes[1];
						table.setType(findIndex(node, table), validTypes[1]);
					} else {
						reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
					}
				}
			}
                        return;
		}

		// R Syntactic Category : PROC
		if(node.tokenClass.equals("R")){
			//enter symbol table
			visitAST(node.getChild(0), table);
			visitAST(node.getChild(2), table);
			//exit symbol table
			if(node.type == '\0' && node.getChild(1).type == '\0'
					&& node.getChild(2).type == 'w'){
				node.getChild(1).type = validTypes[2];
				table.setType(findIndex(node.getChild(1), table), validTypes[2]);
				//perhaps bind in symbol table
				node.type = validTypes[1];
				table.setType(findIndex(node, table), validTypes[1]);
			}
		}

		// C Syntactic Category : CODE
		if(node.tokenClass.equals("C")){
			if (node.childrenSize() == 1){
				visitAST(node.getChild(0), table);
				if(node.type == '\0' && node.getChild(0).type == 'w'){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
				}
			}
			else if (node.childrenSize() == 2) {
				visitAST(node.getChild(0), table);
				visitAST(node.getChild(1), table);
				if (node.type == '\0' && node.getChild(0).type == 'w' && node.getChild(1).type == 'w'){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
				}
			}
		}

		// D Syntactic Category : PROC_DEFS
		if(node.tokenClass.equals("D")){
			if (node.childrenSize() == 1){
				visitAST(node.getChild(0), table);
				if(node.type == '\0' && node.getChild(0).type == 'w'){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
				}
			}
			else if (node.childrenSize() == 2){
				visitAST(node.getChild(0), table);
				visitAST(node.getChild(1), table);
				if (node.type == '\0' && node.getChild(0).type == 'w' && node.getChild(1).type == 'w'){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
				}
			}
		}

		// P Syntactic Category : PROG
		if(node.tokenClass.equals("P")){
			if (node.childrenSize() == 1){
				visitAST(node.getChild(0), table);
				if(node.type == '\0' && node.getChild(0).type == 'w'){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
				}
			}
			else if (node.childrenSize() == 2){
				visitAST(node.getChild(0), table);
				visitAST(node.getChild(1), table);
				if (node.getChild(0).type == 'w' && node.getChild(1).type == 'w'){
					node.type = validTypes[1];
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
				}
			}
		}

		if(node.tokenClass.equals("Q")){
			if(node.type == '\0'){
				visitAST(node.getChild(0), table);
				if(node.getChild(0).type == validTypes[1]){
					node.type = validTypes[1]; // well-typed
					table.setType(findIndex(node, table), validTypes[1]);
				} else {
					reportError(node, "Well-Typed", getTypeOf(node.getChild(0).type));
				}
			}
			//this means tree is done, so check if all have type symbols
		}
               
	}

	private String getTypeOf(char type) {
		if(type == 'w'){
			return "Well-Typed";
		} else if (type == 'p'){
			return "Procedure";
		} else if (type == 'n'){
			return "Number";
		} else if(type == 'o'){
			return "Output";
		} else if (type == 's'){
			return "String";
		} else if (type == 'b'){
			return "Boolean";
		} else if (type == 'k'){
			return "Keyword";
		} else {
			return "Unidentified Type";
		}
	}

	private void reportError(TreeNode node, String guess, String actual){
		System.out.println("Type Error Occurred at:");
		System.out.println("Node ["+node.tokenNo+"]");
		System.out.println("|\tClass: "+node.tokenClass);
		System.out.println("|\tSnippet: "+node.snippet);
		if(!guess.equals("") && !actual.equals("")) {
			System.out.println("Expected a type of " + guess);
			System.out.println("Received a type of " + actual);
		}
		System.out.println("---------------------------------------------");
		System.exit(0);
	}

	private int findIndex(TreeNode node, InfoTable table){
		parser.TableItem item = new parser.TableItem(node.tokenNo, node.tokenClass, node.snippet);
		return table.index(item);
	}
}
