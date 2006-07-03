/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Import;

import java.net.URI;

public class ImportImpl extends BpelObjectImpl implements Import {
	private static final long serialVersionUID = 1L;
	private String _namespace;
  private URI _location;
  private String _importType; 

	public ImportImpl() {
		super();
	}

  /**
	 * @see com.fs.pxe.bom.api.Import#getNamespace()
	 */
	public String getNamespace() {
		return _namespace;
	}

  /**
	 * @see com.fs.pxe.bom.api.Import#setNamespace(java.lang.String)
	 */
	public void setNamespace(String namespace) {
		_namespace = namespace;
	}

  /**
	 * @see com.fs.pxe.bom.api.Import#getLocation()
	 */
	public URI getLocation() {
		return _location;
	}

	/**
	 * @see com.fs.pxe.bom.api.Import#setLocation(java.net.URI)
	 */
	public void setLocation(URI location) {
		_location = location;
	}

  /**
	 * @see com.fs.pxe.bom.api.Import#getImportType()
	 */
	public String getImportType() {
		return _importType;
	}

  /**
	 * @see com.fs.pxe.bom.api.Import#setImportType(java.lang.String)
	 */
	public void setImportType(String importType) {
		_importType = importType;
	}

}
