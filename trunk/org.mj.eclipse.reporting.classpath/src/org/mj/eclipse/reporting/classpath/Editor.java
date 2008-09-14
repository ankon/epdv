/**
 * Copyright (c) 2008, Mounir Jarraï
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. All advertising materials mentioning features or use of this software
 *       must display the following acknowledgement:
 *			This product includes software developed by Mounir Jarraï
 *      	and its contributors.
 *    4. Neither the name Mounir Jarraï nor the names of its contributors may 
 *       be used to endorse or promote products derived from this software 
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY MOUNIR JARRAÏ ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MOUNIR JARRAÏ BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.mj.eclipse.reporting.classpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.actions.ActionFactory;
import org.mj.eclipse.reporting.classpath.actions.layout.LayoutActions;
import org.mj.eclipse.reporting.classpath.mvc.controllers.DiagramPartFactory;
import org.mj.eclipse.reporting.classpath.mvc.models.IDiagramElementsFactory;

/**
 * @author Mounir Jarraï
 *
 */
public final class Editor extends GraphicalEditorWithFlyoutPalette {

	public final static String ID = "org.mj.eclipse.reporting.classpath.Editor";

	private final static Logger logger = Logger.getLogger(Editor.class.getName());

	private RulerComposite rulerComposite;

	public Editor() {
		setEditDomain(new DefaultEditDomain(this));
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("Created");
		}
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getGraphicalControl()
	 */
	protected Control getGraphicalControl() {
		return rulerComposite;
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#createGraphicalViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createGraphicalViewer(Composite parent) {
		rulerComposite = new RulerComposite(parent, SWT.NONE);
		super.createGraphicalViewer(rulerComposite);

		ScrollingGraphicalViewer graphicalViewer = (ScrollingGraphicalViewer) getGraphicalViewer();
		rulerComposite.setGraphicalViewer(graphicalViewer);
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, Boolean.FALSE);
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, Boolean.FALSE);
		getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, Boolean.FALSE);
		getGraphicalViewer().setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1), MouseWheelZoomHandler.SINGLETON);

		// Root editpart: for Zooming and negative positions
		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();

		List<String> zoomLevels = new ArrayList<String>(3);

		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);

		double[] accessibleZoomLevels = new double[100];
		double start = .02;
		for (int i = 0; i < accessibleZoomLevels.length; i++) {
			accessibleZoomLevels[i] = start;
			start += (start < 1 ) ? .02 : (start < 3) ?.1: .25;
		}
		root.getZoomManager().setZoomLevels(accessibleZoomLevels);

		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);
		getSite().getKeyBindingService().registerAction(zoomIn);
		getSite().getKeyBindingService().registerAction(zoomOut);

		getGraphicalViewer().setRootEditPart(root);

		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("GraphicalViewer configured");
		}
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		//Sets the model
		GraphicalViewer graphicalViewer = getGraphicalViewer();
		graphicalViewer.setEditPartFactory(new DiagramPartFactory());
		graphicalViewer.setContents(getModel());

		setPartName(getModel().getRootProject().getName());
		
		// Add contextuel Menu
		LayoutActions layoutActions = new LayoutActions(this);
		ContextMenuProvider contextMenuProvider = new ContextMenuProvider(getGraphicalViewer()) {
			/**
			 * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
			 */
			@Override
			public void buildContextMenu(IMenuManager menuManager) {
				GEFActionConstants.addStandardActionGroups(menuManager);

				if (menuManager.isEnabled()) {

					// Undo, Redo action
					menuManager.appendToGroup(GEFActionConstants.GROUP_UNDO, getActionRegistry().getAction(ActionFactory.UNDO.getId()));
					menuManager.appendToGroup(GEFActionConstants.GROUP_UNDO, getActionRegistry().getAction(ActionFactory.REDO.getId()));

					// Layout actions
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.DIRECTED_GRAPH_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.GRID_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.HORIZONTAL_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.HORIZONTAL_SHIFT_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.HORIZONTAL_TREE_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.RADIAL_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.SPRING_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.TREE_LAYOUT_ACTION_ID));
					menuManager.appendToGroup(GEFActionConstants.GROUP_REST, getActionRegistry().getAction(
							LayoutActions.VERTICAL_LAYOUT_ACTION_ID));
				}
			}

		};
		getGraphicalViewer().setContextMenu(contextMenuProvider);
		layoutActions.applyDefaultLayout();

		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("GraphicalViewer initialized");
		}
	}

	private IDiagramElementsFactory getModel() {
		IEditorInput input = getEditorInput();
		if (input instanceof OnMemoryEditorInput) {
			return ((OnMemoryEditorInput) input).getModel();
		}
		return null;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return super.isDirty();
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#isSaveOnCloseNeeded()
	 */
	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		PaletteStack stackPalette = new PaletteStack("Palette Stack", "no desc !", null);
		stackPalette.add(new SelectionToolEntry());
		stackPalette.add(new MarqueeToolEntry());
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("Root palette created");
		}
		PaletteGroup group = new PaletteGroup("creation tools");
		group.add(stackPalette);
		PaletteRoot rootPalette = new PaletteRoot();
		rootPalette.add(group);
		return rootPalette;
	}

}
