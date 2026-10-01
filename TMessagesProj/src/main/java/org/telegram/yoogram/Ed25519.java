package org.telegram.yoogram;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

/** Minimal RFC 8032 Ed25519 signature verification (no external dependencies). */
public final class Ed25519 {

    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger P = BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19));
    private static final BigInteger L = BigInteger.ONE.shiftLeft(252).add(new BigInteger("27742317777372353535851937790883648493"));
    private static final BigInteger D = BigInteger.valueOf(-121665).multiply(BigInteger.valueOf(121666).modInverse(P)).mod(P);
    private static final BigInteger SQRT_M1 = TWO.modPow(P.subtract(BigInteger.ONE).shiftRight(2), P);
    private static final BigInteger[] B;

    static {
        BigInteger by = BigInteger.valueOf(4).multiply(BigInteger.valueOf(5).modInverse(P)).mod(P);
        BigInteger bx = recoverX(by, 0);
        B = new BigInteger[]{bx, by, BigInteger.ONE, bx.multiply(by).mod(P)};
    }

    private Ed25519() {
    }

    public static boolean verify(byte[] publicKey, byte[] message, byte[] signature) {
        try {
            if (publicKey == null || publicKey.length != 32 || signature == null || signature.length != 64) {
                return false;
            }
            BigInteger[] a = decodePoint(publicKey);
            BigInteger[] r = decodePoint(Arrays.copyOfRange(signature, 0, 32));
            BigInteger s = decodeLE(Arrays.copyOfRange(signature, 32, 64));
            if (a == null || r == null || s.compareTo(L) >= 0) {
                return false;
            }
            MessageDigest sha = MessageDigest.getInstance("SHA-512");
            sha.update(signature, 0, 32);
            sha.update(publicKey);
            sha.update(message);
            BigInteger h = decodeLE(sha.digest()).mod(L);
            BigInteger[] left = mul(B, s);
            BigInteger[] right = add(r, mul(a, h));
            return equal(left, right);
        } catch (Exception e) {
            return false;
        }
    }

    private static BigInteger decodeLE(byte[] b) {
        byte[] be = new byte[b.length + 1];
        for (int i = 0; i < b.length; i++) {
            be[b.length - i] = b[i];
        }
        return new BigInteger(be);
    }

    private static BigInteger recoverX(BigInteger y, int sign) {
        BigInteger y2 = y.multiply(y).mod(P);
        BigInteger xx = y2.subtract(BigInteger.ONE).multiply(D.multiply(y2).add(BigInteger.ONE).modInverse(P)).mod(P);
        if (xx.signum() == 0) {
            return sign == 0 ? BigInteger.ZERO : null;
        }
        BigInteger x = xx.modPow(P.add(BigInteger.valueOf(3)).shiftRight(3), P);
        if (x.multiply(x).subtract(xx).mod(P).signum() != 0) {
            x = x.multiply(SQRT_M1).mod(P);
        }
        if (x.multiply(x).subtract(xx).mod(P).signum() != 0) {
            return null;
        }
        if (x.testBit(0) != (sign == 1)) {
            x = P.subtract(x);
        }
        return x;
    }

    private static BigInteger[] decodePoint(byte[] enc) {
        byte[] copy = enc.clone();
        int sign = (copy[31] >> 7) & 1;
        copy[31] &= 0x7f;
        BigInteger y = decodeLE(copy);
        if (y.compareTo(P) >= 0) {
            return null;
        }
        BigInteger x = recoverX(y, sign);
        if (x == null) {
            return null;
        }
        return new BigInteger[]{x, y, BigInteger.ONE, x.multiply(y).mod(P)};
    }

    private static BigInteger[] add(BigInteger[] p, BigInteger[] q) {
        BigInteger a = p[1].subtract(p[0]).multiply(q[1].subtract(q[0])).mod(P);
        BigInteger b = p[1].add(p[0]).multiply(q[1].add(q[0])).mod(P);
        BigInteger c = TWO.multiply(p[3]).multiply(q[3]).multiply(D).mod(P);
        BigInteger d = TWO.multiply(p[2]).multiply(q[2]).mod(P);
        BigInteger e = b.subtract(a), f = d.subtract(c), g = d.add(c), h = b.add(a);
        return new BigInteger[]{e.multiply(f).mod(P), g.multiply(h).mod(P), f.multiply(g).mod(P), e.multiply(h).mod(P)};
    }

    private static BigInteger[] mul(BigInteger[] p, BigInteger s) {
        BigInteger[] q = {BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE, BigInteger.ZERO};
        for (int i = s.bitLength() - 1; i >= 0; i--) {
            q = add(q, q);
            if (s.testBit(i)) {
                q = add(q, p);
            }
        }
        return q;
    }

    private static boolean equal(BigInteger[] p, BigInteger[] q) {
        return p[0].multiply(q[2]).subtract(q[0].multiply(p[2])).mod(P).signum() == 0
            && p[1].multiply(q[2]).subtract(q[1].multiply(p[2])).mod(P).signum() == 0;
    }
}
