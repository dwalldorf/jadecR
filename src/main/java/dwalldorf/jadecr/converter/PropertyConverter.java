/*
 jadecR
 Copyright (C) 2015  Daniel Walldorf

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package dwalldorf.jadecr.converter;

import dwalldorf.jadecr.exception.ConversionException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.ReflectionUtils;

/**
 * This converter will search for all properties of the object to be converted, and try to find a property with the same
 * name and of the same type, in the {@code destClass} object.<br /> It will ignore getters and setters and use
 * reflection, to directly set values.<br /><br />
 *
 * Objects to be converted, must have the {@link dwalldorf.jadecr.Convertible} annotation configured.
 *
 * @see dwalldorf.jadecr.Convertible
 */
public class PropertyConverter implements Converter {

  @Override
  public Object convert(Object src) throws ConversionException {
    if (!ConvertUtil.isConvertibleObject(src)) {
      return null;
    }

    try {
      Object dest = ConvertUtil.getNewDestInstance(src);
      copyValues(src, dest);

      return dest;
    } catch (Exception e) {
      throw new ConversionException(e.getMessage(), e);
    }
  }

  private void copyValues(final Object src, Object dest) throws IllegalAccessException {
    ReflectionUtils.doWithFields(src.getClass(), field -> {
      Field destField = ReflectionUtils.findField(dest.getClass(), field.getName());

      if (destField == null) {
        return;
      }
      ReflectionUtils.makeAccessible(field);
      ReflectionUtils.makeAccessible(destField);

      Object value = field.get(src);
      if (ConvertUtil.isConvertibleObject(value)) {
        value = this.convert(value);
      }

      ReflectionUtils.setField(destField, dest, value);
    });

//    Map<String, Object> srcKeyValueMap = getKeyValueMap(src);
//    Map<String, Object> destKeyValueMap = getKeyValueMap(dest);
//
//    for (Map.Entry<String, Object> entry : srcKeyValueMap.entrySet()) {
//      String propertyName = entry.getKey();
//
//      try {
//        Field field = dest.getClass().getDeclaredField(propertyName);
//        field.setAccessible(true);
//        field.set(dest, entry.getValue());
//      } catch (NoSuchFieldException ignored) {
//      }
//    }
  }

  private Map<String, Object> getKeyValueMap(final Object obj) throws IllegalAccessException {
    Map<String, Object> retVal = new HashMap<>();

    Field[] declaredFields = obj.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      retVal.put(field.getName(), field.get(obj));
    }

    return retVal;
  }

}