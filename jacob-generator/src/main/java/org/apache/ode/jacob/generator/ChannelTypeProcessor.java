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
import java.util.Date;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.apache.ode.jacob.annotation.ChannelType;


@SupportedAnnotationTypes("org.apache.ode.jacob.annotation.ChannelType")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ChannelTypeProcessor extends AbstractProcessor {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(ChannelType.class)) {
            if (elem.getKind() != ElementKind.INTERFACE) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The @" + ChannelType.class.getSimpleName()
                    + " is only supported on interfaces; " + elem.asType().toString() + " is a " + elem.getKind().toString());
                continue;
            }
            if (generateSourceFile(elem, channelClass(elem)) && generateSourceFile(elem, channelListener(elem))) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generation complete: @ChannelType implementation and listener for " + elem.asType().toString());
            } else {
                return false;
            }
        }
        return true;
    }

    protected boolean generateSourceFile(Element elem, SourceGenerator gen) {
        try {
            gen.generate(processingEnv.getFiler().createSourceFile(gen.getSourceFileName(), elem).openWriter());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, e.getMessage());
            return false;
        }
        return true;
    }
    
    private SourceGenerator channelClass(Element element) {
        return new ChannelClassGenerator(element);
    }

    private SourceGenerator channelListener(Element element) {
        return new ChannelListenerGenerator(element);
    }

    public abstract class SourceGenerator {
        private final Date today = new Date();
        private final String suffix;
        private Element type;
        
        public SourceGenerator(Element type, String suffix) {
            this.type = type;
            this.suffix = suffix;
        }

        public Element getType() {
            return type;
        }

        public void generate(Writer writer) {
            final PrintWriter w = new PrintWriter(writer);
            generateHeader(w);
            generateContent(w);
            w.close();
        }

        // TODO: is it really worth splitting this into a interface/abstract class? maybe later...
        protected abstract void generateContent(PrintWriter writer);

        protected String getSuffix() {
            return suffix;
        }

        protected String getSourceFileName() {
            return getType().asType().toString() + suffix;
        }

        protected String getPackage() {
            return type.asType().toString();
        }
        
        protected void generateHeader(PrintWriter writer) {
            // TODO: ported from earlier version, but ugly as hell, could use a facelift
            writer.println("/*");
            writer.println(" * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR");
            writer.println(" * ");
            writer.println(" *               !!! DO NOT EDIT !!!! ");
            writer.println(" * ");
            writer.println(" * Generated On  : "  + today);
            writer.println(" * For Interface : "  + getType().asType().toString());
            writer.println(" */");
            writer.println();
     
            writer.append("package ").append(getPackage()).println(';');
            writer.println();
        }
    };

    private class ChannelClassGenerator extends SourceGenerator {

        public ChannelClassGenerator(Element type) {
            super(type, "Channel");
        }

        protected void generateContent(PrintWriter writer) {
            writer.append("public interface ").append(getType().getSimpleName()).append(getSuffix()).println("{}");
            writer.flush();
        }
    };
    
    private class ChannelListenerGenerator extends SourceGenerator {

        public ChannelListenerGenerator(Element type) {
            super(type, "ChannelListener");
        }

        protected void generateContent(PrintWriter writer) {
            writer.append("public interface ").append(getType().getSimpleName()).append(getSuffix()).println("{}");
            writer.flush();
        }
    }
}
