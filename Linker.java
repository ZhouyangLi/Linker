import java.util.ArrayList;
import java.util.Scanner;
/**
 * This class read input from console and produce symbol table and memory map
 * based on the input. 
 * @author Zhouyang Li
 * 
 */
public class Linker {
	public Linker() {
		return;
	}
	public class symbol {
		public String name;	//the number of this symbol
		public int rAddress;	//the relative address of this symbol
		public int aAddress;	//the absolute address of this symbol
		public int definedM;	//the Module where the symbol is defined
		public int defined;	//the number of time this symbol is defined
		public int used;	// number of this symbol being used
		public int usedError;	//true if use of this symbol exceeds the size of the module
		public boolean defineError;	//true if define of this symbol exceeds the size of the module
		public ArrayList<Integer> usedErrorM; //modules where used error happened
		public symbol(String n) {
			this.name = n;
			this.usedErrorM = new ArrayList<Integer>();
		}
	}	
	
	
	public class address {
		public int address;
		public String type;
		public symbol symbolU; //the symbol used in this address
		public int mSymbol;	//the number of symbols used in this address
		public boolean symNDefined; //true of the symbol used in that address is not defined
		public int module; //the module index of the address
		public int base; //the base address for this address
		public address(int n) {
			this.mSymbol = n;
		}
	}
	
	public static void main(String[] args) {
		
		Linker L = new Linker();
		address[] addresses = new address[200];
		ArrayList<symbol> symbols = new ArrayList<symbol>();
		Scanner sc = new Scanner(System.in);
		int numOfM = sc.nextInt();
		int[] moduleSize = new int[numOfM];
		//iterate through modules to get data for output
		int curBase = 0;
		for (int curM = 0; curM < numOfM; curM ++) { 
			int numDefine = sc.nextInt();
			//collecting definition part of data
			if (numDefine > 0) {
				
				for (int symbolC = 0; symbolC < numDefine; symbolC ++) {
					String curSymbol = sc.next();
					int definedAddress = sc.nextInt();
					
					int checkS = 0; 
					
					while (checkS < symbols.size()) {
						
						symbol cs = symbols.get(checkS);
						//if the symbol is already defined, increment its defined count
						
						if (cs.name.equals(curSymbol) == true) {
							
							
							if (cs.defined == 0) {
								cs.rAddress = definedAddress;
								cs.aAddress = definedAddress + curBase;
								cs.definedM = curM;
							}
							cs.defined ++;
							
							break;
						}
						else
							checkS ++;
					}
					//if the symbol is not defined, then create a new symbol element in symbols
					
					if (checkS == symbols.size()) {
					
						symbol newS = L.new symbol(curSymbol);
						newS.rAddress = definedAddress;
						newS.aAddress = newS.rAddress + curBase;
						newS.defined = 1;
						newS.definedM = curM;
						symbols.add(newS);	
						
					}
					
				}
			}
			//collecting use part of data
			int numUse = sc.nextInt();
			
			int curUse = 0;
			while (curUse < numUse) {
				String useS = sc.next();
		
				int useRA = sc.nextInt();
				while (useRA != -1) {
				
					int useAA = useRA + curBase;
					int checkS = 0;
					while (checkS < symbols.size()) {
						symbol cs = symbols.get(checkS);
						if (cs.name.equals(useS) == true) {
							cs.used ++;	
							if (addresses[useAA] == null) {
								addresses[useAA] = L.new address(1);
								addresses[useAA].symbolU = cs;
								
							}
							else if (addresses[useAA].symbolU == null) {
								addresses[useAA].symbolU = cs;
								addresses[useAA].mSymbol ++;
							}
							else {
								addresses[useAA].mSymbol ++;
							}
								
							break;
						}
						else
							checkS ++;
					}
					//if the symbol used is not defined/used before, create a new symbol
					if (checkS == symbols.size()) {
						symbol newS = L.new symbol(useS);
						newS.defined = 0;
						newS.definedM = curM;
						newS.used = 1;
						symbols.add(newS);	
						if (addresses[useAA]==null) {
							addresses[useAA] = L.new address(1);
							addresses[useAA].symbolU = newS;
							
						}
						else if (addresses[useAA].symbolU == null) {
							addresses[useAA].symbolU = newS;
							addresses[useAA].mSymbol ++;
						}
						else {
							addresses[useAA].mSymbol ++;
						}

					}
					
					useRA = sc.nextInt();
				}
				curUse ++;
			}
			
			//to delete the use of symbol that exceeds module size
			int numText = sc.nextInt();
			int cleanC = numText+curBase;
			
			while (cleanC < addresses.length) {
				
				
				if (addresses[cleanC] != null && addresses[cleanC].symbolU != null) {
					
					symbol cleanSym = addresses[cleanC].symbolU;
					cleanSym.used --;
					cleanSym.usedError ++;
					cleanSym.usedErrorM.add(curM);
					addresses[cleanC].mSymbol = 0;
					addresses[cleanC].symbolU = null;
				}
				
				cleanC++;
			}
			//collecting text part of data
			int curText = 0;
			while (curText < numText) {
				String curType = sc.next();
				int curAdd = sc.nextInt();
				
				int abText = curText+curBase;
				if (addresses[abText]==null) {
					addresses[abText] = L.new address(0);
					addresses[abText].address = curAdd;
				}
				else {
					addresses[abText].address = curAdd;
				}
				addresses[abText].type = curType;
				addresses[abText].module = curM;
				addresses[abText].base = curBase;
				
				curText ++;
			}
		
			moduleSize[curM] = numText;
			curBase += numText;
			
		}
		
		//check if the address of symbol defined exceeds the module, if so, set 
		//it relative address to 0
		int symSize = symbols.size();
		int symCount = 0;
		while (symCount < symSize) {
			symbol curSym = symbols.get(symCount);
			int moDefined =  curSym.definedM;
			int moSize = moduleSize[moDefined];
			if (moSize <= curSym.rAddress) {
				curSym.aAddress -= curSym.rAddress;
				curSym.rAddress = 0;
				curSym.defineError = true;
			}
			symCount ++;
		}
	
		//to modify external address according to the symbol used in that address
		int addArrS = addresses.length;
		for (int addCount = 0; addCount < addArrS; addCount++) {
			address curAdd = addresses[addCount];
			if (curAdd != null && (curAdd.type).equals("E")) {
				
				if (curAdd.symbolU != null) {
					if ( (curAdd.symbolU).defined==0) {
						curAdd.address /= 1000;
						curAdd.address *= 1000;
						curAdd.symNDefined = true;
					}
					else {
						curAdd.address /= 1000;
						curAdd.address *= 1000;
						curAdd.address += (curAdd.symbolU).aAddress;
					}
				}

				
			}

		}
		
		//printing symbol table and related error massages
		System.out.println("Symbol Table");
		int symbolsSize = symbols.size();
		for (int i = 0; i<symbolsSize; i++) {
			symbol curS = symbols.get(i);
			if (curS.defined > 0) {
				System.out.print(curS.name + "=" + curS.aAddress);
				if (curS.defineError == true) {
					System.err.println(" Error: Definition exceeds module size; first word in module used.");
				}
				if (curS.defined > 1) {
					System.err.println(" Error: This variable is multiply defined; first value used.");
				}
				System.out.println("");
			}
		}
		
		//printing Memory Map and related error messages
		System.out.println("\n" + "Memory Map");
		int addressesSize = addresses.length;
	
		for (int i = 0; i<addressesSize; i++) {
			if (addresses[i] != null && addresses[i].address != 0) {
				if ((addresses[i].type).equals("I")) {
					
					System.out.println(i + ":  " + addresses[i].address);
				}
				else if ((addresses[i].type).equals("A")) {
					if ((addresses[i].address) % 1000 >= 200) {
						addresses[i].address /= 1000;
						addresses[i].address *=1000;
						System.out.print(i + ":  " + addresses[i].address);
						System.err.println(" Error: Absolute address exceeds machine size; zero used.");
					}
					else
						System.out.println(i + ":  " + addresses[i].address);
				}
				else if ((addresses[i].type).equals("R")) {
					int curMS = moduleSize[addresses[i].module];
					
					
					if ((addresses[i]).address%1000 >= curMS) {
						addresses[i].address /= 1000;
						addresses[i].address *=1000;
						System.out.print(i + ":  " + addresses[i].address);
						System.err.println(" Error: Relative address exceeds module size; zero used.");
					}
					else {
						addresses[i].address += addresses[i].base;
					
						System.out.println(i + ":  " + addresses[i].address);
					}
				}
				else if ((addresses[i].type).equals("E")) {
					System.out.print(i + ":  " + addresses[i].address);
					if ((addresses[i].symNDefined) == true) {
						System.err.print(" Error: " + addresses[i].symbolU.name + " is not defined; zero used.");
					}
					if ((addresses[i].mSymbol > 1)) {
						System.err.print(" Error: Multiple variables used in instruction; all but first ignored.");
					}
					System.out.println("");
				}
			}
		}
		System.out.println("");
		
		//printing error messages and warnings
		for (int i = 0; i < symbols.size(); i++) {
			
			if (symbols.get(i).used == 0 && symbols.get(i).defined > 0) {
				int MC = symbols.get(i).definedM + 1;
				System.err.println("Warning: " + symbols.get(i).name + 
						" was defined in module "+ MC +" but never used.");
			}
		
			if (symbols.get(i).usedError > 0) {
				for (int j = 0; i<(symbols.get(i)).usedErrorM.size(); i++) {
					int module = (symbols.get(i)).usedErrorM.get(j).intValue() + 1;
				System.err.println("Error: Use of "+ symbols.get(i).name +
						" in module " + module +
						" exceeds module size; use ignored.");
				}
			}
		}
		System.out.println("");
		
	}



}
