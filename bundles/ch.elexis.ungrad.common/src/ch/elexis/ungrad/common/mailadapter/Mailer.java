package ch.elexis.ungrad.common.mailadapter;

import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.mail.IMailClient;

public class Mailer {

		@Reference
		private IMailClient mailClient;
		
		
}
