/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.cdt.elf;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.ELFhdr;

/**
 * Provides ELF file utilities.
 */
public final class ElfUtils {

	/**
	 * Returns the ELF image type if the specified file is an ELF file at all.
	 *
	 * @param file The file representation of the physical file to test. Must be not <code>null</code>!
	 * @return The ELF image type as defined within <code>org.eclipse.cdt.utils.elf.Elf.Attribute</code>, or <code>-1</code> if invalid or not detectable.
	 */
	public static int getELFType(File file) throws IOException {
		int type = -1;

		if (file != null) {
			Elf elfFile = null;
			try {
				elfFile = new Elf(file.getAbsolutePath());
				type = elfFile.getAttributes().getType();
			}
			finally {
				if (elfFile != null) {
					elfFile.dispose();
				}
				elfFile = null;
			}
		}
		return type;
	}

	/**
	 * Returns the ELF address class if the specified file is an ELF file at all.
	 *
	 * @param file The file representation of the physical file to test. Must be not <code>null</code>!
	 * @return The ELF address class as defined within <code>org.eclipse.cdt.utils.elf.Elf.ELFhdr.ELFCLASS*</code>. <code>ELFCLASSNONE</code> (0) if the ELF address class is not set.
	 */
	public static int getELFClass(File file) throws IOException {
		int elfclass = Elf.ELFhdr.ELFCLASSNONE;

		if (file != null) {
			Elf elfFile = null;
			try {
				elfFile = new Elf(file.getAbsolutePath());
				elfclass = elfFile.getELFhdr().e_ident[ELFhdr.EI_CLASS];
			}
			finally {
				if (elfFile != null) {
					elfFile.dispose();
				}
				elfFile = null;
			}
		}
		return elfclass;
	}

}
