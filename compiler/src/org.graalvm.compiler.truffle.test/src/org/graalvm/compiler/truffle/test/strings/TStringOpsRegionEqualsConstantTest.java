/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.compiler.truffle.test.strings;

import java.util.List;
import java.util.stream.Collectors;

import org.graalvm.compiler.core.common.CompilationIdentifier;
import org.graalvm.compiler.core.common.GraalOptions;
import org.graalvm.compiler.nodes.FixedNode;
import org.graalvm.compiler.nodes.ReturnNode;
import org.graalvm.compiler.nodes.StartNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.replacements.ConstantBindingParameterPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.ResolvedJavaMethod;

@RunWith(Parameterized.class)
public class TStringOpsRegionEqualsConstantTest extends TStringOpsRegionEqualsTest {

    final Object[] constantArgs;

    public TStringOpsRegionEqualsConstantTest(
                    byte[] arrayA, int offsetA, int lengthA, int strideA, int fromIndexA,
                    byte[] arrayB, int offsetB, int lengthB, int strideB, int fromIndexB, int lengthCMP) {
        super(arrayA, offsetA, lengthA, strideA, fromIndexA, arrayB, offsetB, lengthB, strideB, fromIndexB, lengthCMP);
        constantArgs = new Object[]{DUMMY_LOCATION,
                        arrayA, offsetA, lengthA, strideA, fromIndexA,
                        arrayB, offsetB, lengthB, strideB, fromIndexB, JavaConstant.NULL_POINTER, lengthCMP};
    }

    @Parameters(name = "{index}: offset: {1}, {6}, stride: {3}, {8}, length: {12}")
    public static List<Object[]> data() {
        return TStringOpsRegionEqualsTest.data().stream().filter(args -> {
            int length = (int) args[10];
            // this test takes much longer than TStringOpsRegionEqualsTest, reduce number of test
            // cases
            return length == 0 || length == 1 || length == 7 || length == 16;
        }).collect(Collectors.toList());
    }

    @Override
    protected GraphBuilderConfiguration editGraphBuilderConfiguration(GraphBuilderConfiguration conf) {
        ConstantBindingParameterPlugin constantBinding = new ConstantBindingParameterPlugin(constantArgs, this.getMetaAccess(), this.getSnippetReflection());
        conf.getPlugins().appendParameterPlugin(constantBinding);
        return super.editGraphBuilderConfiguration(conf);
    }

    @Override
    protected StructuredGraph parseForCompile(ResolvedJavaMethod method, CompilationIdentifier compilationId, OptionValues options) {
        return makeAllArraysStable(super.parseForCompile(method, compilationId, options));
    }

    @Override
    protected InstalledCode getCode(final ResolvedJavaMethod installedCodeOwner, StructuredGraph graph, boolean ignoreForceCompile, boolean ignoreInstallAsDefault, OptionValues options) {
        return super.getCode(installedCodeOwner, graph, true, false, options);
    }

    @Override
    @Test
    public void testRegionEquals() throws ClassNotFoundException {
        ResolvedJavaMethod method = getTStringOpsMethod("regionEqualsWithOrMaskWithStrideIntl",
                        Object.class, int.class, int.class, int.class, int.class,
                        Object.class, int.class, int.class, int.class, int.class, byte[].class, int.class);
        test(method, null, DUMMY_LOCATION,
                        arrayA, offsetA, lengthA, strideA, fromIndexA,
                        arrayB, offsetB, lengthB, strideB, fromIndexB, null, lengthCMP);
    }

    @Override
    protected void checkLowTierGraph(StructuredGraph graph) {
        if (getTarget().arch instanceof AMD64) {
            if ((lengthCMP << Math.max(strideA, strideB)) < GraalOptions.ArrayRegionEqualsConstantLimit.getValue(graph.getOptions())) {
                StartNode start = graph.start();
                FixedNode next = start.next();
                assertTrue(next instanceof ReturnNode);
                assertTrue(((ReturnNode) next).result().isConstant());
            }
        }
    }
}
