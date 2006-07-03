Example of dynamic assignment of partner links. This example
includes two processes, a main and a responder. To build them run
the following commands:

ant responder
ant main

Deploy the two generated sar files using the 'pxe-deploy' command
and then run:

ant test

The Main process has basically two principal steps. First it
asks the responder for an endpoint reference, second it invokes
the provided endpoint to check that it's really there (by
assigning it to a partner link).

The responder replies to the first message by providing its
own endpoint (myRole) and then replies to the second invocation
(which is dynamic in the sense that Main has no idea which
service it is invoking).

There's also a deployment descriptor packaged with this example
(Main.dd). This is just provided to demonstrate how partner
link endpoints can be overriden using this descriptor. Note that
in this particular case it's pointless as the address provided is
the same as the default address (the partner SOAP address
defined in Responder.wsdl).