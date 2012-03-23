package org.karatachi.portus.redirect;

import java.io.File;

import org.karatachi.system.ClassPropertyLoader;

public class AssemblyInfo extends org.karatachi.portus.core.AssemblyInfo {
    static {
        ClassPropertyLoader loader = new ClassPropertyLoader();
        loader.loadIfNotContains(new File("/portus/portus.properties"));
        loader.loadIfNotContains(AssemblyInfo.class,
                "/portus-redirection.properties");
        loader.setClassProperties(AssemblyInfo.class);
    }
}
