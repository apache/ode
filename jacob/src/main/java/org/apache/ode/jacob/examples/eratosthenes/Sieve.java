/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.jacob.examples.eratosthenes;

import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.jacob.SynchChannelListener;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;

/**
 * Sieve of Eratosthenes prime number generator.
 * This class represents the following process term:
 * <pre><em>
 * Sieve :=
 *  (v integers)(v primes) Counter(integer,2) | Head(integer, primes) | Print(primes)
 * </em></pre>
 *
 * <p>Created on Feb 12, 2004 at 6:32:49 PM.</p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class Sieve extends JacobRunnable {
  private static final long serialVersionUID = -1303509567096202776L;
  
  private static int _cnt = 0;
  private static int _last = 0;

  public void run() {
    NaturalNumberStreamChannel integers =  newChannel(NaturalNumberStreamChannel.class);
    NaturalNumberStreamChannel primes =  newChannel(NaturalNumberStreamChannel.class);
    instance(new Counter(integers,2));
    instance(new Head(integers,primes));
    instance(new Print(primes));
  }

  public class Foo {}

  /**
   * A counter, aka integer stream generator; represents the following process term:
   * <pre><em>
   *  Counter(out, n) := out.val(n) | Counter(out, n+1)
   * </em></pre>
   */
  private static class Counter extends JacobRunnable {
    private static final long serialVersionUID = 4739323750438991003L;
    
    private NaturalNumberStreamChannel _out;
    private int _n;

    public Counter(NaturalNumberStreamChannel out, int n) {
      _out = out;
      _n = n;
    }

    public void run() {
      _out.val(_n, object(new SynchChannelListener(newChannel(SynchChannel.class)) {
        private static final long serialVersionUID = -4336285925619915276L;

        public void ret() {
          instance(new Counter(_out, _n+1));
        }
      }));
    }
  }

  /**
   * Head extractor, takes the first element from a stream; this represent the process
   * term:
   * <pre><em>
   * Head(in, primes) :=
   *  in ? [val(n)={primes.val(n) | (v x) PrimeFilter(n, in, x) | Head(x,primes)}]
   * </em></pre>
   *
   *
   */
  private static final class Head extends JacobRunnable {
    private static final long serialVersionUID = 1791641314141082728L;
    
    NaturalNumberStreamChannel _in;
    NaturalNumberStreamChannel _primes;

    public Head(NaturalNumberStreamChannel in, NaturalNumberStreamChannel primes) {
      _in = in;
      _primes = primes;
    }

    public void run() {
      object(new NaturalNumberStreamChannelListener(_in) {
        private static final long serialVersionUID = -2145752474431263689L;

        public void val(final int n, final SynchChannel ret) {
          _primes.val(n, object(new SynchChannelListener(newChannel(SynchChannel.class)) {
            private static final long serialVersionUID = -3009595654233593893L;

            public void ret() {
              NaturalNumberStreamChannel x = newChannel(NaturalNumberStreamChannel.class);
              instance(new PrimeFilter(n, _in, x));
              instance(new Head(x, _primes));
              ret.ret();
            }
          }));
       }
      });
    }
  }

  private static final class Print extends JacobRunnable {
    private static final long serialVersionUID = -3134193737519487672L;
    
    private NaturalNumberStreamChannel _in;
    public Print(NaturalNumberStreamChannel in) {
      _in = in;
    }
    public void run() {
      object(true, new NaturalNumberStreamChannelListener(_in){
        private static final long serialVersionUID = 7671019806323866866L;

        public void val(int n, SynchChannel ret) {
          _cnt ++;
          _last = n;
          System.out.println("PRIME: " + n);
          ret.ret();
        }
      });
    }
  }

  /**
   * A prime filter, filters out the nubmer in an input stream that are multiple of a
   * prime. This represents the following process term:
   * <pre><em>
   * PrimeFilter(prime, in, out) :=
   *     ! in ? [val(n)={ if(n mod prime <> 0) out.val(n) }
   * </em></prime>
   */
  private static class PrimeFilter extends JacobRunnable {
    private static final long serialVersionUID = 1569523200422202448L;
    
    private int _prime;
    private NaturalNumberStreamChannel _in;
    private NaturalNumberStreamChannel _out;

    public PrimeFilter(int prime, NaturalNumberStreamChannel in, NaturalNumberStreamChannel out) {
      _prime = prime;
      _in = in;
      _out = out;
    }
    public void run() {
       object(true, new NaturalNumberStreamChannelListener(_in) {
        private static final long serialVersionUID = 6625386475773075604L;

        public void val(int n, final SynchChannel ret) {
           if (n % _prime != 0)
             _out.val(n, object(new SynchChannelListener(newChannel(SynchChannel.class)) {
              private static final long serialVersionUID = 2523405590764193613L;

              public void ret() {
                 ret.ret();
               }
             }));
           else
             ret.ret();
         }
       });
    }
  }


  public static void main(String args[]) {
    if (args.length != 1) {
      System.err.println("JACOB Sieve of Eratosthenes Prime Number Generator Demonstration");
      System.err.println("usage: java " + Sieve.class.getName() + " requested-prime");
      System.err.println("  requested-prime = which prime to show (0->inf)");
      System.exit(1);
    } else {
      int request = Integer.parseInt(args[0]);
      JacobVPU vpu = new JacobVPU();
      vpu.setContext(new ExecutionQueueImpl(null));
      vpu.inject(new Sieve());
      while (_cnt != request) {
        vpu.execute();
      }
      System.err.println("The " + _cnt + "th prime is " + _last);
    }


  }
}
