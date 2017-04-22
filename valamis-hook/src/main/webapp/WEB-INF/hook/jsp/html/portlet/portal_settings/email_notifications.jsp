<%--
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portlet/portal_settings/init.jsp" %>

<h3><liferay-ui:message key="email-notifications" /></h3>

<%
    String keyCertificateAchievedSubject = "valamisCertificateUserAchievedSubject";
    String keyCertificateAchievedBody = "valamisCertificateUserAchievedBody";
    String keyCertificateAchievedEnable = "valamis.certificate.user.achieved.enable";

    String keyCertificateAddedSubject = "valamisCertificateUserAddedSubject";
    String keyCertificateAddedBody = "valamisCertificateUserAddedBody";
    String keyCertificateAddedEnable = "valamis.certificate.user.added.enable";

    String keyCertificateDeactivatedSubject = "valamisCertificateUserDeactivatedSubject";
    String keyCertificateDeactivatedBody = "valamisCertificateUserDeactivatedBody";
    String keyCertificateDeactivatedEnable = "valamis.certificate.user.deactivated.enable";

    String keyCourseAddedSubject = "valamisCourseUserAddedSubject";
    String keyCourseAddedBody = "valamisCourseUserAddedBody";
    String keyCourseAddedEnable = "valamis.course.user.added.enable";

    String keyCourseLessonAvailableSubject = "valamisCourseLessonAvailableSubject";
    String keyCourseLessonAvailableBody = "valamisCourseLessonAvailableBody";
    String keyCourseLessonAvailableEnable = "valamis.course.lesson.available.enable";

    String keyGradeCourseSubject = "valamisGradeCourseSubject";
    String keyGradeCourseBody = "valamisGradeCourseBody";
    String keyGradeCourseEnable = "valamis.grade.course.enable";

    String keyGradeLessonSubject = "valamisGradeLessonSubject";
    String keyGradeLessonBody = "valamisGradeLessonBody";
    String keyGradeLessonEnable = "valamis.grade.lesson.enable";

    String keyCertificateExpiresSubject = "valamisCertificateExpiresSubject";
    String keyCertificateExpiresBody = "valamisCertificateExpiresBody";
    String keyCertificateExpiresEnable = "valamis.certificate.expires.enable";

    String keyCertificateExpiredSubject = "valamisCertificateExpiredSubject";
    String keyCertificateExpiredBody = "valamisCertificateExpiredBody";
    String keyCertificateExpiredEnable = "valamis.certificate.expired.enable";


    String adminEmailFromName = PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_FROM_NAME);
    String adminEmailFromAddress = PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

    boolean adminEmailUserAddedEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_USER_ADDED_ENABLED);
    String adminEmailUserAddedSubject = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_USER_ADDED_SUBJECT);
    String adminEmailUserAddedBody = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_USER_ADDED_BODY);
    String adminEmailUserAddedNoPasswordBody = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_USER_ADDED_NO_PASSWORD_BODY);

    String adminEmailPasswordSentSubject = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_PASSWORD_SENT_SUBJECT);
    String adminEmailPasswordSentBody = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_PASSWORD_SENT_BODY);

    String adminEmailPasswordResetSubject = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_PASSWORD_RESET_SUBJECT);
    String adminEmailPasswordResetBody = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_PASSWORD_RESET_BODY);

    String adminEmailVerificationSubject = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_VERIFICATION_SUBJECT);
    String adminEmailVerificationBody = PrefsPropsUtil.getContent(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_VERIFICATION_BODY);

    String valamisEmailCertificateAchievedSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateAchievedSubject);
    String valamisEmailCertificateAchievedBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateAchievedBody);
    boolean valamisEmailCertificateAchievedEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyCertificateAchievedEnable);

    String valamisEmailCertificateAddedSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateAddedSubject);
    String valamisEmailCertificateAddedBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateAddedBody);
    boolean valamisEmailCertificateAddedEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyCertificateAddedEnable);

    String valamisEmailCertificateDeactivatedSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateDeactivatedSubject);
    String valamisEmailCertificateDeactivatedBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateDeactivatedBody);
    boolean valamisEmailCertificateDeactivatedEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyCertificateDeactivatedEnable);

    String valamisEmailCourseAddedSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyCourseAddedSubject);
    String valamisEmailCourseAddedBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyCourseAddedBody);
    boolean valamisEmailCourseAddedEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyCourseAddedEnable);

    String valamisEmailLessonAvailableSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyCourseLessonAvailableSubject);
    String valamisEmailCourseLessonAvailableBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyCourseLessonAvailableBody);
    boolean valamisEmailCourseLessonAvailableEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyCourseLessonAvailableEnable);

    String valamisEmailGradeCourseSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyGradeCourseSubject);
    String valamisEmailGradeCourseBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyGradeCourseBody);
    boolean valamisEmailGradeCourseEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyGradeCourseEnable);

    String valamisEmailGradeLessonSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyGradeLessonSubject);
    String valamisEmailGradeLessonBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyGradeLessonBody);
    boolean valamisEmailGradeLessonEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyGradeLessonEnable);

    String valamisEmailCertificateExpiresSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateExpiresSubject);
    String valamisEmailCertificateExpiresBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateExpiresBody);
    boolean valamisEmailCertificateExpiresEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyCertificateExpiresEnable);

    String valamisEmailCertificateExpiredSubject = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateExpiredSubject);
    String valamisEmailCertificateExpiredBody = PrefsPropsUtil.getContent(company.getCompanyId(), keyCertificateExpiredBody);
    boolean valamisEmailCertificateExpiredEnable = PrefsPropsUtil.getBoolean(company.getCompanyId(), keyCertificateExpiredEnable);

%>

<liferay-ui:error-marker key="errorSection" value="email_notifications" />

<liferay-ui:tabs
        names="sender,account-created-notification,email-verification-notification,password-changed-notification,password-reset-notification,valamis-certificate-user-achieved,valamis-certificate-user-added,valamis-certificate-user-deactivated,valamis-course-user-added,valamis-course-lesson-available-added,valamis-grade-course,valamis-grade-lesson,valamis-certificate-expires,valamis-certificate-expired"
        refresh="<%= false %>"
>
    <liferay-ui:section>
        <aui:fieldset>
            <liferay-ui:error key="emailFromName" message="please-enter-a-valid-name" />

            <aui:input cssClass="lfr-input-text-container" label="name" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_FROM_NAME + "--" %>' type="text" value="<%= adminEmailFromName %>" />

            <liferay-ui:error key="emailFromAddress" message="please-enter-a-valid-email-address" />

            <aui:input cssClass="lfr-input-text-container" label="address" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_FROM_ADDRESS + "--" %>' type="text" value="<%= adminEmailFromAddress %>" />
        </aui:fieldset>
    </liferay-ui:section>
    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_USER_ADDED_ENABLED + "--" %>' type="checkbox" value="<%= adminEmailUserAddedEnable %>" />

            <liferay-ui:error key="emailUserAddedSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_USER_ADDED_SUBJECT + "--" %>' type="text" value="<%= adminEmailUserAddedSubject %>" />

            <liferay-ui:error key="emailUserAddedBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper helpMessage="account-created-notification-body-with-password-help" label="body-with-password">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailUserAddedBodyEditor" %>' name="emailUserAddedBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_USER_ADDED_BODY + "--" %>' type="hidden" value="<%= adminEmailUserAddedBody %>" />
            </aui:field-wrapper>

            <liferay-ui:error key="emailUserAddedNoPasswordBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper helpMessage="account-created-notification-body-without-password-help" label="body-without-password">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailUserAddedNoPasswordBodyEditor" %>' name="emailUserAddedNoPasswordBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_USER_ADDED_NO_PASSWORD_BODY + "--" %>' type="hidden" value="<%= adminEmailUserAddedNoPasswordBody %>" />
            </aui:field-wrapper>

            <div class="terms email-user-add definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>
    <liferay-ui:section>
        <aui:fieldset>
            <liferay-ui:error key="emailVerificationSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_VERIFICATION_SUBJECT + "--" %>' type="text" value="<%= adminEmailVerificationSubject %>" />

            <liferay-ui:error key="emailVerificationBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailVerificationBodyEditor" %>' name="emailVerificationBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_VERIFICATION_BODY + "--" %>' type="hidden" value="<%= adminEmailPasswordResetBody %>" />
            </aui:field-wrapper>

            <div class="terms email-verification definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>
    <liferay-ui:section>
        <aui:fieldset>
            <liferay-ui:error key="emailPasswordSentSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_PASSWORD_SENT_SUBJECT + "--" %>' type="text" value="<%= adminEmailPasswordSentSubject %>" />

            <liferay-ui:error key="emailPasswordSentBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailPasswordSentBodyEditor" %>' name="emailPasswordSentBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_PASSWORD_SENT_BODY + "--" %>' type="hidden" value="<%= adminEmailPasswordSentBody %>" />
            </aui:field-wrapper>

            <div class="terms email-password-sent definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>
    <liferay-ui:section>
        <aui:fieldset>
            <liferay-ui:error key="emailPasswordResetSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_PASSWORD_RESET_SUBJECT + "--" %>' type="text" value="<%= adminEmailPasswordResetSubject %>" />

            <liferay-ui:error key="emailPasswordResetBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailPasswordResetBodyEditor" %>' name="emailPasswordResetBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_PASSWORD_RESET_BODY + "--" %>' type="hidden" value="<%= adminEmailPasswordResetBody %>" />
            </aui:field-wrapper>

            <div class="terms email-password-sent definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>
    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.certificate.user.achieved.enable--" %>' type="checkbox" value="<%=  valamisEmailCertificateAchievedEnable %>" />
            <liferay-ui:error key="valamisCertificateAchievedSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyCertificateAchievedSubject +"--" %>' type="text" value="<%= valamisEmailCertificateAchievedSubject %>" />

            <liferay-ui:error key="emailCertificateAchievedBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailCertificateAchievedBodyEditor" %>' name="emailCertificateAchievedBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyCertificateAchievedBody +"--" %>' type="hidden" value="<%= valamisEmailCertificateAchievedBody %>" />
            </aui:field-wrapper>

            <div class="terms email-certificate-achieved definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.certificate.user.added.enable--" %>' type="checkbox" value="<%=  valamisEmailCertificateAddedEnable %>" />
            <liferay-ui:error key="valamisCertificateAddedSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyCertificateAddedSubject +"--" %>' type="text" value="<%= valamisEmailCertificateAddedSubject %>" />

            <liferay-ui:error key="emailCertificateAddedBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailCertificateAddedBodyEditor" %>' name="emailCertificateAddedBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyCertificateAddedBody +"--" %>' type="hidden" value="<%= valamisEmailCertificateAddedBody %>" />
            </aui:field-wrapper>

            <div class="terms email-certificate-added definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.certificate.user.deactivated.enable--" %>' type="checkbox" value="<%=  valamisEmailCertificateDeactivatedEnable %>" />
            <liferay-ui:error key="valamisCertificateAddedSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyCertificateDeactivatedSubject +"--" %>' type="text" value="<%= valamisEmailCertificateDeactivatedSubject %>" />

            <liferay-ui:error key="emailCertificateDeactivatedBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailCertificateDeactivatedBodyEditor" %>' name="emailCertificateDeactivatedBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyCertificateDeactivatedBody +"--" %>' type="hidden" value="<%= valamisEmailCertificateDeactivatedBody %>" />
            </aui:field-wrapper>

            <div class="terms email-certificate-deactivated definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.course.user.added.enable--" %>' type="checkbox" value="<%=  valamisEmailCourseAddedEnable %>" />
            <liferay-ui:error key="valamisCourseAddedSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyCourseAddedSubject +"--" %>' type="text" value="<%= valamisEmailCourseAddedSubject %>" />

            <liferay-ui:error key="emailCourseAddedBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailCourseAddedBodyEditor" %>' name="emailCourseAddedBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyCourseAddedBody +"--" %>' type="hidden" value="<%= valamisEmailCourseAddedBody %>" />
            </aui:field-wrapper>

            <div class="terms email-course-added definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.course.lesson.available.enable--" %>' type="checkbox" value="<%=  valamisEmailCourseLessonAvailableEnable %>" />
            <liferay-ui:error key="valamisCourseLessonAvailableSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyCourseLessonAvailableSubject +"--" %>' type="text" value="<%= valamisEmailLessonAvailableSubject %>" />

            <liferay-ui:error key="emailCourseLessonAvailableBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailCourseLessonAvailableBodyEditor" %>' name="emailCourseLessonAvailableBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyCourseLessonAvailableBody +"--" %>' type="hidden" value="<%= valamisEmailCourseLessonAvailableBody %>" />
            </aui:field-wrapper>

            <div class="terms email-course-lesson-available definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.grade.course.enable--" %>' type="checkbox" value="<%=  valamisEmailGradeCourseEnable %>" />
            <liferay-ui:error key="valamisGradeCourseSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyGradeCourseSubject +"--" %>' type="text" value="<%= valamisEmailGradeCourseSubject %>" />

            <liferay-ui:error key="emailGradeCourseBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailGradeCourseBodyEditor" %>' name="emailGradeCourseBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyGradeCourseBody +"--" %>' type="hidden" value="<%= valamisEmailGradeCourseBody %>" />
            </aui:field-wrapper>

            <div class="terms email-grade-course definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>


    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.grade.lesson.enable--" %>' type="checkbox" value="<%=  valamisEmailGradeLessonEnable %>" />
            <liferay-ui:error key="valamisGradeLessonSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyGradeLessonSubject +"--" %>' type="text" value="<%= valamisEmailGradeLessonSubject %>" />

            <liferay-ui:error key="emailGradeLessonBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailGradeLessonBodyEditor" %>' name="emailGradeLessonBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyGradeLessonBody +"--" %>' type="hidden" value="<%= valamisEmailGradeLessonBody %>" />
            </aui:field-wrapper>

            <div class="terms email-grade-lesson definition-of-terms">
                <%@ include file="/html/portlet/portal_settings/definition_of_terms.jspf" %>
            </div>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.certificate.expires.enable--" %>' type="checkbox" value="<%= valamisEmailCertificateExpiresEnable %>" />
            <liferay-ui:error key="valamisCertificateExpiresSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyCertificateExpiresSubject +"--" %>' type="text" value="<%= valamisEmailCertificateExpiresSubject %>" />

            <liferay-ui:error key="emailCertificateExpiresBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailCertificateExpiresBodyEditor" %>' name="emailCertificateExpiresBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyCertificateExpiresBody +"--" %>' type="hidden" value="<%= valamisEmailCertificateExpiresBody %>" />
            </aui:field-wrapper>

            <div class="terms email-certificate-expires definition-of-terms">
                <dl>
                    <dt>
                        [$USER_SCREENNAME$]
                    </dt>
                    <dd>
                        <liferay-ui:message key="the-user-screen-name"/>
                    </dd>
                    <dt>
                        [$PORTAL_URL$]
                    </dt>
                    <dd>
                        <%= company.getVirtualHostname() %>
                    </dd>
                    <dt>
                        [$DAYS$]
                     </dt>
                     <dd>
                         number of days
                    </dd>
                    <dt>
                        [$CERTIFICATE_LINK$]
                    </dt>
                    <dd>
                        Certificate link
                    </dd>
                </dl>
            </div>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <aui:fieldset>
            <aui:input label="enabled" name='<%= "settings--valamis.certificate.expired.enable--" %>' type="checkbox" value="<%= valamisEmailCertificateExpiredEnable %>" />
            <liferay-ui:error key="valamisCertificateExpiresSubject" message="please-enter-a-valid-subject" />

            <aui:input cssClass="lfr-input-text-container" label="subject" name='<%= "settings--" + keyCertificateExpiredSubject +"--" %>' type="text" value="<%= valamisEmailCertificateExpiredSubject %>" />

            <liferay-ui:error key="emailCertificateExpiredBody" message="please-enter-a-valid-body" />

            <aui:field-wrapper label="body">
                <liferay-ui:input-editor editorImpl="<%= EDITOR_WYSIWYG_IMPL_KEY %>" initMethod='<%= "initEmailCertificateExpiredBodyEditor" %>' name="emailCertificateExpiredBody" toolbarSet="email" width="470" />

                <aui:input name='<%= "settings--"+ keyCertificateExpiredBody +"--" %>' type="hidden" value="<%= valamisEmailCertificateExpiredBody %>" />
            </aui:field-wrapper>

            <div class="terms email-certificate-expired definition-of-terms">
                <dl>
                    <dt>
                        [$USER_SCREENNAME$]
                    </dt>
                    <dd>
                        <liferay-ui:message key="the-user-screen-name"/>
                    </dd>
                    <dt>
                        [$PORTAL_URL$]
                    </dt>
                    <dd>
                        <%= company.getVirtualHostname() %>
                    </dd>
                    <dt>
                        [$CERTIFICATE_LINK$]
                    </dt>
                    <dd>
                        Certificate link
                    </dd>
                </dl>
            </div>
        </aui:fieldset>
    </liferay-ui:section>



</liferay-ui:tabs>

<aui:script>
    function <portlet:namespace />initEmailUserAddedBodyEditor() {
    return "<%= UnicodeFormatter.toString(adminEmailUserAddedBody) %>";
    }

    function <portlet:namespace />initEmailUserAddedNoPasswordBodyEditor() {
    return "<%= UnicodeFormatter.toString(adminEmailUserAddedNoPasswordBody) %>";
    }

    function <portlet:namespace />initEmailPasswordSentBodyEditor() {
    return "<%= UnicodeFormatter.toString(adminEmailPasswordSentBody) %>";
    }

    function <portlet:namespace />initEmailPasswordResetBodyEditor() {
    return "<%= UnicodeFormatter.toString(adminEmailPasswordResetBody) %>";
    }

    function <portlet:namespace />initEmailCertificateAchievedBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailCertificateAchievedBody) %>";
    }

    function <portlet:namespace />initEmailCertificateAddedBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailCertificateAddedBody) %>";
    }

    function <portlet:namespace />initEmailCertificateDeactivatedBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailCertificateDeactivatedBody) %>";
    }

    function <portlet:namespace />initEmailCourseAddedBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailCourseAddedBody) %>";
    }

    function <portlet:namespace />initEmailCourseLessonAvailableBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailCourseLessonAvailableBody) %>";
    }

    function <portlet:namespace />initEmailGradeCourseBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailGradeCourseBody) %>";
    }

    function <portlet:namespace />initEmailGradeLessonBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailGradeLessonBody) %>";
    }

    function <portlet:namespace />initEmailCertificateExpiresBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailCertificateExpiresBody) %>";
    }

    function <portlet:namespace />initEmailCertificateExpiredBodyEditor() {
    return "<%= UnicodeFormatter.toString(valamisEmailCertificateExpiredBody) %>";
    }

    function <portlet:namespace />initEmailVerificationBodyEditor() {
    return "<%= UnicodeFormatter.toString(adminEmailVerificationBody) %>";
    }

    function <portlet:namespace />saveEmails() {
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%= PropsKeys.ADMIN_EMAIL_USER_ADDED_BODY %>--'].value = window['<portlet:namespace />emailUserAddedBody'].getHTML();
    }
    catch (e) {
    }

    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%= PropsKeys.ADMIN_EMAIL_USER_ADDED_NO_PASSWORD_BODY %>--'].value = window['<portlet:namespace />emailUserAddedNoPasswordBody'].getHTML();
    }
    catch (e) {
    }

    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%= PropsKeys.ADMIN_EMAIL_PASSWORD_SENT_BODY %>--'].value = window['<portlet:namespace />emailPasswordSentBody'].getHTML();
    }
    catch (e) {
    }

    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%= PropsKeys.ADMIN_EMAIL_PASSWORD_RESET_BODY %>--'].value = window['<portlet:namespace />emailPasswordResetBody'].getHTML();
    }
    catch (e) {
    }

    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%= PropsKeys.ADMIN_EMAIL_VERIFICATION_BODY %>--'].value = window['<portlet:namespace />emailVerificationBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyCertificateAchievedBody%>--'].value = window['<portlet:namespace />emailCertificateAchievedBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyCertificateAddedBody%>--'].value = window['<portlet:namespace />emailCertificateAddedBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyCertificateDeactivatedBody%>--'].value = window['<portlet:namespace />emailCertificateDeactivatedBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyCourseAddedBody%>--'].value = window['<portlet:namespace />emailCourseAddedBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyGradeCourseBody%>--'].value = window['<portlet:namespace />emailGradeCourseBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyGradeLessonBody%>--'].value = window['<portlet:namespace />emailGradeLessonBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyCertificateExpiresBody%>--'].value = window['<portlet:namespace />emailCetificateExpiresBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyCertificateExpiredBody%>--'].value = window['<portlet:namespace />emailCetificateExpiredBody'].getHTML();
    }
    catch (e) {
    }
    try {
    document.<portlet:namespace />fm['<portlet:namespace />settings--<%=keyCourseLessonAvailableBody%>--'].value = window['<portlet:namespace />emailCourseLessonAvailableBody'].getHTML();
    }
    catch (e) {
    }
    }
</aui:script>

<%!
    public static final String EDITOR_WYSIWYG_IMPL_KEY = "editor.wysiwyg.portal-web.docroot.html.portlet.portal_settings.email_notifications.jsp";
%>