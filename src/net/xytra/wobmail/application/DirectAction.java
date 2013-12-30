package net.xytra.wobmail.application;
// Generated by the WOLips Templateengine Plug-in at Apr 18, 2007 9:01:26 PM

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

import net.xytra.wobmail.components.FileDownloadPage;
import net.xytra.wobmail.components.FileViewPage;
import net.xytra.wobmail.components.Main;
import net.xytra.wobmail.components.XWMCompose;
import net.xytra.wobmail.components.XWMList;
import net.xytra.wobmail.components.XWMViewMessage;
import net.xytra.wobmail.export.ExportVisitable;
import net.xytra.wobmail.mailconn.folder.WobmailFolderType;
import net.xytra.wobmail.misc.MessageRow;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXConstant;
import er.extensions.logging.ERXLogger;

public class DirectAction extends WODirectAction
{
    public DirectAction(WORequest aRequest) {
		super(aRequest);
	}

	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}

	protected WOActionResults redirectToDefaultAction() {
		WORedirect r = (WORedirect)pageWithName(WORedirect.class.getName());
		r.setUrl(context().directActionURLForActionNamed("default", null).split("\\?")[0]);

		return (r);
	}

	// Login and Logout
	public WOActionResults loginAction() {
		Main page = (Main)pageWithName(Main.class.getName());

		// Set language, username and password from form values; the component
		// will deal with the rest
		String language = request().stringFormValueForKey("l");
		if (language != null) {
			page.selectedLanguage = language;
		}

		page.username = request().stringFormValueForKey("u");
		page.password = request().stringFormValueForKey("p");

		return (page.loginAction());
	}

	public WOActionResults logoutAction() {
		if (hasSession()) {
			session().terminate();
		}

		return (redirectToDefaultAction());
	}

	// Inside actions from PageWrapper
    public WOActionResults composeAction() {
    	if (!hasSession()) {
    		return (redirectToDefaultAction());
    	}

    	return (pageWithName(XWMCompose.class.getName()));
    }

	public WOActionResults listAction() throws MessagingException {
    	if (!hasSession()) {
    		return (redirectToDefaultAction());
    	}

		// Page number
		int pageNumber = integerForFormValueForKeyWithDefault("page", 0);
		session().setSelectedPageIndex(pageNumber);

		// Batch size
		Integer batchSize = integerForFormValueForKey("mpp");
		if (batchSize != null) {
			session().setSelectedNumberPerPage(batchSize.intValue());
		}

		// Sorting
		String sortKey = request().stringFormValueForKey("sort");
		boolean reverseSort = "1".equals(request().stringFormValueForKey("rs"));

		// If a valid sorting key was passed, sort accordingly:
		if ((sortKey != null) && (MessageRow.isSortKeyValid(sortKey))) {
			session().getCurrentFolder().sortMessageRowsWithKey(sortKey, reverseSort);
		}

		// Whether reload needed
		boolean shouldReload = "1".equals(request().stringFormValueForKey("rl"));

		// Select all/none
		String selectionType = request().stringFormValueForKey("select");

		// Create page and set parameters
		XWMList list = (XWMList)pageWithName(XWMList.class.getName());
		list.setForceListReload(shouldReload);
		list.setMessagesAsSelected(selectionType);

		return (list);
	}

	// Inside actions from within message
	public WOActionResults deleteMessageAction() throws MessagingException {
		if (!hasSession()) {
			return (redirectToDefaultAction());
		}

		Integer messageIndex = integerForFormValueForKey("mi");

		// If no message index was specified, just return list
		if (messageIndex == null) {
			return (listAction());
		}

		MessageRow messageRow = session().getCurrentFolder()
				.getMessageRowByIndex(messageIndex);

		session().getCurrentFolder().moveMessageRowsToFolder(
				new NSArray<MessageRow>(messageRow),
				WobmailFolderType.TRASH.name());

		// After deletion, return to list:
		return (listAction());
	}

    public WOActionResults forwardAction() throws IOException, MessagingException {
    	return (forwardAction(false));
    }

    public WOActionResults forwardAsAttachmentAction() throws IOException, MessagingException {
    	return (forwardAction(true));
    }

    protected WOActionResults forwardAction(boolean forwardAsAttachment) throws IOException, MessagingException {
    	if (!hasSession()) {
    		return (redirectToDefaultAction());
    	}

		Integer messageIndex = integerForFormValueForKey("mi");

		// If no message index was specified, just return list
		if (messageIndex == null) {
			return (listAction());
		}

		// TODO: Use MessageRow or equivalent instead of Message directly
		Message message = session().getCurrentFolder()
				.getMessageRowByIndex(messageIndex.intValue()).getMessage();

		XWMCompose page = (XWMCompose)pageWithName(XWMCompose.class.getName());
		page.forwardMessage(message, forwardAsAttachment);

		return (page);
    }

	public WOActionResults replyAction() throws IOException, MessagingException {
		return (replyAction(false));
	}

	public WOActionResults replyToAllAction() throws IOException, MessagingException {
		return (replyAction(true));
	}

	protected WOActionResults replyAction(boolean replyToAll) throws IOException, MessagingException {
    	if (!hasSession()) {
    		return (redirectToDefaultAction());
    	}

		Integer messageIndex = integerForFormValueForKey("mi");

		// If no message index was specified, just return list
		if (messageIndex == null) {
			return (listAction());
		}

		// TODO: Use MessageRow or equivalent instead of Message directly
		Message message = session().getCurrentFolder()
				.getMessageRowByIndex(messageIndex.intValue()).getMessage();

		XWMCompose page = (XWMCompose)pageWithName(XWMCompose.class.getName());
		page.replyToMessage(message, replyToAll);

		return (page);
	}

	public WOActionResults viewMessageAction() throws MessagingException {
    	if (!hasSession()) {
    		return (redirectToDefaultAction());
    	}

		XWMViewMessage page = (XWMViewMessage)pageWithName(XWMViewMessage.class.getName());

		Integer messageIndex = integerForFormValueForKey("mi");
		if (messageIndex != null) {
			page.setMessageIndex(messageIndex.intValue());
		}

		return (page);
	}

	// Inside actions for viewing objects
	public WOActionResults downloadObjectAction()
	{
		return (exportFileActionWithComponent((FileDownloadPage)pageWithName(FileDownloadPage.class.getName())));
	}

	public WOActionResults viewObjectAction()
	{
		return (exportFileActionWithComponent((FileViewPage)pageWithName(FileViewPage.class.getName())));
	}

	// Internal workings
	protected WOActionResults exportFileActionWithComponent(ExportVisitable ev)
	{
		if (!hasSession())
			return (http404response());

		String s = request().stringFormValueForKey("id");
		if (s == null)
			return (http404response());

		int index;
		try {
			index = Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return (http404response());
		}

		ev.accept(((Session)session()).getDownloadableObject(index));
		return (ev);
	}

	protected WOActionResults http404response()
	{
		WOResponse response = new WOResponse();
		response.setStatus(WOMessage.HTTP_STATUS_NOT_FOUND);
		response.setContent("File cannot be found");

		return (response);
	}

	// Sessions
	protected boolean hasSession() {
		return (getSessionIDForRequest(request()) != null) && (existingSession() != null);
	}

	public Session session() {
		return ((Session)super.session());
	}

	// Convenience methods to get form data
	protected Integer integerForFormValueForKey(String key) {
		return (integerForFormValueForKeyWithDefault(key, null));
	}

	protected Integer integerForFormValueForKeyWithDefault(String key, int defaultValue) {
		return (integerForFormValueForKeyWithDefault(key, ERXConstant.integerForInt(defaultValue)).intValue());
	}

	protected Integer integerForFormValueForKeyWithDefault(String key, Integer defaultValue) {
		if (key == null) {
			throw (new NullPointerException("NULL key passed as key"));
		}

		String s = request().stringFormValueForKey(key);
		ERXLogger.log.debug("stringFormValueForKey(" + key + ")=" + s);

		if (s == null) {
			return (defaultValue);
		}

		Integer value = null;
		try {
			value = ERXConstant.integerForString(s);
		}
		catch (NumberFormatException e) {
			ERXLogger.log.debug(e);
		}

		if (value == null) {
			value = defaultValue;
		}

		return (value);
	}

}
