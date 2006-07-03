#!/usr/bin/expect
# Simple expect script to run the spexerciser.
proc abort { msg } {
  send_user "FAILURE: $msg"
  exit -1
}
log_user 0
spawn -noecho pxe -v -console
expect -re pxe.*\r\n?Copyright.*\r {
  } timeout {
    abort "PXE took too long to print the copyright header."
  }
expect -timeout 100 -re ^\r { 
    exp_continue
  } -re ^\n {
    exp_continue
  } -re ^Warning.*XSLT.*\r {
    exp_continue
  } -re ^ERROR.*\r {
    abort "pxe encountered an internal error: $expect_out(buffer)\n"
  } eof {
    abort "pxe exited suddenly (probably due to error).\n"
  } -re ^INFO\.*\ Startup\ completed\. {
    send_user "SUCCESS: pxe server started up successfully.\n"
    exit 0
  } -re ^INFO.*\r {
    exp_continue    
  } timeout {
    abort "pxe took too long to start up; aborting.\n"
  }
