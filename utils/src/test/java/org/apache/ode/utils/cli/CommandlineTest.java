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
package org.apache.ode.utils.cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/*
 * Tests for the commandline handling items in the org.apache.ode.utils.cli package.
 */
public class CommandlineTest extends TestCase {

  private static String[] EMPTY_STRING_ARRAY = new String[]{};

  public CommandlineTest(String arg0) {
    super(arg0);
  }
  
  public void testUserOrder() {
    LastArgument last = new LastArgument("foo", "bar", false);
    Argument first = new Argument("baz", "qux", false);
    Fragments f = new Fragments(new CommandlineFragment[] {last,first});
    CommandlineFragment[] cf = f.getFragmentsInUserOrder();
    assertTrue(cf[0].getDescription(),cf[0] == first);
    assertTrue(cf[1].getDescription(),cf[1] == last);
    Argument second = new Argument("fneh", "fnord", false);
    Fragments g = new Fragments(new CommandlineFragment[] {last,first,second});
    cf = g.getFragmentsInUserOrder();
    assertTrue(cf[0].getDescription(),cf[0] == first);
    assertTrue(cf[1].getDescription(),cf[1] == second);
    assertTrue(cf[2].getDescription(),cf[2] == last);
    LastArgument secondtolast = new LastArgument("grover", "oscar", false);
    Fragments h = new Fragments(new CommandlineFragment[] {last,first, secondtolast, second});
    cf = h.getFragmentsInUserOrder();
    assertTrue(cf[0].getDescription(),cf[0] == first);
    assertTrue(cf[1].getDescription(),cf[1] == second);
    assertTrue(cf[2].getDescription(),cf[2] == secondtolast);
    assertTrue(cf[3].getDescription(),cf[3] == last);
  }
  
  public void testLastArgument() {
    String[] args = new String[] {"a", "b", "c", "d"};
    LastArgument la = new LastArgument("should be d", "", false);
    MultiArgument ma = new MultiArgument("should be a b c", "", false);
    Fragments f = new Fragments(new CommandlineFragment[] {la,ma});
    assertTrue(f.matches(args));
    assertTrue(la.isSet());
    assertTrue(la.getValue().equals("d"));
    assertTrue(ma.isSet());
    String[] mav = ma.getValues();
    assertTrue(mav.length == 3);
    assertTrue(mav[0].equals("a"));
    assertTrue(mav[1].equals("b"));
    assertTrue(mav[2].equals("c"));    
  }
  
  public void testXorGroup() {
    XorGroup xog = BaseCommandlineTool.LOGGING;
    Fragments logging = new Fragments(new CommandlineFragment[] {xog});
    assertTrue(logging.matches(new String[] {"-v"}));
    assertTrue(xog.didMatch());
    assertTrue(xog.getMatched() == BaseCommandlineTool.VERBOSE_F);
  }
  
  private void doFlag(Flag f, List<List<String>> tests, boolean[] good, boolean[] set, 
      List<List<String>> post)
  {
    for (int i=0; i < tests.size(); ++i) {
      List l = null;
      if (good[i]) {
        try {
          l = f.consume(tests.get(i));
        } catch (CommandlineSyntaxException cse) {
          fail(i + ":" + cse.getMessage());
        }
      } else {
        try {
          l = f.consume(tests.get(i));
          fail(i + ": should have failed.");
        } catch (CommandlineSyntaxException cse) {
          // no op.
        }
      }
      if (set[i]) {
        assertTrue(i + " should have been set.",f.isSet());
        assertTrue(i + " didn't match the postcondition.",
            Collections.indexOfSubList(post.get(i), l) != -1);
      } else {
        assertFalse(i + " should not have been set.",f.isSet());
      }
      f.reset();
      assertFalse(i + " should not have been set after reset.",f.isSet());
    }    
  }
  
  private void doFlagWithArgument(FlagWithArgument f, List<List<String>> tests, List<String> values,
      boolean[] good, boolean[] set, List<List<String>> post)
  {
    for (int i=0; i < tests.size(); ++i) {
      List l = null;
      if (good[i]) {
        try {
          l = f.consume(tests.get(i));
        } catch (CommandlineSyntaxException cse) {
          fail(i + ":" + cse.getMessage());
        }
      } else {
        try {
          l = f.consume(tests.get(i));
          fail(i + ": should have failed.");
        } catch (CommandlineSyntaxException cse) {
          // no op.
        }
      }
      if (set[i]) {
        assertTrue(i + " should have been set.",f.isSet());
        assertTrue(i + " didn't match the postcondition.",
            Collections.indexOfSubList(post.get(i) ,l) != -1);
        assertTrue(f.getValue() + "!= " + values.get(i), values.get(i).equals(f.getValue()));
      } else {
        assertFalse(i + " should not have been set.",f.isSet());
      }
      f.reset();
      assertFalse(i + " should not have been set after reset.",f.isSet());
    }    
  }  
  
  public void testOptionalArgument() {
    Argument f = new Argument("x", "foo", true);

    List<List<String>> tests = Arrays.asList(
      Arrays.asList("foo"),
      Arrays.asList(EMPTY_STRING_ARRAY));

    String[] values = new String[] {
        "foo",null
    };

    boolean[] good = new boolean[] {
        true,true
    };

    boolean[] set = new boolean[] {
        true,false
    };

    List<List<String>> post = Arrays.asList(
        Arrays.asList(new String[]{}),
        Arrays.asList(new String[]{}));

    doArgument(f,tests,values,good,set,post);    
  }
  
  public void testRequiredArgument() {
    Argument f = new Argument("x", "foo", false);

    List<List<String>> tests = Arrays.asList(
      Arrays.asList("foo"),
      Arrays.asList(EMPTY_STRING_ARRAY));

    String[] values = new String[] {
        "foo", null
    };

    boolean[] good = new boolean[] {
        true,false
    };

    boolean[] set = new boolean[] {
        true,false
    };

    List<List<String>> post = Arrays.asList(
        Arrays.asList(EMPTY_STRING_ARRAY),
        null);

    doArgument(f, tests, values, good, set, post);    
  }  
  
  public void testOptionalMultiArgument() {
    MultiArgument f = new MultiArgument("x", "foo", true);

    List<List<String>> tests = Arrays.asList(
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY)
    );

    List<List<String>> values = Arrays.asList(
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar"),
        null
    );

    boolean[] good = new boolean[] {
        true,true,true
    };
    
    boolean[] set = new boolean[] {
        true,true,false
    };
    
    List<List<String>> post = Arrays.asList(
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList(EMPTY_STRING_ARRAY)
    );

    doMultiArgument(f, tests, values, good, set, post);    
  }  
  
  public void testRequiredMultiArgument() {
    MultiArgument f = new MultiArgument("x", "foo", false);

    List<List<String>> tests = Arrays.asList(
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY)
    );

    List<List<String>> values = Arrays.asList(
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar"),
        null
    );

    boolean[] good = new boolean[] {
        true,true,false
    };

    boolean[] set = new boolean[] {
        true,true,false
    };

    List<List<String>> post = Arrays.asList(
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList(EMPTY_STRING_ARRAY)
    );

    doMultiArgument(f, tests, values, good, set, post);    
  }    
  
  private void doArgument(Argument f, List<List<String>> tests, String[] values,
      boolean[] good, boolean[] set, List<List<String>> post)
  {
    for (int i=0; i < tests.size(); ++i) {
      List l = null;
      if (good[i]) {
        try {
          l = f.consume(tests.get(i));
        } catch (CommandlineSyntaxException cse) {
          fail(i + ":" + cse.getMessage());
        }
      } else {
        try {
          l = f.consume(tests.get(i));
          fail(i + ": should have failed.");
        } catch (CommandlineSyntaxException cse) {
          // no op.
        }
      }
      if (set[i]) {
        assertTrue(i + " should have been set.",f.isSet());
        assertTrue(i + " didn't match the postcondition.",
            Collections.indexOfSubList(tests.get(i),l) != -1);
        assertTrue(f.getValue() + "!= " + values[i],values[i].equals(f.getValue()));
      } else {
        assertFalse(i + " should not have been set.",f.isSet());
      }
      f.reset();
      assertFalse(i + " should not have been set after reset.",f.isSet());
    }    
  }    
  
  private void doMultiArgument(MultiArgument f, List<List<String>> tests, List<List<String>> values,
      boolean[] good, boolean[] set, List<List<String>> post)
  {
    for (int i=0; i < tests.size(); ++i) {
      List l = null;
      if (good[i]) {
        try {
          l = f.consume(tests.get(i));
        } catch (CommandlineSyntaxException cse) {
          fail(i + ":" + cse.getMessage());
        }
      } else {
        try {
          l = f.consume(tests.get(i));
          fail(i + ": should have failed.");
        } catch (CommandlineSyntaxException cse) {
          // no op.
        }
      }
      if (set[i]) {
        assertTrue(i + " should have been set.",f.isSet());
        assertTrue(i + " didn't match the postcondition.",
            Collections.indexOfSubList(post.get(i), l) != -1);
        assertTrue(i + " had incorrect values.",Collections.indexOfSubList(
            Arrays.asList(f.getValues()), values.get(i)) == 0);
      } else {
        assertFalse(i + " should not have been set.",f.isSet());
      }
      f.reset();
      assertFalse(i + " should not have been set after reset.",f.isSet());
    }    
  }      
  
  public void testOptionalFlagWithArgument() {
    FlagWithArgument f = new FlagWithArgument("x", "foo", "", true);

    List<List<String>> tests = Arrays.asList(
        Arrays.asList("foo", "-x", "bar", "baz"),
        Arrays.asList("-x", "foo", "bar"),
        Arrays.asList("foo", "bar", "-x", "baz"),
        Arrays.asList("-x", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar"),
        Arrays.asList("-x"),
        Arrays.asList("foo", "-x")
    );

    List<String> values = Arrays.asList("bar", "foo", "baz", "bar", null, null, null, null, null);

    boolean[] good = new boolean[] {
        true,true,true,true,true,true,true,false,false
    };

    boolean[] set = new boolean[] {
        true,true,true,true,false,false,false,false,false
    };

    List<List<String>> post = Arrays.asList(
        Arrays.asList("foo", "baz"),
        Arrays.asList("bar"),
        Arrays.asList("foo", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar"),
        null,
        null
    );

    doFlagWithArgument(f, tests, values, good, set, post);
  }  
  
  public void testRequiredFlagWithArgument() {
    FlagWithArgument f = new FlagWithArgument("x", "foo", "", false);
    List<List<String>> tests = Arrays.asList(
        Arrays.asList("foo", "-x", "bar", "baz"),
        Arrays.asList("-x", "foo", "bar"),
        Arrays.asList("foo", "bar", "-x", "baz"),
        Arrays.asList("-x", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar"),
        Arrays.asList("-x"),
        Arrays.asList("foo", "-x")
    );

    List<String> values = Arrays.asList("bar", "foo", "baz", "bar", null, null, null, null, null);

    boolean[] good = new boolean[] {
        true,true,true,true,false,false,false,false,false
    };

    boolean[] set = new boolean[] {
        true,true,true,true,false,false,false,false,false
    };

    List<List<String>> post = Arrays.asList(
        Arrays.asList("foo", "baz"),
        Arrays.asList("bar"),
        Arrays.asList("foo", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        null,null,null,null,null
    );

    doFlagWithArgument(f, tests, values, good, set, post);
  }    
  
  public void testOptionalFlag() {
    Flag f = new Flag("x", "", true);

    List<List<String>> tests = Arrays.asList(
        Arrays.asList("foo", "-x", "bar"),
        Arrays.asList("-x", "foo", "bar"),
        Arrays.asList("foo", "bar", "-x"),
        Arrays.asList("-x"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar")
    );

    boolean[] good = new boolean[] {
        true,true,true,true,true,true,true
    };

    boolean[] set = new boolean[] {
        true,true,true,true,false,false,false
    };

    List<List<String>> post = Arrays.asList(
        Arrays.asList("foo", "bar"),
        Arrays.asList("foo", "bar"),
        Arrays.asList("foo", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar")
    );

    doFlag(f, tests, good, set, post);
  }
  
  public void testRequiredFlag() {
    Flag f = new Flag("x", "", false);

    List<List<String>> tests = Arrays.asList(
        Arrays.asList("foo", "-x", "bar"),
        Arrays.asList("-x", "foo", "bar"),
        Arrays.asList("foo", "bar", "-x"),
        Arrays.asList("-x"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        Arrays.asList("foo"),
        Arrays.asList("foo", "bar")
    );

    boolean[] good = new boolean[] {
        true,true,true,true,false,false,false
    };

    boolean[] set = new boolean[] {
        true,true,true,true,false,false,false
    };

    List<List<String>> post = Arrays.asList(
        Arrays.asList("foo", "bar"),
        Arrays.asList("foo", "bar"),
        Arrays.asList("foo", "bar"),
        Arrays.asList(EMPTY_STRING_ARRAY),
        null,
        null,
        null
    );

    doFlag(f, tests, good, set, post);    
  }
  
  public void testExtraStuff() {
    Fragments f = new Fragments(new CommandlineFragment[] {});
    String[] args = new String[] {"a", "b"};
    assertFalse(f.matches(args));
  }
}
