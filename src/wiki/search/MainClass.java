package wiki.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {

	public static void merge_Files_V3() throws IOException {

		File folder = new File("output_dir");
		File[] listOfFiles = folder.listFiles();
		int file_cnt = 0;
		long tot_docs = 0;
		ArrayList<File> fl = new ArrayList<File>();
		ArrayList<BufferedReader> frs = new ArrayList<BufferedReader>();
		frs.clear();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				frs.add(new BufferedReader(new FileReader(file)));
			}
		}
		file_cnt = listOfFiles.length;

		BufferedWriter pi = new BufferedWriter(new FileWriter(new File(
				"primary_index")));
		BufferedWriter si = new BufferedWriter(new FileWriter(new File(
				"secondary_index")));
		BufferedWriter ti = new BufferedWriter(new FileWriter(new File(
				"tertiary_index")));
		ti.write("0:0\n");

		tot_docs = 16244931;
		// tot_docs = 16293;
		// int MAX_P_LINES = 15;
		int MAX_S_LINES = 700;
		long pi_byte = 0, piadd = 0;// pi_line = 0;
		long si_byte = 4, siadd = 0, si_line = 0;

		ArrayList<String> terms = new ArrayList<String>();
		ArrayList<String> postingList = new ArrayList<String>();
		for (int i = 0; i < file_cnt; i++) {
			String line = frs.get(i).readLine();
			String str[] = line.split(":");
			terms.add(str[0]);
			postingList.add(str[1]);
		}
		int nullCount = file_cnt;
		// StringBuilder sb = new StringBuilder();
		StringBuilder concated_pList = new StringBuilder();
		while (nullCount > 0) {
			String min_string = "zzzzzzzz{";
			ArrayList<Integer> min_indices = new ArrayList<Integer>();
			for (int i = 0; i < file_cnt; i++) {
				String term = terms.get(i);
				if (term != null) {
					if (term.compareTo(min_string) < 0) {
						min_string = term;
						min_indices.clear();
						min_indices.add(i);
					} else if (term.compareTo(min_string) == 0) {
						min_indices.add(i);
					}
				}
			}
			concated_pList.setLength(0);
			piadd += min_string.length() + 1;

			pi.write(min_string + ":");
			int eq_count = min_indices.size();
			for (int j = 0; j < eq_count; j++) {
				int i = min_indices.get(j);
				if (j < eq_count - 1)
					concated_pList.append(postingList.get(i) + "|");
				else
					concated_pList.append(postingList.get(i));
				String line = frs.get(i).readLine();
				if (line != null) {
					String str[] = line.split(":");
					terms.set(i, str[0]);
					postingList.set(i, str[1]);
				} else {
					nullCount--;
				}
			}

			String split_post[] = concated_pList.toString().split("\\|");

			int len = split_post.length;
			int idf = (int) (Math.log10(((double) tot_docs) / len));
			// sb.setLength(0);
			for (int i = 0; i < len; i++) {
				int j = 0;
				while (Character.isDigit(split_post[i].charAt(j)))
					j++;
				String did = split_post[i].substring(0, j);
				pi.write(did);
				piadd += did.length();
				// sb.append(did);
				int k = j;
				while (Character.isLetter(split_post[i].charAt(j))) {
					j++;
				}
				pi.write(split_post[i].substring(k, j));
				piadd += split_post[i].substring(k, j).length();
				// sb.append(split_post[i].substring(k, j));
				long tfidf = Long.parseLong(split_post[i].substring(j,
						split_post[i].length()));
				tfidf = tfidf * idf;
				pi.write(tfidf + "");
				piadd += (tfidf + "").length();
				// sb.append(tfidf);
				if (i < len - 1) {
					// sb.append("|");
					pi.write("|");
					piadd += 1;
				}
			}

			// piadd += sb.length() + 1;
			piadd += 1;
			// pi.write(sb + "\n");

			// pi_line++;

			// if (pi_line == MAX_P_LINES) {

			// pi_line = 0;

			si_line++;
			si.write(min_string + ":" + pi_byte + "\n");
			int lt = ("" + pi_byte).length();
			siadd += min_string.length() + lt + 2;

			if (si_line == MAX_S_LINES) {
				si_line = 0;
				ti.write(min_string + ":" + si_byte + "\n");
			}
			si_byte += siadd;
			siadd = 0;
			// }
			pi_byte += piadd;
			piadd = 0;
			if (nullCount % 20 == 0)
				System.gc();
		}
		pi.close();
		si.close();
		ti.close();

	}

	public static void main(String[] args) {
		/*
		 * try { merge_Files_V3(); System.out.println("Done.."); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		// System.out.println(args[0]+" : "+ args[1]);
		// MySAXParser myparser = new MySAXParser(args[0], args[1]);
		// myparser.parseDump();

		Scanner scan = new Scanner(System.in);

		SearchClass sc = new SearchClass();
		while (true) {
			System.out.println("Enter Query :");
			String query = scan.nextLine();
			try {
				long srt = System.currentTimeMillis();
				if (query.contains(":")) {
					String str[] = query.split(":");
					ArrayList<String> cats = new ArrayList<String>();
					cats.add("b");
					cats.add("t");
					cats.add("i");
					cats.add("r");
					cats.add("c");
					cats.add("e");
					if (cats.contains(str[0].trim())) {
						sc.searchResultFor(query, str[0].trim().charAt(0));
					} else {

						sc.searchResultFor(query);
					}

				} else
					sc.searchResultFor(query);

				long end = System.currentTimeMillis();
				System.out.println((((double) end) - srt) / 1000);
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		// System.out.println(Constants.hs.contains("the"));
	}
}