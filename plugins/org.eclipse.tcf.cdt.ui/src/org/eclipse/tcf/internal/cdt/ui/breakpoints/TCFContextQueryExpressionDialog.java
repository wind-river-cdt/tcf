/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.SelectionDialog;

public class TCFContextQueryExpressionDialog extends SelectionDialog {

    private String expression;
    final private String originalExpression;
    final private String[] attributeList;
    private ParameterDataModel[] parameterData;
    final String[] columnNames = new String[] {"Parameter","Value"};

    protected TCFContextQueryExpressionDialog(Shell parentShell, String[] attributes, String initialExpression) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        expression = initialExpression;
        originalExpression = initialExpression;
        attributeList = attributes;
    }

    String getParameterInitialValue (String comparator, int initIndex){
       if (expression != null && expression.length() > 0) {
           int indexExpr = expression.indexOf (comparator, initIndex);
           if (indexExpr != -1){
               // Make sure we didn't partial match to another parameter name.
               if (indexExpr != 0) {
                   String testChar = expression.substring(indexExpr-1, indexExpr);
                   if (!testChar.equals(",")) {
                       return getParameterInitialValue(comparator,indexExpr+1);
                   }
               }
               int startOfVal = expression.indexOf ('=', indexExpr);
               if (startOfVal != -1) {
                   startOfVal += 1;
                   int endOfVal = -1;
                   if (startOfVal != 0) {
                       endOfVal = expression.indexOf (',', startOfVal);
                       if (endOfVal == -1) {
                           endOfVal = expression.length();
                       }
                   }
                   return expression.substring(startOfVal, endOfVal);
               }
           }
       }
       return null;
   }

    /**
    *
    * ParameterDataModel - Data to populate table view.
    *
    */
   public static class ParameterDataModel {

       private String attribute;
       private String value;

       public ParameterDataModel(String label) {
           attribute = label;
           value ="";
         }

       public String getLabel() {
         return attribute;
       }

       public void setLabel(String label) {
         attribute = label;
       }

       public String getData() {
         return value;
       }

       public void setData(Object data) {
           if (data != null) {
               value = (String)data;
           }
           else {
               value = "";
           }
       }
     }

   ParameterDataModel[] setupTableList() {
       parameterData = new ParameterDataModel[attributeList.length];
       for (int i = 0; i < attributeList.length; i++) {
           parameterData[i] = new ParameterDataModel(attributeList[i]);

           String initialValue = getParameterInitialValue(attributeList[i], 0);
           if (initialValue!= null){
              parameterData[i].setData(initialValue);
          }
       }
       return parameterData;
   }

   public final class ParameterTableLabelProvider extends LabelProvider implements ITableLabelProvider {
       public Image getColumnImage(Object element, int columnIndex) {
         return null;
       }
       public String getColumnText(Object element, int columnIndex) {
         ParameterDataModel data = (ParameterDataModel) element;
         switch (columnIndex) {
           case 0:
             return data.getLabel();
           case 1:
             return data.getData();
           default:
             return "";
           }
       }
     }

   public class ValueCellEditor extends TextCellEditor {
       private Object tableElement;
       public ValueCellEditor(Composite parent) {
           super(parent);
           tableElement = null;
       }
       public ValueCellEditor(Composite parent, int style) {
           super(parent, style);
           tableElement = null;
       }
       public void setTableElement( Object element) {
           tableElement = element;
       }
       public Object getTableElement() {
           return tableElement;
       }
   }

   int findParameter(String comparator, int initIndex) {
       int indexExpr = -1;
       if (expression != null && expression.length() > 0) {
           indexExpr = expression.indexOf (comparator, initIndex);
           if (indexExpr != -1){
               // Make sure we didn't partial match to another parameter name.
               if (indexExpr != 0) {
                   String testChar = expression.substring(indexExpr-1, indexExpr);
                   if (!testChar.equals(",")) {
                       return findParameter(comparator,indexExpr+1);
                   }
               }
           }
       }
       return indexExpr;
   }

   private boolean replaceParameter(String parameter, String replaceString, int index) {
       if (index != 0) {
           String testChar = expression.substring(index+parameter.length(), index+1+parameter.length());
           if (!testChar.equals("=")) {
               return false;
           }
           testChar = expression.substring(index-1, index);
           if (!testChar.equals(",")) {
               return false;
           }
           else {
               index-=1;
           }
       }
       int endLocation = expression.indexOf(',', index+1);
       if (endLocation == -1) {
           endLocation = expression.length();
       }
       else if (index == 0 && replaceString.length() == 0) {
           endLocation++;
       }
       String removeStr = expression.substring(index, endLocation);
       expression = expression.replace(removeStr, replaceString);

       return true;
   }

   public final class CellEditorListener implements ICellEditorListener {
       private ValueCellEditor fcellEditor;
       public CellEditorListener(ValueCellEditor cellEditor) {
           fcellEditor = cellEditor;
       }
       public void applyEditorValue() {
           String cellString = null;
           Object obj = fcellEditor.getValue();
           ParameterDataModel param = (ParameterDataModel)fcellEditor.getTableElement();
           String paramName = param.getLabel();
           if (obj != null) {
               cellString = (String)obj;
           }
           if (cellString == null) {
               return;
           }
           cellString = cellString.trim();
           if (cellString.length() != 0) {
               if (expression == null || expression.length() == 0) {
                   expression = new String(paramName + "=" + cellString);
                   }
               else {
                   String nameValuePair = paramName + "=" + cellString;
                   int strIndex = findParameter(paramName, 0);
                   if (strIndex == -1) {
                       expression += "," + nameValuePair;
                   }
                   else {
                       if (strIndex != 0) {
                           nameValuePair = "," + nameValuePair;
                       }
                       if (!replaceParameter(paramName,nameValuePair,strIndex)) {
                           getButton(IDialogConstants.OK_ID).setEnabled(false);
                       }
                   }
               }
               param.setData(cellString);
           }
           else if (expression != null && expression.length() != 0){
               fcellEditor.setValue(cellString);
               int strIndex = findParameter(paramName, 0);
               if (strIndex != -1) {
                   if (!replaceParameter(paramName,"",strIndex)) {
                       getButton(IDialogConstants.OK_ID).setEnabled(false);
                   }
                   param.setData("");
               }
           }
           if (expression == null ||
              (expression.length() == 0 && originalExpression.length() == 0) ||
               originalExpression.contentEquals(expression)) {
               getButton(IDialogConstants.OK_ID).setEnabled(false);
           }
           else {
               getButton(IDialogConstants.OK_ID).setEnabled(true);
           }
      }

      public void cancelEditor() {
      }
      public void editorValueChanged(boolean oldValidState, boolean newValidState) {
      }
  }

   public final class ExpressionEditingSupport extends EditingSupport {

       private ValueCellEditor cellEditor = null;
       private ColumnViewer fviewer;

       private ExpressionEditingSupport(ColumnViewer viewer) {
           super(viewer);
           fviewer = viewer;
           cellEditor = new ValueCellEditor((Composite) getViewer().getControl(), SWT.NONE);
           cellEditor.addListener(new CellEditorListener(cellEditor));
       }

       @Override
       protected CellEditor getCellEditor(Object element) {
           cellEditor.setTableElement(element);
           return cellEditor;
       }

       @Override
       protected boolean canEdit(Object element) {
           return true;
       }

       @Override
       protected Object getValue(Object element) {
           if (element instanceof ParameterDataModel) {
               ParameterDataModel data = (ParameterDataModel)element;
               return data.getData();
           }
           return null;
       }

       @Override
       protected void setValue(Object element, Object value) {
           if (element instanceof ParameterDataModel) {
               ParameterDataModel data = (ParameterDataModel) element;
               data.setData(value);
               fviewer.update(element, null);
           }
       }
   }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        page.setLayout(gridLayout);
        page.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnWeightData(1));
        tableLayout.addColumnData(new ColumnWeightData(1));
        Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayout(tableLayout);
        TableViewer tableViewer = new TableViewer(table);
        tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Control cntrl = tableViewer.getControl();
        cntrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        TableViewerColumn labelColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        labelColumn.getColumn().setText(columnNames[0]);
        TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.Modify);
        valueColumn.getColumn().setText(columnNames[1]);
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setLabelProvider(new ParameterTableLabelProvider());
        valueColumn.setEditingSupport(new ExpressionEditingSupport(valueColumn.getViewer()));
        tableViewer.setInput(setupTableList());
        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                ParameterDataModel t1 = (ParameterDataModel) e1;
                ParameterDataModel t2 = (ParameterDataModel) e2;
                    return t1.getLabel().compareTo(t2.getLabel());
            };
        });
        return parent;
    }

    public String getExpression() {
        return expression;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
     * .Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Expression Parameters");
    }

    @Override
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
}
