/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package genbuild;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.Commandline;

/**
 * Read the CVS sticky tag of a file. 
 * @ant.task name="cvsgetstickytag"
 */
public class CvsGetStickyTag extends Task {
    
    /** Input file */
    private File _file;

    /** Output property */
    private String _property;


    /**
     * Set the file to check
     *
     * @param file file to check
     */
    public void setFile(final File file) {
        _file = file;
    }


    /**
     * Set the output property.
     *
     * @param property property
     */
    public void setProperty(final String property) {
        _property = property;
    }


    
    /**
     * Execute task
     *
     * @exception BuildException if something goes wrong executing the
     *            cvs command
     */
    public void execute() throws BuildException {

        validate();

        final Commandline command = new Commandline();

        command.setExecutable("cvs");
        command.createArgument().setValue("status");
        command.createArgument().setValue(_file.getName());

        CvsStatusOutputStream handler = new CvsStatusOutputStream();
        
        log(command.describeCommand(), Project.MSG_VERBOSE);

        final Execute exe = new Execute(new PumpStreamHandler(handler,
          new java.io.ByteArrayOutputStream()));

        exe.setWorkingDirectory(_file.getParentFile());
        exe.setCommandline(command.getCommandline());
        exe.setAntRun(getProject());
        try {
            final int resultCode = exe.execute();

            if (0 != resultCode) {
                throw new BuildException("Error running '" + command.getCommandline() + "': " + resultCode);
            }
        } catch (final IOException ioe) {
            throw new BuildException(ioe.toString());
        }

        if (handler.getTag() == null)
          throw new BuildException("Tag not found.");
        
        getProject().setProperty(_property, "".equals(handler.getTag()) ? "HEAD" : handler.getTag());
    }

    /**
     * Validate the parameters specified for task.
     *
     * @throws BuildException if fails validation checks
     */
    private void validate()
         throws BuildException {

           if (null == _file) {
            final String message = "file must be set.";

            throw new BuildException(message);
        }
        if (!_file.exists()) {
            final String message = "Cannot find file "
                 + _file;

            throw new BuildException(message);
        }
        
        if (_property == null)
           throw new BuildException("property must be set.");
         
        
    }


static class CvsStatusOutputStream
     extends LogOutputStream {
    
    private String _tag = null;

    /**
     * Creates a new instance of this class.
     *
     * @param parser the parser to which output is sent.
     */
    CvsStatusOutputStream() {
        super(null, 0);
    }


    /**
     * Logs a line to the log system of ant.
     *
     * @param line the line to log.
     */
    protected void processLine(final String line) {
      if (line.indexOf("Sticky Tag:") != -1) {
        int colpos = line.indexOf(":");
        String tag = line.substring(colpos+1);
        tag = tag.trim();
        int end = tag.indexOf("(");
        tag = tag.substring(0, end).trim();
        _tag = tag;
      }
    }
    
    String getTag() { return _tag; }
}


}

