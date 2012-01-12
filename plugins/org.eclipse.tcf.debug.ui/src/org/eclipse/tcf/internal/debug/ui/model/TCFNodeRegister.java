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
package org.eclipse.tcf.internal.debug.ui.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.internal.debug.model.TCFContextState;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IRegisters;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.tcf.util.TCFTask;


public class TCFNodeRegister extends TCFNode implements IElementEditor, IWatchInExpressions, IDetailsProvider {

    private final TCFChildrenRegisters children;
    private final TCFData<IRegisters.RegistersContext> context;
    private final TCFData<String> expression_text;
    private final TCFData<byte[]> value;
    private final boolean is_stack_frame_register;

    private byte[] prev_value;
    private byte[] next_value;

    private static final RGB
        rgb_error = new RGB(192, 0, 0),
        rgb_highlight = new RGB(255, 255, 128);

    private int index;
    
    private Vector<VectorRegister> vectorList;

    public TCFNodeRegister(TCFNode parent, final String id) {
        super(parent, id);
        vectorList =  new Vector<VectorRegister>();
        if (parent instanceof TCFNodeStackFrame) is_stack_frame_register = true;
        else if (parent instanceof TCFNodeRegister) is_stack_frame_register = ((TCFNodeRegister)parent).is_stack_frame_register;
        else is_stack_frame_register = false;
        children = new TCFChildrenRegisters(this);
        context = new TCFData<IRegisters.RegistersContext>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                IRegisters regs = launch.getService(IRegisters.class);
                command = regs.getContext(id, new IRegisters.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IRegisters.RegistersContext context) {
                        set(token, error, context);
                    }
                });
                return false;
            }
        };
        expression_text = new TCFData<String>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                Throwable err = null;
                TCFNodeRegister n = TCFNodeRegister.this;
                ArrayList<String> names = new ArrayList<String>();
                for (;;) {
                    if (!n.context.validate(this)) return false;
                    IRegisters.RegistersContext ctx = n.context.getData();
                    if (ctx == null) {
                        err = n.context.getError();
                        break;
                    }
                    String s = ctx.getName();
                    if (s == null) break;
                    names.add(s);
                    if (!(n.parent instanceof TCFNodeRegister)) break;
                    n = (TCFNodeRegister)n.parent;
                }
                if (names.size() == 0 || err != null) {
                    set(null, err, null);
                }
                else {
                    StringBuffer bf = new StringBuffer();
                    boolean first = true;
                    int m = names.size();
                    while (m > 0) {
                        String s = names.get(--m);
                        boolean need_quotes = false;
                        int l = s.length();
                        for (int i = 0; i < l; i++) {
                            char ch = s.charAt(i);
                            if (ch >= 'A' && ch <= 'Z') continue;
                            if (ch >= 'a' && ch <= 'z') continue;
                            if (ch >= '0' && ch <= '9') continue;
                            need_quotes = true;
                            break;
                        }
                        if (!first) bf.append('.');
                        if (need_quotes) bf.append("$\"");
                        if (first) bf.append('$');
                        bf.append(s);
                        if (need_quotes) bf.append('"');
                        first = false;
                    }
                    set(null, null, bf.toString());
                }
                return true;
            }
        };
        value = new TCFData<byte[]>(channel) {
            @Override
            protected boolean startDataRetrieval() {
                Boolean b = usePrevValue(this);
                if (b == null) return false;
                if (b) {
                    set(null, null, prev_value);
                    return true;
                }
                if (!context.validate(this)) return false;
                IRegisters.RegistersContext ctx = context.getData();
                if (ctx == null || ctx.getSize() <= 0) {
                    set(null, null, null);
                    return true;
                }
                command = ctx.get(new IRegisters.DoneGet() {
                    public void doneGet(IToken token, Exception error, byte[] value) {
                        if (error != null) {
                            Boolean b = usePrevValue(null);
                            if (b != null && b) {
                                set(token, null, prev_value);
                                return;
                            }
                        }
                        set(token, error, value);
                    }
                });
                return false;
            }
        };
    }

    public TCFDataCache<IRegisters.RegistersContext> getContext() {
        return context;
    }

    public TCFDataCache<byte[]> getValue() {
        return value;
    }

    public TCFChildren getChildren() {
        return children;
    }

    public TCFDataCache<String> getExpressionText() {
        return expression_text;
    }

    void setIndex(int index) {
        this.index = index;
    }

    private Boolean usePrevValue(Runnable done) {
        // Check if view should show old value.
        // Old value is shown if context is running or
        // stack trace does not contain expression parent frame.
        // Return null if waiting for cache update.
        if (prev_value == null) return false;
        if (parent instanceof TCFNodeStackFrame) {
            TCFNodeExecContext exe = (TCFNodeExecContext)parent.parent;
            TCFDataCache<TCFContextState> state_cache = exe.getState();
            if (!state_cache.validate(done)) return null;
            TCFContextState state = state_cache.getData();
            if (state == null || !state.is_suspended) return true;
            TCFChildrenStackTrace stack_trace_cache = exe.getStackTrace();
            if (!stack_trace_cache.validate(done)) return null;
            if (stack_trace_cache.getData().get(parent.id) == null) return true;
        }
        else if (parent instanceof TCFNodeExecContext) {
            TCFNodeExecContext exe = (TCFNodeExecContext)parent;
            TCFDataCache<TCFContextState> state_cache = exe.getState();
            if (!state_cache.validate(done)) return null;
            TCFContextState state = state_cache.getData();
            if (state == null || !state.is_suspended) return true;
        }
        return false;
    }

    private void appendErrorText(StringBuffer bf, Throwable error) {
        if (error == null) return;
        bf.append("Exception: ");
        bf.append(TCFModel.getErrorMessage(error, true));
    }

    public boolean getDetailText(StyledStringBuffer bf, Runnable done) {
        if (!context.validate(done)) return false;
        if (!value.validate(done)) return false;
        int pos = bf.length();
        appendErrorText(bf.getStringBuffer(), context.getError());
        if (bf.length() == 0) appendErrorText(bf.getStringBuffer(), value.getError());
        if (bf.length() > pos) {
            bf.append(pos, 0, null, rgb_error);
        }
        else {
            IRegisters.RegistersContext ctx = context.getData();
            if (ctx != null) {
                if (ctx.getDescription() != null) {
                    bf.append(ctx.getDescription());
                    bf.append('\n');
                }
                int l = bf.length();
                if (ctx.isReadable()) {
                    bf.append("readable");
                }
                if (ctx.isReadOnce()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("read once");
                }
                if (ctx.isWriteable()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("writable");
                }
                if (ctx.isWriteOnce()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("write once");
                }
                if (ctx.hasSideEffects()) {
                    if (l < bf.length()) bf.append(", ");
                    bf.append("side effects");
                }
                if (l < bf.length()) bf.append('\n');
            }
            byte[] v = value.getData();
            if (v != null) {
                bf.append("Hex: ", SWT.BOLD);
                bf.append(toNumberString(16));
                bf.append(", ");
                bf.append("Dec: ", SWT.BOLD);
                bf.append(toNumberString(10));
                bf.append(", ");
                bf.append("Oct: ", SWT.BOLD);
                bf.append(toNumberString(8));
                bf.append('\n');
                bf.append("Bin: ", SWT.BOLD);
                bf.append(toNumberString(2));
                bf.append('\n');
            }
        }
        return true;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
        result.setHasChilren(children.size() > 0);
        
        if (children.size() == 0 && vectorList.size () != 0) {
            result.setHasChilren(true);
        }
        
        return true;
    }

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        if (!children.validate(done)) return false;
        result.setChildCount(children.size());
        
        if (children.size() == 0 && vectorList.size () != 0)
            result.setChildCount(vectorList.size());
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {

        if (children.size() == 0 && vectorList.size() != 0) {
            split(vectorList.size());
            int offset = 0;

            int r_offset = result.getOffset();
            int r_length = result.getLength();
            for (int ix = 0; ix < vectorList.size(); ix++) {
                if (offset >= r_offset && offset < r_offset + r_length) {
                    result.setChild(vectorList.get(ix), offset);
                }
                offset++;
            }

            return true;
        }
        else {
            return children.getData(result, done);
        }
    }

    @Override
    protected boolean getData(ILabelUpdate result, Runnable done) {
        TCFDataCache<?> pending = null;
        if (!context.validate()) pending = context;
        if (!value.validate()) pending = value;
        if (pending != null) {
            pending.wait(done);
            return false;
        }
        String[] cols = result.getColumnIds();
        if (cols == null) {
            setLabel(result, -1, 16);
        }
        else {
            IRegisters.RegistersContext ctx = context.getData();
            for (int i = 0; i < cols.length; i++) {
                String c = cols[i];
                if (ctx == null) {
                    result.setForeground(rgb_error, i);
                    result.setLabel("N/A", i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_NAME)) {
                    result.setLabel(ctx.getName(), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_HEX_VALUE)) {
                    setLabel(result, i, 16);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_DEC_VALUE)) {
                    setLabel(result, i, 10);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_DESCRIPTION)) {
                    result.setLabel(ctx.getDescription(), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_READBLE)) {
                    result.setLabel(bool(ctx.isReadable()), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_READ_ONCE)) {
                    result.setLabel(bool(ctx.isReadOnce()), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_WRITEABLE)) {
                    result.setLabel(bool(ctx.isWriteable()), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_WRITE_ONCE)) {
                    result.setLabel(bool(ctx.isWriteOnce()), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_SIDE_EFFECTS)) {
                    result.setLabel(bool(ctx.hasSideEffects()), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_VOLATILE)) {
                    result.setLabel(bool(ctx.isVolatile()), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_FLOAT)) {
                    result.setLabel(bool(ctx.isFloat()), i);
                }
                else if (c.equals(TCFColumnPresentationRegister.COL_MNEMONIC)) {
                    result.setLabel(getMnemonic(ctx), i);
                }
            }
        }
        boolean changed = false;
        next_value = value.getData();
        if (prev_value != null && next_value != null) {
            if (prev_value.length != next_value.length) {
                changed = true;
            }
            else {
                for (int i = 0; i < prev_value.length; i++) {
                    if (prev_value[i] != next_value[i]) changed = true;
                }
            }
        }
        if (changed) {
            result.setBackground(rgb_highlight, 0);
            if (cols != null) {
                for (int i = 1; i < cols.length; i++) {
                    result.setBackground(rgb_highlight, i);
                }
            }
        }
        result.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_REGISTER), 0);
        return true;
    }

    private void setLabel(ILabelUpdate result, int col, int radix) {
        IRegisters.RegistersContext ctx = context.getData();
        Throwable error = context.getError();
        if (error == null) error = value.getError();
        byte[] data = value.getData();
        if (error != null || ctx == null) {
            result.setForeground(rgb_error, col);
            result.setLabel("N/A", col);
        }
        else if (data != null) {
            String s = toNumberString(radix);
            if (col >= 0) {
                result.setLabel(s, col);
            }
            else {
                result.setLabel(ctx.getName() + " = " + s, 0);
            }
        }
    }
    
    boolean split(final int eltLength) {

        final TCFNodeRegister node = this;

        TCFDataCache<?> pending = null;
        if (!context.validate()) pending = context;
        if (!value.validate()) pending = value;
        if (pending != null) {
            return false;
        }
        IRegisters.RegistersContext ctx = node.getContext().getData();
        if (ctx != null) {
            byte[] data = value.getData();

            if (ctx == null || data == null) {
                return false;
            }

            int regLength = data.length;

            // check if it is possible to split the register value into
            // the asked number
            if ((eltLength == 0) || (regLength % eltLength != 0)) {
                return false;
            }

            int eltLenght = regLength / eltLength;

            byte[] arr = new byte[data.length];
            if (ctx.isBigEndian()) {
                System.arraycopy(data, 0, arr, 0, data.length);
            }
            else {
                for (int ix = 0; ix < data.length; ix++) {
                    arr[arr.length - ix - 1] = data[ix];
                }
            }

            for (int ix = 0; ix < eltLength; ix++) {
                byte[] newData = new byte[eltLenght];
                for (int jx = 0; jx < eltLenght; jx++) {
                    newData[jx] = arr[ix * eltLenght + jx];
                }
                String name = new String(ctx.getName() + "[" + ix + "]");
                VectorRegister newVec = new VectorRegister(name, newData, "vector register", node, ix, eltLenght);
                vectorList.remove(ix);
                vectorList.add(ix, newVec);
            }
            return true;

        }

        return (true);
    }

    String toNumberString(int radix) {
        IRegisters.RegistersContext ctx = context.getData();
        byte[] data = value.getData();
        if (ctx == null || data == null) return "N/A";
        if (radix == 2) {
            StringBuffer bf = new StringBuffer();
            int i = data.length * 8;
            while (i > 0) {
                if (i % 4 == 0 && bf.length() > 0) bf.append(',');
                i--;
                int j = i / 8;
                if (ctx.isBigEndian()) j = data.length - j - 1;
                if ((data[j] & (1 << (i % 8))) != 0) {
                    bf.append('1');
                }
                else {
                    bf.append('0');
                }
            }
            return bf.toString();
        }
        if (radix == 10 && ctx.isFloat()) {
            String s = TCFNumberFormat.toFPString(data, 0, data.length, ctx.isBigEndian());
            if (s != null) return s;
        }
        BigInteger b = TCFNumberFormat.toBigInteger(data, 0, data.length, ctx.isBigEndian(), false);
        String s = b.toString(radix);
        switch (radix) {
        case 8:
            if (!s.startsWith("0")) s = "0" + s;
            break;
        case 16:
            if (s.length() < data.length * 2) {
                StringBuffer bf = new StringBuffer();
                while (bf.length() + s.length() < data.length * 2) bf.append('0');
                bf.append(s);
                s = bf.toString();
            }
            break;
        }
        return s;
    }

    private String bool(boolean b) {
        return b ? "yes" : "no";
    }

    private String getMnemonic(IRegisters.RegistersContext ctx) {
        if (value.getData() != null) {
            IRegisters.NamedValue[] arr = ctx.getNamedValues();
            if (arr != null) {
                for (IRegisters.NamedValue n : arr) {
                    if (Arrays.equals(n.getValue(), value.getData())) return n.getName();
                }
            }
        }
        return "";
    }

    void postStateChangedDelta() {
        for (TCFModelProxy p : model.getModelProxies()) {
            if (!IDebugUIConstants.ID_REGISTER_VIEW.equals(p.getPresentationContext().getId())) continue;
            p.addDelta(this, IModelDelta.STATE);
        }
    }

    void onValueChanged() {
        prev_value = next_value;
        value.reset();
        TCFNode n = parent;
        while (n != null) {
            if (n instanceof TCFNodeExecContext) {
                ((TCFNodeExecContext)n).onRegisterValueChanged();
                break;
            }
            else if (n instanceof TCFNodeRegister) {
                TCFNodeRegister r = (TCFNodeRegister)n;
                if (r.value.isValid() && r.value.getData() != null) {
                    r.value.reset();
                    r.postStateChangedDelta();
                }
            }
            n = n.parent;
        }
        children.onParentValueChanged();
        postStateChangedDelta();
    }

    void onParentValueChanged() {
        value.reset();
        children.onParentValueChanged();
        postStateChangedDelta();
    }

    void onSuspended() {
        prev_value = next_value;
        value.reset();
        children.onSuspended();
        // Unlike thread registers, stack frame register list must be retrieved on every suspend
        if (is_stack_frame_register) children.reset();
        // No need to post delta: parent posted CONTENT
    }

    void onRegistersChanged() {
        children.onRegistersChanged();
        expression_text.reset();
        context.reset();
        value.reset();
        // No need to post delta: parent posted CONTENT
    }

    public CellEditor getCellEditor(IPresentationContext context, String column_id, Object element, Composite parent) {
        assert element == this;
        if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        return null;
    }

    private static final ICellModifier cell_modifier = new ICellModifier() {

        public boolean canModify(Object element, final String property) {
            final TCFNodeRegister node = (TCFNodeRegister)element;
            return new TCFTask<Boolean>() {
                public void run() {
                    if (!node.context.validate(this)) return;
                    IRegisters.RegistersContext ctx = node.context.getData();
                    if (ctx != null && ctx.isWriteable()) {
                        if (!ctx.isReadable()) {
                            done(Boolean.TRUE);
                            return;
                        }
                        if (!node.value.validate(this)) return;
                        if (node.value.getError() == null) {
                            if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                                done(TCFNumberFormat.isValidHexNumber(node.toNumberString(16)) == null);
                                return;
                            }
                            if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                                done(TCFNumberFormat.isValidDecNumber(true, node.toNumberString(10)) == null);
                                return;
                            }
                        }
                    }
                    done(Boolean.FALSE);
                }
            }.getE();
        }

        public Object getValue(Object element, final String property) {
            final TCFNodeRegister node = (TCFNodeRegister)element;
            return new TCFTask<String>() {
                public void run() {
                    if (!node.context.validate(this)) return;
                    IRegisters.RegistersContext ctx = node.context.getData();
                    if (!ctx.isReadable()) {
                        done("0");
                        return;
                    }
                    if (!node.value.validate(this)) return;
                    if (node.value.getError() == null) {
                        if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                            done(node.toNumberString(16));
                            return;
                        }
                        if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                            done(node.toNumberString(10));
                            return;
                        }
                    }
                    done(null);
                }
            }.getE();
        }

        public void modify(Object element, final String property, final Object value) {
            if (value == null) return;
            final TCFNodeRegister node = (TCFNodeRegister)element;
            new TCFTask<Boolean>() {
                public void run() {
                    try {
                        if (!node.context.validate(this)) return;
                        IRegisters.RegistersContext ctx = node.context.getData();
                        if (ctx != null && ctx.isWriteable()) {
                            byte[] bf = null;
                            boolean is_float = ctx.isFloat();
                            int size = ctx.getSize();
                            boolean big_endian = ctx.isBigEndian();
                            String input = (String)value;
                            String error = null;
                            if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                                error = TCFNumberFormat.isValidHexNumber(input);
                                if (error == null) bf = TCFNumberFormat.toByteArray(input, 16, false, size, false, big_endian);
                            }
                            else if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                                error = TCFNumberFormat.isValidDecNumber(is_float, input);
                                if (error == null) bf = TCFNumberFormat.toByteArray(input, 10, is_float, size, is_float, big_endian);
                            }
                            if (error != null) throw new Exception("Invalid value: " + value, new Exception(error));
                            if (bf != null) {
                                ctx.set(bf, new IRegisters.DoneSet() {
                                    public void doneSet(IToken token, Exception error) {
                                        if (error != null) {
                                            node.model.showMessageBox("Cannot modify register value", error);
                                            done(Boolean.FALSE);
                                        }
                                        else {
                                            node.value.reset();
                                            node.postStateChangedDelta();
                                            done(Boolean.TRUE);
                                        }
                                    }
                                });
                                return;
                            }
                        }
                        done(Boolean.FALSE);
                    }
                    catch (Throwable x) {
                        node.model.showMessageBox("Cannot modify register value", x);
                        done(Boolean.FALSE);
                    }
                }
            }.getE();
        }
    };

    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        assert element == this;
        return cell_modifier;
    }

    @Override
    public int compareTo(TCFNode n) {
        if (n instanceof TCFNodeRegister) {
            TCFNodeRegister r = (TCFNodeRegister)n;
            if (index < r.index) return -1;
            if (index > r.index) return +1;
        }
        return id.compareTo(n.id);
    }
    
    public Vector<VectorRegister> GetVectorList () {
        return vectorList;
    }
    
    public void SetVectorList (Vector<VectorRegister> newVecList) {
        vectorList = newVecList;
    }
    
    public void SetVectorListSize (int size) {
        IRegisters.RegistersContext ctx = context.getData();
        if (ctx != null)
            vectorList.setSize (ctx.getSize ()/size);
    }
}

class VectorRegister extends PlatformObject implements IElementLabelProvider, IElementEditor, IDetailsProvider, Comparable<VectorRegister> {
    
    private String name;
    private String description;
    private byte [] data;
    private TCFNodeRegister parent;
    private int index;
    private int size;
    
    VectorRegister (String curName, byte[] curData, String curDescription, TCFNodeRegister parentReg, int ix, int newsize) {
        name = curName;
        data = curData;
        description = curDescription;
        parent = parentReg;
        index = ix;
        size = newsize;
    }
    
    void setName (String newName) {
        name = newName;
    }
    
    void setDesciption (String newDescription) {
        description = newDescription;
    }
    
    void setData (byte[] newData) {
        data = newData;
    }
    
    void setParent (TCFNodeRegister TCFRegister) {
        parent = TCFRegister;
    }
    
    void setIndex (int ix) {
        index = ix;
    }
    
    void setSize (int newSize) {
        size = newSize;
    }
    
    String getName () {
        return name;
    }
    
    String getDesciption () {
        return description;
    }
    
    byte[] getData () {
        return data;
    }
    
    TCFNodeRegister getParent () {
        return (parent);
    }
    
    int getIndex () {
        return (index);
    }
    
    int getSize () {
        return (size);
    }
   

    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        assert element == this;
        return cell_modifier;
    }
    
    public CellEditor getCellEditor(IPresentationContext context, String column_id, Object element, Composite parent) {
        assert element == this;
        if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(column_id)) {
            return new TextCellEditor(parent);
        }
        return null;
    }

    private static final ICellModifier cell_modifier = new ICellModifier() {

        public boolean canModify(Object element, final String property) {
            final TCFNodeRegister node = ((VectorRegister)element).getParent();
            
            return new TCFTask<Boolean>() {
                public void run() {
                    if (!node.getContext().validate(this)) return;
                    IRegisters.RegistersContext ctx = node.getContext().getData();
                    if (ctx != null && ctx.isWriteable()) {
                        if (!ctx.isReadable()) {
                            done(Boolean.TRUE);
                            return;
                        }
                        if (!node.getValue().validate(this)) return;
                        if (node.getValue().getError() == null) {
                            if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                                done(TCFNumberFormat.isValidHexNumber(node.toNumberString(16)) == null);
                                return;
                            }
                            if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                                done(TCFNumberFormat.isValidDecNumber(true, node.toNumberString(10)) == null);
                                return;
                            }
                        }
                    }
                    done(Boolean.FALSE);
                }
            }.getE();
        }
        
        public Object getValue(Object element, final String property) {
            final TCFNodeRegister node = ((VectorRegister)element).getParent ();
            final VectorRegister modVec = ((VectorRegister)element);
            return new TCFTask<String>() {
                public void run() {
                    if (!node.getContext().validate(this)) return;
                    IRegisters.RegistersContext ctx = node.getContext().getData();
                    if (!ctx.isReadable()) {
                        done("0");
                        return;
                    }
                    if (!node.getValue().validate(this)) return;
                    if (node.getValue().getError() == null) {
                        if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                            done(modVec.toNumberString (16, true, false));
                            return;
                        }
                        if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                            done(modVec.toNumberString (10, true, false));
                            return;
                        }
                    }
                    done(null);
                }
            }.getE();
        }

        public void modify(Object element, final String property, final Object value) {
            if (value == null) return;
            final TCFNodeRegister node = ((VectorRegister)element).getParent();
            final VectorRegister elt = ((VectorRegister)element);
            final int modVecIx = ((VectorRegister)element).getIndex();
            new TCFTask<Boolean>() {
                public void run() {
                    try {
                        if (!node.getContext().validate(this)) return;
                        IRegisters.RegistersContext ctx = node.getContext().getData();
                        if (ctx != null && ctx.isWriteable()) {
                            byte[] bf = null;
                            boolean is_float = ctx.isFloat();
                            int size = ctx.getSize();
                            String input = (String)value;
                            String error = null;
                            
                            if (TCFColumnPresentationRegister.COL_HEX_VALUE.equals(property)) {
                                String finalString = new String();
                                    
                                for (int ix = 0; ix < node.GetVectorList().size(); ix ++) {
                                    if (ix == modVecIx) {
                                        finalString = finalString.concat(input);
                                    } else {
                                        finalString = finalString.concat(((VectorRegister)node.GetVectorList().get (ix)).toNumberString(16, true, false));
                                    }
                                }

                                if (error == null) bf = TCFNumberFormat.toByteArray(finalString, 16, false, size, false, ctx.isBigEndian());
                            }
                            else if (TCFColumnPresentationRegister.COL_DEC_VALUE.equals(property)) {
                                
                                byte [] finalByte = new byte [size];
                                byte [] curByte = null;
                                for (int ix = 0; ix < ((Vector<VectorRegister>)node.GetVectorList()).size() ; ix ++) {
                                    if (ix == modVecIx) {
                                        curByte = TCFNumberFormat.toByteArray(input, 10, is_float, ((VectorRegister)node.GetVectorList().get (modVecIx)).getSize(), false, true);;
                                    } else {
                                        curByte = ((VectorRegister)node.GetVectorList().get (ix)).getData();
                                    }
                                    for (int jx = 0; jx < curByte.length; jx ++) {
                                        finalByte [jx + ix * curByte.length] = curByte [jx];
                                    }
                                }
                                
                                byte[] arr = new byte[finalByte.length];
                                if (ctx.isBigEndian()) {
                                    System.arraycopy(finalByte, 0, arr, 0, finalByte.length);
                                }
                                else {
                                    for (int kx = 0; kx < finalByte.length; kx++) {
                                        arr[arr.length - kx - 1] = finalByte[kx];
                                    }
                                }

                                if (error == null) bf = arr;
                            }
                            if (error != null) throw new Exception("Invalid value: " + value, new Exception(error));
                            if (bf != null) {
                                ctx.set(bf, new IRegisters.DoneSet() {
                                    public void doneSet(IToken token, Exception error) {
                                        if (error != null) {
                                            node.model.showMessageBox("Cannot modify register value", error);
                                            done(Boolean.FALSE);
                                        }
                                        else {
                                            node.getValue().reset();
                                            node.postStateChangedDelta();
                                            elt.onValueChanged();
                                            done(Boolean.TRUE);
                                        }
                                    }
                                });
                                return;
                            }
                        }
                        done(Boolean.FALSE);
                    }
                    catch (Throwable x) {
                        node.model.showMessageBox("Cannot modify register value", x);
                        done(Boolean.FALSE);
                    }
                }
            }.getE();
        }
    };


    public boolean getDetailText(StyledStringBuffer buf, Runnable done) {
        // TODO Auto-generated method stub
        
        buf.append ("vector register");
        return true;
    }

    public int compareTo(VectorRegister arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(final Class adapter) {
        if (adapter.isInstance(this)) return this;
        return Platform.getAdapterManager().loadAdapter(this, adapter.getName());
    }
    
    protected boolean getData(ILabelUpdate result, Runnable done) {
        return true;
    }


    public void update(final ILabelUpdate[] updates) {
        // TODO Auto-generated method stub
        for (final ILabelUpdate update : updates) {
            final Object o = update.getElement();
            // Launch label is provided by TCFLaunchLabelProvider class.
            assert !(o instanceof TCFLaunch);
            
            final TCFNodeRegister reg = ((VectorRegister)o).getParent ();
            
            new TCFRunnable(update) {
                public void run() {
                    if (!done) {
                        if (!update.isCanceled()) {
                            if (!((TCFNode)reg).isDisposed() && ((TCFNode)reg).channel.getState() == IChannel.STATE_OPEN) {
                                if (!((TCFNode)reg).getLockedData(update, this)) return;
                            }
                            else {
                                update.setLabel("...", 0);
                            }
                            update.setStatus(Status.OK_STATUS);
                        }
                        String[] cols = update.getColumnIds();
                        
                        if (cols == null) {
                            return;
                        }
                        else {
                            for (int i = 0; i < cols.length; i++) {
                                String c = cols[i];

                                if (c.equals(TCFColumnPresentationRegister.COL_NAME)) {
                                    update.setLabel(((VectorRegister)o).getName(), i);
                                }
                                else if (c.equals(TCFColumnPresentationRegister.COL_HEX_VALUE)) {
                                    update.setLabel(((VectorRegister)o).toNumberString (16, true, false), i);
                                }
                                else if (c.equals(TCFColumnPresentationRegister.COL_DEC_VALUE)) {
                                    update.setLabel(((VectorRegister)o).toNumberString (10, true, false), i);
                                }
                                else if (c.equals(TCFColumnPresentationRegister.COL_FLOAT)) {
                                    update.setLabel(((VectorRegister)o).toNumberString (10, true, true), i);
                                }    
                            }
                        }

                        done();
                    }
                }
            };

        }
    }
    
    String toNumberString( int radix, boolean isBigEndian, boolean isFloat) {
        byte[] data = getData();
        if (data == null) return "N/A";
        if (radix == 2) {
            StringBuffer bf = new StringBuffer();
            int i = data.length * 8;
            while (i > 0) {
                if (i % 4 == 0 && bf.length() > 0) bf.append(',');
                i--;
                int j = i / 8;
                if (isBigEndian) j = data.length - j - 1;
                if ((data[j] & (1 << (i % 8))) != 0) {
                    bf.append('1');
                }
                else {
                    bf.append('0');
                }
            }
            return bf.toString();
        }
        if (radix == 10 && isFloat) {
            String s = TCFNumberFormat.toFPString(data, 0, data.length, isBigEndian);
            if (s != null) return s;
        }
        BigInteger b = TCFNumberFormat.toBigInteger(data, 0, data.length, isBigEndian, false);
        String s = b.toString(radix);
        switch (radix) {
        case 8:
            if (!s.startsWith("0")) s = "0" + s;
            break;
        case 16:
            if (s.length() < data.length * 2) {
                StringBuffer bf = new StringBuffer();
                while (bf.length() + s.length() < data.length * 2) bf.append('0');
                bf.append(s);
                s = bf.toString();
            }
            break;
        }
        return s;
    }
    
    
    void onValueChanged() {
        parent.onValueChanged();
        parent.split(parent.GetVectorList().size());
    }

}
