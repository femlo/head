/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */

package org.mifos.platform.questionnaire;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.customers.surveys.business.Question;
import org.mifos.customers.surveys.helpers.AnswerType;
import org.mifos.framework.components.fieldConfiguration.business.EntityMaster;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.platform.questionnaire.contract.*;
import org.mifos.platform.questionnaire.domain.EventEntity;
import org.mifos.platform.questionnaire.domain.EventSourceEntity;
import org.mifos.platform.questionnaire.domain.QuestionGroup;
import org.mifos.platform.questionnaire.domain.Section;
import org.mifos.platform.questionnaire.mappers.QuestionnaireMapperImpl;
import org.mifos.platform.questionnaire.persistence.EventSourceDao;
import org.mifos.platform.questionnaire.persistence.QuestionDao;
import org.mifos.platform.questionnaire.persistence.QuestionGroupDao;
import org.mifos.platform.questionnaire.validators.QuestionnaireValidator;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mifos.platform.questionnaire.QuestionnaireConstants.QUESTION_GROUP_TITLE_NOT_PROVIDED;
import static org.mifos.platform.questionnaire.QuestionnaireConstants.QUESTION_TITLE_NOT_PROVIDED;
import static org.mifos.platform.questionnaire.contract.QuestionType.FREETEXT;
import static org.mifos.platform.questionnaire.contract.QuestionType.INVALID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QuestionnaireServiceTest {

    private QuestionnaireService questionnaireService;

    @Mock
    private QuestionnaireValidator questionnaireValidator;

    @Mock
    private QuestionDao questionDao;

    @Mock
    private QuestionGroupDao questionGroupDao;

    @Mock
    private EventSourceDao eventSourceDao;

    private static final String QUESTION_TITLE = "Test QuestionDetail Title";
    private static final String QUESTION_GROUP_TITLE = "Question Group Title";

    @Before
    public void setUp() {
        questionnaireService = new QuestionnaireServiceImpl(questionnaireValidator, questionDao, new QuestionnaireMapperImpl(eventSourceDao), questionGroupDao);
    }

    @Test
    public void shouldDefineQuestion() throws ApplicationException {
        QuestionDefinition questionDefinition = new QuestionDefinition(QUESTION_TITLE, FREETEXT);
        try {
            QuestionDetail questionDetail = questionnaireService.defineQuestion(questionDefinition);
            verify(questionDao, times(1)).create(any(Question.class));
            assertNotNull(questionDetail);
            assertEquals(QUESTION_TITLE, questionDetail.getText());
            assertEquals(QUESTION_TITLE, questionDetail.getShortName());
            assertEquals(FREETEXT, questionDetail.getType());
        } catch (ApplicationException e) {
            fail("Should not have thrown the validation exception");
        }
        verify(questionnaireValidator).validate(questionDefinition);
        verify(questionDao).create(any(org.mifos.customers.surveys.business.Question.class));
    }

    @Test(expected = ApplicationException.class)
    public void shouldThrowValidationExceptionWhenQuestionTitleIsNull() throws ApplicationException {
        QuestionDefinition questionDefinition = new QuestionDefinition(null, INVALID);
        doThrow(new ApplicationException(QUESTION_TITLE_NOT_PROVIDED)).when(questionnaireValidator).validate(questionDefinition);
        questionnaireService.defineQuestion(questionDefinition);
        verify(questionnaireValidator).validate(questionDefinition);
    }

    @Test
    public void shouldGetAllQuestions() {
        when(questionDao.getDetailsAll()).thenReturn(asList(getQuestion(1, "q1", AnswerType.DATE), getQuestion(2, "q2", AnswerType.FREETEXT)));
        List<QuestionDetail> questionDetails = questionnaireService.getAllQuestions();
        assertNotNull("getAllQuestions should not return null", questionDetails);
        verify(questionDao, times(1)).getDetailsAll();

        assertThat(questionDetails.get(0).getText(), is("q1"));
        assertThat(questionDetails.get(0).getShortName(), is("q1"));
        assertThat(questionDetails.get(0).getId(), is(1));
        assertThat(questionDetails.get(0).getType(), is(QuestionType.DATE));

        assertThat(questionDetails.get(1).getText(), is("q2"));
        assertThat(questionDetails.get(1).getShortName(), is("q2"));
        assertThat(questionDetails.get(1).getId(), is(2));
        assertThat(questionDetails.get(1).getType(), is(QuestionType.FREETEXT));

    }

    private Question getQuestion(int id, String text, AnswerType type) {
        Question question = new Question();
        question.setQuestionId(id);
        question.setQuestionText(text);
        question.setShortName(text);
        question.setAnswerType(type);
        return question;
    }

    @Test
    public void shouldDefineQuestionGroup() throws ApplicationException {
        List<SectionDefinition> sectionDefinitionList = asList(getSectionDefinition("S1"), getSectionDefinition("S2"));
        EventSource event = getEventSource("Create", "Client");
        QuestionGroupDefinition questionGroupDefinition = new QuestionGroupDefinition(QUESTION_GROUP_TITLE, event, sectionDefinitionList);
        try {
            EventSourceEntity eventSourceEntity = getEventSourceEntity("Create", "Client");
            when(eventSourceDao.retrieveByEventAndSource(anyString(), anyString())).thenReturn(Collections.singletonList(eventSourceEntity));
            QuestionGroupDetail questionGroupDetail = questionnaireService.defineQuestionGroup(questionGroupDefinition);
            verify(questionnaireValidator).validate(questionGroupDefinition);
            verify(questionGroupDao, times(1)).create(any(QuestionGroup.class));
            verify(eventSourceDao, times(1)).retrieveByEventAndSource(anyString(), anyString());
            assertNotNull(questionGroupDetail);
            assertEquals(QUESTION_GROUP_TITLE, questionGroupDetail.getTitle());
            List<SectionDefinition> sectionDefinitions = questionGroupDetail.getSectionDefinitions();
            assertNotNull(sectionDefinitions);
            assertThat(sectionDefinitions.size(), is(2));
            assertThat(sectionDefinitions.get(0).getName(), is("S1"));
            assertThat(sectionDefinitions.get(1).getName(), is("S2"));
            EventSource eventSource = questionGroupDetail.getEventSource();
            assertThat(eventSource, is(not(nullValue())));
            assertThat(eventSource.getEvent(), is("Create"));
            assertThat(eventSource.getSource(), is("Client"));
        } catch (ApplicationException e) {
            fail("Should not have thrown the validation exception");
        }
    }

    private EventSourceEntity getEventSourceEntity(String event, String source) {
        EventSourceEntity eventSourceEntity = new EventSourceEntity();
        EventEntity eventEntity = new EventEntity();
        eventEntity.setName(event);
        eventSourceEntity.setEvent(eventEntity);
        EntityMaster entityMaster = new EntityMaster();
        entityMaster.setEntityType(source);
        eventSourceEntity.setSource(entityMaster);
        return eventSourceEntity;
    }

    private EventSource getEventSource(String event, String source) {
        return new EventSource(event, source, null);
    }

    private SectionDefinition getSectionDefinition(String name) {
        SectionDefinition section = new SectionDefinition();
        section.setName(name);
        return section;
    }

    @Test(expected = ApplicationException.class)
    public void shouldThrowValidationExceptionWhenQuestionGroupTitleIsNull() throws ApplicationException {
        QuestionGroupDefinition questionGroupDefinition = new QuestionGroupDefinition(null, null, asList(getSectionDefinition("S1")));
        doThrow(new ApplicationException(QUESTION_GROUP_TITLE_NOT_PROVIDED)).when(questionnaireValidator).validate(questionGroupDefinition);
        questionnaireService.defineQuestionGroup(questionGroupDefinition);
        verify(questionnaireValidator).validate(questionGroupDefinition);
    }

    @Test
    public void shouldGetAllQuestionGroups() {
        when(questionGroupDao.getDetailsAll()).thenReturn(asList(getQuestionGroup(0, "QG0", asList(new Section("S0_0"))), getQuestionGroup(1, "QG1", asList(new Section("S1_0"), new Section("S1_1")))));
        List<QuestionGroupDetail> questionGroupDetails = questionnaireService.getAllQuestionGroups();
        assertNotNull("getAllQuestionGroups should not return null", questionGroupDetails);
        for (int i = 0; i < questionGroupDetails.size(); i++) {
            assertThat(questionGroupDetails.get(i).getId(), is(i));
            assertThat(questionGroupDetails.get(i).getTitle(), is("QG"+i));
            List<SectionDefinition> sectionDefinitions = questionGroupDetails.get(i).getSectionDefinitions();
            for(int j=0;j<sectionDefinitions.size();j++) {
                assertThat(sectionDefinitions.get(j).getName(), is("S"+i+"_"+j));
            }
        }
    }

    @Test
    public void testGetQuestionGroupByIdSuccess() throws ApplicationException {
        int questionGroupId = 1;
        String title = "Title";
        when(questionGroupDao.getDetails(questionGroupId)).thenReturn(getQuestionGroup(questionGroupId, title, asList(new Section("S1"), new Section("S2"))));
        QuestionGroupDetail groupDetail = questionnaireService.getQuestionGroup(questionGroupId);
        assertNotNull(groupDetail);
        assertThat(groupDetail.getTitle(), is(title));
        verify(questionGroupDao, times(1)).getDetails(questionGroupId);
    }

    @Test
    public void testGetQuestionGroupByIdFailure() {
        int questionGroupId = 1;
        when(questionGroupDao.getDetails(questionGroupId)).thenReturn(null);
        try {
            questionnaireService.getQuestionGroup(questionGroupId);
            fail("Should raise application exception when question group is not present");
        } catch (ApplicationException e) {
            verify(questionGroupDao, times(1)).getDetails(questionGroupId);
            assertThat(e.getKey(), is(QuestionnaireConstants.QUESTION_GROUP_NOT_FOUND));
        }
    }

    @Test
    public void testGetQuestionByIdSuccess() throws ApplicationException {
        int questionId = 1;
        String title = "Title";
        when(questionDao.getDetails(questionId)).thenReturn(getQuestion(questionId, title, AnswerType.DATE));
        QuestionDetail questionDetail = questionnaireService.getQuestion(questionId);
        assertNotNull(questionDetail);
        assertThat(questionDetail.getShortName(), is(title));
        assertThat(questionDetail.getText(), is(title));
        assertThat(questionDetail.getType(), is(QuestionType.DATE));
        verify(questionDao, times(1)).getDetails(questionId);
    }

    @Test
    public void testGetQuestionByIdFailure() {
        int questionId = 1;
        when(questionDao.getDetails(questionId)).thenReturn(null);
        try {
            questionnaireService.getQuestion(questionId);
            fail("Should raise application exception when question group is not present");
        } catch (ApplicationException e) {
            verify(questionDao, times(1)).getDetails(questionId);
            assertThat(e.getKey(), is(QuestionnaireConstants.QUESTION_NOT_FOUND));
        }
    }

    private QuestionGroup getQuestionGroup(int questionGroupId, String title, List<Section> sections) {
        QuestionGroup questionGroup = new QuestionGroup();
        questionGroup.setId(questionGroupId);
        questionGroup.setTitle(title);
        questionGroup.setSections(sections);
        return questionGroup;
    }

    @Test
    public void shouldCheckDuplicates() {
        QuestionDefinition questionDefinition = new QuestionDefinition(QUESTION_TITLE, FREETEXT);
        when(questionDao.retrieveCountOfQuestionsWithTitle(QUESTION_TITLE)).thenReturn(asList((long) 0)).thenReturn(asList((long) 1));
        assertEquals(false, questionnaireService.isDuplicateQuestion(questionDefinition));
        assertEquals(true, questionnaireService.isDuplicateQuestion(questionDefinition));
        verify(questionDao, times(2)).retrieveCountOfQuestionsWithTitle(QUESTION_TITLE);
    }

}