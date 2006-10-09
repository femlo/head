package org.mifos.application.customer.client.struts.action;

import java.net.URISyntaxException;
import java.sql.Date;
import java.util.List;

import org.mifos.application.customer.business.CustomerHierarchyEntity;
import org.mifos.application.customer.center.business.CenterBO;
import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.group.business.GroupBO;
import org.mifos.application.customer.util.helpers.CustomerConstants;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.customer.util.valueobjects.CustomerSearchInput;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.components.audit.business.AuditLog;
import org.mifos.framework.components.audit.business.AuditLogRecord;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.Flow;
import org.mifos.framework.util.helpers.FlowManager;
import org.mifos.framework.util.helpers.ResourceLoader;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class ClientTransferActionTest extends MifosMockStrutsTestCase{
	private CenterBO center;
	private GroupBO group;
	private CenterBO center1;
	private GroupBO group1;
	private ClientBO client;
	private OfficeBO office;
	private String flowKey;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try {
			setServletConfigFile(ResourceLoader.getURI("WEB-INF/web.xml")
					.getPath());
			setConfigFile(ResourceLoader.getURI(
					"org/mifos/application/customer/client/struts-config.xml")
					.getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		UserContext userContext = TestObjectFactory.getContext();
		request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
		addRequestParameter("recordLoanOfficerId", "1");
		addRequestParameter("recordOfficeId", "1");
		request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());		
		Flow flow = new Flow();
		flowKey = String.valueOf(System.currentTimeMillis());
		FlowManager flowManager = new FlowManager();
		flowManager.addFLow(flowKey, flow);
		request.getSession(false).setAttribute(Constants.FLOWMANAGER,
				flowManager);
	}
	
	@Override
	protected void tearDown() throws Exception {
		TestObjectFactory.cleanUp(client);
		TestObjectFactory.cleanUp(group);
		TestObjectFactory.cleanUp(group1);
		TestObjectFactory.cleanUp(center);
		TestObjectFactory.cleanUp(center1);
		TestObjectFactory.cleanUp(office);
		HibernateUtil.closeSession();
		super.tearDown();
	}

	public void testLoad_transferToBranch() throws Exception {
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "loadBranches");
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.loadBranches_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
	}
	
	public void testSuccessfulPreview_transferToBranch() throws Exception {
		createObjectsForClientTransfer();
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "previewBranchTransfer");
		addRequestParameter("officeId", client.getOffice().getOfficeId().toString());
		addRequestParameter("officeName", client.getOffice().getOfficeName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.previewBranchTransfer_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
	}
	
	public void testFailure_transferToBranch() throws Exception {
		createObjectsForClientTransfer();	
		request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, client,request);
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "transferToBranch");
		addRequestParameter("officeId", client.getOffice().getOfficeId().toString());
		addRequestParameter("officeName", client.getOffice().getOfficeName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyActionErrors(new String[]{CustomerConstants.ERRORS_SAME_BRANCH_TRANSFER});
		verifyForward(ActionForwards.transferToBranch_failure.toString());		
		HibernateUtil.closeSession();
		client = (ClientBO)TestObjectFactory.getObject(ClientBO.class,client.getCustomerId());
	}
	
	public void testSuccessful_transferToBranch() throws Exception {
		createObjectsForClientTransfer();
		request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, client,request);
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "transferToBranch");
		addRequestParameter("officeId", office.getOfficeId().toString());
		addRequestParameter("officeName", office.getOfficeName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.update_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
		client = (ClientBO)TestObjectFactory.getObject(ClientBO.class,client.getCustomerId());
		assertEquals(client.getOffice().getOfficeId(), office.getOfficeId());
		assertEquals(CustomerStatus.CLIENT_HOLD, client.getStatus());
		office = client.getOffice();
	}
	
	public void testCancel() throws Exception {
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "cancel");
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.cancel_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
	}
	
	public void testLoad_updateParent() throws Exception {
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "loadParents");
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.loadParents_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
		CustomerSearchInput clientSearchInput = (CustomerSearchInput)SessionUtils.getAttribute(CustomerConstants.CUSTOMER_SEARCH_INPUT,request.getSession());
		assertNotNull(clientSearchInput);
		assertEquals(TestObjectFactory.getUserContext().getBranchId(),clientSearchInput.getOfficeId());
	}
	
	public void testPreview_transferToParent() throws Exception {
		createObjectsForTransferringClientInGroup();
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "previewParentTransfer");
		addRequestParameter("parentGroupId", client.getParentCustomer().getCustomerId().toString());
		addRequestParameter("parentGroupName", client.getParentCustomer().getDisplayName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.previewParentTransfer_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
	}
	
	public void testFailure_transferToParent() throws Exception {
	
		createObjectsForTransferringClientInGroup();
		request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, client,request);
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "updateParent");
		addRequestParameter("parentGroupId", client.getParentCustomer().getCustomerId().toString());
		addRequestParameter("parentGroupName", client.getParentCustomer().getDisplayName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.updateParent_failure.toString());
		group = (GroupBO)TestObjectFactory.getObject(GroupBO.class,group.getCustomerId());
		group1 = (GroupBO)TestObjectFactory.getObject(GroupBO.class,group1.getCustomerId());
		client = (ClientBO)TestObjectFactory.getObject(ClientBO.class,client.getCustomerId());
	}
	
	public void testSuccessful_transferToParent() throws Exception {
		createObjectsForTransferringClientInGroup();
		request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, client,request);
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "updateParent");
		addRequestParameter("parentGroupId", group1.getCustomerId().toString());
		addRequestParameter("parentGroupName", group1.getDisplayName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.update_success.toString());
		client = (ClientBO)TestObjectFactory.getObject(ClientBO.class,client.getCustomerId());
		group = (GroupBO)TestObjectFactory.getObject(GroupBO.class,group.getCustomerId());
		group1 = (GroupBO)TestObjectFactory.getObject(GroupBO.class,group1.getCustomerId());
		center = (CenterBO)TestObjectFactory.getObject(CenterBO.class,center.getCustomerId());
		assertEquals(group1.getCustomerId(),client.getParentCustomer().getCustomerId());
		assertEquals(0, group.getMaxChildCount().intValue());
		assertEquals(1, group1.getMaxChildCount().intValue());
		assertEquals(center1.getSearchId()+".1.1", client.getSearchId());
		CustomerHierarchyEntity currentHierarchy = client.getActiveCustomerHierarchy();
		assertEquals(group1.getCustomerId(),currentHierarchy.getParentCustomer().getCustomerId());
	}
	
	public void testSuccessful_transferToParent_AuditLog() throws Exception {
		createObjectsForTransferringClientInGroup();
		request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, client,request);
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "updateParent");
		addRequestParameter("parentGroupId", group1.getCustomerId().toString());
		addRequestParameter("parentGroupName", group1.getDisplayName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.update_success.toString());
		client = (ClientBO)TestObjectFactory.getObject(ClientBO.class,client.getCustomerId());
		group = (GroupBO)TestObjectFactory.getObject(GroupBO.class,group.getCustomerId());
		group1 = (GroupBO)TestObjectFactory.getObject(GroupBO.class,group1.getCustomerId());
		center = (CenterBO)TestObjectFactory.getObject(CenterBO.class,center.getCustomerId());
		assertEquals(group1.getCustomerId(),client.getParentCustomer().getCustomerId());
		assertEquals(0, group.getMaxChildCount().intValue());
		assertEquals(1, group1.getMaxChildCount().intValue());
		assertEquals(center1.getSearchId()+".1.1", client.getSearchId());
		CustomerHierarchyEntity currentHierarchy = client.getActiveCustomerHierarchy();
		assertEquals(group1.getCustomerId(),currentHierarchy.getParentCustomer().getCustomerId());
		
		List<AuditLog> auditLogList=TestObjectFactory.getChangeLog(EntityType.CLIENT.getValue(),client.getCustomerId());
		assertEquals(1,auditLogList.size());
		assertEquals(EntityType.CLIENT.getValue(),auditLogList.get(0).getEntityType());
		assertEquals(client.getCustomerId(),auditLogList.get(0).getEntityId());
		
		assertEquals(1,auditLogList.get(0).getAuditLogRecords().size());
		
		for(AuditLogRecord auditLogRecord :  auditLogList.get(0).getAuditLogRecords()){
			if(auditLogRecord.getFieldName().equalsIgnoreCase("Group Name"))
				matchValues(auditLogRecord,"Group", "Group2");
		}
		TestObjectFactory.cleanUpChangeLog();
	}
	
	public void testSuccessful_transferToBranch_AuditLog() throws Exception {
		createObjectsForClientTransfer();
		request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, client,request);
		setRequestPathInfo("/clientTransferAction.do");
		addRequestParameter("method", "transferToBranch");
		addRequestParameter("officeId", office.getOfficeId().toString());
		addRequestParameter("officeName", office.getOfficeName());
		addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
		actionPerform();
		verifyForward(ActionForwards.update_success.toString());
		verifyNoActionErrors();
		verifyNoActionMessages();
		client = (ClientBO)TestObjectFactory.getObject(ClientBO.class,client.getCustomerId());
		assertEquals(client.getOffice().getOfficeId(), office.getOfficeId());
		assertEquals(CustomerStatus.CLIENT_HOLD, client.getStatus());
		office = client.getOffice();
		List<AuditLog> auditLogList=TestObjectFactory.getChangeLog(EntityType.CLIENT.getValue(),client.getCustomerId());
		assertEquals(1,auditLogList.size());
		assertEquals(EntityType.CLIENT.getValue(),auditLogList.get(0).getEntityType());
		assertEquals(client.getCustomerId(),auditLogList.get(0).getEntityId());
		
		assertEquals(3,auditLogList.get(0).getAuditLogRecords().size());
		
		for(AuditLogRecord auditLogRecord :  auditLogList.get(0).getAuditLogRecords()){
			if(auditLogRecord.getFieldName().equalsIgnoreCase("Loan Officer Assigned"))
				matchValues(auditLogRecord,"mifos", "-");
			else if(auditLogRecord.getFieldName().equalsIgnoreCase("Status"))
				matchValues(auditLogRecord,"Active", "On Hold");
			else if(auditLogRecord.getFieldName().equalsIgnoreCase("Branch Office Name"))
				matchValues(auditLogRecord,"TestBranchOffice", "customer_office");
		}
		TestObjectFactory.cleanUpChangeLog();
	}
	
	private void createObjectsForClientTransfer()throws Exception{
		office = TestObjectFactory.createOffice(OfficeLevel.BRANCHOFFICE, TestObjectFactory.getOffice(Short.valueOf("1")), "customer_office", "cust");
		client = TestObjectFactory.createClient("client_to_transfer",getMeeting(),CustomerStatus.CLIENT_ACTIVE.getValue(), new java.util.Date());
		HibernateUtil.closeSession();
	}
	
	private void createObjectsForTransferringClientInGroup()throws Exception{
		MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory
				.getMeetingHelper(1, 1, 4, 2));
		MeetingBO meeting1 = TestObjectFactory.createMeeting(TestObjectFactory
				.getMeetingHelper(1, 1, 4, 2));
		center = TestObjectFactory.createCenter("Center", CustomerStatus.CENTER_ACTIVE.getValue(),
				"1.1", meeting, new Date(System.currentTimeMillis()));
		group = TestObjectFactory.createGroup("Group", CustomerStatus.GROUP_ACTIVE.getValue(),
				center.getSearchId()+".1", center, new Date(System.currentTimeMillis()));
		center1 = TestObjectFactory.createCenter("Center1", CustomerStatus.CENTER_ACTIVE.getValue(),
				"1.1", meeting1, new Date(System.currentTimeMillis()));
		group1 = TestObjectFactory.createGroup("Group2", CustomerStatus.GROUP_ACTIVE.getValue(),
				center1.getSearchId()+".1", center1, new Date(System.currentTimeMillis()));
		client = TestObjectFactory.createClient("Client11", CustomerStatus.CLIENT_ACTIVE.getValue(),
				group.getSearchId()+".1", group, new Date(System.currentTimeMillis()));
		HibernateUtil.closeSession();
	}
	
	private MeetingBO getMeeting() {
		MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory
				.getMeetingHelper(1, 1, 4, 2));
		return meeting;
	}
}
