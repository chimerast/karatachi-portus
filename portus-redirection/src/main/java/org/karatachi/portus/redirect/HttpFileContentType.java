package org.karatachi.portus.redirect;

import java.util.HashMap;
import java.util.Map;

public class HttpFileContentType {
    private static Map<String, String> contentType;

    public static String getContentType(String filename) {
        int dot = filename.lastIndexOf(".");
        if (dot > 0) {
            String ext = filename.substring(dot + 1).toLowerCase();
            String ret = contentType.get(ext);
            if (ret != null) {
                return ret;
            }
        }
        return "application/octet-stream";
    }

    static {
        contentType = new HashMap<String, String>();
        contentType.put("abs", "audio/x-mpeg");
        contentType.put("ai", "application/postscript");
        contentType.put("aif", "audio/x-aiff");
        contentType.put("aifc", "audio/x-aiff");
        contentType.put("aiff", "audio/x-aiff");
        contentType.put("aim", "application/x-aim");
        contentType.put("air",
                "application/vnd.adobe.air-application-installer-package+zip");
        contentType.put("art", "image/x-jg");
        contentType.put("asf", "video/x-ms-asf");
        contentType.put("asx", "video/x-ms-asf");
        contentType.put("au", "audio/basic");
        contentType.put("avi", "video/x-msvideo");
        contentType.put("avx", "video/x-rad-screenplay");
        contentType.put("bcpio", "application/x-bcpio");
        contentType.put("bin", "application/octet-stream");
        contentType.put("bmp", "image/bmp");
        contentType.put("body", "text/html");
        contentType.put("cdf", "application/x-netcdf");
        contentType.put("cer", "application/x-x509-ca-cert");
        contentType.put("class", "application/java");
        contentType.put("cpio", "application/x-cpio");
        contentType.put("csh", "application/x-csh");
        contentType.put("css", "text/css");
        contentType.put("dib", "image/bmp");
        contentType.put("doc", "application/msword");
        contentType.put("dtd", "application/xml-dtd");
        contentType.put("dv", "video/x-dv");
        contentType.put("dvi", "application/x-dvi");
        contentType.put("eps", "application/postscript");
        contentType.put("etx", "text/x-setext");
        contentType.put("exe", "application/octet-stream");
        contentType.put("floq",
                "application/vnd.sony.floq-application-package+zip");
        contentType.put("gif", "image/gif");
        contentType.put("gtar", "application/x-gtar");
        contentType.put("gz", "application/x-gzip");
        contentType.put("hdf", "application/x-hdf");
        contentType.put("htc", "text/x-component");
        contentType.put("htm", "text/html");
        contentType.put("html", "text/html");
        contentType.put("hqx", "application/mac-binhex40");
        contentType.put("ico", "image/x-icon");
        contentType.put("ief", "image/ief");
        contentType.put("jad", "text/vnd.sun.j2me.app-descriptor");
        contentType.put("jar", "application/java-archive");
        contentType.put("java", "text/plain");
        contentType.put("jnlp", "application/x-java-jnlp-file");
        contentType.put("jpe", "image/jpeg");
        contentType.put("jpeg", "image/jpeg");
        contentType.put("jpg", "image/jpeg");
        contentType.put("js", "text/javascript");
        contentType.put("jsf", "text/plain");
        contentType.put("jspf", "text/plain");
        contentType.put("kar", "audio/midi");
        contentType.put("latex", "application/x-latex");
        contentType.put("m3u", "audio/x-mpegurl");
        contentType.put("mac", "image/x-macpaint");
        contentType.put("man", "application/x-troff-man");
        contentType.put("mathml", "application/mathml+xml");
        contentType.put("me", "application/x-troff-me");
        contentType.put("mid", "audio/midi");
        contentType.put("midi", "audio/midi");
        contentType.put("mif", "application/vnd.mif");
        contentType.put("mov", "video/quicktime");
        contentType.put("movie", "video/x-sgi-movie");
        contentType.put("mp1", "audio/x-mpeg");
        contentType.put("mp2", "audio/mpeg");
        contentType.put("mp3", "audio/mpeg");
        contentType.put("mp4", "video/mp4");
        contentType.put("mpa", "audio/x-mpeg");
        contentType.put("mpe", "video/mpeg");
        contentType.put("mpeg", "video/mpeg");
        contentType.put("mpega", "audio/x-mpeg");
        contentType.put("mpg", "video/mpeg");
        contentType.put("mpv2", "video/mpeg2");
        contentType.put("ms", "application/x-troff-ms");
        contentType.put("nc", "application/x-netcdf");
        contentType.put("oda", "application/oda");
        contentType.put("odb", "application/vnd.oasis.opendocument.database");
        contentType.put("odc", "application/vnd.oasis.opendocument.chart");
        contentType.put("odf", "application/vnd.oasis.opendocument.formula");
        contentType.put("odg", "application/vnd.oasis.opendocument.graphics");
        contentType.put("odi", "application/vnd.oasis.opendocument.image");
        contentType.put("odm", "application/vnd.oasis.opendocument.text-master");
        contentType.put("odp",
                "application/vnd.oasis.opendocument.presentation");
        contentType.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        contentType.put("odt", "application/vnd.oasis.opendocument.text");
        contentType.put("ogg", "application/ogg");
        contentType.put("otg",
                "application/vnd.oasis.opendocument.graphics-template");
        contentType.put("oth", "application/vnd.oasis.opendocument.text-web");
        contentType.put("otp",
                "application/vnd.oasis.opendocument.presentation-template");
        contentType.put("ots",
                "application/vnd.oasis.opendocument.spreadsheet-template");
        contentType.put("ott",
                "application/vnd.oasis.opendocument.text-template");
        contentType.put("pbm", "image/x-portable-bitmap");
        contentType.put("pct", "image/pict");
        contentType.put("pdf", "application/pdf");
        contentType.put("pgm", "image/x-portable-graymap");
        contentType.put("pic", "image/pict");
        contentType.put("pict", "image/pict");
        contentType.put("pls", "audio/x-scpls");
        contentType.put("png", "image/png");
        contentType.put("pnm", "image/x-portable-anymap");
        contentType.put("pnt", "image/x-macpaint");
        contentType.put("ppm", "image/x-portable-pixmap");
        contentType.put("pps", "application/vnd.ms-powerpoint");
        contentType.put("ppt", "application/vnd.ms-powerpoint");
        contentType.put("ps", "application/postscript");
        contentType.put("psd", "image/x-photoshop");
        contentType.put("qt", "video/quicktime");
        contentType.put("qti", "image/x-quicktime");
        contentType.put("qtif", "image/x-quicktime");
        contentType.put("ras", "image/x-cmu-raster");
        contentType.put("rdf", "application/rdf+xml");
        contentType.put("rgb", "image/x-rgb");
        contentType.put("rm", "application/vnd.rn-realmedia");
        contentType.put("roff", "application/x-troff");
        contentType.put("rtf", "text/rtf");
        contentType.put("rtx", "text/richtext");
        contentType.put("sh", "application/x-sh");
        contentType.put("shar", "application/x-shar");
        contentType.put("smf", "audio/x-midi");
        contentType.put("sit", "application/x-stuffit");
        contentType.put("snd", "audio/basic");
        contentType.put("src", "application/x-wais-source");
        contentType.put("sv4cpio", "application/x-sv4cpio");
        contentType.put("sv4crc", "application/x-sv4crc");
        contentType.put("svg", "image/svg+xml");
        contentType.put("svgz", "image/svg");
        contentType.put("swf", "application/x-shockwave-flash");
        contentType.put("t", "application/x-troff");
        contentType.put("tar", "application/x-tar");
        contentType.put("tcl", "application/x-tcl");
        contentType.put("tex", "application/x-tex");
        contentType.put("texi", "application/x-texinfo");
        contentType.put("texinfo", "application/x-texinfo");
        contentType.put("tif", "image/tiff");
        contentType.put("tiff", "image/tiff");
        contentType.put("tr", "application/x-troff");
        contentType.put("tsv", "text/tab-separated-values");
        contentType.put("txt", "text/plain");
        contentType.put("ulw", "audio/basic");
        contentType.put("ustar", "application/x-ustar");
        contentType.put("vrml", "model/vrml");
        contentType.put("vsd", "application/x-visio");
        contentType.put("vxml", "application/voicexml+xml");
        contentType.put("wav", "audio/x-wav");
        contentType.put("wbmp", "image/vnd.wap.wbmp");
        contentType.put("wml", "text/vnd.wap.wml");
        contentType.put("wmlc", "application/vnd.wap.wmlc");
        contentType.put("wmls", "text/vnd.wap.wmlscript");
        contentType.put("wmlscriptc", "application/vnd.wap.wmlscriptc");
        contentType.put("wrl", "model/vrml");
        contentType.put("xbm", "image/x-xbitmap");
        contentType.put("xht", "application/xhtml+xml");
        contentType.put("xhtml", "application/xhtml+xml");
        contentType.put("xls", "application/vnd.ms-excel");
        contentType.put("xml", "application/xml");
        contentType.put("xpm", "image/x-xpixmap");
        contentType.put("xsl", "application/xml");
        contentType.put("xslt", "application/xslt+xml");
        contentType.put("xul", "application/vnd.mozilla.xul+xml");
        contentType.put("xwd", "image/x-xwindowdump");
        contentType.put("Z", "application/x-compress");
        contentType.put("z", "application/x-compress");
        contentType.put("wax", "audio/x-ms-wax");
        contentType.put("wma", "audio/x-ms-wma");
        contentType.put("asf", "video/x-ms-asf");
        contentType.put("afd", "video/x-ms-afs");
        contentType.put("wvx", "video/x-ms-wvx");
        contentType.put("wmv", "video/x-ms-wmv");
        contentType.put("wma", "video/x-ms-wma");
        contentType.put("wm", "video/x-ms-wm");
    }
}
