package com.ticketfly.spreedly.errors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "hash")
public class SpreedlyErrorHash {

	public String status;
	public String error;
}
