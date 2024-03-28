package eu.dilcis.csip.profile;

public class Constants {
    static final String EMPTY = ""; //$NON-NLS-1$
    static final String DEFAULT_URI = "http://example.com"; //$NON-NLS-1$
    static final String CARD_TERM = "Cardinality"; //$NON-NLS-1$
    static final String REQUIREMENT = "requirement"; //$NON-NLS-1$
    static final String XPATH_TERM = "METS XPath"; //$NON-NLS-1$
    static final String initSaxMess = "Couldn't initialise SAX XML Parser."; //$NON-NLS-1$
    static final String ioExcepMess = "IOException generating markdown tables."; //$NON-NLS-1$
    static final String sectIoMess = "Error opening example file for section %s."; //$NON-NLS-1$
    static final String empty = ""; //$NON-NLS-1$
    static final String space = " "; //$NON-NLS-1$
    static final String period = "."; //$NON-NLS-1$

    private Constants() {
        throw new IllegalStateException();
    }
}
