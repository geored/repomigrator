package org.geored.repomigrator.control;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnownConstants {

    public static Map<String, List<String>> HEADERS = new HashMap();

    public static List<String> HEADER_VALUES = new ArrayList<>();

    public final static String AUTHORIZATION = "Authorization";


    static {
        HEADER_VALUES.add("application/java-archive");
        HEADER_VALUES.add(MediaType.TEXT_HTML);
        HEADER_VALUES.add(MediaType.TEXT_PLAIN);
        HEADER_VALUES.add(MediaType.APPLICATION_XML);
        HEADER_VALUES.add(MediaType.TEXT_XML);
        HEADER_VALUES.add(MediaType.APPLICATION_ATOM_XML);
        HEADER_VALUES.add("application/zip");


        HEADERS.put("Content-Type",HEADER_VALUES);


    }
}
