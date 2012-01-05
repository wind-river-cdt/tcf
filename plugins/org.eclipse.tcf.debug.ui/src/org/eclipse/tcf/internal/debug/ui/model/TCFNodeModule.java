/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tcf.core.ErrorReport;
import org.eclipse.tcf.internal.debug.model.TCFSymFileRef;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.services.IMemoryMap;
import org.eclipse.tcf.util.TCFDataCache;

/**
 * A node representing a memory region (module).
 */
public class TCFNodeModule extends TCFNode implements IDetailsProvider {

    private IMemoryMap.MemoryRegion region;

    private static final RGB
        rgb_error = new RGB(192, 0, 0);

    protected TCFNodeModule(TCFNode parent, String id) {
        super(parent, id);
    }

    void setRegion(IMemoryMap.MemoryRegion region) {
        this.region = region;
    }

    @Override
    protected boolean getData(ILabelUpdate update, Runnable done) {
        String[] col_ids = update.getColumnIds();
        if (col_ids == null) {
            update.setLabel(region.getFileName(), 0);
        }
        else {
            for (int i=0; i < col_ids.length; ++i) {
                String col_id = col_ids[i];
                if (TCFColumnPresentationModules.COL_NAME.equals(col_id)) {
                    update.setLabel(region.getFileName(), i);
                }
                else if (TCFColumnPresentationModules.COL_ADDRESS.equals(col_id)) {
                    update.setLabel(toHexString(region.getAddress()), i);
                }
                else if (TCFColumnPresentationModules.COL_SIZE.equals(col_id)) {
                    update.setLabel(toHexString(region.getSize()), i);
                }
                else if (TCFColumnPresentationModules.COL_FLAGS.equals(col_id)) {
                    update.setLabel(getFlagsLabel(region.getFlags()), i);
                }
                else if (TCFColumnPresentationModules.COL_OFFSET.equals(col_id)) {
                    update.setLabel(toHexString(region.getOffset()), i);
                }
                else if (TCFColumnPresentationModules.COL_SECTION.equals(col_id)) {
                    String sectionName = region.getSectionName();
                    update.setLabel(sectionName != null ? sectionName : "", i);
                }
            }
        }
        update.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_MEMORY_MAP), 0);
        return true;
    }

    public boolean getDetailText(StyledStringBuffer bf, Runnable done) {
        String file_name = region.getFileName();
        if (file_name != null) {
            bf.append("File name: ", SWT.BOLD).append(file_name).append('\n');
            TCFNodeExecContext exe = (TCFNodeExecContext)parent;
            TCFDataCache<TCFSymFileRef> sym_cache = exe.getSymFileInfo(JSON.toBigInteger(region.getAddress()));
            if (sym_cache != null) {
                if (!sym_cache.validate(done)) return false;
                TCFSymFileRef sym_data = sym_cache.getData();
                if (sym_data != null) {
                    if (sym_data.props != null) {
                        String sym_file_name = (String)sym_data.props.get("FileName");
                        if (sym_file_name != null) bf.append("Symbol file name: ", SWT.BOLD).append(sym_file_name).append('\n');
                        @SuppressWarnings("unchecked")
                        Map<String,Object> map = (Map<String,Object>)sym_data.props.get("FileError");
                        if (map != null) {
                            String msg = TCFModel.getErrorMessage(new ErrorReport("", map), false);
                            bf.append("Symbol file error: ", SWT.BOLD).append(msg, SWT.ITALIC, null, rgb_error).append('\n');
                        }
                    }
                    if (sym_data.error != null) bf.append("Symbol file error: ", SWT.BOLD).append(
                            TCFModel.getErrorMessage(sym_data.error, false),
                            SWT.ITALIC, null, rgb_error).append('\n');
                }
            }
            String section = region.getSectionName();
            if (section != null) bf.append("File section: ", SWT.BOLD).append(section).append('\n');
            else bf.append("File offset: ", SWT.BOLD).append(toHexString(region.getOffset())).append('\n');
        }
        bf.append("Address: ", SWT.BOLD).append(toHexString(region.getAddress())).append('\n');
        bf.append("Size: ", SWT.BOLD).append(toHexString(region.getSize())).append('\n');
        bf.append("Flags: ", SWT.BOLD).append(getFlagsLabel(region.getFlags())).append('\n');
        return true;
    }

    private String toHexString(Number address) {
        if (address == null) return "";
        BigInteger addr = JSON.toBigInteger(address);
        String s = addr.toString(16);
        int sz = s.length() <= 8 ? 8 : 16;
        int l = sz - s.length();
        if (l < 0) l = 0;
        if (l > 16) l = 16;
        return "0x0000000000000000".substring(0, 2 + l) + s;
    }

    private String getFlagsLabel(int flags) {
        StringBuilder flagsLabel = new StringBuilder(3);
        if ((flags & IMemoryMap.FLAG_READ) != 0) flagsLabel.append('r');
        else flagsLabel.append('-');
        if ((flags & IMemoryMap.FLAG_WRITE) != 0) flagsLabel.append('w');
        else flagsLabel.append('-');
        if ((flags & IMemoryMap.FLAG_EXECUTE) != 0) flagsLabel.append('x');
        else flagsLabel.append('-');
        return flagsLabel.toString();
    }
}
