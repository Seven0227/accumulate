package com.seven.accumulate.reflect;
/**
import com.google.common.base.Objects;
import org.apache.commons.collections.CollectionUtils;
import com.google.gson.Gson;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @classDesc: (比较菜品字段明细)
 * @Author:
 * @createTime: Created in 10:52 2018/8/24
 */
/**
public class CompareDifferentHelper {
    public static List<String> compare(Object arg0, Object arg1, Map<String, String> columnNameMap, String addition) throws Exception {
        //不是同一个类
        if (!arg0.getClass().isInstance(arg1)) {
            throw new RuntimeException("不是一样的类, 无法比较");
        }

        List<String> differentColumn = new ArrayList<>();
        Field[] allFields = FieldUtils.getAllFields(arg0.getClass());

        for (Field field : allFields) {

            //该列不需要比较
            if (columnNameMap.get(field.getName()) == null) {
                continue;
            }

            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), arg0.getClass());

            //得到get方法
            Method readMethod = propertyDescriptor.getReadMethod();
            Object fieldValue0 = readMethod.invoke(arg0);
            Object fieldValue1 = readMethod.invoke(arg1);

            //属性都为null
            if (fieldValue0 == null && fieldValue1 == null) {
                continue;
            }
            //属性一个为null,一个不为null
            if (fieldValue0 == null || fieldValue1 == null) {
                differentColumn.add(String.format("%s%s%s", addition, columnNameMap.get(field.getName()), "不一致"));
                continue;
            }

            if (field.getType().getClassLoader() != null) {
                //自定义类型
                differentColumn.addAll(compare(fieldValue0, fieldValue1,
                        ((Different) fieldValue0).getColumnNameMap(), ""));

            }else if (field.getType().isAssignableFrom(List.class)) {
                //List

                //得到泛型的数据类型
                ParameterizedType listGenericType = (ParameterizedType) field.getGenericType();
                Type type = listGenericType.getActualTypeArguments()[0];
                Class aClass = Class.forName(type.getTypeName());
                Object object = aClass.newInstance();

                List fieldValue0List = (List) fieldValue0;
                List fieldValue1List = (List) fieldValue1;


                //如果泛型是java类型或者没有key
                if (Class.forName(type.getTypeName()).getClassLoader() == null || !(object instanceof HaveKeyAble)) {
                    //java类型
                    if (!Objects.equal(fieldValue0, fieldValue1)) {

                        //多的
                        List subtract = (List) CollectionUtils.subtract(fieldValue0List, fieldValue1List);

                        for (Object o : subtract) {
                            differentColumn.add(String.format("%s中多余【%s】", columnNameMap.get(field.getName()), o));
                        }

                        //少的
                        subtract = (List) CollectionUtils.subtract(fieldValue1List, fieldValue0List);

                        for (Object o : subtract) {
                            differentColumn.add(String.format("%s中缺少【%s】", columnNameMap.get(field.getName()), o));
                        }

                    }
                }else {
                    //自定义类型

                    Map<String, Object> fieldValue0Map = new HashMap<>();
                    Map<String, Object> fieldValue1Map = new HashMap<>();

                    for (Object o : fieldValue0List) {
                        HaveKeyAble haveKeyAble = (HaveKeyAble) o;
                        fieldValue0Map.put(haveKeyAble.getKeyId(), o);
                    }
                    for (Object o : fieldValue1List) {
                        HaveKeyAble haveKeyAble = (HaveKeyAble) o;
                        fieldValue1Map.put(haveKeyAble.getKeyId(), o);
                    }

                    //交集
                    List intersection = (List) CollectionUtils.intersection(fieldValue0List, fieldValue1List);
                    for (Object o : intersection) {
                        differentColumn.addAll(compare(fieldValue0Map.get(((HaveKeyAble) o).getKeyId()),
                                fieldValue1Map.get(((HaveKeyAble) o).getKeyId()),
                                ((Different)fieldValue0Map.get(((HaveKeyAble) o).getKeyId())).getColumnNameMap(),
                                columnNameMap.get(field.getName()) + "中"));
                    }

                    //多的
                    List subtract = (List) CollectionUtils.subtract(fieldValue0List, fieldValue1List);

                    for (Object o : subtract) {
                        differentColumn.add(String.format("%s中多余【%s】", columnNameMap.get(field.getName()), ((HaveKeyAble) o).getOverColumnValue()));
                    }

                    //少的
                    subtract = (List) CollectionUtils.subtract(fieldValue1List, fieldValue0List);

                    for (Object o : subtract) {
                        differentColumn.add(String.format("%s中缺少【%s】", columnNameMap.get(field.getName()), ((HaveKeyAble) o).getOverColumnValue()));
                    }

                }
            }else {
                //java 类型
                if(!Objects.equal(fieldValue0, fieldValue1)) {
                    differentColumn.add(String.format("%s%s%s", addition, columnNameMap.get(field.getName()), "不一致"));
                }
            }
        }
        return differentColumn;

    }

    public static void main(String[] args) {

        try {

            String bohDishString = "{\"picUrl\":\"\",\"bohUnitList\":[{\"pkUnit\":\"4B8DF6879C794100AF33\"," +
                    "\"name\":\"份\",\"code\":\"101\",\"price\":0.22,\"stock\":0,\"maxStock\":0,\"packingFee\":0.0," +
                    "\"boxNum\":1,\"onShelf\":1},{\"pkUnit\":\"6B1BE901C2194008B6B4\",\"name\":\"条\"," +
                    "\"code\":\"102\",\"price\":3.0,\"stock\":0,\"maxStock\":0,\"packingFee\":0.0,\"boxNum\":1," +
                    "\"onShelf\":1},{\"pkUnit\":\"71D11F07F7804EC8A7DE\",\"name\":\"只\",\"code\":\"103\",\"price\":5" +
                    ".89,\"stock\":0,\"maxStock\":0,\"packingFee\":0.0,\"boxNum\":1,\"onShelf\":1}," +
                    "{\"pkUnit\":\"AAE9FC52845D440589DE\",\"name\":\"碗\",\"code\":\"105\",\"price\":12.0,\"stock\":0," +
                    "\"maxStock\":0,\"packingFee\":0.0,\"boxNum\":1,\"onShelf\":1}]," +
                    "\"bohAdditemGroupList\":[{\"bohAdditemList\":[{\"name\":\"甜100\",\"code\":\"4441\"," +
                    "\"realGroupName\":\"超长\"},{\"name\":\"甜2\",\"code\":\"4442\",\"realGroupName\":\"超长\"}," +
                    "{\"name\":\"甜3\",\"code\":\"4443\",\"realGroupName\":\"超长\"},{\"name\":\"甜4\",\"code\":\"4444\"," +
                    "\"realGroupName\":\"超长\"},{\"name\":\"甜5\",\"code\":\"4445\",\"realGroupName\":\"超长\"}," +
                    "{\"name\":\"酸1\",\"code\":\"4446\",\"realGroupName\":\"超长\"},{\"name\":\"酸2\",\"code\":\"4447\"," +
                    "\"realGroupName\":\"超长\"},{\"name\":\"酸3\",\"code\":\"4448\",\"realGroupName\":\"超长\"}]," +
                    "\"groupName\":\"超长\"}],\"baseBohLabel\":{\"isFeatured\":1,\"isGum\":0,\"isNew\":0," +
                    "\"isSpicy\":0},\"pkPubitem\":\"61EBC2C504F542248BDB\",\"name\":\"鲸鱼2\",\"code\":\"44004402\"," +
                    "\"price\":0.22,\"pkCategory\":\"5272296395d84b97a1b8803a481f95e2\",\"categoryName\":\"外卖鱼酷_鱼\"," +
                    "\"categoryCode\":\"444001\",\"categoryNo\":2,\"isSoldOut\":1,\"minOrderCount\":1,\"boxNum\":1," +
                    "\"boxPrice\":0.0,\"description\":\"没有买卖\",\"number\":11126,\"sellingTime\":\"00:00-23:59\"," +
                    "\"isPackage\":0}";
            String formatDisgString = "{\"bohUnitList\":[{\"pkUnit\":\"4B8DF6879C794100AF33\",\"name\":\"份\"," +
                    "\"code\":\"101\",\"price\":0.22,\"stock\":0,\"maxStock\":0,\"packingFee\":0.0,\"boxNum\":1," +
                    "\"onShelf\":0},{\"pkUnit\":\"6B1BE901C2194008B6B4\",\"name\":\"条\",\"code\":\"102\",\"price\":3" +
                    ".0,\"stock\":0,\"maxStock\":0,\"packingFee\":0.0,\"boxNum\":1,\"onShelf\":0}]," +
                    "\"bohAdditemGroupList\":[{\"bohAdditemList\":[{\"name\":\"甜1\",\"code\":\"4441\"}," +
                    "{\"name\":\"甜2\",\"code\":\"4442\"},{\"name\":\"甜3\",\"code\":\"4443\"},{\"name\":\"甜4\"," +
                    "\"code\":\"4444\"},{\"name\":\"甜5\",\"code\":\"4445\"},{\"name\":\"酸1\",\"code\":\"4446\"}]," +
                    "\"groupName\":\"超长\"}],\"baseBohLabel\":{\"isFeatured\":0,\"isGum\":0,\"isNew\":0," +
                    "\"isSpicy\":0},\"pkPubitem\":\"61EBC2C504F542248BDB\",\"name\":\"鲸鱼2\",\"code\":\"44004402\"," +
                    "\"pkCategory\":\"5272296395d84b97a1b8803a481f95e2\",\"categoryName\":\"外卖鱼酷_鱼\"," +
                    "\"categoryCode\":\"444001\",\"categoryNo\":2,\"isSoldOut\":0,\"minOrderCount\":1,\"boxNum\":0," +
                    "\"boxPrice\":0.0,\"description\":\"没有买卖\",\"number\":11126,\"sellingTime\":\"00:00-23:59\"," +
                    "\"isPackage\":0}";

            Gson gson = new Gson();

            BohDish bohDish = gson.fromJson(bohDishString, BohDish.class);
            BohDish formatDish = gson.fromJson(formatDisgString, BohDish.class);

            System.out.println(compare(formatDish, bohDish, formatDish.getColumnNameMap(), ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
**/