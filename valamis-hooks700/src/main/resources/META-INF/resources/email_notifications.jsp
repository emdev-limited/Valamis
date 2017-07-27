<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

<%@ include file="/init.jsp" %>

<h3><liferay-ui:message key="email-notifications" /></h3>

<%
    String adminEmailFromName = PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_FROM_NAME);
    String adminEmailFromAddress = PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

    PortletPreferences companyPortletPreferences = PrefsPropsUtil.getPreferences(company.getCompanyId(), true);

%>

<liferay-ui:error-marker key="<%= WebKeys.ERROR_SECTION %>" value="email_notifications" />

<liferay-ui:tabs
        names="sender,account-created-notification,email-verification-notification,password-changed-notification,password-reset-notification,valamis-certificate-user-added,valamis-certificate-user-achieved,valamis-certificate-user-deactivated,valamis-course-user-added,valamis-course-lesson-available-added,valamis-grade-course,valamis-grade-lesson,valamis-certificate-expires,valamis-certificate-expired,valamis-event-user-added,valamis-training-event-reminder"
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
            <aui:input label="enabled" name='<%= "settings--" + PropsKeys.ADMIN_EMAIL_USER_ADDED_ENABLED + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), PropsKeys.ADMIN_EMAIL_USER_ADDED_ENABLED) %>" />

            <liferay-ui:error key="emailUserAddedSubject" message="please-enter-a-valid-subject" />

            <aui:field-wrapper label="subject">
                <liferay-ui:input-localized
                        fieldPrefix="settings"
                        fieldPrefixSeparator="--"
                        name="adminEmailUserAddedSubject"
                        xml='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailUserAddedSubject", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_USER_ADDED_SUBJECT)) %>'
                />
            </aui:field-wrapper>

            <liferay-ui:error key="emailUserAddedBody" message="please-enter-a-valid-body" />

            <liferay-frontend:email-notification-settings
                    bodyLabel="body-with-password"
                    emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailUserAddedBody", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_USER_ADDED_BODY)) %>'
                    emailParam="adminEmailUserAdded"
                    fieldPrefix="settings"
                    helpMessage="account-created-notification-body-with-password-help"
                    showEmailEnabled="<%= false %>"
                    showSubject="<%= false %>"
            />

            <liferay-ui:error key="emailUserAddedNoPasswordBody" message="please-enter-a-valid-body" />

            <liferay-frontend:email-notification-settings
                    bodyLabel="body-without-password"
                    emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailUserAddedNoPasswordBody", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_USER_ADDED_NO_PASSWORD_BODY)) %>'
                    emailParam="adminEmailUserAddedNoPassword"
                    fieldPrefix="settings"
                    helpMessage="account-created-notification-body-without-password-help"
                    showEmailEnabled="<%= false %>"
                    showSubject="<%= false %>"
            />

            <aui:fieldset cssClass="definition-of-terms email-user-add terms" label="definition-of-terms">
                <%@ include file="/definition_of_terms.jspf" %>
            </aui:fieldset>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <liferay-ui:error key="emailVerificationSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="emailVerificationBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailVerificationBody", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_VERIFICATION_BODY)) %>'
                emailParam="adminEmailVerification"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailVerificationSubject", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_VERIFICATION_SUBJECT)) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <liferay-ui:error key="emailPasswordSentSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="emailPasswordSentBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailPasswordSentBody", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_PASSWORD_SENT_BODY)) %>'
                emailParam="adminEmailPasswordSent"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailPasswordSentSubject", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_PASSWORD_SENT_SUBJECT)) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>
        <liferay-ui:error key="emailPasswordResetSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="emailPasswordResetBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailPasswordResetBody", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_PASSWORD_RESET_BODY)) %>'
                emailParam="adminEmailPasswordReset"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(companyPortletPreferences, renderRequest, "adminEmailPasswordResetSubject", "settings", ContentUtil.get(ClassLoaderUtil.getPortalClassLoader(), PropsValues.ADMIN_EMAIL_PASSWORD_RESET_SUBJECT)) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.certificate.user.added.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.certificate.user.added.enable") %>" />
        <liferay-ui:error key="valamisCertificateUserAddedSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisCertificateUserAddedBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateUserAddedBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_user_added_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisCertificateUserAdded"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateUserAddedSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_user_added_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>


    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.certificate.user.achieved.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.certificate.user.achieved.enable") %>" />
        <liferay-ui:error key="valamisCertificateUserAchievedSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisCertificateUserAchievedBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateUserAchievedBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_user_achieved_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisCertificateUserAchieved"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateUserAchievedSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_user_achieved_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.certificate.user.deactivated.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.certificate.user.deactivated.enable") %>" />
        <liferay-ui:error key="valamisCertificateUserDeactivatedBody" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisCertificateUserDeactivatedSubject" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateUserDeactivatedBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_deactivated_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisCertificateUserDeactivated"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateUserDeactivatedSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_deactivated_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.course.user.added.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.course.user.added.enable") %>" />
        <liferay-ui:error key="valamisCourseUserAddedBody" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisCourseUserAddedSubject" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCourseUserAddedBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/course_user_added_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisCourseUserAdded"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCourseUserAddedSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/course_user_added_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.course.lesson.available.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.course.lesson.available.enable") %>" />
        <liferay-ui:error key="valamisCourseLessonAvailableBody" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisCourseLessonAvailableSubject" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCourseLessonAvailableBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/course_lesson_available_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisCourseLessonAvailable"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCourseLessonAvailableSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/course_lesson_available_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>


    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.grade.course.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.grade.course.enable") %>" />
        <liferay-ui:error key="valamisGradeCourseBody" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisGradeCourseSubject" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisGradeCourseBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/grade_course_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisGradeCourse"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisGradeCourseSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/grade_course_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.grade.lesson.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.grade.lesson.enable") %>" />
        <liferay-ui:error key="valamisGradeLessonBody" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisGradeLessonSubject" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisGradeLessonBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/grade_lesson_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisGradeLesson"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisGradeLessonSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/grade_lesson_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.certificate.expires.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.certificate.expires.enable") %>" />
        <liferay-ui:error key="valamisCertificateExpiresSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisCertificateExpiresBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateExpiresBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_expires_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisCertificateExpires"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateExpiresSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_expires_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.certificate.expired.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.certificate.expired.enable") %>" />
        <liferay-ui:error key="valamisCertificateExpiredSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisCertificateExpiredBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateExpiredBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_expired_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisCertificateExpired"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisCertificateExpiredSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/certificate_expired_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <%@ include file="/definition_of_terms.jspf" %>
        </aui:fieldset>
    </liferay-ui:section>



    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.events.user.added.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.events.user.added.enable") %>" />
        <liferay-ui:error key="valamisTrainingEventUserAddedSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisTrainingEventUserAddedBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisTrainingEventUserAddedBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/event_user_added_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisTrainingEventUserAdded"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisTrainingEventUserAddedSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/event_user_added_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
                <dt>
                    [$EVENT_LINK$]
                </dt>
                <dd>
                    Event link
                </dd>
        </aui:fieldset>
    </liferay-ui:section>

    <liferay-ui:section>

        <aui:input label="enabled" name='<%= "settings--" + "valamis.event.reminder.enable" + "--" %>' type="checkbox" value="<%= PrefsPropsUtil.getBoolean(company.getCompanyId(), "valamis.event.reminder.enable") %>" />
        <liferay-ui:error key="valamisTrainingEventReminderSubject" message="please-enter-a-valid-subject" />
        <liferay-ui:error key="valamisTrainingEventReminderBody" message="please-enter-a-valid-body" />

        <liferay-frontend:email-notification-settings
                emailBody='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisTrainingEventReminderBody",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/event_reminder_body.tmpl"
                        )
                    ) %>'
                emailParam="valamisTrainingEventReminder"
                emailSubject='<%= LocalizationUtil.getLocalizationXmlFromPreferences(
                        companyPortletPreferences,
                        renderRequest,
                        "valamisTrainingEventReminderSubject",
                        "settings",
                        ContentUtil.get(
                            this.getClass().getClassLoader(),
                            "/META-INF/resources/emails/event_reminder_subject.tmpl"
                        )
                    ) %>'
                fieldPrefix="settings"
                showEmailEnabled="<%= false %>"
        />

        <aui:fieldset cssClass="definition-of-terms email-verification terms" label="definition-of-terms">
            <dt>
                [$EVENT_LINK$]
            </dt>
            <dd>
                Event link
            </dd>
            <dt>
                [$DAYS$]
            </dt>
            <dd>
                Number of days
            </dd>
            <dt>
                [$DATE$]
            </dt>
            <dd>
                Date
            </dd>
        </aui:fieldset>
    </liferay-ui:section>

</liferay-ui:tabs>