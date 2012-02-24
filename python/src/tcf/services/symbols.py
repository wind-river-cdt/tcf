#******************************************************************************
# * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Wind River Systems - initial API and implementation
#******************************************************************************

from tcf import services

# Service name.
NAME = "Symbols"

class SymbolClass:
    unknown = 0                # unknown symbol class
    value = 1                  # constant value
    reference = 2              # variable data object
    function = 3               # function body
    type = 4                   # a type
    comp_unit = 5              # a compilation unit
    block = 6                  # a block of code
    namespace = 7              # a namespace

class TypeClass:
    unknown = 0                # unknown type class
    cardinal = 1               # unsigned integer
    integer = 2                # signed integer
    real = 3                   # float, double
    pointer = 4                # pointer to anything.
    array = 5                  # array of anything.
    composite = 6              # struct, union, or class.
    enumeration = 7            # enumeration type.
    function = 8               # function type.
    member_ptr = 9             # a pointer on member type

SYM_FLAG_PARAMETER = 0x000001
SYM_FLAG_TYPEDEF = 0x000002
SYM_FLAG_CONST_TYPE = 0x000004
SYM_FLAG_PACKET_TYPE = 0x000008
SYM_FLAG_SUBRANGE_TYPE = 0x000010
SYM_FLAG_VOLATILE_TYPE = 0x000020
SYM_FLAG_RESTRICT_TYPE = 0x000040
SYM_FLAG_UNION_TYPE = 0x000080
SYM_FLAG_CLASS_TYPE = 0x000100
SYM_FLAG_INTERFACE_TYPE = 0x000200
SYM_FLAG_SHARED_TYPE = 0x000400
SYM_FLAG_REFERENCE = 0x000800
SYM_FLAG_BIG_ENDIAN = 0x001000
SYM_FLAG_LITTLE_ENDIAN = 0x002000
SYM_FLAG_OPTIONAL = 0x004000
SYM_FLAG_EXTERNAL = 0x008000
SYM_FLAG_VARARG = 0x010000
SYM_FLAG_ARTIFICIAL = 0x020000
SYM_FLAG_TYPE_PARAMETER = 0x040000
SYM_FLAG_PRIVATE = 0x080000
SYM_FLAG_PROTECTED = 0x100000
SYM_FLAG_PUBLIC = 0x200000
SYM_FLAG_ENUM_TYPE = 0x400000
SYM_FLAG_STRUCT_TYPE = 0x800000

#
# Symbol context property names.
#
PROP_ID = "ID"
PROP_OWNER_ID = "OwnerID"
PROP_UPDATE_POLICY = "UpdatePolicy"
PROP_NAME = "Name"
PROP_SYMBOL_CLASS = "Class"
PROP_TYPE_CLASS = "TypeClass"
PROP_TYPE_ID = "TypeID"
PROP_BASE_TYPE_ID = "BaseTypeID"
PROP_INDEX_TYPE_ID = "IndexTypeID"
PROP_SIZE = "Size"
PROP_LENGTH = "Length"
PROP_LOWER_BOUND = "LowerBound"
PROP_UPPER_BOUND = "UpperBound"
PROP_OFFSET = "Offset"
PROP_ADDRESS = "Address"
PROP_VALUE = "Value"
PROP_BIG_ENDIAN = "BigEndian"
PROP_REGISTER = "Register"
PROP_FLAGS = "Flags"

#
# Symbol context properties update policies.
#

# Update policy "Memory Map": symbol properties become invalid when
# memory map changes - when modules are loaded or unloaded.
# Symbol OwnerID indicates memory space (process) that is invalidation events source.
# Most static variables and types have this update policy.
UPDATE_ON_MEMORY_MAP_CHANGES = 0

# Update policy "Execution State": symbol properties become invalid when
# execution state changes - a thread is suspended, resumed or exited.
# Symbol OwnerID indicates executable context (thread) that is invalidation events source.
# Most stack (auto) variables have this update policy.
UPDATE_ON_EXE_STATE_CHANGES = 1


class Symbol(object):
    """
    Symbol context interface.
    """
    def __init__(self, props):
        self._props = props or {}

    def __str__(self):
        return "[Symbol Context %s]" % self._props

    def getID(self):
        """
        Get symbol ID.
        @return symbol ID.
        """
        return self._props.get(PROP_ID)

    def getOwnerID(self):
        """
        Get symbol owner ID.
        The owner can a thread or memory space (process).
        Certain changes in owner state can invalidate cached symbol properties,
        see getUpdatePolicy() and UPDATE_*.
        """
        return self._props.get(PROP_OWNER_ID)

    def getUpdatePolicy(self):
        """
        Get symbol properties update policy ID.
        Symbol properties can change during program execution.
        If a client wants to cache symbols, it should invalidate cached data
        according to update policies of cached symbols.
        @return symbol update policy ID, see UPDATE_*
        """
        return self._props.get(PROP_UPDATE_POLICY)

    def getName(self):
        """
        Get symbol name.
        @return symbol name or None.
        """
        return self._props.get(PROP_NAME)

    def getSymbolClass(self):
        """
        Get symbol class.
        @return symbol class.
        """
        return self._props.get(PROP_SYMBOL_CLASS, SymbolClass.unknown)

    def getTypeClass(self):
        """
        Get symbol type class.
        @return type class.
        """
        return self._props.get(PROP_TYPE_CLASS, TypeClass.unknown)

    def getTypeID(self):
        """
        Get type ID.
        If the symbol is a type and not a 'typedef', return same as getID().
        @return type ID.
        """
        return self._props.get(PROP_TYPE_ID)

    def getBaseTypeID(self):
        """
        Get base type ID.
        If this symbol is a
          pointer type - return pointed type
          array type - return element type
          function type - return function result type
          class type - return base class
        otherwise return None.
        @return type ID.
        """
        return self._props.get(PROP_BASE_TYPE_ID)

    def getIndexTypeID(self):
        """
        Get index type ID.
        If this symbol is a
          array type - return array index type
        otherwise return None.
        @return type ID.
        """
        return self._props.get(PROP_INDEX_TYPE_ID)

    def getSize(self):
        """
        Return value size of the symbol (or type).
        @return size in bytes.
        """
        return self._props.get(PROP_SIZE, 0)

    def getLength(self):
        """
        If symbol is an array type - return number of elements.
        @return number of elements.
        """
        return self._props.get(PROP_LENGTH, 0)

    def getLowerBound(self):
        """
        If symbol is an array type - return array index lower bound.
        @return lower bound.
        """
        return self._props.get(PROP_LOWER_BOUND)

    def getUpperBound(self):
        """
        If symbol is an array type - return array index upper bound.
        @return upper bound.
        """
        return self._props.get(PROP_UPPER_BOUND)

    def getOffset(self):
        """
        Return offset from 'this' for member of class, struct or union.
        @return offset in bytes.
        """
        return self._props.get(PROP_OFFSET, 0)

    def getAddress(self):
        """
        Return address of the symbol.
        @return address or None.
        """
        return self._props.get(PROP_ADDRESS)

    def getValue(self):
        """
        If symbol is a constant object, return its value.
        @return symbol value as array of bytes.
        """
        return self._props.get(PROP_VALUE)

    def isBigEndian(self):
        """
        Get symbol values endianness.
        @return true if symbol is big-endian.
        """
        return self._props.get(PROP_BIG_ENDIAN, False)

    def getRegisterID(self):
        """
        Return register ID if the symbol represents a register variable.
        @return register ID or None.
        """
        return self._props.get(PROP_REGISTER)

    def getFlags(self):
        """
        Get symbol flags.
        @return Symbol flags (see SYM_FLAG_ values).
        """
        return self._props.get(PROP_FLAGS, 0)

    def getProperties(self):
        """
        Get complete map of context properties.
        @return map of context properties.
        """
        return self._props

class SymbolsService(services.Service):
    def getName(self):
        return NAME

    def getContext(self, id, done):
        """
        Retrieve symbol context info for given symbol ID.
        @see Symbol

        @param id - symbol context ID.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def getChildren(self, parent_context_id, done):
        """
        Retrieve children IDs for given parent ID.
        Meaning of the operation depends on parent kind:
        1. struct, union, or class type - get fields
        2. enumeration type - get enumerators

        @param parent_context_id - parent symbol context ID.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def find(self, context_id, ip, name, done):
        """
        Search first symbol with given name in given context.
        The context can be memory space, process, thread or stack frame.

        @param context_id - a search scope.
        @param ip - instruction pointer - ignored if context_id is a stack frame ID
        @param name - symbol name.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def findByName(self, context_id, ip, name, done):
        """
        Search symbols with given name in given context.
        The context can be memory space, process, thread or stack frame.

        @param context_id - a search scope.
        @param ip - instruction pointer - ignored if context_id is a stack frame ID
        @param name - symbol name.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def findByAddr(self, context_id, addr, done):
        """
        Search symbol with given address in given context.
        The context can be memory space, process, thread or stack frame.

        @param context_id - a search scope.
        @param addr - symbol address.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def findInScope(self, context_id, ip, scope_id, name, done):
        """
        Search symbols with given name in given context or in given symbol scope.
        The context can be memory space, process, thread or stack frame.
        The symbol scope can be any symbol.
        If ip is not null, the search of the symbol name is done first in the
        scope defined by @e context_id and @e ip.
        If the symbol is not found, the supplied scope is then used.

        @param context_id - a search scope.
        @param ip - instruction pointer. Can be null if only @e scope_id
                    should be used.
        @param scope_id - symbol context ID.
        @param name - symbol name.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def list(self, context_id, done):
        """
        List all symbols in given context.
        The context can be a stack frame.

        @param context_id - a scope.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def getArrayType(self, type_id, length, done):
        """
        Create an array type.

        @param type_id - symbol ID of the array cell type
        @param length - length of the array. A length of 0 creates a pointer
                        type.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

    def findFrameInfo(self, context_id, address, done):
        """
        Retrieve stack tracing commands for given instruction address in a context memory.
        @param context_id - exacutable context ID.
        @param address - instruction address.
        @param done - call back interface called when operation is completed.
        @return - pending command handle.
        """
        raise NotImplementedError("Abstract method")

class DoneGetContext(object):
    """
    Client call back interface for getContext().
    """
    def doneGetContext(self, token, error, context):
        """
        Called when context data retrieval is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        @param context - context properties.
        """
        pass

class DoneGetChildren(object):
    """
    Client call back interface for getChildren().
    """
    def doneGetChildren(self, token, error, context_ids):
        """
        Called when context list retrieval is done.
        @param token - command handle
        @param error - error description if operation failed, None if succeeded.
        @param context_ids - array of available context IDs.
        """
        pass

class DoneFind(object):
    """
    Client call back interface for find(), findByName() and findInScope().
    """
    def doneFind(self, token, error, symbol_ids):
        """
        Called when symbol search is done.
        @param token - command handle.
        @param error - error description if operation failed, None if succeeded.
        @param symbol_ids - list of symbol ID.
        """
        pass

class DoneList(object):
    """
    Client call back interface for list().
    """
    def doneList(self, token, error, symbol_ids):
        """
        Called when symbol list retrieval is done.
        @param token - command handle.
        @param error - error description if operation failed, None if succeeded.
        @param symbol_ids - array of symbol IDs.
        """

class DoneGetArrayType(object):
    """
    Client call back interface for getArrayType().
    """
    def doneGetArrayType(self, token, error, type_id):
        """
        Called when array type creation is done.
        @param token - command handle.
        @param error - error description if operation failed, None if succeeded.
        @param type_id - array type symbol ID 
        """


#
# Command codes that are used to calculate frame pointer and register values during stack tracing.
#

# Load a number to the evaluation stack. Command argument is the number.
CMD_NUMBER      = 1

# Load a register value to the evaluation stack. Command argument is the register ID.
CMD_REGISTER    = 2

# Load frame address to the evaluation stack.
CMD_FP          = 3

# Read memory at address on the top of the evaluation stack. Command arguments are
# the value size (Number) and endianness (Boolean, false - little-endian, true - big-endian).
CMD_DEREF       = 4

# Add two values on top of the evaluation stack
CMD_ADD         = 5

class DoneFindFrameInfo(object):
    """
    Client call back interface for findFrameInfo().
    """
    def doneFindFrameInfo(self, token, error, address, size, fp_cmds, reg_cmds):
        """
        Called when stack tracing information retrieval is done.
        @param token - command handle.
        @param error - error description if operation failed, None if succeeded.
        @param address - start of instruction address range
        @param size - size of instruction address range
        @param fp_cmds - commands to calculate stack frame pointer
        @param reg_cmds - map register IDs -> commands to calculate register values
        """
        pass
