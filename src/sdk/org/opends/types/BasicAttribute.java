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

package org.opends.types;



import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.opends.schema.Schema;
import org.opends.server.types.ByteString;
import org.opends.util.Validator;



/**
 * Attribute implementation.
 */
final class BasicAttribute extends AbstractAttribute
{
  private static abstract class Impl
  {

    abstract boolean add(BasicAttribute attribute, ByteString value);



    boolean addAll(BasicAttribute attribute,
        Collection<? extends ByteString> values,
        Collection<? super ByteString> duplicateValues)
        throws NullPointerException
    {
      // TODO: could optimize if values is a BasicAttribute.
      ensureCapacity(attribute, values.size());
      boolean modified = false;
      for (ByteString value : values)
      {
        if (add(attribute, value))
        {
          modified = true;
        }
        else if (duplicateValues != null)
        {
          duplicateValues.add(value);
        }
      }
      resize(attribute);
      return modified;
    }



    abstract void clear(BasicAttribute attribute);



    abstract boolean contains(BasicAttribute attribute, ByteString value);



    boolean containsAll(BasicAttribute attribute, Collection<?> values)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      for (Object value : values)
      {
        if (!contains(attribute, ByteString.valueOf(value)))
        {
          return false;
        }
      }
      return true;
    }



    abstract void ensureCapacity(BasicAttribute attribute, int size);



    abstract ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException;



    abstract Iterator<ByteString> iterator(BasicAttribute attribute);



    abstract boolean remove(BasicAttribute attribute, ByteString value);



    <T> boolean removeAll(BasicAttribute attribute,
        Collection<T> values, Collection<? super T> missingValues)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      boolean modified = false;
      for (T value : values)
      {
        if (remove(attribute, ByteString.valueOf(value)))
        {
          modified = true;
        }
        else if (missingValues != null)
        {
          missingValues.add(value);
        }
      }
      return modified;
    }



    abstract void resize(BasicAttribute attribute);



    abstract <T> boolean retainAll(BasicAttribute attribute,
        Collection<T> values, Collection<? super T> missingValues);



    abstract int size(BasicAttribute attribute);
  }



  private static final class MultiValueImpl extends Impl
  {

    boolean add(BasicAttribute attribute, ByteString value)
    {
      ByteString normalizedValue = normalizeValue(attribute, value);
      if (attribute.multipleValues.put(normalizedValue, value) == null)
      {
        return true;
      }
      else
      {
        return false;
      }
    }



    void clear(BasicAttribute attribute)
    {
      attribute.multipleValues = null;
      attribute.pimpl = ZERO_VALUE_IMPL;
    }



    boolean contains(BasicAttribute attribute, ByteString value)
    {
      return attribute.multipleValues.containsKey(normalizeValue(
          attribute, value));
    }



    void ensureCapacity(BasicAttribute attribute, int size)
    {
      // Nothing to do.
    }



    ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException
    {
      return attribute.multipleValues.values().iterator().next();
    }



    Iterator<ByteString> iterator(final BasicAttribute attribute)
    {
      return new Iterator<ByteString>()
      {
        private Impl expectedImpl = MULTI_VALUE_IMPL;
        private Iterator<ByteString> iterator =
            attribute.multipleValues.values().iterator();



        public boolean hasNext()
        {
          return iterator.hasNext();
        }



        public ByteString next()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            return iterator.next();
          }
        }



        public void remove()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            iterator.remove();

            // Resize if we have removed the second to last value.
            if (attribute.multipleValues != null
                && attribute.multipleValues.size() == 1)
            {
              resize(attribute);
              iterator = attribute.pimpl.iterator(attribute);
            }

            // Always update since we may change to single or zero value
            // impl.
            expectedImpl = attribute.pimpl;
          }
        }

      };
    }



    boolean remove(BasicAttribute attribute, ByteString value)
    {
      ByteString normalizedValue = normalizeValue(attribute, value);
      if (attribute.multipleValues.remove(normalizedValue) != null)
      {
        resize(attribute);
        return true;
      }
      else
      {
        return false;
      }
    }



    void resize(BasicAttribute attribute)
    {
      // May need to resize if initial size estimate was wrong (e.g. all
      // values in added collection were the same).
      switch (attribute.multipleValues.size())
      {
      case 0:
        attribute.multipleValues = null;
        attribute.pimpl = ZERO_VALUE_IMPL;
        break;
      case 1:
        Map.Entry<ByteString, ByteString> e =
            attribute.multipleValues.entrySet().iterator().next();
        attribute.singleValue = e.getValue();
        attribute.normalizedSingleValue = e.getKey();
        attribute.multipleValues = null;
        attribute.pimpl = SINGLE_VALUE_IMPL;
        break;
      default:
        // Nothing to do.
        break;
      }
    }



    <T> boolean retainAll(BasicAttribute attribute,
        Collection<T> values, Collection<? super T> missingValues)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      if (values.isEmpty())
      {
        clear(attribute);
        return true;
      }

      Map<ByteString, T> valuesToRetain =
          new HashMap<ByteString, T>(values.size());
      for (T value : values)
      {
        valuesToRetain.put(normalizeValue(attribute, ByteString
            .valueOf(value)), value);
      }

      boolean modified = false;
      Iterator<ByteString> iterator =
          attribute.multipleValues.keySet().iterator();
      while (iterator.hasNext())
      {
        ByteString normalizedValue = iterator.next();
        if (valuesToRetain.remove(normalizedValue) == null)
        {
          modified = true;
          iterator.remove();
        }
      }

      if (missingValues != null)
      {
        missingValues.addAll(valuesToRetain.values());
      }

      resize(attribute);

      return modified;
    }



    int size(BasicAttribute attribute)
    {
      return attribute.multipleValues.size();
    }
  }



  private static final class SingleValueImpl extends Impl
  {

    boolean add(BasicAttribute attribute, ByteString value)
    {
      ByteString normalizedValue = normalizeValue(attribute, value);
      if (attribute.normalizedSingleValue().equals(normalizedValue))
      {
        return false;
      }

      attribute.multipleValues =
          new LinkedHashMap<ByteString, ByteString>(2);
      attribute.multipleValues.put(attribute.normalizedSingleValue,
          attribute.singleValue);
      attribute.multipleValues.put(normalizedValue, value);
      attribute.singleValue = null;
      attribute.normalizedSingleValue = null;
      attribute.pimpl = MULTI_VALUE_IMPL;

      return true;
    }



    void clear(BasicAttribute attribute)
    {
      attribute.singleValue = null;
      attribute.normalizedSingleValue = null;
      attribute.pimpl = ZERO_VALUE_IMPL;
    }



    boolean contains(BasicAttribute attribute, ByteString value)
    {
      ByteString normalizedValue = normalizeValue(attribute, value);
      return attribute.normalizedSingleValue().equals(normalizedValue);
    }



    void ensureCapacity(BasicAttribute attribute, int size)
    {
      if (size == 0)
      {
        return;
      }

      attribute.multipleValues =
          new LinkedHashMap<ByteString, ByteString>(1 + size);
      attribute.multipleValues.put(attribute.normalizedSingleValue,
          attribute.singleValue);
      attribute.singleValue = null;
      attribute.normalizedSingleValue = null;
      attribute.pimpl = MULTI_VALUE_IMPL;
    }



    ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException
    {
      if (attribute.singleValue != null)
      {
        return attribute.singleValue;
      }
      else
      {
        throw new NoSuchElementException();
      }
    }



    Iterator<ByteString> iterator(final BasicAttribute attribute)
    {
      return new Iterator<ByteString>()
      {
        private Impl expectedImpl = SINGLE_VALUE_IMPL;
        private boolean hasNext = true;



        public boolean hasNext()
        {
          return hasNext;
        }



        public ByteString next()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else if (hasNext)
          {
            hasNext = false;
            return attribute.singleValue;
          }
          else
          {
            throw new NoSuchElementException();
          }
        }



        public void remove()
        {
          if (attribute.pimpl != expectedImpl)
          {
            throw new ConcurrentModificationException();
          }
          else if (hasNext || attribute.singleValue == null)
          {
            throw new IllegalStateException();
          }
          else
          {
            clear(attribute);
            expectedImpl = attribute.pimpl;
          }
        }

      };
    }



    boolean remove(BasicAttribute attribute, ByteString value)
    {
      if (contains(attribute, value))
      {
        clear(attribute);
        return true;
      }
      else
      {
        return false;
      }
    }



    void resize(BasicAttribute attribute)
    {
      // Nothing to do.
    }



    <T> boolean retainAll(BasicAttribute attribute,
        Collection<T> values, Collection<? super T> missingValues)
    {
      // TODO: could optimize if objects is a BasicAttribute.
      if (values.isEmpty())
      {
        clear(attribute);
        return true;
      }

      ByteString normalizedSingleValue =
          attribute.normalizedSingleValue();
      boolean retained = false;
      for (T value : values)
      {
        ByteString normalizedValue =
            normalizeValue(attribute, ByteString.valueOf(value));
        if (normalizedSingleValue.equals(normalizedValue))
        {
          if (missingValues == null)
          {
            // We can stop now.
            return false;
          }
          retained = true;
        }
        else if (missingValues != null)
        {
          missingValues.add(value);
        }
      }

      if (!retained)
      {
        clear(attribute);
        return true;
      }
      else
      {
        return false;
      }
    }



    int size(BasicAttribute attribute)
    {
      return 1;
    }
  }



  private static final class ZeroValueImpl extends Impl
  {

    boolean add(BasicAttribute attribute, ByteString value)
    {
      attribute.singleValue = value;
      attribute.pimpl = SINGLE_VALUE_IMPL;
      return true;
    }



    void clear(BasicAttribute attribute)
    {
      // Nothing to do.
    }



    boolean contains(BasicAttribute attribute, ByteString value)
    {
      return false;
    }



    boolean containsAll(BasicAttribute attribute, Collection<?> values)
    {
      return values.isEmpty();
    }



    void ensureCapacity(BasicAttribute attribute, int size)
    {
      if (size < 2)
      {
        return;
      }

      attribute.multipleValues =
          new LinkedHashMap<ByteString, ByteString>(size);
      attribute.pimpl = MULTI_VALUE_IMPL;
    }



    ByteString firstValue(BasicAttribute attribute)
        throws NoSuchElementException
    {
      throw new NoSuchElementException();
    }



    Iterator<ByteString> iterator(final BasicAttribute attribute)
    {
      return new Iterator<ByteString>()
      {
        public boolean hasNext()
        {
          return false;
        }



        public ByteString next()
        {
          if (attribute.pimpl != ZERO_VALUE_IMPL)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            throw new NoSuchElementException();
          }
        }



        public void remove()
        {
          if (attribute.pimpl != ZERO_VALUE_IMPL)
          {
            throw new ConcurrentModificationException();
          }
          else
          {
            throw new IllegalStateException();
          }
        }

      };
    }



    boolean remove(BasicAttribute attribute, ByteString value)
    {
      return false;
    }



    void resize(BasicAttribute attribute)
    {
      // Nothing to do.
    }



    <T> boolean retainAll(BasicAttribute attribute,
        Collection<T> values, Collection<? super T> missingValues)
    {
      if (missingValues != null)
      {
        missingValues.addAll(values);
      }
      return false;
    }



    int size(BasicAttribute attribute)
    {
      return 0;
    }

  }

  private static final MultiValueImpl MULTI_VALUE_IMPL =
      new MultiValueImpl();

  private static final SingleValueImpl SINGLE_VALUE_IMPL =
      new SingleValueImpl();
  private static final ZeroValueImpl ZERO_VALUE_IMPL =
      new ZeroValueImpl();

  private final AttributeDescription attributeDescription;

  private Map<ByteString, ByteString> multipleValues = null;

  private ByteString normalizedSingleValue = null;

  private Impl pimpl = ZERO_VALUE_IMPL;

  private ByteString singleValue = null;



  /**
   * Creates a new basic attribute which is a copy of the provided
   * attribute.
   *
   * @param attribute
   *          The attribute to be copied.
   */
  BasicAttribute(Attribute attribute)
  {
    this.attributeDescription = attribute.getAttributeDescription();

    if (attribute instanceof BasicAttribute)
    {
      BasicAttribute other = (BasicAttribute) attribute;
      this.pimpl = other.pimpl;
      this.singleValue = other.singleValue;
      this.normalizedSingleValue = other.normalizedSingleValue;
      if (other.multipleValues != null)
      {
        this.multipleValues =
            new LinkedHashMap<ByteString, ByteString>(
                other.multipleValues);
      }
    }
    else
    {
      addAll(attribute);
    }
  }



  /**
   * Creates a new basic attribute having the specified attribute
   * description.
   *
   * @param name
   *          The attribute description.
   */
  BasicAttribute(AttributeDescription name)
  {
    Validator.ensureNotNull(name);
    this.attributeDescription = name;
  }



  /**
   * Creates a new basic attribute which is a copy of the provided
   * attribute value sequence.
   *
   * @param attribute
   *          The attribute value sequence to be copied.
   * @param schema
   *          The schema to use for decoding {@code attribute}.
   */
  BasicAttribute(AttributeValueSequence attribute, Schema schema)
  {
    this.attributeDescription =
        AttributeDescription.valueOf(attribute
            .getAttributeDescriptionAsString(), schema);

    this.pimpl.ensureCapacity(this, attribute.size());
    for (ByteString value : attribute)
    {
      this.pimpl.add(this, value);
    }
    this.pimpl.resize(this);
  }



  /**
   * {@inheritDoc}
   */
  public boolean add(ByteString value) throws NullPointerException
  {
    Validator.ensureNotNull(value);
    return pimpl.add(this, value);
  }



  /**
   * {@inheritDoc}
   */
  public boolean addAll(Collection<? extends ByteString> values,
      Collection<? super ByteString> duplicateValues)
      throws NullPointerException
  {
    Validator.ensureNotNull(values);
    return pimpl.addAll(this, values, duplicateValues);
  }



  /**
   * {@inheritDoc}
   */
  public void clear()
  {
    pimpl.clear(this);
  }



  /**
   * {@inheritDoc}
   */
  public boolean containsAll(Collection<?> values)
      throws NullPointerException
  {
    Validator.ensureNotNull(values);
    return pimpl.containsAll(this, values);
  }



  /**
   * {@inheritDoc}
   */
  public ByteString firstValue() throws NoSuchElementException
  {
    return pimpl.firstValue(this);
  }



  /**
   * {@inheritDoc}
   */
  public AttributeDescription getAttributeDescription()
  {
    return attributeDescription;
  }



  /**
   * {@inheritDoc}
   */
  public Iterator<ByteString> iterator()
  {
    return pimpl.iterator(this);
  }



  /**
   * {@inheritDoc}
   */
  public <T> boolean removeAll(Collection<T> values,
      Collection<? super T> missingValues) throws NullPointerException
  {
    Validator.ensureNotNull(values);
    return pimpl.removeAll(this, values, missingValues);
  }



  /**
   * {@inheritDoc}
   */
  public <T> boolean retainAll(Collection<T> values,
      Collection<? super T> missingValues) throws NullPointerException
  {
    Validator.ensureNotNull(values);
    return pimpl.retainAll(this, values, missingValues);
  }



  /**
   * {@inheritDoc}
   */
  public int size()
  {
    return pimpl.size(this);
  }



  /**
   * {@inheritDoc}
   */
  protected boolean contains(ByteString value)
      throws NullPointerException
  {
    Validator.ensureNotNull(value);
    return pimpl.contains(this, value);
  }



  /**
   * {@inheritDoc}
   */
  protected boolean remove(ByteString value)
      throws NullPointerException
  {
    Validator.ensureNotNull(value);
    return pimpl.remove(this, value);
  }



  // Lazily computes the normalized single value.
  private ByteString normalizedSingleValue()
  {
    if (normalizedSingleValue == null)
    {
      normalizedSingleValue = normalizeValue(this, singleValue);
    }
    return normalizedSingleValue;
  }

}
