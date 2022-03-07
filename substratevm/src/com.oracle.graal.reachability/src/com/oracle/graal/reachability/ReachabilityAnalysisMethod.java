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
package com.oracle.graal.reachability;

import com.oracle.graal.pointsto.meta.AnalysisMethod;
import com.oracle.graal.pointsto.meta.AnalysisUniverse;
import com.oracle.graal.pointsto.meta.InvokeInfo;
import com.oracle.graal.pointsto.meta.ReachabilityAnalysisType;
import jdk.vm.ci.code.BytecodePosition;
import jdk.vm.ci.meta.ResolvedJavaMethod;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReachabilityAnalysisMethod extends AnalysisMethod {

    private final List<InvokeInfo> invokeInfos = Collections.synchronizedList(new ArrayList<>());

    public ReachabilityAnalysisMethod(AnalysisUniverse universe, ResolvedJavaMethod wrapped) {
        super(universe, wrapped);
        registerSignatureTypes();
    }

    private void registerSignatureTypes() {
        // todo this is a copy from MethodFlowsGraphs, line 93
        boolean isStatic = Modifier.isStatic(getModifiers());
        int parameterCount = getSignature().getParameterCount(!isStatic);
        // lookup the parameters type so that they are added to the universe even if the method is
        // never linked and parsed
        int offset = isStatic ? 0 : 1;
        for (int i = offset; i < parameterCount; i++) {
            getSignature().getParameterType(i - offset, getDeclaringClass());
        }

        // lookup the return type so that it is added to the universe even if the method is
        // never linked and parsed
        getSignature().getReturnType(getDeclaringClass());
    }

    @Override
    public void startTrackInvocations() {
    }

    @Override
    public Collection<InvokeInfo> getInvokes() {
        return invokeInfos;
    }

    @Override
    public StackTraceElement[] getParsingContext() {
        // todo get context
        return new StackTraceElement[0];
    }

    @Override
    public List<BytecodePosition> getInvokeLocations() {
        // todo resolve
        return new ArrayList<>();
    }

    public void addInvokes(List<InvokeInfo> invokes) {
        this.invokeInfos.addAll(invokes);
    }

    public Collection<AnalysisMethod> collectAllImplementations() {
        return getDeclaringClass().getInstantiatedSubtypes()
                        .stream()
                        .map(type -> type.resolveConcreteMethod(this))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
    }

    @Override
    public boolean registerAsInvoked() {
        if (super.registerAsInvoked()) {
            getDeclaringClass().getInvokedMethods().add(this);
            return true;
        }
        return false;
    }

    @Override
    public ReachabilityAnalysisType getDeclaringClass() {
        return ((ReachabilityAnalysisType) super.getDeclaringClass());
    }
}

