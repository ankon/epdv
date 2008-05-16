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
package org.mj.eclipse.reporting.classpath.mvc.models;

import java.io.Serializable;
import java.util.List;

import org.eclipse.zest.layouts.LayoutGraph;

/**
 * @author Mounir Jarraï
 *
 */
public interface IDiagram extends IDiagramElementsFactory,LayoutGraph, Serializable {

	public final int INTIAL_CONNECTOR_COST = 1;

	/**
	 * @return an Unmodifiable list of all projects in the model as a <code>List<INode></code> instance.
	 */
	public List<INode> getProjects();

	/**
	 * @return an Unmodifiable list of all connectors in the model as a <code>List<IConnector></code> instance.
	 */
	public List<IConnector> getConnectors();

	public List<Object> getElements();

	/**
	 * @param project
	 * @return
	 */
	public List<IConnector> getProjectOutgoingConnections(INode project);

	/**
	 * @param project
	 * @return
	 */
	public List<IConnector> getProjectIncomingConnections(INode project);

}
