/**
 *  Copyright 2007-2011 Zuse Institute Berlin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package de.zib.scalaris;

import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangObject;

/**
 * Exception that is thrown if a the commit of a write operation on a
 * scalaris ring fails.
 *
 * @author Nico Kruber, kruber@zib.de
 * @version 2.5
 * @since 2.5
 */
public class AbortException extends OtpErlangException {
    /**
     * class version for serialisation
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with no message.
     */
    public AbortException() {
    }

    /**
     * Creates the exception with the given message.
     *
     * @param msg
     *            message of the exception
     */
    public AbortException(final String msg) {
        super(msg);
    }

    /**
     * Creates an exception taking the message of the given throwable.
     *
     * @param e the exception to "re-throw"
     */
    public AbortException(final Throwable e) {
        super(e.getMessage());
        setStackTrace(e.getStackTrace());
    }

    /**
     * Creates an exception including the message of the given erlang object.
     *
     * @param erlValue
     *            the erlang message to include
     */
    public AbortException(final OtpErlangObject erlValue) {
        super("Erlang message: " + erlValue.toString());
    }

    /**
     * Creates an exception taking the message of the given throwable.
     *
     * @param e
     *            the exception to "re-throw"
     * @param erlValue
     *            the string representation of this erlang value is included
     *            into the message
     */
    public AbortException(final Throwable e, final OtpErlangObject erlValue) {
        super(e.getMessage() + ",\n  Erlang message: " + erlValue.toString());
        setStackTrace(e.getStackTrace());
    }
}
