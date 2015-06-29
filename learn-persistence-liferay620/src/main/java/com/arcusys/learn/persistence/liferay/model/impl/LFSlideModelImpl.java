package com.arcusys.learn.persistence.liferay.model.impl;

import com.arcusys.learn.persistence.liferay.model.LFSlide;
import com.arcusys.learn.persistence.liferay.model.LFSlideModel;

import com.liferay.portal.kernel.bean.AutoEscapeBeanHandler;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.model.impl.BaseModelImpl;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.util.ExpandoBridgeFactoryUtil;

import java.io.Serializable;

import java.sql.Types;

import java.util.HashMap;
import java.util.Map;

/**
 * The base model implementation for the LFSlide service. Represents a row in the &quot;Learn_LFSlide&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This implementation and its corresponding interface {@link com.arcusys.learn.persistence.liferay.model.LFSlideModel} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link LFSlideImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see LFSlideImpl
 * @see com.arcusys.learn.persistence.liferay.model.LFSlide
 * @see com.arcusys.learn.persistence.liferay.model.LFSlideModel
 * @generated
 */
public class LFSlideModelImpl extends BaseModelImpl<LFSlide>
    implements LFSlideModel {
    /*
     * NOTE FOR DEVELOPERS:
     *
     * Never modify or reference this class directly. All methods that expect a l f slide model instance should use the {@link com.arcusys.learn.persistence.liferay.model.LFSlide} interface instead.
     */
    public static final String TABLE_NAME = "Learn_LFSlide";
    public static final Object[][] TABLE_COLUMNS = {
            { "id_", Types.BIGINT },
            { "bgcolor", Types.VARCHAR },
            { "bgimage", Types.VARCHAR },
            { "title", Types.VARCHAR },
            { "slideSetId", Types.BIGINT },
            { "topSlideId", Types.BIGINT },
            { "leftSlideId", Types.BIGINT },
            { "statementVerb", Types.VARCHAR },
            { "statementObject", Types.VARCHAR },
            { "statementCategoryId", Types.VARCHAR }
        };
    public static final String TABLE_SQL_CREATE = "create table Learn_LFSlide (id_ LONG not null primary key,bgcolor VARCHAR(75) null,bgimage VARCHAR(512) null,title VARCHAR(75) null,slideSetId LONG null,topSlideId LONG null,leftSlideId LONG null,statementVerb VARCHAR(75) null,statementObject VARCHAR(75) null,statementCategoryId VARCHAR(75) null)";
    public static final String TABLE_SQL_DROP = "drop table Learn_LFSlide";
    public static final String ORDER_BY_JPQL = " ORDER BY lfSlide.id ASC";
    public static final String ORDER_BY_SQL = " ORDER BY Learn_LFSlide.id_ ASC";
    public static final String DATA_SOURCE = "liferayDataSource";
    public static final String SESSION_FACTORY = "liferaySessionFactory";
    public static final String TX_MANAGER = "liferayTransactionManager";
    public static final boolean ENTITY_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.util.service.ServiceProps.get(
                "value.object.entity.cache.enabled.com.arcusys.learn.persistence.liferay.model.LFSlide"),
            false);
    public static final boolean FINDER_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.util.service.ServiceProps.get(
                "value.object.finder.cache.enabled.com.arcusys.learn.persistence.liferay.model.LFSlide"),
            false);
    public static final boolean COLUMN_BITMASK_ENABLED = false;
    public static final long LOCK_EXPIRATION_TIME = GetterUtil.getLong(com.liferay.util.service.ServiceProps.get(
                "lock.expiration.time.com.arcusys.learn.persistence.liferay.model.LFSlide"));
    private static ClassLoader _classLoader = LFSlide.class.getClassLoader();
    private static Class<?>[] _escapedModelInterfaces = new Class[] {
            LFSlide.class
        };
    private long _id;
    private String _bgcolor;
    private String _bgimage;
    private String _title;
    private Long _slideSetId;
    private Long _topSlideId;
    private Long _leftSlideId;
    private String _statementVerb;
    private String _statementObject;
    private String _statementCategoryId;
    private LFSlide _escapedModel;

    public LFSlideModelImpl() {
    }

    @Override
    public long getPrimaryKey() {
        return _id;
    }

    @Override
    public void setPrimaryKey(long primaryKey) {
        setId(primaryKey);
    }

    @Override
    public Serializable getPrimaryKeyObj() {
        return _id;
    }

    @Override
    public void setPrimaryKeyObj(Serializable primaryKeyObj) {
        setPrimaryKey(((Long) primaryKeyObj).longValue());
    }

    @Override
    public Class<?> getModelClass() {
        return LFSlide.class;
    }

    @Override
    public String getModelClassName() {
        return LFSlide.class.getName();
    }

    @Override
    public Map<String, Object> getModelAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();

        attributes.put("id", getId());
        attributes.put("bgcolor", getBgcolor());
        attributes.put("bgimage", getBgimage());
        attributes.put("title", getTitle());
        attributes.put("slideSetId", getSlideSetId());
        attributes.put("topSlideId", getTopSlideId());
        attributes.put("leftSlideId", getLeftSlideId());
        attributes.put("statementVerb", getStatementVerb());
        attributes.put("statementObject", getStatementObject());
        attributes.put("statementCategoryId", getStatementCategoryId());

        return attributes;
    }

    @Override
    public void setModelAttributes(Map<String, Object> attributes) {
        Long id = (Long) attributes.get("id");

        if (id != null) {
            setId(id);
        }

        String bgcolor = (String) attributes.get("bgcolor");

        if (bgcolor != null) {
            setBgcolor(bgcolor);
        }

        String bgimage = (String) attributes.get("bgimage");

        if (bgimage != null) {
            setBgimage(bgimage);
        }

        String title = (String) attributes.get("title");

        if (title != null) {
            setTitle(title);
        }

        Long slideSetId = (Long) attributes.get("slideSetId");

        if (slideSetId != null) {
            setSlideSetId(slideSetId);
        }

        Long topSlideId = (Long) attributes.get("topSlideId");

        if (topSlideId != null) {
            setTopSlideId(topSlideId);
        }

        Long leftSlideId = (Long) attributes.get("leftSlideId");

        if (leftSlideId != null) {
            setLeftSlideId(leftSlideId);
        }

        String statementVerb = (String) attributes.get("statementVerb");

        if (statementVerb != null) {
            setStatementVerb(statementVerb);
        }

        String statementObject = (String) attributes.get("statementObject");

        if (statementObject != null) {
            setStatementObject(statementObject);
        }

        String statementCategoryId = (String) attributes.get(
                "statementCategoryId");

        if (statementCategoryId != null) {
            setStatementCategoryId(statementCategoryId);
        }
    }

    @Override
    public long getId() {
        return _id;
    }

    @Override
    public void setId(long id) {
        _id = id;
    }

    @Override
    public String getBgcolor() {
        return _bgcolor;
    }

    @Override
    public void setBgcolor(String bgcolor) {
        _bgcolor = bgcolor;
    }

    @Override
    public String getBgimage() {
        return _bgimage;
    }

    @Override
    public void setBgimage(String bgimage) {
        _bgimage = bgimage;
    }

    @Override
    public String getTitle() {
        return _title;
    }

    @Override
    public void setTitle(String title) {
        _title = title;
    }

    @Override
    public Long getSlideSetId() {
        return _slideSetId;
    }

    @Override
    public void setSlideSetId(Long slideSetId) {
        _slideSetId = slideSetId;
    }

    @Override
    public Long getTopSlideId() {
        return _topSlideId;
    }

    @Override
    public void setTopSlideId(Long topSlideId) {
        _topSlideId = topSlideId;
    }

    @Override
    public Long getLeftSlideId() {
        return _leftSlideId;
    }

    @Override
    public void setLeftSlideId(Long leftSlideId) {
        _leftSlideId = leftSlideId;
    }

    @Override
    public String getStatementVerb() {
        return _statementVerb;
    }

    @Override
    public void setStatementVerb(String statementVerb) {
        _statementVerb = statementVerb;
    }

    @Override
    public String getStatementObject() {
        return _statementObject;
    }

    @Override
    public void setStatementObject(String statementObject) {
        _statementObject = statementObject;
    }

    @Override
    public String getStatementCategoryId() {
        return _statementCategoryId;
    }

    @Override
    public void setStatementCategoryId(String statementCategoryId) {
        _statementCategoryId = statementCategoryId;
    }

    @Override
    public ExpandoBridge getExpandoBridge() {
        return ExpandoBridgeFactoryUtil.getExpandoBridge(0,
            LFSlide.class.getName(), getPrimaryKey());
    }

    @Override
    public void setExpandoBridgeAttributes(ServiceContext serviceContext) {
        ExpandoBridge expandoBridge = getExpandoBridge();

        expandoBridge.setAttributes(serviceContext);
    }

    @Override
    public LFSlide toEscapedModel() {
        if (_escapedModel == null) {
            _escapedModel = (LFSlide) ProxyUtil.newProxyInstance(_classLoader,
                    _escapedModelInterfaces, new AutoEscapeBeanHandler(this));
        }

        return _escapedModel;
    }

    @Override
    public Object clone() {
        LFSlideImpl lfSlideImpl = new LFSlideImpl();

        lfSlideImpl.setId(getId());
        lfSlideImpl.setBgcolor(getBgcolor());
        lfSlideImpl.setBgimage(getBgimage());
        lfSlideImpl.setTitle(getTitle());
        lfSlideImpl.setSlideSetId(getSlideSetId());
        lfSlideImpl.setTopSlideId(getTopSlideId());
        lfSlideImpl.setLeftSlideId(getLeftSlideId());
        lfSlideImpl.setStatementVerb(getStatementVerb());
        lfSlideImpl.setStatementObject(getStatementObject());
        lfSlideImpl.setStatementCategoryId(getStatementCategoryId());

        lfSlideImpl.resetOriginalValues();

        return lfSlideImpl;
    }

    @Override
    public int compareTo(LFSlide lfSlide) {
        long primaryKey = lfSlide.getPrimaryKey();

        if (getPrimaryKey() < primaryKey) {
            return -1;
        } else if (getPrimaryKey() > primaryKey) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LFSlide)) {
            return false;
        }

        LFSlide lfSlide = (LFSlide) obj;

        long primaryKey = lfSlide.getPrimaryKey();

        if (getPrimaryKey() == primaryKey) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) getPrimaryKey();
    }

    @Override
    public void resetOriginalValues() {
    }

    @Override
    public CacheModel<LFSlide> toCacheModel() {
        LFSlideCacheModel lfSlideCacheModel = new LFSlideCacheModel();

        lfSlideCacheModel.id = getId();

        lfSlideCacheModel.bgcolor = getBgcolor();

        String bgcolor = lfSlideCacheModel.bgcolor;

        if ((bgcolor != null) && (bgcolor.length() == 0)) {
            lfSlideCacheModel.bgcolor = null;
        }

        lfSlideCacheModel.bgimage = getBgimage();

        String bgimage = lfSlideCacheModel.bgimage;

        if ((bgimage != null) && (bgimage.length() == 0)) {
            lfSlideCacheModel.bgimage = null;
        }

        lfSlideCacheModel.title = getTitle();

        String title = lfSlideCacheModel.title;

        if ((title != null) && (title.length() == 0)) {
            lfSlideCacheModel.title = null;
        }

        lfSlideCacheModel.slideSetId = getSlideSetId();

        lfSlideCacheModel.topSlideId = getTopSlideId();

        lfSlideCacheModel.leftSlideId = getLeftSlideId();

        lfSlideCacheModel.statementVerb = getStatementVerb();

        String statementVerb = lfSlideCacheModel.statementVerb;

        if ((statementVerb != null) && (statementVerb.length() == 0)) {
            lfSlideCacheModel.statementVerb = null;
        }

        lfSlideCacheModel.statementObject = getStatementObject();

        String statementObject = lfSlideCacheModel.statementObject;

        if ((statementObject != null) && (statementObject.length() == 0)) {
            lfSlideCacheModel.statementObject = null;
        }

        lfSlideCacheModel.statementCategoryId = getStatementCategoryId();

        String statementCategoryId = lfSlideCacheModel.statementCategoryId;

        if ((statementCategoryId != null) &&
                (statementCategoryId.length() == 0)) {
            lfSlideCacheModel.statementCategoryId = null;
        }

        return lfSlideCacheModel;
    }

    @Override
    public String toString() {
        StringBundler sb = new StringBundler(21);

        sb.append("{id=");
        sb.append(getId());
        sb.append(", bgcolor=");
        sb.append(getBgcolor());
        sb.append(", bgimage=");
        sb.append(getBgimage());
        sb.append(", title=");
        sb.append(getTitle());
        sb.append(", slideSetId=");
        sb.append(getSlideSetId());
        sb.append(", topSlideId=");
        sb.append(getTopSlideId());
        sb.append(", leftSlideId=");
        sb.append(getLeftSlideId());
        sb.append(", statementVerb=");
        sb.append(getStatementVerb());
        sb.append(", statementObject=");
        sb.append(getStatementObject());
        sb.append(", statementCategoryId=");
        sb.append(getStatementCategoryId());
        sb.append("}");

        return sb.toString();
    }

    @Override
    public String toXmlString() {
        StringBundler sb = new StringBundler(34);

        sb.append("<model><model-name>");
        sb.append("com.arcusys.learn.persistence.liferay.model.LFSlide");
        sb.append("</model-name>");

        sb.append(
            "<column><column-name>id</column-name><column-value><![CDATA[");
        sb.append(getId());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>bgcolor</column-name><column-value><![CDATA[");
        sb.append(getBgcolor());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>bgimage</column-name><column-value><![CDATA[");
        sb.append(getBgimage());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>title</column-name><column-value><![CDATA[");
        sb.append(getTitle());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>slideSetId</column-name><column-value><![CDATA[");
        sb.append(getSlideSetId());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>topSlideId</column-name><column-value><![CDATA[");
        sb.append(getTopSlideId());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>leftSlideId</column-name><column-value><![CDATA[");
        sb.append(getLeftSlideId());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>statementVerb</column-name><column-value><![CDATA[");
        sb.append(getStatementVerb());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>statementObject</column-name><column-value><![CDATA[");
        sb.append(getStatementObject());
        sb.append("]]></column-value></column>");
        sb.append(
            "<column><column-name>statementCategoryId</column-name><column-value><![CDATA[");
        sb.append(getStatementCategoryId());
        sb.append("]]></column-value></column>");

        sb.append("</model>");

        return sb.toString();
    }
}
