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
package org.apache.ode.jacob.ap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class JacobAnnotationProcessorFactory implements AnnotationProcessorFactory {

    private static final List<String> __supported  = Arrays.asList(
            new String[] { ChannelType.class.getName() }); 
    
    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }

    public Collection<String> supportedAnnotationTypes() {
        return __supported;
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atd, AnnotationProcessorEnvironment ape) {
        
        if (atd.isEmpty())
            return AnnotationProcessors.NO_OP;
        
        for (AnnotationTypeDeclaration a: atd) {
            if (a.getQualifiedName().equals(ChannelType.class.getName()))
                return  new ChannelTypeAnnotationProcessor(a,ape);
        }
        return AnnotationProcessors.NO_OP;
    }
   
}
