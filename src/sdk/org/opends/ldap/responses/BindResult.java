/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.ldap.responses;



import org.opends.ldap.controls.Control;
import org.opends.server.types.ByteString;
import org.opends.types.ResultCode;



/**
 * A Bind result indicates the status of the client's request for
 * authentication.
 * <p>
 * A successful Bind operation is indicated by a Bind result with a
 * result code set to {@link ResultCode#SUCCESS} and can be determined
 * by invoking the {@link #isSuccess} method.
 * <p>
 * The server SASL credentials field is used as part of a SASL-defined
 * bind mechanism to allow the client to authenticate the server to
 * which it is communicating, or to perform "challenge-response"
 * authentication. If the client bound using a form of simple
 * authentication, or the SASL mechanism does not require the server to
 * return information to the client, then this field shall not be
 * included in the Bind result.
 * <p>
 * If the server requires the client to send a new SASL Bind request in
 * order to continue the authentication process then the result code is
 * set to {@link ResultCode#SASL_BIND_IN_PROGRESS} and can be determined
 * by invoking the {@link #isSASLBindInProgress} method.
 */
public interface BindResult extends Result
{

  /**
   * {@inheritDoc}
   */
  BindResult addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  BindResult clearControls() throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  Control getControl(String oid) throws NullPointerException;



  /**
   * {@inheritDoc}
   */
  Iterable<Control> getControls();



  /**
   * {@inheritDoc}
   */
  boolean hasControls();



  /**
   * {@inheritDoc}
   */
  Control removeControl(String oid)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  BindResult addReferralURI(String uri)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  BindResult clearReferralURIs() throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  Throwable getCause();



  /**
   * {@inheritDoc}
   */
  String getDiagnosticMessage();



  /**
   * {@inheritDoc}
   */
  String getMatchedDN();



  /**
   * {@inheritDoc}
   */
  Iterable<String> getReferralURIs();



  /**
   * {@inheritDoc}
   */
  ResultCode getResultCode();



  /**
   * {@inheritDoc}
   */
  boolean hasReferralURIs();



  /**
   * {@inheritDoc}
   */
  BindResult setCause(Throwable cause)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult setDiagnosticMessage(String message)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult setMatchedDN(String dn)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  BindResult setResultCode(ResultCode resultCode)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Returns the server SASL credentials associated with this bind
   * result.
   *
   * @return The server SASL credentials, or {@code null} indicating
   *         that none was provided.
   */
  ByteString getServerSASLCredentials();



  /**
   * Sets the server SASL credentials associated with this bind result.
   *
   * @param credentials
   *          The server SASL credentials associated with this bind
   *          result, which may be {@code null} indicating that none was
   *          provided.
   * @return This bind result.
   * @throws UnsupportedOperationException
   *           If this bind result does not permit the server SASL
   *           credentials to be set.
   */
  BindResult setServerSASLCredentials(ByteString credentials)
      throws UnsupportedOperationException;



  /**
   * {@inheritDoc}
   */
  boolean isSuccess();



  /**
   * {@inheritDoc}
   */
  boolean isReferral();



  /**
   * Indicates whether or not the server requires the client to send a
   * new SASL Bind request with the same SASL mechanism in order to
   * continue the authentication process. This typically occurs during
   * multi-stage (challenge response) authentication.
   * <p>
   * Specifically, this method returns {@code true} if the result code
   * is equal to {@link ResultCode#SASL_BIND_IN_PROGRESS}.
   *
   * @return {@code true} if the server requires the client to send a
   *         new SASL Bind request, otherwise {@code false}.
   */
  boolean isSASLBindInProgress();

}