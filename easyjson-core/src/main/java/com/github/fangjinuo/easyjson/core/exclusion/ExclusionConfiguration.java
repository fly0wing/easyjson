/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the LGPL, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.github.fangjinuo.easyjson.core.exclusion;

import com.github.fangjinuo.easyjson.core.util.FieldAttributes;
import com.github.fangjinuo.easyjson.core.util.Reflects;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ExclusionConfiguration {

    private List<Integer> modifiers = new ArrayList<Integer>(Arrays.asList(new Integer[]{Modifier.TRANSIENT, Modifier.STATIC}));
    private int _modifiers = 0;

    private boolean serializeInnerClasses = true;
    private List<Exclusion> serializationStrategies = Collections.emptyList();
    private List<Exclusion> deserializationStrategies = Collections.emptyList();


    public List<Integer> getModifiers(){
        return modifiers;
    }

    public boolean isSerializeInnerClasses() {
        return serializeInnerClasses;
    }

    public List<Exclusion> isSerializationStrategies(){
        return serializationStrategies;
    }

    public List<Exclusion> isDeserializationStrategies(){
        return deserializationStrategies;
    }


    public ExclusionConfiguration withModifiers(int... modifiers) {
        for (int modifier : modifiers) {
            this.modifiers.add(modifier);
        }
        for (int modifier : modifiers) {
            _modifiers |= modifier;
        }
        return this;
    }

    public ExclusionConfiguration disableInnerClassSerialization() {
        this.serializeInnerClasses = false;
        return this;
    }


    public ExclusionConfiguration withExclusion(Exclusion Exclusion,
                                                boolean serialization, boolean deserialization) {
        if (serialization) {
            this.serializationStrategies = new ArrayList<Exclusion>(serializationStrategies);
            this.serializationStrategies.add(Exclusion);
        }
        if (deserialization) {
            this.deserializationStrategies
                    = new ArrayList<Exclusion>(deserializationStrategies);
            this.deserializationStrategies.add(Exclusion);
        }
        return this;
    }


    public boolean isExcludedField(Field field, boolean serialize) {
        if ((_modifiers & field.getModifiers()) != 0) {
            return true;
        }
        if (field.isSynthetic()) {
            return true;
        }
        if (!serializeInnerClasses && Reflects.isInnerClass(field.getType())) {
            return true;
        }

        if (Reflects.isAnonymousOrLocal(field.getType())) {
            return true;
        }

        List<Exclusion> list = serialize ? serializationStrategies : deserializationStrategies;
        if (!list.isEmpty()) {
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            for (Exclusion Exclusion : list) {
                if (Exclusion.shouldSkipField(fieldAttributes)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean excludedClassChecks(Class<?> clazz) {
        if (!serializeInnerClasses && Reflects.isInnerClass(clazz)) {
            return true;
        }

        if (Reflects.isAnonymousOrLocal(clazz)) {
            return true;
        }

        return false;
    }

    public boolean isExcludedClass(Class<?> clazz, boolean serialize) {
        return excludedClassChecks(clazz) ||
                isExcludedClassInStrategy(clazz, serialize);
    }

    private boolean isExcludedClassInStrategy(Class<?> clazz, boolean serialize) {
        List<Exclusion> list = serialize ? serializationStrategies : deserializationStrategies;
        for (Exclusion Exclusion : list) {
            if (Exclusion.shouldSkipClass(clazz)) {
                return true;
            }
        }
        return false;
    }


}
