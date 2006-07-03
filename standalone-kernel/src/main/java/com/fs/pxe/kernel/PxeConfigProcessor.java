/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.management.*;
import javax.management.loading.MLet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Processor for PXE XML configuration files.
 */
class PxeConfigProcessor {
  private static Log __log = LogFactory.getLog(PxeConfigProcessor.class);

	static final String PXE_HOME_PROP = "pxe.home";

	private MBeanServer _mbeanServer;
  private MLet _mletLoader;

  private final String NSURI = "http://www.fivesight.com/pxe/kconfig";
  
  private final List<String> _imports = new ArrayList<String>();
  private final HashMap<String,Evaluator> _evaluators = new HashMap<String,Evaluator>();
  private Evaluator _textEval = new EvalText();

  private final Collection<ObjectName> _modules = new LinkedList<ObjectName>();
  
  PxeConfigProcessor(MBeanServer mbeanServer, MLet mletLoader) {
  	
    if(System.getProperty(PXE_HOME_PROP) == null) {
      // Not good; this has to get set somehow.
    }

    _mbeanServer = mbeanServer;
    _mletLoader = mletLoader;

    _evaluators.put("classpath", new EvalClasspath());
    _evaluators.put("import", new EvalImport());
    _evaluators.put("kmodule", new EvalKModule());
    _evaluators.put("kconfig", new EvalKConfig());
    _evaluators.put("attribute", new EvalAttribute());
    _evaluators.put("pxe-home", new EvalPxeHome());
    _evaluators.put("getProperty", new EvalGetProperty());
    _evaluators.put("setProperty", new EvalSetProperty());
    _evaluators.put("getAttribute", new EvalGetAttribute());
    _evaluators.put("mlet", new EvalMLet());
  }

  Object evaluate(Object context,Node node) throws PxeConfigException {
    Evaluator evaluator = getEvaluator(node);
    return evaluator.eval(context, node);
  }

  private Evaluator getEvaluator(Node node) throws PxeConfigException {
    switch (node.getNodeType()) {
      case Node.TEXT_NODE:
        return _textEval;
      case Node.ELEMENT_NODE:
        if (node.getNamespaceURI() == null ||
            !NSURI.equals(node.getNamespaceURI())) {
          // Wrong namespace.
          // throw new PxeConfigException();
        }
        return _evaluators.get(((Element)node).getLocalName());
      default:
        return getEvaluator(node.getNextSibling());
    }
  }

  private Class resolveClass(String clsName) throws PxeConfigException {
    try {
      return _mletLoader.loadClass(clsName);
    } catch (Exception ex) {
      //ignore
    }

    for (Iterator i = _imports.iterator();i.hasNext(); ) {
      String prefix = (String) i.next();
      try {
        return _mletLoader.loadClass(prefix + "." + clsName);
      } catch (Exception ex) {
        //ignore
      }
    }

    throw new PxeConfigException("Unable to find class: " + clsName);
  }

  private MBeanAttributeInfo getMBeanAttributeInfo(ObjectName oname, String attrName)
          throws PxeConfigException {
    MBeanInfo info = null;
    try {
      info = _mbeanServer.getMBeanInfo(oname);
    } catch (Exception e) {
      throw new PxeConfigException("JMX Error");
    }

    MBeanAttributeInfo[] attrs = info.getAttributes();
    for (int i = 0 ; i < attrs.length; ++i) {
      if (attrs[i].getName().equals(attrName))
        return attrs[i];
    }
    throw new PxeConfigException(oname + " does not have attribute " + attrName);
  }


  /**
   * Create a new object using its String constructor.
   * @param type class name of the type (e.g. java.lang.Integer)
   * @param value string representation of the value (e.g. "1")
   * @return
   */
  private Object newObject(String type, String value) throws PxeConfigException {
    if("boolean".equals(type))
      type = Boolean.class.getName();
    else if("int".equals(type))
      type = Integer.class.getName();
    else if("short".equals(type))
      type = Short.class.getName();
    else if("long".equals(type))
      type = Long.class.getName();
    else if("float".equals(type))
      type = Float.class.getName();
    else if("double".equals(type))
      type = Double.class.getName();
    Class cls = null;
    try {
      cls = _mletLoader.loadClass(type);
    } catch (ClassNotFoundException e) {
      throw new PxeConfigException("Unknown class: " + type);
    }

    Constructor ctor = null;
    try {
      ctor = cls.getConstructor(new Class[] { String.class} );
    } catch (NoSuchMethodException e) {
      throw new PxeConfigException("Type does not have String constructor: " + type);
    }

    Object val = null;
    try {
      val = ctor.newInstance(new Object[] { value} );
    } catch (Exception e) {
      throw new PxeConfigException("Error instantiating value: " + value);
    }

    return val;
  }


  private interface Evaluator {
    Object eval(Object context, Node node) throws PxeConfigException;
  }
  
  private class EvalClasspath implements Evaluator {

		public Object eval(Object context, Node node) throws PxeConfigException {
			Element e = (Element)node;
			String type = e.getAttribute("type");
			String value = evaluateChildrenAsText(node);
			if("jarDir".equals(type)){
				File f = new File(value);
				if(!f.exists())
					throw new PxeConfigException("Classpath property " + value + " is not a valid file.");
				if(!f.exists())
					throw new PxeConfigException("Classpath property " + value + " is not a valid directory.");
				URL[] jars;
				try {
					jars = getJarsFromDirectory(f);
				} catch (MalformedURLException e1) {
					throw new PxeConfigException("Bad url: " + e1.getMessage());
				}
				for(int i = 0; i < jars.length; ++i) {
          __log.debug("Adding to MLet classloader url=" + jars[i]);
					_mletLoader.addURL(jars[i]);
        }
                                        
			} else if ("url".equals(type)) {
					URL u;
          try {
            u = new URL(value);
            if(u.getProtocol().equals("file")) {
              File tempFile = new File(u.getFile());
              u = tempFile.toURI().toURL();
            }
          } catch (MalformedURLException mue) {
            throw new PxeConfigException("The URL " +value + " is malformed: " + mue);
          }
          __log.debug("Adding to MLet classloader url=" + u);
					_mletLoader.addURL(u);				
			} else {
        throw new PxeConfigException("Unrecognized classpath element type " + type + ".");
      }
			return null;
		}
  	
  }

  private class EvalImport implements Evaluator {

    public Object eval(Object context, Node node) {
      Element imprt = (Element) node;
      _imports.add(imprt.getAttribute("package"));
      return null;
    }
  }

  private class EvalText implements Evaluator {
    public Object eval(Object context, Node node) {
      return node.getNodeValue();
    }
  }

  private class EvalKModule implements Evaluator {

    public Object eval(Object context, Node node) throws PxeConfigException {
      Element kmodule = (Element) node;
      ObjectName oname;
      try {
        oname = new ObjectName(kmodule.getAttribute("name"));
      } catch (Exception ex) {
        throw new PxeConfigException("Invalid objectname: " + kmodule.getAttribute("name"));
      }

      String clazz = kmodule.getAttribute("class");
      Class cls = resolveClass(clazz);

      PxeKernelModMBean mod;
      try {
        mod = (PxeKernelModMBean) cls.newInstance();
      } catch (Exception e) {
        e.printStackTrace();
        throw new PxeConfigException("Error creating mod: " + oname);
      }

      try {
        ObjectInstance oinstance = _mbeanServer.registerMBean(mod, oname);
        if(null == oname)
          oname = oinstance.getObjectName();
      } catch (Exception e) {
        throw new PxeConfigException("Error registering mod: " + oname);
      }

      NodeList nodeList = kmodule.getChildNodes();
      for (int i = 0; i < nodeList.getLength(); ++i)
        evaluate(oname, nodeList.item(i));
      
      try {
				mod.start();
			} catch (PxeKernelModException e1) {
				throw new PxeConfigException(e1);
			} catch (Throwable t) {
        t.printStackTrace();
        throw new PxeConfigException("Error starting kernel mod: " + oname);
      }

      _modules.add(oname);

      return oname;
    }
  }


  private class EvalPxeHome implements Evaluator {

    public Object eval(Object context,Node node) throws PxeConfigException {
      String pxeHome = System.getProperty(PXE_HOME_PROP);
      if(pxeHome == null)
        throw new PxeConfigException("System property '" + PXE_HOME_PROP + "' is not set.");
      return pxeHome;
    }
  }
  
  private String evaluateChildrenAsText(Node node) throws PxeConfigException {
  	NodeList children = node.getChildNodes();

    StringBuffer value = new StringBuffer();
    for (int i = 0; i < children.getLength(); ++i) {
      value.append(evaluate(null, children.item(i)));
    }
    return value.toString();
  }
  
  private class EvalAttribute implements Evaluator {

    public Object eval(Object context,Node node) throws PxeConfigException {
      ObjectName oname = (ObjectName) context;
      Element attribute = (Element) node;
      String attrName = attribute.getAttribute("name");
      
      // attempt to repair attribute name if it needs it
      // attribute must conform to JavaBean, e.g. start with uppercase
      char c = attrName.charAt(0);
      if(Character.isLowerCase(c))
        attrName = Character.toUpperCase(c) + attrName.substring(1);

      MBeanAttributeInfo attrInfo = getMBeanAttributeInfo(oname, attrName);
      Object val = newObject(attrInfo.getType(), evaluateChildrenAsText(node).trim());
      Attribute attr = new Attribute(attrName, val);
      try {
        _mbeanServer.setAttribute(oname, attr);
      } catch (Exception e) {
        throw new PxeConfigException("Error setting " + attr + " on " + oname);
      }

      return null;
    }

  }
  private class EvalKConfig implements Evaluator {

    public Object eval(Object context,Node node) throws PxeConfigException{
      Element kconfig = (Element) node;
      NodeList children = kconfig.getChildNodes();
      for (int i = 0 ; i  < children.getLength(); ++i) {
        evaluate(null, children.item(i));
      }

      return null;
    }
  }
  private class EvalGetProperty implements Evaluator {

    public Object eval(Object context,Node node) {
      Element el = (Element) node;
      String propertyValue = System.getProperty(el.getAttribute("name"));
      if(null == propertyValue)
        propertyValue = el.getAttribute("default");
      //
      // TODO: is null okay here if no sys prop and no default?
      //
      return propertyValue;
    }
  }


  private class EvalSetProperty implements Evaluator {

    public Object eval(Object context,Node node) throws PxeConfigException {
    	 Element el = (Element) node;
    	 String prop = el.getAttribute("name");
    	 String value = evaluateChildrenAsText(node);
       System.setProperty(prop, value);
       return null;
    }
  }

  private class EvalGetAttribute implements Evaluator {

    public Object eval(Object context,Node node) throws PxeConfigException {
      Element attribute = (Element) node;
      String onameStr = attribute.getAttribute("object");
      ObjectName oname;
			try {
				oname = new ObjectName(onameStr);
			} catch (MalformedObjectNameException e1) {
				throw new PxeConfigException("Bad ObjectName '" + onameStr + "'");
			} 
			String attrName = attribute.getAttribute("name");
      
      // attempt to repair attribute name if it needs it
      // attribute must conform to JavaBean, e.g. start with uppercase
      char c = attrName.charAt(0);
      if(Character.isLowerCase(c))
        attrName = Character.toUpperCase(c) + attrName.substring(1);

      try {
        return _mbeanServer.getAttribute(oname, attrName);
      } catch (Exception e) {
        throw new PxeConfigException("Error setting " + attrName + " on " + oname);
      }

    }
  }

  private class EvalMLet implements Evaluator {

  	public Object eval(Object context,Node node) {
      return null;
    }
  }
  
  private URL[] getJarsFromDirectory(File f) throws MalformedURLException {
  	List<URL> urls = new ArrayList<URL>();
  	if (f.isDirectory()) {
      File files[] = f.listFiles();
      // we need to alphabatize the results so we have
      // some control over classloading order
      Set<File> orderedSet = new TreeSet<File>();
      for (int i = 0; i < files.length; ++i) {
      	String name = files[i].getName().toLowerCase();
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
          orderedSet.add(files[i]);
        }            
      }

      for(Iterator iter = orderedSet.iterator(); iter.hasNext(); ) {
        File file = (File)iter.next();
        urls.add(file.toURI().toURL()); 
      }
  	}

  	return urls.toArray(new URL[urls.size()]);
  }
  
  public Collection<ObjectName> getModules() {
    return Collections.unmodifiableCollection(_modules);
  }
}
