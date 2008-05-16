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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.mj.eclipse.reporting.classpath.Activator;
import org.mj.eclipse.reporting.classpath.mvc.models.IConnector;
import org.mj.eclipse.reporting.classpath.mvc.models.INode;
import org.mj.eclipse.reporting.classpath.mvc.views.ProjectFigure;
import org.mj.eclipse.reporting.classpath.preferences.PreferenceConstants;

/**
 * @author Mounir Jarraï
 *
 */
public final class ProjectEditPart extends AbstractComponentEditPart implements NodeEditPart {

	private static final Logger logger = Logger.getLogger(ProjectEditPart.class.getName());

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("Creating figure");
		}
		return new ProjectFigure();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	@Override
	public void refreshVisuals() {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("Refreshing visulals");
		}
		super.refreshVisuals();

		ProjectFigure figure = (ProjectFigure) getFigure();
		INode model = (INode) getModel();
		figure.setName(model.getName());

		refreshLayout();
	}

	/**
	 * @param figure
	 */
	public void refreshLayout() {
		ProjectFigure figure = (ProjectFigure) getFigure();
		// Computes constraints
		Rectangle constraint = null;
		Rectangle bounds = figure.getBounds();
		if (bounds.height == 0 || bounds.width == 0) {
			// By setting the width and height to -1, we ensure that the preferred width and height are calculated automatically.
			constraint = new Rectangle(bounds.x, bounds.y, -1, -1);
		} else {
			constraint = new Rectangle(bounds);
		}
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure, constraint);

		// Check for first Layout refresh and set model to preferred size
		INode model = (INode) getModel();
		if (model.getHeightInLayout() == -1 || model.getWidthInLayout() == -1) {
			Dimension size = getFigure().getPreferredSize();
//			model.setSizeInLayout(size.width * 1.618, size.height * 1.618); // Factor Phi
			model.setSizeInLayout(size.width, size.height); 
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class key) {
		if (IFigure.class.equals(key)) {
			return getFigure();
		} else if (ProjectEditPart.class.equals(key)) {
			return this;
		} else if (INode.class.equals(key)) {
			return (INode) getModel();
		}
		return super.getAdapter(key);
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getContentPane()
	 */
	@Override
	public IFigure getContentPane() {
		IFigure contentPane = ((ProjectFigure) getFigure()).getContentPane();
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("Returning ContentPane: { class=" + contentPane.getClass().getSimpleName() + ", Layout="
					+ contentPane.getLayoutManager().getClass().getSimpleName() + "}");
		}
		return contentPane;
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// Handles model change
		ProjectFigure projectFigure = (ProjectFigure) getFigure();

		if (INode.Properties.LOCATION.represents(evt)) {
			projectFigure.setLocation((Point) evt.getNewValue());
			refreshLayout();
		} else if (INode.Properties.SIZE.represents(evt)) {
			projectFigure.setSize((Dimension) evt.getNewValue());
			refreshLayout();
		} else if (INode.Properties.X.represents(evt)) {
			projectFigure.setLocation(new Point((Double) evt.getNewValue(), projectFigure.getLocation().y));
			refreshLayout();
		} else if (INode.Properties.Y.represents(evt)) {
			projectFigure.setLocation(new Point(projectFigure.getLocation().x, (Double) evt.getNewValue()));
			refreshLayout();
		} else if (INode.Properties.WIDTH.represents(evt)) {
			projectFigure.setSize(((Double) evt.getNewValue()).intValue(), projectFigure.getSize().width);
			refreshLayout();
		} else if (INode.Properties.HEIGHT.represents(evt)) {
			projectFigure.setSize(projectFigure.getSize().width, ((Double) evt.getNewValue()).intValue());
			refreshLayout();
		}
	}

	/**
	 * @see org.mj.eclipse.reporting.classpath.mvc.controllers.AbstractComponentEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
	}

	/**
	 * @param result
	 * @param connections
	 * @return
	 */
	private List<IConnector> applyFilters(Collection<IConnector> connections) {
		Preferences pluginPreferences = Activator.getDefault().getPluginPreferences();
		int maxCoste = pluginPreferences.getInt(PreferenceConstants.HID_CONNECTION_BY_COST);

		List<IConnector> result = new ArrayList<IConnector>();
		Collection<IConnector> synchronizedCollection = Collections.synchronizedCollection(connections);
		synchronized (synchronizedCollection) {
			for (IConnector connector : synchronizedCollection) {
				if (connector.isInCycle() || connector.getCost() <= maxCoste) {
					result.add(connector);
				}
			}
		}
		return result;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
	 */
	@Override
	protected List<IConnector> getModelSourceConnections() {
		INode projectModel = (INode) getModel();
		Collection<IConnector> outgoingConnections = projectModel.getOutgoingConnections();
		return applyFilters(outgoingConnections);
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
	 */
	@Override
	protected List<IConnector> getModelTargetConnections() {
		INode projectModel = (INode) getModel();
		Collection<IConnector> incamingConnections = projectModel.getIncamingConnections();
		return applyFilters(incamingConnections);
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}

	//******************************************************************************************************************
	//
	// Plugin Preferences related logic
	//
	//******************************************************************************************************************

	/**
	 * A listener on plugin preferences.
	 */
	private Preferences.IPropertyChangeListener pluginPreferencesChangeListener = new Preferences.IPropertyChangeListener() {
		public void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
			if (PreferenceConstants.HID_CONNECTION_BY_COST.equals(event.getProperty())) {
				refreshSourceConnections();
				refreshTargetConnections();
			}
		}
	};

	/**
	 * @see org.mj.eclipse.reporting.classpath.mvc.controllers.AbstractComponentEditPart#activate()
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			Preferences pluginPreferences = Activator.getDefault().getPluginPreferences();
			pluginPreferences.addPropertyChangeListener(pluginPreferencesChangeListener);
		}
	}

	/**
	 * @see org.mj.eclipse.reporting.classpath.mvc.controllers.AbstractComponentEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			Preferences pluginPreferences = Activator.getDefault().getPluginPreferences();
			pluginPreferences.removePropertyChangeListener(pluginPreferencesChangeListener);
		}
	}
}
