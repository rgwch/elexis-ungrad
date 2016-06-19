package ch.elexis.ungrad.lucinda;

import org.eclipse.jface.action.IAction;

import ch.elexis.ungrad.lucinda.controller.Controller;

public interface IDocumentHandler{
	public IAction getSyncAction(final Controller controller);
	public IAction getFilterAction(final Controller controller);
}
