package edu.udel.cis.vsl.civl.err;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;

/**
 * Root of CIVL exception hierarchy, representing any kind of event where
 * something goes wrong in CIVL.
 * 
 * @author siegel
 * 
 */
public class CIVLException extends RuntimeException {

	/**
	 * Generated by Eclipse.
	 */
	private static final long serialVersionUID = -5218392349059280169L;

	/**
	 * Source of the element that led to exception, for error reporting. May be
	 * null.
	 */
	private CIVLSource source;

	public CIVLException(String message, CIVLSource source) {
		super(message);
		assert message != null;
		this.source = source;
	}

	@Override
	public String toString() {
		String result = getMessage();

		if (source != null)
			result += "\nat " + source.getSummary() + ".";
		return result;
	}

	public CIVLSource getSource() {
		return source;
	}

}
