package value_crawler;

import parser.InfoTable;
import parser.TableItem;
import parser.TreeNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by juan on 4/27/17.
 */

public class ValueAnalyser {
    InfoTable myTable;
    ArrayList<TreeNode> procStack = new ArrayList<TreeNode>();


    private class StringException extends Throwable
    {
        public String e;
        public StringException(String s)
        {
            e = "ERROR: " + s;
        }
    }

    private void convertAllNames(InfoTable table)
    {
        TableItem tempItem;
        ValueTable vt = new ValueTable();
        //place all distinct variables into vt;
        for (int i = 0; i < table.size(); i++)
        {
            tempItem = table.getItemAt(i);
            if (tempItem.tokenClass.equals("user-defined name"))
            {
                if (!vt.contains(tempItem.snippet, tempItem.type, tempItem.scope))
                {
                    vt.add(tempItem.snippet, tempItem.type, tempItem.scope);
                }
            }
        }

        //convert all names in tables
        for (int i = 0; i < table.size(); i++)
        {
            tempItem = table.getItemAt(i);
            if (tempItem.tokenClass.equals("user-defined name"))
            {
                tempItem.newName = vt.getNewName(tempItem.snippet, tempItem.type, tempItem.scope);
            }
        }
    }

    public void doValueAnalysis(TreeNode node, InfoTable table)
    {
        myTable = table;
        convertAllNames(myTable);

//        try
//        {
            System.out.println("\nStarting value analysis process: ");
            System.out.println("--------------------------------------------------- ");
            convertAllNames(table);
            preprocessing();
            //recursiveValue(node);
            //postprocessing();
            System.out.println("\nSuccessfully completed value analysis process: ");
            System.out.println("\t writing symbol table to file... ");
            System.out.println("--------------------------------------------------- ");
//        }
//        catch (StringException e)
//        {
//            System.out.println("Value Analysis Error: " + e.e);
//            System.out.println("Exiting value checker...\n");
//        }

    }

    private void recursiveValue(TreeNode node) throws StringException
    {

        // T never reach

        // Q
        // P
        // D
        // C
        // I
        // V
        // X
        // L
        // W
        // B
        // U
        if (       node.tokenClass.equals("Q")
                || node.tokenClass.equals("P")
                || node.tokenClass.equals("C")
                || node.tokenClass.equals("D")
                || node.tokenClass.equals("I")
                || node.tokenClass.equals("V")
                || node.tokenClass.equals("X")
                || node.tokenClass.equals("L")
                || node.tokenClass.equals("W")
                || node.tokenClass.equals("B")
                || node.tokenClass.equals("U"))
        {
            for (int i = 0; i < node.childrenSize(); i++)
            {
                recursiveValue(node.getChild(i));
            }
        }

        // A
        if (node.tokenClass.equals("A"))
        {
            recursiveValue(node.getChild(2));
            //right hand side is defined
                myTable.setDefined(node.getChild(0).getChild(0).getChild(0).newName);
        }

        // O
        if (node.tokenClass.equals("O"))
        {
            TreeNode tempNode = node.getChild(1).getChild(0).getChild(0);
            if (node.getChild(0).snippet.equals("input"))
            {
                myTable.setDefined(tempNode.newName);
            }
            else//is output
            {
                if (myTable.getDefined(tempNode.newName) == 'u')
                {
                    throw new StringException("undefined variable in output: " + tempNode.snippet);
                }
            }
        }

        // R
        if (node.tokenClass.equals("R"))
        {
            recursiveValue(node.getChild(2));
            myTable.setDefined(node.getChild(1).newName);
        }

        // S
        if (node.tokenClass.equals("S"))
        {
            if (myTable.getDefined(node.getChild(0).newName) == 'u')
                throw new StringException("undefined variable: " + node.getChild(0).snippet);
        }

        // N
        if (node.tokenClass.equals("N"))
        {
            if (myTable.getDefined(node.getChild(0).newName) == 'u')
                throw new StringException("undefined variable: " + node.getChild(0).snippet);
        }

        // Z
        if (node.tokenClass.equals("Z"))
        {
            if (node.childrenSize() == 3) //while loop
            {
                for (int i = 0; i < node.childrenSize(); i++)
                {
                    recursiveValue(node.getChild(i));
                }
            }
            else //for loop
            {
                myTable.setDefined(node.getChild(1).newName);

                if (myTable.getDefined(node.getChild(4).newName) == 'u')
                    throw new StringException("undefined variable: " + node.getChild(4).snippet);
                if (myTable.getDefined(node.getChild(6).newName) == 'u')
                    throw new StringException("undefined variable: " + node.getChild(6).snippet);
                if (myTable.getDefined(node.getChild(7).newName) == 'u')
                    throw new StringException("undefined variable: " + node.getChild(7).snippet);
                if (myTable.getDefined(node.getChild(10).newName) == 'u')
                    throw new StringException("undefined variable: " + node.getChild(10).snippet);
                recursiveValue(node.getChild(12));
            }

            // Y proc call checked at the end
            if (node.tokenClass.equals("Y"))
            {
                procStack.add(node);
            }
        }

        if(node.tokenClass.equals("short string")){
            myTable.setDefined(node.newName);
        }

        if(node.tokenClass.equals("integer") || node.tokenClass.equals("number")){
            myTable.setDefined(node.newName);
        }
    }

    void preprocessing()
    {
        for (int i = 0; i < myTable.size(); i++)
        {
//            if (myTable.getItemAt(i).tokenClass.equals("short string")
//                    ||myTable.getItemAt(i).tokenClass.equals("integer"))
//            {
//                System.out.println("What: "+ myTable.getItemAt(i).snippet);
//                myTable.getItemAt(i).defined = 'd';
//            }
            myTable.getItemAt(i).defined = 'u';
        }
    }

    void postprocessing() throws StringException
    {
        for (int i = 0; i < procStack.size(); i++)
        {
            if (myTable.getDefined(procStack.get(i).getChild(0).newName) == 'u')
            {
                throw new StringException("undefined procedure: " + procStack.get(i).getChild(0).snippet);
            }
        }
    }
    void toFile()
    {
        try
        {
            String file = "SymbolTable";

            FileWriter fw = new FileWriter(file);
            fw.write(myTable.toString());
            fw.close();
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.out.println("Error opening file: SymbolTable");
        }
    }
}

