package com.iluwatar.property.binding;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;


public class BindableProperty<T> {

  private T value;
  private HashMap<BindableProperty<T>, Function<T, T>> observers = new HashMap<>();
  private HashMap<BindableProperty<T>, Function<T, T>> observed = new HashMap<>();

  public static <T> void bindOneDirection(BindableProperty<T> first, BindableProperty<T> second,
    Function<T, T> converter) {
    if (second.observers.containsKey(first)) {
      throw new RuntimeException("Cyclic binding is not allowed");
    }
    first.addObserver(second, converter);
    second.addObserved(first, converter);
    first.update(first.value);
  }

  public BindableProperty(T value) {
    this.value = value;
  }

  public BindableProperty(BindableProperty<T> property, Function<T, T> converter) {
    Objects.requireNonNull(property, "BindableProperty cannot be null");
    Objects.requireNonNull(converter, "Converter cannot be null");
    property.addObserver(this, converter);
    this.addObserved(property, converter);
    update(converter.apply(property.getValue()));
  }

  public void update(T newValue) {
    observed.keySet().forEach(obs -> obs.removeObserved(this));
    value = newValue;
    observers.entrySet().forEach(obs -> obs.getKey().update(obs.getValue().apply(newValue)));
    observed.keySet().forEach(obs -> obs.addObserver(this, observed.get(obs)));
  }

  public T getValue() {
    return value;
  }

  private void addObserver(BindableProperty<T> prop, Function<T, T> converter) {
    observers.put(prop, converter);
  }

  private void removeObserved(BindableProperty<T> prop) {
    observed.remove(prop);
  }

  private void addObserved(BindableProperty<T> prop, Function<T, T> converter) {
    observed.put(prop, converter);
  }

}
