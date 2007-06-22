package org.perfidix.perclipse.launcher;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.perfidix.perclipse.util.BenchSearchEngine;

public class PerfidixLaunchShortcut implements ILaunchShortcut {

	public class LaunchCancelledByUserException extends Exception {
		private static final long serialVersionUID= 1L;
	}

	public void launch(ISelection selection, String mode) {

		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection) selection).toArray(), mode);
		}

	}

	public void launch(IEditorPart editor, String mode) {
		IJavaElement element = null;
		IEditorInput input = editor.getEditorInput();
		element = (IJavaElement) input.getAdapter(IJavaElement.class);

		if (element != null) {
			searchAndLaunch(new Object[] { element }, mode);
		}
	}

	private void searchAndLaunch(Object[] search, String mode) {
		if (search != null) {
			if (search.length == 0) {
				// TODO Do something real fancy here if no bench is found..
				return;
			}

			if (search[0] instanceof IJavaElement) {
				IJavaElement element = (IJavaElement) search[0];

				// the IJavaElement is a model/container/fragment/package
				if (element.getElementType() < IJavaElement.COMPILATION_UNIT) {
					try {
						launch(mode, describeContainerLaunch(element));
					} catch (LaunchCancelledByUserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				
				// the IJavaElement is a compilation unit/type
				} else if (element.getElementType() 
											== IJavaElement.COMPILATION_UNIT){ 
					launchType(element, mode);
				}
			}
		}
	}

	private void launchType(IJavaElement search, String mode) {
		IType[] types = null;
		try {
			types = BenchSearchEngine.findBenchs(new Object[] { search });
		} catch (InterruptedException e) {
			// TODO Do something real fancy here if exceptions were thrown
			return;
		} catch (InvocationTargetException e) {
			// TODO Do something real fancy here if exceptions were thrown
			return;
		}
		
		IType type = null;
		
		if (types.length == 0 ) {
			// TODO Do something real fancy here if no bench is found..
		} else {	
			type = types[0];
		}

		if (type != null) {
			try {
				launch(mode, describeTypeLaunch(type));
			} catch (LaunchCancelledByUserException e) {
				// OK, silently move on
			}
		}
	}

	/**
	 * Create the description for benching a project/package.
	 * @param container the container to bench
	 * @return
	 */
	public PerfidixLaunchDescription describeContainerLaunch(IJavaElement container) {
		PerfidixLaunchDescription description= new PerfidixLaunchDescription(container, getContainerLabel(container));
		description.setContainer(container.getHandleIdentifier());

		return description;
	}

	/**
	 * Create the description for benching a type (class).
	 * @param type
	 * @return
	 */
	public PerfidixLaunchDescription describeTypeLaunch(IType type) {
		PerfidixLaunchDescription description = new PerfidixLaunchDescription(
				type,
				type.getFullyQualifiedName());
		description.setMainType(type);

		return description;
	}

	private void launch(String mode, PerfidixLaunchDescription description) 
	throws LaunchCancelledByUserException {
		ILaunchConfiguration config = findOrCreateLaunchConfiguration(mode,
				this,
				description);

		if (config != null) {
			DebugUITools.launch(config, mode);
		}
	}

	/**
	 * Check to see if a configuration for this type already exists. If not,
	 * create a new one.
	 * 
	 * @param mode
	 * @param registry
	 * @param description
	 * @return
	 * @throws LaunchCancelledByUserException
	 */
	public ILaunchConfiguration findOrCreateLaunchConfiguration(
			String mode,
			PerfidixLaunchShortcut registry,
			PerfidixLaunchDescription description)
	throws LaunchCancelledByUserException {

		ILaunchConfiguration config= registry.findLaunchConfiguration(mode, description);

		if (config == null) {
			config= registry.createConfiguration(description);
		}
		return config;
	}

	/**
	 * Search for an existing configuration.
	 * 
	 * @param mode
	 * @param description
	 * @return
	 * @throws LaunchCancelledByUserException
	 */
	public ILaunchConfiguration findLaunchConfiguration(
			String mode,
			PerfidixLaunchDescription description)
	throws LaunchCancelledByUserException {

		ILaunchConfigurationType configType= getPerfidixLaunchConfigType();
		List<ILaunchConfiguration> candidateConfigs = new Vector<ILaunchConfiguration>(0);

		try {
			ILaunchConfiguration[] configs= getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs= new ArrayList<ILaunchConfiguration>(configs.length);

			for (int i= 0; i < configs.length; i++) {
				ILaunchConfiguration config= configs[i];
				if (description.attributesMatch(config)) {
					candidateConfigs.add(config);
				}
			}
		} catch (CoreException e) {
			Perclipse.log(e);
		}

		int candidateCount= candidateConfigs.size();

		// return null if no matching configuration was found
		if (candidateCount < 1) {
			return null;

			// TODO: what if more than one matching configuration
		} else {
			return (ILaunchConfiguration) candidateConfigs.get(0);
		}
	}

	/**
	 * Create a configuration from the the perfidix description.
	 * 
	 * @param description
	 * @return
	 */
	public ILaunchConfiguration createConfiguration(
			PerfidixLaunchDescription description) {
		String mainType = description.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME);
		String testName = description.getAttribute(
				PerfidixLaunchConfiguration.BENCH_NAME_ATTR);
		// create the configuration
		return createConfiguration(description.getProject(),
				description.getName(),
				mainType,
				description.getContainer(),
				testName);
	}

	protected ILaunchConfiguration createConfiguration(IJavaProject project,
			String name,
			String mainType,
			String container,
			String benchName) {
		ILaunchConfiguration config= null;

		try {
			ILaunchConfigurationWorkingCopy wc = newWorkingCopy(name);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainType);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
			wc.setAttribute(PerfidixLaunchConfiguration.LAUNCH_CONTAINER_ATTR, container);
			if (benchName.length() > 0)
				wc.setAttribute(PerfidixLaunchConfiguration.BENCH_NAME_ATTR, benchName);  
			config= wc.doSave();    
		} catch (CoreException ce) {
			Perclipse.log(ce);
		}

		return config;
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected ILaunchConfigurationWorkingCopy newWorkingCopy(String name) throws CoreException { 
		ILaunchConfigurationType configType= getPerfidixLaunchConfigType();
		return configType.newInstance(null,
				getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name));
	}

	/**
	 * Returns the local java launch configuration type.
	 * 
	 * @return ILaunchConfigurationType
	 */
	protected ILaunchConfigurationType getPerfidixLaunchConfigType() {
		ILaunchManager lm= getLaunchManager();
		return lm.getLaunchConfigurationType(PerfidixLaunchConfiguration.ID_PERFIDIX_APPLICATION);
	}

	protected String getContainerLabel(IJavaElement container) {
		String name= JavaElementLabels.getTextLabel(container, JavaElementLabels.ALL_FULLY_QUALIFIED);
		return name.substring(name.lastIndexOf(IPath.SEPARATOR) + 1);
	}
}
