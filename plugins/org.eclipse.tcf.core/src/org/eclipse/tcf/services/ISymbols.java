/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
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
 * TCF symbols service interface.
 */
public interface ISymbols extends IService {

    /**
     * Service name.
     */
    static final String NAME = "Symbols";

    enum SymbolClass {
        unknown,                // unknown symbol class
        value,                  // constant value
        reference,              // variable data object
        function,               // function body
        type,                   // a type
        comp_unit,              // compilation unit
        block,                  // lexical block
        namespace               // C++ namespace
    }

    enum TypeClass {
        unknown,                // unknown type class
        cardinal,               // unsigned integer
        integer,                // signed integer
        real,                   // float, double
        pointer,                // pointer to anything.
        array,                  // array of anything.
        composite,              // struct, union, or class.
        enumeration,            // enumeration type.
        function,               // function type.
        member_pointer          // pointer to member type
    }

    static final int
        SYM_FLAG_PARAMETER      = 0x000001,
        SYM_FLAG_TYPEDEF        = 0x000002,
        SYM_FLAG_CONST_TYPE     = 0x000004,
        SYM_FLAG_PACKET_TYPE    = 0x000008,
        SYM_FLAG_SUBRANGE_TYPE  = 0x000010,
        SYM_FLAG_VOLATILE_TYPE  = 0x000020,
        SYM_FLAG_RESTRICT_TYPE  = 0x000040,
        SYM_FLAG_UNION_TYPE     = 0x000080,
        SYM_FLAG_CLASS_TYPE     = 0x000100,
        SYM_FLAG_INTERFACE_TYPE = 0x000200,
        SYM_FLAG_SHARED_TYPE    = 0x000400,
        SYM_FLAG_REFERENCE      = 0x000800,
        SYM_FLAG_BIG_ENDIAN     = 0x001000,
        SYM_FLAG_LITTLE_ENDIAN  = 0x002000,
        SYM_FLAG_OPTIONAL       = 0x004000,
        SYM_FLAG_EXTERNAL       = 0x008000,
        SYM_FLAG_VARARG         = 0x010000,
        SYM_FLAG_ARTIFICIAL     = 0x020000,
        SYM_FLAG_TYPE_PARAMETER = 0x040000,
        SYM_FLAG_PRIVATE        = 0x080000,
        SYM_FLAG_PROTECTED      = 0x100000,
        SYM_FLAG_PUBLIC         = 0x200000,
        SYM_FLAG_ENUM_TYPE      = 0x400000,
        SYM_FLAG_STRUCT_TYPE    = 0x800000;

    /**
     * Symbol context interface.
     */
    interface Symbol {
        /**
         * Get symbol ID.
         * @return symbol ID.
         */
        String getID();

        /**
         * Get symbol owner ID.
         * The owner can a thread or memory space (process).
         * Certain changes in owner state can invalidate cached symbol properties,
         * see getUpdatePolicy() and UPDATE_*.
         */
        String getOwnerID();

        /**
         * Get symbol properties update policy ID.
         * Symbol properties can change during program execution.
         * If a client wants to cache symbols, it should invalidate cached data
         * according to update policies of cached symbols.
         * @return symbol update policy ID, see UPDATE_*
         */
        int getUpdatePolicy();

        /**
         * Get symbol name.
         * @return symbol name or null.
         */
        String getName();

        /**
         * Get symbol class.
         * @return symbol class.
         */
        SymbolClass getSymbolClass();

        /**
         * Get symbol type class.
         * @return type class.
         */
        TypeClass getTypeClass();

        /**
         * Get type ID.
         * If the symbol is a type and not a 'typedef', return same as getID().
         * @return type ID.
         */
        String getTypeID();

        /**
         * Get base type ID.
         * If this symbol is a
         *   pointer type - return pointed type;
         *   array type - return element type;
         *   function type - return function result type;
         * otherwise return null.
         * @return type ID.
         */
        String getBaseTypeID();

        /**
         * Get index type ID.
         * If this symbol is a
         *   array type - return array index type;
         * otherwise return null.
         * @return type ID.
         */
        String getIndexTypeID();

        /**
         * Get container type ID.
         * If this symbol is a
         *   field or member - return containing class type;
         *   member pointer - return containing class type;
         * otherwise return null.
         * @return type ID.
         */
        String getContainerID();

        /**
         * Return value size of the symbol (or type).
         * @return size in bytes.
         */
        int getSize();

        /**
         * If symbol is an array type - return number of elements.
         * @return number of elements.
         */
        int getLength();

        /**
         * If symbol is an array type - return array index lower bound.
         * @return lower bound.
         */
        Number getLowerBound();

        /**
         * If symbol is an array type - return array index upper bound.
         * @return upper bound.
         */
        Number getUpperBound();

        /**
         * Return offset from 'this' for member of class, struct or union.
         * @return offset in bytes.
         */
        int getOffset();

        /**
         * Return address of the symbol.
         * @return address or null.
         */
        Number getAddress();

        /**
         * If symbol is a constant object, return its value.
         * @return symbol value as array of bytes.
         */
        byte[] getValue();

        /**
         * Get symbol values endianness.
         * @return true if symbol is big-endian.
         */
        boolean isBigEndian();

        /**
         * Return register ID if the symbol represents a register variable.
         * @return register ID or null.
         */
        String getRegisterID();

        /**
         * Return symbol flags, see SYM_FLAG_*.
         * @return bit set of symbol flags.
         */
        int getFlags();

        /**
         * Get value of the given flag.
         * @param flag - one of SYM_FLAG_*.
         * @return the flag value.
         */
        boolean getFlag(int flag);

        /**
         * Get complete map of context properties.
         * @return map of context properties.
         */
        Map<String,Object> getProperties();
    }

    /**
     * Symbol context property names.
     */
    static final String
        PROP_ID = "ID",
        PROP_OWNER_ID = "OwnerID",
        PROP_UPDATE_POLICY = "UpdatePolicy",
        PROP_NAME = "Name",
        PROP_SYMBOL_CLASS = "Class",
        PROP_TYPE_CLASS = "TypeClass",
        PROP_TYPE_ID = "TypeID",
        PROP_BASE_TYPE_ID = "BaseTypeID",
        PROP_INDEX_TYPE_ID = "IndexTypeID",
        PROP_CONTAINER_ID = "ContainerID",
        PROP_SIZE = "Size",
        PROP_LENGTH = "Length",
        PROP_LOWER_BOUND = "LowerBound",
        PROP_UPPER_BOUND = "UpperBound",
        PROP_OFFSET = "Offset",
        PROP_ADDRESS = "Address",
        PROP_VALUE = "Value",
        PROP_BIG_ENDIAN = "BigEndian",
        PROP_REGISTER = "Register",
        PROP_FLAGS = "Flags";

    /**
     * Symbol context properties update policies.
     */
    static final int
        /**
         * Update policy "Memory Map": symbol properties become invalid when
         * memory map changes - when modules are loaded or unloaded.
         * Symbol OwnerID indicates memory space (process) that is invalidation events source.
         * Most static variables and types have this update policy.
         */
        UPDATE_ON_MEMORY_MAP_CHANGES = 0,

        /**
         * Update policy "Execution State": symbol properties become invalid when
         * execution state changes - a thread is suspended, resumed or exited.
         * Symbol OwnerID indicates executable context (thread) that is invalidation events source.
         * Most stack (auto) variables have this update policy.
         */
        UPDATE_ON_EXE_STATE_CHANGES = 1;

    /**
     * Retrieve symbol context info for given symbol ID.
     * @see Symbol
     *
     * @param id – symbol context ID.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken getContext(String id, DoneGetContext done);

    /**
     * Client call back interface for getContext().
     */
    interface DoneGetContext {
        /**
         * Called when context data retrieval is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param context – context properties.
         */
        void doneGetContext(IToken token, Exception error, Symbol context);
    }

    /**
     * Retrieve children IDs for given parent ID.
     * Meaning of the operation depends on parent kind:
     * 1. struct, union, or class type - get fields;
     * 2. enumeration type - get enumerators;
     *
     * @param parent_context_id – parent symbol context ID.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken getChildren(String parent_context_id, DoneGetChildren done);

    /**
     * Client call back interface for getChildren().
     */
    interface DoneGetChildren {
        /**
         * Called when context list retrieval is done.
         * @param token - command handle
         * @param error – error description if operation failed, null if succeeded.
         * @param context_ids – array of available context IDs.
         */
        void doneGetChildren(IToken token, Exception error, String[] context_ids);
    }

    /**
     * Search symbol with given name in given context.
     * Return first symbol that matches.
     * The context can be memory space, process, thread or stack frame.
     *
     * @param context_id – a search scope.
     * @param ip - instruction pointer - ignored if context_id is a stack frame ID
     * @param name – symbol name.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken find(String context_id, Number ip, String name, DoneFind done);

    /**
     * Search symbol with given name in given context.
     * Return all symbol that matches, starting with current scope and going up to compilation unit global scope.
     * The context can be memory space, process, thread or stack frame.
     *
     * @param context_id – a search scope.
     * @param ip - instruction pointer - ignored if context_id is a stack frame ID
     * @param name – symbol name.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken findByName(String context_id, Number ip, String name, DoneFindAll done);

    /**
     * Search symbol with given address in given context.
     * The context can be memory space, process, thread or stack frame.
     *
     * @param context_id – a search scope.
     * @param addr – symbol address.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken findByAddr(String context_id, Number addr, DoneFind done);

    /**
     * Search symbol with given address in given context.
     * The context can be memory space, process, thread or stack frame.
     *
     * @param context_id – a search scope.
     * @param ip - instruction pointer - ignored if context_id is a stack frame ID
     * @param scope_id – a symbols ID of visibility scope.
     * @param name – symbol name.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken findInScope(String context_id, Number ip, String scope_id, String name, DoneFindAll done);

    /**
     * Client call back interface for find()and findByAddr().
     */
    interface DoneFind {
        /**
         * Called when symbol search is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param symbol_id - symbol ID.
         */
        void doneFind(IToken token, Exception error, String symbol_id);
    }

    /**
     * Client call back interface for findByName() and findInScope().
     */
    interface DoneFindAll {
        /**
         * Called when symbol search is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param symbol_id - symbol ID.
         */
        void doneFind(IToken token, Exception error, String[] symbol_ids);
    }

    /**
     * List all symbols in given context.
     * The context can be a stack frame.
     *
     * @param context_id – a scope.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken list(String context_id, DoneList done);

    /**
     * Client call back interface for list().
     */
    interface DoneList {
        /**
         * Called when symbol list retrieval is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param symbol_ids - array of symbol IDs.
         */
        void doneList(IToken token, Exception error, String[] symbol_ids);
    }

    /***********************************************************************************************/

    /**
     * Command codes that used to calculate frame pointer and register values during stack tracing.
     */
    static final int
        /** Load a number to the evaluation stack. Command argument is the number. */
        CMD_NUMBER      = 1,

        /** Load a register value to the evaluation stack. Command argument is the register ID. */
        CMD_RD_REG      = 2,

        /** Load frame address to the evaluation stack. */
        CMD_FP          = 3,

        /** Read memory at address on the top of the evaluation stack. Command arguments are
         *  the value size (Number) and endianness (Boolean, false - little-endian, true - big-endian). */
        CMD_RD_MEM      = 4,

        /** Integer arithmetic and bit-wise boolean operations */
        CMD_ADD         = 5,
        CMD_SUB         = 6,
        CMD_MUL         = 7,
        CMD_DIV         = 8,
        CMD_AND         = 9,
        CMD_OR          = 10,
        CMD_XOR         = 11,
        CMD_NEG         = 12,
        CMD_GE          = 13,
        CMD_GT          = 14,
        CMD_LE          = 15,
        CMD_LT          = 16,
        CMD_SHL         = 17,
        CMD_SHR         = 18,

        /** Load expression argument to evaluation stack. */
        CMD_ARG         = 19,

        /** Evaluate DWARF location expression. Command arguments are byte array of
         *  DWARF expression instructions and an object that contains evaluation parameters. */
        CMD_LOCATION    = 20,

        CMD_FCALL       = 21,
        CMD_WR_REG      = 22,
        CMD_WR_MEM      = 23,
        CMD_PIECE       = 24;

    /**
     * @deprecated
     */
    static final int
        CMD_REGISTER    = 2,
        CMD_DEREF       = 4;

    /**
     * Symbol location properties.
     */
    static final String
        /** Number, start address of code range where the location info is valid, or null if it is valid everywhere */
        LOC_CODE_ADDR = "CodeAddr",
        /** Number, size in bytes of code range where the location info is valid, or null if it is valid everywhere */
        LOC_CODE_SIZE = "CodeSize",
        /** Number, number of argument required to execute location instructions */
        LOC_ARG_CNT = "ArgCnt",
        /** List, instructions to compute object value location, e.g. address, or offset, or register ID, etc. */
        LOC_VALUE_CMDS = "ValueCmds",
        /** List, instructions to compute dynamic array length location */
        LOC_LENGTH_CMDS = "LengthCmds";

    /**
     * Retrieve symbol location information.
     * @param symbol_id - symbol ID.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken getLocationInfo(String symbol_id, DoneGetLocationInfo done);

    /**
     * Client call back interface for getLocationInfo().
     */
    interface DoneGetLocationInfo {
        /**
         * Called when location information retrieval is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param props - symbol location properties, see LOC_*.
         */
        void doneGetLocationInfo(IToken token, Exception error, Map<String,Object> props);
    }

    /**
     * Retrieve stack tracing commands for given instruction address in a context memory.
     * @param context_id - executable context ID.
     * @param address - instruction address.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken findFrameInfo(String context_id, Number address, DoneFindFrameInfo done);

    /**
     * Client call back interface for findFrameInfo().
     */
    interface DoneFindFrameInfo {
        /**
         * Called when stack tracing information retrieval is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param address - start of instruction address range
         * @param size - size of instruction address range
         * @param fp_cmds - commands to calculate stack frame pointer
         * @param reg_cmds - map register IDs -> commands to calculate register values
         */
        void doneFindFrameInfo(IToken token, Exception error,
                Number address, Number size,
                Object[] fp_cmds, Map<String,Object[]> reg_cmds);
    }

    /**
     * Get symbol file info for a module that contains given address in a memory space.
     * @param context_id - a memory space (process) ID.
     * @param address - an address in the memory space.
     * @param done - call back interface called when operation is completed.
     * @return - pending command handle.
     */
    IToken getSymFileInfo(String context_id, Number address, DoneGetSymFileInfo done);

    /**
     * Client call back interface for getSymFileInfo().
     */
    interface DoneGetSymFileInfo {
        /**
         * Called when symbol file information retrieval is done.
         * @param token - command handle.
         * @param error – error description if operation failed, null if succeeded.
         * @param props - symbol file properties.
         */
        void doneGetSymFileInfo(IToken token, Exception error, Map<String,Object> props);
    }
}
