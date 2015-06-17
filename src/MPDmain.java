/**
 * 本程序是使用马尔科夫决策过程来解决多轮分区的问题
 * 
 * Description: (1) read data from a file
 * 				(2) MDP
 * 				(3) write output in a file 
 **/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MPDmain {
	public int PNUM = 100;
	public Map<Integer, Integer> counter = new HashMap<Integer, Integer>();
	//二维数组，第一行
	public int[][] Sampletable = new int[3][PNUM];
	public int[] ReduceLoad = new int[4];

	public int TOTALNUM = 100000;
	public int Samplenum = 0;
	public boolean First = true;
	public int AssPnum = 0;
	public int unAssPnum = 0;
	
	public int UNAssiPNUM = -1;

	public int State[] = new int[PNUM];
	public int Action[] = new int[PNUM];

	public double beta = 0.0;

	public List<Integer> AssignedPar = new LinkedList<Integer>();
	public List<Integer> unAssignedPar = new LinkedList<Integer>();

	/**
	 * Read and write a file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void readFileByLines(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader reader = null;

		//String tempString =null;
		int input = 0;

		/** init MDP.state and MDP.action */
		for (int i = 0; i < State.length; i++) {
			State[i] = i;
			Action[i] = i;

			//System.out.println(State[i]);
		}

		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.readLine() != null) {
				input = Integer.parseInt(reader.readLine());

				int hashcodenum = input % PNUM;

				if (counter.containsKey(hashcodenum)) {
					int tempvalue = counter.get(hashcodenum);
					counter.put(hashcodenum, tempvalue + 1);
					Sampletable[1][hashcodenum] = tempvalue + 1;
				} else {
					counter.put(hashcodenum, 1);
					Sampletable[0][hashcodenum] = hashcodenum;
					Sampletable[1][hashcodenum] = 1;
				}

				Samplenum++;

				/**
				 * the moment to decide to mdp samplenum = 10%TOTALNUM, begin
				 * MDP
				 */
				if ((Samplenum > Math.round((double) (TOTALNUM * 0.4))) && First) {
					//System.out.println("Samplenum = " + Samplenum);
					beta = Samplenum / TOTALNUM;
					mdp();
					First = false;
				}else{
					if(unAssignedPar.size() > 0){
						mdp();
					}
					
				}
				
				
				
				
			}

			//mdp();

			//按照hash值写入文件
			String outstring = String.valueOf(counter);

			outstring = outstring.substring(1, outstring.length() - 1);

			String[] outar = outstring.split(",");
			System.out.println("outstring = " + outstring);

			FileWriter writer = new FileWriter("/home/wzhuo/example/mdp/out.txt");
			BufferedWriter bw = new BufferedWriter(writer);

			for (int i = 0; i < outar.length; i++) {
				if (i == 0) {
					//System.out.println("outar[ "+i+"]=" + outar[i]);
					bw.write(outar[i] + '\r');

				} else {
					//System.out.println("outar["+i+"]=" + outar[i]);
					outar[i] = outar[i].substring(1, outar[i].length());
					bw.write(outar[i] + '\r');
				}
			}
			bw.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * MDP
	 */
	public void mdp() {
		int value[] = new int[PNUM];
		double Evalue = 0.0;

		/** sort samplingTable descending */
		for (int i = 0; i < Sampletable[1].length; i++) {
			int maxid = Sampletable[0][i];
			int maxnum = Sampletable[1][i];
			int maxdid = 0;

			for (int j = i + 1; j < Sampletable[1].length; j++) {
				if (maxnum < Sampletable[1][j]) {
					maxid = Sampletable[0][j];
					maxnum = Sampletable[1][j];
					maxdid = j;
				}
			}

			int tempval = 0;

			if (maxid != Sampletable[0][i]) {
				tempval = Sampletable[1][i];
				Sampletable[0][maxdid] = Sampletable[0][i];
				Sampletable[1][maxdid] = tempval;

				Sampletable[0][i] = maxid;
				Sampletable[1][i] = maxnum;

			} else {
				continue;
			}
		}
		// output matrix[3][] 
		for (int i = 0; i < Sampletable.length; i++) {
			for (int j = 0; j < Sampletable[1].length; j++) {
				//System.out.println("Sampletable[" + i + "][" + j + "]=" + Sampletable[i][j]);
				System.out.print(Sampletable[i][j]);
				System.out.print(' ');
			}
			System.out.println(' ');
		}

		/** add assign & unassign paritition in a linkedlist */
		for (int i = 0; i < Sampletable[2].length; i++) {
			if (Sampletable[2][i] == 0) {
				if (unAssignedPar.contains(Sampletable[0][i]) == false) {
					unAssignedPar.add(Sampletable[0][i]);
				}
			} else {
				if (Sampletable[2][i] == 1) {
					if (AssignedPar.contains(Sampletable[0][i]) == false) {
						AssignedPar.add(Sampletable[0][i]);
					}
				}
			}
		}
		//System.out.println("unAssignedPar=" + unAssignedPar + "AssignedPar="+ AssignedPar);

		/** caculate each Evalue */
		double[] EvalueArray = new double[PNUM];
		double problity = 0;

		AssPnum = 0;
		unAssPnum = 0;

		UNAssiPNUM = unAssignedPar.size();

		for (int i = 0; i < PNUM; i++) {
			int unpnum = 0;
			int asspnum = 0;
			int unsqunum = 0;
			int asqunum = 0;

			if (i < UNAssiPNUM) {
				unpnum = unAssignedPar.get(i);

				for (int j = 0; j < PNUM; j++) {
					if (j == unpnum) {
						unsqunum = j;
						break;
					}
				}
				unAssPnum += Sampletable[1][unsqunum];
			}

			if (i < PNUM - UNAssiPNUM) {
				asspnum = AssignedPar.get(i);

				for (int j = 0; j < PNUM; j++) {
					if (j == asspnum) {
						asqunum = j;
						break;
					}
				}
				AssPnum += Sampletable[1][asqunum];
			}

			//PAsUna = (double) AssPnum / unAssPnum;
			//System.out.println("AssPnum=" + AssPnum + "unAssPnum=" + unAssPnum + "Samplenum" + Samplenum);

			problity = (double) unAssPnum / (TOTALNUM - Samplenum);
			DecimalFormat df = new DecimalFormat("#.000000");
			//System.out.println("Sampletable=" + df.format(problity));

			EvalueArray[i] = problity * (TOTALNUM - Samplenum - unAssPnum);
			//System.out.println("EvalueArray=" + EvalueArray[i]);
		}

		/** caculate the max value */
		double maxVal = 0.0;
		int maxAct = 0;
		for (int i = 0; i < EvalueArray.length; i++) {
			if (maxVal < EvalueArray[i]) {
				maxVal = EvalueArray[i];
				maxAct = i;
			}
		}
		System.out.println("maxVal=" + maxVal + "maxAct=" + maxAct +"UNAssiPNUM="+UNAssiPNUM);

		/** assign P in this round */
		int getPround = -1;
		int ungetPround = -1;
		for (int i = 0; i < maxAct; i++) {
			if (maxAct < UNAssiPNUM) {
				ungetPround = unAssignedPar.get(0);

				for (int j = 0; j < PNUM; j++) {
					if (j == ungetPround) {
						getPround = j;
						break;
					}
				}

				Sampletable[2][getPround] = 1;
				unAssignedPar.remove(0);
				AssignedPar.add(ungetPround);
				
				System.out.println("System.out.println"+unAssignedPar);
			}

			//AssignedPar.add
		}
		if(maxAct == 0){
			for(int i=0; i<unAssignedPar.size();i++){
				AssignedPar.add(unAssignedPar.get(0));
				unAssignedPar.remove(0);
				//Sampletable[2][getPround] = 1;
			}
		}

		for (int i = 0; i < Sampletable[2].length; i++) {
			System.out.println("Sampletable[2]" + Sampletable[2][i]);
		}

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		MPDmain mpd = new MPDmain();
		mpd.readFileByLines("/home/wzhuo/example/mdp/zipf6.txt");

	}

}
