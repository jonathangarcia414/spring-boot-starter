package de.codecentric.cxf.autodetection;

import de.codecentric.cxf.common.BootStarterCxfException;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.sun.tools.doclets.formats.html.markup.HtmlStyle.interfaceName;


public class WebServiceAutoDetector {

    private static final Logger LOG = LoggerFactory.getLogger(WebServiceAutoDetector.class);

    public static <T> T searchAndInstantiateSeiImplementation(Class seiName) throws BootStarterCxfException {

        Class implementingClass = scanForClassWhichImplementsInterface(seiName);

        return instantiateFromClass(implementingClass);
    }

    private static Class scanForClassWhichImplementsInterface(Class interfaceName) throws BootStarterCxfException {

        try {
            // see https://github.com/lukehutch/fast-classpath-scanner/wiki/1.-Usage#mechanism-2
            FastClasspathScanner fastClasspathScanner = new FastClasspathScanner();

            ScanResult scanResult = fastClasspathScanner.scan();

            List<String> namesOfClassesWithAnnotation = scanResult.getNamesOfClassesImplementing(interfaceName);

            String className = namesOfClassesWithAnnotation.get(0);

            LOG.debug("Class found: {}", className);

            return Class.forName(className);

        } catch (ClassNotFoundException exception) {
            throw new BootStarterCxfException("WebServiceClient Class not found", exception);
        }
    }

    public static Service searchAndInstantiateWebServiceClient() throws BootStarterCxfException {

        Class webServiceClientClass = scanForClassWithAnnotation(WebServiceClient.class);
        return instantiateFromClass(webServiceClientClass);
    }

    public static Class searchServiceEndpointInterface() throws BootStarterCxfException {

        return scanForClassWithAnnotation(WebService.class);
    }


    private static Class scanForClassWithAnnotation(Class annotationName) throws BootStarterCxfException {

        try {
            // see https://github.com/lukehutch/fast-classpath-scanner/wiki/1.-Usage#mechanism-2
            FastClasspathScanner fastClasspathScanner = new FastClasspathScanner();

            ScanResult scanResult = fastClasspathScanner.scan();

            List<String> namesOfClassesWithAnnotation = scanResult.getNamesOfClassesWithAnnotation(annotationName);

            String className = namesOfClassesWithAnnotation.get(0);

            LOG.debug("Class found: {}", className);

            return Class.forName(className);

        } catch (ClassNotFoundException exception) {
            throw new BootStarterCxfException("WebServiceClient Class not found", exception);
        }

    }

    private static <T> T instantiateFromClass(Class clazz) throws BootStarterCxfException {

        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            return (T) instance;

        } catch (NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException exception) {
            throw new BootStarterCxfException("Class couldn´t be instantiated", exception);
        }


    }



}
