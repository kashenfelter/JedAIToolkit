package org.scify.jedai.datareader;

import org.scify.jedai.datamodel.Attribute;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.datareader.entityreader.EntityXMLreader;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public class TestXMLreader {
    public static void main(String[] args) {
        String filePath = "C:/Users/Manos/workspaceMars/JedAIToolkit-master/datasets/exam.xml";
        EntityXMLreader n3reader = new EntityXMLreader(filePath);
        List<EntityProfile> profiles = n3reader.getEntityProfiles();
        for (EntityProfile profile : profiles) {
            System.out.println("\n\n" + profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                System.out.print(attribute.toString());
                System.out.println();
            }
        }
        n3reader.storeSerializedObject(profiles, (filePath+"PROFILES"));
    }
}