/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.sar.cline;

import com.fs.pxe.sfwk.deployment.ExpandedSAR;
import com.fs.pxe.sfwk.deployment.SarFormatException;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.utils.StreamUtils;
import com.fs.utils.cli.*;
import com.fs.utils.fs.TempFileManager;

import java.io.*;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

/**
 * List the contents of a SAR file.
 */
public class SarList extends BaseCommandlineTool {

	private static final Argument SARFILE_A = new Argument("sarfile",
			"the SAR file to inventory.", false);

	private static final Fragments CLINE = new Fragments(
			new CommandlineFragment[] { SARFILE_A });

	private static final String SYNOPSIS = "inventory the contents of a SAR archive.";

	public static void main(String[] args) {

		if (args.length == 0 || HELP.matches(args)) {
			ConsoleFormatter.printSynopsis(getProgramName(), SYNOPSIS,
					new Fragments[] { CLINE, HELP });
			System.exit(-1);
		} else if (!CLINE.matches(args)) {
			consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName()
					+ " -h\" for help.");
			System.exit(-1);
		}
		initLogging();
		registerTempFileManager();

		File sfile = new File(SARFILE_A.getValue());
		if (!sfile.exists()) {
			consoleErr("File not found: " + sfile);
			System.exit(-2);
		}

		if (sfile.isFile()) {
			try {
				File tmpdir = TempFileManager.getTemporaryDirectory("sarfile-");
				InputStream is = new BufferedInputStream(new FileInputStream(sfile));
				try {
					StreamUtils.extractJar(tmpdir, is);
				} finally {
					is.close();
				}
				
				sfile = tmpdir;
			} catch (IOException ioex) {
				consoleErr("Error extracting SAR file " + sfile);
				System.exit(-2);
				throw new IllegalStateException();
			}
		}

		ExpandedSAR sf;
		try {
			sf = new ExpandedSAR(sfile);
		} catch (SarFormatException sffe) {
			consoleErr(sffe.getMessage());
			System.exit(-1);
			throw new IllegalStateException();
		} catch (IOException ex) {
			consoleErr("Error reading SAR from " + sfile);
			System.exit(-1);
			throw new IllegalStateException();
		}

		SystemDescriptor sd = sf.getDescriptor();
		System.out.println("System name: " + sd.getName());
		System.out.println("Root WSDL URI: " + sd.getWsdlUri().toString());
		System.out.println();
		Service[] ss = sd.getServices();
		System.out.println("Services: (" + ss.length + " service"
				+ ((ss.length == 1) ? "" : "s") + ")");
		for (int i = 0; i < ss.length; ++i) {
			System.out.println("  " + ss[i].getName() + "("
					+ ss[i].getProviderUri().toString() + ")");
		}
		System.out.println();
		Map<URI, String> cres = sf.getSystemResourceRepository()
				.getTableOfContents();
		System.out.println("System Resource Repository: (" + cres.size()
				+ " resource" + ((cres.size() == 1) ? "" : "s") + ")");

		for (Iterator<Map.Entry<URI, String>> i = cres.entrySet().iterator(); i
				.hasNext();) {
			Map.Entry<URI, String> me = i.next();
			System.out.println("     " + me.getKey() + " -->  " + me.getValue());
		}
		System.exit(0);

	}

}
