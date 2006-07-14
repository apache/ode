package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.common.CorrelationKey;

public class CorrelationKeySerializer {

  public static String toCanonicalString(CorrelationKey ckey) {
    if (ckey == null) return null;
    StringBuffer buf = new StringBuffer();
    buf.append(ckey.getCSetId());
    buf.append('~');
    for (int i = 0; i < ckey.getValues().length; ++i) {
      if (i != 0)
        buf.append('~');
      escapeTilde(buf, ckey.getValues()[i]);
    }
    return buf.toString();
  }

  static void escapeTilde(StringBuffer buf, String str) {
    if ( str == null ) return;
    char[] chars = str.toCharArray();
    for (char achar : chars) {
      if (achar == '~') {
        buf.append("~~");
      } else {
        buf.append(achar);
      }
    }
  }

}
