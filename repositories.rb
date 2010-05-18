#    Licensed to the Apache Software Foundation (ASF) under one or more
#    contributor license agreements.  See the NOTICE file distributed with
#    this work for additional information regarding copyright ownership.
#    The ASF licenses this file to You under the Apache License, Version 2.0
#    (the "License"); you may not use this file except in compliance with
#    the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.

repositories.remote << "http://www.intalio.org/public/maven2"
repositories.remote << "http://people.apache.org/repo/m2-incubating-repository"
repositories.remote << "http://repo1.maven.org/maven2"
repositories.remote << "http://people.apache.org/repo/m2-snapshot-repository"
repositories.remote << "http://download.java.net/maven/2"
repositories.remote << "http://svn.apache.org/repos/asf/servicemix/m2-repo"
repositories.remote << "http://www.aqute.biz/repo"
repositories.release_to[:url] ||= "sftp://guest@localhost/home/guest"
