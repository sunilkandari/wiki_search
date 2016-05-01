package wiki.search;

import java.util.ArrayList;

class Posting {
	public String doc_id;
	public ArrayList<Character> cat_list;
	public long tf_idf;

	public Posting(String d_id, ArrayList<Character> c_lst, long tfidf) {
		doc_id = d_id;
		cat_list = c_lst;
		tf_idf = tfidf;
	}

	public String getString() {
		StringBuilder s = new StringBuilder();
		for (Character c : cat_list) {
			s.append(c);
		}
		return doc_id + s + tf_idf;
	}
}

public class IndexEntry {
	public String term;
	public ArrayList<Posting> post;
	public int len;

	public IndexEntry(String raw_list) {
		post = new ArrayList<Posting>();
		String str[] = raw_list.split(":");
		term = str[0];

		String split_post[] = str[1].split("\\|");

		len = split_post.length;

		for (int i = 0; i < len; i++) {
			int j = 0;

			while (Character.isDigit(split_post[i].charAt(j)))
				j++;
			String did = split_post[i].substring(0, j);
			ArrayList<Character> cl = new ArrayList<Character>();
			char c = split_post[i].charAt(j);
			while (Character.isLetter(c)) {
				cl.add(c);
				j++;
				c = split_post[i].charAt(j);
			}
			long tfidf = Long.parseLong(split_post[i].substring(j,
					split_post[i].length()));

			post.add(new Posting(did, cl, tfidf));
		}
	}

	public IndexEntry(String raw_list, char cat) {
		post = new ArrayList<Posting>();
		String str[] = raw_list.split(":");
		term = str[0];

		String split_post[] = str[1].split("\\|");

		len = split_post.length;

		for (int i = 0; i < len; i++) {
			int j = 0;

			while (Character.isDigit(split_post[i].charAt(j)))
				j++;
			String did = split_post[i].substring(0, j);
			ArrayList<Character> cl = new ArrayList<Character>();
			char c = split_post[i].charAt(j);
			while (Character.isLetter(c)) {
				cl.add(c);
				j++;
				c = split_post[i].charAt(j);
			}
			long tfidf = Long.parseLong(split_post[i].substring(j,
					split_post[i].length()));

			if (cl.contains(cat))
				post.add(new Posting(did, cl, tfidf));

			
		}
		len = post.size();
	}

	public void printString() {
		System.out.print(term + "-");
		for (Posting p : post) {
			System.out.print(p.doc_id + ":" + p.cat_list.toString() + ":"
					+ p.tf_idf + "  ");
		}

	}

	public String getString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(post.get(i).getString() + "|");
		}
		return term + ":" + sb;

	}

	public void update_TF_IDF(double idf) {
		for (Posting p : post) {
			p.tf_idf = (long) (p.tf_idf * idf) / 4;

		}
	}

}
