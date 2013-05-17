/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.fusesource.ide.camel.editor.preferences;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.fusesource.camel.tooling.util.Languages;
import org.fusesource.ide.camel.editor.EditorMessages;
import org.fusesource.ide.preferences.PreferenceManager;
import org.fusesource.ide.preferences.PreferencesConstants;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class RiderEditorPreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {

	private ComboFieldEditor defaultLanguageEditor;
	private BooleanFieldEditor preferIdAsLabelEditor;
	private ComboFieldEditor layoutOrientationEditor;
	private BooleanFieldEditor gridVisibilityEditor;

	/**
	 * 
	 */
	public RiderEditorPreferencePage() {
		super(GRID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		String[][] namesAndValues = new Languages().nameAndLanguageArray();

		this.defaultLanguageEditor = new ComboFieldEditor(
				PreferencesConstants.EDITOR_DEFAULT_LANGUAGE,
				EditorMessages.editorPreferencePageDefaultLanguageSetting,
				namesAndValues, getFieldEditorParent());

		addField(this.defaultLanguageEditor);

		this.preferIdAsLabelEditor = new BooleanFieldEditor(
				PreferencesConstants.EDITOR_PREFER_ID_AS_LABEL,
				EditorMessages.editorPreferencePagePreferIdAsLabelSetting,
				getFieldEditorParent());

		addField(this.preferIdAsLabelEditor);

		namesAndValues = new String[][] {
				{ EditorMessages.editorPreferencePageLayoutOrientationEAST,  String.valueOf(PositionConstants.EAST) },
				{ EditorMessages.editorPreferencePageLayoutOrientationSOUTH, String.valueOf(PositionConstants.SOUTH) }
		};

		this.layoutOrientationEditor = new ComboFieldEditor(
				PreferencesConstants.EDITOR_LAYOUT_ORIENTATION,
				EditorMessages.editorPreferencePageLayoutOrientationSetting,
				namesAndValues, getFieldEditorParent());

		addField(this.layoutOrientationEditor);

		this.gridVisibilityEditor = new BooleanFieldEditor(
				PreferencesConstants.EDITOR_GRID_VISIBILITY,
				EditorMessages.editorPreferencePageGridVisibilitySetting,
				getFieldEditorParent());

		addField(this.gridVisibilityEditor);

		// Sets up the context sensitive help for this page
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getFieldEditorParent(), "org.fusesource.ide.camel.editor.editorConfig");
	}

	/* (non-Javadoc)
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(PreferenceManager.getInstance().getUnderlyingStorage());
		setDescription(EditorMessages.editorPreferencePageDescription);
	}


	// IWorkbenchPropertyPage API
	@Override
	public IAdaptable getElement() {
		return null;
	}

	/**
	 * The element passed in is the current selection DiagramEditPart in the diagram if opened via File -> Properties
	 */
	@Override
	public void setElement(IAdaptable element) {
		//System.out.println("====== set from element: " + element);
		setPreferenceStore(PreferenceManager.getInstance().getUnderlyingStorage());
		setDescription(EditorMessages.editorPreferencePageDescription);
	}

}