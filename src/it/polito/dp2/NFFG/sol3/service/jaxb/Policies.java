//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.17 at 01:35:52 PM CET 
//


package it.polito.dp2.NFFG.sol3.service.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="ReachabilityPolicy" type="{http://www.riccardopersiani.com/Schema}ReachabilityPolicyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="TraversalPolicy" type="{http://www.riccardopersiani.com/Schema}TraversalPolicyType" maxOccurs="unbounded" minOccurs="0"/>
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
    "reachabilityPolicy",
    "traversalPolicy"
})
@XmlRootElement(name = "Policies")
public class Policies {

    @XmlElement(name = "ReachabilityPolicy")
    protected List<ReachabilityPolicyType> reachabilityPolicy;
    @XmlElement(name = "TraversalPolicy")
    protected List<TraversalPolicyType> traversalPolicy;

    /**
     * Gets the value of the reachabilityPolicy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reachabilityPolicy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReachabilityPolicy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReachabilityPolicyType }
     * 
     * 
     */
    public List<ReachabilityPolicyType> getReachabilityPolicy() {
        if (reachabilityPolicy == null) {
            reachabilityPolicy = new ArrayList<ReachabilityPolicyType>();
        }
        return this.reachabilityPolicy;
    }

    /**
     * Gets the value of the traversalPolicy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the traversalPolicy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTraversalPolicy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TraversalPolicyType }
     * 
     * 
     */
    public List<TraversalPolicyType> getTraversalPolicy() {
        if (traversalPolicy == null) {
            traversalPolicy = new ArrayList<TraversalPolicyType>();
        }
        return this.traversalPolicy;
    }

}
