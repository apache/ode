#!/usr/bin/expect
# Simple test to determine if message bundles are working correctly.
log_user 0
spawn -noecho bpelc -wsdl foo -bpel bar
expect -re ^\r {
    exp_continue
  } -re ^\n {
    exp_continue
  } -re ^.+\[\{\]1\[\}\]\ \[\{\]2\[\}\]\ \[\{\]3\[\}\]\ \[\{\]4\[\}\]\ \[\{\]5\[\}\]\ \[\{\]6\[\}\]\ \[\{\]7\[\}\]\r {
    send_user "FAILURE: Message bundles are not being dereferenced to messages.\n"  
    exit -1
  } -re ^.+\r { 
	send_user "SUCCESS: Message was dereferenced.\n"
	exit 0  	
  } eof {
    send_user "FAILURE: no message was produced.\n"
    exit -1
  } timeout {
    send_user "FAILURE: the test timed out.\n"
    exit -1
  }
send_user "TEST_ERROR: Impossible state reached.\n"
exit -1