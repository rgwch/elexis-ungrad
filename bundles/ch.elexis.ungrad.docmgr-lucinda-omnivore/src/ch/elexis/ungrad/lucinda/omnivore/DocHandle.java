package ch.elexis.ungrad.lucinda.omnivore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.omnivore.data.Messages;
import ch.elexis.omnivore.data.Preferences;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Log;
import ch.rgw.tools.TimeTool;

/**
 * Since there's no more DocHandle in the omnivore-plugin, we have to recreate a
 * minimal version here
 */
public class DocHandle extends PersistentObject {
	private static final String CATEGORY_MIMETYPE = "text/category";

	private static Logger log = LoggerFactory.getLogger(DocHandle.class);

	private TimeTool toStringTool = new TimeTool();

	private static final String FLD_CAT = "Cat"; //$NON-NLS-1$
	private static final String FLD_TITLE = "Titel"; //$NON-NLS-1$
	private static final String FLD_MIMETYPE = "Mimetype"; //$NON-NLS-1$
	private static final String FLD_DOC = "Doc"; //$NON-NLS-1$
	private static final String FLD_KEYWORDS = "Keywords"; //$NON-NLS-1$
	private static final String FLD_PATID = "PatID"; //$NON-NLS-1$
	private static final String FLD_CREATION_DATE = "CreationDate"; //$NON-NLS-1$

	static final String TABLENAME = "CH_ELEXIS_OMNIVORE_DATA"; //$NON-NLS-1$

	@Override
	public String getTableName() {
		return TABLENAME;
	}

	public DocHandle(String id) {
		super(id);
	}

	public DocHandle() {
	}

	public static DocHandle load(String id) {
		return new DocHandle(id);
	}

	/**
	 * If force is set or the preference Preferences.STOREFS is true a new File
	 * object is created. Else the file is a BLOB in the db and null is returned.
	 * 
	 * The path of the new file will be: Preferences.BASEPATH/PatientCode/
	 * 
	 * The name of the new file will be: PersistentObjectId.FileExtension
	 * 
	 * @param force access to the file system
	 * @return File to read from, or write to, or null
	 */

	private File getStorageFile(boolean force) {
		if (force || Preferences.storeInFilesystem()) {
			String pathname = Preferences.getBasepath();
			if (pathname != null) {

				File dir = new File(pathname);
				if (dir.isDirectory()) {
					Patient pat = Patient.load(get(FLD_PATID));
					File subdir = new File(dir, pat.getPatCode());
					if (!subdir.exists()) {
						subdir.mkdir();
					}
					File file = new File(subdir, getId() + "." //$NON-NLS-1$
							+ FileTool.getExtension(get(FLD_MIMETYPE)));
					return file;
				}

			}

			if (Preferences.storeInFilesystem()) {
				log.warn("config error");
			}

		}

		return null;
	}

	private String getCategoryName() {
		return checkNull(get(FLD_CAT));
	}

	private boolean isCategory() {
		return get(FLD_MIMETYPE).equals(CATEGORY_MIMETYPE);
	}

	/*
	 * public DocHandle getCategoryDH() { String name = getCategoryName(); if
	 * (!StringTool.isNothing(name)) { List<DocHandle> ret = new
	 * Query<DocHandle>(DocHandle.class, FLD_TITLE, name).execute(); if (ret != null
	 * && ret.size() > 0) { return ret.get(0); } } return null; }
	 */

	@Override
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		// avoid adding only a space - causes trouble in renaming of categories
		String date = get(FLD_DATE);
		if (date != null && !date.isEmpty()) {
			sb.append(get(FLD_DATE));
			sb.append(StringConstants.SPACE);
		}
		sb.append(get(FLD_TITLE));
		return sb.toString();
	}

	String getTitle() {
		return get(FLD_TITLE);
	}

	String getKeywords() {
		return get(FLD_KEYWORDS);
	}

	public String getDate() {
		toStringTool.set(get(FLD_DATE));
		return toStringTool.toString(TimeTool.DATE_GER);
	}

	public String getCreationDate() {
		toStringTool.set(get(FLD_CREATION_DATE));
		return toStringTool.toString(TimeTool.DATE_GER);

	}

	byte[] getContents() {
		byte[] ret = getBinary(FLD_DOC);
		if (ret == null) {
			File file = getStorageFile(true);
			if (file != null) {
				try {
					byte[] bytes = Files.readAllBytes(Paths.get(file.toURI()));
					// if we stored the file in the file system but decided
					// later to store it in the
					// database: copy the file from the file system to the
					// database
					if (!Preferences.storeInFilesystem()) {
						try {
							setBinary(FLD_DOC, bytes);
						} catch (PersistenceException pe) {
							SWTHelper.showError(Messages.DocHandle_readErrorCaption,
									Messages.DocHandle_importErrorText + "; " + pe.getMessage());
						}
					}

					return bytes;
				} catch (Exception ex) {
					ExHandler.handle(ex);
					SWTHelper.showError(Messages.DocHandle_readErrorHeading, Messages.DocHandle_importError2,
							MessageFormat.format(Messages.DocHandle_importErrorText2 + ex.getMessage(),
									file.getAbsolutePath()));
				}
			}
		}
		return ret;
	}

	public void execute() {
		try {
			String ext = StringConstants.SPACE; // "";//$NON-NLS-1$
			File temp = createTemporaryFile(getTitle());
			log.debug("execute {} readable {}", temp.getAbsolutePath(), Files.isReadable(temp.toPath()));

			Program proggie = Program.findProgram(ext);
			if (proggie != null) {
				proggie.execute(temp.getAbsolutePath());
			} else {
				if (Program.launch(temp.getAbsolutePath()) == false) {
					Runtime.getRuntime().exec(temp.getAbsolutePath());
				}

			}

		} catch (Exception ex) {
			ExHandler.handle(ex);
			SWTHelper.showError("Kann Dokument nicht öffnen", ex.getMessage());
		}
	}

	/**
	 * create a temporary file
	 * 
	 * @return temporary file
	 **/
	public File createTemporaryFile(String title) {

		String fileExtension = FileTool.getExtension(get(FLD_TITLE));

		if (fileExtension == null) {
			fileExtension = "";
		}

		try {
			Path tmpDir = Files.createTempDirectory("elexis");
			File temp = File.createTempFile("omni", "tmp");
			// use title if given
			if (title != null && !title.isEmpty()) {
				// Remove all characters that shall not appear in the generated filename
				String cleanTitle = title
						.replaceAll(java.util.regex.Matcher.quoteReplacement(Preferences.cotf_unwanted_chars), "_");
				if (!cleanTitle.toLowerCase().contains("." + fileExtension.toLowerCase())) {
					temp = new File(tmpDir.toString(), cleanTitle + "." + fileExtension);
				} else {
					temp = new File(tmpDir.toString(), cleanTitle);
				}
			} else {
				temp = Files.createTempFile(tmpDir, "omni_", "_vore." + fileExtension).toFile();
			}
			tmpDir.toFile().deleteOnExit();
			temp.deleteOnExit();

			byte[] b = getContents(); // getBinary(FLD_DOC);
			if (b == null) {
				SWTHelper.showError(Messages.DocHandle_readErrorCaption2, Messages.DocHandle_loadErrorText);
				return temp;
			}
			try (FileOutputStream fos = new FileOutputStream(temp)) {
				fos.write(b);
			}
			log.debug("createTemporaryFile {} size {} ext {} ", temp.getAbsolutePath(), Files.size(temp.toPath()),
					fileExtension);
			return temp;
		} catch (FileNotFoundException e) {
			log.debug("File not found " + e, Log.WARNINGS);
		} catch (IOException e) {
			log.debug("Error creating file " + e, Log.WARNINGS);
		}

		return null;
	}

	public String getMimetype() {
		return get(FLD_MIMETYPE);
	}

	// IDocument
	public String getCategory() {
		return getCategoryName();
	}

	public String getMimeType() {
		return checkNull(get(FLD_MIMETYPE));
	}

	public Patient getPatient() {
		return Patient.load(get(FLD_PATID));
	}

	public InputStream getContentsAsStream() throws ElexisException {
		return new ByteArrayInputStream(getContents());
	}

	public byte[] getContentsAsBytes() throws ElexisException {
		return getContents();
	}

	public String getGUID() {
		return getId();
	}

}
