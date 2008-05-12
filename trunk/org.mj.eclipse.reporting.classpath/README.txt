
-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl}

-Dsun.java2d.opengl=true -Djava.util.logging.config.file=${project_loc}/etc/logging.properties -Dcom.sun.management.jmxremote -Xmx512m