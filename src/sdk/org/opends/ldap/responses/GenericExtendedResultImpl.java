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



import org.opends.server.types.ByteString;
import org.opends.spi.AbstractExtendedResult;
import org.opends.types.ResultCode;



/**
 * Generic extended result implementation.
 */
final class GenericExtendedResultImpl extends
    AbstractExtendedResult<GenericExtendedResult> implements
    GenericExtendedResult
{
  private ByteString responseValue = null;



  /**
   * Creates a new generic extended result using the provided result
   * code.
   *
   * @param resultCode
   *          The result code.
   * @throws NullPointerException
   *           If {@code resultCode} was {@code null}.
   */
  GenericExtendedResultImpl(ResultCode resultCode)
      throws NullPointerException
  {
    super(resultCode);
  }



  /**
   * {@inheritDoc}
   */
  public ByteString getResponseValue()
  {
    return responseValue;
  }



  /**
   * {@inheritDoc}
   */
  public GenericExtendedResult setResponseValue(ByteString bytes)
  {
    this.responseValue = bytes;
    return this;
  }

}
