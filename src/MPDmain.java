import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 本程序是使用马尔科夫决策过程来解决多轮分区的问题
 **/

public class MPDmain {
	public int PNUM = 40;
	public Map<Integer, Integer> counter = new HashMap<Integer, Integer>();
	public int[] Sampletable = new int[PNUM];
	public int[] ReduceLoad = new int[4];
	
	public int TOTALNUM = 25000;
	public int samplenum = 0;

	public int State[] = new int[PNUM];
	public int Action[] = new int[PNUM];
	public float Problity = (float) 0.0;

	//read write io
	public void readFileByLines(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader reader = null;

		//String tempString =null;
		int input = 0;


		//init MDP.state and MDP.action
		for (int i = 0; i < State.length; i++) {
			State[i] = i;
			Action[i] = i;
			//System.out.println(State[i]);
		}

		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.readLine() != null) {
				//tempString  =reader.toString();
				input = Integer.parseInt(reader.readLine());

				int hashcodenum = input % PNUM;
				//System.out.println("Hashcode = " + hashcodenum);

				if (counter.containsKey(hashcodenum)) {
					int tempvalue = counter.get(hashcodenum);
					counter.put(hashcodenum, tempvalue + 1);
					Sampletable[hashcodenum] = tempvalue + 1;
				} else {
					counter.put(hashcodenum, 1);
					Sampletable[hashcodenum] = 1;
				}
				//System.out.println("Counter = " + counter);
				samplenum++;

				//sample = 10%TOTALNUM, begin MDP
				if (samplenum > TOTALNUM / 10) {
					mdp();

				}

			}

			//按照hash值写入文件
			String outstring = String.valueOf(counter);

			outstring = outstring.substring(1, outstring.length() - 1);

			String[] outar = outstring.split(",");
			System.out.println("outstring = " + outstring);

			FileWriter writer = new FileWriter("/home/wzhuo/example/mdp/out.txt");
			for (int i = 0; i < outar.length; i++) {
				if (i == 0) {
					writer.write(outar[i]);
				} else {
					outar[i] = outar[i].substring(1, outar[i].length() - 1);
					writer.write(outar[i]);
				}
				writer.write('\r');
			}

			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	//MDP
	public void mdp() {
		int value[] = new int[PNUM];
		
		
		
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		MPDmain mpd = new MPDmain();
		mpd.readFileByLines("/home/wzhuo/example/mdp/test.txt");

	}

}
