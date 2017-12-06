//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.12.06 at 03:45:12 PM EST 
//


package com.redhat.syseng.openshift.service.broker.model.amp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for application complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *         &lt;element name="created_at" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="updated_at" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="user_account_id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *         &lt;element name="end_user_required" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="service_id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *         &lt;element name="user_key" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="provider_verification_key" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="plan">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="service_id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                   &lt;element name="end_user_required" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="setup_fee" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                   &lt;element name="cost_per_month" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                   &lt;element name="trial_period_days" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="cancellation_period" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="custom" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extra_fields" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id",
    "createdAt",
    "updatedAt",
    "state",
    "userAccountId",
    "endUserRequired",
    "serviceId",
    "userKey",
    "providerVerificationKey",
    "plan",
    "name",
    "description",
    "extraFields"
})
@XmlRootElement(name = "application")
public class Application {

    protected byte id;
    @XmlElement(name = "created_at", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;
    @XmlElement(name = "updated_at", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updatedAt;
    @XmlElement(required = true)
    protected String state;
    @XmlElement(name = "user_account_id")
    protected byte userAccountId;
    @XmlElement(name = "end_user_required", required = true)
    protected String endUserRequired;
    @XmlElement(name = "service_id")
    protected byte serviceId;
    @XmlElement(name = "user_key", required = true)
    protected String userKey;
    @XmlElement(name = "provider_verification_key", required = true)
    protected String providerVerificationKey;
    @XmlElement(required = true)
    protected Application.Plan plan;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String description;
    @XmlElement(name = "extra_fields", required = true)
    protected String extraFields;

    /**
     * Gets the value of the id property.
     * 
     */
    public byte getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(byte value) {
        this.id = value;
    }

    /**
     * Gets the value of the createdAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the value of the createdAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
    }

    /**
     * Gets the value of the updatedAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the value of the updatedAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUpdatedAt(XMLGregorianCalendar value) {
        this.updatedAt = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String value) {
        this.state = value;
    }

    /**
     * Gets the value of the userAccountId property.
     * 
     */
    public byte getUserAccountId() {
        return userAccountId;
    }

    /**
     * Sets the value of the userAccountId property.
     * 
     */
    public void setUserAccountId(byte value) {
        this.userAccountId = value;
    }

    /**
     * Gets the value of the endUserRequired property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndUserRequired() {
        return endUserRequired;
    }

    /**
     * Sets the value of the endUserRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndUserRequired(String value) {
        this.endUserRequired = value;
    }

    /**
     * Gets the value of the serviceId property.
     * 
     */
    public byte getServiceId() {
        return serviceId;
    }

    /**
     * Sets the value of the serviceId property.
     * 
     */
    public void setServiceId(byte value) {
        this.serviceId = value;
    }

    /**
     * Gets the value of the userKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserKey() {
        return userKey;
    }

    /**
     * Sets the value of the userKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserKey(String value) {
        this.userKey = value;
    }

    /**
     * Gets the value of the providerVerificationKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderVerificationKey() {
        return providerVerificationKey;
    }

    /**
     * Sets the value of the providerVerificationKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderVerificationKey(String value) {
        this.providerVerificationKey = value;
    }

    /**
     * Gets the value of the plan property.
     * 
     * @return
     *     possible object is
     *     {@link Application.Plan }
     *     
     */
    public Application.Plan getPlan() {
        return plan;
    }

    /**
     * Sets the value of the plan property.
     * 
     * @param value
     *     allowed object is
     *     {@link Application.Plan }
     *     
     */
    public void setPlan(Application.Plan value) {
        this.plan = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the extraFields property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtraFields() {
        return extraFields;
    }

    /**
     * Sets the value of the extraFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtraFields(String value) {
        this.extraFields = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="service_id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *         &lt;element name="end_user_required" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="setup_fee" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *         &lt;element name="cost_per_month" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *         &lt;element name="trial_period_days" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="cancellation_period" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *       &lt;/sequence>
     *       &lt;attribute name="custom" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="default" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "id",
        "name",
        "type",
        "state",
        "serviceId",
        "endUserRequired",
        "setupFee",
        "costPerMonth",
        "trialPeriodDays",
        "cancellationPeriod"
    })
    public static class Plan {

        protected byte id;
        @XmlElement(required = true)
        protected String name;
        @XmlElement(required = true)
        protected String type;
        @XmlElement(required = true)
        protected String state;
        @XmlElement(name = "service_id")
        protected byte serviceId;
        @XmlElement(name = "end_user_required", required = true)
        protected String endUserRequired;
        @XmlElement(name = "setup_fee")
        protected float setupFee;
        @XmlElement(name = "cost_per_month")
        protected float costPerMonth;
        @XmlElement(name = "trial_period_days", required = true)
        protected String trialPeriodDays;
        @XmlElement(name = "cancellation_period")
        protected byte cancellationPeriod;
        @XmlAttribute(name = "custom")
        protected String custom;
        @XmlAttribute(name = "default")
        protected String _default;

        /**
         * Gets the value of the id property.
         * 
         */
        public byte getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         */
        public void setId(byte value) {
            this.id = value;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the state property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getState() {
            return state;
        }

        /**
         * Sets the value of the state property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setState(String value) {
            this.state = value;
        }

        /**
         * Gets the value of the serviceId property.
         * 
         */
        public byte getServiceId() {
            return serviceId;
        }

        /**
         * Sets the value of the serviceId property.
         * 
         */
        public void setServiceId(byte value) {
            this.serviceId = value;
        }

        /**
         * Gets the value of the endUserRequired property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEndUserRequired() {
            return endUserRequired;
        }

        /**
         * Sets the value of the endUserRequired property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEndUserRequired(String value) {
            this.endUserRequired = value;
        }

        /**
         * Gets the value of the setupFee property.
         * 
         */
        public float getSetupFee() {
            return setupFee;
        }

        /**
         * Sets the value of the setupFee property.
         * 
         */
        public void setSetupFee(float value) {
            this.setupFee = value;
        }

        /**
         * Gets the value of the costPerMonth property.
         * 
         */
        public float getCostPerMonth() {
            return costPerMonth;
        }

        /**
         * Sets the value of the costPerMonth property.
         * 
         */
        public void setCostPerMonth(float value) {
            this.costPerMonth = value;
        }

        /**
         * Gets the value of the trialPeriodDays property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTrialPeriodDays() {
            return trialPeriodDays;
        }

        /**
         * Sets the value of the trialPeriodDays property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTrialPeriodDays(String value) {
            this.trialPeriodDays = value;
        }

        /**
         * Gets the value of the cancellationPeriod property.
         * 
         */
        public byte getCancellationPeriod() {
            return cancellationPeriod;
        }

        /**
         * Sets the value of the cancellationPeriod property.
         * 
         */
        public void setCancellationPeriod(byte value) {
            this.cancellationPeriod = value;
        }

        /**
         * Gets the value of the custom property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCustom() {
            return custom;
        }

        /**
         * Sets the value of the custom property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCustom(String value) {
            this.custom = value;
        }

        /**
         * Gets the value of the default property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDefault() {
            return _default;
        }

        /**
         * Sets the value of the default property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDefault(String value) {
            this._default = value;
        }

    }

}