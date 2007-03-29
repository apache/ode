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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.DeclarationFilter;

class ChannelTypeAnnotationProcessor implements AnnotationProcessor {

    AnnotationProcessorEnvironment _env;
    AnnotationTypeDeclaration _atd;
    
    ChannelTypeAnnotationProcessor(AnnotationTypeDeclaration atd, AnnotationProcessorEnvironment env) {
        _atd = atd;
        _env = env; 
    }
    
    public void process() {
        Collection<InterfaceDeclaration> channels = DeclarationFilter.getFilter(InterfaceDeclaration.class).filter(_env.getDeclarationsAnnotatedWith(_atd),InterfaceDeclaration.class);
        for (InterfaceDeclaration c : channels) {
            PrintWriter pw = null;
            try {
                pw = _env.getFiler().createSourceFile(c.getQualifiedName() + "Channel");
                writeChannelClass(pw, c);
            } catch (IOException e) {
                _env.getMessager().printError(c.getPosition(), "IO Error: " + e.getMessage());
            } finally {
                if (pw != null) pw.close();
                pw = null;
            }
            
            try {
                pw = _env.getFiler().createSourceFile(c.getQualifiedName() + "ChannelListener");
                writeChannelListenerClass(pw, c);
            } catch (IOException e) {
                _env.getMessager().printError(c.getPosition(), "IO Error: " + e.getMessage());
            } finally {
                if (pw != null) pw.close();
                pw = null;
            }
            
        }
    }
    
    
    private void writeChannelClass(PrintWriter pw, InterfaceDeclaration c) {
        pw.println("/*");
        pw.println(" * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR");
        pw.println(" * ");
        pw.println(" *               !!! DO NOT EDIT !!!! ");
        pw.println(" * ");
        pw.println(" * Generated On  : "  + new Date());
        pw.println(" * For Interface : "  + c.getQualifiedName());
        pw.println(" */");
        pw.println();
        pw.println("package " + c.getPackage().getQualifiedName() + ";");
        pw.println();
        
        pw.println("/**");
        pw.println(" * An auto-generated channel interface for the channel type");
        pw.println(" * {@link " + c.getQualifiedName() + "}.");
        pw.println(" * @see " + c.getQualifiedName() );
        pw.println(" * @see " + c.getQualifiedName() + "ChannelListener");
        pw.println(" */");
        pw.println("public interface " + c.getSimpleName() + "Channel");
        
        Collection<InterfaceType> supers = c.getSuperinterfaces();
        if (supers.isEmpty()) {
            pw.println("    extends org.apache.ode.jacob.Channel, ");
        } else {
            pw.print("    extends ");
            for (InterfaceType s : supers)  
                pw.println("            "+ s.getDeclaration().getQualifiedName() + "Channel, ");
            pw.println    ("            org.apache.ode.jacob.Channel, ");
        }
        
        pw.println("            " + c.getQualifiedName());
        pw.println("{}");
        pw.flush();
    }

    private void writeChannelListenerClass(PrintWriter pw, InterfaceDeclaration c) {
        pw.println("/*");
        pw.println(" * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR");
        pw.println(" * ");
        pw.println(" *               !!! DO NOT EDIT !!!! ");
        pw.println(" * ");
        pw.println(" * Generated On  : "  + new Date());
        pw.println(" * For Interface : "  + c.getQualifiedName());
        pw.println(" */");
        pw.println();
        pw.println("package " + c.getPackage().getQualifiedName() + ";");
        pw.println();
        pw.println("import org.apache.commons.logging.LogFactory;");
        pw.println("import org.apache.commons.logging.Log;");
        pw.println();
        pw.println("/**");
        pw.println(" * An auto-generated channel listener abstract class for the ");
        pw.println(" * {@link " + c.getQualifiedName() + "} channel type. ");
        pw.println(" * @see " + c.getQualifiedName() );
        pw.println(" * @see " + c.getQualifiedName() + "Channel");
        pw.println(" */");
        pw.println("public abstract class " + c.getSimpleName() + "ChannelListener");
        pw.println("    extends org.apache.ode.jacob.ChannelListener<" + c.getQualifiedName() + "Channel>" );
        pw.println("    implements " + c.getQualifiedName());
        pw.println("{");
        pw.println();
        pw.println("    private static final Log __log = LogFactory.getLog(" + c.getQualifiedName() + ".class);");
        pw.println();
        pw.println("    protected Log log() { return __log; } " );
        pw.println();
        pw.println("    protected " + c.getSimpleName() + "ChannelListener(" + c.getQualifiedName() + "Channel channel) {");
        pw.println("       super(channel);");
        pw.println("    }");
        pw.println("}");
        pw.flush();
    }

}
