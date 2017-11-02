package shorturl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ShortUrlEncoder {

	private static final ShortUrlEncoder INSTANCE = new ShortUrlEncoder();
	private static final int BLOCK_SIZE = 24;
	private static final int MIN_LENGTH = 5;
	private static final String ALPHABET_STR = "mn6j2c4rv8bpygw95z7hsdaetxuk3fq";
	private static final List<Character> ALPHABET = ALPHABET_STR
			.chars()
			.mapToObj(e -> (char) e)
			.collect(Collectors.toList());

	public static ShortUrlEncoder getInstance() {
		return INSTANCE;
	}

	private long mask;
	private int[] mapping;

	private ShortUrlEncoder() {
		this.mask = (1L << BLOCK_SIZE) - 1L;
		this.mapping = IntStream.rangeClosed(0, BLOCK_SIZE - 1)
			.map(i -> BLOCK_SIZE - 1 - i) // reverse
			.toArray();
	}

	public String encodeUrl(long n) {
		return enbase(encode(n), MIN_LENGTH);
	}

	public String encodeUrl(long n, int minLength) {
		return enbase(encode(n), minLength);
	}

	public long decodeUrl(String n) {
		return decode(debase(n));
	}

	public long encode(long n) {
		return (n & ~mask) | _encode(n & mask);
	}

	private long _encode(long n) {
		long result = 0;
		for (int i = 0; i < mapping.length; i++) {
			int b = mapping[i];
			if ((n & (1L << i)) != 0L) {
				result |= (1L << b);
			}
		}
		return result;
	}

	public long decode(long n) {
		return (n & ~mask) | _decode(n & mask);
	}

	private long _decode(long n) {
		long result = 0;
		for (int i = 0; i < mapping.length; i++) {
			int b = mapping[i];
			if ((n & (1L << b)) != 0L)
				result |= (1L << i);
		}
		return result;
	}

	public String enbase(long x, int minLength) {
		String result = _enbase(x);
		String padding = fillString(minLength - String.valueOf(result).length(), ALPHABET.get(0));
		return String.format("%s%s", padding, result);
	}

	private static String fillString(int count, char c) {
		if (count <= 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	private String _enbase(long x) {
		long n = ALPHABET.size();
		if (x < n) {
			return String.valueOf(ALPHABET.get((int) x));
		}
		return _enbase(x / n) + ALPHABET.get((int) (x % n));
	}

	public long debase(String x) {
		long n = ALPHABET.size();
		long result = 0;
		long i = 0;
		char[] cs = new StringBuilder(x).reverse().toString().toCharArray();
		for (char c : cs) {
			result += ALPHABET.indexOf(c) * Math.pow(n, i);
			i++;
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(" input       encode  enbase       debase decode");
		for (long a = 0; a <= 200000; a += 37) {
			long b = INSTANCE.encode(a);
			String c = INSTANCE.enbase(b, MIN_LENGTH);
			long d = INSTANCE.debase(c);
			long e = INSTANCE.decode(d);
			assert a == e;
			assert b == d;
			System.out.printf("%6d %12d %s %12d %6d\n", a, b, fillString(7 - c.length(), ' ') + c, d, e);
		}
	}
}
