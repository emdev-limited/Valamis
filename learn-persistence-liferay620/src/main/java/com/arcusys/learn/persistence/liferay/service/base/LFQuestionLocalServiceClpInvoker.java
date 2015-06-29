package com.arcusys.learn.persistence.liferay.service.base;

import com.arcusys.learn.persistence.liferay.service.LFQuestionLocalServiceUtil;

import java.util.Arrays;

/**
 * @author Brian Wing Shun Chan
 * @generated
 */
public class LFQuestionLocalServiceClpInvoker {
    private String _methodName0;
    private String[] _methodParameterTypes0;
    private String _methodName1;
    private String[] _methodParameterTypes1;
    private String _methodName2;
    private String[] _methodParameterTypes2;
    private String _methodName3;
    private String[] _methodParameterTypes3;
    private String _methodName4;
    private String[] _methodParameterTypes4;
    private String _methodName5;
    private String[] _methodParameterTypes5;
    private String _methodName6;
    private String[] _methodParameterTypes6;
    private String _methodName7;
    private String[] _methodParameterTypes7;
    private String _methodName8;
    private String[] _methodParameterTypes8;
    private String _methodName9;
    private String[] _methodParameterTypes9;
    private String _methodName10;
    private String[] _methodParameterTypes10;
    private String _methodName11;
    private String[] _methodParameterTypes11;
    private String _methodName12;
    private String[] _methodParameterTypes12;
    private String _methodName13;
    private String[] _methodParameterTypes13;
    private String _methodName14;
    private String[] _methodParameterTypes14;
    private String _methodName15;
    private String[] _methodParameterTypes15;
    private String _methodName216;
    private String[] _methodParameterTypes216;
    private String _methodName217;
    private String[] _methodParameterTypes217;
    private String _methodName222;
    private String[] _methodParameterTypes222;
    private String _methodName223;
    private String[] _methodParameterTypes223;
    private String _methodName224;
    private String[] _methodParameterTypes224;
    private String _methodName225;
    private String[] _methodParameterTypes225;

    public LFQuestionLocalServiceClpInvoker() {
        _methodName0 = "addLFQuestion";

        _methodParameterTypes0 = new String[] {
                "com.arcusys.learn.persistence.liferay.model.LFQuestion"
            };

        _methodName1 = "createLFQuestion";

        _methodParameterTypes1 = new String[] { "long" };

        _methodName2 = "deleteLFQuestion";

        _methodParameterTypes2 = new String[] { "long" };

        _methodName3 = "deleteLFQuestion";

        _methodParameterTypes3 = new String[] {
                "com.arcusys.learn.persistence.liferay.model.LFQuestion"
            };

        _methodName4 = "dynamicQuery";

        _methodParameterTypes4 = new String[] {  };

        _methodName5 = "dynamicQuery";

        _methodParameterTypes5 = new String[] {
                "com.liferay.portal.kernel.dao.orm.DynamicQuery"
            };

        _methodName6 = "dynamicQuery";

        _methodParameterTypes6 = new String[] {
                "com.liferay.portal.kernel.dao.orm.DynamicQuery", "int", "int"
            };

        _methodName7 = "dynamicQuery";

        _methodParameterTypes7 = new String[] {
                "com.liferay.portal.kernel.dao.orm.DynamicQuery", "int", "int",
                "com.liferay.portal.kernel.util.OrderByComparator"
            };

        _methodName8 = "dynamicQueryCount";

        _methodParameterTypes8 = new String[] {
                "com.liferay.portal.kernel.dao.orm.DynamicQuery"
            };

        _methodName9 = "dynamicQueryCount";

        _methodParameterTypes9 = new String[] {
                "com.liferay.portal.kernel.dao.orm.DynamicQuery",
                "com.liferay.portal.kernel.dao.orm.Projection"
            };

        _methodName10 = "fetchLFQuestion";

        _methodParameterTypes10 = new String[] { "long" };

        _methodName11 = "getLFQuestion";

        _methodParameterTypes11 = new String[] { "long" };

        _methodName12 = "getPersistedModel";

        _methodParameterTypes12 = new String[] { "java.io.Serializable" };

        _methodName13 = "getLFQuestions";

        _methodParameterTypes13 = new String[] { "int", "int" };

        _methodName14 = "getLFQuestionsCount";

        _methodParameterTypes14 = new String[] {  };

        _methodName15 = "updateLFQuestion";

        _methodParameterTypes15 = new String[] {
                "com.arcusys.learn.persistence.liferay.model.LFQuestion"
            };

        _methodName216 = "getBeanIdentifier";

        _methodParameterTypes216 = new String[] {  };

        _methodName217 = "setBeanIdentifier";

        _methodParameterTypes217 = new String[] { "java.lang.String" };

        _methodName222 = "createLFQuestion";

        _methodParameterTypes222 = new String[] {  };

        _methodName223 = "findByCourseIdAndCategoryId";

        _methodParameterTypes223 = new String[] {
                "java.lang.Integer[][]", "java.lang.Integer[][]"
            };

        _methodName224 = "removeAll";

        _methodParameterTypes224 = new String[] {  };

        _methodName225 = "getLFQuestion";

        _methodParameterTypes225 = new String[] { "long" };
    }

    public Object invokeMethod(String name, String[] parameterTypes,
        Object[] arguments) throws Throwable {
        if (_methodName0.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes0, parameterTypes)) {
            return LFQuestionLocalServiceUtil.addLFQuestion((com.arcusys.learn.persistence.liferay.model.LFQuestion) arguments[0]);
        }

        if (_methodName1.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes1, parameterTypes)) {
            return LFQuestionLocalServiceUtil.createLFQuestion(((Long) arguments[0]).longValue());
        }

        if (_methodName2.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes2, parameterTypes)) {
            return LFQuestionLocalServiceUtil.deleteLFQuestion(((Long) arguments[0]).longValue());
        }

        if (_methodName3.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes3, parameterTypes)) {
            return LFQuestionLocalServiceUtil.deleteLFQuestion((com.arcusys.learn.persistence.liferay.model.LFQuestion) arguments[0]);
        }

        if (_methodName4.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes4, parameterTypes)) {
            return LFQuestionLocalServiceUtil.dynamicQuery();
        }

        if (_methodName5.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes5, parameterTypes)) {
            return LFQuestionLocalServiceUtil.dynamicQuery((com.liferay.portal.kernel.dao.orm.DynamicQuery) arguments[0]);
        }

        if (_methodName6.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes6, parameterTypes)) {
            return LFQuestionLocalServiceUtil.dynamicQuery((com.liferay.portal.kernel.dao.orm.DynamicQuery) arguments[0],
                ((Integer) arguments[1]).intValue(),
                ((Integer) arguments[2]).intValue());
        }

        if (_methodName7.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes7, parameterTypes)) {
            return LFQuestionLocalServiceUtil.dynamicQuery((com.liferay.portal.kernel.dao.orm.DynamicQuery) arguments[0],
                ((Integer) arguments[1]).intValue(),
                ((Integer) arguments[2]).intValue(),
                (com.liferay.portal.kernel.util.OrderByComparator) arguments[3]);
        }

        if (_methodName8.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes8, parameterTypes)) {
            return LFQuestionLocalServiceUtil.dynamicQueryCount((com.liferay.portal.kernel.dao.orm.DynamicQuery) arguments[0]);
        }

        if (_methodName9.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes9, parameterTypes)) {
            return LFQuestionLocalServiceUtil.dynamicQueryCount((com.liferay.portal.kernel.dao.orm.DynamicQuery) arguments[0],
                (com.liferay.portal.kernel.dao.orm.Projection) arguments[1]);
        }

        if (_methodName10.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes10, parameterTypes)) {
            return LFQuestionLocalServiceUtil.fetchLFQuestion(((Long) arguments[0]).longValue());
        }

        if (_methodName11.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes11, parameterTypes)) {
            return LFQuestionLocalServiceUtil.getLFQuestion(((Long) arguments[0]).longValue());
        }

        if (_methodName12.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes12, parameterTypes)) {
            return LFQuestionLocalServiceUtil.getPersistedModel((java.io.Serializable) arguments[0]);
        }

        if (_methodName13.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes13, parameterTypes)) {
            return LFQuestionLocalServiceUtil.getLFQuestions(((Integer) arguments[0]).intValue(),
                ((Integer) arguments[1]).intValue());
        }

        if (_methodName14.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes14, parameterTypes)) {
            return LFQuestionLocalServiceUtil.getLFQuestionsCount();
        }

        if (_methodName15.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes15, parameterTypes)) {
            return LFQuestionLocalServiceUtil.updateLFQuestion((com.arcusys.learn.persistence.liferay.model.LFQuestion) arguments[0]);
        }

        if (_methodName216.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes216, parameterTypes)) {
            return LFQuestionLocalServiceUtil.getBeanIdentifier();
        }

        if (_methodName217.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes217, parameterTypes)) {
            LFQuestionLocalServiceUtil.setBeanIdentifier((java.lang.String) arguments[0]);

            return null;
        }

        if (_methodName222.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes222, parameterTypes)) {
            return LFQuestionLocalServiceUtil.createLFQuestion();
        }

        if (_methodName223.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes223, parameterTypes)) {
            return LFQuestionLocalServiceUtil.findByCourseIdAndCategoryId((java.lang.Integer[]) arguments[0],
                (java.lang.Integer[]) arguments[1]);
        }

        if (_methodName224.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes224, parameterTypes)) {
            LFQuestionLocalServiceUtil.removeAll();

            return null;
        }

        if (_methodName225.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes225, parameterTypes)) {
            return LFQuestionLocalServiceUtil.getLFQuestion(((Long) arguments[0]).longValue());
        }

        throw new UnsupportedOperationException();
    }
}
