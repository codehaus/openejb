/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util;

/**
 * A class for HTML utilities; see 
 * <a href="http://www.w3.org/TR/html4" target="_blank">http://www.w3.org/TR/html4</a>
 * for more info
 * 
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class HtmlUtilities {
	/** type for &lt;a name="name"...&gt; */
	public static final String ANCHOR_NAME_TYPE = "name";
	/** type for &lt;a href="href"...&gt; */
	public static final String ANCHOR_HREF_TYPE = "href";

	//we don't want anyone creating new instances of this class
	private HtmlUtilities() {}

	/**
	 * Creates an HTML anchor.  If <code>ANCHOR_NAME_TYPE</code> is passed in, 
	 * it creates a name anchor:<br><br>
	 * <code>
	 * &lt;a name="value"&gt;display&lt;/a&gt;
	 * </code><br><br>
	 * If <code>ANCHOR_HREF_TYPE</code> is passed in it creates an href anchor:
	 * <br><br>
	 * <code>
	 * &lt;a href="href"&gt;display&lt;/a&gt;
	 * </code>
	 * 
	 * @see org.openejb.util.HtmlUtilities#ANCHOR_NAME_TYPE
	 * @see org.openejb.util.HtmlUtilities#ANCHOR_HREF_TYPE
	 * @param value the name or href value for this anchor
	 * @param display the display for this anchor
	 * @param type the type of this anchor (name or href)
	 * @return an HTML anchor element
	 */
	public static String createAnchor(String value, String display, String type) {
		//our type must be one of these two
		if (!(ANCHOR_HREF_TYPE.equals(type) || ANCHOR_NAME_TYPE.equals(type))) {
			throw new IllegalArgumentException("The type argument must be either \"name\" or \"href\"");
		}

		return new StringBuffer(100)
			.append("<a ")
			.append(type)
			.append("=\"")
			.append(value)
			.append("\">")
			.append(display)
			.append("</a>")
			.toString();
	}

	/**
	 * Creates the beginning of an HTML select based on the name passed in
	 * <br>
	 * <code>
	 * &lt;select name="name" onChange="onChange"&gt;
	 * </code>
	 * <br>
	 * @param name the name of the select form field
	 * @param onChange a JavaScript onChange event (pass in null for no onChange) 
	 * @return the constructed select, similar to above
	 */
	public static String createSelectFormField(String name, String onChange) {
		StringBuffer temp = new StringBuffer(60).append("<select name=\"").append(name);

		if (onChange != null) {
			temp.append("\" onChange=\"").append(onChange);
		}

		return temp.append("\">").toString();
	}

	/**
	 * Creates an HTML option used inside an HTML select
	 * <br>
	 * <code>
	 * &lt;option value="value" selected&gt;display&lt;/option&gt;
	 * </code>
	 * <br>
	 * @param value the value for this option
	 * @param display the display for this option
	 * @param selected whether or not this option should be selected
	 * @return the constructed option, similar to above
	 */
	public static String createSelectOption(String value, String display, boolean selected) {
		StringBuffer temp = new StringBuffer(65).append("<option value=\"").append(value).append("\"");

		if (selected) {
			temp.append(" selected");
		}

		return temp.append(">").append(display).append("</option>").toString();
	}

	/**
	 * Creates an HTML text form field based on the parameters passed in
	 * <br>
	 * <code>
	 * &lt;input type="text" name="name" value="value" size="size" maxlength="maxlength"&gt;
	 * </code>
	 * <br>
	 * @param name the name of the text form field
	 * @param value the value of the text form field
	 * @param size the size of the text form field (0 for no size)
	 * @param maxLength the maxlength of the text form field  (0 for no maxlength)
	 * @return the constructed text form field, similar to above
	 */
	public static String createTextFormField(String name, String value, int size, int maxLength) {
		return createInputFormField("text", name, value, size, maxLength, null, null, null, null, false, false, false);
	}
	
	/**
	 * Creates an HTML file form field based on the parameters passed in
	 * <br>
	 * <code>
	 * &lt;input type="file" name="name" value="value" size="size"&gt;
	 * </code>
	 * <br>
	 * @param name the name of the file form field
	 * @param value the value of the file form field
	 * @param size the size of the file form field (0 for no size)
	 * @return the constructed file form field, similar to above
	 */
	public static String createFileFormField(String name, String value, int size) {
		return createInputFormField("file", name, value, size, 0, null, null, null, null, false, false, false);
	}

	/**
	 * Creates an HTML hidden form field based on the parameters passed in
	 * <br>
	 * <code>
	 * &lt;input type="hidden" name="name" value="value"&gt;
	 * </code>
	 * <br>
	 * @param name the name of hidden form field
	 * @param value the value of the hidden form field
	 * @return the constructed hidden form field, similar to above
	 */
	public static String createHiddenFormField(String name, String value) {
		return createInputFormField("hidden", name, value, 0, 0, null, null, null, null, false, false, false);
	}

	/**
	 * Creates an HTML submit button based on the parameters passed in
	 * <br>
	 * <code>
	 * &lt;input type="submit" name="name" value="value"&gt;
	 * </code>
	 * <br>
	 * @param name the name of hidden form field
	 * @param value the value of the hidden form field
	 * @return the constructed hidden form field, similar to above
	 */
	public static String createSubmitFormButton(String name, String value) {
		return createInputFormField("submit", name, value, 0, 0, null, null, null, null, false, false, false);
	}

	/** creates an input type, text, radio, button submit, etc
	 * 
	 * @param type the type of input
	 * @param name the name of the input
	 * @param value the value of the input
	 * @param size the size of the input (0 for no size)
	 * @param maxLength the maxlength of the input (0 for no maxlength
	 * @param onFocus an onfocus event (null for no onfocus)
	 * @param onBlur an onblur event (null for no onblur)
	 * @param onChange an onchange event (null for no onchange)
	 * @param onClick an onclick event (null for no onclick)
	 * @param checked if this input is checked
	 * @param disabled if this input is disabled
	 * @param readOnly if this input is readonly
	 * @return the constructed input
	 */
	public static String createInputFormField(
		String type,
		String name,
		String value,
		int size,
		int maxLength,
		String onFocus,
		String onBlur,
		String onChange,
		String onClick,
		boolean checked,
		boolean disabled,
		boolean readOnly) {

		StringBuffer temp =
			new StringBuffer(150)
				.append("<input type=\"")
				.append(type)
				.append("\" name=\"")
				.append(name)
				.append("\" value=\"")
				.append(value)
				.append("\"");

		if (size > 0) {
			temp.append(" size=\"").append(size).append("\"");
		}
		if (maxLength > 0) {
			temp.append(" maxlength=\"").append(maxLength).append("\"");
		}
		if (onFocus != null) {
			temp.append(" onfocus=\"").append(onFocus).append("\"");
		}
		if (onBlur != null) {
			temp.append(" onblur=\"").append(onBlur).append("\"");
		}
		if (onChange != null) {
			temp.append(" onchange=\"").append(onChange).append("\"");
		}
		if (onClick != null) {
			temp.append(" onclick=\"").append(onClick).append("\"");
		}
		if (checked) {
			temp.append(" checked");
		}
		if (disabled) {
			temp.append(" disabled");
		}
		if (readOnly) {
			temp.append(" readonly");
		}

		return temp.append(">").toString();
	}

	/**
	 * Creates an HTML textarea object<br>
	 * <code>
	 * &lt;textarea name="name" rows="rows" cols="columns" onfocus="onFocus" onblur="onBlur" onchange="onChange"&gt;<br>
	 * content<br>
	 * &lt;/textarea&gt;
	 * </code>
	 * 
	 * @param name the name of the textarea
	 * @param content the content of the textarea
	 * @param rows the number of rows of the textarea
	 * @param columns the numbe of columns of the textarea
	 * @param onFocus a javascript onfocus event (null for no onfocus)
	 * @param onBlur a javascript onblur event (null for no onblur)
	 * @param onChange a javascript onchange event (null for no onchange)
	 * @return the constucted textarea string
	 */
	public static String createTextArea(
		String name,
		String content,
		int rows,
		int columns,
		String onFocus,
		String onBlur,
		String onChange) {
		StringBuffer temp = new StringBuffer(50);
		temp.append("<textarea name=\"").append(name).append("\" rows=\"").append(rows).append("\" cols=\"").append(
			columns).append(
			"\"");

		if (onFocus != null) {
			temp.append(" onfocus=\"").append(onFocus).append("\"");
		}
		if (onBlur != null) {
			temp.append(" onblur=\"").append(onBlur).append("\"");
		}
		if (onChange != null) {
			temp.append(" onchange=\"").append(onChange).append("\"");
		}

		return temp.append(">").append(content).append("</textarea>").toString();
	}
}
