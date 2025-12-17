package aicar.utils.tween;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import aicar.utils.misc.Print;

public class Updatables {

    private static boolean allowPrint = false;

    public static boolean getAllowPrint() { return allowPrint; }
    public static void setAllowPrint(boolean allowPrint) { Updatables.allowPrint = allowPrint; }

    private static ArrayList<Updatable> list = new ArrayList<>();

    public static ArrayList<Updatable> getUpdatables() { return list; }

    public static String getUpdatablesToString(int spaces) {
        String str = "";
        for (Updatable updatable : list) 
            str += " ".repeat(spaces) + updatable + "\n";
        return str;
     }

    public static void addUpdatable(Updatable updatable) {
        boolean valid = true;
        if (updatable.getType() == Updatable.Type.CALL && !hasMethod(updatable.getTarget(), updatable.getMethodName(), updatable.getMethodArgs())) {
            valid = false;
            throw new RuntimeException("method " + updatable.getMethodName() + " does not exist in " + updatable.getTarget() + " with parameters " + getParameterTypesString(updatable.getMethodArgs()));
        }
        
        if (updatable.getType() == Updatable.Type.SET && !hasProperty(updatable.getTarget(), updatable.getPropertyName())) {
            valid = false;
            throw new RuntimeException(updatable + " does not have property named " + updatable.getPropertyName());
        }

        if (valid) {
            if (allowPrint) 
                Print.println("VALID: " + updatable, Print.GREEN);
            
            list.add(updatable);
        } else 
            Print.println("INVALID: " + updatable, Print.RED);
    }

    public static void updateUpdatables(double dt) {
        for (int i = 0; i < list.size(); i++) {
            Updatable updatable = list.get(i);

            if (updatable.shouldDelete()) {
                if (canPrintUpdatable(updatable))
                    Print.println("deleting " + updatable, Print.BLUE);
                list.remove(i);
                i--;
                if (i >= list.size())
                    return;
            }
            // update updatable
            if (!updatable.isPaused()) {
                updatable.updateTime(dt);
                updatable.update();
            }

            // print updatable
            if (canPrintUpdatable(updatable)) 
                System.out.println(updatable.getPrint() + ": " + updatable);
            
            // perform updatable loop action
            if (updatable.isLoopComplete()) 
                updatable.performOnLoopComplete();

            // loop updatable
            if (updatable.isLoopComplete() && !updatable.isComplete()) {
                if (canPrintUpdatable(updatable)) 
                    Print.println("refreshing " + updatable.getName(), Print.GREEN);
                
                updatable.loop();
            }
            
            // remove complete updatables
            if (updatable.isComplete()) {
                updatable.performOnComplete();

                if (canPrintUpdatable(updatable)) {
                    Print.println(updatable.getName() + " is complete", Print.BLUE);
                }
                list.remove(i);
                i--;
                if (i >= list.size())
                    return;
            }
        }
    }

    public static Updatable getUpdatable(String name) {
        for (Updatable updatable : list)
            if (updatable.getName().equals(name))
                return updatable;
        return null;
    }

    public static void deleteUpdatableModifyingSameObjectProperty(Updatable updatable) {
        for (int i=0; i<list.size(); i++) {
            if (list.get(i).modifyingSameObjectProperty(updatable)) {
                list.remove(i);
                i--;
            }
        }
    }

    public static void deleteUpdatables(String[] names) {
        ArrayList<Updatable> newList = new ArrayList<>();
        for (Updatable updatable : new ArrayList<>(list)) {
            boolean seenInList = false;
            for (String name : names)
                if (updatable.getName().equals(name)) {
                    seenInList = true;
                    break;
                }
            if (!seenInList)
                newList.add(updatable);
        }
        list = newList;
    }

    public static void deleteUpdatable(String name) {
        for (int i=0; i<list.size(); i++)
            if (list.get(i).getName().equals(name)) {
                list.remove(i);
                return;
            }
    }

    // removes all updatables except the one with a matching name
    public static void deleteAllUpdatablesExcept(String[] exceptions) {
        ArrayList<Updatable> newList = new ArrayList<>();
        for (Updatable updatable : new ArrayList<>(list))
            for (String exception : exceptions) 
                if (updatable.getName().equals(exception)) {
                    newList.add(updatable);
                    break;
                }
        list = newList;
    }

    private static boolean canPrintUpdatable(Updatable updatable) {
        if (!allowPrint) 
            return false;
        switch (updatable.getPrint()) {
            case ALWAYS:
                return true;
            case NEVER:
                return false;
            case ON_COMPLETE:
                return updatable.isComplete();
            case ON_LOOP:
                return updatable.isLoopComplete();
            default:
                return false;
        }
    }

    public static boolean hasProperty(Object targetObject, String propertyName) {
        Field field = null;
        Class<?> clazz = targetObject.getClass();

        // Look for the field in the class hierarchy
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(propertyName);
                break;  // Field found, break out of the loop
            } catch (NoSuchFieldException e) {
                // Field not found in this class, try the superclass
                clazz = clazz.getSuperclass();
            }
        }
        return field != null;
    }

    public static Object getProperty(Object targetObject, String propertyName) {
        try {
            Field field = null;
            Class<?> clazz = targetObject.getClass();
    
            // Look for the field in the class hierarchy
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(propertyName);
                    break;  // Field found, break out of the loop
                } catch (NoSuchFieldException e) {
                    // Field not found in this class, try the superclass
                    clazz = clazz.getSuperclass();
                }
            }
    
            if (field == null) {
                throw new NoSuchFieldException("Field " + propertyName + " not found in class hierarchy.");
            }
    
            field.setAccessible(true);  // Make the field accessible if it's private
    
            return field.get(targetObject);  // Get the value of the field
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setProperty(Object targetObject, String propertyName, Object value) {
        try {
            Field field = null;
            Class<?> clazz = targetObject.getClass();
    
            // Look for the field in the class hierarchy
            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(propertyName);
                    break;  // Field found, break out of the loop
                } catch (NoSuchFieldException e) {
                    // Field not found in this class, try the superclass
                    clazz = clazz.getSuperclass();
                }
            }
    
            if (field == null) {
                throw new NoSuchFieldException("Field " + propertyName + " not found in class hierarchy.");
            }
    
            field.setAccessible(true);  // Make the field accessible if it's private
    
            Class<?> propertyType = field.getType();  // Get the field's type
    
            // Check for primitive types and their wrappers
            if (propertyType == float.class || propertyType == Float.class) {
                field.setFloat(targetObject, ((Number) value).floatValue());  // Set float value
            } else if (propertyType == int.class || propertyType == Integer.class) {
                field.setInt(targetObject, ((Number) value).intValue());  // Set int value
            } else if (propertyType == double.class || propertyType == Double.class) {
                field.setDouble(targetObject, ((Number) value).doubleValue());  // Set double value
            } else if (propertyType == long.class || propertyType == Long.class) {
                field.setLong(targetObject, ((Number) value).longValue());  // Set long value
            } else if (propertyType == short.class || propertyType == Short.class) {
                field.setShort(targetObject, ((Number) value).shortValue());  // Set short value
            } else if (propertyType == byte.class || propertyType == Byte.class) {
                field.setByte(targetObject, ((Number) value).byteValue());  // Set byte value
            } else if (propertyType == boolean.class || propertyType == Boolean.class) {
                field.setBoolean(targetObject, (Boolean) value);  // Set boolean value
            } else if (propertyType == char.class || propertyType == Character.class) {
                field.setChar(targetObject, (Character) value);  // Set char value
            } else {
                // For other types or objects, use the general set method
                field.set(targetObject, value);  // Set the value of the field
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Class<?>[] getParameterTypes(Object...args) {

        // Determine parameter types and handle primitive type conversion
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                parameterTypes[i] = int.class;
            } else if (args[i] instanceof Double) {
                parameterTypes[i] = double.class;
            } else if (args[i] instanceof Float) {
                parameterTypes[i] = float.class;
            } else if (args[i] instanceof Long) {
                parameterTypes[i] = long.class;
            } else if (args[i] instanceof Boolean) {
                parameterTypes[i] = boolean.class;
            } else if (args[i] instanceof Character) {
                parameterTypes[i] = char.class;
            } else if (args[i] instanceof Byte) {
                parameterTypes[i] = byte.class;
            } else if (args[i] instanceof Short) {
                parameterTypes[i] = short.class;
            } else {
                // For non-primitive types, use the class of the object
                parameterTypes[i] = args[i].getClass();
            }
        }
        return parameterTypes;
    }

    public static boolean hasMethod(Object obj, String methodName, Object...args) {
        try {
            // get the parameter types
            Class<?>[] parameterTypes = getParameterTypes(args);
            // will throw an exception if there doesn't exist a method with the parameters specified
            obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            Print.println("Method not found: " + methodName + " with parameters: " + getParameterTypesString(args) + " in " + obj.getClass(), Print.RED);
            return false;
        }
    }

    public static void callMethodByName(Object obj, String methodName, Object... args) {
        try {
            // Determine parameter types and handle primitive type conversion
            Class<?>[] parameterTypes = getParameterTypes(args);
    
            // Get the method with the specified parameter types
            Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);

            // Invoke the method
            method.invoke(obj, args);
    
        } catch (NoSuchMethodException e) {
            Print.println("Method not found: " + methodName + " with parameters: " + getParameterTypesString(args) + " in " + obj.getClass(), Print.RED);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Converts the parameter types to strings (for error logging)
    public static String getParameterTypesString(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(arg.getClass().getSimpleName());
        }
        return sb.toString();
    }
    public static boolean hasUpdatable(String name) {
        for (Updatable updatable : list)
            if (updatable.getName().equals(name))
                return true;
        return false;
    }


}
