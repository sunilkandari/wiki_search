package wiki.search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class StorageClass {
	public int fieldcount[];

	public StorageClass(int title, int body, int infobox, int category,
			int extlinks, int ref) {
		fieldcount = new int[6];
		fieldcount[0] = title;
		fieldcount[1] = body;
		fieldcount[2] = infobox;
		fieldcount[3] = category;
		fieldcount[4] = extlinks;
		fieldcount[5] = ref;
	}

	public void printData() {
		System.out.println(" t:" + fieldcount[0] + " b:" + fieldcount[1]
				+ " i:" + fieldcount[2] + " c:" + fieldcount[3] + " e:"
				+ fieldcount[4] + " r" + fieldcount[5]);

	}

	public void printCombinedData() {
		int sum = 0;
		for (int i = 0; i < 6; i++)
			sum += fieldcount[i];
		System.out.println(": " + sum);
	}
}

public class IndexClass {
	public TreeMap<String, TreeMap> index;

	public IndexClass() {
		index = new TreeMap<String, TreeMap>();
	}

	/* risky */

	public void insertWord(String word, String doc_id, int field) {
		if (!index.containsKey(word)) {
			StorageClass sc = new StorageClass(0, 0, 0, 0, 0, 0);
			sc.fieldcount[field] = 1;
			TreeMap<String, StorageClass> hm = new TreeMap<String, StorageClass>();
			hm.put(doc_id, sc);
			index.put(word, hm);
		} else {
			TreeMap<String, StorageClass> hm = index.get(word);
			if (!hm.containsKey(doc_id)) {
				StorageClass sc = new StorageClass(0, 0, 0, 0, 0, 0);
				sc.fieldcount[field] = 1;
				hm.put(doc_id, sc);
			} else {
				hm.get(doc_id).fieldcount[field]++;
			}
		}
	}

	public void printIndex() {
		System.out.println("printing Index :");
		for (Map.Entry<String, TreeMap> entry : index.entrySet()) {
			TreeMap<String, StorageClass> hm = entry.getValue();
			System.out.print(entry.getKey() + ":");
			for (Map.Entry<String, StorageClass> inner : hm.entrySet()) {
				int t = inner.getValue().fieldcount[Constants.TITLE];
				int b = inner.getValue().fieldcount[Constants.BODY];
				int i = inner.getValue().fieldcount[Constants.INFOBOX];
				int c = inner.getValue().fieldcount[Constants.CATEGORY];
				int e = inner.getValue().fieldcount[Constants.EXTERNAL_LINKS];
				int r = inner.getValue().fieldcount[Constants.REFERENCES];
				String s = "d" + inner.getKey();
				if (t > 0)
					s += "-t" + t;
				if (b > 0)
					s += "b" + b;
				if (i > 0)
					s += "i" + i;
				if (c > 0)
					s += "c" + c;
				if (e > 0)
					s += "e" + e;
				if (r > 0)
					s += "r" + r;
				s += "|";
				System.out.print(s);
			}
			;
			System.out.println();
		}
	}

	public void writeIndex(String out_file) {

		// StringBuilder sb=new StringBuilder("");
		try {
			File f = new File(out_file);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter writer = new FileWriter(f);
			BufferedWriter bf = new BufferedWriter(writer, 8192);
			for (Map.Entry<String, TreeMap> entry : index.entrySet()) {
				TreeMap<String, StorageClass> hm = entry.getValue();
				// String s = "";
				bf.write(entry.getKey() + ":");
				// s=s+entry.getKey() + ":";
				Set<String> keyset = hm.keySet();
				int tot = keyset.size(), j = 0;
				for (String innerkey : keyset) {
					j++;
					StorageClass sh = hm.get(innerkey);
					int t = sh.fieldcount[0];
					int b = sh.fieldcount[1];
					int i = sh.fieldcount[2];
					int c = sh.fieldcount[3];
					int e = sh.fieldcount[4];
					int r = sh.fieldcount[5];
					// s = s + "d" + innerkey;
					bf.write(innerkey);
					if (t > 0)
						// s += "t" + t;
						bf.write("t" + t);
					if (b > 0)
						// s += "b" + b;
						bf.write("b" + b);
					if (i > 0)
						// s += "i" + i;
						bf.write("i" + i);
					if (c > 0)
						// s += "c" + c;
						bf.write("c" + c);
					if (e > 0)
						// s += "e" + e;
						bf.write("e" + e);
					if (r > 0)
						// s += "r" + r;
						bf.write("r" + r);

					// s += "|";
					if (j < tot)
						bf.write("|");

					// System.out.print(s);

				}

				// s = s.substring(0, s.length() - 1);

				bf.write("\n");
				// System.out.println();
			}
			bf.flush();
			bf.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public void writeIndex2(String out_file,
			HashMap<String, Integer[]> count_hash, long tot_docs) {

		// StringBuilder sb=new StringBuilder("");
		try {
			File f = new File(out_file);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter writer = new FileWriter(f);
			BufferedWriter bf = new BufferedWriter(writer, 8192);
			for (Map.Entry<String, TreeMap> entry : index.entrySet()) {
				TreeMap<String, StorageClass> hm = entry.getValue();
				// s=s+entry.getKey() + ":";
				Set<String> keyset = hm.keySet();
				int tot = keyset.size(), j = 0;
				// String s = "";

				// int idf = (int) (Math.log10(((double) tot_docs) / tot) *
				// 100);
				// System.out.println(tot_docs + " : " + tot + "  : " + idf);
				bf.write(entry.getKey() + ":");

				for (String innerkey : keyset) {
					j++;
					StorageClass sh = hm.get(innerkey);
					int t = sh.fieldcount[0];
					int b = sh.fieldcount[1];
					int i = sh.fieldcount[2];
					int c = sh.fieldcount[3];
					int e = sh.fieldcount[4];
					int r = sh.fieldcount[5];
					// s = s + "d" + innerkey;

					bf.write(innerkey);
					Integer counter[] = count_hash.get(innerkey);
					double tf = (((double) sh.fieldcount[0]) / counter[0])
							* 1400 + (((double) sh.fieldcount[1]) / counter[1])
							* 800 + (((double) sh.fieldcount[2]) / counter[2])
							* 1000 + (((double) sh.fieldcount[3]) / counter[3])
							* 1200 + (((double) sh.fieldcount[4]) / counter[4])
							* 500 + (((double) sh.fieldcount[5]) / counter[5])
							* 300;
					// tf = (int) (tf * 10000);

					// bf.write("-" + (int) (tf*10));

					if (t > 0)
						// s += "t" + t;
						bf.write("t");
					if (b > 0)
						// s += "b" + b;
						bf.write("b");
					if (i > 0)
						// s += "i" + i;
						bf.write("i");
					if (c > 0)
						// s += "c" + c;
						bf.write("c");
					if (e > 0)
						// s += "e" + e;
						bf.write("e");
					if (r > 0)
						// s += "r" + r;
						bf.write("r");

					bf.write("" + (int) (tf * 10));
					// s += "|";
					if (j < tot)
						bf.write("|");

					// System.out.print(s);

				}

				// s = s.substring(0, s.length() - 1);

				bf.write("\n");
				// System.out.println();
			}
			bf.flush();
			bf.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/*
	 * public void writeIndex() { BufferedWriter writer; // StringBuilder sb=new
	 * StringBuilder(""); try {
	 * 
	 * File outfile = new File("output.txt"); if (outfile.exists()) {
	 * outfile.delete(); }
	 * 
	 * outfile.createNewFile();
	 * 
	 * // PrintWriter writer = new PrintWriter("output.txt", "UTF-8"); writer =
	 * new BufferedWriter(new FileWriter(outfile)); for (String entry :
	 * index.keySet()) { HashMap<String, StorageClass> hm = index.get(entry);
	 * String s = ""; writer.write(entry + ":"); for (String inner :
	 * hm.keySet()) { StorageClass sc=hm.get(inner); int t =
	 * sc.fieldcount[Constants.TITLE]; int b = sc.fieldcount[Constants.BODY];
	 * int i =sc.fieldcount[Constants.INFOBOX]; int c
	 * =sc.fieldcount[Constants.CATEGORY]; int e =
	 * sc.fieldcount[Constants.EXTERNAL_LINKS]; int r =
	 * sc.fieldcount[Constants.REFERENCES]; s = s + "d" + inner; if (t > 0) s +=
	 * "t" + t; if (b > 0) s += "b" + b; if (i > 0) s += "i" + i; if (c > 0) s
	 * += "c" + c; if (e > 0) s += "e" + e; if (r > 0) s += "r" + r;
	 * 
	 * s += "|"; // System.out.print(s);
	 * 
	 * } s = s.substring(0, s.length() - 1); writer.write(s + "\n"); //
	 * System.out.println(); } writer.flush(); writer.close(); } catch
	 * (FileNotFoundException err) { err.printStackTrace(); } catch
	 * (UnsupportedEncodingException err) { err.printStackTrace(); } catch
	 * (Exception err) { err.printStackTrace(); } }
	 */
}
