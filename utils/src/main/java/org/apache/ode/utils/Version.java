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

package org.apache.ode.utils;

import org.apache.ode.utils.msg.MessageBundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class for displaying the version of the build
 */
public class Version {

  private static String MINOR_VERSION = "";
  private static String MAJOR_VERSION = "";
  private static String VERSION_NAME = "UNRELEASED";
  private static String BUILD_NUMBER = "";
  private static String BUILD_DATE = "UNKNOWN";

  static {
    Properties props = new Properties();
    InputStream is = Version.class.getResourceAsStream("/ODE.version");
    if (is != null) {
      try {
        props.load(is);
        BUILD_NUMBER = props.getProperty("build.number", "");
        MAJOR_VERSION = props.getProperty("version.major", "");
        MINOR_VERSION = props.getProperty("version.minor", "");
        VERSION_NAME = props.getProperty("version.name", "UNRELEASED");
        BUILD_DATE = props.getProperty("build.date", "UNKNOWN");
      }
      catch (IOException ioe) {
        // TODO BAD -- we should do something more meaningful
        ioe.printStackTrace();
      }
    }
  }

  /**
   * Get the full name of the version, e.g., &quot;1.0B15&quot;.
   *
   * @return the full version name or an empty String if not available.
   */
  public static String getVersionName() {
    return VERSION_NAME;
  }

  /**
   * Get the full name of the version, e.g., &quot;1.0B15&quot;.
   *
   * @return the build number.
   */
  public static String getBuildNumber() {
    return BUILD_NUMBER;
  }

  /**
   * Get the build date.
   *
   * @return the build date as a string of the form yyyyMMdd
   */
  public static String getBuildDate() {
    return BUILD_DATE;
  }

  /**
   * Get the major version number, i.e., the <code>x</code> in version
   * <code>x.y</code>.
   *
   * @return the minor version number or <code>-1</code> for a development
   *         build
   */
  public static int getMajorVersion() {
    if (MAJOR_VERSION.length() == 0) return -1;
    return Integer.parseInt(MAJOR_VERSION);
  }

  /**
   * Get the minor version number, i.e., the <code>y</code> in version
   * <code>x.y</code>.
   *
   * @return the minor version number or <code>-1</code> for a development
   *         build
   */
  public static int getMinorVersion() {
    if (MINOR_VERSION.length() == 0) return -1;
    return Integer.parseInt(MINOR_VERSION);
  }

  /**
   * Print the full version number and licensing information to the console.
   *
   * @param argv
   *          command-line arguments (ignored)
   */
  public static void main(String[] argv) {
    VersionMessages msgs = MessageBundle.getMessages(VersionMessages.class);
    System.out.println(msgs.msgVersionInfo(getVersionName(), getBuildDate()));
    System.out.println(msgs.msgGetCopyright());
  }

}
