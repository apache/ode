/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.tools.bpelc;

import org.apache.ode.bpel.compiler.BpelC;
import org.apache.ode.bpel.compiler.BpelCompiler;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompileListener;
import org.apache.ode.tools.Command;
import org.apache.ode.tools.CommandContext;
import org.apache.ode.tools.ExecutionException;
import org.apache.ode.utils.SystemUtils;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.ode.utils.msg.MessageBundle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

public class BpelCompileCommand implements Command {

  private static final BpelCompileCommandMessages __msgs =
    MessageBundle.getMessages(BpelCompileCommandMessages.class);

  private short _minSeverity = CompilationMessage.ERROR;
  private CompileListener _compileListener;
  private CommandContext _cc;

  private File _outputDir;

  private String _wsdlUri;
  private HashSet<String> _bpelFiles = new HashSet<String>();

  public void setCompileListener(CompileListener cl) {
    _compileListener = cl;
  }

  public void setMinimumSeverity(short m) {
    _minSeverity = m;
  }

  public void setOuputDirectory(File f) {
    _outputDir = f;
  }

  public void setWsdlImportUri(String u) {
    _wsdlUri = u;
  }

  public void addBpelProcessUrl(String u) {
    _bpelFiles.add(u);
  }

  public void execute(CommandContext cc) throws ExecutionException {
    if (_bpelFiles.size() == 0) {
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

    URI u = null;

    if (_wsdlUri != null) {
      try {
        u = new URI(FileUtils.encodePath(_wsdlUri));
      }
      catch (URISyntaxException use) {
        throw new ExecutionException(__msgs.msgInvalidWsdlUrl(_wsdlUri));
      }
    }

    for (String bpelURI : _bpelFiles) {
      BpelC compiler = BpelC.newBpelCompiler();
      if (u != null) {
        compiler.setProcessWSDL(u);
      }
      compiler.setCompileListener(myListener);

      File bpelFile = new File(bpelURI);
      if (!bpelFile.exists()) {
        _cc.debug("File does not exist: " + bpelFile);
        throw new ExecutionException(__msgs.msgInvalidBpelUrl(bpelURI));
      }

      try {
        long start = System.currentTimeMillis();
        compiler.compile(bpelFile, BpelCompiler.getVersion(_outputDir.getAbsolutePath()));
        long t = System.currentTimeMillis() - start;
        _cc.info("Compilation completed in " + t + "ms");
      }
      catch (IOException ioe) {
        throw new ExecutionException(__msgs.msgIoExReadingStreamWithMsg(bpelFile, ioe.getMessage()));
      } catch (CompilationException e) {
        throw new ExecutionException(e.toErrorMessage(), e);
      }
    }
  }

}
