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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
            if (channelClass(elem).generate() && channelListener(elem).generate()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generation complete: @ChannelType implementation and listener for " + elem.asType().toString());
            } else {
                return false;
            }
        }
        return true;
    }

    private SourceGenerator channelClass(Element element) {
        return new ChannelClassGenerator(processingEnv, element);
    }

    private SourceGenerator channelListener(Element element) {
        return new ChannelListenerGenerator(processingEnv, element);
    }

    // TODO: check if instead of using a String and '\n' line terminators wouldn't be better to
    //  return a String[] of lines and use println foreach line (would probably use crlf on Win)
    private static final String HEADER = 
        // TODO: ported from earlier version, but ugly as hell, could use a facelift
        "/*\n" +
        " * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR\n" +
        " * \n" +
        " *               !!! DO NOT EDIT !!!! \n" +
        " * \n" +
        " * Generated On  : <today>\n" +
        " * For Interface : <fqn>\n" +
        " */\n" +
        "package <package>;\n";
    private static final String CHANNEL_DECL = 
		"/**\n" +
		" * An auto-generated channel interface for the channel type\n" +
		" * {@link <fqn>}.\n" +
		" * @see <fqn>\n" +
		" * @see <fqn>ChannelListener\n" +
		" */\n" +
		"public interface <name>Channel extends\n" + 
        "<interfaces> {\n" + 
        "}";
    private static final String LISTENER_DECL = 
        "import org.apache.commons.logging.Log;\n" + 
        "import org.apache.commons.logging.LogFactory;\n" + 
        "\n" + 
        "/**\n" +
        " * An auto-generated channel listener abstract class for the \n" +
        " * {@link <fqn>} channel type. \n" +
        " * @see <fqn>\n" +
        " * @see <fqn>Channel\n" +
        " */\n" +
        "public abstract class <name>ChannelListener\n" +
        "    extends org.apache.ode.jacob.ChannelListener<<fqn>Channel>\n" +
        "    implements <fqn> {\n" + 
        "\n" + 
        "    private static final Log LOG = LogFactory.getLog(<fqn>.class);\n" +
        "\n" + 
        "    protected Log log() {\n" +
        "        return LOG;\n" +
        "    }\n" +
        "\n" + 
        "    protected <name>ChannelListener(<fqn>Channel channel) {\n" +
        "        super(channel);\n" +
        "    }\n" +
        "}";

    public abstract class SourceGenerator {
        public static final String INDENT = "    ";
        private final ProcessingEnvironment penv;
        private final Date today = new Date();
        private final String suffix;
        private Element type;
        
        public SourceGenerator(ProcessingEnvironment penv, Element type, String suffix) {
            this.penv = penv;
            this.type = type;
            this.suffix = suffix;
        }

        public ProcessingEnvironment getProcessingEnvironment() {
            return penv;
        }

        public Element getType() {
            return type;
        }

        public boolean generate() {
            Writer w;
            try {
                w = penv.getFiler().createSourceFile(getSourceFileName(), type).openWriter();
            } catch (IOException e) {
                penv.getMessager().printMessage(Diagnostic.Kind.NOTE, e.getMessage());
                return false;
            }

            final PrintWriter writer = new PrintWriter(w);
            generateHeader(writer);
            generateContent(writer);
            writer.flush();
            writer.close();
            return true;
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
            return penv.getElementUtils().getPackageOf(type).toString();
        }
        
        protected List<TypeMirror> getSuperInterfaces() {
            List<TypeMirror> answer = new ArrayList<TypeMirror>();
            for (TypeMirror m : getProcessingEnvironment().getTypeUtils().directSupertypes(getType().asType())) {
                DeclaredType decl = m.getKind() == TypeKind.DECLARED ? (DeclaredType) m : null;
                if (decl.asElement().getKind() == ElementKind.INTERFACE) {
                    answer.add(m);
                }
            }
            return answer;
        }

        protected void generateHeader(PrintWriter writer) {
        	writer.println(HEADER
        		.replaceAll("<today>", today.toString())
        		.replaceAll("<fqn>", getType().asType().toString())
        		.replaceAll("<package>", getPackage()));
        }
    };

    private class ChannelClassGenerator extends SourceGenerator {

        public ChannelClassGenerator(ProcessingEnvironment penv, Element type) {
            super(penv, type, "Channel");
        }

        protected String generateInterfaces() {
        	StringBuilder ifs = new StringBuilder();
            for (TypeMirror m : getSuperInterfaces()) {
                ifs.append("    ").append(m.toString()).append("\n");
            }
            ifs.append("    org.apache.ode.jacob.Channel,\n");
            ifs.append("    ").append(getType().asType().toString());
            return ifs.toString();
        }

        protected void generateContent(PrintWriter writer) {
            // TODO: add the javadoc class prefix?
        	writer.println(CHANNEL_DECL
        		.replaceAll("<name>", getType().getSimpleName().toString())
        		.replaceAll("<interfaces>", generateInterfaces())
    		    .replaceAll("<fqn>", getType().asType().toString()));
        }
    };
    
    private class ChannelListenerGenerator extends SourceGenerator {

        public ChannelListenerGenerator(ProcessingEnvironment penv, Element type) {
            super(penv, type, "ChannelListener");
        }

        protected void generateContent(PrintWriter writer) {
            // TODO: add the javadoc class prefix?
        	writer.println(LISTENER_DECL
        		.replaceAll("<name>", getType().getSimpleName().toString())
        		.replaceAll("<fqn>", getType().asType().toString()));
        }
    }
}
