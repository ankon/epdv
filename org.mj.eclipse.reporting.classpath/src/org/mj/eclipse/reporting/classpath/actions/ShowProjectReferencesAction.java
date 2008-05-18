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
package org.mj.eclipse.reporting.classpath.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections15.ArrayStack;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.mj.eclipse.reporting.classpath.Activator;
import org.mj.eclipse.reporting.classpath.Editor;
import org.mj.eclipse.reporting.classpath.OnMemoryEditorInput;
import org.mj.eclipse.reporting.classpath.mvc.models.IConnector;
import org.mj.eclipse.reporting.classpath.mvc.models.IDiagram;
import org.mj.eclipse.reporting.classpath.mvc.models.IDiagramElementsFactory;
import org.mj.eclipse.reporting.classpath.mvc.models.INode;
import org.mj.eclipse.reporting.classpath.mvc.models.internal.DiagramModel;
import org.mj.eclipse.reporting.classpath.preferences.PreferenceConstants;

/**
 * @author Mounir Jarraï
 *
 */
public class ShowProjectReferencesAction implements IObjectActionDelegate {

	private static final ILog LOGGER = Activator.getDefault().getLog();

	private ISelection selection;

	/**
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			IDiagram model = null;

			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Object element : structuredSelection.toArray()) {
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}

				if (project != null) {
					// Create model.
					model = new DiagramModel(project);

					final IDiagram modelRef = model;

					// Creating the model
					IProgressMonitor pm = Job.getJobManager().createProgressGroup();
					pm.beginTask("Compute Model", IProgressMonitor.UNKNOWN);
					final ILock lock = Job.getJobManager().newLock();

					Job createModeJob = new Job("Create Model") {
						/**
						 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
						 */
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								lock.acquire();
								return computeModel(modelRef, monitor);
							} finally {
								lock.release();
							}
						}
					};
					createModeJob.setUser(true);
					createModeJob.setProgressGroup(pm, IProgressMonitor.UNKNOWN);
					createModeJob.setThread(new Thread());
					createModeJob.schedule();

					// Creates job : Computing model connection's costs
					final IDiagram workingModel = model;
					Job simplifyModelJob = new Job("Simplify Model") {
						/**
						 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
						 */
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								lock.acquire();
								// IStatus computePathCostSatus = Status.OK_STATUS;
								IStatus computePathCostSatus = computePathCost(workingModel, monitor);
								if (!Status.OK_STATUS.equals(computePathCostSatus)) {
									return computePathCostSatus;
								}

								IStatus editorStatus = null;
								// Open Editor within UI thread.
								if (Display.getCurrent() == null) {
									// Not in UI Thread
									class OpenEditor implements Runnable {
										IStatus status;

										public void run() {
											status = openEditor(workingModel);
										}
									}

									Display display = PlatformUI.getWorkbench().getDisplay();
									OpenEditor openEditor = new OpenEditor();
									display.syncExec(openEditor);
									editorStatus = openEditor.status;
								} else {
									// In UI Thread
									editorStatus = openEditor(workingModel);
								}
								monitor.done();
								return editorStatus;
							} finally {
								lock.release();
							}
						}
					};
					simplifyModelJob.setUser(true);
					simplifyModelJob.setProgressGroup(pm, IProgressMonitor.UNKNOWN);
					simplifyModelJob.setThread(new Thread());
					simplifyModelJob.schedule();

					pm.done();
				}
			}

		}
	}

	/**
	 * @param workingModel
	 */
	private IStatus openEditor(final IDiagram workingModel) {
		try {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			activePage.openEditor(new OnMemoryEditorInput(workingModel), Editor.ID, true);
		} catch (PartInitException e) {
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Oops ! Stupid thing happends when opening editor : "
					+ e.getLocalizedMessage(), e);
			LOGGER.log(status);
			return status;
		}
		return Status.OK_STATUS;
	}

	/**
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/**
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * Creates the model.
	 * 
	 * @param model
	 * @param project
	 * @throws CoreException
	 */
	private IStatus computeModel(final IDiagram model, IProgressMonitor monitor) {
		// Using stack to avoid recursive algorithm
		ArrayStack<IProject> stack = new ArrayStack<IProject>();
		// Used to remember traversed nodes
		Set<IProject> doneSet = new HashSet<IProject>();
		try {
			IProject project = model.getRootProject();

			// Simulate recursive function fist call.
			stack.push(project);
			monitor.beginTask("Create Model", IProgressMonitor.UNKNOWN);
			while (!stack.isEmpty()) {
				// Simulate recursive function exit.
				project = stack.pop();
				doneSet.add(project);
				if (stack.contains(project) && !doneSet.contains(project)) {
					System.out.println("found cycle on " + project + " : " + stack.toString() + "    DONE: " + doneSet.toString());
					continue;
				}
				IProjectDescription description = project.getDescription();
				IProject[] dynamicReferences = description.getDynamicReferences();
				for (IProject referencedProject : dynamicReferences) {
					monitor.subTask("Analyse " + project.getName() + " project dependencies");
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					IConnector createdConnector = model.createConnector(project, referencedProject);
					// Simulate recursive function call.
					if (stack.contains(referencedProject)) {
						//												System.out.println(createdConnector + " : " + stack.toString() + "    DONE: " + doneSet.toString());
					}
					if (!doneSet.contains(referencedProject)) {
						// Found new node that is not already processed.
						stack.push(referencedProject);
					} else {
						// Don't traverse a node that is already traversed.
						// Do nothing
					}

					monitor.worked(1);
				}
			}
			LOGGER.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Dependencies model contains " + model.getProjects().size()
					+ " projects and " + model.getConnectors().size() + " connections"));
			
			long maxDeg = 0;
			long minDeg = Long.MAX_VALUE;
			for (INode node : model.getProjects()) {
				minDeg = Math.min(minDeg, node.getIncamingConnections().size() + node.getOutgoingConnections().size());
				maxDeg = Math.max(maxDeg, node.getIncamingConnections().size() + node.getOutgoingConnections().size());
			}
			LOGGER.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Graph min degree = " + minDeg));
			LOGGER.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Graph max degree = " + maxDeg));
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Oops ! Stupid thing happends", e);
		} finally {
			doneSet.clear();
			stack.clear();
		}
	}

	/**
	 * @param src,
	 *            source node
	 * @param dst,
	 *            target node
	 * @return a <code>List<List<IConnector>></code> instance that contains all possible path between source and target nodes or
	 *         <code>null</code> if operation is canceled
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	private List<List<IConnector>> backTrackingPath(final INode src, final INode dst, final IProgressMonitor monitor) throws Throwable {
		if (src == null || dst == null) {
			throw new IllegalArgumentException("path, src and dst parameters can't be null");
		}

		List<List<IConnector>> allPath = new ArrayList<List<IConnector>>();
		if (src.equals(dst)) {
			return allPath;
		}

		// Using stack to avoid recursive algorithm
		IStack<MemoPoint> stack = null;
		try {
			stack = new OnMemoryStack<MemoPoint>(); //VirtualMemoryStack<MemoPoint>();
			// Simulate recursive function fist call.
			stack.push(new MemoPoint(src, new ArrayList<IConnector>(), new ArrayStack<INode>()));
			while (!stack.isEmpty()) {
				// Simulate recursive function exit.
				MemoPoint memoPoint = stack.pop();
				Collection<IConnector> outgoingConnection = memoPoint.src.getOutgoingConnections();
				for (IConnector connector : outgoingConnection) {
					if (connector.isInCycle()) {
						continue;
					}
					// Makes a copy of the current path to enable back tracking if no solution is found.
					ArrayList<IConnector> subPath = (ArrayList<IConnector>) memoPoint.path.clone();
					subPath.add(connector);
					
					INode connectorSource = connector.getSource();
					INode connectorTarget = connector.getTarget();
					if (!connectorTarget.equals(src) && !connectorTarget.equals(memoPoint.src)) {
						// -- VERSION 2: Check for cycle --						
						int cycleStart = -1;
						int cycleEnd = -1;
						ArrayStack<INode> compressedSubPath = (ArrayStack<INode>) memoPoint.compressedPath.clone(); // using only nodes (No edge) {startNode, nextNode, nextNode, ..., endNode}
						if (compressedSubPath.isEmpty()) {
							// Add startNode
							compressedSubPath.push(connectorSource);
							compressedSubPath.push(connectorTarget);
						} else if (compressedSubPath.contains(connectorTarget)) {
							// CYCLE FOUND
							cycleStart = compressedSubPath.indexOf(connectorTarget);
							cycleEnd = compressedSubPath.size() - 1;
						} else {
							// Add nextNode
							compressedSubPath.push(connectorTarget);
						}
						if (cycleStart != -1 && cycleEnd != -1) {
								ArrayList<IConnector> cyclePath = new ArrayList<IConnector>(cycleEnd - cycleStart + 1);
							// mark cycle
							for (int i = cycleStart; i <= cycleEnd; i++) {
								IConnector inCycleConnector = subPath.get(i);
								inCycleConnector.setInCycle(true);
								cyclePath.add(inCycleConnector);
							}
							Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Cycle found : " + cyclePath
									+ " as subset of path : " + subPath);
							LOGGER.log(status);
							continue;
						}
						// -- VERSION 2 :  End cycle detection as subset of path --

						if (connectorTarget.equals(dst)) {
							// Path from src to dst is found
							allPath.add(subPath);
							continue;
						} else {
							// Simulate recursive function call.
							MemoPoint mp = new MemoPoint(connectorTarget, subPath, compressedSubPath);
							stack.push(mp);
						}
					} else {
						// CYCLE FOUND = subPath
						Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Cycle found in path : " + subPath);
						LOGGER.log(status);
						for (IConnector inCycleConnector : subPath) {
							inCycleConnector.setInCycle(true);
						}
						continue;
					}
				}
			}
			return allPath;
		} catch (Throwable t) {
			throw t;
		} finally {
			if (stack != null) {
				stack.clear();
			}
		}
	}

	/**
	 * @param model
	 * @param monitor
	 * @return
	 */
	private IStatus computePathCost(final IDiagram model, final IProgressMonitor monitor) {
		List<IConnector> connectors = model.getConnectors();

		// Initialize thread pool
		ExecutorService threadPool = Executors.newFixedThreadPool(Activator.getDefault().getPluginPreferences().getInt(
				PreferenceConstants.THREAD_POOL_SIZE));

		monitor.beginTask("Compute connections costs", connectors.size());
		Collection<Callable<IStatus>> callables = new ArrayList<Callable<IStatus>>(connectors.size());

		long startTime = System.currentTimeMillis();
		for (final IConnector connector : connectors) {
			//			if (monitor.isCanceled()) {
			//				return Status.CANCEL_STATUS;
			//			}
			Callable<IStatus> callable = new
			/**
			 * @author bipbip
			 * 
			 */
			Callable<IStatus>() {
				/**
				 * @see java.util.concurrent.Callable#call()
				 */
				public IStatus call() throws Exception {
					synchronized (monitor) {
						monitor.subTask("Compute path " + connector + " cost");
					}

					// Back Tracing path
					List<List<IConnector>> allPath;
					try {
						allPath = backTrackingPath(connector.getSource(), connector.getTarget(), monitor);
						if (allPath == null) {
							return Status.CANCEL_STATUS;
						}
					} catch (Throwable t) {
						throw new Exception(t);
					}
					synchronized (LOGGER) {
						if (Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.LOG_POSSIBLE_PATHS_OCCURENCE)) {
							Status status = new Status(IStatus.INFO, Activator.PLUGIN_ID, allPath.size() + " possible paths for "
									+ connector);
							LOGGER.log(status);
						}
					}
					// compute path cost
					int maxCost = connector.getCost();
					for (List<IConnector> path : allPath) {
						synchronized (monitor) {
							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
						}
						int pathCost = 0;
						for (IConnector subConnector : path) {
							pathCost += subConnector.getCost();
						}
						maxCost = Math.max(maxCost, pathCost);
					}
					connector.setCost(maxCost);
					synchronized (monitor) {
						monitor.worked(1);
					}
					return Status.OK_STATUS;
				}
			};
			callables.add(callable);
		}

		List<Future<IStatus>> futures = null;
		try {
			futures = threadPool.invokeAll(callables);
			threadPool.shutdownNow();
			long endTime = System.currentTimeMillis();
			long timeInSec = (endTime - startTime) / 1000;
			long sec = timeInSec % 60;
			long min = (timeInSec - sec) / 60;
			LOGGER.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "computePathCost takes " + (endTime - startTime) + "ms (" + min + "m "
					+ sec + "s) when using a pool of "
					+ Activator.getDefault().getPluginPreferences().getInt(PreferenceConstants.THREAD_POOL_SIZE) + "thread"));
			// Check sub callables status.
			for (Future<IStatus> future : futures) {
				if (!future.get().isOK()) {
					return future.get();
				}
			}
		} catch (InterruptedException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Oops ! Stupid thing happends", e);
		} catch (ExecutionException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Oops ! Stupid thing happends", e);
		}
		return Status.OK_STATUS;
	}

	//******************************************************************************************************************************
	// Sample recursive implementation. Used as documentation.
	// DO NOT DELETE.
	//******************************************************************************************************************************

	/**
	 * @param allPath
	 * @param path
	 * @param src
	 * @param dst
	 */
	private void backTrackingPathRecursively(final List<List<IConnector>> allPath, final List<IConnector> path, INode src, INode dst) {
		if (allPath == null || path == null || src == null || dst == null) {
			throw new IllegalArgumentException("allPath, path, src and dst parameters can't be null");
		}

		if (src.equals(dst)) {
			return;
		}

		Collection<IConnector> outgoingConnection = src.getOutgoingConnections();
		for (Iterator<IConnector> iterator = outgoingConnection.iterator(); iterator.hasNext();) {
			IConnector connector = iterator.next();
			ArrayList<IConnector> subpath = new ArrayList<IConnector>(path);
			subpath.add(connector);
			if (connector.getTarget().equals(dst)) {
				allPath.add(subpath);
				continue;
			} else {
				backTrackingPathRecursively(allPath, subpath, connector.getTarget(), dst);
			}
		}

	}

	/**
	 * @param project
	 * @throws CoreException
	 */
	private void computeModelRecusively(IDiagramElementsFactory model, IProject project) {
		try {
			IProjectDescription description = project.getDescription();

			//			IProject[] referencedProjects = description.getReferencedProjects();
			//			for (int i = 0; i < referencedProjects.length; i++) {
			//				IProject referencedProject = referencedProjects[i];
			//				//$ANALYSIS-IGNORE
			//				System.out.println(project.getName() + "->" + referencedProject.getName());
			//
			//				IConnector connector = new IConnector(project,  referencedProject);
			//				model.addConnector(connector);
			//
			//				computeModel(model, referencedProject);
			//			}

			IProject[] dynamicReferences = description.getDynamicReferences();
			for (int i = 0; i < dynamicReferences.length; i++) {
				IProject referencedProject = dynamicReferences[i];
				model.createConnector(project, referencedProject);
				computeModelRecusively(model, referencedProject);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}

/**
 * @author mjarraï
 * 
 */
final class MemoPoint implements Serializable {
	ArrayList<IConnector> path;
	INode src;
	ArrayStack<INode> compressedPath;// using only nodes (No edge) {startNode, nextNode, nextNode, ..., endNode}

	/**
	 * @param src
	 * @param path
	 */
	MemoPoint(INode src, ArrayList<IConnector> path, ArrayStack<INode> compressedPath) {
		this.path = path;
		this.src = src;
		this.compressedPath = compressedPath;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.src.toString() + " : " + this.path.toString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null)
				? 0
				: path.hashCode());
		result = prime * result + ((src == null)
				? 0
				: src.hashCode());
		return result;
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
		final MemoPoint other = (MemoPoint) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}
}
