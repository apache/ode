/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.tools.bpelc;

import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompileListener;
import com.fs.pxe.bpel.compiler.BpelC;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.sfwk.rr.ResourceRepositoryException;
import com.fs.pxe.sfwk.rr.URLResourceRepository;
import com.fs.pxe.tools.Command;
import com.fs.pxe.tools.CommandContext;
import com.fs.pxe.tools.ExecutionException;
import com.fs.utils.SystemUtils;
import com.fs.utils.msg.MessageBundle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;

public class BpelCompileCommand implements Command {

  private static final BpelCompileCommandMessages __msgs =
    MessageBundle.getMessages(BpelCompileCommandMessages.class);

  private short _minSeverity = CompilationMessage.ERROR;
  private CompileListener _compileListener;
  private CommandContext _cc;

  private File _outputDir;

  private String _wsdlUri;
  private HashSet<String> _bpelUris = new HashSet<String>();

  private File _rrFile;

  public void setCompileListener(CompileListener cl) {
    _compileListener = cl;
  }

  public void setMinimumSeverity(short m) {
    _minSeverity = m;
  }

  public void setOuputDirectory(File f) {
    _outputDir = f;
  }

  public void setResourceRepository(File f) {
    _rrFile = f;
  }

  public void setWsdlImportUri(String u) {
    _wsdlUri = u;
  }

  public void addBpelProcessUrl(String u) {
    _bpelUris.add(u);
  }

  public void execute(CommandContext cc) throws ExecutionException {
    if (_bpelUris.size() == 0) {
      throw new ExecutionException(__msgs.msgAtLeastOneProcessRequired());
    }

    if (_outputDir == null) {
      _outputDir = new File(SystemUtils.userDirectory());
    }

    _cc = cc;
    CompileListener myListener = new CompileListener() {

      public void onCompilationMessage(CompilationMessage m) {
        if (m.severity >= _minSeverity) {
          _cc.outln(m.toErrorString());
        }
        if (_compileListener != null) {
          _compileListener.onCompilationMessage(m);
        }
      }
    };

    ResourceRepository rr = null;
    URI u = null;

    if (_rrFile != null) {
      if (!_rrFile.exists() || !_rrFile.isDirectory()) {
        throw new ExecutionException(__msgs.msgInvalidRrDirectory(_rrFile.getName()));
      }
      try {
        rr = new URLResourceRepository(_rrFile.toURI());
      }
      catch (ResourceRepositoryException rre) {
        throw new ExecutionException(__msgs.msgBpelcResourceRepositoryIoError(_rrFile
            .toString()), rre);
      }
    }

    if (_wsdlUri != null) {
      try {
        u = new URI(_wsdlUri);
      }
      catch (URISyntaxException use) {
        throw new ExecutionException(__msgs.msgInvalidWsdlUrl(_wsdlUri));
      }
    }

    if (rr != null && _wsdlUri != null && !rr.containsResource(u)) {
      throw new ExecutionException(__msgs.msgNoSuchWsdl(_wsdlUri));
    }

    for (String bpelURI : _bpelUris) {
      BpelC compiler = BpelC.newBpelCompiler();
      if (u != null) {
        compiler.setProcessWSDL(u);
      }
      compiler.setOutputDirectory(_outputDir);
      compiler.setCompileListener(myListener);

      URL bpelFile;
      try {
        bpelFile = new URL(bpelURI);
      } catch (MalformedURLException mue1) {
        _cc.debug(bpelURI + " doesn't look like a URL; trying a file instead.");
        try {
          File bf = new File(bpelURI);
          if (!bf.exists()) {
            _cc.debug("File does not exist: " + bf.getAbsolutePath());
            throw new ExecutionException(__msgs.msgInvalidBpelUrl(bpelURI));
          }
          bpelFile = bf.toURL();
        } catch (MalformedURLException mue2) {
          throw new ExecutionException(__msgs.msgInvalidBpelUrl(bpelURI));
        }
      }

      if (rr != null) {
        try {
          compiler.setWsdlFinder(new RrWsdlFinder(rr, bpelFile.toURI()));
          compiler.setXsltFinder(new RrXsltFinder(rr, bpelFile.toURI()));
        } catch (URISyntaxException e) {
          throw new ExecutionException(__msgs.msgInvalidBpelUrl(bpelURI));
        }
      }

      try {
        long start = System.currentTimeMillis();
        compiler.compile(bpelFile);
        long t = System.currentTimeMillis() - start;
        _cc.info("Compilation completed in " + t + "ms");
      }
      catch (IOException ioe) {
        throw new ExecutionException(__msgs.msgIoExReadingStreamWithMsg(bpelFile, ioe
            .getMessage()));
      }
      catch (CompilationException e) {
        throw new ExecutionException(e.toErrorMessage(), e);
      }
      finally {
        try {
          if (rr != null) rr.close();
        }
        catch (IOException ioex) {
          // ignore.
        }
      }
    }
  }

}
