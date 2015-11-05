import java.util.Arrays;





public class ArrayDeduplication {
	/**
	 * Array Deduplication
	 * @param s  an array to be processed. all strings in s are in lowercase.
	 * @return   a new sorted array with no duplicates and empty strings.
	 */
	public static String[] arrayDedup(String[] s) {
		if (s == null || s.length == 0) {
			return s;
		}
		Arrays.sort(s);
		int i = -1;
		for (int j = 0; j < s.length; j++) {
			if (i == -1 || !s[j].equals(s[i])) {
				s[++i] = s[j];
			}
		}
		return Arrays.copyOfRange(s, 0, i + 1);
		
	}
	public static void main(String[] arg) {
		ArrayDeduplication solution = new ArrayDeduplication();
		String[] s = new String[]{"", " ", "1", "2", "3", "3", "", "4", "4", " ", "4", "5"};
		System.out.println(s.length);
		Arrays.sort(s);
		String[] T = null;
		T = solution.arrayDedup(s);
		System.out.println(T.length);
		for (int i = 0; i < T.length; i++) {
			System.out.println(T[i]);
		}
		
		String mm = "\u0631\u064a\u0647\u0627\u0645_\u0645\u0627\u062a\u062a";
		System.out.println(mm);
		mm = mm.replaceAll("[^\\x00-\\x7F]", "");
		System.out.println(mm);
		String aa = "aa    bb    cc    d";
		aa = aa.replaceAll("\\s+", " ");
		System.out.println(aa);
		System.out.println(T[0].length() + ""+ T[1].length());
	}
}

