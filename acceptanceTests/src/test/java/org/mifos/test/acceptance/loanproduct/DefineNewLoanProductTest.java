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

package org.mifos.test.acceptance.loanproduct;


import org.mifos.test.acceptance.framework.AppLauncher;
import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.UiTestCaseBase;
import org.mifos.test.acceptance.framework.admin.AdminPage;
import org.mifos.test.acceptance.framework.loanproduct.DefineNewLoanProductConfirmationPage;
import org.mifos.test.acceptance.framework.loanproduct.DefineNewLoanProductPage;
import org.mifos.test.acceptance.framework.loanproduct.DefineNewLoanProductPage.SubmitFormParameters;
import org.mifos.test.acceptance.framework.loanproduct.DefineNewLoanProductPreviewPage;
import org.mifos.test.acceptance.framework.testhelpers.FormParametersHelper;
import org.mifos.test.acceptance.framework.testhelpers.NavigationHelper;
import org.mifos.test.acceptance.framework.user.UserViewDetailsPage;
import org.mifos.test.acceptance.util.ApplicationDatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@ContextConfiguration(locations = {"classpath:ui-test-context.xml"})
@Test(sequential = true, groups = {"smoke", "loanproduct", "acceptance"})
public class DefineNewLoanProductTest extends UiTestCaseBase {

    private AppLauncher appLauncher;

    @Autowired
    private ApplicationDatabaseOperation applicationDatabaseOperation;


    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    @BeforeMethod
    @Override
    public void setUp() throws Exception {
        super.setUp();
        appLauncher = new AppLauncher(selenium);
        selenium.windowMaximize();
    }

    @AfterMethod
    public void logOut() {
        (new MifosPage(selenium)).logout();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void createWeeklyLoanProduct() throws Exception {
        SubmitFormParameters formParameters = FormParametersHelper.getWeeklyLoanProductParameters();
        AdminPage adminPage = loginAndNavigateToAdminPage();
        adminPage.verifyPage();
        adminPage.defineLoanProduct(formParameters);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void createMonthlyLoanProduct() throws Exception {
        SubmitFormParameters formParameters = FormParametersHelper.getMonthlyLoanProductParameters();
        AdminPage adminPage = loginAndNavigateToAdminPage();
        adminPage.verifyPage();
        adminPage.defineLoanProduct(formParameters);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void verifyLSIMDisabled() throws Exception {
        applicationDatabaseOperation.updateLSIM(0);
        AdminPage adminPage = loginAndNavigateToAdminPage();
        adminPage.verifyPage();
        DefineNewLoanProductPage defineLoanProductPage = adminPage.navigateToDefineLoanProduct();
        defineLoanProductPage.verifyVariableInstalmentNotAvailable();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void verifyVariableInstalmentUnChecked() throws Exception {
        applicationDatabaseOperation.updateLSIM(1);
        SubmitFormParameters formParameters = FormParametersHelper.getMonthlyLoanProductParameters();
        AdminPage adminPage = loginAndNavigateToAdminPage();
        adminPage.verifyPage();
        DefineNewLoanProductPage defineLoanProductPage = adminPage.navigateToDefineLoanProduct();
        defineLoanProductPage.fillLoanParameters(formParameters);
        DefineNewLoanProductPreviewPage productPreviewPage = defineLoanProductPage.submitAndGotoNewLoanProductPreviewPage();
        productPreviewPage.verifyVariableInstalmentUnChecked();
        DefineNewLoanProductConfirmationPage loanProductConfirmationPage = productPreviewPage.submit();
        UserViewDetailsPage viewDetailsPage = loanProductConfirmationPage.navigateToViewLoanDetails();
        viewDetailsPage.verifyVariableInstalmentOptionsUnChecked();
    }


    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void verifyVariableInstalment() throws Exception {
        applicationDatabaseOperation.updateLSIM(1);
        createAndValidateWithVariableInstalment("60", "1", "100.5");
        createAndValidateWithVariableInstalment("","1","");
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void verifyCashFlow() throws Exception {
        applicationDatabaseOperation.updateLSIM(1);
        createAndValidateWithVariableInstalment("60", "1", "100.5");
        createAndValidateWithVariableInstalment("","1","");
    }



    private void createAndValidateWithVariableInstalment(String maxGap, String minGap, String minInstalmentAmount) {
        SubmitFormParameters formParameters = FormParametersHelper.getMonthlyLoanProductParameters();
        navigateToDefineNewLoanProductPage().
        fillLoanParameters(formParameters).
        verifyVariableInstalmentOptionsDefaults().
        verifyVariableInstalmentOptionsFields().

        new DefineNewLoanProductPage(selenium).fillVariableInstalmentOption(maxGap, minGap, minInstalmentAmount).
        submitAndGotoNewLoanProductPreviewPage().
        verifyVariableInstalmentOption(maxGap, minGap, minInstalmentAmount, "Yes").
        submit().
        navigateToViewLoanDetails().
        verifyVariableInstalmentOptions(maxGap, minGap, minInstalmentAmount, "Yes");
    }

    private DefineNewLoanProductPage navigateToDefineNewLoanProductPage() {
        return new NavigationHelper(selenium).navigateToAdminPage().navigateToDefineLoanProduct();
    }


    private AdminPage loginAndNavigateToAdminPage() {
        return appLauncher
                .launchMifos()
                .loginSuccessfullyUsingDefaultCredentials()
                .navigateToAdminPage();
    }

}

