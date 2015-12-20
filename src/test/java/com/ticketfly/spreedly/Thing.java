package com.ticketfly.spreedly;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Thing {
    @XmlValue public Integer id;
    @XmlAttribute(name = "name") public String name;
}
