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
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext.MemoryRegion;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.services.IMemoryMap;
import org.eclipse.tcf.util.TCFDataCache;

/**
 * A node representing a memory region (module).
 */
public class TCFNodeModule extends TCFNode implements IDetailsProvider {

    private final TCFData<MemoryRegion> region;

    private static final RGB
        rgb_error = new RGB(192, 0, 0);

    protected TCFNodeModule(final TCFNodeExecContext parent, String id, final int index) {
        super(parent, id);
        region = new TCFData<MemoryRegion>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                TCFDataCache<MemoryRegion[]> map_cache = parent.getMemoryMap();
                if (!map_cache.validate(this)) return false;
                Throwable error = map_cache.getError();
                MemoryRegion[] map_data = map_cache.getData();
                MemoryRegion region = null;
                if (map_data != null && index < map_data.length) region = map_data[index];
                set(null, error, region);
                return true;
            }
        };
    }

    public TCFDataCache<MemoryRegion> getRegion() {
        return region;
    }

    void onMemoryMapChanged() {
        region.reset();
    }

    @Override
    protected boolean getData(ILabelUpdate update, Runnable done) {
        if (!region.validate(done)) return false;
        MemoryRegion mr = region.getData();
        IMemoryMap.MemoryRegion r = mr != null ? mr.region : null;
        if (r == null) {
            update.setLabel("...", 0);
        }
        else {
            String[] col_ids = update.getColumnIds();
            if (col_ids == null) {
                update.setLabel(r.getFileName(), 0);
            }
            else {
                for (int i=0; i < col_ids.length; ++i) {
                    String col_id = col_ids[i];
                    if (TCFColumnPresentationModules.COL_NAME.equals(col_id)) {
                        update.setLabel(r.getFileName(), i);
                    }
                    else if (TCFColumnPresentationModules.COL_ADDRESS.equals(col_id)) {
                        update.setLabel(toHexString(r.getAddress()), i);
                    }
                    else if (TCFColumnPresentationModules.COL_SIZE.equals(col_id)) {
                        update.setLabel(toHexString(r.getSize()), i);
                    }
                    else if (TCFColumnPresentationModules.COL_FLAGS.equals(col_id)) {
                        update.setLabel(getFlagsLabel(r.getFlags()), i);
                    }
                    else if (TCFColumnPresentationModules.COL_OFFSET.equals(col_id)) {
                        update.setLabel(toHexString(r.getOffset()), i);
                    }
                    else if (TCFColumnPresentationModules.COL_SECTION.equals(col_id)) {
                        String sectionName = r.getSectionName();
                        update.setLabel(sectionName != null ? sectionName : "", i);
                    }
                }
            }
        }
        update.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_MEMORY_MAP), 0);
        return true;
    }

    public boolean getDetailText(StyledStringBuffer bf, Runnable done) {
        if (!region.validate(done)) return false;
        MemoryRegion mr = region.getData();
        IMemoryMap.MemoryRegion r = mr != null ? mr.region : null;
        if (r == null) return true;
        String file_name = r.getFileName();
        if (file_name != null) {
            bf.append("File name: ", SWT.BOLD).append(file_name).append('\n');
            TCFNodeExecContext exe = (TCFNodeExecContext)parent;
            TCFDataCache<TCFSymFileRef> sym_cache = exe.getSymFileInfo(JSON.toBigInteger(r.getAddress()));
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
            String section = r.getSectionName();
            if (section != null) bf.append("File section: ", SWT.BOLD).append(section).append('\n');
            else bf.append("File offset: ", SWT.BOLD).append(toHexString(r.getOffset())).append('\n');
        }
        bf.append("Address: ", SWT.BOLD).append(toHexString(r.getAddress())).append('\n');
        bf.append("Size: ", SWT.BOLD).append(toHexString(r.getSize())).append('\n');
        bf.append("Flags: ", SWT.BOLD).append(getFlagsLabel(r.getFlags())).append('\n');
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
