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
package org.apache.ode.tools.bpelc.cline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.tools.ClineCommandContext;
import org.apache.ode.tools.ExecutionException;
import org.apache.ode.tools.bpelc.BpelCompileCommand;
import org.apache.ode.utils.cli.BaseCommandlineTool;
import org.apache.ode.utils.cli.CommandlineFragment;
import org.apache.ode.utils.cli.ConsoleFormatter;
import org.apache.ode.utils.cli.FlagWithArgument;
import org.apache.ode.utils.cli.Fragments;
import org.apache.ode.utils.cli.MultiArgument;

import java.io.File;

public class BpelC extends BaseCommandlineTool {
    private static final Log __log = LogFactory.getLog(BpelC.class);

    private static FlagWithArgument OUTPUT_DIR = new FlagWithArgument("od","directory",
            "output directory",true);

    private static FlagWithArgument ROOT_WSDL = new FlagWithArgument("wsdl","uri",
            "URI of the WSDL for the process (used with BPEL4WS 1.1 " +
                    "processes, for WS-BPEL 2.0 processes, use <import>).",true);

    private static MultiArgument BPEL_URLS = new MultiArgument("bpelurl",
            "the URLs of BPEL processes to compile.",false);

    private static Fragments DEFAULT = new Fragments(new CommandlineFragment[] {
            LOGGING, ROOT_WSDL, OUTPUT_DIR, BPEL_URLS
    });

    private static final String SYNOPSIS = "compile one or more BPEL processes";

    protected static String getProgramName() {
        return "bpelc";
    }

    public static void main(String[] args) {
        setClazz(BpelC.class);
        if (args.length ==0 || HELP.matches(args)) {
            ConsoleFormatter.printSynopsis(
                    getProgramName(),SYNOPSIS,
                    new Fragments[] {DEFAULT, HELP});
            System.exit(0);
        }
        if (DEFAULT.matches(args)) {
            // Do nothing; we just want the format enforced.
        } else {
            consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
            System.exit(-1);
        }

        // We don't want the normal logging crap; just the coiler messages.
        initLogging();

        BpelCompileCommand bcc = new BpelCompileCommand();

        if (OUTPUT_DIR.isSet()) {
            String outputDir = OUTPUT_DIR.getValue();
            File od = new File(outputDir);
            if (!od.exists() || !od.isDirectory()) {
                consoleErr(outputDir + ": no such directory or not writable.");
                System.exit(-1);
            }
            bcc.setOuputDirectory(od);
        }

        if (ROOT_WSDL.isSet()) {
            bcc.setWsdlImportUri(ROOT_WSDL.getValue());
        }

        String[] b = BPEL_URLS.getValues();
        if (b == null || b.length == 0) {
            consoleErr("At least one process must be specified.");
            System.exit(-1);
        }
        for (String aB : b) bcc.addBpelProcessUrl(aB);
        try {
            bcc.execute(new ClineCommandContext(__log));
        } catch (ExecutionException ee) {
            consoleErr(ee.getMessage());
            System.exit(-1);
        } catch (Throwable t) {
            // This is really quite unexpected, so we should
            // print the stack trace to stderr.
            consoleErr(t.getMessage());
            t.printStackTrace();
            System.exit(-2);
        }
        System.exit(0);
    }
}
