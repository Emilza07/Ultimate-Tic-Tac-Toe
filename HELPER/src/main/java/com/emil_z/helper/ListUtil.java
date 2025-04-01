package com.emil_z.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Function;

public class ListUtil {

    public static <T> T getItemById(List<T> list, String idFs, Function<T, String> idExtractor) {
        return list.stream()
                .filter(item -> idFs.equals(idExtractor.apply(item)))
                .findFirst()
                .orElse(null);
    }

    //    USAGE:
    //        Method reference syntax
    //        City city = ListUtil.getItemById(cities, item.getCityId(), City::getIdFs);
    //
    //        Lambda syntax
    //        City city = ListUtil.getItemById(cities, "2", city -> city.getIdFs())
    //
    //        Full lambda syntax
    //        City city = ListUtil.getItemById(cities, "2", (City city) -> { return city.getIdFs(); })


    public static <T> int getItemPosition(List<T> list, String idFs, Function<T, String> idExtractor) {
        for (int i = 0; i < list.size(); i++) {
            if (idFs.equals(idExtractor.apply(list.get(i)))) {
                return i;
            }
        }
        return -1;  // Return -1 if item not found
    }

    //    USAGE:
    //        Method reference syntax
    //        ListUtil.getItemPosition(cities, "2", City::getIdFs)
    //
    //        Lambda syntax
    //        ListUtil.getItemPosition(cities, "2", city -> city.getIdFs())
    //
    //        Full lambda syntax
    //        ListUtil.getItemPosition(cities, "2", (City city) -> { return city.getIdFs(); })

    /*
    public static <T> List<T> addTopElement(
            List<T> list,
            Supplier<T> instanceSupplier,
            BiConsumer<T, String> idFsSetter,
            String idFsValue,
            BiConsumer<T, String> nameSetter,
            String nameValue) {

        // Create a new instance of T using the provided supplier
        T newInstance = instanceSupplier.get();

        // Set the properties using the provided setters
        idFsSetter.accept(newInstance, idFsValue);
        nameSetter.accept(newInstance, nameValue);

        // Add the new instance to the top of the list
        list.add(0, newInstance);

        return list;
    }
*/
    //    USAGE:
    //       ListUtil.addTopElement(cities,
    //                              City::new,
    //                              City::setIdFs, "0",
    //                              City::setName, "Select a city");

    public static <T> List<T> addTopElement(
            List<T> list,
            String prompt) {

        return addTopElement(list, "0", "name", prompt);
    }

    //    USAGE:
    //       ListUtil.addTopElement(cities,
    //                              "Select a city");


    public static <T> List<T> addTopElement(
            List<T> list,
            String idFs,
            String prompt) {

        return addTopElement(list, idFs, "name", prompt);
    }

    //    USAGE:
    //       ListUtil.addTopElement(cities,
    //                              "0",
    //                              "Select a city");

    public static <T> List<T> addTopElement(
            List<T> list,
            String idFs,
            String fieldName,
            String prompt) {

        try {
            // Get the type of the first element in the list (or the list's generic type if empty)
            Class<T> elementType = (Class<T>) (list.isEmpty()
                    ? ((ParameterizedType) list.getClass().getGenericSuperclass()).getActualTypeArguments()[0]
                    : list.get(0).getClass());

            // Create a new instance using the default constructor
            T newInstance = elementType.getDeclaredConstructor().newInstance();

            // Set idFs field using reflection
            setFieldValue(newInstance, "idFs", idFs);

            // Set name field using reflection
            setFieldValue(newInstance, fieldName /*"name"*/, prompt);

            // Add the new instance to the top of the list
            list.add(0, newInstance);

            return list;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new instance or set properties", e);
        }
    }

    //    USAGE:
    //       ListUtil.addTopElement(cities,
    //                              "0",
    //                              "name"
    //                              "Select a city");

    private static void setFieldValue(Object object, String fieldName, String value) {
        try {
            // Try to find the field in the current class
            Field field = findField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(object, value);
            } else {
                // If field not found, try to find and use a setter method
                String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Method setter = object.getClass().getMethod(setterName, String.class);
                setter.invoke(object, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        // Search for the field in the class hierarchy
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
}