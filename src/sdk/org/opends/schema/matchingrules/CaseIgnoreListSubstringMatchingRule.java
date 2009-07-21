package org.opends.schema.matchingrules;

import org.opends.schema.SchemaUtils;
import static org.opends.server.schema.SchemaConstants.SMR_CASE_IGNORE_LIST_NAME;
import static org.opends.server.schema.SchemaConstants.SMR_CASE_IGNORE_LIST_OID;
import static org.opends.server.schema.SchemaConstants.SYNTAX_SUBSTRING_ASSERTION_OID;
import static org.opends.schema.StringPrepProfile.prepareUnicode;
import static org.opends.schema.StringPrepProfile.CASE_FOLD;
import org.opends.server.schema.StringPrepProfile;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.server.util.ServerConstants;

import java.util.Collections;

/**
 * This class implements the caseIgnoreListSubstringsMatch matching rule defined
 * in X.520 and referenced in RFC 2252.
 */
public class CaseIgnoreListSubstringMatchingRule
    extends SubstringMatchingRuleImplementation
{
  public CaseIgnoreListSubstringMatchingRule()
  {
    super(SMR_CASE_IGNORE_LIST_OID,
        Collections.singletonList(SMR_CASE_IGNORE_LIST_NAME),
        "",
        false,
        SYNTAX_SUBSTRING_ASSERTION_OID,
        SchemaUtils.RFC4512_ORIGIN);
  }

  public ByteSequence normalizeAttributeValue(ByteSequence value) {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, StringPrepProfile.TRIM, CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return ServerConstants.SINGLE_SPACE_VALUE;
      }
      else
      {
        // The value is empty, so it is already normalized.
        return ByteString.empty();
      }
    }


    // Replace any consecutive spaces with a single space.  Any spaces around a
    // dollar sign will also be removed.
    for (int pos = bufferLength-1; pos > 0; pos--)
    {
      if (buffer.charAt(pos) == ' ')
      {
        char c = buffer.charAt(pos-1);
        if (c == ' ')
        {
          buffer.delete(pos, pos+1);
        }
        else if (c == '$')
        {
          if ((pos <= 1) || (buffer.charAt(pos-2) != '\\'))
          {
            buffer.delete(pos, pos+1);
          }
        }
        else if (buffer.charAt(pos+1) == '$')
        {
          buffer.delete(pos, pos+1);
        }
      }
    }

    return ByteString.valueOf(buffer.toString());
  }

  @Override
  public ByteSequence normalizeSubInitialValue(ByteSequence value) {
    return normalize(value);
  }

  @Override
  public ByteSequence normalizeSubAnyValue(ByteSequence value) {
    return normalize(value);
  }

  @Override
  public ByteSequence normalizeSubFinalValue(ByteSequence value) {
    return normalize(value);
  }

  private ByteSequence normalize(ByteSequence value)
  {
    // In this case, the process for normalizing a substring is the same as
    // normalizing a full value with the exception that it may include an
    // opening or trailing space.
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, false, CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return ServerConstants.SINGLE_SPACE_VALUE;
      }
      else
      {
        // The value is empty, so it is already normalized.
        return value.toByteString();
      }
    }


    // Replace any consecutive spaces with a single space.
    for (int pos = bufferLength-1; pos > 0; pos--)
    {
      if (buffer.charAt(pos) == ' ')
      {
        if (buffer.charAt(pos-1) == ' ')
        {
          buffer.delete(pos, pos+1);
        }
      }
    }

    return ByteString.valueOf(buffer.toString());
  }
}
