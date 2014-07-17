/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.fusesource.ide.server.karaf.core.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
/**
 * SourcePathComputer for the GenericLaunchConfiguration.
 * 
 * @author Gorkem Ercan
 */
public class SourcePathComputerDelegate implements ISourcePathComputerDelegate  {
	public static final String ATTR_SERVER_ID = "server-id";
	public String getId() {
		return "org.fusesource.ide.server.karaf.core.server.sourcePathComputerDelegate";
	}
	/**
	 * Computes the default source lookup path for the launch configuration. The
	 * default source lookup path is the folder or project containing all JavaScript
	 * source files. If the folder is not specified, the workspace is searched by
	 * default.
	 */

	  public ISourceContainer[] computeSourceContainers(
	      ILaunchConfiguration config, IProgressMonitor monitor)
	      throws CoreException {
		  IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		  String serverId = config.getAttribute(ATTR_SERVER_ID, (String) null);
		  IServer server = ServerCore.findServer(serverId);
		  IModule[] modules = server.getModules();	
		  
	    IProject project = wsRoot.getProject(getProjectName(config));

	    //IModule[] modules = ServerUtil.getModules(project);

	    //List<FolderSourceContainer> sourcefolderList = new ArrayList<FolderSourceContainer>();
	    
	    IFolder moduleFolder = project.getFolder("camel-blueprint-paul");
		//if (moduleFolder.exists()) {
		//	sourcefolderList.add(new FolderSourceContainer(moduleFolder, true));
		//}
		ISourceContainer sourceContainer = new FolderSourceContainer(moduleFolder, false);

	    //if (sourceContainer == null) {
	     // sourceContainer = new WorkspaceSourceContainer();
	    //}
	    return new ISourceContainer[] { sourceContainer };
	  }

	  private String getProjectName(ILaunchConfiguration config) throws CoreException {
		  return "camel-blueprint-paul";
	    //return config.getAttribute(LaunchType.CHROMIUM_DEBUG_PROJECT_NAME, (String) null);
	  }
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainersX(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
	
		System.out.println("Fuse source path computer called...");
		
		IRuntimeClasspathEntry[] unresolvedEntries = JavaRuntime.computeUnresolvedSourceLookupPath(configuration);
		List<FolderSourceContainer> sourcefolderList = new ArrayList<FolderSourceContainer>();
		
		IServer server =  ServerUtil.getServer(configuration);
		if (server == null)
			return null;
		
		IModule[] modules = server.getModules();
		
		List<IJavaProject> javaProjectList = new ArrayList<IJavaProject>();
		
		processModules(sourcefolderList, modules, javaProjectList, server,monitor);

		IRuntimeClasspathEntry[] projectEntries = new IRuntimeClasspathEntry[javaProjectList.size()];
		for (int i = 0; i < javaProjectList.size(); i++) {
			projectEntries[i] = JavaRuntime.newProjectRuntimeClasspathEntry(javaProjectList.get(i)); 
		}
		IRuntimeClasspathEntry[] entries =  new IRuntimeClasspathEntry[projectEntries.length+unresolvedEntries.length]; 
		System.arraycopy(unresolvedEntries,0,entries,0,unresolvedEntries.length);
		System.arraycopy(projectEntries,0,entries,unresolvedEntries.length,projectEntries.length);
		
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
		ISourceContainer[] javaSourceContainers = JavaRuntime.getSourceContainers(resolved);
		
		if (!sourcefolderList.isEmpty()) {
			ISourceContainer[] combinedSourceContainers = new ISourceContainer[javaSourceContainers.length + sourcefolderList.size()];
			sourcefolderList.toArray(combinedSourceContainers);
			System.arraycopy(javaSourceContainers, 0, combinedSourceContainers, sourcefolderList.size(), javaSourceContainers.length);
			javaSourceContainers = combinedSourceContainers;
		}
		
		return javaSourceContainers;
		
	}

	private void processModules(List<FolderSourceContainer> sourcefolderList, IModule[] modules, List<IJavaProject> javaProjectList, IServer server, IProgressMonitor monitor) {
		for (int i = 0; i < modules.length; i++) {
			IModule[] cModule = server.getChildModules(new IModule[] {modules[i]}, monitor);
			if (cModule != null && cModule.length>0) {
				processModules(sourcefolderList, cModule, javaProjectList, server, monitor);
			}
			IProject project = modules[i].getProject();
			if (project != null && project.isAccessible()) {
				IFolder moduleFolder = project.getFolder(modules[i].getName());
				if (moduleFolder.exists()) {
					sourcefolderList.add(new FolderSourceContainer(moduleFolder, true));
				} else {
					try {
						if (project.hasNature(JavaCore.NATURE_ID)) {
							IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
							if(!javaProjectList.contains(javaProject)){
								javaProjectList.add(javaProject);
							}
						}
					} catch (CoreException e) {
						//JBossServerCorePlugin.log(e.getStatus());
					}
				}
			}
		}
	}
}
