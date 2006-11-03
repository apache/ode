package org.apache.ode.superbia.multisource;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @goal msdp
 * @phase generate-sources
 * @description Multiple Source Directory Plugin for Maven2
 */
public class MultipleSourceDirMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${sourceDir}"
     * @required
     */
    private File sourcedir;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Adding " + sourcedir + " to compiler path");
        project.addCompileSourceRoot(sourcedir.getAbsolutePath());
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public File getSourceDir() {
        return sourcedir;
    }

    public void setSourceDir(File sourcedir) {
        this.sourcedir = sourcedir;
    }

}
