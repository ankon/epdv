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

import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutGraph;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;
import org.mj.eclipse.reporting.classpath.Activator;
import org.mj.eclipse.reporting.classpath.mvc.models.IConnector;
import org.mj.eclipse.reporting.classpath.preferences.PreferenceConstants;

/**
 * @author Mounir Jarraï
 *
 */
abstract class LayoutAction extends SelectionAction {

	private static final ILog LOGGER = Activator.getDefault().getLog();

	private LayoutAlgorithm algorithm;

	public LayoutAction(IWorkbenchPart part, LayoutAlgorithm algorithm) {
		super(part);
		if (algorithm == null) {
			throw new IllegalArgumentException("LayoutAlgorithm algorithm parameter can't be null");
		}
		this.algorithm = algorithm;
	}

	/**
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		LayoutProgressListenerUIJobProxy job = new LayoutProgressListenerUIJobProxy("Computing layout", this.algorithm) {
			@Override
			public void run(ProgressListener ProgressListener) {
				RootEditPart adapter = (RootEditPart) getWorkbenchPart().getAdapter(EditPart.class);
				if (adapter != null && adapter.getContents() != null) {
					LayoutGraph model = (LayoutGraph) adapter.getContents().getModel();
					doLayout(model, this.algorithm);
				}
			}

		};
		job.setProperty(IProgressConstants.KEEPONE_PROPERTY, Boolean.TRUE);
		job.setThread(new Thread());
		job.setUser(true);
		job.schedule();
	}

	/**
	 * @param layoutGraph
	 * @param algorithm
	 */
	@SuppressWarnings("unchecked")
	public static void doLayout(final LayoutGraph layoutGraph, final LayoutAlgorithm algorithm) {

		List<LayoutEntity> entities = layoutGraph.getEntities();
		LayoutEntity[] array = entities.toArray(new LayoutEntity[entities.size()]);

		List<LayoutRelationship> relationships = layoutGraph.getRelationships();
		LayoutRelationship[] array2 = relationships.toArray(new LayoutRelationship[relationships.size()]);

		Preferences pluginPreferences = Activator.getDefault().getPluginPreferences();
		final int maxCoste = pluginPreferences.getInt(PreferenceConstants.HID_CONNECTION_BY_COST);

		try {
			algorithm.setFilter(new Filter() {

				/**
				 * @see org.eclipse.zest.layouts.Filter#isObjectFiltered(java.lang.Object)
				 */
				public boolean isObjectFiltered(Object object) {
					if (object instanceof IConnector) {
						IConnector connector = (IConnector) object;
						return !connector.isInCycle() && connector.getCost() > maxCoste;
					}
					return false;
				}

			});
			algorithm.applyLayout(array, array2, 0, 0, pluginPreferences.getDouble(PreferenceConstants.LAYOUT_AREA_WIDTH),
					pluginPreferences.getDouble(PreferenceConstants.LAYOUT_AREA_HEIGHT), false, false);
			// Compute new location delta
			double dx = Double.MAX_VALUE;
			double dy = Double.MAX_VALUE;
			for (LayoutEntity entity : entities) {
				dx = Math.min(dx, entity.getXInLayout());
				dy = Math.min(dy, entity.getYInLayout());
			}
			// Apply new location
			for (LayoutEntity entity : entities) {
				entity.setLocationInLayout(10 + entity.getXInLayout() - dx, 10 + entity.getYInLayout() - dy);
			}
		} catch (InvalidLayoutConfiguration e) {
			LOGGER.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}
	}
}