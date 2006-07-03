package com.fs.pxe.bpel.common;

import com.fs.utils.ArrayUtils;
import com.fs.utils.msg.MessageBundle;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.UnaryFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for filters.
 */
public abstract class Filter<FKEY extends Enum> implements Serializable {
  private static final long serialVersionUID = 9999;
  
  
  /** Internationalization. */
  protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /**
   *  Pattern that matches anything like 'abcde <  fgh' or 'ijklm  =nop' using 
   *  supported comparators 
   */
  private static final Pattern __comparatorPattern = 
    Pattern.compile("([^=<> ]*) *(<=|>=|<|>|=) *([^=<> ]*)");

  protected Map<FKEY, Restriction<String>> _criteria = new HashMap<FKEY,Restriction<String>>();

  public static class Restriction<V>{
    public final String originalKey;
    public final String op;
    public final V value;
    
    public Restriction(String originalKey, String op, V value) {
      this.originalKey=originalKey;
      this.op=op;
      this.value = value;
    }
  }

  public void init(String filter) {
    if (filter != null) {
      Matcher expressionMatcher = __comparatorPattern.matcher(filter);
      while (expressionMatcher.find()) {
        String filterKey = expressionMatcher.group(1);
        String op = expressionMatcher.group(2);
        String value = expressionMatcher.group(3);
        FKEY keyval;
        try {
          keyval = parseKey(filterKey.toUpperCase().replaceAll("-", "_"));
        } catch (Exception ex) {
          String errmsg = __msgs.msgUnrecognizedFilterKey(filterKey, getFilterKeysStr());
          throw new IllegalArgumentException(errmsg, ex);
        }
        
        Restriction<String> restriction = new Restriction<String>(filterKey,op,value);
        _criteria.put(keyval, restriction);
        
        process(keyval, restriction);
      }
    }

  }

  /**
   * Get the data part of an "op date" string.
   * @param ddf "op date" string
   * @return date component
   */
  public static String getDateWithoutOp(String ddf) {
    if (ddf != null) {
      if (ddf.startsWith("=")) {
        return ddf.substring(1, ddf.length());
      } else if (ddf.startsWith("<=")) {
        return ddf.substring(2, ddf.length());
      } else if (ddf.startsWith(">=")) {
        return ddf.substring(2, ddf.length());
      } else if (ddf.startsWith("<")) {
        return ddf.substring(1, ddf.length());
      } else if (ddf.startsWith(">")) {
        return ddf.substring(1, ddf.length());
      }
    }
    return null;
 
  }

  /**
   * Parse the string representation of a key into an
   * enumeration value. 
   * @param keyVal string representation
   * @return enumeration value
   */
  protected abstract FKEY parseKey(String keyVal);

  /**
   * Get the list of known (recognized) filter keys. 
   * @return recognized filter keys
   */
  protected abstract FKEY[] getFilterKeys();

  /**
   * Perform additional parsing/processing.
   * @param key filter key
   * @param rest restriction
   */
  protected abstract void process(FKEY key, Restriction<String> rest);

  private Collection<String> getFilterKeysStr() {
    return CollectionsX.transform(new ArrayList<String>(),
        ArrayUtils.makeCollection(ArrayList.class , getFilterKeys()),
        new UnaryFunction<FKEY,String>() {
          public String apply(FKEY x) {
            return x.name();
          }
        });
    
  }
}

