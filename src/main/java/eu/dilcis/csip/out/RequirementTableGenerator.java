/**
 * 
 */
package eu.dilcis.csip.out;

import java.io.IOException;
import java.util.List;

import eu.dilcis.csip.profile.Requirement;

/**
 * @author cfw
 *
 */
public interface RequirementTableGenerator {

	/**
	 * 
	 * @return
	 */
	public List<String> getHeadings();
	
	/**
	 * 
	 * @param requirement
	 * @return
	 */
	public boolean add(Requirement requirement);
	
	/**
	 * 
	 * @return
	 */
	public int size();
	
	/**
	 * 
	 * @param handler
	 * @throws IOException
	 */
	public void toTable(OutputHandler handler) throws IOException;

	/**
	 * 
	 * @param handler
	 * @param addHeader
	 * @throws IOException
	 */
	public void toTable(OutputHandler handler, boolean addHeader) throws IOException;
}
