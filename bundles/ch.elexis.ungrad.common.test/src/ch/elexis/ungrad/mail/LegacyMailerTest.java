package ch.elexis.ungrad.mail;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LegacyMailerTest {

	
	@Test
	public void findMailAccount() {
		ElexisCoreMailer mailer=new ElexisCoreMailer();
		List<String> accounts=mailer.getSendMailAccounts();
		assertTrue(accounts.size()>0);
		
	}
}
