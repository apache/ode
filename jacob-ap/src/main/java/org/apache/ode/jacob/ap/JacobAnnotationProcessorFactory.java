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
