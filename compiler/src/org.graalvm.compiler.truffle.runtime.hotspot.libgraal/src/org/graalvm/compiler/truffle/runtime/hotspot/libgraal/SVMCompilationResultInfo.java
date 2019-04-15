/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.truffle.runtime.hotspot.libgraal;

import static org.graalvm.libgraal.LibGraalScope.getIsolateThread;

import org.graalvm.compiler.truffle.common.TruffleCompilerListener;

/**
 * Encapsulates a handle to a {@code CompilationResultInfo} object in the SVM heap.
 */
final class SVMCompilationResultInfo extends SVMObject implements TruffleCompilerListener.CompilationResultInfo {

    SVMCompilationResultInfo(long handle) {
        super(handle);
    }

    @Override
    public int getTargetCodeSize() {
        return HotSpotToSVMCalls.getTargetCodeSize(getIsolateThread(), handle);
    }

    @Override
    public int getTotalFrameSize() {
        return HotSpotToSVMCalls.getTotalFrameSize(getIsolateThread(), handle);
    }

    @Override
    public int getExceptionHandlersCount() {
        return HotSpotToSVMCalls.getExceptionHandlersCount(getIsolateThread(), handle);
    }

    @Override
    public int getInfopointsCount() {
        return HotSpotToSVMCalls.getInfopointsCount(getIsolateThread(), handle);
    }

    @Override
    public String[] getInfopoints() {
        return HotSpotToSVMCalls.getInfopoints(getIsolateThread(), handle);
    }

    @Override
    public int getMarksCount() {
        return HotSpotToSVMCalls.getMarksCount(getIsolateThread(), handle);
    }

    @Override
    public int getDataPatchesCount() {
        return HotSpotToSVMCalls.getDataPatchesCount(getIsolateThread(), handle);
    }
}
