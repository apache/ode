To deploy any example in Ode just copy its whole directory to the
WEB-INF/processes directory of your deployed exploded webapp. To
be able to easily send messages put the bin directory or Ode's
distribution in your path:

Windows
set PATH=%PATH%;PATH_TO_ODE\bin

Linux
export PATH=$PATH:PATH_TO_ODE/bin

The sendsoap command can either be executed using sendsoap.bat under
Windows or sendsoap with Linux (replace accordingly in the
following command line examples).

Hello World 2
=============
Demonstrates a simple service invocation that synchronously replies to
a message. Built using WS-BPEL 2.0 syntax. After deployment, start a
process with the command:

sendsoap http://localhost:8080/ode/processes/helloWorld testRequest.soap

Please make sure that you execute the command from the example 
directory. The response should be a SOAP message containing the
'hello world' string.

Dynamic Partner
===============
Demonstrates dynamic partner assignment. The main process asks for the
responder process endpoint. The responder process gives its endpoint by
assigning it to a message (assign my role) and replying this message to
the main process. The main process invokes again the responder process
but this time using the endpoint it just receives instead of the
default one.

After deployment, start a process with the command:

sendsoap http://localhost:8080/ode/processes/DynMainService testRequest.soap

Please make sure that you execute the command from the example 
directory. The response should be an 'OK' SOAP message, showing
that all invocations have been successful.

Magic Session
=============
Demonstrates the usage of "magic sessions" or implicit correlation. Ode
supports implicit correlation between two processes or with other
services using a session-based protocol. So you don't need to provide
any correlation mechanism to establish a stateful interaction (see Ode's
website for more information).

After deployment, start a process with the command:

sendsoap http://localhost:8080/ode/processes/MSMainExecuteService testRequest.soap

Please make sure that you execute the command from the example 
directory. The response should be an 'OK' SOAP message, showing
that all invocations have been successful.
