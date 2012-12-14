/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.jacob.generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.ode.jacob.annotation.ChannelType;


@SupportedAnnotationTypes("org.apache.ode.jacob.annotation.ChannelType")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ChannelTypeProcessor extends AbstractProcessor {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(ChannelType.class)) {
            @SuppressWarnings("unused")
            ChannelType channel = elem.getAnnotation(ChannelType.class);
            String message = "annotation found in " + elem.getSimpleName();
            PrintWriter pw = null;
            try {
                // TODO: resolve the output directory issue and plugin the code from the old generator
                JavaFileObject source = processingEnv.getFiler().createSourceFile("org.apache.ode.jacob.generator.Foo", elem);
                final Writer writer = source.openWriter();
                pw = new PrintWriter(writer);
                pw.append("package ").append("org.apache.ode.jacob.generator").println(';');
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, e.getMessage());
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
        }
        return true;
    }

}
