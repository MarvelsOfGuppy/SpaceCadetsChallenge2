import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.ArrayList; // import the ArrayList class

public class Main {
    public static void main(String[] args) {
        //defining a dynamic array to store text file contents
        ArrayList<String> programText = new ArrayList<String>();
        /*defining a dynamic array to store all variables from the program.
        Convention is as follows: each element in the array is a separate
        variable, it has a name and value. ["name:value"] where value is an int.
         */
        ArrayList<String> variables = new ArrayList<String>();

        //read the program text file; assumes name is bareBones.txt
        try {
            File program = new File("bareBones.txt");
            Scanner fileReader= new Scanner(program);
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                programText.add(data);
            }
            fileReader.close();
        }
        catch (FileNotFoundException e){
            System.out.println(("Error (1): Program file not found, line 29"));
            e.printStackTrace();
        }

        //loop through programText, interpret each line
        for (int count = 0; count < programText.size(); count++){
            //split line into its parts
            String[] splitLine = (programText.get(count)).split(" ");
            boolean isWhile = false;
            //due to indents in while program auto skips those instructions
            if (splitLine.length > 2) {
                //know it must be a while, may need to add more checks here in future
                isWhile = true;
                if(!variableCheck(variables, splitLine, isWhile)){
                    variables.add((splitLine[1])+":0");
                }
                //interpret while
                variables = whileInterpret(variables, count, programText, isWhile, splitLine, 1);
                //count gets updated and removed from variables
                count = Integer.parseInt(variables.get(variables.size()-1));
                variables.remove(variables.size()-1);
            }
            else {
                //clear, decr, and incr
                if (!splitLine[0].equals("end;")) {
                    variables = basicInterpret(splitLine, variables, isWhile);
                }
            }
        }
        System.out.println(variables);
    }
    public static boolean variableCheck(ArrayList<String> variables, String[] splitLine, boolean isWhile){
        //return true if variable known, false if not
        if (isWhile){
            //do not need to remove semicolon
            for(int i = 0; i < variables.size(); i++){
                String[] variable = variables.get(i).split(":");
                if (variable[0].equals(splitLine[1])){
                    return true;
                }
            }
        }
        else{
            //need to remove semicolon
            String var = splitLine[1].replace(";","");
            for(int i = 0; i < variables.size(); i++){
                String[] variable = variables.get(i).split(":");
                if (variable[0].equals(var)){
                    return true;
                }
            }
        }
        return false;
    }
    public static int findVar(String variable, ArrayList<String> variables){
        for (int i = 0; i < variables.size(); i++) {
            //find variable in variables arrayList, return index it is at
            if (variable.equals((variables.get(i)).split(":")[0])) {
                return i;
            }
        }
        System.out.println("Error (2): Variable not found, line 90");
        System.exit(0);
        return variables.size()+1;
    }
    public static ArrayList<String> basicInterpret(String[] splitLine, ArrayList<String> variables, boolean isWhile){
        //interprets clear, incr, and decr only
        String variable = splitLine[1].replace(";","");
        if(!variableCheck(variables, splitLine, isWhile)){
            //add variable to variables
            variables.add(variable+":0");
        }
        int pointer = findVar(variable, variables);
        switch (splitLine[0]) {
            case "clear":
                //set to 0
                try {
                    variables.set(pointer, variable + ":0");
                }
                catch(IndexOutOfBoundsException e){
                    //occurs if findVar fails
                    System.out.println("Error (2): Variable not found, line 117");
                }
                break;
            case "incr":
                //increase by 1
                try {
                    int value = Integer.valueOf(variables.get(pointer).split(":")[1])+1;
                    variables.set(pointer, variable + ":" + value);
                }
                catch(IndexOutOfBoundsException e){
                    //occurs if findVar fails
                    System.out.println("Error (2): Variable not found, line 131");
                }
                catch(NumberFormatException e){
                    //occurs if valueOf input is an invalid string
                    System.out.println("Error (3): Input string invalid, line 135");
                }
                break;
            case "decr":
                //decrease by 1, but cannot go negative!
                try {
                    int value = Integer.valueOf(variables.get(pointer).split(":")[1]);
                    if (value == 0){
                        System.out.println("Error (5): Variables cannot be negative, line 143");
                        break;
                    }
                    else{
                        variables.set(pointer, variable + ":" + (value-1));
                    }
                }
                catch(IndexOutOfBoundsException e){
                    //occurs if findVar fails
                    System.out.println("Error (2): Variable not found, line 149");
                }
                catch(NumberFormatException e){
                    //occurs if valueOf input is an invalid string
                    System.out.println("Error (3): Input string invalid, line 153");
                }
                break;
        }
        return variables;
    }
    public static ArrayList<String> whileInterpret(ArrayList<String> variables, int count, ArrayList<String> programText, boolean isWhile, String[] splitLine, int layer){
        //ensure isWhile is checked for, can call itself subsequently if true
        //interpret while command first, variable already in array
        if (splitLine[(layer-1)*3+2].equals("not")){
            String variable = splitLine[(layer-1)*3+1];
            int pointer = findVar(variable, variables);
            int value = Integer.parseInt(variables.get(pointer).split(":")[1]);
            int target = Integer.parseInt(splitLine[(layer-1)*3+3]);
            int startOfLoop = count+1;
            while (value != target){
                count += 1;
                splitLine = programText.get(count).split(" ");
                if (splitLine[(layer-1)*3].equals("end;")){
                    //resets to start of loop in program; branch
                    count = startOfLoop-1;
                    splitLine = programText.get(count+1).split(" ");
                }
                //indents in text file are groups of three
                /*
                remove empty spaces from splitLine, only from command onwards
                new splitLine from command onwards is checked for while
                if while increment layer and have subroutine call itself
                if not call basicInterpret
                 */
                else{
                    String command = splitLine[layer*3];
                    if (command.equals("while")) {
                        variables = whileInterpret(variables, count, programText, true, splitLine, layer + 1);
                        //count gets updated and removed from variables
                        count = Integer.parseInt(variables.get(variables.size()-1));
                        variables.remove(variables.size()-1);
                    } else {
                        //need to ensure end; is split otherwise an error occurs
                        if (!splitLine[3].equals("end;")) {
                            String[] splitLine2 = new String[]{splitLine[layer * 3], splitLine[layer * 3 + 1]};
                            variables = basicInterpret(splitLine2, variables, false);
                        }
                    }
                }
                value = Integer.parseInt(variables.get(pointer).split(":")[1]);
                if (value == target){
                    //temporarily add count to end of variables so that Main goes to the correct part of the code
                    variables.add(Integer.toString(count));
                    break;
                }
            }
            layer-=1;
            return variables;
        }
        else {
            System.out.println("Error (4): While condition not implemented, line 211");
        }
        //find the first end; on same indentation level, all code between in new ArrayList
        return variables;
    }
}