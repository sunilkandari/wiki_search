package wiki.search;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	public StringBuilder rawpage;
	public StringBuilder rawBody;
	public StringBuilder rawInfo;
	public StringBuilder rawCategory;
	public StringBuilder raweLinks;
	public StringBuilder rawRef;
	public String title;
	public String doc_id;
	public StringTokenizer st;

	public long splittime = 0;
	public long bodytime = 0;
	public long cattime = 0;
	public Integer tot_fieldcount[];

	public Parser() {
		rawpage = new StringBuilder();
		rawBody = new StringBuilder();
		rawInfo = new StringBuilder();
		rawCategory = new StringBuilder();
		raweLinks = new StringBuilder();
		rawRef = new StringBuilder();
		tot_fieldcount = new Integer[6];
		tot_fieldcount[0] = 1;
		tot_fieldcount[1] = 1;
		tot_fieldcount[2] = 1;
		tot_fieldcount[3] = 1;
		tot_fieldcount[4] = 1;
		tot_fieldcount[5] = 1;

	}

	public void splitRawText() {
		int l = rawpage.length();

		int body_beg_ind = 0;
		for (int i = 0; i < l; i++) {
			if (i + 3 < l && rawpage.charAt(i) == '<'
					&& rawpage.charAt(i + 1) == '!'
					&& rawpage.substring(i, i + 4).equals("<!--")) {
				if (body_beg_ind < l && body_beg_ind <= i && i <= l)
					rawBody.append(rawpage.substring(body_beg_ind, i));
				while (i + 2 < l && !rawpage.substring(i, i + 3).equals("-->")) {
					// System.out.print(rawpage.charAt(i));
					i++;
				}
				i += 2;
				body_beg_ind = i + 1;

			} else if (i + 4 < l
					&& rawpage.charAt(i) == '<'
					&& rawpage.charAt(i + 1) == 'r'

					&& (rawpage.substring(i, i + 5).equals("<ref>") || rawpage
							.substring(i, i + 5).equals("<ref "))) {
				if (body_beg_ind < l && body_beg_ind <= i && i <= l)
					rawBody.append(rawpage.substring(body_beg_ind, i));
				while (i < l && rawpage.charAt(i) != '>')
					i++;
				if (rawpage.charAt(i - 1) == '/') {
					body_beg_ind = i + 1;
					continue;
				}
				int ref_beg = i + 1;
				while (i + 5 < l
						&& !(rawpage.charAt(i) == '<'
								&& rawpage.charAt(i + 1) == '/'
								&& rawpage.charAt(i + 2) == 'r' && rawpage
								.substring(i, i + 6).equals("</ref>"))) {
					i++;
				}
				if (ref_beg < l && ref_beg <= i && i <= l)
					rawRef.append("\n" + rawpage.substring(ref_beg, i));
				i = i + 5;
				body_beg_ind = i + 1;

			} else if (i + 10 < l && rawpage.charAt(i) == '['
					&& rawpage.charAt(i + 1) == '['
					&& rawpage.charAt(i + 2) == 'C'
					&& rawpage.substring(i, i + 11).equals("[[Category:")) {
				if (body_beg_ind < l && body_beg_ind <= i && i <= l)
					rawBody.append(rawpage.substring(body_beg_ind, i));
				i = i + 11;
				int cat_beg = i, open_square = 0;
				while (i + 1 < l
						&& !(rawpage.charAt(i) == ']'
								&& (rawpage.charAt(i + 1) == ']') && open_square == 0)) {
					if (i + 1 < l && rawpage.charAt(i) == '['
							&& rawpage.charAt(i + 1) == '[') {
						open_square++;
					} else if (i + 1 < l && rawpage.charAt(i) == ']'
							&& rawpage.charAt(i + 1) == ']') {
						open_square--;
					}
					i++;
				}
				if (cat_beg < l && cat_beg <= i && i <= l)
					rawCategory.append("\n" + rawpage.substring(cat_beg, i));
				i = i + 1;
				body_beg_ind = i + 1;
			} else if (i + 9 < l && rawpage.charAt(i) == '{'
					&& rawpage.charAt(i + 1) == '{'
					&& rawpage.charAt(i + 2) == 'I'
					&& rawpage.substring(i, i + 10).equals("{{Infobox ")) {
				if (body_beg_ind < l && body_beg_ind <= i && i <= l)
					rawBody.append(rawpage.substring(body_beg_ind, i));
				i = i + 10;
				int ibox_beg = i, open_curly = 0;
				while (i + 1 < l
						&& !(rawpage.charAt(i) == '}'
								&& (rawpage.charAt(i + 1) == '}') && open_curly == 0)) {
					if (rawpage.charAt(i) == '{'
							&& rawpage.charAt(i + 1) == '{') {
						open_curly++;
					} else if (rawpage.charAt(i) == '}'
							&& rawpage.charAt(i + 1) == '}') {
						open_curly--;
					}
					i++;
				}
				if (ibox_beg < l && ibox_beg <= i && i <= l)
					rawInfo.append("\n" + rawpage.substring(ibox_beg, i));
				i = i + 1;
				body_beg_ind = i + 1;
			}

			else if (i + 1 < l && rawpage.charAt(i) == '='
					&& rawpage.charAt(i + 1) == '=') {
				int j = i + 2;
				while (j + 1 < l
						&& !(rawpage.charAt(j) == '=' && rawpage.charAt(j + 1) == '=')) {
					j++;
				}

				if (rawpage.substring(i + 2, j).trim().equals("External links")) {
					rawBody.append(rawpage.substring(body_beg_ind, i));
					i = j = j + 2;
					while (j + 10 < l
							&& !rawpage.substring(j, j + 11).equals(
									"[[Category:")) {
						// System.out.print(rawpage.charAt(j));
						j++;
					}
					Pattern pattern = Pattern.compile("\\[(.*?)\\]");
					Matcher matcher = pattern
							.matcher(rawpage.subSequence(i, j));
					while (matcher.find()) {
						// System.out.println(matcher.group(0));
						raweLinks.append("\n" + matcher.group(0));
						// System.out.println(raweLinks);
					}
					// raweLinks.append("\n"+rawpage.subSequence(i, j));
					i = j - 1;
					body_beg_ind = i;
				}

			} /*
			 * else if (i + 1 < l && rawpage.charAt(i) == '=' &&
			 * rawpage.charAt(i + 1) == '=') { int j = i + 2; while (j + 1 < l
			 * && !(rawpage.charAt(j) == '=' && rawpage.charAt(j + 1) == '=')) {
			 * j++; }
			 * 
			 * if (rawpage.substring(i + 2, j).trim()
			 * .equalsIgnoreCase("References")) {
			 * rawBody.append(rawpage.substring(body_beg_ind, i)); i = j = j +
			 * 2; while (rawpage.substring(j, j + 2).equals("==") || (j + 10 < l
			 * && !rawpage.substring(j, j + 11) .equals("[[Category:"))) { //
			 * System.out.print(rawpage.charAt(j)); j++; } Pattern pattern =
			 * Pattern.compile("\\{{(.*?)\\}}"); Matcher matcher = pattern
			 * .matcher(rawpage.subSequence(i, j)); while (matcher.find()) {
			 * System.out.println(matcher.group(0)); rawRef.append("\n" +
			 * matcher.group(0)); // System.out.println(raweLinks); } //
			 * raweLinks.append("\n"+rawpage.subSequence(i, j)); i = j - 1;
			 * body_beg_ind = i; }
			 * 
			 * }
			 */
		}
		if (body_beg_ind < l)
			rawBody.append(rawpage.substring(body_beg_ind, l));
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

	public void removeSpecialCharsFromTitle() {
		title = title.toLowerCase().replaceAll("[^a-z0-9]", " ");
	}

	public String removeSpecialCharsFromCategory() {
		return rawCategory.toString().toLowerCase().replaceAll("[^a-z]", " ");
	}

	public String removeSpecialCharsFromBody() {
		return rawBody.toString().toLowerCase().replaceAll("[^a-z0-9]", " ");
	}

	public String removeSpecialCharsFromRef() {
		return rawRef.toString().toLowerCase().replaceAll("[^a-z]", " ");
	}

	public String removeSpecialCharsFromExt() {
		return raweLinks.toString().toLowerCase().replaceAll("[^a-z]", " ");
	}

	public String removeSpecialCharsFromInfobox() {
		return rawInfo.toString().toLowerCase().replaceAll("[^a-z]", " ");
	}

	public void parseTitleAndfillIndex(IndexClass main_index) {
		removeSpecialCharsFromTitle();
		st = new StringTokenizer(title, " ");
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			// word = word.toLowerCase();
			word = getStemmedWord(word);
			if (!Constants.hs.contains(word)) {

				tot_fieldcount[0]++;
				main_index.insertWord(word, doc_id, 0);
			}
		}
	}

	public void parseBodyAndFillIndex(IndexClass main_index) {
		String body = removeSpecialCharsFromBody();

		st = new StringTokenizer(body, " ");
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			// word = word.toLowerCase();
			word = getStemmedWord(word);
			if (!Constants.hs.contains(word)) {
				tot_fieldcount[1]++;
				main_index.insertWord(word, doc_id, 1);
			}
		}

	}

	public void parseRefAndFillIndex(IndexClass main_index) {
		String ref = removeSpecialCharsFromRef();
		st = new StringTokenizer(ref, " ");
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			word = getStemmedWord(word);
			if (!Constants.hs.contains(word)) {
				tot_fieldcount[5]++;
				main_index.insertWord(word, doc_id, 5);
			}
		}

	}

	public void parseElinksAndFillIndex(IndexClass main_index) {
		String elinks = removeSpecialCharsFromExt();
		st = new StringTokenizer(elinks, " ");
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			word = getStemmedWord(word);
			if (!Constants.hs.contains(word)) {
				tot_fieldcount[4]++;
				main_index.insertWord(word, doc_id, 4);
			}
		}

	}

	public void parseInfoBoxAndFillIndex(IndexClass main_index) {
		String infobox = removeSpecialCharsFromInfobox();
		st = new StringTokenizer(infobox, " ");

		while (st.hasMoreTokens()) {

			String word = st.nextToken();
			word = getStemmedWord(word);
			if (!Constants.hs.contains(word)) {
				tot_fieldcount[2]++;
				main_index.insertWord(word, doc_id, 2);
			}
		}

	}

	public void parseCategoryAndFillIndex(IndexClass main_index) {
		String cat = removeSpecialCharsFromCategory();
		st = new StringTokenizer(cat, " ");
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			word = getStemmedWord(word);
			if (!Constants.hs.contains(word)) {
				tot_fieldcount[3]++;
				main_index.insertWord(word, doc_id, 3);
			}
		}

	}

	public void fillIndex(IndexClass main_index) {
		parseTitleAndfillIndex(main_index);

		// long starttime = System.currentTimeMillis();
		splitRawText();
		// System.out.println(rawBody);
		long endtime = System.currentTimeMillis();
		// splittime += (endtime - starttime);

		// starttime = System.currentTimeMillis();
		parseBodyAndFillIndex(main_index);
		// endtime = System.currentTimeMillis();
		// bodytime += (endtime - starttime);

		parseCategoryAndFillIndex(main_index);
		parseElinksAndFillIndex(main_index);
		parseInfoBoxAndFillIndex(main_index);
		parseRefAndFillIndex(main_index);
		// System.out.println("References :::: " + rawRef);
		/*
		 * try { PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
		 * writer.println("Body :: " + rawBody); writer.println("Ref ::" +
		 * rawRef); writer.println("Cat ::" + rawCategory);
		 * writer.println("InfoBox ::" + rawInfo);
		 * writer.println("Ext ::"+raweLinks); writer.close(); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch
		 * (UnsupportedEncodingException e) { e.printStackTrace(); }
		 */

		// System.out.println("Body ::"+rawBody);
	}

}
