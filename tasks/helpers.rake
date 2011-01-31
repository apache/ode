# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with this
# work for additional information regarding copyright ownership. The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

def osgi_version_for(version)
  parts = version.split(/[\.,-]/)
  result = Array.new(3, 0)
  parts.each_index { |i|
    if (result.size == 3) then
      if (parts[i] =~ /\d+/) then result[i] = parts[i] else result = result << parts[i] end
    else
      result[3] = [result[3], parts[i]].join("-")
    end
  }
  result.join(".")
end
