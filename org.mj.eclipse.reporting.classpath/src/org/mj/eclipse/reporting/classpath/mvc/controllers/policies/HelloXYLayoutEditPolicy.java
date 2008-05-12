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
package org.mj.eclipse.reporting.classpath.mvc.controllers.policies;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.mj.eclipse.reporting.classpath.mvc.models.INode;

/**
 * @author Mounir Jarraï
 *
 */
public class HelloXYLayoutEditPolicy extends XYLayoutEditPolicy {

	static final Logger logger = Logger.getLogger(HelloXYLayoutEditPolicy.class.getName());

	/**
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createAddCommand(EditPart, Object)
	 */
	protected Command createAddCommand(EditPart child, Object constraint) {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("EditPart : " + child + " , constraint :" + constraint);
		}
		return super.createAddCommand(child, constraint);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(EditPart, Object)
	 */
	protected Command createChangeConstraintCommand(final ChangeBoundsRequest request, final EditPart child, final Object constraint) {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("EditPart : " + child + " , constraint :" + constraint);
		}
		final Rectangle rectangle = (Rectangle) constraint;
		final INode node = (INode) child.getAdapter(INode.class); // = EditPart.getModel()
		Command command = new LayoutCommand(node, request, rectangle);
		return command;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
	 *      java.lang.Object)
	 */
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		return super.createAddCommand(child, constraint);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(CreateRequest)
	 */
	protected Command getCreateCommand(CreateRequest request) {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("CreateRequest : " + request);
		}
		return null;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createChildEditPolicy(EditPart)
	 */
	protected EditPolicy createChildEditPolicy(EditPart child) {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("EditPart : " + child);
		}
		return new ResizableEditPolicy();
	}

	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(Request)
	 */
	protected Command getDeleteDependantCommand(Request request) {
		if (logger.isLoggable(Level.FINE)) {
			//$ANALYSIS-IGNORE
			logger.fine("CreateRequest : " + request);
		}
		return super.getDeleteDependantCommand(request);
	}

	private class LayoutCommand extends Command {

		INode node;
		double x, y, w, h;
		Rectangle oldValue;
		Rectangle newValue;
		ChangeBoundsRequest request;

		public LayoutCommand(INode node, ChangeBoundsRequest request, Rectangle constraint) {
			this.node = node;
			this.request = request;
			newValue = constraint;
			// save old value to use with undo operation
			oldValue = new Rectangle((int) node.getXInLayout(), (int) node.getYInLayout(), (int) node.getWidthInLayout(), (int) node
					.getHeightInLayout());
		}

		/**
		 * @see org.eclipse.gef.commands.Command#canExecute()
		 */
		@Override
		public boolean canExecute() {
			Object type = request.getType();
			return node != null && newValue != null
					&& (RequestConstants.REQ_MOVE_CHILDREN.equals(type) || RequestConstants.REQ_RESIZE_CHILDREN.equals(type));
		}

		/**
		 * @see org.eclipse.gef.commands.Command#execute()
		 */
		@Override
		public void execute() {
			super.execute();
			if (node != null && newValue != null) {
				node.setLocationInLayout(newValue.x, newValue.y);
				node.setSizeInLayout(newValue.width, newValue.height);
			}
		}

		/**
		 * @see org.eclipse.gef.commands.Command#canUndo()
		 */
		@Override
		public boolean canUndo() {
			return node != null && oldValue != null;
		}

		/**
		 * @see org.eclipse.gef.commands.Command#undo()
		 */
		@Override
		public void undo() {
			if (node != null && oldValue != null) {
				node.setLocationInLayout(oldValue.x, oldValue.y);
				node.setSizeInLayout(oldValue.width, oldValue.height);
			}
		}

	};

}
