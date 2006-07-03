/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.test;

import com.fs.pxe.bpel.test.BpelTestDef.MessageDef;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class BpelTestParser {
	
	public BpelTestDef parse(File propFile) throws Exception {
 
    Properties p = new Properties();
    FileInputStream fis = new FileInputStream(propFile);
    try {
      p.load(fis);
    } finally {
      fis.close();
    }

    SortedMap<Integer, BpelTestDef.MessageDef> requests =
      new TreeMap<Integer, BpelTestDef.MessageDef>();

    SortedMap<Integer, BpelTestDef.MessageDef> invokes =
      new TreeMap<Integer, BpelTestDef.MessageDef>();

    BpelTestDef def = new BpelTestDef();
    Enumeration iter = p.propertyNames();
    while (iter.hasMoreElements()) {
			String key = (String) iter.nextElement();
			String value = p.getProperty(key);
			List<String> keys = parseKey(key);

      String setting = keys.get(0);
      if ("bpel".equalsIgnoreCase(setting)) {
				def.bpelFile = new File(propFile.getParentFile(), value);
			} else if ("wsdl".equalsIgnoreCase(setting)) {
				def.wsdlFile = new File(propFile.getParentFile(), value);
			} else if (setting.startsWith("request")) {
        Integer reqId = Integer.valueOf(keys.get(1));
				BpelTestDef.MessageDef msgDef = requests.get(reqId);
				if (msgDef == null) {
					msgDef = new BpelTestDef.MessageDef();
					requests.put(reqId, msgDef);
				}
				handleMessage(propFile, msgDef, keys.get(2), value);
			} else if (setting.startsWith("invoke")) {
        Integer invokeId = Integer.valueOf(keys.get(1));
				BpelTestDef.MessageDef msgDef = invokes.get(invokeId);
				if (msgDef == null) {
					msgDef = new BpelTestDef.MessageDef();
					invokes.put(invokeId, msgDef);
				}
				handleMessage(propFile, msgDef, keys.get(2), value);
			} else if (setting.startsWith("postConditionNamespace")) {
				int idx = value.indexOf('=');
				String prefix = value.substring(0, idx);
				String ns = value.substring(idx + 1, value.length());
				def.namespaces.put(prefix, ns);
			} else if (setting.startsWith("postCondition")) {
				def.postChecks.add(value);
			} else {
				throw new IllegalArgumentException("unknown setting: " + setting);
			}
		}
    
    // Now do some sanity checks

		if (def.bpelFile == null) {
			throw new IllegalStateException("null bpel");
		}

    if (def.wsdlFile == null) {
			String bpelName = def.bpelFile.getName();
			String name = bpelName.substring(0, bpelName.lastIndexOf('.'));
			def.wsdlFile = new File(def.bpelFile.getParent(), name + ".wsdl");
		}

		for (MessageDef mdef : invokes.values()) {
			def.invokes.add(mdef);
    }

		for (MessageDef mdef : requests.values()) {
			def.receives.add(mdef);
    }

    String[] postChecks = new String[def.postChecks.size()];
		def.postChecks.toArray(postChecks);

    return def;
	}
  
	public List<BpelTestDef> scanForTests(File location) throws Exception {
    List<BpelTestDef> tests = new ArrayList<BpelTestDef>();
    if (location.isFile() && location.getName().endsWith(".tdef")) 
      tests.add(parse(location));
    else if (location.isDirectory()) {
      for (File child : location.listFiles())
        tests.addAll(scanForTests(child));
		}
    return tests;
	}

  private void handleMessage(File propFile, BpelTestDef.MessageDef def,
			String setting, String value) {
		if ("partnerLink".equalsIgnoreCase(setting)) {
			def.partnerLink = value;
		} else if ("operation".equalsIgnoreCase(setting)) {
			def.operation = value;
		} else if ("msg".equalsIgnoreCase(setting)) {
			def.inFile = new File(propFile.getParent(), value);
		} else if ("fault".equalsIgnoreCase(setting)) {
			def.fault = value;
    } else if ("delay".equalsIgnoreCase(setting)) {
      def.delay = Integer.valueOf(value);
		} else {
			throw new IllegalArgumentException("Unknown message nodes: " + setting);
		}
	}
  
	private static List<String> parseKey(String key) {
    List<String> ret = new ArrayList<String>();
    StringTokenizer stok = new StringTokenizer(key,".",false);
    while (stok.hasMoreTokens())
      ret.add(stok.nextToken());
    return ret;
	}
}