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
import org.eclipse.swt.widgets.Table;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

public class TCFContextQueryExpressionDialog extends SelectionDialog {

    private String expression;
    private String[] attributeList;
    private ParameterDataModel[] parameterData;
    final String[] columnNames = new String[] {"Parameter","Value"};
    
    protected TCFContextQueryExpressionDialog(Shell parentShell, String[] attributes, String initialExpression) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        expression = initialExpression;
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
               int startOfVal = expression.indexOf ('=', indexExpr) + 1;
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

           String initialValue = getParameterInitialValue((String)attributeList[i], 0);
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
                   String nameValuePair = paramName + "=" + cellString ;
                   if (expression.lastIndexOf(nameValuePair) == -1) {
                       expression +=",";
                       expression += nameValuePair;
                   }
               }
               param.setData(cellString);
               getButton(IDialogConstants.OK_ID).setEnabled(true);
           }
           else if (expression != null && expression.length() != 0){
               fcellEditor.setValue(cellString);
               // Check to remove expression value.
               int strIndex = findParameter(paramName, 0);
               if (strIndex != -1) {
                   if (strIndex != 0) {
                       String testChar = expression.substring(strIndex+paramName.length(), strIndex+1+paramName.length());
                       if (!testChar.equals("=")) {
                           // malformed expression
                           getButton(IDialogConstants.OK_ID).setEnabled(false);                           
                           return;
                       }
                       testChar = expression.substring(strIndex-1, strIndex);
                       if (!testChar.equals(",")) {
                           // malformed expression
                           getButton(IDialogConstants.OK_ID).setEnabled(false);
                           return;
                       }
                       else {
                           strIndex-=1;
                       }                       
                   }
                   int endLocation = expression.indexOf(',', strIndex+1);
                   if (endLocation == -1) {
                       endLocation = expression.length();
                   }
                   String removeStr = expression.substring(strIndex, endLocation);
                   expression = expression.replace(removeStr, "");
                   param.setData("");
               }
           }
           if (expression != null && expression.length() == 0) {
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
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Expression Parameters");
    }
    
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
}
