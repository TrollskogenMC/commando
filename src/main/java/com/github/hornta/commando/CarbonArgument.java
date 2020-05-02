package com.github.hornta.commando;

import com.github.hornta.commando.completers.IArgumentHandler;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class CarbonArgument implements ICarbonArgument {
  private String name;
  private boolean isOptional;
  private boolean isTabCompletionActive;
  private CarbonArgumentType type;
  private IArgumentHandler handler;
  private double min;
  private double max;
  private int minLength;
  private int maxLength;
  private Pattern pattern;
  private Map<Class<?>, DefaultArgument> defaultValues;
  private Set<ICarbonArgument> dependencies;
  private boolean catchRemaining;
  private String permission;

  private CarbonArgument() { }

  @Override
  public double getMax() { return max; }

  public double getMin() {
    return min;
  }

  public int getMinLength() {
    return minLength;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public boolean isOptional() {
    return isOptional;
  }

  public boolean isTabCompletionActive() {
    return isTabCompletionActive;
  }

  public IArgumentHandler getHandler() {
    return handler;
  }

  public CarbonArgumentType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public DefaultArgument getDefaultValue(Class<?> senderType) {
    if (!defaultValues.containsKey(senderType)) {
      Set<Class<?>> interfaces = Util.getSuperInterfaces(senderType);
      for (Class<?> theInterface : interfaces) {
        if (defaultValues.containsKey(theInterface)) {
          defaultValues.put(senderType, defaultValues.get(theInterface));
          break;
        }
      }
    }
    return defaultValues.get(senderType);
  }

  public Pattern getPattern() {
    return pattern;
  }

  public Set<ICarbonArgument> getDependencies() {
    return dependencies;
  }

  public boolean isCatchRemaining() {
    return catchRemaining;
  }

  public String getPermission() {
    return permission;
  }

  public static class Builder {
    private final String name;
    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.POSITIVE_INFINITY;
    private int minLength = 1;
    private final int maxLength = Integer.MAX_VALUE;
    private Pattern pattern;
    private CarbonArgumentType type = CarbonArgumentType.STRING;
    private IArgumentHandler handler;
    private final Map<Class<?>, DefaultArgument> defaultValues = new HashMap<>();
    private final Set<ICarbonArgument> dependencies = new HashSet<>();
    private boolean showTabCompletion = true;
    private boolean catchRemaining = false;
    private String requiresPermission = null;

    private boolean touchedMin = false;
    private boolean touchedMax = false;
    private boolean touchedMinLength = false;
    private boolean touchedMaxLength = false;
    private boolean touchedPattern = false;
    private boolean touchedType = false;
    private boolean touchedHandler = false;
    private boolean touchedShowTabCompletion = false;
    private boolean touchedCatchRemaining = false;
    private boolean touchedRequiresPermission = false;

    public Builder(String name) {
      this.name = name;
    }

    public Builder setMinLength(int minLength) {
      if(touchedMinLength) {
        throw new Error("setMinLength() has already been called");
      }

      if(minLength < 1) {
        throw new Error("Minimum length must be at least 1");
      }

      if(minLength > maxLength) {
        throw new Error("Minimum length can't be above maximum length");
      }

      this.minLength = minLength;
      touchedMinLength = true;

      return this;
    }

    public Builder setMaxLength(int maxLength) {
      if(touchedMaxLength) {
        throw new Error("setMaxLength() has already been called");
      }

      if(maxLength < 1) {
        throw new Error("Maximum length must be at least 1");
      }

      if(maxLength < minLength) {
        throw new Error("Maximum length can't be below minimum length");
      }

      this.max = maxLength;
      touchedMaxLength = true;

      return this;
    }

    public Builder setMin(double min) {
      if(touchedMin) {
        throw new Error("setMin() has already been called");
      }

      if(min > max) {
        throw new Error("Min value can't be above max value");
      }

      this.min = min;
      touchedMin = true;

      if(!touchedType) {
        type = CarbonArgumentType.NUMBER;
      }

      return this;
    }

    public Builder setMax(double max) {
      if(touchedMax) {
        throw new Error("setMax() has already been called");
      }

      if(max < min) {
        throw new Error("Max value can't be below min value");
      }

      this.max = max;
      touchedMax = true;

      if(!touchedType) {
        type = CarbonArgumentType.NUMBER;
      }

      return this;
    }

    public Builder setType(CarbonArgumentType type) {
      if(touchedType) {
        throw new Error("setType() has already been called");
      }

      this.type = type;
      touchedType = true;
      return this;
    }

    public Builder setHandler(IArgumentHandler handler) {
      if(touchedHandler) {
        throw new Error("setHandler() has already been called");
      }

      if(this.handler != null) {
        throw new Error("An argument handler is already set");
      }

      this.handler = handler;
      touchedHandler = true;

      if(!touchedType) {
        type = CarbonArgumentType.OTHER;
      }

      return this;
    }

    public Builder setDefaultValue(Class<?> senderType, Object value) {
      if(defaultValues.containsKey(senderType)) {
        throw new Error("setDefaultValue() has already been called with the same sender type");
      }

      defaultValues.put(senderType, new DefaultArgument(value));
      return this;
    }

    public Builder setDefaultValue(Class<?> senderType, BiFunction<CommandSender, String[], Object> func) {
      if(defaultValues.containsKey(senderType)) {
        throw new Error("setDefaultValue() has already been called with the same sender type");
      }

      defaultValues.put(senderType, new DefaultArgument(func));
      return this;
    }

    public Builder setPattern(Pattern pattern) {
      if(touchedPattern) {
        throw new Error("setPattern() has already been called");
      }

      this.pattern = pattern;
      touchedPattern = true;
      return this;
    }

    public Builder dependsOn(ICarbonArgument dependency) {
      if(dependencies.contains(dependency)) {
        throw new Error("A dependency to this argument does already exist");
      }

      dependencies.add(dependency);

      return this;
    }

    public Builder showTabCompletion(boolean value) {
      if(touchedShowTabCompletion) {
        throw new Error("showTabCompletion() has already been called");
      }

      showTabCompletion = value;
      touchedShowTabCompletion = true;
      return this;
    }

    public Builder catchRemaining() {
      if(touchedCatchRemaining) {
        throw new Error("catchRemaining() has already been called");
      }

      catchRemaining = true;
      touchedCatchRemaining = true;
      return this;
    }

    public Builder requiresPermission(String permission) {
      if(touchedRequiresPermission) {
        throw new Error("requiresPermission() has already been called");
      }

      if (permission == null) {
        throw new Error("Permission cannot be null");
      }

      requiresPermission = permission;
      touchedRequiresPermission = true;
      return this;
    }

    public ICarbonArgument create() {
      if(name == null || name.isEmpty()) {
        throw new Error("name can not be null or empty");
      }

      if (type != CarbonArgumentType.STRING) {
        if (
          touchedMinLength ||
          touchedMaxLength ||
          touchedPattern
        ) {
          throw new Error("Call to either setMinLength(), setMaxLength() or setPattern() when type is not a string");
        }
      }

      if (type != CarbonArgumentType.INTEGER && type != CarbonArgumentType.NUMBER) {
        if (touchedMin || touchedMax) {
          throw new Error("Call to either setMin() or setMax() when type is not a number");
        }
      }

      if (type.isPrimitive() && touchedHandler) {
        throw new Error("A primitive type can not have a handler");
      }

      if (type.isCompletable()) {
        if (touchedHandler) {
          throw new Error("A native type can not have a handler");
        }
        try {
          handler = (IArgumentHandler) type.getCompleter().getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
          throw new Error("Something went from when instantiating type completer");
        }
      }

      CarbonArgument argument = new CarbonArgument();
      argument.name = name;
      // TODO:
      argument.isOptional = false;
      argument.isTabCompletionActive = showTabCompletion;
      argument.min = min;
      argument.max = max;
      argument.minLength = minLength;
      argument.maxLength = maxLength;
      argument.pattern = pattern;
      argument.type = type;
      argument.handler = handler;
      argument.defaultValues = defaultValues;
      argument.dependencies = dependencies;
      argument.catchRemaining = catchRemaining;
      argument.permission = requiresPermission;
      return argument;
    }
  }
}
