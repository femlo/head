/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 * All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.application.customer.client.business.service;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.accounts.loan.struts.action.LoanCreationGlimDto;
import org.mifos.application.collectionsheet.persistence.MeetingBuilder;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerLevelEntity;
import org.mifos.application.customer.business.service.CustomerBusinessService;
import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.client.business.ClientPerformanceHistoryEntity;
import org.mifos.application.customer.persistence.CustomerDao;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.master.business.BusinessActivityEntity;
import org.mifos.application.master.business.ValueListElement;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.application.productdefinition.persistence.LoanProductDao;
import org.mifos.application.servicefacade.LoanServiceFacade;
import org.mifos.application.servicefacade.LoanServiceFacadeWebTier;
import org.mifos.framework.TestUtils;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.util.helpers.Money;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebTierClientDetailsServiceFacadeTest {
    // class under test
    private WebTierClientDetailsServiceFacade clientDetailsServiceFacade;

    @Mock
    private CustomerBusinessService customerBusinessService;
    
    @Mock
    private ClientBO client;
    
    @Mock
    private ClientPerformanceHistoryEntity clientPerformanceHistoryEntity;
    
    @Before
    public void setupAndInjectDependencies() {
        clientDetailsServiceFacade = new WebTierClientDetailsServiceFacade();
        clientDetailsServiceFacade.setCustomerBusinessService(customerBusinessService);
    }

    @Test
    public void shouldLoadDelinquentPortfolioAmountForClient() throws ServiceException {
        String globalCustNum = "123";
        Short levelId = 1;
        String amount = "10.0";
        when(customerBusinessService.findBySystemId(globalCustNum, levelId)).thenReturn(client);
        when(client.getClientPerformanceHistory()).thenReturn(clientPerformanceHistoryEntity);
        when(clientPerformanceHistoryEntity.getDelinquentPortfolioAmount()).thenReturn(new Money(TestUtils.RUPEE,amount));
        ClientInformationDto clientInformationDto = clientDetailsServiceFacade.getClientInformationDto(globalCustNum, levelId);
        assertThat(clientInformationDto.getDelinquentPortfolioAmount(), is(amount));
    }
}
