/**
 * 
 */
package eu.dilcis.csip.profile;

/**
 * @author cfw
 *
 */
public final class XmlCharBuffer {
	private StringBuilder textBuffer = null;
	/**
	 * Get the value of the text buffer
	 */
	public String getBufferValue() {
		return (this.textBuffer == null) ? null : this.textBuffer.toString();
	}

	/**
	 * Get the value of the text buffer and erase the buffer contents
	 */
	public String voidBuffer() {
		final String retVal = this.getBufferValue();
		this.textBuffer = null;
		return retVal;
	}

	/**
	 * Add text to the buffer / start a new buffer if necessary
	 */
	public void addToBuffer(final String toAdd) {
		if (this.textBuffer == null) {
			this.textBuffer = new StringBuilder(toAdd);
		} else {
			this.textBuffer.append(toAdd);
		}
	}
}
