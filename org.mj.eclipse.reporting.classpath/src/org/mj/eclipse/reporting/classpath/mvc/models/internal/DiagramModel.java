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
package org.mj.eclipse.reporting.classpath.mvc.models.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.mj.eclipse.reporting.classpath.mvc.models.IConnector;
import org.mj.eclipse.reporting.classpath.mvc.models.IDiagram;
import org.mj.eclipse.reporting.classpath.mvc.models.INode;

/**
 * @author Mounir Jarraï
 *
 */
public class DiagramModel extends AbstractModel implements IDiagram, Serializable {

	public transient IProject rootProject;

	private List<INode> projects = new ArrayList<INode>();

	private List<IConnector> connectors = new ArrayList<IConnector>();

	public DiagramModel(IProject rootProject) {
		this.rootProject = rootProject;
	}

	/**
	 * Create a Project and add it to the model.
	 * 
	 * @param project
	 * @return the created project as <code>INode</code> instance.
	 */
	public INode createProject(IProject project) {
		INode tmpProject = null;
		if (project instanceof INode) {
			tmpProject = (INode) project;
		} else {
			tmpProject = new ProjectModel(project);
		}

		int idx = this.projects.indexOf(tmpProject);
		if (idx >= 0) {
			return this.projects.get(idx);
		}
		addProject(tmpProject);
		return tmpProject;
	}

	/**
	 * Create a connection between two projects.
	 * 
	 * @param source
	 * @param target
	 * @return the connection as a <code>IConnector</code> instance.
	 */
	public IConnector createConnector(IProject source, IProject target) {
		INode src = createProject(source);
		INode dst = createProject(target);
		IConnector connector = new ConnectorModel(src, dst);

		int idx = this.connectors.indexOf(connector);
		if (idx >= 0) {
			return this.connectors.get(idx);
		}
		addConnector(connector);
		((ProjectModel) src).addOutgoingConnection(connector);
		((ProjectModel) dst).addIncamingConnection(connector);
		return connector;
	}

	/**
	 * @return the rootProject
	 */
	public IProject getRootProject() {
		return this.rootProject;
	}

	public void setRootProject(IProject rootProject) {
		throw new IllegalStateException ("Root Project can't be set only at model creation time!");
	}	
	
	/**
	 * For internal use.
	 * 
	 * @param project
	 */
	private void addProject(INode project) {
		this.projects.add(project);
	}

	/**
	 * for internal use.
	 * 
	 * @param connector
	 */
	private void addConnector(IConnector connector) {
		this.connectors.add(connector);
	}

	/**
	 * @return an Unmodifiable list of all projects in the model as a <code>List<INode></code> instance.
	 */
	public List<INode> getProjects() {
		return Collections.unmodifiableList(projects);
	}

	/**
	 * @return an Unmodifiable list of all connectors in the model as a <code>List<IConnector></code> instance.
	 */
	public List<IConnector> getConnectors() {
		return Collections.unmodifiableList(this.connectors);
	}

	public List<Object> getElements() {
		List<Object> elements = new ArrayList<Object>();
		elements.addAll(this.projects);
		elements.addAll(this.connectors);
		return Collections.unmodifiableList(elements);
	}

	/**
	 * @param connectors
	 * @param project
	 * @return
	 */
	public static List<IConnector> getProjectOutgoingConnections(List<IConnector> connectors, INode project) {
		List<IConnector> result = new ArrayList<IConnector>();

		for (Iterator<IConnector> conIter = connectors.iterator(); conIter.hasNext();) {
			IConnector connection = conIter.next();
			if (connection.hasSource(project)) {
				result.add(connection);
			}
		}
		return result;
	}

	/**
	 * @param connectors
	 * @param project
	 * @return
	 */
	public static List<IConnector> getProjectIncomingConnections(List<IConnector> connectors, INode project) {
		List<IConnector> result = new ArrayList<IConnector>();

		for (Iterator<IConnector> conIter = connectors.iterator(); conIter.hasNext();) {
			IConnector connection = conIter.next();
			if (connection.hasTarget(project)) {
				result.add(connection);
			}
		}
		return result;
	}

	/**
	 * @param project
	 * @return
	 */
	public List<IConnector> getProjectOutgoingConnections(INode project) {
		return Collections.unmodifiableList(new ArrayList<IConnector>(project.getOutgoingConnections()));
	}

	/**
	 * @param project
	 * @return
	 */
	public List<IConnector> getProjectIncomingConnections(INode project) {
		return Collections.unmodifiableList(new ArrayList<IConnector>(project.getIncamingConnections()));
	}

	/** *************************************************************************************** */
	// For Layout Management 
	/** *************************************************************************************** */

	/**
	 * @see org.eclipse.zest.layouts.LayoutGraph#addEntity(org.eclipse.zest.layouts.LayoutEntity)
	 */
	public void addEntity(LayoutEntity node) {
	}

	/**
	 * @see org.eclipse.zest.layouts.LayoutGraph#addRelationship(org.eclipse.zest.layouts.LayoutRelationship)
	 */
	public void addRelationship(LayoutRelationship relationship) {
	}

	/**
	 * @see org.eclipse.zest.layouts.LayoutGraph#getEntities()
	 */
	public List<INode> getEntities() {
		return this.projects;
	}

	/**
	 * @see org.eclipse.zest.layouts.LayoutGraph#getRelationships()
	 */
	public List<IConnector> getRelationships() {
		return this.connectors;
	}

	public boolean isBidirectional() {
		return true;
	}
}
