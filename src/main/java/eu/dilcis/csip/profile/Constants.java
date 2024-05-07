package eu.dilcis.csip.profile;

public class Constants {
    static final String EMPTY = ""; //$NON-NLS-1$
    static final String DEFAULT_URI = "http://example.com"; //$NON-NLS-1$
    static final String CARD_TERM = "Cardinality"; //$NON-NLS-1$
    static final String XPATH_TERM = "METS XPath"; //$NON-NLS-1$
    static final String INIT_SAX_MESS = "Couldn't initialise SAX XML Parser."; //$NON-NLS-1$
    static final String MESS_IO_EXCEP = "IOException generating markdown tables."; //$NON-NLS-1$
    static final String SECTION_MESS = "Error opening example file for section %s."; //$NON-NLS-1$
    static final String MESS_NULL_PARAM = "Parameter %s (%s) can not be null."; //$NON-NLS-1$
    static final String SPACE = " "; //$NON-NLS-1$
    static final String PERIOD = "."; //$NON-NLS-1$

    private Constants() {
        throw new IllegalStateException();
    }
}
