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
package org.mj.eclipse.reporting.classpath.mvc.controllers;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.AutomaticRouter;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.mj.eclipse.reporting.classpath.mvc.controllers.policies.HelloXYLayoutEditPolicy;
import org.mj.eclipse.reporting.classpath.mvc.models.IDiagram;
import org.mj.eclipse.reporting.classpath.mvc.views.DiagramFigure;

/**
 * @author Mounir Jarraï
 *
 */
public final class DiagramEditpart extends AbstractComponentEditPart implements IAdaptable {

	private MarqueeDragTracker marqueeDragTracker = new MarqueeDragTracker();
	private AutomaticRouter router;

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		DiagramFigure diagramFigure = new DiagramFigure();
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("createFigure() -> " + diagramFigure);
		}
		return diagramFigure;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	public void refreshVisuals() {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("refreshVisuals()");
		}
		super.refreshVisuals();
		ConnectionLayer cLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		cLayer.setConnectionRouter(getConnectionRouter());
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("End refreshVisuals()");
		}
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), new Rectangle(-1,-1,-1,-1));
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@SuppressWarnings("unchecked")
	public List getModelChildren() {
		IDiagram model = (IDiagram) getModel();
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("getModelChildren() -> " + model.getProjects());
		}
		// ATTENTION : les connections ne sont pas considérées comme enfants.Il ne doivent pas être retournés par cette methode.
		return model.getProjects();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getContentPane()
	 */
	@Override
	public IFigure getContentPane() {
		IFigure contentPane = super.getContentPane();
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("getContentPane() -> " + contentPane);
		}
		return contentPane;
	}

	/**
	 * @return
	 */
	public AutomaticRouter getConnectionRouter() {
		if (router == null) {
			router = new FanRouter();
			//						router.setNextRouter(new BendpointConnectionRouter());
			//						router.setNextRouter(new ManhattanConnectionRouter());
			router.setNextRouter(new ShortestPathConnectionRouter(getFigure())); // Cool :-)
		}
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("getConnectionRouter() -> " + router);
		}
		return router;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	public DragTracker getDragTracker(Request request) {
		if (marqueeDragTracker == null) {
			marqueeDragTracker = new MarqueeDragTracker();
		}
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("getDragTracker(" + request + ") -> " + marqueeDragTracker);
		}
		return marqueeDragTracker;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies() You need to tell how children nodes will be layed out...
	 */
	protected void createEditPolicies() {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("createEditPolicies()");
		}
		// The root component cannot be removed from its parent
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new HelloXYLayoutEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ContainerEditPolicy() {
			protected final Logger logger = Logger.getLogger("ContainerEditPolicy");

			/**
			 * @see org.eclipse.gef.editpolicies.ContainerEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
			 */
			@Override
			protected Command getCreateCommand(CreateRequest request) {
				if (logger.isLoggable(Level.FINE)) {
					//$ANALYSIS-IGNORE
					logger.fine("getCreateCommand(" + request + ") -> " + null);
				}
				return null;
			}
		});

	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class key) {
		if (IFigure.class.equals(key)) {
			return getFigure();
		} else if (DiagramEditpart.class.equals(key)) {
			return this;
		}
		return super.getAdapter(key);
	}

	public void propertyChange(PropertyChangeEvent evt) {
	}
}
