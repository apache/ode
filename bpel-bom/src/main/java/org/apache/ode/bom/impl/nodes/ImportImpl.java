/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Import;

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
	 * @see org.apache.ode.bom.api.Import#getNamespace()
	 */
	public String getNamespace() {
		return _namespace;
	}

  /**
	 * @see org.apache.ode.bom.api.Import#setNamespace(java.lang.String)
	 */
	public void setNamespace(String namespace) {
		_namespace = namespace;
	}

  /**
	 * @see org.apache.ode.bom.api.Import#getLocation()
	 */
	public URI getLocation() {
		return _location;
	}

	/**
	 * @see org.apache.ode.bom.api.Import#setLocation(java.net.URI)
	 */
	public void setLocation(URI location) {
		_location = location;
	}

  /**
	 * @see org.apache.ode.bom.api.Import#getImportType()
	 */
	public String getImportType() {
		return _importType;
	}

  /**
	 * @see org.apache.ode.bom.api.Import#setImportType(java.lang.String)
	 */
	public void setImportType(String importType) {
		_importType = importType;
	}

}
