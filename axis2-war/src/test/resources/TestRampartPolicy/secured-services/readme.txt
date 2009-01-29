

How does TestRampartPolicy bundle work?
========================================

The goal of this test is to make sure Rampart is integrated properly.
Rampart comes with a set of samples [1]. A subset of them test the same service with different security policies.

Here we reuse the policy samples and for each of them generate a process bundle based on the TestRampartPolicy/process-template.
The qname of the services vary so two variables are replaced:
{sample.namespace}
{sample.service.name}

This is done before running the tests in the Rakefile, see task :prepare_rampart_policy_test.

The external services are packaged as Axis archives in TestRampartPolicy/services.
The Password Callback Handler classes are in src/test/java.

[1] https://svn.apache.org/repos/asf/webservices/rampart/trunk/java/modules/rampart-samples/policy/