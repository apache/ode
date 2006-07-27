#!/usr/bin/expect 
# Simple expect script to run the spexerciser.
proc abort { msg } {
  send_user "FAILURE: $msg"
  exit -1
}
log_user 0
spawn -noecho ode -v -console
expect -timeout 30 -re ^ode.*\r\n?Copyright.*\r {
    # noop.
  } timeout {
    abort "ode took too long to output the initial header.
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
    # no op; this is what we wanted.
  } -re ^INFO.*\r {
    exp_continue    
  } timeout {
    abort "ode took too long to start up; aborting.\n"
  }
expect_background -timeout -1 -re ^\r {
    exp_continue
  } -re ^\n {
    exp_continue
  } -re ^INFO.*\r {
    exp_continue
  } -re ^ERROR.*\r {
    abort "FAILURE: ode encountered an error - $expect_out(buffer)
  } eof {
    abort "FAILURE: ode exited suddenly.\n"
  }
spawn -noecho bpeltests
set spex $spawn_id
set errors 0
expect -i $spex -timeout 30 -re Running\ \[0-9\]+\ tests: {
  } timeout {
    abort "FAILURE: spexerciser took too long to start up.\n"
  }
expect -timeout 100 -re ^\\. {
    exp_continue      
  } -re ^\r?\n {
    # noop
  } -re \[\[\](.*?)\[\]\] {
    send_user "ERROR: $expect_out(1,string)\n"
    set errors [ expr { $errors + 1 } ]
    exp_continue
  } eof {
    abprt "spexerciser exited abruptly.\n"
    exit -1
  } timeout {
    abort "spexerciser appears to have hung.\n"
    exit -1
  }
if { $errors > 0 } {
  send_user "FAILURE: spexerciser reported $errors error[ expr  $errors == 1 ?"":"s" ].\n"
  exit -1
} else {
  send_user "SUCCESS: spexerciser completed successfully.\n"
  exit 0
}
