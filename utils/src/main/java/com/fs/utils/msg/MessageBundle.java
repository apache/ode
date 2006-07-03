package com.fs.utils.msg;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class MessageBundle extends ResourceBundle {

  public static <T extends MessageBundle> T getMessages(Class<T> bundleInterface) {
    return getMessages(bundleInterface, Locale.getDefault());
  }

  @SuppressWarnings("unchecked")
  public static <T extends MessageBundle> T getMessages(Class<T> bundleInterface, Locale locale) {
    // TODO maybe catch MissingResourceException here
    return (T)ResourceBundle.getBundle(bundleInterface.getName(), locale, bundleInterface.getClassLoader());
  }

  public MessageBundle() {
    super();
  }

  /**
   * Generate a "todo" message.
   * @return todo message string
   */
  protected String todo() {
    Exception ex = new Exception();
    ex.fillInStackTrace();
    return ex.getStackTrace()[1].getMethodName() + ": NO MESSAGE (TODO)";
  }
  
  protected Object handleGetObject(String key) {
    throw new UnsupportedOperationException();
  }

  public Enumeration<String> getKeys() {
    throw new UnsupportedOperationException();
  }

  protected String format(String message) {
    return message;
  }

  protected String format(String message, Object... args) {
    if (args == null || args.length == 0) {
      return message;
    }
    else {
      return new MessageFormat(message).format(args);
    }
  }

}
