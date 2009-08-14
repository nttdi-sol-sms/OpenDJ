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

package org.opends.ldap.requests;



import org.opends.ldap.controls.Control;



/**
 * The Modify DN operation allows a client to change the Relative
 * Distinguished Name (RDN) of an entry in the Directory and/or to move
 * a subtree of entries to a new location in the Directory.
 */
public interface ModifyDNRequest extends Request
{

  /**
   * {@inheritDoc}
   */
  ModifyDNRequest addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  ModifyDNRequest clearControls() throws UnsupportedOperationException;



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
   * Returns the distinguished name of the entry to be renamed. This
   * entry may or may not have subordinate entries. The server shall not
   * dereference any aliases in locating the entry to be renamed.
   *
   * @return The distinguished name of the entry.
   */
  String getName();



  /**
   * Returns the new RDN of the entry to be renamed. The value of the
   * old RDN is supplied when moving the entry to a new superior without
   * changing its RDN. Attribute values of the new RDN not matching any
   * attribute value of the entry are added to the entry, and an
   * appropriate error is returned if this fails.
   *
   * @return The new RDN of the entry.
   */
  String getNewRDN();



  /**
   * Returns the distinguished name of an existing entry that will
   * become the immediate superior (parent) of the entry to be renamed.
   * The server shall not dereference any aliases in locating the new
   * superior entry.
   *
   * @return The distinguished name of the new superior entry, or
   *         {@code null} if the entry is to remain under the same
   *         parent entry.
   */
  String getNewSuperior();



  /**
   * Indicates whether the old RDN attribute values are to be retained
   * as attributes of the entry or deleted from the entry.
   *
   * @return {@code true} if the old RDN attribute values are to be
   *         deleted from the entry, or {@code false} if they are to be
   *         retained.
   */
  boolean isDeleteOldRDN();



  /**
   * Specifies whether the old RDN attribute values are to be retained
   * as attributes of the entry or deleted from the entry.
   *
   * @param deleteOldRDN
   *          {@code true} if the old RDN attribute values are to be
   *          deleted from the entry, or {@code false} if they are to be
   *          retained.
   * @return This modify DN request.
   * @throws UnsupportedOperationException
   *           If this modify DN request does not permit the delete old
   *           RDN parameter to be set.
   */
  ModifyDNRequest setDeleteOldRDN(boolean deleteOldRDN)
      throws UnsupportedOperationException;



  /**
   * Sets the distinguished name of the entry to be renamed. This entry
   * may or may not have subordinate entries. The server shall not
   * dereference any aliases in locating the entry to be renamed.
   *
   * @param dn
   *          The distinguished name of the entry to be renamed.
   * @return This modify DN request.
   * @throws UnsupportedOperationException
   *           If this modify DN request does not permit the
   *           distinguished name to be set.
   * @throws NullPointerException
   *           If {@code dn} was {@code null}.
   */
  ModifyDNRequest setName(String dn)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the new RDN of the entry to be renamed. The value of the old
   * RDN is supplied when moving the entry to a new superior without
   * changing its RDN. Attribute values of the new RDN not matching any
   * attribute value of the entry are added to the entry, and an
   * appropriate error is returned if this fails.
   *
   * @param rdn
   *          The new RDN of the entry to be renamed.
   * @return This modify DN request.
   * @throws UnsupportedOperationException
   *           If this modify DN request does not permit the new RDN to
   *           be set.
   * @throws NullPointerException
   *           If {@code rdn} was {@code null}.
   */
  ModifyDNRequest setNewRDN(String rdn)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * Sets the distinguished name of an existing entry that will become
   * the immediate superior (parent) of the entry to be renamed. The
   * server shall not dereference any aliases in locating the new
   * superior entry.
   *
   * @param dn
   *          The distinguished name of an existing entry that will
   *          become the immediate superior (parent) of the entry to be
   *          renamed, may be {@code null}.
   * @return This modify DN request.
   * @throws UnsupportedOperationException
   *           If this modify DN request does not permit the new
   *           superior to be set.
   */
  ModifyDNRequest setNewSuperior(String dn)
      throws UnsupportedOperationException;

}