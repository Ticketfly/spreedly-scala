package com.ticketfly.spreedly.errors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class SpreedlyError {
    @XmlValue public String error;
    @XmlAttribute(name = "key") public String key;
    @XmlAttribute(name = "attribute") public String attribute;
}

