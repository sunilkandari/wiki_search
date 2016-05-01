package wiki.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

class Docweight {
	String doc_id;
	long weight;

	public Docweight(String id, long wt) {
		doc_id = id;
		weight = wt;
	}
}

public class SearchClass {
	RandomAccessFile pir;
	RandomAccessFile sir;
	BufferedReader tir;

	RandomAccessFile pir2;
	BufferedReader sir2;

	TreeMap<String, String> ter;
	ArrayList<String> ter_al;

	TreeMap<String, String> sec2;
	ArrayList<Long> sec2_al;

	ArrayList<IndexEntry> all_list;
	TreeMap<String, ArrayList<Docweight>> counttodoc;
	ArrayList<String> final_doc_search_list;

	public SearchClass() {
		try {
			pir = new RandomAccessFile("primary_index", "r");
			sir = new RandomAccessFile("secondary_index",
					"r");
			tir = new BufferedReader(new FileReader(new File(
					"tertiary_index")));
			ter = new TreeMap<String, String>();

			pir2 = new RandomAccessFile(
					"DOCID-TERM-PRIMARY.txt", "r");

			sir2 = new BufferedReader(new FileReader(new File(
					"DOCID-TERM-SECONDARY.txt")));

			sec2 = new TreeMap<String, String>();

			String line;
			String str[];
			while ((line = tir.readLine()) != null) {
				str = line.split(":");
				ter.put(str[0], str[1]);
			}

			while ((line = sir2.readLine()) != null) {
				str = line.split("-");
				sec2.put(str[0], str[1]);
			}

			ter_al = new ArrayList<String>(ter.keySet());
			sec2_al = new ArrayList<Long>();
			for (String l : sec2.keySet())
				sec2_al.add(Long.parseLong(l));

			Collections.sort(sec2_al);
			// System.out.println(sec2_al);

			all_list = new ArrayList<IndexEntry>();
			counttodoc = new TreeMap<String, ArrayList<Docweight>>();
			final_doc_search_list = new ArrayList<String>();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public String getStemmedWord(String word) {
		Stemmer stem = new Stemmer();
		String stemed_word;
		int len = word.length();
		for (int i = 0; i < len; i++) {
			stem.add(word.charAt(i));
		}
		stem.stem();
		{
			stemed_word = stem.toString();
		}
		return stemed_word;
	}

	public long getTertiaryLine(String term) throws IOException {
		String forwardLine = "";
		forwardLine = tir.readLine();
		String str[] = forwardLine.split(":");
		long retval = Long.parseLong(str[1]);

		while ((forwardLine = tir.readLine()) != null) {
			str = forwardLine.split(":");
			if (str[0].compareTo(term) > 0) {
				break;
			}
			retval = Long.parseLong(str[1]);
		}
		return retval;
	}

	public long getTertiarybyte(String term) throws IOException {
		String key = getTertiaryLine2(term);

		return Long.parseLong(ter.get(key));
	}

	public String getTertiaryLine2(String term) throws IOException {
		int beg = 0, end = ter.size() - 1, mid;
		mid = (beg + end) / 2;
		while (beg <= end) {

			if (ter_al.get(mid).equals(term)) {
				break;
			} else if (ter_al.get(mid).compareTo(term) > 0) {
				end = mid - 1;
			} else {
				beg = mid + 1;
			}
			mid = (beg + end) / 2;
		}
		if (mid >= 0 && ter_al.get(mid).compareTo(term) > 0) {
			return ter_al.get(mid - 1);
		} else {
			return ter_al.get(mid);
		}

	}

	public long getSecondary2byte(String docid) throws IOException {
		if (Long.parseLong(docid) < sec2_al.get(0)) {
			return 0;
		}

		String key = getSecondary2Line2(docid);
		return Long.parseLong(sec2.get(key));
	}

	public String getSecondary2Line2(String docid) throws IOException {
		int beg = 0, end = sec2_al.size() - 1, mid;
		mid = (beg + end) / 2;

		while (beg <= end) {
			// System.out.println(sec2_al.get(mid) + ":" + docid);
			if (sec2_al.get(mid) == Long.parseLong(docid)) {
				break;
			} else if (sec2_al.get(mid) > Long.parseLong(docid)) {
				end = mid - 1;
			} else {
				beg = mid + 1;
			}
			mid = (beg + end) / 2;

		}

		if (mid >= 0 && sec2_al.get(mid) > Long.parseLong(docid)) {
			return sec2_al.get(mid - 1).toString();
		} else {
			return sec2_al.get(mid).toString();
		}

	}

	public long getSecondaryLine(String term, long byteNum) throws IOException {

		sir.seek(byteNum);
		String forwardLine = sir.readLine();

		String str[] = forwardLine.split(":");
		long retval = Long.parseLong(str[1]);

		while ((forwardLine = sir.readLine()) != null) {
			str = forwardLine.split(":");
			if (str[0].compareTo(term) > 0) {
				break;
			}
			retval = Long.parseLong(str[1]);
		}
		return retval;
	}

	public String getPrimaryLine(String term, long byteNum) throws IOException {

		pir.seek(byteNum);

		return pir.readLine();
	}

	public String getPrimaryLine2(String docid) throws IOException {

		long byt = getSecondary2byte(docid);
		pir2.seek(byt);

		String line;
		while ((line = pir2.readLine()) != null) {

			// System.out.println(line);
			if (line.startsWith(docid + "-")) {
				return line;
			}
		}

		return "";
	}

	public void searchResultFor(String query) throws IOException {
		query = query.toLowerCase().replaceAll("[^a-z0-9]", " ");
		String words[] = query.split(" ");
		int qlen = words.length;
		long byt;
		for (int i = 0; i < qlen; i++) {
			words[i] = getStemmedWord(words[i]);
			if (!Constants.hs.contains(words[i])) {
				// byt = getTertiaryLine(words[i]);
				byt = getTertiarybyte(words[i]);
				// System.out.println(byt);
				byt = getSecondaryLine(words[i], byt);
				// System.out.println(getPrimaryLine(words[i], byt));

				all_list.add(new IndexEntry(getPrimaryLine(words[i], byt)));
			}
		}

		unionAllList();
		obtainFinalDocSearchList();

		// System.out.println(final_doc_search_list);
		printResult();

		all_list = new ArrayList<IndexEntry>();
		final_doc_search_list = new ArrayList<String>();

		counttodoc = new TreeMap<String, ArrayList<Docweight>>();

		// System.out.println(counttodoc.toString());
	}

	public void searchResultFor(String query, char cat) throws IOException {
		query = query.toLowerCase().replaceAll("[^a-z0-9]", " ");
		String words[] = query.split(" ");
		int qlen = words.length;
		long byt;

		for (int i = 0; i < qlen; i++) {
			words[i] = getStemmedWord(words[i]);
			if (!Constants.hs.contains(words[i])) {
				// byt = getTertiaryLine(words[i]);
				byt = getTertiarybyte(words[i]);
				// System.out.println(byt);
				byt = getSecondaryLine(words[i], byt);
				// System.out.println(getPrimaryLine(words[i], byt));
				IndexEntry ie = new IndexEntry(getPrimaryLine(words[i], byt),
						cat);
				if (ie.len > 0)
					all_list.add(ie);
			}
		}

		/*
		 * for (IndexEntry ie : all_list) { System.out.println(ie.len);
		 * ie.printString(); }
		 */
		unionAllList();

		obtainFinalDocSearchList();
		printResult();

		// System.out.println(final_doc_search_list);

		all_list = new ArrayList<IndexEntry>();
		final_doc_search_list = new ArrayList<String>();
		counttodoc = new TreeMap<String, ArrayList<Docweight>>();
		// System.out.println(counttodoc.toString());
	}

	public void unionAllList() {
		int listcount = all_list.size();
		int nulllistcount = listcount;

		/* sab list ka ek uthane ke liye */
		ArrayList<Docweight> dws = new ArrayList<Docweight>();
		/* har list ka ek index */
		ArrayList<Integer> indices = new ArrayList<Integer>();

		for (int i = 0; i < listcount; i++) {
			indices.add(0);
			Posting p = all_list.get(i).post.get(0);
			dws.add(new Docweight(p.doc_id, p.tf_idf));
		}

		ArrayList<Integer> minIndices = new ArrayList<Integer>();

		while (nulllistcount > 0) {
			String min = "zzz{";
			long min_weight = 0;
			for (int i = 0; i < listcount; i++) {
				Docweight dw = dws.get(i);
				if (dw != null) {
					if (dw.doc_id.compareTo(min) < 0) {
						min = dw.doc_id;
						min_weight = dw.weight;
						minIndices.clear();
						minIndices.add(i);
					} else if (dw.doc_id.compareTo(min) == 0) {
						min_weight += dw.weight;
						minIndices.add(i);
					}
				}
			}
			int eq_count = minIndices.size();
			if (!counttodoc.containsKey(eq_count + "")) {
				ArrayList<Docweight> obj = new ArrayList<Docweight>();
				long wt = min_weight / eq_count;
				obj.add(new Docweight(min, wt));
				counttodoc.put(eq_count + "", obj);
			} else {
				long wt = min_weight / eq_count;
				counttodoc.get(eq_count + "").add(new Docweight(min, wt));
			}

			for (int j = 0; j < eq_count; j++) {
				int i = minIndices.get(j);
				int opt = indices.get(i) + 1;
				if (opt < all_list.get(i).len) {
					indices.set(i, indices.get(i) + 1);
					// String docid =
					// all_list.get(i).post.get(indices.get(i)).doc_id;
					Posting p = all_list.get(i).post.get(opt);
					dws.get(i).doc_id = p.doc_id;
					dws.get(i).weight = p.tf_idf;
				} else {
					dws.set(i, null);
					nulllistcount--;
				}
			}

		}

	}

	public void obtainFinalDocSearchList() {
		int tot = counttodoc.size();
		// final_doc_search_list.clear();
		int finaldoccount = 0;

		final_doc_search_list = new ArrayList<String>();
		for (int i = tot; i >= 1; i--) {
			ArrayList<Docweight> docweightlist = counttodoc.get(i + "");
			Collections.sort(docweightlist, new Comparator<Docweight>() {
				@Override
				public int compare(Docweight p1, Docweight p2) {
					return (int) (p2.weight - p1.weight); // Ascending
				}

			});

			for (Docweight dw : docweightlist) {
				final_doc_search_list.add(dw.doc_id);
				finaldoccount++;
				if (finaldoccount >= 10)
					break;
			}
		}
	}

	public void printResult() throws IOException {

		for (String doc : final_doc_search_list) {
			// System.out.println(doc);

			if (doc.equals("zzz{"))
				break;
			System.out.println(getPrimaryLine2(doc));

		}

	}
}
