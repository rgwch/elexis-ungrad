package ch.elexis.ungrad.lucinda.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Test_Controller {

	@Test
	public void testPrepareQuery() {
		Controller ctrl=new Controller();
		String a=ctrl.buildQuery("Irgendwans");
		assertEquals("Irgendwas", a);
	}
}
