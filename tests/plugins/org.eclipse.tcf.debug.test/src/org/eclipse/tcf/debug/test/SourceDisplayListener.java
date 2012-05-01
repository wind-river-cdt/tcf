/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.Iterator;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 */
public class SourceDisplayListener implements IPartListener, IAnnotationModelListener {

    private static final String ANNOTATION_TOP_FRAME = "org.eclipse.tcf.debug.top_frame";
    
    private IWorkbenchPage fPage;
    private ITextEditor fActiveEditor;
    
    private IPath fExpectedFile;
    private int fExpectedLine;
    
    private boolean fFileFound = false;
    private boolean fAnnotationFound = false;
    private IAnnotationModel fAnnotationModel = null;

    public SourceDisplayListener() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        fPage = window.getActivePage();
        fPage.addPartListener(this);
        fPage.getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                partBroughtToTop(fPage.getActiveEditor());
            }
        });
    }
    
    public void dispose() {
        if (fAnnotationModel != null) {
            fAnnotationModel.removeAnnotationModelListener(this);
            fAnnotationModel = null;
        }
        fPage.removePartListener(this);
    }
    
    public void reset() {
        fFileFound = false;
        fAnnotationFound = false;
    }
    
    public void setCodeArea(CodeArea area) {
        fExpectedFile = new Path(area.file);
        fExpectedLine = area.start_line;
        checkFile();
    }
    
    public void waitTillFinished() throws InterruptedException {
        synchronized(this) {
            while(!isFinished()) {
                wait(100);
            }
        }
    }
    
    public synchronized boolean isFinished() {
        return fFileFound && fAnnotationFound;
    }
    
    public void partActivated(IWorkbenchPart part) {
        System.out.println("activated");
        partBroughtToTop(part);
    }
    
    public void partBroughtToTop(IWorkbenchPart part) {
        System.out.println("top");
        fActiveEditor = null;
        if (fAnnotationModel != null) {
            fAnnotationModel.removeAnnotationModelListener(this);
        }
        
        if (part instanceof ITextEditor) {
            fActiveEditor = ((ITextEditor)part);
            checkFile();
        }
    }
    public void partClosed(IWorkbenchPart part) {
        System.out.println("closed");

    }
    public void partDeactivated(IWorkbenchPart part) {
        System.out.println("deactivated");
    }
    public void partOpened(IWorkbenchPart part) {
        System.out.println("opened");
    }
    
    public void modelChanged(IAnnotationModel model) {
        checkAnnotations();
    }
    
    private synchronized void checkFile() {
        if (fActiveEditor == null) return;
        IEditorInput input = fActiveEditor.getEditorInput();
        IPath location = null;
        
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            location = file.getLocation();
        } else if (input instanceof FileStoreEditorInput) {
            location = URIUtil.toPath(((FileStoreEditorInput)input).getURI()); 
        }
        
        if (location != null && fExpectedFile != null &&
            location.lastSegment().equals(fExpectedFile.lastSegment())) 
        {
            fFileFound = true;
            
            IDocumentProvider docProvider = fActiveEditor.getDocumentProvider();
            fAnnotationModel = docProvider.getAnnotationModel(fActiveEditor.getEditorInput());
            fAnnotationModel.addAnnotationModelListener(this);
            
            checkAnnotations();
        }
    }
    
    private synchronized void checkAnnotations() {
        Position expectedPosition = calcExpectedPosition();
        if (checkTopFrameAnnotation(expectedPosition)) {
            fAnnotationFound = true;
        }
    }
    
    private Position calcExpectedPosition() {
        IDocument doc = fActiveEditor.getDocumentProvider().getDocument(fActiveEditor.getEditorInput());
        if (doc == null) return null;
        try {
            IRegion region = doc.getLineInformation(fExpectedLine - 1);
            return new Position(region.getOffset(), region.getLength());
        } catch (BadLocationException e) {
        }
        return null;
    }
    
    private boolean checkTopFrameAnnotation(Position pos) {
        if (fAnnotationModel == null) return false;
        
        for (Iterator<?> itr = fAnnotationModel.getAnnotationIterator(); itr.hasNext();) {
            Annotation ann = (Annotation)itr.next();
            if ( ANNOTATION_TOP_FRAME.equals(ann.getType()) ) {
                // Compare requested line to annotation location.
                if (pos.equals(fAnnotationModel.getPosition(ann))) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
