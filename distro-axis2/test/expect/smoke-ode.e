#!/usr/bin/expect
# Simple expect script to run the spexerciser.
proc abort { msg } {
  send_user "FAILURE: $msg"
  exit -1
}
log_user 0
spawn -noecho ode -v -console
expect -re ode.*\r\n?Copyright.*\r {
  } timeout {
    abort "ODE took too long to print the copyright header."
  }
expect -timeout 100 -re ^\r { 
    exp_continue
  } -re ^\n {
    exp_continue
  } -re ^Warning.*XSLT.*\r {
    exp_continue
  } -re ^ERROR.*\r {
    abort "ode encountered an internal error: $expect_out(buffer)\n"
  } eof {
    abort "ode exited suddenly (probably due to error).\n"
  } -re ^INFO\.*\ Startup\ completed\. {
    send_user "SUCCESS: ode server started up successfully.\n"
    exit 0
  } -re ^INFO.*\r {
    exp_continue    
  } timeout {
    abort "ode took too long to start up; aborting.\n"
  }
