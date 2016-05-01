package wiki.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class MySAXParser {
	private File input_file;
	private String out_file;

	public MySAXParser(String file_name, String out_file) {
		this.out_file = out_file;
		this.input_file = new File(file_name);
	}

	public void parseDump() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			MyHandler myhandle = new MyHandler(input_file, out_file);

			 saxParser.parse(input_file, myhandle);
			//System.out.println("Direct Merging starts ...");
			//myhandle.merge_Files_V3();

		} catch (Exception e) {
			System.out.println("Exception occured");
			e.printStackTrace();
		}
	}
}

class MyHandler extends DefaultHandler {
	public IndexClass main_index;
	public Parser page;
	private int count = 0;
	public long starttime;
	public long endtime;
	private boolean isText;
	private boolean isPage;
	private boolean isTitle;
	private boolean isID;
	private boolean docidfound;
	private long time = 0;
	private long htime = 0;
	File in_file;
	String out_file;

	private int max_doc = 5000;
	private int doc_cnt = 0;
	private int file_cnt = 0;
	private long tot_docs = 0;
	ArrayList<File> fl = new ArrayList<File>();
	ArrayList<BufferedReader> frs = new ArrayList<BufferedReader>();

	HashMap<String, Integer[]> count_hash;

	// private PrintWriter writer = null;

	public MyHandler(File in_file, String out_file) {
		super();
		try {
			this.in_file = in_file;
			this.out_file = out_file;
			// System.out.println(in_file+" : "+out_file);
			main_index = new IndexClass();
			count_hash = new HashMap<String, Integer[]>();
			// writer = new PrintWriter("output.txt", "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startDocument() {
		System.out.println("Please wait...");
		starttime = System.currentTimeMillis();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("page")) {
			page = new Parser();
			isPage = true;
			docidfound = false;
		}
		if (!docidfound && qName.equalsIgnoreCase("id") && isPage) {
			isID = true;
			docidfound = true;
		}
		if (qName.equalsIgnoreCase("text")) {
			isText = true;
		}
		if (qName.equalsIgnoreCase("title")) {
			isTitle = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equalsIgnoreCase("text")) {
			isText = false;
		}

		if (qName.equalsIgnoreCase("page")) {
			isPage = false;
			docidfound = false;
			page.fillIndex(main_index);
			time += page.splittime;
			htime += page.bodytime;
			count_hash.put(page.doc_id, page.tot_fieldcount);

			page = null;
			doc_cnt++;
			tot_docs++;
			if (doc_cnt == max_doc) {
				doc_cnt = 0;
				main_index.writeIndex2(out_file + file_cnt, count_hash,
						tot_docs);
				File f = new File(out_file + file_cnt);
				fl.add(f);
				try {
					frs.add(new BufferedReader(new FileReader(f)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				count_hash.clear();
				main_index = null;
				main_index = new IndexClass();
				file_cnt++;
			}

			/*
			 * count++; if (count == 20) { main_index.writeIndex();
			 * System.exit(0); }
			 */

		}
		if (qName.equalsIgnoreCase("id")) {
			isID = false;
		}
		if (qName.equalsIgnoreCase("title")) {
			isTitle = false;
		}
	}

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		if (isText) {
			page.rawpage.append(new String(ch, start, length));
			// System.out.println(new String(ch, start, length));
			// writer.println(new String(ch, start, length));
		}
		if (isID) {
			page.doc_id = new String(ch, start, length);
		}
		if (isTitle) {
			page.title = new String(ch, start, length);
		}
	}

	public void merge_Files() throws IOException {

		BufferedWriter pi = new BufferedWriter(new FileWriter(new File(
				"primary_index")));
		BufferedWriter si = new BufferedWriter(new FileWriter(new File(
				"secondary_index")));
		BufferedWriter ti = new BufferedWriter(new FileWriter(new File(
				"tertiary_index")));

		ArrayList<String> terms = new ArrayList<String>();
		ArrayList<String> postingList = new ArrayList<String>();
		for (int i = 0; i < file_cnt; i++) {
			String line = frs.get(i).readLine();
			String str[] = line.split(":");
			terms.add(str[0]);
			postingList.add(str[1]);
		}
		int nullCount = file_cnt;
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
			StringBuilder concated_pList = new StringBuilder();
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
			pi.write(concated_pList + "\n");
		}
		pi.close();

		for (File file : fl) {
			file.delete();
		}
	}

	public void merge_Files_V2() throws IOException {

		BufferedWriter pi = new BufferedWriter(new FileWriter(new File(
				"primary_index")));
		BufferedWriter si = new BufferedWriter(new FileWriter(new File(
				"secondary_index")));
		BufferedWriter ti = new BufferedWriter(new FileWriter(new File(
				"tertiary_index")));
		ti.write("0:0\n");
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
		StringBuilder sb = new StringBuilder();
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
			sb.setLength(0);
			for (int i = 0; i < len; i++) {
				int j = 0;
				while (Character.isDigit(split_post[i].charAt(j)))
					j++;
				String did = split_post[i].substring(0, j);
				sb.append(did);
				int k = j;
				while (Character.isLetter(split_post[i].charAt(j))) {
					j++;
				}
				sb.append(split_post[i].substring(k, j));
				long tfidf = Long.parseLong(split_post[i].substring(j,
						split_post[i].length()));
				tfidf = tfidf * idf;
				sb.append(tfidf);
				if (i < len - 1)
					sb.append("|");
			}

			piadd += sb.length() + 1;

			pi.write(sb + "\n");

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

		}
		pi.close();
		si.close();
		ti.close();
		for (File file : fl) {
			file.delete();
		}

	}

	public void merge_Files_V3() throws IOException {

		File folder = new File("output");
		File[] listOfFiles = folder.listFiles();

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
		//tot_docs=16293;
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
		StringBuilder sb = new StringBuilder();
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
			sb.setLength(0);
			for (int i = 0; i < len; i++) {
				int j = 0;
				while (Character.isDigit(split_post[i].charAt(j)))
					j++;
				String did = split_post[i].substring(0, j);
				sb.append(did);
				int k = j;
				while (Character.isLetter(split_post[i].charAt(j))) {
					j++;
				}
				sb.append(split_post[i].substring(k, j));
				long tfidf = Long.parseLong(split_post[i].substring(j,
						split_post[i].length()));
				tfidf = tfidf * idf;
				sb.append(tfidf);
				if (i < len - 1)
					sb.append("|");
			}

			piadd += sb.length() + 1;

			pi.write(sb + "\n");

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

		}
		pi.close();
		si.close();
		ti.close();
		for (File file : fl) {
			file.delete();
		}

	}

	@Override
	public void endDocument() {
		// writer.close();
		// System.out.println("Split" + time / 1000.0);
		// System.out.println("Bodyhash" + htime / 1000.0);
		// endtime = System.currentTimeMillis();
		// System.out.println((endtime - starttime) / 1000);
		System.out.println("Writing to file starts now...");
		// main_index.printIndex();
		// System.out.println("Total docs" + tot_docs);

		try {
			// BufferedWriter bfwr = new BufferedWriter(new FileWriter(new File(
			// "doc_count")));
			// bfwr.write("" + tot_docs);
			// bfwr.close();
			System.out.println("Merging Files in progress...");
			merge_Files_V2();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// main_index.writeIndex(out_file);
		endtime = System.currentTimeMillis();
		System.out.println((endtime - starttime) / 1000);
	}
}
