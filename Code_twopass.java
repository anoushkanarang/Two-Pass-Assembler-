// package co_project;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

public class Code {

	public static void main(String[] args) {
		FileWriter fW = null; //for machine code
		BufferedWriter bW = null;

		FileWriter fW2 = null; //for error file
		BufferedWriter bW2 = null;
		try {
			File infile = new File("C:/Users/YUVRAJ/Desktop/AssemblyCode.txt");
			Scanner sc = new Scanner(infile);

			fW = new FileWriter("C:/Users/YUVRAJ/Desktop/MachineCode.txt");
			bW = new BufferedWriter(fW);

			fW2 = new FileWriter("C:/Users/YUVRAJ/Desktop/ErrorFile.txt");
			bW2 = new BufferedWriter(fW2);


			// assembly opcode map
			HashMap<String, String> imperStatements = new HashMap<String, String>();
			ArrayList<String> IC = new ArrayList<>();

			imperStatements.put("CLA", "0000");
			imperStatements.put("LAC", "0001");
			imperStatements.put("SAC", "0010");
			imperStatements.put("ADD", "0011");
			imperStatements.put("SUB", "0100");
			imperStatements.put("BRZ", "0101");
			imperStatements.put("BRN", "0110");
			imperStatements.put("INP", "1000");
			imperStatements.put("DSP", "1001");
			imperStatements.put("MUL", "1010");
			imperStatements.put("DIV", "1011");
			imperStatements.put("STP", "1100"); // same as END
			imperStatements.put("BRP", "0111");

			// declarative statements map
			HashMap<String, String> dec = new HashMap<String, String>();
			dec.put("DC", "01");

			HashMap<String, String> opcodet = new HashMap<>();
			HashMap<String, ArrayList<Integer>> symbolt = new HashMap<>();
			HashMap<String, ArrayList<Integer>> literalt = new HashMap<>();
			HashMap<String, Integer> constantt = new HashMap<>();

			String arr[] = new String[1000]; // to store all the variables in the array
			int index = 0; // for array indexing
			String assemblyOpcode = ""; // to compare it in the hashMap of imperativeStatements
			String currLine = "";
			int lcounter = 0;
			int noOfErrors = 0;
			int memaddress = 0;
			while (sc.hasNextLine()) {
				currLine = sc.nextLine();

				if (!checkComment(currLine) ) {
					// currLine = currLine.strip();
					if (checkStart(currLine)) {
						lcounter = Integer.parseInt(currLine.split(" ")[1]) + 1; // initialising the lcounter to the // VALUE given by START VALUE
					}
					break;
				}
			}
			String str;
			while (sc.hasNextLine()) {

				String error = "";
				String opNum = "";
				int length = 1; // TAKING EVERY INSTRUCTION LENGTH AS 1
				int flag = 0; // TO TERMINATE IF THE OPCODE IS INVALID
				currLine = sc.nextLine();
				if (!checkComment(currLine)) {
//symbol table      //LABELS + CONSTANT DECLARATIONS
					// PEHLE SAARE LABELS ADD HONGE USKE BAAD END MEI SAARE CONSTANTS
					String sym = currLine.split(" ")[0]; // string to store the symbol/label , if present
					if (!sym.isEmpty() && !(sym.equals(" ")) && !(checkSymbol(sym, symbolt))) { // checks if the string is notEmpty or a SPACE and the currSymbol is not present in the symbol table
						ArrayList<Integer> s = new ArrayList<>();
						s.add(lcounter);
						s.add(memaddress);// table beforehand
						symbolt.put(sym, s);
						memaddress++;

					}
					String delimiter1 = " ";
					String delimiter2 = " =";
					currLine = currLine.replaceAll(delimiter2, delimiter1);
//----------------------------------------------------------------------------------
/* KARTA DHARTA */ String s[] = currLine.split(" |\\,|\\=");                             // currLine =" add r,x='1'" --------> s[] = [add,r,x,'1']
																				 
//----------------------------------------------------------------------------                                              

//literal Table                   	                  
					for (int i = 0; i < s.length; i++) { // |
						if (!s[i].isEmpty() && !s[i].equals(" ")) { // V
							if (s[i].charAt(0) == '\'') { // charAt(0) because s[i] = '1'
								if (!(checkLiteral(s[i], literalt))) {
									ArrayList<Integer> l = new ArrayList<>();
									l.add(lcounter);
									l.add(memaddress);
									literalt.put(s[i], l);
									memaddress++;
								}
							}
						}
					}
//constant table 	
					if (!s[0].isEmpty() && !s[0].equals(" ") && s[0].length() == 1) {
						if (s[1].equals("DC")) {
							if (!(checkConstant(s[0], constantt))) {
								opNum = dec.get(s[1]);
								assemblyOpcode = "DC";

								int c = (Integer.parseInt( s[2].strip() ));

                           // table beforehand
								constantt.put(s[0], c);

							} else if (checkConstant(s[0], constantt)) {
								System.out.println("********ERROR : " + "\"" + s[0] + "\"" + " HAS BEEN DEFINED MORE THAN ONCE********");
								bW2.write("********ERROR : " + "\"" + s[0] + "\"" + " HAS BEEN DEFINED MORE THAN ONCE********");
								bW2.newLine();
								noOfErrors++;
								error = "#";
							}
						}
					}

//extracting the opcodes from the assembly opcodes table
					if (Character.isLetter(s[1].charAt(0)) && s[1].length() == 3) { // to ensure that the length is
																					// three
						assemblyOpcode = s[1];
						opNum = retOpcode(assemblyOpcode, imperStatements);
						if (!opNum.equals("Invalid Opcode")) {
							opcodet.put(assemblyOpcode, opNum);

						}
					}

					if (opNum.equals("Invalid Opcode")) {
						System.out.println("********ERROR : INVALID OPCODE AT LINE : " + currLine + " ****************");
						bW2.write("********ERROR : INVALID OPCODE AT LINE : " + currLine + " ****************");
						bW2.newLine();
						noOfErrors++;
						flag = 1;
						error = "#";
					}
//storing the variables int arr[]
					int check = 0; // to check whether the opcode has sufficient operands or not
					if (flag == 0 && !s[1].equals("START") && !s[1].equals("CLA") && !s[1].equals("STP")) {
						if (s.length == 2) { // CHECK FOR NUMBER OF OPERAND ERROR
							System.out.println("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS NOT SUPPLIED WITH ENOUGH OPERANDS ********");
							noOfErrors++;
							bW2.write("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS NOT SUPPLIED WITH ENOUGH OPERANDS ********");
							bW2.newLine();
							check = 1;
							error = "#";
						}
						if (!s[1].equals("DC") && check == 0) {
							if (!strInArr(arr, index, s[2]) && !checkSymbol(s[2], symbolt)) { // to ensure that there
																								// are
																								// no duplicates
								arr[index] = s[2];
								index++;
							}
						}
					}
//An opcode is supplied with too many operands
					if (flag != 1) {
						if (s[1].equals("CLA") || s[1].equals("STP")) {

							if (s.length > 2) {
								System.out.println("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS SUPPLIED WITH TOO MANY OPERANDS ********");
								noOfErrors++;
								bW2.write("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS SUPPLIED WITH TOO MANY OPERANDS ********");
								bW2.newLine();
								error = "#";
							}
						}
						else if (s[1].equals("DIV")) {
							if (s[3].charAt(0) != 'R' || s[3].charAt(0) == 'R' && (s.length > 5)) {
								System.out.println("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS SUPPLIED WITH TOO MANY OPERANDS ********");
								noOfErrors++;
								bW.write("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS SUPPLIED WITH TOO MANY OPERANDS ********");
								bW2.newLine();
								error = "#";
							}
						} else if (s.length > 3) {
							System.out.println("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS SUPPLIED WITH TOO MANY OPERANDS ********");
							noOfErrors++;
							bW2.write("********ERROR : THE OPCODE " + "\"" + s[1] + "\"" + " IS SUPPLIED WITH TOO MANY OPERANDS ********");
							bW2.newLine();
							error = "#";
						}
					}

				}

				if (checkComment(currLine)) {
					opNum = "Comment";
				} 			// if(not comment) condition over

				if (!opNum.equals("Comment") && !checkStart(currLine) && !haserror(error)) {
					IC.add((opNum + "\t" + assemblyOpcode + "\t" + length + "\t" + currLine)); // write in IC
					
				} // INTERMEDIATE CODE
				if (!checkComment(currLine)) {

					lcounter = lcounter + 1;
				}

				if (opNum.equals("1100")) { // STOP = 1100
					break;
				}

			}// while loop over

			if (!currLine.strip().equals("STP")) { // to check if end statement is missing or not
				System.out.println("********ERROR : END STATEMENT MISSING  ***********************");
				noOfErrors++;
				bW2.write("********ERROR : END STATEMENT MISSING  ***********************");
				bW2.newLine();

			}

			for (int i = 0; i < index; i++) {
				if (!checkConstant(arr[i], constantt) && !checkLiteral(arr[i], literalt)
						&& !checkSymbol(arr[i], symbolt)) {
					System.out.println("********ERROR : SYMBOL " + "\"" + arr[i] + "\""	+ " HAS BEEN USED BUT NOT DEFINED *******");
					noOfErrors++;
					bW2.write("********ERROR : SYMBOL " + "\"" + arr[i] + "\""	+ " HAS BEEN USED BUT NOT DEFINED *******");
					bW2.newLine();
					bW2.newLine();
				}
			}
			bW2.write(noOfErrors +  " Errors");
			System.out.println(noOfErrors + " Errors");
			System.out.println();
			System.out.println("OPCODE TABLE:");
			if(opcodet.size() == 0){
				System.out.println("**EMPTY TABLE**");
			}
			printMapString(opcodet);
			System.out.println();
			System.out.println("LITERAL TABLE: " );
			printMap(literalt);
			if(literalt.size() == 0){
				System.out.println("**EMPTY TABLE**");
			}
			System.out.println();
			System.out.println("SYMBOL TABLE: ");
			printsymbolt(symbolt, constantt);
			if(symbolt.size() == 0){
				System.out.println("**EMPTY TABLE**");
			}


//**********************************************************************************************************************
//			                                           SECOND PASS
//**********************************************************************************************************************

//TRAVERSING ARRAYLIST IC			
			for (int i =0 ; i<IC.size();i++) {
				currLine = IC.get(i);
				// checking type of instruction
				String[] s = currLine.split("\t");

				if (s[0].length() == 2) { // DC
					continue;
				}

				else if (s[0].equals("1100")) { // STP
					bW.write(s[0]);
					bW.newLine();
					break;
				} 
				else if (s[0].equals("0000")) { // CLA
					bW.write(s[0]);
					bW.newLine();
				}
				else if (s[0].length() == 4) { // imperative
					if (s[0].equals("1011")) {  // checking for division
						str = s[3].split(" ")[2].charAt(0) + "";							
						if(str.equals("\'")){
							str = s[3].split(" ")[2].substring(0,3);
							if(checkLiteral(str,literalt)){
								ArrayList<Integer> m2 = literalt.get(str);				           	//checkliteralt( )   ?? symbol wali rpoblem to nahi aegi?
								int n = m2.get(m2.size() - 1);
								String b = getbinary(n);
								bW.write(s[0] + "  " + b);
								bW.newLine();
							}
						}

						else{
							if(checkSymbol(str,symbolt)){
								ArrayList<Integer> m1 = symbolt.get(str); // getting the memory address
								int m = m1.get(m1.size()-1);
								// converting to binary
								String bin1 = getbinary(m);
								bW.write(s[0] + "  " + bin1);
								bW.newLine();
							}
						}

					} 
					else if (s[0].equals("0101") || s[0].equals("0111") || s[0].equals("0110")) { //branching statements

						str = s[3];
						String[] c = str.split(" ");

						String lbl = c[2];
						

						if(checkSymbol(lbl,symbolt)){
							ArrayList<Integer> mem = symbolt.get(lbl);
							int m = mem.get(1);
							// converting to binary
							String bin1 = getbinary(m);

							bW.write(s[0] + "  " + bin1);
							bW.newLine();
						}
					}
					else {
						str = s[3];
						String[] c = str.split(" ");
						String cstr = c[c.length - 1];

						if (constantt.containsKey(cstr)) { // is a constant
							if(checkSymbol(cstr,symbolt)){
								ArrayList<Integer> m1 = symbolt.get(cstr); // getting the memory address
								int m = m1.get(m1.size()-1);

								// converting to binary
								String bin1 = getbinary(m);
								bW.write(s[0] + "  " + bin1);
								bW.newLine();
							}
						} 
						else if (literalt.containsKey(cstr)) { // is a literal

							ArrayList<Integer> mem = literalt.get(cstr);
							int m = mem.get(mem.size() - 1); // getting the last element of arraylist, i.e, its memory
							// converting to binary
							String bin1 = getbinary(m);
							bW.write(s[0] + "  " + bin1);
							bW.newLine();

						}
					}
				}

			}
			bW.close();
			fW.close();
			bW2.close();
			fW2.close();

		}

		catch (Exception e) { // SAARI EXCEPTIONS CATCH HO JAEGI
			e.printStackTrace();
		}

	}

	public static boolean haserror(String str) {
		return str.equals("#");

	}

	public static boolean checkComment(String str) {
		str = str.strip();
		return (str.substring(0, 2).equals("//"));

	}

	public static boolean checkStart(String str) {
		String s = str.split(" ")[0];
		return (s.contains("START"));

	}

	public static boolean checkend(String str) {
		String s = str.split(" ")[1];
		return (s.equals("END"));

	}

	public static boolean stringEmpty(String str) {
		return str.isEmpty();
	}

	public static boolean checkSymbol(String str, HashMap symbolt) { // checks if a symbol is already present in the
																		// symbol table
		return symbolt.containsKey(str);
	}

	public static boolean checkLiteral(String str, HashMap literalt) { // checks if a literal is already present in the
																		// literal table
		return literalt.containsKey(str);

	}

	public static boolean checkConstant(String str, HashMap constantt) { // checks if a constant is already present in
																			// the constant table
		return constantt.containsKey(str);

	}

	public static String retOpcode(String str, HashMap<String, String> imperStatements) { // checks if a mneumonic is
																							// there in the opcode table
																							// and
																							// returns the corresponding
																							// opcode(IF FOUND)
		for (Map.Entry<String, String> mapElement : imperStatements.entrySet()) {
			if (str.equals((String) mapElement.getKey())) {
				return (String) (mapElement.getValue());
			}
		}
		return "Invalid Opcode";
	}

	public static void printMap(HashMap<String, ArrayList<Integer>> hm) { // to print HashMap with a string and an
//																		// ArrayList of integer as an argument
		for (Entry<String, ArrayList<Integer>> mapElement : hm.entrySet()) {
			ArrayList<Integer> a1 = new ArrayList<>();
			a1 = mapElement.getValue();
			int a2 = a1.get(a1.size() - 1);
			String a3 = getbinary(a2);
			System.out.println((String) mapElement.getKey() + "\t " + a3);

		}
	}

	public static void printsymbolt(HashMap<String, ArrayList<Integer>> hm, HashMap<String, Integer> constantt) {
		System.out.println("TYPE          SYMBOL            VALUE            MEMORY ADDRESS");
		for (Entry<String, ArrayList<Integer>> mapElement : hm.entrySet()) {
			// constant
			if (constantt.containsKey(mapElement.getKey())) {
				ArrayList<Integer> a1 = new ArrayList<>();
				a1 = mapElement.getValue();
				int a2 = a1.get(a1.size() - 1);
				String a3 = getbinary(a2);
				System.out.println("CONSTANT         "+ (mapElement.getKey()) +"                " +constantt.get(mapElement.getKey()) + "               " + a3);
			} else {
				ArrayList<Integer> a1 = new ArrayList<>();
				a1 = mapElement.getValue();
				int a2 = a1.get(a1.size() - 1);
				String a3 = getbinary(a2);
				System.out.println("LABEL           "+(String) mapElement.getKey() + "                                  " + a3);
			}
		}

	}

	public static void printMapString(HashMap<String, String> hm) { // to print hashmap with both strings
		for (Map.Entry<String, String> mapElement : hm.entrySet()) {
			System.out.println((String) mapElement.getKey() + " : " + (String) mapElement.getValue());
		}
	}

	public static boolean strInArr(String[] arr, int index, String str) {
		for (int i = 0; i < index; i++) {
			if (arr[i].equals(str)) {
				return true; // string[] has the string str
			}
		}
		return false; // string[] doesn't have the string str
	}

	public static String getbinary(int n) {

		String bin = Integer.toBinaryString(n);
		String bin1 = "";
		for (int i = 1; i <= 8 - bin.length(); i++) {
			bin1 += "0";
		}
		bin1 += bin;
		return bin1;

	}

}
