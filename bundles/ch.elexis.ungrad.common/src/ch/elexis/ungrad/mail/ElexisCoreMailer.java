package ch.elexis.ungrad.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;

/**
 * Trying to use elexis's core mail system. But doesn't work at all until now. 
 */

public class ElexisCoreMailer {
	public List<String> getSendMailAccounts() {
		List<String> ret = new ArrayList<String>();
		List<String> accounts = MailClientHolder.get().getAccounts();
		for (String accountId : accounts) {
			Optional<MailAccount> accountOptional = MailClientHolder.get().getAccount(accountId);
			if (accountOptional.isPresent()) {
				if (accountOptional.get().getType() == TYPE.SMTP) {
					ret.add(accountId);
				}
			}
		}
		return ret;
	}

}
