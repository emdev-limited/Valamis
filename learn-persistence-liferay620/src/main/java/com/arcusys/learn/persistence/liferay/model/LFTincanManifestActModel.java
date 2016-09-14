package com.arcusys.learn.persistence.liferay.model;

import com.liferay.portal.kernel.bean.AutoEscape;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;

import java.io.Serializable;

/**
 * The base model interface for the LFTincanManifestAct service. Represents a row in the &quot;Learn_LFTincanManifestAct&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.arcusys.learn.persistence.liferay.model.impl.LFTincanManifestActModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.arcusys.learn.persistence.liferay.model.impl.LFTincanManifestActImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see LFTincanManifestAct
 * @see com.arcusys.learn.persistence.liferay.model.impl.LFTincanManifestActImpl
 * @see com.arcusys.learn.persistence.liferay.model.impl.LFTincanManifestActModelImpl
 * @generated
 */
public interface LFTincanManifestActModel extends BaseModel<LFTincanManifestAct> {
    /*
     * NOTE FOR DEVELOPERS:
     *
     * Never modify or reference this interface directly. All methods that expect a l f tincan manifest act model instance should use the {@link LFTincanManifestAct} interface instead.
     */

    /**
     * Returns the primary key of this l f tincan manifest act.
     *
     * @return the primary key of this l f tincan manifest act
     */
    public long getPrimaryKey();

    /**
     * Sets the primary key of this l f tincan manifest act.
     *
     * @param primaryKey the primary key of this l f tincan manifest act
     */
    public void setPrimaryKey(long primaryKey);

    /**
     * Returns the ID of this l f tincan manifest act.
     *
     * @return the ID of this l f tincan manifest act
     */
    public long getId();

    /**
     * Sets the ID of this l f tincan manifest act.
     *
     * @param id the ID of this l f tincan manifest act
     */
    public void setId(long id);

    /**
     * Returns the tincan i d of this l f tincan manifest act.
     *
     * @return the tincan i d of this l f tincan manifest act
     */
    @AutoEscape
    public String getTincanID();

    /**
     * Sets the tincan i d of this l f tincan manifest act.
     *
     * @param tincanID the tincan i d of this l f tincan manifest act
     */
    public void setTincanID(String tincanID);

    /**
     * Returns the package i d of this l f tincan manifest act.
     *
     * @return the package i d of this l f tincan manifest act
     */
    public Long getPackageID();

    /**
     * Sets the package i d of this l f tincan manifest act.
     *
     * @param packageID the package i d of this l f tincan manifest act
     */
    public void setPackageID(Long packageID);

    /**
     * Returns the activity type of this l f tincan manifest act.
     *
     * @return the activity type of this l f tincan manifest act
     */
    @AutoEscape
    public String getActivityType();

    /**
     * Sets the activity type of this l f tincan manifest act.
     *
     * @param activityType the activity type of this l f tincan manifest act
     */
    public void setActivityType(String activityType);

    /**
     * Returns the name of this l f tincan manifest act.
     *
     * @return the name of this l f tincan manifest act
     */
    @AutoEscape
    public String getName();

    /**
     * Sets the name of this l f tincan manifest act.
     *
     * @param name the name of this l f tincan manifest act
     */
    public void setName(String name);

    /**
     * Returns the description of this l f tincan manifest act.
     *
     * @return the description of this l f tincan manifest act
     */
    @AutoEscape
    public String getDescription();

    /**
     * Sets the description of this l f tincan manifest act.
     *
     * @param description the description of this l f tincan manifest act
     */
    public void setDescription(String description);

    /**
     * Returns the launch of this l f tincan manifest act.
     *
     * @return the launch of this l f tincan manifest act
     */
    @AutoEscape
    public String getLaunch();

    /**
     * Sets the launch of this l f tincan manifest act.
     *
     * @param launch the launch of this l f tincan manifest act
     */
    public void setLaunch(String launch);

    /**
     * Returns the resource i d of this l f tincan manifest act.
     *
     * @return the resource i d of this l f tincan manifest act
     */
    @AutoEscape
    public String getResourceID();

    /**
     * Sets the resource i d of this l f tincan manifest act.
     *
     * @param resourceID the resource i d of this l f tincan manifest act
     */
    public void setResourceID(String resourceID);

    @Override
    public boolean isNew();

    @Override
    public void setNew(boolean n);

    @Override
    public boolean isCachedModel();

    @Override
    public void setCachedModel(boolean cachedModel);

    @Override
    public boolean isEscapedModel();

    @Override
    public Serializable getPrimaryKeyObj();

    @Override
    public void setPrimaryKeyObj(Serializable primaryKeyObj);

    @Override
    public ExpandoBridge getExpandoBridge();

    @Override
    public void setExpandoBridgeAttributes(BaseModel<?> baseModel);

    @Override
    public void setExpandoBridgeAttributes(ExpandoBridge expandoBridge);

    @Override
    public void setExpandoBridgeAttributes(ServiceContext serviceContext);

    @Override
    public Object clone();

    @Override
    public int compareTo(LFTincanManifestAct lfTincanManifestAct);

    @Override
    public int hashCode();

    @Override
    public CacheModel<LFTincanManifestAct> toCacheModel();

    @Override
    public LFTincanManifestAct toEscapedModel();

    @Override
    public LFTincanManifestAct toUnescapedModel();

    @Override
    public String toString();

    @Override
    public String toXmlString();
}