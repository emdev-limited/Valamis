package com.arcusys.learn.persistence.liferay.service.messaging;

import com.arcusys.learn.persistence.liferay.service.*;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Message;


public class ClpMessageListener extends BaseMessageListener {
    public static String getServletContextName() {
        return ClpSerializer.getServletContextName();
    }

    @Override
    protected void doReceive(Message message) throws Exception {
        String command = message.getString("command");
        String servletContextName = message.getString("servletContextName");

        if (command.equals("undeploy") &&
                servletContextName.equals(getServletContextName())) {
            LFActivityLocalServiceUtil.clearService();

            LFActivityDataMapLocalServiceUtil.clearService();

            LFActivityStateLocalServiceUtil.clearService();

            LFActivityStateNodeLocalServiceUtil.clearService();

            LFActivityStateTreeLocalServiceUtil.clearService();

            LFAnswerLocalServiceUtil.clearService();

            LFAttemptLocalServiceUtil.clearService();

            LFAttemptDataLocalServiceUtil.clearService();

            LFBigDecimalLocalServiceUtil.clearService();

            LFChildrenSelectionLocalServiceUtil.clearService();

            LFConditionRuleLocalServiceUtil.clearService();

            LFCourseLocalServiceUtil.clearService();

            LFGlblObjectiveStateLocalServiceUtil.clearService();

            LFLessonLimitLocalServiceUtil.clearService();

            LFLRSToActivitySettingLocalServiceUtil.clearService();

            LFObjectiveLocalServiceUtil.clearService();

            LFObjectiveMapLocalServiceUtil.clearService();

            LFObjectiveStateLocalServiceUtil.clearService();

            LFPackageLocalServiceUtil.clearService();

            LFPackageGradeStorageLocalServiceUtil.clearService();

            LFPackageScopeRuleLocalServiceUtil.clearService();

            LFPlayerScopeRuleLocalServiceUtil.clearService();

            LFQuestionLocalServiceUtil.clearService();

            LFQuestionCategoryLocalServiceUtil.clearService();

            LFQuizLocalServiceUtil.clearService();

            LFQuizAnswerScoreLocalServiceUtil.clearService();

            LFQuizQuestCatLocalServiceUtil.clearService();

            LFQuizQuestionLocalServiceUtil.clearService();

            LFQuizTreeElementLocalServiceUtil.clearService();

            LFResourceLocalServiceUtil.clearService();

            LFRollupContributionLocalServiceUtil.clearService();

            LFRollupRuleLocalServiceUtil.clearService();

            LFRuleConditionLocalServiceUtil.clearService();

            LFSeqPermissionsLocalServiceUtil.clearService();

            LFSequencingLocalServiceUtil.clearService();

            LFSequencingTrackingLocalServiceUtil.clearService();

            LFSiteDependentConfigLocalServiceUtil.clearService();

            LFTCClntApiStorageLocalServiceUtil.clearService();

            LFTincanLrsEndpointLocalServiceUtil.clearService();

            LFTincanManifestActLocalServiceUtil.clearService();

            LFTincanPackageLocalServiceUtil.clearService();

            LFTincanURILocalServiceUtil.clearService();

            LFUserLocalServiceUtil.clearService();
        }
    }
}
