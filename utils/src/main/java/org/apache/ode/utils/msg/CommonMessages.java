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

package org.apache.ode.utils.msg;

public class CommonMessages extends MessageBundle {

    public String strError() {
        return this.format("Error");
    }

    public String strFatal() {
        return this.format("Fatal");
    }

    public String strInfo() {
        return this.format("Info");
    }

    public String strWarning() {
        return this.format("Warning");
    }

    public String msgFileNotFound(String string) {
        return this.format("File not found: {0}", string);
    }

    public String msgCannotWriteToFile(String string) {
        return this.format("Unable to write to file \"{0}\";"
                + " it may be a directory or otherwise unwritable.", string);
    }

    public String msgCannotReadFromFile(String string) {
        return this.format("Unable to read from file \"{0}\";"
                + " it may be missing, a directory, or otherwise unreadable.", string);
    }

    public String msgBadPort(String string) {
        return this.format("The string you specified for proxy port \"{0}\" doesn't appear to be "
                + " correct, it must be a number.", string);
    }

}
