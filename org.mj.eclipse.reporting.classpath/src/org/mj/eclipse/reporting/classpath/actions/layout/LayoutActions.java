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
package org.mj.eclipse.reporting.classpath.actions.layout;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.VerticalLayoutAlgorithm;

/**
 * @author Mounir Jarraï
 * 
 */
public final class LayoutActions {

	public static final String SPRING_LAYOUT_ACTION_ID = "Spring Layout";
	public static final String DIRECTED_GRAPH_LAYOUT_ACTION_ID = "Directed Graph Layout";
	public static final String GRID_LAYOUT_ACTION_ID = "Grid Layout";
	public static final String HORIZONTAL_LAYOUT_ACTION_ID = "Horizontal Layout";
	public static final String VERTICAL_LAYOUT_ACTION_ID = "Vertical Layout";
	public static final String HORIZONTAL_SHIFT_LAYOUT_ACTION_ID = "Horizontal Shift Layout";
	public static final String TREE_LAYOUT_ACTION_ID = "Tree Layout";
	public static final String HORIZONTAL_TREE_LAYOUT_ACTION_ID = "Horizontal Tree Layout";
	public static final String RADIAL_LAYOUT_ACTION_ID = "Radial Layout";

	private GraphicalEditor workbenchPart;

	private ActionRegistry actionRegistry;

	private Action springLayoutAction;
	private Action directedGraphLayoutAction;
	private Action gridLayoutAction;
	private Action horizontalLayoutAction;
	private Action verticalLayoutAction;
	private Action horizontalShiftLayoutAction;
	private Action treeLayoutAction;
	private Action horizontalTreeLayoutAction;
	private Action radialLayoutAction;

	/**
	 * @param workbenchPart
	 */
	public LayoutActions(IWorkbenchPart workbenchPart) {
		this.workbenchPart = (GraphicalEditor) workbenchPart;
		initActions();

		Object actionRegistry = this.workbenchPart.getAdapter(ActionRegistry.class);
		if (actionRegistry != null) {
			this.actionRegistry = (ActionRegistry) actionRegistry;
			registerActions();
		}
	}

	public void applyDefaultLayout() {
		directedGraphLayoutAction.run();
	}

	/**
	 * 
	 */
	private void registerActions() {
		actionRegistry.registerAction(springLayoutAction);
		actionRegistry.registerAction(directedGraphLayoutAction);
		actionRegistry.registerAction(gridLayoutAction);
		actionRegistry.registerAction(horizontalLayoutAction);
		actionRegistry.registerAction(verticalLayoutAction);
		actionRegistry.registerAction(horizontalShiftLayoutAction);
		actionRegistry.registerAction(treeLayoutAction);
		actionRegistry.registerAction(horizontalTreeLayoutAction);
		actionRegistry.registerAction(radialLayoutAction);
	}

	/**
	 * 
	 */
	private void initActions() {
		springLayoutAction = new LayoutAction(this.workbenchPart, new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return SPRING_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return SPRING_LAYOUT_ACTION_ID;
			}

		};

		CompositeLayoutAlgorithm algorithm = new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] {
				new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING),
				new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING) });

		//		directedGraphLayoutAction = new LayoutAction(this.workbenchPart, new DirectedGraphLayoutAlgorithm(
		//				LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {
		directedGraphLayoutAction = new LayoutAction(this.workbenchPart, algorithm) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return DIRECTED_GRAPH_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return DIRECTED_GRAPH_LAYOUT_ACTION_ID;
			}
		};

		gridLayoutAction = new LayoutAction(this.workbenchPart, new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return GRID_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return GRID_LAYOUT_ACTION_ID;
			}
		};

		horizontalLayoutAction = new LayoutAction(this.workbenchPart, new HorizontalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return HORIZONTAL_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return HORIZONTAL_LAYOUT_ACTION_ID;
			}
		};

		verticalLayoutAction = new LayoutAction(this.workbenchPart, new VerticalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return VERTICAL_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return VERTICAL_LAYOUT_ACTION_ID;
			}
		};

		horizontalShiftLayoutAction = new LayoutAction(this.workbenchPart, new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return HORIZONTAL_SHIFT_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return HORIZONTAL_SHIFT_LAYOUT_ACTION_ID;
			}
		};

		treeLayoutAction = new LayoutAction(this.workbenchPart, new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return TREE_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return TREE_LAYOUT_ACTION_ID;
			}
		};

		horizontalTreeLayoutAction = new LayoutAction(this.workbenchPart, new HorizontalTreeLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return HORIZONTAL_TREE_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return HORIZONTAL_TREE_LAYOUT_ACTION_ID;
			}
		};

		radialLayoutAction = new LayoutAction(this.workbenchPart, new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING)) {

			/**
			 * @see org.eclipse.jface.action.Action#getId()
			 */
			@Override
			public String getId() {
				return RADIAL_LAYOUT_ACTION_ID;
			}

			/**
			 * @see org.eclipse.jface.action.Action#getText()
			 */
			@Override
			public String getText() {
				return RADIAL_LAYOUT_ACTION_ID;
			}
		};
	}
}
