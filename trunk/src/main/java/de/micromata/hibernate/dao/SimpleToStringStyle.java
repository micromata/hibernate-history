/**
 * 
 */
package de.micromata.hibernate.dao;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author wolle
 * 
 */
public class SimpleToStringStyle extends ToStringStyle
{
  /**
   * 
   */
  private static final long serialVersionUID = 2657561698991176563L;

  private static final Transformer DESC_TRANSFORMER = new Transformer() {
    public Object transform(Object input)
    {
      if (input instanceof DescriptiveItem) {
        DescriptiveItem item = (DescriptiveItem) input;
        return item.getDescriptiveName();
      }
      return input;
    }
  };

  public static final SimpleToStringStyle INSTANCE = new SimpleToStringStyle();

  public SimpleToStringStyle()
  {
    super();
    setUseShortClassName(true);
    setDefaultFullDetail(true);
    setUseFieldNames(true);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void appendDetail(StringBuffer buffer, String fieldName, Collection coll)
  {
    super.appendDetail(buffer, fieldName, CollectionUtils.collect(coll, DESC_TRANSFORMER));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void appendDetail(StringBuffer buffer, String fieldName, Map map)
  {
    super.appendDetail(buffer, fieldName, MapUtils.transformedMap(map, DESC_TRANSFORMER, DESC_TRANSFORMER));
  }

  @Override
  protected void appendDetail(StringBuffer buffer, String fieldName, Object object)
  {
    super.appendDetail(buffer, fieldName, DESC_TRANSFORMER.transform(object));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void appendSummary(StringBuffer buffer, String fieldName, Object obj)
  {
    if (obj instanceof Map) {
      Map map = (Map) obj;
      super.appendSummary(buffer, fieldName, MapUtils.transformedMap(map, DESC_TRANSFORMER, DESC_TRANSFORMER));
    } else if (obj instanceof Collection) {
      Collection coll = (Collection) obj;
      super.appendSummary(buffer, fieldName, CollectionUtils.collect(coll, DESC_TRANSFORMER));
    } else {
      super.appendSummary(buffer, fieldName, DESC_TRANSFORMER.transform(obj));
    }
  }

}
