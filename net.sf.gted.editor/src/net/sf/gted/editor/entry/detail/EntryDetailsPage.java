/*
 gted - gted.sourceforge.net
 Copyright (C) 2013 by Simon Martinelli, Erlach, Switzerland

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.sf.gted.editor.entry.detail;

import net.sf.gted.editor.POFileEditorPlugin;
import net.sf.gted.editor.entry.detail.preferences.ReferenceTableContentProvider;
import net.sf.gted.editor.entry.detail.preferences.ReferenceTableLabelProvider;
import net.sf.gted.editor.entry.master.POMasterDetailsBlock;
import net.sf.gted.editor.preferences.PreferenceConstants;
import net.sf.gted.editor.util.OpenEditorHelper;
import net.sf.gted.model.POEntry;
import net.sf.gted.model.POReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IManagedForm;

/**
 * EntryDetailsPage.
 * 
 * @author $Author: simas_ch $
 * @version $Revision: 1.7 $, $Date: 2009/09/22 11:24:26 $
 */
public abstract class EntryDetailsPage implements IDetailsPage {

	protected IManagedForm managedForm;

	protected boolean dirty;

	protected boolean filled;

	protected final String[] columnNames = { "File", "Line" };
	
	protected TableViewer viewer;

	protected Table table;

	protected POMasterDetailsBlock block;
 
	protected Text msgCtxtText;
 
	protected Text msgIdText;
 
	protected Button fuzzyButton;
 
	protected Text commentsText;

	/**
	 * Initialize the details page
	 * 
	 * @param form
	 */
	public void initialize(IManagedForm form) {
		managedForm = form;
	}

	/**
	 * Create a references table viewer. @note the given viewer will be
	 * overriden.
	 * 
	 * @param viewer
	 *            table viewer reference which should be recreated
	 * @param entry
	 *            current selected po entry
	 * @param table
	 *            the tabe
	 * @param columnNames
	 *            column names
	 * @param poFile
	 *            source po file
	 */
	protected void createReferencesViewer(TableViewer viewer, POEntry entry,
			Table table, String[] columnNames, final IFile poFile) {
		viewer = new TableViewer(table);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(columnNames);

		viewer.setContentProvider(new ReferenceTableContentProvider());
		viewer.setLabelProvider(new ReferenceTableLabelProvider());
		viewer.setInput(entry);

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(final DoubleClickEvent event) {
				final Object obj = ((StructuredSelection) event.getSelection())
						.getFirstElement();
				if (obj instanceof POReference) {
					final POReference reference = (POReference) obj;

					final Integer lineNumber = reference.lineAsInteger();
					
					IProject project = poFile.getProject();

					String filename = applySourceFilePathPrefixIfPresent(project,reference);

					final IFile sourcefile = poFile.getProject().getFile(
							filename);

					OpenEditorHelper.openEditor(sourcefile, lineNumber);
				}
			}


		});

	}

	// read source reference prefix and apply to filename if exists
	private static String applySourceFilePathPrefixIfPresent(
			final IProject project,
			final POReference reference) {
		String referencePrefix = null;
		try {
			final String project_enable = project
					.getPersistentProperty(new QualifiedName(
							PreferenceConstants.P_QUALIFIER,
							PreferenceConstants.P_SOURCE_REFERENCE_ENABLE));
			if(Boolean.valueOf(project_enable)) {
				referencePrefix = project
						.getPersistentProperty(new QualifiedName(
								PreferenceConstants.P_QUALIFIER,
								PreferenceConstants.P_SOURCE_REFERENCE_PREFIX));
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (referencePrefix == null) {
			referencePrefix = POFileEditorPlugin
					.getDefault()
					.getPreferenceStore().getString(
							PreferenceConstants.P_SOURCE_REFERENCE_PREFIX);
		}

		boolean applyPrefix = referencePrefix != null
				&& referencePrefix.length() > 0;
		if(applyPrefix) {
			if (!referencePrefix.endsWith("/")
					&& !reference.filename.startsWith("/")) {
				referencePrefix += "/";
			}
			return referencePrefix + reference.filename;
		}

		return reference.filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#dispose()
	 */
	public void dispose() {
		// Dispose
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#setFocus()
	 */
	public void setFocus() {
		// Set focus
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#setFormInput(java.lang.Object)
	 */
	public boolean setFormInput(Object input) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
	 */
	public void commit(boolean onSave) {
		changeDirty(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#isDirty()
	 */
	public boolean isDirty() {
		return dirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#isStale()
	 */
	public boolean isStale() {
		return false;
	}

	/**
	 * @param b
	 */
	protected void changeDirty(boolean b) {
		if (managedForm != null) {
			dirty = b;
			managedForm.dirtyStateChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#refresh()
	 */
	public void refresh() {
		update();
	}
	
	protected void update_common_widgets(POEntry entry) {
			
		String msgCtxt = entry.getMsgCtxt();
		if (msgCtxt != null) {
			this.msgCtxtText.setText(msgCtxt);
		} else {
			this.msgCtxtText.setText("");
		}
		
		String msgId = entry.getMsgId();
		msgIdText.setText(msgId);

		boolean fuzzy = entry.isFuzzy();
		fuzzyButton.setSelection(fuzzy);

		this.commentsText.setText(entry.getTranslaterCommentsAsString());

		IFile file = block.getPage().getEditor().getPoEditor().getIFile();
		createReferencesViewer(viewer, entry, table, columnNames, file);
	}

	/**
   * 
   */
	protected abstract void update();

}