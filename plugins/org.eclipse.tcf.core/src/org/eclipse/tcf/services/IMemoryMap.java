/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.services;

import java.util.Map;

import org.eclipse.tcf.protocol.IService;
import org.eclipse.tcf.protocol.IToken;

/**
 * IMemoryMap service provides information about executable modules (files) mapped (loaded) into target memory.
 */
public interface IMemoryMap extends IService {

    static final String NAME = "MemoryMap";

    /**
     * Memory region property names.
     */
    static final String
        /** String, memory region ID */
        PROP_ID = "ID",

        /** String, memory region context query, see IContextQuery */
        PROP_CONTEXT_QUERY = "ContextQuery",

        /** Number, region address in memory */
        PROP_ADDRESS = "Addr",

        /** Number, region size */
        PROP_SIZE = "Size",

        /** Number, region offset in the file */
        PROP_OFFSET = "Offs",

        /** Boolean, true if the region represents BSS */
        PROP_BSS = "BSS",

        /** Number, region memory protection flags, see FLAG_* */
        PROP_FLAGS = "Flags",

        /** String, name of the file */
        PROP_FILE_NAME = "FileName",

        /** String, name of the object file section */
        PROP_SECTION_NAME = "SectionName";

    /**
     * Memory region flags.
     */
    static final int
        /** Read access is allowed */
        FLAG_READ = 1,

        /** Write access is allowed */
        FLAG_WRITE = 2,

        /** Instruction fetch access is allowed */
        FLAG_EXECUTE = 4;

    /**
     * Memory region interface.
     */
    interface MemoryRegion {

        /**
         * Get region properties. See PROP_* definitions for property names.
         * Properties are read only, clients should not try to modify them.
         * @return Map of region properties.
         */
        Map<String,Object> getProperties();

        /**
         * Get memory region address.
         * Same as getProperties().get(PROP_ADDRESS)
         * @return region address.
         */
        Number getAddress();

        /**
         * Get memory region size.
         * Same as getProperties().get(PROP_SIZE)
         * @return region size.
         */
        Number getSize();

        /**
         * Get memory region file offset.
         * Same as getProperties().get(PROP_OFFSET)
         * @return file offset.
         */
        Number getOffset();

        /**
         * Check if the region represents BSS - data segment containing
         * statically-allocated variables represented solely by zero-valued bits initially.
         * Memory for BSS segments is not backed by a file contents.
         * Same as getProperties().get(PROP_BSS)
         * @return file offset.
         */
        boolean isBSS();

        /**
         * Get memory region flags.
         * Same as getProperties().get(PROP_FLAGS)
         * @return region flags.
         */
        int getFlags();

        /**
         * Get memory region file name.
         * Same as getProperties().get(PROP_FILE_NAME)
         * @return file name.
         */
        String getFileName();

        /**
         * Get memory region section name.
         * Same as getProperties().get(PROP_SECTION_NAME)
         * @return section name.
         */
        String getSectionName();

        /**
         * Get context query that defines scope of the region, see also IContextQuery.
         * Same as getProperties().get(PROP_CONTEXT_QUERY)
         * Only user-defined regions can have a context query property.
         * @return context query expression, or null.
         */
        String getContextQuery();
    }

    /**
     * Retrieve memory map for given context ID.
     *
     * @param id – context ID.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken get(String id, DoneGet done);

    /**
     * Client call back interface for get().
     */
    interface DoneGet {
        /**
         * Called when memory map data retrieval is done.
         * @param error – error description if operation failed, null if succeeded.
         * @param map – memory map data.
         */
        void doneGet(IToken token, Exception error, MemoryRegion[] map);
    }

    /**
     * Set memory map for given context.
     * 'id' can be null, in such case scope of each memory region is
     * defined by its ContextQuery property.
     *
     * Using non-null 'id' is deprecated - use ContextQuery instead.
     *
     * @param id – symbols context group ID or name.
     * @param map – memory map data.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken set(String id, MemoryRegion[] map, DoneSet done);

    /**
     * Client call back interface for set().
     */
    interface DoneSet {
        /**
         * Called when memory map set command is done.
         * @param error – error description if operation failed, null if succeeded.
         */
        void doneSet(IToken token, Exception error);
    }

    /**
     * Add memory map event listener.
     * @param listener - memory map event listener to add.
     */
    void addListener(MemoryMapListener listener);

    /**
     * Remove memory map event listener.
     * @param listener - memory map event listener to remove.
     */
    void removeListener(MemoryMapListener listener);

    /**
     * Service events listener interface.
     */
    interface MemoryMapListener {

        /**
         * Called when context memory map changes.
         * @param context_id - context ID.
         */
        void changed(String context_id);
    }
}
