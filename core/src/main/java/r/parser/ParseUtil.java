/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.parser;

import com.google.common.base.Function;
import r.lang.Logical;
import r.lang.RealExp;

import java.text.NumberFormat;

public class ParseUtil {
  public static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();
  public static final NumberFormat REAL_FORMAT = createRealFormat();


  public static void appendEscaped(StringBuilder buf, String s) {
    for(int i=0;i!=s.length(); ++i) {

      int codePoint = s.codePointAt(i);
      if(codePoint == '\n') {
        buf.append("\\n");
      } else if(codePoint == '\r') {
        buf.append("\\r");
      } else if(codePoint == '\t') {
        buf.append("\\t");
      } else if(codePoint == 7) {
        buf.append("\\a");
      } else if(codePoint == '\b') {
        buf.append("\\b");
      } else if(codePoint == '\f') {
        buf.append("\\f");
      } else if(codePoint == 11) {
        buf.append("\\v");
      } else if(codePoint == '\"') {
        buf.append("\\\"");
      } else if(codePoint == '\\') {
        buf.append("\\\\");
      } else if(codePoint < 32 || codePoint > 126) {
        buf.append("\\u");
        buf.append(Integer.toHexString(codePoint));
      } else
        buf.appendCodePoint(codePoint);
    }
  }


  public static NumberFormat createRealFormat() {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(15);
    return format;
  }

  public static String toString(int value) {
    return INTEGER_FORMAT.format(value);
  }

  public static String toString(double value) {
    return REAL_FORMAT.format(value);
  }

  public static class DoubleResult {
    public final double value;
    public final int length;

    public DoubleResult(double value, int length) {
      this.value = value;
      this.length = length;
    }
  }

  /**
   * Parses a String to a double using the decimal point '.'
   *
   * @param text
   * @return
   */
  public static double parseDouble(String text) {
    return parseDouble(text, '.', false).value;
  }

  /**
   * Parses a string to a double.
   *
   * @param s the string to parse
   * @param dec the decimal point character to use
   * @param NA true to return NA upon parser failure
   * @return
   */
  public static DoubleResult parseDouble(String s, char dec, boolean NA) {
    double ans = 0.0, p10 = 10.0, fac = 1.0;
    int n, expn = 0, sign = 1, ndigits = 0, exph = -1;
    int p = 0;

    /* optional whitespace */
    while (Character.isWhitespace(s.charAt(p))) p++;

    if (NA && s.substring(p, p+2).equals("NA")) {
      ans = RealExp.NA;
      p += 2;
      return new DoubleResult(ans, p);
    }

    /* optional sign */
    switch (s.charAt(p)) {
      case '-': sign = -1;
      case '+': p++;
      default:
    }

    if ( nextWordIgnoringCaseIs(s, p, "NaN")) {
      ans = Double.NaN;
      p += 3;
      return new DoubleResult(sign * ans, p);

    } else if ( nextWordIgnoringCaseIs(s, p, "Inf") ) {
      ans = Double.POSITIVE_INFINITY;
      p += 3;
      return new DoubleResult(ans, p);

      /* C99 specifies this */
    } else if ( nextWordIgnoringCaseIs(s, p, "infinity") ) {
      ans = Double.POSITIVE_INFINITY;
      p += 8;
      return new DoubleResult(sign * ans, p);
    }

    if(s.substring(p).length() > 2 && s.charAt(p) == '0' && (s.charAt(p+1) == 'x' || s.charAt(p+2) == 'X')) {
      /* This will overflow to Inf if appropriate */
      for(p += 2; p<s.length(); p++) {
        if('0' <= s.charAt(p) && s.charAt(p) <= '9') ans = 16*ans + (s.charAt(p) -'0');
        else if('a' <= s.charAt(p) && s.charAt(p) <= 'f') ans = 16*ans + (s.charAt(p) -'a' + 10);
        else if('A' <= s.charAt(p) && s.charAt(p) <= 'F') ans = 16*ans + (s.charAt(p) -'A' + 10);
        else if(s.charAt(p) == dec) {exph = 0; continue;}
        else break;
        if (exph >= 0) exph += 4;
      }
      if ( p < s.length() && (s.charAt(p) == 'p' || s.charAt(p) == 'P')) {
        int expsign = 1;
        double p2 = 2.0;
        switch(s.charAt(++p)) {
          case '-': expsign = -1;
          case '+': p++;
          default:
        }
        for (n = 0; s.charAt(p) >= '0' && s.charAt(p) <= '9'; p++) n = n * 10 + (s.charAt(p) - '0');
        expn += expsign * n;
        if(exph > 0) expn -= exph;
        if (expn < 0) {
          for (n = -expn, fac = 1.0; n!=0; n >>= 1, p2 *= p2)
            if ((n & 1)!=0) fac *= p2;
          ans /= fac;
        } else {                        
          for (n = expn, fac = 1.0; n!=0; n >>= 1, p2 *= p2)
            if ((n & 1)!=0) fac *= p2;
          ans *= fac;
        }
      }
      return new DoubleResult(sign * ans, p);
    }

    for ( ; p < s.length() && s.charAt(p) >= '0' && s.charAt(p) <= '9'; p++, ndigits++)
      ans = 10*ans + (s.charAt(p) - '0');
    if ( p < s.length() && s.charAt(p) == dec)
      for (p++; s.charAt(p) >= '0' && s.charAt(p) <= '9'; p++, ndigits++, expn--)
        ans = 10*ans + (s.charAt(p) - '0');
    if (ndigits == 0) {
      ans = RealExp.NA;
      p = 0; /* back out */
      return new DoubleResult(sign * ans, p);
    }


    if ( p < s.length() && (s.charAt(p) == 'e' || s.charAt(p) == 'E')) {
      int expsign = 1;
      switch(s.charAt(++p)) {
        case '-': expsign = -1;
        case '+': p++;
        default: ;
      }
      for (n = 0; s.charAt(p) >= '0' && s.charAt(p) <= '9'; p++) n = n * 10 + (s.charAt(p) - '0');
      expn += expsign * n;
    }

    /* avoid unnecessary underflow for large negative exponents */
    if (expn + ndigits < -300) {
      for (n = 0; n < ndigits; n++) ans /= 10.0;
      expn += ndigits;
    }
    if (expn < -307) { /* use underflow, not overflow */
      for (n = -expn, fac = 1.0; n!=0; n >>= 1, p10 *= p10)
        if ((n & 1) != 0) fac /= p10;
      ans *= fac;
    } else if (expn < 0) { /* positive powers are exact */
      for (n = -expn, fac = 1.0; n!=0; n >>= 1, p10 *= p10)
        if ((n & 1) != 0) fac *= p10;
      ans /= fac;
    } else {
      for (n = expn, fac = 1.0; n!=0; n >>= 1, p10 *= p10)
        if ((n & 1) != 0) fac *= p10;
      ans *= fac;
    }
    return new DoubleResult(sign * ans, p);
  }

  private static boolean nextWordIgnoringCaseIs(String s, int i, String word) {
    return s.substring(Math.min(i+word.length(), s.length()))
        .equalsIgnoreCase(word);
  }



  public static class RealPrinter implements Function<Double, String> {
    @Override
    public String apply(Double aDouble) {
      return ParseUtil.toString(aDouble);
    }
  }

  public static class RealDeparser extends RealPrinter {

  }

  public static class IntPrinter implements Function<Integer, String> {
    @Override
    public String apply(Integer integer) {
      return ParseUtil.toString(integer);
    }
  }

  public static class IntDeparser extends IntPrinter {

  }

  public static class LogicalPrinter implements Function<Logical, String> {
    @Override
    public String apply(Logical logical) {
      return logical.toString();
    }
  }

  public static class LogicalDeparser extends LogicalPrinter {

  }

  public static class StringPrinter implements Function<String, String> {
    @Override
    public String apply(String s) {
      StringBuilder sb = new StringBuilder("\"");
      appendEscaped(sb, s);
      sb.append('"');
      return sb.toString();
    }
  }

  public static class StringDeparser extends StringPrinter {

  }
}
