/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.pointsto.meta;

import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.ResolvedJavaType;
import org.graalvm.compiler.graph.Node;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReachabilityAnalysisType extends AnalysisType {

    private final Set<AnalysisType> instantiatedSubtypes = ConcurrentHashMap.newKeySet();
    private final Set<AnalysisMethod> invokedMethods = ConcurrentHashMap.newKeySet();

    public ReachabilityAnalysisType(AnalysisUniverse universe, ResolvedJavaType javaType, JavaKind storageKind, AnalysisType objectType, AnalysisType cloneableType) {
        super(universe, javaType, storageKind, objectType, cloneableType);
    }

    @Override
    public boolean registerAsInHeap() {
        if (super.registerAsInHeap()) {
            registerAsInstantiated();
            return true;
        }
        return false;
    }

    @Override
    public boolean registerAsAllocated(Node node) {
        if (super.registerAsAllocated(node)) {
            registerAsInstantiated();
            return true;
        }
        return false;
    }

    /** Register the type as instantiated with all its super types. */
    private void registerAsInstantiated() {
        forAllSuperTypes(t -> ((ReachabilityAnalysisType) t).instantiatedSubtypes.add(this));
    }

    public Set<AnalysisType> getInstantiatedSubtypes() {
        return instantiatedSubtypes;
    }

    public Set<AnalysisMethod> getInvokedMethods() {
        return invokedMethods;
    }
}
