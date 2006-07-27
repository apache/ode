/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.rr;

import com.fs.utils.StreamUtils;
import com.fs.utils.XMLParserUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import org.xml.sax.*;

/**
 * Builder for creating on-disk representations of {@link URLResourceRepository} objects.
 */
public class ResourceRepositoryBuilder {

  /** Destination <em>directory</em>. */
  private File _dest;

  /** The mapping file. */
  private File _mapFile = null;

  /** The actual uri->local resource mappings. */
  private Properties _map = new Properties();


  public ResourceRepositoryBuilder(File dest) throws FileNotFoundException, IOException {
    _dest = dest;
    _mapFile = new File(_dest, URLResourceRepository.RR_PROPERTIES);
    if (!_dest.isDirectory())
      throw new FileNotFoundException(_dest.toString());
    loadMapping();
    saveMapping();
  }

  public boolean containsResource(URI uri) {
    return _map.containsKey(uri.toASCIIString());
  }

  public void addURI(URI uri, InputStream is) throws IOException {
    addURI(uri, createLocalName(uri), is);
  }

  public void addURI(URI uri, URL url) throws IOException {
    InputStream is = url.openStream();
    try {
      addURI(uri, createLocalName(uri), is);
    } finally {
      is.close();
    }
  }


  public void addURI(URI uri, String localName, InputStream is) throws IOException{
    File f = new File(_dest, localName);
    if (!f.getParentFile().exists() && !f.getParentFile().mkdirs())
      throw new IOException("unable to create directories for " + f);

    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
    try {
      StreamUtils.copy(bos,is);
    } finally {
      bos.close();
    }
    _map.setProperty(uri.toASCIIString(), localName);
    saveMapping();
  }

  public void addURI(URI uri, InputSource source, EntityResolver resolver) throws IOException, SAXException {
    String localName = createLocalName(uri);
    File f = new File(_dest, localName);
    if (!f.getParentFile().exists() && !f.getParentFile().mkdirs())
      throw new IOException("unable to create directories for " + f);

    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
    ContentHandler handler = XMLParserUtils.getXercesSerializer(bos);
    XMLReader reader = XMLParserUtils.getXMLReader();
    reader.setEntityResolver(resolver);
    reader.setContentHandler(handler);
    try {
      reader.parse(source);
    } finally {
      bos.close();
    }

    _map.setProperty(uri.toASCIIString(), localName);
    saveMapping();
  }

  public ResourceRepository toResourceRepository() throws ResourceRepositoryException {
    return new URLResourceRepository(_mapFile.toURI());
  }

  private void loadMapping() throws IOException {
    if (_mapFile.exists()) {
      InputStream is = new BufferedInputStream(new FileInputStream(_mapFile));
      try {
        _map.load(is);
      } finally {
        is.close();
      }
    }
  }

  private void saveMapping() throws IOException {
    OutputStream os = new BufferedOutputStream(new FileOutputStream(_mapFile));
    try {
      _map.store(os, "PXE Resource Repository");
    } finally {
      os.close();
    }
  }

  private String createLocalName(URI uri) {
    String prefix = uri.getPath();
    if (prefix == null) {
      prefix = "resource";
    }
    else {
      StringTokenizer stok = new StringTokenizer(prefix, ":/",false);
      while (stok.hasMoreTokens()) {
        prefix = stok.nextToken();
      }
      prefix = prefix.trim();
      if (prefix.length() == 0) {
        prefix = "resource";
      }
    }

    int i = 0;
    String path = prefix+i;

    // Lets just try to use the path component of the URI,
    // possibly with an index appended.
    while(_map.containsValue(path) && i < Integer.MAX_VALUE) {
      path = prefix + ++i;
    }

    // If we ran out of integers (hm.... thats a lot of resources
    // but better safe than sorry), try a system generated unique
    // name.
    if (_map.containsValue(path)) {
      throw new RuntimeException("Too many resources.");
    }

    return path;
  }

  public void removeURI(URI uri) throws IOException {
    _map.remove(uri.toASCIIString());
    saveMapping();
  }

  public void addAlias(URI from, URI to) throws IOException {
    String localName = _map.getProperty(to.toASCIIString());
    if (localName == null) {
      throw new FileNotFoundException(to.toASCIIString());
    }
    _map.setProperty(from.toASCIIString(),localName);
    saveMapping();
  }

}
