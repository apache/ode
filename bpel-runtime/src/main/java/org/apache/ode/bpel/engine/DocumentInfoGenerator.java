package org.apache.ode.bpel.engine;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;



/**
 * Class for generating information about a document resource.  
 */
class DocumentInfoGenerator {
  private final File _baseDir;
  private final File _file;
  private String _type;
  
  private static final Map<String, String> __extToTypeMap = new HashMap<String,String>();
  static {
    // Assume WSDL is 1.1 for now...
    __extToTypeMap.put(".wsdl", "http://schemas.xmlsoap.org/wsdl/");

    __extToTypeMap.put(".xsd",  "http://www.w3.org/2001/XMLSchema");
    __extToTypeMap.put(".svg",  "http://www.w3.org/2000/svg");
    __extToTypeMap.put(".cbp",  "http://www.fivesight.com/schemas/2005/12/19/CompiledBPEL");
    // Assume BPEL is 2.0 for now...
    __extToTypeMap.put(".bpel", "http://schemas.xmlsoap.org/ws/2004/03/business-process/");
  }
  
  
  DocumentInfoGenerator(File baseDir, File f) {
    _baseDir = baseDir;
    _file = f;
    
    recognize();
  }


  public boolean isRecognized() {
    return _type != null;
  }

  public boolean isVisible() {
    return !_file.isHidden();
  }

  public String getName() {
    return _file.getName();
  }

  public String getURL() {
    try {
      return _file.toURL().toExternalForm();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public String getType() {
    return _type;
  }

  private void recognize() {
    String fname = _file.getName().toLowerCase();
    
    for (Map.Entry<String,String>i:__extToTypeMap.entrySet()) {
      if (fname.endsWith(i.getKey().toLowerCase())) {
        _type = i.getValue();
        break;
      }
    }
  }
}
