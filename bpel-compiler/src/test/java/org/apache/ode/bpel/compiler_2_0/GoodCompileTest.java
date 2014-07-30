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

package org.apache.ode.bpel.compiler_2_0;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;

import org.apache.ode.bpel.compiler.api.CompileListener;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.migrate.EqualityVisitor;
import org.apache.ode.bpel.obj.migrate.ExtensibeImplEqualityComp;
import org.apache.ode.bpel.obj.migrate.ObjectTraverser;
import org.apache.ode.bpel.obj.serde.DeSerializer;
import org.apache.ode.bpel.obj.serde.OmDeserializer;
import org.apache.ode.bpel.obj.serde.OmSerdeFactory;
import org.junit.Assert;
import org.junit.Test;

public class GoodCompileTest extends AbstractCompileTestCase implements
		CompileListener {

	public void runTest(String bpel) throws Exception {
		try {
			Class testClass = getClass();
			URL url = testClass.getResource(bpel);
			URI uri = url.toURI();
			String path = uri.getPath();
			File bpelFile = new File(path);
			OProcess origi = _compiler.compile2OProcess(bpelFile, 0);
			String bpelPath = bpelFile.getAbsolutePath();
			String cbpPath = bpelPath.substring(0, bpelPath.lastIndexOf("."))
					+ ".cbp";
			_compiler.serializeOProcess(origi, cbpPath);

			DeSerializer deserializer = new DeSerializer(new FileInputStream(cbpPath));
			OProcess desered = deserializer.deserialize();

			ObjectTraverser traverse = new ObjectTraverser();
			EqualityVisitor visitor = new EqualityVisitor(desered);
			visitor.addCustomComparator(new ExtensibeImplEqualityComp(visitor));
			traverse.accept(visitor);
			boolean res = (Boolean) traverse.traverseObject(origi);
			assertEquals(Boolean.TRUE, res);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Compilation or deserialization did not succeed.");
		}
	}

	@Test
	public void testAssign1_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign1-2.0.bpel");
	}

	@Test
	public void testAssign2_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign2-2.0.bpel");
	}

	@Test
	public void testAssign3_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign3-2.0.bpel");
	}

	@Test
	public void testAssign5_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign5-2.0.bpel");
	}

	@Test
	public void testAssign6_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign6-2.0.bpel");
	}

	@Test
	public void testAssign7_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign7-2.0.bpel");
	}

	@Test
	public void testAssign8_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign8-2.0.bpel");
	}

	@Test
	public void testAssign9_BPEL20() throws Exception {
		runTest("/2.0/good/assign/Assign9-2.0.bpel");
	}

	@Test
	public void testAsyncProcess() throws Exception {
		runTest("/2.0/good/AsyncProcess/AsyncProcess2.bpel");
	}

	@Test
	public void testCompensation1() throws Exception {
		runTest("/2.0/good/compensation/comp1-2.0.bpel");
	}

	@Test
	public void testCompensation2() throws Exception {
		runTest("/2.0/good/compensation/comp2-2.0.bpel");
	}

	@Test
	public void testFlow2() throws Exception {
		runTest("/2.0/good/flow/flow2-2.0.bpel");
	}

	@Test
	public void testFlow3() throws Exception {
		runTest("/2.0/good/flow/flow3-2.0.bpel");
	}

	@Test
	public void testFlow4() throws Exception {
		runTest("/2.0/good/flow/flow4-2.0.bpel");
	}

	@Test
	public void testFlow5() throws Exception {
		runTest("/2.0/good/flow/flow5-2.0.bpel");
	}

	@Test
	public void testFlow6() throws Exception {
		runTest("/2.0/good/flow/flow6-2.0.bpel");
	}

	@Test
	public void testFlow7() throws Exception {
		runTest("/2.0/good/flow/flow7-2.0.bpel");
	}

	@Test
	public void testForEach1() throws Exception {
		runTest("/2.0/good/foreach/ForEach1-2.0.bpel");
	}

	@Test
	public void testForEach2() throws Exception {
		runTest("/2.0/good/foreach/ForEach2-2.0.bpel");
	}

	@Test
	public void testForEach3() throws Exception {
		runTest("/2.0/good/foreach/ForEach3-2.0.bpel");
	}

	@Test
	public void testIf1() throws Exception {
		runTest("/2.0/good/if/If1-2.0.bpel");
	}

	@Test
	public void testIf2() throws Exception {
		runTest("/2.0/good/if/If2-2.0.bpel");
	}

	@Test
	public void testIf3() throws Exception {
		runTest("/2.0/good/if/If3-2.0.bpel");
	}

	@Test
	public void testPick3() throws Exception {
		runTest("/2.0/good/pick/Pick3-2.0.bpel");
	}

	@Test
	public void testPick4() throws Exception {
		runTest("/2.0/good/pick/Pick4-2.0.bpel");
	}

	@Test
	public void testPick5() throws Exception {
		runTest("/2.0/good/pick/Pick5-2.0.bpel");
	}

	@Test
	public void testPick6() throws Exception {
		runTest("/2.0/good/pick/Pick6-2.0.bpel");
	}

	@Test
	public void testRethrow1() throws Exception {
		runTest("/2.0/good/rethrow/Rethrow1-2.0.bpel");
	}

	@Test
	public void testRethrow2() throws Exception {
		runTest("/2.0/good/rethrow/Rethrow2-2.0.bpel");
	}

	@Test
	public void testThrow1() throws Exception {
		runTest("/2.0/good/throw/Throw1-2.0.bpel");
	}

	@Test
	public void testThrow2() throws Exception {
		runTest("/2.0/good/throw/Throw2-2.0.bpel");
	}

	@Test
	public void testThrow3() throws Exception {
		runTest("/2.0/good/throw/Throw3-2.0.bpel");
	}

	@Test
	public void testThrow4() throws Exception {
		runTest("/2.0/good/throw/Throw4-2.0.bpel");
	}

	@Test
	public void testThrow5() throws Exception {
		runTest("/2.0/good/throw/Throw5-2.0.bpel");
	}

	@Test
	public void testThrow6() throws Exception {
		runTest("/2.0/good/throw/Throw6-2.0.bpel");
	}

	@Test
	public void testThrow7() throws Exception {
		runTest("/2.0/good/throw/Throw7-2.0.bpel");
	}

	@Test
	public void testWait1() throws Exception {
		runTest("/2.0/good/wait/Wait1-2.0.bpel");
	}

	@Test
	public void testWait2() throws Exception {
		runTest("/2.0/good/wait/Wait2-2.0.bpel");
	}

	@Test
	public void testWhile() throws Exception {
		runTest("/2.0/good/while/While1-2.0.bpel");
	}

	@Test
	public void testXPath10GetVariableData1() throws Exception {
		runTest("/2.0/good/xpath10-func/GetVariableData1-2.0.bpel");
	}

	@Test
	public void testXPath10GetVariableData2() throws Exception {
		runTest("/2.0/good/xpath10-func/GetVariableData2-2.0.bpel");
	}

	@Test
	public void testXPath10GetVariableData3() throws Exception {
		runTest("/2.0/good/xpath10-func/GetVariableData3-2.0.bpel");
	}

	@Test
	public void testXPath10GetVariableData4() throws Exception {
		runTest("/2.0/good/xpath10-func/GetVariableData4-2.0.bpel");
	}

	@Test
	public void testXPath10GetVariableProperty1() throws Exception {
		runTest("/2.0/good/xpath10-func/GetVariableProperty1-2.0.bpel");
	}

	@Test
	public void testXPath20GetVariableData2() throws Exception {
		runTest("/2.0/good/xpath20-func/GetVariableData2-xp2.0.bpel");
	}

	@Test
	public void testXPath20GetVariableData3() throws Exception {
		runTest("/2.0/good/xpath20-func/GetVariableData3-xp2.0.bpel");
	}

	@Test
	public void testXPath20GetVariableData4() throws Exception {
		runTest("/2.0/good/xpath20-func/GetVariableData4-xp2.0.bpel");
	}

	@Test
	public void testXPath20GetVariableProperty1() throws Exception {
		runTest("/2.0/good/xpath20-func/GetVariableProperty1-xp2.0.bpel");
	}

	@Test
	public void testXSDImport() throws Exception {
		runTest("/2.0/good/xsd-import/helloworld-Server.bpel");
	}

	@Test
	public void testCircularReference() throws Exception {
		runTest("/2.0/good/circularReference/CircularReference.bpel");
	}

	@Test
	public void testMultipleEmbeddedSchemas() throws Exception {
		runTest("/org/apache/ode/bpel/compiler/MultipleEmbeddedSchemas.bpel");
	}
}
