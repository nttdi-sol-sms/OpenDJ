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

package org.opends.ldap;



import org.opends.ldap.responses.SearchResult;
import org.opends.ldap.responses.SearchResultEntry;
import org.opends.ldap.responses.SearchResultReference;



/**
 * A handler for consuming the results of an asynchronous search
 * operation.
 */
public interface SearchResponseHandler extends
    CompletionHandler<SearchResult>
{
  /**
   * Invoked each time a search result entry is returned from an
   * asynchronous search operation.
   *
   * @param entry
   *          The search result entry.
   */
  void handleEntry(SearchResultEntry entry);



  /**
   * Invoked each time a search result reference is returned from an
   * asynchronous search operation.
   *
   * @param reference
   *          The search result reference.
   */
  void handleReference(SearchResultReference reference);
}
