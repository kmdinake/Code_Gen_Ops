package translator;

import java.util.ArrayList;
import java.util.Scanner;
import parser.InfoTable;
import parser.Parser;
import parser.TreeNode;

/**
 *
 * @author Priscilla
 */


public class Translator {
    private int counter = 1;
    private boolean foundIf = false;
    private InfoTable symbolTable;
    public void translate(TreeNode node, InfoTable symbolTable){
        this.symbolTable = symbolTable;
        String code = translateExp(node);
        System.out.println("Translated code BEFORE replacements: " + code);
        code = replaceHalts(code);
        System.out.println("Translated code AFTER replacements: " + code);
    }
    
    //Translate Function
    public String translateExp(TreeNode node){
        String code = "";
        if(node == null){
            System.out.println("Root Null");
            return "";
        }
        System.out.println(node.toString());
        /**************************************************
         * NON-TERMINALS
         **************************************************/
        if(node.tokenClass.equals("Q")){ 
            code += translateExp(node.getChild(0));
        }
        
        if(node.tokenClass.equals("C")){ 
            for(int i = 0; i < node.childrenSize(); i++){
                code += "\t" + translateExp(node.getChild(i));
            }
        }
        
        if(node.tokenClass.equals("I")){ 
            code += translateExp(node.getChild(0));
        }
        
        if(node.tokenClass.equals("P")){
            for(int i = 0; i < node.childrenSize(); i++){
                code += "\t" + translateExp(node.getChild(i));
            }
        }
        
        // PROC_DEFS
        if(node.tokenClass.equals("D")){
            for(int i = 0; i < node.childrenSize(); i++){
                code += translateExp(node.getChild(i));
            }
        }
        
        // PROC
        if(node.tokenClass.equals("R")){
            code += ("\n" + counter++);
            code += " DEF FN";
            code += translateExp(node.getChild(1));
            code += " = ";
            code += translateExp(node.getChild(2)); // should be on one line
        }
        
        // CALL
        if(node.tokenClass.equals("Y")){
            if(!foundIf){
                code += ("\n" + counter++ + " ");
            } else {
                code += ("\n" + counter++ + "\t");
            }
            code += translateExp(node.getChild(0));
        }
        
        //Translate Output
        if(node.tokenClass.equals("O")){
            if(!foundIf){
                code += ("\n" + counter++ + " ");
            } else {
                code += ("\n" + counter++ + "\t");
            }
            if(node.getChild(0).snippet.equals("input")){
                code += "INPUT ";
            } else {
                code += "PRINT ";
            }
            code += translateExp(node.getChild(1));
        }
        
        //VAR_BRANCH
        if(node.tokenClass.equals("V")){
            code += translateExp(node.getChild(0));
        }
        
        //SVAR
        if(node.tokenClass.equals("S")){
            code += translateExp(node.getChild(0));
        }
        
        //Translate Assignment
        if(node.tokenClass.equals("A")){
            if(!foundIf){
                code += ("\n" + counter++ + " LET ");
            } else {
                code += ("\n" + counter++ + "\tLET ");
            }
            code += translateExp(node.getChild(0));
            code += " = ";
            code += translateExp(node.getChild(2));
        }
        
        //Translate LHSExpr - Assignment
        if(node.tokenClass.equals("T")){
            code += translateExp(node.getChild(0));
        }
        
        //Translate RHSExpr - Assignment
        if(node.tokenClass.equals("U")){
            code += translateExp(node.getChild(0));
        }
        
        //NUMEXPR
        if(node.tokenClass.equals("X")){
            code += translateExp(node.getChild(0));
        }
        
        //NVAR
        if(node.tokenClass.equals("N")){
            code += translateExp(node.getChild(0));
        }
        
        //CALC
        if(node.tokenClass.equals("L")){
            code += "(";
            code += translateExp(node.getChild(1));
            code += getOperator(node.getChild(0).snippet);
            code += translateExp(node.getChild(2));
            code += ")";
        }
        
        //BOOL
        if(node.tokenClass.equals("B")){
            if(node.childrenSize() == 2){
                code += "(";
                code += getOperator(node.getChild(0).snippet);
                code += translateExp(node.getChild(1));
                code += ")";
            } else if(node.childrenSize() == 3){
                code += "(";
                if(node.getChild(1).tokenClass.equals("comparison symbol")){
                    code += translateExp(node.getChild(0));
                    code += getOperator(node.getChild(1).snippet);
                    code += translateExp(node.getChild(2));
                } else {
                    code += translateExp(node.getChild(1));
                    code += getOperator(node.getChild(0).snippet);
                    code += translateExp(node.getChild(2));
                }
                code += ")";
            } else {
                System.out.println("Why are you here! Invalid Boolean Expression");
            }
        }
        
        //COND_BRANCH
        if(node.tokenClass.equals("W")){
            if(!foundIf){
                code += ("\n" + counter++ + " IF ");
                foundIf = true;
                code += translateExp(node.getChild(1));
                code += " THEN ";
                code += translateExp(node.getChild(3));
                if(node.childrenSize() == 6){
                    code += ("\n" + counter++ + " ELSE");
                    code += translateExp(node.getChild(5));
                }
                code += ("\n" + counter++ + " ENDIF");
                foundIf = false;
            } else {
                code += ("\n" + counter++ + "\tIF ");
                code += translateExp(node.getChild(1));
                code += " THEN ";
                code += "\t\t" + translateExp(node.getChild(3));
                if(node.childrenSize() == 6){
                    code += ("\n" + counter++ + "\tELSE");
                    code += "\t\t" + translateExp(node.getChild(5));
                }
                code += ("\n" + counter++ + " ENDIF");
                foundIf = false;
            }            
        }
        
        //COND_LOOP
        if(node.tokenClass.equals("Z")){
            int startIndex = counter + 1;
            code += ("\n" + counter++);
            if(node.childrenSize() == 3){// WHILE LOOP
                code += " IF ";
                code += translateExp(node.getChild(1)); // BOOL
                code += " THEN ";
                foundIf = true;
                code += translateExp(node.getChild(2)); // CODE
                code += ("\n" + counter++ + "\tGOTO " + (startIndex - 1));
                foundIf = false;
                code += ("\n" + counter++ + " ELSE");
                code += ("\n" + counter++ + "\tGOTO " + counter);
                code += ("\n" + counter++ + " ENDIF");
            } else if(node.childrenSize() == 13){ // FOR LOOP
                // ASSIGN
                code += " LET ";
                code += translateExp(node.getChild(1)); // UDN --> get UniqueName
                code += " = ";
                code += translateExp(node.getChild(3)); // Integer
                
                // COMPARE
                code += ("\n" + counter++ + " IF ");
                code += translateExp(node.getChild(4)); // UDN --> get UniqueName
                code += getOperator(node.getChild(5).snippet); // comparision operator
                code += translateExp(node.getChild(6)); // UDN --> get UniqueName
                code += " THEN ";
                
                // CODE
                foundIf = true;
                code += translateExp(node.getChild(12));
                foundIf = false;
                
                // INC
                code += ("\n" + counter++ + "\t");
                code += ( translateExp(node.getChild(7)) + " = "); // UDN --> get UniqueName
                code += translateExp(node.getChild(10)); // UDN --> get UniqueName
                code += getOperator(node.getChild(9).snippet); 
                code += translateExp(node.getChild(11)); // Number Literal
                
                // JUMP
                code += ("\n" + counter++ + "\tGOTO " + startIndex);
                code += ("\n" + counter++ + " ELSE");
                code += ("\n" + counter++ + "\tGOTO " + counter);
                code += ("\n" + counter++ + " ENDIF");
            } else {
                System.out.println("Woah, what are you doing here. Invalid Condition Loop");
            }
        }
        
        /**************************************************
         * TERMINALS
         **************************************************/
        
        //UDN
        if(node.tokenClass.equals("user-defined name")){
            //code += node.snippet;   //Don't forget unique name
            code += this.symbolTable.getUniqueNameFromTokenNo(node.tokenNo);
        }
        
        //String literal
        if(node.tokenClass.equals("short string")){
            code += node.snippet;   
        }
        
        //Number literal
        if(node.tokenClass.equals("number") || node.tokenClass.equals("integer")){
            code += node.snippet;   
        }
        
        // Halt
        if(node.tokenClass.equals("keyword") && node.snippet.equals("halt")){
            if(!foundIf){
                code += ("\n" + counter++ + " GOTO @#@");
            } else {
                code += ("\n" + counter++ + "\tGOTO @#@");
            }
        }
        
        System.out.println("Code so far: " + code);
        return code;
    }

    //Get corresponding binary operator
    private String getOperator(String op){
        switch(op){
            case "add": return " + ";
            case "sub": return " - ";
            case "mult": return " * ";
            case "and": return " AND ";
            case "or": return " OR ";
            case "not": return " NOT ";
            case "eq": return " = ";
            case "<" : return " < ";
            case ">" : return " > ";
        }
        System.out.println("Who are you. Invalid operator " + op);
        return "";
    }
    
    private String replaceHalts(String code){
        Scanner scanner = new Scanner(code);
        scanner.useDelimiter("\n");
        ArrayList<String> result = new ArrayList<>();
        while(scanner.hasNext()){
            String line = scanner.next();
            if(line.contains("GOTO @#@")){
                line = line.replace("@#@", String.valueOf(counter));
            }
            result.add(line);
        }
        String actual_code = "\n";
        for(int i = 0; i < result.size(); i++){
            actual_code += result.get(i) + "\n";
        }
        actual_code += (counter + " END\n");
        return actual_code;
    }
}