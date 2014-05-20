/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2006-2008 Sun Microsystems, Inc.
 *      Portions Copyright 2014 ForgeRock AS.
 */
package org.opends.server.replication.service;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class creates an output stream that can be used to export entries
 * to a synchronization domain.
 */
class ReplOutputStream extends OutputStream
{
  /** The synchronization domain on which the export is done */
  private final ReplicationDomain domain;

  /** The current number of entries exported */
  private final long numExportedEntries = 0;

  /**
   * Creates a new ReplLDIFOutputStream related to a replication
   * domain.
   *
   * @param domain The replication domain
   */
  ReplOutputStream(ReplicationDomain domain)
  {
    this.domain = domain;
  }

  /** {@inheritDoc} */
  @Override
  public void write(int i) throws IOException
  {
    throw new IOException("Invalid call");
  }

  /** {@inheritDoc} */
  @Override
  public void write(byte b[], int off, int len) throws IOException
  {
    domain.exportLDIFEntry(b, off, len);
  }

  /**
   * Return the number of exported entries.
   * @return the numExportedEntries
   */
  public long getNumExportedEntries() {
    return numExportedEntries;
  }
}
