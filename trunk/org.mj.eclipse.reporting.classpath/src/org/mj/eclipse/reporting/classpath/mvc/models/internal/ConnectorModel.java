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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.zest.layouts.LayoutBendPoint;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.constraints.BasicEdgeConstraints;
import org.eclipse.zest.layouts.constraints.LabelLayoutConstraint;
import org.eclipse.zest.layouts.constraints.LayoutConstraint;
import org.eclipse.zest.layouts.dataStructures.BendPoint;
import org.mj.eclipse.reporting.classpath.mvc.models.IConnector;
import org.mj.eclipse.reporting.classpath.mvc.models.INode;

/**
 * @author Mounir Jarraï
 *
 */
public class ConnectorModel extends AbstractModel implements IConnector {

	private static transient final Logger logger = Logger.getLogger("ConnectorModel");

	private static transient final String SOURCE_AND_TARGET_MUST_BE_NOT_NULL = "Source and target must be not null!";

	private INode source;

	private INode target;

	private int cost = 1;

	private boolean inCycle = false;

	protected ConnectorModel(INode source, INode target) {
		if (source == null || target == null) {
			logger.severe(SOURCE_AND_TARGET_MUST_BE_NOT_NULL);
			throw new IllegalArgumentException(SOURCE_AND_TARGET_MUST_BE_NOT_NULL);
		}
		this.source = source;
		this.target = target;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Creates connection from " + source.getName() + " to " + target.getName());
		}
	}

	public final INode getSource() {
		return source;
	}

	public final INode getTarget() {
		return target;
	}

	public final boolean hasSource(INode project) {
		return source.equals(project);
	}

	public final boolean hasTarget(INode project) {
		return target.equals(project);
	}

	private Object costLock = new Object();

	/**
	 * @return the relationCost
	 */
	public final int getCost() {
		synchronized (costLock) {
			return cost;
		}
	}

	/**
	 * @param relationCost
	 *            the relationCost to set
	 */
	public final void setCost(int relationCost) {
		synchronized (costLock) {
			this.cost = relationCost;
		}
	}

	private Object inCycleLock = new Object();

	/**
	 * @return the inCycle
	 */
	public final boolean isInCycle() {
		synchronized (inCycleLock) {
			return inCycle;
		}
	}

	/**
	 * @param inCycle
	 *            the inCycle to set
	 */
	public final void setInCycle(boolean inCycle) {
		synchronized (inCycleLock) {
			this.inCycle = inCycle;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String srcName = (source.getName() != null)
				? source.getName()
				: "";
		String dstName = (target.getName() != null)
				? target.getName()
				: "";
		//$ANALYSIS-IGNORE
		return srcName + "->" + dstName;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return source.hashCode() ^ target.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ConnectorModel other = (ConnectorModel) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	//--------------------------------
	// For Layout Management
	//--------------------------------

	private transient Object internalRelationship;

	private transient LayoutBendPoint[] bendPoints;

	/**
	 * @see org.eclipse.zest.layouts.LayoutRelationship#clearBendPoints()
	 */
	public void clearBendPoints() {
		this.bendPoints = new BendPoint[0];
	}

	/**
	 * @see org.eclipse.zest.layouts.LayoutRelationship#getDestinationInLayout()
	 */
	public LayoutEntity getDestinationInLayout() {
		return target;
	}

	/**
	 * @see org.eclipse.zest.layouts.LayoutRelationship#getSourceInLayout()
	 */
	public LayoutEntity getSourceInLayout() {
		return source;
	}

	/**
	 * @see org.eclipse.zest.layouts.LayoutRelationship#populateLayoutConstraint(org.eclipse.zest.layouts.constraints.LayoutConstraint)
	 */
	public void populateLayoutConstraint(LayoutConstraint constraint) {
		if (constraint instanceof LabelLayoutConstraint) {
			//			LabelLayoutConstraint labelConstraint = (LabelLayoutConstraint) constraint;
			//			labelConstraint.label = this.toString();
			//			labelConstraint.pointSize = 18;
		} else if (constraint instanceof BasicEdgeConstraints) {
			// noop

		}
	}

	/**
	 * @see org.eclipse.zest.layouts.LayoutRelationship#setBendPoints(org.eclipse.zest.layouts.LayoutBendPoint[])
	 */
	public void setBendPoints(LayoutBendPoint[] bendPoints) {
		this.bendPoints = bendPoints;
	}

	/**
	 * @see ca.uvic.cs.chisel.layouts.LayoutRelationship#getInternalRelationship()
	 */
	public Object getLayoutInformation() {
		return internalRelationship;
	}

	/**
	 * @see ca.uvic.cs.chisel.layouts.LayoutRelationship#setInternalRelationship(java.lang.Object)
	 */
	public void setLayoutInformation(Object layoutInformation) {
		this.internalRelationship = layoutInformation;
	}
}
