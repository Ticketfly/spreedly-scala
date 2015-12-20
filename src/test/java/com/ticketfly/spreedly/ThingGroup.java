package com.ticketfly.spreedly;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "thing_group")
public class ThingGroup {
    @XmlElement(name = "thing") public List<Thing> things = new ArrayList<Thing>();

    public String toString() {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < this.things.size(); i++) {
            formatted.append(this.things.get(i).name).append("\n");
        }
        return formatted.toString();
    }
}
