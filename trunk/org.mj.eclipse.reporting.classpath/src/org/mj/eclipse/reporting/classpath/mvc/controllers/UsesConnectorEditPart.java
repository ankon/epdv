/**
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.mj.eclipse.reporting.classpath.mvc.controllers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.mj.eclipse.reporting.classpath.mvc.models.IConnector;
import org.mj.eclipse.reporting.classpath.mvc.models.IDiagram;
import org.mj.eclipse.reporting.classpath.mvc.views.UseConnectorFigure;

/**
 * @author Mounir Jarraï
 *
 */
public class UsesConnectorEditPart extends AbstractConnectionEditPart implements IAdaptable {

	protected static final Logger logger = Logger.getLogger("ConnectionEditPart");

	/**
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		// Create figure
		UseConnectorFigure connectorFigure = new UseConnectorFigure();

		IConnector connector = (IConnector) getModel();
		connectorFigure.setToolTip(connector.toString() + " : "+ connector.getCost());

		// sets end style
		PolygonDecoration polygonDecoration = new PolygonDecoration(); // End connection triangle decorator
		connectorFigure.setTargetDecoration(polygonDecoration);
		connectorFigure.setLineStyle(Graphics.LINE_SOLID);
		connectorFigure.setLineWidth(1);
		
		if (connector.isInCycle()) {
			connectorFigure.setForegroundColor(ColorConstants.red);
		} else if (connector.getCost() > IDiagram.INTIAL_CONNECTOR_COST) {
			connectorFigure.setForegroundColor(ColorConstants.lightGray);
		}

		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("createFigure() -> " + connectorFigure);
		}
		return connectorFigure;

	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	@Override
	public void refreshVisuals() {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("refreshVisuals()");
		}
		super.refreshVisuals();

//		Object model = getModel();
		PolylineConnection figureConnection = (PolylineConnection) getFigure();
		// set color ...
		figureConnection.invalidate();
		//setRoutingConstraint(null);
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class key) {
		if (IFigure.class.equals(key)) {
			return getFigure();
		} else if (UsesConnectorEditPart.class.equals(key)) {
			return this;
		}
		return super.getAdapter(key);
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("createEditPolicies()");
		}
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionEditPolicy() {

			protected final Logger logger = Logger.getLogger("ConnectionEditPolicy");

			/**
			 * @see org.eclipse.gef.editpolicies.ConnectionEditPolicy#getDeleteCommand(org.eclipse.gef.requests.GroupRequest)
			 */
			@Override
			protected Command getDeleteCommand(GroupRequest request) {
				if (logger.isLoggable(Level.FINE)) {
					//$ANALYSIS-IGNORE
					logger.fine("getDeleteCommand(" + request + ") -> " + null);
				}
				return null;
			}
		});
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#setSource(org.eclipse.gef.EditPart)
	 */
	@Override
	public void setSource(EditPart editPart) {
		super.setSource(editPart);
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("setSource(" + editPart + ")");
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#setTarget(org.eclipse.gef.EditPart)
	 */
	@Override
	public void setTarget(EditPart editPart) {
		super.setTarget(editPart);
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("setTarget(" + editPart + ")");
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("activate()");
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		super.deactivate();
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("deactivate()");
		}
	}

}
