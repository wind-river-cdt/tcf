/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

#include <backend/backend.h>

static RegisterDefinition * get_reg_by_dwarf_id(unsigned id) {
    static RegisterDefinition ** map = NULL;
    static unsigned map_length = 0;

    if (map == NULL) {
        RegisterDefinition * r;
        RegisterDefinition * regs_index = get_reg_definitions(NULL);
        for (r = regs_index; r->name != NULL; r++) {
            if (r->dwarf_id >= (int)map_length) map_length = r->dwarf_id + 1;
        }
        map = (RegisterDefinition **)loc_alloc_zero(sizeof(RegisterDefinition *) * map_length);
        for (r = regs_index; r->name != NULL; r++) {
            if (r->dwarf_id >= 0) map[r->dwarf_id] = r;
        }
    }
    return id < map_length ? map[id] : NULL;
}

static RegisterDefinition * get_reg_by_eh_frame_id(unsigned id) {
    static RegisterDefinition ** map = NULL;
    static unsigned map_length = 0;

    if (map == NULL) {
        RegisterDefinition * r;
        RegisterDefinition * regs_index = get_reg_definitions(NULL);
        for (r = regs_index; r->name != NULL; r++) {
            if (r->eh_frame_id >= (int)map_length) map_length = r->eh_frame_id + 1;
        }
        map = (RegisterDefinition **)loc_alloc_zero(sizeof(RegisterDefinition *) * map_length);
        for (r = regs_index; r->name != NULL; r++) {
            if (r->eh_frame_id >= 0) map[r->eh_frame_id] = r;
        }
    }
    return id < map_length ? map[id] : NULL;
}

RegisterDefinition * get_reg_by_id(Context * ctx, unsigned id, RegisterIdScope * scope) {
    switch (scope->id_type) {
    case REGNUM_DWARF: return get_reg_by_dwarf_id(id);
    case REGNUM_EH_FRAME: return get_reg_by_eh_frame_id(id);
    }
    return NULL;
}

int read_reg_bytes(StackFrame * frame, RegisterDefinition * reg_def, unsigned offs, unsigned size, uint8_t * buf) {
    if (reg_def != NULL && frame != NULL) {
        if (frame->is_top_frame) {
            return context_read_reg(frame->ctx, reg_def, offs, size, buf);
        }
        if (frame->regs != NULL) {
            size_t i;
            uint8_t * r_addr = (uint8_t *)&frame->regs->data + reg_def->offset;
            uint8_t * m_addr = (uint8_t *)&frame->regs->mask + reg_def->offset;
            for (i = 0; i < size; i++) {
                if (m_addr[offs + i] != 0xff) {
                    errno = ERR_INV_CONTEXT;
                    return -1;
                }
            }
            if (offs + size > reg_def->size) {
                errno = ERR_INV_DATA_SIZE;
                return -1;
            }
            memcpy(buf, r_addr + offs, size);
            return 0;
        }
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int write_reg_bytes(StackFrame * frame, RegisterDefinition * reg_def, unsigned offs, unsigned size, uint8_t * buf) {
    if (reg_def != NULL && frame != NULL) {
        if (frame->is_top_frame) {
            return context_write_reg(frame->ctx, reg_def, offs, size, buf);
        }
        if (frame->regs == NULL && context_has_state(frame->ctx)) {
            frame->regs = (RegisterData *)loc_alloc_zero(sizeof(RegisterData));
        }
        if (frame->regs != NULL) {
            uint8_t * r_addr = (uint8_t *)&frame->regs->data + reg_def->offset;
            uint8_t * m_addr = (uint8_t *)&frame->regs->mask + reg_def->offset;

            if (offs + size > reg_def->size) {
                errno = ERR_INV_DATA_SIZE;
                return -1;
            }
            memcpy(r_addr + offs, buf, size);
            memset(m_addr + offs, 0xff, size);
            return 0;
        }
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}
