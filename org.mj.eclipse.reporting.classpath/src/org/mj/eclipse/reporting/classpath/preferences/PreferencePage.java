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
package org.mj.eclipse.reporting.classpath.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.mj.eclipse.reporting.classpath.Activator;

/**
 * @author Mounir Jarraï
 *
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Classpath reporting tools preferences");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various types of preferences.
	 * Each field editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new IntegerFieldEditor(PreferenceConstants.LAYOUT_AREA_WIDTH, "Layout area &width", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.LAYOUT_AREA_HEIGHT, "Layout area &height", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.THREAD_POOL_SIZE, "&Thread Pool size", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.HID_CONNECTION_BY_COST, "&Hide connections with cost > ", getFieldEditorParent()));

//		addField(new BooleanFieldEditor(PreferenceConstants.HID_DIRECT_CONNECTION, "&Hid direct connections", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.LOG_POSSIBLE_PATHS_OCCURENCE, "&Log path occurences", getFieldEditorParent()));
		
		addField(new FontFieldEditor(PreferenceConstants.PROJECT_NAME_FONT, "&Project name Font", getFieldEditorParent()));
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}