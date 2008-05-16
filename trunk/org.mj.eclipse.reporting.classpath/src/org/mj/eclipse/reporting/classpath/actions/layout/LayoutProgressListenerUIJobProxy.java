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
package org.mj.eclipse.reporting.classpath.actions.layout;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.progress.ProgressEvent;
import org.eclipse.zest.layouts.progress.ProgressListener;


/**
 * @author Mounir Jarraï
 *
 */
abstract class LayoutProgressListenerUIJobProxy extends UIJob implements ProgressListener {

	protected IProgressMonitor monitor;
	protected LayoutAlgorithm algorithm;
	protected IStatus status;

	private int oldStep = 0;

	/**
	 * @param name,
	 *            the job name
	 * @param algorithm,
	 *            the Layout algorithm to be executed by this job.
	 */
	public LayoutProgressListenerUIJobProxy(String name, LayoutAlgorithm algorithm) {
		super(name);
		if (algorithm == null) {
			throw new IllegalArgumentException("LayoutAlgorithm algorithm parameter can't be null");
		}
		this.algorithm = algorithm;
	}

	/**
	 * @see org.eclipse.zest.layouts.progress.ProgressListener#progressStarted(org.eclipse.zest.layouts.progress.ProgressEvent)
	 */
	public void progressStarted(ProgressEvent e) {
		int totalNumberOfSteps = e.getTotalNumberOfSteps();
		int stepsCompleted = e.getStepsCompleted();
		monitor.beginTask(stepsCompleted + "/" + totalNumberOfSteps, totalNumberOfSteps);
	}

	/**
	 * @see org.eclipse.zest.layouts.progress.ProgressListener#progressUpdated(org.eclipse.zest.layouts.progress.ProgressEvent)
	 */
	public void progressUpdated(ProgressEvent e) {
		if (monitor.isCanceled() && algorithm.isRunning()) {
			algorithm.stop();
			status = Status.CANCEL_STATUS;
			return;
		}
		int totalNumberOfSteps = e.getTotalNumberOfSteps();
		int stepsCompleted = e.getStepsCompleted();
		monitor.subTask((String) (stepsCompleted + "/" + totalNumberOfSteps));
		monitor.worked(stepsCompleted - oldStep);
		oldStep = stepsCompleted;
	}

	/**
	 * @see org.eclipse.zest.layouts.progress.ProgressListener#progressEnded(org.eclipse.zest.layouts.progress.ProgressEvent)
	 */
	public void progressEnded(ProgressEvent e) {
		status = Status.OK_STATUS;
		monitor.done();
	}

	/**
	 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		this.monitor = monitor;
		algorithm.addProgressListener(this);
		run(this);
		algorithm.removeProgressListener(this);
		return status;
	}

	public abstract void run(ProgressListener ProgressListener);

}