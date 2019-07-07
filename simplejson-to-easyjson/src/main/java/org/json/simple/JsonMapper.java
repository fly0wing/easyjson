/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json.simple;

import com.github.fangjinuo.easyjson.core.JSON;
import com.github.fangjinuo.easyjson.core.JSONBuilderProvider;
import com.github.fangjinuo.easyjson.core.JsonTreeNode;
import com.github.fangjinuo.easyjson.core.node.*;

import java.util.Iterator;
import java.util.Map;

public class JsonMapper {

    public static Object fromJsonTreeNode(JsonObjectNode treeNode) {
        return JsonTreeNodes.toJSON(treeNode, new ToJSONMapper<JSONObject, JSONArray, Object, Object>() {
            @Override
            public Object mappingNull(JsonNullNode node) {
                return null;
            }

            @Override
            public Object mappingPrimitive(JsonPrimitiveNode node) {
                return null;
            }

            @Override
            public JSONArray mappingArray(JsonArrayNode node) {
                JSONArray jsonArray = new JSONArray();
                for (JsonTreeNode item : node) {
                    jsonArray.add(JsonTreeNodes.toJSON(item, this));
                }
                return jsonArray;
            }

            @Override
            public JSONObject mappingObject(JsonObjectNode node) {
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, JsonTreeNode> entry : node.propertySet()) {
                    jsonObject.put(entry.getKey(), JsonTreeNodes.toJSON(entry.getValue(), this));
                }
                return jsonObject;
            }
        });
    }

    public static JsonTreeNode toJsonTreeNode(Object object) {
        return JsonTreeNodes.fromJavaObject(object, new ToJsonTreeNodeMapper() {
            @Override
            public JsonTreeNode mapping(Object object) {
                if (object instanceof JSONArray) {
                    JsonArrayNode arrayNode = new JsonArrayNode();
                    for (Object item : (JSONArray) object) {
                        arrayNode.add(JsonTreeNodes.fromJavaObject(item, this));
                    }
                    return arrayNode;
                }

                if (object instanceof JSONObject) {
                    JsonObjectNode objectNode = new JsonObjectNode();
                    Iterator<Map.Entry<String, Object>> iter = ((JSONObject) object).entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, Object> entry = iter.next();
                        objectNode.addProperty(entry.getKey(), JsonTreeNodes.fromJavaObject(entry.getValue(), this));
                    }
                    return objectNode;
                }

                if (object instanceof JSONAware) {
                    JSON json = JSONBuilderProvider.simplest();
                    String jsonString = ((JSONAware) object).toJSONString();
                    return json.fromJson(jsonString);
                }

                return null;
            }
        });

    }
}
